package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.brewing.AdvancedBrewingReload;
import com.vincenthuto.hemomancy.common.brewing.BrewingMatch;
import com.vincenthuto.hemomancy.common.brewing.BrewingResolver;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BloodCrystalBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer;
import com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ghastly Alembic Block Entity — a blood distillery powered by fire below.
 * <p>
 * Slots:
 * <ul>
 *   <li>0 = Input ingredient (meat, blood items, etc.)</li>
 *   <li>1 = Flask slot (cured clay flasks to bottle blood)</li>
 *   <li>2 = Result output</li>
 * </ul>
 * Heat source: checks the block directly below for fire, soul fire, lit campfire,
 * lit soul campfire, lava, magma, or crimson flames.
 */
public class GhastlyAlembicBlockEntity extends BaseContainerBlockEntity
		implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, IBloodReservoir, IBloodContainerSlotAccess {

	static final String TAG_BLOOD_LEVEL = "bloodLevel";

	// Slot indices
	public static final int SLOT_INPUT    = 0;
	public static final int SLOT_FLASK    = 1;
	public static final int SLOT_RESULT   = 2;
	public static final int SLOT_CATALYST = 3;
	public static final int SLOT_FLASK_OUTPUT = 4;
	public static final int SLOT_TINCTURE_BLOOD = 5;
	public static final int SLOT_CATALYST_2 = 6;
	public static final int NUM_SLOTS     = 7;

	// Container data indices
	public static final int DATA_HEATED = 0;
	public static final int DATA_COOKING_PROGRESS = 1;
	public static final int DATA_COOKING_TOTAL_TIME = 2;
	public static final int DATA_STATUS = 3;
	public static final int DATA_TIER = 4;
	public static final int DATA_ADVANCED_PROGRESS = 5;
	public static final int DATA_ADVANCED_TOTAL_TIME = 6;
	public static final int NUM_DATA_VALUES = 7;

	public enum Status { NO_HEAT, NO_INPUT, MISSING_INGREDIENTS, OUTPUT_BLOCKED, TANK_FULL, DISTILLING }

	public static final int BURN_TIME_STANDARD = 200;

	// Hopper / sided access
	private static final int[] SLOTS_FOR_UP    = new int[]{SLOT_INPUT};
	private static final int[] SLOTS_FOR_DOWN  = new int[]{SLOT_RESULT, SLOT_FLASK_OUTPUT};
	private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FLASK, SLOT_CATALYST, SLOT_TINCTURE_BLOOD};
	private static final int[] SLOTS_FOR_SIDES_ATHANOR = new int[]{SLOT_FLASK, SLOT_CATALYST, SLOT_TINCTURE_BLOOD, SLOT_CATALYST_2};

	// ---- Fields ----

	public NonNullList<ItemStack> items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	private AlembicTier tier = AlembicTier.BASE;
	private final java.util.List<ItemStack> hiddenCatalystRecovery = new java.util.ArrayList<>();
	private boolean riteLocked;
	private java.util.UUID machineIdentity = java.util.UUID.randomUUID();
	private java.util.UUID lastUpgradeRite;
	private boolean heated;
	int cookingProgress;
	int cookingTotalTime;
	private BrewingMatch advancedMatch;
	private int advancedProgress;
	private ItemStack advancedPreview = ItemStack.EMPTY;
	private String advancedFeedback = "";
	private int advancedBloodCost;
	private int advancedTotalTicks;
	private int observedRecipeGeneration = AdvancedBrewingReload.generation();
	private ItemStack completedOutput = ItemStack.EMPTY;
	private String completedOperation = "";

	private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();

	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_HEATED -> heated ? 1 : 0;
				case DATA_COOKING_PROGRESS -> cookingProgress;
				case DATA_COOKING_TOTAL_TIME -> cookingTotalTime;
				case DATA_STATUS -> getProcessingStatus().ordinal();
				case DATA_TIER -> tier.ordinal();
				case DATA_ADVANCED_PROGRESS -> advancedProgress;
				case DATA_ADVANCED_TOTAL_TIME -> advancedTotalTicks;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_HEATED -> heated = value != 0;
				case DATA_COOKING_PROGRESS -> cookingProgress = value;
				case DATA_COOKING_TOTAL_TIME -> cookingTotalTime = value;
				case DATA_ADVANCED_PROGRESS -> advancedProgress = value;
				case DATA_ADVANCED_TOTAL_TIME -> advancedTotalTicks = value;
			}
		}

		@Override
		public int getCount() {
			return NUM_DATA_VALUES;
		}
	};

	// ---- Constructor ----

	public GhastlyAlembicBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.ghastly_alembic.get(), pos, state);
	}

	public AlembicTier tier() { return tier; }
	public boolean isRiteLocked() { return riteLocked; }
	public boolean isProcessing() { return cookingProgress > 0 || advancedProgress > 0; }
	public java.util.UUID machineIdentity() { return machineIdentity; }
	public void setRiteLocked(boolean locked) { riteLocked = locked; sendUpdates(); }
	@Override public boolean canReceiveBlood() { return !riteLocked; }
	@Override public boolean canProvideBlood() { return !riteLocked; }
	public ItemStack advancedPreview() { return advancedPreview; }
	public String advancedFeedback() { return advancedFeedback; }
	public int advancedBloodCost() { return advancedBloodCost; }
	public int advancedTotalTicks() { return advancedTotalTicks; }

	public void setTier(AlembicTier tier) {
		if (tier == null || tier.ordinal() < this.tier.ordinal()) return;
		this.tier = tier;
		setChanged();
		sendUpdates();
	}

	public boolean wasUpgradedBy(java.util.UUID riteId) {
		return riteId != null && riteId.equals(lastUpgradeRite);
	}

	public boolean completeUpgrade(AlembicTier target, java.util.UUID riteId) {
		if (target == null || riteId == null || target.ordinal() != tier.ordinal() + 1) return false;
		lastUpgradeRite = riteId;
		setTier(target);
		return true;
	}

	public ItemStack takeHiddenCatalystRecovery() {
		if (hiddenCatalystRecovery.isEmpty()) return ItemStack.EMPTY;
		ItemStack result = hiddenCatalystRecovery.removeFirst();
		setChanged();
		return result;
	}

	// ---- Heat source detection ----

	/**
	 * Returns true if the block directly below is a valid heat source.
	 */
	public static boolean isHeatSource(Level level, BlockPos alembicPos) {
		BlockPos below = alembicPos.below();
		BlockState belowState = level.getBlockState(below);

		// Vanilla fire blocks
		if (belowState.is(Blocks.FIRE) || belowState.is(Blocks.SOUL_FIRE)) {
			return true;
		}
		// Campfires (only when lit)
		if (belowState.is(Blocks.CAMPFIRE) || belowState.is(Blocks.SOUL_CAMPFIRE)) {
			return belowState.getValue(CampfireBlock.LIT);
		}
		// Lava & magma
		if (belowState.is(Blocks.LAVA) || belowState.is(Blocks.MAGMA_BLOCK)) {
			return true;
		}
		// Mod's own crimson flames
		return belowState.is(BlockInit.crimson_flames.get());
	}

	// ---- Capability (lazy) ----

	@Nullable
	private IBloodVolume resolveVolume() {
		return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
	}

	// ---- Ticking ----

	private static int getTotalCookTime(Level level, GhastlyAlembicBlockEntity te) {
		return level.getRecipeManager()
				.getAllRecipesFor(RecipeInit.distillation_recipe_type.get())
				.stream()
				.filter(h -> !h.value().isPallid() && h.value().matchesItems(te.items.get(SLOT_INPUT),
						te.items.get(SLOT_CATALYST), te.items.get(SLOT_TINCTURE_BLOOD)))
				.mapToInt(h -> h.value().getCookingTime())
				.findFirst()
				.orElse(200);
	}

	@Nullable
	private static RecipeHolder<DistillationRecipe> findMatchingRecipe(Level level, GhastlyAlembicBlockEntity te) {
		return level.getRecipeManager()
				.getAllRecipesFor(RecipeInit.distillation_recipe_type.get())
				.stream()
				.filter(h -> !h.value().isPallid() && h.value().matchesItems(te.items.get(SLOT_INPUT),
						te.items.get(SLOT_CATALYST), te.items.get(SLOT_TINCTURE_BLOOD)))
				.findFirst()
				.orElse(null);
	}

	private static void createExperience(ServerLevel level, Vec3 pos, int count, float xpPerItem) {
		int i = Mth.floor(count * xpPerItem);
		float f = Mth.frac(count * xpPerItem);
		if (f != 0.0F && Math.random() < f) {
			++i;
		}
		ExperienceOrb.award(level, pos, i);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, GhastlyAlembicBlockEntity te) {
		if (te.riteLocked) return;
		if (te.observedRecipeGeneration != AdvancedBrewingReload.generation()) {
			te.observedRecipeGeneration = AdvancedBrewingReload.generation();
			te.advancedMatch = null;
			te.advancedProgress = 0;
		}
		if (te.items.get(SLOT_RESULT).isEmpty()) te.completedOperation = "";
		boolean wasHeated = te.heated;
		te.heated = isHeatSource(level, pos);
		boolean dirty = false;

		IBloodVolume vol = te.resolveVolume();
		if (vol == null) return;
		RecipeHolder<DistillationRecipe> ordinary = findMatchingRecipe(level, te);
		BrewingMatch advanced = ordinary == null ? BrewingResolver.resolve(level, te.tier,
				te.items.get(SLOT_INPUT), te.items.get(SLOT_CATALYST), te.items.get(SLOT_CATALYST_2)) : null;
		te.updateAdvancedPreview(advanced, ordinary != null, vol);
		te.tickAdvanced(advanced, vol);

		if (advanced == null && te.heated && !te.items.get(SLOT_INPUT).isEmpty()) {
			RecipeHolder<DistillationRecipe> recipe = findMatchingRecipe(level, te);
			boolean canStoreByproduct = vol.getBloodVolume() < vol.getMaxBloodVolume() - 99;
			if (recipe != null && (canStoreByproduct || recipe.value().requiresBloodInput())) {
				int maxStack = te.getMaxStackSize();

				if (te.canBurn(level.registryAccess(), recipe, te.items, maxStack)) {
					++te.cookingProgress;
					if (te.cookingProgress >= te.cookingTotalTime) {
						te.cookingProgress = 0;
						te.cookingTotalTime = getTotalCookTime(level, te);
						if (te.burn(level.registryAccess(), recipe, te.items, maxStack)) {
							te.recordCompleted("distill");
							te.setRecipeUsed(recipe);
							if (!recipe.value().requiresBloodInput()) {
								vol.fill(100);
							}
							te.sendUpdates();
						}
						dirty = true;
					}
				} else {
					te.cookingProgress = 0;
				}
			} else {
				te.cookingProgress = 0;
			}
		} else if (!te.heated && te.cookingProgress > 0) {
			// Cool down when no heat
			te.cookingProgress = Mth.clamp(te.cookingProgress - 2, 0, te.cookingTotalTime);
		} else if (vol.getBloodVolume() >= vol.getMaxBloodVolume() - 99) {
			te.cookingProgress = 0;
		}

		// Update LIT blockstate
		if (wasHeated != te.heated) {
			dirty = true;
			state = state.setValue(AbstractFurnaceBlock.LIT, te.heated);
			level.setBlock(pos, state, 3);
		}

		// Drain stored blood into a gourd placed in the blood output slot
		tryDrainBloodIntoGourd(te);

		// Drain stored blood into flasks (independent of cooking)
		tryDrainBloodIntoFlask(te);

		// Fill blood from bloody flasks (independent of cooking)
		tryFillBloodFromFlask(te);

		// Alembic leak — drip blood onto nearby substrate blocks, grow blood crystals
		tryLeakBloodOntoBlock(level, pos, te, vol);

		if (dirty) {
			setChanged(level, pos, state);
		}
	}

	private void updateAdvancedPreview(@Nullable BrewingMatch match, boolean ordinary, IBloodVolume volume) {
		String feedback;
		if (ordinary) feedback = "ordinary";
		else if (tier == AlembicTier.BASE && items.get(SLOT_INPUT).is(net.minecraft.world.item.Items.POTION)) feedback = "upgrade_required";
		else if (match == null && items.get(SLOT_INPUT).is(net.minecraft.world.item.Items.POTION))
			feedback = items.get(SLOT_CATALYST).isEmpty() ? "missing_catalyst" : "invalid_formula";
		else if (match == null) feedback = "";
		else if (!heated) feedback = "no_heat";
		else if (volume.getBloodVolume() < match.blood()) feedback = "insufficient_blood";
		else if (!canAcceptAdvanced(match.result())) feedback = "output_occupied";
		else feedback = match.operation();
		ItemStack preview = match == null ? ItemStack.EMPTY : match.result();
		if (advancedFeedback.equals(feedback)
				&& ItemStack.isSameItemSameComponents(advancedPreview, preview)
				&& advancedBloodCost == (match == null ? 0 : match.blood())) return;
		advancedFeedback = feedback;
		advancedPreview = preview.copy();
		advancedBloodCost = match == null ? 0 : match.blood();
		advancedTotalTicks = match == null ? 0 : match.ticks();
		sendUpdates();
	}

	private void tickAdvanced(@Nullable BrewingMatch candidate, IBloodVolume volume) {
		if (candidate == null || !heated || !canAcceptAdvanced(candidate.result())
				|| volume.getBloodVolume() < candidate.blood()) {
			advancedMatch = null;
			advancedProgress = 0;
			return;
		}
		if (advancedMatch == null || !sameMatch(advancedMatch, candidate)) {
			advancedMatch = candidate;
			advancedProgress = 0;
		}
		if (++advancedProgress < candidate.ticks()) return;
		BrewingMatch refreshed = BrewingResolver.resolve(level, tier, items.get(SLOT_INPUT),
				items.get(SLOT_CATALYST), items.get(SLOT_CATALYST_2));
		if (refreshed != null && sameMatch(candidate, refreshed)
				&& canAcceptAdvanced(refreshed.result()) && volume.getBloodVolume() >= refreshed.blood()
				&& volume.drain(refreshed.blood())) {
			items.get(SLOT_INPUT).shrink(1);
			items.get(SLOT_CATALYST).shrink(1);
			if (!refreshed.catalyst2().isEmpty()) items.get(SLOT_CATALYST_2).shrink(1);
			ItemStack output = items.get(SLOT_RESULT);
			if (output.isEmpty()) items.set(SLOT_RESULT, refreshed.result().copy());
			else output.grow(1);
			recordCompleted(refreshed.operation());
			sendUpdates();
		}
		advancedMatch = null;
		advancedProgress = 0;
	}

	private boolean canAcceptAdvanced(ItemStack result) {
		ItemStack output = items.get(SLOT_RESULT);
		return output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result)
				&& output.getCount() < Math.min(getMaxStackSize(), output.getMaxStackSize()));
	}

	private void recordCompleted(String operation) {
		completedOperation = operation;
		completedOutput = items.get(SLOT_RESULT).copyWithCount(1);
		setChanged();
	}

	public void onPlayerExtract(Player player, ItemStack extracted) {
		if (level == null || level.isClientSide || completedOperation.isEmpty()
				|| !ItemStack.isSameItemSameComponents(completedOutput, extracted)) return;
		if (player instanceof ServerPlayer serverPlayer)
			com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.advancedBrewing(serverPlayer)
					.record(completedOperation);
		completedOperation = "";
		completedOutput = ItemStack.EMPTY;
		setChanged();
	}

	private static boolean sameMatch(BrewingMatch left, BrewingMatch right) {
		return left.operation().equals(right.operation()) && left.blood() == right.blood()
				&& left.ticks() == right.ticks()
				&& ItemStack.isSameItemSameComponents(left.input(), right.input())
				&& ItemStack.isSameItemSameComponents(left.catalyst(), right.catalyst())
				&& ItemStack.isSameItemSameComponents(left.catalyst2(), right.catalyst2())
				&& ItemStack.isSameItemSameComponents(left.result(), right.result());
	}

	public Status getProcessingStatus() {
		if (!heated) return Status.NO_HEAT;
		if (items.get(SLOT_INPUT).isEmpty()) return Status.NO_INPUT;
		if (!advancedPreview.isEmpty()) {
			if (advancedFeedback.equals("output_occupied")) return Status.OUTPUT_BLOCKED;
			if (advancedFeedback.equals("insufficient_blood")) return Status.MISSING_INGREDIENTS;
			return Status.DISTILLING;
		}
		if (level == null) return Status.MISSING_INGREDIENTS;
		RecipeHolder<DistillationRecipe> recipe = findMatchingRecipe(level, this);
		if (recipe == null) return Status.MISSING_INGREDIENTS;
		if (!canBurn(level.registryAccess(), recipe, items, getMaxStackSize())) return Status.OUTPUT_BLOCKED;
		if (!recipe.value().requiresBloodInput() && getBloodVolume() >= getMaxBloodVolume() - 99) {
			return Status.TANK_FULL;
		}
		return Status.DISTILLING;
	}

	// ---- Recipe logic ----

	private boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<DistillationRecipe> recipeHolder, NonNullList<ItemStack> inv, int maxStack) {
		if (inv.get(SLOT_INPUT).isEmpty() || recipeHolder == null) return false;

		ItemStack result = recipeHolder.value().getResultItem(registryAccess).copy();
		if (result.isEmpty()) return false;

		ItemStack currentResult = inv.get(SLOT_RESULT);
		if (currentResult.isEmpty()) return true;
		if (!ItemStack.isSameItem(currentResult, result)) return false;
		int totalCount = currentResult.getCount() + result.getCount();
		return totalCount <= maxStack && totalCount <= currentResult.getMaxStackSize();
	}

	private boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<DistillationRecipe> recipeHolder, NonNullList<ItemStack> inv, int maxStack) {
		if (recipeHolder == null || !canBurn(registryAccess, recipeHolder, inv, maxStack)) return false;

		DistillationRecipe recipe = recipeHolder.value();
		ItemStack input = inv.get(SLOT_INPUT);
		ItemStack recipeResult = recipe.getResultItem(registryAccess).copy();
		ItemStack resultStack = inv.get(SLOT_RESULT);
		if (resultStack.isEmpty()) {
			inv.set(SLOT_RESULT, recipeResult);
		} else {
			resultStack.grow(recipeResult.getCount());
		}

		DistillationConsumptionRules.Consumption consumption = DistillationConsumptionRules.forRecipe(
				recipe.consumesCatalyst(), recipe.requiresBloodInput());
		input.shrink(consumption.mainInput());
		inv.get(SLOT_CATALYST).shrink(consumption.catalyst());
		inv.get(SLOT_TINCTURE_BLOOD).shrink(consumption.bloodInput());
		return true;
	}

	// ---- Flask filling from stored blood ----

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (riteLocked) return;
		if (slot == SLOT_RESULT && !stack.isEmpty()) completedOperation = "";
		ItemStack existing = this.items.get(slot);
		boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack);
		this.items.set(slot, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
		// Reset cooking progress when input changes
		if ((slot == SLOT_INPUT || slot == SLOT_CATALYST || slot == SLOT_CATALYST_2 || slot == SLOT_TINCTURE_BLOOD) && !sameItem) {
			advancedMatch = null;
			advancedProgress = 0;
			this.cookingTotalTime = (this.level != null) ? getTotalCookTime(this.level, this) : BURN_TIME_STANDARD;
			this.cookingProgress = 0;
			this.setChanged();
		}
	}

	/**
	 * Called from serverTick — drains stored blood into flasks in the flask slot,
	 * one flask per tick if conditions are met.
	 */
	private static void tryDrainBloodIntoFlask(GhastlyAlembicBlockEntity te) {
		ItemStack flaskStack = te.items.get(SLOT_FLASK);
		if (flaskStack.isEmpty()) return;
		boolean jug = flaskStack.getItem() == ItemInit.cured_clay_jug.get();
		if (!jug && flaskStack.getItem() != HLItemInit.cured_clay_flask.get()) return;
		int requiredBlood = AlembicVesselRules.requiredBlood(jug
				? AlembicVesselRules.Vessel.JUG : AlembicVesselRules.Vessel.FLASK);
		Item outputItem = jug ? ItemInit.bloody_jug.get() : ItemInit.bloody_flask.get();

		IBloodVolume vol = te.resolveVolume();
		if (vol == null || vol.getBloodVolume() < requiredBlood) return;

		ItemStack resultStack = te.items.get(SLOT_FLASK_OUTPUT);
		if (resultStack.isEmpty()) {
			flaskStack.shrink(1);
			te.items.set(SLOT_FLASK_OUTPUT, new ItemStack(outputItem));
			vol.drain(requiredBlood);
			te.sendUpdates();
		} else if (resultStack.getItem() == outputItem
				&& resultStack.getCount() < resultStack.getMaxStackSize()) {
			flaskStack.shrink(1);
			resultStack.grow(1);
			vol.drain(requiredBlood);
			te.sendUpdates();
		}
	}

	/**
	 * Called from serverTick — consumes bloody flasks in the flask slot to fill
	 * the blood reservoir, outputting empty clay flasks to the flask output slot.
	 */
	private static void tryDrainBloodIntoGourd(GhastlyAlembicBlockEntity te) {
		if (BloodContainerTransfer.drainReservoirIntoGourdSlot(te, te, SLOT_FLASK_OUTPUT, 100D)) {
			te.sendUpdates();
		}
	}

	private static void tryFillBloodFromFlask(GhastlyAlembicBlockEntity te) {
		if (te.processBloodContainerInputSlot(te, te)) {
			te.sendUpdates();
		}
	}

	// ---- Menu creation ----

	@Override
	protected AbstractContainerMenu createMenu(int windowId, Inventory playerInv) {
		return new GhastlyAlembicMenu(windowId, playerInv, this, this.dataAccess);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.ghastly_alembic");
	}

	// ---- Blood helpers ----

	@Nullable
	public IBloodVolume getBloodCapability() {
		return resolveVolume();
	}

	@Override
	public int getBloodContainerInputSlot() {
		return SLOT_FLASK;
	}

	@Override
	public int getEmptyBloodContainerOutputSlot() {
		return SLOT_FLASK_OUTPUT;
	}

	public double getBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getBloodVolume() : 0;
	}

	public double getMaxBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getMaxBloodVolume() : 0;
	}

	public boolean isHeated() {
		return heated;
	}

	// ---- Experience / Recipe used ----

	public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
		List<RecipeHolder<?>> holders = Lists.newArrayList();
		for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
			player.serverLevel().getRecipeManager().byKey(entry.getKey()).ifPresent(holder -> {
				holders.add(holder);
				if (holder.value() instanceof DistillationRecipe gar) {
					createExperience(player.serverLevel(), player.position(), entry.getIntValue(), gar.getExperience());
				}
			});
		}
		player.awardRecipes(holders);
		this.recipesUsed.clear();
	}

	public void getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 pos) {
		for (Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
			level.getRecipeManager().byKey(entry.getKey()).ifPresent(holder -> {
				if (holder.value() instanceof DistillationRecipe recipe) {
					createExperience(level, pos, entry.getIntValue(), recipe.getExperience());
				}
			});
		}
		this.recipesUsed.clear();
	}

	@Override
	@Nullable
	public RecipeHolder<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
		if (recipe != null) {
			this.recipesUsed.addTo(recipe.id(), 1);
		}
	}

	// ---- Container impl ----

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.items) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (riteLocked) return ItemStack.EMPTY;
		return ContainerHelper.removeItem(this.items, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (riteLocked) return ItemStack.EMPTY;
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public boolean stillValid(Player player) {
		return !riteLocked && (this.level.getBlockEntity(this.worldPosition) == this)
				&& player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
				this.worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public void fillStackedContents(StackedContents contents) {
		for (ItemStack stack : this.items) {
			contents.accountStack(stack);
		}
	}

	// ---- WorldlyContainer ----

	@Override
	public int[] getSlotsForFace(Direction direction) {
		if (riteLocked) return new int[0];
		return switch (direction) {
			case UP -> SLOTS_FOR_UP;
			case DOWN -> SLOTS_FOR_DOWN;
			default -> tier.hasSecondCatalyst() ? SLOTS_FOR_SIDES_ATHANOR : SLOTS_FOR_SIDES;
		};
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (riteLocked) return false;
		if (slot == SLOT_RESULT) return false;
		if (slot == SLOT_FLASK_OUTPUT) return BloodContainerTransfer.isBloodGourd(stack);
		if (slot == SLOT_FLASK) {
			return stack.getItem() == HLItemInit.cured_clay_flask.get()
					|| stack.getItem() == ItemInit.cured_clay_jug.get()
					|| BloodContainerTransfer.isFilledBloodContainer(stack);
		}
		if (slot == SLOT_CATALYST) return true; // any item allowed as catalyst
		if (slot == SLOT_CATALYST_2) return tier.hasSecondCatalyst();
		if (slot == SLOT_TINCTURE_BLOOD) return stack.getItem() == ItemInit.bloody_flask.get()
				|| stack.getItem() == ItemInit.bloody_jug.get();
		return true; // SLOT_INPUT
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
		return !riteLocked && (slot == SLOT_RESULT || slot == SLOT_FLASK_OUTPUT);
	}

	// ---- Load / Save ----

	@Override
	public void onLoad() {
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setActive(true);
			vol.setMaxBloodVolume(AlembicVesselRules.requiredBlood(AlembicVesselRules.Vessel.JUG));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.items, registries);
		if (tag.contains("AlembicTier") && (tag.getInt("AlembicTier") < 0
				|| tag.getInt("AlembicTier") >= AlembicTier.values().length))
			com.vincenthuto.hemomancy.Hemomancy.LOGGER.warn(
					"Invalid Alembic tier {} at {}; loading as BASE", tag.getInt("AlembicTier"), worldPosition);
		this.tier = AlembicTier.fromSaved(tag.getInt("AlembicTier"));
		this.riteLocked = tag.getBoolean("RiteLocked");
		this.machineIdentity = tag.hasUUID("MachineIdentity") ? tag.getUUID("MachineIdentity") : java.util.UUID.randomUUID();
		this.lastUpgradeRite = tag.hasUUID("LastUpgradeRite") ? tag.getUUID("LastUpgradeRite") : null;
		this.advancedFeedback = tag.getString("AdvancedFeedback");
		this.advancedBloodCost = tag.getInt("AdvancedBloodCost");
		this.advancedTotalTicks = tag.getInt("AdvancedTotalTicks");
		this.advancedPreview = tag.contains("AdvancedPreview")
				? ItemStack.parseOptional(registries, tag.getCompound("AdvancedPreview")) : ItemStack.EMPTY;
		this.completedOperation = tag.getString("CompletedOperation");
		this.completedOutput = tag.contains("CompletedOutput")
				? ItemStack.parseOptional(registries, tag.getCompound("CompletedOutput")) : ItemStack.EMPTY;
		hiddenCatalystRecovery.clear();
		if (tag.contains("HiddenCatalystRecovery")) {
			ItemStack legacy = ItemStack.parseOptional(registries, tag.getCompound("HiddenCatalystRecovery"));
			if (!legacy.isEmpty()) hiddenCatalystRecovery.add(legacy);
		}
		ListTag recovered = tag.getList("HiddenCatalystRecoveries", Tag.TAG_COMPOUND);
		for (int i = 0; i < recovered.size(); i++) {
			ItemStack stack = ItemStack.parseOptional(registries, recovered.getCompound(i));
			if (!stack.isEmpty()) hiddenCatalystRecovery.add(stack);
		}
		if (!tier.hasSecondCatalyst() && !items.get(SLOT_CATALYST_2).isEmpty()) {
			ItemStack hidden = items.get(SLOT_CATALYST_2);
			items.set(SLOT_CATALYST_2, ItemStack.EMPTY);
			hiddenCatalystRecovery.add(hidden);
		}
		this.heated = tag.getBoolean("Heated");
		this.cookingProgress = tag.getInt("CookTime");
		this.cookingTotalTime = tag.getInt("CookTimeTotal");
		CompoundTag recipesTag = tag.getCompound("RecipesUsed");
		for (String s : recipesTag.getAllKeys()) {
			this.recipesUsed.put(ResourceLocation.parse(s), recipesTag.getInt(s));
		}
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putBoolean("Heated", this.heated);
		tag.putInt("AlembicTier", tier.ordinal());
		tag.putBoolean("RiteLocked", riteLocked);
		tag.putUUID("MachineIdentity", machineIdentity);
		if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
		tag.putString("AdvancedFeedback", advancedFeedback);
		tag.putInt("AdvancedBloodCost", advancedBloodCost);
		tag.putInt("AdvancedTotalTicks", advancedTotalTicks);
		if (!advancedPreview.isEmpty()) tag.put("AdvancedPreview", advancedPreview.save(registries));
		tag.putString("CompletedOperation", completedOperation);
		if (!completedOutput.isEmpty()) tag.put("CompletedOutput", completedOutput.save(registries));
		ListTag recovered = new ListTag();
		for (ItemStack stack : hiddenCatalystRecovery) if (!stack.isEmpty()) recovered.add(stack.save(registries));
		if (!recovered.isEmpty()) tag.put("HiddenCatalystRecoveries", recovered);
		tag.putInt("CookTime", this.cookingProgress);
		tag.putInt("CookTimeTotal", this.cookingTotalTime);
		ContainerHelper.saveAllItems(tag, this.items, registries);
		CompoundTag recipesTag = new CompoundTag();
		this.recipesUsed.forEach((key, val) -> recipesTag.putInt(key.toString(), val));
		tag.put("RecipesUsed", recipesTag);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
	}

	// ---- Sync ----

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("Heated", this.heated);
		tag.putInt("AlembicTier", tier.ordinal());
		tag.putBoolean("RiteLocked", riteLocked);
		tag.putUUID("MachineIdentity", machineIdentity);
		tag.putString("AdvancedFeedback", advancedFeedback);
		tag.putInt("AdvancedBloodCost", advancedBloodCost);
		tag.putInt("AdvancedTotalTicks", advancedTotalTicks);
		if (!advancedPreview.isEmpty()) tag.put("AdvancedPreview", advancedPreview.save(registries));
		tag.putInt("CookTime", this.cookingProgress);
		tag.putInt("CookTimeTotal", this.cookingTotalTime);
		ContainerHelper.saveAllItems(tag, this.items, registries);
		CompoundTag recipesTag = new CompoundTag();
		this.recipesUsed.forEach((key, val) -> recipesTag.putInt(key.toString(), val));
		tag.put("RecipesUsed", recipesTag);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		this.tier = AlembicTier.fromSaved(tag.getInt("AlembicTier"));
		this.riteLocked = tag.getBoolean("RiteLocked");
		if (tag.hasUUID("MachineIdentity")) this.machineIdentity = tag.getUUID("MachineIdentity");
		this.advancedFeedback = tag.getString("AdvancedFeedback");
		this.advancedBloodCost = tag.getInt("AdvancedBloodCost");
		this.advancedTotalTicks = tag.getInt("AdvancedTotalTicks");
		this.advancedPreview = tag.contains("AdvancedPreview")
				? ItemStack.parseOptional(registries, tag.getCompound("AdvancedPreview")) : ItemStack.EMPTY;
		if (tag != null) {
			IBloodVolume vol = resolveVolume();
			if (vol != null) {
				vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			}
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		if (pkt.getTag() != null) {
			IBloodVolume vol = resolveVolume();
			if (vol != null) {
				vol.setBloodVolume(pkt.getTag().getFloat(TAG_BLOOD_LEVEL));
			}
		}
	}

	@Override
	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	// ---- Alembic Leak ----

	/**
	 * Scans the 3x3 floor area one block below the alembic for a venous stone
	 * (any variant) or a {@link net.minecraft.world.level.block.Blocks#BONE_BLOCK}.
	 * When found, blood seeps onto that block and grows a {@link BloodCrystalBlock}
	 * in the open space immediately above it.
	 * <p>
	 * The wider scan avoids the fire / magma block that is typically placed directly
	 * under the alembic for heating. Each leak interval first tries to place a fresh
	 * crystal under any valid open trigger tile; only when no placement is possible does
	 * it search again for existing crystals to grow. If neither succeeds, no blood is
	 * spent and the next interval begins a fresh search.
	 * <p>
	 * Layout per trigger tile:
	 * {@code [blood_crystal at alembic Y] / [venous_stone or bone_block at alembic Y-1]}
	 */
	private static void tryLeakBloodOntoBlock(Level level, BlockPos pos,
			GhastlyAlembicBlockEntity te, IBloodVolume vol) {
		if (level.isClientSide) return;

		int interval = com.vincenthuto.hemomancy.config.HemoServerConfig.ALEMBIC_LEAK_INTERVAL_TICKS.get();
		if (level.getGameTime() % interval != 0) return;

		double leakRate = com.vincenthuto.hemomancy.config.HemoServerConfig.ALEMBIC_LEAK_RATE_PER_TICK.get();
		if (vol.getBloodVolume() < leakRate) return;

		// Scan the 3x3 ring at Y-1 (the floor row directly below the alembic).
		// The centre is skipped because the crystal position above it is occupied by the alembic.
		int[][] offsets = new int[][]{
				{-1, -1}, {0, -1}, {1, -1},
				{-1, 0},           {1, 0},
				{-1, 1},  {0, 1},  {1, 1}
		};
		List<BlockPos> triggerPositions = Lists.newArrayList();
		for (int[] offset : offsets) {
			BlockPos basePos = pos.offset(offset[0], -1, offset[1]);
			BlockState baseState = level.getBlockState(basePos);
			boolean isVenous = baseState.is(BlockInit.venous_stone.get())
					|| baseState.is(BlockInit.polished_venous_stone.get())
					|| baseState.is(BlockInit.gilded_venous_stone.get());
			boolean isBoneBlock = baseState.is(net.minecraft.world.level.block.Blocks.BONE_BLOCK);
			if (isVenous || isBoneBlock) {
				triggerPositions.add(basePos);
			}
		}

		BlockState freshCrystalState = BlockInit.blood_crystal.get().defaultBlockState()
				.setValue(BloodCrystalBlock.FACING, Direction.UP)
				.setValue(BloodCrystalBlock.AGE, 0);

		// Pass 1: place a fresh crystal on top of the first valid open trigger tile.
		for (BlockPos basePos : triggerPositions) {
			BlockPos crystalPos = basePos.above();
			BlockState existingState = level.getBlockState(crystalPos);
			if (!existingState.canBeReplaced()) continue;
			if (!freshCrystalState.canSurvive(level, crystalPos)) continue;

			level.setBlock(crystalPos, freshCrystalState, Block.UPDATE_ALL);
			vol.drain(leakRate);
			te.sendUpdates();
			return;
		}

		// Pass 2: only when no fresh placement is possible, grow an existing crystal.
		for (BlockPos basePos : triggerPositions) {
			BlockPos crystalPos = basePos.above();
			BlockState existingState = level.getBlockState(crystalPos);
			if (!existingState.is(BlockInit.blood_crystal.get())) continue;
			if (existingState.getValue(BloodCrystalBlock.FACING) != Direction.UP) continue;

			if (BlockInit.blood_crystal.get() instanceof BloodCrystalBlock crystal
					&& crystal.tryGrow(level, crystalPos, existingState, level.getRandom())) {
				vol.drain(leakRate);
				te.sendUpdates();
				return;
			}
		}
	}

}
