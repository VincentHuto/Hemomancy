package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.InkCapRules;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Ink Cap's Veil through the mob visibility hook, so it multiplies with sneaking, Invisibility and worn armour the way
 * those already combine. Only hostile mobs are fooled, and the strain never cuts detection by more than
 * {@link InkCapRules#VEIL_CAP}. Silent Archon armour conceals through invisibility, not this hook, so neither doubles
 * the other.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MorphlingVisibility {
	private MorphlingVisibility() {
	}

	@SubscribeEvent
	public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
		if (!(event.getEntity() instanceof Player player) || !(event.getLookingEntity() instanceof Enemy)) return;
		var cap = HemoCapabilityAccess.getEquippedMorphling(player);
		if (!cap.hasMorphling() || !(cap.getEquippedMorphling().getItem() instanceof IMorphling morphling)) return;
		double multiplier = morphling.visibilityMultiplier(player, cap.getEquippedMorphling());
		if (multiplier < 1.0D) event.modifyVisibility(Math.max(1.0D - InkCapRules.VEIL_CAP, multiplier));
	}
}
