package tecna.oldwalkinganimation;



//? if fabric
import net.fabricmc.api.ModInitializer;
import eu.midnightdust.lib.config.MidnightConfig;
import tecna.oldwalkinganimation.config.Config;
//? if >=26.1 || neoforge {
//? if fabric
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.lwjgl.glfw.GLFW;
import tecna.oldwalkinganimation.config.PresetManager;
//?} else if fabric {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
//? if >=1.21.9
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import tecna.oldwalkinganimation.config.PresetManager;
*///?}
//? if forge {
/*import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
@Mod("oldwalkinganimation")

*///?} if neoforge {
/*import net.neoforged.fml.common.Mod;

@Mod("oldwalkinganimation")
*///?}


public class OldWalkingAnimation
		//? if fabric
		implements ModInitializer
{



	public static final String MOD_ID = "oldwalkinganimation";


	//? if forge {





    /*public OldWalkingAnimation() {


		onInitialize();
		//? if <=1.19.4 {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
				new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, "oldwalkinganimation")));
		//?}
	}
    *///?}
	//? if neoforge {
	/*public OldWalkingAnimation(net.neoforged.fml.ModContainer container) {
		onInitialize();
		//? if >=1.20.6 {
		container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
				(mc, parent) -> new tecna.oldwalkinganimation.config.OwaConfigScreen(parent));
		//?} else if <=1.20.4 {
		/^net.neoforged.fml.ModLoadingContext.get().registerExtensionPoint(net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new tecna.oldwalkinganimation.config.OwaConfigScreen(parent)));
		^///?}
	}
	*///?}




	public void onInitialize() {
		Config.init("oldwalkinganimation", Config.class);
		//? if >=26.1 || neoforge {
		PresetManager.init("oldwalkinganimation");
		OwaSkins.init("oldwalkinganimation");

		//? if >=1.21.11 {
		KeyMapping.Category owaCategory = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("oldwalkinganimation", "main"));
		KeyMapping spawnSteveKey = new KeyMapping(
				"key.oldwalkinganimation.spawn_steve",
				GLFW.GLFW_KEY_G,
				owaCategory);
		//?} else if >=1.21.9 {
		/*KeyMapping.Category owaCategory = KeyMapping.Category.register(
				ResourceLocation.fromNamespaceAndPath("oldwalkinganimation", "main"));
		KeyMapping spawnSteveKey = new KeyMapping(
				"key.oldwalkinganimation.spawn_steve",
				GLFW.GLFW_KEY_G,
				owaCategory);
		*///?} else {
		/*KeyMapping spawnSteveKey = new KeyMapping(
				"key.oldwalkinganimation.spawn_steve",
				com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"Old Walking Animation");
		*///?}
		//? if fabric
		KeyMappingHelper.registerKeyMapping(spawnSteveKey);
		OwaSteveManager.setSpawnKey(spawnSteveKey);
		//?} else if fabric {
		/*PresetManager.init("oldwalkinganimation");
		OwaSkins.init("oldwalkinganimation");

		//? if >=1.21.9 {
		KeyBinding.Category owaCategory = KeyBinding.Category.create(
				Identifier.of("oldwalkinganimation", "main"));
		KeyBinding spawnSteveKey = new KeyBinding(
				"key.oldwalkinganimation.spawn_steve",
				GLFW.GLFW_KEY_G,
				owaCategory);
		//?} else {
		/^KeyBinding spawnSteveKey = new KeyBinding(
				"key.oldwalkinganimation.spawn_steve",
				GLFW.GLFW_KEY_G,
				"key.categories.oldwalkinganimation");
		^///?}
		KeyBindingHelper.registerKeyBinding(spawnSteveKey);
		OwaSteveManager.setSpawnKey(spawnSteveKey);
		*///?}

		System.out.println("Old Walking Animation Starting!");
	}



}
