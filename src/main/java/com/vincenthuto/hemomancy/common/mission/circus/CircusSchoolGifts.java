package com.vincenthuto.hemomancy.common.mission.circus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * D-44, NQ-15-10 gifts A (chunk 15 T11): the Circus keeps no chest loot; a professor gives a gift once, on the
 * practical the player first passes with them: two Enthralling Filaments and one material from the frame their body
 * is made on. The stacks are owed before the practical is recorded and handed over by the {@link OwedRewards} claim, so
 * a full pack or a death keeps them. A gift is a material, never a memory, kit or progression proof.
 */
public final class CircusSchoolGifts {
	public static final int FILAMENTS = 2;
	/** The frame material each body's professor gives, by summon: one ingredient of that body's frame recipe. */
	private static final Map<String, Supplier<ItemLike>> FRAME_MATERIAL = Map.of(
			PuppeteerSummonDefinitions.VEINWING_VULTURE, ItemInit.venous_pinion::get,
			PuppeteerSummonDefinitions.MARROW_SPITTER, ItemInit.barbed_splinter::get,
			PuppeteerSummonDefinitions.SCARLET_MUMMER, BlockInit.puppeteers_wool::get,
			PuppeteerSummonDefinitions.GOREBOUND_HULK, ItemInit.hematic_iron_scrap::get,
			PuppeteerSummonDefinitions.SANGUINE_HOUND, () -> Items.LEATHER,
			PuppeteerSummonDefinitions.CINDER_BELLOWS, () -> Items.BLAZE_POWDER,
			PuppeteerSummonDefinitions.MNEMONIST_PUPPET, ItemInit.engram_stamp::get);

	private CircusSchoolGifts() {
	}

	/** The gift for passing {@code summon}'s practical. */
	public static List<ItemStack> gift(String summon) {
		var material = FRAME_MATERIAL.get(summon);
		if (material == null) return List.of();
		return List.of(new ItemStack(ItemInit.enthralling_filament.get(), FILAMENTS), new ItemStack(material.get()));
	}

	/**
	 * Records the practical with its gift: the gift is owed first, then the one-time practical flag, then it is handed
	 * over. Returns false, owing nothing, when the practical was already passed or cannot be recorded.
	 */
	public static boolean passPractical(ServerPlayer player, String summon) {
		var gift = gift(summon);
		if (gift.isEmpty() || !CircusApprenticeshipProgress.canCompletePractical(player, summon)) return false;
		OwedRewards.owe(player, gift);
		CircusApprenticeshipProgress.completePractical(player, summon);
		player.displayClientMessage(Component.translatable("message.hemomancy.circus.school.gift",
				gift.get(1).getHoverName()), false);
		OwedRewards.deliver(player);
		return true;
	}
}
