package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.2) {
//? if >=1.21.11 {
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
//?} else {
/*import net.minecraft.client.model.AbstractEquineModel;
*///?}
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(AbstractEquineModel.class)
public abstract class HorseEntityModelMixin {

    @Shadow protected ModelPart headParts;
    @Shadow protected ModelPart rightHindLeg;
    @Shadow protected ModelPart leftHindLeg;
    @Shadow protected ModelPart rightFrontLeg;
    @Shadow protected ModelPart leftFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(EquineRenderState state, CallbackInfo ci) {
        LivingEntity entity = OwaLegAnim.entityFor(state);
        if (entity == null) return;

        float var8 = SharedValueUtil.getVar8(entity);
        float ismoving = SharedValueUtil.getIsMoving(entity);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(headParts, var8, ismoving);
    }
}
//?} else if neoforge {
/*import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(HorseModel.class)
public abstract class HorseEntityModelMixin<T extends AbstractHorse> {

    @Shadow protected ModelPart headParts;
    @Shadow private ModelPart rightHindLeg;
    @Shadow private ModelPart leftHindLeg;
    @Shadow private ModelPart rightFrontLeg;
    @Shadow private ModelPart leftFrontLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;FFFFF)V", at = @At("RETURN"))
    private void owa$setupAnim(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        LivingEntity le = OwaLegAnim.entityFor(entity);
        if (le == null) return;

        float var8 = SharedValueUtil.getVar8(le);
        float ismoving = SharedValueUtil.getIsMoving(le);

        OwaLegAnim.quadLegs(rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, var8, ismoving);
        OwaLegAnim.headBob(headParts, var8, ismoving);
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractHorseEntityModel;
import net.minecraft.client.render.entity.state.LivingHorseEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaLegAnim;
import tecna.oldwalkinganimation.SharedValueUtil;

@Mixin(AbstractHorseEntityModel.class)
public abstract class HorseEntityModelMixin {

    @Shadow protected ModelPart head;
    @Shadow private ModelPart rightHindLeg;
    @Shadow private ModelPart leftHindLeg;
    @Shadow private ModelPart rightFrontLeg;
    @Shadow private ModelPart leftFrontLeg;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/LivingHorseEntityRenderState;)V", at = @At("RETURN"))
    private void owa$setupAnim(LivingHorseEntityRenderState state, CallbackInfo ci) {
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
import net.minecraft.client.render.entity.model.HorseEntityModel;
//? if >=1.19 {
import net.minecraft.entity.passive.AbstractHorseEntity;
 //?} else
/^import net.minecraft.entity.passive.HorseBaseEntity;^/
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(HorseEntityModel.class)
public class HorseEntityModelMixin<T extends
        //? if >=1.19 {
        AbstractHorseEntity
        //?} else
        /^HorseBaseEntity^/
        > {



            @Shadow @Final private ModelPart rightHindLeg;
    @Shadow
    @Final
    private ModelPart leftHindLeg;

    @Shadow
    @Final
    private ModelPart rightFrontLeg;

    @Shadow
    @Final
    private ModelPart leftFrontLeg;


    @Shadow @Final protected ModelPart head;

    @Shadow @Final protected ModelPart body;

    @Shadow @Final private ModelPart tail;

    @Shadow @Final private ModelPart rightHindBabyLeg;

    @Shadow @Final private ModelPart leftHindBabyLeg;

    @Shadow @Final private ModelPart rightFrontBabyLeg;

    @Shadow @Final private ModelPart leftFrontBabyLeg;
    //? if >=1.19 {
    @Inject(method = "animateModel(Lnet/minecraft/entity/passive/AbstractHorseEntity;FFF)V", at = @At("RETURN"))
     //?} else
    /^@Inject(method = "animateModel(Lnet/minecraft/entity/passive/HorseBaseEntity;FFF)V", at = @At("RETURN"))^/
    public void animateModel(T abstractHorseEntity, float f, float g, float h, CallbackInfo ci) {


        if (enableMod && enableMobs) {



            float var8 = SharedValueUtil.getVar8(abstractHorseEntity);
            float ismoving = SharedValueUtil.getIsMoving(abstractHorseEntity);

            f = var8;
            g = ismoving;

            float i = MathHelper.lerpAngleDegrees(h, abstractHorseEntity.prevBodyYaw, (abstractHorseEntity).bodyYaw);
            float j = MathHelper.lerpAngleDegrees(h, abstractHorseEntity.prevHeadYaw, abstractHorseEntity.headYaw);
            float k = MathHelper.lerp(h, abstractHorseEntity.prevPitch,
                    //? if >=1.17 {
                    abstractHorseEntity.getPitch());
                    //?} else
                    /^abstractHorseEntity.pitch);^/
            float l = j - i;
            float m = k * ((float)Math.PI / 180);
            if (l > 20.0f) {
                l = 20.0f;
            }
            if (l < -20.0f) {
                l = -20.0f;
            }
            if (g > 0.2f) {
                m += MathHelper.cos(f * 0.8f) * 0.15f * g;
            }
            float n = abstractHorseEntity.getEatingGrassAnimationProgress(h);
            float o = abstractHorseEntity.getAngryAnimationProgress(h);
            float p = 1.0f - o;
            float q = abstractHorseEntity.getEatingAnimationProgress(h);
            boolean bl = abstractHorseEntity.tailWagTicks != 0;
            float r = (float) abstractHorseEntity.age + h;
            this.head.pivotY = 4.0f;
            this.head.pivotZ = -12.0f;
            this.body.pitch = 0.0f;
            this.head.pitch = 0.5235988f + m;
            this.head.yaw = l * ((float)Math.PI / 180);
            float s = abstractHorseEntity.isTouchingWater() ? 0.2f : 1.0f;
            float t = MathHelper.cos(s * f * 0.6662f + (float)Math.PI);
            float u = t * 0.8f * g;
            float v = (1.0f - Math.max(o, n)) * (0.5235988f + m + q * MathHelper.sin(r) * 0.05f);
            this.head.pitch = o * (0.2617994f + m) + n * (2.1816616f + MathHelper.sin(r) * 0.05f) + v;
            this.head.yaw = o * l * ((float)Math.PI / 180) + (1.0f - Math.max(o, n)) * this.head.yaw;
            this.head.pivotY = o * -4.0f + n * 11.0f + (1.0f - Math.max(o, n)) * this.head.pivotY;
            this.head.pivotZ = o * -4.0f + n * -12.0f + (1.0f - Math.max(o, n)) * this.head.pivotZ;
            this.body.pitch = o * -0.7853982f + p * this.body.pitch;
            float w = 0.2617994f * o;
            float x = MathHelper.cos(r * 0.6f + (float)Math.PI);
            this.leftFrontLeg.pivotY = 2.0f * o + 14.0f * p;
            this.leftFrontLeg.pivotZ = -6.0f * o - 10.0f * p;
            this.rightFrontLeg.pivotY = this.leftFrontLeg.pivotY;
            this.rightFrontLeg.pivotZ = this.leftFrontLeg.pivotZ;
            float y = (-1.0471976f + x) * o + u * p;
            float z = (-1.0471976f - x) * o - u * p;
            this.leftHindLeg.pitch = w - t * 0.5f * g * p;
            this.rightHindLeg.pitch = w + t * 0.5f * g * p;
            this.leftFrontLeg.pitch = y;
            this.rightFrontLeg.pitch = z;
            this.tail.pitch = 0.5235988f + g * 0.75f;
            this.tail.pivotY = -5.0f + g;
            this.tail.pivotZ = 2.0f + g * 2.0f;
            this.tail.yaw = bl ? MathHelper.cos(r * 0.7f) : 0.0f;
            this.rightHindBabyLeg.pivotY = this.rightHindLeg.pivotY;
            this.rightHindBabyLeg.pivotZ = this.rightHindLeg.pivotZ;
            this.rightHindBabyLeg.pitch = this.rightHindLeg.pitch;
            this.leftHindBabyLeg.pivotY = this.leftHindLeg.pivotY;
            this.leftHindBabyLeg.pivotZ = this.leftHindLeg.pivotZ;
            this.leftHindBabyLeg.pitch = this.leftHindLeg.pitch;
            this.rightFrontBabyLeg.pivotY = this.rightFrontLeg.pivotY;
            this.rightFrontBabyLeg.pivotZ = this.rightFrontLeg.pivotZ;
            this.rightFrontBabyLeg.pitch = this.rightFrontLeg.pitch;
            this.leftFrontBabyLeg.pivotY = this.leftFrontLeg.pivotY;
            this.leftFrontBabyLeg.pivotZ = this.leftFrontLeg.pivotZ;
            this.leftFrontBabyLeg.pitch = this.leftFrontLeg.pitch;
            boolean bl2 = abstractHorseEntity.isBaby();
            this.rightHindLeg.visible = !bl2;
            this.leftHindLeg.visible = !bl2;
            this.rightFrontLeg.visible = !bl2;
            this.leftFrontLeg.visible = !bl2;
            this.rightHindBabyLeg.visible = bl2;
            this.leftHindBabyLeg.visible = bl2;
            this.rightFrontBabyLeg.visible = bl2;
            this.leftFrontBabyLeg.visible = bl2;
            this.body.pivotY = bl2 ? 10.8f : 0.0f;


        }

    }

}
*///?}
