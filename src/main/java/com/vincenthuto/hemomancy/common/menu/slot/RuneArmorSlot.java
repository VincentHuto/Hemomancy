package com.vincenthuto.hemomancy.common.menu.slot;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.menu.CharmGourdMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RuneArmorSlot extends Slot {

	private EquipmentSlot slotType;
	private Player playerEntity;

	public RuneArmorSlot(Container inventoryIn, int index, int xPosition, int yPosition, EquipmentSlot slotType,
			Player playerEntity) {
		super(inventoryIn, index, xPosition, yPosition);
		this.slotType = slotType;
		this.playerEntity = playerEntity;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { // getSlotTexture
		return Pair.of(InventoryMenu.BLOCK_ATLAS, CharmGourdMenu.ARMOR_SLOT_TEXTURES[slotType.getIndex()]);
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		ItemStack itemstack = this.getItem();
		return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack))
				&& super.mayPickup(playerIn);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.canEquip(this.slotType, this.playerEntity);
	}
}
