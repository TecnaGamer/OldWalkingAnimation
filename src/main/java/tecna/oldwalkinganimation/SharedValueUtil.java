package tecna.oldwalkinganimation;


//? if >=26.1 || neoforge {
import net.minecraft.world.entity.LivingEntity;
//?} else {
/*import net.minecraft.entity.LivingEntity;
*///?}

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


public class SharedValueUtil {
    private static final ThreadLocal<Map<LivingEntity, Float>> var10Map = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> var8Map = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> isMovingMap = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> bodyRotMap = new ThreadLocal<>();
    private static final Set<LivingEntity> damageDirInvalidations =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static boolean isHoldingMap;

    public static void invalidateDamageDir(LivingEntity entity) {
        damageDirInvalidations.add(entity);
    }

    public static boolean consumeDamageDirInvalidation(LivingEntity entity) {
        return damageDirInvalidations.remove(entity);
    }

    public static void setVar10(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = var10Map.get();
        if (entityMap == null) {
            entityMap = new WeakHashMap<>();
            var10Map.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getVar10(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = var10Map.get();
        if (entityMap == null) {
            return 0.0f;
        }
        return entityMap.getOrDefault(entity, 0.0f);
    }



public static void setVar8(LivingEntity entity, float value) {
    Map<LivingEntity, Float> entityMap = var8Map.get();
    if (entityMap == null) {
        entityMap = new WeakHashMap<>();
        var8Map.set(entityMap);
    }
    entityMap.put(entity, value);
}

public static float getVar8(LivingEntity entity) {
    Map<LivingEntity, Float> entityMap = var8Map.get();
    if (entityMap == null) {
        return 0.0f;
    }
    return entityMap.getOrDefault(entity, 0.0f);
}

    public static void setIsMoving(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = isMovingMap.get();
        if (entityMap == null) {
            entityMap = new WeakHashMap<>();
            isMovingMap.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getIsMoving(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = isMovingMap.get();
        if (entityMap == null) {
            return 0.0f;
        }
        return entityMap.getOrDefault(entity, 0.0f);
    }


    public static void setBodyRot(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = bodyRotMap.get();
        if (entityMap == null) {
            entityMap = new WeakHashMap<>();
            bodyRotMap.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getBodyRot(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = bodyRotMap.get();
        if (entityMap == null) {
            return 0.0f;
        }
        return entityMap.getOrDefault(entity, 0.0f);
    }


    public static void setIsHoldingMap(Boolean value) {
        isHoldingMap = value;
    }

    public static boolean getIsHoldingMap() {
        return isHoldingMap;
    }

}
