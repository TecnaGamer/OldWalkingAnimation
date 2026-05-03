package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.9) {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
//? if >=26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else {
/*import net.minecraft.client.renderer.state.CameraRenderState;
*///?}
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.21.11 {
import net.minecraft.world.entity.animal.equine.AbstractHorse;
//?} else {
/*import net.minecraft.world.entity.animal.horse.AbstractHorse;
*///?}
import net.minecraft.world.entity.decoration.ArmorStand;
//? if >=1.21.11 {
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
//?} else {
/*import net.minecraft.world.entity.vehicle.AbstractBoat;
*///?}
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.Map;
import java.util.WeakHashMap;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique private final Map<LivingEntity, Float> owa$animStepMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMapP = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$lastAnimStepTimeMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$bodMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$onGroundMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, double[]> owa$damageDirMap = new WeakHashMap<>();

    @Unique private float owa$yBodyRotO;
    @Unique private float owa$yRot;
    @Unique private float owa$yRotO;
    @Unique private float owa$run;
    @Unique private float owa$oRun;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void owa$extractRenderState(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        OwaStateHolder.setEntity(state, entity);
        OwaStateHolder.setPartialTick(state, tickDelta);
        if (entity instanceof OwaSteve && !steveShowNames) {
            state.nameTag = null;
        }
        if (!enableMod) return;

        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) {
            SharedValueUtil.setVar10(entity, 0);
            return;
        }

        boolean classic = OwaSteve.isClassicAnim(entity);
        float aBounceHeight = classic ? 1.0F : bounceHeight;
        boolean aSpeedLimbAngle = classic ? false : speedLimbAngle;
        boolean aVanillaSpeed = classic ? false : vanillaSpeed;
        boolean aClassicRun = classic ? true : classicRun;
        float aSpeed = classic ? 1.0F : speed;
        boolean aBodyRot = classic ? true : bodyRot;
        boolean aSmoothing = classic ? true : smoothing;
        float aDecayFactor = classic ? 0.3F : decayFactor;

        float currentTime = (float) GLFW.glfwGetTime();

        Float lastAnimStepTime = owa$lastAnimStepTimeMap.get(entity);
        if (lastAnimStepTime == null) {
            lastAnimStepTime = currentTime;
            owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);
        }

        float tickScale = OwaTimeScale.scaleFactor();
        float deltaTime = (currentTime - lastAnimStepTime) * 20 * tickScale;
        if (deltaTime > 3f) deltaTime = 3f;
        if (deltaTime < 0.000000000000001f) deltaTime = 0.000000000000001f;

        Float yBodyRot = owa$bodMap.get(entity);
        if (yBodyRot == null) {
            yBodyRot = 0f;
            owa$bodMap.put(entity, yBodyRot);
        }

        this.owa$yRot = Mth.rotLerp(tickDelta, entity.yHeadRotO, entity.yHeadRot);
        this.owa$yBodyRotO = yBodyRot;
        this.owa$yRotO = this.owa$yRot;

        float var1 = (float) entity.getX() - (float) entity.xOld;
        float var2 = (float) entity.getZ() - (float) entity.zOld;
        float var3 = Mth.sqrt(var1 * var1 + var2 * var2);

        float var4 = this.owa$yBodyRotO + (yBodyRot - this.owa$yBodyRotO);
        float var5 = 0.0F;
        this.owa$oRun = this.owa$run;
        float var6 = 0.0F;

        float ST = speedTrigger;
        if (entity instanceof AbstractHorse) {
            ST = speedTrigger * 0.5f;
        }

        if (!(var3 <= ST)) {
            if (var3 >= maxSpeed && maxSpeed != 1) {
                var3 = maxSpeed;
            }
            var6 = 1.0F;
            var5 = var3 * 3.0F;
            var4 = (float) Math.atan2(var2, var1) * 180.0F / 3.1415927F - 90.0F;
        }

        if (entity.getVehicle() != null && !ridingMobAnimation) {
            var6 = 0.0F;
        }

        Float entityRun = owa$runMap.get(entity);
        if (entityRun == null) {
            entityRun = 0.0f;
            owa$runMap.put(entity, entityRun);
        }

        Float entityRun_previous = owa$runMapP.get(entity);
        if (entityRun_previous == null) {
            entityRun_previous = entityRun;
            owa$runMapP.put(entity, entityRun_previous);
        }

        if (aSmoothing) {
            entityRun += (var6 - entityRun) * (aDecayFactor * deltaTime);
        } else {
            entityRun = var6;
        }
        owa$runMap.put(entity, entityRun);

        this.owa$run += (var6 - this.owa$run) * (0.3F * deltaTime);

        // Body-rot smoothing reordered from Classic c0.30 (smooth, clamp, drag) to (smooth,
        // drag, clamp) so the visible body position when the 75° limit triggers is exactly
        // head ± 75° - a constant independent of dt - which kills per-frame jitter caused by
        // the original ordering (post-clamp drag = clamp_value * 0.1 * dt scaled with dt).
        // Both passes still pull body at rate 0.1/tick toward their targets so behavior away
        // from the limit is unchanged. dt capped at 1.0 (one tick worth) to bound stutters.
        float owa$bodyDt = deltaTime > 1.0F ? 1.0F : deltaTime;
        for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) {}
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // smooth toward motion

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // Classic drag toward head (pre-clamp)

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        boolean var7 = var1 < -90.0F || var1 >= 90.0F;
        if (var1 < -75.0F) var1 = -75.0F;
        if (var1 >= 75.0F) var1 = 75.0F;
        yBodyRot = this.owa$yRot - var1;

        owa$bodMap.put(entity, yBodyRot);
        if (var7) var5 = -var5;

        Float entityAnimStep = owa$animStepMap.get(entity);
        if (entityAnimStep == null) {
            entityAnimStep = (float) Math.random();
            owa$animStepMap.put(entity, entityAnimStep);
        }

        float animSpeed = aSpeed;
        if (entity instanceof AbstractHorse) {
            animSpeed = aSpeed * 0.6f;
        }

        float previousAnimStep = entityAnimStep;
        entityAnimStep += (var5 * (animSpeed * deltaTime));
        owa$animStepMap.put(entity, entityAnimStep);

        lastAnimStepTime = currentTime;
        owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);

        while (this.owa$yRot - this.owa$yRotO < -180.0F) this.owa$yRotO -= 360.0F;
        while (this.owa$yRot - this.owa$yRotO >= 180.0F) this.owa$yRotO += 360.0F;

        while (this.owa$yBodyRotO - yBodyRot < -180.0F) this.owa$yBodyRotO += 360.0F;
        while (this.owa$yBodyRotO - yBodyRot >= 180.0F) this.owa$yBodyRotO -= 360.0F;

        float body = this.owa$yBodyRotO + deltaTime * (yBodyRot - this.owa$yBodyRotO);

        if (aBodyRot && !OwaStateHolder.isPreview(entity)) {
            boolean allowOverride = !entity.hasControllingPassenger()
                    && !(entity.getVehicle() instanceof AbstractBoat)
                    && !(entity instanceof ArmorStand);
            if (allowOverride) {
                float oldBody = state.bodyRot;
                entity.yBodyRot = body;
                state.bodyRot = body;
                state.yRot = Mth.wrapDegrees(state.yRot + oldBody - body);
            }
        }

        float ismoving = entityRun_previous + (entityRun - entityRun_previous);

        if (aSpeedLimbAngle) {
            ismoving = entity.walkAnimation.speed(tickDelta) * entityRun;
        }

        float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f);
        if (aVanillaSpeed) {
            var8 = entity.walkAnimation.position(tickDelta);
        }

        Float onGround = owa$onGroundMap.get(entity);
        if (onGround == null) onGround = 1.0f;
        float ground = entity.onGround() ? 1.0f : 0.0f;
        if (aSmoothing) {
            onGround += (ground - onGround) * (aDecayFactor * deltaTime);
        } else {
            onGround = ground;
        }
        owa$onGroundMap.put(entity, onGround);

        if (aClassicRun) {
            double raw = OwaTimeScale.scaledTime() * 10.0;
            if (entity instanceof OwaSteve steve) {
                raw += steve.owaTimeOffs;
            }
            final double WRAP = 9431.625; // 1000 * 2π/0.6662, preserves main arm/leg cycle
            raw = ((raw % WRAP) + WRAP) % WRAP;
            var8 = (float) raw;
            ismoving = 1.0f;
        }

        float bounceGround = classic ? 1.0f : onGround;
        float bounceTrig = classic
                ? Mth.sin(var8 * 0.6662F)
                : (bounceInverted ? Mth.sin(var8 * 0.6662F) : Mth.cos(var8 * 0.6662F));
        float var10 = -Math.abs(bounceTrig) * 5.0F * ismoving * aBounceHeight * bounceGround;

        SharedValueUtil.setVar10(entity, var10);
        SharedValueUtil.setVar8(entity, var8);
        SharedValueUtil.setIsMoving(entity, ismoving);
    }

    //? if >=26.1 {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.AFTER))
    //?} else {
    /*@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.AFTER))
    *///?}
    private void owa$applyBounce(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (!enableMod) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) return;
        if (!bounce && !OwaSteve.isClassicAnim(entity)) return;
        float var10 = SharedValueUtil.getVar10(entity);
        if (entity.getVehicle() instanceof LivingEntity rider) {
            var10 = SharedValueUtil.getVar10(rider);
            SharedValueUtil.setVar10(entity, var10);
        }
        pose.translate(0f, -var10 * 0.0625f, 0f);
    }

    @ModifyArg(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sqrt(F)F"),
            index = 0)
    private float owa$suppressVanillaDeathTilt(float f) {
        return (enableMod && damage) ? 0f : f;
    }

    //? if >=26.1 {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.BEFORE))
    //?} else {
    /*@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.BEFORE))
    *///?}
    private void owa$applyDamageTilt(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (!enableMod || !damage) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) return;

        float partialTick = OwaStateHolder.getPartialTick(state);
        boolean dying = entity.getHealth() <= 0;
        float hurt = (float) entity.hurtTime - partialTick;
        if (hurt <= 0.0F && !dying) {
            if (entity.hurtTime <= 0 && entity.deathTime == 0) {
                owa$damageDirMap.remove(entity);
            }
            return;
        }

        float tilt;
        if (hurt < 0.0F) {
            tilt = 0.0F;
        } else {
            float denom = entity.hurtDuration <= 0 ? 10.0F : (float) entity.hurtDuration;
            float t = hurt / denom;
            tilt = Mth.sin(t * t * t * t * (float) Math.PI) * damageIntensity;
        }

        if (dying) {
            float deathFactor = ((float) entity.deathTime + partialTick) / 20.0F;
            tilt += deathFactor * deathFactor * 800.0F;
            if (tilt > 90.0F) tilt = 90.0F;
        }

        if (SharedValueUtil.consumeDamageDirInvalidation(entity)) {
            owa$damageDirMap.remove(entity);
        }
        double[] cached = owa$damageDirMap.get(entity);
        double dx, dz;
        float rotOffs;
        if (cached != null && lockRot) {
            dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2];
        } else {
            double tryDx = 0, tryDz = 0;
            float tryOffs = 0.0F;
            boolean have = false;

            if (!velocity) {
                DamageSource src = entity.getLastDamageSource();
                Vec3 srcPos = src == null ? null : src.getSourcePosition();
                if (srcPos != null) {
                    double ddx = srcPos.x - entity.getX();
                    double ddz = srcPos.z - entity.getZ();
                    if (ddx != 0 || ddz != 0) {
                        tryDx = ddx; tryDz = ddz; tryOffs = 0.0F; have = true;
                    }
                }
            }

            if (!have && (velocity || fallback)) {
                Vec3 v = entity.getDeltaMovement();
                if (v.x != 0 || v.z != 0) {
                    tryDx = v.x; tryDz = v.z; tryOffs = 180.0F; have = true;
                }
            }

            if (!have) {
                if (cached != null) { dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2]; }
                else if (dying) {
                    // Fallback: fall on back when dying with no direction info
                    float yaw = entity.getYRot();
                    dx = -Math.sin(Math.toRadians(yaw));
                    dz = Math.cos(Math.toRadians(yaw));
                    rotOffs = 180.0F;
                    owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
                }
                else return;
            } else {
                dx = tryDx; dz = tryDz; rotOffs = tryOffs;
                owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
            }
        }

        double angleDegrees = Math.toDegrees(Math.atan2(dz, dx));
        if (angleDegrees < 0) angleDegrees += 360;
        float directionAngle = (float) -angleDegrees + rotOffs;

        pose.mulPose(Axis.YP.rotationDegrees(directionAngle));
        pose.mulPose(Axis.ZP.rotationDegrees(tilt));
        pose.mulPose(Axis.YP.rotationDegrees(-directionAngle));
    }
}
//?} else if (neoforge && >=1.21.2) {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.Map;
import java.util.WeakHashMap;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique private final Map<LivingEntity, Float> owa$animStepMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMapP = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$lastAnimStepTimeMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$bodMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$onGroundMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, double[]> owa$damageDirMap = new WeakHashMap<>();

    @Unique private float owa$yBodyRotO;
    @Unique private float owa$yRot;
    @Unique private float owa$yRotO;
    @Unique private float owa$run;
    @Unique private float owa$oRun;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void owa$extractRenderState(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        OwaStateHolder.setEntity(state, entity);
        OwaStateHolder.setPartialTick(state, tickDelta);
        if (entity instanceof OwaSteve && !steveShowNames) {
            state.nameTag = null;
        }
        if (!enableMod) return;

        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) {
            SharedValueUtil.setVar10(entity, 0);
            return;
        }

        boolean classic = OwaSteve.isClassicAnim(entity);
        float aBounceHeight = classic ? 1.0F : bounceHeight;
        boolean aSpeedLimbAngle = classic ? false : speedLimbAngle;
        boolean aVanillaSpeed = classic ? false : vanillaSpeed;
        boolean aClassicRun = classic ? true : classicRun;
        float aSpeed = classic ? 1.0F : speed;
        boolean aBodyRot = classic ? true : bodyRot;
        boolean aSmoothing = classic ? true : smoothing;
        float aDecayFactor = classic ? 0.3F : decayFactor;

        float currentTime = (float) GLFW.glfwGetTime();

        Float lastAnimStepTime = owa$lastAnimStepTimeMap.get(entity);
        if (lastAnimStepTime == null) {
            lastAnimStepTime = currentTime;
            owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);
        }

        float tickScale = OwaTimeScale.scaleFactor();
        float deltaTime = (currentTime - lastAnimStepTime) * 20 * tickScale;
        if (deltaTime > 3f) deltaTime = 3f;
        if (deltaTime < 0.000000000000001f) deltaTime = 0.000000000000001f;

        Float yBodyRot = owa$bodMap.get(entity);
        if (yBodyRot == null) {
            yBodyRot = 0f;
            owa$bodMap.put(entity, yBodyRot);
        }

        this.owa$yRot = Mth.rotLerp(tickDelta, entity.yHeadRotO, entity.yHeadRot);
        this.owa$yBodyRotO = yBodyRot;
        this.owa$yRotO = this.owa$yRot;

        float var1 = (float) entity.getX() - (float) entity.xo;
        float var2 = (float) entity.getZ() - (float) entity.zo;
        float var3 = Mth.sqrt(var1 * var1 + var2 * var2);

        float var4 = this.owa$yBodyRotO + (yBodyRot - this.owa$yBodyRotO);
        float var5 = 0.0F;
        this.owa$oRun = this.owa$run;
        float var6 = 0.0F;

        float ST = speedTrigger;
        if (entity instanceof AbstractHorse) {
            ST = speedTrigger * 0.5f;
        }

        if (!(var3 <= ST)) {
            if (var3 >= maxSpeed && maxSpeed != 1) {
                var3 = maxSpeed;
            }
            var6 = 1.0F;
            var5 = var3 * 3.0F;
            var4 = (float) Math.atan2(var2, var1) * 180.0F / 3.1415927F - 90.0F;
        }

        if (entity.getVehicle() != null && !ridingMobAnimation) {
            var6 = 0.0F;
        }

        Float entityRun = owa$runMap.get(entity);
        if (entityRun == null) {
            entityRun = 0.0f;
            owa$runMap.put(entity, entityRun);
        }

        Float entityRun_previous = owa$runMapP.get(entity);
        if (entityRun_previous == null) {
            entityRun_previous = entityRun;
            owa$runMapP.put(entity, entityRun_previous);
        }

        if (aSmoothing) {
            entityRun += (var6 - entityRun) * (aDecayFactor * deltaTime);
        } else {
            entityRun = var6;
        }
        owa$runMap.put(entity, entityRun);

        this.owa$run += (var6 - this.owa$run) * (0.3F * deltaTime);

        // Body-rot smoothing reordered from Classic c0.30 (smooth, clamp, drag) to (smooth,
        // drag, clamp) so the visible body position when the 75° limit triggers is exactly
        // head ± 75° - a constant independent of dt - which kills per-frame jitter caused by
        // the original ordering (post-clamp drag = clamp_value * 0.1 * dt scaled with dt).
        // Both passes still pull body at rate 0.1/tick toward their targets so behavior away
        // from the limit is unchanged. dt capped at 1.0 (one tick worth) to bound stutters.
        float owa$bodyDt = deltaTime > 1.0F ? 1.0F : deltaTime;
        for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) {}
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // smooth toward motion

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // Classic drag toward head (pre-clamp)

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        boolean var7 = var1 < -90.0F || var1 >= 90.0F;
        if (var1 < -75.0F) var1 = -75.0F;
        if (var1 >= 75.0F) var1 = 75.0F;
        yBodyRot = this.owa$yRot - var1;

        owa$bodMap.put(entity, yBodyRot);
        if (var7) var5 = -var5;

        Float entityAnimStep = owa$animStepMap.get(entity);
        if (entityAnimStep == null) {
            entityAnimStep = (float) Math.random();
            owa$animStepMap.put(entity, entityAnimStep);
        }

        float animSpeed = aSpeed;
        if (entity instanceof AbstractHorse) {
            animSpeed = aSpeed * 0.6f;
        }

        float previousAnimStep = entityAnimStep;
        entityAnimStep += (var5 * (animSpeed * deltaTime));
        owa$animStepMap.put(entity, entityAnimStep);

        lastAnimStepTime = currentTime;
        owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);

        while (this.owa$yRot - this.owa$yRotO < -180.0F) this.owa$yRotO -= 360.0F;
        while (this.owa$yRot - this.owa$yRotO >= 180.0F) this.owa$yRotO += 360.0F;

        while (this.owa$yBodyRotO - yBodyRot < -180.0F) this.owa$yBodyRotO += 360.0F;
        while (this.owa$yBodyRotO - yBodyRot >= 180.0F) this.owa$yBodyRotO -= 360.0F;

        float body = this.owa$yBodyRotO + deltaTime * (yBodyRot - this.owa$yBodyRotO);

        if (aBodyRot && !OwaStateHolder.isPreview(entity)) {
            boolean allowOverride = !entity.hasControllingPassenger()
                    && !(entity.getVehicle() instanceof AbstractBoat)
                    && !(entity instanceof ArmorStand);
            if (allowOverride) {
                float oldBody = state.bodyRot;
                entity.yBodyRot = body;
                state.bodyRot = body;
                state.yRot = Mth.wrapDegrees(state.yRot + oldBody - body);
            }
        }

        float ismoving = entityRun_previous + (entityRun - entityRun_previous);

        if (aSpeedLimbAngle) {
            ismoving = entity.walkAnimation.speed(tickDelta) * entityRun;
        }

        float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f);
        if (aVanillaSpeed) {
            var8 = entity.walkAnimation.position(tickDelta);
        }

        Float onGround = owa$onGroundMap.get(entity);
        if (onGround == null) onGround = 1.0f;
        float ground = entity.onGround() ? 1.0f : 0.0f;
        if (aSmoothing) {
            onGround += (ground - onGround) * (aDecayFactor * deltaTime);
        } else {
            onGround = ground;
        }
        owa$onGroundMap.put(entity, onGround);

        if (aClassicRun) {
            double raw = OwaTimeScale.scaledTime() * 10.0;
            if (entity instanceof OwaSteve steve) {
                raw += steve.owaTimeOffs;
            }
            final double WRAP = 9431.625;
            raw = ((raw % WRAP) + WRAP) % WRAP;
            var8 = (float) raw;
            ismoving = 1.0f;
        }

        float bounceGround = classic ? 1.0f : onGround;
        float bounceTrig = classic
                ? Mth.sin(var8 * 0.6662F)
                : (bounceInverted ? Mth.sin(var8 * 0.6662F) : Mth.cos(var8 * 0.6662F));
        float var10 = -Math.abs(bounceTrig) * 5.0F * ismoving * aBounceHeight * bounceGround;

        SharedValueUtil.setVar10(entity, var10);
        SharedValueUtil.setVar8(entity, var8);
        SharedValueUtil.setIsMoving(entity, ismoving);
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.AFTER))
    private void owa$applyBounce(LivingEntityRenderState state, PoseStack pose, MultiBufferSource buf, int light, CallbackInfo ci) {
        if (!enableMod) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) return;
        if (!bounce && !OwaSteve.isClassicAnim(entity)) return;
        float var10 = SharedValueUtil.getVar10(entity);
        if (entity.getVehicle() instanceof LivingEntity rider) {
            var10 = SharedValueUtil.getVar10(rider);
            SharedValueUtil.setVar10(entity, var10);
        }
        pose.translate(0f, -var10 * 0.0625f, 0f);
    }

    @ModifyArg(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sqrt(F)F"),
            index = 0)
    private float owa$suppressVanillaDeathTilt(float f) {
        return (enableMod && damage) ? 0f : f;
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
                     shift = At.Shift.BEFORE))
    private void owa$applyDamageTilt(LivingEntityRenderState state, PoseStack pose, MultiBufferSource buf, int light, CallbackInfo ci) {
        if (!enableMod || !damage) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof net.minecraft.world.entity.player.Player)) return;

        float partialTick = OwaStateHolder.getPartialTick(state);
        boolean dying = entity.getHealth() <= 0;
        float hurt = (float) entity.hurtTime - partialTick;
        if (hurt <= 0.0F && !dying) {
            if (entity.hurtTime <= 0 && entity.deathTime == 0) {
                owa$damageDirMap.remove(entity);
            }
            return;
        }

        float tilt;
        if (hurt < 0.0F) {
            tilt = 0.0F;
        } else {
            float denom = entity.hurtDuration <= 0 ? 10.0F : (float) entity.hurtDuration;
            float t = hurt / denom;
            tilt = Mth.sin(t * t * t * t * (float) Math.PI) * damageIntensity;
        }

        if (dying) {
            float deathFactor = ((float) entity.deathTime + partialTick) / 20.0F;
            tilt += deathFactor * deathFactor * 800.0F;
            if (tilt > 90.0F) tilt = 90.0F;
        }

        if (SharedValueUtil.consumeDamageDirInvalidation(entity)) {
            owa$damageDirMap.remove(entity);
        }
        double[] cached = owa$damageDirMap.get(entity);
        double dx, dz;
        float rotOffs;
        if (cached != null && lockRot) {
            dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2];
        } else {
            double tryDx = 0, tryDz = 0;
            float tryOffs = 0.0F;
            boolean have = false;

            if (!velocity) {
                DamageSource src = entity.getLastDamageSource();
                Vec3 srcPos = src == null ? null : src.getSourcePosition();
                if (srcPos != null) {
                    double ddx = srcPos.x - entity.getX();
                    double ddz = srcPos.z - entity.getZ();
                    if (ddx != 0 || ddz != 0) {
                        tryDx = ddx; tryDz = ddz; tryOffs = 0.0F; have = true;
                    }
                }
            }

            if (!have && (velocity || fallback)) {
                Vec3 v = entity.getDeltaMovement();
                if (v.x != 0 || v.z != 0) {
                    tryDx = v.x; tryDz = v.z; tryOffs = 180.0F; have = true;
                }
            }

            if (!have) {
                if (cached != null) { dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2]; }
                else if (dying) {
                    float yaw = entity.getYRot();
                    dx = -Math.sin(Math.toRadians(yaw));
                    dz = Math.cos(Math.toRadians(yaw));
                    rotOffs = 180.0F;
                    owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
                }
                else return;
            } else {
                dx = tryDx; dz = tryDz; rotOffs = tryOffs;
                owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
            }
        }

        double angleDegrees = Math.toDegrees(Math.atan2(dz, dx));
        if (angleDegrees < 0) angleDegrees += 360;
        float directionAngle = (float) -angleDegrees + rotOffs;

        pose.mulPose(Axis.YP.rotationDegrees(directionAngle));
        pose.mulPose(Axis.ZP.rotationDegrees(tilt));
        pose.mulPose(Axis.YP.rotationDegrees(-directionAngle));
    }
}
*///?} else if neoforge {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.HashMap;
import java.util.Map;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) { super(ctx); }

    @Shadow protected abstract float getBob(T entity, float tickDelta);
    @Shadow protected abstract float getFlipDegrees(T entity);

    private double lastTickTime;
    private float bobStrength = 1;
    private Vec3 prevVelocity;
    private float hurtframe = 0;
    private float animStep;
    private float animStepO;
    protected float oRun;
    protected float run;
    private float yBodyRotO;
    private float yRot;
    private float yRotO;
    private int owaDbgCounter = 0;

    private final Map<LivingEntity, Double> dxMap = new HashMap<>();
    private final Map<LivingEntity, Double> dzMap = new HashMap<>();
    private final Map<LivingEntity, Float> animStepMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMapP = new HashMap<>();
    private final Map<LivingEntity, Float> lastAnimStepTimeMap = new HashMap<>();
    private final Map<LivingEntity, Float> bodMap = new HashMap<>();
    private final Map<LivingEntity, Float> onGroundMap = new HashMap<>();

    private float rotOffs = 0f;

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void owa$showSteveName(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof OwaSteve) {
            cir.setReturnValue(entity.shouldShowName());
        }
    }

    @ModifyArg(
            //? if >=1.20.5 {
            method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            //?} else
            /^method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",^/
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;sqrt(F)F"),
            index = 0
    )
    private float owa$modifySqrtArgument(float f) {
        if (damage && enableMod) return 0;
        return f;
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void owa$preRenderApplyBody(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buf, int light, CallbackInfo ci) {
        if (!enableMod) return;
        if (!enableMobs && !(entity instanceof Player)) return;
        if (tecna.oldwalkinganimation.OwaStateHolder.isPreview(entity)) return;
        boolean classic = OwaSteve.isClassicAnim(entity);
        boolean aBodyRot = classic ? true : bodyRot;
        if (!aBodyRot) return;
        if (entity.hasControllingPassenger() || entity.getVehicle() instanceof Boat || entity instanceof ArmorStand) return;
        Float cachedBody = bodMap.get(entity);
        if (cachedBody == null) return;
        entity.yBodyRot = entity.yBodyRotO = cachedBody;
    }

    //? if >=1.20.5 {
    @Inject(method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("HEAD"))
    private void owa$setupRotations(T entity, PoseStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scaleArg, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
            at = @At("HEAD"))
    private void owa$setupRotations(T entity, PoseStack matrices, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
    ^///?}
        if (!enableMod) return;
        boolean runCode;
        if (enableMobs) {
            runCode = true;
        } else if (entity instanceof Player) {
            runCode = true;
        } else {
            runCode = false;
            SharedValueUtil.setVar10(entity, 0);
        }
        if (!runCode) return;

        boolean classic = OwaSteve.isClassicAnim(entity);
        float aBounceHeight = classic ? 1.0F : bounceHeight;
        boolean aSpeedLimbAngle = classic ? false : speedLimbAngle;
        boolean aVanillaSpeed = classic ? false : vanillaSpeed;
        boolean aClassicRun = classic ? true : classicRun;
        float aSpeed = classic ? 1.0F : speed;
        boolean aBodyRot = classic ? true : bodyRot;
        boolean aSmoothing = classic ? true : smoothing;
        float aDecayFactor = classic ? 0.3F : decayFactor;

        float currentTime = (float) GLFW.glfwGetTime();

        Float lastAnimStepTime = lastAnimStepTimeMap.get(entity);
        if (lastAnimStepTime == null) {
            lastAnimStepTime = currentTime;
            lastAnimStepTimeMap.put(entity, lastAnimStepTime);
        }

        float tickScale = OwaTimeScale.scaleFactor();
        float deltaTime = (currentTime - lastAnimStepTime) * 20 * tickScale;
        if (deltaTime > 3f) deltaTime = 3f;
        if (deltaTime < 0.000000000000001f) deltaTime = 0.000000000000001f;

        Float yBodyRot = bodMap.get(entity);
        if (yBodyRot == null) {
            yBodyRot = 0f;
            bodMap.put(entity, yBodyRot);
        }

        this.yRot = entity.getViewYRot(tickDelta);
        this.yBodyRotO = yBodyRot;
        this.yRotO = this.yRot;

        float var1 = (float) entity.getX() - (float) entity.xo;
        float var2 = (float) entity.getZ() - (float) entity.zo;
        float var3 = Mth.sqrt(var1 * var1 + var2 * var2);

        float var4 = this.yBodyRotO + (yBodyRot - this.yBodyRotO);
        float var5 = 0.0F;
        this.oRun = this.run;
        float var6 = 0.0F;

        float ST = speedTrigger;
        if (entity instanceof AbstractHorse) {
            ST = speedTrigger * 0.5f;
        }

        if (!(var3 <= ST)) {
            if (var3 >= maxSpeed && maxSpeed != 1) {
                var3 = maxSpeed;
            }
            var6 = 1.0F;
            var5 = var3 * 3.0F;
            var4 = (float) Math.atan2((double) var2, (double) var1) * 180.0F / 3.1415927F - 90.0F;
        }

        if (entity.getVehicle() != null && !ridingMobAnimation) {
            var6 = 0.0F;
        }

        Float entityRun = runMap.get(entity);
        if (entityRun == null) {
            entityRun = 0.0f;
            runMap.put(entity, entityRun);
        }
        Float entityRun_previous = runMapP.get(entity);
        if (entityRun_previous == null) {
            entityRun_previous = entityRun;
            runMapP.put(entity, entityRun_previous);
        }

        if (aSmoothing) {
            entityRun += (var6 - entityRun) * (aDecayFactor * deltaTime);
        } else {
            entityRun = var6;
        }
        runMap.put(entity, entityRun);

        this.run += (var6 - this.run) * (0.3F * deltaTime);

        // Body-rot smoothing reordered from Classic c0.30 (smooth, clamp, drag) to (smooth,
        // drag, clamp). When the 75° limit triggers, body lands at head ± 75° - constant in
        // dt - which kills per-frame jitter that the original order produced (post-clamp drag
        // = clamp_value * 0.1 * dt scaled with dt). Math away from the limit is unchanged.
        float owa$bodyDt = deltaTime > 1.0F ? 1.0F : deltaTime;
        for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) ;
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // smooth toward motion

        var1 = this.yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // Classic drag toward head (pre-clamp)

        var1 = this.yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        boolean var7 = var1 < -90.0F || var1 >= 90.0F;
        if (var1 < -75.0F) var1 = -75.0F;
        if (var1 >= 75.0F) var1 = 75.0F;
        yBodyRot = this.yRot - var1;

        bodMap.put(entity, yBodyRot);
        if (var7) var5 = -var5;

        Float entityAnimStep = animStepMap.get(entity);
        if (entityAnimStep == null) {
            entityAnimStep = (float) Math.random();
            animStepMap.put(entity, entityAnimStep);
        }

        float animSpeed = aSpeed;
        if (entity instanceof AbstractHorse) {
            animSpeed = aSpeed * 0.6f;
        }

        float previousAnimStep = entityAnimStep;
        entityAnimStep += (var5 * (animSpeed * deltaTime));
        animStepMap.put(entity, entityAnimStep);

        lastAnimStepTime = currentTime;
        lastAnimStepTimeMap.put(entity, lastAnimStepTime);

        while (this.yRot - this.yRotO < -180.0F) this.yRotO -= 360.0F;
        while (this.yRot - this.yRotO >= 180.0F) this.yRotO += 360.0F;

        while (this.yBodyRotO - yBodyRot < -180.0F) this.yBodyRotO += 360.0F;
        while (this.yBodyRotO - yBodyRot >= 180.0F) this.yBodyRotO -= 360.0F;

        float body = this.yBodyRotO + deltaTime * (yBodyRot - this.yBodyRotO);

        if (aBodyRot && !tecna.oldwalkinganimation.OwaStateHolder.isPreview(entity)) {
            if (!entity.hasControllingPassenger()
                    && !(entity.getVehicle() instanceof Boat)
                    && !(entity instanceof ArmorStand)) {
                entity.yBodyRot = entity.yBodyRotO = body;
            }
        }

        float ismoving = entityRun_previous + (entityRun - entityRun_previous);
        if (aSpeedLimbAngle) {
            ismoving = entity.walkAnimation.speed(tickDelta) * entityRun;
        }

        float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f);
        if (aVanillaSpeed) {
            var8 = entity.walkAnimation.position(tickDelta);
        }

        Float onGround = onGroundMap.get(entity);
        if (onGround == null) onGround = 1.0f;
        float ground = entity.onGround() ? 1.0f : 0.0f;
        if (aSmoothing) {
            onGround += (ground - onGround) * (aDecayFactor * deltaTime);
        } else {
            onGround = ground;
        }
        onGroundMap.put(entity, onGround);

        if (aClassicRun) {
            double raw = OwaTimeScale.scaledTime() * 10.0;
            if (entity instanceof OwaSteve steve) {
                raw += steve.owaTimeOffs;
            }
            final double WRAP = 9431.625;
            raw = ((raw % WRAP) + WRAP) % WRAP;
            var8 = (float) raw;
            ismoving = 1.0f;
        }

        float var9 = 0.0625F;
        float bounceGround = classic ? 1.0f : onGround;
        float bounceTrig = classic
                ? Mth.sin(var8 * 0.6662F)
                : (bounceInverted ? Mth.sin(var8 * 0.6662F) : Mth.cos(var8 * 0.6662F));
        float var10 = -Math.abs(bounceTrig) * 5.0F * ismoving * aBounceHeight * bounceGround;

        float var11;
        if ((var11 = (float) entity.hurtTime - tickDelta) > 0.0F || entity.getHealth() <= 0) {
            if (var11 < 0.0F) {
                var11 = 0.0F;
            } else {
                var11 = Mth.sin((var11 /= (float) entity.hurtDuration) * var11 * var11 * var11 * 3.1415927F) * damageIntensity;
            }

            float var12;
            if (entity.getHealth() <= 0) {
                var12 = ((float) entity.deathTime + tickDelta) / 20.0F;
                if ((var11 += var12 * var12 * 800.0F) > this.getFlipDegrees(entity)) {
                    var11 = this.getFlipDegrees(entity);
                }
            }

            if (damage) {
                if (SharedValueUtil.consumeDamageDirInvalidation(entity)) {
                    dxMap.remove(entity);
                    dzMap.remove(entity);
                }
                Double dx = dxMap.get(entity);
                Double dz = dzMap.get(entity);
                try {
                    if (dx == null || !lockRot) {
                        if (velocity) {
                            if (entity.getDeltaMovement() != prevVelocity && (!(entity.getDeltaMovement().x == 0) && !(entity.getDeltaMovement().z == 0))) {
                                rotOffs = 180;
                                dx = entity.getDeltaMovement().x;
                                dz = entity.getDeltaMovement().z;
                            }
                        } else {
                            rotOffs = 0;
                            dx = entity.getLastDamageSource().getSourcePosition().x - entity.position().x;
                            dz = entity.getLastDamageSource().getSourcePosition().z - entity.position().z;
                        }
                        dxMap.put(entity, dx);
                        dzMap.put(entity, dz);
                    }

                    double angleRadians = Math.atan2(dz, dx);
                    double angleDegrees = Math.toDegrees(angleRadians);
                    if (angleDegrees < 0) angleDegrees += 360;
                    float directionAngle = (float) -angleDegrees + rotOffs;

                    matrices.mulPose(Axis.YP.rotationDegrees(directionAngle));
                    matrices.mulPose(Axis.ZP.rotationDegrees(var11));
                    matrices.mulPose(Axis.YP.rotationDegrees(-directionAngle));
                } catch (NullPointerException ignored) {
                    try {
                        if (fallback) {
                            if (dx == null || !lockRot) {
                                if (entity.getDeltaMovement() != prevVelocity && (!(entity.getDeltaMovement().x == 0) && !(entity.getDeltaMovement().z == 0))) {
                                    rotOffs = 180;
                                    dx = entity.getDeltaMovement().x;
                                    dz = entity.getDeltaMovement().z;
                                }
                                dxMap.put(entity, dx);
                                dzMap.put(entity, dz);
                            }
                            double angleRadians = Math.atan2(dz, dx);
                            double angleDegrees = Math.toDegrees(angleRadians);
                            if (angleDegrees < 0) angleDegrees += 360;
                            float directionAngle = (float) -angleDegrees + rotOffs;
                            matrices.mulPose(Axis.YP.rotationDegrees(directionAngle));
                            matrices.mulPose(Axis.ZP.rotationDegrees(var11));
                            matrices.mulPose(Axis.YP.rotationDegrees(-directionAngle));
                        } else if (entity.getHealth() <= 0) {
                            float yaw = entity.getYRot();
                            double dxF = -Math.sin(Math.toRadians(yaw));
                            double dzF = Math.cos(Math.toRadians(yaw));
                            float rot = 180.0F;
                            double angleRadians = Math.atan2(dzF, dxF);
                            double angleDegrees = Math.toDegrees(angleRadians);
                            if (angleDegrees < 0) angleDegrees += 360;
                            float directionAngle = (float) -angleDegrees + rot;
                            matrices.mulPose(Axis.YP.rotationDegrees(directionAngle));
                            matrices.mulPose(Axis.ZP.rotationDegrees(var11));
                            matrices.mulPose(Axis.YP.rotationDegrees(-directionAngle));
                        }
                    } catch (NullPointerException ignored1) {}
                }
            }
        }

        if (entity.hurtTime <= 0 && entity.deathTime == 0) {
            dxMap.put(entity, null);
            dzMap.put(entity, null);
            hurtframe = 0;
        }

        SharedValueUtil.setVar10(entity, var10);
        SharedValueUtil.setVar8(entity, var8);
        SharedValueUtil.setIsMoving(entity, ismoving);

        prevVelocity = entity.getDeltaMovement();

        if (bounce) {
            try {
                if (entity.getVehicle() != null) {
                    var10 = SharedValueUtil.getVar10((LivingEntity) entity.getVehicle());
                    SharedValueUtil.setVar10(entity, var10);
                    matrices.translate(0, (-var10 * var9), 0);
                } else {
                    matrices.translate(0, (-var10 * var9), 0);
                }
            } catch (ClassCastException ignored) {}
        }
    }
}
*///?} else if >=1.21.2 {
/*//? if >=1.21.9 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
//?} else
/^import net.minecraft.client.render.VertexConsumerProvider;^/
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.Map;
import java.util.WeakHashMap;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique private final Map<LivingEntity, Float> owa$animStepMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$runMapP = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$lastAnimStepTimeMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$bodMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, Float> owa$onGroundMap = new WeakHashMap<>();
    @Unique private final Map<LivingEntity, double[]> owa$damageDirMap = new WeakHashMap<>();

    @Unique private float owa$yBodyRotO;
    @Unique private float owa$yRot;
    @Unique private float owa$yRotO;
    @Unique private float owa$run;
    @Unique private float owa$oRun;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void owa$extractRenderState(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        OwaStateHolder.setEntity(state, entity);
        OwaStateHolder.setPartialTick(state, tickDelta);
        if (entity instanceof OwaSteve && !steveShowNames) {
            state.displayName = null;
        }
        if (!enableMod) return;

        if (!enableMobs && !(entity instanceof PlayerEntity)) {
            SharedValueUtil.setVar10(entity, 0);
            return;
        }

        boolean classic = OwaSteve.isClassicAnim(entity);
        float aBounceHeight = classic ? 1.0F : bounceHeight;
        boolean aSpeedLimbAngle = classic ? false : speedLimbAngle;
        boolean aVanillaSpeed = classic ? false : vanillaSpeed;
        boolean aClassicRun = classic ? true : classicRun;
        float aSpeed = classic ? 1.0F : speed;
        boolean aBodyRot = classic ? true : bodyRot;
        boolean aSmoothing = classic ? true : smoothing;
        float aDecayFactor = classic ? 0.3F : decayFactor;

        float currentTime = (float) GLFW.glfwGetTime();

        Float lastAnimStepTime = owa$lastAnimStepTimeMap.get(entity);
        if (lastAnimStepTime == null) {
            lastAnimStepTime = currentTime;
            owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);
        }

        float tickScale = OwaTimeScale.scaleFactor();
        float deltaTime = (currentTime - lastAnimStepTime) * 20 * tickScale;
        if (deltaTime > 3f) deltaTime = 3f;
        if (deltaTime < 0.000000000000001f) deltaTime = 0.000000000000001f;

        Float yBodyRot = owa$bodMap.get(entity);
        if (yBodyRot == null) {
            yBodyRot = 0f;
            owa$bodMap.put(entity, yBodyRot);
        }

        //? if >=1.21.5 {
        this.owa$yRot = MathHelper.lerpAngleDegrees(tickDelta, entity.lastHeadYaw, entity.headYaw);
        //?} else
        /^this.owa$yRot = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw);^/
        this.owa$yBodyRotO = yBodyRot;
        this.owa$yRotO = this.owa$yRot;

        //? if >=1.21.5 {
        float var1 = (float) entity.getX() - (float) entity.lastX;
        float var2 = (float) entity.getZ() - (float) entity.lastZ;
        //?} else {
        /^float var1 = (float) entity.getX() - (float) entity.prevX;
        float var2 = (float) entity.getZ() - (float) entity.prevZ;
        ^///?}
        float var3 = MathHelper.sqrt(var1 * var1 + var2 * var2);

        float var4 = this.owa$yBodyRotO + (yBodyRot - this.owa$yBodyRotO);
        float var5 = 0.0F;
        this.owa$oRun = this.owa$run;
        float var6 = 0.0F;

        float ST = speedTrigger;
        if (entity instanceof AbstractHorseEntity) {
            ST = speedTrigger * 0.5f;
        }

        if (!(var3 <= ST)) {
            if (var3 >= maxSpeed && maxSpeed != 1) {
                var3 = maxSpeed;
            }
            var6 = 1.0F;
            var5 = var3 * 3.0F;
            var4 = (float) Math.atan2(var2, var1) * 180.0F / 3.1415927F - 90.0F;
        }

        if (entity.getVehicle() != null && !ridingMobAnimation) {
            var6 = 0.0F;
        }

        Float entityRun = owa$runMap.get(entity);
        if (entityRun == null) {
            entityRun = 0.0f;
            owa$runMap.put(entity, entityRun);
        }

        Float entityRun_previous = owa$runMapP.get(entity);
        if (entityRun_previous == null) {
            entityRun_previous = entityRun;
            owa$runMapP.put(entity, entityRun_previous);
        }

        if (aSmoothing) {
            entityRun += (var6 - entityRun) * (aDecayFactor * deltaTime);
        } else {
            entityRun = var6;
        }
        owa$runMap.put(entity, entityRun);

        this.owa$run += (var6 - this.owa$run) * (0.3F * deltaTime);

        // Body-rot smoothing reordered from Classic c0.30 (smooth, clamp, drag) to (smooth,
        // drag, clamp) so the visible body position when the 75° limit triggers is exactly
        // head ± 75° - a constant independent of dt - which kills per-frame jitter caused by
        // the original ordering (post-clamp drag = clamp_value * 0.1 * dt scaled with dt).
        // Both passes still pull body at rate 0.1/tick toward their targets so behavior away
        // from the limit is unchanged. dt capped at 1.0 (one tick worth) to bound stutters.
        float owa$bodyDt = deltaTime > 1.0F ? 1.0F : deltaTime;
        for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) {}
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // smooth toward motion

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        yBodyRot += var1 * (0.1F * owa$bodyDt);          // Classic drag toward head (pre-clamp)

        var1 = this.owa$yRot - yBodyRot;
        while (var1 < -180.0F) var1 += 360.0F;
        while (var1 >= 180.0F) var1 -= 360.0F;
        boolean var7 = var1 < -90.0F || var1 >= 90.0F;
        if (var1 < -75.0F) var1 = -75.0F;
        if (var1 >= 75.0F) var1 = 75.0F;
        yBodyRot = this.owa$yRot - var1;

        owa$bodMap.put(entity, yBodyRot);
        if (var7) var5 = -var5;

        Float entityAnimStep = owa$animStepMap.get(entity);
        if (entityAnimStep == null) {
            entityAnimStep = (float) Math.random();
            owa$animStepMap.put(entity, entityAnimStep);
        }

        float animSpeed = aSpeed;
        if (entity instanceof AbstractHorseEntity) {
            animSpeed = aSpeed * 0.6f;
        }

        float previousAnimStep = entityAnimStep;
        entityAnimStep += (var5 * (animSpeed * deltaTime));
        owa$animStepMap.put(entity, entityAnimStep);

        lastAnimStepTime = currentTime;
        owa$lastAnimStepTimeMap.put(entity, lastAnimStepTime);

        while (this.owa$yRot - this.owa$yRotO < -180.0F) this.owa$yRotO -= 360.0F;
        while (this.owa$yRot - this.owa$yRotO >= 180.0F) this.owa$yRotO += 360.0F;

        while (this.owa$yBodyRotO - yBodyRot < -180.0F) this.owa$yBodyRotO += 360.0F;
        while (this.owa$yBodyRotO - yBodyRot >= 180.0F) this.owa$yBodyRotO -= 360.0F;

        float body = this.owa$yBodyRotO + deltaTime * (yBodyRot - this.owa$yBodyRotO);

        if (aBodyRot && !OwaStateHolder.isPreview(entity)) {
            boolean allowOverride = !entity.hasControllingPassenger()
                    && !(entity.getVehicle() instanceof AbstractBoatEntity)
                    && !(entity instanceof ArmorStandEntity);
            if (allowOverride) {
                float oldBody = state.bodyYaw;
                entity.bodyYaw = body;
                state.bodyYaw = body;
                //? if >=1.21.5 {
                state.relativeHeadYaw = MathHelper.wrapDegrees(state.relativeHeadYaw + oldBody - body);
                //?} else
                /^state.yawDegrees = MathHelper.wrapDegrees(state.yawDegrees + oldBody - body);^/
            }
        }

        float ismoving = entityRun_previous + (entityRun - entityRun_previous);

        if (aSpeedLimbAngle) {
            //? if >=1.21.5 {
            ismoving = entity.limbAnimator.getAmplitude(tickDelta) * entityRun;
            //?} else
            /^ismoving = entity.limbAnimator.getSpeed(tickDelta) * entityRun;^/
        }

        float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f);
        if (aVanillaSpeed) {
            //? if >=1.21.5 {
            var8 = entity.limbAnimator.getAnimationProgress(tickDelta);
            //?} else
            /^var8 = entity.limbAnimator.getPos(tickDelta);^/
        }

        Float onGround = owa$onGroundMap.get(entity);
        if (onGround == null) onGround = 1.0f;
        float ground = entity.isOnGround() ? 1.0f : 0.0f;
        if (aSmoothing) {
            onGround += (ground - onGround) * (aDecayFactor * deltaTime);
        } else {
            onGround = ground;
        }
        owa$onGroundMap.put(entity, onGround);

        if (aClassicRun) {
            double raw = OwaTimeScale.scaledTime() * 10.0;
            if (entity instanceof OwaSteve steve) {
                raw += steve.owaTimeOffs;
            }
            final double WRAP = 9431.625;
            raw = ((raw % WRAP) + WRAP) % WRAP;
            var8 = (float) raw;
            ismoving = 1.0f;
        }

        float bounceGround = classic ? 1.0f : onGround;
        float bounceTrig = classic
                ? MathHelper.sin(var8 * 0.6662F)
                : (bounceInverted ? MathHelper.sin(var8 * 0.6662F) : MathHelper.cos(var8 * 0.6662F));
        float var10 = -Math.abs(bounceTrig) * 5.0F * ismoving * aBounceHeight * bounceGround;

        SharedValueUtil.setVar10(entity, var10);
        SharedValueUtil.setVar8(entity, var8);
        SharedValueUtil.setIsMoving(entity, ismoving);
    }

    //? if >=1.21.9 {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
                     shift = At.Shift.AFTER))
    private void owa$applyBounce(LivingEntityRenderState state, MatrixStack pose, OrderedRenderCommandQueue collector, CameraRenderState camera, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
                     shift = At.Shift.AFTER))
    private void owa$applyBounce(LivingEntityRenderState state, MatrixStack pose, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
    ^///?}
        if (!enableMod) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof PlayerEntity)) return;
        if (!bounce && !OwaSteve.isClassicAnim(entity)) return;
        float var10 = SharedValueUtil.getVar10(entity);
        if (entity.getVehicle() instanceof LivingEntity rider) {
            var10 = SharedValueUtil.getVar10(rider);
            SharedValueUtil.setVar10(entity, var10);
        }
        pose.translate(0f, -var10 * 0.0625f, 0f);
    }

    @ModifyArg(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sqrt(F)F"),
            index = 0)
    private float owa$suppressVanillaDeathTilt(float f) {
        return (enableMod && damage) ? 0f : f;
    }

    //? if >=1.21.9 {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
                     shift = At.Shift.BEFORE))
    private void owa$applyDamageTilt(LivingEntityRenderState state, MatrixStack pose, OrderedRenderCommandQueue collector, CameraRenderState camera, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
                     shift = At.Shift.BEFORE))
    private void owa$applyDamageTilt(LivingEntityRenderState state, MatrixStack pose, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
    ^///?}
        if (!enableMod || !damage) return;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof PlayerEntity)) return;

        float partialTick = OwaStateHolder.getPartialTick(state);
        boolean dying = entity.getHealth() <= 0;
        float hurt = (float) entity.hurtTime - partialTick;
        if (hurt <= 0.0F && !dying) {
            if (entity.hurtTime <= 0 && entity.deathTime == 0) {
                owa$damageDirMap.remove(entity);
            }
            return;
        }

        float tilt;
        if (hurt < 0.0F) {
            tilt = 0.0F;
        } else {
            float denom = entity.maxHurtTime <= 0 ? 10.0F : (float) entity.maxHurtTime;
            float t = hurt / denom;
            tilt = MathHelper.sin(t * t * t * t * (float) Math.PI) * damageIntensity;
        }

        if (dying) {
            float deathFactor = ((float) entity.deathTime + partialTick) / 20.0F;
            tilt += deathFactor * deathFactor * 800.0F;
            if (tilt > 90.0F) tilt = 90.0F;
        }

        if (SharedValueUtil.consumeDamageDirInvalidation(entity)) {
            owa$damageDirMap.remove(entity);
        }
        double[] cached = owa$damageDirMap.get(entity);
        double dx, dz;
        float rotOffs;
        if (cached != null && lockRot) {
            dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2];
        } else {
            double tryDx = 0, tryDz = 0;
            float tryOffs = 0.0F;
            boolean have = false;

            if (!velocity) {
                DamageSource src = entity.getRecentDamageSource();
                Vec3d srcPos = src == null ? null : src.getPosition();
                if (srcPos != null) {
                    double ddx = srcPos.x - entity.getX();
                    double ddz = srcPos.z - entity.getZ();
                    if (ddx != 0 || ddz != 0) {
                        tryDx = ddx; tryDz = ddz; tryOffs = 0.0F; have = true;
                    }
                }
            }

            if (!have && (velocity || fallback)) {
                Vec3d v = entity.getVelocity();
                if (v.x != 0 || v.z != 0) {
                    tryDx = v.x; tryDz = v.z; tryOffs = 180.0F; have = true;
                }
            }

            if (!have) {
                if (cached != null) { dx = cached[0]; dz = cached[1]; rotOffs = (float) cached[2]; }
                else if (dying) {
                    float yaw = entity.getYaw();
                    dx = -Math.sin(Math.toRadians(yaw));
                    dz = Math.cos(Math.toRadians(yaw));
                    rotOffs = 180.0F;
                    owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
                }
                else return;
            } else {
                dx = tryDx; dz = tryDz; rotOffs = tryOffs;
                owa$damageDirMap.put(entity, new double[]{dx, dz, rotOffs});
            }
        }

        double angleDegrees = Math.toDegrees(Math.atan2(dz, dx));
        if (angleDegrees < 0) angleDegrees += 360;
        float directionAngle = (float) -angleDegrees + rotOffs;

        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(directionAngle));
        pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt));
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-directionAngle));
    }
}
*///?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
        //? if >=1.17
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
        //? if >=1.19.4 {
