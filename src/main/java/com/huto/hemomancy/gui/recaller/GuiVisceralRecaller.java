package com.huto.hemomancy.gui.recaller;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerVisceralRecaller;
import com.huto.hemomancy.tile.TileEntityVisceralRecaller;
import com.hutoslib.client.gui.GuiUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class GuiVisceralRecaller extends ContainerScreen<ContainerVisceralRecaller> {
	private static final ResourceLocation GUI_RECALLER = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/recaller_gui.png");
	private static final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/blood_bar.png");

	private static final ResourceLocation fill_texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/blood_fill_tiled.png");
	@SuppressWarnings("unused")
	private final PlayerInventory playerInv;
	private final TileEntityVisceralRecaller te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;

	public GuiVisceralRecaller(ContainerVisceralRecaller screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.guiLeft = 0;
		this.guiTop = 0;
		this.xSize = 176;
		this.ySize = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	private Minecraft mc = Minecraft.getInstance();

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).renderWidget(matrixStack, mouseX, mouseY, 10);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

	}

	@Override
	public void renderBackground(MatrixStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		this.font.drawString(matrixStack, "Visceral Recaller", 8, 4, 0);
		this.font.drawString(matrixStack, String.valueOf(te.getBloodVolume()), 130, 4, 0000);
		this.font.drawString(matrixStack, "Inventory", 8, this.ySize - 90, 000000);
		float bloodVolume = te.getBloodVolume();
		bloodVolume = 0.01f * (float) Math.floor(bloodVolume * 100.0);
		float newBarWidth = (int) ((bloodVolume) / 120) - 8;

		mc.textureManager.bindTexture(fill_texture);
		float textureUShift = (te.getWorld().getGameTime() * 0.25f % 256);
		float textureVShift = (te.getWorld().getGameTime() * 0.25f % 256);
		float heightShift = (float) Math.cos(te.getWorld().getGameTime() * 0.1);
		// bar
		GL11.glPushMatrix();
		RenderSystem.enableAlphaTest();
		GlStateManager.rotatef(180, 1, 0, 3);
		// GlStateManager.scaled(1.25, 1, 1.25);
		// Render anything you want to render here
		GuiUtil.drawTexturedModalRect(-18.5f, -86, 23 + textureUShift, textureVShift, 6,
				(int) newBarWidth + 8 + heightShift);
		mc.textureManager.bindTexture(texture);
		// Cap
		GuiUtil.drawTexturedModalRect(-21.5f, -47, 9, 244, 13, 12);
		// Frame
		GuiUtil.drawTexturedModalRect(-21.5f, -97, 1, 0, 12, 51);
		RenderSystem.disableAlphaTest();
		GL11.glPopMatrix();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.renderBackground(matrixStack);
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_RECALLER);
		GuiUtils.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, 0f);

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
	}

	@Override
	protected <T extends IGuiEventListener> T addListener(T listener) {
		return super.addListener(listener);
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

}