package com.vincenthuto.hemomancy.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.menu.VisceralRecallerMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.ClearRecallerStatePacket;
import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.ScreenUtils;

public class VisceralRecallerScreen extends AbstractContainerScreen<VisceralRecallerMenu> {
	static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_gui.png");
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
	final VisceralRecallerBlockEntity te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	private int zLevel = 10;
	HLButtonTextured forgetButton;

	int FORGETBUTTONID = 1;

	public VisceralRecallerScreen(VisceralRecallerMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	private void drawCenter(GuiGraphics stack, int xOff, int yOff) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int iconDiameter = 65;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff + 90;
			int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff + 90;
			int cy1 = (int) (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff + 47;
			int cy2 = (int) (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff + 47;
			double depthDist = ((iconDiameter - diameter) * affs.get(tend) * 0.5 + diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff + 90;
			int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff + 47;
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			HLGuiUtils.fracLine(stack.pose(), lx + centerOffset, ly + centerOffset, cx1 + centerOffset,
					cy1 + centerOffset, this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(stack.pose(), lx + centerOffset, ly + centerOffset, cx2 + centerOffset,
					cy2 + centerOffset, this.zLevel, tend.getColor(), displace, 1.1);
			HLGuiUtils.fracLine(stack.pose(), cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset,
					this.zLevel, tend.getColor(), displace, 0.8);
			HLGuiUtils.fracLine(stack.pose(), cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset,
					ly + centerOffset, this.zLevel, tend.getColor(), displace, 0.8);
			int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			HLGuiUtils.renderItemStackInGui(stack, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)),
					newX + xOff + 90, newY + yOff + 47);
			rotAngle += 45;
		}
	}

	@Override
	protected void init() {
		super.init();
		renderables.clear();
		this.addRenderableWidget(forgetButton = new HLButtonTextured(GUI_RECALLER, FORGETBUTTONID, leftPos + 152,
				topPos + 47, 16, 16, 176, 0, null, (press) -> {
					if (press instanceof HLButtonTextured) {
						PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new ClearRecallerStatePacket());
					}
				}));

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		forgetButton.render(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		renderVolumeBar(graphics, centerX, centerY, te.getLevel());

		drawCenter(graphics, centerX, centerY);
		this.renderTooltip(graphics, mouseX, mouseY);
		if (forgetButton.isHoveredOrFocused()) {
			List<Component> ClosePage = new ArrayList<>();
			ClosePage.add(Component.literal(I18n.get("Forget Current State")));
			if (forgetButton.isHoveredOrFocused()) {
				graphics.renderComponentTooltip(font, ClosePage, mouseX, mouseY);
			}
		}

	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		super.renderBackground(graphics);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		this.renderBackground(graphics);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_RECALLER);
		graphics.blit(GUI_RECALLER, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawString(font, "Visceral Recaller", 8, 4, 0);
		graphics.drawString(font, String.valueOf(te.getBloodVolume()), 130, 4, 0000);
		graphics.drawString(font, "Inventory", 8, this.imageHeight - 90, 000000);

	}

	public void renderVolumeBar(GuiGraphics graphics, int screenWidth, int screenHeight, Level world) {

		graphics.pose().pushPose();
		double bloodVolume = te.getBloodVolume();
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;
		ResourceLocation frame = Hemomancy.rloc("textures/gui/blood_bar.png");
		ResourceLocation fill_texture = Hemomancy.rloc("textures/gui/blood_fill_tiled.png");
		graphics.pose().popPose();

		float textureUShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float textureVShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);
		// Fill
		graphics.pose().pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, fill_texture);
		graphics.pose().mulPose(new Quaternion(Vector3.ZP, 45, true).toMoj());
		drawFlippedTexturedModalRect(-12 + screenWidth + 5, 60 + screenHeight + 32, 23 + textureUShift, textureVShift,
				6, heightShift - newBarWidth);
		graphics.pose().popPose();

		// Frame
		graphics.pose().translate(0, 0, 100);

		graphics.pose().pushPose();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame); // Cap
		ScreenUtils.drawTexturedModalRect(graphics, 1 + screenWidth + 5, 51 + screenHeight + 32, 9, 244, 13, 12,
				heightShift);
		ScreenUtils.drawTexturedModalRect(graphics, 1 + screenWidth + 5, 1 + screenHeight + 32, 1, 0, 12, 51,
				heightShift);
		graphics.pose().popPose();
	}

}
