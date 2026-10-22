package com.vincenthuto.hemomancy.client.render.entity.weave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadHazardEntity;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** A thread's lingering patch: a faint ring of its colour on the ground, pulsing, under its own embers or miasma. */
public class ThreadHazardRenderer extends EntityRenderer<ThreadHazardEntity> {
	private static final int SEGMENTS = 24;

	public ThreadHazardRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0f;
	}

	@Override
	public void render(ThreadHazardEntity hazard, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		float time = hazard.tickCount + partialTicks;
		float[] c = ThreadKnotGeometry.vivid(hazard.tendency().getColor());
		float r = hazard.radius();
		float pulse = 0.25f + 0.15f * Mth.sin(time * 0.2f);
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		Matrix4f mat = stack.last().pose();
		float y = 0.02f;
		for (int i = 0; i < SEGMENTS; i++) {
			float a0 = i * Mth.TWO_PI / SEGMENTS, a1 = (i + 1) * Mth.TWO_PI / SEGMENTS;
			float c0 = Mth.cos(a0), s0 = Mth.sin(a0), c1 = Mth.cos(a1), s1 = Mth.sin(a1);
			float inner = r * 0.55f;
			// A soft disc fading out toward the rim, then a brighter rim line.
			ThreadKnotGeometry.emitQuad(vc, mat, c0 * inner, y, s0 * inner, c[0], c[1], c[2], pulse * 0.6f,
					c0 * r, y, s0 * r, c[0], c[1], c[2], 0f, c1 * r, y, s1 * r, c[0], c[1], c[2], 0f,
					c1 * inner, y, s1 * inner, c[0], c[1], c[2], pulse * 0.6f);
			ThreadKnotGeometry.emitQuad(vc, mat, 0, y, 0, c[0], c[1], c[2], pulse * 0.4f,
					c0 * inner, y, s0 * inner, c[0], c[1], c[2], pulse * 0.6f, c1 * inner, y, s1 * inner, c[0], c[1], c[2], pulse * 0.6f,
					0, y, 0, c[0], c[1], c[2], pulse * 0.4f);
			float rim0 = r * 0.92f;
			ThreadKnotGeometry.emitQuad(vc, mat, c0 * rim0, y + 0.01f, s0 * rim0, c[0], c[1], c[2], pulse,
					c0 * r, y + 0.01f, s0 * r, c[0], c[1], c[2], pulse, c1 * r, y + 0.01f, s1 * r, c[0], c[1], c[2], pulse,
					c1 * rim0, y + 0.01f, s1 * rim0, c[0], c[1], c[2], pulse);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(ThreadHazardEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
