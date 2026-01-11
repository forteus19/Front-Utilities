package red.vuis.frontutil.server;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.server.data.config.AddonServerConfig;

@Mod(
	value = AddonConstants.MOD_ID,
	dist = Dist.DEDICATED_SERVER
)
public final class FrontUtilServer {
	public FrontUtilServer(ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, AddonServerConfig.SPEC);
		
		AddonConstants.LOGGER.info("Initialized Front-Utilities for server!");
	}
}
