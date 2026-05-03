package tecna.oldwalkinganimation;

//? if >=26.1 || neoforge {
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.PlayerModelPart;
//? if >=1.21.9 {
import net.minecraft.world.entity.player.PlayerSkin;
//?} else {
/*import net.minecraft.client.resources.PlayerSkin;
*///?}
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Supplier;

import static tecna.oldwalkinganimation.config.Config.*;

public class OwaSteve extends RemotePlayer {

    private static final Random RANDOM = new Random();

    private double rotation;
    private double rotationMotionFactor;
    private double stepAccumulator;
    public final float owaTimeOffs = (float) (RANDOM.nextDouble() * 1239813.0);

    private Supplier<PlayerSkin> skinSupplier;
    private Supplier<String> nameSupplier;

    public OwaSteve(ClientLevel level, GameProfile profile) {
        super(level, profile);
        this.noPhysics = false;
        //? if <1.20.5 {
        /*// RemotePlayer's constructor sets maxUpStep=1.0 for remote-position smoothing of other
        // players in multiplayer; that lets our locally-simulated Steves step up full blocks.
        // Reset to the regular player step height so they walk like normal Steves. 1.20.5+
        // dropped this hack (step height became an attribute defaulting to ~0.6).
        this.setMaxUpStep(0.6F);
        *///?}
        this.rotation = RANDOM.nextDouble() * Math.PI * 2.0;
        this.rotationMotionFactor = (RANDOM.nextDouble() + 1.0) * 0.01;
        applyYaw();
    }

    public void setSkinSupplier(Supplier<PlayerSkin> supplier) {
        this.skinSupplier = supplier;
    }

    public void setNameSupplier(Supplier<String> supplier) {
        this.nameSupplier = supplier;
    }

    @Override
    public PlayerSkin getSkin() {
        if (this.skinSupplier != null) {
            PlayerSkin s = this.skinSupplier.get();
            if (s != null) return s;
        }
        return DefaultPlayerSkin.get(this.getUUID());
    }

    public static boolean isClassicAnim(LivingEntity entity) {
        return steveClassicAnim && entity instanceof OwaSteve;
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isSpectator() { return false; }
    @Override public boolean shouldShowName() { return steveShowNames; }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return true;
    }

    //? if >=26.1 {
    // Vanilla AbstractClientPlayer.showExtraEars is set once at construction from
    // gameProfile.name() == "deadmau5". Our Steves are always constructed with name "Steve",
    // so override the getter to honour the live nameSupplier. (Method only exists on 26.1+.)
    @Override
    public boolean showExtraEars() {
        if (nameSupplier != null) {
            String n = nameSupplier.get();
            if ("deadmau5".equals(n)) return true;
        }
        return super.showExtraEars();
    }
    //?}

    private Component owa$dynamicCustomName = null;
    private com.mojang.authlib.GameProfile owa$dynamicProfile = null;

