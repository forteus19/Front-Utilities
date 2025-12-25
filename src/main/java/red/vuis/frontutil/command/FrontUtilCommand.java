package red.vuis.frontutil.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.boehmod.blockfront.registry.BFDataComponents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
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
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
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
}
