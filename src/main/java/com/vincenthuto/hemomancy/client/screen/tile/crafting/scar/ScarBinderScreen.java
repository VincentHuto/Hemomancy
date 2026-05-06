package com.vincenthuto.hemomancy.client.screen.tile.crafting.scar;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.ScarBinderInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScarBinderScreen extends AbstractContainerScreen<ScarBinderInventoryMenu> {
	public ScarBinderScreen(ScarBinderInventoryMenu container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name);

		switch (container.slotcount) {
		case 18:
			GUI = Hemomancy.rloc("textures/gui/scar_binder.png");
			imageWidth = 176;
			imageHeight = 150;
			break;
		case 27:
			GUI = Hemomancy.rloc("textures/gui/scar_binder_upgrade.png");
			imageWidth = 176;
			imageHeight = 168;
			break;
		default:
			GUI = Hemomancy.rloc("textures/gui/epic_gui.png");
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
		BufferBuilder buffer = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		buffer.addVertex((float) x, (float) y + height, z).setUv(tx, ty + th);
		buffer.addVertex((float) x + width, (float) y + height, z).setUv(tx + tw, ty + th);
		buffer.addVertex((float) x + width, (float) y, z).setUv(tx + tw, ty);
		buffer.addVertex((float) x, (float) y, z).setUv(tx, ty);

		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawCenteredString(font, this.title.getString(), 36, 6, 0xFFFFFFFF);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(graphics, p_render_1_, p_render_2_, p_render_3_);
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