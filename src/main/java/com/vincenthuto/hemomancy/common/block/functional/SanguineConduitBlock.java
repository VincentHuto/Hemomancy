package com.vincenthuto.hemomancy.common.block.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineConduitBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * The Sanguine Conduit in its placed form — a covenant anchor driven into the
 * ground by an Illuminatus who has reached Degree 5. Right-clicking it opens
 * the Harbinger skill-tree screen, the same as right-clicking the item in air.
 * <p>
 * The block has a minimal {@link SanguineConduitBlockEntity} solely to support
 * the BER that renders the pulsing blood-oath rings.
 * <p>
 * Placement is gated in
 * {@link com.vincenthuto.hemomancy.common.item.ItemSanguineConduit#useOn} to
 * require Initiatory Degree ≥ 5; the block itself imposes no runtime gate.
 */
public class SanguineConduitBlock extends BaseEntityBlock {

	public static final MapCodec<SanguineConduitBlock> CODEC = simpleCodec(SanguineConduitBlock::new);

	public SanguineConduitBlock(Properties properties) {
		super(properties);
	}

	public SanguineConduitBlock() {
		this(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.requiresCorrectToolForDrops()
				.strength(3.0f, 6.0f)
				.sound(SoundType.BONE_BLOCK)
				.lightLevel(state -> 4));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	// ── BlockEntity (needed for the ring BER) ─────────────────────────────────

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SanguineConduitBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return null; // No tick needed — rings are rendered purely client-side via the BER
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		// The block has a regular cube model; the BER only adds the ring overlay.
		return RenderShape.MODEL;
	}

	// ── Interaction: open the Harbinger skill-tree screen ────────────────────

	/**
	 * Opens the Harbinger skill-tree screen when right-clicked without an item,
	 * mirroring the behaviour of the Sanguine Conduit item when used in air.
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult result) {
		if (level.isClientSide) {
			HarbingerProgressScreen.openScreen();
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * Forwards item-in-hand right-clicks to the screen as well, so the screen
	 * opens regardless of whether the player is holding something.
	 */
	@Override
	protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
			BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult result) {
		if (level.isClientSide) {
			HarbingerProgressScreen.openScreen();
		}
		return net.minecraft.world.ItemInteractionResult.SUCCESS;
	}
}

