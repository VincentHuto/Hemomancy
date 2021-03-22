package com.huto.hemomancy.particle;

import java.math.BigDecimal;
import java.util.List;

public class ParticleColor {

	private final float r;
	private final float g;
	private final float b;
	private final int color;

	public ParticleColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.color = (r << 16) | (g << 8) | b;
	}

	public ParticleColor(float r, float g, float b) {
		this((int) r, (int) g, (int) b);
	}

	public static ParticleColor averageFromList(List<ParticleColor> list) {
		float runningR = 0, runningG = 0, runningB = 0, count = 0;

		for (ParticleColor color : list) {
			count++;
			runningR += color.getRed();
			runningG += color.getGreen();
			runningB += color.getBlue();
		}
		float avgR = runningR / count;
		float avgG = runningG / count;
		float avgB = runningB / count;
		return new ParticleColor(avgR, avgG, avgB);
	}

	public static ParticleColor averageColors(ParticleColor... colors) {
		float runningR = 0, runningG = 0, runningB = 0, count = 0;
		for (ParticleColor color : colors) {
			count++;
			runningR += color.getRed();
			runningG += color.getGreen();
			runningB += color.getBlue();
		}
		float avgR = runningR / count;
		float avgG = runningG / count;
		float avgB = runningB / count;
		return new ParticleColor(avgR, avgG, avgB);

	}

	public float getRed() {
		return r;
	}

	public float getGreen() {
		return g;
	}

	public float getBlue() {
		return b;
	}

	public int getColor() {
		return color;
	}

	public String serialize() {
		return "" + this.r + "," + this.g + "," + this.b;
	}

	@Override
	public String toString() {
		return this.r + "," + this.g + "," + this.b;

	}

	public static ParticleColor deserialize(String string) {
		String[] arr = string.split(",");
		BigDecimal d = new BigDecimal(arr[0].trim());
		BigDecimal d1 = new BigDecimal(arr[1].trim());
		BigDecimal d2 = new BigDecimal(arr[2].trim());
		BigDecimal d4 = d.setScale(0, BigDecimal.ROUND_DOWN);
		BigDecimal d5 = d1.setScale(0, BigDecimal.ROUND_DOWN);
		BigDecimal d6 = d2.setScale(0, BigDecimal.ROUND_DOWN);
		return new ParticleColor(d4.intValue(), d5.intValue(), d6.intValue());
	}

	public static class IntWrapper {
		public int r;
		public int g;
		public int b;

		public IntWrapper(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public ParticleColor toParticleColor() {
			return new ParticleColor(r, g, b);
		}

		public String serialize() {
			return "" + this.r + "," + this.g + "," + this.b;
		}

		public void makeVisible() {
			if (r + g + b < 20) {
				b += 10;
				g += 10;
				r += 10;
			}
		}

		public static ParticleColor.IntWrapper deserialize(String string) {
			String[] arr = string.split(",");
			BigDecimal d = new BigDecimal(arr[0].trim());
			BigDecimal d1 = new BigDecimal(arr[1].trim());
			BigDecimal d2 = new BigDecimal(arr[2].trim());
			BigDecimal d4 = d.setScale(0, BigDecimal.ROUND_DOWN);
			BigDecimal d5 = d1.setScale(0, BigDecimal.ROUND_DOWN);
			BigDecimal d6 = d2.setScale(0, BigDecimal.ROUND_DOWN);
			return new ParticleColor.IntWrapper(d4.intValue(), d5.intValue(), d6.intValue());
		}
	}
}