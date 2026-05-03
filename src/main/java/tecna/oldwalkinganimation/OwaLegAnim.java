//? if >=26.1 || (neoforge && >=1.21.2) {
package tecna.oldwalkinganimation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static tecna.oldwalkinganimation.config.Config.*;

public final class OwaLegAnim {
    private OwaLegAnim() {}

    public static LivingEntity entityFor(LivingEntityRenderState state) {
        if (!enableMod) return null;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return null;
        if (!enableMobs && !(entity instanceof Player)) return null;
        return entity;
    }

    public static void quadLegs(ModelPart rHind, ModelPart lHind, ModelPart rFront, ModelPart lFront, float var8, float ismoving) {
        float c = Mth.cos(var8 * 0.6662F) * 1.4F * ismoving;
        float cp = Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        rHind.xRot = c;
        lHind.xRot = cp;
        rFront.xRot = cp;
        lFront.xRot = c;
    }

    public static void bipedLegs(ModelPart right, ModelPart left, float var8, float ismoving, float scale) {
        right.xRot = Mth.cos(var8 * 0.6662F) * 1.4F * ismoving * scale;
        left.xRot = Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving * scale;
    }

    public static void headBob(ModelPart head, float var8, float ismoving) {
        if (!headBob || head == null) return;
        head.yRot += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
        head.xRot += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
    }
}
//?} else if neoforge {
/*package tecna.oldwalkinganimation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static tecna.oldwalkinganimation.config.Config.*;

public final class OwaLegAnim {
    private OwaLegAnim() {}

    public static LivingEntity entityFor(LivingEntity entity) {
        if (!enableMod) return null;
        if (entity == null) return null;
        if (!enableMobs && !(entity instanceof Player)) return null;
        return entity;
    }

    public static void quadLegs(ModelPart rHind, ModelPart lHind, ModelPart rFront, ModelPart lFront, float var8, float ismoving) {
        float c = Mth.cos(var8 * 0.6662F) * 1.4F * ismoving;
        float cp = Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving;
        rHind.xRot = c;
        lHind.xRot = cp;
        rFront.xRot = cp;
        lFront.xRot = c;
    }

    public static void bipedLegs(ModelPart right, ModelPart left, float var8, float ismoving, float scale) {
        right.xRot = Mth.cos(var8 * 0.6662F) * 1.4F * ismoving * scale;
        left.xRot = Mth.cos(var8 * 0.6662F + Mth.PI) * 1.4F * ismoving * scale;
    }

    public static void headBob(ModelPart head, float var8, float ismoving) {
        if (!headBob || head == null) return;
        head.yRot += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
        head.xRot += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
    }
}
*///?} else if >=1.21.2 {
/*package tecna.oldwalkinganimation;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import static tecna.oldwalkinganimation.config.Config.*;

public final class OwaLegAnim {
    private OwaLegAnim() {}

    public static LivingEntity entityFor(LivingEntityRenderState state) {
        if (!enableMod) return null;
        LivingEntity entity = OwaStateHolder.getEntity(state);
        if (entity == null) return null;
        if (!enableMobs && !(entity instanceof PlayerEntity)) return null;
        return entity;
    }

    public static void quadLegs(ModelPart rHind, ModelPart lHind, ModelPart rFront, ModelPart lFront, float var8, float ismoving) {
        float c = MathHelper.cos(var8 * 0.6662F) * 1.4F * ismoving;
        float cp = MathHelper.cos(var8 * 0.6662F + (float) Math.PI) * 1.4F * ismoving;
        rHind.pitch = c;
        lHind.pitch = cp;
        rFront.pitch = cp;
        lFront.pitch = c;
    }

    public static void bipedLegs(ModelPart right, ModelPart left, float var8, float ismoving, float scale) {
        right.pitch = MathHelper.cos(var8 * 0.6662F) * 1.4F * ismoving * scale;
        left.pitch = MathHelper.cos(var8 * 0.6662F + (float) Math.PI) * 1.4F * ismoving * scale;
    }

    public static void headBob(ModelPart head, float var8, float ismoving) {
        if (!headBob || head == null) return;
        head.yaw += ((float) Math.sin((var8 * headSpeed) * 0.83D) * headAmount) * ismoving;
        head.pitch += ((float) Math.sin((var8 * headSpeed) * 0.8F) * headAmount) * ismoving;
    }
}
*///?}
