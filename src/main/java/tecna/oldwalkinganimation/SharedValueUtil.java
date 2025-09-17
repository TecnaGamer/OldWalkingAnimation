package tecna.oldwalkinganimation;


import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;


public class SharedValueUtil {
    private static final ThreadLocal<Map<LivingEntity, Float>> var10Map = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> var8Map = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> isMovingMap = new ThreadLocal<>();
    private static final ThreadLocal<Map<LivingEntity, Float>> bodyRotMap = new ThreadLocal<>();

    //    private static final ThreadLocal<Map<LivingEntity, Boolean>> isHoldingMap = new ThreadLocal<>();
    private static boolean isHoldingMap;

    public static void setVar10(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = var10Map.get();
        if (entityMap == null) {
            entityMap = new HashMap<>();
            var10Map.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getVar10(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = var10Map.get();
        if (entityMap == null) {
            return 0.0f; // Or any default value
        }
        return entityMap.getOrDefault(entity, 0.0f); // Return default if not found
    }



public static void setVar8(LivingEntity entity, float value) {
    Map<LivingEntity, Float> entityMap = var8Map.get();
    if (entityMap == null) {
        entityMap = new HashMap<>();
        var8Map.set(entityMap);
    }
    entityMap.put(entity, value);
}

public static float getVar8(LivingEntity entity) {
    Map<LivingEntity, Float> entityMap = var8Map.get();
    if (entityMap == null) {
        return 0.0f; // Or any default value
    }
    return entityMap.getOrDefault(entity, 0.0f); // Return default if not found
}

    public static void setIsMoving(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = isMovingMap.get();
        if (entityMap == null) {
            entityMap = new HashMap<>();
            isMovingMap.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getIsMoving(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = isMovingMap.get();
        if (entityMap == null) {
            return 0.0f; // Or any default value
        }
        return entityMap.getOrDefault(entity, 0.0f); // Return default if not found
    }


    public static void setBodyRot(LivingEntity entity, float value) {
        Map<LivingEntity, Float> entityMap = bodyRotMap.get();
        if (entityMap == null) {
            entityMap = new HashMap<>();
            bodyRotMap.set(entityMap);
        }
        entityMap.put(entity, value);
    }

    public static float getBodyRot(LivingEntity entity) {
        Map<LivingEntity, Float> entityMap = bodyRotMap.get();
        if (entityMap == null) {
            return 0.0f; // Or any default value
        }
        return entityMap.getOrDefault(entity, 0.0f); // Return default if not found
    }


    public static void setIsHoldingMap(Boolean value) {

        isHoldingMap = value;

    }

    public static boolean getIsHoldingMap() {

        return isHoldingMap; // Return default if not found
    }

}
