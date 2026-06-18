package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
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

import java.util.List;

public class HarbingerAssignmentLedgerItem extends ItemGuideBook {
	public HarbingerAssignmentLedgerItem(Properties properties, ResourceLocation texture) {
		super(properties, texture);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new OpenHarbingerAssignmentLedgerPacket(
					HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							Hemomancy.rloc("hemomancy/the_first_awakening")),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED),
					HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(serverPlayer),
					HarbingerAdvancementGranter.isRedTaxonomyComplete(serverPlayer),
					hasBlankHematicMemory(serverPlayer),
					HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer)));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	private static boolean hasBlankHematicMemory(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.is(ItemInit.hematic_memory.get()));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
