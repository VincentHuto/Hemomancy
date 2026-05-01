package com.vincenthuto.hemomancy.common.item.tile;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Block item for the Mycelial Crucible.
 * Uses the standard BlockItem — no custom renderer until a model is created.
 */
public class MycelialCrucibleBlockItem extends BlockItem {

    public MycelialCrucibleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
