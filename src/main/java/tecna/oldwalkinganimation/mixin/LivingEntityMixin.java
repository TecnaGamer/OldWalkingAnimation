package tecna.oldwalkinganimation.mixin;

//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;
import tecna.oldwalkinganimation.config.Config;

import static tecna.oldwalkinganimation.config.Config.*;
import static tecna.oldwalkinganimation.config.Config.bodyRot;

@Mixin(LivingEntity.class)
//@Environment(EnvType.CLIENT)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public float bodyYaw;

    @Shadow public float headYaw;

    @Shadow public float handSwingProgress;

    @Shadow public float prevBodyYaw;

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow protected abstract float turnHead(float bodyRotation, float headRotation);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }




//    // Capture the arguments of turnHead before it's invoked
//    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;turnHead(FF)F"), cancellable = true)
//    private void onTick(CallbackInfo ci) {
//
//
////
////        //? if >=1.15 {
////        float var1 = (float) this.getX() - (float) this.prevX;
////        float var2 = (float) this.getZ() - (float) this.prevZ;
////        //?} else {
////                /*float var1 = (float) entity.x - (float) entity.prevX;
////                float var2 = (float) entity.z - (float) entity.prevZ;
////                *///?}
////        float var3 = MathHelper.sqrt(var1 * var1 + var2 * var2);
////
////
////        float var4 = this.prevBodyYaw + (this.bodyYaw - this.prevBodyYaw);
////
////        float var5 = 0.0F;
////       // this.oRun = this.run;
////        float var6 = 0.0F;
////
////
////
////        if (!(var3 <= speedTrigger)) {
////
////            if (var3 >= maxSpeed && maxSpeed != 1) {
////                var3 = maxSpeed;
////            }
////
////            var6 = 1.0F;
////            var5 = var3 * 3.0F;
////            var4 = (float) Math.atan2((double) var2, (double) var1) * 180.0F / 3.1415927F - 90.0F;
////        }
////
//////                System.out.println("var3: " + var3 + " var5: " + var5);
////
////
////
////
////        if (this.getVehicle() != null && !ridingMobAnimation) {
////            var6 = 0.0F;
////        }
////
////
//////        Float entityRun = runMap.get(entity);
//////        if (entityRun == null) {
//////            entityRun = 0.0f;
//////            runMap.put(entity, entityRun);
//////        }
//////
//////
//////        Float entityRun_previous = runMapP.get(entity);
//////        if (entityRun_previous == null) {
//////            entityRun_previous = entityRun;
//////            runMapP.put(entity, entityRun_previous);
//////        }
////
////
////        //float previousEntityRun = entityRun; // Store current animStep as previous
////        //entityAnimStep += var5 * speed;
////
////        //  float deltaTime = (float) e;
////
//////        entityRun += (var6 - entityRun) * (decayFactor * deltaTime);
//////
//////
//////        runMap.put(entity, entityRun);
//////
//////
//////        this.run += (var6 - this.run) * (0.3F * deltaTime);
////
////        for (var1 = var4 - this.bodyYaw; var1 < -180.0F; var1 += 360.0F) {
////        }
////
////        while (var1 >= 180.0F) {
////            var1 -= 360.0F;
////        }
////
////        bodyYaw += var1 * 0.1f;
////
////        for (var1 = this.getYaw() - bodyYaw; var1 < -180.0F; var1 += 360.0F) {
////            ;
////        }
////
////        while (var1 >= 180.0F) {
////            var1 -= 360.0F;
////        }
////
////        boolean var7 = var1 < -90.0F || var1 >= 90.0F;
////        if (var1 < -75.0F) {
////            var1 = -75.0F;
////        }
////
////        if (var1 >= 75.0F) {
////            var1 = 75.0F;
////        }
////
////
////        bodyYaw = this.getYaw() - var1;
////        bodyYaw += var1 * 0.1f;
////
////
////
//////                float f = MathHelper.wrapDegrees(entity.getYaw() - yBodyRot);
//////                yBodyRot += f * 0.3F * deltaTime;
//////                float g = MathHelper.wrapDegrees(entity.getYaw() - yBodyRot);
//////                float h = 50;
//////                if (Math.abs(g) > h) {
//////                    yBodyRot += g - (float)MathHelper.sign((double)g) * h * deltaTime;
//////                }
////
////
//////        bodMap.put(entity, yBodyRot);
////        if (var7) {
////            var5 = -var5;
////        }
//
//
////        Float entityAnimStep = animStepMap.get(entity);
////        if (entityAnimStep == null) {
////            entityAnimStep = (float) Math.random(); // Initialize random animStep
////            animStepMap.put(entity, entityAnimStep);
////        }
////
////
////        float previousAnimStep = entityAnimStep; // Store current animStep as previous
////        entityAnimStep += (var5 * (speed * deltaTime));
////        animStepMap.put(entity, entityAnimStep);
////
////        lastAnimStepTime = currentTime;
////        lastAnimStepTimeMap.put(entity, lastAnimStepTime);
//
////            float previousAnimStep = entityAnimStep; // Store current animStep as previous
////            entityAnimStep += var5 * (Config.speed * 0.5f);
////            animStepMap.put(livingEntity, entityAnimStep);
//
//
////        while (this.yRot - this.yRotO < -180.0F) {
////            this.yRotO -= 360.0F;// * deltaTime;
////        }
////
////        while (this.yRot - this.yRotO >= 180.0F) {
////            this.yRotO += 360.0F;// * deltaTime;
////        }
//
////                while (this.yBodyRotO - yBodyRot < -180.0F) {
////                    this.yBodyRotO += 360.0F;// * deltaTime;
////                }
////
////                while (this.yBodyRotO - yBodyRot >= 180.0F) {
////                    this.yBodyRotO -= 360.0F;// * deltaTime;
////                }
//
//
//
////        while(this.xRot - this.xRotO < -180.0F) {
////            this.xRotO -= 360.0F;
////        }
////
////        while(this.xRot - this.xRotO >= 180.0F) {
////            this.xRotO += 360.0F;
////        }
//
////
////        while (this.yBodyRotO - yBodyRot < -180.0F) {
////            this.yBodyRotO += 360.0F;// * deltaTime;
////        }
////
////        while (this.yBodyRotO - yBodyRot >= 180.0F) {
////            this.yBodyRotO -= 360.0F;// * deltaTime;
////        }
//
//
//
////                System.out.println(yBodyRot + "  " + yBodyRotO);
//
//
//
////        float body = this.b + deltaTime * (yBodyRot - this.yBodyRotO);
//
//
////                head -= body;
//
//
//
//
////        if (bodyRot) {
////            if (
////                //? if >=1.19.4
////                    !entity.hasControllingPassenger() &&
////                            !(entity.getVehicle() instanceof BoatEntity))
////                //? if >=1.16.4 {
//////                        SharedValueUtil.setBodyRot(entity, body);
////                entity.setBodyYaw(body);
////
////
////            //?} else
////            /*entity.setYaw(body);*/
////        }
//
//
//
//
//        double d = this.getX() - this.prevX;
//        double e = this.getZ() - this.prevZ;
//        float f = (float)(d * d + e * e);
//        float g = this.bodyYaw;
//        float h = 0.0F;
//        //this.prevStepBobbingAmount = this.stepBobbingAmount;
//        float k = 0.0F;
//        float l;
//        if (f > 0.0025000002F) {
//            k = 1.0F;
//            h = (float)Math.sqrt((double)f) * 3.0F;
//            l = (float)MathHelper.atan2(e, d) * 57.295776F - 90.0F;
//            float m = MathHelper.abs(MathHelper.wrapDegrees(this.getYaw()) - l);
//            if (95.0F < m && m < 265.0F) {
//                g = l - 180.0F;
//            } else {
//                g = l;
//            }
//        }
//
//        if (this.handSwingProgress > 0.0F) {
//            g = this.getYaw();
//        }
//
//        // Replace the turnHead method with your own custom code
//        customTurnHead(g, h);
//
//        // Cancel the original turnHead() method call
//        ci.cancel();
//    }

    // Your custom turn head logic
