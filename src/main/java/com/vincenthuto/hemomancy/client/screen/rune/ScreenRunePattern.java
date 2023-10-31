package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

public class ScreenRunePattern extends Screen {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_pattern.png");
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	String title;
	String text;

	static Component titleComponent = Component.translatable("");
	RegistryObject<Item> icon;
	Minecraft mc = Minecraft.getInstance();
	ChiselRecipe recipe;
	public HLButtonTextured[][] runeButtonArray = new HLButtonTextured[8][8];
	protected List<HLButtonTextured> buttonList = Lists.<HLButtonTextured>newArrayList();

	@OnlyIn(Dist.CLIENT)
	public ScreenRunePattern(RegistryObject<Item> iconIn, ChiselRecipe recipeIn, String textIn) {
		super(titleComponent);
		this.icon = iconIn;
		this.recipe = recipeIn;
		this.text = textIn;

	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(graphics);

		graphics.drawCenteredString(font, ChatFormatting.GOLD + I18n.get(recipe.getResultItem().getDescriptionId()),
				-85, 0, 8060954);
		graphics.drawWordWrap(font, Component.translatable(ChatFormatting.BLACK + I18n.get(text)), centerX + 1 + 10,
				centerY + 151, 150, 0);
		graphics.drawWordWrap(font, Component.translatable(ChatFormatting.GOLD + I18n.get(text)), centerX + 10,
				centerY + 150, 150, 0);

		Lighting.setupFor3DItems();
		graphics.renderItem(new ItemStack(icon.get()), centerX + 79, centerY + 17);
		if (recipe.getIngredients().size() == 1) {
			graphics.renderItem(recipe.getIngredients().get(0).getItems()[0], 36, 6);
		} else if (recipe.getIngredients().size() == 2) {

			graphics.renderItem(recipe.getIngredients().get(0).getItems()[0], centerX + 30, centerY + 70);
			graphics.renderItem(recipe.getIngredients().get(1).getItems()[0], centerX + 30, centerY + 100);

		}

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).render(graphics, 0, 0, 10);
		}

	}

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttonList.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				buttonList.add(runeButtonArray[i][j] = new HLButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 75 - (j * 8)), top + guiHeight - (163 - (i * 8)), 8, 8, 176, 0,
						recipe.getPattern()[i][j] == 0 ? false : true, null, null));
				inc++;
			}
		}

		super.init();
	}

	@Override
	public void renderBackground(GuiGraphics pGuiGraphics) {
		super.renderBackground(pGuiGraphics);

		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		pGuiGraphics.blit(texture, left - guiWidth + 175, top + guiHeight - 228, 0, 0, guiWidth - 1, guiHeight);

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
}
