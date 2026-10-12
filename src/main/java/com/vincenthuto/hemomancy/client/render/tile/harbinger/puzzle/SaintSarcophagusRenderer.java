package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import static com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
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
 * Draws the moving parts over the Saint Sarcophagus's baked tomb: the gisant lid sliding aside and tipping
 * over the rim, the offering blood rising in the bowl, the cresset coals once consecrated, and the
 * corpus's memory veins kindling with its state. Parts and anchors come from
 * tools/model_export/saint_sarcophagus.py via {@code SarcophagusGeometry}.
 */
public class SaintSarcophagusRenderer implements BlockEntityRenderer<SaintSarcophagusBlockEntity> {
	public static final String[] PART_MODELS = {"lid", "blood", "coals", "veins"};

	public SaintSarcophagusRenderer(BlockEntityRendererProvider.Context context) {}

	public static ModelResourceLocation part(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/saint_sarcophagus_" + name));
	}

	@Override
	public void render(SaintSarcophagusBlockEntity tomb, float partialTicks, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		float time = tomb.getLevel() == null ? 0 : tomb.getLevel().getGameTime() + partialTicks;
		float open = tomb.getLidAngle(partialTicks);

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(180 - tomb.getBlockState().getValue(SaintSarcophagusBlock.FACING).toYRot()));
		pose.translate(-.5, 0, -.5);

		// The lid slides east off the tomb, then leans down over the rim edge.
		pose.pushPose();
		float slide = smooth(Math.min(open / .7F, 1)), tip = smooth(Math.max((open - .6F) / .4F, 0));
		pose.translate(LID_PIVOT_X / 16F, LID_PIVOT_Y / 16F, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(-LID_TIP * tip));
		pose.translate(-LID_PIVOT_X / 16F + LID_SLIDE * slide / 16F, -LID_PIVOT_Y / 16F, 0);
		renderPart(mc, "lid", pose, buffers, light, overlay, 1F);
		pose.popPose();

		float fill = tomb.isConsecrated() ? 1F
				: Math.min(tomb.getOfferingBloodVolume(), SaintSarcophagusBlockEntity.OFFERING_THRESHOLD)
						/ (float) SaintSarcophagusBlockEntity.OFFERING_THRESHOLD;
		if (fill > 0) {
			pose.pushPose();
			pose.translate(0, POOL_FLOOR / 16F, 0);
			pose.scale(1, fill, 1);
			pose.translate(0, -POOL_FLOOR / 16F, 0);
			renderPart(mc, "blood", pose, buffers, light, overlay, 1F);
			pose.popPose();
		}

		if (tomb.isConsecrated())
			renderPart(mc, "coals", pose, buffers, LightTexture.FULL_BRIGHT, overlay, .8F + .2F * Mth.sin(time * .37F));

		if (open > .05F) {
			float pulse = switch (tomb.getCorpusState()) {
				case DORMANT -> .35F;
				case RESPONSIVE -> .6F + .15F * Mth.sin(time * .08F);
				case AWAKENED -> .8F + .2F * Mth.sin(time * .35F);
			};
			renderPart(mc, "veins", pose, buffers, LightTexture.FULL_BRIGHT, overlay, pulse * Math.min(open * 2, 1));
		}

		pose.popPose();
	}

	private static float smooth(float t) {
		return t * t * (3 - 2 * t);
	}

	private static void renderPart(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light,
			int overlay, float tint) {
		var model = mc.getModelManager().getModel(part(name));
		mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
				buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)), null, model, tint, tint, tint,
				light, overlay);
	}

	@Override
	public AABB getRenderBoundingBox(SaintSarcophagusBlockEntity tomb) {
		// The tomb runs a block either way along its facing; the open lid leans out past its flank.
		return new AABB(tomb.getBlockPos()).inflate(1.5, 0, 1.5).expandTowards(0, 1, 0);
	}
}
