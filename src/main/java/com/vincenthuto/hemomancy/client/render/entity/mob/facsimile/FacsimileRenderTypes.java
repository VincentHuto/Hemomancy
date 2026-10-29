package com.vincenthuto.hemomancy.client.render.entity.mob.facsimile;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.renderer.RenderType;

/** The fungal growth each facsimile wears over its borrowed skin, one render type per creature, built once. */
public final class FacsimileRenderTypes {
	public static final RenderType BAT = growth("facsimile_bat");
	public static final RenderType ENDERMAN = growth("facsimile_enderman");
	public static final RenderType BLAZE = growth("facsimile_blaze");
	public static final RenderType GOLEM = growth("facsimile_golem");
	public static final RenderType HORSE = growth("facsimile_horse");
	public static final RenderType DONKEY = growth("facsimile_donkey");
	public static final RenderType WOLF = growth("facsimile_wolf");
	public static final RenderType SPIDER = growth("facsimile_spider");

	private FacsimileRenderTypes() {}

	private static RenderType growth(String id) {
		return RenderType.entityCutoutNoCull(Hemomancy.rloc("textures/entity/facsimile/" + id + "_growth.png"));
	}
}
