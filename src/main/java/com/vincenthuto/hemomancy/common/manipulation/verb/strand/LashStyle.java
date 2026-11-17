package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.world.phys.Vec3;

/**
 * What sets one lash apart from another in the hand: how its rope carries, what a charge buys beyond damage, and the
 * one thing it does that no other lash does. Data on the registration, inside the {@link LashProfile}; {@link #PLAIN}
 * is a lash with none of it. Built from {@code PLAIN} with the methods below, one per trait.
 *
 * @param length       multiplies the lash's length
 * @param taut         multiplies how hard the tip is drawn to the aim: above 1 answers the hand at once, below 1 lags
 * @param damping      multiplies the share of its velocity a node keeps: above 1 carries and overshoots
 * @param fade         multiplies how long a still lash takes to dissolve back
 * @param tails        how many tips the lash ends in; a crack or tap lands that many quick blows on a body, not one
 * @param tailShare    the share of the whole blow each tail lands
 * @param rootTicks    how long a full charge's crack holds what it strikes in place; 0 for none
 * @param burst        how much wider a full charge makes the crack, as a share of its radius
 * @param fireTicks    how long a full charge's crack sets what it strikes alight
 * @param pierceCharge the charge from which a throw cuts through every body on its arc and does not stop; 0 for never
 * @param sweepReturn  blood a tick's sweep gives back when it lands
 * @param heat         what each crack in a row adds to the next crack's damage, as a share
 * @param heatStacks   the most cracks in a row that count
 * @param heatRest     ticks without a crack after which the row is over
 * @param ambush       what a blow is multiplied by on a body struck from behind or in the dark
 * @param tapChain     taps in a row that end in a finishing tap (a small crack that lays the tap's state); 0 for none
 * @param faint        whether other players see the lash only faintly
 */
public record LashStyle(double length, double taut, double damping, double fade, int tails, float tailShare,
		int rootTicks, double burst, int fireTicks, double pierceCharge, double sweepReturn, float heat, int heatStacks,
		int heatRest, float ambush, int tapChain, boolean faint) {
	public static final LashStyle PLAIN = new LashStyle(1.0D, 1.0D, 1.0D, 1.0D, 1, 1.0F, 0, 0.0D, 0, 0.0D, 0.0D, 0.0F, 0, 0,
			1.0F, 0, false);
	/** Ticks between one tail's blow and the next. */
	public static final int TAIL_GAP = 2;
	/** Ticks a tap chain waits for its next tap before it starts over. */
	public static final int CHAIN_WINDOW = 25;
	/** What the chain's finishing tap multiplies the tap's damage by. */
	public static final float FINISHER = 1.5F;
	/** Block light at or under which a body stands in the dark. */
	public static final int AMBUSH_LIGHT = 4;
	/** A body is struck from behind from outside this much of its front, either side of where it faces (cosine of 60). */
	public static final double FRONT_DOT = 0.5D;
	/** A charge under this buys none of a full charge's root or fire. */
	public static final double LEAST_CHARGE = 0.25D;

	public LashStyle {
		length = Math.max(0.25D, length);
		taut = Math.max(0.1D, taut);
		damping = Math.max(0.0D, damping);
		fade = Math.max(0.01D, fade);
		tails = Math.max(1, tails);
		tailShare = tails == 1 ? 1.0F : Math.max(0.0F, tailShare);
		ambush = Math.max(1.0F, ambush);
	}

	public LashStyle rope(double length, double taut, double damping, double fade) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle tails(int tails, float tailShare) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle root(int rootTicks) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle burst(double burst, int fireTicks) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle pierce(double pierceCharge) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle sweepReturn(double sweepReturn) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle heat(float heat, int heatStacks, int heatRest) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle ambush(float ambush) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle tapChain(int tapChain) {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, faint);
	}

	public LashStyle hidden() {
		return new LashStyle(length, taut, damping, fade, tails, tailShare, rootTicks, burst, fireTicks, pierceCharge, sweepReturn,
				heat, heatStacks, heatRest, ambush, tapChain, true);
	}

	/** What one tail lands of a blow worth {@code whole}. */
	public float tailDamage(float whole) {
		return whole * tailShare;
	}

	/** A crack's radius for the charge it carries. */
	public double crackRadius(double charge) {
		return LashRules.CRACK_RADIUS * (1.0D + burst * Math.max(0.0D, Math.min(1.0D, charge)));
	}

	/** What a crack is multiplied by after {@code row} cracks in a row before it. */
	public float crackHeat(int row) {
		return 1.0F + heat * Math.max(0, Math.min(heatStacks, row));
	}

	/** Whether a throw carrying {@code charge} cuts through the bodies on its arc. */
	public boolean pierces(double charge) {
		return pierceCharge > 0.0D && charge >= pierceCharge;
	}

	/** Ticks of a full-charge effect ({@code full}) that {@code charge} buys: none under {@link #LEAST_CHARGE}. */
	public static int charged(int full, double charge) {
		return charge < LEAST_CHARGE ? 0 : (int) Math.round(full * Math.min(1.0D, charge));
	}

	/**
	 * Whether a blow from {@code toAttacker} (the way from the body to who struck it) comes from outside the front of a
	 * body facing along {@code facing}. Only the level directions count: above and below are neither front nor back.
	 */
	public static boolean behind(Vec3 facing, Vec3 toAttacker) {
		double fx = facing.x, fz = facing.z, ax = toAttacker.x, az = toAttacker.z;
		double f = Math.sqrt(fx * fx + fz * fz), a = Math.sqrt(ax * ax + az * az);
		if (f < 1.0E-6D || a < 1.0E-6D) return false;
		return (fx * ax + fz * az) / (f * a) < FRONT_DOT;
	}
}
