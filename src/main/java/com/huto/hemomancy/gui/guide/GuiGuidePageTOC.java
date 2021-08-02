package com.huto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.Component;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGuidePageTOC extends GuiGuidePage {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/guidepage.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	final int ARROWF = 0, ARROWB = 1, TITLEBUTTON = 2, CLOSEBUTTON = 3;
	GuiButtonBookArrow arrowF;
	GuiButtonBookArrow arrowB;
	GuiButtonTextured buttonTitle;
	GuiButtonTextured buttonCloseTab;
	GuiButtonTextured buttonTOC;
	public static List<GuiGuidePage> chapterPages = new ArrayList<GuiGuidePage>();
	GuiButtonTextured[] buttonArray = new GuiButtonTextured[chapterPages.size()];
	public static List<GuiButtonTextured> buttonList = new ArrayList<GuiButtonTextured>();
	ItemStack icon;
	EnumTomeCatagories catagory;

	@OnlyIn(Dist.CLIENT)
	public GuiGuidePageTOC(EnumTomeCatagories catagoryIn, ItemStack iconIn) {
		super(0, catagoryIn, "Table of Contents", "", iconIn, "");
		this.icon = iconIn;
		this.catagory = catagoryIn;
	}

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
		{
			//GlStateManager._translatef((width / 2) - 40, centerY + 10, 10);
			//GlStateManager._scalef(1, 1, 1);
			drawString(matrixStack, font, title, -5, 0, 8060954);
		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			// Draw Strings
		}
		//GlStateManager._popMatrix();

		//GlStateManager._pushMatrix();
		{
			//GlStateManager._color4f(1, 1, 1, 1);

			buttonTitle.renderButton(matrixStack, mouseX, mouseY, 311);
			buttonCloseTab.renderButton(matrixStack, mouseX, mouseY, 411);

			for (int i = 1; i < buttonList.size(); i++) {
				buttonList.get(i).renderButton(matrixStack, mouseX, mouseY, 511);
				//GlStateManager._translatef(0, 0, 10);
				drawString(matrixStack, font, "Pg." + i, (buttonList.get(i).posX + 2), buttonList.get(i).posY + 2,
						8060954);

				drawString(matrixStack, font, getMatchingChapter().get(i).title, (int) (buttonList.get(i).posX + 30),
						buttonList.get(i).posY + 2, 8060954);

			}

			arrowF.renderButton(matrixStack, mouseX, mouseY, 511);
			arrowB.renderButton(matrixStack, mouseX, mouseY, 511);

		}
		//GlStateManager._popMatrix();

		//GlStateManager._translatef(3, 0, 0);
		//GlStateManager._pushMatrix();
		{
			//GlStateManager._translatef(centerX, centerY, 0);
			//GlStateManager._translatef(3, 3, 0);
			//GlStateManager._scalef(1.9f, 1.7f, 1.9f);
			Lighting.turnBackOn();
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(icon, 0, 2);

		}
		//GlStateManager._popMatrix();

		if (!(mouseX >= 0 && mouseX <= (16 * 2) + 16 + width && mouseY >= (16 * 2) + 20
				&& mouseY <= (16 * 2) + 20 + height)) {
			List<Component> text = new ArrayList<Component>();
			text.add(new TextComponent(I18n.get(icon.getHoverName().getString())));
			renderComponentTooltip(matrixStack, text, centerX, centerY);
		}
		List<Component> titlePage = new ArrayList<Component>();
		titlePage.add(new TextComponent(I18n.get("Title")));
		titlePage.add(new TextComponent(I18n.get("Return to Catagories")));
		if (buttonTitle.isHovered()) {
			renderComponentTooltip(matrixStack, titlePage, mouseX, mouseY);
		}
		List<Component> ClosePage = new ArrayList<Component>();
		ClosePage.add(new TextComponent(I18n.get("Close Book")));
		if (buttonCloseTab.isHovered()) {
			renderComponentTooltip(matrixStack, ClosePage, mouseX, mouseY);
		}
	}

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		int sideLoc = left + guiWidth;
		int verticalLoc = top + guiHeight;
		buttons.clear();
		buttonList.clear();
		checkChapter();
		this.addButton(buttonTitle = new GuiButtonTextured(texture, TITLEBUTTON, left - guiWidth + 150,
				top + guiHeight - 209, 24, 16, 174, 32, null, (press) -> {
					mc.setScreen(new GuiGuideTitlePage());
				}));

		this.addButton(buttonCloseTab = new GuiButtonTextured(texture, CLOSEBUTTON, left - guiWidth + 150,
				top + guiHeight - 193, 24, 16, 174, 64, null, (press) -> {
					this.onClose();
				}));

		for (int i = 0; i < chapterPages.size(); i++) {
			buttonList.add(new GuiButtonTextured(texture, i, sideLoc - (guiWidth - 5), (verticalLoc - 210) + (i * 15),
					163, 14, 5, 228, null, new IPressable() {
						@Override
						public void onPress(Button press) {
							if (press instanceof GuiButtonTextured) {
								GuiButtonTextured button = (GuiButtonTextured) press;
								tableButtonCheck((button.getId()));
							}
						}
					}));
		}

		for (int i = 0; i < buttonList.size(); i++) {
			this.addButton(buttonList.get(i));
		}

		this.addButton(arrowF = new GuiButtonBookArrow(ARROWF, left + guiWidth - 18, top + guiHeight - 10, 16, 14, 175,
				1, new IPressable() {
					@Override
					public void onPress(Button p_onPress_1_) {
						mc.setScreen(getMatchingChapter().get(1));
					}
				}));
		this.addButton(
				arrowB = new GuiButtonBookArrow(ARROWB, left, top + guiHeight - 10, 16, 14, 192, 1, new IPressable() {
					@Override
					public void onPress(Button p_onPress_1_) {

						mc.setScreen(new GuiGuideTitlePage());

					}
				}));

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		for (IGuiEventListener iguieventlistener : this.children()) {
			if (iguieventlistener.mouseClicked(mouseX, mouseY, mouseButton)) {
				this.setFocused(iguieventlistener);
				if (mouseButton == 0) {
					this.setDragging(true);
				}
				return true;
			}
		}
		return false;
	}

	public void tableButtonCheck(int page) {
		mc.setScreen(this.getMatchingChapter().get(page));

	}

	public void checkChapter() {
		switch (this.catagory) {
		case INTRO:
			chapterPages = GuideBookLib.getIntroPageList();
			break;
		case VASCULARSYSTEM:
			chapterPages = GuideBookLib.getVascularPageList();
			break;
		case TENDENCY:
			chapterPages = GuideBookLib.getTendencyPageList();
			break;
		case MANIPULATION:
			chapterPages = GuideBookLib.getManipulationPageList();
			break;
		default:
			break;
		}
	}

	public List<GuiGuidePage> getMatchingChapter() {
		switch (this.catagory) {
		case INTRO:
			return GuideBookLib.getIntroPageList();
		case VASCULARSYSTEM:
			return GuideBookLib.getVascularPageList();
		case TENDENCY:
			return GuideBookLib.getTendencyPageList();
		case MANIPULATION:
			return GuideBookLib.getManipulationPageList();
		default:
			break;
		}
		return null;
	}

}
