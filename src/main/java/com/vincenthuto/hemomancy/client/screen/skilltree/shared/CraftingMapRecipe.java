package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * One entry of the crafting browser: a floorless Blood Structure, fed by hand
 * at its activate block; a Working craft, built beside a Cardinal Focus and
 * swallowed by the Working floor; or another making rite with a result (the
 * vessels, the Horn), shown with its Working floor in place of a structure.
 */
public final class CraftingMapRecipe {
	public record Offering(Ingredient ingredient, int count) {
	}

	private final ResourceLocation id;
	private final ItemStack result;
	private final MultiblockPattern pattern;
	private final double bloodCost;
	private final ItemStack catalyst;
	private final Block hitBlock;
	private final List<Offering> offerings;
	private final ResourceLocation floorId;
	private final boolean unstained;
	private final int requiredDegree;
	private final boolean structure;
	private final boolean showsStructure;
	private final Component name;
	private final Component description;
	private final double riteCost;

	private CraftingMapRecipe(ResourceLocation id, ItemStack result, MultiblockPattern pattern, double bloodCost,
			ItemStack catalyst, Block hitBlock, List<Offering> offerings, ResourceLocation floorId, boolean unstained,
			int requiredDegree, boolean structure, boolean showsStructure, Component name, Component description,
			double riteCost) {
		this.id = id;
		this.result = result;
		this.pattern = pattern;
		this.bloodCost = bloodCost;
		this.catalyst = catalyst;
		this.hitBlock = hitBlock;
		this.offerings = List.copyOf(offerings);
		this.floorId = floorId;
		this.unstained = unstained;
		this.requiredDegree = requiredDegree;
		this.structure = structure;
		this.showsStructure = showsStructure;
		this.name = name;
		this.description = description;
		this.riteCost = riteCost;
	}

	public static CraftingMapRecipe of(BloodStructureRecipe recipe) {
		return new CraftingMapRecipe(recipe.getId(), recipe.getResult(), recipe.getPattern(), recipe.getBloodCost(),
				recipe.getHeldItem(), recipe.getHitBlock(), List.of(), null, recipe.isUnstained(),
				RecipeDegreeGates.getRequiredDegree(recipe), true, true, recipe.getResult().getHoverName(), null, 0);
	}

	public static CraftingMapRecipe of(CardinalRiteRecipe craft) {
		ItemStack[] media = craft.getMedium().getItems();
		MultiblockPattern shown = craft.getSubject() != null ? craft.getSubject()
				: craft.getRequiredStructure() != null ? craft.getRequiredStructure()
				: craft.getFloorId() == null ? null
				: CardinalRiteFloorRegistry.get(craft.getFloorId()).map(CardinalRiteFloorDefinition::pattern).orElse(null);
		return new CraftingMapRecipe(craft.getId(), craft.getResult(), shown, craft.getSubjectFeedMl(),
				media.length == 0 ? ItemStack.EMPTY : media[0], null,
				craft.getBrazierSignature().stream()
						.map(requirement -> new Offering(requirement.ingredient(), requirement.count())).toList(),
				craft.getFloorId(), false, RecipeDegreeGates.getRequiredDegree(craft), craft.isWorkingCraft(),
				craft.getSubject() != null || craft.getRequiredStructure() != null,
				craft.displayName(), craft.displayDescription(), craft.getBloodCost());
	}

	public ResourceLocation getId() {
		return id;
	}

	public ItemStack getResult() {
		return result;
	}

	/** The structure to build (floorless pattern, Working subject or a rite's required structure), else the rite's floor. */
	public MultiblockPattern getPattern() {
		return pattern;
	}

	/** Blood fed into the structure, by hand or during the Working rite. */
	public double getBloodCost() {
		return bloodCost;
	}

	/** The offhand catalyst, or the item a Working craft seats in its Focus. */
	public ItemStack getHeldItem() {
		return catalyst;
	}

	/** The activate block of a floorless structure; Working crafts have none. */
	public Block getHitBlock() {
		return hitBlock;
	}

	public List<Offering> getOfferings() {
		return offerings;
	}

	public boolean isUnstained() {
		return unstained;
	}

	public int requiredDegree() {
		return requiredDegree;
	}

	/** The minimum Working floor, or null for a floorless structure. */
	public ResourceLocation floorId() {
		return floorId;
	}

	/** "greater Working floor", from the minimum floor's tier. */
	public Component floorLabel() {
		String path = floorId == null ? "" : floorId.getPath();
		String tier = path.substring(path.lastIndexOf('_') + 1);
		return tier.isEmpty() ? Component.translatable("screen.hemomancy.crafting_map_recipe.working_floor")
				: Component.translatable("screen.hemomancy.crafting_map_recipe.tier_working_floor",
						Component.translatableWithFallback("cardinal_rite_floor.hemomancy.tier." + tier, tier));
	}

	public boolean isWorking() {
		return floorId != null;
	}

	/** Whether there is a structure to build and feed; false for a making rite performed on its floor alone. */
	/** Whether the preview is something built on or beside the floor, rather than the floor itself. */
	public boolean showsStructure() {
		return showsStructure;
	}

	public boolean hasStructure() {
		return structure;
	}

	/** The rite's name, or the result's name for a floorless structure. */
	public Component name() {
		return name;
	}

	/** The rite's description, or null for a floorless structure. */
	public Component description() {
		return description;
	}

	/** Blood the rite itself draws on its floor, apart from what the subject is fed; zero for a floorless structure. */
	public double riteCost() {
		return riteCost;
	}

	public MnemonicBlueprintTarget blueprintTarget() {
		return new MnemonicBlueprintTarget(isWorking() ? MnemonicBlueprintTarget.Type.CARDINAL_RITE
				: MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, id);
	}
}
