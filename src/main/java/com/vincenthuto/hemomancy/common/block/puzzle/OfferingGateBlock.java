package com.vincenthuto.hemomancy.common.block.puzzle;

import com.vincenthuto.hemomancy.common.tile.puzzle.BloodTrialAltarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OfferingGateBlock extends Block {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public OfferingGateBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN) ? Shapes.empty() : super.getCollisionShape(state, level, pos, ctx);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return !state.getValue(OPEN);
    }

    private static final int SCAN_RADIUS = 15;

      private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // If the gate is already open, nothing to do
        if (state.getValue(OPEN)) return InteractionResult.PASS;

        // Find a nearby BloodTrialAltarBlockEntity and activate it
        for (BlockPos p : BlockPos.betweenClosed(
                pos.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                pos.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (level.getBlockEntity(p) instanceof BloodTrialAltarBlockEntity altar) {
                if (altar.isCompleted()) {
                    // Trial already done — no message needed
                    return InteractionResult.PASS;
                }
                if (!altar.isActive()) {
                    altar.activate();
                    player.displayClientMessage(
                            Component.translatable("message.hemomancy.offering_gate.trial_started")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                            false);
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.hemomancy.offering_gate.trial_active")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
                            true);
                }
                return InteractionResult.CONSUME;
            }
        }

        // No altar found — gate is just locked
        player.displayClientMessage(
                Component.literal("The gate won't budge.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                true);
        return InteractionResult.CONSUME;
    }

          @Override
          protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
              BlockHitResult hit) {
            return handleInteraction(state, level, pos, player);
          }

          @Override
          protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
              Player player, InteractionHand hand, BlockHitResult hit) {
            return handleInteraction(state, level, pos, player) == InteractionResult.PASS
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SUCCESS;
          }
}
