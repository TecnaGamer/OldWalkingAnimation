package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin114<T extends LivingEntity> {




//	@Shadow protected abstract ModelPart getArm(Arm arm);
//
//	@Inject(method = "setAngles*", at = @At(value = "FIELD",
//			target = "Lnet/minecraft/client/model/ModelPart;roll:F",
//			ordinal = 1,
//			shift = At.Shift.AFTER))
//
//	private void setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
//
//		if (livingEntity instanceof PlayerEntity) {
//
//			this.getArm(Arm.RIGHT).pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g;
//			this.getArm(Arm.RIGHT).roll = (MathHelper.cos(f * 0.2312F) + 1.0F) * 1.0F * g;
//			this.getArm(Arm.LEFT).roll = (MathHelper.cos(f * 0.2812F) - 1.0F) * 1.0F * g;
//			this.getArm(Arm.LEFT).pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g;
//
//		}
//
//
//	}
//
//}




//If runing 1.14 then run this code instead


	@Shadow protected abstract ModelPart getArm(Arm arm);

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F"))

		private void setAngles(T livingEntity, float f, float g, float h, float i, float j, float k, CallbackInfo ci) {

		if (livingEntity instanceof PlayerEntity) {
				this.getArm(Arm.RIGHT).pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g;
				this.getArm(Arm.RIGHT).roll = (MathHelper.cos(f * 0.2312F) + 1.0F) * 1.0F * g;
				this.getArm(Arm.LEFT).roll = (MathHelper.cos(f * 0.2812F) - 1.0F) * 1.0F * g;
				this.getArm(Arm.LEFT).pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g;
		}
	}


}
