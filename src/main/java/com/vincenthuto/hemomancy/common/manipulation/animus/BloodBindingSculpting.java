package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTetherRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTethers;
import com.vincenthuto.hemomancy.common.effect.ConstrictedRules;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.List;
import java.util.Set;

/**
 * Blood Binding as a Sculpting of Projection: hold Projection on a creature and it is bound (held still) for as long as
 * blood flows, then constricted (slowed to a crawl) for a short tail after the flow stops. The tail is the old
 * Constrictor Cord folded in as part of the same binding. The chain and lattice variants spread each pulse to the
 * creatures the R cast would reach. {@link ManipulationCombatHelper#canHarm(Player, LivingEntity)} decides who may be
 * bound, as for any cast. A surrogate on a Qliphoth grand floor is held without end, as the R cast holds it. Every
 * creature the binding holds also carries the binder's Animus brand (core-loop round two P5).
 */
public final class BloodBindingSculpting implements VerbSculpting {
	/** The hold outlasts its pulse by this much, so a held bind never flickers between pulses. */
	static final int HOLD_OVERLAP_TICKS = 6;

	private final BloodBindingManip.Mode mode;

	public BloodBindingSculpting(BloodBindingManip.Mode mode) {
		this.mode = mode;
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.CREATURE);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Creature creature
				&& ManipulationCombatHelper.canHarm(player, creature.living());
	}

	@Override
	public double bloodPerPulse() {
		return bloodPerPulse(mode);
	}

	/** Blood one pulse costs before the cost ledger: the wider the binding, the dearer the flow. */
	public static double bloodPerPulse(BloodBindingManip.Mode mode) {
		return switch (mode) {
			case LINGERING -> 12.0D;
			case CHAIN -> 16.0D;
			case LATTICE -> 22.0D;
			default -> 10.0D;
		};
	}

	/** One binding on a boss: the mode's R-cast length halved (60 ticks; Lingering 120, Chain 40, Lattice 80). */
	public static int bossHoldTicks(BloodBindingManip.Mode mode) {
		return BloodBindingManip.durationTicks(mode) / 2;
	}

	/** How long a creature stays constricted once the flow stops; bosses half as long. */
	public static int tailTicks(BloodBindingManip.Mode mode, boolean boss) {
		int tail = switch (mode) {
			case LINGERING -> 100;
			case LATTICE -> 50;
			default -> 30;
		};
		return boss ? tail / 2 : tail;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Creature creature)) return;
		LivingEntity aimed = creature.living();
		List<LivingEntity> bound = BloodBindingManip.reach(player, player.level(), aimed, mode,
				candidate -> ManipulationCombatHelper.canHarm(player, candidate));
		LivingEntity previous = player;
		boolean any = false;
		for (LivingEntity current : bound) {
			// A creature the binder already leads walks with them; the binding does not hold it.
			if (MenagerieLeash.leads(player, current)) continue;
			boolean boss = HemoEntityPredicates.isBoss(current);
			// A boss is held one boss-length hold at a time (NQ-04-1): it is not renewed while it lasts, and a held form
			// that slipped waits before trying that boss again (NQ-04-3).
			if (boss && (current.hasEffect(EffectInit.blood_binding) || BossBindings.waiting(player, current))) continue;
			int holdTicks = boss ? bossHoldTicks(mode) : cadence() + HOLD_OVERLAP_TICKS;
			MobEffectInstance hold = SurrogateTethers.binding(current, new MobEffectInstance(EffectInit.blood_binding,
					holdTicks, 0, false, false, true));
			if (!CommonHooks.canMobEffectBeApplied(current, hold)) continue;
			if (boss || refreshes(current, hold)) {
				if (!BloodBindingManip.applyBinding(player, current, hold)) continue;
				SurrogateTethers.bound(player, current, hold);
			}
			current.addEffect(new MobEffectInstance(EffectInit.constricted,
					holdTicks + tailTicks(mode, boss), 0, false, true, true), player);
			// Blood Binding is the Animus brand (core-loop Track 12): a creature the binding holds carries the binder's
			// brand, so Absorption reaches it from twice as far.
			StrandService.brand(player, EnumBloodTemper.ANIMUS, current);
			LivingEntity caster = mode == BloodBindingManip.Mode.LATTICE && previous != player ? aimed : previous;
			int shown = hold.isInfiniteDuration() ? SurrogateTetherRules.VISUAL_TICKS
					: boss ? holdTicks : cadence() * 2 + HOLD_OVERLAP_TICKS;
			// One seed per caster and creature: each pulse extends the same tendril rather than growing a new one.
			PacketHandler.sendBloodBindingTendril(caster, current, shown,
					((long) caster.getId() << 32) ^ current.getId());
			previous = current;
			any = true;
		}
		if (any && pulse.index() == 0) {
			player.level().playSound(null, aimed.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS,
					0.6F, 0.8F);
		}
	}

	/** A longer binding already on the creature (an R cast, a surrogate's endless hold) is left as it is. */
	private static boolean refreshes(LivingEntity target, MobEffectInstance hold) {
		MobEffectInstance current = target.getEffect(EffectInit.blood_binding);
		return current == null || hold.isInfiniteDuration()
				|| !current.isInfiniteDuration() && current.getDuration() <= hold.getDuration();
	}

	/** The constricted slow left on a creature, as a multiplier of its speed; for docs and tests. */
	public static double constrictedSpeed() {
		return ConstrictedRules.MOVEMENT_SPEED_MULTIPLIER;
	}
}
