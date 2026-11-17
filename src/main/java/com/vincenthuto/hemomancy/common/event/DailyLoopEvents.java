package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFoods;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodRegenRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/**
 * The daily loop's body (core-loop Track 10, CL-18): the one meal hook and the one sleep hook. A Harbinger whose blood
 * is awake gets blood back from meat ({@link BloodFoods}, once per item eaten) and wakes from a full night Well
 * Rested; the regeneration tick in {@code BloodVolumeEvents} reads the food bar and the effect through
 * {@link BloodRegenRules}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class DailyLoopEvents {
	public static final String MEAL_SOURCE = "meal";
	public static final String MEAL_LABEL = "blood_source.hemomancy.meal";

	private DailyLoopEvents() {
	}

	@SubscribeEvent
	public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		eat(player, event.getItem());
	}

	/**
	 * Pays the meal {@code eaten} is worth to {@code player}: blood by nutrition and any Temper the table names. Only
	 * an awake Harbinger's blood answers food. Returns the blood paid.
	 */
	public static double eat(ServerPlayer player, ItemStack eaten) {
		FoodProperties food = eaten.getFoodProperties(player);
		BloodFoods.Food meal = BloodFoods.of(eaten);
		if (food == null || meal == null) return 0.0D;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || ProgressionGates.isUnstained(player)) return 0.0D;
		double ml = BloodRegenRules.mealBlood(food.nutrition(), meal.mlPerNutrition());
		double paid = BloodFlowLedger.applyDirectIncome(player, volume, MEAL_SOURCE, MEAL_LABEL, Category.BODY, ml, 1);
		float temper = TemperCharge.fillFromFood(player, meal.temper(), meal.temperAmount());
		if (paid > 0.0D) {
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_MEAT_IS_BLOOD);
			player.displayClientMessage(Component.translatable("message.hemomancy.daily_loop.meal",
					eaten.getHoverName(), Math.round(paid)).withStyle(ChatFormatting.DARK_RED), true);
		} else if (temper > 0.0F) {
			player.displayClientMessage(Component.translatable("message.hemomancy.daily_loop.meal_temper",
					eaten.getHoverName()).withStyle(ChatFormatting.DARK_RED), true);
		}
		return paid;
	}

	@SubscribeEvent
	public static void onWake(PlayerWakeUpEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		// Leaving bed early also uses the slow wake; only the server's night skip completes a night (as ChamberVisitEvents).
		boolean completed = !event.wakeImmediately() && !event.updateLevel();
		if (completed) rest(player);
	}

	/** A full night's sleep: Well Rested for {@link BloodRegenRules#RESTED_TICKS}, for an awake Harbinger. */
	public static boolean rest(ServerPlayer player) {
		if (!HemoCapabilityAccess.getBloodVolume(player).isActive() || ProgressionGates.isUnstained(player)) return false;
		player.addEffect(new MobEffectInstance(EffectInit.well_rested, BloodRegenRules.RESTED_TICKS, 0, false, false, true));
		player.displayClientMessage(Component.translatable("message.hemomancy.daily_loop.rested")
				.withStyle(ChatFormatting.DARK_RED), true);
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FULL_NIGHT);
		return true;
	}
}
