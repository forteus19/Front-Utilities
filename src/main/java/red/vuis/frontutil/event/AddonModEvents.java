package red.vuis.frontutil.event;

import com.boehmod.blockfront.BlockFront;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.net.packet.GunModifiersPacket;
import red.vuis.frontutil.setup.GunModifierIndex;
import red.vuis.frontutil.setup.GunSkinIndex;
import red.vuis.frontutil.setup.LoadoutIndex;

@EventBusSubscriber(
	modid = FrontUtil.MOD_ID,
	bus = EventBusSubscriber.Bus.MOD
)
public final class AddonModEvents {
	private AddonModEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(GunModifiersPacket.TYPE, GunModifiersPacket.STREAM_CODEC, GunModifiersPacket::handle);
	}
	
	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		FrontUtil.info("Indexing default gun properties...");
		GunModifierIndex.init();
		
		FrontUtil.info("Indexing default loadouts...");
		LoadoutIndex.init();
		
		var manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			FrontUtil.error("Failed to get BlockFront manager! Some things will be broken.");
		} else {
			FrontUtil.info("Indexing skins...");
			GunSkinIndex.init(manager.getCloudRegistry());
		}
	}
}
