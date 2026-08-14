package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.tile.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
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
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class MortalDisplayBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE_N = Block.box(2, 0, 2, 14, 14, 14);

	public MortalDisplayBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));

	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
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
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;
		if (!(worldIn.getBlockEntity(pos) instanceof MortalDisplayBlockEntity display)
				|| display.getLinkedHermit() == null) {
			player.displayClientMessage(Component.literal(
					"This heart has no living oath to answer.").withStyle(ChatFormatting.GRAY), false);
			return InteractionResult.SUCCESS;
		}
		if (!TempleOathRules.canClaimHeart(display.getLinkedHermit(),
				TempleOathRules.blessedHermit(player), display.isClaimed())) {
			player.displayClientMessage(Component.literal(display.isClaimed()
							? "The heart has already been claimed."
							: "Speak with this temple's hermit and accept their blessing first.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return InteractionResult.SUCCESS;
		}

		display.claim(player.getUUID());
		TempleOathRules.recordHeartClaim(player, display.getLinkedHermit());
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			for (int i = 0; i < 10; i++) {
				Vec3 startVec = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.5, 0.5);
				Vec3 endVec = player.position().add(0, player.getBbHeight() - worldIn.random.nextDouble(), 0).add(
						worldIn.random.nextDouble() - worldIn.random.nextDouble(), 0,
						worldIn.random.nextDouble() - worldIn.random.nextDouble());
				PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f, (ServerLevel) worldIn);
				HLPacketHandler.sendLightningSpawn(startVec, endVec, 64.0f, player.level().dimension(),
						ParticleColor.RED, 2, 20, 9, 1.2f);
			}

			// Equip the Charm of Vascularium into the player's VASC scar slot
			HemoCapabilityAccess.getEquipment(player).ifPresent(scars -> {
					ItemStack charm = new ItemStack(ItemInit.charm_of_vascularium.get());
					int vascSlot = 5; // HarbingerEquipmentType.VASC slot
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
			player.displayClientMessage(Component.literal(
					"The heart is yours. The charm remains dormant until the temple rite is completed.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}

		return InteractionResult.SUCCESS;

	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(state, worldIn, pos, player);
		return ItemInteractionResult.SUCCESS;
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

}
