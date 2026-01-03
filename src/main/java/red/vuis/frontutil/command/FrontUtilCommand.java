package red.vuis.frontutil.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.boehmod.bflib.cloud.common.CloudRegistry;
import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.CloudItemStack;
import com.boehmod.blockfront.BlockFront;
import com.boehmod.blockfront.assets.AssetStore;
import com.boehmod.blockfront.assets.impl.GameAsset;
import com.boehmod.blockfront.cloud.CloudItemCache;
import com.boehmod.blockfront.common.BFAbstractManager;
import com.boehmod.blockfront.common.match.DivisionData;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.game.AbstractGame;
import com.boehmod.blockfront.game.AbstractGamePlayerManager;
import com.boehmod.blockfront.game.GameStageTimer;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.ITimedStage;
import com.boehmod.blockfront.game.impl.ffa.FreeForAllGame;
import com.boehmod.blockfront.registry.BFDataComponents;
import com.boehmod.blockfront.util.BFRes;
import com.boehmod.blockfront.util.BFUtils;
import com.boehmod.blockfront.util.math.FDSPose;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.command.arg.MatchClassArgumentType;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.net.packet.ViewSpawnsPacket;
import red.vuis.frontutil.setup.GunSkinIndex;
import red.vuis.frontutil.setup.LoadoutIndex;
import red.vuis.frontutil.util.AddonCommandUtils;
import red.vuis.frontutil.util.AddonUtils;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class FrontUtilCommand {
	private FrontUtilCommand() {
	}
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		var root = literal("frontutil").requires(stack -> stack.hasPermissionLevel(3));
		
		root.then(
			literal("changeClass").then(
				argument("players", EntityArgumentType.players()).then(
					argument("class", MatchClassArgumentType.matchClass()).then(
						argument("level", IntegerArgumentType.integer(1)).executes(FrontUtilCommand::changeClass)
					)
				)
			)
		).then(
			literal("gun").then(
				literal("giveWithSkin").then(
					argument("id", IdentifierArgumentType.identifier()).suggests(FrontUtilCommand::suggestGunGiveId).then(
						argument("skin", StringArgumentType.word()).suggests(FrontUtilCommand::suggestGunGiveSkin).executes(FrontUtilCommand::gunGiveWithSkin)
					)
				)
			).then(
				literal("modifier").then(
					literal("list").executes(FrontUtilCommand::gunModifierList)
				)
			)
		).then(
			literal("loadout").then(
				literal("list").executes(FrontUtilCommand::loadoutList)
			).then(
				literal("read").then(
					argument("filename", StringArgumentType.word()).executes(FrontUtilCommand::loadoutRead)
				)
			).then(
				literal("write").then(
					argument("filename", StringArgumentType.word()).executes(FrontUtilCommand::loadoutWrite)
				)
			)
		).then(
			literal("match").then(
				literal("timer").then(
					literal("add").then(
						argument("seconds", IntegerArgumentType.integer(0)).executes(FrontUtilCommand::matchTimerAdd)
					)
				).then(
					literal("set").then(
						argument("seconds", IntegerArgumentType.integer(0)).executes(FrontUtilCommand::matchTimerSet)
					)
				)
			)
		).then(
			literal("randomDrop").then(
				argument("players", EntityArgumentType.players()).executes(context -> randomDrop(context, 1)).then(
					argument("count", IntegerArgumentType.integer(1, 100)).executes(context -> randomDrop(context, IntegerArgumentType.getInteger(context, "count")))
				)
			)
		).then(
			literal("spawnView").then(
				literal("enable").then(
					AddonArguments.asset("game", GameAsset.class).executes(FrontUtilCommand::spawnViewEnable)
				)
			)
		);
		
		dispatcher.register(root);
	}
	
	private static CompletableFuture<Suggestions> suggestGunGiveId(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestions) {
		return CommandSource.suggestMatching(GunSkinIndex.SKINS.keySet().stream().map(Identifier::toString), suggestions);
	}
	
	private static CompletableFuture<Suggestions> suggestGunGiveSkin(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestions) {
		Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
		if (!GunSkinIndex.SKINS.containsKey(id)) {
			return Suggestions.empty();
		}
		return CommandSource.suggestMatching(GunSkinIndex.SKINS.get(id).keySet(), suggestions);
	}
	
	private static int changeClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		
		Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
		MatchClass matchClass = MatchClassArgumentType.getMatchClass(context, "class");
		int level = IntegerArgumentType.getInteger(context, "level") - 1;
		
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		assert manager != null;
		Set<Integer> sentMissingErrors = new HashSet<>();
		
		AssetStore.getInstance().getRegistry(GameAsset.class).getEntries().values().stream().map(GameAsset::getGame).filter(Objects::nonNull).forEach(game -> {
			AbstractGamePlayerManager<?> playerManager = game.getPlayerManager();
			players.stream().filter(player -> playerManager.hasPlayer(player.getUuid())).forEach(player -> {
				GameTeam team = playerManager.getPlayerTeam(player.getUuid());
				if (team == null) {
					return;
				}
				
				DivisionData division = team.getDivisionData(game);
				if (division.getLoadout(matchClass, level) == null && sentMissingErrors.add(division.getId())) {
					source.sendError(Text.translatable(
						"frontutil.message.command.changeClass.error.missing",
						AddonUtils.getMatchClassText(matchClass, level).formatted(Formatting.GOLD),
						AddonUtils.getDivisionText(division).formatted(Formatting.GOLD)
					));
					return;
				}
				
				playerManager.changePlayerClass(manager, player.getServerWorld(), player, player.getUuid(), matchClass, level);
			});
		});
		
		return 1;
	}
	
	private static int gunGiveWithSkin(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return -1;
		}
		
		Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
		String skin = StringArgumentType.getString(context, "skin");
		
		Item item = Registries.ITEM.get(id);
		if (item == Items.AIR) {
			return -1;
		}
		if (!GunSkinIndex.SKINS.get(id).containsKey(skin)) {
			return -1;
		}
		
		ItemStack itemStack = new ItemStack(item);
		itemStack.set(BFDataComponents.SKIN_ID, GunSkinIndex.SKINS.get(id).get(skin));
		
		player.giveItemStack(itemStack);
		player.currentScreenHandler.sendContentUpdates();
		return 1;
	}
	
	private static int gunModifierList(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		CommandOutput output = source.output;
		
		for (RegistryEntry<Item> itemEntry : GunModifier.ACTIVE.keySet()) {
			output.sendMessage(Text.literal(itemEntry.getIdAsString()));
		}
		
		return 1;
	}
	
	private static int loadoutList(CommandContext<ServerCommandSource> context) {
		ServerCommandSource sourceStack = context.getSource();
		
		Path loadoutsPath = AddonUtils.getServerDataPath(sourceStack.getServer()).resolve("loadouts");
		if (!Files.isDirectory(loadoutsPath)) {
			sourceStack.sendError(Text.translatable("frontutil.message.command.loadout.list.none"));
			return 0;
		}
		
		String filenameList;
		try (Stream<Path> filesList = Files.list(loadoutsPath)) {
			filenameList = AddonUtils.listify(
				filesList
					.map(path -> {
						String pathStr = path.getFileName().toString();
						return pathStr.substring(0, pathStr.lastIndexOf('.'));
					})
					.iterator()
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		sourceStack.sendFeedback(() -> Text.translatable("frontutil.message.command.loadout.list.success", filenameList), false);
		return 1;
	}
	
	private static int loadoutRead(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		MinecraftServer server = source.getServer();
		
		if (!server.isDedicated()) {
			source.sendError(Text.translatable("frontutil.message.command.loadout.read.error.server"));
			return -1;
		}
		
		if (AddonUtils.anyGamesActive()) {
			source.sendError(Text.translatable("frontutil.message.command.loadout.read.game"));
			return -1;
		}
		
		String filename = StringArgumentType.getString(context, "filename") + ".dat";
		Path indexPath = AddonUtils.getServerDataPath(server).resolve("loadouts").resolve(filename);
		
		if (!LoadoutIndex.parseAndApply(indexPath)) {
			source.sendError(Text.translatable("frontutil.message.command.loadout.read.error"));
			return -1;
		}
		
		PacketDistributor.sendToAllPlayers(new LoadoutsPacket(LoadoutIndex.currentFlat()));
		
		source.sendFeedback(() -> Text.translatable("frontutil.message.command.loadout.read.success", filename), true);
		return 1;
	}
	
	private static int loadoutWrite(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		MinecraftServer server = source.getServer();
		
		if (!server.isDedicated()) {
			source.sendError(Text.translatable("frontutil.message.command.loadout.write.error.server"));
			return -1;
		}
		
		String filename = StringArgumentType.getString(context, "filename") + ".dat";
		
		Path loadoutsPath = AddonUtils.getServerDataPath(server).resolve("loadouts");
		if (!Files.isDirectory(loadoutsPath)) {
			try {
				Files.createDirectory(loadoutsPath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		Path indexPath = loadoutsPath.resolve(filename);
		
		if (!LoadoutIndex.saveCurrent(indexPath)) {
			source.sendError(Text.translatable("frontutil.message.command.loadout.write.error"));
			return -1;
		}
		
		return 1;
	}
	
	private static int matchTimerAdd(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return -1;
		}
		
		int seconds = IntegerArgumentType.getInteger(context, "seconds");
		
		return handleMatchTimerOperation(
			context, source, player, timer -> timer.setSecondsRemaining(timer.getSecondsRemaining() + seconds),
			Text.translatable("frontutil.message.command.match.timer.add.success", player.getNameForScoreboard(), seconds)
		);
	}
	
	private static int matchTimerSet(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return -1;
		}
		
		int seconds = IntegerArgumentType.getInteger(context, "seconds");
		
		return handleMatchTimerOperation(
			context, source, player, timer -> timer.setSecondsRemaining(seconds),
			Text.translatable("frontutil.message.command.match.timer.set.success", player.getNameForScoreboard(), seconds)
		);
	}
	
	private static int handleMatchTimerOperation(CommandContext<ServerCommandSource> context, ServerCommandSource source, ServerPlayerEntity player, Consumer<GameStageTimer> timerOperation, Text message) {
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		assert manager != null;
		AbstractGame<?, ?, ?> game = manager.getGameWithPlayer(player);
		if (game == null) {
			source.sendError(Text.translatable("frontutil.message.command.match.error.none"));
			return -1;
		}
		
		if (!(game.getStageManager().getCurrentStage() instanceof ITimedStage<?, ?> stage)) {
			source.sendError(Text.translatable("frontutil.message.command.match.timer.error.invalid"));
			return -1;
		}
		
		@SuppressWarnings("DataFlowIssue") // unused parameter
		GameStageTimer timer = stage.getStageTimer(null);
		if (timer == null) {
			return 0;
		}
		timerOperation.accept(timer);
		BFUtils.sendFancyMessage(
			game.getPlayerManager().getPlayerUUIDs(), BFUtils.ADMIN_PREFIX,
			message
		);
		
		return 1;
	}
	
	private static int randomDrop(CommandContext<ServerCommandSource> context, int count) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		
		Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
		
		BFAbstractManager<?, ?, ?> manager = BlockFront.getInstance().getManager();
		assert manager != null;
		CloudRegistry cloudRegistry = manager.getCloudRegistry();
		
		for (ServerPlayerEntity player : players) {
			for (int i = 0; i < count; i++) {
				handleRandomDrop(player, cloudRegistry);
			}
			player.currentScreenHandler.sendContentUpdates();
		}
		
		if (players.size() == 1) {
			source.sendFeedback(() -> Text.translatable("frontutil.message.command.randomDrop.single.success", count, players.iterator().next().getDisplayName()), true);
		} else {
			source.sendFeedback(() -> Text.translatable("frontutil.message.command.randomDrop.multiple.success", count, players.size()), true);
		}
		return players.size();
	}
	
	private static void handleRandomDrop(ServerPlayerEntity player, CloudRegistry cloudRegistry) {
		CloudItemStack dropStack = CloudItem.getRandomDrop(cloudRegistry);
		CloudItem<?> dropItem = dropStack.getCloudItem(cloudRegistry);
		assert dropItem != null;
		
		Item item = Registries.ITEM.get(BFRes.fromCloud(dropItem.getMinecraftItem()));
		if (item == Items.AIR) {
			throw new RuntimeException("Cloud item id %d does not have a valid mc item location!".formatted(dropStack.getItemId()));
		}
		ItemStack itemStack = new ItemStack(item);
		CloudItemCache.method_5941(dropItem, itemStack);
		CloudItemCache.method_5942(dropStack, itemStack);
		
		player.giveItemStack(itemStack);
	}
	
	private static int spawnViewEnable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = AddonCommandUtils.getContextPlayer(context);
		if (player == null) {
			return -1;
		}
		
		GameAsset gameAsset = AddonArguments.getAsset(context, "game", GameAsset.class);
		
		switch (gameAsset.getGame()) {
			case FreeForAllGame game -> {
				List<FDSPose> spawns = game.getPlayerManager().method_3566();
				PacketDistributor.sendToPlayer(player, new ViewSpawnsPacket(game.getName(), spawns));
			}
			case null, default -> {
				source.sendError(Text.translatable("frontutil.message.command.viewSpawns.enable.error.type"));
				return -1;
			}
		}
		
		return 1;
	}
}
