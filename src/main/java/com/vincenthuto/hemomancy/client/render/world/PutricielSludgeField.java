package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;

import java.util.ArrayList;
import java.util.List;

/**
 * The frame's live rot sludge pools (revision 2026-11-06, R2.4), so a telegraph drawn over a pool rides its surface:
 * {@link PutricielGroundGeometry} lifts every floor vertex by {@link #lift} and no ground read hides under sludge.
 * Built once a frame from the hazards in range.
 */
final class PutricielSludgeField {
	static final PutricielSludgeField EMPTY = new PutricielSludgeField(List.of());
	/** A ground read stands this far over the surface it rides, and ramps back down to the floor over this much past the rim. */
	static final double CLEARANCE = 0.02D, RAMP = 0.25D;

	/** One pool as it stands this frame. */
	record Live(PutricielSludgeGeometry.Pool pool, PutricielSludgeGeometry.State state, PutricielHazard kind) {
	}

	private final List<Live> pools;

	PutricielSludgeField(List<Live> pools) {
		this.pools = List.copyOf(pools);
	}

	/** Every sludge and burning sludge hazard of Putriciel's in {@code hazards}, at {@code partial}. */
	static PutricielSludgeField of(List<BossGroundHazardEntity> hazards, float partial) {
		List<Live> live = new ArrayList<>();
		for (BossGroundHazardEntity hazard : hazards) {
			PutricielHazard kind = PutricielHazard.byId(hazard.kind());
			if (kind != PutricielHazard.ROT_SLUDGE && kind != PutricielHazard.SLUDGE_FIRE) continue;
			PutricielEntity boss = hazard.owner() instanceof PutricielEntity owner ? owner : null;
			PutricielSludgeGeometry.Pool pool = PutricielSludgeGeometry.Pool.of(hazard);
			PutricielSludgeGeometry.State state = PutricielSludgeGeometry.state(hazard, kind, AnimationTime.age(hazard, partial), pool, boss);
			if (state != null && state.look().visible()) live.add(new Live(pool, state, kind));
		}
		return live.isEmpty() ? EMPTY : new PutricielSludgeField(live);
	}

	List<Live> pools() {
		return pools;
	}

	boolean isEmpty() {
		return pools.isEmpty();
	}

	/**
	 * How far a floor read at ({@code x}, {@code z}) is lifted so it rides the pools: the surface plus a clearance inside
	 * a pool, easing back down to the floor just past its rim; 0 away from every pool.
	 */
	double lift(double x, double z) {
		double best = 0.0D;
		for (Live live : pools) {
			PutricielSludgeGeometry.Pool pool = live.pool();
			double rho = pool.rho(x, z, live.state().look().spread());
			if (rho >= 1.0D + RAMP) continue;
			double surface;
			if (rho < 1.0D) {
				surface = PutricielSludgeGeometry.height(pool, live.state(), x, z);
			} else {
				// past the rim: from the rim's lip height down to the floor, so a quad across the rim never dips into it
				double lip = PutricielBloomRules.sludgeProfile(0.9D) * live.state().look().thick();
				surface = lip * (1.0D - (rho - 1.0D) / RAMP);
			}
			if (surface > 0.004D) best = Math.max(best, surface + CLEARANCE);
		}
		return best;
	}

	/** The height of the sludge's own surface over the floor at a point, 0 off every pool. */
	double height(double x, double z) {
		double best = 0.0D;
		for (Live live : pools) best = Math.max(best, PutricielSludgeGeometry.height(live.pool(), live.state(), x, z));
		return best;
	}
}
