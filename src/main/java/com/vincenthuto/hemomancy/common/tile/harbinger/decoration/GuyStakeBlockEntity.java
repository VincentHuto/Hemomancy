package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A big top's guy stake. It holds nothing saved: the client works out where its line ties off on the tent
 * ({@code GuyLineRules.anchor}) and keeps that here until it is time to look again, so a torn wall drops its line.
 */
public class GuyStakeBlockEntity extends HemoBlockEntity {
	private Vec3 anchor;
	private long framesSinceRead = -1L;

	public GuyStakeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.guy_stake.get(), pos, state);
	}

	/** The cached tie-off point, or null when there is none. */
	public Vec3 anchor() {
		return anchor;
	}

	/** Frames drawn since the tie-off was last read, or -1 before the first read. */
	public long framesSinceRead() {
		return framesSinceRead;
	}

	public void setAnchor(Vec3 anchor, long frames) {
		this.anchor = anchor;
		this.framesSinceRead = frames;
	}
}
