package com.huto.hemomancy.model.entity.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;

public class ModelLivingBladeHip extends PlayerModel<Player> {
	private final ModelPart whole;

	public ModelLivingBladeHip() {
		super(1.0f, false);
		textureWidth = 32;
		textureHeight = 32;

		whole = new ModelRenderer(this);
		whole.setRotationPoint(0.0029F, 16.4441F, 0.0147F);
		setRotationAngle(whole, -2.4435F, 0.0F, 0.0F);
		whole.setTextureOffset(16, 20).addBox(-1.0029F, 5.5559F, -1.0147F, 2.0F, 2.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(24, 5).addBox(-0.5029F, 2.9559F, -1.2647F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(16, 24).addBox(-0.5029F, 2.9559F, 0.2353F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(24, 0).addBox(-0.5029F, -1.0441F, -1.2647F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 24).addBox(-0.5029F, -1.0441F, 0.2353F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(6, 19).addBox(-0.5029F, 1.4559F, -1.2647F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 14).addBox(-0.5029F, 1.4559F, 0.2353F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(12, 12).addBox(-1.0029F, 4.9559F, -2.0147F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(22, 10).addBox(0.9971F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(22, 22).addBox(-2.0029F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(22, 20).addBox(-1.0029F, 4.9559F, 0.9853F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 19).addBox(-1.0029F, -1.4441F, -1.0147F, 2.0F, 6.0F, 2.0F, 0.0F, false);
		whole.setTextureOffset(8, 24).addBox(-1.2529F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(0.2471F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(-1.2529F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 6).addBox(0.2471F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 9).addBox(-1.2529F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(0, 3).addBox(0.2471F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		whole.setTextureOffset(15, 0).addBox(-1.5029F, 4.5559F, -1.5147F, 3.0F, 2.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-1.5029F, -2.4441F, -1.5147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-1.5029F, -2.5441F, -1.8147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-1.5029F, -2.5441F, -1.2147F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(0.7471F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(15, 6).addBox(-1.7529F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(12, 15).addBox(-2.0029F, -5.9441F, -2.0147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(16, 10).addBox(-0.5029F, -6.9441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(8, 20).addBox(-0.5029F, -7.9441F, -1.0147F, 1.0F, 1.0F, 3.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(-2.0029F, -4.4441F, -2.0147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(-2.0029F, -4.4441F, -1.7147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(-2.0029F, -4.4441F, -2.3147F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(-2.2029F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 12).addBox(1.2971F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		whole.setTextureOffset(0, 6).addBox(-2.5029F, -3.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, 0.0F, false);
		whole.setTextureOffset(0, 0).addBox(-2.5029F, -5.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, 0.0F, false);
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red,
			float green, float blue, float alpha) {
		whole.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}