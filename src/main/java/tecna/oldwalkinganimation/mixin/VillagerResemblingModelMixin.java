package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21.11 {
import net.minecraft.client.model.npc.VillagerModel;
//?} else {
/*import net.minecraft.client.model.VillagerModel;
*///?}
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(VillagerModel.class)
public abstract class VillagerResemblingModelMixin {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(VillagerRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 0.5F);
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.VillagerModel;
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

@Mixin(VillagerModel.class)
public abstract class VillagerResemblingModelMixin<T extends Entity> {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", at = @At("RETURN"))
    private void owa$setupAnim(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = OwaLegAnim.entityFor((LivingEntity) entity);
        if (le == null) return;

        float var8 = SharedValueUtil.getVar8(le);
        float ismoving = SharedValueUtil.getIsMoving(le);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 0.5F);
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(VillagerResemblingModel.class)
public abstract class VillagerResemblingModelMixin {

    @Shadow private ModelPart rightLeg;
    @Shadow private ModelPart leftLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/VillagerEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(VillagerEntityRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.bipedLegs(rightLeg, leftLeg, var8, ismoving, 0.5F);
        rightLeg.yaw = 0.0F;
        leftLeg.yaw = 0.0F;
    }
}
*///?} else {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
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

@Mixin(VillagerResemblingModel.class)
public class VillagerResemblingModelMixin<T extends Entity> {


    @Shadow
    @Final
    private ModelPart rightLeg;

    @Shadow
    @Final
    private ModelPart leftLeg;

    @Inject(method = "setAngles", at = @At("RETURN"))
        //? if >=1.15 {
    void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
     //?} else
    /^void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo ci) {^/

        if (enableMod && enableMobs) {

            float var8 = SharedValueUtil.getVar8((LivingEntity) entity);
            float ismoving = SharedValueUtil.getIsMoving((LivingEntity) entity);

            this.rightLeg.pitch = MathHelper.cos(var8 * 0.6662f) * 1.4f * ismoving * 0.5f;
            this.leftLeg.pitch = MathHelper.cos(var8 * 0.6662f + (float) Math.PI) * 1.4f * ismoving * 0.5f;
            this.rightLeg.yaw = 0.0f;
            this.leftLeg.yaw = 0.0f;
        }

    }
}
*///?}
