package tecna.oldwalkinganimation.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=26.1 || neoforge {
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(ClientPacketListener.class)
public class OwaExplosionMixin {

    @Inject(method = "handleExplosion(Lnet/minecraft/network/protocol/game/ClientboundExplodePacket;)V",
            at = @At("TAIL"))
    private void owa$applyExplosionToSteves(ClientboundExplodePacket packet, CallbackInfo ci) {
        //? if >=26.1 || >=1.21.9 {
        OwaSteveManager.applyExplosion(packet.center(), packet.radius());
        //?} else if neoforge && >=1.21.2 {
        /*OwaSteveManager.applyExplosion(packet.center(), 4.0F);
        *///?} else {
        /*OwaSteveManager.applyExplosion(new net.minecraft.world.phys.Vec3(packet.getX(), packet.getY(), packet.getZ()), packet.getPower());
        *///?}
    }
}
//?} else if >=1.21.2 {
/*import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(ClientPlayNetworkHandler.class)
public class OwaExplosionMixin {

    @Inject(method = "onExplosion(Lnet/minecraft/network/packet/s2c/play/ExplosionS2CPacket;)V",
            at = @At("TAIL"))
    private void owa$applyExplosionToSteves(ExplosionS2CPacket packet, CallbackInfo ci) {
        //? if >=1.21.9 {
        OwaSteveManager.applyExplosion(packet.center(), packet.radius());
        //?} else
        /^OwaSteveManager.applyExplosion(packet.center(), 4.0F);^/
    }
}
*///?} else {
/*import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tecna.oldwalkinganimation.OwaSteveManager;

@Mixin(ClientPlayNetworkHandler.class)
public class OwaExplosionMixin {

    @Inject(method = "onExplosion(Lnet/minecraft/network/packet/s2c/play/ExplosionS2CPacket;)V",
            at = @At("TAIL"))
    private void owa$applyExplosionToSteves(ExplosionS2CPacket packet, CallbackInfo ci) {
        OwaSteveManager.applyExplosion(new Vec3d(packet.getX(), packet.getY(), packet.getZ()), packet.getRadius());
    }
}
*///?}
