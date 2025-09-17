package tecna.oldwalkinganimation.mixin;


import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.config.Config;

@Mixin(OverlayTexture.class)
public class OverlayTextureMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        applyConfigToTexture();
    }

    @Inject(method = "setupOverlayColor", at = @At("HEAD"))
    private void onSetupOverlayColor(CallbackInfo ci) {
        applyConfigToTexture();
    }

    @Unique
    private void applyConfigToTexture() {

        OverlayTextureAccessor accessor = (OverlayTextureAccessor) (Object) this;
        NativeImageBackedTexture texture = accessor.getTexture();
        NativeImage nativeImage = texture.getImage();

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    int color = (Config.alpha << 24) | (Config.blue << 16) | (Config.green << 8) | Config.red;
                    if (Config.enableMod && Config.damageFlash) {
                        nativeImage.setColor(j, i, color);
                    } else {
                        nativeImage.setColor(j, i, -1308622593);
                    }
                    continue;
                }
                int k = (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);

                nativeImage.setColor(j, i, k << 24 | 0xFFFFFF);
            }
        }

//        for (int i = 0; i < 16; ++i) {
//            for (int j = 0; j < 16; ++j) {
//                int alpha = (i < 8) ? 128 : (int)((1.0f - (float)j / 15.0f * 0.75f) * 255.0f);
//                System.out.println(alpha);
//                int color = (alpha << 24) | (Config.red << 16) | (Config.green << 8) | Config.blue;
//                nativeImage.setColor(j, i, color);
//
//            }
//        }

        texture.upload();
    }

//    @Inject(method = "teardownOverlayColor", at = @At("HEAD"))
//    private void onTeardownOverlayColor(CallbackInfo ci) {
//        // Inject code before the teardownOverlayColor method is called
//      //  System.out.println("Tearing down overlay color");
//    }
}
