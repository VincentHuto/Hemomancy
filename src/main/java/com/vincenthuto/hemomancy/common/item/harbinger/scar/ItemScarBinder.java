package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.menu.ScarBinderInventoryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
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

import javax.annotation.Nullable;
import java.util.List;

public class ItemScarBinder extends Item {

	String name;
	Integer size;
	Rarity rarity;

	public ItemScarBinder(String name, Integer size, Rarity rarity) {
		super(new Item.Properties().stacksTo(1));
		this.name = name;
		this.size = size;
		this.rarity = rarity;
	}

	public Rarity getRarity(ItemStack stack) {
		return rarity;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack heldStack = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide) {
			if (!playerIn.isShiftKeyDown()) {
			//	Hemomancy.proxy.openBinderGui();
				ClientBridge.get().openScarBinderViewer(heldStack);
				playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
			}
		}

		if (!worldIn.isClientSide) {
			if (playerIn.isShiftKeyDown()) {
				// open
				playerIn.openMenu(new MenuProvider() {
					@Override
					public Component getDisplayName() {
						return playerIn.getItemInHand(handIn).getHoverName();
					}

					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new ScarBinderInventoryMenu(windowId, p_createMenu_3_.level(),
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
					}
				});

			}
		}
		return InteractionResultHolder.success(playerIn.getItemInHand(handIn));

	}

	public boolean filterItem(ItemStack item, ItemStack packItem) {
		return item.getItem() instanceof ItemScarPattern;

	}


	private boolean hasTranslation(String key) {
		return ClientBridge.get().hasTranslation(key);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		String translationKey = getDescriptionId();


		if (ClientBridge.get().hasShiftDown()) {
			tooltip.add(Component.translatable(ClientBridge.get().translate(translationKey + ".info")));
			if (hasTranslation(translationKey + ".info2"))
				tooltip.add(Component.translatable(ClientBridge.get().translate(translationKey + ".info2")));
			if (hasTranslation(translationKey + ".info3"))
				tooltip.add(Component.translatable(ClientBridge.get().translate(translationKey + ".info3")));
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.item_scar_binder.press_shift_for_info"));
		}
	}
}
