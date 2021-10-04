package com.vincenthuto.hemomancy.gui.recaller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.container.ContainerVisceralRecaller;
import com.vincenthuto.hemomancy.network.PacketClearRecallerState;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmlclient.gui.GuiUtils;

public class GuiVisceralRecaller extends AbstractContainerScreen<ContainerVisceralRecaller> {
	static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_gui.png");
	static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_bar.png");
	static final ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/blood_fill_tiled.png");
	final Inventory playerInv;
	final BlockEntityVisceralRecaller te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	private int zLevel = 10;
	private Minecraft mc = Minecraft.getInstance();
	GuiButtonTextured forgetButton;
	int FORGETBUTTONID = 1;

	public GuiVisceralRecaller(ContainerVisceralRecaller screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

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

	public void renderVolumeBar(PoseStack matrix, int screenWidth, int screenHeight, Level world) {

		matrix.pushPose();
		float bloodVolume = te.getBloodVolume();
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;
		ResourceLocation frame = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_bar.png");
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
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		forgetButton.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		renderVolumeBar(matrixStack, centerX, centerY, te.getLevel());

		drawCenter(matrixStack, centerX, centerY);
		this.renderTooltip(matrixStack, mouseX, mouseY);
		if (forgetButton.isHovered()) {
			List<Component> ClosePage = new ArrayList<Component>();
			ClosePage.add(new TextComponent(I18n.get("Forget Current State")));
			if (forgetButton.isHovered()) {
				renderComponentTooltip(matrixStack, ClosePage, mouseX, mouseY);
			}
		}

	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, "Visceral Recaller", 8, 4, 0);
		this.font.draw(matrixStack, String.valueOf(te.getBloodVolume()), 130, 4, 0000);
		this.font.draw(matrixStack, "Inventory", 8, this.imageHeight - 90, 000000);

	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		this.renderBackground(matrixStack);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_RECALLER);
		HLGuiUtils.drawTexturedModalRect(this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void init() {
		super.init();
		renderables.clear();
		this.addRenderableWidget(forgetButton = new GuiButtonTextured(GUI_RECALLER, FORGETBUTTONID, leftPos + 152,
				topPos + 47, 16, 16, 176, 0, null, (press) -> {
					if (press instanceof GuiButtonTextured) {
						PacketHandler.CHANNELMAIN.sendToServer(new PacketClearRecallerState());
					}
				}));

	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void drawCenter(PoseStack stack, int xOff, int yOff) {
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
			double depthDist = ((iconDiameter - diameter) * affs.get(tend) + diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff + 90;
			int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff + 47;
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			fracLine(stack, lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset, this.zLevel,
					tend.getColor(), displace, 1.1);
			fracLine(stack, lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset, this.zLevel,
					tend.getColor(), displace, 1.1);
			fracLine(stack, cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			fracLine(stack, cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor(), displace, 0.8);
			int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
			mc.getItemRenderer().renderGuiItem(new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX + xOff + 90,
					newY + yOff + 47);
			rotAngle += 45;
		}
	}

	private static void drawLine(PoseStack stack, int x1, int y1, int x2, int y2, ParticleColor color, int displace) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._disableTexture();
		GlStateManager._depthMask(false);
		GlStateManager._disableCull();
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		Tesselator var4 = RenderSystem.renderThreadTesselator();
		BufferBuilder var5 = var4.getBuilder();
		RenderSystem.lineWidth(1.0F);
		var5.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		Vector3d vector3f = new Vector3d(x2 - x1, y2 - y1, 0);
		Vector3d vector3f2 = new Vector3d(x1 - x2, y1 - y2, 0);
		int red = (int) color.getRed();
		int green = (int) color.getGreen();
		int blue = (int) color.getBlue();
		var5.vertex(x1, y1, 0.0D).color(red, green, blue, 255).normal((float) vector3f.x, (float) vector3f.y, 0.0F)
				.endVertex();
		var5.vertex(x2, y2, 0.0D).color(red, green, blue, 255).normal((float) vector3f2.x, (float) vector3f2.y, 0.0F)
				.endVertex();
		var4.end();
		GlStateManager._enableCull();
		GlStateManager._depthMask(true);
		GlStateManager._enableTexture();
	}

	public static void fracLine(PoseStack matrix, int src_x, int src_y, int dst_x, int dst_y, int zLevel,
			ParticleColor color, int displace, double detail) {
		if (displace < detail) {
			drawLine(matrix, src_x, src_y, dst_x, dst_y, color, displace);
		} else {
			Random rand = new Random();
			int mid_x = (dst_x + src_x) / 2;
			int mid_y = (dst_y + src_y) / 2;
			mid_x = (int) (mid_x + (rand.nextFloat() - 0.25) * displace * 0.25);
			mid_y = (int) (mid_y + (rand.nextFloat() - 0.25) * displace * 0.25);
			fracLine(matrix, src_x, src_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);
			fracLine(matrix, dst_x, dst_y, mid_x, mid_y, zLevel, color, (displace / 2), detail);

		}
	}
}
