package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingArsenalInventoryGuard;
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

	DeferredHolder<Item, Item> item;

	public ConjurationManip(String name,DeferredHolder<Item, Item> item, double cost, double alignLevel, double xpCost,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, EnumManipulationType.QUICK, rank, tendency, section);
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
		if (CellHandFormHelper.applySelection(player, this)
				&& CellHandFormHelper.isCellHandManip(getName())
				&& CellHandFormHelper.isCellHandForm(player.getMainHandItem())) {
			return;
		}
		if (heldItemMainhand.isEmpty()) {
			if (item.get() == ItemInit.living_staff.get()
					&& LivingArsenalInventoryGuard.summonOrRecoverStaff(player, heldItemMainhand)) {
				ManipulationVisuals.replay(player, ManipulationVisuals.Form.WEAPON_CONJURE, 1, WeaponConjureRules.VISUAL_TICKS,
						WeaponConjureRules.encode(WeaponConjureRules.STAFF, player.getRandom().nextInt(8)));
				return;
			}
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get()));
			if (item.get() instanceof ICellHand) {
				int mode = CellHandRules.summonMode(item.get() == ItemInit.blood_absorption.get());
				ManipulationVisuals.replay(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.ticks(mode), mode);
			}
		}
	        }
    }

	/** The cell hand unravels back into the arm when a conjured hand is dispelled. */
	public static void dispelled(Player player, ItemStack held) {
		if (held.getItem() instanceof ICellHand)
			ManipulationVisuals.replay(player, ManipulationVisuals.Form.CELL_HAND, 1, CellHandRules.DISPEL_TICKS, CellHandRules.DISPEL);
	}

	@Override
	public boolean usesDefaultActivationParticles() {
		return false;
	}

	public 	DeferredHolder<Item, Item> getItem() {
		return item;
	}
}
