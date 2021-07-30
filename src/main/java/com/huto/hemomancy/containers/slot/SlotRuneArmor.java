package com.huto.hemomancy.containers.slot;

import javax.annotation.Nullable;

import com.huto.hemomancy.container.PlayerExpandedContainer;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.canEquip(this.slotType, this.playerEntity);
	}

	@Override
	public boolean canTakeStack(Player playerIn) {
		ItemStack itemstack = this.getStack();
		return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack))
				&& super.canTakeStack(playerIn);
	}

	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getBackground() { // getSlotTexture
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
				PlayerExpandedContainer.ARMOR_SLOT_TEXTURES[slotType.getIndex()]);
	}
}
