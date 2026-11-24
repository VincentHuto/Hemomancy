package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

import java.util.Locale;

public final class HemojellyModel extends HierarchicalModel<HemojellyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("hemojelly"), "main");

	private final ModelPart root;
	private final ModelPart surfaceFloat;
	private final ModelPart sail;
	private final ModelPart feedingColony;
	private final ModelPart captureTendrils;
	private final ModelPart[][] polyps;
	private final ModelPart[][] tendrils;

	public HemojellyModel(ModelPart bakedRoot) {
		super(RenderType::entityTranslucent);
		root = bakedRoot.getChild("root");
		surfaceFloat = root.getChild("surface_float");
		sail = surfaceFloat.getChild("sail_crest");
		feedingColony = root.getChild("feeding_colony");
		captureTendrils = root.getChild("capture_tendrils");
		polyps = chains(feedingColony, "feeding_polyp", 3, 3, 3, 3, 3, 3);
		tendrils = chains(captureTendrils, "capture_tendril", 5, 6, 5, 5, 4, 5);
	}

	private static ModelPart[][] chains(ModelPart parent, String prefix, int... jointCounts) {
		ModelPart[][] chains = new ModelPart[jointCounts.length][];
		for (int i = 0; i < chains.length; i++) {
			String name = String.format(Locale.ROOT, "%s_%02d", prefix, i + 1);
			ModelPart[] chain = new ModelPart[jointCounts[i] + 1];
			chain[0] = parent.getChild(name);
			for (int j = 1; j < chain.length; j++) {
				chain[j] = chain[j - 1].getChild(String.format(Locale.ROOT, "%s_%02d", name, j));
			}
			chains[i] = chain;
		}
		return chains;
	}

	public static LayerDefinition createBodyLayer() {
		return HemojellyMesh.createBodyLayer();
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(HemojellyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float phase = ageInTicks * 0.11F + entity.getId();
		float lift = 0.35F * Mth.sin(phase);
		surfaceFloat.y -= lift;
		feedingColony.y -= lift;
		captureTendrils.y -= lift;
		sail.xRot = 0.08F * Mth.sin(phase - 0.6F);
		surfaceFloat.yScale = 1.0F + 0.035F * Mth.cos(phase);

		float capture = entity.captureTicks();
		float gather = Mth.clamp((30.0F - capture) / 8.0F, 0.0F, 1.0F)
				* Mth.clamp(capture / 5.0F, 0.0F, 1.0F);
		sway(polyps, phase + 0.8F, 0.09F, gather * 0.10F);
		sway(tendrils, phase, 0.055F, gather * 0.24F);
	}

	private static void sway(ModelPart[][] chains, float phase, float amplitude, float gather) {
		for (int i = 0; i < chains.length; i++) {
			ModelPart[] chain = chains[i];
			ModelPart base = chain[0];
			float towardX = -Mth.clamp(base.x / 4.0F, -1.0F, 1.0F);
			float towardZ = -Math.signum(base.z);
			for (int j = 1; j < chain.length; j++) {
				float wave = phase + i * 0.9F - (j - 1) * 0.62F;
				float curl = gather / j;
				chain[j].xRot += Mth.sin(wave) * amplitude + towardZ * curl;
				chain[j].zRot += Mth.cos(wave) * amplitude - towardX * curl;
			}
		}
	}
}
