package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class MemoryWeavingRecipe extends StationRecipe {

	public static final int MAX_ENZYMES_PER_TEMPER = 8;
	private static final double LEGACY_BLOOD_COST_PER_TEMPER = 50.0;

	private final ResourceLocation id;
	private final List<Ingredient> catalysts;
	private final EnumMap<EnumBloodTemper, Integer> enzymeRequirements;
	private final double bloodCost;
	private final ItemStack result;

	public static HashMap<EnumBloodTemper, Float> blank() {
		return new HashMap<>(Map.of(
				EnumBloodTemper.ANIMUS, 0f,
				EnumBloodTemper.MORTEM, 0f,
				EnumBloodTemper.DUCTILIS, 0f,
				EnumBloodTemper.FERRIC, 0f,
				EnumBloodTemper.LUX, 0f,
				EnumBloodTemper.TENEBRIS, 0f,
				EnumBloodTemper.FLAMMEUS, 0f,
				EnumBloodTemper.CONGEATIO, 0f));
	}

	public static EnumMap<EnumBloodTemper, Integer> blankEnzymeRequirements() {
		EnumMap<EnumBloodTemper, Integer> blank = new EnumMap<>(EnumBloodTemper.class);
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			blank.put(temper, 0);
		}
		return blank;
	}

	public static List<MemoryWeavingRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}

	public MemoryWeavingRecipe(ResourceLocation id, List<Ingredient> catalysts,
			Map<EnumBloodTemper, Integer> enzymeRequirements, double bloodCost, ItemStack result) {
		this.id = id;
		this.catalysts = sanitizeCatalysts(catalysts);
		this.enzymeRequirements = sanitizeEnzymes(enzymeRequirements);
		this.bloodCost = Math.max(0.0D, bloodCost);
		this.result = result == null ? ItemStack.EMPTY : result;
	}

	public MemoryWeavingRecipe(ResourceLocation id, Ingredient ingredient,
			Map<EnumBloodTemper, Float> tempers, ItemStack result) {
		this(id, legacyCatalysts(ingredient), legacyEnzymes(tempers),
				legacyBloodCost(tempers), result);
	}

	public MemoryWeavingRecipe(ResourceLocation id, Map<EnumBloodTemper, Float> tempers, ItemStack result) {
		this(id, Ingredient.EMPTY, tempers, result);
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

	private static EnumMap<EnumBloodTemper, Integer> sanitizeEnzymes(Map<EnumBloodTemper, Integer> raw) {
		EnumMap<EnumBloodTemper, Integer> cleaned = blankEnzymeRequirements();
		if (raw == null) {
			return cleaned;
		}
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			int amount = Math.max(0, raw.getOrDefault(temper, 0));
			cleaned.put(temper, Math.min(MAX_ENZYMES_PER_TEMPER, amount));
		}
		return cleaned;
	}

	private static List<Ingredient> legacyCatalysts(Ingredient ingredient) {
		if (ingredient == null || ingredient == Ingredient.EMPTY || ingredient.isEmpty()) {
			return List.of();
		}
		return List.of(ingredient);
	}

	private static EnumMap<EnumBloodTemper, Integer> legacyEnzymes(Map<EnumBloodTemper, Float> tempers) {
		EnumMap<EnumBloodTemper, Integer> enzymes = blankEnzymeRequirements();
		if (tempers == null) {
			return enzymes;
		}
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			enzymes.put(temper, tempers.getOrDefault(temper, 0f) > 0f ? 1 : 0);
		}
		return enzymes;
	}

	private static double legacyBloodCost(Map<EnumBloodTemper, Float> tempers) {
		double count = 0.0D;
		if (tempers != null) {
			for (Float value : tempers.values()) {
				if (value != null && value > 0f) {
					count++;
				}
			}
		}
		return count * LEGACY_BLOOD_COST_PER_TEMPER;
	}

	public ResourceLocation getId() {
		return id;
	}

	@Override
	public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
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
	 * Legacy temper view. Values now mirror integer enzyme requirements.
	 */
	public Map<EnumBloodTemper, Float> getTemper() {
		Map<EnumBloodTemper, Float> view = blank();
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			view.put(temper, (float) getEnzymeRequirement(temper));
		}
		return view;
	}

	public Map<EnumBloodTemper, Integer> getEnzymeRequirements() {
		return Map.copyOf(enzymeRequirements);
	}

	public int getEnzymeRequirement(EnumBloodTemper temper) {
		return enzymeRequirements.getOrDefault(temper, 0);
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
	public boolean matches(RecipeInput container, Level level) {
		return true;
	}

	public boolean matchRecipe(MemoryWeavingRecipe checkRecipe) {
		return checkRecipe != null
				&& enzymeRequirements.equals(checkRecipe.enzymeRequirements)
				&& catalysts.equals(checkRecipe.catalysts);
	}

	public void setTemper(Map<EnumBloodTemper, Float> temper) {
		enzymeRequirements.clear();
		enzymeRequirements.putAll(legacyEnzymes(temper));
	}

	public int getRequiredTemperCount() {
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

	public int getTotalOrbCount() {
		int total = 0;
		for (int amount : enzymeRequirements.values()) {
			total += getOrbCountForRequirement(amount);
		}
		return total;
	}

	public static int getOrbCountForRequirement(int requirement) {
		return Mth.clamp(requirement, 0, MAX_ENZYMES_PER_TEMPER);
	}

	@Override
	public String toString() {
		return "Memory Weaving Recipe:" + result;
	}
}
