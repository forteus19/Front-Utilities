package red.vuis.frontutil.util;

import java.util.Optional;
import java.util.function.Function;

public final class AddonUtils {
	private AddonUtils() {
	}

	public static <T> Optional<T> parse(Function<String, ? extends T> parser, String arg) {
		try {
			return Optional.ofNullable(parser.apply(arg));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static boolean anyEmpty(Optional<?>... optionals) {
		for (Optional<?> optional : optionals) {
			if (optional.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
