package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The one read of a block's blood. Every path to a reservoir's volume ({@link IBloodReservoir#getBloodCapability},
 * {@code HemoCapabilityAccess.getBloodVolume(BlockEntity)}, projection) comes here, so the volume is settled against
 * the level's clock before anyone sees it.
 */
public final class ReservoirBlood {
	private ReservoirBlood() {
	}

	/**
	 * The block entity whose tank {@code blockEntity} reads: a multiblock's filler reads its main block's, when that is a
	 * reservoir and not itself a filler; a joined vessel's part reads the part holding the vessel's tank
	 * ({@link SharedTankMember}); anything else reads its own.
	 */
	public static BlockEntity tank(BlockEntity blockEntity) {
		if (blockEntity instanceof FillerBlockEntity filler) {
			BlockEntity main = filler.mainBlockEntity();
			if (main instanceof IBloodReservoir && !(main instanceof FillerBlockEntity)) return main;
		}
		if (blockEntity instanceof SharedTankMember member) {
			BlockEntity holder = member.tankHolder();
			if (holder != null) return holder;
		}
		return blockEntity;
	}

	/**
	 * The block's volume, clotted up to now. A block with no tank reads as an empty tank of size zero; a filler reads
	 * its main block's tank ({@link #tank}), or no tank when it has none.
	 */
	public static BlockBloodVolume settled(BlockEntity blockEntity) {
		blockEntity = tank(blockEntity);
		BlockBloodVolume volume = blockEntity.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME);
		if (blockEntity instanceof IBloodReservoir reservoir && !reservoir.holdsBlood()) {
			volume.setMaxBloodVolume(0.0D);
			volume.setBloodVolume(0.0D);
			volume.scrapeClot(volume.clotted());
			return volume;
		}
		if (blockEntity.getLevel() != null) {
			long now = blockEntity.getLevel().getGameTime();
			if (riteHeld(blockEntity)) volume.pause(now);
			else volume.settle(now, held(blockEntity));
		}
		return volume;
	}

	/** Whether something alive holds this blood (the Gourdvine Tap): it never clots and reads as freshly fed. */
	public static boolean held(BlockEntity blockEntity) {
		return tank(blockEntity) instanceof IBloodReservoir reservoir && reservoir.isLivingVessel();
	}

	/**
	 * A station held by an upgrade rite, whose snapshot must not change while the rite runs: its clock stops, so its
	 * blood neither clots nor freshens (NQ-01-6).
	 */
	public static boolean riteHeld(BlockEntity blockEntity) {
		return !held(blockEntity) && tank(blockEntity) instanceof UpgradeableStation station && station.isRiteLocked();
	}
}
