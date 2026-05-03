package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin {

    @Shadow public ModelPart head;
    @Shadow public ModelPart hat;
    @Shadow public ModelPart body;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftLeg;
    @Shadow public ModelPart rightLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(HumanoidRenderState state, CallbackInfo ci) {
        if (!enableMod) return;

        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof Player)) return;

        boolean classic = OwaSteve.isClassicAnim(entity);
        boolean classicRdLimbs = classic && rdStyleLimbs;
        boolean classicRdHead = classic && rdHeadBob;
        boolean aTpose = classic ? false : tpose;
        boolean aArms = classic ? true : arms;
        boolean aSneakPose17 = classic ? true : sneakPose17;

        float f = state.walkAnimationPos;
        float g = state.walkAnimationSpeed;

        float l = 1.0F;
        if (state.isFallFlying) {
            l = (float) entity.getDeltaMovement().lengthSqr();
            l /= 0.2F;
            l *= l * l;
        }
        if (l < 1.0F) l = 1.0F;

        this.rightArm.xRot += -Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * g * 0.5F / l;
        this.leftArm.xRot += -Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / l;
        this.rightLeg.xRot += -Mth.cos(f * 0.6662F) * 1.4F * g / l;
        this.leftLeg.xRot += -Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * g / l;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        if (aTpose) {
            this.leftArm.zRot += -tposeAngle - (-tposeAngle * ismoving);
            this.rightArm.zRot += tposeAngle - (tposeAngle * ismoving);
        }

        if (classicRdLimbs) {
            this.rightArm.zRot += ((float) Math.sin(var8 * 0.2312F) + 1.0F) * ismoving;
            this.leftArm.zRot += ((float) Math.sin(var8 * 0.2812F) - 1.0F) * ismoving;
            this.rightArm.xRot += (float) Math.sin(var8 * 0.6662F + Mth.PI) * 2.0F * ismoving;
            this.leftArm.xRot += (float) Math.sin(var8 * 0.6662F) * 2.0F * ismoving;
            this.rightLeg.xRot += (float) Math.sin(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.xRot += (float) Math.sin(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        } else {
            if (aArms) {
                this.rightArm.zRot += (Mth.cos(var8 * 0.2312F) + 1.0F) * ismoving;
                this.leftArm.zRot += (Mth.cos(var8 * 0.2812F) - 1.0F) * ismoving;
            }

            float armAmp = aArms ? 2.0F : 1.0F;
            this.rightArm.xRot += Mth.cos(var8 * 0.6662F + Mth.PI) * armAmp * ismoving;
            this.leftArm.xRot += Mth.cos(var8 * 0.6662F) * armAmp * ismoving;
            this.rightLeg.xRot += Mth.cos(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.xRot += Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        }

        if (classicRdHead) {
            head.yRot += (float) Math.sin(var8 * 0.83D) * ismoving;
            head.xRot += (float) Math.sin(var8) * 0.8F * ismoving;
        } else if (!classic && headBob) {
            head.yRot += (float) Math.sin(var8 * headSpeed * 0.83D) * headAmount * ismoving;
            head.xRot += (float) Math.sin(var8 * headSpeed) * 0.8F * headAmount * ismoving;
        }

        if (aSneakPose17 && state.isCrouching) {
            this.head.y -= 3.2F;
            this.hat.y -= 3.2F;
            this.body.y -= 3.2F;
            this.leftArm.y -= 3.2F;
            this.rightArm.y -= 3.2F;
            this.leftLeg.y -= 3.0F;
            this.rightLeg.y -= 3.0F;
        }
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

    @Shadow public ModelPart head;
    @Shadow public ModelPart hat;
    @Shadow public ModelPart body;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftLeg;
    @Shadow public ModelPart rightLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("RETURN"))
    private void owa$setupAnim(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!enableMod) return;
        if (!enableMobs && !(livingEntity instanceof Player)) return;

        boolean classic = OwaSteve.isClassicAnim(livingEntity);
        boolean classicRdLimbs = classic && rdStyleLimbs;
        boolean classicRdHead = classic && rdHeadBob;
        boolean aTpose = classic ? false : tpose;
        boolean aArms = classic ? true : arms;
        boolean aSneakPose17 = classic ? true : sneakPose17;

        boolean bl = livingEntity.getFallFlyingTicks() > 4;

        float l = 1.0F;
        if (bl) {
            l = (float) livingEntity.getDeltaMovement().lengthSqr();
            l /= 0.2F;
            l *= l * l;
        }
        if (l < 1.0F) l = 1.0F;

        this.rightArm.xRot += -Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * g * 0.5F / l;
        this.leftArm.xRot += -Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / l;
        this.rightLeg.xRot += -Mth.cos(f * 0.6662F) * 1.4F * g / l;
        this.leftLeg.xRot += -Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * g / l;

        float var8 = SharedValueUtil.getVar8(livingEntity);
        float ismoving = SharedValueUtil.getIsMoving(livingEntity);

        if (aTpose) {
            this.leftArm.zRot += -tposeAngle - (-tposeAngle * ismoving);
            this.rightArm.zRot += tposeAngle - (tposeAngle * ismoving);
        }

        if (classicRdLimbs) {
            this.rightArm.zRot += ((float) Math.sin(var8 * 0.2312F) + 1.0F) * ismoving;
            this.leftArm.zRot += ((float) Math.sin(var8 * 0.2812F) - 1.0F) * ismoving;
            this.rightArm.xRot += (float) Math.sin(var8 * 0.6662F + Mth.PI) * 2.0F * ismoving;
            this.leftArm.xRot += (float) Math.sin(var8 * 0.6662F) * 2.0F * ismoving;
            this.rightLeg.xRot += (float) Math.sin(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.xRot += (float) Math.sin(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        } else {
            if (aArms) {
                this.rightArm.zRot += (Mth.cos(var8 * 0.2312F) + 1.0F) * ismoving;
                this.leftArm.zRot += (Mth.cos(var8 * 0.2812F) - 1.0F) * ismoving;
            }

            float armAmp = aArms ? 2.0F : 1.0F;
            this.rightArm.xRot += Mth.cos(var8 * 0.6662F + Mth.PI) * armAmp * ismoving;
            this.leftArm.xRot += Mth.cos(var8 * 0.6662F) * armAmp * ismoving;
            this.rightLeg.xRot += Mth.cos(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.xRot += Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        }

        if (classicRdHead) {
            head.yRot += (float) Math.sin(var8 * 0.83D) * ismoving;
            head.xRot += (float) Math.sin(var8) * 0.8F * ismoving;
            hat.yRot += (float) Math.sin(var8 * 0.83D) * ismoving;
            hat.xRot += (float) Math.sin(var8) * 0.8F * ismoving;
        } else if (!classic && headBob) {
            head.yRot += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
            head.xRot += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
            hat.yRot += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
            hat.xRot += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
        }

        if (aSneakPose17 && livingEntity.isCrouching()) {
            this.head.y -= 3.2F;
            this.hat.y -= 3.2F;
            this.body.y -= 3.2F;
            this.leftArm.y -= 3.2F;
            this.rightArm.y -= 3.2F;
            this.leftLeg.y -= 3.0F;
            this.rightLeg.y -= 3.0F;
        }
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {

    @Shadow public ModelPart head;
    @Shadow public ModelPart hat;
    @Shadow public ModelPart body;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftLeg;
    @Shadow public ModelPart rightLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(BipedEntityRenderState state, CallbackInfo ci) {
        if (!enableMod) return;

        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return;
        if (!enableMobs && !(entity instanceof PlayerEntity)) return;

        boolean classic = OwaSteve.isClassicAnim(entity);
        boolean classicRdLimbs = classic && rdStyleLimbs;
        boolean classicRdHead = classic && rdHeadBob;
        boolean aTpose = classic ? false : tpose;
        boolean aArms = classic ? true : arms;
        boolean aSneakPose17 = classic ? true : sneakPose17;

        //? if >=1.21.5 {
        float f = state.limbSwingAnimationProgress;
        float g = state.limbSwingAmplitude;
        //?} else {
        /^float f = state.limbFrequency;
        float g = state.limbAmplitudeMultiplier;
        ^///?}

        float l = 1.0F;
        if (state.isGliding) {
            l = (float) entity.getVelocity().lengthSquared();
            l /= 0.2F;
            l *= l * l;
        }
        if (l < 1.0F) l = 1.0F;

        this.rightArm.pitch += -MathHelper.cos(f * 0.6662F + (float) Math.PI) * 2.0F * g * 0.5F / l;
        this.leftArm.pitch += -MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / l;
        this.rightLeg.pitch += -MathHelper.cos(f * 0.6662F) * 1.4F * g / l;
        this.leftLeg.pitch += -MathHelper.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g / l;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        if (aTpose) {
            this.leftArm.roll += -tposeAngle - (-tposeAngle * ismoving);
            this.rightArm.roll += tposeAngle - (tposeAngle * ismoving);
        }

        if (classicRdLimbs) {
            this.rightArm.roll += ((float) Math.sin(var8 * 0.2312F) + 1.0F) * ismoving;
            this.leftArm.roll += ((float) Math.sin(var8 * 0.2812F) - 1.0F) * ismoving;
            this.rightArm.pitch += (float) Math.sin(var8 * 0.6662F + (float) Math.PI) * 2.0F * ismoving;
            this.leftArm.pitch += (float) Math.sin(var8 * 0.6662F) * 2.0F * ismoving;
            this.rightLeg.pitch += (float) Math.sin(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.pitch += (float) Math.sin(var8 * 0.6662F + (float) Math.PI) * 1.4F * ismoving;
        } else {
            if (aArms) {
                this.rightArm.roll += (MathHelper.cos(var8 * 0.2312F) + 1.0F) * ismoving;
                this.leftArm.roll += (MathHelper.cos(var8 * 0.2812F) - 1.0F) * ismoving;
            }

            float armAmp = aArms ? 2.0F : 1.0F;
            this.rightArm.pitch += MathHelper.cos(var8 * 0.6662F + (float) Math.PI) * armAmp * ismoving;
            this.leftArm.pitch += MathHelper.cos(var8 * 0.6662F) * armAmp * ismoving;
            this.rightLeg.pitch += MathHelper.cos(var8 * 0.6662F) * 1.4F * ismoving;
            this.leftLeg.pitch += MathHelper.cos(var8 * 0.6662F + (float) Math.PI) * 1.4F * ismoving;
        }

        if (classicRdHead) {
            head.yaw += (float) Math.sin(var8 * 0.83D) * ismoving;
            head.pitch += (float) Math.sin(var8) * 0.8F * ismoving;
        } else if (!classic && headBob) {
            head.yaw += (float) Math.sin(var8 * headSpeed * 0.83D) * headAmount * ismoving;
            head.pitch += (float) Math.sin(var8 * headSpeed) * 0.8F * headAmount * ismoving;
        }

        if (aSneakPose17 && state.isInSneakingPose) {
            //? if >=1.21.5 {
            this.head.originY -= 3.2F;
            this.hat.originY -= 3.2F;
            this.body.originY -= 3.2F;
            this.leftArm.originY -= 3.2F;
            this.rightArm.originY -= 3.2F;
            this.leftLeg.originY -= 3.0F;
            this.rightLeg.originY -= 3.0F;
            //?} else {
            /^this.head.pivotY -= 3.2F;
            this.hat.pivotY -= 3.2F;
            this.body.pivotY -= 3.2F;
            this.leftArm.pivotY -= 3.2F;
            this.rightArm.pivotY -= 3.2F;
            this.leftLeg.pivotY -= 3.0F;
            this.rightLeg.pivotY -= 3.0F;
            ^///?}
        }
    }
}
*///?} else {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteve;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity, C extends Camera> {


	@Shadow public ModelPart head;

	@Shadow public ModelPart body;

	@Shadow public ModelPart leftLeg;

	@Shadow public ModelPart rightLeg;

	@Shadow public ModelPart rightArm;

	@Shadow public ModelPart leftArm;

	@Shadow @Final public ModelPart hat;

	//? if >=1.15 {
	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "RETURN"))
	 private void setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
	  //?} else {
	/^@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "RETURN"))
	private void setAngles(LivingEntity livingEntity, float f, float g, float h, float i, float j, float k, CallbackInfo ci) {
		^///?}


		if (enableMod) {

			boolean runCode;

			if (enableMobs) {
				if (livingEntity instanceof LivingEntity) {
					runCode = true;
				} else {
					runCode = false;
				}
			} else {
				if (livingEntity instanceof PlayerEntity) {
					runCode = true;
				} else {
					runCode = false;
				}
			}

			if (runCode) {

				boolean classic = OwaSteve.isClassicAnim(livingEntity);
				boolean classicRdLimbs = classic && rdStyleLimbs;
				boolean classicRdHead = classic && rdHeadBob;
				boolean aTpose = classic ? false : tpose;
				boolean aArms = classic ? true : arms;
				boolean aSneakPose17 = classic ? true : sneakPose17;


				//? if >=1.20.5 {
				boolean bl = livingEntity.getFallFlyingTicks() > 4;
				//?} else
				/^boolean bl = livingEntity.getRoll() > 4;^/



				float l = 1.0F;
				if (bl) {
					l = (float) livingEntity.getVelocity().lengthSquared();
					l /= 0.2F;
					l *= l * l;
				}

				if (l < 1.0F) {
					l = 1.0F;
				}


//Cancel Vanilla Animation

				this.rightArm.pitch += -MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g * 0.5F / l;
				this.leftArm.pitch += -MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / l;
				this.rightLeg.pitch += -MathHelper.cos(f * 0.6662F) * 1.4F * g / l;
				this.leftLeg.pitch += -MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g / l;


				float var8 = SharedValueUtil.getVar8(livingEntity);
				float ismoving = SharedValueUtil.getIsMoving(livingEntity);



				if (aTpose) {
					this.leftArm.roll += -tposeAngle - (-tposeAngle * ismoving);
					this.rightArm.roll += tposeAngle - (tposeAngle * ismoving);
				}

				if (classicRdLimbs) {
					this.rightArm.roll += ((float) Math.sin(var8 * 0.2312F) + 1.0F) * ismoving;
					this.leftArm.roll += ((float) Math.sin(var8 * 0.2812F) - 1.0F) * ismoving;
					this.rightArm.pitch += (float) Math.sin(var8 * 0.6662F + 3.1415927F) * 2.0F * ismoving;
					this.leftArm.pitch += (float) Math.sin(var8 * 0.6662F) * 2.0F * ismoving;
					this.rightLeg.pitch += (float) Math.sin(var8 * 0.6662F) * 1.4F * ismoving;
					this.leftLeg.pitch += (float) Math.sin(var8 * 0.6662F + 3.1415927F) * 1.4F * ismoving;
				} else {
					if (aArms) {
						this.rightArm.roll += (MathHelper.cos(var8 * 0.2312F) + 1.0F) * ismoving;
						this.leftArm.roll += (MathHelper.cos(var8 * 0.2812F) - 1.0F) * ismoving;
					}

					float armAmp = aArms ? 2.0F : 1.0F;
					this.rightArm.pitch += MathHelper.cos(var8 * 0.6662F + 3.1415927F) * armAmp * ismoving;
					this.leftArm.pitch += MathHelper.cos(var8 * 0.6662F) * armAmp * ismoving;
					this.rightLeg.pitch += MathHelper.cos(var8 * 0.6662F) * 1.4F * ismoving;
					this.leftLeg.pitch += MathHelper.cos(var8 * 0.6662F + 3.1415927F) * 1.4F * ismoving;
				}


				if (classicRdHead) {
					head.yaw += (float) Math.sin(var8 * 0.83D) * ismoving;
					head.pitch += (float) Math.sin(var8) * 0.8F * ismoving;
					hat.yaw += (float) Math.sin(var8 * 0.83D) * ismoving;
					hat.pitch += (float) Math.sin(var8) * 0.8F * ismoving;
				} else if (!classic && headBob) {
					head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
					head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;

					hat.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
					hat.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
				}

				if (aSneakPose17 && livingEntity.isInSneakingPose()) {
					this.head.pivotY -= 3.2F;
					this.hat.pivotY -= 3.2F;
					this.body.pivotY -= 3.2F;
					this.leftArm.pivotY -= 3.2F;
					this.rightArm.pivotY -= 3.2F;
					this.leftLeg.pivotY -= 3.0F;
					this.rightLeg.pivotY -= 3.0F;
				}

			}
	}

	}



}
*///?}
