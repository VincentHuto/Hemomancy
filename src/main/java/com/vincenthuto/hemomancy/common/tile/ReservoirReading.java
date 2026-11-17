package com.vincenthuto.hemomancy.common.tile;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A tank as a screen draws it (core-loop Track 3): fresh blood over a band of clot, in a tank of {@code capacity}, with
 * {@code freshness} (1 just fed, 0 at the end of the fresh window) shown as colour draining from the blood, and the
 * state of its suture ({@code link}, synced from the server's routing pass) so a stalled machine says why.
 */
public record ReservoirReading(double fresh, double clotted, double capacity, double freshness, boolean living,
		ReservoirReadout.Link link) {
	public static final ReservoirReading EMPTY = new ReservoirReading(0.0D, 0.0D, 0.0D, 1.0D, false, ReservoirReadout.Link.NONE);

	public ReservoirReading(double fresh, double clotted, double capacity, double freshness, boolean living) {
		this(fresh, clotted, capacity, freshness, living, ReservoirReadout.Link.NONE);
	}

	/** A plain amount with no clot, for a tank that is not a block's (a gourd, the bloodline pool). */
	public static ReservoirReading plain(double fresh, double capacity) {
		return new ReservoirReading(fresh, 0.0D, capacity, 1.0D, false);
	}

	public static ReservoirReading of(BlockEntity blockEntity) {
		if (blockEntity == null) return EMPTY;
		BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
		boolean living = ReservoirBlood.held(blockEntity);
		double freshness = living || blockEntity.getLevel() == null ? 1.0D
				: volume.freshness(blockEntity.getLevel().getGameTime());
		return new ReservoirReading(volume.getBloodVolume(), volume.clotted(), volume.capacity(), freshness, living,
				volume.link());
	}

	/**
	 * A reading whose fresh blood and room come from a menu's synced numbers, and whose clot and freshness come from the
	 * block: {@code room} is the menu's maximum, which is the tank less its clot.
	 */
	public static ReservoirReading ofMenu(double fresh, double room, BlockEntity blockEntity) {
		ReservoirReading block = of(blockEntity);
		return new ReservoirReading(fresh, block.clotted(), room + block.clotted(), block.freshness(), block.living(),
				block.link());
	}
}
