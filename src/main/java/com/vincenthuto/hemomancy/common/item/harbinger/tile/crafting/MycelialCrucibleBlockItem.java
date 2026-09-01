package com.vincenthuto.hemomancy.common.item.harbinger.tile.crafting;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting.MorphlingIncubatorItemRenderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Block item for the Mycelial Crucible.
 * Uses the Morphling Incubator's BEWLR as a placeholder model until custom art exists.
 */
public class MycelialCrucibleBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {

    public MycelialCrucibleBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public IClientItemExtensions hemomancy$getClientItemExtensions() {
        return new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer =
                    new MorphlingIncubatorItemRenderer(null, null);

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };
    }
}
