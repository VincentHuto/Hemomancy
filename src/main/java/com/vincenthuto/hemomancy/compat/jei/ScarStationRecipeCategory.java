package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI category for the Cerebral Scarring Station: the two inks, the 8x8 pattern to carve (a chisel line sweeps
 * the marked cells), and the scar with its inscription tier.
 */
public class ScarStationRecipeCategory implements IRecipeCategory<ScarRecipe> {

	private static final int W = 176;
	private static final int H = 102;
	private static final int FOOTER_Y = 86;

	private static final int INPUT_X = 14;
	private static final int INPUT1_Y = 22;
	private static final int INPUT2_Y = 46;
	private static final int OUTPUT_X = 148;
	private static final int OUTPUT_Y = 32;

	private static final int GRID_SIZE = 8;
	private static final int CELL = 8;
	private static final int GRID_X = 52;
	private static final int GRID_Y = 8;

	/** Carving needs Degree 4 (see PacketScarCraftingEvent); tier three also needs Deep Inscription. */
	private static final int CARVING_DEGREE = 4;
	private static final int DEEP_TIER = 3;

	private final IDrawable icon;

	public ScarStationRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.scar_station.get()));
	}

	@Override public IDrawable getIcon() { return icon; }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public RecipeType<ScarRecipe> getRecipeType() { return JEIPlugin.scar_station_recipe_type; }
	@Nonnull @Override public Component getTitle() { return Component.translatable("hemomancy.jei.scar_station"); }

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ScarRecipe recipe, IFocusGroup focuses) {
		List<List<ItemStack>> inputs = new ArrayList<>();
		for (Ingredient ingr : recipe.getIngredients()) {
			if (ingr != null && ingr != Ingredient.EMPTY && ingr.getItems().length > 0) {
				inputs.add(Arrays.asList(ingr.getItems()));
			}
		}
		if (inputs.size() >= 1)
			builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT1_Y).addIngredients(VanillaTypes.ITEM_STACK, inputs.get(0));
		if (inputs.size() >= 2)
			builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT2_Y).addIngredients(VanillaTypes.ITEM_STACK, inputs.get(1));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem());
	}

	private static List<JeiPanel.Stat> footer(ScarRecipe recipe) {
		JeiPanel.Theme t = JeiPanel.BLOOD;
		List<JeiPanel.Stat> stats = new ArrayList<>();
		stats.add(JeiPanel.Stat.of(JeiPanel.Icon.DEGREE, Component.translatable("hemomancy.jei.stat.degree", CARVING_DEGREE),
				t.title(), Component.translatable("hemomancy.jei.scar_station.degree.tip", CARVING_DEGREE)));
		if (recipe.getTier() >= DEEP_TIER) {
			stats.add(JeiPanel.Stat.of(JeiPanel.Icon.NONE, Component.translatable("hemomancy.jei.scar_station.deep"),
					0xFFFF7AD0, Component.translatable("skill.hemomancy.skill_deep_inscription.desc")));
		}
		return stats;
	}

	private static int tierColor(int tier) {
		return switch (tier) {
			case 1 -> 0xFFB58C8C;
			case 2 -> 0xFFD03A3A;
			case 3 -> 0xFFE8506A;
			default -> tier > 3 ? 0xFFFF7AD0 : JeiPanel.BLOOD.text();
		};
	}

	@Override
	public void draw(ScarRecipe recipe, IRecipeSlotsView slots, GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();
		int tierColor = tierColor(recipe.getTier());

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.scar_station.inks"), INPUT_X + 8, INPUT1_Y - 11, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X, INPUT1_Y);
		JeiPanel.slot(gfx, t, INPUT_X, INPUT2_Y);
		// Inks thread into the grid's edge
		int threadX = INPUT_X + 18;
		gfx.fill(threadX, INPUT1_Y + 8, GRID_X - 2, INPUT1_Y + 9, t.ring());
		gfx.fill(threadX, INPUT2_Y + 8, GRID_X - 2, INPUT2_Y + 9, t.ring());

		drawPatternGrid(gfx, t, recipe, time);

		JeiProgressArrow.draw(gfx, GRID_X + GRID_SIZE * CELL + 4, OUTPUT_Y + 7, OUTPUT_X - 6, time, t);
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, OUTPUT_Y, tierColor, time);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.scar_station.tier", recipe.getTier()),
				OUTPUT_X + 8, OUTPUT_Y + 23, tierColor);

		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	/** The 8x8 pattern: marked cells glow crimson and brighten as a chisel line sweeps down the grid. */
	private static void drawPatternGrid(GuiGraphics gfx, JeiPanel.Theme t, ScarRecipe recipe, float time) {
		byte[][] pattern = recipe.getPattern();
		if (pattern == null) return;
		int size = GRID_SIZE * CELL;
		gfx.fill(GRID_X - 2, GRID_Y - 2, GRID_X + size + 2, GRID_Y + size + 2, t.frameDark());
		JeiPanel.bevel(gfx, GRID_X - 2, GRID_Y - 2, size + 4, size + 4, t.slotDark(), t.slotLight());
		float sweep = (time * 0.35f) % 1.3f * GRID_SIZE;
		for (int i = 0; i < GRID_SIZE; i++) {
			float scan = Mth.clamp(1f - Math.abs(i + 0.5f - sweep) / 1.5f, 0f, 1f);
			for (int j = 0; j < GRID_SIZE; j++) {
				int x = GRID_X + j * CELL;
				int y = GRID_Y + i * CELL;
				gfx.fill(x, y, x + CELL, y + CELL, 0xFF1E0B0D);
				if (pattern[i][j] != 0) {
					float beat = 0.82f + 0.18f * Mth.sin(time * 3f + i * 0.4f + j * 0.3f);
					int body = JeiPanel.lerpColor(0xFF6A0A10, 0xFFE0303A, Mth.clamp(beat - 0.5f + scan * 0.6f, 0f, 1f));
					gfx.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, body);
					gfx.fill(x + 1, y + 1, x + CELL - 1, y + 2, JeiPanel.lerpColor(body, 0xFFFFC0C0, 0.35f + scan * 0.4f));
				} else {
					gfx.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, scan > 0 ? JeiPanel.lerpColor(0xFF120607, 0xFF2A0E10, scan) : 0xFF120607);
				}
			}
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ScarRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
