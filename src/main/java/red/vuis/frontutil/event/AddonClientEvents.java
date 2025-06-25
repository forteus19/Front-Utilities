package red.vuis.frontutil.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.client.command.FrontUtilClientCommand;

@EventBusSubscriber(
	modid = FrontUtil.MOD_ID,
	value = Dist.CLIENT
)
public final class AddonClientEvents {
	private AddonClientEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		FrontUtil.LOGGER.info("Registering client-only commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilClientCommand.register(dispatcher);
	}
}
