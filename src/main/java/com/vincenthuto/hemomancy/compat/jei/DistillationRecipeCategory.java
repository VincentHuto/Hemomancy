package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JEI category for the Ghastly Alembic and the Pallid Retort. Reads left to right: the charge and its catalyst,
 * the still over its fire with a falling drip, then the distillate and the vessel it is drawn into. The footer
 * carries blood or white humor, experience and time.
 */
public class DistillationRecipeCategory implements IRecipeCategory<DistillationRecipe> {

	private static final int W = 176;
	private static final int H = 86;
	private static final int FOOTER_Y = 70;

	private static final int INPUT_X = 12;
	private static final int CATALYST_X = 42;
	private static final int SLOT_Y = 24;
	private static final int HEAT_X = 80;
	private static final int HEAT_Y = 44;
	private static final int OUTPUT_X = 132;
	private static final int VESSEL_X = 156;

	private final IDrawable icon;
	private final boolean pallid;

	public DistillationRecipeCategory(IGuiHelper guiHelper, boolean pallid) {
		this.pallid = pallid;
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(pallid ? BlockInit.pallid_retort.get() : BlockInit.ghastly_alembic.get()));
	}

	private JeiPanel.Theme theme() {
		return pallid ? JeiPanel.STILL : JeiPanel.BLOOD;
	}

	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Nonnull @Override public IDrawable getIcon() { return icon; }

	@Override
	public RecipeType<DistillationRecipe> getRecipeType() {
		return pallid ? JEIPlugin.pallid_distillation_recipe_type : JEIPlugin.ghastly_distillation_recipe_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable(pallid ? "block.hemomancy.pallid_retort" : "block.hemomancy.ghastly_alembic");
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DistillationRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getIngredient().getItems()));

		if (recipe.requiresCatalyst()) {
			boolean consumed = recipe.consumesCatalyst();
			builder.addSlot(consumed ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST, CATALYST_X, SLOT_Y)
					.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getCatalyst().getItems()))
					.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(consumed
							? "hemomancy.jei.distillation.catalyst.consumed" : "hemomancy.jei.distillation.catalyst.kept")
							.withStyle(ChatFormatting.GRAY)));
		}

		var heat = builder.addSlot(RecipeIngredientRole.CATALYST, HEAT_X, HEAT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.CAMPFIRE))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"gui.hemomancy.ghastly_alembic.requires_fire").withStyle(ChatFormatting.GRAY)));
		if (!pallid) heat.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
				"hemomancy.jei.alembic.athanor_hint").withStyle(ChatFormatting.DARK_RED)));

		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem(null));

		if (recipe.requiresVesselInput()) {
			builder.addSlot(RecipeIngredientRole.INPUT, VESSEL_X, SLOT_Y)
					.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getVesselInput().getItems()))
					.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
							"hemomancy.jei.distillation.vessel.tip").withStyle(ChatFormatting.GRAY)));
		}
	}

	private List<JeiPanel.Stat> footer(DistillationRecipe recipe) {
		JeiPanel.Theme t = theme();
		List<JeiPanel.Stat> stats = new ArrayList<>();
		if (recipe.getBloodCost() > 0) {
			stats.add(JeiPanel.Stat.of(JeiPanel.Icon.BLOOD,
					Component.translatable("hemomancy.jei.stat.blood", recipe.getBloodCost()), JeiPanel.BLOOD_RED,
					Component.translatable("hemomancy.jei.distillation.blood.tip")));
		}
		if (pallid) {
			int humor = recipe.getWhiteHumorCost();
			stats.add(humor > 0
					? JeiPanel.Stat.of(JeiPanel.Icon.HUMOR, Component.translatable("hemomancy.jei.stat.humor", humor),
							t.accent(), Component.translatable("hemomancy.jei.distillation.humor.cost.tip"))
					: JeiPanel.Stat.of(JeiPanel.Icon.HUMOR, Component.translatable("hemomancy.jei.distillation.humor.yield"),
							t.accent(), Component.translatable("hemomancy.jei.distillation.humor.yield.tip")));
		}
		if (recipe.getExperience() > 0) {
			stats.add(JeiPanel.Stat.of(JeiPanel.Icon.XP,
					Component.translatable("hemomancy.jei.stat.xp", formatXp(recipe.getExperience())), 0xFFB8E070));
		}
		if (recipe.getCookingTime() > 0) {
			stats.add(JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(recipe.getCookingTime()), t.text(),
					pallid ? null : Component.translatable("hemomancy.jei.alembic.athanor_hint")));
		}
		return stats;
	}

	private static String formatXp(float xp) {
		return xp == Math.floor(xp) ? String.valueOf((int) xp) : String.format("%.1f", xp);
	}

	@Override
	public void draw(DistillationRecipe recipe, IRecipeSlotsView slots, GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = theme();
		float time = JeiPanel.time();

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);

		// Charge + catalyst
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.charge"), INPUT_X + 8, SLOT_Y - 11, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X, SLOT_Y);
		if (recipe.requiresCatalyst()) {
			JeiPanel.plus(gfx, (INPUT_X + 16 + CATALYST_X) / 2, SLOT_Y + 8, t.text());
			JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.catalyst"), CATALYST_X + 8, SLOT_Y - 11, t.muted());
			JeiPanel.slot(gfx, t, CATALYST_X, SLOT_Y);
		}

		// The still: the arrow runs over its fire
		int arrowStart = (recipe.requiresCatalyst() ? CATALYST_X : INPUT_X) + 22;
		JeiProgressArrow.draw(gfx, arrowStart, SLOT_Y + 7, OUTPUT_X - 8, time, t);
		int heat = pallid ? 0xFF8AC4E4 : 0xFFE8743A;
		JeiPanel.glow(gfx, HEAT_X + 8, HEAT_Y + 4, 18, 10, heat, JeiPanel.pulse(time, 3f));
		JeiPanel.shimmer(gfx, HEAT_X + 8, HEAT_Y - 2, 9, time, heat);
		JeiPanel.slot(gfx, t, HEAT_X, HEAT_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.heat"), HEAT_X + 8, HEAT_Y + 19, t.muted());

		// Distillate + vessel
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.distillate"), OUTPUT_X + 8, SLOT_Y - 13, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, t.accent(), time);
		if (recipe.requiresVesselInput()) {
			JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.vessel"), VESSEL_X + 8, SLOT_Y + 23, t.muted());
			JeiPanel.slot(gfx, t, VESSEL_X, SLOT_Y);
		}

		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, DistillationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