    public void owa$syncNameToCustomName() {
        if (nameSupplier == null) return;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) {
            owa$dynamicCustomName = null;
            owa$dynamicProfile = null;
            return;
        }
        if (owa$dynamicCustomName != null && n.equals(owa$dynamicCustomName.getString())) return;
        owa$dynamicCustomName = Component.literal(n);
        owa$dynamicProfile = new com.mojang.authlib.GameProfile(this.getUUID(), n);
    }

    /** The name the supplier currently wants for this Steve, or null if unresolved. */
    public String owa$getExpectedName() {
        if (nameSupplier == null) return null;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) return null;
        return n;
    }

    /** Records the GameProfile name this Steve was constructed with, for re-spawn checks. */
    private String owa$spawnedName = "Steve";
    public void owa$setSpawnedName(String n) { this.owa$spawnedName = n; }
    public String owa$getSpawnedName() { return this.owa$spawnedName; }

    @Override
    public com.mojang.authlib.GameProfile getGameProfile() {
        if (owa$dynamicProfile != null) return owa$dynamicProfile;
        return super.getGameProfile();
    }

    @Override
    public Component getCustomName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        return super.getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        return owa$dynamicCustomName != null || super.hasCustomName();
    }

    @Override
    public Component getName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        if (nameSupplier != null) {
            String n = nameSupplier.get();
            if (n != null && !n.isEmpty()) return Component.literal(n);
        }
        return super.getName();
    }

    @Override
    public void tick() {
        owa$syncNameToCustomName();
        super.tick();
    }

    @Override
    public void aiStep() {
        float scale = OwaTimeScale.scaleFactor();
        int steps = 1;
        if (scale > 1.0f) {
            stepAccumulator += scale;
            steps = (int) stepAccumulator;
            stepAccumulator -= steps;
            if (steps < 1) steps = 1;
        } else {
            stepAccumulator = 0.0;
        }
        for (int i = 0; i < steps; i++) {
            doPhysicsStep();
        }
    }

    private void doPhysicsStep() {
        this.noPhysics = false;

        double jumpVel, gravity, accelGround, accelAir, groundFrictionExtra, rotAAccum;
        float jumpChance;

        if (authenticPhysics && rdEarlyPhysics) {
            jumpVel = 0.12;
            gravity = 0.005;
            jumpChance = 0.01F;
            accelGround = 0.02;
            accelAir = 0.005;
            groundFrictionExtra = 0.8;
            rotAAccum = 0.01;
        } else if (authenticPhysics) {
            jumpVel = 0.5;
            gravity = 0.08;
            jumpChance = 0.08F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.08;
        } else {
            jumpVel = 0.42;
            gravity = 0.08;
            jumpChance = 0.04F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.01;
        }

        this.rotation += this.rotationMotionFactor;
        this.rotationMotionFactor *= 0.99;
        this.rotationMotionFactor += (RANDOM.nextDouble() - RANDOM.nextDouble())
                * RANDOM.nextDouble() * RANDOM.nextDouble() * rotAAccum;

        double vertical = Math.sin(this.rotation);
        double forward = Math.cos(this.rotation);

        if (this.onGround() && RANDOM.nextFloat() < jumpChance) {
            Vec3 v = this.getDeltaMovement();
            this.setDeltaMovement(v.x, jumpVel, v.z);
        }

        double accel = this.onGround() ? accelGround : accelAir;

        Vec3 dm = this.getDeltaMovement();
        double dx = dm.x + vertical * accel;
        double dz = dm.z + forward * accel;
        double dy = dm.y - gravity;

        this.setDeltaMovement(dx, dy, dz);
        this.move(MoverType.SELF, this.getDeltaMovement());

        Vec3 nd = this.getDeltaMovement();
        dx = nd.x * 0.91;
        dz = nd.z * 0.91;
        if (this.onGround()) {
            dx *= groundFrictionExtra;
            dz *= groundFrictionExtra;
            dy = 0.0;
        } else {
            dy = nd.y * 0.98;
        }
        this.setDeltaMovement(dx, dy, dz);

        applyYaw();
        this.updateSwingTime();
    }

    private void applyYaw() {
        float yawDeg = (float) Math.toDegrees(-this.rotation);
        this.setYRot(yawDeg);
        this.yRotO = yawDeg;
        this.setYHeadRot(yawDeg);
        this.setYBodyRot(yawDeg);
    }
}
//?} else if >=1.21.9 {
/*import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.Supplier;

import static tecna.oldwalkinganimation.config.Config.*;

public class OwaSteve extends OtherClientPlayerEntity {

    private static final Random RANDOM = new Random();

    private double rotation;
    private double rotationMotionFactor;
    private double stepAccumulator;
    public final float owaTimeOffs = (float) (RANDOM.nextDouble() * 1239813.0);

    private Supplier<SkinTextures> skinSupplier;
    private Supplier<String> nameSupplier;

    public OwaSteve(ClientWorld world, GameProfile profile) {
        super(world, profile);
        this.noClip = false;
        // OtherClientPlayerEntity sets stepHeight=1.0 in the constructor for remote-position
        // smoothing in MP; that lets our locally-simulated Steves walk up full blocks. Reset to
        // standard player step. 1.20.5+ moved this to an attribute so no reset needed there.
        //? if <1.20.5
        /^this.setStepHeight(0.6F);^/
        this.rotation = RANDOM.nextDouble() * Math.PI * 2.0;
        this.rotationMotionFactor = (RANDOM.nextDouble() + 1.0) * 0.01;
        applyYaw();
    }

    public void setSkinSupplier(Supplier<SkinTextures> supplier) {
        this.skinSupplier = supplier;
    }

    public void setNameSupplier(Supplier<String> supplier) {
        this.nameSupplier = supplier;
    }

    @Override
    public SkinTextures getSkin() {
        if (this.skinSupplier != null) {
            SkinTextures s = this.skinSupplier.get();
            if (s != null) return s;
        }
        return DefaultSkinHelper.getSkinTextures(this.getUuid());
    }

    public static boolean isClassicAnim(LivingEntity entity) {
        return steveClassicAnim && entity instanceof OwaSteve;
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean canHit() { return false; }
    @Override public boolean isSpectator() { return false; }
    @Override public boolean shouldRenderName() { return steveShowNames; }

    @Override
    public boolean isModelPartVisible(PlayerModelPart part) {
        return true;
    }

    private Text owa$dynamicCustomName = null;

    public void owa$syncNameToCustomName() {
        if (nameSupplier == null) return;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) {
            owa$dynamicCustomName = null;
            return;
        }
        if (owa$dynamicCustomName != null && n.equals(owa$dynamicCustomName.getString())) return;
        owa$dynamicCustomName = Text.literal(n);
    }

    public String owa$getExpectedName() {
        if (nameSupplier == null) return null;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) return null;
        return n;
    }

    private String owa$spawnedName = "Steve";
    public void owa$setSpawnedName(String n) { this.owa$spawnedName = n; }
    public String owa$getSpawnedName() { return this.owa$spawnedName; }

    @Override
    public Text getCustomName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        return super.getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        return owa$dynamicCustomName != null || super.hasCustomName();
    }

    @Override
    public Text getName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        if (nameSupplier != null) {
            String n = nameSupplier.get();
            if (n != null && !n.isEmpty()) return Text.literal(n);
        }
        return super.getName();
    }

    @Override
    public void tickMovement() {
        float scale = OwaTimeScale.scaleFactor();
        int steps = 1;
        if (scale > 1.0f) {
            stepAccumulator += scale;
            steps = (int) stepAccumulator;
            stepAccumulator -= steps;
            if (steps < 1) steps = 1;
        } else {
            stepAccumulator = 0.0;
        }
        for (int i = 0; i < steps; i++) {
            doPhysicsStep();
        }
    }

    private void doPhysicsStep() {
        this.noClip = false;

        double jumpVel, gravity, accelGround, accelAir, groundFrictionExtra, rotAAccum;
        float jumpChance;

        if (authenticPhysics && rdEarlyPhysics) {
            jumpVel = 0.12;
            gravity = 0.005;
            jumpChance = 0.01F;
            accelGround = 0.02;
            accelAir = 0.005;
            groundFrictionExtra = 0.8;
            rotAAccum = 0.01;
        } else if (authenticPhysics) {
            jumpVel = 0.5;
            gravity = 0.08;
            jumpChance = 0.08F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.08;
        } else {
            jumpVel = 0.42;
            gravity = 0.08;
            jumpChance = 0.04F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.01;
        }

        this.rotation += this.rotationMotionFactor;
        this.rotationMotionFactor *= 0.99;
        this.rotationMotionFactor += (RANDOM.nextDouble() - RANDOM.nextDouble())
                * RANDOM.nextDouble() * RANDOM.nextDouble() * rotAAccum;

        double vertical = Math.sin(this.rotation);
        double forward = Math.cos(this.rotation);

        if (this.isOnGround() && RANDOM.nextFloat() < jumpChance) {
            Vec3d v = this.getVelocity();
            this.setVelocity(v.x, jumpVel, v.z);
        }

        double accel = this.isOnGround() ? accelGround : accelAir;

        Vec3d dm = this.getVelocity();
        double dx = dm.x + vertical * accel;
        double dz = dm.z + forward * accel;
        double dy = dm.y - gravity;

        this.setVelocity(dx, dy, dz);
        this.move(MovementType.SELF, this.getVelocity());

        Vec3d nd = this.getVelocity();
        dx = nd.x * 0.91;
        dz = nd.z * 0.91;
        if (this.isOnGround()) {
            dx *= groundFrictionExtra;
            dz *= groundFrictionExtra;
            dy = 0.0;
        } else {
            dy = nd.y * 0.98;
        }
        this.setVelocity(dx, dy, dz);

        applyYaw();
        this.tickHandSwing();
    }

    private void applyYaw() {
        float yawDeg = (float) Math.toDegrees(-this.rotation);
        this.setYaw(yawDeg);
        this.lastYaw = yawDeg;
        this.setHeadYaw(yawDeg);
        this.setBodyYaw(yawDeg);
    }
}
*///?} else {
/*import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
//? if >=1.21
import net.minecraft.entity.player.PlayerModelPart;
//? if <1.21
/^import net.minecraft.client.render.entity.PlayerModelPart;^/
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.Supplier;

import static tecna.oldwalkinganimation.config.Config.*;

public class OwaSteve extends OtherClientPlayerEntity {

    private static final Random RANDOM = new Random();

    private double rotation;
    private double rotationMotionFactor;
    private double stepAccumulator;
    public final float owaTimeOffs = (float) (RANDOM.nextDouble() * 1239813.0);

    private Supplier<SkinTextures> skinSupplier;
    private Supplier<String> nameSupplier;

    public OwaSteve(ClientWorld world, GameProfile profile) {
        super(world, profile);
        this.noClip = false;
        // OtherClientPlayerEntity sets stepHeight=1.0 in the constructor for remote-position
        // smoothing in MP; that lets our locally-simulated Steves walk up full blocks. Reset to
        // standard player step. 1.20.5+ moved this to an attribute so no reset needed there.
        //? if <1.20.5
        /^this.setStepHeight(0.6F);^/
        this.rotation = RANDOM.nextDouble() * Math.PI * 2.0;
        this.rotationMotionFactor = (RANDOM.nextDouble() + 1.0) * 0.01;
        applyYaw();
    }

    public void setSkinSupplier(Supplier<SkinTextures> supplier) {
        this.skinSupplier = supplier;
    }

    public void setNameSupplier(Supplier<String> supplier) {
        this.nameSupplier = supplier;
    }

    @Override
    public SkinTextures getSkinTextures() {
        if (this.skinSupplier != null) {
            SkinTextures s = this.skinSupplier.get();
            if (s != null) return s;
        }
        return DefaultSkinHelper.getSkinTextures(this.getUuid());
    }

    public static boolean isClassicAnim(LivingEntity entity) {
        return steveClassicAnim && entity instanceof OwaSteve;
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean canHit() { return false; }
    @Override public boolean isSpectator() { return false; }
    @Override public boolean shouldRenderName() { return steveShowNames; }

    @Override
    public boolean isPartVisible(PlayerModelPart part) {
        return true;
    }

    private Text owa$dynamicCustomName = null;

    @Override
    public Text getCustomName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        return super.getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        return owa$dynamicCustomName != null || super.hasCustomName();
    }

    @Override
    public Text getName() {
        if (owa$dynamicCustomName != null) return owa$dynamicCustomName;
        if (nameSupplier != null) {
            String n = nameSupplier.get();
            if (n != null && !n.isEmpty()) return Text.literal(n);
        }
        return super.getName();
    }

    public void owa$syncNameToCustomName() {
        if (nameSupplier == null) return;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) {
            owa$dynamicCustomName = null;
            return;
        }
        if (owa$dynamicCustomName != null && n.equals(owa$dynamicCustomName.getString())) return;
        owa$dynamicCustomName = Text.literal(n);
    }

    public String owa$getExpectedName() {
        if (nameSupplier == null) return null;
        String n = nameSupplier.get();
        if (n == null || n.isEmpty() || "Custom".equals(n)) return null;
        return n;
    }

    private String owa$spawnedName = "Steve";
    public void owa$setSpawnedName(String n) { this.owa$spawnedName = n; }
    public String owa$getSpawnedName() { return this.owa$spawnedName; }

    @Override
    public void tickMovement() {
        float scale = OwaTimeScale.scaleFactor();
        int steps = 1;
        if (scale > 1.0f) {
            stepAccumulator += scale;
            steps = (int) stepAccumulator;
            stepAccumulator -= steps;
            if (steps < 1) steps = 1;
        } else {
            stepAccumulator = 0.0;
        }
        for (int i = 0; i < steps; i++) {
            doPhysicsStep();
        }
    }

    private void doPhysicsStep() {
        this.noClip = false;

        double jumpVel, gravity, accelGround, accelAir, groundFrictionExtra, rotAAccum;
        float jumpChance;

        if (authenticPhysics && rdEarlyPhysics) {
            jumpVel = 0.12;
            gravity = 0.005;
            jumpChance = 0.01F;
            accelGround = 0.02;
            accelAir = 0.005;
            groundFrictionExtra = 0.8;
            rotAAccum = 0.01;
        } else if (authenticPhysics) {
            jumpVel = 0.5;
            gravity = 0.08;
            jumpChance = 0.08F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.08;
        } else {
            jumpVel = 0.42;
            gravity = 0.08;
            jumpChance = 0.04F;
            accelGround = 0.1;
            accelAir = 0.02;
            groundFrictionExtra = 0.7;
            rotAAccum = 0.01;
        }

        this.rotation += this.rotationMotionFactor;
        this.rotationMotionFactor *= 0.99;
        this.rotationMotionFactor += (RANDOM.nextDouble() - RANDOM.nextDouble())
                * RANDOM.nextDouble() * RANDOM.nextDouble() * rotAAccum;

        double vertical = Math.sin(this.rotation);
        double forward = Math.cos(this.rotation);

        if (this.isOnGround() && RANDOM.nextFloat() < jumpChance) {
            Vec3d v = this.getVelocity();
            this.setVelocity(v.x, jumpVel, v.z);
        }

        double accel = this.isOnGround() ? accelGround : accelAir;

        Vec3d dm = this.getVelocity();
        double dx = dm.x + vertical * accel;
        double dz = dm.z + forward * accel;
        double dy = dm.y - gravity;

        this.setVelocity(dx, dy, dz);
        this.move(MovementType.SELF, this.getVelocity());

        Vec3d nd = this.getVelocity();
        dx = nd.x * 0.91;
        dz = nd.z * 0.91;
        if (this.isOnGround()) {
            dx *= groundFrictionExtra;
            dz *= groundFrictionExtra;
            dy = 0.0;
        } else {
            dy = nd.y * 0.98;
        }
        this.setVelocity(dx, dy, dz);

        applyYaw();
        this.tickHandSwing();
    }

    private void applyYaw() {
        float yawDeg = (float) Math.toDegrees(-this.rotation);
        this.setYaw(yawDeg);
        //? if >=1.21.5 {
        /^this.lastYaw = yawDeg;^/
        //?} else
        this.prevYaw = yawDeg;
        this.setHeadYaw(yawDeg);
        this.setBodyYaw(yawDeg);
    }
}
*///?}
