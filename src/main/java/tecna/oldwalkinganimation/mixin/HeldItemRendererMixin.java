package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
//? if >=1.15
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
//? if >=1.15
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;
import tecna.oldwalkinganimation.config.Config;

import static tecna.oldwalkinganimation.config.Config.*;

@Debug(export = true)
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {


    private float capturedF;

//    @ModifyVariable(
//            method = "renderArm",
//            at = @At("STORE"),
//            ordinal = 0 // The ordinal refers to the first assignment to f in the method
//    )
//    private float captureFValue(float f) {
//        this.capturedF = f;
//        return f; // Return the original value to keep it unchanged in the method
//    }
//
//
//    @Redirect(
//            method = "renderArm",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"
//            )
//    )
//    private void redirectTranslate(MatrixStack matrices, float x, float y, float z) {
//        float modifiedX = this.capturedF * Xtrans; // Replace with your desired X value
//        float modifiedY = Ytrans; // Replace with your desired Y value
//        float modifiedZ = Ztrans; // Replace with your desired Z value
//
//        System.out.println("test");
//
//        matrices.translate(modifiedX, modifiedY, modifiedZ); // Apply the modified translation
//    }



@Unique
boolean holdingItem;

    @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {

        if (enableMod && enableArm && holdingItem) {

            float f = arm == Arm.RIGHT ? 1.0F : -1.0F;

            matrices.translate(f * Xtrans, Ytrans, Ztrans);

        }
    }


//    @Inject(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0), cancellable = true)
//    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm, CallbackInfo ci) {
//                //float f = arm == Arm.RIGHT ? 1.0F : -1.0F;
//
//                //? if >=1.15 {
//                matrices.translate(f * Xtrans, Ytrans, Ztrans);
//                 //?} else
//                /*GlStateManager.translatef(f * Xtrans, -Ytrans, -Ztrans);*/
//
//        ci.cancel();
//
//    }


    @Shadow private ItemStack mainHand;
    @Unique


    @Inject(
            method = {"renderFirstPersonItem"},
            at = {@At("HEAD")}
    )
    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                       //? if >=1.15
                                       MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       int light, CallbackInfo ci) {
        if (item.isEmpty()) {
            holdingItem = true;
            SharedValueUtil.setIsHoldingMap(true);
        } else {
            holdingItem = false;
            SharedValueUtil.setIsHoldingMap(false);
        }
    }


//    @Inject(method = "renderMapInOneHand", at = @At("HEAD"))
//            //? if >=1.15 {
//    private void renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack, CallbackInfo ci) {
//     //?} else
//    /*private void renderMapInOneHand(float equipProgress, Arm hand, float f, ItemStack item, CallbackInfo ci) {*/
//        if (enableMod && enableArm) {
//
//
//
//
////            System.out.println(SharedValueUtil.getIsHoldingMap());
//
//            float g = arm == Arm.RIGHT ? 1.0f : -1.0f;
//            //? if >=1.15 {
//            matrices.translate(g * Xtrans, Ytrans, Ztrans);
//             //?} else
//            /*GlStateManager.translatef(g * Xtrans, -Ytrans, -Ztrans);*/
//        }
//
//    }





}




