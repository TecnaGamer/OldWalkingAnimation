package tecna.oldwalkinganimation.mixin;

//? if (fabric && <1.20.6) {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tecna.oldwalkinganimation.config.OwaConfigScreen;

@Mixin(TabNavigationWidget.class)
public abstract class OwaTabNavMixin {

    @Redirect(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void owa$skipBlackBar(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        if (MinecraftClient.getInstance().currentScreen instanceof OwaConfigScreen) return;
        ctx.fill(x1, y1, x2, y2, color);
    }

    @Redirect(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V"))
    private void owa$skipDirtSeparator(DrawContext ctx, Identifier tex, int x, int y, float u, float v, int w, int h, int texW, int texH) {
        if (MinecraftClient.getInstance().currentScreen instanceof OwaConfigScreen) return;
        ctx.drawTexture(tex, x, y, u, v, w, h, texW, texH);
    }
}
*///?}
