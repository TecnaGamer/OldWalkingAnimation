package tecna.oldwalkinganimation.mixin;

import net.fabricmc.api.EnvType;
import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.lwjgl.system.CallbackI;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import tecna.oldwalkinganimation.OldWalkingAnimation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MyMixinConfigPlugin implements IMixinConfigPlugin {


    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        try {
            String gameVersion = MinecraftVersion.create().getId();
            if (gameVersion != null && gameVersion.startsWith("1.14")) {
                //System.out.println("Old Walking Animation Started, Running 1.14!");
                return false;
            } else {
                System.out.println("Old Walking Animation Started!");
                return true;
            }
        } catch (NoSuchMethodError error) {
            System.out.println("Old Walking Animation Started! Running Modern!");
    return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
