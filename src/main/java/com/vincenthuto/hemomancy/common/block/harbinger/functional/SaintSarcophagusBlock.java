package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.SaintRelicItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.functional.EnumCorpusState;
import com.vincenthuto.hemomancy.common.tile.functional.SaintSarcophagusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Saint Sarcophagus — the centerpiece of a Sainted Mausoleum.
 * Contains a Preserved Corpus. Players draw blood from the corpus using an empty
 * Blood Vial to receive a Consecrated Syringe tagged with the saint's type.
 * The syringe is then processed in a Vial Centrifuge to extract Hallowed Residuum.
 *
 * Corpus states:
 * - DORMANT: approachable, unstable
 * - RESPONSIVE: reacts to player interaction
 * - AWAKENED: player has offended it, combat triggered
 */
public class SaintSarcophagusBlock extends Block implements EntityBlock, IMultiBlock, SimpleWaterloggedBlock {

public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);
/** Blood cost per offering (drained from player's blood volume). */
private static final double BLOOD_COST_PER_OFFERING = 100.0;
private static final String TAG_SAINT_TYPE = "SaintType";

/**
 * Base filler offsets defined for SOUTH facing.
 * 1-wide (X: 0), 3-deep (Z: -1,0,1), 2-tall (Y: 0,1).
 * These get rotated based on the block's actual facing direction.
 */
private static final BlockPos[] BASE_FILLER_OFFSETS = new BlockPos[] {
		// Y=0 layer (excluding origin 0,0,0)
		new BlockPos(0, 0, -1),
		new BlockPos(0, 0,  1),
		// Y=1 layer
		new BlockPos(0, 1, -1),
		new BlockPos(0, 1,  0),
		new BlockPos(0, 1,  1),
};

/**
 * Returns the filler offsets rotated for the given facing direction.
 */
private static BlockPos[] getRotatedOffsets(Direction facing) {
	return HorizontalFacingRotationHelper.rotateSouthOffsetsReversed(BASE_FILLER_OFFSETS, facing);
}

public SaintSarcophagusBlock(Properties properties) {
super(properties);
this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH)
		.setValue(WATERLOGGED, false));
}

@Override
public BlockPos[] getFillerOffsets() {
	// Fallback — prefer the facing-aware overrides below
	return BASE_FILLER_OFFSETS;
}

@Override
public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
	Direction facing = mainState.getValue(FACING);
	for (BlockPos offset : getRotatedOffsets(facing)) {
		BlockPos fillerPos = mainPos.offset(offset);
		level.setBlockAndUpdate(fillerPos, com.vincenthuto.hemomancy.common.init.BlockInit.filler_block.get()
				.defaultBlockState()
				.setValue(FillerBlock.WATERLOGGED, level.getFluidState(fillerPos).is(net.minecraft.tags.FluidTags.WATER)));
		BlockEntity be = level.getBlockEntity(fillerPos);
		if (be instanceof com.vincenthuto.hemomancy.common.tile.FillerBlockEntity filler) {
			filler.setMainBlockPos(mainPos);
		}
	}
}

@Override
public void removeFillers(Level level, BlockPos mainPos) {
	// Try all 4 rotations to ensure cleanup works even if state is gone
	for (Direction dir : Direction.Plane.HORIZONTAL) {
		for (BlockPos offset : getRotatedOffsets(dir)) {
			BlockPos fillerPos = mainPos.offset(offset);
			BlockState fillerState = level.getBlockState(fillerPos);
			if (fillerState.is(com.vincenthuto.hemomancy.common.init.BlockInit.filler_block.get())) {
				if (fillerState.getValue(FillerBlock.WATERLOGGED)) {
					level.setBlockAndUpdate(fillerPos, Blocks.WATER.defaultBlockState());
				} else {
					level.removeBlock(fillerPos, false);
				}
			}
		}
	}
}

@Override
public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
	// We don't know the facing yet at canPlace time, so we check from getStateForPlacement
	// This default checks the SOUTH orientation as a fallback
	for (BlockPos offset : BASE_FILLER_OFFSETS) {
		BlockPos fillerPos = mainPos.offset(offset);
		if (!level.getBlockState(fillerPos).canBeReplaced()) {
			return false;
		}
	}
	return true;
}

/**
 * Facing-aware placement check.
 */
private boolean canPlaceMultiBlock(Level level, BlockPos mainPos, Direction facing) {
	for (BlockPos offset : getRotatedOffsets(facing)) {
		BlockPos fillerPos = mainPos.offset(offset);
		if (!level.getBlockState(fillerPos).canBeReplaced()) {
			return false;
		}
	}
	return true;
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

@Override
protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
builder.add(FACING, WATERLOGGED);
}

