package com.huto.hemomancy.model.entity.mob;

import com.huto.hemomancy.entity.mob.EntityMorphlingPolyp;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelMorphlingPolyp extends EntityModel<EntityMorphlingPolyp> {
	private final ModelRenderer bone;
	private final ModelRenderer bone3;
	private final ModelRenderer bone4;
	private final ModelRenderer bone5;
	private final ModelRenderer bone6;
	private final ModelRenderer bone7;
	private final ModelRenderer bone8;
	private final ModelRenderer bone2;
	private final ModelRenderer bone9;
	private final ModelRenderer bone17;
	private final ModelRenderer bone10;
	private final ModelRenderer bone11;
	private final ModelRenderer bone12;
	private final ModelRenderer bone13;
	private final ModelRenderer bone16;
	private final ModelRenderer bone15;
	private final ModelRenderer eye;
	private final ModelRenderer eye2;
	private final ModelRenderer eye3;
	private final ModelRenderer eye7;
	private final ModelRenderer eye12;
	private final ModelRenderer eye8;
	private final ModelRenderer eye9;
	private final ModelRenderer eye13;
	private final ModelRenderer eye10;
	private final ModelRenderer eye11;
	private final ModelRenderer eye4;
	private final ModelRenderer eye5;
	private final ModelRenderer eye6;
	private final ModelRenderer bone14;

	public ModelMorphlingPolyp() {
		textureWidth = 128;
		textureHeight = 128;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0F, 25.0F, 0.0F);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(1.9F, -6.0F, -2.15F);
		bone.addChild(bone3);
		bone3.setTextureOffset(56, 70).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(2.9F, -8.0F, -3.4F);
		bone.addChild(bone4);
		bone4.setTextureOffset(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(1.15F, -8.0F, 4.4F);
		bone.addChild(bone5);
		bone5.setTextureOffset(32, 78).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F, false);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(1.4F, -5.0F, 5.35F);
		bone.addChild(bone6);
		bone6.setTextureOffset(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-4.1F, -6.0F, 4.6F);
		bone.addChild(bone7);
		bone7.setTextureOffset(32, 62).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(-3.6F, -7.55F, -1.25F);
		bone.addChild(bone8);
		bone8.setTextureOffset(26, 29).addBox(-3.5F, -4.25F, -2.5F, 5.0F, 4.0F, 6.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(2.4F, -3.3F, -4.75F);
		bone.addChild(bone2);
		bone2.setTextureOffset(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(-2.1F, -5.25F, -4.15F);
		bone.addChild(bone9);
		bone9.setTextureOffset(58, 54).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone17 = new ModelRenderer(this);
		bone17.setRotationPoint(-2.6F, -6.75F, 7.85F);
		bone.addChild(bone17);
		bone17.setTextureOffset(58, 54).addBox(-2.5F, -3.5F, -3.0F, 5.0F, 7.0F, 6.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(-6.1F, -4.55F, 2.55F);
		bone.addChild(bone10);
		bone10.setTextureOffset(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(3.65F, -5.0F, -2.35F);
		bone.addChild(bone11);
		bone11.setTextureOffset(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(3.4F, -4.0F, 3.35F);
		bone.addChild(bone12);
		bone12.setTextureOffset(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, 0.0F, false);

		bone13 = new ModelRenderer(this);
		bone13.setRotationPoint(-0.1F, -4.35F, 5.85F);
		bone.addChild(bone13);
		bone13.setTextureOffset(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		bone16 = new ModelRenderer(this);
		bone16.setRotationPoint(-3.85F, -3.3F, -2.4F);
		bone.addChild(bone16);
		bone16.setTextureOffset(0, 71).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);

		bone15 = new ModelRenderer(this);
		bone15.setRotationPoint(-4.9F, -5.5F, 6.6F);
		bone.addChild(bone15);
		bone15.setTextureOffset(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		eye = new ModelRenderer(this);
		eye.setRotationPoint(1.0F, -12.0F, -1.5F);
		bone.addChild(eye);
		eye.setTextureOffset(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye2 = new ModelRenderer(this);
		eye2.setRotationPoint(-7.5F, -10.0F, 4.5F);
		bone.addChild(eye2);
		eye2.setTextureOffset(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye3 = new ModelRenderer(this);
		eye3.setRotationPoint(-0.75F, -11.0F, 4.5F);
		bone.addChild(eye3);
		eye3.setTextureOffset(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye7 = new ModelRenderer(this);
		eye7.setRotationPoint(-4.75F, -5.0F, 10.5F);
		bone.addChild(eye7);
		eye7.setTextureOffset(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye12 = new ModelRenderer(this);
		eye12.setRotationPoint(5.25F, -8.25F, 9.5F);
		bone.addChild(eye12);
		eye12.setTextureOffset(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye8 = new ModelRenderer(this);
		eye8.setRotationPoint(-2.75F, -3.0F, -6.5F);
		bone.addChild(eye8);
		eye8.setTextureOffset(76, 16).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye9 = new ModelRenderer(this);
		eye9.setRotationPoint(8.0F, -3.0F, -6.5F);
		bone.addChild(eye9);
		eye9.setTextureOffset(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye13 = new ModelRenderer(this);
		eye13.setRotationPoint(8.0F, -4.0F, 5.5F);
		bone.addChild(eye13);
		eye13.setTextureOffset(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye10 = new ModelRenderer(this);
		eye10.setRotationPoint(-6.75F, -5.0F, -5.5F);
		bone.addChild(eye10);
		eye10.setTextureOffset(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye11 = new ModelRenderer(this);
		eye11.setRotationPoint(-9.75F, -3.0F, 8.5F);
		bone.addChild(eye11);
		eye11.setTextureOffset(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye4 = new ModelRenderer(this);
		eye4.setRotationPoint(7.25F, -10.0F, 4.5F);
		bone.addChild(eye4);
		eye4.setTextureOffset(80, 70).addBox(-3.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye5 = new ModelRenderer(this);
		eye5.setRotationPoint(7.25F, -10.0F, -4.5F);
		bone.addChild(eye5);
		eye5.setTextureOffset(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		eye6 = new ModelRenderer(this);
		eye6.setRotationPoint(-3.75F, -10.0F, -7.5F);
		bone.addChild(eye6);
		eye6.setTextureOffset(76, 22).addBox(-1.5F, -2.3F, 1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		bone14 = new ModelRenderer(this);
		bone14.setRotationPoint(-1.75F, -11.8F, 1.5F);
		bone.addChild(bone14);
		bone14.setTextureOffset(76, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(EntityMorphlingPolyp entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void setLivingAnimations(EntityMorphlingPolyp entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTick);
		float frame = entityIn.ticksExisted + partialTick;
		this.bone.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone2.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone3.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone4.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone5.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone6.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone7.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone8.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone9.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone10.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone11.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone12.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone13.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.bone14.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone15.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.bone16.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);

		this.eye.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye2.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye2.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye3.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye3.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye4.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye4.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye5.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye5.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye6.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye6.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye7.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye7.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye8.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye8.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye9.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye9.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye10.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye10.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye11.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye11.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye12.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye12.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);
		this.eye13.rotateAngleZ = (float) (Math.sin((frame)) * 0.0325);
		this.eye13.rotateAngleY = (float) (Math.cos((frame)) * 0.0325);

	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}