package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.encounter.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.saint.EnumCorpusState;
import com.vincenthuto.hemomancy.common.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.functional.SaintSarcophagusBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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
public class SaintSarcophagusBlock extends Block implements EntityBlock {

public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

public SaintSarcophagusBlock(Properties properties) {
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
public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
return new SaintSarcophagusBlockEntity(pos, state);
}

@Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
BlockEntityType<T> type) {
if (!level.isClientSide) {
return (lvl, pos, st, be) -> {
if (be instanceof SaintSarcophagusBlockEntity sarcophagus) {
sarcophagus.tick();
}
};
}
return null;
}

@Override
public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
InteractionHand handIn, BlockHitResult result) {
if (worldIn.isClientSide) {
return InteractionResult.sidedSuccess(true);
}

BlockEntity be = worldIn.getBlockEntity(pos);
if (!(be instanceof SaintSarcophagusBlockEntity sarcophagus)) {
return InteractionResult.PASS;
}

// If the corpus is AWAKENED, trigger combat encounter and consume block
if (sarcophagus.getCorpusState() == EnumCorpusState.AWAKENED) {
player.displayClientMessage(
Component.literal("The corpus seethes with hostility. It will not receive you.")
.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
false);
if (worldIn instanceof ServerLevel serverLevel) {
HarbingerSaintEncounterHooks.spawnSaintBoss(serverLevel, pos, player);
}
worldIn.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.2f);
worldIn.removeBlock(pos, false);
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

ItemStack stack = player.getItemInHand(handIn);

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
&& (!stack.hasTag() || !stack.getOrCreateTag().getBoolean(BloodVialItem.TAG_STATE))) {
consecrateVial(worldIn, pos, player, handIn, stack, sarcophagus);
return InteractionResult.CONSUME;
}

return InteractionResult.PASS;
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
syringe.getOrCreateTag().putString(ConsecratedSyringeItem.TAG_SAINT_TYPE,
sarcophagus.getSaintType().name());
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
}

private void spawnAmbienceParticles(Level worldIn, BlockPos pos) {
if (worldIn instanceof ServerLevel serverLevel) {
serverLevel.sendParticles(ParticleTypes.SOUL,
pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
15, 0.5, 0.5, 0.5, 0.02);
}
}
}
