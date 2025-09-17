package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin<T extends Entity> {



	@Shadow @Final protected ModelPart head;

	@Shadow @Final protected ModelPart rightHindLeg;

	@Shadow @Final protected ModelPart leftHindLeg;

	@Shadow @Final protected ModelPart rightFrontLeg;

	@Shadow @Final protected ModelPart leftFrontLeg;

	@Inject(method = "setAngles", at = @At(value = "RETURN"))


		//? if >=1.15 {
	private void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		//?} else
		/*void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {*/


		if (enableMod && enableMobs) {

			float var8 = SharedValueUtil.getVar8((LivingEntity) entity);
			float ismoving = SharedValueUtil.getIsMoving((LivingEntity) entity);

//			try {

				this.rightHindLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
				this.leftHindLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
				this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
				this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;

//			} catch (NullPointerException nullPointerException) {
//				System.out.println(nullPointerException);
//			}


			if (headBob) {
				head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
				head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
			}


	}

	}


}




