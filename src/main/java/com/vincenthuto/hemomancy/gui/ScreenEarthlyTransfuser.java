package com.vincenthuto.hemomancy.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuEarthlyTransfuser;
import com.vincenthuto.hemomancy.tile.BlockEntityEarthlyTransfuser;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.GuiUtils;

public class ScreenEarthlyTransfuser extends AbstractContainerScreen<MenuEarthlyTransfuser> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/earthly_transfuser_gui.png");
	private final ResourceLocation texture;
	final BlockEntityEarthlyTransfuser te;

	public ScreenEarthlyTransfuser(MenuEarthlyTransfuser screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.texture = TEXTURE;
		this.te = screenContainer.getTe();

	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

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

	public void renderVolumeBar(PoseStack matrix, int screenWidth, int screenHeight, Level world) {

		matrix.pushPose();
		float bloodVolume = te.clientBloodLevel * 5;
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;
		ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_fill_tiled.png");
		matrix.popPose();

		float textureUShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float textureVShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);

		// Fill
		matrix.pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, fill_texture);
		matrix.mulPose(new Quaternion(Vector3f.ZP, 45, true));
		drawFlippedTexturedModalRect(-12 + screenWidth + 5, 60 + screenHeight + 32, 23 + textureUShift, textureVShift,
				6, heightShift - newBarWidth);
		matrix.popPose();

	}

	public void renderVolumeFrame(PoseStack matrix, int screenWidth, int screenHeight, Level world) {
		matrix.translate(0, 0, 100);
		matrix.pushPose();
		float bloodVolume = te.clientBloodLevel * 5;
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		ResourceLocation frame = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_bar.png");
		matrix.popPose();

		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);

		// Frame
		matrix.pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame); // Cap
		GuiUtils.drawTexturedModalRect(matrix, 1 + screenWidth + 5, 51 + screenHeight + 32, 9, 244, 13, 12,
				heightShift);
		GuiUtils.drawTexturedModalRect(matrix, 1 + screenWidth + 5, 1 + screenHeight + 32, 1, 0, 12, 51, heightShift);
		matrix.popPose();

	}

	@Override
	public void render(PoseStack p_97858_, int p_97859_, int p_97860_, float p_97861_) {
		this.renderBackground(p_97858_);
		this.renderBg(p_97858_, p_97861_, p_97859_, p_97860_);
		super.render(p_97858_, p_97859_, p_97860_, p_97861_);
		int guiWidth = 176;
		int guiHeight = 166;
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		p_97858_.pushPose();
		p_97858_.translate(0, -30, 0);
		renderVolumeBar(p_97858_, centerX, centerY, te.getLevel());
		renderVolumeFrame(p_97858_, centerX, centerY, te.getLevel());
		p_97858_.popPose();
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
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, String.valueOf(te.clientBloodLevel), 130, 4, 0000);

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
