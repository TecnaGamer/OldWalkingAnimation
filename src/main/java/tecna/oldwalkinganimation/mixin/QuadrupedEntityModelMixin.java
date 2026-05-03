package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(QuadrupedModel.class)
public abstract class QuadrupedEntityModelMixin {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightFrontLeg;
    @Shadow protected ModelPart leftFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(LivingEntityRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(head, var8, ismoving);
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(QuadrupedModel.class)
public abstract class QuadrupedEntityModelMixin<T extends Entity> {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightFrontLeg;
    @Shadow protected ModelPart leftFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", at = @At("RETURN"))
    private void owa$setupAnim(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = OwaLegAnim.entityFor((LivingEntity) entity);
        if (le == null) return;

        float var8 = SharedValueUtil.getVar8(le);
        float ismoving = SharedValueUtil.getIsMoving(le);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(head, var8, ismoving);
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightFrontLeg;
    @Shadow protected ModelPart leftFrontLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(LivingEntityRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(head, var8, ismoving);
    }
}
*///?} else {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
		/^void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {^/


		if (enableMod && enableMobs) {

			float var8 = SharedValueUtil.getVar8((LivingEntity) entity);
			float ismoving = SharedValueUtil.getIsMoving((LivingEntity) entity);

				this.rightHindLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
				this.leftHindLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
				this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
				this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;


			if (headBob) {
				head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
				head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
			}


	}

	}


}
*///?}
