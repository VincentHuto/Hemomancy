package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Render/sync carrier for the Consecrated Bloodwell. The actual blood belongs
 * to the linked bloodline pool; these fields only mirror pool fullness to the
 * client so the fountain can animate.
 */
public class ConsecratedBloodwellBlockEntity extends BlockEntity {
	private static final int POOL_SYNC_INTERVAL = 20;
	private static final String TAG_POOL_BLOOD = "poolBlood";
	private static final String TAG_POOL_MAX = "poolMax";

	private double syncedPoolBlood = 0.0D;
	private double syncedPoolMax = 0.0D;

	public ConsecratedBloodwellBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.consecrated_bloodwell.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state,
			ConsecratedBloodwellBlockEntity te) {
		if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % POOL_SYNC_INTERVAL != 0) {
			return;
		}
		UUID owner = FoundingFaneSavedData.get(serverLevel).findOwnerForHeart(pos);
		if (owner == null) {
			te.syncPoolValues(0.0D, 0.0D);
			return;
		}
		Bloodline globalLine = BloodlineSavedData.get(serverLevel.getServer().overworld())
				.getAllBloodlines()
				.values()
				.stream()
				.filter(Bloodline::isValid)
				.filter(line -> owner.equals(line.getLeaderUUID()))
				.findFirst()
				.orElse(null);
		if (globalLine == null) {
			te.syncPoolValues(0.0D, 0.0D);
			return;
		}
		te.syncFromBloodlinePool(globalLine);
	}

	public void syncFromBloodlinePool(Bloodline bloodline) {
		if (bloodline == null || !bloodline.isValid()) {
			syncPoolValues(0.0D, 0.0D);
			return;
		}
		syncPoolValues(bloodline.getBloodVolume(), bloodline.getMaxBloodVolume());
	}

	private void syncPoolValues(double blood, double max) {
		if (Math.abs(syncedPoolBlood - blood) < 0.01D && Math.abs(syncedPoolMax - max) < 0.01D) {
			return;
		}
		syncedPoolBlood = blood;
		syncedPoolMax = max;
		sendUpdates();
	}

	private void sendUpdates() {
		if (level == null) {
			return;
		}
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble(TAG_POOL_BLOOD, syncedPoolBlood);
		tag.putDouble(TAG_POOL_MAX, syncedPoolMax);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		syncedPoolBlood = tag.getDouble(TAG_POOL_BLOOD);
		syncedPoolMax = tag.getDouble(TAG_POOL_MAX);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putDouble(TAG_POOL_BLOOD, syncedPoolBlood);
		tag.putDouble(TAG_POOL_MAX, syncedPoolMax);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		syncedPoolBlood = tag.getDouble(TAG_POOL_BLOOD);
		syncedPoolMax = tag.getDouble(TAG_POOL_MAX);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
			HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		CompoundTag tag = pkt.getTag();
		if (tag != null) {
			syncedPoolBlood = tag.getDouble(TAG_POOL_BLOOD);
			syncedPoolMax = tag.getDouble(TAG_POOL_MAX);
		}
	}

	public double getStoredBlood() {
		return syncedPoolBlood;
	}

	public double getMaxBlood() {
		return Math.max(1.0D, syncedPoolMax);
	}
}
