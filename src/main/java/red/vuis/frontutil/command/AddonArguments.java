package red.vuis.frontutil.command;

import com.boehmod.blockfront.assets.AssetRegistry;
import com.boehmod.blockfront.assets.AssetStore;
import com.boehmod.blockfront.assets.IAsset;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class AddonArguments {
	private static final Dynamic2CommandExceptionType ERROR_ASSET_NOT_FOUND = new Dynamic2CommandExceptionType(
		(assetName, registryName) -> Text.stringifiedTranslatable("frontutil.message.argument.asset.error.not_found", assetName, registryName)
	);
	
	private AddonArguments() {
	}
	
	public static <T extends IAsset> RequiredArgumentBuilder<ServerCommandSource, String> asset(String argumentName, Class<T> assetClass) {
		return CommandManager.argument(argumentName, StringArgumentType.word()).suggests(suggestAssets(assetClass));
	}
	
	public static <T extends IAsset> SuggestionProvider<ServerCommandSource> suggestAssets(Class<T> assetClass) {
		return (context, suggestions) -> CommandSource.suggestMatching(
			AssetStore.getInstance().getRegistry(assetClass).getEntries().keySet(),
			suggestions
		);
	}
	
	public static <T extends IAsset> T getAsset(CommandContext<ServerCommandSource> context, String argumentName, Class<T> assetClass) throws CommandSyntaxException {
		String assetName = StringArgumentType.getString(context, argumentName);
		AssetRegistry<T> registry = AssetStore.getInstance().getRegistry(assetClass);
		T asset = registry.getByName(assetName);
		if (asset == null) {
			throw ERROR_ASSET_NOT_FOUND.create(assetName, registry.getType());
		}
		return asset;
	}
}
