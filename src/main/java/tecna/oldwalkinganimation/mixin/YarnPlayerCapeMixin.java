package tecna.oldwalkinganimation.mixin;

//? if (!neoforge) && (>=1.21.9) && (<26.1) {
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PlayerLikeEntity;
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

// 1.21.9+ yarn moved cape rotation to PlayerEntityRenderState's three obfuscated floats
// (field_53536 = lift, field_53537 = forward, field_53538 = roll), populated by
// PlayerEntityRenderer.updateCape. We inject at TAIL of updateCape and add a spring-mass
// bounce contribution to field_53536 (the lift), matching what AvatarRendererMixin does on
// mojmap NeoForge 26.1+.
@Mixin(PlayerEntityRenderer.class)
public abstract class YarnPlayerCapeMixin {
    @Unique private static final Map<LivingEntity, double[]> owa$capeState = new WeakHashMap<>();

    @Inject(method = "updateCape", at = @At("TAIL"))
    private void owa$applyBounceToCapeFlap(PlayerLikeEntity entity, PlayerEntityRenderState state,
                                           float partialTicks, CallbackInfo ci) {
        if (!enableMod || !bounce) return;
        if (!enableMobs && !(entity instanceof net.minecraft.entity.player.PlayerEntity)) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (entity.isSneaking()) return;

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
        state.field_53536 += (float) (-lag * scale);
    }
}
//?} else {
/*import org.spongepowered.asm.mixin.Mixin;
@Mixin(tecna.oldwalkinganimation.SharedValueUtil.class) public abstract class YarnPlayerCapeMixin {}
*///?}
