package com.huto.hemomancy.recipe;

import com.huto.hemomancy.item.armor.ItemArmBanner;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

public class ArmBannerCraftRecipe extends CustomRecipe {
	public ArmBannerCraftRecipe(ResourceLocation idIn) {
		super(idIn);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		ItemStack itemstack = ItemStack.EMPTY;
		ItemStack itemstack1 = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itemstack2 = inv.getStackInSlot(i);
			if (!itemstack2.isEmpty()) {
				if (itemstack2.getItem() instanceof BannerItem) {
					if (!itemstack1.isEmpty()) {
						return false;
					}

					itemstack1 = itemstack2;
				} else {
					if (!(itemstack2.getItem() instanceof ItemArmBanner)) {
						return false;
					}

					if (!itemstack.isEmpty()) {
						return false;
					}

					if (itemstack2.getChildTag("BlockEntityTag") != null) {
						return false;
					}

					itemstack = itemstack2;
				}
			}
		}

		return !itemstack.isEmpty() && !itemstack1.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack itemstack = ItemStack.EMPTY;
		ItemStack itemstack1 = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itemstack2 = inv.getStackInSlot(i);
			if (!itemstack2.isEmpty()) {
				if (itemstack2.getItem() instanceof BannerItem) {
					itemstack = itemstack2;
				} else if (itemstack2.getItem() instanceof ItemArmBanner) {
					itemstack1 = itemstack2.copy();
				}
			}
		}

		if (itemstack1.isEmpty()) {
			return itemstack1;
		} else {
			CompoundNBT compoundnbt = itemstack.getChildTag("BlockEntityTag");
			CompoundNBT compoundnbt1 = compoundnbt == null ? new CompoundNBT() : compoundnbt.copy();
			compoundnbt1.putInt("Base", ((BannerItem) itemstack.getItem()).getColor().getId());
			itemstack1.setTagInfo("BlockEntityTag", compoundnbt1);
			return itemstack1;
		}
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@ObjectHolder("hemomancy:arm_banner_craft")
	public static SpecialRecipeSerializer<ArmBannerCraftRecipe> SERIALIZER;

}