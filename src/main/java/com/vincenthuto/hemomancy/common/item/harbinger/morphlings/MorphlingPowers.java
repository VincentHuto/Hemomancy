package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * Glue between the worn strain and the shared Brined Court mechanics: each strain's power list for the balance
 * bands, the synced active state, and the one place a host's strain state is torn down (taken off, death, logout).
 */
public final class MorphlingPowers {
	private MorphlingPowers() {
	}

	/** The strain's ladder as {@link MorphlingPower} rows, checked by {@code MorphlingBalanceBandsTest}. */
	public static List<MorphlingPower> of(MorphlingStrain strain) {
		if (strain == MorphlingStrains.SUNBURST) return SunburstRules.POWERS;
		if (strain == MorphlingStrains.HOLDFAST) return HoldfastRules.POWERS;
		if (strain == MorphlingStrains.MERMAIDS_PURSE) return MermaidsPurseRules.POWERS;
		if (strain == MorphlingStrains.DEAD_MANS_ROPE) return DeadMansRopeRules.POWERS;
		if (strain == MorphlingStrains.INK_CAP) return InkCapRules.POWERS;
		if (strain == MorphlingStrains.HUSHBELL) return HushbellRules.POWERS;
		if (strain == MorphlingStrains.MARROWPLUME) return MarrowplumeRules.POWERS;
		if (strain == MorphlingStrains.KINBUD) return KinbudRules.POWERS;
		throw new IllegalArgumentException("no power ladder for " + strain);
	}

	/** Home, stillness and the strain's own bits, as synced to trackers. */
	public static int activeFlags(Player player, IMorphling morphling, ItemStack stack) {
		int flags = morphling.activeFlags(player, stack);
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.HOME, MorphlingHomeConditions.isHome(player, stack));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.STILL,
				MorphlingAnchorState.isStill(player, MorphlingHomeConditions.STILL_HOME_TICKS));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.ANCHORED, MorphlingAnchorState.isAnchored(player, 1));
		return flags;
	}

	public static int[] linkedEntityIds(Player player) {
		List<Integer> ids = new ArrayList<>();
		if (player.level() instanceof ServerLevel level) {
			for (MorphlingLinks.Link link : MorphlingLinks.links(player)) {
				if (link.isBlock()) continue;
				Entity entity = level.getEntity(link.entity());
				if (entity != null) ids.add(entity.getId());
			}
		}
		return ids.stream().mapToInt(Integer::intValue).toArray();
	}

	public static long[] linkedAnchors(Player player) {
		return MorphlingLinks.links(player).stream().filter(MorphlingLinks.Link::isBlock)
				.mapToLong(link -> link.anchor().asLong()).toArray();
	}

	/** Undoes everything a strain holds on its host. Safe to call when nothing is held. */
	public static void clearHost(Player player) {
		AttributeModifiers.sync(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), HoldfastMorphlingItem.GRIP_KNOCKBACK,
				0.0D, AttributeModifier.Operation.ADD_VALUE, false);
		AttributeModifiers.sync(player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED), MermaidsPurseMorphlingItem.SWIM_SPEED,
				0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
		MorphlingLinks.clear(player);
		KinbudSecondHead.clear(player.getUUID());
		MorphlingRegrowthQueue.remove(player.getUUID());
		MorphlingHomeConditions.forget(player);
		InkCapMorphlingItem.endDeliquesce(player);
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			if (strain.item().get() instanceof IMorphling morphling) morphling.onUnequipped(player, ItemStack.EMPTY);
		}
	}
}
