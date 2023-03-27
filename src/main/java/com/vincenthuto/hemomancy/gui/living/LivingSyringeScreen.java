package com.vincenthuto.hemomancy.gui.living;

import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.LivingSyringeMenu;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LivingSyringeScreen extends AbstractContainerScreen<LivingSyringeMenu> {
	private ResourceLocation GUI;

	public LivingSyringeScreen(LivingSyringeMenu container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name);

		switch (container.slotcount) {
		case 1:
			GUI = Hemomancy.rloc("textures/gui/living_syringe_gui.png");
			imageWidth = 176;
			imageHeight = 127;
			break;
		default:
			GUI = Hemomancy.rloc("textures/gui/living_syringe_gui.png");
			imageWidth = 176;
			imageHeight = 127;
			break;
		}
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

		return super.keyPressed(keyCode, scanCode, modifiers);
	}


	@Override
	public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.getModelViewMatrix().translate(new Vector3f(0, 50, 0));
		RenderSystem.setShaderTexture(0, GUI);
		HLGuiUtils.drawTexturedModalRect(getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, this.title.getString(), 7, 6, 0x404040);
	}
}