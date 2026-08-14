package com.vincenthuto.hemomancy.client.screen.tile.crafting.scar;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

//GlStateManager;

public class ScreenScarPattern extends Screen {
	final ResourceLocation texture = Hemomancy.rloc("textures/gui/scar_pattern.png");
	private static final ResourceLocation GUI_Chisel = Hemomancy.rloc("textures/gui/scar_station.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	String title;
	String text;

	static Component titleComponent = Component.translatable("");
	DeferredHolder<Item, Item> icon;
	Minecraft mc = Minecraft.getInstance();
	ScarRecipe recipe;
	public HLButtonTextured[][] scarbuttonArray = new HLButtonTextured[8][8];
	protected List<HLButtonTextured> buttonList = Lists.<HLButtonTextured>newArrayList();

	@OnlyIn(Dist.CLIENT)
	public ScreenScarPattern(DeferredHolder<Item, Item> iconIn, ScarRecipe recipeIn, String textIn) {
		super(titleComponent);
		this.icon = iconIn;
		this.recipe = recipeIn;
		this.text = textIn;

	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);

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
		for (int i = 0; i < scarbuttonArray.length; i++) {
			for (int j = 0; j < scarbuttonArray.length; j++) {
				buttonList.add(scarbuttonArray[i][j] = new HLButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 75 - (j * 8)), top + guiHeight - (163 - (i * 8)), 8, 8, 176, 0,
						recipe.getPattern()[i][j] != 0, null, null));
				inc++;
			}
		}

		// Back button to return to the binder viewer
		this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
				Component.literal("Back"), (press) -> {
					ScreenScarBinderViewer.openScreen(false);
				}).bounds(left, top - 20, 55, 18).build());

		super.init();
	}

	@Override
	public void renderBackground(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(pGuiGraphics, mouseX, mouseY, partialTick);

		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		pGuiGraphics.blit(texture, left - guiWidth + 175, top + guiHeight - 228, 0, 0, guiWidth - 1, guiHeight);

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		// Right-click anywhere to go back to the binder viewer
		if (mouseButton == 1) {
			ScreenScarBinderViewer.openScreen(false);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		// Backspace or Escape goes back to the binder viewer
		if (pKeyCode == InputConstants.KEY_BACKSPACE || pKeyCode == InputConstants.KEY_ESCAPE) {
			ScreenScarBinderViewer.openScreen(false);
			return true;
		}
		if (this.minecraft != null && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
}
