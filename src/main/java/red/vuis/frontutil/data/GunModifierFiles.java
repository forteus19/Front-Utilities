package red.vuis.frontutil.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import org.apache.commons.io.file.PathUtils;

import red.vuis.frontutil.AddonConstants;

public final class GunModifierFiles {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path BACKUP_PATH_SUFFIX = Path.of("backup");
	
	private GunModifierFiles() {
	}
	
	public static void saveModifierMap(Path basePath, Map<RegistryEntry<Item>, GunModifier> modifiers) throws IOException {
		Files.createDirectories(basePath);
		
		Path backupPath = basePath.resolve(BACKUP_PATH_SUFFIX);
		
		if (Files.exists(backupPath)) {
			PathUtils.deleteDirectory(backupPath);
		}
		
		if (!PathUtils.isEmptyDirectory(basePath)) {
			Files.createDirectories(backupPath);
			
			try (Stream<Path> pathStream = Files.walk(basePath)) {
				Iterator<Path> fileIterator = pathStream.filter(Files::isRegularFile).iterator();
				while (fileIterator.hasNext()) {
					Path oldModifierPath = fileIterator.next();
					Files.move(oldModifierPath, backupPath.resolve(oldModifierPath.getFileName()));
				}
			}
		}
		
		for (Map.Entry<RegistryEntry<Item>, GunModifier> modifierEntry : modifiers.entrySet()) {
			if (!modifierEntry.getValue().hasData()) {
				continue;
			}
			
			JsonElement serialized = GunModifier.CODEC.encodeStart(JsonOps.INSTANCE, modifierEntry.getValue())
				.resultOrPartial(AddonConstants.LOGGER::error)
				.orElseThrow();
			
			Path modifierPath = basePath.resolve(Path.of(modifierEntry.getKey().getIdAsString().replace(':', '+') + ".json"));
			try (BufferedWriter writer = Files.newBufferedWriter(modifierPath)) {
				GSON.toJson(serialized, writer);
			}
		}
	}
}
