package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
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

@Mixin(ChickenEntityModel.class)
public class ChickenEntityModelMixin<T extends Entity> {

    @Final @Shadow
    private ModelPart rightLeg;

    @Final @Shadow
    private ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("RETURN"))
            //? if >=1.15 {
    private void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
     //?} else
    /*private void setAngles(Entity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {*/

        if (enableMod && enableMobs) {

            float var8 = SharedValueUtil.getVar8((LivingEntity) entity);
            float ismoving = SharedValueUtil.getIsMoving((LivingEntity) entity);

            limbAngle = var8;
            limbDistance = ismoving;

            this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
            this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + (float) Math.PI) * 1.4f * limbDistance;

        }

    }
}