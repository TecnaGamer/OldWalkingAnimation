package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.9) {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD"))
    private void owa$captureMapHold(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        SharedValueUtil.setIsHoldingMap(item.isEmpty());
    }

    @Inject(method = "renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFFLnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At("HEAD"), cancellable = true)
    private void owa$applyStArmPose(PoseStack pose, SubmitNodeCollector collector, int light, float inverseArmHeight, float attackValue, HumanoidArm arm, CallbackInfo ci) {
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean isRight = arm != HumanoidArm.LEFT;
        float sign = isRight ? 1.0F : -1.0F;

        pose.translate(sign * Xtrans, Ytrans, Ztrans);

        float sqrtAttack = Mth.sqrt(attackValue);
        float xSwing = -0.3F * Mth.sin(sqrtAttack * (float)Math.PI);
        float ySwing = 0.4F * Mth.sin(sqrtAttack * (float)Math.PI * 2.0F);
        float zSwing = -0.4F * Mth.sin(attackValue * (float)Math.PI);
        pose.translate(sign * (xSwing + 0.64F), ySwing - 0.6F + inverseArmHeight * -0.6F, zSwing - 0.72F);
        pose.mulPose(Axis.YP.rotationDegrees(sign * 45.0F));

        float zSwingRot = Mth.sin(attackValue * attackValue * (float)Math.PI);
        float ySwingRot = Mth.sin(sqrtAttack * (float)Math.PI);
        pose.mulPose(Axis.YP.rotationDegrees(sign * ySwingRot * 70.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(sign * zSwingRot * -20.0F));

        pose.translate(sign * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(sign * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(sign * -135.0F));
        pose.translate(sign * 5.6F, 0.0F, 0.0F);

        AvatarRenderer<AbstractClientPlayer> avatarRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        //? if >=1.21.11 {
        Identifier skinTexture = player.getSkin().body().texturePath();
        //?} else {
        /*ResourceLocation skinTexture = player.getSkin().body().texturePath();
        *///?}
        boolean sleeveShown = player.isModelPartShown(isRight ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
        if (isRight) {
            avatarRenderer.renderRightHand(pose, collector, light, skinTexture, sleeveShown);
        } else {
            avatarRenderer.renderLeftHand(pose, collector, light, skinTexture, sleeveShown);
        }
        ci.cancel();
    }
}
//?} else if neoforge {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void owa$captureMapHold(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack pose, MultiBufferSource buf, int light, CallbackInfo ci) {
        SharedValueUtil.setIsHoldingMap(item.isEmpty());
    }

    @Inject(method = "renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFFLnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At("HEAD"), cancellable = true)
    private void owa$applyStArmPose(PoseStack pose, MultiBufferSource buf, int light, float inverseArmHeight, float attackValue, HumanoidArm arm, CallbackInfo ci) {
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean isRight = arm != HumanoidArm.LEFT;
        float sign = isRight ? 1.0F : -1.0F;

        pose.translate(sign * Xtrans, Ytrans, Ztrans);

        float sqrtAttack = Mth.sqrt(attackValue);
        float xSwing = -0.3F * Mth.sin(sqrtAttack * (float)Math.PI);
        float ySwing = 0.4F * Mth.sin(sqrtAttack * (float)Math.PI * 2.0F);
        float zSwing = -0.4F * Mth.sin(attackValue * (float)Math.PI);
        pose.translate(sign * (xSwing + 0.64F), ySwing - 0.6F + inverseArmHeight * -0.6F, zSwing - 0.72F);
        pose.mulPose(Axis.YP.rotationDegrees(sign * 45.0F));

        float zSwingRot = Mth.sin(attackValue * attackValue * (float)Math.PI);
        float ySwingRot = Mth.sin(sqrtAttack * (float)Math.PI);
        pose.mulPose(Axis.YP.rotationDegrees(sign * ySwingRot * 70.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(sign * zSwingRot * -20.0F));

        pose.translate(sign * -1.0F, 3.6F, 3.5F);
        pose.mulPose(Axis.ZP.rotationDegrees(sign * 120.0F));
        pose.mulPose(Axis.XP.rotationDegrees(200.0F));
        pose.mulPose(Axis.YP.rotationDegrees(sign * -135.0F));
        pose.translate(sign * 5.6F, 0.0F, 0.0F);

        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        //? if >=1.21.2 {
        ResourceLocation skinTexture = player.getSkin().texture();
        boolean sleeveShown = player.isModelPartShown(isRight ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
        if (isRight) {
            playerRenderer.renderRightHand(pose, buf, light, skinTexture, sleeveShown);
        } else {
            playerRenderer.renderLeftHand(pose, buf, light, skinTexture, sleeveShown);
        }
        //?} else {
        /^if (isRight) {
            playerRenderer.renderRightHand(pose, buf, light, player);
        } else {
            playerRenderer.renderLeftHand(pose, buf, light, player);
        }
        ^///?}
        ci.cancel();
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
//? if >=1.21.9 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//?} else
/^import net.minecraft.client.render.VertexConsumerProvider;^/
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    //? if >=1.21.9 {
    @Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
            at = @At("HEAD"))
    private void owa$captureMapHold(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                    MatrixStack matrices, OrderedRenderCommandQueue collector,
                                    int light, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"))
    private void owa$captureMapHold(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                    MatrixStack matrices, VertexConsumerProvider collector,
                                    int light, CallbackInfo ci) {
    ^///?}
        SharedValueUtil.setIsHoldingMap(item.isEmpty());
    }

    //? if >=1.21.9 {
    @Inject(method = "renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V",
            at = @At("HEAD"), cancellable = true)
    private void owa$applyStArmPose(MatrixStack pose, OrderedRenderCommandQueue collector, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V",
            at = @At("HEAD"), cancellable = true)
    private void owa$applyStArmPose(MatrixStack pose, VertexConsumerProvider collector, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
    ^///?}
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        boolean isRight = arm != Arm.LEFT;
        float sign = isRight ? 1.0F : -1.0F;

        pose.translate(sign * Xtrans, Ytrans, Ztrans);

        float sqrtAttack = MathHelper.sqrt(swingProgress);
        float xSwing = -0.3F * MathHelper.sin(sqrtAttack * (float) Math.PI);
        float ySwing = 0.4F * MathHelper.sin(sqrtAttack * (float) Math.PI * 2.0F);
        float zSwing = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        pose.translate(sign * (xSwing + 0.64F), ySwing - 0.6F + equipProgress * -0.6F, zSwing - 0.72F);
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sign * 45.0F));

        float zSwingRot = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float ySwingRot = MathHelper.sin(sqrtAttack * (float) Math.PI);
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sign * ySwingRot * 70.0F));
        pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sign * zSwingRot * -20.0F));

        pose.translate(sign * -1.0F, 3.6F, 3.5F);
        pose.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sign * 120.0F));
        pose.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0F));
        pose.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sign * -135.0F));
        pose.translate(sign * 5.6F, 0.0F, 0.0F);

        //? if >=1.21.9 {
        PlayerEntityRenderer<AbstractClientPlayerEntity> playerRenderer =
                MinecraftClient.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        boolean sleeveShown = player.isModelPartVisible(isRight ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
        //?} else {
        /^PlayerEntityRenderer playerRenderer =
                (PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
        Identifier skinTexture = player.getSkinTextures().texture();
        boolean sleeveShown = player.isPartVisible(isRight ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
        ^///?}
        if (isRight) {
            playerRenderer.renderRightArm(pose, collector, light, skinTexture, sleeveShown);
        } else {
            playerRenderer.renderLeftArm(pose, collector, light, skinTexture, sleeveShown);
        }
        ci.cancel();
    }
}
*///?} else {
/*import net.minecraft.client.network.AbstractClientPlayerEntity;
//? if >=1.15
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
//? if >=1.15
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Unique
    private boolean holdingItem;

    @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        if (enableMod && enableArm && holdingItem) {
            float f = arm == Arm.RIGHT ? 1.0F : -1.0F;
            matrices.translate(f * Xtrans, Ytrans, Ztrans);
        }
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At("HEAD")
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
}
*///?}
