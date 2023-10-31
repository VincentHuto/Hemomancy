package com.vincenthuto.hemomancy.client.screen.rune;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.RuneBinderInventoryMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RuneBinderScreen extends AbstractContainerScreen<RuneBinderInventoryMenu> {
	public RuneBinderScreen(RuneBinderInventoryMenu container, Inventory playerInventory, Component name) {
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
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
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
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawCenteredString(font, this.title.getString(), 36, 6, 0xFFFFFFFF);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(graphics);
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}