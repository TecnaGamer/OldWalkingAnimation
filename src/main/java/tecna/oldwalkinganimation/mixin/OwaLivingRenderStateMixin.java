package tecna.oldwalkinganimation.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=26.1 {
import org.spongepowered.asm.mixin.Unique;
import tecna.oldwalkinganimation.OwaStateHolder;

@Mixin(net.minecraft.client.renderer.entity.state.LivingEntityRenderState.class)
public class OwaLivingRenderStateMixin implements OwaStateHolder {
    @Unique private net.minecraft.world.entity.LivingEntity owa$entity;
    @Unique private float owa$partialTick;

    @Override public void owa$setEntity(net.minecraft.world.entity.LivingEntity entity) { this.owa$entity = entity; }
    @Override public net.minecraft.world.entity.LivingEntity owa$getEntity() { return this.owa$entity; }
    @Override public void owa$setPartialTick(float partialTick) { this.owa$partialTick = partialTick; }
    @Override public float owa$getPartialTick() { return this.owa$partialTick; }
}
//?} else if >=1.21.2 {
/*@Mixin(java.lang.Object.class)
public class OwaLivingRenderStateMixin {
}
*///?} else {
/*@Mixin(java.lang.Object.class)
public class OwaLivingRenderStateMixin {
}
*///?}
