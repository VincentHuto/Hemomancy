package com.vincenthuto.hemomancy.client.screen.item;

import java.util.ArrayList;
import java.util.List;

/** Deterministic animated light trails used by the mnemonic folio background. */
final class MnemonicFolioWisps {
	private static final int WISP_COUNT = 6;
	private static final int SEGMENTS_PER_WISP = 28;
	private static final int PURPLE = 0xC6A4FF;
	private static final int GOLD = 0xFFE58A;

	private MnemonicFolioWisps() {
	}

	static List<Sample> samples(float timeTicks, int width, int height) {
		if (width <= 0 || height <= 0) return List.of();
		List<Sample> samples = new ArrayList<>(WISP_COUNT * SEGMENTS_PER_WISP);
		for (int wisp = 0; wisp < WISP_COUNT; wisp++) {
			float phase = wisp * 1.873F;
			float baseY = 14.0F + wisp * Math.max(1.0F, (height - 28.0F) / (WISP_COUNT - 1));
			float speed = 0.12F + wisp * 0.017F;
			int rgb = (wisp & 1) == 0 ? PURPLE : GOLD;
			for (int segment = 0; segment < SEGMENTS_PER_WISP; segment++) {
				float along = segment / (float) (SEGMENTS_PER_WISP - 1);
				int x = Mth.floorPositive(Mth.wrap(along * width + timeTicks * speed + phase * 17.0F, width));
				float broadWave = (float) Math.sin(along * Math.PI * (2.1F + wisp * 0.11F)
						+ timeTicks * (0.022F + wisp * 0.0015F) + phase) * (6.0F + wisp % 3 * 1.5F);
				float fineWave = (float) Math.sin(along * Math.PI * 6.0F - timeTicks * 0.014F + phase * 0.7F) * 2.0F;
				int y = Mth.clamp(Math.round(baseY + broadWave + fineWave), 0, height - 1);
				float pulse = 0.5F + 0.5F * (float) Math.sin(timeTicks * 0.035F + phase + along * 4.0F);
				int alpha = 12 + Math.round(18.0F * pulse);
				int radius = 3 + Math.round(2.0F * (float) Math.sin(Math.PI * along));
				samples.add(new Sample(x, y, radius, alpha, rgb));
			}
		}
		return List.copyOf(samples);
	}

	record Sample(int x, int y, int radius, int alpha, int rgb) {
	}

	private static final class Mth {
		private Mth() {
		}

		static float wrap(float value, int modulus) {
			float wrapped = value % modulus;
			return wrapped < 0.0F ? wrapped + modulus : wrapped;
		}

		static int floorPositive(float value) {
			return (int) value;
		}

		static int clamp(int value, int min, int max) {
			return Math.max(min, Math.min(max, value));
		}
	}
}
