package com.vincenthuto.hemomancy.common.summon;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PuppeteerSummonTrialEvents {
	private PuppeteerSummonTrialEvents() {
	}

	public static ResourceLocation recipeId(PuppeteerSummonDefinition definition) {
		return Hemomancy.rloc("puppeteer_trial/" + definition.name());
	}

	public static void awardTrialRecipes(ServerPlayer player, int degree) {
		if (player == null || degree <= 0) {
			return;
		}
		List<RecipeHolder<?>> recipes = Lists.newArrayList();
		for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
			if (definition.requiredDegree() <= degree) {
				player.server.getRecipeManager().byKey(recipeId(definition)).ifPresent(recipes::add);
			}
		}
		if (!recipes.isEmpty()) {
			player.awardRecipes(recipes);
		}
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof BoundPuppeteerSummon summon) || !summon.hemomancy$isTrialSummon()) {
			return;
		}
		UUID casterId = summon.hemomancy$getTrialCasterUUID();
		MinecraftServer server = event.getEntity().level().getServer();
		if (casterId == null || server == null) {
			return;
		}
		if (!(server.getPlayerList().getPlayer(casterId) instanceof ServerPlayer caster)) {
			return;
		}
		PuppeteerSummonDefinitions.byName(summon.hemomancy$getSummonName()).ifPresent(definition -> {
			if (KnownSummonEvents.grantSummon(caster, definition)) {
				caster.displayClientMessage(Component.translatable("hemomancy.summon.trial.complete",
						Component.translatable(definition.translationKey())).withStyle(ChatFormatting.RED), false);
			}
		});
	}
}
