package red.vuis.frontutil;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import red.vuis.frontutil.registry.AddonSounds;
import red.vuis.frontutil.setup.AddonRegistryMigration;

@Mod(AddonConstants.MOD_ID)
public final class FrontUtil {
	public FrontUtil(IEventBus eventBus) {
		AddonSounds.init(eventBus);
		AddonRegistryMigration.init();
		
		AddonConstants.LOGGER.info("Initialized Front-Utilities for common!");
	}
}
