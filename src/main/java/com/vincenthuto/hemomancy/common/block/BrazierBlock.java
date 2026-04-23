package com.vincenthuto.hemomancy.common.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.item.OrganEchoItem;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Iron Brazier block — a versatile ritual apparatus. In its base form it
 * can be lit and extinguished by hand. When used for organ echo upgrades,
 * the ritual phase property tracks the brazier's state:
 * <ul>
 *   <li><b>0</b> — Unlit (cold)</li>
 *   <li><b>1</b> — Normal fire (lit)</li>
 *   <li><b>2</b> — Sanguine flames (reagents accepted, awaiting echo)</li>
 * </ul>
 */
public class BrazierBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	/**
	 * Ritual phase: 0 = unlit, 1 = normal fire, 2 = sanguine flames (awaiting echo).
	 */
	public static final IntegerProperty RITUAL_PHASE = IntegerProperty.create("ritual_phase", 0, 2);

	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(4, 0, 4, 12, 2, 12), Block.box(5, 2, 5, 11, 4, 11), Block.box(6, 4, 6, 10, 13, 10),
					Block.box(5, 13, 5, 11, 16, 11), Block.box(6, 15, 6, 10, 17, 10), Block.box(5, 14, 11, 11, 17, 12),
					Block.box(11, 14, 5, 12, 17, 11), Block.box(5, 14, 4, 11, 17, 5), Block.box(4, 14, 5, 5, 17, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public BrazierBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(RITUAL_PHASE, 0));
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		super.animateTick(pState, pLevel, pPos, pRandom);
		double d0 = (double) pPos.getX() + 0.5D;
		double d1 = (double) pPos.getY() + 1.2D;
		double d2 = (double) pPos.getZ() + 0.5D;
		int phase = pState.getValue(RITUAL_PHASE);

		if (phase == 1) {
			// Normal fire — standard smoke
			pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		} else if (phase == 2) {
			// Sanguine flames — dark red blood particles swirling upward
			for (int i = 0; i < 3; i++) {
				double ox = (pRandom.nextDouble() - 0.5) * 0.4;
				double oz = (pRandom.nextDouble() - 0.5) * 0.4;
				pLevel.addParticle(
						BloodCellParticleFactory.createData(new ParticleColor(180, 0, 20)),
						d0 + ox, d1 + pRandom.nextDouble() * 0.3, d2 + oz,
						0.0D, 0.04D, 0.0D);
			}
			pLevel.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0D, 0.02D, 0.0D);
		}
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, RITUAL_PHASE);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(RITUAL_PHASE, 0);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING))).setValue(RITUAL_PHASE, 0);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new IronBrazierBlockEntity(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) return null;
		return (BlockEntityTicker<T>) (lvl, pos, st, te) -> {
			if (te instanceof IronBrazierBlockEntity brazierTE) {
				IronBrazierBlockEntity.serverTick(lvl, pos, st, brazierTE);
			}
		};
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(RITUAL_PHASE, 0);
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player,
			ItemStack stack) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		int phase = state.getValue(RITUAL_PHASE);

		// === Phase 0: Unlit — light the brazier ===
		if (phase == 0) {
			if (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE) {
				worldIn.setBlock(pos, state.setValue(RITUAL_PHASE, 1), 3);
				return InteractionResult.SUCCESS;
			}
			if (stack.isEmpty()) {
				worldIn.setBlock(pos, state.setValue(RITUAL_PHASE, 1), 3);
				player.hurt(player.damageSources().generic(), 1.5f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos,
							BloodCellParticleFactory.createData(ParticleColor.BLOOD));
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// === Phase 1: Normal lit — accept reagents or extinguish ===
		if (phase == 1) {
			// Try accepting reagent items for the organ rite
			BlockEntity be = worldIn.getBlockEntity(pos);
			if (be instanceof IronBrazierBlockEntity brazierTE) {
				if (!stack.isEmpty() && brazierTE.tryAcceptReagent(player, stack, worldIn, pos, state)) {
					return InteractionResult.SUCCESS;
				}
			}

			// Empty hand sneak = extinguish
			if (stack.isEmpty() && player.isShiftKeyDown()) {
				worldIn.setBlock(pos, state.setValue(RITUAL_PHASE, 0), 3);
				player.hurt(player.damageSources().generic(), 1f);
				if (!worldIn.isClientSide) {
					HLParticleUtils.spawnPoof((ServerLevel) worldIn, pos, ParticleTypes.SMOKE);
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// === Phase 2: Sanguine flames — accept echo item for upgrade ===
		if (phase == 2) {
			BlockEntity be = worldIn.getBlockEntity(pos);
			if (be instanceof IronBrazierBlockEntity brazierTE) {
				if (!stack.isEmpty() && stack.getItem() instanceof OrganEchoItem) {
					brazierTE.tryAcceptEcho(player, stack, worldIn, pos, state);
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.PASS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player, stack) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	/**
	 * Convenience to check whether the brazier is in sanguine flame state.
	 */
	public static boolean isSanguineFlame(BlockState state) {
		return state.getBlock() instanceof BrazierBlock && state.getValue(RITUAL_PHASE) == 2;
	}

	/**
	 * Returns true if the brazier is lit (any fire state).
	 */
	public static boolean isLit(BlockState state) {
		return state.getBlock() instanceof BrazierBlock && state.getValue(RITUAL_PHASE) > 0;
	}
}
