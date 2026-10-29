package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.item.harbinger.MorphicTonicItem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.ApotheosBodyRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoad;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoadRules;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** An Apotheos body: no hunger, spores gathered in the Gardens, regrowth while sunk into fungal ground. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ApotheosBodyEvents {
	private ApotheosBodyEvents() {}

	public static boolean isApotheos(Player player) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		return HarbingerPathPermissions.isApotheos(degree.getDegreeNumber(), degree.getArchonPath());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || !isApotheos(player)) return;
		var food = player.getFoodData();
		food.setFoodLevel(ApotheosBodyRules.HUNGER_PIN);
		food.setSaturation(0f);
		food.setExhaustion(0f);

		SporeLoad spores = HemoCapabilityAccess.getSporeLoad(player);
		if (player.tickCount % 20 == 0) {
			spores.add(SporeLoadRules.gainPerSecond(FungalGardensPlaces.inDimension(player.level()),
					FungalGardensPlaces.inOverworldBiome(player.level(), player.blockPosition())));
		}

		boolean sinking = SporeLoadRules.isSinking(player.isCrouching(), player.onGround(),
				spores.moveSinceLastTick(player.getX(), player.getZ()),
				player.level().getBlockState(player.blockPosition().below()).is(RootedStateHelper.FUNGAL_NETWORK_GROUND),
				FungalGardensPlaces.isInGardens(player.level(), player.blockPosition()));
		spores.setStillTicks(sinking ? spores.stillTicks() + 1 : 0);
		float heal = sinking ? SporeLoadRules.sinkHeal(spores.stillTicks()) : 0f;
		if (heal > 0f && player.getHealth() < player.getMaxHealth()) {
			player.heal(heal);
			((ServerLevel) player.level()).sendParticles(HemoParticleData.glow(new ParticleColor(150, 30, 60)),
					player.getX(), player.getY() + 0.1, player.getZ(), 10, 0.35, 0.05, 0.35, 0.01);
		}
	}

	@SubscribeEvent
	public static void onUseStart(LivingEntityUseItemEvent.Start event) {
		if (!(event.getEntity() instanceof Player player) || !isApotheos(player)) return;
		ItemStack stack = event.getItem();
		boolean potionOrMilk = stack.is(Items.MILK_BUCKET)
				|| (stack.has(DataComponents.POTION_CONTENTS) && stack.getUseAnimation() == UseAnim.DRINK);
		if (!ApotheosBodyRules.refusesUse(true, FungalGardensPlaces.isInGardens(player.level(), player.blockPosition()),
				stack.get(DataComponents.FOOD) != null, potionOrMilk, stack.getItem() instanceof MorphicTonicItem)) return;
		event.setCanceled(true);
		if (player instanceof ServerPlayer) {
			player.displayClientMessage(Component.translatable("message.hemomancy.apotheos_body.refuses_it")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		}
	}
}
