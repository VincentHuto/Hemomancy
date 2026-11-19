package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * The Ringmaster's bats in his finale (2026-11-18): four roosts of three hang upside down in the copy's rigging from
 * the reveal on. Every {@link #SWOOP_EVERY} ticks two or three from one roost swoop on the fighter, each biting once:
 * a sip of {@link #SIP_ML} mL through {@link BloodAccounting#RINGMASTER_SWARM} when the fighter's blood is active
 * (plain damage when it is not), carried back to him. Only blood that reaches him charges him ({@link #chargeFrom}); a bat
 * killed on the way back brings nothing. Its state lives on the finale. Every number is the builder's starting value.
 */
public final class FinaleSwarm {
	public static final int SIP_ML = 25;
	public static final float DRY_BITE = 3.0F;
	public static final int SWOOP_EVERY = 160, FIRST_SWOOP = 60, ROOSTS = 4, PER_ROOST = 3;
	/** Flight speeds in blocks a tick: the dive on the fighter, the flight back to him with the blood, home to roost. */
	public static final double DIVE = 0.75D, CARRY = 0.55D, HOME = 0.45D;
	/** How close a bat must come to bite, or to hand him its blood. */
	public static final double BITE_REACH = 1.2D, HAND_REACH = 1.4D;
	/** A swoop gives up after this long in the air (the fighter fled through the rigging). */
	public static final int GIVE_UP = 80;

	private enum Flight { ROOST, DIVE, CARRY, HOME }

	private static final class Bat {
		final UUID id;
		final Vec3 roost;
		Flight flight = Flight.ROOST;
		boolean carrying;
		int airborne;

		Bat(UUID id, Vec3 roost) {
			this.id = id;
			this.roost = roost;
		}
	}

	/** One finale's swarm, held on its {@link Finale}. */
	public static final class State {
		private final List<Bat> bats = new ArrayList<>();
		private boolean roosted;
		private int nextSwoop = FIRST_SWOOP;
		int charge;

		public int charge() {
			return charge;
		}
	}

	private FinaleSwarm() {
	}

	/** His charge after one bat's flight: blood that reached him adds a sip's worth, capped; any other adds nothing. */
	public static int chargeFrom(int charge, boolean reachedHim) {
		return reachedHim ? FinaleRules.charge(charge, 1) : charge;
	}

	/** A review or test hook: his charge is set to {@code charge}. */
	public static void setCharge(UUID player, int charge) {
		RingmasterFinaleDirector.finale(player).ifPresent(finale -> finale.swarm().charge = Mth.clamp(charge, 0,
				FinaleRules.CHARGE_MAX));
	}

	/** A review or test hook: the next swoop sets off now. */
	public static void swoopNow(UUID player) {
		RingmasterFinaleDirector.finale(player).ifPresent(finale -> finale.swarm().nextSwoop = 1);
	}

	/** Pays for an empowered move: false (nothing spent) while he holds too little. */
	static boolean spend(Finale finale) {
		State state = finale.swarm();
		if (!FinaleRules.mayEmpower(state.charge)) return false;
		state.charge -= FinaleRules.EMPOWERED_COST;
		return true;
	}

	/** One tick of the swarm, from the reveal on. {@code active} false lets every bat home and hang still (the kneel). */
	static void tick(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity copy, Finale finale, boolean active) {
		State state = finale.swarm();
		if (!state.roosted) {
			state.roosted = true;
			roost(chamber, finale, state);
		}
		if (active && --state.nextSwoop <= 0) {
			state.nextSwoop = SWOOP_EVERY;
			launch(chamber, state, copy.getRandom());
		}
		for (Bat bat : List.copyOf(state.bats)) {
			if (!(chamber.getEntity(bat.id) instanceof VampireBatEntity body) || !body.isAlive()) {
				// Killed: on its way back the blood it carried is lost with it.
				state.bats.remove(bat);
				continue;
			}
			fly(chamber, player, copy, state, bat, body, active);
		}
	}

	/** Four roosts under the quarter poles' collars, three bats hanging from each, facing the ring. */
	private static void roost(ServerLevel chamber, Finale finale, State state) {
		Vec3 floor = finale.floor();
		for (int r = 0; r < ROOSTS; r++) {
			BlockPos cell = FinaleStage.HIGH_PERCHES.get(2 + r);
			Vec3 collar = RingmasterFinaleMoves.perch(finale, cell);
			Vec3 in = new Vec3(floor.x - collar.x, 0.0D, floor.z - collar.z).normalize();
			Vec3 side = new Vec3(-in.z, 0.0D, in.x);
			for (int i = 0; i < PER_ROOST; i++) {
				VampireBatEntity bat = EntityInit.vampire_bat.get().create(chamber);
				if (bat == null) return;
				Vec3 at = collar.add(in.scale(0.9D)).add(side.scale((i - 1) * 0.6D)).add(0.0D, -1.9D, 0.0D);
				bat.moveTo(at.x, at.y, at.z, (float) Math.toDegrees(Math.atan2(-in.x, in.z)), 0.0F);
				bat.markFinaleRoost();
				bat.setResting(true);
				bat.addTag(RingmasterFinaleDirector.COPY_TAG);
				chamber.addFreshEntity(bat);
				state.bats.add(new Bat(bat.getUUID(), at));
			}
		}
	}

	/** Two or three bats of one roost, all hanging, drop and dive. */
	private static void launch(ServerLevel chamber, State state, RandomSource random) {
		List<List<Bat>> roosts = new ArrayList<>();
		for (Bat bat : state.bats) {
			if (bat.flight != Flight.ROOST) continue;
			List<Bat> group = roosts.stream().filter(g -> g.getFirst().roost.distanceToSqr(bat.roost) < 4.0D).findFirst().orElse(null);
			if (group == null) roosts.add(group = new ArrayList<>());
			group.add(bat);
		}
		roosts.removeIf(group -> group.size() < 2);
		if (roosts.isEmpty()) return;
		List<Bat> group = roosts.get(random.nextInt(roosts.size()));
		int count = Math.min(group.size(), 2 + random.nextInt(2));
		for (int i = 0; i < count; i++) {
			Bat bat = group.get(i);
			bat.flight = Flight.DIVE;
			bat.airborne = -4 * i;
		}
		Vec3 at = group.getFirst().roost;
		chamber.playSound(null, at.x, at.y, at.z, SoundEvents.BAT_TAKEOFF, SoundSource.HOSTILE, 2.5F, 0.6F);
	}

	private static void fly(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity copy, State state, Bat bat,
			VampireBatEntity body, boolean active) {
		if (!active && bat.flight == Flight.DIVE) bat.flight = Flight.HOME;
		switch (bat.flight) {
			case ROOST -> {
				body.setResting(true);
				body.setPos(bat.roost);
			}
			case DIVE -> {
				// The bats of one swoop leave a few ticks apart.
				if (++bat.airborne <= 0) {
					body.setPos(bat.roost);
					return;
				}
				body.setResting(false);
				Vec3 target = player.getEyePosition().add(0.0D, -0.3D, 0.0D);
				if (step(body, target, DIVE) <= BITE_REACH) bite(chamber, player, body, bat);
				else if (bat.airborne > GIVE_UP) bat.flight = Flight.HOME;
				if (bat.airborne % 3 == 0) chamber.sendParticles(HemoParticleData.glow(CircusEffects.CLOT), body.getX(), body.getY() + 0.3D,
						body.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D);
			}
			case CARRY -> {
				// Back to him with the blood, a thread of it trailing.
				Vec3 target = copy.position().add(0.0D, copy.getBbHeight() * 0.7D, 0.0D);
				if (step(body, target, CARRY) <= HAND_REACH) {
					if (bat.carrying) {
						state.charge = chargeFrom(state.charge, true);
						BossTelegraphs.bloodCells(chamber, target, CircusEffects.CRIMSON, 16, 0.3D, 0.08D);
						chamber.playSound(null, target.x, target.y, target.z, SoundEvents.HONEY_DRINK, SoundSource.HOSTILE, 1.6F, 0.6F);
					}
					bat.carrying = false;
					bat.flight = Flight.HOME;
				} else if (bat.carrying) {
					chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), body.getX(), body.getY(), body.getZ(), 1,
							0.05D, 0.05D, 0.05D, 0.0D);
				}
			}
			case HOME -> {
				if (step(body, bat.roost, HOME) < 0.2D) {
					body.setPos(bat.roost);
					body.setResting(true);
					bat.flight = Flight.ROOST;
				}
			}
		}
	}

	/** A bite on the fighter: a sip of their blood if it runs, else a plain wound; then back to him. */
	private static void bite(ServerLevel chamber, ServerPlayer player, VampireBatEntity body, Bat bat) {
		body.biteFlourish();
		boolean active = HemoCapabilityAccess.getBloodVolume(player).isActive();
		if (active) {
			bat.carrying = BloodAccounting.pay(player, SIP_ML, BloodAccounting.RINGMASTER_SWARM).paid() > 0.0D;
		} else {
			player.hurt(chamber.damageSources().mobAttack(body), DRY_BITE);
			bat.carrying = false;
		}
		BossTelegraphs.bloodCells(chamber, player.getEyePosition().add(0.0D, -0.4D, 0.0D), CircusEffects.CRIMSON, 10, 0.2D, 0.06D);
		bat.flight = Flight.CARRY;
	}

	/** Moves a bat up to {@code speed} toward {@code target}, facing its way; the distance left. */
	private static double step(VampireBatEntity body, Vec3 target, double speed) {
		Vec3 delta = target.subtract(body.position());
		double distance = delta.length();
		if (distance < 1.0E-4D) return 0.0D;
		Vec3 next = body.position().add(delta.scale(Math.min(1.0D, speed / distance)));
		body.setPos(next);
		float yaw = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
		body.setYRot(yaw);
		body.yBodyRot = yaw;
		body.yHeadRot = yaw;
		return Math.max(0.0D, distance - speed);
	}
}
