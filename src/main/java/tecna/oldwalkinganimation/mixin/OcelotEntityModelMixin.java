package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
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
    /*private void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {*/

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