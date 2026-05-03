package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(Minecraft.class)
public class OwaClientTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void owa$onClientTick(CallbackInfo ci) {
        OwaSteveManager.tick((Minecraft) (Object) this);
    }

    @Inject(method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void owa$onClearLevel(CallbackInfo ci) {
        OwaSteveManager.clear();
    }
}
//?} else if >=1.21.2 {
/*import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(MinecraftClient.class)
public class OwaClientTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void owa$onClientTick(CallbackInfo ci) {
        OwaSteveManager.tick((MinecraftClient) (Object) this);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void owa$onClearLevel(CallbackInfo ci) {
        OwaSteveManager.clear();
    }
}
*///?} else {
/*import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(MinecraftClient.class)
public class OwaClientTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void owa$onClientTick(CallbackInfo ci) {
        OwaSteveManager.tick((MinecraftClient) (Object) this);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void owa$onClearLevel(CallbackInfo ci) {
        OwaSteveManager.clear();
    }
}
*///?}
