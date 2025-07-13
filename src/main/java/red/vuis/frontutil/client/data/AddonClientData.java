package red.vuis.frontutil.client.data;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.map.MapEnvironment;
import net.minecraft.client.MinecraftClient;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.LoadoutIndex;

public final class AddonClientData {
	private static AddonClientData instance = null;
	
	public Map<LoadoutIndex.Identifier, List<Loadout>> tempLoadouts;
	public @Nullable MapEnvironment editing = null;
	
	private AddonClientData() {
		reloadLoadouts();
	}
	
	public static AddonClientData getInstance() {
		if (!FMLEnvironment.dist.isClient()) {
			throw new RuntimeException("Tried to get client data when not on the client!");
		}
		if (instance == null) {
			instance = new AddonClientData();
		}
		return instance;
	}
	
	public void reloadLoadouts() {
		tempLoadouts = LoadoutIndex.currentFlat();
	}
	
	public void resetLoadouts() {
		tempLoadouts = LoadoutIndex.copyFlat(LoadoutIndex.DEFAULT);
	}
	
	public @Nullable List<Loadout> getTempLoadouts(BFCountry country, String skin, MatchClass matchClass) {
		return tempLoadouts.get(new LoadoutIndex.Identifier(country, skin, matchClass));
	}
	
	public void setTempLoadout(BFCountry country, String skin, MatchClass matchClass, int level, Loadout loadout) {
		LoadoutIndex.Identifier id = new LoadoutIndex.Identifier(country, skin, matchClass);
		tempLoadouts.get(id).set(level, loadout);
	}
	
	public void resetTempLoadout(LoadoutIndex.Identifier id, int level) {
		List<Loadout> toReset = tempLoadouts.get(id);
		List<Loadout> defaults = LoadoutIndex.DEFAULT.get(id);
		
		if (toReset != null && defaults != null) {
			Loadout defaultLoadout = defaults.get(level);
			
			if (defaultLoadout != null) {
				toReset.set(level, LoadoutIndex.cloneLoadout(defaultLoadout));
			}
		}
	}
	
	public void resetTempLoadout(BFCountry country, String skin, MatchClass matchClass, int level) {
		resetTempLoadout(new LoadoutIndex.Identifier(country, skin, matchClass), level);
	}
	
	public void resetTempLoadoutAllLevels(LoadoutIndex.Identifier id) {
		List<Loadout> toReset = tempLoadouts.get(id);
		List<Loadout> defaults = LoadoutIndex.DEFAULT.get(id);
		
		if (toReset != null) {
			toReset.clear();
			if (defaults != null) {
				defaults.stream().map(LoadoutIndex::cloneLoadout).forEach(toReset::add);
			}
		}
	}
	
	public void resetTempLoadoutAllLevels(BFCountry country, String skin, MatchClass matchClass) {
		resetTempLoadoutAllLevels(new LoadoutIndex.Identifier(country, skin, matchClass));
	}
	
	public void syncTempLoadouts() {
		LoadoutIndex.apply(tempLoadouts);
		reloadLoadouts();
		
		if (!MinecraftClient.getInstance().isInSingleplayer()) {
			AddonConstants.LOGGER.info("Syncing edited loadouts with the server...");
			PacketDistributor.sendToServer(new LoadoutsPacket(tempLoadouts));
		}
	}
}
