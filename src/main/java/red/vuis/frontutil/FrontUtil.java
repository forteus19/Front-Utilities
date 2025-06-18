package red.vuis.frontutil;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import red.vuis.frontutil.registry.AddonSounds;

@Mod(FrontUtil.MOD_ID)
public final class FrontUtil {
	public static final String MOD_ID = "frontutil";
	private static final Logger LOGGER = LogManager.getLogger("Front-Utilities");
	
	public FrontUtil(IEventBus eventBus) {
		AddonSounds.init(eventBus);
		
		info("Front-Utilities is active!");
	}
	
	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	public static void info(String message, Object... args) {
		LOGGER.info(message, args);
	}
	
	public static void error(String message, Object... args) {
		LOGGER.error(message, args);
	}
}
