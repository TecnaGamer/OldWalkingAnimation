package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
//?} else {
/*import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OverlayTexture.class)
public interface OverlayTextureAccessor {
    @Accessor("texture")
    //? if >=26.1 || neoforge {
    DynamicTexture getTexture();
    //?} else {
    /*NativeImageBackedTexture getTexture();
    *///?}
}
