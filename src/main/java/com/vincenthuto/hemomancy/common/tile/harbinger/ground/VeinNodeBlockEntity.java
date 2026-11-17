package com.vincenthuto.hemomancy.common.tile.harbinger.ground;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A vein node's memory: who set it down, and when its owner last tended its vein (stood near while it ran). The work
 * is {@link GroundNetwork#tickNode}, once a second.
 */
public class VeinNodeBlockEntity extends HemoBlockEntity {
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_TENDED = "LastTended";
	@Nullable private UUID owner;
	private long lastTended = Long.MIN_VALUE;

	public VeinNodeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.vein_node.get(), pos, state);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	public void setOwner(@Nullable UUID owner, long now) {
		this.owner = owner;
		this.lastTended = now;
		setChanged();
	}

	/** The game time its owner last tended it; a node never tended counts from now. */
	public long lastTended(long now) {
		if (lastTended == Long.MIN_VALUE) {
			lastTended = now;
			setChanged();
		}
		return lastTended;
	}

	public void tended(long now) {
		lastTended = now;
		setChanged();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, VeinNodeBlockEntity node) {
		if (level instanceof ServerLevel server && Math.floorMod(server.getGameTime() + pos.asLong(), 20L) == 0L) {
			GroundNetwork.tickNode(server, node);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (owner != null) tag.putUUID(TAG_OWNER, owner);
		if (lastTended != Long.MIN_VALUE) tag.putLong(TAG_TENDED, lastTended);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
		lastTended = tag.contains(TAG_TENDED) ? tag.getLong(TAG_TENDED) : Long.MIN_VALUE;
	}
}
