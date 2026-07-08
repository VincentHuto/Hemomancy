package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * JEI category for armor upgrades performed by the Hematic Armature.
 */
public class HematicArmatureRecipeCategory implements IRecipeCategory<ArmatureUpgradeRecipe> {
	private static final int BG_W = 170;
	private static final int BG_H = 88;

	private static final int BASE_X = 16;
	private static final int REAGENT_X = 76;
	private static final int OUTPUT_X = 136;
	private static final int SLOT_Y = 35;

	private static final int BG_COLOR = 0xFF090204;
	private static final int BORDER_OUTER = 0xFF3A0808;
	private static final int BORDER_INNER = 0xFF180506;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF4A1515;
	private static final int TEXT_RED = 0xFFB83A35;
	private static final int TEXT_MUTED = 0xFF93817C;
	private static final int TEXT_GOLD = 0xFFC49A55;
	private static final int RIGHT_LABEL_X = 64;
	private static final int RIGHT_LABEL_W = BG_W - RIGHT_LABEL_X - 5;
	private static final int ARMOR_MAP_X = REAGENT_X - 4;
	private static final int ARMOR_MAP_Y = SLOT_Y + 25;

	private final IDrawable background;
	private final IDrawable icon;
	private float animTime = 0f;

	public HematicArmatureRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(BG_W, BG_H);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(BlockInit.hematic_armature.get()));
	}

	@Override
	public RecipeType<ArmatureUpgradeRecipe> getRecipeType() {
		return JEIPlugin.armature_upgrade_type;
	}

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.hematic_armature");
	}

	@SuppressWarnings("removal")
	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ArmatureUpgradeRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, BASE_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getValidBase().getItems()));
		builder.addSlot(RecipeIngredientRole.INPUT, REAGENT_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.getReagent().getItems()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipeResult(recipe));
	}

	@Override
	public void draw(ArmatureUpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx,
			double mouseX, double mouseY) {
		animTime += 0.016f;
		float time = animTime;
		Font font = Minecraft.getInstance().font;

		drawBackground(gfx, time);
		drawSlot(gfx, BASE_X - 1, SLOT_Y - 1);
		drawSlot(gfx, REAGENT_X - 1, SLOT_Y - 1);
		drawSlot(gfx, OUTPUT_X - 1, SLOT_Y - 1);
		JeiProgressArrow.draw(gfx, BASE_X + 22, SLOT_Y + 7, REAGENT_X - 6, time,
				0x553A1212, 0x663A1212, 170, 42, 35);
		JeiProgressArrow.draw(gfx, REAGENT_X + 22, SLOT_Y + 7, OUTPUT_X - 6, time + 0.6f,
				0x553A1212, 0x663A1212, 170, 42, 35);
		drawBowlMap(gfx, recipe);

		Component slotText = Component.literal(recipe.getArmorSlot().name());
		gfx.drawString(font, slotText, 5, 5, TEXT_GOLD, false);

		Component degreeText = Component.literal("Degree " + recipe.getRequiredDegree());
		gfx.drawString(font, degreeText, 5, BG_H - 12, TEXT_MUTED, false);

		Component bloodText = Component.literal("Blood " + (int) recipe.getBloodCost());
		gfx.drawString(font, bloodText, BG_W - font.width(bloodText) - 5, BG_H - 12, TEXT_RED, false);

		if (recipe.getRequiredArmatureTier() != ArmatureUpgradeRules.ArmatureTier.BASE) {
			Component tierText = Component.translatable("block.hemomancy.hematic_armature.tier."
					+ recipe.getRequiredArmatureTier().serializedName());
			drawRightAlignedFitted(gfx, font, tierText, BG_W - 5, 5, RIGHT_LABEL_W, TEXT_MUTED);
		}

		if (recipe.getPersistentDataGate() != null) {
			Component gateText = Component.literal("Gate: " + recipe.getPersistentDataGate().value());
			int gateY = recipe.getRequiredArmatureTier() == ArmatureUpgradeRules.ArmatureTier.BASE ? 5 : 16;
			drawRightAlignedFitted(gfx, font, gateText, BG_W - 5, gateY, RIGHT_LABEL_W, TEXT_MUTED);
		}
	}

	private static ItemStack recipeResult(ArmatureUpgradeRecipe recipe) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			return recipe.createResult(level.registryAccess());
		}
		return recipe.getResultItem(null).copy();
	}

	private void drawBackground(GuiGraphics gfx, float time) {
		gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
		float pulse = 0.55f + 0.45f * Mth.sin(time * 1.7f);
		int centerX = REAGENT_X + 8;
		int centerY = SLOT_Y + 8;
		for (int ring = 42; ring > 0; ring -= 5) {
			float falloff = 1f - ring / 42f;
			int alpha = (int) (18 * falloff * pulse);
			gfx.fill(centerX - ring, centerY - ring / 2, centerX + ring, centerY + ring / 2,
					(alpha << 24) | (0x80 << 16) | (0x0A << 8) | 0x08);
		}
		gfx.fill(0, 0, BG_W, 1, BORDER_OUTER);
		gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_INNER);
		gfx.fill(0, 0, 1, BG_H, BORDER_OUTER);
		gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_INNER);
	}

	private void drawBowlMap(GuiGraphics gfx, ArmatureUpgradeRecipe recipe) {
		int startX = ARMOR_MAP_X;
		int startY = ARMOR_MAP_Y;
		for (int i = 0; i < 4; i++) {
			int x = startX + (i % 2) * 12;
			int y = startY + (i / 2) * 10;
			gfx.fill(x, y, x + 8, y + 6, 0xFF140708);
			gfx.fill(x + 1, y + 1, x + 7, y + 5, 0xFF2A1111);
		}

		int active = switch (recipe.getArmorSlot()) {
			case HEAD -> 0;
			case CHEST -> 1;
			case LEGS -> 2;
			case FEET -> 3;
		};
		int x = startX + (active % 2) * 12;
		int y = startY + (active / 2) * 10;
		gfx.fill(x, y, x + 8, y + 1, TEXT_RED);
		gfx.fill(x, y + 5, x + 8, y + 6, TEXT_RED);
		gfx.fill(x, y, x + 1, y + 6, TEXT_RED);
		gfx.fill(x + 7, y, x + 8, y + 6, TEXT_RED);
	}

	private void drawSlot(GuiGraphics gfx, int x, int y) {
		gfx.fill(x, y, x + 18, y + 18, SLOT_BG);
		gfx.fill(x, y, x + 18, y + 1, SLOT_BORDER_DARK);
		gfx.fill(x, y, x + 1, y + 18, SLOT_BORDER_DARK);
		gfx.fill(x, y + 17, x + 18, y + 18, SLOT_BORDER_LIGHT);
		gfx.fill(x + 17, y, x + 18, y + 18, SLOT_BORDER_LIGHT);
	}

	private void drawRightAlignedFitted(GuiGraphics gfx, Font font, Component text, int rightX, int y, int maxWidth,
			int color) {
		int width = font.width(text);
		if (width <= maxWidth) {
			gfx.drawString(font, text, rightX - width, y, color, false);
			return;
		}

		float scale = Math.max(0.55f, maxWidth / (float) width);
		gfx.pose().pushPose();
		gfx.pose().scale(scale, scale, 1.0f);
		int scaledRight = Math.round(rightX / scale);
		int scaledY = Math.round(y / scale);
		gfx.drawString(font, text, scaledRight - width, scaledY, color, false);
		gfx.pose().popPose();
	}

}
