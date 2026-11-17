package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

public class MortalDisplayBlockEntity extends HemoBlockEntity {
	private UUID linkedHermit;
	private UUID claimant;
	private boolean claimed;

	public MortalDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mortal_display.get(), pos, state);
	}

	public UUID getLinkedHermit() {
		return linkedHermit;
	}

	public boolean isClaimed() {
		return claimed;
	}

	public boolean isClaimedBy(UUID player) {
		return claimed && player != null && player.equals(claimant);
	}

	public void linkHermit(UUID hermit) {
		linkedHermit = hermit;
		setChanged();
	}

	public void claim(UUID player) {
		claimed = true;
		claimant = player;
		BlockEntitySync.markAndSync(this);
	}

	/** Reach of a temple: how far the Display looks for its Hermit, and the Hermit for his Display. */
	private static final int TEMPLE_REACH = 16;

	/** PT-8: the linked Hermit, alive and not dissolving, near the Display; null when the link is spent or broken. */
	@Nullable
	public HarbingerHermitEntity livingHermit() {
		if (level == null || linkedHermit == null) return null;
		return level.getEntitiesOfClass(HarbingerHermitEntity.class, new AABB(worldPosition).inflate(TEMPLE_REACH * 2),
				hermit -> hermit.getUUID().equals(linkedHermit) && hermit.isAlive() && !hermit.isFarewellDying())
				.stream().findFirst().orElse(null);
	}

	/**
	 * PT-8: a Display placed or found with no living Hermit link takes the nearest Hermit of its temple that no Display
	 * near him names. Returns whether it now has a living link.
	 */
	public boolean relink() {
		if (level == null || level.isClientSide || claimed) return livingHermit() != null;
		if (livingHermit() != null) return true;
		HarbingerHermitEntity nearest = level.getEntitiesOfClass(HarbingerHermitEntity.class,
						new AABB(worldPosition).inflate(TEMPLE_REACH),
						hermit -> hermit.isAlive() && !hermit.isFarewellDying() && of(hermit) == null)
				.stream().min(Comparator.comparingDouble(hermit -> hermit.distanceToSqr(worldPosition.getCenter())))
				.orElse(null);
		if (nearest == null) return false;
		linkedHermit = nearest.getUUID();
		BlockEntitySync.markAndSync(this);
		return true;
	}

	/** The Display near {@code hermit} that names him, if any. */
	@Nullable
	public static MortalDisplayBlockEntity of(HarbingerHermitEntity hermit) {
		Level level = hermit.level();
		ChunkPos centre = hermit.chunkPosition();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (!level.hasChunk(centre.x + dx, centre.z + dz)) continue;
				for (BlockEntity entity : level.getChunk(centre.x + dx, centre.z + dz).getBlockEntities().values()) {
					if (entity instanceof MortalDisplayBlockEntity display && hermit.getUUID().equals(display.linkedHermit)
							&& !display.isRemoved()) return display;
				}
			}
		}
		return null;
	}

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (linkedHermit != null) tag.putUUID("LinkedHermit", linkedHermit);
		if (claimant != null) tag.putUUID("Claimant", claimant);
		tag.putBoolean("Claimed", claimed);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		linkedHermit = tag.hasUUID("LinkedHermit") ? tag.getUUID("LinkedHermit") : null;
		claimant = tag.hasUUID("Claimant") ? tag.getUUID("Claimant") : null;
		claimed = tag.getBoolean("Claimed");
	}

}
