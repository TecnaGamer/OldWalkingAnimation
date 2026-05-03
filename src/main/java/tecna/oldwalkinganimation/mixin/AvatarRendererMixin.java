package tecna.oldwalkinganimation.mixin;

//? if >=26.1 && neoforge {
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;
import static tecna.oldwalkinganimation.config.Config.bounce;
import static tecna.oldwalkinganimation.config.Config.enableMod;
import static tecna.oldwalkinganimation.config.Config.enableMobs;

import java.util.Map;
import java.util.WeakHashMap;

// 26.1 moved cape rotation off Player's xCloak/yCloak/zCloak fields and onto
// AvatarRenderState as three pre-baked floats (capeFlap = lift, capeLean = forward
// projection, capeLean2 = side roll). AvatarRenderer.extractCapeState writes them each
// frame from the entity's ClientAvatarState cloak position vs current Y. We hook the tail
// of that method and add a bounce-driven contribution to state.capeFlap, mirroring what
// CapeFeatureRendererMixin does on <1.21.2 yarn (and CapeLayerMixin on <1.21.2 mojmap):
// spring-mass-damper sim with asymmetric damping for the "rises slow, snaps down"
// asymmetry the user wanted.
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Unique private static final Map<LivingEntity, double[]> owa$capeState = new WeakHashMap<>();

    @Inject(method = "extractCapeState", at = @At("TAIL"))
    private void owa$applyBounceToCapeFlap(Avatar entity, AvatarRenderState state,
                                           float partialTicks, CallbackInfo ci) {
        if (!enableMod || !bounce) return;
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (entity.isCrouching()) return;

        float var10 = SharedValueUtil.getVar10(living);
        double bodyY = -var10 * 0.0625;

        long now = System.nanoTime();
        double[] s = owa$capeState.get(living);
        if (s == null) {
            s = new double[]{ bodyY, 0.0, now };
            owa$capeState.put(living, s);
        }
        double capeY = s[0];
        double capeVel = s[1];
        long lastNs = (long) s[2];
        double dt = (now - lastNs) / 1e9;
        if (dt <= 0.0 || dt > 0.1) dt = 0.0;
        dt *= OwaTimeScale.scaleFactor();

        double spring = 50.0;
        double damping = 9.9;
        double dampingCoeff = capeVel > 0 ? damping * 3.0 : damping * 0.5;
        double effectiveSpring = capeY > bodyY ? spring * 3.0 : spring;
        double accel = (bodyY - capeY) * effectiveSpring - capeVel * dampingCoeff;
        capeVel += accel * dt;
        capeY += capeVel * dt;
        s[0] = capeY;
        s[1] = capeVel;
        s[2] = now;

        double lag = bodyY - capeY;
        double scale = lag > 0 ? 80.0 : 50.0;
        // capeFlap is vanilla's lift (e*10, clamped -6..32). Negative lag (cape above body
        // during overshoot) => positive flap addition (cape billows back further). Positive
        // lag (cape below body on body-rise) => negative flap (cape pitches toward back).
        state.capeFlap += (float) (-lag * scale);
    }
}
//?} else {
/*import org.spongepowered.asm.mixin.Mixin;
@Mixin(tecna.oldwalkinganimation.SharedValueUtil.class) public abstract class AvatarRendererMixin {}
*///?}