@Override
public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
return SHAPE;
}

@Override
public BlockState getStateForPlacement(BlockPlaceContext context) {
BlockPos pos = context.getClickedPos();
Level level = (Level) context.getLevel();
Direction facing = context.getHorizontalDirection().getOpposite();
if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos, facing)) {
	return this.defaultBlockState().setValue(FACING, facing)
			.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
}
return null;
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
public BlockState mirror(BlockState state, Mirror mirrorIn) {
return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
}

@Override
public BlockState rotate(BlockState state, Rotation rot) {
return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
}

@Override
public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
return new SaintSarcophagusBlockEntity(pos, state);
}

@Override
public void setPlacedBy(Level level, BlockPos pos, BlockState state,
		@Nullable LivingEntity placer, ItemStack stack) {
	super.setPlacedBy(level, pos, state, placer, stack);
	if (!level.isClientSide) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof SaintSarcophagusBlockEntity sarcophagus) {
			CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
			if (customData != null) {
				CompoundTag tag = customData.copyTag();
				if (tag.contains(TAG_SAINT_TYPE)) {
					try {
						sarcophagus.setSaintType(EnumSaintType.valueOf(tag.getString(TAG_SAINT_TYPE)));
					} catch (IllegalArgumentException ignored) {
						assignRandomSaint(level, sarcophagus);
					}
				} else {
					assignRandomSaint(level, sarcophagus);
				}
			} else {
				assignRandomSaint(level, sarcophagus);
			}
		}
		placeFillers(level, pos, state);
	}
}

@Override
public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
	if (!state.is(newState.getBlock())) {
		if (!level.isClientSide) {
			removeFillers(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}

@Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
BlockEntityType<T> type) {
if (level.isClientSide) {
return (lvl, pos, st, be) -> {
if (be instanceof SaintSarcophagusBlockEntity sarcophagus) {
SaintSarcophagusBlockEntity.clientTick(lvl, pos, st, sarcophagus);
}
};
} else {
return (lvl, pos, st, be) -> {
if (be instanceof SaintSarcophagusBlockEntity sarcophagus) {
sarcophagus.tick();
// Spawn continuous flame & glow particles when consecrated
if (sarcophagus.isConsecrated() && lvl instanceof ServerLevel serverLevel) {
	Direction facing = st.getValue(FACING);
	spawnBrazierFlames(serverLevel, pos, facing);
}
}
};
}
}

/**
 * Brazier positions in facing-relative terms.
 * fwdDist:  distance along the FACING direction (positive = toward bowl).
 * sideDist: distance along facing.getClockWise() (positive = right when looking from the bowl).
 * y:        height offset from block origin.
 *
 * World position = blockPos + 0.5 + fwd * fwdDist + right * sideDist
 */
private static final double[] BRAZIER_FWD  = { -1.28,  0.91, -1.28,  0.91 };
private static final double[] BRAZIER_SIDE = {  0.69,  0.69, -0.71, -0.71 };
private static final double[] BRAZIER_Y    = {  1.45,  1.17,  1.45,  1.17 };

/** Resolves a brazier world-position array (4 × [x,y,z]) for the given facing direction. */
private static double[][] getBrazierPositions(Direction facing) {
	Direction right = facing.getClockWise();
	double[][] out = new double[4][3];
	for (int i = 0; i < 4; i++) {
		out[i][0] = 0.5 + facing.getStepX() * BRAZIER_FWD[i] + right.getStepX() * BRAZIER_SIDE[i];
		out[i][1] = BRAZIER_Y[i];
		out[i][2] = 0.5 + facing.getStepZ() * BRAZIER_FWD[i] + right.getStepZ() * BRAZIER_SIDE[i];
	}
	return out;
}

/**
 * Returns the bowl world-relative offset for any facing direction.
 * The bowl is always on the FACING side (the front face) of the sarcophagus.
 */
private static double[] getBowlOffset(Direction facing) {
	return new double[]{
		0.5 + facing.getStepX() * 1.04,
		0.6,
		0.5 + facing.getStepZ() * 1.04
	};
}

/** Spawns flame and blood glow particles on each brazier and the offering bowl. */
private static void spawnConsecrationParticles(Level level, BlockPos pos, Direction facing) {
	if (!(level instanceof ServerLevel serverLevel)) return;
	double bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
	for (double[] r : getBrazierPositions(facing)) {
		serverLevel.sendParticles(ParticleTypes.FLAME,
			bx + r[0], by + r[1], bz + r[2], 5, 0.10, 0.10, 0.10, 0.02);
		serverLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
			bx + r[0], by + r[1] + 0.1, bz + r[2], 4, 0.08, 0.08, 0.08, 0.0);
	}
	double[] bowl = getBowlOffset(facing);
	serverLevel.sendParticles(ParticleTypes.FLAME,
		bx + bowl[0], by + bowl[1], bz + bowl[2], 5, 0.05, 0.05, 0.05, 0.02);
	serverLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
		bx + bowl[0], by + bowl[1] + 0.1, bz + bowl[2], 4, 0.05, 0.05, 0.05, 0.0);
}

