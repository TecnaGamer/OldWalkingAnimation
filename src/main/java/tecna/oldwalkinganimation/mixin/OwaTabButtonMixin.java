package tecna.oldwalkinganimation.mixin;

//? if (fabric && <1.20.6) {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tecna.oldwalkinganimation.config.OwaConfigScreen;

@Mixin(TabButtonWidget.class)
public abstract class OwaTabButtonMixin {

    @Redirect(method = "renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void owa$replaceTabButtonBg(DrawContext ctx, Identifier tex, int x, int y, int w, int h) {
        if (!(MinecraftClient.getInstance().currentScreen instanceof OwaConfigScreen)) {
            ctx.drawGuiTexture(tex, x, y, w, h);
            return;
        }
        TabButtonWidget self = (TabButtonWidget)(Object) this;
        boolean current = self.isCurrentTab();
        boolean hover = self.isHovered();
        int color;
        if (current) color = 0x80FFFFFF;
        else if (hover) color = 0x60FFFFFF;
        else color = 0x40000000;
        ctx.fill(x, y, x + w, y + h, color);
        if (current) {
            ctx.fill(x, y + h - 2, x + w, y + h, 0xFFFFFFFF);
        }
    }
}
*///?}
