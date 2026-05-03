package tecna.oldwalkinganimation.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=26.1 {
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaStateHolder;

@Mixin(net.minecraft.client.renderer.entity.LivingEntityRenderer.class)
public class OwaRendererCaptureMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void owa$captureEntity(net.minecraft.world.entity.LivingEntity entity,
                                   net.minecraft.client.renderer.entity.state.LivingEntityRenderState state,
                                   float partialTick,
                                   CallbackInfo ci) {
        OwaStateHolder holder = (OwaStateHolder) (Object) state;
        holder.owa$setEntity(entity);
        holder.owa$setPartialTick(partialTick);
    }
}
//?} else if >=1.21.2 {
/*@Mixin(java.lang.Object.class)
public class OwaRendererCaptureMixin {
}
*///?} else {
/*@Mixin(java.lang.Object.class)
public class OwaRendererCaptureMixin {
}
*///?}