/** Spawns ambient flame & blood glow on each brazier and the bowl (called every tick when consecrated). */
private static void spawnBrazierFlames(ServerLevel level, BlockPos pos, Direction facing) {
	double bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
	for (double[] r : getBrazierPositions(facing)) {
		level.sendParticles(ParticleTypes.FLAME,
			bx + r[0], by + r[1], bz + r[2], 1, 0.02, 0.04, 0.02, 0.0);
		if (level.random.nextInt(4) == 0) {
			level.sendParticles(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
				bx + r[0], by + r[1] + 0.15, bz + r[2], 1, 0.05, 0.05, 0.05, 0.0);
		}
	}
	double[] bowl = getBowlOffset(facing);
	level.sendParticles(ParticleTypes.FLAME,
		bx + bowl[0], by + bowl[1], bz + bowl[2], 1, 0.02, 0.04, 0.02, 0.0);
	if (level.random.nextInt(4) == 0) {
		level.sendParticles(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
			bx + bowl[0], by + bowl[1] + 0.1, bz + bowl[2], 1, 0.03, 0.03, 0.03, 0.0);
	}
}

private InteractionResult handleUse(BlockState state, Level worldIn, BlockPos pos, Player player,
		@Nullable InteractionHand handIn, ItemStack heldStack, BlockHitResult result) {
if (worldIn.isClientSide) {
return InteractionResult.sidedSuccess(true);
}

BlockEntity be = worldIn.getBlockEntity(pos);
if (!(be instanceof SaintSarcophagusBlockEntity sarcophagus)) {
return InteractionResult.PASS;
}

if (heldStack.getItem() instanceof SaintRelicItem relic) {
	sarcophagus.setSaintType(relic.getSaintType());
	sarcophagus.setCorpusState(EnumCorpusState.AWAKENED);
	if (!player.getAbilities().instabuild) {
		heldStack.shrink(1);
	}
	triggerSaintEncounter(worldIn, pos, player, sarcophagus,
		Component.translatable("message.hemomancy.saint_sarcophagus.relic_summon",
			relic.getSaintType().getDisplayName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
	return InteractionResult.CONSUME;
}

// Foul Paste is deliberate desecration: it forces the saint awake regardless of alignment.
if (heldStack.is(ItemInit.foul_paste.get())) {
	sarcophagus.setCorpusState(EnumCorpusState.AWAKENED);
	if (!player.getAbilities().instabuild) {
		heldStack.shrink(1);
	}
	player.displayClientMessage(
		Component.literal("The paste blackens on the stone. The Saint remembers insult before mercy.")
			.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
		false);
	triggerSaintEncounter(worldIn, pos, player, sarcophagus);
	return InteractionResult.CONSUME;
}

// ── Consecration gate ─────────────────────────────────────────────────────
// The sarcophagus demands a blood offering poured into the bowl on its
// northern face before the lid will yield. Blood is drawn from the player.
	if (!sarcophagus.isConsecrated()) {
		Direction hitFace = result.getDirection();
		// The bowl is on the front face — the same direction the block faces
		Direction bowlFace = state.getValue(FACING);

		if (hitFace == bowlFace) {
		// Attempt to drain blood from the player
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || volume.getBloodVolume() < BLOOD_COST_PER_OFFERING) {
			player.displayClientMessage(
				Component.literal("You lack sufficient blood to offer.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				true);
			worldIn.playSound(null, pos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.5f, 0.6f);
			return InteractionResult.CONSUME;
		}

		// Drain blood and record offering
		volume.drain(BLOOD_COST_PER_OFFERING);
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
		}
		sarcophagus.addOffering();

		int vol = sarcophagus.getOfferingBloodVolume();
		int threshold = SaintSarcophagusBlockEntity.OFFERING_THRESHOLD;

		if (vol >= threshold) {
			// Threshold reached — the sarcophagus accepts the reverence
			sarcophagus.setConsecrated(true);
			Direction facing = state.getValue(FACING);
			worldIn.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.4f);
			spawnConsecrationParticles(worldIn, pos, facing);
			player.displayClientMessage(
				Component.literal("The bowl drinks deep. A heavy click echoes from within the stone.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
		} else {
			int remaining = threshold - vol;
			worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 0.8f);
			player.displayClientMessage(
				Component.literal("The bowl accepts your blood... " + remaining + " more offering"
					+ (remaining == 1 ? "" : "s") + " required.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
		}
		return InteractionResult.CONSUME;
	}

	// Non-north face — the lid refuses
	player.displayClientMessage(
		Component.literal("The lid refuses to budge without proper reverence.")
			.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
		true);
	worldIn.playSound(null, pos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.6f, 0.5f);
	return InteractionResult.CONSUME;
}
// ─────────────────────────────────────────────────────────────────────────

// Shift+right-click on an open sarcophagus closes the lid again
if (sarcophagus.isOpen() && player.isShiftKeyDown()) {
sarcophagus.setOpen(false);
player.displayClientMessage(
Component.literal("You heave the lid back into place.")
.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
false);
worldIn.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.5f);
return InteractionResult.CONSUME;
}

// If the lid has not been slid off yet, open it on the first interaction
if (!sarcophagus.isOpen()) {
sarcophagus.setOpen(true);
player.displayClientMessage(
Component.literal("You slide the lid off to the side with a loud thud and marvel the corpse infront of you.")
.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
false);
worldIn.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.4f);
return InteractionResult.CONSUME;
}

// If the corpus is AWAKENED, trigger combat encounter and consume block
if (sarcophagus.getCorpusState() == EnumCorpusState.AWAKENED) {
	triggerSaintEncounter(worldIn, pos, player, sarcophagus);
	return InteractionResult.CONSUME;
}

// Check for cooldown
if (sarcophagus.isOnCooldown()) {
player.displayClientMessage(
Component.literal("The corpus is still... recovering. Wait.")
.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
true);
return InteractionResult.CONSUME;
}

ItemStack stack = heldStack;

// Empty-hand interaction: inspect the sarcophagus
if (stack.isEmpty()) {
showSarcophagusInfo(player, sarcophagus);
// In DORMANT state, add an atmospheric pulse so the block feels alive
if (sarcophagus.getCorpusState() == EnumCorpusState.DORMANT) {
worldIn.playSound(null, pos, SoundEvents.SCULK_SENSOR_PLACE, SoundSource.BLOCKS, 0.4f, 0.6f);
if (worldIn instanceof ServerLevel serverLevel) {
serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
6, 0.3, 0.3, 0.3, 0.01);
}
}
return InteractionResult.CONSUME;
}

// Blood Vial interaction: draw blood from the corpus into an empty vial
if (stack.getItem() instanceof BloodVialItem
&& (!stack.has(DataComponents.CUSTOM_DATA)
|| !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(BloodVialItem.TAG_STATE))) {
	if (handIn == null) {
		return InteractionResult.PASS;
	}
	if (sarcophagus.hasSampleYielded()) {
		player.displayClientMessage(
			Component.literal("The corpus has already yielded what it will give peacefully.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
			false);
		return InteractionResult.CONSUME;
	}
	if (!isPlayerAlignedWithSaint(player, sarcophagus.getSaintType())) {
		sarcophagus.incrementExtractionAttempts();
		sarcophagus.setCorpusState(EnumCorpusState.AWAKENED);
		player.displayClientMessage(
			Component.literal("The corpus recoils from your blood. The Saint wakes in rejection.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
			false);
		triggerSaintEncounter(worldIn, pos, player, sarcophagus);
		return InteractionResult.CONSUME;
	}
	consecrateVial(worldIn, pos, player, handIn, stack, sarcophagus);
	return InteractionResult.CONSUME;
}

return InteractionResult.PASS;
}

@Override
protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
		BlockHitResult result) {
	return handleUse(state, worldIn, pos, player, null, ItemStack.EMPTY, result);
}

@Override
protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
		Player player, InteractionHand handIn, BlockHitResult result) {
	InteractionResult interactionResult = handleUse(state, worldIn, pos, player, handIn, stack, result);
	return interactionResult == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
			: ItemInteractionResult.SUCCESS;
}

private void showSarcophagusInfo(Player player, SaintSarcophagusBlockEntity sarcophagus) {
EnumSaintType saint = sarcophagus.getSaintType();
EnumCorpusState state = sarcophagus.getCorpusState();
player.displayClientMessage(
Component.literal("A sarcophagus of Saint " + saint.getDisplayName() + ".")
.withStyle(ChatFormatting.GOLD),
false);
player.displayClientMessage(
Component.literal("State: " + state.getDisplayName())
.withStyle(ChatFormatting.GRAY),
false);
player.displayClientMessage(
Component.literal("Aligned tendencies: " + saint.getPrimaryTendency().name()
+ " + " + saint.getSecondaryTendency().name())
.withStyle(ChatFormatting.DARK_PURPLE),
false);

// State-dependent guidance
switch (state) {
case DORMANT -> player.displayClientMessage(
Component.translatable("message.hemomancy.saint_sarcophagus.dormant_hint")
.withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),
false);
case RESPONSIVE -> player.displayClientMessage(
Component.translatable("message.hemomancy.saint_sarcophagus.responsive_hint")
.withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
false);
case AWAKENED -> player.displayClientMessage(
Component.translatable("message.hemomancy.saint_sarcophagus.awakened_hint")
.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
false);
}
}

