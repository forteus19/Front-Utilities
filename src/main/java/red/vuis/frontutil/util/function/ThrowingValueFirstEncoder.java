package red.vuis.frontutil.util.function;

@FunctionalInterface
public interface ThrowingValueFirstEncoder<O, T> {
	void encode(T value, O buf) throws Exception;
}
