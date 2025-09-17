package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
        //? if >=1.17
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
        //? if >=1.19.4 {
import net.minecraft.util.math.RotationAxis;
//?} else
/*import net.minecraft.util.math.Vec3f;*/
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.render.entity.LivingEntityRenderer.getOverlay;
import static net.minecraft.client.util.GlfwUtil.getTime;
import static tecna.oldwalkinganimation.config.Config.*;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {


//    private T entity;


    double lastTickTime;

    float timeOffs = (float) Math.random() * 1239813.0F;
    float rot = (float) (Math.random() * Math.PI * 2.0D);
    //float speed = 1.5F;
    private float bobStrength = 1;
    private float f;
    Vec3d prevVelocity;

    float hurtframe = 0;
//    float onGround;

    //float entityRun_previous;

    float animStep;
    float animStepO;
    protected float oRun;
    protected float run;
    private float yBodyRotO;
    //private float yBodyRot;
    private float yRot;
    private float yRotO;
    private float lastX;
    private float lastZ;


    private final Map<LivingEntity, Double> dxMap = new HashMap<>();
    private final Map<LivingEntity, Double> dzMap = new HashMap<>();


//    double dx;
//    double dz;

    private final Map<LivingEntity, Float> animStepMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMap = new HashMap<>();
    private final Map<LivingEntity, Float> runMapP = new HashMap<>();
    private final Map<LivingEntity, Float> lastAnimStepTimeMap = new HashMap<>();
    private final Map<LivingEntity, Float> bodMap = new HashMap<>();
    private final Map<LivingEntity, Float> onGroundMap = new HashMap<>();

    private final Map<LivingEntity, Float> headrotMap = new HashMap<>();

//    float headrot;


    private float rotOffs = 0f;

    private T storedEntity;



    //? if >=1.17 {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
                //?} else {
    /*protected LivingEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }
    *///?}






    @Shadow
    protected abstract float getLyingAngle(T entity);


    @Shadow protected M model;

    @Shadow protected abstract float getHandSwingProgress(T entity, float tickDelta);

    @Shadow
    public static boolean shouldFlipUpsideDown(LivingEntity entity) {
        return false;
    }

    @Shadow protected abstract float getAnimationProgress(T entity, float tickDelta);

    @Shadow protected abstract void scale(T entity, MatrixStack matrices, float amount);

    @Shadow protected abstract boolean isVisible(T entity);

    @Shadow @Nullable
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    @Shadow protected abstract float getAnimationCounter(T entity, float tickDelta);

    @Shadow @Final protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected static float getYaw(Direction direction) {
        return 0;
    }

    @ModifyArg(
            method = "setupTransforms",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;sqrt(F)F"
            ),
            index = 0
    )
    private float modifySqrtArgument(float f) {
        if (damage && enableMod) {
            return 0;
        } else {
            return f;
        }
    }


//    @ModifyArg(
//            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F"
//            ),
//            index = 0
//    )
//    private float modifySqrtArgument2(float h) {
//        if (damage) {
//            return 0;
//        } else {
//            return h;
//        }
//    }
//
//    @ModifyArg(
//            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F"
//            ),
//            index = 0
//    )
//    private float modifySqrtArgument3(float j) {
//        if (damage) {
//            return 0;
//        } else {
//            return j;
//        }
//    }



//    @ModifyArg(
//            method = "setupTransforms",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"
//            ),
//            index = 0
//    )
//    private float modifySqrtArgument2(float bodyYaw) {
//
//        if (damage && enableMod) {
//
//            return 0;
//        } else {
//            return bodyYaw;
//        }
//
//
//    }


//    private float getHeadrot(T entity) {
//        return entity.headYaw;
//    }


