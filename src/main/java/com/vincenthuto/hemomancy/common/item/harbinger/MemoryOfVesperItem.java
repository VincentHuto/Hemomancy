package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.MemoryOfVesperItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return super.use(level, player, hand);
		}
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.inactive")
					.withStyle(ChatFormatting.DARK_RED), true);
			return InteractionResultHolder.fail(stack);
		}

		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress == null || !progress.hasLivingStaffBond()) {
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.no_bond")
					.withStyle(ChatFormatting.DARK_RED), true);
			return InteractionResultHolder.fail(stack);
		}
		if (progress.isVesperMemoryAwakened()) {
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.already_awakened")
					.withStyle(ChatFormatting.DARK_RED), true);
			return InteractionResultHolder.fail(stack);
		}

		if (progress.awakenVesperMemory()) {
			stack.shrink(1);
			if (player instanceof ServerPlayer serverPlayer) {
				LivingStaffBondHelper.syncProgress(serverPlayer);
			}
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.awakened")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
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
