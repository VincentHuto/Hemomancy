package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.ToggleablePlayerPowerRules;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ToggleableSkillEvents {
	private ToggleableSkillEvents() {}

	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
		if (player.tickCount % PuppeteerSummonRules.SANGUINE_SPINNING_INTERVAL_TICKS == 0) {
			trySanguineSpinning(player);
		}
		if (player.tickCount % 20 == 0 && SkillPointHelper.isTechniqueEnabled(player,
				SkillPointInit.skill_bloodhound_sense)) {
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(16.0D), target -> target != player && target.isAlive())) {
				if (ToggleablePlayerPowerRules.bloodhoundCanSense(true, target.getHealth(), target.getMaxHealth())) {
					target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
				}
			}
		}
		if (player.tickCount % 4 == 0 && ToggleablePlayerPowerRules.leaveCrimsonWake(
				SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_crimson_wake),
				player.isSprinting(), player.getHealth(), player.getMaxHealth())) {
			level.sendParticles(AbsorbedBloodCellParticleFactory.createData(new ParticleColor(150, 8, 12)),
					player.getX(), player.getY() + 0.1D, player.getZ(), 3, 0.22D, 0.04D, 0.22D, 0.01D);
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
					player.getBoundingBox().inflate(1.25D), target -> target instanceof Enemy)) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 0, false, true, true));
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 24, 0, false, false, false));
			}
		}
	}

	public static boolean trySanguineSpinning(ServerPlayer player) {
		if (!SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_sanguine_spinning)) return false;
		ItemStack crossbar = ItemStack.EMPTY;
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			if (held.getItem() instanceof MarionetteCrossbarItem
					&& MarionetteCrossbarItem.isBoundTo(held, player)
					&& MarionetteCrossbarItem.getThread(held) < MarionetteCrossbarItem.getThreadCapacity(held)) {
				crossbar = held;
				break;
			}
		}
		if (crossbar.isEmpty()) return false;
		var volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null) return false;
		BloodFlowLedger.DrainResult payment = BloodFlowLedger.applyDrain(player, volume,
				"sanguine_spinning", "Sanguine Spinning", Category.TOOL,
				PuppeteerSummonRules.SANGUINE_SPINNING_BLOOD_COST,
				PuppeteerSummonRules.SANGUINE_SPINNING_INTERVAL_TICKS, true);
		if (!payment.satisfied()) return false;
		MarionetteCrossbarItem.addThread(crossbar, PuppeteerSummonRules.SANGUINE_SPINNING_THREAD_CHARGE);
		volume.addBloodSpend(payment.actual());
		return true;
	}
}
