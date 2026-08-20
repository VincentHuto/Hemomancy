package com.vincenthuto.hemomancy.common.item.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mission.OpenBookOfObservancesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import java.util.List;

/** Portable journal of NPC-directed Unstained observances. */
public class BookOfObservancesItem extends ItemGuideBook {
	public BookOfObservancesItem(Properties properties, ResourceLocation texture) { super(properties, texture); }

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
				int availableMask = 0;
				int readyMask = 0;
				for (UnstainedObservances.Observance observance : UnstainedObservances.Observance.values()) {
					if (UnstainedObservances.isAvailable(progress, observance)) {
						availableMask |= observance.mask();
					}
					if ((progress.getAcceptedObservances() & observance.mask()) != 0
							&& (progress.getClaimedObservances() & observance.mask()) == 0
							&& UnstainedObservances.isReady(serverPlayer, observance)) {
						readyMask |= observance.mask();
					}
				}
				PacketHandler.sendToPlayer(serverPlayer, new OpenBookOfObservancesPacket(
						progress.getAcceptedObservances(), progress.getClaimedObservances(),
						availableMask, readyMask, progress.getPurity(), progress.getClarity(),
						progress.hasClarityUnlocked()));
			});
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.hemomancy.book_of_observances.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
