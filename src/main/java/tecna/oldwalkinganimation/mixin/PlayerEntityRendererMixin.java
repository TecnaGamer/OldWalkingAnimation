package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
//? if >=1.17
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
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
    /*public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }
    *///?}

    @Inject(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
        private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {


        if (enableMod && enableArm && SharedValueUtil.getIsHoldingMap()) {

//            System.out.println(SharedValueUtil.getIsHoldingMap());

            Arm mainarm = player.getMainArm();

            float f = mainarm == Arm.RIGHT ? 1.0f : -1.0f;

            arm.pitch = Pitch;
            arm.roll = f * Roll;
            arm.yaw = f * Yaw;


//    arm.pivotY = Ytrans;
//    arm.pivotX = Xtrans;
//    arm.pivotZ = Ztrans;


            sleeve.pitch = Pitch;
            sleeve.roll = f * Roll;
            sleeve.yaw = f * Yaw;


        } else {
            arm.roll = 0;
            sleeve.roll = 0;
        }

    }
}


