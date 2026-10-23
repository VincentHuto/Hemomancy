package com.vincenthuto.hemomancy.client.render.entity.weave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.entity.weave.ThreadBoltEntity;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** A small hot knot of its tendency streaking at the weaver, fibres trailing behind. */
public class ThreadBoltRenderer extends EntityRenderer<ThreadBoltEntity> {
	public ThreadBoltRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0f;
	}

	@Override
	public void render(ThreadBoltEntity bolt, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 eye = camera.getPosition().subtract(bolt.getPosition(partialTicks));
		Vector3f right = new Vector3f(camera.getLeftVector()).negate();
		Vector3f up = new Vector3f(camera.getUpVector());
		float time = AnimationTime.age(bolt, partialTicks);
		float seed = (bolt.getId() * 0.618f) % 1f;
		float[] colour = ThreadKnotGeometry.hot(ThreadKnotGeometry.vivid(bolt.tendency().getColor()));
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		Matrix4f mat = stack.last().pose();
		Vector3f at = new Vector3f(0, bolt.getBbHeight() / 2, 0);
		ThreadKnotGeometry.tails(vc, mat, eye, at, bolt.getDeltaMovement().scale(4), colour, time, seed, 1f);
		ThreadKnotGeometry.knot(vc, mat, right, up, at, colour, time, seed, 0.7f, 1f);
	}

	@Override
	public ResourceLocation getTextureLocation(ThreadBoltEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
