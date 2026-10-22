package com.vincenthuto.hemomancy.client.render.world.floor;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.RingOpeningMath;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState.RiteEvent;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * How a consecrated floor answers the rite held on it, read each frame from the rite's live snapshot and its recent
 * beats: shocks, pulses and moods for the membrane shader, glow along the trench, and the tear of a failed rite. Slow
 * moods (quickening, bruising, swelling) are eased per Focus so they never jump when a rite starts or ends. Timings
 * and curves live in {@link CardinalRiteFloorVisualRules}.
 */
public final class FloorRiteResponse {
	public static final int WOUNDS = 4;
	private static final int MEMORY_LIMIT = 128;
	/** Two wounds closer than this, in radians, are one. */
	private static final float WOUND_MERGE = 0.35F;
	/** An offering's inward pulse starts this far inside the brazier, on the trench's inner bank. */
	private static final float DRAW_INSET = 0.6F;
	private static final Map<Long, FloorRiteResponse> MEMORY = new HashMap<>();

	/** Forgets eased per-Focus moods; called on logout and level change. */
	public static void clear() {
		MEMORY.clear();
	}

	// Eased moods.
	private double lastTime = Double.NaN;
	private float quick;
	private float intense;
	private float vigor;
	private float bruise;
	private float swell;
	private final float[] woundAngles = new float[WOUNDS];
	private final float[] woundLevels = new float[WOUNDS];

	// This frame's values.
	final float[] rings = new float[4];
	final float[] surge = new float[4];
	final float[] draw = new float[4];
	final float[] mood = new float[4];
	final float[] swelling = new float[4];
	final float[] wounds = new float[4];
	final float[] woundStrengths = new float[4];
	final float[] blood = new float[4];
	private float tearTicks = -1.0F;
	private final List<float[]> offerings = new ArrayList<>();

	private FloorRiteResponse() {
	}

	/** The response at a Focus this frame. {@code reach} is how far its membrane spreads from the Focus centre. */
	public static FloorRiteResponse at(BlockPos origin, float reach, float partialTick, double now) {
		FloorRiteResponse response = MEMORY.get(origin.asLong());
		if (response == null) {
			if (MEMORY.size() > MEMORY_LIMIT) MEMORY.clear();
			response = new FloorRiteResponse();
			MEMORY.put(origin.asLong(), response);
		}
		response.update(origin, reach, partialTick, now);
		return response;
	}

	/** Whether a grounding rite's ring is running out past this Focus's floor, so its bounds must reach further. */
	public static boolean groundRingLive(BlockPos origin) {
		RiteEvent completed = CardinalRiteEventClientState.latest(origin, CardinalRiteEventKind.COMPLETED);
		return completed != null && "grounding".equals(completed.style())
				&& completed.age(0.0F) < CardinalRiteFloorVisualRules.GROUND_RING_TICKS;
	}

	/** Weight of the quickened heartbeat, 0 at rest to 1 during a rite. */
	public float quick() {
		return quick;
	}

	/** How far the quickened heartbeat leans toward racing, for the most demanding phases. */
	public float intense() {
		return intense;
	}

	/** Ticks since a collapse or break began tearing the membrane, or -1 when it is whole. */
	public float tearTicks() {
		return tearTicks;
	}

	public float groundRingRadius() {
		return rings[2];
	}

	public float groundRingStrength() {
		return rings[3];
	}

	/** The world stage's creep radius from the Focus centre this frame, or 0 when it has none. */
	public float creepRadius() {
		return blood[3] > 0.01F ? blood[2] : 0.0F;
	}

