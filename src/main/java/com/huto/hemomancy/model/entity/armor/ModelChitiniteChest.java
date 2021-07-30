package com.huto.hemomancy.model.entity.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class ModelChitiniteChest extends HumanoidModel<LivingEntity> {
	private final ModelPart Body;

	public ModelChitiniteChest() {
		super(0.8f, 0, 64, 128);

		Body = new ModelPart(this);
		Body.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);
		Body.setTextureOffset(0, 81).addBox(-5.0F, 0.0F, 2.0F, 10.0F, 11.0F, 2.0F, 0.25F, false);
		Body.setTextureOffset(18, 75).addBox(-3.5F, 0.5F, 4.25F, 7.0F, 4.0F, 1.0F, 0.25F, false);
		Body.setTextureOffset(1, 73).addBox(-4.0F, 7.5F, 4.25F, 8.0F, 4.0F, 1.0F, 0.25F, false);
		Body.setTextureOffset(21, 109).addBox(-4.0F, 1.5F, 4.25F, 8.0F, 9.0F, 2.0F, 0.25F, false);
		Body.setTextureOffset(0, 116).addBox(-4.5F, 1.5F, 4.25F, 9.0F, 9.0F, 1.0F, 0.25F, false);
		Body.setTextureOffset(0, 96).addBox(-3.0F, 2.0F, 6.25F, 6.0F, 8.0F, 1.0F, 0.25F, false);
		Body.setTextureOffset(3, 85).addBox(-4.0F, 9.5F, 2.25F, 8.0F, 3.0F, 2.0F, 0.25F, false);
		this.bipedBody.addChild(Body);

	}

}