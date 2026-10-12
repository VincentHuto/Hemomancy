package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.circus.ThreadRipperRules;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Vex;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class ThreadRipperManip extends BloodManipulation {
	public static final int CHARGE_TICKS = 30;
	public static final double RANGE = 24.0D;
	public static final int SILENCE_TICKS = 100;

	public ThreadRipperManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (chargeTicks < CHARGE_TICKS) {
			player.displayClientMessage(Component.translatable("message.hemomancy.thread_ripper.charge")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		return true;
	}

	@Override
	public void getAction(Player player, Level level, ItemStack heldItem, BlockPos position, float chargeTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : chargeTicks / getRequiredChargeTicks())) {

		if (!(level instanceof ServerLevel server)) return;
		CircusCarouselEntity carousel = aimedCarousel(player);
		if (carousel != null && carousel.severCaptive(player)) { ManipulationVisuals.burst(server, ManipulationVisuals.Form.THREAD, player.getEyePosition(), carousel.position().add(0, 2.5, 0), 1, 24); return; }

		EntityHitResult hit = DeadlyGazeManip.rayTraceEntities(player, RANGE,
				entity -> entity instanceof LivingEntity && entity != player);
		if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) return;
		boolean tethered = target instanceof BoundPuppeteerSummon;
		boolean protectedBody = protectedBody(player, target);
		ThreadRipperRules.Outcome outcome = ThreadRipperRules.outcome(false, tethered,
				!tethered && foreignSummon(player, target), protectedBody,
				target.getHealth() / Math.max(1.0F, target.getMaxHealth()));
		if (outcome == ThreadRipperRules.Outcome.DISRUPT) {
			ManipulationParticles.hurt(this, target, level.damageSources().magic(), target.getMaxHealth() * 0.25F);
			if (target instanceof Mob mob) BoundSummonBehavior.silenceCommands(mob, SILENCE_TICKS);
		} else if (outcome == ThreadRipperRules.Outcome.UNRAVEL) {
			// Unravelling skips die(); a Drudge still gives back the Blood Memory it carries.
			if (target instanceof DrudgeEntity drudge) drudge.dropEquippedMemory();
			target.discard();
		} else {
			player.displayClientMessage(Component.translatable("message.hemomancy.thread_ripper.no_tether")
					.withStyle(ChatFormatting.DARK_GRAY), true);
			return;
		}
		Entity controller = controller(server, target);
        ManipulationVisuals.burst(server, ManipulationVisuals.Form.THREAD,
                controller!=null?controller.getEyePosition():player.getEyePosition(), target.getEyePosition(), 1, 24); server.playSound(null, target.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.8F, 1.25F);
	        }
    }

	private static CircusCarouselEntity aimedCarousel(Player player) {
		return player.level().getEntitiesOfClass(CircusCarouselEntity.class,
				player.getBoundingBox().inflate(RANGE)).stream()
				.filter(carousel -> player.getEyePosition().vectorTo(carousel.position().add(0.0D, 2.5D, 0.0D))
						.normalize().dot(player.getLookAngle()) > 0.75D)
				.min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
	}

	/** Whoever holds the target's thread, for the severed-thread visual. */
	private static Entity controller(ServerLevel server, LivingEntity target) {
		if (target instanceof BoundPuppeteerSummon bound)
			return bound.hemomancy$getOwnerUUID() == null ? null : server.getEntity(bound.hemomancy$getOwnerUUID());
		if (target instanceof Vex vex) return vex.getOwner();
		return target instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null
				? server.getEntity(ownable.getOwnerUUID()) : null;
	}

	/**
	 * A hostile summoned or owned body outside the puppet tethers: any Vex, or a creature owned by someone who is
	 * neither the caster nor an ally. An owner the caster could not harm, such as a player while PvP is off,
	 * keeps their creature; an owner who cannot be found counts as a player under the server's PvP rule.
	 * Tamed animals and horses are bonded, not summoned, and are never unravelled.
	 */
	public static boolean foreignSummon(Player caster, LivingEntity target) {
		if (target instanceof BoundPuppeteerSummon || ManipulationCombatHelper.allied(caster, target)) return false;
		if (target instanceof Vex) return true;
		if (target instanceof TamableAnimal || target instanceof AbstractHorse) return false;
		if (!(target instanceof OwnableEntity ownable) || ownable.getOwnerUUID() == null) return false;
		Entity owner = caster.level() instanceof ServerLevel server ? server.getEntity(ownable.getOwnerUUID()) : null;
		if (owner == null) owner = caster.level().getPlayerByUUID(ownable.getOwnerUUID());
		if (owner instanceof LivingEntity living) return ManipulationCombatHelper.canHarm(caster, living);
		return caster.getServer() == null || caster.getServer().isPvpAllowed();
	}

	private static boolean protectedBody(Player caster, LivingEntity target) {
		if (caster.isAlliedTo(target) || target.isAlliedTo(caster)
				|| target instanceof CircusPerformerEntity || target instanceof CircusRingmasterEntity
				|| ManipulationReactiveEvents.isBoss(target)) return true;
		if (target instanceof BoundPuppeteerSummon summon) {
			if (summon.hemomancy$isTrialSummon()
					|| caster.getUUID().equals(summon.hemomancy$getOwnerUUID())) return true;
			Player owner = caster.level().getPlayerByUUID(summon.hemomancy$getOwnerUUID());
			if (owner != null && (caster.isAlliedTo(owner) || owner.isAlliedTo(caster))) return true;
		}
		return target.getClass().getPackageName().contains(".entity.boss.");
	}
}
