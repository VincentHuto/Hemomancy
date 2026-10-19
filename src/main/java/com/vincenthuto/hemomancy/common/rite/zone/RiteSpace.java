package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * The lasting space a completed Harbinger rite leaves above its floor: the cube braziers counted in, keyed to the
 * purpose of the last rite completed there.
 *
 * @param focus the Cardinal Focus at the floor's centre
 * @param floor the floor the rite completed on; the space ends when another floor (or none) is formed there
 * @param half  blocks from the Focus to the floor's edge
 * @param rite  the rite that consecrated the space
 */
public record RiteSpace(BlockPos focus, ResourceKey<Level> dimension, UUID owner, CardinalRitePurpose purpose,
		ResourceLocation floor, int half, ResourceLocation rite, long createdTick) {
	public boolean contains(BlockPos pos, ResourceKey<Level> level) {
		return dimension.equals(level) && RiteSpaceRules.covers(focus, half, pos);
	}
}
