package com.vincenthuto.hemomancy.gui.mindrunes;

import java.util.List;

import com.google.common.collect.Lists;
//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;
import com.vincenthuto.hutoslib.client.screen.GuiButtonTextured;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public class ScreenRunePattern extends Screen {
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
	ChiselRecipe recipe;
	public GuiButtonTextured[][] runeButtonArray = new GuiButtonTextured[8][8];
	protected List<Button> buttonList = Lists.<Button>newArrayList();

	@OnlyIn(Dist.CLIENT)
	public ScreenRunePattern(RegistryObject<Item> iconIn, ChiselRecipe recipeIn, String textIn) {
		super(titleComponent);
		this.icon = iconIn;
		this.recipe = recipeIn;
		this.text = textIn;

	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		HLGuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).render(matrixStack, 0, 0, 10);
		}

		matrixStack.translate(centerX + 100, centerY + 10, 0);
		drawString(matrixStack, font, ChatFormatting.GOLD + I18n.get(recipe.getOutputItem().getDescriptionId()), -85, 0,
				8060954);
		font.drawWordWrap(new TextComponent(ChatFormatting.BLACK + I18n.get(text)), centerX + 1 + 10, centerY + 151,
				150, 0);
		font.drawWordWrap(new TextComponent(ChatFormatting.GOLD + I18n.get(text)), centerX + 10, centerY + 150, 150, 0);

		Lighting.setupFor3DItems();
		Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(icon.get()), centerX + 79,
				centerY + 17);
		if (recipe.getIngredients().size() == 1) {
			Minecraft.getInstance().getItemRenderer()
					.renderAndDecorateItem(recipe.getIngredients().get(0).getItems()[0], 36, 6);
		} else if (recipe.getIngredients().size() == 2) {

			Minecraft.getInstance().getItemRenderer()
					.renderAndDecorateItem(recipe.getIngredients().get(0).getItems()[0], centerX + 30, centerY + 70);
			Minecraft.getInstance().getItemRenderer()
					.renderAndDecorateItem(recipe.getIngredients().get(1).getItems()[0], centerX + 30, centerY + 100);

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
				buttonList.add(runeButtonArray[i][j] = new GuiButtonTextured(GUI_Chisel, inc,
						left + guiWidth - (guiWidth - 75 - (i * 8)), top + guiHeight - (163 - (j * 8)), 8, 8, 176, 0,
						recipe.getPattern()[i][j] == 0 ? false : true, null, null));
				inc++;
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
