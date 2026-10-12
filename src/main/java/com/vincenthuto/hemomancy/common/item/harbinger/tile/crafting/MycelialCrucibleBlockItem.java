package com.vincenthuto.hemomancy.common.item.harbinger.tile.crafting;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Block item for the Mycelial Crucible. Its item model parents the baked block model authored by
 * tools/model_export/mycelial_crucible.py.
 */
public class MycelialCrucibleBlockItem extends BlockItem {

    public MycelialCrucibleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
