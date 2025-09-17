package tecna.oldwalkinganimation.mixin;


import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

//
//    @Inject(method = "getBlockLight", at = @At("RETURN"), cancellable = true)
//    protected void getBlockLight(T entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
//        if (OwaConfigManager.getConfig().enableMod && OwaConfigManager.getConfig().damageGlow) {
//            if (entity instanceof LivingEntity livingEntity) {
//                if (livingEntity.hurtTime > 0 || livingEntity.deathTime > 0) {
//                    cir.setReturnValue(15);
//                }
//            }
//
//        }
//
//
//    }


}