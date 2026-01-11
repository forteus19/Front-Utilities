package red.vuis.frontutil.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import red.vuis.frontutil.util.AddonUtils;

public final class AddonCommonData {
	private static AddonCommonData instance = null;
	
	public Map<UUID, PlayerCloudData> profileOverrides = new Object2ObjectOpenHashMap<>();
	
	private AddonCommonData() {
	}
	
	public static AddonCommonData getInstance() {
		if (instance == null) {
			instance = new AddonCommonData();
		}
		return instance;
	}
	
	private void putNewProfileOverride(Pair<UUID, String> idPair) {
		profileOverrides.put(idPair.left(), AddonUtils.createPlayerCloudData(idPair));
	}
	
	public void putNewProfileOverrides(Collection<Pair<UUID, String>> idPairs) {
		idPairs.forEach(this::putNewProfileOverride);
	}
	
	public Map<UUID, ProfileOverrideData> getProfileOverrideData() {
		Map<UUID, ProfileOverrideData> data = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<UUID, PlayerCloudData> entry : profileOverrides.entrySet()) {
			data.put(entry.getKey(), ProfileOverrideData.of(entry.getValue()));
		}
		return data;
	}
}
