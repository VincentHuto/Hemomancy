package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.tile.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MortalDisplayBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Block.box(2, 0, 2, 14, 14, 14);

	public MortalDisplayBlock(Properties properties) {
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
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new MortalDisplayBlockEntity(p_153215_, p_153216_);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	private InteractionResult handleInteraction(Level worldIn, BlockPos pos, Player player) {

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		if (!volume.isActive()) {
			volume.setActive(true);
			player.displayClientMessage(
					Component.translatable("hemomancy.mortal_display.activated")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer, Hemomancy.rloc("hemomancy/the_first_awakening"));
			}
			for (int i = 0; i < 10; i++) {
				Vec3 startVec = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.5, 0.5);
				Vec3 endVec = player.position().add(0, player.getBbHeight() - worldIn.random.nextDouble(), 0).add(
						worldIn.random.nextDouble() - worldIn.random.nextDouble(), 0,
						worldIn.random.nextDouble() - worldIn.random.nextDouble());
				if (!worldIn.isClientSide) {
					PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f, (ServerLevel) worldIn);
					HLPacketHandler.sendLightningSpawn(startVec, endVec, 64.0f, player.level().dimension(),
							ParticleColor.RED, 2, 20, 9, 1.2f);
				}
			}

			// Equip the Charm of Vascularium into the player's VASC scar slot
			if (!worldIn.isClientSide) {
				HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
					ItemStack charm = new ItemStack(ItemInit.charm_of_vascularium.get());
					int vascSlot = 5; // ScarType.VASC slot
					if (scars.getStackInSlot(vascSlot).isEmpty()) {
						scars.setStackInSlot(vascSlot, charm);
						player.displayClientMessage(
								Component.translatable("hemomancy.mortal_display.charm_equipped")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
					} else {
						// Slot occupied — drop the charm as an item entity instead
						ItemEntity drop = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, charm);
						worldIn.addFreshEntity(drop);
					}
				});
			}

		} else {
			ItemEntity drops = new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					new ItemStack(ItemInit.bloody_flask.get(), worldIn.random.nextInt(4)));
			worldIn.explode(player, pos.getX(), pos.getY(), pos.getZ(), 4.0F, ExplosionInteraction.BLOCK);
			worldIn.addFreshEntity(drops);
		}

		return InteractionResult.SUCCESS;

	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(worldIn, pos, player);
		return ItemInteractionResult.SUCCESS;
	}

}