import net.minecraft.util.math.RotationAxis;
//?} else
/^import net.minecraft.util.math.Vec3f;^/
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.OwaTimeScale;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.render.entity.LivingEntityRenderer.getOverlay;
import static net.minecraft.client.util.GlfwUtil.getTime;
import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {


//    private T entity;


    double lastTickTime;

    float timeOffs = (float) Math.random() * 1239813.0F;
    float rot = (float) (Math.random() * Math.PI * 2.0D);
    //float speed = 1.5F;
    private float bobStrength = 1;
    private float f;
    Vec3d prevVelocity;

    float hurtframe = 0;
//    float onGround;

    //float entityRun_previous;

    float animStep;
    float animStepO;
    protected float oRun;
    protected float run;
    private float yBodyRotO;
    //private float yBodyRot;
    private float yRot;
    private float yRotO;
    private float lastX;
    private float lastZ;


    private final Map<LivingEntity, Double> dxMap = new HashMap<>();
    private final Map<LivingEntity, Double> dzMap = new HashMap<>();


//    double dx;
//    double dz;

    private final Map<LivingEntity, Float> animStepMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMapP = new HashMap<>();
    private final Map<LivingEntity, Float> lastAnimStepTimeMap = new HashMap<>();
    private final Map<LivingEntity, Float> bodMap = new HashMap<>();
    private final Map<LivingEntity, Float> onGroundMap = new HashMap<>();
    private final Map<LivingEntity, Integer> owa$lastRenderAgeMap = new HashMap<>();

    private final Map<LivingEntity, Float> headrotMap = new HashMap<>();

//    float headrot;


    private float rotOffs = 0f;

    private T storedEntity;



    //? if >=1.17 {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
                //?} else {
    /^protected LivingEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }
    ^///?}




    @Shadow
    protected abstract float getLyingAngle(T entity);


    @Shadow protected M model;

    @Shadow protected abstract float getHandSwingProgress(T entity, float tickDelta);

    @Shadow
    public static boolean shouldFlipUpsideDown(LivingEntity entity) {
        return false;
    }

    @Shadow protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow protected abstract void scale(T entity, MatrixStack matrices, float amount);

    @Shadow protected abstract boolean isVisible(T entity);

    @Shadow @Nullable
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    @Shadow protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow @Final protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected static float getYaw(Direction direction) {
        return 0;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void owa$hasLabelForSteve(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof tecna.oldwalkinganimation.OwaSteve) {
            cir.setReturnValue(entity.shouldRenderName());
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"))
    private void owa$preRenderApplyBody(T entity, float yaw, float partialTick, MatrixStack matrices, VertexConsumerProvider buf, int light, CallbackInfo ci) {
        if (!enableMod) return;
        if (!enableMobs && !(entity instanceof PlayerEntity)) return;
        if (tecna.oldwalkinganimation.OwaStateHolder.isPreview(entity)) return;
        boolean classic = OwaSteve.isClassicAnim(entity);
        boolean aBodyRot = classic ? true : bodyRot;
        if (!aBodyRot) return;
        //? if >=1.19.4
        if (entity.hasControllingPassenger()) return;
        if (entity.getVehicle() instanceof BoatEntity || entity instanceof ArmorStandEntity) return;
        Float cachedBody = bodMap.get(entity);
        if (cachedBody == null) return;
        // If the renderer hasn't rendered this entity for several ticks (e.g., local player went
        // into first person, or entity left frustum), the cached body yaw is stale - vanilla
        // updated entity.bodyYaw while we weren't watching. Sync to current bodyYaw so we don't
        // snap it back. Threshold of 5 ticks ignores normal per-tick rendering.
        Integer lastAge = owa$lastRenderAgeMap.get(entity);
        int currentAge = entity.age;
        if (lastAge == null || currentAge - lastAge > 5) {
            cachedBody = entity.bodyYaw;
            bodMap.put(entity, cachedBody);
        }
        owa$lastRenderAgeMap.put(entity, currentAge);
        entity.bodyYaw = entity.prevBodyYaw = cachedBody;
    }

    @ModifyArg(
            method = "setupTransforms",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;sqrt(F)F"
            ),
            index = 0
    )
    private float modifySqrtArgument(float f) {
        if (damage && enableMod) {
            return 0;
        } else {
            return f;
        }
    }


        @Inject(method = "setupTransforms", at = @At(value = "HEAD"))
    protected void setupTransforms(T entity,
                                   MatrixStack matrices,
                                   float animationProgress, float bodyYaw, float tickDelta,
                                   //? if >=1.20.5
                                   float scale,
                                   CallbackInfo ci) {



        if (enableMod) {

            boolean runCode;

            if (enableMobs) {
                runCode = true;
            } else {
                if (entity instanceof PlayerEntity) {
                    runCode = true;
                } else {
                    runCode = false;
                    SharedValueUtil.setVar10(entity, 0);
                }
            }

            if (runCode) {

                boolean classic = OwaSteve.isClassicAnim(entity);
                float aBounceHeight = classic ? 1.0F : bounceHeight;
                boolean aSpeedLimbAngle = classic ? false : speedLimbAngle;
                boolean aVanillaSpeed = classic ? false : vanillaSpeed;
                boolean aClassicRun = classic ? true : classicRun;
                float aSpeed = classic ? 1.0F : speed;
                boolean aBodyRot = classic ? true : bodyRot;
                boolean aSmoothing = classic ? true : smoothing;
                float aDecayFactor = classic ? 0.3F : decayFactor;

                float currentTime = (float) getTime();


                Float lastAnimStepTime = lastAnimStepTimeMap.get(entity);
                if (lastAnimStepTime == null) {
                    lastAnimStepTime = currentTime;
                    lastAnimStepTimeMap.put(entity, lastAnimStepTime);
                }

                float tickScale = OwaTimeScale.scaleFactor();
                float deltaTime = (currentTime - lastAnimStepTime) * 20 * tickScale;


                if (deltaTime > 3f) {
                    deltaTime = 3f;
                }

                if (deltaTime < 0.000000000000001f) {
                    deltaTime = 0.000000000000001f;
                }

                Float yBodyRot = bodMap.get(entity);
                if (yBodyRot == null) {
                    // Init from the entity's actual look direction, not 0 (south). On spawn,
                    // entity.bodyYaw may still be the default 0 even though the player's actual
                    // yaw is non-zero. Using getYaw avoids the "spawn facing south" snap.
                    yBodyRot = entity.getYaw(tickDelta);
                    bodMap.put(entity, yBodyRot);
                }


                this.yRot = entity.getYaw(tickDelta);

                this.yBodyRotO = yBodyRot;
                this.yRotO = this.yRot;

                //? if >=1.15 {
                float var1 = (float) entity.getX() - (float) entity.prevX;
                float var2 = (float) entity.getZ() - (float) entity.prevZ;
                //?} else {
                /^float var1 = (float) entity.x - (float) entity.prevX;
                float var2 = (float) entity.z - (float) entity.prevZ;
                ^///?}
                float var3 = MathHelper.sqrt(var1 * var1 + var2 * var2);


                float var4 = this.yBodyRotO + (yBodyRot - this.yBodyRotO);
                ;
                float var5 = 0.0F;
                this.oRun = this.run;
                float var6 = 0.0F;



                float ST = speedTrigger;

                if (entity instanceof HorseEntity) {
                    ST = speedTrigger * 0.5f;
                }

                if (!(var3 <= ST)) {

                    if (var3 >= maxSpeed && maxSpeed != 1) {
                        var3 = maxSpeed;
                    }

                    var6 = 1.0F;
                    var5 = var3 * 3.0F;
                    var4 = (float) Math.atan2((double) var2, (double) var1) * 180.0F / 3.1415927F - 90.0F;
                }


                if (entity.getVehicle() != null && !ridingMobAnimation) {
                    var6 = 0.0F;
                }


                Float entityRun = runMap.get(entity);
                if (entityRun == null) {
                    entityRun = 0.0f;
                    runMap.put(entity, entityRun);
                }


                Float entityRun_previous = runMapP.get(entity);
                if (entityRun_previous == null) {
                    entityRun_previous = entityRun;
                    runMapP.put(entity, entityRun_previous);
                }


                if (aSmoothing) {
                    entityRun += (var6 - entityRun) * (aDecayFactor * deltaTime);
                } else {
                    entityRun = var6;
                }


                runMap.put(entity, entityRun);


                this.run += (var6 - this.run) * (0.3F * deltaTime);

                for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) {
                }

                while (var1 >= 180.0F) {
                    var1 -= 360.0F;
                }

                // Per-frame Classic c0.30 Mob.tick smoothing. Cap dt at 1.0 (= one tick worth)
                // so a stutter can't blow up a single step.
                float owa$bodyDt = deltaTime > 1.0F ? 1.0F : deltaTime;

                // Order is reordered from Classic (Classic does smooth, clamp, drag). Both the
                // smooth pass (toward motion direction) and the drag pass (toward head) run
                // BEFORE the hard clamp, then the clamp lands the body on a dt-independent
                // boundary. Original order made the post-frame body position depend on dt
                // because drag's `var1 * 0.1 * dt` ran AFTER clamp, so its dt-dependent step
                // showed through frame-to-frame as ~0.5° jitter at the rotation limit. With
                // smooth+drag merged before clamp, the visible position when the limit is hit
                // is exactly `head ± 75°` regardless of dt. Behavior away from the limit is
                // mathematically equivalent to Classic (both passes still pull body at rate
                // 0.1/tick toward their respective targets).
                yBodyRot += var1 * (0.1F * owa$bodyDt);          // smooth toward motion
                var1 = this.yRot - yBodyRot;
                while (var1 < -180.0F) var1 += 360.0F;
                while (var1 >= 180.0F) var1 -= 360.0F;
                yBodyRot += var1 * (0.1F * owa$bodyDt);          // drag toward head (pre-clamp)

                // Clamp last: head-body diff forced to ±75° gives a dt-independent end state.
                var1 = this.yRot - yBodyRot;
                while (var1 < -180.0F) var1 += 360.0F;
                while (var1 >= 180.0F) var1 -= 360.0F;
                boolean var7 = var1 < -90.0F || var1 >= 90.0F;
                if (var1 < -75.0F) var1 = -75.0F;
                if (var1 >= 75.0F) var1 = 75.0F;
                yBodyRot = this.yRot - var1;

                bodMap.put(entity, yBodyRot);
                if (var7) {
                    var5 = -var5;
                }


                Float entityAnimStep = animStepMap.get(entity);
                if (entityAnimStep == null) {
                    entityAnimStep = (float) Math.random(); // Initialize random animStep
                    animStepMap.put(entity, entityAnimStep);
                }

                float animSpeed = aSpeed;

                if (entity instanceof HorseEntity) {
                    animSpeed = aSpeed * 0.6f;
                }

                float previousAnimStep = entityAnimStep; // Store current animStep as previous
                entityAnimStep += (var5 * (animSpeed * deltaTime));
                animStepMap.put(entity, entityAnimStep);

                lastAnimStepTime = currentTime;
                lastAnimStepTimeMap.put(entity, lastAnimStepTime);


                while (this.yRot - this.yRotO < -180.0F) {
                    this.yRotO -= 360.0F;
                }

                while (this.yRot - this.yRotO >= 180.0F) {
                    this.yRotO += 360.0F;
                }


                while (this.yBodyRotO - yBodyRot < -180.0F) {
                    this.yBodyRotO += 360.0F;
                }

                while (this.yBodyRotO - yBodyRot >= 180.0F) {
                    this.yBodyRotO -= 360.0F;
                }



                float body = this.yBodyRotO + deltaTime * (yBodyRot - this.yBodyRotO);




                if (aBodyRot && !tecna.oldwalkinganimation.OwaStateHolder.isPreview(entity)) {
                    if (
                            //? if >=1.19.4
                            !entity.hasControllingPassenger() &&
                                    !(entity.getVehicle() instanceof BoatEntity) && !(entity instanceof ArmorStandEntity))
                        //? if >=1.16.4 {
                        entity.bodyYaw = entity.prevBodyYaw = body;



                    //?} else
                        /^entity.setYaw(body);^/
                }
                float ismoving = entityRun_previous + (entityRun - entityRun_previous);


                if (aSpeedLimbAngle) {
                    //? if >=1.21.6 {
                    ismoving = entity.limbAnimator.getAmplitude(tickDelta) * entityRun;
                    //?} else if >=1.19.4
                    /^ismoving = entity.limbAnimator.getSpeed(tickDelta) * entityRun;^/
                    //? if <1.19.4
                    /^ismoving = entity.limbDistance;^/

                }


                float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f);

                if (aVanillaSpeed) {
                    //? if >=1.21.6 {
                    var8 = entity.limbAnimator.getAnimationProgress(tickDelta);
                    //?} else if >=1.19.4
                    /^var8 = entity.limbAnimator.getPos(tickDelta);^/
                    //? if <1.19.4
                    /^var8 = entity.handSwingProgress;^/
                }

                Float onGround = onGroundMap.get(entity);
                if (onGround == null) {
                    onGround = 1.0f;
                }

                float ground = 0.0f;
                if (//? if >=1.16.4 {
                entity.isOnGround()
                //?} else
                /^entity.onGround^/
                ) {
                ground = 1.0f;
                }
                if (aSmoothing) {
                    onGround += (ground - onGround) * (aDecayFactor * deltaTime);
                } else {
                    onGround = ground;
                }

                onGroundMap.put(entity, onGround);




                if (aClassicRun) {
                    double raw = OwaTimeScale.scaledTime() * 10.0;
                    if (entity instanceof OwaSteve steve) {
                        raw += steve.owaTimeOffs;
                    }
                    final double WRAP = 9431.625;
                    raw = ((raw % WRAP) + WRAP) % WRAP;
                    var8 = (float) raw;
                    ismoving = 1.0f;
                }

                float var9;
                var9 = 0.0625F;
                float bounceGround = classic ? 1.0f : onGround;
                float bounceTrig = classic
                        ? (float) Math.sin(var8 * 0.6662F)
                        : (bounceInverted ? (float) Math.sin(var8 * 0.6662F) : MathHelper.cos(var8 * 0.6662F));
                float var10 = -Math.abs(bounceTrig) * 5.0F * ismoving * aBounceHeight * bounceGround;



                float var11;
                if ((var11 = (float) entity.hurtTime - tickDelta) > 0.0F || entity.getHealth() <= 0) {
                    if (var11 < 0.0F) {
                        var11 = 0.0F;
                    } else {
                        var11 = MathHelper.sin((var11 /= (float)
                    //? if >=1.15 {
                entity.maxHurtTime
                //?} else
                         /^entity.field_6254^/

                        ) * var11 * var11 * var11 * 3.1415927F) * damageIntensity;
                    }


                    float var12 = 0.0F;
                    if (entity.getHealth() <= 0) {
                        var12 = ((float) entity.deathTime + tickDelta) / 20.0F;
                        if ((var11 += var12 * var12 * 800.0F) > this.getLyingAngle(entity)) {
                            var11 = this.getLyingAngle(entity);
                        }
                    }


                    if (damage) {
                        if (SharedValueUtil.consumeDamageDirInvalidation(entity)) {
                            dxMap.remove(entity);
                            dzMap.remove(entity);
                        }
                        Double dx = dxMap.get(entity);
                        Double dz = dzMap.get(entity);
                        try {

                            if (dx == null || !lockRot) {

                                if (velocity) {
                                    if (entity.getVelocity() != prevVelocity && (!(entity.getVelocity().x == 0) && !(entity.getVelocity().z == 0))) {
                                        rotOffs = 180;
                                        dx = entity.getVelocity().x;
                                        dz = entity.getVelocity().z;
                                    }
                                } else {
                                    rotOffs = 0;
                                    //? if >=1.15 {
                                    dx = entity.getRecentDamageSource().getPosition().x - entity.getPos().x;
                                    dz = entity.getRecentDamageSource().getPosition().z - entity.getPos().z;
                                     //?} else {
                                    /^dx = entity.getRecentDamageSource().method_5510().x - entity.getPos().x;
                                    dz = entity.getRecentDamageSource().method_5510().z - entity.getPos().z;
                                    ^///?}
                                }


                                dxMap.put(entity, dx);
                                dzMap.put(entity, dz);
                            }


                            double angleRadians = Math.atan2(dz, dx);
                            double angleDegrees = Math.toDegrees(angleRadians);

                            if (angleDegrees < 0) {
                                angleDegrees += 360;
                            }

                            float directionAngle = (float) -angleDegrees + rotOffs;



                            //? if >=1.19.4 {
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(directionAngle));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var11));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(directionAngle)));
                                     //?} else {
                            /^matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(directionAngle));
                            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(var11));
                            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(directionAngle)));
                            ^///?}









                        } catch (NullPointerException ignored) {

                            try {
                                if (fallback) {


                                    if (dx == null || !lockRot) {


                                        if (entity.getVelocity() != prevVelocity && (!(entity.getVelocity().x == 0) && !(entity.getVelocity().z == 0))) {
                                            rotOffs = 180;
                                            dx = entity.getVelocity().x;
                                            dz = entity.getVelocity().z;
                                        }


                                        dxMap.put(entity, dx);
                                        dzMap.put(entity, dz);
                                    }


                                    double angleRadians = Math.atan2(dz, dx);
                                    double angleDegrees = Math.toDegrees(angleRadians);

                                    if (angleDegrees < 0) {
                                        angleDegrees += 360;
                                    }

                                    float directionAngle = (float) -angleDegrees + rotOffs;



                                    //? if >=1.19.4 {
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(directionAngle));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var11));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(directionAngle)));
                                     //?} else {
                                    /^matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(directionAngle));
                                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(var11));
                                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(directionAngle)));
                                    ^///?}


                                }
                            } catch (NullPointerException ignored1) {
                            }
                        }


                    }

                }

                if (entity.hurtTime <= 0 && entity.deathTime == 0) {
                    dxMap.put(entity, null);
                    dzMap.put(entity, null);
                    hurtframe = 0;
                }



                SharedValueUtil.setVar10(entity, var10);
                SharedValueUtil.setVar8(entity, var8);
                SharedValueUtil.setIsMoving(entity, ismoving);


                prevVelocity = entity.getVelocity();

                if (bounce) {


                    try {
                        if (entity.getVehicle() != null) {

                            var10 = SharedValueUtil.getVar10((LivingEntity) entity.getVehicle());

                            SharedValueUtil.setVar10(entity, var10);

                            //? if >=1.15 {
                            matrices.translate(0, (-var10 * var9), 0);
                             //?} else
                            /^GlStateManager.translatef(0, (-var10 * var9), 0);^/




                        } else {
                            //? if >=1.15 {
                            matrices.translate(0, (-var10 * var9), 0);
                             //?} else
                            /^GlStateManager.translatef(0, (-var10 * var9), 0);^/
                        }
                    } catch (ClassCastException ignored) {

                    }

                }


            }

        }



    }


}
*///?}
