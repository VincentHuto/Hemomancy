package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HematicIronBarsBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BloodCrystalBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HematicStakeBlockEntity;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.UUID;

public class HematicStakeBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

	public HematicStakeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(FACE, AttachFace.FLOOR));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HematicStakeBlockEntity(pos, state);
	}

	public HematicStakeBlock() {
		this(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.noCollission()
				.instabreak()
				.sound(SoundType.METAL)
				.noOcclusion());
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = stateForAttachment(context.getClickedFace(), context.getHorizontalDirection().getOpposite());
		if (context.getLevel() instanceof ServerLevel level
				&& context.getPlayer() instanceof ServerPlayer player
				&& !canPlaceStake(level, player, context.getClickedPos(), state)) {
			player.displayClientMessage(Component.translatable("block.hemomancy.hematic_stake.invalid")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
			return null;
		}
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FACE);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos supportPos = getSupportPos(pos, state);
		Direction supportFace = getSupportFace(state);
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, supportFace);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
				: net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel serverLevel && placer instanceof ServerPlayer player) {
			registerStake(serverLevel, player, pos);
		}
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			removeStake(serverLevel, pos);
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	public static boolean canPlaceStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		return canPlaceStake(level, player, pos, BlockInit.hematic_stake.get().defaultBlockState());
	}

	private static boolean canPlaceStake(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
		if (!isProgenitor(player) || !level.isEmptyBlock(pos) || !state.canSurvive(level, pos)) {
			return false;
		}
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		UUID owner = bloodline.getLeaderUUID();
		int budget = configuredStakeBudget(
				bloodline.getPlayerUUIDS().size(),
				bloodline.getNpcMemberCount());
		return FoundingFaneSavedData.get(level).canAddStake(owner, pos, budget);
	}

	@Nullable
	public static Formation findFormation(ServerLevel level, ServerPlayer player, BlockPos barPos, BlockPos crystalPos) {
		BlockState bar = level.getBlockState(barPos);
		BlockState crystal = level.getBlockState(crystalPos);
		Direction direction = Direction.fromDelta(
				crystalPos.getX() - barPos.getX(),
				crystalPos.getY() - barPos.getY(),
				crystalPos.getZ() - barPos.getZ());
		if (direction == null
				|| !(bar.getBlock() instanceof HematicIronBarsBlock)
				|| !crystal.is(BlockInit.blood_crystal.get())
				|| bar.getValue(HematicIronBarsBlock.AXIS) != direction.getAxis()
				|| crystal.getValue(BloodCrystalBlock.FACING) != direction) {
			return null;
		}

		BlockState stake = stateForAttachment(direction, Direction.NORTH);
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		int budget = configuredStakeBudget(bloodline.getPlayerUUIDS().size(), bloodline.getNpcMemberCount());
		if (!isProgenitor(player)
				|| !stake.canSurvive(level, barPos)
				|| !FoundingFaneSavedData.get(level).canAddStake(bloodline.getLeaderUUID(), barPos, budget)) {
			return null;
		}
		return new Formation(barPos.immutable(), crystalPos.immutable(), stake, bloodline.getLeaderUUID(), budget);
	}

	public static boolean placeFormation(ServerLevel level, Formation formation) {
		FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
		BlockState bar = level.getBlockState(formation.pos());
		BlockState crystal = level.getBlockState(formation.crystalPos());
		Direction direction = Direction.fromDelta(
				formation.crystalPos().getX() - formation.pos().getX(),
				formation.crystalPos().getY() - formation.pos().getY(),
				formation.crystalPos().getZ() - formation.pos().getZ());
		if (direction == null
				|| !(bar.getBlock() instanceof HematicIronBarsBlock)
				|| !crystal.is(BlockInit.blood_crystal.get())
				|| bar.getValue(HematicIronBarsBlock.AXIS) != direction.getAxis()
				|| crystal.getValue(BloodCrystalBlock.FACING) != direction
				|| !formation.state().canSurvive(level, formation.pos())
				|| !data.addStake(formation.owner(), formation.pos(), formation.budget())) {
			return false;
		}
		if (level.setBlock(formation.pos(), formation.state(), Block.UPDATE_ALL)) {
			return true;
		}
		data.removeStake(formation.owner(), formation.pos());
		return false;
	}

	public static boolean isProgenitor(ServerPlayer player) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		return bloodline.isValid() && player.getUUID().equals(bloodline.getLeaderUUID());
	}

	private static void registerStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		if (!bloodline.isValid()) {
			return;
		}
		UUID owner = bloodline.getLeaderUUID();
		int budget = configuredStakeBudget(
				bloodline.getPlayerUUIDS().size(),
				bloodline.getNpcMemberCount());
		FoundingFaneSavedData.get(level).addStake(owner, pos, budget);
	}

	private static BlockPos getSupportPos(BlockPos pos, BlockState state) {
		return switch (state.getValue(FACE)) {
			case CEILING -> pos.above();
			case FLOOR -> pos.below();
			case WALL -> pos.relative(state.getValue(FACING).getOpposite());
		};
	}

	private static BlockState stateForAttachment(Direction clickedFace, Direction horizontalFallback) {
		AttachFace attachFace;
		Direction horizontal;
		if (clickedFace.getAxis().isHorizontal()) {
			attachFace = AttachFace.WALL;
			horizontal = clickedFace;
		} else {
			attachFace = clickedFace == Direction.DOWN ? AttachFace.CEILING : AttachFace.FLOOR;
			horizontal = horizontalFallback;
		}
		return BlockInit.hematic_stake.get().defaultBlockState()
				.setValue(FACING, horizontal)
				.setValue(FACE, attachFace);
	}

	private static Direction getSupportFace(BlockState state) {
		return switch (state.getValue(FACE)) {
			case CEILING -> Direction.DOWN;
			case FLOOR -> Direction.UP;
			case WALL -> state.getValue(FACING);
		};
	}

	private static int configuredStakeBudget(int playerMembers, int npcMembers) {
		int earned = FaneFootprint.BASE_STAKE_BUDGET + Math.max(0, playerMembers) + Math.max(0, npcMembers);
		int cap = HemoServerConfig.FANE_MAX_STAKE_BUDGET != null
				? HemoServerConfig.FANE_MAX_STAKE_BUDGET.get()
				: FaneFootprint.MAX_STAKE_BUDGET;
		return Math.min(cap, earned);
	}

	private static void removeStake(ServerLevel level, BlockPos pos) {
		FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
		UUID owner = data.findOwnerForStake(pos);
		if (owner != null) {
			data.removeStake(owner, pos);
		}
	}

	public record Formation(BlockPos pos, BlockPos crystalPos, BlockState state, UUID owner, int budget) {
	}
}
