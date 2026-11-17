package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.world.ground.GougeCatalysts;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEI category for gouge catalysts (core-loop Track 6, shown in round two P8): one page per function a lit run can
 * have. Projection into a gouge with one of the listed items in the off hand dissolves it into the run, which stays lit
 * for that item's time and does what the function does. The table is data ({@link GougeCatalysts#listed}); a remote
 * client's mirror can arrive after JEI starts, so {@link #runtime} rebuilds the pages when it does.
 */
public final class GougeCatalystRecipeCategory implements IRecipeCategory<GougeCatalystRecipeCategory.Display> {
	public static final RecipeType<Display> JEI_TYPE = RecipeType.create(Hemomancy.MOD_ID, "gouge_catalyst", Display.class);

	private static final int W = 176;
	private static final int H = 80;
	private static final int SLOT_Y = 18;
	private static final int HAND_X = 14;
	private static final int INPUT_X = 60;
	private static final int SWATCH_X = 122;
	private static final int TEXT_Y = 50;

	/** One function and every item that gives it, with the time each lights the run for. */
	public record Display(GroundFunction function, List<GougeCatalysts.Entry> entries) {
		List<ItemStack> stacks() {
			List<ItemStack> stacks = new ArrayList<>();
			for (GougeCatalysts.Entry entry : entries) {
				Item item = BuiltInRegistries.ITEM.get(entry.item());
				if (item != Items.AIR) stacks.add(new ItemStack(item));
			}
			return stacks;
		}

		int ticksFor(ItemStack stack) {
			ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
			for (GougeCatalysts.Entry entry : entries) {
				if (entry.item().equals(id)) return entry.ticks();
			}
			return 0;
		}
	}

	private static IJeiRuntime runtime;
	private static final List<Display> shown = new ArrayList<>();

	private final IDrawable icon;

