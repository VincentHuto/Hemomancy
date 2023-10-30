package com.vincenthuto.hemomancy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.JuiceinatorMenu;
import com.vincenthuto.hemomancy.common.tile.JuicinatorBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.ScreenUtils;

public class JuiceinatorScreen extends AbstractContainerScreen<JuiceinatorMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/juiceinator_gui.png");

	public static void drawFlippedTexturedModalRect(float x, float y, float textureX, float textureY, float width,
			float height) {

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		x = x + 10;
		y = y - 33;
		// soon to be top right
		bufferbuilder.vertex(x + 13f, y + height - 13.25, 1)
				.uv((textureX + 0) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
		// soon to be top left
		bufferbuilder.vertex(x + width, y + height - 13.25, 1)
				.uv((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();

		// soon to be bottom left
		bufferbuilder.vertex(x + width, y - 3, 1).uv((textureX + width) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();
		// now bottom right
		bufferbuilder.vertex(x + 13f, y - 3, 1).uv((textureX + 0) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();

		tessellator.end();
	}

	private final ResourceLocation texture;

	final JuicinatorBlockEntity te;

	public JuiceinatorScreen(JuiceinatorMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.texture = TEXTURE;
		this.te = screenContainer.getTe();

	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void removed() {
		super.removed();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int p_97859_, int p_97860_, float p_97861_) {
		this.renderBackground(guiGraphics);
		this.renderBg(guiGraphics, p_97861_, p_97859_, p_97860_);
		super.render(guiGraphics, p_97859_, p_97860_, p_97861_);
		int guiWidth = 176;
		int guiHeight = 166;
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, -30, 0);
		renderVolumeBar(guiGraphics, centerX, centerY, te.getLevel());
		renderVolumeFrame(guiGraphics, centerX, centerY, te.getLevel());
		guiGraphics.pose().popPose();
		this.renderTooltip(guiGraphics, p_97859_, p_97860_);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float p_97854_, int p_97855_, int p_97856_) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = this.leftPos;
		int j = this.topPos;
		graphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight);
		if (this.menu.isLit()) {
			int k = this.menu.getLitProgress();
			graphics.blit(this.texture, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		int l = this.menu.getBurnProgress();
		graphics.blit(this.texture, i + 79, j + 34, 176, 14, l + 1, 16);
	}

	
	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.drawString(font,String.valueOf(te.getBloodVolume()), 130, 4, 0000);

	}

	public void renderVolumeBar(GuiGraphics guiGraphics, int screenWidth, int screenHeight, Level world) {

		guiGraphics.pose().pushPose();
		double bloodVolume = te.getBloodVolume() * 2.5;
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;
		ResourceLocation fill_texture = Hemomancy.rloc("textures/gui/blood_fill_tiled.png");
		guiGraphics.pose().popPose();

		float textureUShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float textureVShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);

		// Fill
		guiGraphics.pose().pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, fill_texture);
		guiGraphics.pose().mulPose(new Quaternion(Vector3.ZP, 45, true).toMoj());
		drawFlippedTexturedModalRect(-12 + screenWidth + 5, 60 + screenHeight + 32, 23 + textureUShift, textureVShift,
				6, heightShift - newBarWidth);
		guiGraphics.pose().popPose();

	}

	public void renderVolumeFrame(GuiGraphics guiGraphics, int screenWidth, int screenHeight, Level world) {
		guiGraphics.pose().translate(0, 0, 100);
		guiGraphics.pose().pushPose();
		double bloodVolume = te.getBloodVolume() * 5;
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		ResourceLocation frame = Hemomancy.rloc("textures/gui/blood_bar.png");
		guiGraphics.pose().popPose();

		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);

		// Frame
		guiGraphics.pose().pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame); // Cap
		ScreenUtils.drawTexturedModalRect(guiGraphics, 1 + screenWidth + 5, 51 + screenHeight + 32, 9, 244, 13, 12,
				heightShift);
		ScreenUtils.drawTexturedModalRect(guiGraphics, 1 + screenWidth + 5, 1 + screenHeight + 32, 1, 0, 12, 51,
				heightShift);
		guiGraphics.pose().popPose();

	}

	@Override
	protected void slotClicked(Slot p_97848_, int p_97849_, int p_97850_, ClickType p_97851_) {
		super.slotClicked(p_97848_, p_97849_, p_97850_, p_97851_);
	}

}
