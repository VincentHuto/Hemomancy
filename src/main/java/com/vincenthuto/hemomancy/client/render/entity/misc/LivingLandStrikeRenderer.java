package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.manipulation.LivingLandStrikeEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The ground a Living Land strike carries (core-loop Track 9): each block drawn whole, lit where it stands, a hair
 * smaller than a cell so it reads as torn out rather than placed. The blood that holds it (the vein net over each
 * block and the cord along the column) is drawn by {@code LivingLandRenderer} from the world-effect pass.
 */
public final class LivingLandStrikeRenderer extends EntityRenderer<LivingLandStrikeEntity> {
	/** The carried blocks are drawn this much smaller than a cell. */
	public static final float BLOCK_SCALE = 0.94F;
	private final BlockRenderDispatcher blocks;

	public LivingLandStrikeRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.blocks = context.getBlockRenderDispatcher();
		this.shadowRadius = 0.0F;
	}

	@Override
	public void render(LivingLandStrikeEntity strike, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int packedLight) {
		Vec3 head = strike.getPosition(partialTick);
		// A raise's marker carries nothing: its blocks stand in the world.
		for (int i = 0; i < (strike.isRaise() ? 0 : strike.length()); i++) {
			BlockState state = strike.carried(i);
			if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) continue;
			Vec3 centre = strike.blockCentre(i, head);
			int light = LevelRenderer.getLightColor(strike.level(), BlockPos.containing(centre));
			poseStack.pushPose();
			poseStack.translate(centre.x - head.x, centre.y - head.y, centre.z - head.z);
			poseStack.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
			poseStack.translate(-0.5D, -0.5D, -0.5D);
			blocks.renderSingleBlock(state, poseStack, buffers, light, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
		super.render(strike, yaw, partialTick, poseStack, buffers, packedLight);
	}

	@Override
	public boolean shouldRender(LivingLandStrikeEntity strike, net.minecraft.client.renderer.culling.Frustum frustum,
			double camX, double camY, double camZ) {
		return frustum.isVisible(strike.getBoundingBoxForCulling());
	}

	@Override
	public ResourceLocation getTextureLocation(LivingLandStrikeEntity strike) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
