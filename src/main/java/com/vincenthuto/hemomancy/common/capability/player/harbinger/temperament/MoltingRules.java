package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoadRules;

/**
 * Molting (D-5, ruled 2026-11-06): an Apotheos, at their own heart-node in the Gardens, sheds their temper and lets
 * it set again in a new shape. Every point of permanent alignment is added up, a tenth is lost to the shed skin, and
 * the rest is placed by hand across the eight tempers, whole points only. A molt takes a full spore load and leaves
 * the new skin soft for a day. Scar alignment is borrowed, not grown, so it is neither counted nor reshaped.
 */
public final class MoltingRules {
	public static final int TEMPERS = EnumBloodTemper.values().length;
	/** The share of the total that survives a molt. */
	public static final double KEPT = 0.90D;
	public static final float SPORE_COST = SporeLoadRules.MAX;
	public static final long COOLDOWN_TICKS = 24_000L;

	public enum Refusal { NONE, NOT_APOTHEOS, OUTSIDE_GARDENS, SPORES, SOFT, NOTHING, UNPLACED }

	private MoltingRules() {
	}

	/** The points to place after a molt: the permanent total, less the tenth that is shed, in whole points. */
	public static int pool(float[] base) {
		double total = 0.0D;
		for (float value : base) total += value;
		return (int) Math.floor(Math.max(0.0D, total) * KEPT + 1.0E-6D);
	}

	/** The current shape scaled into {@code pool}: where the screen starts. Leftover points go to the largest. */
	public static int[] suggested(float[] base, int pool) {
		int[] out = new int[base.length];
		double positive = 0.0D;
		for (float value : base) positive += Math.max(0.0F, value);
		if (pool <= 0 || positive <= 0.0D) return out;
		int placed = 0;
		int largest = 0;
		for (int i = 0; i < base.length; i++) {
			out[i] = (int) Math.floor(Math.max(0.0F, base[i]) / positive * pool);
			placed += out[i];
			if (base[i] > base[largest]) largest = i;
		}
		out[largest] += pool - placed;
		return out;
	}

	/** Every point placed, none below zero, one value per temper. */
	public static boolean placed(int[] allocation, int pool) {
		if (allocation == null || allocation.length != TEMPERS) return false;
		long sum = 0;
		for (int value : allocation) {
			if (value < 0) return false;
			sum += value;
		}
		return sum == pool;
	}

	public static Refusal refusal(boolean apotheos, boolean inGardens, float spores, long now, long softUntil,
			int pool) {
		if (!apotheos) return Refusal.NOT_APOTHEOS;
		if (!inGardens) return Refusal.OUTSIDE_GARDENS;
		if (pool <= 0) return Refusal.NOTHING;
		if (spores < SPORE_COST) return Refusal.SPORES;
		if (softUntil > now) return Refusal.SOFT;
		return Refusal.NONE;
	}

	public static String messageKey(Refusal refusal) {
		return switch (refusal) {
			case NONE -> "message.hemomancy.molting.done";
			case NOT_APOTHEOS -> "message.hemomancy.molting.not_apotheos";
			case OUTSIDE_GARDENS -> "message.hemomancy.molting.outside_gardens";
			case SPORES -> "message.hemomancy.molting.spores";
			case SOFT -> "message.hemomancy.molting.soft";
			case NOTHING -> "message.hemomancy.molting.nothing";
			case UNPLACED -> "message.hemomancy.molting.unplaced";
		};
	}
}
