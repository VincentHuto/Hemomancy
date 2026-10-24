package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.ItemInit;
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
 * JEI category for Living Weapon grafts: the graft rests in an Iron Brazier, the Living Staff draws it in
 * through Blood Absorption, and the staff wakes as that weapon form. Frame origins are one pixel up-left of
 * their slots.
 */
public class LivingWeaponGraftRecipeCategory implements IRecipeCategory<LivingWeaponGraftJeiRecipe> {
	private static final int W = 176;
	private static final int H = 92;
	private static final int FOOTER_Y = 76;

	private static final int STAFF_X = 15;
	private static final int BRAZIER_X = 69;
	private static final int GRAFT_X = BRAZIER_X;
	private static final int OUTPUT_X = 139;
	private static final int GRAFT_Y = 19;
	private static final int SLOT_Y = 47;

	private final IDrawable icon;

	public LivingWeaponGraftRecipeCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
				new ItemStack(ItemInit.living_weapon_graft.get()));
	}

	@Override @Nonnull public RecipeType<LivingWeaponGraftJeiRecipe> getRecipeType() {
		return JEIPlugin.living_weapon_graft_type;
	}

	@Override @Nonnull public Component getTitle() { return Component.translatable("hemomancy.jei.living_weapon_graft"); }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override @Nonnull public IDrawable getIcon() { return icon; }

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull LivingWeaponGraftJeiRecipe recipe,
			@Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, STAFF_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.livingStaff());
		builder.addSlot(RecipeIngredientRole.CATALYST, BRAZIER_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.ironBrazier());
		builder.addSlot(RecipeIngredientRole.INPUT, GRAFT_X + 1, GRAFT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.graft())
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
						"hemomancy.jei.living_weapon_graft.graft.tip").withStyle(ChatFormatting.GRAY)));
		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, SLOT_Y + 1)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.output());
	}

	private static List<JeiPanel.Stat> footer() {
		return List.of(JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.living_weapon_graft.hint"),
				JeiPanel.BLOOD.text(), Component.translatable("hemomancy.jei.living_weapon_graft.steps")));
	}

	@Override
	public void draw(@Nonnull LivingWeaponGraftJeiRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView,
			@Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();
		int brazierCx = BRAZIER_X + 9;

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.title(gfx, font, t, recipe.form().graftName(), 6, 4, W - 12, W);

		// Staff, drawing blood from the brazier across a thin thread
		JeiPanel.slot(gfx, t, STAFF_X + 1, SLOT_Y + 1);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.living_weapon_graft.staff"), STAFF_X + 9, SLOT_Y + 21, t.muted());
		int threadY = SLOT_Y + 9;
		gfx.fill(STAFF_X + 19, threadY, BRAZIER_X - 1, threadY + 1, t.ring());
		for (int i = 0; i < 3; i++) {
			float p = (time * 0.9f + i / 3f) % 1f;
			int mx = Math.round(Mth.lerp(p, BRAZIER_X - 3, STAFF_X + 20));
			gfx.fill(mx, threadY, mx + 2, threadY + 1, JeiPanel.withAlpha(0xFFE0303A, (int) (230 * (1f - p * 0.7f))));
		}

		// Graft resting over the brazier's fire
		JeiPanel.slot(gfx, t, GRAFT_X + 1, GRAFT_Y + 1);
		JeiPanel.labelLeft(gfx, font, Component.translatable("hemomancy.jei.living_weapon_graft.graft"), GRAFT_X + 22, GRAFT_Y + 6, t.muted());
		JeiPanel.glow(gfx, brazierCx, SLOT_Y + 2, 16, 10, 0xFFCA4C28, JeiPanel.pulse(time, 4f));
		JeiPanel.flame(gfx, brazierCx, SLOT_Y - 1, time, 0xAA8A1612, 0xCCCA4C28, 0xFFE8A447);
		JeiPanel.tintedSlot(gfx, t, BRAZIER_X + 1, SLOT_Y + 1, 0xFF8A1612);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.living_weapon_graft.brazier"), brazierCx, SLOT_Y + 21, t.muted());

		JeiProgressArrow.draw(gfx, BRAZIER_X + 24, SLOT_Y + 8, OUTPUT_X - 7, time, t);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.living_weapon_graft.result"), OUTPUT_X + 9, SLOT_Y - 12, t.muted());
		JeiPanel.resultSlot(gfx, t, OUTPUT_X + 1, SLOT_Y + 1, t.accent(), time);

		JeiPanel.footer(gfx, font, footer(), W, FOOTER_Y);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, LivingWeaponGraftJeiRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(), W, FOOTER_Y, H, mouseX, mouseY);
	}
}
