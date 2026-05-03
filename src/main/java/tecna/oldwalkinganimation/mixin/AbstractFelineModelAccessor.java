//? if >=26.1 {
package tecna.oldwalkinganimation.mixin;

import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFelineModel.class)
public interface AbstractFelineModelAccessor {
    @Accessor("head") ModelPart owa$getHead();
    @Accessor("rightHindLeg") ModelPart owa$getRightHindLeg();
    @Accessor("leftHindLeg") ModelPart owa$getLeftHindLeg();
    @Accessor("rightFrontLeg") ModelPart owa$getRightFrontLeg();
    @Accessor("leftFrontLeg") ModelPart owa$getLeftFrontLeg();
}
//?}
