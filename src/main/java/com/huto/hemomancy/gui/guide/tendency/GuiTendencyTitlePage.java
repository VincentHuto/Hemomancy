package com.huto.hemomancy.gui.guide.tendency;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.hutoslib.client.gui.GuiButtonTextured;
import com.hutoslib.client.gui.GuiUtil;
import com.hutoslib.client.render.RenderMultiBlockInGui;
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
public class GuiTendencyTitlePage extends Screen {

	Minecraft mc = Minecraft.getInstance();
	int guiWidth = 186;
	int guiHeight = 240;
	int left, top;
	final int BUTTONCLOSE = 0;
	final int BUTTONANIMUS = 1;
	final int BUTTONMORTEM = 2;
	final int BUTTONDUCTILIS = 3;
	final int BUTTONFERRIC = 4;

	final int BUTTONLUX = 5;
	final int BUTTONTENEBRIS = 6;
	final int BUTTONFLAMMEUS = 7;
	final int BUTTONCONGEATIO = 8;
	final int BUTTONHIDDEN = 9;
	static String title = " Table of Contents";
	static StringTextComponent titleComponent = new StringTextComponent(title);
	String subtitle = " Hemomancy; Blood Tendency";
	ItemStack icon = new ItemStack(ItemInit.sanguine_formation.get());
	GuiButtonTextured buttonclose, animusButton, mortemButton, ductilisButton, ferricButton, luxButton, tenebrisButton,
			flammeusButton, congeatioButton, hiddenButton;
	boolean isElder;
	ResourceLocation texture;

	public GuiTendencyTitlePage(boolean isElderIn) {
		super(titleComponent);
		this.isElder = isElderIn;
		texture = isElder ? new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/tendencybook_hidden.png")
				: new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/tendencybook.png");
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
				hiddenButton.renderWidget(matrixStack, mouseX, mouseY, 16);
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
			RenderMultiBlockInGui.renderPatternInGUI(matrixStack, minecraft, ModBloodCraftingRecipes.ssc_pattern);

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
		if (animusButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Animus"), mouseX, mouseY);
		}
		if (mortemButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Mortem"), mouseX, mouseY);
		}
		if (ductilisButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Ductilis"), mouseX, mouseY);
		}
		if (ferricButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Ferric"), mouseX, mouseY);
		}
		if (luxButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Lux"), mouseX, mouseY);
		}
		if (tenebrisButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Tenebris"), mouseX, mouseY);
		}
		if (flammeusButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Flammeus"), mouseX, mouseY);
		}
		if (congeatioButton.isHovered()) {
			renderTooltip(matrixStack, new StringTextComponent("Congeatio"), mouseX, mouseY);
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
		this.addButton(animusButton = new GuiButtonTextured(texture, BUTTONANIMUS, sideLoc - (guiWidth - 174),
				verticalLoc - 226, 23, 16, 186, 0, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getAnimusPageList().get(0));
				}));
		this.addButton(mortemButton = new GuiButtonTextured(texture, BUTTONMORTEM, sideLoc - (guiWidth - 175),
				verticalLoc - 181, 23, 16, 186, 32, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getMortemPageList().get(0));
				}));
		this.addButton(ductilisButton = new GuiButtonTextured(texture, BUTTONDUCTILIS, sideLoc - (guiWidth - 175),
				verticalLoc - 153, 23, 16, 186, 64, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getDuctilisPageList().get(0));
				}));
		this.addButton(ferricButton = new GuiButtonTextured(texture, BUTTONFERRIC, sideLoc - (guiWidth - 177),
				verticalLoc - 121, 24, 16, 186, 96, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getFerricPageList().get(0));
				}));
		this.addButton(luxButton = new GuiButtonTextured(texture, BUTTONLUX, sideLoc - (guiWidth - 180),
				verticalLoc - 91, 24, 16, 186, 128, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getLuxPageList().get(0));
				}));
		this.addButton(tenebrisButton = new GuiButtonTextured(texture, BUTTONTENEBRIS, sideLoc - (guiWidth - 177),
				verticalLoc - 49, 24, 16, 186, 160, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getTenebrisPageList().get(0));
				}));
		this.addButton(flammeusButton = new GuiButtonTextured(texture, BUTTONFLAMMEUS, sideLoc - (guiWidth - 177),
				verticalLoc - 69, 24, 16, 209, 160, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getFlammeusPageList().get(0));
				}));
		this.addButton(congeatioButton = new GuiButtonTextured(texture, BUTTONCONGEATIO, sideLoc - (guiWidth - 177),
				verticalLoc - 200, 24, 16, 186, 192, null, (press) -> {
					mc.displayGuiScreen(TendencyBookLib.getCongeatioPageList().get(0));
				}));

		if (isElder) {
			this.addButton(hiddenButton = new GuiButtonTextured(texture, BUTTONHIDDEN, sideLoc - (guiWidth - 155),
					verticalLoc - 30, 16, 16, 209, 0, null, (press) -> {
						mc.displayGuiScreen(TendencyBookLib.getHiddenPageList().get(0));
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
