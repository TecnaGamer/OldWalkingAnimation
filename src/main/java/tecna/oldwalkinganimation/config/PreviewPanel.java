//? if >=26.1 || (neoforge && >=1.21.6) {
package tecna.oldwalkinganimation.config;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
//? if >=1.21.9 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} else {
/*import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?}
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PreviewPanel {
    public enum Mode {
        IDLE("Idle"), WALK("Walk"), RUN("Run"), SNEAK("Sneak");
        public final String label;
        Mode(String l) { this.label = l; }
    }

    private static final long TICK_INTERVAL_MS = 50L;
    private static final int DAMAGE_CYCLE_TICKS = 25;
    private static final int DAMAGE_HURT_TIME = 10;
    private static final int HIT_DAMAGE = 1;
    private static final int MAX_HEALTH = 20;
    private static final int DEATH_ANIM_TICKS = 20;
    private static final int RESPAWN_DELAY_TICKS = 40;

    private Mode mode = Mode.WALK;
    private RemotePlayer fake;
    private ClientLevel lastLevel;
    private long lastTickMs = 0L;
    private int damageCycleTick = 0;
    private int accumulatedDamage = 0;
    private float lastMouseX = 0f;
    private float lastMouseY = 0f;
    private float lastCenterX = 0f;
    private float lastCenterY = 0f;
    private int lastSize = 0;
    private double currentDx = 0.0;
    private float currentAnimSpeed = 0.0f;
    private boolean owa$cloakInited = false;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    //? if >=1.21.11 {
    private static final Identifier[] POOF_FRAMES = {
        Identifier.withDefaultNamespace("textures/particle/generic_0.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_1.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_2.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_3.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_4.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_5.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_6.png"),
        Identifier.withDefaultNamespace("textures/particle/generic_7.png"),
    };
    //?} else {
    /*private static final ResourceLocation[] POOF_FRAMES = {
        ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_1.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_2.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_3.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_4.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_5.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_6.png"),
        ResourceLocation.withDefaultNamespace("textures/particle/generic_7.png"),
    };
    *///?}

    private static class Particle {
        float x, y, vx, vy;
        float gravity, drag;
        int life, maxLife, size;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode m) {
        if (mode == m) return;
        mode = m;
        damageCycleTick = 0;
        if (fake != null && fake.deathTime == 0) {
            fake.hurtTime = 0;
            fake.setDeltaMovement(Vec3.ZERO);
        }
    }

    public boolean available() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.player != null;
    }

    //? if >=26.1 {
    public void render(GuiGraphicsExtractor gge, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
    //?} else {
    /*public void render(GuiGraphics gge, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
    *///?}
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            drawUnavailable(gge, x0, y0, x1, y1);
            return;
        }

        if (fake == null || lastLevel != mc.level) {
            GameProfile profile = mc.player.getGameProfile();
            fake = new RemotePlayer(mc.level, profile);
            tecna.oldwalkinganimation.OwaStateHolder.markPreview(fake);
            // Copy the local player's DATA_PLAYER_MODE_CUSTOMISATION byte so the preview shows
            // outer skin layers (jacket/hat/sleeve/pants). RemotePlayer defaults the byte to 0.
            try {
                java.lang.reflect.Field accessorField = null;
                for (Class<?> c = net.minecraft.world.entity.player.Player.class; c != null && accessorField == null; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType() == net.minecraft.network.syncher.EntityDataAccessor.class
                                && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                && f.getName().toUpperCase().contains("CUSTOMIS")) {
                            accessorField = f;
                            break;
                        }
                    }
                }
                if (accessorField != null) {
                    accessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    net.minecraft.network.syncher.EntityDataAccessor<Byte> accessor =
                            (net.minecraft.network.syncher.EntityDataAccessor<Byte>) accessorField.get(null);
                    fake.getEntityData().set(accessor, mc.player.getEntityData().get(accessor));
                }
            } catch (Throwable ignored) {}
            lastLevel = mc.level;
            lastTickMs = System.currentTimeMillis();
            damageCycleTick = 0;
            double rx = mc.player.getX();
            double ry = mc.player.getY();
            double rz = mc.player.getZ();
            fake.setPosRaw(rx, ry, rz);
            fake.xOld = rx; fake.yOld = ry; fake.zOld = rz;
            fake.xo = rx;   fake.yo = ry;   fake.zo = rz;
            fake.setYRot(0f);
            fake.setYBodyRot(0f);
            fake.setYHeadRot(0f);
            fake.setXRot(0f);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastCenterX = (x0 + x1) / 2.0f;
        lastCenterY = (y0 + y1) / 2.0f;

        tickFake();

        int rectW = x1 - x0;
        int rectH = y1 - y0;
        int sizeByH = (int) (rectH / 2.8f);
        int sizeByW = (int) (rectW / 1.7f);
        int size = Math.max(24, Math.min(sizeByH, sizeByW));
        lastSize = size;
        if (!isGone()) {
            float centerY = (y0 + y1) / 2.0f;
            float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);
            if (yAngle > 0) {
                renderEntity(gge, x0, y0, x1, y1, size, 0.0625f, mouseX, mouseY);
                drawShadow(gge, x0, y0, x1, y1, size, mouseX, mouseY);
            } else {
                drawShadow(gge, x0, y0, x1, y1, size, mouseX, mouseY);
                renderEntity(gge, x0, y0, x1, y1, size, 0.0625f, mouseX, mouseY);
            }
        }
        renderParticles(gge, x0, y0, x1, y1);
        owa$checkHoverHint(mouseX, mouseY);
    }

    private void owa$checkHoverHint(float mouseX, float mouseY) {
        if (lastSize <= 0) return;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mouseX >= lastCenterX - halfW && mouseX < lastCenterX + halfW
            && mouseY >= lastCenterY - halfH && mouseY < lastCenterY + halfH) {
            KillStats.tryShowHoverHint();
        }
    }

    private boolean isGone() {
        return fake != null && fake.deathTime >= DEATH_ANIM_TICKS;
    }

    private double dxForMode() {
        switch (mode) {
            case WALK:   return 0.21;
            case RUN:    return 0.289;
            case SNEAK:  return 0.065;
            default:     return 0.0;
        }
    }

    private float targetSpeedForMode() {
        switch (mode) {
            case RUN:   return 1.0f;
            case WALK:  return 0.7f;
            case SNEAK: return 0.25f;
            default:    return 0f;
        }
    }

    private void tickFake() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickMs;
        if (elapsed < TICK_INTERVAL_MS) return;
        boolean dying = fake.deathTime > 0 && fake.deathTime < DEATH_ANIM_TICKS;
        long maxTicks = dying ? 1L : 5L;
        int ticks = (int) Math.min(maxTicks, elapsed / TICK_INTERVAL_MS);
        lastTickMs += (long) ticks * TICK_INTERVAL_MS;
        for (int i = 0; i < ticks; i++) doTick();
    }

    private void doTick() {
        tickParticles();

        if (fake.deathTime > 0) {
            handleDeathTick();
            return;
        }

        // Vanilla BipedEntityModel.setAngles passes entity.tickCount (mojmap) into
        // CrossbowPosing.swingArm which adds the subtle idle arm sway. The preview entity is
        // never world-ticked so its tickCount stays at 0 and the sway only varies by
        // partialTick — a sub-pixel wobble. Advance it manually each tick.
        fake.tickCount++;

        double targetDx = dxForMode();
        float targetAnim = targetSpeedForMode();
        double dxRate = (Math.abs(targetDx) > Math.abs(currentDx)) ? 0.20 : 0.40;
        float animRate = (targetAnim > currentAnimSpeed) ? 0.20f : 0.40f;
        currentDx += (targetDx - currentDx) * dxRate;
        currentAnimSpeed += (targetAnim - currentAnimSpeed) * animRate;

        boolean sneak = mode == Mode.SNEAK;

        fake.setShiftKeyDown(sneak);
        fake.setPose(sneak ? Pose.CROUCHING : Pose.STANDING);

        double curX = fake.getX();
        double curY = fake.getY();
        double curZ = fake.getZ();

        // Move along body-forward (mirrors xAngle in renderEntity) so cape lag rotates with the
        // body when mouse turns the preview, instead of trailing world-space -Z.
        float xAngleTick = (float) Math.atan((lastCenterX - lastMouseX) / 40.0F);
        float facing = 180.0F + xAngleTick * 20.0F;
        float yawRad = (float) Math.toRadians(facing);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        fake.xOld = curX; fake.yOld = curY; fake.zOld = curZ;
        fake.xo = curX;   fake.yo = curY;   fake.zo = curZ;
        double newX = curX + currentDx * dirX;
        double newY = curY;
        double newZ = curZ + currentDx * dirZ;
        fake.setPosRaw(newX, newY, newZ);

        // Drive cape sway. Vanilla updates xCloak in moveCloak() but our preview entity
        // doesn't run vanilla's tick path. Lazy-init on first tick so the first delta isn't huge.
        // Gated to <1.21.9 — at 1.21.9+ Mojang dropped Player.xCloak/yCloak/zCloak entirely and
        // moved the cape rotation onto PlayerEntityRenderState, computed at render-state-update
        // time instead of from a tracked entity-space lag.
        //? if <1.21.9 {
        if (!owa$cloakInited) {
            fake.xCloak = curX; fake.yCloak = curY; fake.zCloak = curZ;
            fake.xCloakO = curX; fake.yCloakO = curY; fake.zCloakO = curZ;
            owa$cloakInited = true;
        }
        fake.xCloakO = fake.xCloak;
        fake.yCloakO = fake.yCloak;
        fake.zCloakO = fake.zCloak;
        double cdx = newX - fake.xCloak;
        double cdy = newY - fake.yCloak;
        double cdz = newZ - fake.zCloak;
        if (cdx > 10 || cdx < -10) fake.xCloak = newX;
        if (cdz > 10 || cdz < -10) fake.zCloak = newZ;
        if (cdy > 10 || cdy < -10) fake.yCloak = newY;
        fake.xCloak += cdx * 0.25;
        fake.yCloak += cdy * 0.25;
        fake.zCloak += cdz * 0.25;
        //?}
        //? if >=26.1 {
        // Mojmap >=26.1 (1.21.9+) moved cape lag tracking onto ClientAvatarState. Drive it the
        // same way AbstractClientPlayer.tick() does — pos+vel each tick, plus walk-distance and
        // bob terms so AvatarRenderer.extractCapeState produces the lean and lift+bobbing.
        net.minecraft.client.entity.ClientAvatarState clientState = fake.avatarState();
        clientState.tick(new net.minecraft.world.phys.Vec3(newX, newY, newZ),
                         new net.minecraft.world.phys.Vec3(currentDx * dirX, 0, currentDx * dirZ));
        clientState.updateBob((float) Math.min(0.1, Math.abs(currentDx)));
        clientState.addWalkDistance((float) (Math.abs(currentDx) * 0.6));
        //?}

        fake.yRotO     = fake.getYRot();
        fake.xRotO     = fake.getXRot();
        fake.yBodyRotO = fake.yBodyRot;
        fake.yHeadRotO = fake.yHeadRot;
        fake.setYRot(facing);
        fake.setXRot(0f);
        fake.setYBodyRot(facing);
        fake.setYHeadRot(facing);

        fake.setOnGround(true);

        fake.walkAnimation.update(currentAnimSpeed, 0.4f, 1.0f);

        if (fake.hurtTime > 0) {
            fake.setDeltaMovement(damageDirFromCursor());
        } else {
            fake.setDeltaMovement(new Vec3(currentDx * dirX, 0, currentDx * dirZ));
            damageCycleTick = 0;
        }
        if (fake.hurtTime > 0) fake.hurtTime--;
    }

    private void handleDeathTick() {
        fake.deathTime++;
        if (fake.hurtTime > 0) fake.hurtTime--;
        fake.setDeltaMovement(Vec3.ZERO);
        // Sync xOld/yOld/zOld with current pos so LivingEntityRendererMixin's
        // var3 = sqrt((x-xOld)^2 + (z-zOld)^2) reads zero while dead. Otherwise entityRun keeps
        // the stale walking-tick delta and the limb anim continues swinging when
        // speedLimbAngle is off.
        fake.xOld = fake.getX();
        fake.yOld = fake.getY();
        fake.zOld = fake.getZ();
        //? if >=26.1 {
        // Keep ticking the cape state so its lag decays toward the (stationary) entity. Without
        // this state.x and state.lastX freeze at the last live-tick values and updateCape's
        // interpolation oscillates between them — visible as a vibrating cape during death.
        net.minecraft.client.entity.ClientAvatarState clientState = fake.avatarState();
        clientState.tick(new net.minecraft.world.phys.Vec3(fake.getX(), fake.getY(), fake.getZ()),
                         net.minecraft.world.phys.Vec3.ZERO);
        clientState.updateBob(0f);
        //?}
        //? if >=1.21.2 {
        fake.walkAnimation.update(0.0f, 0.6f, 1.0f);
        //?} else
        /^fake.walkAnimation.update(0.0f, 0.6f);^/
        if (fake.deathTime == DEATH_ANIM_TICKS) {
            spawnDeathParticles(20);
        }
        if (fake.deathTime >= DEATH_ANIM_TICKS + RESPAWN_DELAY_TICKS) {
            respawn();
        }
    }

    private void hit() {
        if (fake == null || fake.deathTime > 0) return;
        accumulatedDamage += HIT_DAMAGE;
        if (accumulatedDamage >= MAX_HEALTH) {
            triggerDeath();
        } else {
            fake.hurtTime = DAMAGE_HURT_TIME;
            fake.hurtDuration = DAMAGE_HURT_TIME;
            SharedValueUtil.invalidateDamageDir(fake);
            playSound(SoundEvents.PLAYER_HURT);
        }
    }

    private void triggerDeath() {
        fake.setHealth(0);
        fake.deathTime = 1;
        fake.hurtTime = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
        playSound(SoundEvents.PLAYER_DEATH);
        KillStats.onKill();
    }

    private void respawn() {
        fake.deathTime = 0;
        fake.hurtTime = 0;
        fake.setHealth(fake.getMaxHealth());
        accumulatedDamage = 0;
        damageCycleTick = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.core.Holder.direct(sound), 1.0f));
    }

    private void spawnDeathParticles(int count) {
        float cx = lastCenterX;
        float cy = lastCenterY + lastSize * 0.45f;
        float halfW = lastSize * 0.95f;
        float halfH = lastSize * 0.95f;

        float pxPerBlock = lastSize / 1.8f;
        float velScale = 0.05f * pxPerBlock;
        float gaussScale = 0.02f * pxPerBlock;

        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx + (rng.nextFloat() * 2f - 1f) * halfW;
            p.y = cy + (rng.nextFloat() * 2f - 1f) * halfH;
            p.vx = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.vy = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.gravity = -0.04f * 0.1f * pxPerBlock;
            p.drag = 0.9f;
            p.maxLife = 24 + rng.nextInt(16);
            p.life = 4 + rng.nextInt(p.maxLife - 3);
            p.size = 14 + rng.nextInt(30);
            particles.add(p);
        }
    }

    private void tickParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.life--;
            if (p.life <= 0) it.remove();
        }
    }

    //? if >=26.1 {
    private void renderParticles(GuiGraphicsExtractor gge, int x0, int y0, int x1, int y1) {
    //?} else {
    /*private void renderParticles(GuiGraphics gge, int x0, int y0, int x1, int y1) {
    *///?}
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            float frac = p.life / (float) p.maxLife;
            int px = (int) p.x - p.size / 2;
            int py = (int) p.y - p.size / 2;
            if (px + p.size < x0 || px >= x1 || py + p.size < y0 || py >= y1) continue;
            int frame = (int) (frac * POOF_FRAMES.length);
            if (frame < 0) frame = 0;
            if (frame >= POOF_FRAMES.length) frame = POOF_FRAMES.length - 1;
            int color = 0xFFFFFFFF;
            gge.blit(RenderPipelines.GUI_TEXTURED, POOF_FRAMES[frame], px, py,
                    0f, 0f, p.size, p.size, 8, 8, 8, 8, color);
        }
    }

    private Vec3 damageDirFromCursor() {
        float sdx = lastMouseX - lastCenterX;
        float sdy = lastMouseY - lastCenterY;
        float mag = (float) Math.sqrt(sdx * sdx + sdy * sdy);
        if (mag < 1e-3f) return new Vec3(0.1, 0, 0);
        double wx = sdx / mag * 0.2;
        double wz = -sdy / mag * 0.2;
        return new Vec3(wx, 0, wz);
    }

    private float currentPartialTick() {
        long delta = System.currentTimeMillis() - lastTickMs;
        float p = delta / (float) TICK_INTERVAL_MS;
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }

    //? if >=26.1 {
    private void renderEntity(GuiGraphicsExtractor gge, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY) {
    //?} else {
    /*private void renderEntity(GuiGraphics gge, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY) {
    *///?}
        Minecraft mc = Minecraft.getInstance();
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(fake);
        EntityRenderState renderState = renderer.createRenderState(fake, currentPartialTick());
        //? if >=1.21.9 {
        renderState.outlineColor = 0;
        renderState.shadowPieces.clear();
        renderState.shadowRadius = 0f;
        //?}

        if (renderState instanceof LivingEntityRenderState lrs) {
            lrs.bodyRot = 180.0F + xAngle * 20.0F;
            lrs.yRot = xAngle * 20.0F;
            if (lrs.pose != Pose.FALL_FLYING) lrs.xRot = -yAngle * 20.0F;
            else lrs.xRot = 0.0F;
            lrs.boundingBoxWidth /= lrs.scale;
            lrs.boundingBoxHeight /= lrs.scale;
            lrs.scale = 1.0F;
        }
        //? if >=1.21.9 {
        if (renderState instanceof AvatarRenderState ars) {
            ars.showHat = true;
            ars.showJacket = true;
            ars.showLeftSleeve = true;
            ars.showRightSleeve = true;
            ars.showLeftPants = true;
            ars.showRightPants = true;
            ars.showCape = true;
        }
        //?} else {
        /*if (renderState instanceof PlayerRenderState ars) {
            ars.showHat = true;
            ars.showJacket = true;
            ars.showLeftSleeve = true;
            ars.showRightSleeve = true;
            ars.showLeftPants = true;
            ars.showRightPants = true;
            ars.showCape = true;
        }
        *///?}

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        int marginX = size;
        int marginY = size;
        //? if >=26.1 {
        gge.entity(renderState, (float) size, translation, rotation, xRotation,
                x0 - marginX, y0 - marginY, x1 + marginX, y1 + marginY);
        //?} else {
        /*gge.submitEntityRenderState(renderState, (float) size, translation, rotation, xRotation,
                x0 - marginX, y0 - marginY, x1 + marginX, y1 + marginY);
        *///?}
    }

    //? if >=1.21.11 {
    private static final Identifier SHADOW_TEX = Identifier.withDefaultNamespace("textures/misc/shadow.png");
    //?} else {
    /*private static final ResourceLocation SHADOW_TEX = ResourceLocation.withDefaultNamespace("textures/misc/shadow.png");
    *///?}

    //? if >=26.1 {
    private void drawShadow(GuiGraphicsExtractor gge, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
    //?} else {
    /*private void drawShadow(GuiGraphics gge, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
    *///?}
        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float feetY = centerY + size * 0.95f + 1.0f;
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        float tiltRad = yAngle * 20.0F * (float) (Math.PI / 180.0);
        float verticalScale = Math.abs((float) Math.sin(tiltRad)) * 1.3f;

        int shadowSize = (int) (size * 1.05f);
        int half = shadowSize / 2;

        var pose = gge.pose();
        pose.pushMatrix();
        pose.translate(centerX, feetY);
        pose.scale(1.0f, verticalScale);
        gge.blit(RenderPipelines.GUI_TEXTURED, SHADOW_TEX,
                -half, -half, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize,
                0x55000000);
        pose.popMatrix();
    }

    //? if >=26.1 {
    private void drawUnavailable(GuiGraphicsExtractor gge, int x0, int y0, int x1, int y1) {
    //?} else {
    /*private void drawUnavailable(GuiGraphics gge, int x0, int y0, int x1, int y1) {
    *///?}
        gge.fill(x0, y0, x1, y1, 0x80000000);
        Minecraft mc = Minecraft.getInstance();
        String msg = "Preview available in-world";
        int tx = (x0 + x1) / 2 - mc.font.width(msg) / 2;
        int ty = (y0 + y1) / 2 - 4;
        //? if >=26.1 {
        gge.text(mc.font, msg, tx, ty, 0xFFAAAAAA, true);
        //?} else {
        /*gge.drawString(mc.font, msg, tx, ty, 0xFFAAAAAA, true);
        *///?}
    }

    public boolean mouseClicked(double mx, double my) {
        if (fake == null || lastSize <= 0 || fake.deathTime > 0) return false;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mx < lastCenterX - halfW || mx > lastCenterX + halfW
            || my < lastCenterY - halfH || my > lastCenterY + halfH) return false;
        lastMouseX = (float) mx;
        lastMouseY = (float) my;
        fake.setDeltaMovement(damageDirFromCursor());
        hit();
        return true;
    }

    public void dispose() {
        fake = null;
        lastLevel = null;
        particles.clear();
        accumulatedDamage = 0;
    }
}
//?} else if neoforge {
/*package tecna.oldwalkinganimation.config;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PreviewPanel {
    public enum Mode {
        IDLE("Idle"), WALK("Walk"), RUN("Run"), SNEAK("Sneak");
        public final String label;
        Mode(String l) { this.label = l; }
    }

    private static final long TICK_INTERVAL_MS = 50L;
    private static final int DAMAGE_CYCLE_TICKS = 25;
    private static final int DAMAGE_HURT_TIME = 10;
    private static final int HIT_DAMAGE = 1;
    private static final int MAX_HEALTH = 20;
    private static final int DEATH_ANIM_TICKS = 20;
    private static final int RESPAWN_DELAY_TICKS = 40;

    private Mode mode = Mode.WALK;
    private RemotePlayer fake;
    private ClientLevel lastLevel;
    private long lastTickMs = 0L;
    private int damageCycleTick = 0;
    private int accumulatedDamage = 0;
    private float lastMouseX = 0f;
    private float lastMouseY = 0f;
    private float lastCenterX = 0f;
    private float lastCenterY = 0f;
    private int lastSize = 0;
    private double currentDx = 0.0;
    private float currentAnimSpeed = 0.0f;
    private boolean owa$cloakInited = false;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    // ResourceLocation static factories were added in 1.21; 1.20.x mojmap only exposes the public ctor.
    //? if >=1.21 {
    private static ResourceLocation rl(String ns, String path) { return ResourceLocation.fromNamespaceAndPath(ns, path); }
    private static ResourceLocation rlVanilla(String path) { return ResourceLocation.withDefaultNamespace(path); }
    //?} else {
    /^private static ResourceLocation rl(String ns, String path) { return new ResourceLocation(ns, path); }
    private static ResourceLocation rlVanilla(String path) { return new ResourceLocation(path); }
    ^///?}

    private static final ResourceLocation[] POOF_FRAMES = {
        rlVanilla("textures/particle/generic_0.png"),
        rlVanilla("textures/particle/generic_1.png"),
        rlVanilla("textures/particle/generic_2.png"),
        rlVanilla("textures/particle/generic_3.png"),
        rlVanilla("textures/particle/generic_4.png"),
        rlVanilla("textures/particle/generic_5.png"),
        rlVanilla("textures/particle/generic_6.png"),
        rlVanilla("textures/particle/generic_7.png"),
    };

    private static class Particle {
        float x, y, vx, vy;
        float gravity, drag;
        int life, maxLife, size;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode m) {
        if (mode == m) return;
        mode = m;
        damageCycleTick = 0;
        if (fake != null && fake.deathTime == 0) {
            fake.hurtTime = 0;
            fake.setDeltaMovement(Vec3.ZERO);
        }
    }

    public boolean available() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.player != null;
    }

    public void render(GuiGraphics gg, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            drawUnavailable(gg, x0, y0, x1, y1);
            return;
        }

        if (fake == null || lastLevel != mc.level) {
            GameProfile profile = mc.player.getGameProfile();
            fake = new RemotePlayer(mc.level, profile);
            tecna.oldwalkinganimation.OwaStateHolder.markPreview(fake);
            // Copy the local player's DATA_PLAYER_MODE_CUSTOMISATION byte (jacket/hat/sleeve/pants
            // overlay flags) so the preview shows the same outer skin layers the user sees on
            // themselves. RemotePlayer defaults to 0 (all hidden) which makes the overlay disappear.
            // The accessor field is protected so we reflect it; getDeclaredField via the runtime
            // class handles SRG/mojmap mapping differences across NF versions.
            try {
                java.lang.reflect.Field accessorField = null;
                for (Class<?> c = net.minecraft.world.entity.player.Player.class; c != null && accessorField == null; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType() == net.minecraft.network.syncher.EntityDataAccessor.class
                                && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                && f.getName().toUpperCase().contains("CUSTOMIS")) {
                            accessorField = f;
                            break;
                        }
                    }
                }
                if (accessorField != null) {
                    accessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    net.minecraft.network.syncher.EntityDataAccessor<Byte> accessor =
                            (net.minecraft.network.syncher.EntityDataAccessor<Byte>) accessorField.get(null);
                    fake.getEntityData().set(accessor, mc.player.getEntityData().get(accessor));
                }
            } catch (Throwable ignored) {}
            lastLevel = mc.level;
            lastTickMs = System.currentTimeMillis();
            damageCycleTick = 0;
            double rx = mc.player.getX();
            double ry = mc.player.getY();
            double rz = mc.player.getZ();
            fake.setPos(rx, ry, rz);
            fake.xo = rx; fake.yo = ry; fake.zo = rz;
            fake.xOld = rx; fake.yOld = ry; fake.zOld = rz;
            fake.setYRot(0f);
            fake.setYBodyRot(0f);
            fake.setYHeadRot(0f);
            fake.setXRot(0f);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastCenterX = (x0 + x1) / 2.0f;
        lastCenterY = (y0 + y1) / 2.0f;

        tickFake();

        int rectW = x1 - x0;
        int rectH = y1 - y0;
        int sizeByH = (int) (rectH / 2.8f);
        int sizeByW = (int) (rectW / 1.7f);
        int size = Math.max(24, Math.min(sizeByH, sizeByW));
        lastSize = size;
        if (!isGone()) {
            float centerY = (y0 + y1) / 2.0f;
            float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);
            if (yAngle > 0) {
                renderEntity(gg, x0, y0, x1, y1, size, mouseX, mouseY);
                drawShadow(gg, x0, y0, x1, y1, size, mouseX, mouseY);
            } else {
                drawShadow(gg, x0, y0, x1, y1, size, mouseX, mouseY);
                renderEntity(gg, x0, y0, x1, y1, size, mouseX, mouseY);
            }
        }
        renderParticles(gg, x0, y0, x1, y1);
        owa$checkHoverHint(mouseX, mouseY);
    }

    private void owa$checkHoverHint(float mouseX, float mouseY) {
        if (lastSize <= 0) return;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mouseX >= lastCenterX - halfW && mouseX < lastCenterX + halfW
            && mouseY >= lastCenterY - halfH && mouseY < lastCenterY + halfH) {
            KillStats.tryShowHoverHint();
        }
    }

    private boolean isGone() {
        return fake != null && fake.deathTime >= DEATH_ANIM_TICKS;
    }

    private double dxForMode() {
        switch (mode) {
            case WALK:   return 0.21;
            case RUN:    return 0.289;
            case SNEAK:  return 0.065;
            default:     return 0.0;
        }
    }

    private float targetSpeedForMode() {
        switch (mode) {
            case RUN:   return 1.0f;
            case WALK:  return 0.7f;
            case SNEAK: return 0.25f;
            default:    return 0f;
        }
    }

    private void tickFake() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickMs;
        if (elapsed < TICK_INTERVAL_MS) return;
        boolean dying = fake.deathTime > 0 && fake.deathTime < DEATH_ANIM_TICKS;
        long maxTicks = dying ? 1L : 5L;
        int ticks = (int) Math.min(maxTicks, elapsed / TICK_INTERVAL_MS);
        lastTickMs += (long) ticks * TICK_INTERVAL_MS;
        for (int i = 0; i < ticks; i++) doTick();
    }

    private void doTick() {
        tickParticles();
        if (fake.deathTime > 0) {
            handleDeathTick();
            return;
        }

        // CrossbowPosing.swingArm in vanilla BipedEntityModel.setAngles uses entity.tickCount
        // (mojmap) for the idle arm sway phase. Preview entity isn't world-ticked, so advance it.
        fake.tickCount++;

        double targetDx = dxForMode();
        float targetAnim = targetSpeedForMode();
        double dxRate = (Math.abs(targetDx) > Math.abs(currentDx)) ? 0.20 : 0.40;
        float animRate = (targetAnim > currentAnimSpeed) ? 0.20f : 0.40f;
        currentDx += (targetDx - currentDx) * dxRate;
        currentAnimSpeed += (targetAnim - currentAnimSpeed) * animRate;

        boolean sneak = mode == Mode.SNEAK;
        fake.setShiftKeyDown(sneak);
        fake.setPose(sneak ? Pose.CROUCHING : Pose.STANDING);

        double curX = fake.getX();
        double curY = fake.getY();
        double curZ = fake.getZ();

        // Move along body-forward (mirrors xAngle in renderEntity) so cape lag rotates with the
        // body when mouse turns the preview, instead of trailing world-space -Z.
        float xAngleTick = (float) Math.atan((lastCenterX - lastMouseX) / 40.0F);
        float facing = 180.0F + xAngleTick * 20.0F;
        float yawRad = (float) Math.toRadians(facing);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        fake.xo = curX; fake.yo = curY; fake.zo = curZ;
        fake.xOld = curX; fake.yOld = curY; fake.zOld = curZ;
        double newX = curX + currentDx * dirX;
        double newY = curY;
        double newZ = curZ + currentDx * dirZ;
        fake.setPos(newX, newY, newZ);

        if (!owa$cloakInited) {
            fake.xCloak = curX; fake.yCloak = curY; fake.zCloak = curZ;
            fake.xCloakO = curX; fake.yCloakO = curY; fake.zCloakO = curZ;
            owa$cloakInited = true;
        }
        fake.xCloakO = fake.xCloak;
        fake.yCloakO = fake.yCloak;
        fake.zCloakO = fake.zCloak;
        double cdx = newX - fake.xCloak;
        double cdy = newY - fake.yCloak;
        double cdz = newZ - fake.zCloak;
        if (cdx > 10 || cdx < -10) fake.xCloak = newX;
        if (cdz > 10 || cdz < -10) fake.zCloak = newZ;
        if (cdy > 10 || cdy < -10) fake.yCloak = newY;
        fake.xCloak += cdx * 0.25;
        fake.yCloak += cdy * 0.25;
        fake.zCloak += cdz * 0.25;

        fake.yRotO = fake.getYRot();
        fake.xRotO = fake.getXRot();
        fake.yBodyRotO = fake.yBodyRot;
        fake.yHeadRotO = fake.yHeadRot;
        fake.setYRot(facing);
        fake.setXRot(0f);
        fake.setYBodyRot(facing);
        fake.setYHeadRot(facing);

        fake.setOnGround(true);
        //? if >=1.21.2 {
        fake.walkAnimation.update(currentAnimSpeed, 0.4f, 1.0f);
        //?} else
        /^fake.walkAnimation.update(currentAnimSpeed, 0.4f);^/

        if (fake.hurtTime > 0) {
            fake.setDeltaMovement(damageDirFromCursor());
        } else {
            fake.setDeltaMovement(new Vec3(currentDx * dirX, 0, currentDx * dirZ));
            damageCycleTick = 0;
        }
        if (fake.hurtTime > 0) fake.hurtTime--;
    }

    private void handleDeathTick() {
        fake.deathTime++;
        if (fake.hurtTime > 0) fake.hurtTime--;
        fake.setDeltaMovement(Vec3.ZERO);
        // Sync xOld/yOld/zOld with current pos so LivingEntityRendererMixin's
        // var3 = sqrt((x-xOld)^2 + (z-zOld)^2) reads zero while dead. Otherwise entityRun keeps
        // the stale walking-tick delta and the limb anim continues swinging when
        // speedLimbAngle is off.
        fake.xOld = fake.getX();
        fake.yOld = fake.getY();
        fake.zOld = fake.getZ();
        //? if >=26.1 {
        // Keep ticking the cape state so its lag decays toward the (stationary) entity. Without
        // this state.x and state.lastX freeze at the last live-tick values and updateCape's
        // interpolation oscillates between them — visible as a vibrating cape during death.
        net.minecraft.client.entity.ClientAvatarState clientState = fake.avatarState();
        clientState.tick(new net.minecraft.world.phys.Vec3(fake.getX(), fake.getY(), fake.getZ()),
                         net.minecraft.world.phys.Vec3.ZERO);
        clientState.updateBob(0f);
        //?}
        //? if >=1.21.2 {
        fake.walkAnimation.update(0.0f, 0.6f, 1.0f);
        //?} else
        /^fake.walkAnimation.update(0.0f, 0.6f);^/
        if (fake.deathTime == DEATH_ANIM_TICKS) {
            spawnDeathParticles(20);
        }
        if (fake.deathTime >= DEATH_ANIM_TICKS + RESPAWN_DELAY_TICKS) respawn();
    }

    private void hit() {
        if (fake == null || fake.deathTime > 0) return;
        accumulatedDamage += HIT_DAMAGE;
        if (accumulatedDamage >= MAX_HEALTH) {
            triggerDeath();
        } else {
            fake.hurtTime = DAMAGE_HURT_TIME;
            fake.hurtDuration = DAMAGE_HURT_TIME;
            SharedValueUtil.invalidateDamageDir(fake);
            playSound(SoundEvents.PLAYER_HURT);
        }
    }

    private void triggerDeath() {
        fake.setHealth(0);
        fake.deathTime = 1;
        fake.hurtTime = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
        playSound(SoundEvents.PLAYER_DEATH);
        KillStats.onKill();
    }

    private void respawn() {
        fake.deathTime = 0;
        fake.hurtTime = 0;
        fake.setHealth(fake.getMaxHealth());
        accumulatedDamage = 0;
        damageCycleTick = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
    }

    private void playSound(SoundEvent sound) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0f, 1.0f));
    }

    private void spawnDeathParticles(int count) {
        float cx = lastCenterX;
        float cy = lastCenterY + lastSize * 0.45f;
        float halfW = lastSize * 0.95f;
        float halfH = lastSize * 0.95f;
        float pxPerBlock = lastSize / 1.8f;
        float velScale = 0.05f * pxPerBlock;
        float gaussScale = 0.02f * pxPerBlock;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx + (rng.nextFloat() * 2f - 1f) * halfW;
            p.y = cy + (rng.nextFloat() * 2f - 1f) * halfH;
            p.vx = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.vy = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.gravity = -0.04f * 0.1f * pxPerBlock;
            p.drag = 0.9f;
            p.maxLife = 24 + rng.nextInt(16);
            p.life = 4 + rng.nextInt(p.maxLife - 3);
            p.size = 14 + rng.nextInt(30);
            particles.add(p);
        }
    }

    private void tickParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.life--;
            if (p.life <= 0) it.remove();
        }
    }

    private void renderParticles(GuiGraphics gg, int x0, int y0, int x1, int y1) {
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            int px = (int) p.x - p.size / 2;
            int py = (int) p.y - p.size / 2;
            if (px + p.size < x0 || px >= x1 || py + p.size < y0 || py >= y1) continue;
            float frac = p.life / (float) p.maxLife;
            int frame = (int) (frac * POOF_FRAMES.length);
            if (frame < 0) frame = 0;
            if (frame >= POOF_FRAMES.length) frame = POOF_FRAMES.length - 1;
            //? if >=1.21.2 {
            gg.blit(RenderType::guiTextured, POOF_FRAMES[frame],
                    px, py, 0f, 0f, p.size, p.size, 8, 8, 8, 8, 0xFFFFFFFF);
            //?} else
            /^gg.blit(POOF_FRAMES[frame], px, py, p.size, p.size, 0f, 0f, 8, 8, 8, 8);^/
        }
    }

    private float currentPartialTick() {
        long delta = System.currentTimeMillis() - lastTickMs;
        float p = delta / (float) TICK_INTERVAL_MS;
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }

    private void renderEntity(GuiGraphics gg, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        Minecraft mc = Minecraft.getInstance();
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);

        float savedBodyYaw = fake.yBodyRot;
        float savedYaw = fake.getYRot();
        float savedPitch = fake.getXRot();
        float savedHeadYaw = fake.yHeadRot;
        float savedLastBodyYaw = fake.yBodyRotO;
        float savedLastYaw = fake.yRotO;
        float savedLastPitch = fake.xRotO;
        float savedLastHeadYaw = fake.yHeadRotO;

        fake.yBodyRot = 180.0F + xAngle * 20.0F;
        fake.setYRot(180.0F + xAngle * 40.0F);
        fake.setXRot(-yAngle * 20.0F);
        fake.yHeadRot = fake.getYRot();
        fake.yHeadRotO = fake.getYRot();
        fake.yBodyRotO = fake.yBodyRot;
        fake.yRotO = fake.getYRot();
        fake.xRotO = fake.getXRot();

        Vector3f translation = new Vector3f(0.0F, fake.getBbHeight() / 2.0F + 0.0625F, 0.0F);

        PoseStack matrices = gg.pose();
        matrices.pushPose();
        matrices.translate(centerX, centerY, 1050.0);
        // mulPose(Matrix4f) scales position only, leaving the normal matrix alone. matrices.scale
        // would also flip normals because of the negative-Z mirror, which inverts the inventory
        // lighting and hides the outer skin layer (jacket/hat/sleeve overlays). The fabric branch
        // calls multiplyPositionMatrix(new Matrix4f().scaling(...)) for the same reason.
        //? if >=1.20.5 {
        matrices.mulPose(new org.joml.Matrix4f().scaling((float) size, (float) size, (float) -size));
        //?} else
        /^matrices.mulPoseMatrix(new org.joml.Matrix4f().scaling((float) size, (float) size, (float) -size));^/
        matrices.translate(translation.x, translation.y, translation.z);
        matrices.mulPose(rotation);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Quaternionf cameraOrientation = new Quaternionf(xRotation).conjugate();
        dispatcher.overrideCameraOrientation(cameraOrientation);
        dispatcher.setRenderShadow(false);
        float partialTick = currentPartialTick();
        //? if >=1.21.2 {
        gg.drawSpecial(buf -> dispatcher.render(fake, 0.0, 0.0, 0.0, partialTick, matrices, buf, 0xF000F0));
        //?} else {
        /^net.minecraft.client.renderer.MultiBufferSource.BufferSource immediate = gg.bufferSource();
        dispatcher.render(fake, 0.0, 0.0, 0.0, 0.0F, partialTick, matrices, immediate, 0xF000F0);
        immediate.endBatch();
        ^///?}
        dispatcher.setRenderShadow(true);
        matrices.popPose();
        Lighting.setupFor3DItems();

        fake.yBodyRot = savedBodyYaw;
        fake.setYRot(savedYaw);
        fake.setXRot(savedPitch);
        fake.yHeadRot = savedHeadYaw;
        fake.yBodyRotO = savedLastBodyYaw;
        fake.yRotO = savedLastYaw;
        fake.xRotO = savedLastPitch;
        fake.yHeadRotO = savedLastHeadYaw;
    }

    private static final ResourceLocation SHADOW_TEX = rlVanilla("textures/misc/shadow.png");

    private void drawShadow(GuiGraphics gg, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float feetY = centerY + size * 0.95f + 1.0f;
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        float tiltRad = yAngle * 20.0F * (float) (Math.PI / 180.0);
        float verticalScale = Math.abs((float) Math.sin(tiltRad)) * 1.3f;

        int shadowSize = (int) (size * 1.05f);
        int half = shadowSize / 2;

        PoseStack matrices = gg.pose();
        matrices.pushPose();
        matrices.translate(centerX, feetY, 1100.0);
        matrices.scale(1.0f, verticalScale, 1.0f);
        //? if >=1.21.2 {
        gg.blit(RenderType::guiTexturedOverlay, SHADOW_TEX,
                -half, -half, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize, 0x55000000);
        gg.flush();
        //?} else {
        /^com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(0f, 0f, 0f, 0.4f);
        gg.blit(SHADOW_TEX, -half, -half, shadowSize, shadowSize, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize);
        gg.flush();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        ^///?}
        matrices.popPose();
    }

    private void drawUnavailable(GuiGraphics gg, int x0, int y0, int x1, int y1) {
        gg.fill(x0, y0, x1, y1, 0x80000000);
        Minecraft mc = Minecraft.getInstance();
        String msg = "Preview available in-world";
        int tx = (x0 + x1) / 2 - mc.font.width(msg) / 2;
        int ty = (y0 + y1) / 2 - 4;
        gg.drawString(mc.font, msg, tx, ty, 0xFFAAAAAA, true);
    }

    public boolean mouseClicked(double mx, double my) {
        if (fake == null || lastSize <= 0 || fake.deathTime > 0) return false;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mx < lastCenterX - halfW || mx > lastCenterX + halfW
            || my < lastCenterY - halfH || my > lastCenterY + halfH) return false;
        lastMouseX = (float) mx;
        lastMouseY = (float) my;
        fake.setDeltaMovement(damageDirFromCursor());
        hit();
        return true;
    }

    private Vec3 damageDirFromCursor() {
        float sdx = lastMouseX - lastCenterX;
        float sdy = lastMouseY - lastCenterY;
        float mag = (float) Math.sqrt(sdx * sdx + sdy * sdy);
        if (mag < 1e-3f) return new Vec3(0.1, 0, 0.001);
        double wx = sdx / mag * 0.2;
        double wz = -sdy / mag * 0.2;
        if (Math.abs(wx) < 1.0e-5) wx = wx >= 0 ? 1.0e-5 : -1.0e-5;
        if (Math.abs(wz) < 1.0e-5) wz = wz >= 0 ? 1.0e-5 : -1.0e-5;
        return new Vec3(wx, 0, wz);
    }

    public void dispose() {
        fake = null;
        lastLevel = null;
        particles.clear();
        accumulatedDamage = 0;
    }
}
*///?} else if >=1.21.6 {
/*package tecna.oldwalkinganimation.config;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PreviewPanel {
    public enum Mode {
        IDLE("Idle"), WALK("Walk"), RUN("Run"), SNEAK("Sneak");
        public final String label;
        Mode(String l) { this.label = l; }
    }

    private static final long TICK_INTERVAL_MS = 50L;
    private static final int DAMAGE_CYCLE_TICKS = 25;
    private static final int DAMAGE_HURT_TIME = 10;
    private static final int HIT_DAMAGE = 1;
    private static final int MAX_HEALTH = 20;
    private static final int DEATH_ANIM_TICKS = 20;
    private static final int RESPAWN_DELAY_TICKS = 40;

    private Mode mode = Mode.WALK;
    private OtherClientPlayerEntity fake;
    private ClientWorld lastWorld;
    private long lastTickMs = 0L;
    private int damageCycleTick = 0;
    private int accumulatedDamage = 0;
    private float lastMouseX = 0f;
    private float lastMouseY = 0f;
    private float lastCenterX = 0f;
    private float lastCenterY = 0f;
    private int lastSize = 0;
    private double currentDx = 0.0;
    private float currentAnimSpeed = 0.0f;
    private boolean owa$cloakInited = false;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    private static final Identifier[] POOF_FRAMES = {
        Identifier.of("minecraft", "textures/particle/generic_0.png"),
        Identifier.of("minecraft", "textures/particle/generic_1.png"),
        Identifier.of("minecraft", "textures/particle/generic_2.png"),
        Identifier.of("minecraft", "textures/particle/generic_3.png"),
        Identifier.of("minecraft", "textures/particle/generic_4.png"),
        Identifier.of("minecraft", "textures/particle/generic_5.png"),
        Identifier.of("minecraft", "textures/particle/generic_6.png"),
        Identifier.of("minecraft", "textures/particle/generic_7.png"),
    };

    private static class Particle {
        float x, y, vx, vy;
        float gravity, drag;
        int life, maxLife, size;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode m) {
        if (mode == m) return;
        mode = m;
        damageCycleTick = 0;
        if (fake != null && fake.deathTime == 0) {
            fake.hurtTime = 0;
            fake.setVelocity(Vec3d.ZERO);
        }
    }

    public boolean available() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.world != null && mc.player != null;
    }

    public void render(DrawContext context, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            drawUnavailable(context, x0, y0, x1, y1);
            return;
        }

        if (fake == null || lastWorld != mc.world) {
            GameProfile profile = mc.player.getGameProfile();
            fake = new OtherClientPlayerEntity(mc.world, profile);
            tecna.oldwalkinganimation.OwaStateHolder.markPreview(fake);
            // Copy local player's model-customization byte (jacket/hat/sleeve/pants overlay flags)
            // so the preview shows outer skin layers. OtherClientPlayerEntity defaults to 0.
            // Yarn 1.21.6-1.21.8 calls it PLAYER_MODEL_PARTS; 1.21.9+ renamed to
            // PLAYER_MODE_CUSTOMIZATION_ID and moved the field onto PlayerLikeEntity.
            try {
                java.lang.reflect.Field accessorField = null;
                for (Class<?> c = net.minecraft.entity.player.PlayerEntity.class; c != null && accessorField == null; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType() == net.minecraft.entity.data.TrackedData.class
                                && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                && (f.getName().toUpperCase().contains("MODEL_PARTS")
                                    || f.getName().toUpperCase().contains("CUSTOMI"))) {
                            accessorField = f;
                            break;
                        }
                    }
                }
                if (accessorField != null) {
                    accessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    net.minecraft.entity.data.TrackedData<Byte> accessor =
                            (net.minecraft.entity.data.TrackedData<Byte>) accessorField.get(null);
                    fake.getDataTracker().set(accessor, mc.player.getDataTracker().get(accessor));
                }
            } catch (Throwable ignored) {}
            lastWorld = mc.world;
            lastTickMs = System.currentTimeMillis();
            damageCycleTick = 0;
            double rx = mc.player.getX();
            double ry = mc.player.getY();
            double rz = mc.player.getZ();
            fake.setPosition(rx, ry, rz);
            fake.lastX = rx; fake.lastY = ry; fake.lastZ = rz;
            fake.setYaw(0f);
            fake.setBodyYaw(0f);
            fake.setHeadYaw(0f);
            fake.setPitch(0f);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastCenterX = (x0 + x1) / 2.0f;
        lastCenterY = (y0 + y1) / 2.0f;

        tickFake();

        int rectW = x1 - x0;
        int rectH = y1 - y0;
        int sizeByH = (int) (rectH / 2.8f);
        int sizeByW = (int) (rectW / 1.7f);
        int size = Math.max(24, Math.min(sizeByH, sizeByW));
        lastSize = size;
        if (!isGone()) {
            float centerY = (y0 + y1) / 2.0f;
            float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);
            if (yAngle > 0) {
                owa$drawEntity(context, x0, y0, x1, y1, size, 0.0625f, mouseX, mouseY);
                owa$drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
            } else {
                owa$drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
                owa$drawEntity(context, x0, y0, x1, y1, size, 0.0625f, mouseX, mouseY);
            }
        }
        owa$renderParticles(context, x0, y0, x1, y1);
        owa$checkHoverHint(mouseX, mouseY);
    }

    private void owa$checkHoverHint(float mouseX, float mouseY) {
        if (lastSize <= 0) return;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mouseX >= lastCenterX - halfW && mouseX < lastCenterX + halfW
            && mouseY >= lastCenterY - halfH && mouseY < lastCenterY + halfH) {
            KillStats.tryShowHoverHint();
        }
    }

    private void owa$renderParticles(DrawContext context, int x0, int y0, int x1, int y1) {
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            float frac = p.life / (float) p.maxLife;
            int px = (int) p.x - p.size / 2;
            int py = (int) p.y - p.size / 2;
            if (px + p.size < x0 || px >= x1 || py + p.size < y0 || py >= y1) continue;
            int frame = (int) (frac * POOF_FRAMES.length);
            if (frame < 0) frame = 0;
            if (frame >= POOF_FRAMES.length) frame = POOF_FRAMES.length - 1;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, POOF_FRAMES[frame],
                    px, py, 0f, 0f, p.size, p.size, 8, 8, 8, 8, 0xFFFFFFFF);
        }
    }

    private void owa$tickParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.life--;
            if (p.life <= 0) it.remove();
        }
    }

    private void owa$spawnDeathParticles(int count) {
        float cx = lastCenterX;
        float cy = lastCenterY + lastSize * 0.45f;
        float halfW = lastSize * 0.95f;
        float halfH = lastSize * 0.95f;
        float pxPerBlock = lastSize / 1.8f;
        float velScale = 0.05f * pxPerBlock;
        float gaussScale = 0.02f * pxPerBlock;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx + (rng.nextFloat() * 2f - 1f) * halfW;
            p.y = cy + (rng.nextFloat() * 2f - 1f) * halfH;
            p.vx = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.vy = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.gravity = -0.04f * 0.1f * pxPerBlock;
            p.drag = 0.9f;
            p.maxLife = 24 + rng.nextInt(16);
            p.life = 4 + rng.nextInt(p.maxLife - 3);
            p.size = 14 + rng.nextInt(30);
            particles.add(p);
        }
    }

    private static final Identifier SHADOW_TEX = Identifier.of("minecraft", "textures/misc/shadow.png");

    private void owa$drawShadow(DrawContext context, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float feetY = centerY + size * 0.95f + 1.0f;
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        float tiltRad = yAngle * 20.0F * (float) (Math.PI / 180.0);
        float verticalScale = Math.abs((float) Math.sin(tiltRad)) * 1.3f;

        int shadowSize = (int) (size * 1.05f);
        int half = shadowSize / 2;

        Matrix3x2fStack pose = context.getMatrices();
        pose.pushMatrix();
        pose.translate(centerX, feetY);
        pose.scale(1.0f, verticalScale);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SHADOW_TEX,
                -half, -half, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize, 0x55000000);
        pose.popMatrix();
    }

    private float currentPartialTick() {
        long delta = System.currentTimeMillis() - lastTickMs;
        float p = delta / (float) TICK_INTERVAL_MS;
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }

    private void owa$drawEntity(DrawContext context, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf invRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(invRotation);

        EntityRenderer<? super OtherClientPlayerEntity, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(fake);
        if (renderer == null) return;

        // Vanilla updateCape projects the (lerpX-X, lerpZ-Z) lag through entity.bodyYaw and clamps
        // forward lean to [0, 150]. With our entity yawed to 0 but moving on -Z, the projection
        // would go negative and clamp to 0 — cape stays at idle. Force entity.bodyYaw to render-
        // time yaw (180°) before getAndUpdateRenderState so vanilla's math produces visible lean,
        // then restore so the next tick keeps its facing-0 invariant.
        float owa$savedBodyYaw = fake.bodyYaw;
        float owa$savedLastBodyYaw = fake.lastBodyYaw;
        float owa$savedYaw = fake.getYaw();
        float owa$renderYaw = 180.0F + xAngle * 20.0F;
        fake.bodyYaw = owa$renderYaw;
        fake.lastBodyYaw = owa$renderYaw;
        fake.setYaw(owa$renderYaw);

        EntityRenderState renderState = renderer.getAndUpdateRenderState(fake, currentPartialTick());

        fake.bodyYaw = owa$savedBodyYaw;
        fake.lastBodyYaw = owa$savedLastBodyYaw;
        fake.setYaw(owa$savedYaw);
        //? if >=1.21.9 {
        renderState.outlineColor = 0;
        renderState.shadowPieces.clear();
        renderState.shadowRadius = 0f;
        renderState.light = 15728880;
        //?}

        if (renderState instanceof LivingEntityRenderState lrs) {
            lrs.bodyYaw = 180.0F + xAngle * 20.0F;
            lrs.relativeHeadYaw = xAngle * 20.0F;
            if (lrs.pose != EntityPose.GLIDING) lrs.pitch = -yAngle * 20.0F;
            else lrs.pitch = 0.0F;
            lrs.width /= lrs.baseScale;
            lrs.height /= lrs.baseScale;
            lrs.baseScale = 1.0F;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.height / 2.0F + offsetY, 0.0F);
        context.addEntity(renderState, (float) size, translation, rotation, invRotation, x0, y0, x1, y1);
    }

    private boolean isGone() {
        return fake != null && fake.deathTime >= DEATH_ANIM_TICKS;
    }

    private double dxForMode() {
        switch (mode) {
            case WALK:   return 0.21;
            case RUN:    return 0.289;
            case SNEAK:  return 0.065;
            default:     return 0.0;
        }
    }

    private float targetSpeedForMode() {
        switch (mode) {
            case RUN:   return 1.0f;
            case WALK:  return 0.7f;
            case SNEAK: return 0.25f;
            default:    return 0f;
        }
    }

    private void tickFake() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickMs;
        if (elapsed < TICK_INTERVAL_MS) return;
        boolean dying = fake.deathTime > 0 && fake.deathTime < DEATH_ANIM_TICKS;
        long maxTicks = dying ? 1L : 5L;
        int ticks = (int) Math.min(maxTicks, elapsed / TICK_INTERVAL_MS);
        lastTickMs += (long) ticks * TICK_INTERVAL_MS;
        for (int i = 0; i < ticks; i++) doTick();
    }

    private void doTick() {
        owa$tickParticles();
        if (fake.deathTime > 0) {
            handleDeathTick();
            return;
        }

        // CrossbowPosing.swingArm in vanilla BipedEntityModel.setAngles uses entity.age (yarn) for
        // the idle arm sway phase. Preview entity isn't world-ticked, so advance it.
        fake.age++;

        double targetDx = dxForMode();
        float targetAnim = targetSpeedForMode();
        double dxRate = (Math.abs(targetDx) > Math.abs(currentDx)) ? 0.20 : 0.40;
        float animRate = (targetAnim > currentAnimSpeed) ? 0.20f : 0.40f;
        currentDx += (targetDx - currentDx) * dxRate;
        currentAnimSpeed += (targetAnim - currentAnimSpeed) * animRate;

        boolean sneak = mode == Mode.SNEAK;
        fake.setSneaking(sneak);
        fake.setPose(sneak ? EntityPose.CROUCHING : EntityPose.STANDING);

        double curX = fake.getX();
        double curY = fake.getY();
        double curZ = fake.getZ();

        // Move along the body's render-time forward direction (mirrors xAngle in owa$drawEntity)
        // so cape lag rotates with the body — when the mouse turns the preview, the cape stays
        // behind the rotated body instead of trailing world-space -Z.
        float xAngleTick = (float) Math.atan((lastCenterX - lastMouseX) / 40.0F);
        float facing = 180.0F + xAngleTick * 20.0F;
        float yawRad = (float) Math.toRadians(facing);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        fake.lastX = curX; fake.lastY = curY; fake.lastZ = curZ;
        double newX = curX + currentDx * dirX;
        double newY = curY;
        double newZ = curZ + currentDx * dirZ;
        fake.setPosition(newX, newY, newZ);

        //? if <1.21.9 {
        // 1.21.6-1.21.8 yarn still tracks cloak position on the entity itself.
        if (!owa$cloakInited) {
            fake.capeX = curX; fake.capeY = curY; fake.capeZ = curZ;
            fake.lastCapeX = curX; fake.lastCapeY = curY; fake.lastCapeZ = curZ;
            owa$cloakInited = true;
        }
        fake.lastCapeX = fake.capeX;
        fake.lastCapeY = fake.capeY;
        fake.lastCapeZ = fake.capeZ;
        double cdx = newX - fake.capeX;
        double cdy = newY - fake.capeY;
        double cdz = newZ - fake.capeZ;
        if (cdx > 10 || cdx < -10) fake.capeX = newX;
        if (cdz > 10 || cdz < -10) fake.capeZ = newZ;
        if (cdy > 10 || cdy < -10) fake.capeY = newY;
        fake.capeX += cdx * 0.25;
        fake.capeY += cdy * 0.25;
        fake.capeZ += cdz * 0.25;
        //?} else {
        /^net.minecraft.client.network.ClientPlayerLikeState clientState = fake.getState();
        clientState.tick(new net.minecraft.util.math.Vec3d(newX, newY, newZ),
                         new net.minecraft.util.math.Vec3d(currentDx * dirX, 0, currentDx * dirZ));
        // Vanilla AbstractClientPlayerEntity.tickPlayerMovement passes min(0.1, |horizontalVel|);
        // ClientPlayerEntity.move passes hypot(dx,dz)*0.6 to addDistanceMoved. Without these the
        // bobbing-lift term in updateCape stays silent. The 0.6 sets the swing phase rate.
        clientState.tickMovement((float) Math.min(0.1, Math.abs(currentDx)));
        clientState.addDistanceMoved((float) (Math.abs(currentDx) * 0.6));
        ^///?}

        fake.lastYaw     = fake.getYaw();
        fake.lastPitch   = fake.getPitch();
        fake.lastBodyYaw = fake.bodyYaw;
        fake.lastHeadYaw = fake.headYaw;
        fake.setYaw(facing);
        fake.setPitch(0f);
        fake.setBodyYaw(facing);
        fake.setHeadYaw(facing);

        fake.setOnGround(true);
        fake.limbAnimator.updateLimbs(currentAnimSpeed, 0.4f, 1.0f);

        if (fake.hurtTime > 0) {
            fake.setVelocity(damageDirFromCursor());
        } else {
            fake.setVelocity(new Vec3d(0, 0, -currentDx));
            damageCycleTick = 0;
        }
        if (fake.hurtTime > 0) fake.hurtTime--;
    }

    private void handleDeathTick() {
        fake.deathTime++;
        if (fake.hurtTime > 0) fake.hurtTime--;
        fake.setVelocity(Vec3d.ZERO);
        // Sync lastX/Y/Z with current pos so LivingEntityRendererMixin's
        // var3 = sqrt((x-lastX)^2 + (z-lastZ)^2) reads zero while dead. Otherwise entityRun keeps
        // the stale walking-tick delta and the limb anim continues swinging when
        // speedLimbAngle is off.
        fake.lastX = fake.getX();
        fake.lastY = fake.getY();
        fake.lastZ = fake.getZ();
        // Keep ticking the cape state so its lag decays toward the (now stationary) entity.
        // Without this state.x and state.lastX freeze at the last live-tick values (~0.2m apart),
        // and updateCape's lerpZ(td) oscillates between them every render frame — visible as a
        // vibrating cape during the death animation.
        //? if >=1.21.9 {
        net.minecraft.client.network.ClientPlayerLikeState clientState = fake.getState();
        clientState.tick(new net.minecraft.util.math.Vec3d(fake.getX(), fake.getY(), fake.getZ()),
                         net.minecraft.util.math.Vec3d.ZERO);
        clientState.tickMovement(0f);
        //?}
        fake.limbAnimator.updateLimbs(0.0f, 0.6f, 1.0f);
        if (fake.deathTime == DEATH_ANIM_TICKS) {
            owa$spawnDeathParticles(20);
        }
        if (fake.deathTime >= DEATH_ANIM_TICKS + RESPAWN_DELAY_TICKS) respawn();
    }

    private void hit() {
        if (fake == null || fake.deathTime > 0) return;
        accumulatedDamage += HIT_DAMAGE;
        if (accumulatedDamage >= MAX_HEALTH) {
            triggerDeath();
        } else {
            fake.hurtTime = DAMAGE_HURT_TIME;
            fake.maxHurtTime = DAMAGE_HURT_TIME;
            SharedValueUtil.invalidateDamageDir(fake);
            playSound(SoundEvents.ENTITY_PLAYER_HURT);
        }
    }

    private void triggerDeath() {
        fake.setHealth(0);
        fake.deathTime = 1;
        fake.hurtTime = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
        playSound(SoundEvents.ENTITY_PLAYER_DEATH);
        KillStats.onKill();
    }

    private void playSound(SoundEvent sound) {
        MinecraftClient mc = MinecraftClient.getInstance();
        //? if >=1.21.11 {
        mc.getSoundManager().play(PositionedSoundInstance.ui(sound, 1.0f, 1.0f));
        //?} else
        /^mc.getSoundManager().play(PositionedSoundInstance.master(sound, 1.0f, 1.0f));^/
    }

    private void respawn() {
        fake.deathTime = 0;
        fake.hurtTime = 0;
        fake.setHealth(fake.getMaxHealth());
        accumulatedDamage = 0;
        damageCycleTick = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
    }

    private Vec3d damageDirFromCursor() {
        float sdx = lastMouseX - lastCenterX;
        float sdy = lastMouseY - lastCenterY;
        float mag = (float) Math.sqrt(sdx * sdx + sdy * sdy);
        if (mag < 1e-3f) return new Vec3d(0.1, 0, 0);
        double wx = sdx / mag * 0.2;
        double wz = -sdy / mag * 0.2;
        return new Vec3d(wx, 0, wz);
    }

    private void drawUnavailable(DrawContext context, int x0, int y0, int x1, int y1) {
        context.fill(x0, y0, x1, y1, 0x80000000);
        MinecraftClient mc = MinecraftClient.getInstance();
        String msg = "Preview available in-world";
        int tx = (x0 + x1) / 2 - mc.textRenderer.getWidth(msg) / 2;
        int ty = (y0 + y1) / 2 - 4;
        context.drawText(mc.textRenderer, msg, tx, ty, 0xFFAAAAAA, true);
    }

    public boolean mouseClicked(double mx, double my) {
        if (fake == null || lastSize <= 0 || fake.deathTime > 0) return false;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mx < lastCenterX - halfW || mx > lastCenterX + halfW
            || my < lastCenterY - halfH || my > lastCenterY + halfH) return false;
        lastMouseX = (float) mx;
        lastMouseY = (float) my;
        fake.setVelocity(damageDirFromCursor());
        hit();
        return true;
    }

    public void dispose() {
        fake = null;
        lastWorld = null;
        particles.clear();
        accumulatedDamage = 0;
    }
}
*///?} else if >=1.21.2 {
/*package tecna.oldwalkinganimation.config;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PreviewPanel {
    public enum Mode {
        IDLE("Idle"), WALK("Walk"), RUN("Run"), SNEAK("Sneak");
        public final String label;
        Mode(String l) { this.label = l; }
    }

    private static final long TICK_INTERVAL_MS = 50L;
    private static final int DAMAGE_CYCLE_TICKS = 25;
    private static final int DAMAGE_HURT_TIME = 10;
    private static final int HIT_DAMAGE = 1;
    private static final int MAX_HEALTH = 20;
    private static final int DEATH_ANIM_TICKS = 20;
    private static final int RESPAWN_DELAY_TICKS = 40;

    private Mode mode = Mode.WALK;
    private OtherClientPlayerEntity fake;
    private ClientWorld lastWorld;
    private long lastTickMs = 0L;
    private int damageCycleTick = 0;
    private int accumulatedDamage = 0;
    private float lastMouseX = 0f;
    private float lastMouseY = 0f;
    private float lastCenterX = 0f;
    private float lastCenterY = 0f;
    private int lastSize = 0;
    private double currentDx = 0.0;
    private float currentAnimSpeed = 0.0f;
    private boolean owa$cloakInited = false;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    private static final Identifier[] POOF_FRAMES = {
        Identifier.of("minecraft", "textures/particle/generic_0.png"),
        Identifier.of("minecraft", "textures/particle/generic_1.png"),
        Identifier.of("minecraft", "textures/particle/generic_2.png"),
        Identifier.of("minecraft", "textures/particle/generic_3.png"),
        Identifier.of("minecraft", "textures/particle/generic_4.png"),
        Identifier.of("minecraft", "textures/particle/generic_5.png"),
        Identifier.of("minecraft", "textures/particle/generic_6.png"),
        Identifier.of("minecraft", "textures/particle/generic_7.png"),
    };

    private static class Particle {
        float x, y, vx, vy;
        float gravity, drag;
        int life, maxLife, size;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode m) {
        if (mode == m) return;
        mode = m;
        damageCycleTick = 0;
        if (fake != null && fake.deathTime == 0) {
            fake.hurtTime = 0;
            fake.setVelocity(Vec3d.ZERO);
        }
    }

    public boolean available() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.world != null && mc.player != null;
    }

    public void render(DrawContext context, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            drawUnavailable(context, x0, y0, x1, y1);
            return;
        }

        if (fake == null || lastWorld != mc.world) {
            GameProfile profile = mc.player.getGameProfile();
            fake = new OtherClientPlayerEntity(mc.world, profile);
            tecna.oldwalkinganimation.OwaStateHolder.markPreview(fake);
            // Copy local player's model-customization byte (jacket/hat/sleeve/pants overlay flags)
            // so the preview shows outer skin layers. OtherClientPlayerEntity defaults to 0.
            // Yarn 1.21.6-1.21.8 calls it PLAYER_MODEL_PARTS; 1.21.9+ renamed to
            // PLAYER_MODE_CUSTOMIZATION_ID and moved the field onto PlayerLikeEntity.
            try {
                java.lang.reflect.Field accessorField = null;
                for (Class<?> c = net.minecraft.entity.player.PlayerEntity.class; c != null && accessorField == null; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType() == net.minecraft.entity.data.TrackedData.class
                                && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                && (f.getName().toUpperCase().contains("MODEL_PARTS")
                                    || f.getName().toUpperCase().contains("CUSTOMI"))) {
                            accessorField = f;
                            break;
                        }
                    }
                }
                if (accessorField != null) {
                    accessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    net.minecraft.entity.data.TrackedData<Byte> accessor =
                            (net.minecraft.entity.data.TrackedData<Byte>) accessorField.get(null);
                    fake.getDataTracker().set(accessor, mc.player.getDataTracker().get(accessor));
                }
            } catch (Throwable ignored) {}
            lastWorld = mc.world;
            lastTickMs = System.currentTimeMillis();
            damageCycleTick = 0;
            double rx = mc.player.getX();
            double ry = mc.player.getY();
            double rz = mc.player.getZ();
            fake.setPosition(rx, ry, rz);
            //? if >=1.21.5 {
            fake.lastX = rx; fake.lastY = ry; fake.lastZ = rz;
            //?} else {
            /^fake.prevX = rx; fake.prevY = ry; fake.prevZ = rz;
            ^///?}
            fake.lastRenderX = rx; fake.lastRenderY = ry; fake.lastRenderZ = rz;
            fake.setYaw(0f);
            fake.setBodyYaw(0f);
            fake.setHeadYaw(0f);
            fake.setPitch(0f);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastCenterX = (x0 + x1) / 2.0f;
        lastCenterY = (y0 + y1) / 2.0f;

        tickFake();

        int rectW = x1 - x0;
        int rectH = y1 - y0;
        int sizeByH = (int) (rectH / 2.8f);
        int sizeByW = (int) (rectW / 1.7f);
        int size = Math.max(24, Math.min(sizeByH, sizeByW));
        lastSize = size;
        if (!isGone()) {
            float centerY = (y0 + y1) / 2.0f;
            float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);
            if (yAngle > 0) {
                renderEntity(context, x0, y0, x1, y1, size, mouseX, mouseY);
                drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
            } else {
                drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
                renderEntity(context, x0, y0, x1, y1, size, mouseX, mouseY);
            }
        }
        renderParticles(context, x0, y0, x1, y1);
        owa$checkHoverHint(mouseX, mouseY);
    }

    private void owa$checkHoverHint(float mouseX, float mouseY) {
        if (lastSize <= 0) return;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mouseX >= lastCenterX - halfW && mouseX < lastCenterX + halfW
            && mouseY >= lastCenterY - halfH && mouseY < lastCenterY + halfH) {
            KillStats.tryShowHoverHint();
        }
    }

    private boolean isGone() {
        return fake != null && fake.deathTime >= DEATH_ANIM_TICKS;
    }

    private double dxForMode() {
        switch (mode) {
            case WALK:   return 0.21;
            case RUN:    return 0.289;
            case SNEAK:  return 0.065;
            default:     return 0.0;
        }
    }

    private float targetSpeedForMode() {
        switch (mode) {
            case RUN:   return 1.0f;
            case WALK:  return 0.7f;
            case SNEAK: return 0.25f;
            default:    return 0f;
        }
    }

    private void tickFake() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickMs;
        if (elapsed < TICK_INTERVAL_MS) return;
        boolean dying = fake.deathTime > 0 && fake.deathTime < DEATH_ANIM_TICKS;
        long maxTicks = dying ? 1L : 5L;
        int ticks = (int) Math.min(maxTicks, elapsed / TICK_INTERVAL_MS);
        lastTickMs += (long) ticks * TICK_INTERVAL_MS;
        for (int i = 0; i < ticks; i++) doTick();
    }

    private void doTick() {
        tickParticles();
        if (fake.deathTime > 0) {
            handleDeathTick();
            return;
        }

        // CrossbowPosing.swingArm in vanilla BipedEntityModel.setAngles uses entity.age (yarn) for
        // the idle arm sway phase. Preview entity isn't world-ticked, so advance it.
        fake.age++;

        double targetDx = dxForMode();
        float targetAnim = targetSpeedForMode();
        double dxRate = (Math.abs(targetDx) > Math.abs(currentDx)) ? 0.20 : 0.40;
        float animRate = (targetAnim > currentAnimSpeed) ? 0.20f : 0.40f;
        currentDx += (targetDx - currentDx) * dxRate;
        currentAnimSpeed += (targetAnim - currentAnimSpeed) * animRate;

        boolean sneak = mode == Mode.SNEAK;
        fake.setSneaking(sneak);
        fake.setPose(sneak ? EntityPose.CROUCHING : EntityPose.STANDING);

        double curX = fake.getX();
        double curY = fake.getY();
        double curZ = fake.getZ();

        // Move along body-forward (mirrors xAngle in renderEntity) so cape lag rotates with the
        // body when mouse turns the preview, instead of trailing world-space -Z.
        float xAngleTick = (float) Math.atan((lastCenterX - lastMouseX) / 40.0F);
        float facing = 180.0F + xAngleTick * 20.0F;
        float yawRad = (float) Math.toRadians(facing);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        //? if >=1.21.5 {
        fake.lastX = curX; fake.lastY = curY; fake.lastZ = curZ;
        //?} else {
        /^fake.prevX = curX; fake.prevY = curY; fake.prevZ = curZ;
        ^///?}
        fake.lastRenderX = curX; fake.lastRenderY = curY; fake.lastRenderZ = curZ;
        double newX = curX + currentDx * dirX;
        double newY = curY;
        double newZ = curZ + currentDx * dirZ;
        fake.setPosition(newX, newY, newZ);

        if (!owa$cloakInited) {
            fake.capeX = curX; fake.capeY = curY; fake.capeZ = curZ;
            //? if >=1.21.5 {
            fake.lastCapeX = curX; fake.lastCapeY = curY; fake.lastCapeZ = curZ;
            //?} else {
            /^fake.prevCapeX = curX; fake.prevCapeY = curY; fake.prevCapeZ = curZ;
            ^///?}
            owa$cloakInited = true;
        }
        //? if >=1.21.5 {
        fake.lastCapeX = fake.capeX;
        fake.lastCapeY = fake.capeY;
        fake.lastCapeZ = fake.capeZ;
        //?} else {
        /^fake.prevCapeX = fake.capeX;
        fake.prevCapeY = fake.capeY;
        fake.prevCapeZ = fake.capeZ;
        ^///?}
        double cdx = newX - fake.capeX;
        double cdy = newY - fake.capeY;
        double cdz = newZ - fake.capeZ;
        if (cdx > 10 || cdx < -10) fake.capeX = newX;
        if (cdz > 10 || cdz < -10) fake.capeZ = newZ;
        if (cdy > 10 || cdy < -10) fake.capeY = newY;
        fake.capeX += cdx * 0.25;
        fake.capeY += cdy * 0.25;
        fake.capeZ += cdz * 0.25;

        //? if >=1.21.5 {
        fake.lastYaw     = fake.getYaw();
        fake.lastPitch   = fake.getPitch();
        fake.lastBodyYaw = fake.bodyYaw;
        fake.lastHeadYaw = fake.headYaw;
        //?} else {
        /^fake.prevYaw     = fake.getYaw();
        fake.prevPitch   = fake.getPitch();
        fake.prevBodyYaw = fake.bodyYaw;
        fake.prevHeadYaw = fake.headYaw;
        ^///?}
        fake.setYaw(facing);
        fake.setPitch(0f);
        fake.setBodyYaw(facing);
        fake.setHeadYaw(facing);

        fake.setOnGround(true);
        fake.limbAnimator.updateLimbs(currentAnimSpeed, 0.4f, 1.0f);

        if (fake.hurtTime > 0) {
            fake.setVelocity(damageDirFromCursor());
        } else {
            fake.setVelocity(new Vec3d(currentDx * dirX, 0, currentDx * dirZ));
            damageCycleTick = 0;
        }
        if (fake.hurtTime > 0) fake.hurtTime--;
    }

    private void handleDeathTick() {
        fake.deathTime++;
        if (fake.hurtTime > 0) fake.hurtTime--;
        fake.setVelocity(Vec3d.ZERO);
        // Sync prev/last pos so LivingEntityRendererMixin's var3 reads zero while dead.
        // Otherwise entityRun keeps the stale walking-tick delta and the limb anim continues
        // swinging when speedLimbAngle is off.
        //? if >=1.21.5 {
        fake.lastX = fake.getX();
        fake.lastY = fake.getY();
        fake.lastZ = fake.getZ();
        //?} else {
        /^fake.prevX = fake.getX();
        fake.prevY = fake.getY();
        fake.prevZ = fake.getZ();
        ^///?}
        fake.limbAnimator.updateLimbs(0.0f, 0.6f, 1.0f);
        if (fake.deathTime == DEATH_ANIM_TICKS) {
            spawnDeathParticles(20);
        }
        if (fake.deathTime >= DEATH_ANIM_TICKS + RESPAWN_DELAY_TICKS) respawn();
    }

    private void hit() {
        if (fake == null || fake.deathTime > 0) return;
        accumulatedDamage += HIT_DAMAGE;
        if (accumulatedDamage >= MAX_HEALTH) {
            triggerDeath();
        } else {
            fake.hurtTime = DAMAGE_HURT_TIME;
            fake.maxHurtTime = DAMAGE_HURT_TIME;
            SharedValueUtil.invalidateDamageDir(fake);
            playSound(SoundEvents.ENTITY_PLAYER_HURT);
        }
    }

    private void triggerDeath() {
        fake.setHealth(0);
        fake.deathTime = 1;
        fake.hurtTime = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
        playSound(SoundEvents.ENTITY_PLAYER_DEATH);
        KillStats.onKill();
    }

    private void respawn() {
        fake.deathTime = 0;
        fake.hurtTime = 0;
        fake.setHealth(fake.getMaxHealth());
        accumulatedDamage = 0;
        damageCycleTick = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
    }

    private void playSound(SoundEvent sound) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().play(PositionedSoundInstance.master(sound, 1.0f, 1.0f));
    }

    private void spawnDeathParticles(int count) {
        float cx = lastCenterX;
        float cy = lastCenterY + lastSize * 0.45f;
        float halfW = lastSize * 0.95f;
        float halfH = lastSize * 0.95f;
        float pxPerBlock = lastSize / 1.8f;
        float velScale = 0.05f * pxPerBlock;
        float gaussScale = 0.02f * pxPerBlock;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx + (rng.nextFloat() * 2f - 1f) * halfW;
            p.y = cy + (rng.nextFloat() * 2f - 1f) * halfH;
            p.vx = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.vy = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.gravity = -0.04f * 0.1f * pxPerBlock;
            p.drag = 0.9f;
            p.maxLife = 24 + rng.nextInt(16);
            p.life = 4 + rng.nextInt(p.maxLife - 3);
            p.size = 14 + rng.nextInt(30);
            particles.add(p);
        }
    }

    private void tickParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.life--;
            if (p.life <= 0) it.remove();
        }
    }

    private void renderParticles(DrawContext context, int x0, int y0, int x1, int y1) {
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            int px = (int) p.x - p.size / 2;
            int py = (int) p.y - p.size / 2;
            if (px + p.size < x0 || px >= x1 || py + p.size < y0 || py >= y1) continue;
            float frac = p.life / (float) p.maxLife;
            int frame = (int) (frac * POOF_FRAMES.length);
            if (frame < 0) frame = 0;
            if (frame >= POOF_FRAMES.length) frame = POOF_FRAMES.length - 1;
            context.drawTexture(RenderLayer::getGuiTextured, POOF_FRAMES[frame],
                    px, py, 0f, 0f, p.size, p.size, 8, 8, 8, 8, 0xFFFFFFFF);
        }
    }

    private float currentPartialTick() {
        long delta = System.currentTimeMillis() - lastTickMs;
        float p = delta / (float) TICK_INTERVAL_MS;
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }

    private void renderEntity(DrawContext context, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);

        float savedBodyYaw = fake.bodyYaw;
        float savedYaw = fake.getYaw();
        float savedPitch = fake.getPitch();
        float savedHeadYaw = fake.headYaw;
        //? if >=1.21.5 {
        float savedLastBodyYaw = fake.lastBodyYaw;
        float savedLastYaw = fake.lastYaw;
        float savedLastPitch = fake.lastPitch;
        float savedLastHeadYaw = fake.lastHeadYaw;
        //?} else {
        /^float savedLastBodyYaw = fake.prevBodyYaw;
        float savedLastYaw = fake.prevYaw;
        float savedLastPitch = fake.prevPitch;
        float savedLastHeadYaw = fake.prevHeadYaw;
        ^///?}

        fake.bodyYaw = 180.0F + xAngle * 20.0F;
        fake.setYaw(180.0F + xAngle * 40.0F);
        fake.setPitch(-yAngle * 20.0F);
        fake.headYaw = fake.getYaw();
        //? if >=1.21.5 {
        fake.lastHeadYaw = fake.getYaw();
        fake.lastBodyYaw = fake.bodyYaw;
        fake.lastYaw = fake.getYaw();
        fake.lastPitch = fake.getPitch();
        //?} else {
        /^fake.prevHeadYaw = fake.getYaw();
        fake.prevBodyYaw = fake.bodyYaw;
        fake.prevYaw = fake.getYaw();
        fake.prevPitch = fake.getPitch();
        ^///?}

        Vector3f translation = new Vector3f(0.0F, fake.getHeight() / 2.0F + 0.0625F, 0.0F);

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 1050.0);
        // multiplyPositionMatrix scales position only, leaving the normal matrix alone. matrices.scale
        // would also flip normals because of the negative-Z mirror, which inverts the inventory
        // lighting and hides the outer skin layer (jacket/hat/sleeve overlays).
        matrices.multiplyPositionMatrix(new org.joml.Matrix4f().scaling((float) size, (float) size, (float) -size));
        matrices.translate(translation.x, translation.y, translation.z);
        matrices.multiply(rotation);
        DiffuseLighting.disableGuiDepthLighting();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Quaternionf cameraOrientation = new Quaternionf(xRotation).conjugate();
        dispatcher.setRotation(cameraOrientation);
        dispatcher.setRenderShadows(false);
        float partialTick = currentPartialTick();
        context.draw(vc -> dispatcher.render(fake, 0.0, 0.0, 0.0, partialTick, matrices, vc, 0xF000F0));
        dispatcher.setRenderShadows(true);
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();

        fake.bodyYaw = savedBodyYaw;
        fake.setYaw(savedYaw);
        fake.setPitch(savedPitch);
        fake.headYaw = savedHeadYaw;
        //? if >=1.21.5 {
        fake.lastBodyYaw = savedLastBodyYaw;
        fake.lastYaw = savedLastYaw;
        fake.lastPitch = savedLastPitch;
        fake.lastHeadYaw = savedLastHeadYaw;
        //?} else {
        /^fake.prevBodyYaw = savedLastBodyYaw;
        fake.prevYaw = savedLastYaw;
        fake.prevPitch = savedLastPitch;
        fake.prevHeadYaw = savedLastHeadYaw;
        ^///?}
    }

    private static final Identifier SHADOW_TEX = Identifier.of("minecraft", "textures/misc/shadow.png");

    private void drawShadow(DrawContext context, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float feetY = centerY + size * 0.95f + 1.0f;
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        float tiltRad = yAngle * 20.0F * (float) (Math.PI / 180.0);
        float verticalScale = Math.abs((float) Math.sin(tiltRad)) * 1.3f;

        int shadowSize = (int) (size * 1.05f);
        int half = shadowSize / 2;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, feetY, 1100.0);
        matrices.scale(1.0f, verticalScale, 1.0f);
        context.drawTexture(RenderLayer::getGuiTexturedOverlay, SHADOW_TEX,
                -half, -half, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize, 0x55000000);
        context.draw();
        matrices.pop();
    }

    private void drawUnavailable(DrawContext context, int x0, int y0, int x1, int y1) {
        context.fill(x0, y0, x1, y1, 0x80000000);
        MinecraftClient mc = MinecraftClient.getInstance();
        String msg = "Preview available in-world";
        int tx = (x0 + x1) / 2 - mc.textRenderer.getWidth(msg) / 2;
        int ty = (y0 + y1) / 2 - 4;
        context.drawText(mc.textRenderer, msg, tx, ty, 0xFFAAAAAA, true);
    }

    public boolean mouseClicked(double mx, double my) {
        if (fake == null || lastSize <= 0 || fake.deathTime > 0) return false;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mx < lastCenterX - halfW || mx > lastCenterX + halfW
            || my < lastCenterY - halfH || my > lastCenterY + halfH) return false;
        lastMouseX = (float) mx;
        lastMouseY = (float) my;
        fake.setVelocity(damageDirFromCursor());
        hit();
        return true;
    }

    private Vec3d damageDirFromCursor() {
        float sdx = lastMouseX - lastCenterX;
        float sdy = lastMouseY - lastCenterY;
        float mag = (float) Math.sqrt(sdx * sdx + sdy * sdy);
        if (mag < 1e-3f) return new Vec3d(0.1, 0, 0.001);
        double wx = sdx / mag * 0.2;
        double wz = -sdy / mag * 0.2;
        if (Math.abs(wx) < 1.0e-5) wx = wx >= 0 ? 1.0e-5 : -1.0e-5;
        if (Math.abs(wz) < 1.0e-5) wz = wz >= 0 ? 1.0e-5 : -1.0e-5;
        return new Vec3d(wx, 0, wz);
    }

    public void dispose() {
        fake = null;
        lastWorld = null;
        particles.clear();
        accumulatedDamage = 0;
    }
}
*///?} else {
/*package tecna.oldwalkinganimation.config;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tecna.oldwalkinganimation.SharedValueUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PreviewPanel {
    public enum Mode {
        IDLE("Idle"), WALK("Walk"), RUN("Run"), SNEAK("Sneak");
        public final String label;
        Mode(String l) { this.label = l; }
    }

    private static final long TICK_INTERVAL_MS = 50L;
    private static final int DAMAGE_CYCLE_TICKS = 25;
    private static final int DAMAGE_HURT_TIME = 10;
    private static final int HIT_DAMAGE = 1;
    private static final int MAX_HEALTH = 20;
    private static final int DEATH_ANIM_TICKS = 20;
    private static final int RESPAWN_DELAY_TICKS = 40;

    private Mode mode = Mode.WALK;
    private OtherClientPlayerEntity fake;
    private ClientWorld lastLevel;
    private long lastTickMs = 0L;
    private int damageCycleTick = 0;
    private int accumulatedDamage = 0;
    private float lastMouseX = 0f;
    private float lastMouseY = 0f;
    private float lastCenterX = 0f;
    private float lastCenterY = 0f;
    private int lastSize = 0;
    private double currentDx = 0.0;
    private float currentAnimSpeed = 0.0f;
    private boolean owa$cloakInited = false;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    private static final Identifier[] POOF_FRAMES = {
        Identifier.of("minecraft", "textures/particle/generic_0.png"),
        Identifier.of("minecraft", "textures/particle/generic_1.png"),
        Identifier.of("minecraft", "textures/particle/generic_2.png"),
        Identifier.of("minecraft", "textures/particle/generic_3.png"),
        Identifier.of("minecraft", "textures/particle/generic_4.png"),
        Identifier.of("minecraft", "textures/particle/generic_5.png"),
        Identifier.of("minecraft", "textures/particle/generic_6.png"),
        Identifier.of("minecraft", "textures/particle/generic_7.png"),
    };

    private static class Particle {
        float x, y, vx, vy;
        float gravity, drag;
        int life, maxLife, size;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode m) {
        if (mode == m) return;
        mode = m;
        damageCycleTick = 0;
        if (fake != null && fake.deathTime == 0) {
            fake.hurtTime = 0;
            fake.setVelocity(Vec3d.ZERO);
        }
    }

    public boolean available() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.world != null && mc.player != null;
    }

    public void render(DrawContext context, int x0, int y0, int x1, int y1, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            drawUnavailable(context, x0, y0, x1, y1);
            return;
        }

        if (fake == null || lastLevel != mc.world) {
            GameProfile profile = mc.player.getGameProfile();
            fake = new OtherClientPlayerEntity(mc.world, profile);
            tecna.oldwalkinganimation.OwaStateHolder.markPreview(fake);
            // Copy local player's model-customization byte (jacket/hat/sleeve/pants overlay flags)
            // so the preview shows outer skin layers. OtherClientPlayerEntity defaults to 0.
            try {
                java.lang.reflect.Field accessorField = null;
                for (Class<?> c = net.minecraft.entity.player.PlayerEntity.class; c != null && accessorField == null; c = c.getSuperclass()) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType() == net.minecraft.entity.data.TrackedData.class
                                && java.lang.reflect.Modifier.isStatic(f.getModifiers())
                                && f.getName().toUpperCase().contains("PLAYER_MODEL_PARTS")) {
                            accessorField = f;
                            break;
                        }
                    }
                }
                if (accessorField != null) {
                    accessorField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    net.minecraft.entity.data.TrackedData<Byte> accessor =
                            (net.minecraft.entity.data.TrackedData<Byte>) accessorField.get(null);
                    fake.getDataTracker().set(accessor, mc.player.getDataTracker().get(accessor));
                }
            } catch (Throwable ignored) {}
            lastLevel = mc.world;
            lastTickMs = System.currentTimeMillis();
            damageCycleTick = 0;
            double rx = mc.player.getX();
            double ry = mc.player.getY();
            double rz = mc.player.getZ();
            fake.setPosition(rx, ry, rz);
            fake.prevX = rx; fake.prevY = ry; fake.prevZ = rz;
            fake.lastRenderX = rx; fake.lastRenderY = ry; fake.lastRenderZ = rz;
            fake.setYaw(0f);
            fake.bodyYaw = 0f;
            fake.headYaw = 0f;
            fake.setPitch(0f);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastCenterX = (x0 + x1) / 2.0f;
        lastCenterY = (y0 + y1) / 2.0f;

        tickFake();

        int rectW = x1 - x0;
        int rectH = y1 - y0;
        int sizeByH = (int) (rectH / 2.8f);
        int sizeByW = (int) (rectW / 1.7f);
        int size = Math.max(24, Math.min(sizeByH, sizeByW));
        lastSize = size;
        if (!isGone()) {
            float centerY = (y0 + y1) / 2.0f;
            float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);
            if (yAngle > 0) {
                renderEntity(context, x0, y0, x1, y1, size, mouseX, mouseY);
                drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
            } else {
                drawShadow(context, x0, y0, x1, y1, size, mouseX, mouseY);
                renderEntity(context, x0, y0, x1, y1, size, mouseX, mouseY);
            }
        }
        renderParticles(context, x0, y0, x1, y1);
        owa$checkHoverHint(mouseX, mouseY);
    }

    private void owa$checkHoverHint(float mouseX, float mouseY) {
        if (lastSize <= 0) return;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mouseX >= lastCenterX - halfW && mouseX < lastCenterX + halfW
            && mouseY >= lastCenterY - halfH && mouseY < lastCenterY + halfH) {
            KillStats.tryShowHoverHint();
        }
    }

    private boolean isGone() {
        return fake != null && fake.deathTime >= DEATH_ANIM_TICKS;
    }

    private double dxForMode() {
        switch (mode) {
            case WALK:   return 0.21;
            case RUN:    return 0.289;
            case SNEAK:  return 0.065;
            default:     return 0.0;
        }
    }

    private float targetSpeedForMode() {
        switch (mode) {
            case RUN:   return 1.0f;
            case WALK:  return 0.7f;
            case SNEAK: return 0.25f;
            default:    return 0f;
        }
    }

    private void tickFake() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickMs;
        if (elapsed < TICK_INTERVAL_MS) return;
        boolean dying = fake.deathTime > 0 && fake.deathTime < DEATH_ANIM_TICKS;
        long maxTicks = dying ? 1L : 5L;
        int ticks = (int) Math.min(maxTicks, elapsed / TICK_INTERVAL_MS);
        lastTickMs += (long) ticks * TICK_INTERVAL_MS;
        for (int i = 0; i < ticks; i++) doTick();
    }

    private void doTick() {
        tickParticles();

        if (fake.deathTime > 0) {
            handleDeathTick();
            return;
        }

        // Vanilla BipedEntityModel.setAngles passes entity.age (yarn) into CrossbowPosing.swingArm
        // which adds the subtle idle arm sway. The preview entity is never world-ticked so its
        // age stays at 0 and the sway only varies by partialTick — a sub-pixel wobble. Advance it
        // manually each tick.
        fake.age++;

        double targetDx = dxForMode();
        float targetAnim = targetSpeedForMode();
        double dxRate = (Math.abs(targetDx) > Math.abs(currentDx)) ? 0.20 : 0.40;
        float animRate = (targetAnim > currentAnimSpeed) ? 0.20f : 0.40f;
        currentDx += (targetDx - currentDx) * dxRate;
        currentAnimSpeed += (targetAnim - currentAnimSpeed) * animRate;

        boolean sneak = mode == Mode.SNEAK;

        fake.setSneaking(sneak);
        fake.setPose(sneak ? EntityPose.CROUCHING : EntityPose.STANDING);

        double curX = fake.getX();
        double curY = fake.getY();
        double curZ = fake.getZ();

        // Move along body-forward (mirrors xAngle in renderEntity) so cape lag rotates with the
        // body when mouse turns the preview, instead of trailing world-space -Z.
        float xAngleTick = (float) Math.atan((lastCenterX - lastMouseX) / 40.0F);
        float facing = 180.0F + xAngleTick * 20.0F;
        float yawRad = (float) Math.toRadians(facing);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        fake.prevX = curX; fake.prevY = curY; fake.prevZ = curZ;
        fake.lastRenderX = curX; fake.lastRenderY = curY; fake.lastRenderZ = curZ;
        double newX = curX + currentDx * dirX;
        double newY = curY;
        double newZ = curZ + currentDx * dirZ;
        fake.setPosition(newX, newY, newZ);

        // Drive cape sway. Vanilla updates capeX in tickMovement() but our preview entity
        // doesn't run vanilla's tick path. Lazy-init on first tick so the first delta isn't huge.
        if (!owa$cloakInited) {
            fake.capeX = curX; fake.capeY = curY; fake.capeZ = curZ;
            fake.prevCapeX = curX; fake.prevCapeY = curY; fake.prevCapeZ = curZ;
            owa$cloakInited = true;
        }
        fake.prevCapeX = fake.capeX;
        fake.prevCapeY = fake.capeY;
        fake.prevCapeZ = fake.capeZ;
        double cdx = newX - fake.capeX;
        double cdy = newY - fake.capeY;
        double cdz = newZ - fake.capeZ;
        if (cdx > 10 || cdx < -10) fake.capeX = newX;
        if (cdz > 10 || cdz < -10) fake.capeZ = newZ;
        if (cdy > 10 || cdy < -10) fake.capeY = newY;
        fake.capeX += cdx * 0.25;
        fake.capeY += cdy * 0.25;
        fake.capeZ += cdz * 0.25;

        // Drive vanilla CapeFeatureRenderer's swing/bob term:
        //   xRotate += sin(lerp(td, prevHorizontalSpeed, horizontalSpeed) * 6) * 32
        //              * lerp(td, prevStrideDistance, strideDistance)
        // Mirrors LivingEntity.tickMovement which runs for in-world entities but not our fake.
        fake.prevHorizontalSpeed = fake.horizontalSpeed;
        fake.horizontalSpeed += (float) (Math.abs(currentDx) * 0.6);
        fake.prevStrideDistance = fake.strideDistance;
        fake.strideDistance += (Math.min(0.1f, (float) Math.abs(currentDx)) - fake.strideDistance) * 0.4f;

        fake.prevYaw = fake.getYaw();
        fake.prevPitch = fake.getPitch();
        fake.prevBodyYaw = fake.bodyYaw;
        fake.prevHeadYaw = fake.headYaw;
        fake.setYaw(facing);
        fake.setPitch(0f);
        fake.bodyYaw = facing;
        fake.headYaw = facing;

        fake.setOnGround(true);

        fake.limbAnimator.updateLimbs(currentAnimSpeed, 0.4f);

        if (fake.hurtTime > 0) {
            fake.setVelocity(damageDirFromCursor());
        } else {
            fake.setVelocity(new Vec3d(currentDx * dirX, 0, currentDx * dirZ));
            damageCycleTick = 0;
        }
        if (fake.hurtTime > 0) fake.hurtTime--;
    }

    private void handleDeathTick() {
        fake.deathTime++;
        if (fake.hurtTime > 0) fake.hurtTime--;
        fake.setVelocity(Vec3d.ZERO);
        // Sync prevX/Y/Z with current pos so LivingEntityRendererMixin's var3 reads zero while
        // dead. Otherwise entityRun keeps the stale walking-tick delta and the limb anim continues
        // swinging when speedLimbAngle is off.
        fake.prevX = fake.getX();
        fake.prevY = fake.getY();
        fake.prevZ = fake.getZ();
        // Keep advancing capeX/Y/Z toward the (stationary) entity so the cape lag decays smoothly
        // during death. Without this, capeX and prevCapeX freeze ~0.2m apart from the last live
        // tick's ease step and CapeFeatureRenderer's lerp(td, prevCape, cape) oscillates between
        // them every render frame — visible as a vibrating cape.
        fake.prevCapeX = fake.capeX;
        fake.prevCapeY = fake.capeY;
        fake.prevCapeZ = fake.capeZ;
        fake.capeX += (fake.getX() - fake.capeX) * 0.25;
        fake.capeY += (fake.getY() - fake.capeY) * 0.25;
        fake.capeZ += (fake.getZ() - fake.capeZ) * 0.25;
        // Same idea for the swing/bob term: ease strideDistance toward 0 (= "no movement"), and
        // freeze the phase so sin(lerp(td, prevHorizontalSpeed, horizontalSpeed) * 6) is constant
        // within the tick.
        fake.prevHorizontalSpeed = fake.horizontalSpeed;
        fake.prevStrideDistance = fake.strideDistance;
        fake.strideDistance += (0f - fake.strideDistance) * 0.4f;
        fake.limbAnimator.updateLimbs(0.0f, 0.6f);
        if (fake.deathTime == DEATH_ANIM_TICKS) {
            spawnDeathParticles(20);
        }
        if (fake.deathTime >= DEATH_ANIM_TICKS + RESPAWN_DELAY_TICKS) {
            respawn();
        }
    }

    private void hit() {
        if (fake == null || fake.deathTime > 0) return;
        accumulatedDamage += HIT_DAMAGE;
        if (accumulatedDamage >= MAX_HEALTH) {
            triggerDeath();
        } else {
            fake.hurtTime = DAMAGE_HURT_TIME;
            fake.maxHurtTime = DAMAGE_HURT_TIME;
            SharedValueUtil.invalidateDamageDir(fake);
            playSound(SoundEvents.ENTITY_PLAYER_HURT);
        }
    }

    private void triggerDeath() {
        fake.setHealth(0);
        fake.deathTime = 1;
        fake.hurtTime = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
        playSound(SoundEvents.ENTITY_PLAYER_DEATH);
        KillStats.onKill();
    }

    private void respawn() {
        fake.deathTime = 0;
        fake.hurtTime = 0;
        fake.setHealth(fake.getMaxHealth());
        accumulatedDamage = 0;
        damageCycleTick = 0;
        currentDx = 0.0;
        currentAnimSpeed = 0.0f;
        SharedValueUtil.invalidateDamageDir(fake);
    }

    private void playSound(SoundEvent sound) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().play(PositionedSoundInstance.master(sound, 1.0f, 1.0f));
    }

    private void spawnDeathParticles(int count) {
        float cx = lastCenterX;
        float cy = lastCenterY + lastSize * 0.45f;
        float halfW = lastSize * 0.95f;
        float halfH = lastSize * 0.95f;

        float pxPerBlock = lastSize / 1.8f;
        float velScale = 0.05f * pxPerBlock;
        float gaussScale = 0.02f * pxPerBlock;

        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = cx + (rng.nextFloat() * 2f - 1f) * halfW;
            p.y = cy + (rng.nextFloat() * 2f - 1f) * halfH;
            p.vx = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.vy = (float) rng.nextGaussian() * gaussScale + (rng.nextFloat() * 2f - 1f) * velScale;
            p.gravity = -0.04f * 0.1f * pxPerBlock;
            p.drag = 0.9f;
            p.maxLife = 24 + rng.nextInt(16);
            p.life = 4 + rng.nextInt(p.maxLife - 3);
            p.size = 14 + rng.nextInt(30);
            particles.add(p);
        }
    }

    private void tickParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= p.drag;
            p.vy *= p.drag;
            p.life--;
            if (p.life <= 0) it.remove();
        }
    }

    private void renderParticles(DrawContext context, int x0, int y0, int x1, int y1) {
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            int px = (int) p.x - p.size / 2;
            int py = (int) p.y - p.size / 2;
            if (px + p.size < x0 || px >= x1 || py + p.size < y0 || py >= y1) continue;
            float frac = p.life / (float) p.maxLife;
            int frame = (int) (frac * POOF_FRAMES.length);
            if (frame < 0) frame = 0;
            if (frame >= POOF_FRAMES.length) frame = POOF_FRAMES.length - 1;
            context.drawTexture(POOF_FRAMES[frame], px, py, p.size, p.size, 0f, 0f, 8, 8, 8, 8);
        }
    }

    private Vec3d damageDirFromCursor() {
        float sdx = lastMouseX - lastCenterX;
        float sdy = lastMouseY - lastCenterY;
        float mag = (float) Math.sqrt(sdx * sdx + sdy * sdy);
        if (mag < 1e-3f) return new Vec3d(0.1, 0, 0.001);
        double wx = sdx / mag * 0.2;
        double wz = -sdy / mag * 0.2;
        if (Math.abs(wx) < 1.0e-5) wx = wx >= 0 ? 1.0e-5 : -1.0e-5;
        if (Math.abs(wz) < 1.0e-5) wz = wz >= 0 ? 1.0e-5 : -1.0e-5;
        return new Vec3d(wx, 0, wz);
    }

    private float currentPartialTick() {
        long delta = System.currentTimeMillis() - lastTickMs;
        float p = delta / (float) TICK_INTERVAL_MS;
        if (p < 0f) return 0f;
        if (p > 1f) return 1f;
        return p;
    }

    private void renderEntity(DrawContext context, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        float xAngle = (float) Math.atan((centerX - mouseX) / 40.0F);
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);

        float savedBodyYaw = fake.bodyYaw;
        float savedYaw = fake.getYaw();
        float savedPitch = fake.getPitch();
        float savedPrevBodyYaw = fake.prevBodyYaw;
        float savedPrevYaw = fake.prevYaw;
        float savedPrevPitch = fake.prevPitch;
        float savedPrevHeadYaw = fake.prevHeadYaw;
        float savedHeadYaw = fake.headYaw;

        fake.bodyYaw = 180.0F + xAngle * 20.0F;
        fake.setYaw(180.0F + xAngle * 40.0F);
        fake.setPitch(-yAngle * 20.0F);
        fake.headYaw = fake.getYaw();
        fake.prevHeadYaw = fake.getYaw();
        fake.prevBodyYaw = fake.bodyYaw;
        fake.prevYaw = fake.getYaw();
        fake.prevPitch = fake.getPitch();

        Vector3f translation = new Vector3f(0.0F, fake.getHeight() / 2.0F + 0.0625F, 0.0F);

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, centerY, 1050.0);
        matrices.multiplyPositionMatrix(new org.joml.Matrix4f().scaling((float) size, (float) size, (float) -size));
        matrices.translate(translation.x, translation.y, translation.z);
        matrices.multiply(rotation);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Quaternionf cameraOrientation = new Quaternionf(xRotation).conjugate();
        dispatcher.setRotation(cameraOrientation);
        dispatcher.setRenderShadows(false);
        float partialTick = currentPartialTick();
        RenderSystem.runAsFancy(() -> dispatcher.render(fake, 0.0, 0.0, 0.0, 0.0F, partialTick, matrices, context.getVertexConsumers(), 0xF000F0));
        context.draw();
        dispatcher.setRenderShadows(true);
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();

        fake.bodyYaw = savedBodyYaw;
        fake.setYaw(savedYaw);
        fake.setPitch(savedPitch);
        fake.prevBodyYaw = savedPrevBodyYaw;
        fake.prevYaw = savedPrevYaw;
        fake.prevPitch = savedPrevPitch;
        fake.prevHeadYaw = savedPrevHeadYaw;
        fake.headYaw = savedHeadYaw;
    }

    private static final Identifier SHADOW_TEX = Identifier.of("minecraft", "textures/misc/shadow.png");

    private void drawShadow(DrawContext context, int x0, int y0, int x1, int y1, int size, float mouseX, float mouseY) {
        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float feetY = centerY + size * 0.95f + 1.0f;
        float yAngle = (float) Math.atan((centerY - mouseY) / 40.0F);

        float tiltRad = yAngle * 20.0F * (float) (Math.PI / 180.0);
        float verticalScale = Math.abs((float) Math.sin(tiltRad)) * 1.3f;

        int shadowSize = (int) (size * 1.05f);
        int half = shadowSize / 2;

        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(centerX, feetY, 1100.0);
        matrices.scale(1.0f, verticalScale, 1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0f, 0f, 0f, 0x55 / 255f);
        context.drawTexture(SHADOW_TEX, -half, -half, shadowSize, shadowSize, 0f, 0f, shadowSize, shadowSize, shadowSize, shadowSize);
        context.draw();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private void drawUnavailable(DrawContext context, int x0, int y0, int x1, int y1) {
        context.fill(x0, y0, x1, y1, 0x80000000);
        MinecraftClient mc = MinecraftClient.getInstance();
        String msg = "Preview available in-world";
        int tx = (x0 + x1) / 2 - mc.textRenderer.getWidth(msg) / 2;
        int ty = (y0 + y1) / 2 - 4;
        context.drawText(mc.textRenderer, msg, tx, ty, 0xFFAAAAAA, true);
    }

    public boolean mouseClicked(double mx, double my) {
        if (fake == null || lastSize <= 0 || fake.deathTime > 0) return false;
        float halfW = lastSize * 0.45f;
        float halfH = lastSize * 0.95f;
        if (mx < lastCenterX - halfW || mx > lastCenterX + halfW
            || my < lastCenterY - halfH || my > lastCenterY + halfH) return false;
        lastMouseX = (float) mx;
        lastMouseY = (float) my;
        fake.setVelocity(damageDirFromCursor());
        hit();
        return true;
    }

    public void dispose() {
        fake = null;
        lastLevel = null;
        particles.clear();
        accumulatedDamage = 0;
    }
}
*///?}
