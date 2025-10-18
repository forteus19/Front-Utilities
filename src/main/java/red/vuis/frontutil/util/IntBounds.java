package red.vuis.frontutil.util;

public class IntBounds {
	public static final IntBounds ALL = new IntBounds(Integer.MIN_VALUE, Integer.MAX_VALUE);
	
	public final int min, max;
	
	private IntBounds(int min, int max) {
		if (max < min) {
			throw new IllegalArgumentException("Invalid bounds");
		}
		this.min = min;
		this.max = max;
	}
	
	public static IntBounds intAll() {
		return ALL;
	}
	
	public static IntBounds intBounds(int min, int max) {
		return new IntBounds(min, max);
	}
	
	public static IntBounds intMin(int min) {
		return new IntBounds(min, Integer.MAX_VALUE);
	}
	
	public static IntBounds intMax(int max) {
		return new IntBounds(Integer.MIN_VALUE, max);
	}
	
	public static IntBounds intEquals(int value) {
		return new IntBounds(value, value);
	}
	
	public boolean within(int value) {
		return value >= min && value <= max;
	}
	
	public int clamp(int value) {
		return Math.max(min, Math.min(max, value));
	}
}
