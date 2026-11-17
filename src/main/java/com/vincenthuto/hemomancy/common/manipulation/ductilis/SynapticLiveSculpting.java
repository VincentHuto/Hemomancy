package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Synaptic Jolt under held Projection: live conductors. Held on water, iron, copper or an owned Ferric construct, the
 * arc jumps to it and keeps its whole connected run live for as long as blood flows; whatever stands in or touches the
 * run is shocked by the conduction it already had ({@link ConductionManager}). Held on a creature it jolts it each
 * pulse, lightly, and Disrupts it; a creature standing on a conductor makes that run live too. It claims nothing else:
 * aimed at bare stone or open air, Projection is plain Projection.
 *
 * <p>The R cast does the same once. Held, the charge is kept up, which is what makes a flooded room or an iron floor a
 * weapon.
 */
public final class SynapticLiveSculpting implements VerbSculpting {
	/** Blood a pulse costs before the cost ledger: 40 mL a second. */
	public static final double BLOOD_PER_PULSE = 20.0D;
	/** A held jolt's damage on a creature each pulse, before Crimson Mastery: lighter than the R cast's 3. */
	public static final float PULSE_DAMAGE = 2.0F;
	/** How long each pulse leaves a creature Disrupted. */
	public static final int DISRUPTED_TICKS = 10;

	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return switch (target) {
			case VerbTarget.Creature creature -> ManipulationCombatHelper.canHarm(player, creature.living());
			case VerbTarget.Block block -> conductor(player.level(), block) != null;
			default -> false;
		};
	}

	/**
	 * The conductor a struck face gives: the block itself, or what lies against the struck face (a puddle on a floor is
	 * struck through to the floor under it). Null when neither conducts.
	 */
	@Nullable
	public static BlockPos conductor(Level level, VerbTarget.Block block) {
		if (ConductionManager.isConductor(level, block.pos())) return block.pos();
		BlockPos against = block.pos().relative(block.hit().getDirection());
		return ConductionManager.isConductor(level, against) ? against : null;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public boolean drawsOwnStream() {
		return true;
	}

	/** Long Strand reaches the jolt further (CQ-20 B). It sets no pace of its own and takes no accent, so Free Hand and Whipcrack do nothing here. */
	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		Discharge discharge = new Discharge();
		if (target instanceof VerbTarget.Creature creature) {
			LivingEntity struck = creature.living();
			BloodManipulation jolt = manipulation(player);
			if (jolt == null || !ConductionManager.claimHit(player, struck, discharge)) return;
			DuctilisLightningEffects.synapticJolt(player, struck);
			float damage = (float) (PULSE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			if (ManipulationCombatHelper.hurt(jolt, player, struck, level, damage, DISRUPTED_TICKS, 1)) {
				ConductionManager.energizeTouching(player, struck, discharge);
			}
			level.playSound(null, struck.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.3F, 1.9F);
		} else if (target instanceof VerbTarget.Block block) {
			BlockPos seed = conductor(level, block);
			if (seed == null || !ConductionManager.energizeAt(player, seed, discharge)) return;
			DuctilisLightningEffects.conductorStrike(player, Vec3.atCenterOf(seed));
			if (pulse.index() % 2 == 0) {
				level.playSound(null, seed, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.25F, 2.0F);
			}
		}
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}
}
