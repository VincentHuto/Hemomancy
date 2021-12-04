package com.vincenthuto.hemomancy.gui.mindrunes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuRuneBinder;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenRuneBinder extends AbstractContainerScreen<MenuRuneBinder> {
	public ScreenRuneBinder(MenuRuneBinder container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name);

		switch (container.slotcount) {
		case 18:
			GUI = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_binder.png");
			imageWidth = 176;
			imageHeight = 150;
			break;
		case 27:
			GUI = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_binder_upgrade.png");
			imageWidth = 176;
			imageHeight = 168;
			break;
		default:
			GUI = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/epic_gui.png");
			imageWidth = 212;
			imageHeight = 276;
			break;
		}
	}

	private ResourceLocation GUI;

	@Override
	protected void init() {
		super.init();
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI);
		drawTexturedQuad(leftPos, topPos, imageWidth, imageHeight, 0, 0, 1, 1, 0);
	}

	private void drawTexturedQuad(int x, int y, int width, int height, float tx, float ty, float tw, float th,
			float z) {
		Tesselator tess = Tesselator.getInstance();
		BufferBuilder buffer = tess.getBuilder();

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex((double) x + 0, (double) y + height, z).uv(tx, ty + th).endVertex();
		buffer.vertex((double) x + width, (double) y + height, z).uv(tx + tw, ty + th).endVertex();
		buffer.vertex((double) x + width, (double) y + 0, z).uv(tx + tw, ty).endVertex();
		buffer.vertex((double) x + 0, (double) y + 0, z).uv(tx, ty).endVertex();

		tess.end();
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, this.title.getString(), 7, 6, 0x404040);
	}

	@Override
	public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}