//    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
//    public void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
////        matrixStack.push();
////        this.model.handSwingProgress = this.getHandSwingProgress(livingEntity, g);
////        this.model.riding = livingEntity.hasVehicle();
////        this.model.child = livingEntity.isBaby();
////        float h = 0;//-MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
////        float j = 0;//-MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
////        float k = j - h;
//        float l;
//        if (livingEntity.hasVehicle()) {
//            Entity var11 = livingEntity.getVehicle();
//            if (var11 instanceof LivingEntity) {
//                LivingEntity livingEntity2 = (LivingEntity)var11;
//                h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
//                k = j - h;
//                l = MathHelper.wrapDegrees(k);
//                if (l < -85.0F) {
//                    l = -85.0F;
//                }
//
//                if (l >= 85.0F) {
//                    l = 85.0F;
//                }
//
//                h = j - l;
//                if (l * l > 2500.0F) {
//                    h += l * 0.2F;
//                }
//
//                k = j - h;
//            }
//        }
//
//        float m = MathHelper.lerp(g, livingEntity.prevPitch, livingEntity.getPitch());
//        if (shouldFlipUpsideDown(livingEntity)) {
//            m *= -1.0F;
//            k *= -1.0F;
//        }
//
//        k = MathHelper.wrapDegrees(k);
//        float n;
//        if (livingEntity.isInPose(EntityPose.SLEEPING)) {
//            Direction direction = livingEntity.getSleepingDirection();
//            if (direction != null) {
//                n = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;
//                matrixStack.translate((float)(-direction.getOffsetX()) * n, 0.0F, (float)(-direction.getOffsetZ()) * n);
//            }
//        }
//
//        l = livingEntity.getScale();
////        matrixStack.scale(l, l, l);
//        n = this.getAnimationProgress(livingEntity, g);
//        this.setupTransforms(livingEntity, matrixStack, n, h, g, l);
////        matrixStack.scale(-1.0F, -1.0F, 1.0F);
////        this.scale(livingEntity, matrixStack, g);
////        matrixStack.translate(0.0F, -1.501F, 0.0F);
////        float o = 0.0F;
////        float p = 0.0F;
////        if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
////            o = livingEntity.limbAnimator.getSpeed(g);
////            p = livingEntity.limbAnimator.getPos(g);
////            if (livingEntity.isBaby()) {
////                p *= 3.0F;
////            }
////
////            if (o > 1.0F) {
////                o = 1.0F;
////            }
////        }
////
////        this.model.animateModel(livingEntity, p, o, g);
//        this.model.setAngles(livingEntity, p, o, n, k, m);
////        MinecraftClient minecraftClient = MinecraftClient.getInstance();
////        boolean bl = this.isVisible(livingEntity);
////        boolean bl2 = !bl && !livingEntity.isInvisibleTo(minecraftClient.player);
////        boolean bl3 = minecraftClient.hasOutline(livingEntity);
////        RenderLayer renderLayer = this.getRenderLayer(livingEntity, bl, bl2, bl3);
////        if (renderLayer != null) {
////            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
////            int q = getOverlay(livingEntity, this.getAnimationCounter(livingEntity, g));
////            this.model.render(matrixStack, vertexConsumer, i, q, bl2 ? 654311423 : -1);
////        }
//
//        if (!livingEntity.isSpectator()) {
//            Iterator var25 = this.features.iterator();
//
//            while(var25.hasNext()) {
//                FeatureRenderer<T, M> featureRenderer = (FeatureRenderer)var25.next();
//                featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, p, o, g, n, k, m);
//            }
//        }
//
////        matrixStack.pop();
////        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
//    }





        @Inject(method = "setupTransforms", at = @At(value = "HEAD"))
    protected void setupTransforms(T entity,
                                   MatrixStack matrices,
                                   float animationProgress, float bodyYaw, float tickDelta,
                                   //? if >=1.20.6
                                   float scale,
                                   CallbackInfo ci) {



        if (enableMod) {

            boolean runCode;

            if (enableMobs) {
                runCode = true;
            } else {
                if (entity instanceof PlayerEntity) {
                    runCode = true;
                } else {
                    runCode = false;
                    SharedValueUtil.setVar10(entity, 0);
                }
            }

            if (runCode) {


                float currentTime = (float) getTime();


                Float lastAnimStepTime = lastAnimStepTimeMap.get(entity);
                if (lastAnimStepTime == null) {
                    lastAnimStepTime = currentTime;
                    lastAnimStepTimeMap.put(entity, lastAnimStepTime);
                }

                float deltaTime = (currentTime - lastAnimStepTime) * 20;

                // delta = DeltaTime.getDelta;


                if (deltaTime > 3f) {
                    deltaTime = 3f;
                }

                if (deltaTime < 0.000000000000001f) {
                    deltaTime = 0.000000000000001f;
                }

//                System.out.println(deltaTime);

                //float var5 = 0.0f;
                //this.animStep += var5;


                //super.tick();
//        this.oTilt = this.tilt;
//        if (this.attackTime > 0) {
//            --this.attackTime;
//        }
//
//        if (this.hurtTime > 0) {
//            --this.hurtTime;
//        }
//
//        if (this.invulnerableTime > 0) {
//            --this.invulnerableTime;
//        }
//
//        if (this.health <= 0) {
//            ++this.deathTime;
//            if (this.deathTime > 20) {
//                if (this.ai != null) {
//                    this.ai.beforeRemove();
//                }
//
//                this.remove();
//            }
//        }
//
//        if (this.isUnderWater()) {
//            if (this.airSupply > 0) {
//                --this.airSupply;
//            } else {
//                this.hurt((Entity)null, 2);
//            }
//        } else {
//            this.airSupply = 300;
//        }
//
//        if (this.isInWater()) {
//            this.fallDistance = 0.0F;
//        }
//
//        if (this.isInLava()) {
//            this.hurt((Entity)null, 10);
//        }

                Float yBodyRot = bodMap.get(entity);
                if (yBodyRot == null) {
                    yBodyRot = 0f;
                    bodMap.put(entity, yBodyRot);
                }


//            Float yBodyRot0 = bodMapP.get(entity);
//            if (yBodyRot0 == null) {
//                yBodyRot0 = yBodyRot;
//                bodMapP.put(entity, yBodyRot0);
//            }


                this.yRot = entity.getYaw(tickDelta);

                //float previousAnimStep = entityAnimStep;
                this.yBodyRotO = yBodyRot;
                this.yRotO = this.yRot;
//        this.xRotO = this.xRot;
//        ++this.tickCount;
//        this.aiStep();

//
//            lastX = (float) livingEntity.getX();
//            lastZ = (float) livingEntity.getZ();
//

                //? if >=1.15 {
                float var1 = (float) entity.getX() - (float) entity.prevX;
                float var2 = (float) entity.getZ() - (float) entity.prevZ;                
                //?} else {
                /*float var1 = (float) entity.x - (float) entity.prevX;
                float var2 = (float) entity.z - (float) entity.prevZ;
                *///?}
                float var3 = MathHelper.sqrt(var1 * var1 + var2 * var2);


                float var4 = this.yBodyRotO + (yBodyRot - this.yBodyRotO);
                ;
                float var5 = 0.0F;
                this.oRun = this.run;
                float var6 = 0.0F;



                float ST = speedTrigger;

                if (entity instanceof HorseEntity) {
                    ST = speedTrigger * 0.5f;
                }

                if (!(var3 <= ST)) {

                    if (var3 >= maxSpeed && maxSpeed != 1) {
                        var3 = maxSpeed;
                    }

                    var6 = 1.0F;
                    var5 = var3 * 3.0F;
                    var4 = (float) Math.atan2((double) var2, (double) var1) * 180.0F / 3.1415927F - 90.0F;
                }

//                System.out.println("var3: " + var3 + " var5: " + var5);




                if (entity.getVehicle() != null && !ridingMobAnimation) {
                    var6 = 0.0F;
                }


                Float entityRun = runMap.get(entity);
                if (entityRun == null) {
                    entityRun = 0.0f;
                    runMap.put(entity, entityRun);
                }


                Float entityRun_previous = runMapP.get(entity);
                if (entityRun_previous == null) {
                    entityRun_previous = entityRun;
                    runMapP.put(entity, entityRun_previous);
                }


                //float previousEntityRun = entityRun; // Store current animStep as previous
                //entityAnimStep += var5 * speed;

                //  float deltaTime = (float) e;

                entityRun += (var6 - entityRun) * (decayFactor * deltaTime);


                runMap.put(entity, entityRun);


                this.run += (var6 - this.run) * (0.3F * deltaTime);

                for (var1 = var4 - yBodyRot; var1 < -180.0F; var1 += 360.0F) {
                }

                while (var1 >= 180.0F) {
                    var1 -= 360.0F;
                }

                yBodyRot += var1 * (0.1F * deltaTime);

                for (var1 = this.yRot - yBodyRot; var1 < -180.0F; var1 += 360.0F) {
                    ;
                }

                while (var1 >= 180.0F) {
                    var1 -= 360.0F;
                }

                boolean var7 = var1 < -90.0F || var1 >= 90.0F;
                if (var1 < -75.0F) {
                    var1 = -75.0F;
                }

                if (var1 >= 75.0F) {
                    var1 = 75.0F;
                }


                yBodyRot = this.yRot - var1;
                yBodyRot += var1 * (0.1F * deltaTime);



//                float f = MathHelper.wrapDegrees(entity.getYaw() - yBodyRot);
//                yBodyRot += f * 0.3F * deltaTime;
//                float g = MathHelper.wrapDegrees(entity.getYaw() - yBodyRot);
//                float h = 50;
//                if (Math.abs(g) > h) {
//                    yBodyRot += g - (float)MathHelper.sign((double)g) * h * deltaTime;
//                }


                bodMap.put(entity, yBodyRot);
                if (var7) {
                    var5 = -var5;
                }


                Float entityAnimStep = animStepMap.get(entity);
                if (entityAnimStep == null) {
                    entityAnimStep = (float) Math.random(); // Initialize random animStep
                    animStepMap.put(entity, entityAnimStep);
                }

                float animSpeed = speed;

                if (entity instanceof HorseEntity) {
                    animSpeed = speed * 0.6f;
                }

                float previousAnimStep = entityAnimStep; // Store current animStep as previous
                entityAnimStep += (var5 * (animSpeed * deltaTime));
                animStepMap.put(entity, entityAnimStep);

                lastAnimStepTime = currentTime;
                lastAnimStepTimeMap.put(entity, lastAnimStepTime);

//            float previousAnimStep = entityAnimStep; // Store current animStep as previous
//            entityAnimStep += var5 * (Config.speed * 0.5f);
//            animStepMap.put(livingEntity, entityAnimStep);


                while (this.yRot - this.yRotO < -180.0F) {
                    this.yRotO -= 360.0F;// * deltaTime;
                }

                while (this.yRot - this.yRotO >= 180.0F) {
                    this.yRotO += 360.0F;// * deltaTime;
                }

//                while (this.yBodyRotO - yBodyRot < -180.0F) {
//                    this.yBodyRotO += 360.0F;// * deltaTime;
//                }
//
//                while (this.yBodyRotO - yBodyRot >= 180.0F) {
//                    this.yBodyRotO -= 360.0F;// * deltaTime;
//                }



//        while(this.xRot - this.xRotO < -180.0F) {
//            this.xRotO -= 360.0F;
//        }
//
//        while(this.xRot - this.xRotO >= 180.0F) {
//            this.xRotO += 360.0F;
//        }


                while (this.yBodyRotO - yBodyRot < -180.0F) {
                    this.yBodyRotO += 360.0F;// * deltaTime;
                }

                while (this.yBodyRotO - yBodyRot >= 180.0F) {
                    this.yBodyRotO -= 360.0F;// * deltaTime;
                }



//                System.out.println(yBodyRot + "  " + yBodyRotO);



                float body = this.yBodyRotO + deltaTime * (yBodyRot - this.yBodyRotO);





                if (bodyRot) {
                    if (
                            //? if >=1.19.4
                            !entity.hasControllingPassenger() &&
                                    !(entity.getVehicle() instanceof BoatEntity) && !(entity instanceof ArmorStandEntity))
                        //? if >=1.16.4 {
                        entity.bodyYaw = body;



                    //?} else
                        /*entity.setYaw(body);*/
                }
                float ismoving = entityRun_previous + (entityRun - entityRun_previous);// * tposeAngle;


                if (speedLimbAngle) {
                    //? if >=1.19.4 {
                    ismoving = entity.limbAnimator.getSpeed(tickDelta) * var6;
                    //?} else
                    /*ismoving = entity.limbDistance;*/

                }


               // if (ismoving >= 0.5) {
                  //  entity.setHeadYaw(entity.getYaw(tickDelta));
               // }

                float var8 = previousAnimStep + ((entityAnimStep - previousAnimStep) * 0.001f); //* 0.00000000000001f;

                if (vanillaSpeed) {
                    //? if >=1.19.4 {
                    var8 = entity.limbAnimator.getPos(tickDelta);
                    //?} else
                    /*var8 = entity.handSwingProgress;*/
                }

                Float onGround = onGroundMap.get(entity);
                if (onGround == null) {
                    onGround = 1.0f;
                }

                float ground = 0.0f;
                if (//? if >=1.16.4 {
                entity.isOnGround()
                //?} else
                /*entity.onGround*/
                ) {
                ground = 1.0f;
                }
                onGround += (ground - onGround) * (decayFactor * deltaTime);

                onGroundMap.put(entity, onGround);




                float var9;
                var9 = 0.0625F;
                float var10 = -Math.abs(MathHelper.cos(var8 * 0.6662F)) * 5.0F * ismoving * bounceHeight * onGround;// - 23.0F;



                float var11;
                if ((var11 = (float) entity.hurtTime - tickDelta) > 0.0F || entity.getHealth() <= 0) {
                    if (var11 < 0.0F) {
                        var11 = 0.0F;
                    } else {
                        var11 = MathHelper.sin((var11 /= (float)
                    //? if >=1.15 {
                entity.maxHurtTime
                //?} else
                         /*entity.field_6254*/

                        ) * var11 * var11 * var11 * 3.1415927F) * damageIntensity;
                    }


                    float var12 = 0.0F;
                    if (entity.getHealth() <= 0) {
                        var12 = ((float) entity.deathTime + tickDelta) / 20.0F;
                        if ((var11 += var12 * var12 * 800.0F) > this.getLyingAngle(entity)) {
                            var11 = this.getLyingAngle(entity);
                        }
                    }

                    //var12 = entity.hurtTime;


                    // First Rotation (Y-axis)
                    //  float angle = 180.0F - var4 + this.rotOffs;
                    //           matrices.rotateY(Math.toRadians(angle));

                    //   matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.toRadians(angle)));
//
//                // Second Rotation (Z-axis)
//                matrices.rotateZ(Math.toRadians(-var12));
//
//                // Third Rotation (X-axis)
//                matrices.rotateX(Math.toRadians(-var11));
//
//                // Fourth Rotation (Inverse of First Rotation)
//                matrices.rotateY(Math.toRadians(-angle));


//                float angle = 180.0F - var4 + this.rotOffs; // Assuming var4 and rotOffs are angles
//
//// Assuming createQuaternionFromYXZ exists (replace with your actual method)
//                Quaternionf rotationQuaternion = new Quaternionf(0.0F, angle, 0.0F, 0.0F);
//
//                matrices.multiply(rotationQuaternion, 0.0F, 1.0F, 0.0F);


//                GL11.glRotatef(180.0F - var4 + this.rotOffs, 0.0F, 1.0F, 0.0F);
//                GL11.glScalef(1.0F, 1.0F, 1.0F);
//                GL11.glRotatef(-var12, 0.0F, 1.0F, 0.0F);
//                GL11.glRotatef(-var11, 0.0F, 0.0F, 1.0F);
//                GL11.glRotatef(var12, 0.0F, 1.0F, 0.0F);
//                GL11.glRotatef(-(180.0F - var4 + this.rotOffs), 0.0F, 1.0F, 0.0F);


                    if (damage) {
                        Double dx = dxMap.get(entity);
                        Double dz = dzMap.get(entity);
                        try {

                            if (dx == null || !lockRot) {

                                if (velocity) {
                                    if (entity.getVelocity() != prevVelocity && (!(entity.getVelocity().x == 0) && !(entity.getVelocity().z == 0))) {
                                        rotOffs = 180;
                                        dx = entity.getVelocity().x;
                                        dz = entity.getVelocity().z;
                                    }
                                } else {
                                    rotOffs = 0;
                                    //? if >=1.15 {
                                    dx = entity.getRecentDamageSource().getPosition().x - entity.getPos().x;
                                    dz = entity.getRecentDamageSource().getPosition().z - entity.getPos().z;
                                     //?} else {
                                    /*dx = entity.getRecentDamageSource().method_5510().x - entity.getPos().x;
                                    dz = entity.getRecentDamageSource().method_5510().z - entity.getPos().z;
                                    *///?}
                                }


                                dxMap.put(entity, dx);
                                dzMap.put(entity, dz);
                            }


                            double angleRadians = Math.atan2(dz, dx);
                            double angleDegrees = Math.toDegrees(angleRadians);

                            if (angleDegrees < 0) {
                                angleDegrees += 360;
                            }

                            float directionAngle = (float) -angleDegrees + rotOffs;



                            //? if >=1.19.4 {
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(directionAngle));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var11));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(directionAngle)));
                                     //?} else {
                            /*matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(directionAngle));
                            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(var11));
                            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(directionAngle)));
                            *///?}









                        } catch (NullPointerException ignored) {

                            try {
                                if (fallback) {


//                            Double dx = dxMap.get(entity);
//                            Double dz = dzMap.get(entity);


                                    if (dx == null || !lockRot) {


                                        if (entity.getVelocity() != prevVelocity && (!(entity.getVelocity().x == 0) && !(entity.getVelocity().z == 0))) {
                                            rotOffs = 180;
                                            dx = entity.getVelocity().x;
                                            dz = entity.getVelocity().z;
                                        }


                                        dxMap.put(entity, dx);
                                        dzMap.put(entity, dz);
                                    }


                                    double angleRadians = Math.atan2(dz, dx);
                                    double angleDegrees = Math.toDegrees(angleRadians);

                                    if (angleDegrees < 0) {
                                        angleDegrees += 360;
                                    }

                                    float directionAngle = (float) -angleDegrees + rotOffs;



                                    //? if >=1.19.4 {
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(directionAngle));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var11));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(directionAngle)));
                                     //?} else {
                                    /*matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(directionAngle));
                                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(var11));
                                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(directionAngle)));
                                    *///?}


                                }
                            } catch (NullPointerException ignored1) {
                            }
                        }


                    }

                }

                if (entity.hurtTime <= 0 && entity.deathTime == 0) {
                    dxMap.put(entity, null);
                    dzMap.put(entity, null);
                    hurtframe = 0;
                }



                SharedValueUtil.setVar10(entity, var10);
                SharedValueUtil.setVar8(entity, var8);
                SharedValueUtil.setIsMoving(entity, ismoving);


                prevVelocity = entity.getVelocity();

                if (bounce) {


                    try {
                        if (entity.getVehicle() != null) {

                            var10 = SharedValueUtil.getVar10((LivingEntity) entity.getVehicle());

                            SharedValueUtil.setVar10(entity, var10);

                            //? if >=1.15 {
                            matrices.translate(0, (-var10 * var9), 0);
                             //?} else
                            /*GlStateManager.translatef(0, (-var10 * var9), 0);*/




                        } else {
                            //? if >=1.15 {
                            matrices.translate(0, (-var10 * var9), 0);
                             //?} else
                            /*GlStateManager.translatef(0, (-var10 * var9), 0);*/
                        }
                    } catch (ClassCastException ignored) {

                    }

                }


//            if (true) {
//                if (entity.deathTime > 0) {
//
//                    float z = ((float)entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
//                    z = MathHelper.sqrt(z);
//                    if (z > 1.0F) {
//                        z = 1.0F;
//                    }
//
//                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(z * -this.getLyingAngle(entity)));
//
//                }
//
//
//            }

            }

        }



    }


//    @ModifyArg(
//            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/math/MathHelper;wrapDegrees(F)F"
//            ),
//            index = 0
//    )
//    private float modifySqrtArgument3(float k) {
//        if (damage && enableMod) {
//            return 0;
//        } else {
//            return k;
//        }
//    }


}