package red.vuis.frontutil.registry;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import red.vuis.frontutil.AddonConstants;

public final class AddonSounds {
	private AddonSounds() {
	}
	
	private static final DeferredRegister<SoundEvent> DR = DeferredRegister.create(Registries.SOUND_EVENT, AddonConstants.MOD_ID);
	
	/**
	 * For testing purposes
	 */
	@ApiStatus.Internal
	public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_LSP_RADIO_BIENVENIDA = register("ambient.lsp.radio.bienvenida");
	
	private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
		return DR.register(id, () -> SoundEvent.of(AddonConstants.id(id)));
	}
	
	public static void init(IEventBus eventBus) {
		DR.register(eventBus);
	}
}
