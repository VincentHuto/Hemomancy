package com.huto.hemomancy.gui.guide;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.hutoslib.client.render.block.render.RenderMultiBlockInGui;
import com.hutoslib.client.screen.GuiButtonTextured;
import com.hutoslib.client.screen.GuiUtils;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ChatFormatting;
import net.minecraft.world.item.ItemStack;
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
	static TextComponent titleComponent = new TextComponent(title);
	String subtitle = " Hemomancy; Sanguine Mastery";
	ItemStack icon = new ItemStack(ItemInit.sanguine_formation.get());
	GuiButtonTextured buttonclose, introButton, vascularButton, tendencyButton, manipulationButton, hiddenButton;
	boolean isElder;

	public GuiGuideTitlePage() {
		super(titleComponent);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int centerX = (width / 2) - guiWidth / 2;
		int centerY = (height / 2) - guiHeight / 2;
		this.renderBackground(matrixStack);
		//GlStateManager._pushMatrix();
		{
			//GlStateManager._color4f(1, 1, 1, 1);
			Minecraft.getInstance().getTextureManager().bindForSetup(texture);
			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth, guiHeight);
		}
		//GlStateManager._popMatrix();
		//GlStateManager._pushMatrix();
		{
			// The 10 for the z translate draws the text ON the book
			//GlStateManager._translatef((width / 2) - font.width(title) / 2 - 40, centerY + 10, 30);
			drawString(matrixStack, font, ChatFormatting.GOLD + title, 0, 0, 8060954);
			drawString(matrixStack, font, ChatFormatting.GOLD + subtitle, 0, 10, 8060954);
		}
		//GlStateManager._popMatrix();

		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
			if (isElder) {
				hiddenButton.renderButton(matrixStack, mouseX, mouseY, 16);
			}

		}

		//GlStateManager._pushMatrix();
		{

			//GlStateManager._translatef(centerX, centerY, 0);
			//GlStateManager._scalef(2, 2, 2);
			// mc.getItemRenderer().renderItemAndEffectIntoGUI(icon, 0, 0);
			//GlStateManager._scalef(2, 2, 2);
			//GlStateManager._translatef(18, 25, 0);
			// Enables lighting so it doesnt look dark
			Lighting.setupForFlatItems();
			/*
			 * mc.getItemRenderer().renderItemIntoGUI(new
			 * ItemStack(BlockInit.somnolent_sapling.get()), 0, -4);
			 * mc.getItemRenderer().renderItemIntoGUI(new
			 * ItemStack(BlockInit.somnolent_earth.get()), 0, 8);
			 */
			//GlStateManager._pushMatrix();
			//GlStateManager._translatef(3, -5, 0);
			//GlStateManager._scalef(0.5f, 0.5f, 0.5f);
			RenderMultiBlockInGui.renderPatternInGUI(matrixStack, minecraft,
					ModBloodCraftingRecipes.hematic_iron_pattern);

			// mc.getItemRenderer().renderItemIntoGUI(new ItemStack(Items.FIRE_CHARGE), 0,
			// -9);
			//GlStateManager._popMatrix();

			//GlStateManager._scalef(15, 15, 15);
			//GlStateManager._translatef(0.5f, 1.2f, 0f);
			//GlStateManager._rotatef(180, 100, 0, 360);
			// This lightmap turns the brightness back up so the gui doesnt get dark at
			// night

			// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f,
			// 240f);
			// mc.getRenderManager().renderEntityStatic(entityIn, xIn, yIn, zIn,
			// rotationYawIn, partialTicks, matrixStackIn, bufferIn, packedLightIn);
//			mc.getRenderManager().renderEntityStatic(CowEntity, 1, 0, 0, 0, partialTicks, matrixStack,null, 2);

			IRenderTypeBuffer.Impl bufferIn = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

			/*
			 * mc.getRenderManager().renderEntityStatic( new
			 * EntityColin(EntityInit.colin.get(),
			 * ClientEventSubscriber.getClientPlayer().getEntityLevel()), 1, 0, 0, 33, 0,
			 * matrixStack, bufferIn, 0);
			 */

			bufferIn.endBatch();

			//GlStateManager._rotatef(-25, 0, 10, 40);
			//GlStateManager._translatef(0f, 1.5f, 3f);
			//GlStateManager._scalef(0.5f, 0.5f, 0.5f);
			// mc.getRenderManager().renderEntity(new EntityElemental(mc.world), 1, 0, 0,
			// 40, 0, true);

		}
		//GlStateManager._popMatrix();

		if (buttonclose.isHovered()) {
			renderTooltip(matrixStack, new TextComponent("Close"), mouseX, mouseY);
		}
		if (introButton.isHovered()) {
			renderTooltip(matrixStack, new TextComponent("Intro"), mouseX, mouseY);
		}
		if (vascularButton.isHovered()) {
			renderTooltip(matrixStack, new TextComponent("Vascular System"), mouseX, mouseY);
		}
		if (tendencyButton.isHovered()) {
			renderTooltip(matrixStack, new TextComponent("Blood Tendency"), mouseX, mouseY);
		}
		if (manipulationButton.isHovered()) {
			renderTooltip(matrixStack, new TextComponent("Manipulations"), mouseX, mouseY);
		}
		if (isElder) {
			if (hiddenButton.isHovered()) {
				renderTooltip(matrixStack, new TextComponent("Hidden"), mouseX, mouseY);
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
					onClose();
				}));
		this.addButton(introButton = new GuiButtonTextured(texture, BUTTONINTRO, sideLoc - (guiWidth - 174),
				verticalLoc - 226, 23, 16, 186, 0, null, (press) -> {
					mc.setScreen(GuideBookLib.getIntroPageList().get(0));
				}));
		this.addButton(vascularButton = new GuiButtonTextured(texture, BUTTONVASCULAR, sideLoc - (guiWidth - 175),
				verticalLoc - 181, 23, 16, 186, 32, null, (press) -> {
					mc.setScreen(GuideBookLib.getVascularPageList().get(0));
				}));
		this.addButton(tendencyButton = new GuiButtonTextured(texture, BUTTONTENDENCY, sideLoc - (guiWidth - 175),
				verticalLoc - 153, 23, 16, 186, 64, null, (press) -> {
					mc.setScreen(GuideBookLib.getTendencyPageList().get(0));
				}));
		this.addButton(manipulationButton = new GuiButtonTextured(texture, BUTTONMANIPULATION,
				sideLoc - (guiWidth - 177), verticalLoc - 121, 24, 16, 186, 96, null, (press) -> {
					mc.setScreen(GuideBookLib.getManipulationPageList().get(0));
				}));
	}

	@Override
	protected <T extends IGuiEventListener> T addWidget(T listener) {

		return super.addWidget(listener);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
