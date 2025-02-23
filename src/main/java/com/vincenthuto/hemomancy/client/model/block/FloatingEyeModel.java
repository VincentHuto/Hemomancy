package com.vincenthuto.hemomancy.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.IAnimatedModel;
import com.vincenthuto.hemomancy.client.render.tile.EarthenVeinRenderer.EarthenVeinAnimContext;

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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.Level;

public class FloatingEyeModel extends Model implements IAnimatedModel<EarthenVeinAnimContext> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("floatingeyemodel"),
			"root");
	private final ModelPart root;

	public FloatingEyeModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root.getChild("root");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 19.0F, -2.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition vein = root.addOrReplaceChild("vein", CubeListBuilder.create().texOffs(0, 14).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -2.0F, 0.0F));

		PartDefinition vein2 = root.addOrReplaceChild("vein2", CubeListBuilder.create().texOffs(11, 0).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 2.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition vein3 = root.addOrReplaceChild("vein3", CubeListBuilder.create().texOffs(10, 10).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 0.0F, 2.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition vein4 = root.addOrReplaceChild("vein4", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 0.0F, -2.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition nerve = root.addOrReplaceChild("nerve", CubeListBuilder.create().texOffs(14, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition eye = root.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	
	@Override
	public ModelPart getRoot() {
		return this.root;
	}

	@Override
	public void setupAnimation(Level level, float partialTicks, EarthenVeinAnimContext ctx) {
		this.getRoot().getAllParts().forEach(ModelPart::resetPose);
        AnimationState state = ctx.state();
        float time = (float)level.getGameTime() + partialTicks;
//        AnimationHelper.animate(this, state, EARTHENVEINMODEL_WIGGLE, time, 1F);
//        float animTime = AnimationHelper.getElapsedSeconds(EARTHENVEINMODEL_WIGGLE,state.getAccumulatedTime());
	}
	
	 public void setupAnim(float pTime, float pRightPageFlipAmount, float pLeftPageFlipAmount, float pBookOpenAmount) {
	      float f = (Mth.sin(pTime * 0.02F) * 0.1F + 1.25F) * pBookOpenAmount;
	     this.root.zRot =-f;
	   }


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}