package red.vuis.frontutil.command.bf;

import java.util.List;
import java.util.function.BiFunction;

import com.boehmod.blockfront.assets.AssetCommandBuilder;
import com.boehmod.blockfront.assets.AssetCommandValidators;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.util.BFAdminUtils;
import com.boehmod.blockfront.util.BFStyles;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import red.vuis.frontutil.util.AddonUtils;
import red.vuis.frontutil.util.CommandUtils;

public final class GameCommands {
	private GameCommands() {
	}
	
	public static <T extends AbstractCapturePoint<?>> void capturePointCommands(AssetCommandBuilder base, List<T> capturePoints, BiFunction<Player, String, T> constructor) {
		base.subCommand("list", AddonAssetCommands.genericList(
			"frontutil.message.command.game.cpoint.list.none",
			"frontutil.message.command.game.cpoint.list.header",
			capturePoints,
			InfoFunctions::capturePoint
		));
		
		base.subCommand("remove", AddonAssetCommands.genericRemove(
			"frontutil.message.command.game.cpoint.remove.success",
			capturePoints
		));
		
		base.subCommand("insert", insertCapturePoint(
			capturePoints,
			constructor
		));
		
		base.subCommand("rename", renameCapturePoint(
			capturePoints
		));
		
		base.subCommand("move", moveCapturePoint(
			capturePoints
		));
		
		base.subCommand("autoname", autonameCapturePoints(
			capturePoints
		));
	}
	
	public static <T extends AbstractCapturePoint<?>> AssetCommandBuilder insertCapturePoint(List<T> capturePoints, BiFunction<Player, String, T> constructor) {
		return new AssetCommandBuilder((context, args) -> {
			ServerPlayer player = CommandUtils.getContextPlayer(context);
			if (player == null) {
				return;
			}
			
			var indexParse = AddonAssetCommands.parseIndex(player, args[0], capturePoints, true);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Component indexComponent = indexParse.right();
			String name = args[1];
			
			capturePoints.add(index, constructor.apply(player, name));
			BFAdminUtils.sendBfa(player, Component.translatable("frontutil.message.command.game.cpoint.insert.success", name, indexComponent, capturePoints.size()));
		}).validator(
			AssetCommandValidators.count(new String[]{"index", "name"})
		);
	}
	
	public static <T extends AbstractCapturePoint<?>> AssetCommandBuilder renameCapturePoint(List<T> capturePoints) {
		return new AssetCommandBuilder((context, args) -> {
			CommandSource source = context.getSource().source;
			
			var indexParse = AddonAssetCommands.parseIndex(source, args[0], capturePoints, true);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Component indexComponent = indexParse.right();
			String name = args[1];
			
			capturePoints.get(index).name = name;
			BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.game.cpoint.rename.success", indexComponent, name));
		}).validator(
			AssetCommandValidators.count(new String[]{"index", "name"})
		);
	}
	
	public static <T extends AbstractCapturePoint<?>> AssetCommandBuilder moveCapturePoint(List<T> capturePoints) {
		return new AssetCommandBuilder((context, args) -> {
			ServerPlayer player = CommandUtils.getContextPlayer(context);
			if (player == null) {
				return;
			}
			
			var indexParse = AddonAssetCommands.parseIndex(player, args[0], capturePoints, true);
			if (indexParse == null) {
				return;
			}
			int index = indexParse.leftInt();
			Component indexComponent = indexParse.right();
			
			T capturePoint = capturePoints.get(index);
			Component cpNameComponent = Component.literal(capturePoint.name).withStyle(BFStyles.LIME);
			Component positionComponent = Component.literal(AddonUtils.formatVec3(player.position())).withStyle(BFStyles.LIME);
			
			AddonUtils.setPoseFromEntity(capturePoint, player);
			BFAdminUtils.sendBfa(player, Component.translatable("frontutil.message.command.game.cpoint.move.success", cpNameComponent, indexComponent, positionComponent));
		}).validator(
			AssetCommandValidators.count(new String[]{"index"})
		);
	}
	
	public static <T extends AbstractCapturePoint<?>> AssetCommandBuilder autonameCapturePoints(List<T> capturePoints) {
		return new AssetCommandBuilder((context, args) -> {
			CommandSource source = context.getSource().source;
			
			if (capturePoints.size() > 26) {
				BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.game.cpoint.autoname.error.count"));
				return;
			}
			
			for (int i = 0; i < capturePoints.size(); i++) {
				capturePoints.get(i).name = String.valueOf((char) ('A' + i));
			}
			BFAdminUtils.sendBfa(source, Component.translatable("frontutil.message.command.game.cpoint.autoname.success"));
		});
	}
}
