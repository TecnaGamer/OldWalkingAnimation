package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
//? if >=1.21.5 {
import net.minecraft.world.level.levelgen.Heightmap;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tecna.oldwalkinganimation.OwaSteveManager;

//? if >=1.21.5
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class OwaClientChunkCacheMixin {
    @Shadow @Final private ClientLevel level;

    @Inject(
        method = "replaceWithPacketData",
        at = @At("RETURN")
    )
    //? if >=1.21.5 {
    private void owa$onChunkLoad(int x, int z, FriendlyByteBuf buf,
                                 Map<Heightmap.Types, long[]> heightmaps,
                                 Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> beConsumer,
                                 CallbackInfoReturnable<LevelChunk> cir) {
    //?} else {
    /*private void owa$onChunkLoad(int x, int z, FriendlyByteBuf buf,
                                 CompoundTag nbt,
                                 Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> beConsumer,
                                 CallbackInfoReturnable<LevelChunk> cir) {
    *///?}
        LevelChunk chunk = cir.getReturnValue();
        if (chunk != null) {
            OwaSteveManager.onChunkLoad(level, chunk);
        }
    }
}
//?} else if >=1.21.2 {
/*import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
//? if <1.21.5
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
//? if >=1.21.5
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tecna.oldwalkinganimation.OwaSteveManager;

//? if >=1.21.5
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class OwaClientChunkCacheMixin {
    @Shadow @Final private ClientWorld world;

    @Inject(
        method = "loadChunkFromPacket",
        at = @At("RETURN")
    )
    //? if >=1.21.5 {
    private void owa$onChunkLoad(int x, int z, PacketByteBuf buf,
                                 Map<Heightmap.Type, long[]> heightmaps,
                                 Consumer<ChunkData.BlockEntityVisitor> beConsumer,
                                 CallbackInfoReturnable<WorldChunk> cir) {
    //?} else {
    /^private void owa$onChunkLoad(int x, int z, PacketByteBuf buf,
                                 NbtCompound nbt,
                                 Consumer<ChunkData.BlockEntityVisitor> beConsumer,
                                 CallbackInfoReturnable<WorldChunk> cir) {
    ^///?}
        WorldChunk chunk = cir.getReturnValue();
        if (chunk != null) {
            OwaSteveManager.onChunkLoad(world, chunk);
        }
    }
}
*///?} else {
/*import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tecna.oldwalkinganimation.OwaSteveManager;

import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public abstract class OwaClientChunkCacheMixin {
    @Shadow @Final private ClientWorld world;

    @Inject(
        method = "loadChunkFromPacket",
        at = @At("RETURN")
    )
    private void owa$onChunkLoad(int x, int z, PacketByteBuf buf,
                                 NbtCompound nbt,
                                 Consumer<ChunkData.BlockEntityVisitor> beConsumer,
                                 CallbackInfoReturnable<WorldChunk> cir) {
        WorldChunk chunk = cir.getReturnValue();
        if (chunk != null) {
            OwaSteveManager.onChunkLoad(world, chunk);
        }
    }
}
*///?}
