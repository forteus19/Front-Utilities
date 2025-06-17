package red.vuis.frontutil.data;

import java.util.Arrays;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.MatchClass;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class AddonCodecs {
	public static final Codec<MatchClass> MATCH_CLASS = stringKey(MatchClass.values(), MatchClass::getKey, key -> "Invalid match class: " + key);
	
	private AddonCodecs() {
	}
	
	public static <T> Codec<T> stringKey(T[] values, Function<T, String> keyFunc, Function<String, String> errorMsg) {
		return Codec.STRING.comapFlatMap(
			str -> Arrays.stream(values)
				.filter(value -> keyFunc.apply(value).equals(str))
				.findFirst()
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> errorMsg.apply(str))),
			keyFunc
		);
	}
}
