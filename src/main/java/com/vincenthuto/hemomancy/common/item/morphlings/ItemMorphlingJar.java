package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.screen.item.living.MorphlingJarViewerScreen;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarType;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.server.level.ServerPlayer;

public class ItemMorphlingJar extends Item implements IScar {

	String name;
	Integer size;
	Rarity rarity;

	public ItemMorphlingJar(String name, Integer size, Rarity rarity) {
		super(new Item.Properties().stacksTo(1).rarity(rarity));
		this.name = name;
		this.size = size;
		this.rarity = rarity;
	}

	@Override
	public ScarType getScarType() {
		return ScarType.JAR;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		IItemHandler jarHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
		if (jarHandler instanceof MorphlingJarItemHandler mjh) {
			mjh.load();
			boolean hasAny = false;
			for (int i = 0; i < jarHandler.getSlots(); i++) {
				ItemStack contained = jarHandler.getStackInSlot(i);
				if (!contained.isEmpty()) {
					tooltip.add(Component.literal("§7• ").append(contained.getHoverName()));
					hasAny = true;
				}
			}
			if (!hasAny)
				tooltip.add(Component.translatable("hemomancy.jar.empty").withStyle(s -> s.withColor(0x888888)));
		}
	}


	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack held = playerIn.getItemInHand(handIn);

		if (worldIn.isClientSide) {
			// Light click → open the morph selection viewer
			if (!playerIn.isShiftKeyDown()) {
				MorphlingJarViewerScreen.openScreenViaItem();
				playerIn.playSound(SoundEvents.GLASS_PLACE, 0.40f, 1F);
			}
		} else {
			// Shift-click server-side → open the insertion menu
			if (playerIn.isShiftKeyDown() && playerIn instanceof ServerPlayer sp) {
				sp.openMenu(new MenuProvider() {
					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
						return new MorphlingJarMenu(windowId, p.level(),
								p.blockPosition(), inv, p);
					}

					@Override
					public Component getDisplayName() {
						return held.getHoverName();
					}
				});
			}
		}
		return InteractionResultHolder.success(held);
	}
}