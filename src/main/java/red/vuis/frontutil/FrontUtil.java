package red.vuis.frontutil;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import red.vuis.frontutil.registry.AddonSounds;

@Mod(FrontUtil.MOD_ID)
public final class FrontUtil {
	public static final String MOD_ID = "frontutil";
	private static final Logger LOGGER = LogUtils.getLogger();

	public FrontUtil(IEventBus eventBus) {
		AddonSounds.init(eventBus);
		LOGGER.info("Front-Utilities is active!");
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
