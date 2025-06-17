package red.vuis.frontutil.data;

import java.util.Optional;

import com.boehmod.blockfront.common.match.MatchClass;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LoadoutModifier(Mode mode, MatchClass matchClass, Optional<Integer> level) {
	public static final Codec<LoadoutModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Mode.CODEC.fieldOf("mode").forGetter(LoadoutModifier::mode),
			AddonCodecs.MATCH_CLASS.fieldOf("match_class").forGetter(LoadoutModifier::matchClass),
			Codec.INT.optionalFieldOf("level").forGetter(LoadoutModifier::level)
		).apply(instance, LoadoutModifier::new)
	);
	
	public enum Mode {
		APPEND("append"),
		MODIFY("modify");
		
		public static final Codec<Mode> CODEC = AddonCodecs.stringKey(Mode.values(), Mode::getKey, key -> "Invalid mode: " + key);
		
		private final String key;
		
		Mode(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}
	}
}
