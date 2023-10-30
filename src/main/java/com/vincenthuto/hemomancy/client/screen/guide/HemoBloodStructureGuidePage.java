package com.vincenthuto.hemomancy.client.screen.guide;

import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.client.screen.ScreenBlockTintGetter;
import com.vincenthuto.hutoslib.client.screen.guide.HLGuiGuidePage;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class HemoBloodStructureGuidePage extends HLGuiGuidePage {

	public HemoBloodStructureGuidePage(int pageNum, BookCodeModel book, ChapterTemplate chapter) {
		super(pageNum, book, chapter);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		int line = 0;

		BloodStructureRecipe structure = BloodStructureRecipe.getStructureByLocation(HLClientUtils.getWorld(),
				((BloodStructurePageTemplate) getPageTemplate()).getStructureKey());
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
			graphics.pose().pushPose();
			graphics.pose().mulPose(Vector3.XN.rotationDegrees(-45 + (float) dragUpDown).toMoj());
			graphics.pose().mulPose(Vector3.YP.rotationDegrees(45 + (float) dragLeftRight).toMoj());
			float structScale = 2f;
			graphics.pose().scale(structScale, structScale, structScale);
			HLGuiUtils.renderMultiBlock(graphics.pose(), pattern, HLClientUtils.getPartialTicks(),
					new ScreenBlockTintGetter(), left - guiWidth + 260, top + guiHeight - 65);
			graphics.pose().popPose();

			graphics.renderItem(structure.getHeldItem(), (int) (left - guiWidth + 180), (int) (top + guiHeight - 160));
			graphics.renderItem(new ItemStack(structure.getHitBlock().asItem()), (int) (left - guiWidth + 196),
					(int) (top + guiHeight - 160));
		}
	}
	
	public static void openScreenViaItem(int pNum, BookCodeModel pBook, ChapterTemplate pChapterTemplate) {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new HemoBloodStructureGuidePage(pNum, pBook, pChapterTemplate));
	}

}