	/**
	 * The rite's glow along the trench, per centre-line sample of the mesh: each drawn offering lights the stretch
	 * beside its brazier, and the staff's shock and a grounding ring light it as they cross. Null when nothing does.
	 */
	public float[] trenchGlow(FloorMesh mesh) {
		int samples = mesh.samples();
		boolean shock = rings[1] > 0.01F;
		boolean ground = rings[3] > 0.01F;
		if (samples == 0 || offerings.isEmpty() && !shock && !ground) return null;
		float[][] centre = mesh.loop().centre();
		float length = mesh.loopLength();
		float[] glow = new float[samples + 1];
		for (float[] offering : offerings) {
			// The sample nearest the brazier the offering was drawn from.
			int nearest = 0;
			float best = Float.MAX_VALUE;
			for (int i = 0; i < samples; i++) {
				float dx = centre[i][0] - offering[0];
				float dz = centre[i][1] - offering[1];
				float distance = dx * dx + dz * dz;
				if (distance < best) {
					best = distance;
					nearest = i;
				}
			}
			float from = mesh.arc(nearest);
			for (int i = 0; i < samples; i++) {
				float gap = Math.abs(mesh.arc(i) - from);
				gap = Math.min(gap, length - gap);
				glow[i] = Math.max(glow[i], CardinalRiteFloorVisualRules.offeringTrenchGlow(offering[2], gap));
			}
		}
		for (int i = 0; i < samples; i++) {
			float radius = mesh.sampleRadius(i);
			if (shock) {
				float band = (radius - rings[0]) / 0.7F;
				glow[i] = Math.max(glow[i], 0.8F * rings[1] * (float) Math.exp(-band * band));
			}
			if (ground) {
				float band = (radius - rings[2]) / 0.9F;
				glow[i] = Math.max(glow[i], rings[3] * (float) Math.exp(-band * band));
			}
		}
		glow[samples] = glow[0];
		return glow;
	}

