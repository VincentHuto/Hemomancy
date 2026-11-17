package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Endless Hour, Canon Memory of Velorum, Saint of the Endless Hour (D-1 spec 3.4). The caster plants their feet and
 * becomes the still thing: inside 9 blocks every body that stops moving fills with stillness until it is set in its
 * own frozen blood, and the liquid dark at the rim makes stopping easy and hides the caster (NQ-08-9). Planted, the
 * caster takes less damage but cannot run; stepping off the point ends the Hour at once. At the close the still are set
 * and then the caster sets in their own cruor (NQ-08-10). A heavy wound breaks it into the Bow. Not time manipulation:
 * the world runs at its speed and only bodies slow. Player only (NQ-08-5). Imprinted, not learned.
 */
public class EndlessHourManip extends BloodManipulation {
	static final BloodAccounting.Source HOLD = BloodAccounting.Source.of("endless_hour_hold", Category.CAST, false);

	public EndlessHourManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	/** One Hour at a time: none is planted while one holds or its caster still sets or bows. */
	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		return super.canPerformAction(player, chargeTicks) && CanonGiftState.hourRecord(player.getUUID()) == null;
	}

	/** The cooldown starts from the Hour's end, never while it holds. */
	@Override
	public boolean ignoresCooldown(Player player) {
		return CanonGiftState.hour(player.getUUID()) != null;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(player instanceof ServerPlayer caster) || !(world instanceof ServerLevel level)) return;
		CanonGiftState.Hour hour = new CanonGiftState.Hour(caster.position(), level.getGameTime(), level.dimension());
		CanonGiftState.plant(caster.getUUID(), hour);
		caster.setSprinting(false);
		caster.displayClientMessage(Component.translatable("message.hemomancy.endless_hour.planted")
				.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
		level.playSound(null, caster.blockPosition(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.0F);
		level.playSound(null, caster.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.PLAYERS, 0.4F, 0.4F);
		show(caster, hour, 1);
	}

	/** One server tick of the caster's Hour, then of their set or Bow. Called by {@link CanonGiftEvents}. */
	static void tick(ServerPlayer caster) {
		CanonGiftState.Hour hour = CanonGiftState.hourRecord(caster.getUUID());
		if (hour == null) return;
		ServerLevel level = caster.serverLevel();
		long now = level.getGameTime();
		if (hour.ended) {
			// warmth ends the self-set early
			if (hour.setUntil > now && SchoolStates.has(caster, SchoolState.SEARING)) {
				hour.setUntil = now;
				caster.removeEffect(EffectInit.paralysis);
			}
			if (now >= Math.max(hour.setUntil, hour.bowUntil)) CanonGiftState.hours().remove(caster.getUUID());
			return;
		}
		if (!level.dimension().equals(hour.dimension)) return;
		caster.setSprinting(false);
		long age = now - hour.planted;
		if (!EndlessHourRules.planted(horizontal(caster.position(), hour.point))) {
			caster.displayClientMessage(Component.translatable("message.hemomancy.endless_hour.stepped")
					.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
			end(caster, hour);
			return;
		}
		if (age > 0 && age % EndlessHourRules.UPKEEP_TICKS == 0
				&& !BloodAccounting.payUpkeep(caster, EndlessHourRules.UPKEEP, HOLD, EndlessHourRules.UPKEEP_TICKS, true).satisfied()) {
			end(caster, hour);
			return;
		}
		EndlessHourManip manip = (EndlessHourManip) ManipulationInit.endless_hour.get();
		try (var scope = SchoolDamage.cast(manip, caster, 1)) {
			manip.still(caster, level, hour, now, age);
		}
		if (age >= EndlessHourRules.HOUR_TICKS) {
			manip.close(caster, level, hour, now);
			return;
		}
		if (age % 10 == 0) show(caster, hour, 1);
	}

	/** Each tick: every harmable body inside fills or drains its stillness, and a full one is set in its cruor. */
	private void still(ServerPlayer caster, ServerLevel level, CanonGiftState.Hour hour, long now, long age) {
		double reach = EndlessHourRules.RADIUS * Math.max(0.15D, EndlessHourRules.seams(age));
		for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, area(hour, reach),
				body -> body != caster && body.isAlive() && !body.isSpectator())) {
			double distance = horizontal(body.position(), hour.point);
			if (distance > reach || Math.abs(body.getY() - hour.point.y) > 3.0D || !ManipulationCombatHelper.canHarm(caster, body))
				continue;
			CanonGiftState.Stillness still = CanonGiftState.stillness().computeIfAbsent(body.getUUID(),
					ignored -> new CanonGiftState.Stillness(caster.getUUID()));
			still.caster = caster.getUUID();
			still.lastHour = now;
			still.dimension = level.dimension();
			boolean boss = HemoEntityPredicates.isBoss(body);
			boolean dark = EndlessHourRules.inDark(distance);
			if (dark && now % 10 == 0 && !SchoolStates.has(body, SchoolState.ILLUMINATED))
				SchoolStates.apply(caster, body, SchoolState.OBSCURED, EndlessHourRules.DARK_OBSCURED);
			double change = EndlessHourRules.step(still.pace(body.position()), body.swinging || body.isUsingItem(),
					SchoolStates.levels(body, SchoolState.RIME), dark, SchoolStates.has(body, SchoolState.SEARING),
					SchoolStates.has(body, SchoolState.DISRUPTED));
			still.value = EndlessHourRules.still(still.value, change, boss);
			if (EndlessHourRules.preserves(still.value, boss)) preserve(caster, level, body, still, boss);
			else if (now % 5 == 0) showStill(body, still);
		}
	}

	/**
	 * Sets a body in a shell of its own blood frozen to cruor: Paralysis, Rime to 3 (a Ferric hit then shatters it),
	 * 6 Congeatio, and its stillness back to 30. A boss is never set; it takes the 6 and drops back.
	 */
	private void preserve(ServerPlayer caster, ServerLevel level, LivingEntity body, CanonGiftState.Stillness still, boolean boss) {
		still.value = EndlessHourRules.AFTER_PRESERVE;
		body.invulnerableTime = 0;
		strike(caster, body, context(caster).newAttack(), EndlessHourRules.PRESERVE_DAMAGE
				* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster));
		if (!body.isAlive()) return;
		if (!boss) Paralysis.apply(body, EndlessHourRules.PRESERVE_PARALYSIS);
		SchoolStates.apply(caster, body, SchoolState.RIME, SchoolState.RIME.duration);
		SchoolStates.setLevels(body, SchoolState.RIME, EndlessHourRules.PRESERVE_RIME);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.HOUR_PRESERVE, body.position(), body.position(),
				body.getBbHeight(), EndlessHourRules.PRESERVE_PARALYSIS, 1);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CAST_IMPACT, body.position(), caster.position(), 1.0D, 16);
		showStill(body, still);
		level.playSound(null, body.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.7F, 1.0F);
		level.playSound(null, body.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.5F, 0.8F);
	}

	/** The close: every body at 50 or more is set, the rest take 4, then the caster sets in their own cruor. */
	private void close(ServerPlayer caster, ServerLevel level, CanonGiftState.Hour hour, long now) {
		Set<UUID> inside = new HashSet<>();
		try (var scope = SchoolDamage.cast(this, caster, 1)) {
			for (LivingEntity body : level.getEntitiesOfClass(LivingEntity.class, area(hour, EndlessHourRules.RADIUS),
					body -> body != caster && body.isAlive() && !body.isSpectator())) {
				if (!EndlessHourRules.inHour(horizontal(body.position(), hour.point))
						|| !ManipulationCombatHelper.canHarm(caster, body)) continue;
				inside.add(body.getUUID());
				CanonGiftState.Stillness still = CanonGiftState.stillness().get(body.getUUID());
				if (still != null && still.value >= EndlessHourRules.CLOSE_PRESERVE) {
					preserve(caster, level, body, still, HemoEntityPredicates.isBoss(body));
				} else {
					body.invulnerableTime = 0;
					strike(caster, body, context(caster).newAttack(), EndlessHourRules.CLOSE_DAMAGE
							* (float) SkillPointHelper.getCrimsonMasteryMultiplier(caster));
				}
			}
		}
		level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.6F);
		show(caster, hour, 2);
		// the still thing pays by being still
		hour.setUntil = now + EndlessHourRules.SELF_SET_TICKS;
		end(caster, hour);
		Paralysis.apply(caster, EndlessHourRules.SELF_SET_TICKS);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.HOUR_PRESERVE, caster.position(), caster.position(),
				caster.getBbHeight(), EndlessHourRules.SELF_SET_TICKS, 2);
		caster.displayClientMessage(Component.translatable("message.hemomancy.endless_hour.set")
				.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
	}

	/** The Bow: a heavy wound inside the Hour breaks it with no close. The caster bows, softer and unable to cast. */
	static void bow(ServerPlayer caster, CanonGiftState.Hour hour) {
		ServerLevel level = caster.serverLevel();
		show(caster, hour, 3);
		hour.bowUntil = level.getGameTime() + EndlessHourRules.BOW_TICKS;
		end(caster, hour);
		level.playSound(null, caster.blockPosition(), SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.PLAYERS, 0.6F, 0.8F);
		level.playSound(null, caster.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 0.7F);
		caster.displayClientMessage(Component.translatable("message.hemomancy.endless_hour.bow")
				.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
	}

	/** Ends the live Hour: its stillness goes with it and the cooldown runs from now. */
	private static void end(ServerPlayer caster, CanonGiftState.Hour hour) {
		hour.ended = true;
		ManipulationVisuals.attached(caster, ManipulationVisuals.Form.HOUR_FIELD, 0, 0, 0);
		ServerLevel level = caster.serverLevel();
		CanonGiftState.stillness().entrySet().removeIf(entry -> {
			if (!entry.getValue().caster.equals(caster.getUUID())) return false;
			if (level.getEntity(entry.getKey()) instanceof LivingEntity body)
				ManipulationVisuals.attached(body, ManipulationVisuals.Form.HOUR_STILL, 0, 0, 0);
			return true;
		});
		if (now(caster) >= Math.max(hour.setUntil, hour.bowUntil)) CanonGiftState.hours().remove(caster.getUUID());
		ManipulationInit.endless_hour.get().restartCooldown(caster);
	}

	private static long now(ServerPlayer caster) {
		return caster.level().getGameTime();
	}

	/**
	 * The share of a hit the caster takes: the Still Guard while the Hour holds, the self-set at its close, the Bow
	 * after a heavy wound. Read by {@link CanonGiftEvents#applyBodyStates}.
	 */
	public static double taken(Player player) {
		CanonGiftState.Hour hour = CanonGiftState.hourRecord(player.getUUID());
		if (hour == null) return 1.0D;
		long now = player.level().getGameTime();
		return EndlessHourRules.taken(!hour.ended && guarded(player), hour.ended && now < hour.setUntil,
				hour.ended && now < hour.bowUntil);
	}

	/** Whether a planted caster keeps the Still Guard: not in Velorum's own fight, where it is his hour (NQ-08-11). */
	static boolean guarded(Player player) {
		return CanonGifts.fightOf(player, VelorumEntity.class) == null;
	}

	/** True while this player's Endless Hour holds. Velorum's stillness fills faster on them then (NQ-08-11). */
	public static boolean holding(Player player) {
		return CanonGiftState.hour(player.getUUID()) != null;
	}

	/** Counts a landed hit toward the Bow. Called by {@link CanonGiftEvents} for each hit on a player. */
	static void wounded(ServerPlayer player, float damage) {
		CanonGiftState.Hour hour = CanonGiftState.hour(player.getUUID());
		if (hour == null || damage <= 0.0F) return;
		hour.taken += damage;
		if (EndlessHourRules.bows(hour.taken, player.getMaxHealth())) bow(player, hour);
	}

	/** True while the caster bows: they cannot cast. */
	public static boolean bowing(Player player) {
		CanonGiftState.Hour hour = CanonGiftState.hourRecord(player.getUUID());
		return hour != null && hour.ended && player.level().getGameTime() < hour.bowUntil;
	}

	/** Out of every Hour, stillness drains at the travel rate and goes at 0. Called by {@link CanonGiftEvents}. */
	static boolean drain(LivingEntity body, CanonGiftState.Stillness still, long now) {
		if (now - still.lastHour < 2) return false;
		still.value = Math.max(0.0D, still.value - EndlessHourRules.OUTSIDE_DRAIN);
		if (still.value <= 0.0D) {
			ManipulationVisuals.attached(body, ManipulationVisuals.Form.HOUR_STILL, 0, 0, 0);
			return true;
		}
		if (now % 5 == 0) showStill(body, still);
		return false;
	}

	/** Stillness an Hour laid on {@code body}, for tests and the gift events; 0 when none. */
	public static double stillnessOf(LivingEntity body) {
		CanonGiftState.Stillness still = CanonGiftState.stillness().get(body.getUUID());
		return still == null ? 0.0D : still.value;
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		double dx = a.x - b.x, dz = a.z - b.z;
		return Math.sqrt(dx * dx + dz * dz);
	}

	private static AABB area(CanonGiftState.Hour hour, double reach) {
		return new AABB(hour.point, hour.point).inflate(reach + 0.5D, 3.0D, reach + 0.5D);
	}

	/** The held field keyed to its caster (state 1), or the close (2) and the broken Hour (3) at the planted point. */
	private static void show(ServerPlayer caster, CanonGiftState.Hour hour, int state) {
		if (state == 1) ManipulationVisuals.attached(caster, ManipulationVisuals.Form.HOUR_FIELD, hour.point,
				EndlessHourRules.RADIUS, 20, state);
		else ManipulationVisuals.burst(caster.serverLevel(), ManipulationVisuals.Form.HOUR_FIELD, hour.point, hour.point,
				EndlessHourRules.RADIUS, EndlessHourRules.SET_END, state);
	}

	private static void showStill(LivingEntity body, CanonGiftState.Stillness still) {
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.HOUR_STILL,
				still.value / EndlessHourRules.MAX_STILLNESS, 20, 1);
	}

	private static SchoolHitContext context(ServerPlayer caster) {
		return SchoolHitContext.direct(Hemomancy.rloc("endless_hour"), EnumBloodTemper.CONGEATIO, EnumBloodTemper.TENEBRIS, caster);
	}

	private boolean strike(ServerPlayer caster, LivingEntity target, SchoolHitContext hit, float amount) {
		if (!ManipulationCombatHelper.canHarm(caster, target)) return false;
		return ManipulationParticles.hurt(this, target,
				SchoolDamage.attributed(caster.serverLevel().damageSources().magic(), hit, caster), amount);
	}
}
