package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.PuppeteerTrialRiteRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PuppeteerSummonTrialEvents {
	private PuppeteerSummonTrialEvents() {
	}

	public static ResourceLocation recipeId(PuppeteerSummonDefinition definition) {
		return Hemomancy.rloc("cardinal_rite/puppeteer_trial_" + definition.name());
	}

	public static ResourceLocation componentRecipeId(PuppeteerSummonDefinition definition) {
		return Hemomancy.rloc(switch (definition.name()) {
			case PuppeteerSummonDefinitions.VEINWING_VULTURE -> "veinwing_harness";
			case PuppeteerSummonDefinitions.MARROW_SPITTER -> "marrow_spitter_carriage";
			case PuppeteerSummonDefinitions.GOREBOUND_HULK -> "gorebound_yoke";
			case PuppeteerSummonDefinitions.MNEMONIST_PUPPET -> "mnemonist_cradle";
			default -> definition.name();
		});
	}

	public static void awardOrdealRecipes(ServerPlayer player, int degree) {
		if (player == null || degree <= 0) {
			return;
		}
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
			if (definition.requiredDegree() <= degree) {
				player.server.getRecipeManager().byKey(recipeId(definition)).ifPresent(recipes::add);
				player.server.getRecipeManager().byKey(componentRecipeId(definition)).ifPresent(recipes::add);
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
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(caster.serverLevel());
		ActiveCardinalRite rite = saved.getActiveRites().get(casterId);
		if (rite == null) return;
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(caster.serverLevel(), rite.getRecipeId());
		if (recipe == null || !recipe.isPuppeteerTrial()) return;
		if (PuppeteerTrialRiteRules.matchesDeath(rite.getPlayerUUID(), rite.getPuppeteerTrialEntityId(),
				rite.getPuppeteerTrialSummonName(), casterId, event.getEntity().getUUID(),
				summon.hemomancy$getSummonName())
				&& rite.markPuppeteerTrialDefeated(event.getEntity().getUUID())) {
			saved.setDirty();
		}
	}
}
