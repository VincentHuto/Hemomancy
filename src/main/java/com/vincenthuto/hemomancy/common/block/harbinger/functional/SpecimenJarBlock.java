package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenCaptureRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenBrood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The Specimen Jar: a small creature kept in glass (core-loop Track 5). A kept creature lives on Vigor fed from the
 * jar's living buffer; held Absorption draws from it, Projection feeds it, an empty hand opens its screen (or takes
 * out its husk, or puts a led creature in), and an empty vial draws a sample from it. Sneaking with an empty hand picks the jar
 * up with its creature; breaking it lets the creature go.
 */
public class SpecimenJarBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, BlockBloodEndpoint {
	public static final MapCodec<SpecimenJarBlock> CODEC = simpleCodec(SpecimenJarBlock::new);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);

	public SpecimenJarBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
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
		return new SpecimenJarBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.specimen_jar.get(), SpecimenJarBlockEntity::serverTick);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar
				&& SpecimenJarData.hasSpecimen(stack)) {
			jar.kept().loadFromItem(stack, level.getGameTime());
			jar.sendUpdates();
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!player.isShiftKeyDown()) {
			// A kept creature's screen opens only for an empty hand; anything held keeps its own use (a syringe samples).
			if (!SpecimenVessels.opensScreen(player) && level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity held
					&& held.kept().living()) {
				return InteractionResult.PASS;
			}
			if (level.isClientSide) {
				return InteractionResult.SUCCESS;
			}
			if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer
					&& level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar
					&& SpecimenVessels.useEmptyHand(server, pos, jar, serverPlayer, SpecimenJarBlock::fits)) {
				return InteractionResult.CONSUME;
			}
			return InteractionResult.PASS;
		}
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (!(level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar)) {
			return InteractionResult.PASS;
		}
		if (!SpecimenVessels.mayHandle(jar, player)) {
			return InteractionResult.CONSUME;
		}

		ItemStack stack = new ItemStack(this);
		if (jar.hasSpecimen()) {
			jar.kept().saveToItem(stack);
		}
		jar.suppressNextRelease();
		level.removeBlock(pos, false);
		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!(stack.getItem() instanceof BloodVialItem) || com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.isFilled(stack)
				|| !(level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar) || !jar.kept().living()) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
			SpecimenVessels.sample(server, pos, jar, serverPlayer, stack, hand);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Whether a led creature fits the jar: the jar's own kinds and size. */
	public static boolean fits(Mob mob) {
		return SpecimenCaptureRules.judge(new SpecimenCaptureRules.Candidate(mob.getType().is(EntityInit.SPECIMEN_JAR_CAPTURABLE),
				mob.isAlive(), mob.getBbWidth(), mob.getBbHeight(), mob.getMaxHealth(), false, false, mob.isVehicle()))
				== SpecimenCaptureRules.Verdict.CAPTURE;
	}

	@Override
	public boolean canDrawAbsorptionParticles(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar && jar.kept().occupied();
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar ? SpecimenVessels.absorb(level, jar, player) : 0.0D;
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar
				? SpecimenVessels.project(jar, player, pos, maxAmount) : 0.0D;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide
				&& level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar) {
			// Broken or lifted, a jar loses its brood on both sides; what was paid is gone (P6).
			SpecimenBrood.sever(level, jar);
			boolean suppressed = jar.consumeSuppressNextRelease();
			if (!suppressed && jar.hasSpecimen() && level instanceof ServerLevel serverLevel) {
				var specimen = jar.getSpecimenCopy();
				if (jar.kept().husk()) {
					jar.kept().dropHusk(serverLevel, pos);
				} else if (SpecimenJarData.releaseSpecimen(serverLevel, pos, specimen).isEmpty()) {
					Block.popResource(level, pos, SpecimenJarData.createStackWithSpecimen(specimen));
				}
				jar.clearSpecimen();
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		// A failing creature rattling its glass: the client shakes the specimen for a moment.
		if (id == 1 || id == 2) {
			if (level.isClientSide && level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar) {
				// 2: a young quickens in the jar (P6).
				if (id == 1) jar.kept().rattle(level.getGameTime());
				else jar.kept().quicken(level.getGameTime());
			}
			return true;
		}
		return super.triggerEvent(state, level, pos, id, param);
	}
}
