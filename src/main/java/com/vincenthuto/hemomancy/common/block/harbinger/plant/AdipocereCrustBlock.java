package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.Map;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

/**
 * Corpse wax crusted on the wall of an old dark room: a monster room, a stronghold, an ancient city. Scraped once, by
 * hand or with any tool, it comes away as adipocere (two with an axe) and does not grow back. {@link #FACING} is the
 * way it faces out of its wall. Grown by the decoration pass ({@code AdipocereCrustDecoration}), or crafted from four
 * adipocere and hung on any sturdy wall for decor; broken, it drops itself.
 * Model from tools/model_export/adipocere_crust.py.
 */
public class AdipocereCrustBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(
			ModelShapes.of(new double[] {2, 1, 14, 14, 15, 16}));
	private static final ParticleColor WAX = new ParticleColor(214, 204, 168);
	private static final ParticleColor RIND = new ParticleColor(122, 104, 74);

	public AdipocereCrustBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos wall = pos.relative(facing.getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return state;
	}

	/** Hangs on the wall clicked, or failing that on the nearest wall the placer faces, like a wall torch. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		for (Direction toward : context.getNearestLookingDirections()) {
			if (!toward.getAxis().isHorizontal()) continue;
			BlockState state = defaultBlockState().setValue(FACING, toward.getOpposite());
			if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
		}
		return null;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		scrape(state, level, pos, player, 1, false);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		boolean axe = stack.canPerformAction(ItemAbilities.AXE_SCRAPE);
		if (!axe && !stack.is(Tags.Items.TOOLS)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (axe && !level.isClientSide) {
			stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
		}
		scrape(state, level, pos, player, axe ? 2 : 1, axe);
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Lifts the crust off its wall for {@code wax} adipocere; the wall is bare afterwards. */
	private static void scrape(BlockState state, Level level, BlockPos pos, Player player, int wax, boolean axe) {
		level.playSound(player, pos, axe ? SoundEvents.AXE_SCRAPE : SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS,
				1.0f, axe ? 0.8f : 0.9f);
		if (!(level instanceof ServerLevel server)) return;
		Direction facing = state.getValue(FACING);
		double x = pos.getX() + 0.5 - facing.getStepX() * 0.38;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5 - facing.getStepZ() * 0.38;
		server.sendParticles(HemoParticleData.bloodCell(WAX), x, y, z, 10, 0.22, 0.3, 0.22, 0.01);
		server.sendParticles(HemoParticleData.bloodCell(RIND), x, y, z, 4, 0.18, 0.25, 0.18, 0.01);
		server.removeBlock(pos, false);
		Block.popResourceFromFace(server, pos, facing, new ItemStack(ItemInit.adipocere.get(), wax));
	}
}
