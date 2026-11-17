package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingArsenalInventoryGuard;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ConjurationManip extends BloodManipulation {
	private static final double DISPEL_REFUND_SHARE = 0.9D;

	DeferredHolder<Item, Item> item;

	public ConjurationManip(String name,DeferredHolder<Item, Item> item, double cost, double alignLevel, double xpCost,
			EnumManipulationRank rank, EnumBloodTemper temper, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, EnumManipulationType.QUICK, rank, temper, section);
		this.item = item;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (item.get() == ItemInit.living_staff.get() && player instanceof ServerPlayer serverPlayer
				&& CardinalRiteStaffEscrow.isPlanted(serverPlayer)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.conjuration_manip.your_living_staff_is_planted_in").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
			return;
		}
		if (heldItemMainhand.isEmpty()) {
			if (item.get() == ItemInit.living_staff.get()
					&& LivingArsenalInventoryGuard.summonOrRecoverStaff(player, heldItemMainhand)) {
				stampPaid(player.getMainHandItem());
				ManipulationVisuals.replay(player, ManipulationVisuals.Form.WEAPON_CONJURE, 1, WeaponConjureRules.VISUAL_TICKS,
						WeaponConjureRules.encode(WeaponConjureRules.STAFF, player.getRandom().nextInt(8)));
				return;
			}
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get()));
			stampPaid(player.getMainHandItem());
			if (item.get() instanceof ICellHand) {
				int mode = CellHandRules.summonMode(item.get() == ItemInit.blood_absorption.get());
				ManipulationVisuals.replay(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.ticks(mode), mode);
			}
		}
	        }
    }

	private static void stampPaid(ItemStack conjured) {
		if (conjured.getItem() instanceof IDispellable) conjured.set(DataComponentInit.CONJURE_PAID.get(), paidForCurrentCast());
	}

	/** What dispelling this conjured item returns: 90% of the blood paid to conjure it (NQ-04-15). */
	public static double dispelRefund(ItemStack conjured) {
		return DISPEL_REFUND_SHARE * conjured.getOrDefault(DataComponentInit.CONJURE_PAID.get(), 0.0D);
	}

	/** The cell hand unravels back into the arm when a conjured hand is dispelled. */
	public static void dispelled(Player player, ItemStack held) {
		if (held.getItem() instanceof ICellHand)
			ManipulationVisuals.replay(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.DISPEL_TICKS, CellHandRules.DISPEL);
	}

	/**
	 * Dispels the conjured item in the main hand and refunds part of its price through the blood-flow ledger.
	 *
	 * @return false when the main hand holds something that was not conjured
	 */
	public static boolean dispelHeld(ServerPlayer player) {
		ItemStack mainStack = player.getMainHandItem();
		if (!(mainStack.getItem() instanceof IDispellable)) return false;
		dispelled(player, mainStack);
		// A Staff weapon form carries the Staff inside it, and the Staff carries what its conjure paid.
		double bloodRefund = dispelRefund(LivingStaffWeaponFormHelper.isTransformedStaffWeapon(mainStack)
				? LivingStaffWeaponFormHelper.restoredStaffStack(mainStack, player.registryAccess()) : mainStack);
		mainStack.shrink(1);
		// Income goes through the ledger, which records it and sends the owner sync.
		if (bloodRefund > 0.0D) {
			BloodFlowLedger.applyDirectIncome(player, HemoCapabilityAccess.getBloodVolume(player), "conjuration_dispel",
					"blood_source.hemomancy.conjuration_dispel", BloodFlowContribution.Category.SKILL, bloodRefund, 1);
		}
		player.displayClientMessage(Component.translatable("message.hemomancy.use_manip_key_packet.dispelled_conjured_item").withStyle(ChatFormatting.RED), true);
		return true;
	}

	@Override
	public boolean usesDefaultActivationParticles() {
		return false;
	}

	public 	DeferredHolder<Item, Item> getItem() {
		return item;
	}

	/** Conjuring a tool is not a school's effect: it burns no Temper. */
	@Override
	public boolean chargesTemper() {
		return false;
	}
}
