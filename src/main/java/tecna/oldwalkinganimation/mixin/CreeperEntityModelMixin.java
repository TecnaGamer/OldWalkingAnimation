package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21.11 {
import net.minecraft.client.model.monster.creeper.CreeperModel;
//?} else {
/*import net.minecraft.client.model.CreeperModel;
*///?}
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(CreeperModel.class)
public abstract class CreeperEntityModelMixin {

    @Shadow private ModelPart head;
    @Shadow private ModelPart rightHindLeg;
    @Shadow private ModelPart leftHindLeg;
    @Shadow private ModelPart rightFrontLeg;
    @Shadow private ModelPart leftFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/CreeperRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(CreeperRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(head, var8, ismoving);
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.CreeperModel;
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

@Mixin(CreeperModel.class)
public abstract class CreeperEntityModelMixin<T extends Entity> {

    @Shadow private ModelPart head;
    @Shadow private ModelPart rightHindLeg;
    @Shadow private ModelPart leftHindLeg;
    @Shadow private ModelPart rightFrontLeg;
    @Shadow private ModelPart leftFrontLeg;

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
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(CreeperEntityModel.class)
public abstract class CreeperEntityModelMixin {

    @Shadow private ModelPart head;
    @Shadow private ModelPart leftHindLeg;
    @Shadow private ModelPart rightHindLeg;
    @Shadow private ModelPart leftFrontLeg;
    @Shadow private ModelPart rightFrontLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/CreeperEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(CreeperEntityRenderState state, CallbackInfo ci) {
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

    @Shadow @Final private ModelPart head;

    //? if >=1.17 {
    @Shadow @Final private ModelPart leftHindLeg;

    @Shadow @Final private ModelPart rightHindLeg;

    @Shadow @Final private ModelPart leftFrontLeg;

    @Shadow @Final private ModelPart rightFrontLeg;

    //?} else {
    /^@Shadow @Final private ModelPart leftBackLeg;

    @Shadow @Final private ModelPart rightBackLeg;

    @Shadow @Final private ModelPart leftFrontLeg;

    @Shadow @Final private ModelPart rightFrontLeg;
    ^///?}



    @Inject(method = "setAngles", at = @At("RETURN"))
            //? if >=1.15 {
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
     //?} else
    /^public void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {^/

        if (enableMod && enableMobs) {

            LivingEntity livingEntity = (LivingEntity) entity;

            float var8 = SharedValueUtil.getVar8(livingEntity);
            float ismoving = SharedValueUtil.getIsMoving(livingEntity);

            //? if >=1.17 {
            this.leftHindLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            this.rightHindLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
                //?} else {
            /^this.leftBackLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            this.rightBackLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.leftFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving;
            this.rightFrontLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving;
            ^///?}
            if (headBob) {
                head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
                head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
            }


        }
    }

}
*///?}
