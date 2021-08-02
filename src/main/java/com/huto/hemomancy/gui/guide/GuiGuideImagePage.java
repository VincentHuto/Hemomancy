package com.huto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.huto.hemomancy.Hemomancy;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGuideImagePage extends GuiGuidePage {
	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/guidepage.png");
	int guiWidth = 175;
	int guiHeight = 228;
	int left, top;
	final int ARROWF = 0, ARROWB = 1, TITLEBUTTON = 2, CLOSEBUTTON = 3;
	int pageNum;
	String title;
	static TextComponent titleComponent = new TextComponent("");
	String subtitle;
	String text;
	ItemStack icon;
	GuiButtonBookArrow arrowF, arrowB;
	private final ImmutableList<GuiTomeImage> images;
	GuiButtonTextured buttonTitle, buttonCloseTab;
	List<GuiTomeImage> imagesArray;

	EditBox textBox;
	EnumTomeCatagories catagory;
	Minecraft mc = Minecraft.getInstance();

	@OnlyIn(Dist.CLIENT)
	public GuiGuideImagePage(int pageNumIn, EnumTomeCatagories catagoryIn, String titleIn, String subtitleIn,
			String textIn, GuiTomeImage... imagesIn) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, new ItemStack(Items.AIR), textIn);
		Preconditions.checkArgument(imagesIn.length <= 12);
		this.title = titleIn;
		this.subtitle = subtitleIn;
		this.pageNum = pageNumIn;
		this.text = textIn;
		this.catagory = catagoryIn;
		this.icon = new ItemStack(Items.AIR);
		this.images = ImmutableList.copyOf(imagesIn);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);

		// Draw Background
		GlStateManager.pushMatrix();
		{
			GlStateManager.color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth - 1, guiHeight);
		}
		GlStateManager.popMatrix();

		// Title Strings
		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef((width / 2) - 40, centerY + 10, 10);
			GlStateManager.scalef(1, 1, 1);
			drawString(matrixStack, font, "Pg." + pageNum, 90, 0, 0000000);
			drawString(matrixStack, font, title, -5, 0, 8060954);
			drawString(matrixStack, font, subtitle, -5, 10, 8060954);
		}
		GlStateManager.popMatrix();
		// String Text
		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef((width / 2) - 20, centerY + 10, 11);
			GlStateManager.scalef(0.9f, 1, 1);
			GlStateManager.translatef(-65f, 25, 0);

			// drawCenteredString(matrixStack, font, I18n.format(text), 175, 10, 10);
			// Split String(text,x,y,wrapwidth,color)
			// Max text length 377 characters
			font.func_238418_a_(new StringTextComponent(I18n.format(text)), 0, 77, 175, 0);
		}
		GlStateManager.popMatrix();

		// Button Rendering
		GlStateManager.pushMatrix();
		{
			GlStateManager.color4f(1, 1, 1, 1);
			if (pageNum != (getMatchingChapter().size() - 1)) {
				arrowF.renderWidget(matrixStack, mouseX, mouseY, 111);
			}

			if (pageNum > 0) {

				arrowB.renderWidget(matrixStack, mouseX, mouseY, 211);
			}
			buttonTitle.renderWidget(matrixStack, mouseX, mouseY, 311);
			buttonCloseTab.renderWidget(matrixStack, mouseX, mouseY, 411);

			for (int j = 0; j < imagesArray.size(); j++) {
				imagesArray.get(j).render(matrixStack, mouseX, mouseY, partialTicks, centerX + 5, centerY + 6);
			}
			for (int j = 0; j < imagesArray.size(); j++) {
				if (imagesArray.get(j).isHovered()) {
					func_243308_b(matrixStack, imagesArray.get(j).getText(), mouseX, mouseY);
				}
			}
			GlStateManager.popMatrix();

		}
		// Icon Rendering
		GlStateManager.translatef(3, 0, 0);
		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef(centerX, centerY, 0);
			GlStateManager.translatef(3, 3, 0);
			GlStateManager.scalef(1.9f, 1.7f, 1.9f);
			RenderHelper.enableStandardItemLighting();
			Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(icon, 0, 2);
		}
		GlStateManager.popMatrix();
		// ToolTips
		textBox.render(matrixStack, mouseX, mouseY, partialTicks);
		if (!(mouseX >= (16 * 2) + 16 && mouseX <= (16 * 2) + 16 + width && mouseY >= (16 * 2) + 20
				&& mouseY <= (16 * 2) + 20 + height)) {
			List<ITextComponent> text = new ArrayList<ITextComponent>();
			text.add(new StringTextComponent(I18n.format(icon.getDisplayName().getString())));
			func_243308_b(matrixStack, text, centerX, centerY);
		}
		List<ITextComponent> titlePage = new ArrayList<ITextComponent>();
		titlePage.add(new StringTextComponent(I18n.format("Title")));
		titlePage.add(new StringTextComponent(I18n.format("Return to Catagories")));
		if (buttonTitle.isHovered()) {
			func_243308_b(matrixStack, titlePage, left - guiWidth + 149, top + guiHeight - 209);
		}
		List<ITextComponent> ClosePage = new ArrayList<ITextComponent>();
		ClosePage.add(new StringTextComponent(I18n.format("Close Book")));
		if (buttonCloseTab.isHovered()) {
			func_243308_b(matrixStack, ClosePage, left - guiWidth + 149, top + guiHeight - 193);
		}

	}

	@Override
	protected void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		imagesArray = new ArrayList<>(images);
		buttons.clear();

		if (pageNum != (getMatchingChapter().size() - 1)) {
			this.addButton(arrowF = new GuiButtonBookArrow(ARROWF, left + guiWidth - 18, top + guiHeight - 10, 16, 14,
					175, 1, (press) -> {
						if (pageNum != (getMatchingChapter().size() - 1)) {
							mc.displayGuiScreen(getMatchingChapter().get((pageNum + 1)));
						} else {
							mc.displayGuiScreen(getMatchingChapter().get((pageNum)));

						}
					}));
		}
		if (pageNum != 0) {
			this.addButton(
					arrowB = new GuiButtonBookArrow(ARROWB, left, top + guiHeight - 10, 16, 14, 192, 1, (press) -> {
						if (pageNum > 0) {
							mc.displayGuiScreen(getMatchingChapter().get((pageNum - 1)));
						} else {
							mc.displayGuiScreen(getMatchingChapter().get((0)));

						}
					}));
		}
		this.addButton(buttonTitle = new GuiButtonTextured(texture, TITLEBUTTON, left - guiWidth + 150,
				top + guiHeight - 209, 24, 16, 174, 32, null, (press) -> {
					mc.displayGuiScreen(new GuiGuideTitlePage());
				}));
		this.addButton(buttonCloseTab = new GuiButtonTextured(texture, CLOSEBUTTON, left - guiWidth + 150,
				top + guiHeight - 193, 24, 16, 174, 64, null, (press) -> {
					this.closeScreen();
				}));
		textBox = new TextFieldWidget(font, left - guiWidth + 155, top + guiHeight - 227, 14, 14,
				new StringTextComponent(""));

		super.init();
	}

	public void updateTextBoxes() {
		if (!textBox.getText().isEmpty()) {
			if (NumberUtils.isCreatable(textBox.getText())) {
				int searchNum = (Integer.parseInt(textBox.getText()));
				if (searchNum < getMatchingChapter().size()) {
					mc.displayGuiScreen(getMatchingChapter().get(searchNum));
				} else if (searchNum >= getMatchingChapter().size()) {
					mc.displayGuiScreen(getMatchingChapter().get(getMatchingChapter().size() - 1));
				}
			}
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		textBox.setText(GLFW.glfwGetKeyName(keyCode, scanCode));
		updateTextBoxes();
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		textBox.mouseClicked(mouseX, mouseY, mouseButton);
		updateTextBoxes();
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
