package com.huto.hemomancy.model.entity.armor;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelChitiniteHelmet extends BipedModel<LivingEntity> {
	private final ModelRenderer Head;

	public ModelChitiniteHelmet() {
		super(0.8f, 0, 64, 128);

		Head = new ModelRenderer(this);
		Head.setRotationPoint(0.0F, 0.0F, 0.0F);
		Head.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 1.0F, false);
		Head.setTextureOffset(39, 101).addBox(5.0F, -7.0F, -2.0F, 0.0F, 4.0F, 4.0F, 1.0F, false);
		Head.setTextureOffset(26, 102).addBox(-5.0F, -7.0F, -2.0F, 0.0F, 4.0F, 4.0F, 1.0F, false);
		Head.setTextureOffset(2, 93).addBox(-2.5F, -9.0F, -3.75F, 5.0F, 0.0F, 7.0F, 1.0F, false);
		Head.setTextureOffset(45, 105).addBox(-2.5F, -6.5F, 4.75F, 5.0F, 3.0F, 0.0F, 1.0F, false);
		this.bipedHead.addChild(Head);

	}

}