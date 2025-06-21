package red.vuis.frontutil.client.data;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.LoadoutIndex;

public final class AddonClientData {
	private static AddonClientData instance = null;
	
	private Map<LoadoutIndex.Identifier, List<Loadout>> tempLoadouts;
	
	private AddonClientData() {
		reloadLoadouts();
	}
	
	public static AddonClientData getInstance() {
		if (FMLEnvironment.dist != Dist.CLIENT) {
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
	
	public @Nullable List<Loadout> getTempLoadouts(BFCountry country, String skin, MatchClass matchClass) {
		return tempLoadouts.get(new LoadoutIndex.Identifier(country, skin, matchClass));
	}
	
	public void setTempLoadout(BFCountry country, String skin, MatchClass matchClass, int level, Loadout loadout) {
		LoadoutIndex.Identifier baseId = new LoadoutIndex.Identifier(country, skin, matchClass);
		tempLoadouts.get(baseId).set(level, loadout);
	}
	
	public void syncTempLoadouts() {
		LoadoutIndex.apply(tempLoadouts);
		
		if (!Minecraft.getInstance().isLocalServer()) {
			FrontUtil.LOGGER.info("Syncing edited loadouts with the server...");
			PacketDistributor.sendToServer(new LoadoutsPacket(tempLoadouts));
		}
	}
}
