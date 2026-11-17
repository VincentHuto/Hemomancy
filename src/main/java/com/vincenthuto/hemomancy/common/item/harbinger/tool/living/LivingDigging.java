package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Blood for each block a digging form breaks (core-loop Track 8). The form costs {@link LivingDigRules#bloodPerBlock}
 * by hardness, paid in full through {@link BloodAccounting} as {@link LivingToolItem#DIG}; a player who cannot pay
 * digs at hand speed and gets a bare hand's drops, so nothing is ever taken on credit. Both sides ask the same rule:
 * the owner's volume is synced to them.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingDigging {
	private LivingDigging() {
	}

	/** The digging form in {@code player}'s main hand, or null. */
	static LivingToolItem diggingForm(Player player) {
		ItemStack held = player.getMainHandItem();
		return held.getItem() instanceof LivingToolItem tool && tool.digs() ? tool : null;
	}

	/** Whether {@code player} can pay in full for a block of {@code hardness}; Creative always can. */
	public static boolean canPay(Player player, float hardness) {
		if (player.isCreative()) return true;
		double cost = LivingDigRules.bloodPerBlock(hardness);
		if (cost <= 0.0D) return true;
		return HemoCapabilityAccess.getBloodVolume(player).isActive()
				&& BloodAccounting.canPayInFull(player, cost, LivingToolItem.DIG);
	}

	/** Pays for one broken block and ages the Staff by what was paid; does nothing when it cannot be paid whole. */
	static void payForBlock(ServerPlayer player, float hardness) {
		if (player.isCreative() || !canPay(player, hardness)) return;
		double cost = LivingDigRules.bloodPerBlock(hardness);
		if (cost <= 0.0D) return;
		BloodAccounting.Payment paid = BloodAccounting.pay(player, cost, LivingToolItem.DIG);
		LivingStaffGrowth.record(player, paid.paid());
	}

	private static boolean unpaid(Player player, BlockState state, BlockGetter level, BlockPos pos) {
		LivingToolItem form = diggingForm(player);
		if (form == null || !state.is(form.mineable())) return false;
		return !canPay(player, state.getDestroySpeed(level, pos));
	}

	/** Out of blood, a digging form works like a bare hand. */
	@SubscribeEvent
	public static void breakSpeed(PlayerEvent.BreakSpeed event) {
		Player player = event.getEntity();
		BlockPos pos = event.getPosition().orElse(null);
		if (pos == null || diggingForm(player) == null) return;
		if (unpaid(player, event.getState(), player.level(), pos)) event.setNewSpeed(LivingDigRules.HAND_SPEED);
	}

	/** ...and gets what a bare hand gets. */
	@SubscribeEvent
	public static void harvestCheck(PlayerEvent.HarvestCheck event) {
		Player player = event.getEntity();
		if (diggingForm(player) == null || event.getPos() == null || event.getLevel() == null) return;
		BlockState state = event.getTargetBlock();
		if (unpaid(player, state, event.getLevel(), event.getPos())) {
			event.setCanHarvest(!state.requiresCorrectToolForDrops());
		}
	}
}
