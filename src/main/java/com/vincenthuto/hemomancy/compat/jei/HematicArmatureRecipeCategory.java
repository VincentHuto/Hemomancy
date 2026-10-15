package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * JEI category for armor upgrades performed by the Hematic Armature: the worn piece and its bowl reagent
 * become the upgrade, with the armature stage it needs shown as that stage's kit.
 */
public class HematicArmatureRecipeCategory implements IRecipeCategory<ArmatureUpgradeRecipe> {
	private static final int W = 176;
	private static final int H = 92;
	private static final int FOOTER_Y = 76;
	private static final int SLOT_Y = 34;
	private static final int BASE_X = 12;
	private static final int REAGENT_X = 42;
	private static final int OUTPUT_X = 114;
	private static final int TIER_X = 148;

	private final IDrawable icon;

	public HematicArmatureRecipeCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.hematic_armature.get()));
	}

	@Override public RecipeType<ArmatureUpgradeRecipe> getRecipeType() { return JEIPlugin.armature_upgrade_type; }
	@Nonnull @Override public Component getTitle() { return Component.translatable("hemomancy.jei.hematic_armature"); }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Nonnull @Override public IDrawable getIcon() { return icon; }

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ArmatureUpgradeRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, BASE_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getValidBase().getItems()));
		builder.addSlot(RecipeIngredientRole.INPUT, REAGENT_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getReagent().getItems()))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.armature.reagent.tip").withStyle(ChatFormatting.GRAY)));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipeResult(recipe));
		ArmatureUpgradeRules.ArmatureTier tier = recipe.getRequiredArmatureTier();
		builder.addSlot(RecipeIngredientRole.CATALYST, TIER_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, tierItem(tier))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.armature.tier." + tier.serializedName() + ".desc").withStyle(ChatFormatting.GRAY)));
	}

	private static ItemStack tierItem(ArmatureUpgradeRules.ArmatureTier tier) {
		return switch (tier) {
			case VICAR_CONSECRATED -> new ItemStack(ItemInit.vicars_consecration_kit.get());
			case MONOLITHIC -> new ItemStack(ItemInit.monolithic_cornerstone.get());
			default -> new ItemStack(BlockInit.hematic_armature.get());
		};
	}

	private static ItemStack recipeResult(ArmatureUpgradeRecipe recipe) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			return recipe.createResult(level.registryAccess());
		}
		return recipe.getResultItem(null).copy();
	}

	private static List<JeiPanel.Stat> footer(ArmatureUpgradeRecipe recipe) {
		JeiPanel.Theme t = JeiPanel.BLOOD;
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.DEGREE, Component.translatable("hemomancy.jei.stat.degree",
						recipe.getRequiredDegree()), t.title(),
						Component.translatable("hemomancy.jei.craft.degree_full", recipe.getRequiredDegree())),
				JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.stat.blood",
						(int) recipe.getBloodCost()), JeiPanel.BLOOD_RED,
						Component.translatable("hemomancy.jei.armature.blood.tip")));
	}

	@Override
	public void draw(ArmatureUpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();
		String slotName = recipe.getArmorSlot().name().toLowerCase(Locale.ROOT);

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		drawDoll(gfx, t, 6, 3, switch (slotName) {
			case "head" -> 0;
			case "chest" -> 1;
			case "legs" -> 2;
			default -> 3;
		});
		JeiPanel.title(gfx, font, t, Component.translatable("hemomancy.jei.armature.slot." + slotName), 17, 5, W - 23, W);

		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.armature.worn"), BASE_X + 8, SLOT_Y - 11, t.muted());
		JeiPanel.slot(gfx, t, BASE_X, SLOT_Y);
		JeiPanel.plus(gfx, (BASE_X + 16 + REAGENT_X) / 2, SLOT_Y + 8, t.text());
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.armature.reagent"), REAGENT_X + 8, SLOT_Y - 11, t.muted());
		JeiPanel.slot(gfx, t, REAGENT_X, SLOT_Y);

		JeiProgressArrow.draw(gfx, REAGENT_X + 22, SLOT_Y + 7, OUTPUT_X - 8, time, t);
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, t.accent(), time);

		ArmatureUpgradeRules.ArmatureTier tier = recipe.getRequiredArmatureTier();
		JeiPanel.slot(gfx, t, TIER_X, SLOT_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.armature.tier." + tier.serializedName()),
				TIER_X + 8, SLOT_Y + 20, tierColor(tier), 2 * (W - TIER_X - 8) - 4);

		if (recipe.getPersistentDataGate() != null) {
			String value = recipe.getPersistentDataGate().value();
			Component gate = Component.translatable("hemomancy.jei.armature.gate",
					Character.toUpperCase(value.charAt(0)) + value.substring(1));
			JeiPanel.labelLeft(gfx, font, gate, BASE_X - 2, SLOT_Y + 24, 0xFFC49A55);
		}

		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	private static int tierColor(ArmatureUpgradeRules.ArmatureTier tier) {
		return switch (tier) {
			case VICAR_CONSECRATED -> 0xFFE8506A;
			case MONOLITHIC -> 0xFFFF7AD0;
			default -> JeiPanel.BLOOD.text();
		};
	}

	/** A tiny figure with the upgraded piece lit: head, chest, legs, feet top to bottom. */
	private static void drawDoll(GuiGraphics gfx, JeiPanel.Theme t, int x, int y, int lit) {
		int[][] parts = { { 3, 0, 3, 3 }, { 1, 3, 7, 4 }, { 2, 7, 5, 3 }, { 1, 10, 7, 2 } };
		for (int i = 0; i < parts.length; i++) {
			int[] p = parts[i];
			gfx.fill(x + p[0], y + p[1], x + p[0] + p[2], y + p[1] + p[3], i == lit ? t.accent() : t.slotLight());
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ArmatureUpgradeRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