	private void update(BlockPos origin, float reach, float partialTick, double now) {
		float dt = Double.isNaN(lastTime) ? 0.0F : (float) Math.max(0.0D, Math.min(10.0D, now - lastTime));
		lastTime = now;
		double cx = origin.getX() + 0.5D;
		double cz = origin.getZ() + 0.5D;
		ActiveRiteData.RiteEntry entry = liveRite(origin);
		java.util.Arrays.fill(rings, 0.0F);
		java.util.Arrays.fill(surge, 0.0F);
		java.util.Arrays.fill(draw, 0.0F);
		java.util.Arrays.fill(mood, 0.0F);
		java.util.Arrays.fill(swelling, 0.0F);
		offerings.clear();

		// The heart quickens while a rite is held here, and races through its most demanding phases.
		int tempo = entry == null ? CardinalRiteFloorVisualRules.TEMPO_IDLE
				: CardinalRiteFloorVisualRules.riteTempo(entry.getPhase());
		quick = ease(quick, tempo >= CardinalRiteFloorVisualRules.TEMPO_ACTIVE ? 1.0F : 0.0F, dt, 10.0F);
		intense = ease(intense, tempo >= CardinalRiteFloorVisualRules.TEMPO_INTENSE ? 1.0F : 0.0F, dt, 10.0F);
		vigor = ease(vigor, CardinalRiteFloorVisualRules.tempoVigor(tempo), dt, 8.0F);

		// The staff strikes, or the rite pushes the world a step further: one hard shock out from the Focus.
		// ... or a ring tears open, harder the deeper it lies.
		RiteEvent woke = CardinalRiteEventClientState.latest(origin, CardinalRiteEventKind.RING_OPENED);
		float wokeAge = woke == null ? -1.0F : woke.age(partialTick);
		float impact = youngest(youngest(
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.STAFF_IMPACT, partialTick),
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.ESCALATION, partialTick)), wokeAge);
		if (impact >= 0.0F) {
			boolean ring = wokeAge >= 0.0F && impact == wokeAge;
			float depth = ring ? RingOpeningMath.depth(woke.index(), RingTear.ringsOf(woke.style())) : 0.0F;
			rings[0] = CardinalRiteFloorVisualRules.shockRadius(impact,
					ring ? reach * RingOpeningMath.shockReach(depth) : reach);
			rings[1] = CardinalRiteFloorVisualRules.shockStrength(impact)
					* (ring ? RingOpeningMath.shockScale(depth) : 1.0F);
		}

		// An anchor fills: blood runs out along the floor toward it.
		RiteEvent filled = CardinalRiteEventClientState.latest(origin, CardinalRiteEventKind.ANCHOR_FILLED);
		if (filled != null && !filled.points().isEmpty()) {
			Vec3 anchor = filled.points().getFirst();
			float age = filled.age(partialTick);
			float distance = (float) Math.hypot(anchor.x - cx, anchor.z - cz);
			surge[0] = (float) Math.atan2(anchor.z - cz, anchor.x - cx);
			surge[1] = CardinalRiteFloorVisualRules.surgeRadius(age, Math.max(distance, 1.0F));
			surge[2] = CardinalRiteFloorVisualRules.surgeStrength(age);
		}

		// Offerings are drawn: each lights the trench beside its brazier, and the newest runs in to the Focus.
		RiteEvent newestOffering = null;
		for (RiteEvent event : CardinalRiteEventClientState.recent(origin)) {
			if (event.kind() != CardinalRiteEventKind.OFFERING_DRAWN || event.points().isEmpty()) continue;
			float age = event.age(partialTick);
			if (age >= CardinalRiteFloorVisualRules.OFFERING_TICKS + 4.0F) continue;
			Vec3 brazier = event.points().getFirst();
			offerings.add(new float[] {(float) (brazier.x - origin.getX()), (float) (brazier.z - origin.getZ()), age});
			newestOffering = event;
		}
		if (newestOffering != null) {
			Vec3 brazier = newestOffering.points().getFirst();
			float age = newestOffering.age(partialTick);
			float distance = (float) Math.hypot(brazier.x - cx, brazier.z - cz);
			draw[0] = (float) Math.atan2(brazier.z - cz, brazier.x - cx);
			draw[1] = CardinalRiteFloorVisualRules.offeringDrawRadius(age, Math.max(0.5F, distance - DRAW_INSET));
			draw[2] = CardinalRiteFloorVisualRules.offeringDrawStrength(age);
			surge[3] = CardinalRiteFloorVisualRules.offeringFocusFlare(age);
		}

		// A wave arrives: the membrane blanches and a contraction runs in. Answering it brings a flush of relief.
		float wave = CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.WAVE_START, partialTick);
		if (wave >= 0.0F) {
			mood[2] = CardinalRiteFloorVisualRules.flinchBlanch(wave);
			draw[3] = CardinalRiteFloorVisualRules.flinchRippleRadius(wave, reach);
			swelling[2] = CardinalRiteFloorVisualRules.flinchRippleStrength(wave);
		}
		float cleared = CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.WAVE_CLEARED, partialTick);
		if (cleared >= 0.0F) mood[3] = CardinalRiteFloorVisualRules.reliefFlush(cleared);

		// Culmination swells the whole membrane; completion flushes it and lets it settle.
		float swellTarget = 0.0F;
		if (entry != null && "CULMINATION".equals(entry.getPhase())) {
			swellTarget = CardinalRiteFloorVisualRules.culminationSwell(entry.phaseRenderTicks(partialTick));
		}
		RiteEvent completed = CardinalRiteEventClientState.latest(origin, CardinalRiteEventKind.COMPLETED);
		if (completed != null) {
			float age = completed.age(partialTick);
			mood[3] = Math.max(mood[3], CardinalRiteFloorVisualRules.completionFlush(age));
			if (entry == null) swellTarget = Math.max(swellTarget, CardinalRiteFloorVisualRules.completionSwell(age));
			if ("grounding".equals(completed.style())) {
				// A grounding rite sends its pulse on out through the floor into the ground around it.
				rings[2] = CardinalRiteFloorVisualRules.groundRingRadius(age, reach);
				rings[3] = CardinalRiteFloorVisualRules.groundRingStrength(age);
			}
		}
		swell = ease(swell, swellTarget, dt, 5.0F);
		swelling[0] = swell;

		// A cancelled rite dims the membrane gently.
		float cancelled = CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.CANCELLED, partialTick);
		if (cancelled >= 0.0F) swelling[1] = CardinalRiteFloorVisualRules.cancelDim(cancelled);

		// A collapse or break tears the membrane back toward the Focus, bruised, before it regrows.
		float collapsed = youngest(
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.COLLAPSED, partialTick),
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.BROKEN, partialTick));
		tearTicks = CardinalRiteFloorVisualRules.tearing(collapsed) ? collapsed : -1.0F;

		// Instability bruises the whole membrane; damaged anchors bruise it toward themselves.
		float bruiseTarget = entry == null ? 0.0F : CardinalRiteFloorVisualRules.instabilityBruise(entry.getInstability());
		bruise = ease(bruise, bruiseTarget, dt, bruiseTarget > bruise ? 8.0F : 20.0F);
		mood[0] = vigor;
		mood[1] = Math.max(bruise, CardinalRiteFloorVisualRules.tearBruise(collapsed));
		updateWounds(origin, entry, partialTick, dt, cx, cz);
		// The world stage: blood rising in the trench, spilling over late on, and the creep out over the ground.
		System.arraycopy(com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.bloodUniform(
				com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.at(origin), partialTick),
				0, blood, 0, 4);
	}

	private void updateWounds(BlockPos origin, ActiveRiteData.RiteEntry entry, float partialTick, float dt,
			double cx, double cz) {
		List<float[]> targets = new ArrayList<>();
		if (entry != null) {
			for (ActiveRiteData.SanguineBlob blob : entry.getSanguineBlobs()) {
				if (blob.kind() != ActiveRiteData.NodeKind.BOUNDARY_ANCHOR) continue;
				float level = CardinalRiteFloorVisualRules.anchorWound(blob.integrity());
				if (level > 0.02F) addWound(targets, (float) Math.atan2(blob.z() - cz, blob.x() - cx), level);
			}
		}
		for (RiteEvent event : CardinalRiteEventClientState.recent(origin)) {
			if (event.kind() != CardinalRiteEventKind.ANCHOR_BROKEN || event.points().isEmpty()) continue;
			RiteEvent repaired = latestRepair(origin, event.index());
			if (repaired != null && repaired.receivedTick() >= event.receivedTick()) continue;
			Vec3 anchor = event.points().getFirst();
			float level = CardinalRiteFloorVisualRules.brokenWound(event.age(partialTick));
			if (level > 0.02F) addWound(targets, (float) Math.atan2(anchor.z - cz, anchor.x - cx), level);
		}
		// Each slot eases toward the wound at its angle; a new wound takes the weakest slot.
		boolean[] claimed = new boolean[WOUNDS];
		targets.sort((a, b) -> Float.compare(b[1], a[1]));
		for (float[] target : targets) {
			int slot = -1;
			for (int i = 0; i < WOUNDS; i++) {
				if (!claimed[i] && woundLevels[i] > 0.01F && angleGap(woundAngles[i], target[0]) < WOUND_MERGE) {
					slot = i;
					break;
				}
			}
			if (slot < 0) {
				for (int i = 0; i < WOUNDS; i++) {
					if (claimed[i]) continue;
					if (slot < 0 || woundLevels[i] < woundLevels[slot]) slot = i;
				}
				if (slot < 0) break;
				if (woundLevels[slot] > target[1]) continue;
				woundAngles[slot] = target[0];
			}
			claimed[slot] = true;
			woundLevels[slot] = ease(woundLevels[slot], target[1], dt, 6.0F);
		}
		for (int i = 0; i < WOUNDS; i++) {
			if (!claimed[i]) woundLevels[i] = ease(woundLevels[i], 0.0F, dt, 20.0F);
			wounds[i] = woundAngles[i];
			woundStrengths[i] = woundLevels[i];
		}
	}

	private static void addWound(List<float[]> wounds, float angle, float level) {
		for (float[] wound : wounds) {
			if (angleGap(wound[0], angle) < WOUND_MERGE) {
				wound[1] = Math.max(wound[1], level);
				return;
			}
		}
		wounds.add(new float[] {angle, level});
	}

	private static RiteEvent latestRepair(BlockPos origin, int index) {
		RiteEvent newest = null;
		for (RiteEvent event : CardinalRiteEventClientState.recent(origin)) {
			if (event.kind() == CardinalRiteEventKind.ANCHOR_REPAIRED && event.index() == index) newest = event;
		}
		return newest;
	}

	private static ActiveRiteData.RiteEntry liveRite(BlockPos origin) {
		for (ActiveRiteData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!entry.isUnstained() && origin.equals(entry.getCenter())) return entry;
		}
		return null;
	}

	private static float youngest(float a, float b) {
		if (a < 0.0F) return b;
		if (b < 0.0F) return a;
		return Math.min(a, b);
	}

	private static float angleGap(float a, float b) {
		return (float) Math.abs(Math.IEEEremainder(a - b, Math.PI * 2.0D));
	}

	/** Moves a value toward its target with a time constant of {@code ticks}, frame-rate independent. */
	private static float ease(float value, float target, float dt, float ticks) {
		return value + (target - value) * (1.0F - (float) Math.exp(-dt / ticks));
	}
}
