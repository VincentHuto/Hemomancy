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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
		super(CraftingBookCategory.EQUIPMENT);
		this.id = id;
		this.requiredDegree = Math.max(0, requiredDegree);
		this.armorSlot = armorSlot;
		this.validBase = validBase;
		this.reagent = reagent;
		this.bloodCost = Math.max(0, bloodCost);
		this.result = result;
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

	@Nullable
	public CompoundTag getResultData() {
		return resultData == null ? null : resultData.copy();
	}

	@Nullable
	public PersistentDataGate getPersistentDataGate() {
		return persistentDataGate;
	}

	public boolean matchesUpgrade(ArmatureUpgradeRules.ArmatureSlot slot, ItemStack wornBase,
			ItemStack bowlReagent, Player player) {
		if (armorSlot != slot || wornBase.isEmpty() || bowlReagent.isEmpty()) {
			return false;
		}
		if (!validBase.test(wornBase) || !reagent.test(bowlReagent)) {
			return false;
		}
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < requiredDegree) {
			return false;
		}
		return persistentDataGate == null || persistentDataGate.matches(player);
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
