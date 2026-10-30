package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A raised Mycelial Synapse: whose it is and the heart it was raised for, both synced for the link threads and the
 * renderer. While it stands unwithered it checks now and then that its owner's record still holds it, and withers
 * once it doesn't, so a synapse in a chunk that was unloaded when its node went catches up when it loads.
 */
public class MycelialSynapseBlockEntity extends HemoBlockEntity {
	private static final int RECORD_CHECK_TICKS = 40;
	@Nullable
	private UUID owner;
	@Nullable
	private BlockPos linkedHeart;
	/** Client only: whether the renderer has seen this synapse alive, and when it then saw it wither. */
	public boolean clientSeenActive;
	public long clientWitheredAt = Long.MIN_VALUE;

	public MycelialSynapseBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mycelial_synapse.get(), pos, state);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	@Nullable
	public BlockPos linkedHeart() {
		return linkedHeart;
	}

	public void raise(UUID owner, @Nullable BlockPos heart) {
		this.owner = owner;
		this.linkedHeart = heart == null ? null : heart.immutable();
		BlockEntitySync.markAndSync(this);
	}

	/** Whether its owner's record holds this synapse: the node it was raised for still stands and counts it. */
	public boolean active(ServerLevel level) {
		if (owner == null) return false;
		HeartNodeEntry entry = HeartNodeSavedData.get(level).entry(owner);
		return entry != null && entry.synapses().contains(worldPosition);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MycelialSynapseBlockEntity synapse) {
		if ((level.getGameTime() + pos.asLong()) % RECORD_CHECK_TICKS != 0) return;
		if (!synapse.active((ServerLevel) level)) MycelialSynapseBlock.wither(level, pos);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (owner != null) tag.putUUID("Owner", owner);
		if (linkedHeart != null) tag.put("LinkedHeart", NbtUtils.writeBlockPos(linkedHeart));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		linkedHeart = NbtUtils.readBlockPos(tag, "LinkedHeart").orElse(null);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}
}
