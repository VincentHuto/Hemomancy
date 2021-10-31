package com.vincenthuto.hemomancy.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuJuiceinator;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class JuiceinatorScreen extends AbstractContainerScreen<MenuJuiceinator> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/juiceinator_gui.png");
	private final ResourceLocation texture;

	public JuiceinatorScreen(MenuJuiceinator screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.texture = TEXTURE;

	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void render(PoseStack p_97858_, int p_97859_, int p_97860_, float p_97861_) {
		this.renderBackground(p_97858_);
		this.renderBg(p_97858_, p_97861_, p_97859_, p_97860_);
		super.render(p_97858_, p_97859_, p_97860_, p_97861_);
		this.renderTooltip(p_97858_, p_97859_, p_97860_);
	}

	@Override
	protected void renderBg(PoseStack p_97853_, float p_97854_, int p_97855_, int p_97856_) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, this.texture);
		int i = this.leftPos;
		int j = this.topPos;
		this.blit(p_97853_, i, j, 0, 0, this.imageWidth, this.imageHeight);
		if (this.menu.isLit()) {
			int k = this.menu.getLitProgress();
			this.blit(p_97853_, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		int l = this.menu.getBurnProgress();
		this.blit(p_97853_, i + 79, j + 34, 176, 14, l + 1, 16);
	}

	@Override
	protected void slotClicked(Slot p_97848_, int p_97849_, int p_97850_, ClickType p_97851_) {
		super.slotClicked(p_97848_, p_97849_, p_97850_, p_97851_);
	}

	@Override
	public void removed() {
		super.removed();
	}

}
