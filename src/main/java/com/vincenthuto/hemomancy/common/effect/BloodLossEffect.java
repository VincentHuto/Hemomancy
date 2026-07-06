package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BloodLossEffect extends MobEffect {

	public BloodLossEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		// Entities without blood cant lose any...

		if (entity != null) {

			if (entity instanceof Player) {
				if (!entity.level().isClientSide) {
					Player playerIn = (Player) entity;

					// ── Skill: Coagulation — chance to block this blood-drain tick ──
					double coagChance = SkillPointHelper.getCoagulationChance(playerIn);
					if (coagChance > 0 && playerIn.level().random.nextDouble() < coagChance) {
						BloodFlowLedger.recordApplied((ServerPlayer) playerIn, "blood_loss",
								"Blood Loss", Category.EFFECT, -(0.5f * amplifier), 0.0D, 1, false,
								"Blocked by Coagulation");
						return true; // Blocked by Coagulation skill
					}

					IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
							.orElseThrow(NullPointerException::new);
					if (playerVolume != null) {
						BloodFlowLedger.applyDrain((ServerPlayer) playerIn, playerVolume, "blood_loss",
								"Blood Loss", Category.EFFECT, 0.5f * amplifier, 1, false);
					}

				}
			} else if (!HemoEntityPredicates.NOBLOOD.test(entity)) {
				entity.hurt(entity.damageSources().generic(), 0.5F);
				if (entity.level().random.nextDouble() > 0.999) {
					if (!entity.level().isClientSide) {
						ServerLevel sLevel = (ServerLevel) entity.level();
						sLevel.addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
								new ItemStack(ItemInit.sanguine_formation.get())));
					}
				}
			}
		}

		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Blood Loss");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}

