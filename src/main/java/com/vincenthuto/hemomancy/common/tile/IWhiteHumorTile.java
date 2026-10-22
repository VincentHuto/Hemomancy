package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IWhiteHumorTile {

	default void sendUpdates() {
		if (this instanceof BlockEntity blockEntity) BlockEntitySync.markAndSync(blockEntity);
	}

}
