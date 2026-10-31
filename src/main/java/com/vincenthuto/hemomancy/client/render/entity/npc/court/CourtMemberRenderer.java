package com.vincenthuto.hemomancy.client.render.entity.npc.court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.npc.court.CourtHumanoidModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.court.CourtMemberModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.court.CourtMemberModels;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a Court member from its role's generated model ({@link CourtMemberModels.Row}): the body, an emissive pass for
 * a role that carries biological light, and on the jointed humanoid its house strain at the role's stage
 * ({@link CourtStrainLayer}). A member swimming in open water is laid along its stroke.
 */
public class CourtMemberRenderer extends MobRenderer<CourtMemberEntity, CourtMemberModel> {
	private static final float SWIM_TILT = 72.0F;

	private final ResourceLocation texture;

	public CourtMemberRenderer(EntityRendererProvider.Context context, CourtMemberModels.Row row) {
		super(context, row.factory().apply(context.bakeLayer(row.layer())), row.shadow());
		this.texture = row.texture();
		if (row.glow() != null) this.addLayer(new Glow(this, RenderType.entityTranslucentEmissive(row.glow())));
		if (this.model instanceof CourtHumanoidModel) this.addLayer(new CourtStrainLayer(this, context));
	}

	@Override
	public ResourceLocation getTextureLocation(CourtMemberEntity entity) {
		return texture;
	}

	/**
	 * A Veiled resident folded away is drawn as the faint translucent outline vanilla gives an invisible mob a player
	 * can still make out: there, if you look for it.
	 */
	@Override
	protected boolean isBodyVisible(CourtMemberEntity entity) {
		return super.isBodyVisible(entity) && !folded(entity);
	}

	public static boolean folded(CourtMemberEntity entity) {
		return entity.role() == com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.VEILED_RESIDENT && entity.isWithdrawn();
	}

	@Override
	protected void setupRotations(CourtMemberEntity entity, PoseStack poseStack, float bob, float yBodyRot,
			float partialTick, float scale) {
		super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
		if (CourtHumanoidModel.swimming(entity)) {
			poseStack.translate(0.0F, 1.0F, 0.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(-SWIM_TILT));
			poseStack.translate(0.0F, -1.0F, 0.0F);
		}
	}

	/** Biological light: the role's glow atlas, full bright. */
	private static final class Glow extends RenderLayer<CourtMemberEntity, CourtMemberModel> {
		private final RenderType type;

		Glow(CourtMemberRenderer renderer, RenderType type) {
			super(renderer);
			this.type = type;
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, CourtMemberEntity entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			if (entity.isInvisible() || folded(entity)) return;
			getParentModel().renderToBuffer(poseStack, buffers.getBuffer(type), 0xF000F0, OverlayTexture.NO_OVERLAY);
		}
	}
}
