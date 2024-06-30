package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalModel.class)
public abstract class AnimalModelMixin <E extends LivingEntity> extends EntityModel<E> {

	@Shadow @Final private float childBodyYOffset;

	@Inject(method = "render", at = @At("HEAD"))

private void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {

		//matrices.translate(0.0, (double)(this.childBodyYOffset / 30.0F), 0.0);

	}
}

//If runing 1.14 then run this code instead


//@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;roll:F"))
//
//		private void setAngles(T livingEntity, float f, float g, float h, float i, float j, float k, CallbackInfo ci) {
//
//		if (livingEntity instanceof PlayerEntity) {
//				this.getArm(Arm.RIGHT).pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 2.0F * g;
//				this.getArm(Arm.RIGHT).roll = (MathHelper.cos(f * 0.2312F) + 1.0F) * 1.0F * g;
//				this.getArm(Arm.LEFT).roll = (MathHelper.cos(f * 0.2812F) - 1.0F) * 1.0F * g;
//				this.getArm(Arm.LEFT).pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g;
//		}
//	}


//}
