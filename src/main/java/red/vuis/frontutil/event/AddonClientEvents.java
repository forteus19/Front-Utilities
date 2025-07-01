package red.vuis.frontutil.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.command.FrontUtilClientCommand;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID,
	value = Dist.CLIENT
)
public final class AddonClientEvents {
	private AddonClientEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		AddonConstants.LOGGER.info("Registering client-only commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilClientCommand.register(dispatcher);
	}
}
