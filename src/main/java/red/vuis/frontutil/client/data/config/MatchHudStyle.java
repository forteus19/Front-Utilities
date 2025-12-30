package red.vuis.frontutil.client.data.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

import com.boehmod.blockfront.game.BFGameType;

@AllArgsConstructor
public enum MatchHudStyle {
	MODERN(Set.of(), false, false, false),
	OLD(Set.of(BFGameType.DOMINATION, BFGameType.GUN_GAME, BFGameType.FREE_FOR_ALL), true, true, true),
	DAY_OF_INFAMY(Set.of(BFGameType.DOMINATION), true, true, true);

	@Getter
	private final Set<BFGameType> disabledGameElementTypes;
	@Getter
	private final boolean oldKillFeed;
	@Getter
	private final boolean oldWaitingText;
	@Getter
	private final boolean oldCapturingText;
}
