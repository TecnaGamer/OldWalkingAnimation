package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
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

@Mixin(CreeperEntityModel.class)
public class CreeperEntityModelMixin<T extends Entity> {

//    @Final private ModelPart leftHindLeg;
//
//    @Final private ModelPart rightHindLeg;
//
//    @Final private ModelPart leftFrontLeg;
//
//    @Final private ModelPart rightFrontLeg;

    @Shadow @Final private ModelPart head;

    //? if >=1.17 {
    @Shadow @Final private ModelPart leftHindLeg;

    @Shadow @Final private ModelPart rightHindLeg;

    @Shadow @Final private ModelPart leftFrontLeg;

    @Shadow @Final private ModelPart rightFrontLeg;

    //?} else {
    /*@Shadow @Final private ModelPart leftBackLeg;

    @Shadow @Final private ModelPart rightBackLeg;

    @Shadow @Final private ModelPart leftFrontLeg;

    @Shadow @Final private ModelPart rightFrontLeg;
    *///?}



    @Inject(method = "setAngles", at = @At("RETURN"))
            //? if >=1.15 {
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
     //?} else
    /*public void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {*/

        if (enableMod && enableMobs) {

            LivingEntity livingEntity = (LivingEntity) entity;

            float var8 = SharedValueUtil.getVar8(livingEntity);
            float ismoving = SharedValueUtil.getIsMoving(livingEntity);

//        this.head.yaw = headYaw * ((float)Math.PI / 180);
//        this.head.pitch = headPitch * ((float)Math.PI / 180);
//
            //? if >=1.17 {
            this.leftHindLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            this.rightHindLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
                //?} else {
            /*this.leftBackLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            this.rightBackLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            *///?}
            if (headBob) {
                head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
                head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
            }


        }
    }

}
