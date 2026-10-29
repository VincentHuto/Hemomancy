package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import static com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Draws the moving parts over the Saint Sarcophagus's baked tomb: the gisant lid sliding aside and tipping
 * over the rim, the offering blood rising in the bowl, the cresset coals once consecrated, and the
 * corpus's memory veins kindling with its state. Parts and anchors come from
 * tools/model_export/saint_sarcophagus.py via {@code SarcophagusGeometry}. The tomb is four cells long and a block
 * model stops at three, so each part comes as its foot section and a {@code _head} section drawn one cut further on;
 * the static tomb's head section is baked in by {@code SectionedBakedModel}. Every part is drawn in the palette of the
 * Saint at rest in the tomb ({@link SarcophagusPalettes}).
 */
public class SaintSarcophagusRenderer implements BlockEntityRenderer<SaintSarcophagusBlockEntity> {
	/** Every standalone model: the static head section and both sections of each moving part. */
	public static final String[] PART_MODELS = {"head", "lid", "lid_head", "blood", "blood_head", "coals", "coals_head",
			"veins", "veins_head"};

	public SaintSarcophagusRenderer(BlockEntityRendererProvider.Context context) {}

	public static ModelResourceLocation part(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/saint_sarcophagus_" + name));
	}

	@Override
	public void render(SaintSarcophagusBlockEntity tomb, float partialTicks, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		EnumSaintType saint = tomb.getSaintType();
		float time = tomb.getLevel() == null ? 0 : AnimationTime.gameTime(tomb.getLevel(), partialTicks);
		float open = tomb.getLidAngle(partialTicks);

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(180 - tomb.getBlockState().getValue(SaintSarcophagusBlock.FACING).toYRot()));
		pose.translate(-.5, 0, -.5);

		// The lid slides east off the tomb, then leans down over the rim edge.
		pose.pushPose();
		float slide = HemoEasing.smoothstepUnclamped(Math.min(open / .7F, 1)), tip = HemoEasing.smoothstepUnclamped(Math.max((open - .6F) / .4F, 0));
		pose.translate(LID_PIVOT_X / 16F, LID_PIVOT_Y / 16F, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(-LID_TIP * tip));
		pose.translate(-LID_PIVOT_X / 16F + LID_SLIDE * slide / 16F, -LID_PIVOT_Y / 16F, 0);
		renderPart(saint, "lid", pose, buffers, light, overlay, 1F);
		pose.popPose();

		float fill = tomb.isConsecrated() ? 1F
				: Math.min(tomb.getOfferingBloodVolume(), SaintSarcophagusBlockEntity.OFFERING_THRESHOLD)
						/ (float) SaintSarcophagusBlockEntity.OFFERING_THRESHOLD;
		if (fill > 0) {
			pose.pushPose();
			pose.translate(0, POOL_FLOOR / 16F, 0);
			pose.scale(1, fill, 1);
			pose.translate(0, -POOL_FLOOR / 16F, 0);
			renderPart(saint, "blood", pose, buffers, light, overlay, 1F);
			pose.popPose();
		}

		if (tomb.isConsecrated())
			renderPart(saint, "coals", pose, buffers, LightTexture.FULL_BRIGHT, overlay, .8F + .2F * Mth.sin(time * .37F));

		if (open > .05F) {
			float pulse = switch (tomb.getCorpusState()) {
				case DORMANT -> .35F;
				case RESPONSIVE -> .6F + .15F * Mth.sin(time * .08F);
				case AWAKENED -> .8F + .2F * Mth.sin(time * .35F);
			};
			renderPart(saint, "veins", pose, buffers, LightTexture.FULL_BRIGHT, overlay, pulse * Math.min(open * 2, 1));
		}

		pose.popPose();
	}

	/** A moving part's foot section, then its head section one cut further toward the head end. */
	private static void renderPart(@Nullable EnumSaintType saint, String name, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay, float tint) {
		renderModel(saint, name, pose, buffers, light, overlay, tint);
		pose.pushPose();
		pose.translate(0, 0, HEAD_CUT / 16F);
		renderModel(saint, name + "_head", pose, buffers, light, overlay, tint);
		pose.popPose();
	}

	private static void renderModel(@Nullable EnumSaintType saint, String name, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay, float tint) {
		BakedModel model = SarcophagusPalettes.part(name, saint);
		Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(),
				buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS)), null, model, tint, tint, tint,
				light, overlay);
	}

	@Override
	public AABB getRenderBoundingBox(SaintSarcophagusBlockEntity tomb) {
		// Four cells long from a block in front of this one, and the open lid leans out past its flank.
		return new AABB(tomb.getBlockPos()).inflate(3, 0, 3).expandTowards(0, 2, 0);
	}
}
