package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;

public class GourdBlock extends PumpkinBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 3.0D, 12.0D, 14.0D, 13.0D);

	public GourdBlock(BlockBehaviour.Properties p_55284_) {
		super(p_55284_);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack itemstack, BlockState p_55289_, Level p_55290_, BlockPos p_55291_,
			Player p_55292_, InteractionHand p_55293_, BlockHitResult p_55294_) {
		if (itemstack.canPerformAction(ItemAbilities.SHEARS_CARVE)) {
			if (!p_55290_.isClientSide) {
				Direction direction = p_55294_.getDirection();
				Direction direction1 = direction.getAxis() == Direction.Axis.Y ? p_55292_.getDirection().getOpposite()
						: direction;
				p_55290_.playSound((Player) null, p_55291_, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
				p_55290_.setBlock(p_55291_,
						Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction1), 11);
				ItemEntity itementity = new ItemEntity(p_55290_,
						(double) p_55291_.getX() + 0.5D + (double) direction1.getStepX() * 0.65D,
						(double) p_55291_.getY() + 0.1D,
						(double) p_55291_.getZ() + 0.5D + (double) direction1.getStepZ() * 0.65D,
						new ItemStack(Items.PUMPKIN_SEEDS, 4));
				itementity.setDeltaMovement(
						0.05D * (double) direction1.getStepX() + p_55290_.random.nextDouble() * 0.02D, 0.05D,
						0.05D * (double) direction1.getStepZ() + p_55290_.random.nextDouble() * 0.02D);
				p_55290_.addFreshEntity(itementity);
				itemstack.hurtAndBreak(1, p_55292_, p_55293_ == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND
						: EquipmentSlot.OFFHAND);
				p_55290_.gameEvent(p_55292_, GameEvent.SHEAR, p_55291_);
				p_55292_.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
			}

			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	public StemBlock getStem() {
		return (StemBlock) BlockInit.gourd_stem.get();
	}

	public AttachedStemBlock getAttachedStem() {
		return (AttachedStemBlock) BlockInit.attached_gourd_stem.get();
	}
}
