package com.vincenthuto.hemomancy.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelEarthenVein extends Model  {
	public static final ModelLayerLocation earth_vein = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelearthenvein"), "main");
	
	private final ModelPart base;

	public ModelEarthenVein(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.base = root.getChild("base");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.5F, -0.11F, -3.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 24.1F, -0.5F));

		PartDefinition seg1 = base.addOrReplaceChild("seg1",
				CubeListBuilder.create().texOffs(15, 17)
						.addBox(-2.5F, -1.7F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(30, 9)
						.addBox(-1.5F, -2.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, -0.1F, 0.5F));

		PartDefinition frill16 = seg1.addOrReplaceChild("frill16",
				CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, -0.5005F, -0.0218F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.2008F, 2.5349F, 0.1745F, 0.0F, 0.0F));

		PartDefinition frill15 = seg1.addOrReplaceChild("frill15",
				CubeListBuilder.create().texOffs(12, 26).addBox(0.0218F, -0.5005F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5349F, -1.2008F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition frill14 = seg1.addOrReplaceChild("frill14",
				CubeListBuilder.create().texOffs(30, 18).addBox(-2.5F, -0.5005F, 0.0218F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.2008F, -2.5349F, -0.1745F, 0.0F, 0.0F));

		PartDefinition frill13 = seg1.addOrReplaceChild("frill13",
				CubeListBuilder.create().texOffs(22, 26).addBox(-0.0218F, -0.5005F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5349F, -1.2008F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition seg2 = seg1.addOrReplaceChild("seg2",
				CubeListBuilder.create().texOffs(15, 10)
						.addBox(-2.5F, -1.7F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(28, 24)
						.addBox(-1.5F, -2.7F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition frill6 = seg2.addOrReplaceChild("frill6",
				CubeListBuilder.create().texOffs(12, 28).addBox(-0.0218F, -0.5005F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5349F, -1.2008F, 0.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition frill7 = seg2.addOrReplaceChild("frill7",
				CubeListBuilder.create().texOffs(32, 31).addBox(-2.5F, -0.5005F, 0.0218F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.2008F, -2.5349F, -0.0436F, 0.0F, 0.0F));

		PartDefinition frill5 = seg2.addOrReplaceChild("frill5",
				CubeListBuilder.create().texOffs(22, 28).addBox(0.0218F, -0.5005F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5349F, -1.2008F, 0.0F, 0.0F, 0.0F, 0.0436F));

		PartDefinition frill8 = seg2.addOrReplaceChild("frill8",
				CubeListBuilder.create().texOffs(30, 20).addBox(-2.5F, -0.5005F, -0.0218F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.2008F, 2.5349F, 0.0436F, 0.0F, 0.0F));

		PartDefinition seg3 = seg2.addOrReplaceChild("seg3",
				CubeListBuilder.create().texOffs(0, 15)
						.addBox(-2.5F, -1.7F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(16, 24)
						.addBox(-1.5F, -2.7F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition seg4 = seg3.addOrReplaceChild("seg4",
				CubeListBuilder.create().texOffs(0, 8)
						.addBox(-2.5F, -1.7F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 29)
						.addBox(-1.5F, -2.7F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition frill = seg4.addOrReplaceChild("frill",
				CubeListBuilder.create().texOffs(10, 30).addBox(0.0F, -0.2F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5F, -0.75F, 0.0F, 0.0F, 0.0F, 0.0436F));

		PartDefinition frill2 = seg4.addOrReplaceChild("frill2",
				CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -0.2F, -2.5F, 0.0F, 2.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, -0.75F, 0.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition frill3 = seg4.addOrReplaceChild("frill3",
				CubeListBuilder.create().texOffs(34, 0).addBox(-2.5F, -0.2F, 0.0F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.75F, -2.5F, -0.0436F, 0.0F, 0.0F));

		PartDefinition frill4 = seg4.addOrReplaceChild("frill4",
				CubeListBuilder.create().texOffs(32, 33).addBox(-2.5F, -0.2F, 0.0F, 5.0F, 2.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.75F, 2.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition mouth = seg4.addOrReplaceChild("mouth",
				CubeListBuilder.create().texOffs(0, 22)
						.addBox(-2.0F, -1.75F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(19, 3)
						.addBox(-2.5F, -2.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition frill12 = mouth
				.addOrReplaceChild("frill12",
						CubeListBuilder.create().texOffs(24, 2).addBox(-2.5F, -2.1981F, 0.0872F, 5.0F, 1.0F, 0.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.25F, 2.5F, 0.0436F, 0.0F, 0.0F));

		PartDefinition frill11 = mouth.addOrReplaceChild("frill11",
				CubeListBuilder.create().texOffs(34, 2).addBox(-2.5F, -0.0005F, 0.0218F, 5.0F, 1.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.9503F, -2.5131F, -0.0436F, 0.0F, 0.0F));

		PartDefinition frill10 = mouth.addOrReplaceChild("frill10",
				CubeListBuilder.create().texOffs(15, 4).addBox(-0.0218F, -0.0005F, -2.5F, 0.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5131F, -1.9503F, 0.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition frill9 = mouth.addOrReplaceChild("frill9",
				CubeListBuilder.create().texOffs(30, 12).addBox(0.0218F, -0.0005F, -2.5F, 0.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5131F, -1.9503F, 0.0F, 0.0F, 0.0F, 0.0436F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	
	}

	
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		base.render(poseStack, buffer, packedLight, packedOverlay);
	}
}