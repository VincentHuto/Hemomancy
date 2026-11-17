package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * What each Alchemist commission (the Basin comparison, the Deep Dark sample) pays once reported (PT-41, NQ-10-8):
 * 10 rapport with the Alchemist, Stranger to Known, and 2 Vivacious Enzyme through {@link OwedRewards}.
 */
public final class AlchemistCommissionReward {
	public static final int RAPPORT = 10;
	public static final int ENZYMES = 2;
	private static final String SPEAKER = "alchemist";

	private AlchemistCommissionReward() {
	}

	/** Owes the enzymes; call before the commission's reported flag, then {@link #pay}. */
	public static void owe(ServerPlayer player) {
		OwedRewards.owe(player, List.of(new ItemStack(ItemInit.vivacious_enzyme.get(), ENZYMES)));
	}

	/** Credits the rapport and hands over what is owed. Call after the reported flag. */
	public static void pay(ServerPlayer player) {
		NpcTradeService.creditRapport(player, SPEAKER, RAPPORT);
		OwedRewards.deliver(player);
	}
}
