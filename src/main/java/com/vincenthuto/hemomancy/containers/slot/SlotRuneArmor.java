package com.vincenthuto.hemomancy.containers.slot;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.container.PlayerExpandedContainer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SlotRuneArmor extends Slot {

	private EquipmentSlot slotType;
	private Player playerEntity;

	public SlotRuneArmor(Container inventoryIn, int index, int xPosition, int yPosition, EquipmentSlot slotType,
			Player playerEntity) {
		super(inventoryIn, index, xPosition, yPosition);
		this.slotType = slotType;
		this.playerEntity = playerEntity;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.canEquip(this.slotType, this.playerEntity);
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		ItemStack itemstack = this.getItem();
		return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack))
				&& super.mayPickup(playerIn);
	}

	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { // getSlotTexture
		return Pair.of(InventoryMenu.BLOCK_ATLAS, PlayerExpandedContainer.ARMOR_SLOT_TEXTURES[slotType.getIndex()]);
	}
}
