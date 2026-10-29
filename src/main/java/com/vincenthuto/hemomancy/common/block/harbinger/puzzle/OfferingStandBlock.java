package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.OfferingStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A Litany wing's offering stand: a bone stem holding one of the Litany's candidates, so a challenger finds all six on
 * the way and the puzzle is which three, in what order. Use takes one; the same item given back returns it.
 * {@link #STYLE} picks the cut: the bone-stemmed stand, or Velorum's frozen bowl, which stands straight on a refectory
 * table top (the block below) and holds its candidate on its skin of ice. Unbreakable. Models from
 * tools/model_export/offering_stand.py.
 */
public class OfferingStandBlock extends Block implements EntityBlock {
	/** The stand's cut; the renderer rests the candidate at that cut's own dish height. */
	public enum Style implements StringRepresentable {
		STAND, BOWL;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);
	private static final VoxelShape SHAPE = ModelShapes.of(new double[] {3, 0, 3, 13, 4.75, 13},
			new double[] {5.75, 4.75, 5.75, 10.25, 11.75, 10.25}, new double[] {3, 11.75, 3, 13, 16, 13});
	private static final VoxelShape BOWL_SHAPE = ModelShapes.of(new double[] {5, 0, 5, 11, 1, 11},
			new double[] {4, 1, 4, 12, 2.25, 12}, new double[] {3, 2.25, 3, 13, 5.25, 13});

	public OfferingStandBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(STYLE, Style.STAND));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STYLE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(STYLE) == Style.BOWL ? BOWL_SHAPE : SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OfferingStandBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof OfferingStandBlockEntity stand)) return InteractionResult.PASS;
		if (level.isClientSide) return stand.held().isEmpty() ? InteractionResult.PASS : InteractionResult.SUCCESS;
		ItemStack taken = stand.take();
		if (taken.isEmpty()) return InteractionResult.PASS;
		if (!player.getInventory().add(taken)) player.drop(taken, false);
		return InteractionResult.CONSUME;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof OfferingStandBlockEntity stand)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!stand.accepts(stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide) return ItemInteractionResult.SUCCESS;
		stand.give(stack, player);
		return ItemInteractionResult.CONSUME;
	}
}
