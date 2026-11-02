package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.CardinalFocusBlock;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivation;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class SanguineFormationItem extends Item {
	public SanguineFormationItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player clicker = context.getPlayer();
		BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
		if (clicker != null && clicker.isShiftKeyDown() && clicked.getBlock() instanceof CardinalFocusBlock) {
			// Vanilla skips the block's own use when the off hand holds anything that does not bypass the sneak, so the
			// Formation hands the sneak-click to the Focus here: it seats as a medium whatever the off hand holds.
			BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
					context.getClickedPos(), context.isInside());
			return clicked.useItemOn(context.getItemInHand(), context.getLevel(), clicker, context.getHand(), hit)
					.result();
		}
		if (!context.getLevel().isClientSide
				&& context.getPlayer() instanceof ServerPlayer player) {
			CardinalRiteActivationRules.ActivationAttempt attempt =
					CardinalRiteActivation.tryStart(
							player, context.getClickedPos(),
							CardinalRiteActivationRules.Trigger.SANGUINE_FORMATION_BLOCK_USE);
			if (attempt.shouldConsumeActivator(player.getAbilities().instabuild)) {
				context.getItemInHand().shrink(1);
			}
			if (attempt.handled()) return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	/**
	 * A sneak-click on a Cardinal Focus reaches the Focus, which seats the Formation as a rite medium; without this
	 * the sneak skips the block and the item tries the floor again.
	 */
	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return level.getBlockState(pos).getBlock() instanceof CardinalFocusBlock;
	}
}
