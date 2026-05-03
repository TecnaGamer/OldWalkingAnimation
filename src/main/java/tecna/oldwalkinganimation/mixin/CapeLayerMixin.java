package tecna.oldwalkinganimation.mixin;

//? if neoforge && <1.21.2 {
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;
import static tecna.oldwalkinganimation.config.Config.bounce;
import static tecna.oldwalkinganimation.config.Config.enableMod;
import static tecna.oldwalkinganimation.config.Config.enableMobs;

import java.util.Map;
import java.util.WeakHashMap;

// Mojmap mirror of CapeFeatureRendererMixin — same spring-mass-damper bounce model and
// rotation contribution applied via @ModifyArg on Axis.XP.rotationDegrees.
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Unique private static final Map<LivingEntity, double[]> owa$capeState = new WeakHashMap<>();
    @Unique private float owa$bounceContribution = 0f;

    @Inject(method = "render", at = @At("HEAD"))
    private void owa$captureBounce(com.mojang.blaze3d.vertex.PoseStack pose,
                                   net.minecraft.client.renderer.MultiBufferSource bufs,
                                   int light,
                                   AbstractClientPlayer entity,
                                   float limbSwing, float limbSwingAmount, float partialTicks,
                                   float ageInTicks, float netHeadYaw, float headPitch,
                                   CallbackInfo ci) {
        if (!enableMod || !bounce) { owa$bounceContribution = 0f; return; }
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) {
            owa$bounceContribution = 0f;
            return;
        }
        if (entity.isCrouching()) { owa$bounceContribution = 0f; return; }

        float var10 = SharedValueUtil.getVar10(entity);
        double bodyY = -var10 * 0.0625;

        long now = System.nanoTime();
        double[] state = owa$capeState.get(entity);
        if (state == null) {
            state = new double[]{ bodyY, 0.0, now };
            owa$capeState.put(entity, state);
        }
        double capeY = state[0];
        double capeVel = state[1];
        long lastNs = (long) state[2];
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
        state[0] = capeY;
        state[1] = capeVel;
        state[2] = now;

        double lag = bodyY - capeY;
        double scale = lag > 0 ? 80.0 : 50.0;
        owa$bounceContribution = (float) (-lag * scale);
    }

    @ModifyArg(
        method = "render",
        at = @At(value = "INVOKE",
                 target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;",
                 ordinal = 0))
    private float owa$tweakCapeXRotation(float deg) {
        if (owa$bounceContribution == 0f) return deg;
        float dampen = (deg - 6f) / 10f;
        if (dampen <= 0f) return deg;
        if (dampen > 1f) dampen = 1f;
        float result = deg + owa$bounceContribution * dampen;
        return result < 0f ? 0f : result;
    }
}
//?} else {
/*import org.spongepowered.asm.mixin.Mixin;
@Mixin(tecna.oldwalkinganimation.SharedValueUtil.class) public abstract class CapeLayerMixin {}
*///?}
