package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryWeavingRecipe extends CustomRecipe {

	public static final int MAX_ENZYMES_PER_TENDENCY = 8;
	private static final double LEGACY_BLOOD_COST_PER_TENDENCY = 50.0;
	private static final int BASE_CRAFT_TIME_TICKS = 100;
	private static final int CRAFT_TIME_PER_ORB = 20;

	private final ResourceLocation id;
	private final List<Ingredient> catalysts;
	private final EnumMap<EnumBloodTendency, Integer> enzymeRequirements;
	private final double bloodCost;
	private final ItemStack result;

	public static HashMap<EnumBloodTendency, Float> blank() {
		return new HashMap<>(Map.of(
				EnumBloodTendency.ANIMUS, 0f,
				EnumBloodTendency.MORTEM, 0f,
				EnumBloodTendency.DUCTILIS, 0f,
				EnumBloodTendency.FERRIC, 0f,
				EnumBloodTendency.LUX, 0f,
				EnumBloodTendency.TENEBRIS, 0f,
				EnumBloodTendency.FLAMMEUS, 0f,
				EnumBloodTendency.CONGEATIO, 0f));
	}

	public static EnumMap<EnumBloodTendency, Integer> blankEnzymeRequirements() {
		EnumMap<EnumBloodTendency, Integer> blank = new EnumMap<>(EnumBloodTendency.class);
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			blank.put(tendency, 0);
		}
		return blank;
	}

	public static List<MemoryWeavingRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}

	public MemoryWeavingRecipe(ResourceLocation id, List<Ingredient> catalysts,
			Map<EnumBloodTendency, Integer> enzymeRequirements, double bloodCost, ItemStack result) {
		super(CraftingBookCategory.MISC);
		this.id = id;
		this.catalysts = sanitizeCatalysts(catalysts);
		this.enzymeRequirements = sanitizeEnzymes(enzymeRequirements);
		this.bloodCost = Math.max(0.0D, bloodCost);
		this.result = result == null ? ItemStack.EMPTY : result;
	}

	public MemoryWeavingRecipe(ResourceLocation id, Ingredient ingredient,
			Map<EnumBloodTendency, Float> tendencies, ItemStack result) {
		this(id, legacyCatalysts(ingredient), legacyEnzymes(tendencies),
				legacyBloodCost(tendencies), result);
	}

	public MemoryWeavingRecipe(ResourceLocation id, Map<EnumBloodTendency, Float> tendencies, ItemStack result) {
		this(id, Ingredient.EMPTY, tendencies, result);
	}

	private static List<Ingredient> sanitizeCatalysts(List<Ingredient> rawCatalysts) {
		if (rawCatalysts == null || rawCatalysts.isEmpty()) {
			return List.of();
		}
		List<Ingredient> cleaned = new ArrayList<>();
		for (Ingredient catalyst : rawCatalysts) {
			if (catalyst != null && catalyst != Ingredient.EMPTY && !catalyst.isEmpty()) {
				cleaned.add(catalyst);
			}
		}
		return List.copyOf(cleaned);
	}

	private static EnumMap<EnumBloodTendency, Integer> sanitizeEnzymes(Map<EnumBloodTendency, Integer> raw) {
		EnumMap<EnumBloodTendency, Integer> cleaned = blankEnzymeRequirements();
		if (raw == null) {
			return cleaned;
		}
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			int amount = Math.max(0, raw.getOrDefault(tendency, 0));
			cleaned.put(tendency, Math.min(MAX_ENZYMES_PER_TENDENCY, amount));
		}
		return cleaned;
	}

	private static List<Ingredient> legacyCatalysts(Ingredient ingredient) {
		if (ingredient == null || ingredient == Ingredient.EMPTY || ingredient.isEmpty()) {
			return List.of();
		}
		return List.of(ingredient);
	}

	private static EnumMap<EnumBloodTendency, Integer> legacyEnzymes(Map<EnumBloodTendency, Float> tendencies) {
		EnumMap<EnumBloodTendency, Integer> enzymes = blankEnzymeRequirements();
		if (tendencies == null) {
			return enzymes;
		}
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			enzymes.put(tendency, tendencies.getOrDefault(tendency, 0f) > 0f ? 1 : 0);
		}
		return enzymes;
	}

	private static double legacyBloodCost(Map<EnumBloodTendency, Float> tendencies) {
		double count = 0.0D;
		if (tendencies != null) {
			for (Float value : tendencies.values()) {
				if (value != null && value > 0f) {
					count++;
				}
			}
		}
		return count * LEGACY_BLOOD_COST_PER_TENDENCY;
	}

	public ResourceLocation getId() {
		return id;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		return getResultItem(registries).copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public boolean isSpecial() {
		return false;
	}

	/**
	 * Legacy single-catalyst accessor used by older UI code. New code should use
	 * {@link #getCatalysts()}.
	 */
	public Ingredient getIngredient() {
		return catalysts.isEmpty() ? Ingredient.EMPTY : catalysts.getFirst();
	}

	public List<Ingredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.addAll(catalysts);
		return ingredients;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.memory_weaving_serializer.get();
	}

	/**
	 * Legacy tendency view. Values now mirror integer enzyme requirements.
	 */
	public Map<EnumBloodTendency, Float> getTendency() {
		Map<EnumBloodTendency, Float> view = blank();
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			view.put(tendency, (float) getEnzymeRequirement(tendency));
		}
		return view;
	}

	public Map<EnumBloodTendency, Integer> getEnzymeRequirements() {
		return Map.copyOf(enzymeRequirements);
	}

	public int getEnzymeRequirement(EnumBloodTendency tendency) {
		return enzymeRequirements.getOrDefault(tendency, 0);
	}

	public int getTotalEnzymeRequirement() {
		int total = 0;
		for (int amount : enzymeRequirements.values()) {
			total += amount;
		}
		return total;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.memory_weaving_type.get();
	}

	@Override
	public boolean matches(CraftingInput container, Level level) {
		return true;
	}

	public boolean matchRecipe(MemoryWeavingRecipe checkRecipe) {
		return checkRecipe != null
				&& enzymeRequirements.equals(checkRecipe.enzymeRequirements)
				&& catalysts.equals(checkRecipe.catalysts);
	}

	public void setTendency(Map<EnumBloodTendency, Float> tendency) {
		enzymeRequirements.clear();
		enzymeRequirements.putAll(legacyEnzymes(tendency));
	}

	public boolean isTendencyRequired(EnumBloodTendency tendency) {
		return getEnzymeRequirement(tendency) > 0;
	}

	public int getRequiredTendencyCount() {
		int count = 0;
		for (int amount : enzymeRequirements.values()) {
			if (amount > 0) {
				count++;
			}
		}
		return count;
	}

	public double getBloodCost() {
		return bloodCost;
	}

	public int getCraftTimeTicks() {
		return BASE_CRAFT_TIME_TICKS + getTotalOrbCount() * CRAFT_TIME_PER_ORB;
	}

	public int getTotalOrbCount() {
		int total = 0;
		for (int amount : enzymeRequirements.values()) {
			total += getOrbCountForRequirement(amount);
		}
		return total;
	}

	public static int getOrbCountForRequirement(int requirement) {
		return Mth.clamp(requirement, 0, MAX_ENZYMES_PER_TENDENCY);
	}

	@Override
	public String toString() {
		return "Memory Weaving Recipe:" + result;
	}
}
