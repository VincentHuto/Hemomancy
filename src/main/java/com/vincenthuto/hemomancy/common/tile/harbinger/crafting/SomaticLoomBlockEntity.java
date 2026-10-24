package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomFrame;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramAim;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoardGenerator;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramRecollections;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveSession;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SomaticLoomBlockEntity extends HemoBlockEntity implements IBloodReservoir, IMultiBlockEntity, UpgradeableStation {
	private static final BloodAccounting.Source RITUAL_CHARGE = BloodAccounting.Source.of("somatic_loom", Category.STATION, false, false);
	private static final BloodAccounting.Source BURNT_SPANS = BloodAccounting.Source.of("loom_burnt_spans", Category.STATION, false, false);

	private static final String TAG_CONTENT_SIZE = "contentSize";
	private static final String TAG_BLOOD_LEVEL = "bloodLevel";
	private static final String TAG_RECIPE = "recipe";
	private static final String TAG_CRAFT_PHASE = "craftPhase";
	private static final String TAG_RITUAL_BLOOD_CHARGED = "ritualBloodCharged";
	private static final String TAG_STORED_ENZYMES = "storedEnzymes";
	private static final String TAG_LEGACY_WEAVE_THREADS = "weaveThreads";
	private static final String TAG_ENGRAM = "engram";
	private static final String TAG_WEAVER = "weaver";
	private static final String TAG_WEAVE_TICKS = "weaveTicks";
	private static final String TAG_CLIMAX_TICKS = "climaxTicks";
	private static final String TAG_PAUSED_TICKS = "pausedTicks";
	private static final String TAG_LEGACY_SPUN = "spun";
	/** 2: stored enzyme is kept in spans of thread and a paid weave is an engram. */
	private static final int SAVE_VERSION = 2;
	private static final String TAG_LEGACY_RITUAL_ORBS = "ritualOrbs";

	public static final int PHASE_IDLE = 0;
	public static final int PHASE_AWAITING_BLOOD = 1;
	public static final int PHASE_WEAVING = 2;
	/** Every fragment is home: the echo grows whole and the memory crystallizes. */
	public static final int PHASE_CLIMAX = 3;

	/** Spans of thread each tendency's vial holds: sixty-four enzymes, three spans each. */
	public static final int ENZYME_CAPACITY = EngramWeaveRules.SPAN_CAPACITY;
	/** Where the engram is centred, in the north-facing model's pixels: the middle of the loom's footprint. */
	private static final double SIGIL_X = 8.0D, SIGIL_Z = 8.0D;
	/** Soot shed by a strand as it is drawn home into the warp. */
	private static final ParticleColor STRAND_SOOT = new ParticleColor(22, 2, 6);

	public NonNullList<ItemStack> contents = createMutableContents(1);

	IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(this).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(this).orElseThrow(IllegalStateException::new);

	private final EnumMap<EnumBloodTendency, Integer> storedEnzymes =
			MemoryWeavingRecipe.blankEnzymeRequirements();
	/** The live engram: its board, the lane in the weaver's staff, and the beats clients replay. */
	private final EngramWeaveSession engram = new EngramWeaveSession();
	/** The engram a payer would recall for the selected memory, kept while their blood is projected. */
	@Nullable private EngramBoard pendingBoard;
	private String pendingKey = "";
	@Nullable private UUID weaver;
	/** The weaver as last seen; players not on the server list (fakes, test fixtures) resolve through it. */
	@Nullable private Player weaverRef;
	/** The weaver's Chamber of Will sky, which a Cortical loom raises as a dome around the weave. */
	@Nullable private ResourceLocation weaverTheme;
	/** Counted only while the weave runs, so pausing freezes the echo's loop in place. */
	private long weaveTicks;
	private int climaxTicks;
	private int pausedTicks;
	private boolean paused;
	/** Client-only: the last engram beat this client has played, so each landing, snap or scar plays once. */
	private int beatSeen = -1;
	/** Client-smoothed thread on the pirns and vial stock, in spans, so threading visibly draws liquid up into thread. */
	private final float[] shownSpun = new float[EnumBloodTendency.values().length];
	private final float[] shownStock = new float[EnumBloodTendency.values().length];
	private boolean echoClimaxFired;

	private MemoryWeavingRecipe curRecipe = null;
	private String recipePath = "";
	private String selectedRecipeId = "";
	private CompoundTag committedRecipe = new CompoundTag();
	private boolean recoveryRequired;
	/** Shared station-upgrade bookkeeping: a rite holds the loom still, and remembers which rite raised it. */
	private boolean riteLocked;
	private UUID machineIdentity = UUID.randomUUID();
	@Nullable private UUID lastUpgradeRite;
	private int craftingPhase = PHASE_IDLE;
	private double ritualBloodCharged = 0.0D;

	public SomaticLoomBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.somatic_loom.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state, SomaticLoomBlockEntity self) {
		self.trackEngram();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int i = tend.ordinal();
			self.shownSpun[i] += (self.getSpun(tend) - self.shownSpun[i]) * 0.08F;
			self.shownStock[i] += (self.getStoredEnzyme(tend) - self.shownStock[i]) * 0.08F;
		}
		if (self.craftingPhase == PHASE_WEAVING && !self.paused) {
			self.weaveTicks++;
			// Motes of the memory drift up off the echo while it is woven.
			BloodManipulation manip = self.getWeaveManipulation();
			if (manip != null && self.weaveTicks % 5 == 0) {
				Vec3 at = self.echoAnchor();
				double dx = (level.random.nextDouble() - 0.5D) * 0.4D, dz = (level.random.nextDouble() - 0.5D) * 0.4D;
				level.addParticle(HemoParticleData.glow(manip.getTend().getColor()), at.x + dx, at.y + 0.1D + level.random.nextDouble() * 0.6D,
						at.z + dz, 0.0D, 0.015D, 0.0D);
			}
		}
		else if (self.craftingPhase == PHASE_CLIMAX) self.climaxTicks++;
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, SomaticLoomBlockEntity self) {
		self.trackEngram();
		if (self.curRecipe == null || level.getGameTime() % 20L == 0L) self.refreshRecipe();
		if (level instanceof ServerLevel server && self.isWeaving() && !self.recoveryRequired) self.tickWeave(server);
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		if (stack.isEmpty() || refuseWhileRiteHeld(player)) {
			return false;
		}

		if (stack.getItem() instanceof EnzymeItem enzyme) {
			depositEnzyme(player, stack, enzyme, player != null && player.isShiftKeyDown());
			return true;
		}

		var upgrade = StationUpgradeCatalog.all().stream().filter(tier -> tier.station() == UpgradeStation.LOOM
				&& tier.upgradeItem().equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()))).findFirst();
		if (upgrade.isPresent()) {
			// Kits are installed by their rite, never woven: keep them out of the catalyst row.
			msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.install_this_at_the_with_the", (upgrade.get().tier() == 1 ? Component.translatable("message.hemomancy.somatic_loom_block_entity.rite_of_the_ganglionic_weave")
					: Component.translatable("message.hemomancy.somatic_loom_block_entity.rite_of_the_cortical_weave"))), ChatFormatting.YELLOW, true);
			return false;
		}

		if (stack.getItem() instanceof RecycledEnzymeItem) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.recycled_enzymes_cannot_be_loaded_into"), ChatFormatting.GRAY, true);
			return false;
		}

		if (contents.get(0).isEmpty() && stack.getItem() == ItemInit.hematic_memory.get()) {
			placeMemory(player, stack, hand);
			return true;
		}

		if (stack.getItem() == ItemInit.lethean_dew.get()) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_stored_enzymes_are_already_bound"), ChatFormatting.GRAY, true);
			return false;
		}

		if (ritualBloodCharged > 0 || isWeaving()) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.cancel_the_paid_weave_before_changing"), ChatFormatting.YELLOW, true);
			return false;
		}
		placeCatalyst(player, stack, hand);
		return true;
	}

	public boolean removeItem(@Nullable Player player, boolean removeMemory) {
		if (player == null || refuseWhileRiteHeld(player)) return false;
		if (removeMemory) {
			if (contents.get(0).isEmpty()) return false;
			player.getInventory().placeItemBackInInventory(contents.get(0).copy());
			contents.set(0, ItemStack.EMPTY);
		} else {
			int slot = lastCatalystSlot();
			if (slot < 1) return false;
			player.getInventory().placeItemBackInInventory(contents.get(slot).copy());
			contents.remove(slot);
		}
		clearActiveRitual(false);
		refreshRecipe();
		BlockEntitySync.markAndSync(this);
		return true;
	}

	public void dropContents() {
		if (level == null || level.isClientSide) return;
		for (ItemStack stack : contents) {
			if (!stack.isEmpty()) {
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
						worldPosition.getZ(), stack);
			}
		}
		contents = createMutableContents(1);
	}

	private void depositEnzyme(@Nullable Player player, ItemStack stack, EnzymeItem enzyme, boolean bulk) {
		EnumBloodTendency tend = enzyme.getTend();
		int current = storedEnzymes.getOrDefault(tend, 0);
		// Each enzyme dissolves into three spans of thread; the vial takes only whole enzymes.
		int room = (ENZYME_CAPACITY - current) / EngramWeaveRules.SPANS_PER_ENZYME;
		if (room <= 0) {
			msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.enzyme_reservoir_is_full", BloodManipulation.tendencyName(tend)), ChatFormatting.DARK_RED, true);
			return;
		}
		int amount = bulk ? Math.min(room, stack.getCount()) : 1;
		storedEnzymes.put(tend, current + amount * EngramWeaveRules.SPANS_PER_ENZYME);
		stack.shrink(amount);
		msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.thread_spans", BloodManipulation.tendencyName(tend), storedEnzymes.get(tend), ENZYME_CAPACITY),
				ChatFormatting.DARK_PURPLE, true);
		refreshRecipe();
		BlockEntitySync.markAndSync(this);
	}

	private void placeMemory(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		ItemStack toPlace = stack.copy();
		toPlace.setCount(1);
		contents.set(0, toPlace);
		consumeItem(player, stack, hand);
		resetEditableRitualProgress();
		refreshRecipe();
		provideMissingEnzymeFeedback(player);
		BlockEntitySync.markAndSync(this);
	}

	private void placeCatalyst(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		ItemStack toPlace = stack.copy();
		toPlace.setCount(1);
		contents.add(toPlace);
		consumeItem(player, stack, hand);
		resetEditableRitualProgress();
		refreshRecipe();
		provideMissingEnzymeFeedback(player);
		BlockEntitySync.markAndSync(this);
	}

	private void consumeItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		if (player != null && player.getAbilities().instabuild) return;
		stack.shrink(1);
		if (stack.isEmpty() && player != null && hand != null) {
			player.setItemInHand(hand, ItemStack.EMPTY);
		}
	}

	private void resetEditableRitualProgress() {
		if (craftingPhase == PHASE_AWAITING_BLOOD) {
			ritualBloodCharged = 0.0D;
		}
	}

	public void refreshRecipe() {
		if (level == null) return;
		curRecipe = null;
		// Older saves named the output, not the recipe. Recover only one exact compatible candidate.
		if (selectedRecipeId.isEmpty() && !recipePath.isEmpty()) {
			var matches = recipeChoices().stream().filter(holder -> recipePath.equals(HLTextUtils.getItemRegistryName(
					holder.value().getResultItem(level.registryAccess()).getItem()))).toList();
			if (matches.size() == 1) selectedRecipeId = matches.getFirst().id().toString();
		}
		ResourceLocation id = ResourceLocation.tryParse(selectedRecipeId);
		var holder = id == null ? null : level.getRecipeManager().byKey(id).orElse(null);
		if (holder != null && holder.value() instanceof MemoryWeavingRecipe recipe
				&& !contents.get(0).isEmpty() && catalystsMatch(recipe)
				&& (ritualBloodCharged > 0 || isWeaving() || stageAllows(recipe))) {
			if (!committedRecipe.isEmpty() && !committedRecipe.equals(recipeCommitment(recipe))) {
				recoveryRequired = true;
				return;
			}
			curRecipe = recipe;
			recipePath = HLTextUtils.getItemRegistryName(recipe.getResultItem(level.registryAccess()).getItem());
			if (craftingPhase == PHASE_IDLE) craftingPhase = PHASE_AWAITING_BLOOD;
			if (ritualBloodCharged > 0 && committedRecipe.isEmpty()) committedRecipe = recipeCommitment(recipe);
			recoveryRequired = false;
		} else if (ritualBloodCharged > 0 || isWeaving()) {
			recoveryRequired = true;
		} else {
			craftingPhase = PHASE_IDLE;
			recoveryRequired = false;
		}
	}

	private List<RecipeHolder<MemoryWeavingRecipe>> recipeChoices() {
		if (level == null || contents.get(0).isEmpty()) return List.of();
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get()).stream()
				.filter(holder -> catalystsMatch(holder.value()))
				.sorted(java.util.Comparator.comparing(holder -> holder.id().toString())).toList();
	}

	/** An empty-hand click deliberately cycles matching outputs; stocking enzymes never selects one. */
	public void chooseNextRecipe(Player player) {
		if (refuseWhileRiteHeld(player)) return;
		if (ritualBloodCharged > 0 || isWeaving()) {
			provideTendencyFeedback(player);
			return;
		}
		var choices = recipeChoices();
		if (choices.isEmpty()) { provideTendencyFeedback(player); return; }
		int current = -1;
		for (int i = 0; i < choices.size(); i++) {
			if (choices.get(i).id().toString().equals(selectedRecipeId)) current = i;
		}
		var next = choices.get((current + 1) % choices.size());
		if (!stageAllows(next.value())) {
			// Cycling still lands on a memory this stage cannot weave, so the next click moves past it; refreshRecipe
			// holds it back and payment refuses it.
			selectedRecipeId = next.id().toString();
			committedRecipe = new CompoundTag();
			refreshRecipe();
			msg(player, tierRequired(next.value()), ChatFormatting.YELLOW, true);
			BlockEntitySync.markAndSync(this);
			return;
		}
		selectRecipe(player, next.id());
	}

	public boolean selectRecipe(@Nullable Player player, ResourceLocation id) {
		if (level == null || level.isClientSide || riteLocked || ritualBloodCharged > 0 || isWeaving()) return false;
		var choice = recipeChoices().stream().filter(holder -> holder.id().equals(id)).findFirst();
		if (choice.isEmpty()) return false;
		if (!stageAllows(choice.get().value())) {
			msg(player, tierRequired(choice.get().value()), ChatFormatting.YELLOW, false);
			return false;
		}
		selectedRecipeId = id.toString();
		committedRecipe = new CompoundTag();
		refreshRecipe();
		provideTendencyFeedback(player);
		BlockEntitySync.markAndSync(this);
		return true;
	}

	private CompoundTag recipeCommitment(MemoryWeavingRecipe recipe) {
		CompoundTag tag = new CompoundTag();
		tag.put("result", recipe.getResultItem(level.registryAccess()).saveOptional(level.registryAccess()));
		tag.putDouble("blood", recipe.getBloodCost());
		for (EnumBloodTendency tend : EnumBloodTendency.values()) tag.putInt(tend.name(), recipe.getEnzymeRequirement(tend));
		return tag;
	}

	private boolean catalystsMatch(MemoryWeavingRecipe recipe) {
		List<Ingredient> required = recipe.getCatalysts();
		List<ItemStack> placed = getCatalysts();
		if (required.size() != placed.size()) {
			return false;
		}
		boolean[] used = new boolean[placed.size()];
		for (Ingredient ingredient : required) {
			boolean matched = false;
			for (int i = 0; i < placed.size(); i++) {
				if (!used[i] && ingredient.test(placed.get(i))) {
					used[i] = true;
					matched = true;
					break;
				}
			}
			if (!matched) {
				return false;
			}
		}
		return true;
	}

	/** Enough thread for the shortest possible engram: three spans per fragment. The payer's own engram may need more. */
	private boolean hasStoredEnzymesFor(MemoryWeavingRecipe recipe) {
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			if (storedEnzymes.getOrDefault(tend, 0) < minimumSpans(recipe, tend)) {
				return false;
			}
		}
		return true;
	}

	private static int minimumSpans(MemoryWeavingRecipe recipe, EnumBloodTendency tend) {
		return recipe.getEnzymeRequirement(tend) * EngramWeaveRules.SPANS_PER_ENZYME;
	}

	public boolean isCrafting() {
		return craftingPhase != PHASE_IDLE;
	}

	public boolean isAwaitingBlood() {
		return craftingPhase == PHASE_AWAITING_BLOOD && curRecipe != null;
	}

	public boolean hasValidRecipe() {
		return curRecipe != null;
	}

	public int getCraftingProgress() {
		if (curRecipe == null || curRecipe.getBloodCost() <= 0) {
			return 0;
		}
		return (int) Math.max(0, curRecipe.getBloodCost() - ritualBloodCharged);
	}

	public int getCraftingTotalTime() {
		return curRecipe == null ? 0 : (int) curRecipe.getBloodCost();
	}

	public int getCraftingPhase() {
		return craftingPhase;
	}

	public MemoryWeavingRecipe getCurRecipe() {
		return curRecipe;
	}

	public MemoryWeavingRecipe getPreviewRecipe() {
		if (level == null || contents.get(0).isEmpty() || recoveryRequired) return null;
		ResourceLocation id = ResourceLocation.tryParse(selectedRecipeId);
		var holder = id == null ? null : level.getRecipeManager().byKey(id).orElse(null);
		if (holder == null || !(holder.value() instanceof MemoryWeavingRecipe recipe) || !catalystsMatch(recipe)) return null;
		return committedRecipe.isEmpty() || committedRecipe.equals(recipeCommitment(recipe)) ? recipe : null;
	}
	public String getRecipePath() {
		return recipePath;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	public double getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	public double getRitualBloodCharged() {
		return ritualBloodCharged;
	}

	public double getRitualBloodRequired() {
		return curRecipe == null ? 0.0D : curRecipe.getBloodCost();
	}

	public IBloodTendency getTendCapability() {
		return tendency;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		Map<EnumBloodTendency, Float> view = MemoryWeavingRecipe.blank();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			view.put(tend, (float) storedEnzymes.getOrDefault(tend, 0));
		}
		return view;
	}

	public Map<EnumBloodTendency, Integer> getStoredEnzymes() {
		return Map.copyOf(storedEnzymes);
	}

	public int getStoredEnzyme(EnumBloodTendency tendency) {
		return storedEnzymes.getOrDefault(tendency, 0);
	}

	public ItemStack getResultItem() {
		return curRecipe != null && level != null
				? curRecipe.getResultItem(level.registryAccess()) : ItemStack.EMPTY;
	}

	public boolean startRitual(Player player) {
		if (isWeaving() || ritualBloodCharged > 0) return false;
		refreshRecipe();
		if (curRecipe == null) {
			return false;
		}
		craftingPhase = PHASE_AWAITING_BLOOD;
		msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_loom_darkens_waiting_for_blood"), ChatFormatting.DARK_RED, true);
		BlockEntitySync.markAndSync(this);
		return true;
	}

	/** Projected blood only charges a memory awaiting blood; the loom's tank is never filled by projection. */
	@Override
	public double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
		double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		if (!tryChargeRitualBlood(player, rate, livingStaff)) return 0.0D;
		return Math.max(0.0D, before - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume());
	}

	public boolean tryChargeRitualBlood(Player player, double maxAmount, boolean livingStaff) {
		if (player == null || level == null || refuseWhileRiteHeld(player)) return false;
		refreshRecipe();

		if (isWeaving() && curRecipe != null && !recoveryRequired) return false;

		if (curRecipe == null || recoveryRequired || craftingPhase != PHASE_AWAITING_BLOOD) {
			provideTendencyFeedback(player);
			return false;
		}
		if (level.isClientSide && !hasStoredEnzymesFor(curRecipe)) {
			provideMissingEnzymeFeedback(player);
			return false;
		}
		if (!level.isClientSide) {
			// The payer recalls their own engram; its reference lanes must be affordable before any blood is taken.
			// Plying can make that cheaper than three spans a fragment, so the engram, not the recipe, decides.
			EngramBoard board = engramFor(player);
			int lacking = EngramWeaveRules.shortTendency(storedSpans(), board.reference());
			if (lacking >= 0) {
				EnumBloodTendency tend = EnumBloodTendency.values()[lacking];
				int missing = board.reference()[lacking] - storedEnzymes.getOrDefault(tend, 0);
				msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.the_engram_this_memory_recalls_in",
						missing,
						EnumBloodTendency.getRepEnzyme(tend).getDescription()), ChatFormatting.YELLOW, true);
				return false;
			}
		}

		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player);
		if (playerVolume == null || !playerVolume.isActive() || playerVolume.getBloodVolume() <= 0) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.no_blood_answers_the_loom"), ChatFormatting.DARK_RED, true);
			return false;
		}

		double needed = Math.max(0.0D, curRecipe.getBloodCost() - ritualBloodCharged);
		if (needed <= 0.0D) {
			beginWeave(player);
			return true;
		}

		double amount = Math.min(Math.min(maxAmount, needed), playerVolume.getBloodVolume());
		if (amount <= 0.0D) {
			return false;
		}
		if (committedRecipe.isEmpty()) committedRecipe = recipeCommitment(curRecipe);
		BloodAccounting.pay(player, amount, RITUAL_CHARGE);
		ritualBloodCharged += amount;

		if (livingStaff) {
			playSound(SoundEvents.BEACON_POWER_SELECT, 0.35f, 0.8f);
		}
		if (ritualBloodCharged >= curRecipe.getBloodCost() - 0.001D) {
			beginWeave(player);
		} else if (level.getGameTime() % 10L == 0L) {
			msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.blood_charge", (int) ritualBloodCharged, (int) curRecipe.getBloodCost()),
					ChatFormatting.DARK_RED, true);
		}
		BlockEntitySync.markAndSync(this);
		return true;
	}

	public void cancelRitual(@Nullable Player player) {
		if (!isCrafting() || refuseWhileRiteHeld(player)) return;
		if (player != null) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_engram_withdraws_into_the_loom"), ChatFormatting.RED, true);
		}
		playSound(SoundEvents.BEACON_DEACTIVATE, 0.7f, 0.8f);
		clearActiveRitual(true);
		BlockEntitySync.markAndSync(this);
	}

	private void beginWeave(Player player) {
		if (curRecipe == null || level == null) return;
		craftingPhase = PHASE_WEAVING;
		weaver = player.getUUID();
		weaverRef = player;
		weaverTheme = chamberThemeOf(player.getUUID());
		weaveTicks = 0;
		climaxTicks = 0;
		pausedTicks = 0;
		paused = false;
		EngramBoard board = EngramBoard.load(engramFor(player).save());
		dressIntrusions(board, player.getUUID());
		engram.start(board);
		msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_engram_unfurls_around_the_loom"),
				ChatFormatting.DARK_PURPLE, true);
		playSound(SoundEvents.END_PORTAL_FRAME_FILL, 0.8f, 0.55f);
		if (level instanceof ServerLevel serverLevel)
			PacketHandler.sendMonolithShatterBurst(Vec3.atCenterOf(worldPosition), 48.0D, serverLevel);
		BlockEntitySync.markAndSync(this);
	}

	private void tickWeave(ServerLevel server) {
		if (craftingPhase == PHASE_CLIMAX) {
			if (++climaxTicks >= SomaticWeaveRules.CLIMAX_TICKS) tickCompletion(weaverPlayer(server));
			return;
		}
		if (weaver == null) adoptNearestWeaver(server);
		Player player = weaverPlayer(server);
		boolean present = player != null && SomaticWeaveRules.weaverPresent(player.isAlive(),
				player.level() == server, player.distanceToSqr(Vec3.atCenterOf(worldPosition)));
		if (pausedTicks > 0) pausedTicks--;
		boolean shouldPause = !present || pausedTicks > 0;
		if (shouldPause != paused) {
			paused = shouldPause;
			if (paused) engram.release();
			BlockEntitySync.markAndSync(this);
		}
		if (paused) return;
		if (!engram.live()) recallMigratedEngram(player);
		weaveTicks++;
	}

	/** A weave paid before engrams existed recalls one now, for its weaver, with its vials topped up to the reference. */
	private void recallMigratedEngram(@Nullable Player player) {
		if (player == null || curRecipe == null || level == null) return;
		EngramBoard board = EngramBoard.load(engramFor(player).save());
		int[] reference = board.reference();
		for (EnumBloodTendency tend : EnumBloodTendency.values())
			storedEnzymes.put(tend, Math.min(ENZYME_CAPACITY, Math.max(storedEnzymes.getOrDefault(tend, 0), reference[tend.ordinal()])));
		dressIntrusions(board, player.getUUID());
		engram.start(board);
		BlockEntitySync.markAndSync(this);
	}

	/** A weave carried over from an older save, or whose weaver is unknown, is taken up by the nearest player. */
	private void adoptNearestWeaver(ServerLevel server) {
		Vec3 centre = Vec3.atCenterOf(worldPosition);
		Player nearest = server.getNearestPlayer(centre.x, centre.y, centre.z, SomaticWeaveRules.RITUAL_RADIUS,
				p -> p.isAlive() && !p.isSpectator());
		if (nearest != null) {
			weaver = nearest.getUUID();
			weaverRef = nearest;
			weaverTheme = chamberThemeOf(weaver);
			BlockEntitySync.markAndSync(this);
		}
	}

	//  Engram Weaving: the star unfurled around the loom and threaded with the weaver's Living Staff

	/** The engram a payer recalls for the selected memory: generated once per payer and kept while blood is projected. */
	private EngramBoard engramFor(Player player) {
		String key = selectedRecipeId + "|" + player.getUUID();
		if (pendingBoard == null || !key.equals(pendingKey)) {
			long worldSeed = level instanceof ServerLevel server ? server.getSeed() : 0L;
			pendingBoard = EngramBoardGenerator.generate(EngramBoardGenerator.seed(worldSeed, selectedRecipeId, player.getUUID()),
					enzymeCounts(curRecipe), rankOf(curRecipe));
			pendingKey = key;
		}
		return pendingBoard;
	}

	private static int[] enzymeCounts(MemoryWeavingRecipe recipe) {
		int[] counts = new int[EnumBloodTendency.values().length];
		if (recipe == null) return counts;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) counts[tend.ordinal()] = recipe.getEnzymeRequirement(tend);
		return counts;
	}

	private int[] storedSpans() {
		int[] stock = new int[EnumBloodTendency.values().length];
		for (EnumBloodTendency tend : EnumBloodTendency.values()) stock[tend.ordinal()] = storedEnzymes.getOrDefault(tend, 0);
		return stock;
	}

	/** False fragments replay another memory of their tendency; a bloodline that already wove this one marks them. */
	private void dressIntrusions(EngramBoard board, UUID weaverId) {
		java.util.Random random = new java.util.Random(pendingKey.hashCode());
		for (int i = 0; i < board.fragmentCount(); i++) {
			EngramBoard.Fragment fragment = board.fragment(i);
			if (fragment.intrusion() && fragment.decoy().isEmpty()) board.setDecoy(i, decoyFor(fragment.colour(), random));
		}
		if (level instanceof ServerLevel server
				&& EngramRecollections.get(server).bloodlineRemembers(server, weaverId, selectedRecipeId))
			board.premarkIntrusions();
	}

	private String decoyFor(int colour, java.util.Random random) {
		BloodManipulation own = getWeaveManipulation();
		List<BloodManipulation> same = new ArrayList<>(), any = new ArrayList<>();
		for (BloodManipulation manip : com.vincenthuto.hemomancy.common.init.ManipulationInit.getAllEntries()) {
			if (own != null && manip.getName().equals(own.getName())) continue;
			any.add(manip);
			if (manip.getTend().ordinal() == colour) same.add(manip);
		}
		List<BloodManipulation> pool = same.isEmpty() ? any : same;
		pool.sort(java.util.Comparator.comparing(BloodManipulation::getName));
		return pool.isEmpty() ? "" : pool.get(random.nextInt(pool.size())).getName();
	}

	/**
	 * The weaver's Living Staff on `node`: a press takes a lane or threads the held one there, holding sweeps it on,
	 * a sneaking press cuts it back. Nobody else threads, and nothing threads while the weave is paused.
	 */
	public EngramWeaveRules.Step engramStep(Player player, int node, boolean sneaking, boolean press) {
		if (!(level instanceof ServerLevel server) || craftingPhase != PHASE_WEAVING || paused || !engram.live()
				|| curRecipe == null || weaver == null || !weaver.equals(player.getUUID())) return EngramWeaveRules.Step.IDLE;
		int serial = engram.serial();
		EngramWeaveSession.Result result = engram.step(node, sneaking, press, storedSpans());
		if (result.changed()) {
			playTraceFeedback(server, node, result.step(), serial);
			settleIntrusions(server, player);
			if (EngramWeaveRules.complete(engram.board(), enzymeCounts(curRecipe))) completeEngram(server);
		}
		if (result.step() == EngramWeaveRules.Step.NO_STOCK && engram.serial() != serial) {
			int colour = EngramWeaveRules.payColour(engram.board(), result.fragment());
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_vial_is_out_of_thread",
					EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[Math.max(0, colour)]).getDescription()), ChatFormatting.YELLOW, true);
		}
		if (result.changed() || engram.serial() != serial || press) BlockEntitySync.markAndSync(this);
		return result.step();
	}

	/** The weaver let go of the staff: no lane is held until the next press. */
	public void releaseEngram(Player player) {
		if (weaver != null && weaver.equals(player.getUUID()) && engram.active() >= 0) {
			engram.release();
			BlockEntitySync.markAndSync(this);
		}
	}

	private void playTraceFeedback(ServerLevel server, int node, EngramWeaveRules.Step step, int since) {
		Vec3 at = nodeWorld(node);
		if (step == EngramWeaveRules.Step.RETRACTED)
			server.playSound(null, at.x, at.y, at.z, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.5F, 1.3F);
		else
			server.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.45F,
					0.7F + server.random.nextFloat() * 0.3F);
		for (EngramWeaveSession.Beat beat : engram.beats())
			if (beat.serial() > since && beat.event() == EngramWeaveSession.Event.LAND) playWeaveFeedback(
					EnumBloodTendency.values()[Math.max(0, EngramWeaveRules.payColour(engram.board(), beat.fragment()))]);
	}

	/** Every false lane that reached home tears loose and costs what its rank asks. */
	private void settleIntrusions(ServerLevel server, Player player) {
		EngramBoard board = engram.board();
		for (int fragment : EngramWeaveRules.connectedIntrusions(board)) {
			EngramBoard.Fragment lie = board.fragment(fragment);
			EnumBloodTendency tend = EnumBloodTendency.values()[Math.max(0, lie.colour())];
			int spans = engram.snap(fragment);
			Vec3 at = nodeWorld(lie.node());
			server.playSound(null, at.x, at.y, at.z, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0F, 0.5F);
			server.playSound(null, at.x, at.y, at.z, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.35F, 1.6F);
			switch (EngramWeaveRules.stakeFor(getWeaveRank())) {
				case BURN -> burnSpans(player, tend, spans);
				case PRESSURE -> addPressure(server, player);
				case BITE -> {
					com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity.spawnIntrusion(server, this, tend, at, getWeaveRank());
					msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.a_failed_harbinger_s_memory_tears"), ChatFormatting.DARK_RED, true);
				}
				case NONE -> {
				}
			}
		}
	}

	/** A Mediocritas mistake burns the false lane's spans from what the vials hold beyond the reserve; blood pays the rest. */
	private void burnSpans(Player player, EnumBloodTendency tend, int spans) {
		EngramBoard board = engram.board();
		int stock = storedEnzymes.getOrDefault(tend, 0);
		int burnt = EngramWeaveRules.burnable(spans, stock, board.reference()[tend.ordinal()],
				EngramWeaveRules.committed(board, tend.ordinal()));
		storedEnzymes.put(tend, stock - burnt);
		int unpaid = spans - burnt;
		if (unpaid > 0 && player instanceof ServerPlayer serverPlayer) {
			var volume = HemoCapabilityAccess.getBloodVolume(serverPlayer);
			if (volume.isActive())
				BloodAccounting.pay(serverPlayer,
						Math.min(volume.getBloodVolume(), unpaid * EngramWeaveRules.BURN_BLOOD_PER_SPAN), BURNT_SPANS);
		}
		msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.the_false_memory_burns_spans_of", spans, BloodManipulation.tendencyName(tend), (unpaid > 0 ? Component.translatable("message.hemomancy.somatic_loom_block_entity.and_takes_the_rest_from_your_blood")
				: Component.literal("."))), ChatFormatting.RED, true);
	}

	/** Summa and Magister mistakes build fungal pressure; when it boils over the engram's flesh scars or recalls a lie. */
	private void addPressure(ServerLevel server, Player player) {
		EngramBoard board = engram.board();
		board.setPressure(board.pressure() + 1);
		if (board.pressure() < EngramWeaveRules.PRESSURE_LIMIT) {
			msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.fungal_pressure_gathers_in_the_engram", board.pressure(), EngramWeaveRules.PRESSURE_LIMIT),
					ChatFormatting.DARK_GREEN, true);
			return;
		}
		board.setPressure(0);
		java.util.Random random = new java.util.Random(server.getGameTime() ^ worldPosition.asLong());
		int colour = 0;
		List<Integer> colours = new ArrayList<>();
		for (EngramBoard.Fragment f : board.fragments()) if (!f.grey()) colours.add(f.colour());
		if (!colours.isEmpty()) colour = colours.get(random.nextInt(colours.size()));
		int node = EngramWeaveRules.applyPressure(board, random, colour, decoyFor(colour, random));
		if (node < 0) return;
		boolean intrusion = board.fragmentAt(node) >= 0;
		if (intrusion && weaver != null && EngramRecollections.get(server).bloodlineRemembers(server, weaver, selectedRecipeId))
			board.premarkIntrusions();
		engram.logPressure(node, intrusion);
		msg(player, (intrusion ? Component.translatable("message.hemomancy.somatic_loom_block_entity.the_pressure_boils_over_another_false") : Component.translatable("message.hemomancy.somatic_loom_block_entity.the_pressure_boils_over_the_engram")),
				ChatFormatting.DARK_GREEN, true);
	}

	/** Every true fragment is home: the thread its lanes hold is spent and the climax begins. */
	private void completeEngram(ServerLevel server) {
		EngramBoard board = engram.board();
		int[] spent = new int[EnumBloodTendency.values().length];
		for (int i = 0; i < board.fragmentCount(); i++)
			if (!board.fragment(i).intrusion()) spent[EngramWeaveRules.payColour(board, i)] += EngramWeaveRules.laneSpans(board, i);
		for (EnumBloodTendency tend : EnumBloodTendency.values())
			storedEnzymes.put(tend, Math.max(0, storedEnzymes.getOrDefault(tend, 0) - spent[tend.ordinal()]));
		engram.release();
		craftingPhase = PHASE_CLIMAX;
		climaxTicks = 0;
		Vec3 at = echoAnchor();
		server.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.9F, 0.6F);
	}

	public void blackout() {
		pausedTicks = SomaticWeaveRules.BLACKOUT_TICKS;
	}

	@Nullable
	public Player weaverPlayer(ServerLevel server) {
		if (weaver == null) return null;
		ServerPlayer online = server.getServer().getPlayerList().getPlayer(weaver);
		if (online != null) return online;
		return weaverRef != null && !weaverRef.isRemoved() && weaver.equals(weaverRef.getUUID()) ? weaverRef : null;
	}

	//  Where the engram lies: centred on the loom's footprint, on the floor the loom stands on, turned with the loom

	public Vec3 sigilCentre() {
		Vec3 at = SomaticLoomFrame.toWorld(worldPosition, facing(), SIGIL_X, 0, SIGIL_Z);
		return new Vec3(at.x, worldPosition.getY(), at.z);
	}

	/** Quarter turns clockwise from north, the way the loom's model is turned. */
	public int sigilTurns() {
		return switch (facing()) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
	}

	public double sigilRadius() {
		return EngramWeaveRules.tipRadius(upgradeTier());
	}

	/** A node of the engram in the world, on the plane of the loom's floor. */
	public Vec3 nodeWorld(int node) {
		double[] unit = EngramSigilGeometry.unit(node);
		double radius = sigilRadius();
		double[] world = EngramAim.toWorld(unit[0] * radius, unit[1] * radius, sigilTurns());
		return sigilCentre().add(world[0], 0, world[1]);
	}

	private Direction facing() {
		return getBlockState().hasProperty(SomaticLoomBlock.FACING) ? getBlockState().getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
	}

	@Nullable
	public EngramBoard getEngram() {
		return engram.board();
	}

	/** A copy of the engram `player` would recall if they paid for the chosen memory now; null with none chosen. */
	@Nullable
	public EngramBoard engramPreview(Player player) {
		if (level == null || level.isClientSide) return null;
		refreshRecipe();
		return curRecipe == null || isWeaving() ? null : EngramBoard.load(engramFor(player).save());
	}

	public int engramActive() {
		return engram.active();
	}

	public List<EngramWeaveSession.Beat> engramBeats() {
		return engram.beats();
	}

	/** Client-only: beats already played. */
	public int beatSeen() { return beatSeen; }
	public void markBeatSeen(int serial) { beatSeen = serial; }

	public Vec3 echoAnchor() {
		Direction facing = getBlockState().hasProperty(SomaticLoomBlock.FACING)
				? getBlockState().getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		return SomaticLoomFrame.toWorld(worldPosition, facing, SomaticLoomGeometry.ECHO_X, SomaticLoomGeometry.ECHO_Y,
				SomaticLoomGeometry.ECHO_Z);
	}

	@Nullable
	public BloodManipulation getWeaveManipulation() {
		MemoryWeavingRecipe recipe = getWeaveRecipe();
		if (recipe == null || level == null) return null;
		return recipe.getResultItem(level.registryAccess()).getItem() instanceof BloodMemoryItem memory
				? memory.getManip() : null;
	}

	/**
	 * The weave in hand on either side. The server resolves curRecipe; the client never does, so while weaving it
	 * reads the synced selection, which payment has already committed.
	 */
	@Nullable
	public MemoryWeavingRecipe getWeaveRecipe() {
		if (curRecipe != null) return curRecipe;
		if (level == null || !level.isClientSide || !isWeaving()) return null;
		ResourceLocation id = ResourceLocation.tryParse(selectedRecipeId);
		var holder = id == null ? null : level.getRecipeManager().byKey(id).orElse(null);
		return holder != null && holder.value() instanceof MemoryWeavingRecipe recipe ? recipe : null;
	}

	/** The rank a recipe's memory carries; non-memory results (effigies, formations) weave as Humilis. */
	private EnumManipulationRank rankOf(MemoryWeavingRecipe recipe) {
		return level != null && recipe.getResultItem(level.registryAccess()).getItem() instanceof BloodMemoryItem memory
				&& memory.getManip() != null ? memory.getManip().getRank() : EnumManipulationRank.HUMILIS;
	}

	/** Base looms weave to Summa, Ganglionic to Magister, Cortical to Perfectus: the ranks each Degree teaches. */
	public boolean stageAllows(MemoryWeavingRecipe recipe) {
		return SomaticWeaveRules.rankAllowed(rankOf(recipe), upgradeTier());
	}

	private Component tierRequired(MemoryWeavingRecipe recipe) {
		return Component.translatable("hemomancy.loom.tier_required." + SomaticWeaveRules.stageFor(rankOf(recipe)));
	}

	public EnumManipulationRank getWeaveRank() {
		BloodManipulation manip = getWeaveManipulation();
		return manip == null ? EnumManipulationRank.HUMILIS : manip.getRank();
	}

	public boolean isWeaving() { return craftingPhase == PHASE_WEAVING || craftingPhase == PHASE_CLIMAX; }
	public boolean isInClimax() { return craftingPhase == PHASE_CLIMAX; }
	public boolean isPaused() { return paused; }
	@Nullable public UUID getWeaver() { return weaver; }
	@Nullable public ResourceLocation getWeaverTheme() { return weaverTheme; }

	private ResourceLocation chamberThemeOf(UUID player) {
		return level instanceof ServerLevel server
				? com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.get(server.getServer()).skyThemeOf(player)
				: com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.THEME_WILL_DEFAULT;
	}
	public long getWeaveTicks() { return weaveTicks; }
	public int getClimaxTicks() { return climaxTicks; }
	/** Spans of a tendency's thread the engram's lanes hold: wound on its pirn until the weave completes or is cut back. */
	public int getSpun(EnumBloodTendency tendency) {
		return engram.live() ? EngramWeaveRules.committed(engram.board(), tendency.ordinal()) : 0;
	}
	public float displayedSpun(EnumBloodTendency tendency) { return shownSpun[tendency.ordinal()]; }
	public float displayedStock(EnumBloodTendency tendency) { return shownStock[tendency.ordinal()]; }

	public boolean echoClimaxFired() { return echoClimaxFired; }
	public void markEchoClimaxFired() { echoClimaxFired = true; }

	private void playWeaveFeedback(EnumBloodTendency tendency) {
		if (level == null) {
			return;
		}
		Vec3 pos = echoAnchor();
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME,
				SoundSource.BLOCKS, 0.85F, 1.25F);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WOOL_PLACE,
				SoundSource.BLOCKS, 1.0F, 0.6F);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(HemoParticleData.darkGlow(STRAND_SOOT), pos.x, pos.y, pos.z,
					12, 0.18D, 0.18D, 0.18D, 0.045D);
			serverLevel.sendParticles(HemoParticleData.ember(tendency.getColor(), 0.1F, 0.8F, 24),
					pos.x, pos.y, pos.z, 18, 0.28D, 0.28D, 0.28D, 0.06D);
			serverLevel.sendParticles(HemoParticleData.glow(tendency.getColor()), pos.x, pos.y, pos.z,
					28, 0.24D, 0.24D, 0.24D, 0.035D);
		}
	}

	private void tickCompletion(@Nullable Player player) {
		MemoryWeavingRecipe completedRecipe = curRecipe;
		EnumManipulationRank completedRank = getWeaveRank();
		if (completedRecipe == null || !catalystsMatch(completedRecipe)) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_ritual_unravels_configuration_changed"), ChatFormatting.RED, true);
			clearActiveRitual(true);
			BlockEntitySync.markAndSync(this);
			return;
		}

		ItemStack result = completedRecipe.getResultItem(level.registryAccess()).copy();
		contents.set(0, ItemStack.EMPTY);
		consumeCatalystsForRecipe(completedRecipe);

		spawnMemoryResult(result);

		msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_memory_crystallizes"), ChatFormatting.DARK_PURPLE, false);
		playSound(SoundEvents.END_PORTAL_SPAWN, 0.5f, 1.5f);
		if (level instanceof ServerLevel server && weaver != null && !selectedRecipeId.isEmpty())
			EngramRecollections.get(server).record(weaver, selectedRecipeId);
		if (player instanceof ServerPlayer serverPlayer) {
			var stationProgress = HemoCapabilityAccess.stationUpgrades(serverPlayer);
			stationProgress.recordUse(UpgradeStation.LOOM, StationUpgradeCatalog.LOOM_WEAVE);
			if (upgradeTier() >= 1 && SomaticWeaveRules.stageFor(completedRank) >= 1)
				stationProgress.recordUse(UpgradeStation.LOOM, StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC);
			SkillPointGainEvents.onMemoryWeavingCompleted(serverPlayer);
			if (result.is(ItemInit.memory_blood_shot.get()) && result.getItem() instanceof BloodMemoryItem memory) {
				var learning = KnownManipulationGrantHelper.checkMemoryGrant(serverPlayer, memory.getManip(), memory);
				if (learning.success() || learning.status() == KnownManipulationGrantHelper.MemoryGrantStatus.ALREADY_KNOWN) {
					HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
							HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
				}
			}
			if (HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer)
					&& HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(serverPlayer)) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
			}
		}

		clearActiveRitual(false);
		refreshRecipe();
		BlockEntitySync.markAndSync(this);
	}

	private void spawnMemoryResult(ItemStack result) {
		ItemEntity entity = new ItemEntity(level,
				worldPosition.getX() + 0.5, worldPosition.getY() + 1.5,
				worldPosition.getZ() + 0.5, result);
		entity.setDeltaMovement(0, 0.1, 0);
		level.addFreshEntity(entity);
	}

	private void consumeCatalystsForRecipe(MemoryWeavingRecipe recipe) {
		ItemStack memory = contents.get(0);
		contents = createMutableContents(1);
		contents.set(0, memory);
	}

	private void clearActiveRitual(boolean loseCharge) {
		craftingPhase = PHASE_IDLE;
		ritualBloodCharged = 0.0D;
		selectedRecipeId = "";
		committedRecipe = new CompoundTag();
		recoveryRequired = false;
		curRecipe = null;
		recipePath = "";
		engram.clear();
		pendingBoard = null;
		pendingKey = "";
		weaver = null;
		weaverRef = null;
		weaverTheme = null;
		weaveTicks = 0;
		climaxTicks = 0;
		pausedTicks = 0;
		paused = false;
		echoClimaxFired = false;
	}

	public void provideTendencyFeedback(@Nullable Player player) {
		if (recoveryRequired) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.the_saved_recipe_is_missing_or"), ChatFormatting.RED, false);
			return;
		}
		if (contents.get(0).isEmpty()) { msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.insert_a_hematic_memory"), ChatFormatting.GRAY, true); return; }
		MemoryWeavingRecipe held = curRecipe == null ? getPreviewRecipe() : null;
		if (held != null && !stageAllows(held)) {
			msg(player, tierRequired(held), ChatFormatting.YELLOW, true);
			return;
		}
		if (curRecipe == null) {
			msg(player, (recipeChoices().isEmpty() ? Component.translatable("message.hemomancy.somatic_loom_block_entity.add_catalysts_for_a_known_recipe") : Component.translatable("message.hemomancy.somatic_loom_block_entity.use_the_loom_with_an_empty")), ChatFormatting.YELLOW, false);
			return;
		}
		if (isWeaving()) {
			msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.with_a_living_staff_in_hand"), ChatFormatting.DARK_PURPLE, false);
			return;
		}
		msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.selected_blood_empty_hand_use_cycles",
				curRecipe.getResultItem(level.registryAccess()).getHoverName(),
				(int) curRecipe.getBloodCost()), ChatFormatting.DARK_PURPLE, false);
		// The payer's own engram decides the price: say exactly what it asks before any blood is taken.
		int[] reference = player != null && !level.isClientSide ? engramFor(player).reference() : null;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int needed = curRecipe.getEnzymeRequirement(tend);
			if (needed <= 0) continue;
			int spans = reference != null ? reference[tend.ordinal()] : minimumSpans(curRecipe, tend);
			int stored = storedEnzymes.getOrDefault(tend, 0);
			msg(player, Component.translatable(needed == 1
					? "message.hemomancy.somatic_loom_block_entity.engram_needs_one_fragment"
					: "message.hemomancy.somatic_loom_block_entity.engram_needs_fragments",
					needed,
					EnumBloodTendency.getRepEnzyme(tend).getDescription(),
					spans,
					stored,
					(stored < spans ? Component.translatable("message.hemomancy.somatic_loom_block_entity.more_enzyme",
							Math.ceilDiv(spans - stored, EngramWeaveRules.SPANS_PER_ENZYME)) : Component.empty())),
					stored < spans ? ChatFormatting.YELLOW : ChatFormatting.GRAY, false);
		}
		msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.have_a_living_staff_ready_the"), ChatFormatting.GRAY, false);
		if (player instanceof ServerPlayer serverPlayer && curRecipe.getResultItem(level.registryAccess()).getItem() instanceof BloodMemoryItem memory) {
			var check = KnownManipulationGrantHelper.checkMemoryGrant(serverPlayer, memory.getManip(), memory);
			Component requirement = switch (check.status()) {
				case MASTERY_TOO_LOW -> HemoText.translatable("message.hemomancy.somatic_loom_block_entity.requires_mastery", check.familyId().replace('_', ' '), check.requiredMastery(), check.currentMastery());
				case RANK_TOO_LOW -> HemoText.translatable("message.hemomancy.somatic_loom_block_entity.requires_degree", check.requiredDegree());
				case GRANTED, GRANTED_EQUIPPED -> Component.translatable("message.hemomancy.somatic_loom_block_entity.can_learn_memory");
				case ALREADY_KNOWN -> Component.translatable("message.hemomancy.somatic_loom_block_entity.already_know_memory");
				default -> Component.translatable("message.hemomancy.somatic_loom_block_entity.cannot_learn_memory");
			};
			msg(player, HemoText.translatable("message.hemomancy.somatic_loom_block_entity.choosing_a_locked_memory_crafts_it", requirement), ChatFormatting.YELLOW, false);
		}
	}

	private boolean provideMissingEnzymeFeedback(@Nullable Player player) {
		MemoryWeavingRecipe recipe = curRecipe;
		if (recipe == null) return false;
		// With a player in hand, their own engram's price; otherwise the shortest lanes' (three spans a fragment).
		int[] reference = player != null && level != null && !level.isClientSide ? engramFor(player).reference() : null;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int spans = reference != null ? reference[tend.ordinal()] : minimumSpans(recipe, tend);
			int needed = Math.ceilDiv(spans, EngramWeaveRules.SPANS_PER_ENZYME);
			int stored = storedEnzymes.getOrDefault(tend, 0) / EngramWeaveRules.SPANS_PER_ENZYME;
			if (storedEnzymes.getOrDefault(tend, 0) < spans) {
				msg(player, missingEnzymeMessage(EnumBloodTendency.getRepEnzyme(tend).getDescription(), stored, needed),
						ChatFormatting.YELLOW, true);
				return true;
			}
		}
		return false;
	}

	private static Component missingEnzymeMessage(Component enzymeName, int stored, int needed) {
		return HemoText.translatable("message.hemomancy.somatic_loom_block_entity.missing_stored",
				(needed - stored),
				enzymeName,
				stored,
				needed);
	}

	private List<ItemStack> getCatalysts() {
		List<ItemStack> catalysts = new ArrayList<>();
		for (int i = 1; i < contents.size(); i++) {
			if (!contents.get(i).isEmpty()) {
				catalysts.add(contents.get(i));
			}
		}
		return catalysts;
	}

	private int lastCatalystSlot() {
		for (int i = contents.size() - 1; i >= 1; i--) {
			if (!contents.get(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	private void msg(@Nullable Player player, Component text, ChatFormatting color, boolean actionBar) {
		if (player != null) {
			player.displayClientMessage(text.copy().withStyle(color), actionBar);
		}
	}

	private void playSound(net.minecraft.sounds.SoundEvent sound, float vol, float pitch) {
		if (level != null) {
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, vol, pitch);
		}
	}

	/** A loom with its engram unfurled is where a Living Staff looks for one. */
	private void trackEngram() {
		if (level == null) return;
		if (craftingPhase == PHASE_WEAVING && engram.live()) com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.ActiveEngramLooms.track(level, worldPosition);
		else com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.ActiveEngramLooms.untrack(level, worldPosition);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null) com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.ActiveEngramLooms.untrack(level, worldPosition);
	}

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this).inflate(SomaticWeaveRules.RITUAL_RADIUS);
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
	}

	//  Station upgrades: the shared catalog, progress and rites raise the loom in place.

	@Override public UpgradeStation upgradeStation() { return UpgradeStation.LOOM; }
	@Override public UUID machineIdentity() { return machineIdentity; }
	@Override public boolean isRiteLocked() { return riteLocked; }
	@Override public void setRiteLocked(boolean locked) { riteLocked = locked; BlockEntitySync.markAndSync(this); }
	@Override public boolean canReceiveBlood() { return !riteLocked; }
	@Override public boolean canProvideBlood() { return !riteLocked; }

	/** A chosen but unpaid weave may wait through a rite; paid blood or a live weave must finish or be cancelled first. */
	@Override
	public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) {
		return !riteLocked && !isWeaving() && ritualBloodCharged <= 0.0D;
	}

	@Override
	public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
		CompoundTag tag = saveWithoutMetadata(registries);
		tag.remove("RiteLocked");
		tag.remove("LastUpgradeRite");
		tag.remove("neoforge:attachments");
		// Derived from the selection each refresh, not owned state.
		tag.remove(TAG_RECIPE);
		tag.remove(TAG_CRAFT_PHASE);
		// Blood lives in the attachment; keep it in the snapshot under its old key so a mid-rite change still aborts.
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return tag;
	}

	@Override public boolean wasUpgradedBy(UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
	@Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; setChanged(); }

	@Override
	public boolean completeUpgrade(int targetTier, UUID riteId) {
		boolean upgraded = !isWeaving() && UpgradeableStation.super.completeUpgrade(targetTier, riteId);
		if (upgraded) BlockEntitySync.markAndSync(this);
		return upgraded;
	}

	private boolean refuseWhileRiteHeld(@Nullable Player player) {
		if (!riteLocked) return false;
		msg(player, Component.translatable("message.hemomancy.somatic_loom_block_entity.a_rite_holds_the_loom_still"), ChatFormatting.GRAY, true);
		return true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		saveShared(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		// Loading replaces the attachment instance; keep the field on the live one.
		volume = HemoCapabilityAccess.getBloodVolume(this).orElseThrow(IllegalStateException::new);
		loadShared(tag, registries);
	}

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveShared(tag, registries);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		if (tag != null) {
			loadShared(tag, registries);
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		if (pkt.getTag() != null) {
			loadShared(pkt.getTag(), registries);
		}
	}

	private void saveShared(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putInt(TAG_CONTENT_SIZE, contents.size());
		ContainerHelper.saveAllItems(tag, contents, registries);
		tag.putString(TAG_RECIPE, recipePath);
		tag.putInt("weaveSaveVersion", SAVE_VERSION);
		tag.putBoolean("RiteLocked", riteLocked);
		tag.putUUID("MachineIdentity", machineIdentity);
		if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
		tag.putString("selectedRecipeId", selectedRecipeId);
		tag.put("committedRecipe", committedRecipe.copy());
		tag.putInt(TAG_CRAFT_PHASE, craftingPhase);
		tag.putDouble(TAG_RITUAL_BLOOD_CHARGED, ritualBloodCharged);

		CompoundTag enzymeTag = new CompoundTag();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			enzymeTag.putInt(tend.name(), storedEnzymes.getOrDefault(tend, 0));
		}
		tag.put(TAG_STORED_ENZYMES, enzymeTag);

		tag.put(TAG_ENGRAM, engram.save());
		if (weaver != null) tag.putUUID(TAG_WEAVER, weaver);
		if (weaverTheme != null) tag.putString("weaverTheme", weaverTheme.toString());
		tag.putLong(TAG_WEAVE_TICKS, weaveTicks);
		tag.putInt(TAG_CLIMAX_TICKS, climaxTicks);
		tag.putInt(TAG_PAUSED_TICKS, pausedTicks);
		tag.putBoolean("paused", paused);
	}

	private void loadShared(CompoundTag tag, HolderLookup.Provider registries) {
		int size = Math.max(1, tag.getInt(TAG_CONTENT_SIZE));
		contents = createMutableContents(size);
		ContainerHelper.loadAllItems(tag, contents, registries);
		// Saves before the attachment became the only store still carry their own copy.
		if (tag.contains(TAG_BLOOD_LEVEL)) volume.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
		recipePath = tag.getString(TAG_RECIPE);
		selectedRecipeId = tag.getString("selectedRecipeId");
		committedRecipe = tag.getCompound("committedRecipe").copy();
		curRecipe = null;
		recoveryRequired = false;
		craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
		ritualBloodCharged = tag.getDouble(TAG_RITUAL_BLOOD_CHARGED);
		riteLocked = tag.getBoolean("RiteLocked");
		if (tag.hasUUID("MachineIdentity")) machineIdentity = tag.getUUID("MachineIdentity");
		lastUpgradeRite = tag.hasUUID("LastUpgradeRite") ? tag.getUUID("LastUpgradeRite") : null;

		// Older saves counted whole enzymes; each is now three spans of thread.
		boolean legacy = tag.getInt("weaveSaveVersion") < SAVE_VERSION;
		int scale = legacy ? EngramWeaveRules.SPANS_PER_ENZYME : 1;
		CompoundTag enzymeTag = tag.getCompound(TAG_STORED_ENZYMES);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			storedEnzymes.put(tend, Mth.clamp(enzymeTag.getInt(tend.name()) * scale, 0, ENZYME_CAPACITY));
		}

		engram.load(tag.getCompound(TAG_ENGRAM));
		if (legacy && craftingPhase >= PHASE_WEAVING) {
			// A witnessed weave's thread still on the pirns (or a pre-witnessed save's unwoven orbs) returns to its
			// vials; the weave recalls a fresh engram for its weaver, its blood already paid.
			int[] returned = new int[EnumBloodTendency.values().length];
			if (tag.contains(TAG_LEGACY_WEAVE_THREADS)) {
				CompoundTag spunTag = tag.getCompound(TAG_LEGACY_SPUN);
				for (EnumBloodTendency tend : EnumBloodTendency.values()) returned[tend.ordinal()] = spunTag.getInt(tend.name());
			} else if (tag.contains(TAG_LEGACY_RITUAL_ORBS)) {
				ListTag orbs = tag.getList(TAG_LEGACY_RITUAL_ORBS, Tag.TAG_COMPOUND);
				for (int i = 0; i < orbs.size(); i++) {
					CompoundTag orb = orbs.getCompound(i);
					if (!orb.getBoolean("completed")) {
						try {
							returned[EnumBloodTendency.valueOf(orb.getString("tendency")).ordinal()]++;
						} catch (IllegalArgumentException ignored) {
						}
					}
				}
			}
			for (EnumBloodTendency tend : EnumBloodTendency.values())
				storedEnzymes.put(tend, Math.min(ENZYME_CAPACITY,
						storedEnzymes.getOrDefault(tend, 0) + returned[tend.ordinal()] * EngramWeaveRules.SPANS_PER_ENZYME));
			engram.clear();
			if (craftingPhase == PHASE_CLIMAX) craftingPhase = PHASE_WEAVING;
		}
		weaver = tag.hasUUID(TAG_WEAVER) ? tag.getUUID(TAG_WEAVER) : null;
		weaverTheme = tag.contains("weaverTheme") ? ResourceLocation.tryParse(tag.getString("weaverTheme")) : null;
		weaveTicks = tag.getLong(TAG_WEAVE_TICKS);
		// The client never runs clearActiveRitual: a new weave must replay its climax, and its beats from the start.
		if (craftingPhase != PHASE_CLIMAX) echoClimaxFired = false;
		if (craftingPhase < PHASE_WEAVING || engram.serial() < beatSeen) beatSeen = -1;
		climaxTicks = tag.getInt(TAG_CLIMAX_TICKS);
		pausedTicks = tag.getInt(TAG_PAUSED_TICKS);
		paused = tag.getBoolean("paused");
	}

	private static NonNullList<ItemStack> createMutableContents(int size) {
		NonNullList<ItemStack> items = NonNullList.createWithCapacity(Math.max(1, size));
		for (int i = 0; i < Math.max(1, size); i++) {
			items.add(ItemStack.EMPTY);
		}
		return items;
	}
}
