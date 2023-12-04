package com.vincenthuto.hemomancy.client.screen.living;

import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MorphlingJarScreen extends AbstractContainerScreen<MorphlingJarMenu> {
	private ResourceLocation GUI;

	public MorphlingJarScreen(MorphlingJarMenu container, Inventory playerInventory, Component name) {
		super(container, playerInventory, name);

		switch (container.slotcount) {
		case 4:
			GUI = Hemomancy.rloc("textures/gui/morphling_jar_gui.png");
			imageWidth = 176;
			imageHeight = 228;
			break;
		case 8:
			GUI = Hemomancy.rloc("textures/gui/morphling_jar_large.png");
			imageWidth = 176;
			imageHeight = 168;
			break;
		case 12:
			GUI = Hemomancy.rloc("textures/gui/morphling_jar_max.png");
			imageWidth = 176;
			imageHeight = 184;
			break;
		default:
			GUI = Hemomancy.rloc("textures/gui/morphling_jar.png");
			imageWidth = 212;
			imageHeight = 276;
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
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(graphics);
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.getModelViewMatrix().translate(new Vector3f(0, 78, 0));
		RenderSystem.setShaderTexture(0, GUI);
		HLGuiUtils.drawTexturedModalRect(getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawString(font,this.title.getString(), 7, 6, 0x404040);
	}
}