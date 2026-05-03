package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
import net.minecraft.client.renderer.texture.OverlayTexture;
//?} else {
/*import net.minecraft.client.render.OverlayTexture;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OverlayTextureRefresher;

@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        OverlayTextureRefresher.register((OverlayTexture) (Object) this);
        OverlayTextureRefresher.refresh();
    }

    //? if <1.21.11 {
    /*@Inject(method = "setupOverlayColor", at = @At("HEAD"))
    private void onSetupOverlayColor(CallbackInfo ci) {
        OverlayTextureRefresher.refresh();
    }
    *///?}
}
