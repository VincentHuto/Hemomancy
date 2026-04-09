package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.AltarOfCleansingBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Altar of Cleansing — a sacred altar found in Unstained temples, blessed by
 * Our Lady of Lethe. When a player on the Unstained path interacts with it
 * using Tears of Lethe, they receive a large one-time purity boost and
 * advancement progress. The altar can only be used once per player.
 */
public class AltarOfCleansingBlock extends Block implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 12, 15);

	/** One-time purity boost granted by the altar. */
	private static final float ALTAR_PURITY_BOOST = 25.0f;

	public AltarOfCleansingBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AltarOfCleansingBlockEntity(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		ItemStack stack = player.getItemInHand(handIn);

		player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification()) {
				player.displayClientMessage(
						Component.translatable("hemomancy.altar.not_on_path"), false);
				return;
			}

			if (stack.getItem() == ItemInit.tears_of_lethe.get()) {
				handleTearsOfLethe(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.lethean_poppy_wreath.get()) {
				handlePoppyWreath(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.silver_chalice.get()) {
				handleSilverChalice(worldIn, pos, player, stack, unstained);
			} else {
				// Empty-hand interaction — show lore
				player.displayClientMessage(
						Component.translatable("hemomancy.altar.lore"), false);
			}
		});

		return InteractionResult.SUCCESS;
	}

	private void handleTearsOfLethe(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (unstained.hasUsedAltarOfCleansing()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.already_blessed"), false);
			return;
		}

		// One-time large purity boost
		stack.shrink(1);
		unstained.addPurity(ALTAR_PURITY_BOOST);
		unstained.setUsedAltarOfCleansing(true);
		unstained.addAdvancementEarned();

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.blessed"), false);
		worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1.2f);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
		spawnBlessingParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void handlePoppyWreath(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		// Offering a wreath grants a small purity boost (repeatable)
		stack.shrink(1);
		unstained.addPurity(5.0f);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.wreath_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
		spawnPurityParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void handleSilverChalice(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		// Offering a chalice at the altar grants clarity progress (requires clarity unlocked)
		if (!unstained.hasClarityUnlocked()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.clarity_not_unlocked"), false);
			return;
		}
		unstained.addClarity(5.0f);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.chalice_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
		spawnClarityParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void spawnBlessingParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					40, 0.5, 1.0, 0.5, 0.05);
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnPurityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					15, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnClarityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}
}
