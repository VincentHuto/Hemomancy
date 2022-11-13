package com.vincenthuto.hemomancy.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.tile.VialCentrifugeBlockEntity;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.ScreenUtils;

public class VialCentrifugeScreen extends AbstractContainerScreen<VialCentrifugeMenu> {
	static final ResourceLocation GUI_CENTRIFUGE = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/centrifuge_gui.png");
	static final ResourceLocation texture = Hemomancy.rloc("textures/gui/blood_bar.png");
	static final ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/blood_fill_tiled.png");
	public static void drawFlippedTexturedModalRect(float x, float y, float textureX, float textureY, float width,
			float height) {

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		x = x + 10;
		// soon to be top right
		bufferbuilder.vertex(x + 13f, y + height - 13.25, 1)
				.uv((textureX + 0) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
		// soon to be top left
		bufferbuilder.vertex(x + width, y + height - 13.25, 1)
				.uv((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();

		// soon to be bottom left
		bufferbuilder.vertex(x + width, y - 6, 1).uv((textureX + width) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();
		// now bottom right
		bufferbuilder.vertex(x + 13f, y - 6, 1).uv((textureX + 0) * 0.00390625F, (textureY + 0) * 0.00390625F)
				.endVertex();

		tessellator.end();
	}
	final Inventory playerInv;
	final VialCentrifugeBlockEntity te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	GuiButtonTextured forgetButton;

	int FORGETBUTTONID = 1;

	public VialCentrifugeScreen(VialCentrifugeMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	@Override
	protected void init() {
		super.init();
		renderables.clear();
		this.addRenderableWidget(forgetButton = new GuiButtonTextured(GUI_CENTRIFUGE, FORGETBUTTONID, leftPos + 90-28,
				topPos + 44, 16, 16, 176, 0, null, (press) -> {
					if (press instanceof GuiButtonTextured) {
						PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new StartCentrifugeButtonPacket());
					}
				}));

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		forgetButton.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		renderVolumeBar(matrixStack, centerX, centerY, te.getLevel());
		this.renderTooltip(matrixStack, mouseX, mouseY);
		if (forgetButton.isHoveredOrFocused()) {
			List<Component> ClosePage = new ArrayList<>();
			ClosePage.add(Component.literal(I18n.get("Start Centrifuge")));
			if (forgetButton.isHoveredOrFocused()) {
				renderComponentTooltip(matrixStack, ClosePage, mouseX, mouseY);
			}
		}
	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		this.renderBackground(matrixStack);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_CENTRIFUGE);
		HLGuiUtils.drawTexturedModalRect(this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		int l = this.menu.getSpinProgress();
		if (l < 24)
			this.blit(matrixStack, this.leftPos + 118, this.topPos + 80, 177, 33, l, 16);
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		//this.font.draw(matrixStack, "Vial Centrifuge", 8, 4, 0);
		this.font.draw(matrixStack, String.valueOf(te.getBloodVolume()), 26, 85, 0000);
		this.font.draw(matrixStack, "Inventory", 8, this.imageHeight - 90, 000000);

	}

	public void renderVolumeBar(PoseStack matrix, int screenWidth, int screenHeight, Level world) {

		matrix.pushPose();
		double bloodVolume = te.getBloodVolume();
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 61.25) - 8;
		ResourceLocation frame = Hemomancy.rloc("textures/gui/blood_bar.png");
		ResourceLocation fill_texture = Hemomancy.rloc("textures/gui/blood_fill_tiled.png");
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
		drawFlippedTexturedModalRect(-12 + screenWidth + 6, 60 + screenHeight + 14, 23 + textureUShift, textureVShift,
				6, heightShift - newBarWidth);
		matrix.popPose();

		// Frame
		matrix.translate(0, 0, 100);

		matrix.pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame); // Cap
		ScreenUtils.drawTexturedModalRect(matrix, 1 + screenWidth + 7, 51 + screenHeight + 16, 9, 244, 13, 12,
				heightShift);
		ScreenUtils.drawTexturedModalRect(matrix, 1 + screenWidth + 7, 1 + screenHeight + 25, 1, 0, 12, 42, heightShift);
		matrix.popPose();
	}

}
