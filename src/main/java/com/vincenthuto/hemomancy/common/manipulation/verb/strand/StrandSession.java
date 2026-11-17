package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * One player's open strand: the form's running state for this tick and the last, per-target lockouts, and how strong
 * its last paid pulse was. Transient, held by {@link StrandService} and cleared through
 * {@code TransientServerStateEvents}.
 */
public final class StrandSession {
	final StrandSculpting sculpting;
	final BloodManipulation manipulation;
	final long opened;
	final boolean leftArm;
	/** The off hand pours a twin of this strand (Second Strand, CL-27); fixed when the strand opens. */
	final boolean twin;
	/** The caster's strand skills when it opened (StrandSkills reads what each level is worth). */
	final int longStrand, tautLine, whipcrack, deepBrand;
	/** What the last paid pulse could carry: 1, or less when it ran dry of Temper. */
	float strength = 1.0F;
	/** Entity id to the game time of the strand's last hit on it. */
	final Map<Integer, Long> lastHit = new HashMap<>();
	/** The twin lash's own lockouts: its sweeps never use up the lead's. */
	final Map<Integer, Long> twinLastHit = new HashMap<>();

	/** The server's lash reads no flicks from the look: the caster's client tells it of each ({@code StrandService.flick}). */
	final LashRules.State lash = new LashRules.State(false);
	/** The whip aim the server holds for a lash: the rules' own while it charges or is thrown, the caster's after. */
	float aimYaw, aimPitch;
	/** Lash steps since the caster's client last sent a whip aim; 0 on a step one arrived before. */
	int aimAge = Integer.MAX_VALUE / 2;
	/** The whip aim as the last lash step left it, and the aims taken since: one step's worth of movement is the limit. */
	float aimBaseYaw, aimBasePitch;
	int aimsThisStep;
	/** Ticks the lash has spent gathering, out (thrown or live) and fading since its last paid pulse. */
	int chargingTicks, outTicks, fadingTicks;
	/** What the hit being struck multiplies its state's duration by: a charged crack lays it longer. */
	double stateScale = 1.0D;
	/** Cracks in a row (a lash that builds heat), and the game time of the last. */
	int heat;
	long lastHeatCrack = Long.MIN_VALUE / 2;
	/** Bodies the throw in flight has already cut through (a lash whose charge pierces). */
	final java.util.Set<Integer> pierced = new java.util.HashSet<>();
	/** The twin lash, from the off hand: it trails the lead. */
	final LashRules.State twinLash = new LashRules.State(false);
	final JetRules.State jet = new JetRules.State();
	final BrandRules.State brand = new BrandRules.State();
	BlockPos plantPos;
	Vec3 plantPoint;
	/** The wet stain cell the plant sank into, or null: a plant there runs along the stain instead of ringing. */
	BlockPos plantStain;
	int sunkPulses;

	StrandSession(StrandSculpting sculpting, BloodManipulation manipulation, long opened, boolean leftArm, boolean twin,
			int longStrand, int tautLine, int whipcrack, int deepBrand) {
		this.sculpting = sculpting;
		this.manipulation = manipulation;
		this.opened = opened;
		this.leftArm = leftArm;
		this.twin = twin;
		this.longStrand = longStrand;
		this.tautLine = tautLine;
		this.whipcrack = whipcrack;
		this.deepBrand = deepBrand;
	}

	public boolean twin() {
		return twin;
	}

	public LashRules.State twinLash() {
		return twinLash;
	}

	/** Ticks a brand takes to set on this strand. */
	public int brandSetTicks() {
		return StrandSkills.brandSetTicks(deepBrand);
	}

	public StrandForm form() {
		return sculpting.form();
	}

	public BloodManipulation manipulation() {
		return manipulation;
	}

	public float strength() {
		return strength;
	}

	public double jetPressure() {
		return jet.pressure;
	}

	public double brandProgress() {
		return BrandRules.progress(brand, brandSetTicks());
	}

	public int sunkPulses() {
		return sunkPulses;
	}

	public Vec3 plantPoint() {
		return plantPoint;
	}

	public LashRules.State lash() {
		return lash;
	}

	public float aimYaw() {
		return aimYaw;
	}

	public float aimPitch() {
		return aimPitch;
	}

	/** Whether the strand may hit {@code entityId} now: once per {@link #HIT_LOCKOUT}-tick window. */
	boolean mayHit(int entityId, long now) {
		return mayHit(lastHit, entityId, now);
	}

	void hit(int entityId, long now) {
		hit(lastHit, entityId, now);
	}

	static boolean mayHit(Map<Integer, Long> lockouts, int entityId, long now) {
		Long last = lockouts.get(entityId);
		return last == null || now - last >= HIT_LOCKOUT;
	}

	static void hit(Map<Integer, Long> lockouts, int entityId, long now) {
		lockouts.put(entityId, now);
		if (lockouts.size() > 64) lockouts.values().removeIf(at -> now - at >= HIT_LOCKOUT);
	}

	/** A strand hits one creature at most once in this many ticks: vanilla's own hurt cooldown, so no hit is wasted. */
	public static final int HIT_LOCKOUT = 10;
}
