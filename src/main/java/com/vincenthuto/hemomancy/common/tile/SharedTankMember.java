package com.vincenthuto.hemomancy.common.tile;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A block entity that is one part of a joined vessel (Specimen Vats joined into one, core-loop Track 5) and reads the
 * vessel's one tank: every read of its blood ({@link ReservoirBlood#tank}) reaches the part that holds it.
 */
public interface SharedTankMember {
	/** The block entity holding this vessel's tank: this one when it holds the tank itself. */
	BlockEntity tankHolder();
}
