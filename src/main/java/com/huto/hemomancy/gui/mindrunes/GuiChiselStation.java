package com.huto.hemomancy.gui.mindrunes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.huto.hemomancy.network.PacketChiselCraftingEvent;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.PacketUpdateChiselRunes;
import com.huto.hemomancy.recipe.RecipeChiselStation;
import com.huto.hemomancy.tile.BlockEntityChiselStation;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
import com.hutoslib.common.item.ItemKnapper;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiChiselStation extends AbstractContainerScreen<ContainerChiselStation> {
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");

	private final Inventory playerInv;
	private final BlockEntityChiselStation te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public GuiButtonTextured[][] runeButtonArray = new GuiButtonTextured[8][8];
	public GuiButtonTextured[][] runePatternButtonArray = new GuiButtonTextured[8][8];

	int CLEARBUTTONID = 70;
	GuiButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	GuiButtonTextured chiselButton;
	public List<Integer> activatedRuneList = new ArrayList<Integer>();

	public GuiChiselStation(ContainerChiselStation screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
	}

	@SuppressWarnings({ "deprecation", "unused" })
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;

		// GlStateManager._pushMatrix();
		{
			//// GlStateManager._enableAlphaTest();
			// GlStateManager._enableBlend();
			// GlStateManager._color4f(1, 1, 1, 1);
			Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);

		}
		// GlStateManager._popMatrix();
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(matrixStack, mouseX, mouseY, 10);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		// GlStateManager._pushMatrix();
		{
			// GlStateManager._translatef(-80, 0, 10);
			for (int i = 0; i < 64; i++) {

				if (((GuiButtonTextured) renderables.get(i)).isHovered()) {
					renderTooltip(matrixStack, new TextComponent("Rune:" + i), mouseX, mouseY);
				}
			}

			List<Component> cat1 = new ArrayList<Component>();
			cat1.add(new TextComponent(I18n.get("Clear Runes")));
			if (clearButton.isHovered()) {
				renderComponentTooltip(matrixStack, cat1, left + guiWidth - (guiWidth - 120), top + guiHeight - (170));
			}
			List<Component> cat9 = new ArrayList<Component>();
			cat9.add(new TextComponent(I18n.get("Chisel Rune")));
			if (chiselButton.isHovered()) {
				renderComponentTooltip(matrixStack, cat9, left + guiWidth - (guiWidth - 120), top + guiHeight - (150));
			}
		}
		// GlStateManager._popMatrix();

		if (te.getLevel().getGameTime() % 2 == 0) {
			if (te.getItem(4) != null && this.te.getItem(4) != ItemStack.EMPTY) {
				Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);
				if (te.getItem(4).getItem() instanceof ItemRunePattern) {
					// GlStateManager._translatef(0, 0, -1);
					ItemRunePattern runePattern = (ItemRunePattern) te.getItem(4).getItem();
					int incPattern = 100;
					for (int i = 0; i < runePatternButtonArray.length; i++) {
						for (int j = 0; j < runePatternButtonArray.length; j++) {

							renderables.add(runePatternButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, incPattern,
									left + guiWidth - (guiWidth + 80 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8,
									8, 176, 0, runePattern.getRecipe().getActivatedRunes().contains(incPattern - 100),
									null, (press) -> {
									}));
							incPattern++;
						}
					}
				}
			} else {
				for (int i = 0; i < renderables.size(); i++) {
					if (renderables.get(i) instanceof GuiButtonTextured) {
						GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
						if (test.getId() > 99) {
							renderables.remove(test);
						}

					}
				}
			}
		}

	}

	@Override
	public void renderBackground(PoseStack matrixStack) {
		super.renderBackground(matrixStack);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderLabels(PoseStack matrixStack, int x, int y) {
		this.font.draw(matrixStack, this.te.getDisplayName().getContents(), 8, 6, 65444444);
		this.font.draw(matrixStack, this.playerInv.getDisplayName().getContents(), 8, this.imageHeight - 92, 000000);
		// GlStateManager._pushMatrix();
		if (te.hasValidRecipe()) {
			RecipeChiselStation currentRecipe = te.getCurrentRecipe();
			// GlStateManager._translatef(0, 0, 10);
			minecraft.getItemRenderer().renderAndDecorateItem(currentRecipe.getOutput(), 145, 44);
			// GlStateManager._scalef(0.45f, 0.5f, 0.5f);
			renderTooltip(matrixStack, new TextComponent(I18n.get(currentRecipe.getOutput().getDescriptionId())), 250,
					150);

		}
		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		if (te.hasValidRecipe()) {
			Minecraft.getInstance().textureManager.bindForSetup(GUI_Chisel);
			if (te.areRunesMatching()) {
				GuiUtils.drawScaledTexturedModalRect(162 - 42, 45 + 32, 176, 96, 16, 16, 10f);
			} else {
				GuiUtils.drawScaledTexturedModalRect(162 - 42, 45 + 32, 176, 80, 16, 16, 10f);
			}
			if (te.getItem(3).getItem() instanceof ItemKnapper) {
				//// GlStateManager._enableAlphaTest();
				GuiUtils.drawScaledTexturedModalRect(162 - 42 + 16, 45 + 32, 176 + 16, 96, 16, 16, 10f);
			} else {
				//// GlStateManager._enableAlphaTest();
				GuiUtils.drawScaledTexturedModalRect(162 - 42 + 16, 45 + 32, 176 + 16, 96 - 16, 16, 16, 10f);
			}
		}

		// GlStateManager._popMatrix();

		// GlStateManager._pushMatrix();
		// Prevents the runes from disappering on resize or jei check
		for (int l = 0; l < runeButtonArray.length; l++) {
			for (int m = 0; m < runeButtonArray.length; m++) {
				for (int k = 0; k < this.getActivatedRuneList().size(); k++) {
					if (runeButtonArray[l][m].getId() == this.getActivatedRuneList().get(k)) {
						runeButtonArray[l][m].setState(true);
					}
				}
			}
		}

		// GlStateManager._popMatrix();

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		// GlStateManager._color4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.renderBackground(matrixStack);
		Minecraft.getInstance().getTextureManager().bindForSetup(GUI_Chisel);
		GuiUtils.drawScaledTexturedModalRect(this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 0f);

	}

	@Override
	protected void init() {
		super.init();
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		renderables.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				this.addRenderableWidget(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 50 - (i * 8)), top + guiHeight - (160 - (j * 8)), 8, 8, 176, 0,
						false, null, (press) -> {
							if (press instanceof GuiButtonTextured) {
								GuiButtonTextured button = (GuiButtonTextured) press;
								if (button.getId() <= 64) {
									if (renderables.get(button.getId()) instanceof GuiButtonTextured) {
										GuiButtonTextured test = (GuiButtonTextured) renderables.get(button.getId());
										if (test.getState() == false) {
											test.setState(true);
											activatedRuneList.add(test.getId());
											PacketHandler.CHANNELMAIN
													.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
										} else if (test.getState() == true) {
											test.setState(false);
											activatedRuneList.remove(Integer.valueOf(test.getId()));
											PacketHandler.CHANNELMAIN
													.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
										}

									}

								}
							}

						}));

				inc++;
			}
		}
		this.addRenderableWidget(clearButton = new GuiButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, null, (press) -> {
					// PacketHandler.INSTANCE.sendToServer(new PacketChiselCraftingEvent());
					for (int i = 0; i < 64; i++) {
						if (renderables.get(i) instanceof GuiButtonTextured) {
							GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
							if (test.getState() == true) {
								test.setState(false);
								activatedRuneList.clear();
								PacketHandler.CHANNELMAIN.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
							}
						}
					}
				}));
		this.addRenderableWidget(chiselButton = new GuiButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, null, (press) -> {
					if (te.chestContents.get(3).getItem() != Items.AIR) {
						PacketHandler.CHANNELMAIN.sendToServer(new PacketChiselCraftingEvent());
						for (

								int i = 0; i < 64; i++) {
							if (renderables.get(i) instanceof GuiButtonTextured) {
								GuiButtonTextured test = (GuiButtonTextured) renderables.get(i);
								if (test.getState() == true) {
									test.setState(false);
									activatedRuneList.clear();
									PacketHandler.CHANNELMAIN
											.sendToServer(new PacketUpdateChiselRunes(activatedRuneList));
								}

							}
						}
					}
				}));

	}

	public List<Integer> getActivatedRuneList() {
		return activatedRuneList;
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