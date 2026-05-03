package tecna.oldwalkinganimation;

//? if >=26.1 || neoforge {
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
//?} else {
/*import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
*///?}
import tecna.oldwalkinganimation.config.Config;
import tecna.oldwalkinganimation.mixin.OverlayTextureAccessor;

public final class OverlayTextureRefresher {
    private static OverlayTexture instance;

    private OverlayTextureRefresher() {}

    public static void register(OverlayTexture t) {
        instance = t;
    }

    public static void refresh() {
        if (instance == null) return;
        //? if >=26.1 || neoforge {
        DynamicTexture texture = ((OverlayTextureAccessor) (Object) instance).getTexture();
        if (texture == null) return;
        NativeImage image = texture.getPixels();
        //?} else {
        /*NativeImageBackedTexture texture = ((OverlayTextureAccessor) (Object) instance).getTexture();
        if (texture == null) return;
        NativeImage image = texture.getImage();
        *///?}
        if (image == null) return;

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    //? if (>=1.21.2 && <1.21.5) || (neoforge && >=1.21.2 && <1.21.5) {
                    /*int color = (Config.alpha << 24) | (Config.red << 16) | (Config.green << 8) | Config.blue;
                    int defaultPixel = -1291911168;
                    *///?} else {
                    int color = (Config.alpha << 24) | (Config.blue << 16) | (Config.green << 8) | Config.red;
                    int defaultPixel = -1308622593;
                    //?}
                    int pixel = (Config.enableMod && Config.damageFlash) ? color : defaultPixel;
                    //? if >=26.1 || (neoforge && >=1.21.5) {
                    image.setPixelABGR(j, i, pixel);
                    //?} else if (neoforge && >=1.21.2) {
                    /*image.setPixel(j, i, pixel);
                    *///?} else if neoforge {
                    /*image.setPixelRGBA(j, i, pixel);
                    *///?} else if >=1.21.2 && <1.21.5 {
                    /*image.setColorArgb(j, i, pixel);
                    *///?} else {
                    /*image.setColor(j, i, pixel);
                    *///?}
                    continue;
                }
                int k = (int) ((1.0f - (float) j / 15.0f * 0.75f) * 255.0f);
                //? if >=26.1 || (neoforge && >=1.21.5) {
                image.setPixelABGR(j, i, k << 24 | 0xFFFFFF);
                //?} else if (neoforge && >=1.21.2) {
                /*image.setPixel(j, i, k << 24 | 0xFFFFFF);
                *///?} else if neoforge {
                /*image.setPixelRGBA(j, i, k << 24 | 0xFFFFFF);
                *///?} else if >=1.21.2 && <1.21.5 {
                /*image.setColorArgb(j, i, k << 24 | 0xFFFFFF);
                *///?} else {
                /*image.setColor(j, i, k << 24 | 0xFFFFFF);
                *///?}
            }
        }
        texture.upload();
    }
}
