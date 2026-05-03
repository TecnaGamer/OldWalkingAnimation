package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || (neoforge && >=1.21.9) {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
//? if >=1.21.11 {
import net.minecraft.client.model.player.PlayerModel;
//?} else {
/*import net.minecraft.client.model.PlayerModel;
*///?}
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin {

    //? if >=1.21.11 {
    @Inject(method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void owa$poseHandBeforeSubmit(PoseStack pose, SubmitNodeCollector collector, int light, Identifier texture, ModelPart arm, boolean hatLayer, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void owa$poseHandBeforeSubmit(PoseStack pose, SubmitNodeCollector collector, int light, ResourceLocation texture, ModelPart arm, boolean hatLayer, CallbackInfo ci) {
    *///?}
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;
        PlayerModel m = (PlayerModel) ((LivingEntityRenderer<?, ?, ?>) (Object) this).getModel();
        float sign = (arm == m.rightArm) ? 1.0F : -1.0F;
        arm.xRot = Pitch;
        arm.yRot = sign * Yaw;
        arm.zRot = sign * Roll;
    }
}
//?} else if (neoforge && >=1.21.2) {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/model/geom/ModelPart;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                     shift = At.Shift.BEFORE))
    private void owa$poseArm(PoseStack pose, MultiBufferSource buf, int light, ResourceLocation texture, ModelPart arm, boolean hatLayer, CallbackInfo ci) {
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;
        PlayerModel m = (PlayerModel) ((LivingEntityRenderer<?, ?, ?>) (Object) this).getModel();
        float sign = (arm == m.rightArm) ? 1.0F : -1.0F;
        arm.xRot = Pitch;
        arm.yRot = sign * Yaw;
        arm.zRot = sign * Roll;
    }
}
*///?} else if neoforge {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                     shift = At.Shift.BEFORE))
    private void owa$poseArm(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;
        PlayerModel<?> m = (PlayerModel<?>) ((LivingEntityRenderer<?, ?>) (Object) this).getModel();
        float sign = (arm == m.rightArm) ? 1.0F : -1.0F;
        arm.xRot = Pitch;
        arm.yRot = sign * Yaw;
        arm.zRot = sign * Roll;
        if (sleeve != null) {
            sleeve.xRot = Pitch;
            sleeve.yRot = sign * Yaw;
            sleeve.zRot = sign * Roll;
        }
    }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.model.ModelPart;
//? if >=1.21.9 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//?} else
/^import net.minecraft.client.render.VertexConsumerProvider;^/
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    //? if >=1.21.9 {
    @Inject(method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;Lnet/minecraft/client/model/ModelPart;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;)V"))
    private void owa$poseArmBeforeSubmit(MatrixStack pose, OrderedRenderCommandQueue collector, int light, Identifier texture, ModelPart arm, boolean hatLayer, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "renderArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Lnet/minecraft/client/model/ModelPart;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void owa$poseArmBeforeSubmit(MatrixStack pose, VertexConsumerProvider collector, int light, Identifier texture, ModelPart arm, boolean hatLayer, CallbackInfo ci) {
    ^///?}
        if (!enableMod || !enableArm || !SharedValueUtil.getIsHoldingMap()) return;
        PlayerEntityModel m = (PlayerEntityModel) ((LivingEntityRenderer<?, ?, ?>) (Object) this).getModel();
        float sign = (arm == m.rightArm) ? 1.0F : -1.0F;
        arm.pitch = Pitch;
        arm.yaw = sign * Yaw;
        arm.roll = sign * Roll;
    }
}
*///?} else {
/*import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
//? if >=1.17
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.SharedValueUtil;

import static tecna.oldwalkinganimation.config.Config.*;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    //? if >=1.17 {
    	public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }
     //?} else {
    /^public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }
    ^///?}

    @Inject(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
        private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {


        if (enableMod && enableArm && SharedValueUtil.getIsHoldingMap()) {

            Arm mainarm = player.getMainArm();

            float f = mainarm == Arm.RIGHT ? 1.0f : -1.0f;

            arm.pitch = Pitch;
            arm.roll = f * Roll;
            arm.yaw = f * Yaw;


            sleeve.pitch = Pitch;
            sleeve.roll = f * Roll;
            sleeve.yaw = f * Yaw;


        } else {
            arm.roll = 0;
            sleeve.roll = 0;
        }

    }
}
*///?}
