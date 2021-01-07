package com.huto.hemomancy.gui.guide;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.gui.GuiButtonTextured;
import com.huto.hemomancy.gui.GuiUtil;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipes.ModBloodCraftingRecipes;
import com.huto.hemomancy.render.RenderMultiBlock;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGuideTitlePage extends Screen {

	final ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/guidebook.png");
	Minecraft mc = Minecraft.getInstance();
	int guiWidth = 186;
	int guiHeight = 240;
	int left, top;
	final int BUTTONCLOSE = 0;
	final int BUTTONINTRO = 1;
	final int BUTTONVASCULAR = 2;
	final int BUTTONTENDENCY = 3;
	final int BUTTONMANIPULATION = 4;
	final int BUTTONHIDDEN = 8;
	static String title = " Table of Contents";
	static StringTextComponent titleComponent = new StringTextComponent(title);
	String subtitle = " Hemomancy; Sanguine Mastery";
	ItemStack icon = new ItemStack(ItemInit.sanguine_formation.get());
	GuiButtonTextured buttonclose, introButton, vascularButton, tendencyButton, manipulationButton, hiddenButton;
	boolean isElder;

	public GuiGuideTitlePage(boolean isElderIn) {
		super(titleComponent);
		this.isElder = isElderIn;

	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);
		GlStateManager.pushMatrix();
		{
			GlStateManager.color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
			GuiUtil.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth, guiHeight);
		}
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		{
			// The 10 for the z translate draws the text ON the book
			GlStateManager.translatef((width / 2) - font.getStringWidth(title) / 2 - 40, centerY + 10, 30);
			drawString(matrixStack, font, TextFormatting.GOLD + title, 0, 0, 8060954);
			drawString(matrixStack, font, TextFormatting.GOLD + subtitle, 0, 10, 8060954);
		}
		GlStateManager.popMatrix();

		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
			if (isElder) {
				hiddenButton.renderButton(matrixStack, mouseX, mouseY, 16);
			}

		}

		GlStateManager.pushMatrix();
		{

			GlStateManager.translatef(centerX, centerY, 0);
			GlStateManager.scalef(2, 2, 2);
			// mc.getItemRenderer().renderItemAndEffectIntoGUI(icon, 0, 0);
			GlStateManager.scalef(2, 2, 2);
			GlStateManager.translatef(18, 25, 0);
			// Enables lighting so it doesnt look dark
			RenderHelper.enableStandardItemLighting();
			/*
			 * mc.getItemRenderer().renderItemIntoGUI(new
			 * ItemStack(BlockInit.somnolent_sapling.get()), 0, -4);
			 * mc.getItemRenderer().renderItemIntoGUI(new
			 * ItemStack(BlockInit.somnolent_earth.get()), 0, 8);
			 */
			GlStateManager.pushMatrix();
			GlStateManager.translatef(3, -5, 0);
			GlStateManager.scalef(0.5f, 0.5f, 0.5f);
			RenderMultiBlock.renderPatternInGUI(matrixStack, minecraft,
					ModBloodCraftingRecipes.liber_sanguinum_pattern);

			// mc.getItemRenderer().renderItemIntoGUI(new ItemStack(Items.FIRE_CHARGE), 0,
			// -9);
			GlStateManager.popMatrix();

			GlStateManager.scalef(15, 15, 15);
			GlStateManager.translatef(0.5f, 1.2f, 0f);
			GlStateManager.rotatef(180, 100, 0, 360);
			// This lightmap turns the brightness back up so the gui doesnt get dark at
			// night

			// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f,
			// 240f);
			// mc.getRenderManager().renderEntityStatic(entityIn, xIn, yIn, zIn,
			// rotationYawIn, partialTicks, matrixStackIn, bufferIn, packedLightIn);
//			mc.getRenderManager().renderEntityStatic(CowEntity, 1, 0, 0, 0, partialTicks, matrixStack,null, 2);

			IRenderTypeBuffer.Impl bufferIn = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

			/*
			 * mc.getRenderManager().renderEntityStatic( new
			 * EntityColin(EntityInit.colin.get(),
			 * ClientEventSubscriber.getClientPlayer().getEntityWorld()), 1, 0, 0, 33, 0,
			 * matrixStack, bufferIn, 0);
			 */

			bufferIn.finish();

			GlStateManager.rotatef(-25, 0, 10, 40);
			GlStateManager.translatef(0f, 1.5f, 3f);
			GlStateManager.scalef(0.5f, 0.5f, 0.5f);
			// mc.getRenderManager().renderEntity(new EntityElemental(mc.world), 1, 0, 0,
			// 40, 0, true);

		}
		GlStateManager.popMatrix();

		if (buttonclose.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Close"), mouseX, mouseY);
		}
		if (introButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Intro"), mouseX, mouseY);
		}
		if (vascularButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Vascular System"), mouseX, mouseY);
		}
		if (tendencyButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Blood Tendency"), mouseX, mouseY);
		}
		if (manipulationButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Manipulations"), mouseX, mouseY);
		}
		if (isElder) {
			if (hiddenButton.isHovered()) {
				renderTooltip(matrixStack, new StringTextComponent("Hidden"), mouseX, mouseY);
			}
		}
	}

	@Override
	public void init() {
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		int sideLoc = left + guiWidth;
		int verticalLoc = top + guiHeight;
		buttons.clear();
		this.addButton(buttonclose = new GuiButtonTextured(texture, BUTTONCLOSE, sideLoc - (guiWidth - 10),
				verticalLoc - 50, 32, 32, 209, 32, null, (press) -> {
					closeScreen();
				}));
		this.addButton(introButton = new GuiButtonTextured(texture, BUTTONINTRO, sideLoc - (guiWidth - 174),
				verticalLoc - 226, 23, 16, 186, 0, null, (press) -> {
					mc.displayGuiScreen(GuideBookLib.getIntroPageList().get(0));
				}));
		this.addButton(vascularButton = new GuiButtonTextured(texture, BUTTONVASCULAR, sideLoc - (guiWidth - 175),
				verticalLoc - 181, 23, 16, 186, 32, null, (press) -> {
					mc.displayGuiScreen(GuideBookLib.getVascularPageList().get(0));
				}));
		this.addButton(tendencyButton = new GuiButtonTextured(texture, BUTTONTENDENCY, sideLoc - (guiWidth - 175),
				verticalLoc - 153, 23, 16, 186, 64, null, (press) -> {
					mc.displayGuiScreen(GuideBookLib.getTendencyPageList().get(0));
				}));
		this.addButton(manipulationButton = new GuiButtonTextured(texture, BUTTONMANIPULATION,
				sideLoc - (guiWidth - 177), verticalLoc - 121, 24, 16, 186, 96, null, (press) -> {
					mc.displayGuiScreen(GuideBookLib.getManipulationPageList().get(0));
				}));
		if (isElder) {
			this.addButton(hiddenButton = new GuiButtonTextured(texture, BUTTONHIDDEN, sideLoc - (guiWidth - 155),
					verticalLoc - 30, 16, 16, 209, 0, null, (press) -> {
						mc.displayGuiScreen(GuideBookLib.getHiddenPageList().get(0));
					}));
		}

	}

	@Override
	protected <T extends IGuiEventListener> T addListener(T listener) {

		return super.addListener(listener);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
