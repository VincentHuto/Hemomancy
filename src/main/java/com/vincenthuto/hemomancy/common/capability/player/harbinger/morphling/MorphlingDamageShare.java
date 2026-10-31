package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.DeadMansRopeRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.HoldfastMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.HoldfastRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingDamageShareRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLinks;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The one damage-redirection handler (two rule sets in {@link MorphlingDamageShareRules}): Holdfast's Breakwater
 * moves a share of a sheltered ally's damage to the anchored host for blood, and Dead Man's Rope's Colony spreads a
 * share of a linked creature's damage across the others. Moved damage is taken straight off health, never re-dealt,
 * so it cannot redirect again; the floors keep every giver alive.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MorphlingDamageShare {
	private static final BloodAccounting.Source BREAKWATER = BloodAccounting.Source.of("breakwater", Category.MORPHLING,
			false, false);
	private static boolean redirecting;

	private MorphlingDamageShare() {
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		LivingEntity victim = event.getEntity();
		if (redirecting || victim.level().isClientSide || event.getAmount() <= 0.0F) return;
		redirecting = true;
		try {
			float amount = event.getAmount();
			amount = breakwater(victim, amount);
			amount = colony(victim, amount);
			event.setAmount(amount);
		} finally {
			redirecting = false;
		}
	}

	private static float breakwater(LivingEntity victim, float amount) {
		Player host = HoldfastMorphlingItem.shelteringBreakwater(victim);
		if (host == null || host == victim) return amount;
		var volume = HemoCapabilityAccess.getBloodVolume(host);
		double blood = volume.isActive() ? volume.getBloodVolume() : 0.0D;
		float moved = MorphlingDamageShareRules.breakwater(amount, HoldfastRules.BREAKWATER_SHARE, host.getHealth(),
				host.getMaxHealth(), blood, HoldfastRules.BREAKWATER_BLOOD_PER_HP);
		if (moved <= 0.0F || !BloodAccounting.pay(host, moved * HoldfastRules.BREAKWATER_BLOOD_PER_HP, BREAKWATER).satisfied()) {
			return amount;
		}
		host.setHealth(host.getHealth() - moved);
		return amount - moved;
	}

	private static float colony(LivingEntity victim, float amount) {
		List<LivingEntity> group = colonyOf(victim);
		if (group.size() < 2) return amount;
		List<LivingEntity> others = new ArrayList<>(group);
		others.remove(victim);
		float[] health = new float[others.size()];
		float[] max = new float[others.size()];
		for (int i = 0; i < others.size(); i++) {
			health[i] = others.get(i).getHealth();
			max[i] = others.get(i).getMaxHealth();
		}
		float[] parts = MorphlingDamageShareRules.colony(amount, DeadMansRopeRules.COLONY_SHARE, health, max);
		for (int i = 0; i < others.size(); i++) {
			if (parts[i] > 0.0F) others.get(i).setHealth(others.get(i).getHealth() - parts[i]);
		}
		return amount - MorphlingDamageShareRules.sum(parts);
	}

	/** The Colony {@code victim} belongs to: an Apex Rope host and its living partners, or empty. */
	private static List<LivingEntity> colonyOf(LivingEntity victim) {
		List<Player> hosts = new ArrayList<>(MorphlingLinks.hostsOf(victim));
		if (victim instanceof Player player) hosts.add(0, player);
		for (Player host : hosts) {
			ItemStack stack = HemoCapabilityAccess.getEquippedMorphling(host).getEquippedMorphling();
			if (!MorphlingStrains.is(stack, MorphlingStrains.DEAD_MANS_ROPE) || MorphlingItem.getMaturityLevel(stack) < 4) {
				continue;
			}
			List<LivingEntity> partners = MorphlingLinks.partners(host);
			if (partners.isEmpty()) continue;
			List<LivingEntity> group = new ArrayList<>();
			group.add(host);
			group.addAll(partners);
			if (group.contains(victim)) return group;
		}
		return List.of();
	}
}
