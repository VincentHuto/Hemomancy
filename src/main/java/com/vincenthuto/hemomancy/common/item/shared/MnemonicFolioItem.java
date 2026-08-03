package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.menu.MnemonicFolioMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class MnemonicFolioItem extends Item {
	public MnemonicFolioItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack folio = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new MenuProvider() {
				@Override public Component getDisplayName() { return folio.getHoverName(); }
				@Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player owner) {
					return new MnemonicFolioMenu(id, inventory, folio);
				}
			});
			player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.4F, 1.0F);
		}
		return InteractionResultHolder.sidedSuccess(folio, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.hemomancy.mnemonic_folio.tooltip")
				.withStyle(ChatFormatting.GRAY));
	}
}
