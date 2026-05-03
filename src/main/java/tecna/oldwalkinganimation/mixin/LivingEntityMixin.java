package tecna.oldwalkinganimation.mixin;

//? if >=26.1 || neoforge {
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
}
//?} else if >=1.21.2 {
/*import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
}
*///?} else {
/*import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tecna.oldwalkinganimation.config.Config;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public float bodyYaw;

    @Shadow public float headYaw;

    @Shadow public float handSwingProgress;

    @Shadow public float prevBodyYaw;

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow protected abstract float turnHead(float bodyRotation, float headRotation);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }


    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;turnHead(FF)F"))
    public float redirectTurnHead(LivingEntity instance, float bodyRotation, float headRotation) {
        // Defer to vanilla turnHead for players so vanilla's body-yaw smoothing (rate 0.3 + 50°
        // head clamp) runs every tick regardless of camera mode. The renderer mixin's
        // owa$preRenderApplyBody only fires in third-person; without vanilla's per-tick update,
        // a player's body yaw would freeze in first-person and look mis-rotated on F5 back.
        if (instance instanceof PlayerEntity) {
            return this.turnHead(bodyRotation, headRotation);
        }
        if (Config.enableMod && Config.bodyRot
                //? if >=1.19.4
                && !instance.hasControllingPassenger()
                && !(instance.getVehicle() instanceof BoatEntity) && !(instance instanceof ArmorStandEntity)) {
        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);

        this.bodyYaw += f * 0.1f;


        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);

        boolean bl = g < -90.0F || g >= 90.0F;
        if (bl) {
            headRotation *= -1.0F;
        }



        return headRotation;
        } else {
            return this.turnHead(bodyRotation, headRotation);
        }
    }


}
*///?}
