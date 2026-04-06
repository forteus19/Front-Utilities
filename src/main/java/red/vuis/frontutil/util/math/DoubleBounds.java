package red.vuis.frontutil.util.math;

public class DoubleBounds {
	private static final DoubleBounds ALL = new DoubleBounds(Double.MIN_VALUE, Double.MAX_VALUE);
	
	public final double min, max;
	
	private DoubleBounds(double min, double max) {
		if (max < min) {
			throw new IllegalArgumentException("Invalid bounds");
		}
		this.min = min;
		this.max = max;
	}
	
	public static DoubleBounds all() {
		return ALL;
	}
	
	public static DoubleBounds of(double min, double max) {
		return new DoubleBounds(min, max);
	}
	
	public static DoubleBounds ofMin(double min) {
		return new DoubleBounds(min, Double.MAX_VALUE);
	}
	
	public static DoubleBounds ofMax(double max) {
		return new DoubleBounds(Double.MIN_VALUE, max);
	}
	
	public static DoubleBounds ofOnly(double value) {
		return new DoubleBounds(value, value);
	}
	
	public boolean within(double value) {
		return value >= min && value <= max;
	}
	
	public double clamp(double value) {
		return Math.clamp(value, min, max);
	}
}
