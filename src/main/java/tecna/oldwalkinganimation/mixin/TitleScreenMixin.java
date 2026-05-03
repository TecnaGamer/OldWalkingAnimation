package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.config.OwaConfigScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void owa$addButton(CallbackInfo ci) {
        if (!tecna.oldwalkinganimation.config.Config.showQuickAccess) return;
        Button btn = Button.builder(Component.literal("OWA Config"),
                b -> Minecraft.getInstance().setScreen(new OwaConfigScreen((TitleScreen) (Object) this)))
                .bounds(this.width - 110, 6, 100, 20).build();
        this.addRenderableWidget(btn);
    }
}
*///?} else {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.config.OwaConfigScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void owa$addButton(CallbackInfo ci) {
        if (!tecna.oldwalkinganimation.config.Config.showQuickAccess) return;
        ButtonWidget btn = ButtonWidget.builder(Text.literal("OWA Config"),
                b -> MinecraftClient.getInstance().setScreen(new OwaConfigScreen((TitleScreen) (Object) this)))
                .dimensions(this.width - 110, 6, 100, 20).build();
        this.addDrawableChild(btn);
    }
}
//?}
