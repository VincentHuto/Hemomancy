package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatAssembly;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The Specimen Vat (core-loop Track 5): two blocks of glass in an iron cage. Empty vats set side by side or one on
 * another join into one octagonal vessel up to three vats across and two high ({@link VatAssembly}); what a vessel
 * holds is set by its clear inside and 40 health a vat ({@link MenagerieRules#vatFits}), so a bigger creature needs a
 * bigger vessel. Lead a creature to an empty vessel on the Tendon Line and use it with an empty hand to put it in. A kept creature does not despawn, wander, path or think; it lives on Vigor fed from the vat's 800 mL
 * living buffer, and gives Temper to held Absorption. An empty hand opens its screen, or takes out its husk; an empty
 * vial draws a sample (a Living Syringe for a thick-hided kind). Sneaking with an empty hand picks a lone vat up with its
 * creature, or one vat out of an empty vessel; breaking any vat of a vessel lets its creature go.
 * The lower half holds the block entity; the upper half is the column's top and answers for it. Both halves carry the
 * vat's {@link VatPart}: a joined vessel is drawn as one octagonal vat grown to its size.
 */
public class SpecimenVatBlock extends BaseEntityBlock implements BlockBloodEndpoint {
	public static final MapCodec<SpecimenVatBlock> CODEC = simpleCodec(SpecimenVatBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	/** What this vat draws: its own column, nothing, or its layer's slice of a joined vessel ({@link VatPart}). */
	public static final EnumProperty<VatPart> PART = EnumProperty.create("part", VatPart.class);
	/** The vessel's floor above its base, in blocks (specimen_vat.py STEP_TOP + 0.1). */
	public static final double FLOOR = 3.3D / 16.0D;
	private static final VoxelShape LOWER = Shapes.or(Block.box(0.2D, 0.0D, 0.2D, 15.8D, 3.2D, 15.8D),
			Block.box(1.0D, 3.2D, 1.0D, 15.0D, 16.0D, 15.0D));
	private static final VoxelShape UPPER = Shapes.or(Block.box(1.0D, 0.0D, 1.0D, 15.0D, 11.4D, 15.0D),
			Block.box(0.6D, 11.4D, 0.6D, 15.4D, 14.0D, 15.4D), Block.box(5.8D, 14.0D, 5.8D, 10.2D, 16.0D, 10.2D));

	public SpecimenVatBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(PART, VatPart.SINGLE));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, PART);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// A vat inside a vessel is walled by its neighbours; it fills its block.
		if (state.getValue(PART) != VatPart.SINGLE) return Shapes.block();
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
			return null;
		}
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpecimenVatBlockEntity vat
				&& SpecimenJarData.hasSpecimen(stack)) {
			vat.kept().loadFromItem(stack, level.getGameTime());
			vat.sendUpdates();
		}
		// An empty vat joins the empty vats it stands among; one set down with a creature stays alone.
		if (level instanceof ServerLevel server) VatAssembly.placed(server, pos);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		// The halves stand together: when one goes, so does the other.
		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)
				&& !(neighbor.is(this) && neighbor.getValue(HALF) != half)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// Broken from the top without a drop due, the base goes with it and drops nothing either.
		if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER && player.isCreative()) {
			BlockPos below = pos.below();
			BlockState base = level.getBlockState(below);
			if (base.is(this) && base.getValue(HALF) == DoubleBlockHalf.LOWER) {
				level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
				level.levelEvent(player, 2001, below, Block.getId(base));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SpecimenVatBlockEntity(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.specimen_vat.get(), SpecimenVatBlockEntity::serverTick);
	}

	/** The vat this half belongs to (its lower half's block entity), not the vessel it runs in. */
	@Nullable
	public static SpecimenVatBlockEntity ownVat(BlockGetter level, BlockPos pos, BlockState state) {
		BlockPos base = state.hasProperty(HALF) && state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
		return level.getBlockEntity(base) instanceof SpecimenVatBlockEntity vat ? vat : null;
	}

	/** The vat running the vessel this half belongs to: any vat of a joined vessel answers for the whole. */
	@Nullable
	public static SpecimenVatBlockEntity vat(BlockGetter level, BlockPos pos, BlockState state) {
		SpecimenVatBlockEntity own = ownVat(level, pos, state);
		return own == null ? null : own.runner();
	}

	/** Whether a led creature may go into a vat at all: kept by the one rule, and carrying no rider. */
	public static boolean keeps(Mob mob) {
		return MenagerieLeash.verdict(mob).keeps() && !mob.isVehicle();
	}

	/** Whether a led creature fits a vessel {@code width} vats across and {@code layers} high. */
	public static boolean fits(Mob mob, int width, int layers) {
		return keeps(mob) && MenagerieRules.vatFits(mob.getBbWidth(), mob.getBbHeight(), mob.getMaxHealth(), width, layers);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		SpecimenVatBlockEntity vat = vat(level, pos, state);
		if (vat == null) return InteractionResult.PASS;
		// A kept creature's screen opens only for an empty hand; anything held keeps its own use (a syringe samples).
		if (!player.isShiftKeyDown() && !SpecimenVessels.opensScreen(player) && vat.kept().living()) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.SUCCESS;
		BlockPos base = vat.getBlockPos();
		if (player.isShiftKeyDown()) {
			if (!SpecimenVessels.mayHandle(vat, player)) return InteractionResult.CONSUME;
			// A lone vat lifts with its creature; a joined vessel gives up one empty vat at a time, never its creature.
			if (vat.vats() > 1 && vat.kept().occupied()) {
				player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.vat_too_heavy")
						.withStyle(ChatFormatting.GRAY), true);
				return InteractionResult.CONSUME;
			}
			SpecimenVatBlockEntity lifted = ownVat(level, pos, state);
			if (lifted == null) return InteractionResult.PASS;
			BlockPos liftedBase = lifted.getBlockPos();
			ItemStack stack = new ItemStack(this);
			if (vat.vats() == 1) {
				if (vat.kept().occupied()) vat.kept().saveToItem(stack);
				vat.suppressNextRelease();
			}
			level.setBlock(liftedBase.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
			level.setBlock(liftedBase, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
			if (!player.addItem(stack)) player.drop(stack, false);
			return InteractionResult.CONSUME;
		}
		if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer
				&& SpecimenVessels.useEmptyHand(server, base, vat, serverPlayer, vat::fits)) {
			return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		SpecimenVatBlockEntity vat = vat(level, pos, state);
		if (!(stack.getItem() instanceof BloodVialItem) || BloodSampleData.isFilled(stack) || vat == null
				|| !vat.kept().living()) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
			SpecimenVessels.sample(server, vat.getBlockPos(), vat, serverPlayer, stack, hand);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public boolean canDrawAbsorptionParticles(Level level, BlockPos pos, BlockState state) {
		SpecimenVatBlockEntity vat = vat(level, pos, state);
		return vat != null && vat.kept().occupied();
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		SpecimenVatBlockEntity vat = vat(level, pos, state);
		return vat == null ? 0.0D : SpecimenVessels.absorb(level, vat, player);
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		SpecimenVatBlockEntity vat = vat(level, pos, state);
		return vat == null ? 0.0D : SpecimenVessels.project(vat, player, vat.getBlockPos(), maxAmount);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel server && state.getValue(HALF) == DoubleBlockHalf.LOWER
				&& level.getBlockEntity(pos) instanceof SpecimenVatBlockEntity vat) {
			// The going vat's vessel lets its creature go and the rest of it re-forms with the vats around it.
			VatAssembly.removed(server, pos, vat, vat.runner().consumeSuppressNextRelease());
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		if (id == 1 || id == 2) {
			if (level.isClientSide && vat(level, pos, state) instanceof SpecimenVatBlockEntity vat) {
				// 1: a failing creature rattles its glass; 2: a young quickens in it (P6).
				if (id == 1) vat.kept().rattle(level.getGameTime());
				else vat.kept().quicken(level.getGameTime());
			}
			return true;
		}
		return super.triggerEvent(state, level, pos, id, param);
	}
}
