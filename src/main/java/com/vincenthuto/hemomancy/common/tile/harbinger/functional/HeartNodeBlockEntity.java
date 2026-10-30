package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.HeartNodeBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A planted heart-node: whose it is, when it was planted, and how far it has matured (0 to
 * {@link HeartNodeRules#MATURE_STAGE}). It grows nothing into the world; the sanctum is its envelope.
 */
public class HeartNodeBlockEntity extends HemoBlockEntity {
	private static final ParticleColor CRIMSON = new ParticleColor(150, 40, 58);
	private static final int STAGE_CHECK_TICKS = 20;
	@Nullable
	private UUID owner;
	private long plantedAt;
	private int stage;
	/** Client only: the last heartbeat the renderer shed motes for, so each beat sheds them once. */
	public long lastBeatShed = Long.MIN_VALUE;

	public HeartNodeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.heart_node.get(), pos, state);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	/** Binds the node to {@code owner}, planted at {@code gameTime}; it starts unripe. */
	public void plant(UUID owner, long gameTime) {
		this.owner = owner;
		this.plantedAt = gameTime;
		this.stage = 0;
		showStage();
		BlockEntitySync.markAndSync(this);
	}

	public int stage() {
		return stage;
	}

	public long plantedAt() {
		return plantedAt;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, HeartNodeBlockEntity node) {
		if (level.getGameTime() % STAGE_CHECK_TICKS == 0) node.refreshStage((ServerLevel) level);
	}

	/**
	 * Brings the stage up to the level's clock and the block's {@link HeartNodeBlock#STAGE} to the stage (a node saved
	 * before the stage was a block state has it at 0); a step up puffs spores from the node.
	 */
	public void refreshStage(ServerLevel level) {
		int next = HeartNodeRules.stageAt(plantedAt, level.getGameTime());
		if (next == stage) {
			showStage();
			return;
		}
		boolean rose = next > stage;
		stage = next;
		showStage();
		BlockEntitySync.markAndSync(this);
		if (!rose) return;
		level.sendParticles(HemoParticleData.sporiticSpore(CRIMSON), worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5, 6 + stage * 4, 0.3, 0.3, 0.3, 0.02);
		level.playSound(null, worldPosition, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 0.8f, 0.6f + stage * 0.1f);
	}

	/** Sets the block's model stage to {@link #stage()} when it differs. */
	private void showStage() {
		if (level == null || level.isClientSide) return;
		BlockState state = getBlockState();
		if (state.hasProperty(HeartNodeBlock.STAGE) && state.getValue(HeartNodeBlock.STAGE) != stage) {
			level.setBlock(worldPosition, state.setValue(HeartNodeBlock.STAGE, stage), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (owner != null) tag.putUUID("Owner", owner);
		tag.putLong("PlantedAt", plantedAt);
		tag.putInt("Stage", stage);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		// A node from before maturity stages has no planting time: it reads as long grown.
		plantedAt = tag.contains("PlantedAt") ? tag.getLong("PlantedAt") : HeartNodeRules.LONG_GROWN;
		stage = Math.min(tag.getInt("Stage"), HeartNodeRules.MATURE_STAGE);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}
}
