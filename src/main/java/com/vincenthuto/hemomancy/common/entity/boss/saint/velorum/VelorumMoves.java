package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * What Velorum's moves share: the damage rule (a preserved player takes x1.5 from him), the stillness a hit adds, the
 * slows, the move's stage helpers, the named blood sources for every drain (resolution D-5) and his sounds.
 */
public final class VelorumMoves {
	/** Held Projection into him in the Guttering Lantern. */
	public static final BloodAccounting.Source WARMING = BloodAccounting.Source.of("velorum_warming", Category.WORLD, true, false);
	/** Held Projection into a frozen figure until it wakes (proper fights). */
	public static final BloodAccounting.Source WAKING = BloodAccounting.Source.of("velorum_waking", Category.WORLD, true, false);
	/** Held Projection into a frozen figure until hyphae pin it (forced fights). */
	public static final BloodAccounting.Source PINNING = BloodAccounting.Source.of("velorum_pinning", Category.WORLD, true, false);
	/** Held Projection into a preserved challenger, warming them out of the ice. */
	public static final BloodAccounting.Source THAWING = BloodAccounting.Source.of("velorum_thawing", Category.WORLD, true, false);
	/** The Draw of Silence (the Litany deed): blood drawn through the crooked hand. */
	public static final BloodAccounting.Source SILENCE = BloodAccounting.Source.of("velorum_silence", Category.HAZARD, false, false);
	/** Held Projection into his closed claw in the Eclipse, relighting the lantern (revision 2026-11-06, R.7). */
	public static final BloodAccounting.Source KINDLING = BloodAccounting.Source.of("velorum_kindling", Category.WORLD, true, false);

	/** Slowness levels as vanilla amplifiers. */
	public static final int SLOWNESS_I = 0;
	public static final int SLOWNESS_II = 1;
	public static final int SLOWNESS_IV = 3;
	/** Below this horizontal speed (blocks a tick) a body is not walking: the Hollow Toll and the Still Floor take it. */
	public static final double WALKING_PACE = 0.15D;
	/** The 15% wind-up cut every phase-3 move takes, clamped to the floors. */
	public static final float PHASE_THREE_CUT = 0.15F;
	/** Velorum never takes a challenger's Rime past two levels, so walking always gets them out (revision R.3). */
	public static final int RIME_CAP = 2;

	private VelorumMoves() {
	}

	/** Damage of his type, x1.5 against a preserved player; where it draws blood is recorded for the Splinter Crown. */
	public static boolean hurt(SaintMoveContext ctx, LivingEntity target, float base) {
		float scaled = base;
		if (ctx.saint() instanceof VelorumEntity velorum && target instanceof Player player && velorum.stillness().preserved(player)) {
			scaled *= VelorumStillnessRules.PRESERVED_TAKEN;
		}
		boolean hurt = SaintMoves.hurt(ctx, target, scaled);
		if (hurt && ctx.saint() instanceof VelorumEntity velorum) velorum.spilled(target, scaled);
		return hurt;
	}

	/** The same outside a move (a beat, a grip, a reaching arm). */
	public static boolean hurt(VelorumEntity velorum, LivingEntity target, float base) {
		float scaled = base;
		if (target instanceof Player player && velorum.stillness().preserved(player)) scaled *= VelorumStillnessRules.PRESERVED_TAKEN;
		boolean hurt = SaintMoves.hurt(velorum, target, scaled, null);
		if (hurt) velorum.spilled(target, scaled);
		return hurt;
	}

	/** Raises the target's Rime by {@code levels} for {@code ticks}, never past {@link #RIME_CAP}; at the cap it refreshes. */
	public static void rime(LivingEntity source, LivingEntity target, int ticks, int levels) {
		int room = RIME_CAP - SchoolStates.levels(target, SchoolState.RIME);
		if (room <= 0) {
			SchoolStates.refresh(target, SchoolState.RIME, ticks);
			return;
		}
		SchoolStates.apply(source, target, SchoolState.RIME, ticks, Math.min(room, Math.max(1, levels)));
	}

	/** Adds to a struck challenger's stillness; companions carry none. */
	public static void stillness(SaintMoveContext ctx, LivingEntity target, float amount) {
		if (ctx.saint() instanceof VelorumEntity velorum && target instanceof Player player) {
			velorum.stillness().add(velorum, player, amount);
		}
	}

	public static void slow(LivingEntity target, int ticks, int amplifier) {
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, amplifier, false, false, true));
	}

	/** The last tick of the move's active span, as the controller runs it. */
	public static boolean lastActive(SaintMoveContext ctx) {
		BossMoveTimeline timeline = ctx.saint().controller().timeline();
		return ctx.stage() == BossMoveTimeline.Stage.ACTIVE && ctx.tick() == timeline.windup() + timeline.active() - 1;
	}

	/** Ticks into the active span, -1 outside it. */
	public static int activeTick(SaintMoveContext ctx) {
		if (ctx.stage() != BossMoveTimeline.Stage.ACTIVE) return -1;
		return ctx.tick() - ctx.saint().controller().timeline().windup();
	}

	/** Horizontal speed of a body this tick. */
	public static double speed(LivingEntity entity) {
		double dx = entity.getX() - entity.xo, dz = entity.getZ() - entity.zo;
		return Math.sqrt(dx * dx + dz * dz);
	}

	public static boolean moving(LivingEntity entity) {
		return speed(entity) >= WALKING_PACE;
	}

	/** Dark blood welling up the floor's seams before a cruor floor move freezes it (revision R.13). */
	public static void welling(SaintMoveContext ctx) {
		sound(ctx, net.minecraft.sounds.SoundEvents.HONEY_BLOCK_SLIDE, 0.4F, 0.4F);
	}

	public static void sound(SaintMoveContext ctx, SoundEvent sound, float volume, float pitch) {
		sound(ctx.saint(), ctx.saint().position(), sound, volume, pitch);
	}

	public static void sound(SaintMoveContext ctx, Holder<SoundEvent> sound, float volume, float pitch) {
		sound(ctx, sound.value(), volume, pitch);
	}

	public static void sound(LivingEntity source, Vec3 at, SoundEvent sound, float volume, float pitch) {
		source.level().playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
	}
}
