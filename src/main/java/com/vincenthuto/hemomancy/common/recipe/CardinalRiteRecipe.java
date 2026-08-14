package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

public class CardinalRiteRecipe extends CustomRecipe {

	public static List<CardinalRiteRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get())
				.stream().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public static CardinalRiteRecipe getRiteByLocation(Level world, ResourceLocation loc) {
		CardinalRiteRecipe direct = world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
				.filter(h -> h.id().equals(loc)).findFirst().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (direct != null) {
			return direct;
		}

		String path = loc.getPath();
		ResourceLocation prefixed = path.startsWith("cardinal_rite/")
				? loc
				: ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "cardinal_rite/" + path);
		CardinalRiteRecipe withPrefix = world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
				.filter(h -> h.id().equals(prefixed)).findFirst().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (withPrefix != null) {
			return withPrefix;
		}

		if (path.startsWith("cardinal_rite/")) {
			ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), path.substring("cardinal_rite/".length()));
			return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
					.filter(h -> h.id().equals(stripped)).findFirst().map(holder -> {
						CardinalRiteRecipe recipe = holder.value();
						recipe.setId(holder.id());
						return recipe;
					}).orElse(null);
		}
		return null;
	}

	private ResourceLocation id;
	protected double bloodCost;
	protected CardinalRiteType riteType;
	protected MultiblockPattern pattern;
	protected ItemStack result;
	protected String riteName;
	protected String riteDescription;
	protected int requiredDegree;
	protected float requiredPurity = -1.0f;
	protected float requiredClarity = -1.0f;
	protected boolean breakBlocksOnCreation;
	protected boolean unstained;
	protected boolean rankup;
	protected CardinalRiteCeremonyDefinition ceremony;
	protected ResourceLocation floorId;
	protected MultiblockPattern requiredStructure;
	protected boolean consumeRequiredStructure;
	protected List<BrazierRequirement> brazierSignature = List.of();
	protected Ingredient medium = Ingredient.EMPTY;
	protected boolean consumeMediumOnSuccess = true;
	protected PuppeteerTrial puppeteerTrial;

	public record PuppeteerTrial(String summonName) {
		public PuppeteerTrial {
			if (summonName == null || summonName.isBlank()) {
				throw new IllegalArgumentException("Puppeteer trial summon name cannot be blank");
			}
		}
	}

	public record BrazierRequirement(Ingredient ingredient, int count, boolean consumeOnSuccess) {
		public BrazierRequirement {
			if (ingredient == null) throw new IllegalArgumentException("Brazier ingredient cannot be null");
			if (count < 1) throw new IllegalArgumentException("Brazier ingredient count must be positive");
		}
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, -1, true, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, requiredDegree, true, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree, boolean breakBlocksOnCreation) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, requiredDegree, breakBlocksOnCreation, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree, boolean breakBlocksOnCreation, boolean unstained) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, requiredDegree, breakBlocksOnCreation,
				unstained, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree, boolean breakBlocksOnCreation, boolean unstained, boolean rankup) {
		super(CraftingBookCategory.MISC);
		this.id = pId;
		this.bloodCost = bloodCost;
		this.riteType = riteType;
		this.pattern = pattern;
		this.result = result;
		this.riteName = riteName;
		this.riteDescription = riteDescription;
		this.requiredDegree = requiredDegree;
		this.breakBlocksOnCreation = breakBlocksOnCreation;
		this.unstained = unstained;
		this.rankup = rankup;
	}

	public ResourceLocation getId() { return id; }

	public void setId(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider p_267165_) {
		return this.getResultItem(p_267165_).copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	public double getBloodCost() {
		return bloodCost;
	}

	public CardinalRiteType getRiteType() {
		return riteType;
	}

	public MultiblockPattern getPattern() {
		return pattern;
	}

	public MultiblockPattern getFloorPattern() {
		return floorId == null ? null
				: CardinalRiteFloorRegistry.get(floorId)
						.map(com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition::pattern)
						.orElse(null);
	}

	public MultiblockPattern getPreviewPattern() {
		return requiredStructure != null ? requiredStructure
				: hasLayeredStation() ? getFloorPattern() : pattern;
	}

	public boolean hasLayeredStation() {
		return floorId != null;
	}

	public ResourceLocation getFloorId() {
		return floorId;
	}

	public void setFloorId(ResourceLocation floorId) {
		this.floorId = floorId;
	}

	public MultiblockPattern getRequiredStructure() {
		return requiredStructure;
	}

	public void setRequiredStructure(MultiblockPattern requiredStructure) {
		this.requiredStructure = requiredStructure;
	}

	public boolean shouldConsumeRequiredStructure() {
		return consumeRequiredStructure;
	}

	public void setConsumeRequiredStructure(boolean consumeRequiredStructure) {
		this.consumeRequiredStructure = consumeRequiredStructure;
	}

	public List<BrazierRequirement> getBrazierSignature() {
		return brazierSignature;
	}

	public void setBrazierSignature(List<BrazierRequirement> brazierSignature) {
		this.brazierSignature = List.copyOf(brazierSignature);
	}

	public Ingredient getMedium() {
		return medium;
	}

	public boolean hasMedium() {
		return medium != null && medium != Ingredient.EMPTY && !medium.isEmpty();
	}

	public void setMedium(Ingredient medium) {
		this.medium = medium == null ? Ingredient.EMPTY : medium;
	}

	public boolean shouldConsumeMediumOnSuccess() {
		return consumeMediumOnSuccess;
	}

	public void setConsumeMediumOnSuccess(boolean consumeMediumOnSuccess) {
		this.consumeMediumOnSuccess = consumeMediumOnSuccess;
	}

	public boolean isPuppeteerTrial() {
		return puppeteerTrial != null;
	}

	public PuppeteerTrial getPuppeteerTrial() {
		return puppeteerTrial;
	}

	public void setPuppeteerTrial(PuppeteerTrial puppeteerTrial) {
		this.puppeteerTrial = puppeteerTrial;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider a) {
		return this.result;
	}

	public String getRiteName() {
		return riteName;
	}

	public String getRiteDescription() {
		return riteDescription;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.cardinal_rite_recipe_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.cardinal_rite_recipe_type.get();
	}

	@Override
	public boolean matches(CraftingInput pContainer, Level pLevel) {
		return true;
	}

	public boolean matchRecipe(CardinalRiteRecipe checkRecipe) {
		return this.riteType == checkRecipe.riteType && this.riteName.equals(checkRecipe.riteName);
	}

	public void setBloodCost(double bloodCost) {
		this.bloodCost = bloodCost;
	}

	public void setRiteType(CardinalRiteType riteType) {
		this.riteType = riteType;
	}

	public void setPattern(MultiblockPattern pattern) {
		this.pattern = pattern;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public void setRiteName(String riteName) {
		this.riteName = riteName;
	}

	public void setRiteDescription(String riteDescription) {
		this.riteDescription = riteDescription;
	}

	/**
	 * Returns the per-recipe degree / stage requirement.
	 */
	public int getRequiredDegree() {
		return requiredDegree;
	}

	public void setRequiredDegree(int requiredDegree) {
		this.requiredDegree = requiredDegree;
	}

	public float getRequiredPurity() { return requiredPurity; }

	public void setRequiredPurity(float requiredPurity) { this.requiredPurity = requiredPurity; }

	public float getRequiredClarity() { return requiredClarity; }

	public void setRequiredClarity(float requiredClarity) { this.requiredClarity = requiredClarity; }

	/**
	 * Returns whether the rite should break the multiblock structure blocks
	 * upon completion.
	 */
	public boolean shouldBreakBlocksOnCreation() {
		return breakBlocksOnCreation;
	}

	public void setBreakBlocksOnCreation(boolean breakBlocksOnCreation) {
		this.breakBlocksOnCreation = breakBlocksOnCreation;
	}

	/**
	 * Returns whether this rite belongs to the Unstained path.
	 */
	public boolean isUnstained() {
		return unstained;
	}

	public void setUnstained(boolean unstained) {
		this.unstained = unstained;
	}

	/**
	 * Returns whether this rite advances a player through the Harbinger degrees.
	 */
	public boolean isRankup() {
		return rankup;
	}

	public void setRankup(boolean rankup) {
		this.rankup = rankup;
	}

	public CardinalRiteCeremonyDefinition getCeremony() {
		return ceremony;
	}

	public void setCeremony(CardinalRiteCeremonyDefinition ceremony) {
		this.ceremony = ceremony;
	}

	public boolean hasInteractiveCeremony() {
		return !unstained && ceremony != null;
	}

	@Override
	public String toString() {
		return "Cardinal Rite Recipe: " + riteName + " [" + riteType.getSerializedName() + "]";
	}

}