/**
 * Consecration step — the player draws blood from the Preserved Corpus using an empty Blood Vial.
 * The corpus fills the vial with its sanctified blood, yielding a Consecrated Syringe tagged
 * with the saint's type. Process the syringe in a Vial Centrifuge to extract Hallowed Residuum.
 * The empty Blood Vial is consumed in the process.
 */
private void consecrateVial(Level worldIn, BlockPos pos, Player player, InteractionHand hand,
ItemStack vialStack, SaintSarcophagusBlockEntity sarcophagus) {
// Transition to RESPONSIVE on first interaction
if (sarcophagus.getCorpusState() == EnumCorpusState.DORMANT) {
sarcophagus.setCorpusState(EnumCorpusState.RESPONSIVE);
}

// Consume the empty blood vial
vialStack.shrink(1);
if (vialStack.isEmpty()) {
player.setItemInHand(hand, ItemStack.EMPTY);
}

// Give the player a Consecrated Syringe tagged with the saint type
ItemStack syringe = new ItemStack(ItemInit.consecrated_syringe.get());
CompoundTag syringeTag = syringe.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
syringeTag.putString(ConsecratedSyringeItem.TAG_SAINT_TYPE,
sarcophagus.getSaintType().name());
syringe.set(DataComponents.CUSTOM_DATA, CustomData.of(syringeTag));
if (!player.getInventory().add(syringe)) {
player.drop(syringe, false);
}

// Apply a short cooldown so the sarcophagus cannot be spammed
sarcophagus.setCooldownTicks(100);

player.displayClientMessage(
Component.literal("The corpus yields its blood. Process the Consecrated Syringe in a Vial Centrifuge.")
.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
false);
worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
spawnAmbienceParticles(worldIn, pos);
sarcophagus.setSampleYielded(true);
}

