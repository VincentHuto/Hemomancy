package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class VascularStatusGaugeItem extends Item {

	public VascularStatusGaugeItem(Properties properties) {
		super(properties);
	}

	/**
	 * On a blood machine, the gauge reads its tank: fresh and clotted blood, and whether a suture feeds it. It runs before
	 * the machine's own use, so the reading comes instead of the machine's screen.
	 */
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		BlockEntity blockEntity = reservoirAt(level, context.getClickedPos());
		if (blockEntity == null) return InteractionResult.PASS;
		if (level instanceof ServerLevel serverLevel && context.getPlayer() != null) {
			for (Component line : ReservoirReadout.lines(serverLevel, blockEntity))
				context.getPlayer().sendSystemMessage(line);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	private static BlockEntity reservoirAt(Level level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null)
			blockEntity = level.getBlockEntity(filler.getMainBlockPos());
		return blockEntity instanceof IBloodReservoir ? blockEntity : null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			ClientBridge.get().openVascularStatus();
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.hemomancy.vascular_status_gauge.right_click_to_view_your_vascular").withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.translatable("tooltip.hemomancy.vascular_status_gauge.shows_the_health_of_each_vein").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.hemomancy.vascular_status_gauge.reads_machines").withStyle(ChatFormatting.GRAY));
	}
}
