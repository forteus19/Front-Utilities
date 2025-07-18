package red.vuis.frontutil.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.data.config.AddonClientConfig;

@Mod(
	value = AddonConstants.MOD_ID,
	dist = Dist.CLIENT
)
public final class FrontUtilClient {
	private static FrontUtilClient instance = null;
	
	public final ModContainer container;
	
	public FrontUtilClient(ModContainer container) {
		instance = this;
		
		this.container = container;
		
		container.registerConfig(ModConfig.Type.CLIENT, AddonClientConfig.SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		
		AddonConstants.LOGGER.info("Initialized Front-Utilities for client!");
	}
	
	public static FrontUtilClient getInstance() {
		if (instance == null) {
			throw new IllegalStateException("FrontUtilClient instance is null!");
		}
		return instance;
	}
}
