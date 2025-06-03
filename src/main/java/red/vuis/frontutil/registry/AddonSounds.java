package red.vuis.frontutil.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import red.vuis.frontutil.FrontUtil;

public final class AddonSounds {
	private AddonSounds() {
	}
	
	private static final DeferredRegister<SoundEvent> DR = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, FrontUtil.MOD_ID);
	
	public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_LSP_RADIO_BIENVENIDA = register("ambient.lsp.radio.bienvenida");
	
	private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
		return DR.register(id, () -> SoundEvent.createVariableRangeEvent(FrontUtil.res(id)));
	}
	
	public static void init(IEventBus eventBus) {
		DR.register(eventBus);
	}
}
