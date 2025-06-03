package red.vuis.frontutil;

import com.boehmod.blockfront.BlockFront;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.slf4j.Logger;

import red.vuis.frontutil.registry.AddonSounds;
import red.vuis.frontutil.setup.GunSkinIndex;

@Mod(FrontUtil.MOD_ID)
public final class FrontUtil {
	public static final String MOD_ID = "frontutil";
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public FrontUtil(IEventBus eventBus) {
		eventBus.addListener(FrontUtil::onLoadComplete);
		
		AddonSounds.init(eventBus);
		
		LOGGER.info("Front-Utilities is active!");
	}
	
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		var manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			LOGGER.error("Failed to get BlockFront manager! Some things will be broken.");
		} else {
			LOGGER.info("Indexing skins...");
			GunSkinIndex.init(manager.getCloudRegistry());
		}
	}
	
	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