//    private float customTurnHead(float bodyRotation, float headRotation) {
//        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
//
//        this.bodyYaw += f * 0.05;
//        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
//        float h = 90.F;
//        if (Math.abs(g) > h) {
////            this.bodyYaw += g - (float)MathHelper.sign((double)g) * 0;
//        }
//
//        boolean bl = g < -90.0F || g >= 90.0F;
//        if (bl) {
//            headRotation *= -1.0F;
//        }
//
//        return headRotation;
//    }

//    @ModifyReturnValue(
//            method = "getMaxRelativeHeadRotation()F",
//            at = @At("RETURN")
//    )
//    private float modifyMaxRelativeHeadRotation(float original) {
//        // Return your custom value instead of the original one
//        return 180.0F; // Example: doubling the max relative head rotation
//    }

//    @Inject(
//            method = "turnHead(FF)F",
//            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/MathHelper;wrapDegrees(F)F", ordinal = 0),
//            cancellable = true
//    )
//    private void preventBodyRotation(float bodyRotation, float headRotation, CallbackInfoReturnable<Float> cir) {
//        // Modify the behavior of `turnHead` to prevent bodyYaw from being changed
//
//        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
//        // Prevent the modification of bodyYaw
//        // this.bodyYaw += f * 0.3F;  // Commenting out this line prevents bodyYaw change
//
//        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
//        float h = this.getMaxRelativeHeadRotation();
//        if (Math.abs(g) > h) {
//            // Prevent the modification of bodyYaw here as well
//            // this.bodyYaw += g - (float)MathHelper.sign((double)g) * h;  // Commenting out this line prevents bodyYaw change
//        }
//
//        boolean bl = g < -90.0F || g >= 90.0F;
//        if (bl) {
//            //headRotation *= -1.0F;
//        }
//
//        cir.setReturnValue(0.0F);
//    }


    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;turnHead(FF)F"))
    public float redirectTurnHead(LivingEntity instance, float bodyRotation, float headRotation) {
        if (Config.enableMod && Config.bodyRot// && (instance instanceof PlayerEntity || Config.enableMobs)
                //? if >=1.19.4
                && !instance.hasControllingPassenger()
                && !(instance.getVehicle() instanceof BoatEntity) && !(instance instanceof ArmorStandEntity)) {
        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);

        if (!(instance instanceof PlayerEntity)) {
            this.bodyYaw += f * 0.1f;
        }


        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
//        float h = 90.F;
//        if (Math.abs(g) > h) {
////            this.bodyYaw += g - (float)MathHelper.sign((double)g) * 0;
//        }

        boolean bl = g < -90.0F || g >= 90.0F;
        if (bl) {
            headRotation *= -1.0F;
        }



        return headRotation;
        } else {
            // Call the shadowed method when the mod is disabled
            return this.turnHead(bodyRotation, headRotation);
        }
    }



//    @Redirect(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;wrapDegrees(F)F"))
//    public float turnHeadMixin(LivingEntity float bodyRotation, float headRotation) {
//        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
//
//        if ()
//        this.bodyYaw += f * 0.05;
//
//
//        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
////        float h = 90.F;
////        if (Math.abs(g) > h) {
//////            this.bodyYaw += g - (float)MathHelper.sign((double)g) * 0;
////        }
//
//        boolean bl = g < -90.0F || g >= 90.0F;
//        if (bl) {
//            headRotation *= -1.0F;
//        }
//
//        return headRotation;
//    }



//    @ModifyArg(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;wrapDegrees(F)F"))
//    protected float turnheadarg(float headRotation) {
//
//
//
//        return this.getYaw();
//    }




}