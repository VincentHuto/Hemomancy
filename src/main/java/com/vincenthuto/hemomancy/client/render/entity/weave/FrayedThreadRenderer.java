package com.vincenthuto.hemomancy.client.render.entity.weave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomFrame;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * An intrusion's bite: a failed Harbinger's memory as a knot of its tendency trailing fibres, tethered back to the
 * loom's echo it tore from. Its windup swells it and flushes it hot; once it has struck it comes apart, widening and
 * fading as it rises; Tenebris ones nearly vanish while faded.
 */
public class FrayedThreadRenderer extends EntityRenderer<FrayedThreadEntity> {
	public FrayedThreadRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0f;
	}

	@Override
	public boolean shouldRender(FrayedThreadEntity thread, Frustum frustum, double x, double y, double z) {
		// The tether reaches back to the loom, so keep drawing while either end is in view.
		return true;
	}

	@Override
	public void render(FrayedThreadEntity thread, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer,
			int light) {
		var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 origin = thread.getPosition(partialTicks);
		Vec3 eye = camera.getPosition().subtract(origin);
		Vector3f right = new Vector3f(camera.getLeftVector()).negate();
		Vector3f up = new Vector3f(camera.getUpVector());
		float time = thread.tickCount + partialTicks;
		float seed = (thread.getId() * 0.618f) % 1f;
		float[] colour = ThreadKnotGeometry.vivid(thread.tendency().getColor());
		float unravel = thread.unravelling() <= 0 ? 0f
				: Mth.clamp((thread.unravelling() + partialTicks) / FrayedThreadEntity.UNRAVEL_TICKS, 0f, 1f);
		float alpha = (thread.isFaded() ? 0.15f : 1f) * (1f - unravel);
		float scale;
		if (unravel > 0) {
			scale = 1.3f + 1.1f * unravel;
		} else if (thread.windup() > 0) {
			float progress = 1f - thread.windup() / (float) SomaticWeaveRules.WINDUP_TICKS;
			scale = 1.3f + 0.6f * progress + 0.15f * Mth.sin(time * 1.2f);
			colour = ThreadKnotGeometry.mix(colour, ThreadKnotGeometry.HOT, 0.25f + 0.5f * progress);
		} else {
			scale = 1.3f;
		}
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		Matrix4f mat = stack.last().pose();
		Vector3f knot = new Vector3f(0, thread.getBbHeight() / 2, 0);
		Vec3 echo = echoAnchor(thread).subtract(origin);
		ThreadKnotGeometry.tether(vc, mat, eye, knot, new Vector3f((float) echo.x, (float) echo.y, (float) echo.z),
				colour, time, seed, alpha * 0.7f);
		ThreadKnotGeometry.tails(vc, mat, eye, knot, thread.getDeltaMovement(), colour, time, seed, alpha);
		ThreadKnotGeometry.knot(vc, mat, right, up, knot, colour, time, seed, scale, alpha);
	}

	private static Vec3 echoAnchor(FrayedThreadEntity thread) {
		BlockState state = thread.level().getBlockState(thread.loomPos());
		Direction facing = state.hasProperty(SomaticLoomBlock.FACING) ? state.getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		return SomaticLoomFrame.toWorld(thread.loomPos(), facing, SomaticLoomGeometry.ECHO_X, SomaticLoomGeometry.ECHO_Y,
				SomaticLoomGeometry.ECHO_Z);
	}

	@Override
	public ResourceLocation getTextureLocation(FrayedThreadEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
