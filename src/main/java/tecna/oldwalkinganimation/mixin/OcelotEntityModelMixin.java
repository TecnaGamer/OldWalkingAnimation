package tecna.oldwalkinganimation.mixin;

//? if >=26.1 {
import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(AdultFelineModel.class)
public abstract class OcelotEntityModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/FelineRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(FelineRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        AbstractFelineModelAccessor self = (AbstractFelineModelAccessor) this;
        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(self.owa$getRightHindLeg(), self.owa$getLeftHindLeg(),
                self.owa$getRightFrontLeg(), self.owa$getLeftFrontLeg(), var8, ismoving);
        OwaLegAnim.headBob(self.owa$getHead(), var8, ismoving);
    }
}
//?} else if (neoforge && >=1.21.2) {
/*import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21.11 {
import net.minecraft.client.model.animal.feline.FelineModel;
//?} else {
/^import net.minecraft.client.model.FelineModel;
^///?}
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(FelineModel.class)
public abstract class OcelotEntityModelMixin {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftFrontLeg;
    @Shadow protected ModelPart rightFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/FelineRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(FelineRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(head, var8, ismoving);
    }
}
*///?} else if neoforge {
/*import net.minecraft.client.model.OcelotModel;
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

@Mixin(OcelotModel.class)
public abstract class OcelotEntityModelMixin<T extends Entity> {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftFrontLeg;
    @Shadow protected ModelPart rightFrontLeg;

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
import net.minecraft.client.render.entity.model.FelineEntityModel;
import net.minecraft.client.render.entity.state.FelineEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(FelineEntityModel.class)
public abstract class OcelotEntityModelMixin {

    @Shadow protected ModelPart head;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftFrontLeg;
    @Shadow protected ModelPart rightFrontLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/FelineEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(FelineEntityRenderState state, CallbackInfo ci) {
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
import net.minecraft.client.render.entity.model.OcelotEntityModel;
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

import static tecna.oldwalkinganimation.config.Config.enableMobs;
import static tecna.oldwalkinganimation.config.Config.enableMod;

@Mixin(OcelotEntityModel.class)
public class OcelotEntityModelMixin<T extends Entity> {

    @Shadow
    protected int animationState;

    @Shadow
    @Final
    protected ModelPart leftHindLeg;

    @Shadow
    @Final
    protected ModelPart rightHindLeg;

    @Shadow
    @Final
    protected ModelPart leftFrontLeg;

    @Shadow
    @Final
    protected ModelPart rightFrontLeg;

    @Shadow
    @Final
    protected ModelPart lowerTail;

    @Inject(method = "setAngles", at = @At("RETURN"))
            //? if >=1.15 {
    private void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
     //?} else
    /^private void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {^/

        if (enableMod && enableMobs) {

            float var8 = SharedValueUtil.getVar8((LivingEntity) entity);
            float ismoving = SharedValueUtil.getIsMoving((LivingEntity) entity);

            limbAngle = var8;
            limbDistance = ismoving;

            if (this.animationState != 3) {
                if (this.animationState == 2) {
                    this.leftHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * limbDistance;
                    this.rightHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + 0.3f) * limbDistance;
                    this.leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI + 0.3f) * limbDistance;
                    this.rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * limbDistance;
                    this.lowerTail.pitch = 1.7278761f + 0.31415927f * MathHelper.cos(limbAngle) * limbDistance;
                } else {
                    this.leftHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * limbDistance;
                    this.rightHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * limbDistance;
                    this.leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * limbDistance;
                    this.rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * limbDistance;
                    this.lowerTail.pitch = this.animationState == 1 ? 1.7278761f + 0.7853982f * MathHelper.cos(limbAngle) * limbDistance : 1.7278761f + 0.47123894f * MathHelper.cos(limbAngle) * limbDistance;
                }
            }

        }
    }
}
*///?}
