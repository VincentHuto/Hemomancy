package com.vincenthuto.hemomancy.common.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.UnstainedPodiumBlockEntity;

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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

public class UnstainedPodiumBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(3, 0, 3, 13, 2, 13), Block.box(3, 12, 3, 13, 14, 13),
			Block.box(3, 9, 3, 13, 12, 13), Block.box(2, 11, 2, 14, 13, 14), Block.box(4, 1, 4, 12, 3, 12),
			Block.box(4, 8, 4, 12, 10, 12), Block.box(5, 2, 5, 11, 9, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public UnstainedPodiumBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
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
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new UnstainedPodiumBlockEntity(arg0, arg1);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {

		worldIn.playSound(player, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.25f, 1f);
		ItemStack stack = player.getItemInHand(handIn);
		if (!player.isShiftKeyDown()) {
			if (stack.getItem() == ItemInit.sanguine_conduit.get()) {
			//	worldIn.destroyBlock(pos, false);
				stack.shrink(1);
			//	worldIn.setBlockAndUpdate(pos, BlockInit.rune_mod_station.get().defaultBlockState());
			}

			if (stack.getItem() == ItemInit.scrying_dish.get()) {
				worldIn.destroyBlock(pos, false);
				stack.shrink(1);
				worldIn.setBlockAndUpdate(pos, BlockInit.scrying_podium.get().defaultBlockState());
			}

			// Unstained path interactions — server-side only
			if (!worldIn.isClientSide) {
				handleUnstainedInteraction(worldIn, pos, player, stack);
			}
		}

		return InteractionResult.SUCCESS;

	}

	private void handleUnstainedInteraction(Level worldIn, BlockPos pos, Player player, ItemStack stack) {
		int degreeNumber = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
		if (degreeNumber < EnumInitiatoryDegree.VOTARY.getNumber()) {
			// Player hasn't reached the required degree yet
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.not_ready"), false);
			return;
		}

		player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
			if (stack.getItem() == ItemInit.hemolytic_solution.get()) {
				handleHemolyticSolution(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.consecrated_copper_ingot.get()) {
				handleConsecratedCopper(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.hemolytic_plating.get()) {
				handleHemolyticPlating(worldIn, pos, player, stack, unstained);
			}
		});
	}

	private void handleHemolyticSolution(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (!unstained.hasBegunPurification()) {
			// Begin purification
			stack.shrink(1);
			unstained.setBegunPurification(true);
			unstained.setPurity(5.0f);
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.purification_begun"), false);
			worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
			spawnPurityParticles(worldIn, pos);
			syncUnstainedProgress(worldIn, player, unstained);
		} else if (!unstained.isPurified()) {
			// Add purity progress
			stack.shrink(1);
			unstained.addPurity(10.0f);
			worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
			spawnPurityParticles(worldIn, pos);
			if (unstained.isPurified()) {
				player.displayClientMessage(
						Component.translatable("hemomancy.unstained.purity_complete"), false);
			} else {
				player.displayClientMessage(
						Component.translatable("hemomancy.unstained.purity_progress",
								unstained.getPurity()),
						false);
			}
			syncUnstainedProgress(worldIn, player, unstained);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.already_purified"), false);
		}
	}

	private void handleConsecratedCopper(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (!unstained.isPurified()) {
			// Not yet purified — cannot perform the ritual
			return;
		}
		if (unstained.hasClarityUnlocked()) {
			// Ritual already done
			return;
		}
		// Perform the Rite of Clarity
		stack.shrink(1);
		unstained.setClarityUnlocked(true);
		// Disable blood magic permanently
		player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
			volume.setActive(false);
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
					new BloodVolumeServerPacket(volume));
		});
		player.displayClientMessage(
				Component.translatable("hemomancy.unstained.clarity_unlocked"), false);
		worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 0.8f);
		spawnClarityParticles(worldIn, pos);
		syncUnstainedProgress(worldIn, player, unstained);
	}

	private void handleHemolyticPlating(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (!unstained.hasClarityUnlocked()) {
			// Clarity not yet unlocked
			return;
		}
		if (unstained.isEnlightened()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.already_enlightened"), false);
			return;
		}
		// Add clarity progress
		stack.shrink(1);
		unstained.addClarity(15.0f);
		worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f);
		spawnClarityParticles(worldIn, pos);
		if (unstained.isEnlightened()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.enlightened"), false);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.clarity_progress",
							unstained.getClarity()),
					false);
		}
		syncUnstainedProgress(worldIn, player, unstained);
	}

	private void spawnPurityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnClarityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					30, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void syncUnstainedProgress(Level worldIn, Player player, IUnstainedProgress unstained) {
		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

}

