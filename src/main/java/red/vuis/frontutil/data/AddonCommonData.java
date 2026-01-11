package red.vuis.frontutil.data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
	
	public void putNewProfileOverrides(Set<UUID> uuidStream) {
		uuidStream.forEach(uuid -> profileOverrides.put(uuid, new PlayerCloudData(uuid)));
	}
	
	public Map<UUID, ProfileOverrideData> getProfileOverrideData() {
		Map<UUID, ProfileOverrideData> data = new Object2ObjectOpenHashMap<>();
		for (Map.Entry<UUID, PlayerCloudData> entry : profileOverrides.entrySet()) {
			data.put(entry.getKey(), ProfileOverrideData.of(entry.getValue()));
		}
		return data;
	}
}
