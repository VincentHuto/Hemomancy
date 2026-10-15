package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
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
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * JEI recipe category for the Somatic Loom. The engram is drawn as the loom's octagram: each required tendency is a
 * lit spoke ending in a real enzyme slot (count = fragments), unused tendencies stay as dim pips. The right column
 * names the woven result's rank and the loom stage that can hold it.
 */
public class MemoryWeavingRecipeCategory implements IRecipeCategory<MemoryWeavingRecipe> {

	private static final int W = 176;
	private static final int H = 116;

	private static final int WHEEL_X = 84;
	private static final int WHEEL_Y = 47;
	private static final int WHEEL_RADIUS = 30;
	private static final int HUB_RADIUS = 7;

	private static final int MEMORY_X = 7;
	private static final int MEMORY_Y = 25;
	private static final int CATALYST_X = 7;
	private static final int CATALYST_Y = 50;
	private static final int OUTPUT_X = 146;
	private static final int OUTPUT_Y = 39;
	private static final int LOOM_X = 146;
	private static final int LOOM_Y = 72;
	private static final int FOOTER_Y = 100;

	private final IDrawable icon;

	public MemoryWeavingRecipeCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.somatic_loom.get()));
	}

	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public IDrawable getIcon() { return icon; }
	@Override public RecipeType<MemoryWeavingRecipe> getRecipeType() { return JEIPlugin.memory_weaving_type; }

	@Nonnull
	@Override
	public Component getTitle() {
		return Component.translatable("hemomancy.jei.memory_weaving");
	}

	// ── Layout ──

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MemoryWeavingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, MEMORY_X, MEMORY_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.hematic_memory.get()));

		int catalystIndex = 0;
		for (Ingredient ingr : recipe.getCatalysts()) {
			if (ingr == null || ingr.isEmpty()) continue;
			builder.addSlot(RecipeIngredientRole.INPUT, CATALYST_X, CATALYST_Y + catalystIndex * 20)
					.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(ingr.getItems()));
			catalystIndex++;
		}

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int amount = recipe.getEnzymeRequirement(tend);
			if (amount <= 0) continue;
			int[] at = spokeEnd(tend, WHEEL_RADIUS);
			builder.addSlot(RecipeIngredientRole.INPUT, at[0] - 8, at[1] - 8)
					.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(EnumBloodTendency.getRepEnzyme(tend), amount))
					.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
							"hemomancy.jei.memory_weaving.fragments_of", amount).withStyle(ChatFormatting.GRAY)));
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem(null));

		int stage = SomaticWeaveRules.stageFor(rankOf(recipe));
		builder.addSlot(RecipeIngredientRole.CATALYST, LOOM_X, LOOM_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, loomItem(stage))
				.addRichTooltipCallback((view, tooltip) -> {
					tooltip.add(Component.translatable("hemomancy.jei.memory_weaving.stage." + stage + ".desc")
							.withStyle(ChatFormatting.GRAY));
				});
	}

	// ── Drawing ──

	private static List<JeiPanel.Stat> footer(MemoryWeavingRecipe recipe) {
		int fragments = recipe.getTotalEnzymeRequirement();
		return List.of(
				JeiPanel.Stat.of(JeiPanel.Icon.BLOOD,
						Component.translatable("hemomancy.jei.memory_weaving.blood", (int) recipe.getBloodCost()),
						JeiPanel.BLOOD_RED, Component.translatable("hemomancy.jei.memory_weaving.blood.tooltip")),
				JeiPanel.Stat.of(JeiPanel.Icon.FRAGMENT, Component.translatable(fragments == 1
								? "hemomancy.jei.memory_weaving.fragment" : "hemomancy.jei.memory_weaving.fragments", fragments),
						JeiPanel.BLOOD.text(), Component.translatable("hemomancy.jei.memory_weaving.fragments.tooltip")));
	}

	@Override
	public void draw(MemoryWeavingRecipe recipe, IRecipeSlotsView slots, GuiGraphics gfx, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		float time = JeiPanel.time();
		EnumManipulationRank rank = rankOf(recipe);
		int stage = SomaticWeaveRules.stageFor(rank);
		JeiPanel.Theme t = JeiPanel.BLOOD;

		JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.memory_weaving.offer"), MEMORY_X + 8, MEMORY_Y - 9, t.muted());
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.memory_weaving.woven"), OUTPUT_X + 8, OUTPUT_Y - 12, t.muted());

		// Input column: memory + catalyst joined by a thread
		JeiPanel.slot(gfx, t, MEMORY_X, MEMORY_Y);
		int catalysts = (int) recipe.getCatalysts().stream().filter(i -> i != null && !i.isEmpty()).count();
		for (int i = 0; i < catalysts; i++) JeiPanel.slot(gfx, t, CATALYST_X, CATALYST_Y + i * 20);
		if (catalysts > 0) {
			gfx.fill(MEMORY_X + 7, MEMORY_Y + 17, MEMORY_X + 9, CATALYST_Y - 1, t.ring());
			JeiPanel.plus(gfx, MEMORY_X + 8, MEMORY_Y + 20, t.text());
		}

		drawEngram(gfx, recipe, time);

		// Flow: wheel -> output
		int arrowY = OUTPUT_Y + 8;
		float flow = (time * 0.8f) % 1f;
		for (int i = 0; i < 3; i++) {
			float lit = 1f - Math.abs(((i / 3f) - flow + 1f) % 1f - 0.5f) * 2f;
			chevron(gfx, 124 + i * 5, arrowY, JeiPanel.lerpColor(t.ring(), t.arrowLit(), Mth.clamp(lit, 0f, 1f)));
		}

		int rankColor = rankColor(rank);
		JeiPanel.resultSlot(gfx, t, OUTPUT_X, OUTPUT_Y, rankColor, time);
		if (isMemory(recipe))
			JeiPanel.label(gfx, font, rankName(rank), OUTPUT_X + 8, OUTPUT_Y + 23, rankColor);

		JeiPanel.slot(gfx, t, LOOM_X, LOOM_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.memory_weaving.stage." + stage),
				LOOM_X + 8, LOOM_Y + 19, stage == 0 ? t.text() : stageColor(stage), 2 * (W - LOOM_X - 8) - 4);

		JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, MemoryWeavingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
		JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
	}

	/** The octagram: a faint ring through all eight tendencies, lit spokes and moving motes for the required ones. */
	private void drawEngram(GuiGraphics gfx, MemoryWeavingRecipe recipe, float time) {
		EnumBloodTendency[] tends = EnumBloodTendency.values();

		// Faint outer octagon + inner star (skip-two chords), the loom's own figure
		for (int i = 0; i < tends.length; i++) {
			int[] a = spokeEnd(tends[i], WHEEL_RADIUS);
			int[] b = spokeEnd(tends[(i + 1) % tends.length], WHEEL_RADIUS);
			int[] c = spokeEnd(tends[(i + 3) % tends.length], WHEEL_RADIUS - 10);
			int[] d = spokeEnd(tends[i], WHEEL_RADIUS - 10);
			JeiPanel.line(gfx, a[0], a[1], b[0], b[1], JeiPanel.BLOOD.ring());
			JeiPanel.line(gfx, d[0], d[1], c[0], c[1], 0xFF240709);
		}

		for (EnumBloodTendency tend : tends) {
			boolean required = recipe.getEnzymeRequirement(tend) > 0;
			int color = JeiPanel.brighten(tend.getColor().getColor());
			int[] inner = spokeEnd(tend, HUB_RADIUS);
			if (required) {
				int[] outer = spokeEnd(tend, WHEEL_RADIUS - 10);
				JeiPanel.line(gfx, inner[0], inner[1], outer[0], outer[1], color);
				// a mote travelling from the slot down into the hub
				float t = (time * 0.7f + tend.ordinal() * 0.13f) % 1f;
				int mx = Math.round(Mth.lerp(t, outer[0], inner[0]));
				int my = Math.round(Mth.lerp(t, outer[1], inner[1]));
				gfx.fill(mx - 1, my - 1, mx + 1, my + 1, 0xFFFFFFFF);
				int[] at = spokeEnd(tend, WHEEL_RADIUS);
				JeiPanel.tintedSlot(gfx, JeiPanel.BLOOD, at[0] - 8, at[1] - 8, color);
			} else {
				int[] at = spokeEnd(tend, WHEEL_RADIUS);
				JeiPanel.diamond(gfx, at[0], at[1], 2, JeiPanel.darken(color, 0.45f));
			}
		}

		// Hub: a pulsing heart-knot
		float pulse = JeiPanel.pulse(time, 3f);
		gfx.fill(WHEEL_X - HUB_RADIUS, WHEEL_Y - HUB_RADIUS, WHEEL_X + HUB_RADIUS, WHEEL_Y + HUB_RADIUS,
				JeiPanel.withAlpha(0xFF8A0E16, (int) (30 + 40 * pulse)));
		JeiPanel.diamond(gfx, WHEEL_X, WHEEL_Y, 5, 0xFF5A0A10);
		JeiPanel.diamond(gfx, WHEEL_X, WHEEL_Y, 3, JeiPanel.lerpColor(0xFF9A1018, 0xFFFF4A50, pulse));
		gfx.fill(WHEEL_X, WHEEL_Y, WHEEL_X + 1, WHEEL_Y + 1, 0xFFFFE0E0);
	}

	// ── Recipe facts ──

	private static boolean isMemory(MemoryWeavingRecipe recipe) {
		return recipe.getResultItem(null).getItem() instanceof BloodMemoryItem memory && memory.getManip() != null;
	}

	/** Mirrors the loom: non-memory results (effigies, formations) weave as Humilis. */
	private static EnumManipulationRank rankOf(MemoryWeavingRecipe recipe) {
		return recipe.getResultItem(null).getItem() instanceof BloodMemoryItem memory && memory.getManip() != null
				? memory.getManip().getRank() : EnumManipulationRank.HUMILIS;
	}

	private static ItemStack loomItem(int stage) {
		return switch (stage) {
			case 1 -> new ItemStack(ItemInit.ganglionic_harness.get());
			case 2 -> new ItemStack(ItemInit.cortical_crown.get());
			default -> new ItemStack(BlockInit.somatic_loom.get());
		};
	}

	private static Component rankName(EnumManipulationRank rank) {
		String name = rank.name().toLowerCase(Locale.ROOT);
		return Component.translatableWithFallback("hemomancy.jei.memory_weaving.rank." + name,
				Character.toUpperCase(name.charAt(0)) + name.substring(1));
	}

	private static int rankColor(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> 0xFF8C7A7A;
			case MEDIOCRITAS -> 0xFFB58C8C;
			case SUMMA -> 0xFFD03A3A;
			case MAGISTER -> 0xFFE8506A;
			case PERFECTUS -> 0xFFFF7AD0;
		};
	}

	private static int stageColor(int stage) {
		return stage == 1 ? 0xFFE8506A : 0xFFFF7AD0;
	}

	// ── Geometry ──

	/** Spoke endpoint for a tendency; ANIMUS points straight up and the rest follow clockwise in 45° steps. */
	private static int[] spokeEnd(EnumBloodTendency tend, int radius) {
		double rad = Math.toRadians(-90 + tend.ordinal() * 45);
		return new int[] { WHEEL_X + (int) Math.round(Math.cos(rad) * radius), WHEEL_Y + (int) Math.round(Math.sin(rad) * radius) };
	}

	private static void chevron(GuiGraphics gfx, int x, int cy, int color) {
		for (int i = 0; i < 4; i++) {
			gfx.fill(x + i, cy - 3 + i, x + i + 2, cy - 2 + i, color);
			gfx.fill(x + i, cy + 3 - i, x + i + 2, cy + 4 - i, color);
		}
	}

}
