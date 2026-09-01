package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MorphlingIncubatorModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MorphlingIncubatorRenderer implements BlockEntityRenderer<MorphlingIncubatorBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/morphling_incubator_model.png");

	private final MorphlingIncubatorModel model;
	Minecraft mc;

	public MorphlingIncubatorRenderer(BlockEntityRendererProvider.Context p_173636_) {
		mc = Minecraft.getInstance();
		this.model = new MorphlingIncubatorModel(p_173636_.bakeLayer(MorphlingIncubatorModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(MorphlingIncubatorBlockEntity te) {
		return true;
	}

	@Override
	public void render(MorphlingIncubatorBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		renderCreature(ms, bufferIn, te, combinedLightIn, partialTicks);
		renderFluidLevel(ms, bufferIn, te);
		if (bufferIn instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.INCUBATOR_FLUID);
		}


		// ── Blood volume ring ────────────────────────────────────────────────────

		// ── Render center item ──────────────────────────────────────────────────
		ItemStack centerStack = te.getCenterStack();
		if (!centerStack.isEmpty()) {
			ms.pushPose();
			ms.translate(0.5F, 1.05F, 0.5F);
			ms.mulPose(Vector3.YP.rotationDegrees(te.getLevel().getGameTime() * 2f).toMoj());
			ms.scale(0.5f, 0.5f, 0.5f);

			Vec3 loc = new Vec3(te.getBlockPos().getX() + 0.5F,
					te.getBlockPos().getY() + 1.05F,
					te.getBlockPos().getZ() + 0.5F);
			te.getLevel().addParticle(ParticleTypes.MYCELIUM, loc.x, loc.y, loc.z, 0.0D, 0.0D, 0.0D);

			Minecraft.getInstance().getItemRenderer().renderStatic(null, centerStack, ItemDisplayContext.FIXED, true,
					ms, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
			ms.popPose();
		}

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

	protected void renderNameTag(MorphlingIncubatorBlockEntity te, Component component, PoseStack pose,
			MultiBufferSource buffer, int color) {
		pose.pushPose();
		pose.translate(0.0F, 1, 0.0F);
		pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
		pose.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = pose.last().pose();
		float f1 = mc.options.getBackgroundOpacity(0.25F);
		int j = (int) (f1 * 255.0F) << 24;
		Font font = mc.font;
		float f2 = (float) (-font.width(component) / 2);
		font.drawInBatch(component, f2, (float) 0, 553648127, false, matrix4f, buffer, Font.DisplayMode.NORMAL, j,
				color);

		pose.popPose();
	}

	// ========================== BLOOD VOLUME RING ==========================

	private void drawBloodVolumeRing(PoseStack stack, MultiBufferSource bufferIn,
			Vec3 center, double fillRatio, float currentTime) {
		double ringRadius = 1.1;
		double ringY = center.y + 1.75;
		double cx = center.x;
		double cz = center.z;
		float rotOffset = currentTime * 0.5f;

		int segments = 64;
		int filledSegments = (int) (segments * fillRatio);

		for (int i = 0; i < segments; i++) {
			float angle1 = rotOffset + (360f / segments) * i;
			float angle2 = rotOffset + (360f / segments) * (i + 1);

			double wave1 = Math.sin(Math.toRadians(angle1 * 3) + currentTime * 0.1) * 0.03;
			double wave2 = Math.sin(Math.toRadians(angle2 * 3) + currentTime * 0.1) * 0.03;

			double x1 = cx + Math.cos(Math.toRadians(angle1)) * ringRadius;
			double z1 = cz + Math.sin(Math.toRadians(angle1)) * ringRadius;
			double x2 = cx + Math.cos(Math.toRadians(angle2)) * ringRadius;
			double z2 = cz + Math.sin(Math.toRadians(angle2)) * ringRadius;

			Vec3 from = new Vec3(x1, ringY + wave1, z1);
			Vec3 to = new Vec3(x2, ringY + wave2, z2);

			if (i < filledSegments) {
			} else {
			}
		}
	}

	// ========================== LOW-LEVEL BEAM DRAWING ==========================

	private static void drawBeamSegment(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LOOM_BEAM_CORE);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		drawLaser(builder, positionMatrix, endLaser, startLaser, 0, 0, 0, alpha * 0.6f, thickness * 1.5f,
				v, v + diffY * -5.5, new Vector3f(sortPos.x(), sortPos.y() - 0.05f, sortPos.z()));
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	private static void drawBeamSegmentGlow(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LOOM_BEAM_GLOW);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	private static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			float r, float g, float b, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		Vector3f SE = new Vector3f(to);
		SE.sub(from);
		float seLen = SE.length();
		if (seLen < 1e-6f) return;

		Vector3f P = new Vector3f((float) player.getX() - sortPos.x(), (float) player.getEyeY() - sortPos.y(),
				(float) player.getZ() - sortPos.z());
		Vector3f PS = new Vector3f(from);
		PS.sub(P);
		Vector3f adjustedVec = new Vector3f(PS);
		adjustedVec.cross(SE);
		float len = adjustedVec.length();
		if (len < 1e-6f) {
			adjustedVec.set(0, 1, 0);
			adjustedVec.cross(SE);
			len = adjustedVec.length();
			if (len < 1e-6f) {
				adjustedVec.set(1, 0, 0);
				adjustedVec.cross(SE);
				len = adjustedVec.length();
				if (len < 1e-6f) return;
			}
		}
		adjustedVec.mul(thickness / len);

		Vector3f perp2 = new Vector3f(adjustedVec);
		Vector3f seNorm = new Vector3f(SE);
		seNorm.normalize();
		perp2.cross(seNorm);
		float len2 = perp2.length();
		if (len2 > 1e-6f) {
			perp2.mul(thickness / len2);
		} else {
			perp2.set(adjustedVec);
		}

		drawQuad(builder, positionMatrix, from, to, adjustedVec, r, g, b, alpha, v1, v2);
		drawQuad(builder, positionMatrix, from, to, perp2, r, g, b, alpha, v1, v2);
	}

	private static void drawQuad(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			Vector3f offset, float r, float g, float b, float alpha, double v1, double v2) {
		Vector3f p1 = new Vector3f(from).add(offset);
		Vector3f p2 = new Vector3f(from).sub(offset);
		Vector3f p3 = new Vector3f(to).add(offset);
		Vector3f p4 = new Vector3f(to).sub(offset);

		builder.addVertex(positionMatrix, p1.x(), p1.y(), p1.z()).setColor(r, g, b, alpha).setUv(1, (float) v1)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880);
		builder.addVertex(positionMatrix, p3.x(), p3.y(), p3.z()).setColor(r, g, b, alpha).setUv(1, (float) v2)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880);
		builder.addVertex(positionMatrix, p4.x(), p4.y(), p4.z()).setColor(r, g, b, alpha).setUv(0, (float) v2)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880);
		builder.addVertex(positionMatrix, p2.x(), p2.y(), p2.z()).setColor(r, g, b, alpha).setUv(0, (float) v1)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880);
	}

	/**
	 * Renders the morphling incubator entity model. Flips Y-down → Y-up
	 * (Blockbench convention) and rotates to match the block's FACING direction.
	 */
	private void renderCreature(PoseStack poseStack, MultiBufferSource bufferIn,
			MorphlingIncubatorBlockEntity te, int combinedLightIn, float partialTicks) {
		poseStack.pushPose();

		poseStack.translate(0.5D, 0D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case SOUTH -> 0f;
			case WEST  -> 90f;
			default    -> 0f;
		};
		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		float ageInTicks = te.getLevel().getGameTime() + partialTicks;
		model.setupAnim(ageInTicks);

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderCreature(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);

		poseStack.popPose();
	}

	/**
	 * Renders a translucent red fluid box inside the Tank glass,
	 * whose height is proportional to the blood fill percentage.
	 */
	private void renderFluidLevel(PoseStack poseStack, MultiBufferSource bufferIn,
			MorphlingIncubatorBlockEntity te) {
		double current = te.getBloodVolume();
		double max = te.getMaxBloodVolume();
		if (max <= 0 || current <= 0) return;

		float fillPct = (float) Math.min(current / max, 1.0);

		// Tank box: 10x16x10 centred, inset slightly so fluid sits inside glass
		float halfW = 4.5f / 16f;
		float halfD = 4.5f / 16f;
		float tankBottomY = 4f / 16f;
		float tankHeight = 15f / 16f;
		float fluidHeight = tankHeight * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, tankBottomY, 0.5f);

		// Blood colour: dark red, semi-transparent
		int r = 153, g = 13, b = 13, a = 191;

		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.INCUBATOR_FLUID);
		Matrix4f mat = poseStack.last().pose();

		renderColorBox(vc, mat, -halfW, 0, -halfD, halfW, fluidHeight, halfD, r, g, b, a);

		poseStack.popPose();
	}

	private static void renderColorBox(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0, float x1, float y1, float z1,
			int r, int g, int b, int a) {
		// Top
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		// Bottom
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		// North (z0)
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		// South (z1)
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		// West (x0)
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		// East (x1)
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
	}
}

