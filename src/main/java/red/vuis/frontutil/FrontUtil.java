package red.vuis.frontutil;

import com.boehmod.blockfront.BlockFront;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import red.vuis.frontutil.registry.AddonSounds;
import red.vuis.frontutil.setup.GunSkinIndex;

@Mod(FrontUtil.MOD_ID)
public final class FrontUtil {
	public static final String MOD_ID = "frontutil";
	private static final Logger LOGGER = LogManager.getLogger("Front-Utilities");
	
	public FrontUtil(IEventBus eventBus) {
		eventBus.addListener(FrontUtil::onLoadComplete);
		
		AddonSounds.init(eventBus);
		
		info("Front-Utilities is active!");
	}
	
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		var manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			error("Failed to get BlockFront manager! Some things will be broken.");
		} else {
			info("Indexing skins...");
			GunSkinIndex.init(manager.getCloudRegistry());
		}
	}
	
	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	public static void info(String message) {
		LOGGER.info(message);
	}
	
	public static void error(String message) {
		LOGGER.error(message);
	}
}