private void spawnAmbienceParticles(Level worldIn, BlockPos pos) {
if (worldIn instanceof ServerLevel serverLevel) {
serverLevel.sendParticles(ParticleTypes.SOUL,
pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
15, 0.5, 0.5, 0.5, 0.02);
}
}

private static void assignRandomSaint(Level level, SaintSarcophagusBlockEntity sarcophagus) {
	EnumSaintType[] saints = EnumSaintType.values();
	sarcophagus.setSaintType(saints[level.random.nextInt(saints.length)]);
}

private static boolean isPlayerAlignedWithSaint(Player player, EnumSaintType saint) {
	IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player).orElse(null);
	if (tendency == null) {
		return false;
	}
	EnumBloodTendency dominant = null;
	float dominantValue = 0.0F;
	for (EnumBloodTendency candidate : EnumBloodTendency.values()) {
		float value = tendency.getAlignmentByTendency(candidate);
		if (value > dominantValue) {
			dominant = candidate;
			dominantValue = value;
		}
	}
	return dominant != null && dominantValue > 0.0F && saint.isAligned(dominant);
}

private static void triggerSaintEncounter(Level worldIn, BlockPos pos, Player player,
		SaintSarcophagusBlockEntity sarcophagus) {
	triggerSaintEncounter(worldIn, pos, player, sarcophagus,
		Component.literal("The corpus seethes with hostility. Saint " + sarcophagus.getSaintType().getDisplayName()
			+ " will not receive you.")
			.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
}

private static void triggerSaintEncounter(Level worldIn, BlockPos pos, Player player,
		SaintSarcophagusBlockEntity sarcophagus, Component message) {
	BlockState state = worldIn.getBlockState(pos);
	player.displayClientMessage(message, false);
	if (worldIn instanceof ServerLevel serverLevel) {
		HarbingerSaintEncounterHooks.spawnSaintBoss(serverLevel, pos, player, sarcophagus.getSaintType());
	}
	worldIn.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.2f);
	worldIn.setBlockAndUpdate(pos, WaterloggedBlockSupport.survivorOrWater(state));
}
}
