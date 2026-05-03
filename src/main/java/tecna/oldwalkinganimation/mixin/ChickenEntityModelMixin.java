package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
//? if >=1.21.11 {
import net.minecraft.client.model.animal.chicken.ChickenModel;
//?} else {
/*import net.minecraft.client.model.ChickenModel;
*///?}
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(ChickenModel.class)
public abstract class ChickenEntityModelMixin {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ChickenRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(ChickenRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 1.0F);
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.ChickenModel;
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

@Mixin(ChickenModel.class)
public abstract class ChickenEntityModelMixin<T extends Entity> {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", at = @At("RETURN"))
    private void owa$setupAnim(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = OwaLegAnim.entityFor((LivingEntity) entity);
        if (le == null) return;

        float var8 = SharedValueUtil.getVar8(le);
        float ismoving = SharedValueUtil.getIsMoving(le);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 1.0F);
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(ChickenEntityModel.class)
public abstract class ChickenEntityModelMixin {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/ChickenEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(ChickenEntityRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 1.0F);
    }
}
*///?} else {
/*import net.minecraft.client.model.ModelPart;
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
    /^private void setAngles(Entity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {^/

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
*///?}
