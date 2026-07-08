package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.MemoryOfVesperItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class MemoryOfVesperItem extends Item implements HemoClientItemExtensionsProvider {
	public MemoryOfVesperItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.memory_of_vesper.tooltip")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.hemomancy.memory_of_vesper.tooltip.graft")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.rite_guidance")
				.withStyle(ChatFormatting.DARK_RED), false);
		return InteractionResultHolder.sidedSuccess(stack, false);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MemoryOfVesperItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}
}