	public GougeCatalystRecipeCategory(IGuiHelper guiHelper) {
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.catalyst_feeder.get()));
	}

	/** Every function with at least one catalyst, in the function's order (the bare run has none). */
	public static List<Display> displays() {
		Map<GroundFunction, List<GougeCatalysts.Entry>> listed = GougeCatalysts.listed();
		List<Display> displays = new ArrayList<>();
		for (GroundFunction function : GroundFunction.values()) {
			List<GougeCatalysts.Entry> entries = listed.get(function);
			if (function != GroundFunction.NONE && entries != null && !entries.isEmpty()) displays.add(new Display(function, entries));
		}
		return displays;
	}

	/** The displays JEI was given at registration; kept so a later mirror can replace them. */
	static List<Display> registered() {
		List<Display> displays = displays();
		shown.clear();
		shown.addAll(displays);
		return displays;
	}

	/**
	 * JEI's runtime came or went. While it is up, a catalyst table mirrored from a remote server after JEI started
	 * replaces the pages built from the empty table.
	 */
	static void runtime(IJeiRuntime value) {
		runtime = value;
		GougeCatalysts.onClientTable = value == null ? null : () -> Minecraft.getInstance().execute(GougeCatalystRecipeCategory::rebuild);
	}

	private static void rebuild() {
		IJeiRuntime current = runtime;
		if (current == null) return;
		List<Display> fresh = displays();
		// Only what changed: JEI hides by equality, so a page hidden and added again would stay hidden.
		List<Display> gone = shown.stream().filter(display -> !fresh.contains(display)).toList();
		List<Display> added = fresh.stream().filter(display -> !shown.contains(display)).toList();
		if (!gone.isEmpty()) current.getRecipeManager().hideRecipes(JEI_TYPE, gone);
		if (!added.isEmpty()) current.getRecipeManager().addRecipes(JEI_TYPE, added);
		shown.clear();
		shown.addAll(fresh);
	}

	@Override public RecipeType<Display> getRecipeType() { return JEI_TYPE; }
	@Override public Component getTitle() { return Component.translatable("hemomancy.jei.gouge_catalyst"); }
	@Override public int getWidth() { return W; }
	@Override public int getHeight() { return H; }
	@Override public IDrawable getIcon() { return icon; }

	@Override
	public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull Display display, @Nonnull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.CATALYST, HAND_X, SLOT_Y)
				.addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemInit.blood_projection.get()))
				.addRichTooltipCallback((view, tip) -> tip.add(Component.translatable("hemomancy.jei.gouge_catalyst.projection.tip")
						.withStyle(ChatFormatting.GRAY)));
		Map<Item, Integer> times = new HashMap<>();
		for (ItemStack stack : display.stacks()) times.put(stack.getItem(), display.ticksFor(stack));
		builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
				.addIngredients(VanillaTypes.ITEM_STACK, display.stacks())
				.addRichTooltipCallback((view, tip) -> view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack ->
						tip.add(Component.translatable("hemomancy.jei.gouge_catalyst.time",
								clock(times.getOrDefault(stack.getItem(), 0))).withStyle(ChatFormatting.GRAY))));
	}

	/** Ticks as m:ss. */
	static String clock(int ticks) {
		int seconds = Math.max(0, ticks) / 20;
		return (seconds / 60) + ":" + String.format(java.util.Locale.ROOT, "%02d", seconds % 60);
	}

	@Override
	public void draw(@Nonnull Display display, @Nonnull IRecipeSlotsView slots, @Nonnull GuiGraphics gfx,
			double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		JeiPanel.Theme t = JeiPanel.BLOOD;
		float time = JeiPanel.time();
		JeiPanel.panel(gfx, t, W, H, 0);
		JeiPanel.tintedSlot(gfx, t, HAND_X, SLOT_Y, 0xFFB01A22);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.gouge_catalyst.projected"), HAND_X + 8, SLOT_Y + 21, t.muted());
		JeiPanel.slot(gfx, t, INPUT_X, SLOT_Y);
		JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.gouge_catalyst.off_hand"), INPUT_X + 8, SLOT_Y + 21, t.muted());
		JeiProgressArrow.draw(gfx, INPUT_X + 24, SLOT_Y + 7, SWATCH_X - 8, time, t);
		swatch(gfx, display.function(), SWATCH_X, SLOT_Y, time);
		Component name = Component.translatable("ground_function.hemomancy." + display.function().getSerializedName());
		JeiPanel.label(gfx, font, name, SWATCH_X + 8, SLOT_Y + 21, t.title(), 60);
		gfx.fill(4, TEXT_Y - 4, W - 4, TEXT_Y - 3, t.ring());
		JeiPanel.wrapped(gfx, font, Component.translatable("hemomancy.jei.gouge_catalyst." + display.function().getSerializedName()),
				6, TEXT_Y, W - 12, t.text());
	}

	/**
	 * A lit run's colour as a 16x16 swatch: a groove of its colour, brightening and dimming slowly. A dark run hides
	 * itself, so its swatch is only an outline round an empty groove.
	 */
	private static void swatch(GuiGraphics gfx, GroundFunction function, int x, int y, float time) {
		int rgb = function.color() & 0xFFFFFF;
		gfx.fill(x - 1, y - 1, x + 17, y + 17, 0xFF070102);
		if (function.drawn()) {
			int glow = (int) (150 + 90 * JeiPanel.pulse(time, 1.6f));
			gfx.fill(x - 3, y - 3, x + 19, y + 19, (60 << 24) | rgb);
			gfx.fill(x, y + 6, x + 16, y + 10, (glow << 24) | rgb);
			gfx.fill(x + 6, y, x + 10, y + 16, (glow << 24) | rgb);
			JeiPanel.bevel(gfx, x - 1, y - 1, 18, 18, 0xFF000000 | rgb, 0xFF1C0606);
		} else {
			JeiPanel.bevel(gfx, x - 1, y - 1, 18, 18, 0xFF5A4A6A, 0xFF5A4A6A);
			gfx.fill(x, y + 7, x + 16, y + 9, 0xFF120A18);
		}
	}
}
