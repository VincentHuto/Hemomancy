package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MnemonicReliquaryBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MnemonicReliquaryBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Draws the moving parts over the Mnemonic Reliquary's baked casket: the gabled lid swinging back on its
 * rear hinge, and the preserved relic lifting off its cushion while its memory veins kindle full-bright.
 * Parts come from tools/model_export/mnemonic_reliquary.py; the anchors below mirror it.
 */
public class MnemonicReliquaryRenderer implements BlockEntityRenderer<MnemonicReliquaryBlockEntity> {
	public static final String[] PART_MODELS = {"lid", "relic", "veins"};

	/** Lid hinge axis in block pixels, north-facing frame; the lid turns back about +x. */
	private static final float[] HINGE = {8F, 9F, 12.5F};
	private static final float LID_OPEN = 100F, RELIC_RISE = 4.5F, RELIC_BOB = .5F;

	public MnemonicReliquaryRenderer(BlockEntityRendererProvider.Context context) {}

	public static ModelResourceLocation part(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/mnemonic_reliquary_" + name));
	}

	@Override
	public void render(MnemonicReliquaryBlockEntity reliquary, float partialTicks, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		float open = reliquary.getLidAngle(partialTicks);
		float eased = open * open * (3 - 2 * open);

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(180 - reliquary.getBlockState().getValue(MnemonicReliquaryBlock.FACING).toYRot()));
		pose.translate(-.5, 0, -.5);

		pose.pushPose();
		pose.translate(HINGE[0] / 16F, HINGE[1] / 16F, HINGE[2] / 16F);
		pose.mulPose(Axis.XP.rotationDegrees(LID_OPEN * eased));
		pose.translate(-HINGE[0] / 16F, -HINGE[1] / 16F, -HINGE[2] / 16F);
		renderPart(mc, "lid", pose, buffers, light, overlay, 1F);
		pose.popPose();

		pose.pushPose();
		if (open > 0 && reliquary.getLevel() != null) {
			float time = reliquary.getLevel().getGameTime() + partialTicks;
			pose.translate(0, (RELIC_RISE + Mth.sin(time * .1F) * RELIC_BOB) * eased / 16F, 0);
		}
		renderPart(mc, "relic", pose, buffers, light, overlay, 1F);
		if (open > .05F)
			renderPart(mc, "veins", pose, buffers, LightTexture.FULL_BRIGHT, overlay, .3F + .7F * eased);
		pose.popPose();

		pose.popPose();
	}

	private static void renderPart(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light,
			int overlay, float tint) {
		var model = mc.getModelManager().getModel(part(name));
		mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
				buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)), null, model, tint, tint, tint,
				light, overlay);
	}

	@Override
	public AABB getRenderBoundingBox(MnemonicReliquaryBlockEntity reliquary) {
		// The open lid swings a little past the back of the block.
		return new AABB(reliquary.getBlockPos()).inflate(.25);
	}
}
