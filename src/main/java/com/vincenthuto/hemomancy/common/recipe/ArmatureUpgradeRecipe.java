package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ArmatureUpgradeRecipe extends CustomRecipe {
	public record PersistentDataGate(String key, String value) {
		public boolean matches(Player player) {
			return player != null && value.equals(player.getPersistentData().getString(key));
		}
	}

	private ResourceLocation id;
	private final int requiredDegree;
	private final ArmatureUpgradeRules.ArmatureSlot armorSlot;
	private final Ingredient validBase;
	private final Ingredient reagent;
	private final double bloodCost;
	private final ItemStack result;
	private final ArmatureUpgradeRules.ArmatureTier requiredArmatureTier;
	@Nullable
	private final CompoundTag requiredBaseData;
	@Nullable
	private final CompoundTag resultData;
	@Nullable
	private final PersistentDataGate persistentDataGate;

	public static List<ArmatureUpgradeRecipe> getAllRecipes(Level level) {
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.armature_upgrade_type.get())
				.stream().map(holder -> {
					ArmatureUpgradeRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public ArmatureUpgradeRecipe(ResourceLocation id, int requiredDegree,
			ArmatureUpgradeRules.ArmatureSlot armorSlot, Ingredient validBase, Ingredient reagent,
			double bloodCost, ItemStack result, @Nullable CompoundTag resultData,
			@Nullable PersistentDataGate persistentDataGate) {
		this(id, requiredDegree, armorSlot, validBase, reagent, bloodCost, result,
				ArmatureUpgradeRules.requiredTierForDegree(requiredDegree), null, resultData, persistentDataGate);
	}

	public ArmatureUpgradeRecipe(ResourceLocation id, int requiredDegree,
			ArmatureUpgradeRules.ArmatureSlot armorSlot, Ingredient validBase, Ingredient reagent,
			double bloodCost, ItemStack result, ArmatureUpgradeRules.ArmatureTier requiredArmatureTier,
			@Nullable CompoundTag requiredBaseData, @Nullable CompoundTag resultData,
			@Nullable PersistentDataGate persistentDataGate) {
		super(CraftingBookCategory.EQUIPMENT);
		this.id = id;
		this.requiredDegree = Math.max(0, requiredDegree);
		this.armorSlot = armorSlot;
		this.validBase = validBase;
		this.reagent = reagent;
		this.bloodCost = Math.max(0, bloodCost);
		this.result = result;
		this.requiredArmatureTier = requiredArmatureTier == null
				? ArmatureUpgradeRules.requiredTierForDegree(this.requiredDegree)
				: requiredArmatureTier;
		this.requiredBaseData = requiredBaseData == null ? null : requiredBaseData.copy();
		this.resultData = resultData == null ? null : resultData.copy();
		this.persistentDataGate = persistentDataGate;
	}

	public ResourceLocation getId() {
		return id;
	}

	public void setId(ResourceLocation id) {
		this.id = id;
	}

	public int getRequiredDegree() {
		return requiredDegree;
	}

	public ArmatureUpgradeRules.ArmatureSlot getArmorSlot() {
		return armorSlot;
	}

	public Ingredient getValidBase() {
		return validBase;
	}

	public Ingredient getReagent() {
		return reagent;
	}

	public double getBloodCost() {
		return bloodCost;
	}

	public ArmatureUpgradeRules.ArmatureTier getRequiredArmatureTier() {
		return requiredArmatureTier;
	}

	@Nullable
	public CompoundTag getResultData() {
		return resultData == null ? null : resultData.copy();
	}

	@Nullable
	public CompoundTag getRequiredBaseData() {
		return requiredBaseData == null ? null : requiredBaseData.copy();
	}

	@Nullable
	public PersistentDataGate getPersistentDataGate() {
		return persistentDataGate;
	}

	public boolean matchesUpgrade(ArmatureUpgradeRules.ArmatureSlot slot, ItemStack wornBase,
			ItemStack bowlReagent, Player player) {
		return matchesUpgrade(slot, wornBase, bowlReagent, player, ArmatureUpgradeRules.ArmatureTier.BASE);
	}

	public boolean matchesUpgrade(ArmatureUpgradeRules.ArmatureSlot slot, ItemStack wornBase,
			ItemStack bowlReagent, Player player, ArmatureUpgradeRules.ArmatureTier armatureTier) {
		if (armorSlot != slot || wornBase.isEmpty() || bowlReagent.isEmpty()) {
			return false;
		}
		if (!validBase.test(wornBase) || !reagent.test(bowlReagent) || !matchesRequiredBaseData(wornBase)) {
			return false;
		}
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < requiredDegree) {
			return false;
		}
		if (armatureTier.id() < requiredArmatureTier.id()) {
			return false;
		}
		return persistentDataGate == null || persistentDataGate.matches(player);
	}

	private boolean matchesRequiredBaseData(ItemStack wornBase) {
		if (requiredBaseData == null || requiredBaseData.isEmpty()) return true;
		CompoundTag actual = wornBase.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return requiredBaseData.getAllKeys().stream()
				.allMatch(key -> requiredBaseData.get(key).equals(actual.get(key)));
	}

	public ItemStack createResult(HolderLookup.Provider registries) {
		ItemStack copy = getResultItem(registries).copy();
		if (resultData != null && !resultData.isEmpty()) {
			CompoundTag data = copy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			data.merge(resultData.copy());
			copy.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		}
		return copy;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		return createResult(registries);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.add(validBase);
		ingredients.add(reagent);
		return ingredients;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.armature_upgrade_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.armature_upgrade_type.get();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return false;
	}
}
