package com.vincenthuto.hemomancy.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.gui.HemoBlockPosBlockPair;
import com.vincenthuto.hemomancy.gui.HemoGuiUtils;
import com.vincenthuto.hemomancy.gui.ScreenBlockTintGetter;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hutoslib.client.ClientUtils;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class HemoGuidePage extends GuiGuidePage {

	public HemoGuidePage(int pageNumIn, String catagoryIn) {
		super(pageNumIn, catagoryIn);
	}

	public HemoGuidePage(int pageNumIn, String catagoryIn, String textIn) {
		super(pageNumIn, catagoryIn, "", "", ItemStack.EMPTY, textIn);
	}

	public HemoGuidePage(int pageNumIn, String catagoryIn, String titleIn, String subtitleIn, String textIn) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, ItemStack.EMPTY, textIn);
	}

	public HemoGuidePage(int pageNumIn, String catagoryIn, String titleIn, String subtitleIn, String textIn,
			ItemStack icon) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, icon, textIn);

	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
		super.render(matrices, mouseX, mouseY, partialTicks);
		int centerX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) - width / 2;
		int centerY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) - height / 2;
		matrices.mulPose(Vector3f.XN.rotationDegrees(-45 + (int) this.dragUpDown));
		matrices.mulPose(Vector3f.YP.rotationDegrees(45 + (int) this.dragLeftRight));
		float structScale = 2f;
		matrices.scale(structScale, structScale, structScale);
		HemoGuiUtils.renderMultiBlock(matrices, BloodCraftingRecipes.living_grip_recipe.getBundledPattern(), ClientUtils.getPartialTicks(),
				new ScreenBlockTintGetter(), centerX + 150, centerY + 150);

	}

	private double xDragPos = 0;
	private double yDragPos = 0;
	private double dragLeftRight = 0;
	private double dragUpDown = 0;

	@Override
	public boolean mouseDragged(double xPos, double yPos, int button, double dragLeftRight, double dragUpDown) {
		xDragPos = xPos;
		yDragPos = yPos;
		this.dragLeftRight += dragLeftRight / 2;
		this.dragUpDown -= dragUpDown / 2;
		return super.mouseDragged(xPos, yPos, button, dragLeftRight, dragUpDown);
	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}

}
