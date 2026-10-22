package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Draws what moves inside and around the baked Morphling Incubator model: the blood in its glass tank,
 * the incubated item floating under the umbilical cord, and the orbiting catalysts. Tank dimensions
 * mirror tools/model_export/morphling_incubator.py.
 */
public class MorphlingIncubatorRenderer implements BlockEntityRenderer<MorphlingIncubatorBlockEntity> {
	private static final float FLUID_RADIUS = 4.4F / 16F;
	private static final float FLUID_BOTTOM = 5.55F / 16F;
	private static final float FLUID_TOP = 18.4F / 16F;
	private static final float ITEM_Y = 11F / 16F;
	private static final float TAN = (float) Math.tan(Math.PI / 8);

	Minecraft mc;

	public MorphlingIncubatorRenderer(BlockEntityRendererProvider.Context p_173636_) {
		mc = Minecraft.getInstance();
	}

	@Override
	public boolean shouldRenderOffScreen(MorphlingIncubatorBlockEntity te) {
		return true;
	}

	@Override
	public void render(MorphlingIncubatorBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		renderFluidLevel(ms, bufferIn, te);
		if (bufferIn instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.INCUBATOR_FLUID);
		}
		renderIncubatedItem(te, partialTicks, ms, bufferIn, combinedLightIn, combinedOverlayIn);

		// ── Render orbiting catalyst items ──────────────────────────────────────
		java.util.List<ItemStack> catalysts = te.getCatalystStacks();
		int items = 0;
		for (ItemStack s : catalysts) {
			if (!s.isEmpty()) items++;
		}

		if (items > 0) {
			double time = te.dataAccess.get(0);
			int idx = 0;
			for (int i = 0; i < catalysts.size(); i++) {
				ItemStack stack = catalysts.get(i);
				if (stack.isEmpty()) continue;

				ms.pushPose();

				float r = 1.5f;
				float w = 1.0f;
				float t = (float) time * 0.125f;
				float Cx = te.getBlockPos().getX() + 0.5f;
				float Cy = te.getBlockPos().getY() + 1;
				float Cz = te.getBlockPos().getZ() + 0.5f;

				if (t < 1.5 && t > 0) {
					r = r - (1.5f - t);
				}

				double angle = w * t + idx * Math.PI / 2;
				float x = (float) (Cx + r * Math.cos(angle));
				float y;
				if (idx % 2 == 0) {
					y = Cy + (float) (Math.sin(te.getLevel().getGameTime() * 0.1f) * 0.1f);
				} else {
					y = Cy + (float) (Math.cos(te.getLevel().getGameTime() * 0.1f) * 0.1f);
				}
				float z = (float) (Cz + r * Math.sin(angle));

				Vector3f locVec = new Vector3f(x, y, z);

				ms.translate(x - te.getBlockPos().getX(), y - te.getBlockPos().getY() - 0.1865f,
						z - te.getBlockPos().getZ());

				te.getLevel().addParticle(EmberParticleFactory.createData(ParticleColor.PURPLE, 2, 0.12f, 14),
						locVec.x(), locVec.y(), locVec.z(), 0.0D, 0.0D, 0.0D);

				if (time > 0 && time < 25) {
					for (Vec3 v : HLParticleUtils.randomSphere(4, 0.25f, 0.25f)) {
						te.getLevel().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
								locVec.x() + v.x, locVec.y() + v.y, locVec.z() + v.z, 0.0D, 0.0D, 0.0D);
					}
				}

				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn,
						combinedOverlayIn, ms, bufferIn, te.getLevel(), 0);
				ms.popPose();
				idx++;
			}
		}
	}

	/**
	 * The centre stack drifts in the tank's blood beneath the cord's bulb, turning slowly.
	 */
	private void renderIncubatedItem(MorphlingIncubatorBlockEntity te, float partialTicks, PoseStack ms,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		ItemStack centerStack = te.getCenterStack();
		if (centerStack.isEmpty() || te.getLevel() == null) return;
		float time = te.getLevel().getGameTime() + partialTicks;
		float y = ITEM_Y + (float) Math.sin(time * 0.06F) * 0.4F / 16F;

		ms.pushPose();
		ms.translate(0.5F, y, 0.5F);
		ms.mulPose(Vector3.YP.rotationDegrees(time * 1.2F).toMoj());
		ms.scale(0.4F, 0.4F, 0.4F);
		te.getLevel().addParticle(ParticleTypes.MYCELIUM, te.getBlockPos().getX() + 0.5F,
				te.getBlockPos().getY() + y, te.getBlockPos().getZ() + 0.5F, 0.0D, 0.0D, 0.0D);
		mc.getItemRenderer().renderStatic(null, centerStack, ItemDisplayContext.FIXED, true,
				ms, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
		ms.popPose();
	}

	/**
	 * Renders translucent blood as an octagonal column inside the tank glass, its height proportional
	 * to the stored blood.
	 */
	private void renderFluidLevel(PoseStack poseStack, MultiBufferSource bufferIn,
			MorphlingIncubatorBlockEntity te) {
		double current = te.getBloodVolume();
		double max = te.getMaxBloodVolume();
		if (max <= 0 || current <= 0) return;

		float fillPct = (float) Math.min(current / max, 1.0);
		float top = FLUID_BOTTOM + (FLUID_TOP - FLUID_BOTTOM) * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, 0, 0.5f);

		// Blood colour: dark red, semi-transparent
		int r = 153, g = 13, b = 13, a = 191;

		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.INCUBATOR_FLUID);
		renderOctagonPrism(vc, poseStack.last().pose(), FLUID_RADIUS, FLUID_BOTTOM, top, r, g, b, a);

		poseStack.popPose();
	}

	/** Upright regular octagon of apothem `apothem` about the origin, matching the model's octagonal glass. */
	private static void renderOctagonPrism(VertexConsumer vc, Matrix4f mat, float apothem, float y0, float y1,
			int r, int g, int b, int a) {
		float s = apothem * TAN;
		float[][] v = {{apothem, -s}, {apothem, s}, {s, apothem}, {-s, apothem},
				{-apothem, s}, {-apothem, -s}, {-s, -apothem}, {s, -apothem}};
		for (int i = 0; i < 8; i++) {
			float[] p = v[i], q = v[(i + 1) % 8];
			vc.addVertex(mat, p[0], y1, p[1]).setColor(r, g, b, a);
			vc.addVertex(mat, p[0], y0, p[1]).setColor(r, g, b, a);
			vc.addVertex(mat, q[0], y0, q[1]).setColor(r, g, b, a);
			vc.addVertex(mat, q[0], y1, q[1]).setColor(r, g, b, a);
		}
		// Each cap is a centre rectangle and two trapezoids.
		int[][] caps = {{0, 1, 4, 5}, {1, 2, 3, 4}, {5, 6, 7, 0}};
		for (float y : new float[] {y0, y1}) {
			for (int[] quad : caps) {
				for (int i : quad) {
					vc.addVertex(mat, v[i][0], y, v[i][1]).setColor(r, g, b, a);
				}
			}
		}
	}
}
