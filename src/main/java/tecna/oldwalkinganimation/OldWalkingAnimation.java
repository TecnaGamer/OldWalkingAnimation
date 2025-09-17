package tecna.oldwalkinganimation;



//? if fabric
import net.fabricmc.api.ModInitializer;
import eu.midnightdust.lib.config.MidnightConfig;
import tecna.oldwalkinganimation.config.Config;
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


	//? if forge || neoforge {





    /*public OldWalkingAnimation() {


		onInitialize();
		//? if <=1.19.4 {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
				new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, "oldwalkinganimation")));
		//?}
	}
    *///?}




	public void onInitialize() {
		Config.init("oldwalkinganimation", Config.class);


		System.out.println("Old Walking Animation Starting!");
	}



}
