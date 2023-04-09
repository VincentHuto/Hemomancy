package com.vincenthuto.hemomancy.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.ScreenBlockTintGetter;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class HemoGuideBloodStructurePage extends GuiGuidePage {

	double xDragPos = 0;
	double yDragPos = 0;
	private double dragLeftRight = 0;
	private double dragUpDown = 0;
	private ResourceLocation recipe;

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, ResourceLocation recipe) {
		super(pageNumIn, catagoryIn);
		this.recipe = recipe;
	}

	public HemoGuideBloodStructurePage(int pageNumIn, String categoryIn, String titleIn, ItemStack iconIn,
			ResourceLocation recipe) {
		super(pageNumIn, categoryIn, titleIn, "", iconIn);
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, ResourceLocation recipe) {
		super(pageNumIn, catagoryIn, titleIn, "", "");
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, String textIn,
			ResourceLocation recipe) {
		super(pageNumIn, catagoryIn, titleIn, "", textIn);
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String categoryIn, String titleIn, String subtitleIn,
			String textIn, ItemStack iconIn, ResourceLocation recipe) {
		super(pageNumIn, categoryIn, titleIn, subtitleIn, iconIn, textIn);
		this.recipe = recipe;

	}

	public HemoGuideBloodStructurePage(int pageNumIn, String catagoryIn, String titleIn, String subtitleIn,
			String textIn, ResourceLocation recipe) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, textIn);
		this.recipe = recipe;

	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}

	@Override
	public boolean mouseDragged(double xPos, double yPos, int button, double dragLeftRight, double dragUpDown) {
		xDragPos = xPos;
		yDragPos = yPos;
		this.dragLeftRight += dragLeftRight / 2;
		this.dragUpDown -= dragUpDown / 2;
		return super.mouseDragged(xPos, yPos, button, dragLeftRight, dragUpDown);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
		super.render(matrices, mouseX, mouseY, partialTicks);
		float left = width / 2 - guiWidth / 2;
		float top = height / 2 - guiHeight / 2;
		int line = 0;

		BloodStructureRecipe structure = BloodStructureRecipe.getStructureByLocation(HLClientUtils.getWorld(), recipe);
		if (structure != null) {
			MultiblockPattern pattern = structure.getPattern();
			for (Block block : pattern.getBlockCount(false).keySet()) {
				HLGuiUtils.drawMaxWidthString(font,
						Component.literal(
								I18n.get(block.getDescriptionId()) + ": " + pattern.getBlockCount(false).get(block)),
						(int) (left - guiWidth + 180), (int) (top + guiHeight - 140) - line * -10, 160, 0xffffff, true);
				line++;
				// System.out.println(I18n.get(block.getDescriptionId()) + ": " +
				// pattern.getBlockCount(false).get(block));
			}

			matrices.mulPose(Vector3.XN.rotationDegrees(-45 + (float) this.dragUpDown).toMoj());
			matrices.mulPose(Vector3.YP.rotationDegrees(45 + (float) this.dragLeftRight).toMoj());
			float structScale = 2f;
			matrices.scale(structScale, structScale, structScale);
			HLGuiUtils.renderMultiBlock(matrices, pattern, HLClientUtils.getPartialTicks(), new ScreenBlockTintGetter(),
					left - guiWidth + 260, top + guiHeight - 65);
			float guiHeight = 228, guiWidth = 174;
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(matrices, structure.getHeldItem(),
					(int) (left - guiWidth + 180), (int) (top + guiHeight - 160));
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(matrices, new ItemStack(structure.getHitBlock()),
					(int) (left - guiWidth + 196), (int) (top + guiHeight - 160));
		}

	}

}
