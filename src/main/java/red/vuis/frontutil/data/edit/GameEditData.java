package red.vuis.frontutil.data.edit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.assets.impl.GameAsset;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllGame;
import com.boehmod.blockfront.game.impl.inf.InfectedGame;
import com.boehmod.blockfront.game.impl.ttt.TroubleTownGame;
import com.boehmod.blockfront.util.math.FDSPose;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import red.vuis.frontutil.data.AddonPacketCodecs;

@AllArgsConstructor
public class GameEditData {
	private static final PacketCodec<ByteBuf, Map<String, List<FDSPose>>> TEAM_SPAWNS_CODEC = PacketCodecs.map(
		Object2ObjectOpenHashMap::new,
		PacketCodecs.STRING,
		PacketCodecs.collection(
			ObjectArrayList::new,
			AddonPacketCodecs.FDS_POSE
		)
	);
	
	public static final PacketCodec<ByteBuf, GameEditData> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.BOOL, GameEditData::isInRotation,
		TEAM_SPAWNS_CODEC, GameEditData::getTeamSpawns,
		GameEditData::new
	);
	
	@Getter @Setter
	private boolean inRotation;
	@Getter
	private final Map<String, List<FDSPose>> teamSpawns;
	
	public static GameEditData of(GameAsset gameAsset) {
		AbstractGame<?, ?, ?> game = gameAsset.getGame();
		if (game == null) {
			throw new NullPointerException("No game associated with game asset");
		}
		
		Map<String, List<FDSPose>> teamSpawns = new Object2ObjectOpenHashMap<>();

		switch (game) {
			case FreeForAllGame ffaGame -> {
				teamSpawns.put("Allies", new ObjectArrayList<>(ffaGame.getPlayerManager().method_3566()));
			}
			case InfectedGame infGame -> {
				teamSpawns.put("Survivors", new ObjectArrayList<>(infGame.getPlayerManager().method_3676()));
			}
			case TroubleTownGame tttGame -> {
				teamSpawns.put("Allies", new ObjectArrayList<>(tttGame.getPlayerManager().playerSpawns));
			}
			default -> {
				for (GameTeam team : game.getPlayerManager().getTeams()) {
					teamSpawns.put(team.getName(), new ObjectArrayList<>(team.getPlayerSpawns()));
				}
			}
		}
		
		return new GameEditData(
			gameAsset.isInRotation(),
			teamSpawns
		);
	}
}
