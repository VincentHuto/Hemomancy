package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MorphlingIncubatorBlockEntity;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI category for the Morphling Incubator: a polyp in the centre socket, up to four catalysts at the cardinal
 * points feeding it, and the same crimson gestation ring the incubator screen shows.
 */
public class IncubatorRecipeCategory implements IRecipeCategory<IncubatorRecipe> {

	private static final int W = 176;
	private static final int H = 110;
	private static final int FOOTER_Y = 94;

	private static final int CENTER_X = 54;
	private static final int CENTER_Y = 44;
	private static final int[][] CATALYST_OFFSETS = { { 0, -30 }, { 30, 0 }, { 0, 30 }, { -30, 0 } };
	private static final int OUTPUT_X = 146;
	private static final int OUTPUT_Y = CENTER_Y;

	// Gestation ring — mirrors MorphlingIncubatorScreen.renderProgressRing
	private static final int RING_OUTER_RADIUS = 24;
	private static final int RING_INNER_RADIUS = 20;
	private static final int RING_SEGMENTS = 64;
	private static final float RING_PERIOD = 10f;

	private final IDrawable icon;

	public IncubatorRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.morphling_incubator.get()));
	}

	@Nonnull @Override public IDrawable getIcon() { return icon; }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public RecipeType<IncubatorRecipe> getRecipeType() { return JEIPlugin.incubator_recipe_type; }
	@Nonnull @Override public Component getTitle() { return Component.translatable("hemomancy.jei.morphling_incubator"); }

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IncubatorRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X, CENTER_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.morphling_polyp.get()))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.incubator.polyp.tip").withStyle(ChatFormatting.GRAY)));

		var catalysts = recipe.getCatalysts();
		for (int i = 0; i < catalysts.size() && i < 4; i++) {
			builder.addSlot(RecipeIngredientRole.INPUT, CENTER_X + CATALYST_OFFSETS[i][0], CENTER_Y + CATALYST_OFFSETS[i][1])
					.addIngredients(catalysts.get(i));
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItemStack().copy());
	}

	private static List<JeiPanel.Stat> footer() {
		int blood = Math.round(MorphlingIncubatorBlockEntity.CRAFT_TIME * MorphlingIncubatorBlockEntity.BLOOD_COST_PER_TICK);
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.stat.blood", blood),
						JeiPanel.BLOOD_RED, Component.translatable("hemomancy.jei.incubator.blood.tip")),
				JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(MorphlingIncubatorBlockEntity.CRAFT_TIME),
						JeiPanel.BLOOD.text(), Component.translatable("hemomancy.jei.incubator.time.tip")));
	}

	@Override
	public void draw(IncubatorRecipe recipe, IRecipeSlotsView slots, GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();
		int cx = CENTER_X + 8, cy = CENTER_Y + 8;

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.glow(gfx, cx, cy, 36, 36, 0xFF7A0A10, 0.5f + 0.5f * JeiPanel.pulse(time, 1.4f));

		drawRing(gfx, cx, cy, time);
		int catalystCount = recipe.getCatalysts().size();
		for (int i = 0; i < 4; i++) {
			int sx = CENTER_X + CATALYST_OFFSETS[i][0];
			int sy = CENTER_Y + CATALYST_OFFSETS[i][1];
			if (i < catalystCount) {
				drawFeed(gfx, sx + 8, sy + 8, cx, cy, time, i);
				JeiPanel.slot(gfx, t, sx, sy);
			} else {
				JeiPanel.ghostSlot(gfx, t, sx, sy);
			}
		}

		JeiPanel.tintedSlot(gfx, t, CENTER_X, CENTER_Y, 0xFF8A1A22);

		JeiProgressArrow.draw(gfx, CENTER_X + 48, OUTPUT_Y + 7, OUTPUT_X - 8, time, t);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.incubator.morphling"), OUTPUT_X + 8, OUTPUT_Y - 13, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, OUTPUT_Y, t.accent(), time);

		JeiPanel.footer(gfx, font, footer(), W, FOOTER_Y);
	}

	/** The incubator's gestation ring, cycling on a timer since JEI has no real progress. */
	private static void drawRing(GuiGraphics gfx, int cx, int cy, float time) {
		float progress = (time % RING_PERIOD) / RING_PERIOD;
		int filled = (int) (RING_SEGMENTS * progress);
		for (int i = 0; i < RING_SEGMENTS; i++) {
			double a0 = -Math.PI / 2 + (2 * Math.PI / RING_SEGMENTS) * i;
			double a1 = a0 + 2 * Math.PI / RING_SEGMENTS;
			int color;
			if (i < filled) {
				float pulse = 0.7f + 0.3f * Mth.sin(time * 3f + i * 0.1f);
				color = (0xDD << 24) | ((int) (200 * pulse) << 16) | ((int) (20 * pulse) << 8) | 0x15;
			} else {
				color = 0x40300A0C;
			}
			for (int r = RING_INNER_RADIUS; r <= RING_OUTER_RADIUS; r++) {
				for (double a = a0; a < a1; a += 0.04) {
					int px = cx + (int) (r * Math.cos(a));
					int py = cy + (int) (r * Math.sin(a));
					gfx.fill(px, py, px + 1, py + 1, color);
				}
			}
		}
		if (filled > 0 && filled < RING_SEGMENTS) {
			double lead = -Math.PI / 2 + (2 * Math.PI / RING_SEGMENTS) * filled;
			int r = (RING_INNER_RADIUS + RING_OUTER_RADIUS) / 2;
			int lx = cx + (int) (r * Math.cos(lead)), ly = cy + (int) (r * Math.sin(lead));
			gfx.fill(lx - 1, ly - 1, lx + 2, ly + 2, JeiPanel.withAlpha(0xFFFF3020, (int) (120 + 120 * JeiPanel.pulse(time, 5f))));
		}
	}

	/** A catalyst's thread into the polyp, with a mote running inward. */
	private static void drawFeed(GuiGraphics gfx, int x0, int y0, int x1, int y1, float time, int index) {
		JeiPanel.line(gfx, x0, y0, x1, y1, JeiPanel.BLOOD.ring());
		float p = (time * 0.8f + index * 0.25f) % 1f;
		int mx = Math.round(Mth.lerp(p, x0, x1)), my = Math.round(Mth.lerp(p, y0, y1));
		gfx.fill(mx - 1, my - 1, mx + 1, my + 1, JeiPanel.withAlpha(0xFFE0303A, (int) (230 * (1f - p * 0.6f))));
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, IncubatorRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
