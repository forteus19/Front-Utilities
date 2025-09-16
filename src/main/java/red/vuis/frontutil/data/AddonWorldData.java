package red.vuis.frontutil.data;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import red.vuis.frontutil.AddonConstants;

public class AddonWorldData extends PersistentState {
	private static final Type<AddonWorldData> TYPE = new Type<>(AddonWorldData::new, AddonWorldData::readNbt, null);
	
	public final Map<RegistryEntry<Item>, GunModifier> gunModifiers = new Object2ObjectOpenHashMap<>();
	
	public AddonWorldData() {
	}
	
	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		NbtCompound gunModifiersData = new NbtCompound();
		for (Map.Entry<RegistryEntry<Item>, GunModifier> entry : gunModifiers.entrySet()) {
			entry.getKey().getKey().ifPresent(key -> {
				NbtCompound modifierData = (NbtCompound) GunModifier.CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue())
					.resultOrPartial(AddonConstants.LOGGER::error)
					.orElseThrow();
				gunModifiersData.put(key.toString(), modifierData);
			});
		}
		nbt.put("gunModifiers", gunModifiersData);
		
		return nbt;
	}
	
	public static AddonWorldData readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		AddonWorldData data = new AddonWorldData();
		
		NbtCompound gunModifiersData = nbt.getCompound("gunModifiers");
		for (String key : gunModifiersData.getKeys()) {
			Registries.ITEM.getEntry(Identifier.tryParse(key)).ifPresent(itemEntry -> {
				NbtCompound modifierData = gunModifiersData.getCompound(key);
				GunModifier modifier = GunModifier.CODEC.parse(NbtOps.INSTANCE, modifierData)
					.resultOrPartial(AddonConstants.LOGGER::error)
					.orElseThrow();
				data.gunModifiers.put(itemEntry, modifier);
			});
		}
		
		return data;
	}
	
	public static AddonWorldData get(MinecraftServer server) {
		AddonWorldData data = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, AddonConstants.MOD_ID);
		data.markDirty();
		return data;
	}
}
