package com.vincenthuto.hemomancy.compat.jei;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.GhastlyAlembicRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * JEI recipe category for the Ghastly Alembic — fully programmatic rendering.
 */
public class GhastlyAlembicRecipeCategory implements IRecipeCategory<GhastlyAlembicRecipe> {

	private static final int BG_W = 150;
	private static final int BG_H = 90;  // slightly taller to fit catalyst label

	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;

	private final IDrawable background;
	private final IDrawable icon;
	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

	public GhastlyAlembicRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(BG_W, BG_H);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.ghastly_alembic.get()));
		this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25).build(new CacheLoader<>() {
			@Override
			public IDrawableAnimated load(Integer cookTime) {
				return guiHelper.drawableBuilder(
						new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/ghastly_alembic_gui_overlay.png"),
						143 + 16, 14, 24, 17
				).buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
			}
		});
	}

	@Override
	public void draw(GhastlyAlembicRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX,
			double mouseY) {

		float time = System.nanoTime() / 1_000_000_000f;

		// ── Dark background ──
		gfx.fill(0, 0, BG_W, BG_H, 0xFF0A0204);
		// Subtle radial glow
		int cx = BG_W / 2;
		int cy = BG_H / 2;
		for (int ring = 40; ring > 0; ring -= 4) {
			float t = ring / 40f;
			int alpha = (int) (20 * (1f - t));
			int red = (int) (30 * (1f - t));
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
		}

		// ── Border ──
		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_OUTER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_OUTER);

		// ── Slot backgrounds ──
		drawSlot(gfx, 20, 24);   // Input
		drawSlot(gfx, 110, 24);  // Output (reddish tint)
		gfx.fill(110, 24, 126, 40, 0x20FF4444);

		// ── Optional catalyst slot (top-left) ──
		if (recipe.requiresCatalyst()) {
			drawSlot(gfx, 1, 1);
			Font font = Minecraft.getInstance().font;
			gfx.drawString(font, Component.literal("Catalyst"), 20, 3, 0xFF886644, false);
		}

		// ── Fire indicator (campfire catalyst) ──
		drawSlot(gfx, 20, 50);   // Campfire hint slot
		// Animated flame above campfire slot
		int flameX = 20;
		int flameY = 44;
		for (int row = 0; row < 6; row++) {
			float rowT = (float) row / 6;
			float flicker = 0.6f + 0.4f * Mth.sin(time * 7f + row * 0.6f);
			for (int col = 3; col < 13; col++) {
				float center = 8f;
				float dist = Math.abs(col - center) / center;
				float taper = 1f - dist * (1f - rowT * 0.5f);
				if (taper < 0.15f) continue;
				float a = rowT * taper * flicker;
				int r = (int) (255 * a);
				int g = (int) (Mth.clamp(160 * rowT * a, 0, 255));
				int px = flameX + col;
				int py = flameY - row;
				int alpha = (int) (180 * a);
				if (alpha < 5) continue;
				gfx.fill(px, py, px + 1, py + 1, (alpha << 24) | (r << 16) | (g << 8) | 0x10);
			}
		}

		// ── Progress arrow ──
		int arrowX = 44;
		int arrowY = 28;
		int arrowW = 58;
		int arrowH = 8;
		// Track
		gfx.fill(arrowX, arrowY, arrowX + arrowW, arrowY + arrowH, 0x40200808);
		gfx.fill(arrowX, arrowY + 3, arrowX + arrowW, arrowY + 5, 0x30400808);
		// Arrowhead outline — points RIGHT (narrows to tip)
		int headBaseX = arrowX + arrowW;
		int midY = arrowY + arrowH / 2;
		int headLen = 5;
		for (int i = 0; i < headLen; i++) {
			int spread = headLen - 1 - i;
			gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, 0x30600808);
		}
		// Animated fill
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) cookTime = 200;
		float period = cookTime / 20f; // seconds per cook
		float animProgress = (time % period) / period;
		int filledW = (int) (arrowW * animProgress);
		if (filledW > 0) {
			for (int col = 0; col < filledW; col++) {
				float pulse = 0.7f + 0.3f * Mth.sin(time * 4f + col * 0.15f);
				int r = (int) (180 * pulse);
				int g = (int) (20 * pulse);
				int b = (int) (15 * pulse);
				int alpha = (int) (200 * pulse);
				gfx.fill(arrowX + col, arrowY + 1, arrowX + col + 1, arrowY + arrowH - 1,
						(alpha << 24) | (r << 16) | (g << 8) | b);
			}
		}

		// ── Text ──
		drawExperience(recipe, gfx, BG_H - 20);
		drawCookTime(recipe, gfx, BG_H - 10);

		// "Requires fire below" hint
		Font font = Minecraft.getInstance().font;
		MutableComponent hint = Component.translatable("gui.hemomancy.ghastly_alembic.requires_fire");
		gfx.drawString(font, hint, 42, 56, 0xFF884400, false);
	}

	private void drawSlot(GuiGraphics gfx, int sx, int sy) {
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
	}

	protected void drawCookTime(GhastlyAlembicRecipe recipe, GuiGraphics graphics, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			MutableComponent timeString = Component.translatable("gui.jei.category.smelting.time.seconds",
					cookTimeSeconds);
			Font fontRenderer = Minecraft.getInstance().font;
			int stringWidth = fontRenderer.width(timeString);
			graphics.drawString(fontRenderer, timeString, BG_W - stringWidth - 2, y, 0xFF808080, false);
		}
	}

	protected void drawExperience(GhastlyAlembicRecipe recipe, GuiGraphics graphics, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			MutableComponent experienceString = Component.translatable("gui.jei.category.smelting.experience",
					experience);
			Font fontRenderer = Minecraft.getInstance().font;
			int stringWidth = fontRenderer.width(experienceString);
			graphics.drawString(fontRenderer, experienceString, BG_W - stringWidth - 2, y, 0xFF808080, false);
		}
	}

	@Nonnull
	@Override
	@SuppressWarnings("removal")
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public RecipeType<GhastlyAlembicRecipe> getRecipeType() {
		return JEIPlugin.ghastly_alembic_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("block.hemomancy.ghastly_alembic");
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, GhastlyAlembicRecipe recipe, IFocusGroup focuses) {
		// Input ingredient slot
		builder.addSlot(RecipeIngredientRole.INPUT, 21, 25)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getIngredient().getItems()));

		// Optional catalyst slot (top-left, slot position 2, 2 in JEI space)
		if (recipe.requiresCatalyst()) {
			builder.addSlot(RecipeIngredientRole.CATALYST, 2, 2)
					.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getCatalyst().getItems()));
		}

		// Campfire as heat hint — shows that fire below is needed
		builder.addSlot(RecipeIngredientRole.CATALYST, 21, 51)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.CAMPFIRE));

		// Output slot
		builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 25)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem(null));
	}
}

