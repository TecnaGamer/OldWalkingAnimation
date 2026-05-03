package tecna.oldwalkinganimation;

//? if >=26.1 {
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public interface OwaStateHolder {
    void owa$setEntity(LivingEntity entity);
    LivingEntity owa$getEntity();
    void owa$setPartialTick(float partialTick);
    float owa$getPartialTick();

    java.util.Set<LivingEntity> PREVIEW_ENTITIES = java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    static LivingEntity getEntity(LivingEntityRenderState state) {
        return ((OwaStateHolder) (Object) state).owa$getEntity();
    }
    static void setEntity(LivingEntityRenderState state, LivingEntity entity) {
        ((OwaStateHolder) (Object) state).owa$setEntity(entity);
    }
    static float getPartialTick(LivingEntityRenderState state) {
        return ((OwaStateHolder) (Object) state).owa$getPartialTick();
    }
    static void setPartialTick(LivingEntityRenderState state, float partialTick) {
        ((OwaStateHolder) (Object) state).owa$setPartialTick(partialTick);
    }
    static void markPreview(LivingEntity entity) { PREVIEW_ENTITIES.add(entity); }
    static boolean isPreview(LivingEntity entity) { return PREVIEW_ENTITIES.contains(entity); }
}
//?} else if (neoforge && >=1.21.2) {
/*import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.WeakHashMap;

public final class OwaStateHolder {
    private static final Map<LivingEntityRenderState, LivingEntity> ENTITY = new WeakHashMap<>();
    private static final Map<LivingEntityRenderState, Float> PARTIAL = new WeakHashMap<>();
    private static final java.util.Set<LivingEntity> PREVIEW_ENTITIES = java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new WeakHashMap<>()));

    private OwaStateHolder() {}

    public static void setEntity(LivingEntityRenderState state, LivingEntity entity) {
        synchronized (ENTITY) { ENTITY.put(state, entity); }
    }

    public static LivingEntity getEntity(LivingEntityRenderState state) {
        synchronized (ENTITY) { return ENTITY.get(state); }
    }

    public static void setPartialTick(LivingEntityRenderState state, float partialTick) {
        synchronized (PARTIAL) { PARTIAL.put(state, partialTick); }
    }

    public static float getPartialTick(LivingEntityRenderState state) {
        synchronized (PARTIAL) {
            Float v = PARTIAL.get(state);
            return v == null ? 0f : v;
        }
    }

    public static void markPreview(LivingEntity entity) { PREVIEW_ENTITIES.add(entity); }
    public static boolean isPreview(LivingEntity entity) { return PREVIEW_ENTITIES.contains(entity); }
}
*///?} else if >=1.21.2 {
/*import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;

import java.util.Map;
import java.util.WeakHashMap;

public final class OwaStateHolder {
    private static final Map<LivingEntityRenderState, LivingEntity> ENTITY = new WeakHashMap<>();
    private static final Map<LivingEntityRenderState, Float> PARTIAL = new WeakHashMap<>();
    private static final java.util.Set<LivingEntity> PREVIEW_ENTITIES = java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new WeakHashMap<>()));

    private OwaStateHolder() {}

    public static void setEntity(LivingEntityRenderState state, LivingEntity entity) {
        synchronized (ENTITY) { ENTITY.put(state, entity); }
    }

    public static LivingEntity getEntity(LivingEntityRenderState state) {
        synchronized (ENTITY) { return ENTITY.get(state); }
    }

    public static void setPartialTick(LivingEntityRenderState state, float partialTick) {
        synchronized (PARTIAL) { PARTIAL.put(state, partialTick); }
    }

    public static float getPartialTick(LivingEntityRenderState state) {
        synchronized (PARTIAL) {
            Float v = PARTIAL.get(state);
            return v == null ? 0f : v;
        }
    }

    public static void markPreview(LivingEntity entity) { PREVIEW_ENTITIES.add(entity); }
    public static boolean isPreview(LivingEntity entity) { return PREVIEW_ENTITIES.contains(entity); }
}
*///?} else if neoforge {
/*import net.minecraft.world.entity.LivingEntity;
import java.util.WeakHashMap;

public final class OwaStateHolder {
    private static final java.util.Set<LivingEntity> PREVIEW_ENTITIES = java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new WeakHashMap<>()));
    private OwaStateHolder() {}
    public static void markPreview(LivingEntity entity) { if (entity != null) PREVIEW_ENTITIES.add(entity); }
    public static boolean isPreview(LivingEntity entity) { return entity != null && PREVIEW_ENTITIES.contains(entity); }
}
*///?} else {
/*import net.minecraft.entity.LivingEntity;
import java.util.WeakHashMap;

public final class OwaStateHolder {
    private static final java.util.Set<LivingEntity> PREVIEW_ENTITIES = java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new WeakHashMap<>()));
    private OwaStateHolder() {}
    public static void markPreview(LivingEntity entity) { if (entity != null) PREVIEW_ENTITIES.add(entity); }
    public static boolean isPreview(LivingEntity entity) { return entity != null && PREVIEW_ENTITIES.contains(entity); }
}
*///?}
