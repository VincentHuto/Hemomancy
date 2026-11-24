package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusTrueRingmasterModel;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * The Ringmaster's true self in his finale (2026-11-18), worn by his copy from the reveal on: the showman with his
 * mask, ribs, veins, staff core and strings lit by the glow atlas. He walks the ring and fights in it, so it stands on
 * the entity's feet, drawn {@link #TRUE_SCALE} times over by the {@code TrueSelfRenderer} that picks it.
 */
public class CircusTrueRingmasterRenderer extends MobRenderer<CircusRingmasterEntity, CircusTrueRingmasterModel> {
	/**
	 * How much larger than his body the true self is drawn: half again, near four blocks to the hat's brim and five to
	 * its crown, so he towers over the player and reads across the ring without outgrowing the melee he still fights.
	 */
	public static final float TRUE_SCALE = 1.5F;
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_ringmaster.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/npc/harbinger/circus/circus_true_ringmaster_glow.png"));

	public CircusTrueRingmasterRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusTrueRingmasterModel(), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusRingmasterEntity entity) {
		return TEXTURE;
	}

	/** The body is a man's box; the drawn figure stands three times as tall, its strings running far up into the dark. */
	@Override
	public boolean shouldRender(CircusRingmasterEntity entity, Frustum frustum, double x, double y, double z) {
		AABB drawn = new AABB(entity.getX() - 3.0D, entity.getY(), entity.getZ() - 3.0D, entity.getX() + 3.0D,
				entity.getY() + 12.0D, entity.getZ() + 3.0D);
		return entity.shouldRender(x, y, z) && frustum.isVisible(drawn);
	}
}
