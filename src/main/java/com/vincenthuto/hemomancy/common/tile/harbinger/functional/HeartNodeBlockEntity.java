package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
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

/** A planted heart-node: whose it is, and how many ribs of the sanctum it has grown. */
public class HeartNodeBlockEntity extends HemoBlockEntity {
	private static final ParticleColor CRIMSON = new ParticleColor(150, 40, 58);
	@Nullable
	private UUID owner;
	private int stage;
	private int growth;

	public HeartNodeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.heart_node.get(), pos, state);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		BlockEntitySync.markAndSync(this);
	}

	public int stage() {
		return stage;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, HeartNodeBlockEntity node) {
		if (node.stage >= HeartNodeRules.RIBS || ++node.growth < HeartNodeRules.GROW_TICKS) return;
		node.growth = 0;
		node.growRib((ServerLevel) level, node.stage);
		node.stage++;
		BlockEntitySync.markAndSync(node);
	}

	/** Grows rib {@code rib}: a calcified pillar capped with flesh, only into air or soft plants. */
	public void growRib(ServerLevel level, int rib) {
		BlockPos base = HeartNodeRules.ribBase(worldPosition, rib);
		BlockState pillar = BlockInit.calcified_hyphae.get().defaultBlockState();
		for (int y = 0; y < HeartNodeRules.RIB_HEIGHT; y++) {
			placeSoftly(level, base.above(y), pillar);
		}
		placeSoftly(level, base.above(HeartNodeRules.RIB_HEIGHT), BlockInit.infected_cap.get().defaultBlockState());
		level.sendParticles(HemoParticleData.sporiticSpore(CRIMSON), base.getX() + 0.5, base.getY() + 1.5,
				base.getZ() + 0.5, 10, 0.3, 0.8, 0.3, 0.02);
		level.playSound(null, base, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 0.8f, 0.7f);
	}

	private static void placeSoftly(ServerLevel level, BlockPos pos, BlockState state) {
		BlockState there = level.getBlockState(pos);
		if (there.isAir() || (there.canBeReplaced() && there.getFluidState().isEmpty())) {
			level.setBlock(pos, state, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (owner != null) tag.putUUID("Owner", owner);
		tag.putInt("Stage", stage);
		tag.putInt("Growth", growth);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		stage = tag.getInt("Stage");
		growth = tag.getInt("Growth");
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}
}
