package com.huto.hemomancy.gui.recaller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.container.ContainerVisceralRecaller;
import com.huto.hemomancy.network.PacketClearRecallerState;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.hutoslib.client.gui.GuiButtonTextured;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import GuiButtonTextured;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.entity.player.getInventory();
import net.minecraft.world.item.ItemStack;
import com.hutoslib.client.screen.GuiUtils;

public class GuiVisceralRecaller extends AbstractContainerScreen<ContainerVisceralRecaller> {
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_gui.png");
	private static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/blood_bar.png");
	private static final ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
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

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		forgetButton.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
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
	public void renderBackground(MatrixStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, "Visceral Recaller", 8, 4, 0);
		this.font.draw(matrixStack, String.valueOf(te.getBloodVolume()), 130, 4, 0000);
		this.font.draw(matrixStack, "Inventory", 8, this.imageHeight - 90, 000000);
		// Draw Volume
		float bloodVolume = te.getBloodVolume();
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;
		mc.textureManager.bindForSetup(fill_texture);
		float textureUShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float textureVShift = (te.getLevel().getGameTime() * 0.25f % 256);
		float heightShift = (float) Math.cos(te.getLevel().getGameTime() * 0.1);
		// bar
		//GlStateManager._pushMatrix();
		RenderSystem.enableAlphaTest();
		//GlStateManager._rotatef(180, 1, 0, 3);
		// //GlStateManager.scaled(1.25, 1, 1.25);
		GuiUtils.drawTexturedModalRect(-18.5f, -84, 23 + textureUShift, textureVShift, 6,
				(int) newBarWidth + 8 + heightShift);
		mc.textureManager.bindForSetup(texture);
		// Cap
		GuiUtils.drawTexturedModalRect(-21.5f, -45, 9, 244, 13, 12);
		// Frame
		GuiUtils.drawTexturedModalRect(-21.5f, -95, 1, 0, 12, 51);
		RenderSystem.disableAlphaTest();
		//GlStateManager._popMatrix();

		// Draw lines
		drawCenter();
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
		//GlStateManager._color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.renderBackground(matrixStack);
		Minecraft.getInstance().getTextureManager().bindForSetup(GUI_RECALLER);
		GuiUtils.drawTexturedModalRect(this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	protected void init() {
		super.init();
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttons.clear();
		this.addButton(forgetButton = new GuiButtonTextured(GUI_RECALLER, FORGETBUTTONID, leftPos + 152, topPos + 47,
				16, 16, 176, 0, null, (press) -> {
					if (press instanceof GuiButtonTextured) {
						PacketHandler.CHANNELMAIN.sendToServer(new PacketClearRecallerState());
					}
				}));

	}

	@Override
	protected <T extends IGuiEventListener> T addWidget(T listener) {
		return super.addWidget(listener);
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

	private void drawCenter() {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		//GlStateManager._pushMatrix();
		//GlStateManager._translated(0, 2, 0);
		//GlStateManager._pushMatrix();
		//GlStateManager._scaled(0.25, 0.25, 0.25);
		//GlStateManager._translated(385, 210, 0);
		int centerOffset = 8;
		int cx = 0, cy = 0;
		float rotAngle = -90f;
		int distance = 85;
		int diameter = 35;
		float spikeBaseWidth = 23.5f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			double cx1 = ((double) cx
					+ Math.cos(Math.toRadians((float) rotAngle + spikeBaseWidth)) * (double) diameter);
			double cx2 = ((double) cx
					+ Math.cos(Math.toRadians((float) rotAngle - spikeBaseWidth)) * (double) diameter);
			double cy1 = ((double) cy
					+ Math.sin(Math.toRadians((float) rotAngle + spikeBaseWidth)) * (double) diameter);
			double cy2 = ((double) cy
					+ Math.sin(Math.toRadians((float) rotAngle - spikeBaseWidth)) * (double) diameter);
			double depthDist = ((float) (distance - diameter) * affs.get(tend) + (float) diameter);
			int lx = (int) ((double) cx + Math.cos(Math.toRadians(rotAngle)) * (double) depthDist);
			int ly = (int) ((double) cy + Math.sin(Math.toRadians(rotAngle)) * (double) depthDist);
			int displace = (int) ((float) (Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2)
					- Math.min(cy1, cy2)) / 2f);
			GuiUtils.fracLine(lx + centerOffset, ly + centerOffset, cx1 + centerOffset, cy1 + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 1.1);
			GuiUtils.fracLine(lx + centerOffset, ly + centerOffset, cx2 + centerOffset, cy2 + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 1.1);
			GuiUtils.fracLine(cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 0.8);
			GuiUtils.fracLine(cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset, ly + centerOffset, this.zLevel,
					tend.getColor().getColor(), displace, 0.8);
			rotAngle += 45;
		}
		//GlStateManager._popMatrix();
		//GlStateManager._scaled(0.75, 0.75, 0.75);
		//GlStateManager._translated(122, 65, 0);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int newX = (int) ((double) cx + Math.cos(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			int newY = (int) ((double) cy + Math.sin(Math.toRadians(rotAngle)) * (double) distance / 1.75);
			mc.getItemRenderer().renderGuiItem(new ItemStack(EnumBloodTendency.getRepEnzyme(tend)), newX, newY);
			rotAngle += 44.5f;

		}
		//GlStateManager._popMatrix();
	}

}
