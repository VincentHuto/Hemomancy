package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Routes the ordinary Blood Absorption channel into the Ringmaster at his kneel (2026-11-18), as Vesper's is routed into
 * the Evening Star: a fit owner draws his blood off and, when it is all drawn, takes his place (the Succession). An unfit
 * one gets his refusal and no blood rises.
 */
public final class RingmasterAbsorption {
	/** How often he repeats his refusal to someone channelling at him anyway. */
	public static final int REFUSAL_EVERY = 80;

	private RingmasterAbsorption() {
	}

	/** The progress drawn this tick, or 0 when the channel is not his (so the channel's other uses get their turn). */
	public static double tryAbsorb(Level level, LivingEntity user, double range, float progressPerTick) {
		if (level.isClientSide || !(user instanceof ServerPlayer player) || !(level instanceof ServerLevel server)) return 0.0D;
		Optional<CircusRingmasterEntity> kneeling = level.getEntitiesOfClass(CircusRingmasterEntity.class,
				player.getBoundingBox().inflate(range + 1.0D), copy -> copy.stageCopy() && copy.isAlive()
						&& copy.finalePhase() == FinalePhase.KNEEL)
				.stream().min(Comparator.comparingDouble(player::distanceToSqr));
		if (kneeling.isEmpty()) return 0.0D;
		CircusRingmasterEntity copy = kneeling.get();
		Optional<Map.Entry<UUID, Finale>> owned = RingmasterFinaleDirector.ofCopy(copy);
		if (owned.isEmpty()) return 0.0D;
		Finale finale = owned.get().getValue();
		boolean owner = owned.get().getKey().equals(player.getUUID());
		if (!eligible(player, finale, owner)) {
			if (owner && finale.phaseTicks() >= FinaleRules.KNEEL_SETTLE_TICKS
					&& server.getGameTime() - finale.refusedAt >= REFUSAL_EVERY) {
				finale.refusedAt = server.getGameTime();
				RingmasterFinaleMoves.say(player, RingmasterFinaleMoves.LINE_REFUSED);
			}
			return 0.0D;
		}
		float before = finale.absorption;
		finale.absorption = RingmasterAbsorptionRules.advance(before, progressPerTick);
		copy.setAbsorbed(finale.absorption);
		// His blood pulled off him in a stream to the hand that draws it.
		Vec3 from = copy.position().add(0.0D, copy.getBbHeight() * 0.6D, 0.0D), to = player.getEyePosition().add(0.0D, -0.4D, 0.0D);
		for (int i = 0; i < 5; i++) {
			Vec3 at = from.lerp(to, (i + server.getGameTime() % 5 / 5.0D) / 5.0D);
			BossTelegraphs.bloodCells(server, at, CircusEffects.CRIMSON, 2, 0.08D, 0.02D);
		}
		if (RingmasterAbsorptionRules.complete(finale.absorption)) {
			RingmasterFinaleMoves.say(player, CircusRingmasterEntity.YIELD_LINE);
			BossTelegraphs.bloodCells(server, to, CircusEffects.CRIMSON, 40, 0.5D, 0.12D);
			RingmasterFinaleDirector.finish(player, Ending.ABSORBED);
		}
		return finale.absorption - before;
	}

	/** Whether {@code player} may absorb him now. */
	static boolean eligible(ServerPlayer player, Finale finale, boolean owner) {
		return RingmasterAbsorptionRules.canAbsorb(finale.phase() == FinalePhase.KNEEL, finale.phaseTicks(), owner,
				HemoCapabilityAccess.getBloodVolume(player).isActive(), CircusPlayerProgress.acclimation(player),
				CircusPlayerProgress.challenges(player), CircusRingmasterEntity.unstained(player));
	}

	/** Whether {@code player} is fit to hold the whip at all: what the prompt and the rising blood answer. */
	static boolean fit(ServerPlayer player) {
		return RingmasterAbsorptionRules.fit(HemoCapabilityAccess.getBloodVolume(player).isActive(),
				CircusPlayerProgress.acclimation(player), CircusPlayerProgress.challenges(player), CircusRingmasterEntity.unstained(player));
	}
}
