package red.vuis.frontutil.command.arg;

import java.util.concurrent.CompletableFuture;

import com.boehmod.blockfront.assets.AssetRegistry;
import com.boehmod.blockfront.assets.AssetStore;
import com.boehmod.blockfront.assets.IAsset;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class AssetArgument<T extends IAsset> implements ArgumentType<T> {
	private static final Dynamic2CommandExceptionType ERROR_NOT_FOUND = new Dynamic2CommandExceptionType(
		(assetName, registryName) -> Component.translatableEscape("frontutil.message.argument.asset.error.not_found", assetName, registryName)
	);
	
	private final AssetRegistry<T> registry;
	
	private AssetArgument(AssetRegistry<T> registry) {
		this.registry = registry;
	}
	
	public static <T extends IAsset> AssetArgument<T> asset(AssetRegistry<T> registry) {
		return new AssetArgument<>(registry);
	}
	
	public static <T extends IAsset> AssetArgument<T> asset(Class<T> assetClass) {
		return asset(AssetStore.getInstance().getRegistry(assetClass));
	}
	
	public static <T extends IAsset> T getAsset(CommandContext<CommandSourceStack> context, String argument, Class<T> assetClass) {
		return context.getArgument(argument, assetClass);
	}
	
	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		String assetName = reader.readUnquotedString();
		T asset = registry.getByName(assetName);
		if (asset == null) {
			throw ERROR_NOT_FOUND.create(assetName, registry.getType());
		} else {
			return asset;
		}
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(registry.getEntries().keySet(), builder);
	}
}
