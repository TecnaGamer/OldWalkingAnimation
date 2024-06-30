package tecna.oldwalkinganimation.mixin;

import net.minecraft.MinecraftVersion;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MyMixinConfigPlugin114 implements IMixinConfigPlugin {


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
                System.out.println("Old Walking Animation Started, Running 1.14!");
                return true;
            } else {
                //System.out.println("Old Walking Animation Started!");
                return false;
            }
        } catch (NoSuchMethodError error) {
            //System.out.println("Old Walking Animation Started! Running Modern!");
    return false;
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
