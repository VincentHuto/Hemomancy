package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.CovenantThroneModel;
import com.vincenthuto.hemomancy.common.block.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Covenant Throne as four solid black cubes assembled into a
 * throne silhouette:
 *
 * <ul>
 *   <li>A tall <b>backrest</b> slab at the rear.</li>
 *   <li>Two <b>armrests</b> on the left and right sides.</li>
 *   <li>A shorter, cube-like <b>seat</b> in the centre.</li>
 * </ul>
 *
 * <p>Uses the same {@link RenderType#entityTranslucentCull} black-box technique
 * as {@link SanguineMonolithRenderer}: all faces are coloured {@code 0xFF0D0505}
 * (near-black with a faint red tint), making the throne read as a monolithic,
 * deeply dark object.</p>
 *
 * <p>The model is oriented for SOUTH facing and rotated around the Y axis to
 * match whichever direction the placed block is facing.</p>
 */
public class CovenantThroneRenderer implements BlockEntityRenderer<CovenantThroneBlockEntity> {

	/** Reuses the monolith texture — the throne is rendered solid black so the
	 *  actual UV sample is not visible. */
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/model_sanguine_monolith.png");

	/** Near-black with a faint crimson tint, matching the Sanguine Monolith. */
	private static final int BLACK_COLOR = 0xFF0D0505;

	private final CovenantThroneModel model;

	public CovenantThroneRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new CovenantThroneModel(context.bakeLayer(CovenantThroneModel.LAYER_LOCATION));
	}

	@Override
	public void render(CovenantThroneBlockEntity te, float partialTicks, PoseStack ms,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		Direction facing = te.getBlockState().getValue(CovenantThroneBlock.FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case WEST  ->  90f;
			default    ->   0f; // SOUTH
		};

		ms.pushPose();
		// Translate to the block centre at the top of the 2-block multiblock space,
		// then flip on X (same convention as SanguineMonolithRenderer).
		ms.translate(0.5D, 1.5D, 0.5D);
		ms.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		ms.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vc = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(ms, vc, combinedLightIn, OverlayTexture.NO_OVERLAY, BLACK_COLOR);

		ms.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(CovenantThroneBlockEntity te) {
		return true;
	}
}
