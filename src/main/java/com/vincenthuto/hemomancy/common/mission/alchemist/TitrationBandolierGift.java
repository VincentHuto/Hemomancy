package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * CQ-63 A: the Alchemist gives the Titration Bandolier once, with her D2 Temper lesson, against no further proof. It is
 * owed before the one-time flag is set, and {@link OwedRewards} hands it over. Owning it proves nothing; a lost one is
 * crafted.
 */
public final class TitrationBandolierGift {
	/** The lesson id whose telling gives it. */
	public static final String LESSON = "temper";

	private TitrationBandolierGift() {
	}

	/** Owes and delivers the Bandolier unless it was given before. Returns whether this call gave it. */
	public static boolean give(ServerPlayer player) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		if (temper.bandolierGiven()) return false;
		OwedRewards.owe(player, List.of(new ItemStack(ItemInit.titration_bandolier.get())));
		temper.markBandolierGiven();
		player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.alchemist_bandolier_given")
				.withStyle(ChatFormatting.DARK_RED), false);
		OwedRewards.deliver(player);
		return true;
	}
}
