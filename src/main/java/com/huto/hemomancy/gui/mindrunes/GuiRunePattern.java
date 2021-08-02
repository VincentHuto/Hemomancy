package com.huto.hemomancy.gui.mindrunes;

import java.util.List;

import com.google.common.collect.Lists;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.recipe.RecipeChiselStation;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.RegistryObject;

@OnlyIn(Dist.CLIENT)
public class GuiRunePattern extends Screen {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_pattern.png");
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	String title;
	String text;

	static TextComponent titleComponent = new TextComponent("");
	RegistryObject<Item> icon;
	Minecraft mc = Minecraft.getInstance();
	RegistryObject<RecipeChiselStation> recipe;
	public GuiButtonTextured[][] runeButtonArray = new GuiButtonTextured[8][8];
	protected List<Button> buttonList = Lists.<Button>newArrayList();

	@OnlyIn(Dist.CLIENT)
	public GuiRunePattern(RegistryObject<Item> iconIn, RegistryObject<RecipeChiselStation> recipeIn, String textIn) {
		super(titleComponent);
		this.icon = iconIn;
		this.recipe = recipeIn;
		this.text = textIn;

	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindForSetup(texture);
			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).renderButton(matrixStack, 0, 0, 10);
		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._translatef((width / 2) - 20, centerY + 10, 10);
			//GlStateManager._scalef(0.9f, 1, 1);
			//GlStateManager._translatef(-65f, 25, 0);

			// drawCenteredString(matrixStack, font, I18n.format(text), 175, 10, 10);
			// Split String(text,x,y,wrapwidth,color)
			font.drawWordWrap(new TextComponent(ChatFormatting.BLACK + I18n.get(text)), 5, 101, 170, 0);
			font.drawWordWrap(new TextComponent(ChatFormatting.GOLD + I18n.get(text)), 4, 100, 170, 0);

		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			// The 10 for the z translate draws the text ON the book
			//GlStateManager._translatef(((width / 2) - font.width(title) / 2) - 75, centerY + 50, 10);
			drawString(matrixStack, font, ChatFormatting.GOLD + I18n.get(recipe.get().getOutput().getDescriptionId()),
					0, 0, 8060954);

		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._translatef(centerX, centerY, 0);
			//GlStateManager._translatef(3, 3, 0);
			//GlStateManager._scalef(1.9f, 1.7f, 1.9f);
			Lighting.turnBackOn();
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(icon.get()), 36, 6);
			if (recipe.get().getInputs().size() == 1) {
				Minecraft.getInstance().getItemRenderer()
						.renderAndDecorateItem(recipe.get().getInputs().get(0).getItems()[0], 36, 6);
			} else if (recipe.get().getInputs().size() == 2) {
				Minecraft.getInstance().getItemRenderer()
						.renderAndDecorateItem(recipe.get().getInputs().get(0).getItems()[0], 10, 36);
				Minecraft.getInstance().getItemRenderer()
						.renderAndDecorateItem(recipe.get().getInputs().get(1).getItems()[0], 10, 60);

			}

		}
		//GlStateManager._popMatrix();

	}

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		buttonList.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				buttonList.add(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 75 - (i * 8)), top + guiHeight - (163 - (j * 8)), 8, 8, 176, 0,
						false, null, null));
				inc++;
			}
		}
		for (int l = 0; l < runeButtonArray.length; l++) {
			for (int m = 0; m < runeButtonArray.length; m++) {
				for (int k = 0; k < recipe.get().getActivatedRunes().size(); k++) {
					if (runeButtonArray[l][m].getId() == recipe.get().getActivatedRunes().get(k)) {
						runeButtonArray[l][m].setState(true);
					}
				}
			}
		}

		super.init();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
