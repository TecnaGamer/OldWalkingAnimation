package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity, C extends Camera> {


	@Shadow public ModelPart head;

	@Shadow public ModelPart leftLeg;

	@Shadow public ModelPart rightLeg;

	@Shadow public ModelPart rightArm;

	@Shadow public ModelPart leftArm;

	@Final public ModelPart hat;

	//? if >=1.15 {
	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "RETURN"))
	 private void setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
	  //?} else {
	/*@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "RETURN"))
	private void setAngles(LivingEntity livingEntity, float f, float g, float h, float i, float j, float k, CallbackInfo ci) {
		*///?}


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



				//? if >=1.20.6 {
				boolean bl = livingEntity.getFallFlyingTicks() > 4;
				//?} else
				/*boolean bl = livingEntity.getRoll() > 4;*/



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



				if (tpose) {
					if (arms) {
						this.leftArm.roll += -tposeAngle - (-tposeAngle * ismoving);
						this.rightArm.roll += tposeAngle - (tposeAngle * ismoving);
					} else {
						this.leftArm.roll += -tposeAngle;
						this.rightArm.roll += tposeAngle;
					}
				}
				if (arms) {
					this.rightArm.roll += (MathHelper.cos(var8 * 0.2312F) + 1.0F) * ismoving;
					this.leftArm.roll += (MathHelper.cos(var8 * 0.2812F) - 1.0F) * ismoving;
				}

				this.rightArm.pitch += MathHelper.cos(var8 * 0.6662F + 3.1415927F) * 2.0F * ismoving;
				this.leftArm.pitch += MathHelper.cos(var8 * 0.6662F) * 2.0F * ismoving;
				this.rightLeg.pitch += MathHelper.cos(var8 * 0.6662F) * 1.4F * ismoving;
				this.leftLeg.pitch += MathHelper.cos(var8 * 0.6662F + 3.1415927F) * 1.4F * ismoving;


//				this.rightArm.roll += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
//				this.leftArm.roll -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
//				this.rightArm.pitch += MathHelper.sin(var3 * 0.067F) * 0.05F;
//				this.leftArm.pitch -= MathHelper.sin(var3 * 0.067F) * 0.05F;


//
//				getArm(Arm.RIGHT).pitch += (float) Math.sin(f * 0.6662D + Math.PI) * 2.0F * g;
//				getArm(Arm.RIGHT).roll += (float) (Math.sin(f * 0.2312D) + 1.0D) * 1.0F * g;
//				getArm(Arm.LEFT).pitch += (float) Math.sin(f * 0.6662) * 2.0F * g;
//				getArm(Arm.LEFT).roll += (float) (Math.sin(f * 0.2812) - 1.0) * 1.0F * g;
//
//				leftLeg.pitch += (float) Math.sin(f * 0.6662D) * 1.4F * g;
//				rightLeg.pitch += (float) Math.sin(f * 0.6662D + Math.PI) * 1.4F * g;


				if (headBob) {
					head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
					head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;

					hat.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
					hat.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
				}

			}
	}

	}



}
