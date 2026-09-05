package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.circus.ThreadRipperRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
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
		if (!(level instanceof ServerLevel server)) return;
		CircusCarouselEntity carousel = aimedCarousel(player);
		if (carousel != null && carousel.severCaptive(player)) return;

		EntityHitResult hit = DeadlyGazeManip.rayTraceEntities(player, RANGE,
				entity -> entity instanceof LivingEntity && entity != player);
		if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) return;
		boolean tethered = target instanceof BoundPuppeteerSummon;
		boolean protectedBody = protectedBody(player, target);
		ThreadRipperRules.Outcome outcome = ThreadRipperRules.outcome(false, tethered, protectedBody,
				target.getHealth() / Math.max(1.0F, target.getMaxHealth()));
		if (outcome == ThreadRipperRules.Outcome.DISRUPT) {
			target.hurt(level.damageSources().magic(), target.getMaxHealth() * 0.25F);
			if (target instanceof Mob mob) BoundSummonBehavior.silenceCommands(mob, SILENCE_TICKS);
		} else if (outcome == ThreadRipperRules.Outcome.UNRAVEL) {
			target.discard();
		} else {
			player.displayClientMessage(Component.translatable("message.hemomancy.thread_ripper.no_tether")
					.withStyle(ChatFormatting.DARK_GRAY), true);
			return;
		}
		server.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + target.getBbHeight() * 0.5D,
				target.getZ(), 28, 0.35D, 0.55D, 0.35D, 0.04D);
		server.playSound(null, target.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.8F, 1.25F);
	}

	private static CircusCarouselEntity aimedCarousel(Player player) {
		return player.level().getEntitiesOfClass(CircusCarouselEntity.class,
				player.getBoundingBox().inflate(RANGE)).stream()
				.filter(carousel -> player.getEyePosition().vectorTo(carousel.position().add(0.0D, 2.5D, 0.0D))
						.normalize().dot(player.getLookAngle()) > 0.75D)
				.min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
	}

	private static boolean protectedBody(Player caster, LivingEntity target) {
		if (caster.isAlliedTo(target) || target.isAlliedTo(caster)
				|| target instanceof CircusPerformerEntity || target instanceof CircusRingmasterEntity
				|| target instanceof EnderDragon || target instanceof WitherBoss) return true;
		if (target instanceof BoundPuppeteerSummon summon) {
			if (summon.hemomancy$isTrialSummon()
					|| caster.getUUID().equals(summon.hemomancy$getOwnerUUID())) return true;
			Player owner = caster.level().getPlayerByUUID(summon.hemomancy$getOwnerUUID());
			if (owner != null && (caster.isAlliedTo(owner) || owner.isAlliedTo(caster))) return true;
		}
		return target.getClass().getPackageName().contains(".entity.boss.");
	}
}
