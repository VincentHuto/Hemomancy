package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.mixin.core.MobEffectInstanceAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BodyRefinementSkillEvents {
	private static final ResourceLocation IRON_HAND_KNOCKBACK = Hemomancy.rloc("iron_handed_knockback");
	private static final ResourceLocation IRON_HAND_SPEED = Hemomancy.rloc("iron_handed_attack_speed");
	private static final ResourceLocation LIGHT_FOOT_SPEED = Hemomancy.rloc("light_footed_speed");
	private static final ResourceLocation LIGHT_FOOT_STEP = Hemomancy.rloc("light_footed_step_height");

	private BodyRefinementSkillEvents() {
	}

	@SubscribeEvent
	public static void onKnockback(LivingKnockBackEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		event.setStrength((float) (event.getStrength() * BodyRefinementSkillRules.knockbackMultiplier(
				SkillPointHelper.getNervesOfSteelLevel(player))));
	}

	@SubscribeEvent
	public static void onEffectAdded(MobEffectEvent.Added event) {
		if (!(event.getEntity() instanceof Player player)) return;
		MobEffectInstance effect = event.getEffectInstance();
		int duration = effect.getDuration();
		if (effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS)) {
			duration = BodyRefinementSkillRules.visionDebuffTicks(duration,
					SkillPointHelper.getBrightEyedLevel(player));
		} else if (effect.is(MobEffects.MOVEMENT_SLOWDOWN)) {
			duration = BodyRefinementSkillRules.signalDebuffTicks(duration,
					SkillPointHelper.getNervesOfSteelLevel(player));
		}
		((MobEffectInstanceAccessor) effect).hemomancy$setDuration(duration);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || player.tickCount % 5 != 0) return;
		int ironHanded = SkillPointHelper.getIronHandedLevel(player);
		updateModifier(player.getAttribute(Attributes.ATTACK_KNOCKBACK), IRON_HAND_KNOCKBACK,
				BodyRefinementSkillRules.meleeKnockbackBonus(ironHanded), AttributeModifier.Operation.ADD_VALUE);
		updateModifier(player.getAttribute(Attributes.ATTACK_SPEED), IRON_HAND_SPEED,
				BodyRefinementSkillRules.attackSpeedModifier(ironHanded), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

		int lightFooted = SkillPointHelper.getLightFootedLevel(player);
		boolean lit = lightFooted > 0 && BodyRefinementSkillRules.strongLight(
				player.level().getMaxLocalRawBrightness(player.blockPosition()));
		updateModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), LIGHT_FOOT_SPEED,
				lit ? BodyRefinementSkillRules.lightMovementModifier(lightFooted) : 0.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		updateModifier(player.getAttribute(Attributes.STEP_HEIGHT), LIGHT_FOOT_STEP,
				lit ? BodyRefinementSkillRules.lightStepHeightBonus(lightFooted) : 0.0D,
				AttributeModifier.Operation.ADD_VALUE);
	}

	private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation) {
		if (attribute == null) return;
		AttributeModifier current = attribute.getModifier(id);
		if (current != null && (amount == 0.0D || current.amount() != amount)) {
			attribute.removeModifier(id);
			current = null;
		}
		if (amount != 0.0D && current == null) {
			attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
		}
	}
}
