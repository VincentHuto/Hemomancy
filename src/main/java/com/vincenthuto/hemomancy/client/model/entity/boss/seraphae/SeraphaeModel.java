package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Model for Saint Seraphae — The Bound Radiance.
 * <p>
 * A sacred, fractured humanoid silhouette: tall, slender, with wing-like
 * protrusions and a halo crown. The body conveys an entity that cannot hold
 * itself together — seams of light leak from every joint.
 * <p>
 * UV layout is 64×64.
 */
public class SeraphaeModel<T extends SeraphaeEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("seraphae"), "main");

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Halo-crowned head — slightly wider, with a thin ring on top
		partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create()
						// Main head
						.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
						// Halo ring
						.texOffs(32, 0).addBox(-5.0F, -10.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		// Hat layer — required by HumanoidModel, left empty
		partdefinition.addOrReplaceChild("hat",
				CubeListBuilder.create(),
				PartPose.ZERO);

		// Slender torso with fractured seams — narrower than standard
		partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create()
						// Core torso
						.texOffs(16, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
						// Wing-like protrusions from upper back (sacred radiance shards)
						.texOffs(0, 48).addBox(-8.0F, 1.0F, 1.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(12, 48).addBox(3.0F, 1.0F, 1.5F, 5.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		// Graceful arms — slightly thinner, posed outward as if radiating
		partdefinition.addOrReplaceChild("right_arm",
				CubeListBuilder.create()
						.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, -0.15F, 0.0F, 0.2F));
		partdefinition.addOrReplaceChild("left_arm",
				CubeListBuilder.create()
						.texOffs(40, 16).mirror().addBox(0.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, -0.15F, 0.0F, -0.2F));

		// Elongated legs — robed appearance
		partdefinition.addOrReplaceChild("right_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public SeraphaeModel(ModelPart root) {
		super(root);
	}
}
