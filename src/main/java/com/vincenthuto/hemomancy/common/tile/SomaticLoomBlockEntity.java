package com.vincenthuto.hemomancy.common.tile;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class SomaticLoomBlockEntity extends BlockEntity implements IBloodTile {

	// ========================== CONSTANTS ==========================

	private static final String TAG_BLOOD_LEVEL = "bloodLevel";
	private static final String TAG_RECIPE = "recipe";
	private static final String TAG_CRAFT_PROGRESS = "craftProgress";
	private static final String TAG_CRAFT_TOTAL = "craftTotal";
	private static final String TAG_CRAFT_PHASE = "craftPhase";
	private static final String TAG_CRAFTING_PLAYER = "craftingPlayer";
	private static final String TAG_BLOOD_COST_PER_TICK = "bloodCostPerTick";

	/** How much each enzyme click adjusts a tendency. */
	private static final float TENDENCY_STEP = 0.2f;
	/** Tolerance for comparing tendency floats when matching recipes (±0.1). */
	private static final float TENDENCY_EPSILON = 0.1f;
	/** Max squared distance the player can be during a ritual (6 blocks). */
	private static final double MAX_RITUAL_DISTANCE_SQ = 36.0;
	/** Ticks the player can be out of range before the ritual collapses (3s). */
	private static final int MAX_DISTANCE_GRACE_TICKS = 60;

	// ========================== FIELDS ==========================

	/** Slot 0 = hematic memory, Slot 1 = catalyst item. */
	public NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);

	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA).orElseThrow(IllegalStateException::new);

	private MemoryWeavingRecipe curRecipe = null;
	String recipePath = "";

	/*
	 * Crafting state — kept simple on purpose.
	 * Phase 0 = idle, 1 = channeling (blood drains, progress ticks down),
	 * 2 = completing (single-tick finalisation).
	 */
	private int craftingProgress = 0;
	private int craftingTotalTime = 0;
	private int craftingPhase = 0;
	private UUID craftingPlayerUUID = null;
	private float bloodCostPerTick = 0;
	private int distanceGraceTicks = 0;

	// ========================== CONSTRUCTOR ==========================

	public SomaticLoomBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.somatic_loom.get(), pos, state);
	}

	// ========================== TICK ==========================

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			SomaticLoomBlockEntity self) {
		// Client-side effects can be added here later
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			SomaticLoomBlockEntity self) {
		if (self.craftingPhase == 0) return;

		Player player = self.findCraftingPlayer();
		if (player == null || self.isPlayerTooFar(player)) {
			self.tickDistancePenalty(player);
			return;
		}
		self.distanceGraceTicks = 0;

		if (self.craftingPhase == 1) {
			self.tickChanneling(player);
		} else if (self.craftingPhase == 2) {
			self.tickCompletion(player);
		}
	}

	// ========================== ITEM INTERACTION ==========================

	/**
	 * Handles a player right-clicking the loom with an item.
	 * Each item type has its own handler; the first match wins.
	 *
	 * @return true if the interaction was consumed
	 */
	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {

		// --- Lethian dew: reset all tendencies to zero ---
		if (stack.getItem() == ItemInit.lethian_dew.get()) {
			consumeItem(player, stack, hand);
			tendency.setTendency(MemoryWeavingRecipe.blank());
			refreshRecipe();
			markDirtyAndSync();
			return true;
		}

		// --- Blood flask: fill the blood reservoir ---
		if (stack.getItem() instanceof BloodyFlaskItem flask) {
			if (volume.isFull()) return false;
			volume.addBloodVolume(flask.getAmount());
			consumeItem(player, stack, hand);
			markDirtyAndSync();
			return true;
		}

		// --- Blood gourd: drain blood from gourd into block ---
		if (stack.getItem() instanceof BloodGourdItem) {
			IBloodVolume gourdVol = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
			if (gourdVol == null || gourdVol.getBloodVolume() <= 0 || volume.isFull()) return false;
			double transfer = Math.min(gourdVol.getBloodVolume(),
					volume.getMaxBloodVolume() - volume.getBloodVolume());
			if (transfer <= 0) return false;
			gourdVol.subtractBloodVolume(transfer);
			volume.addBloodVolume(transfer);
			markDirtyAndSync();
			return true;
		}

		// --- Enzyme: adjust a specific tendency ---
		if (stack.getItem() instanceof EnzymeItem enzyme) {
			applyEnzyme(player, stack, enzyme);
			return true;
		}

		// --- Recycled enzyme: subtract from the highest tendency ---
		if (stack.getItem() instanceof RecycledEnzymeItem) {
			applyRecycledEnzyme(stack);
			return true;
		}

		// --- Hematic memory: place into slot 0 ---
		if (contents.get(0).isEmpty() && stack.getItem() == ItemInit.hematic_memory.get()) {
			placeInSlot(0, player, stack, hand);
			return true;
		}

		// --- Anything else: place as catalyst into slot 1 ---
		if (contents.get(1).isEmpty() && !stack.isEmpty()) {
			placeInSlot(1, player, stack, hand);
			return true;
		}

		return false;
	}

	/**
	 * Removes an item from a slot and returns it to the player.
	 *
	 * @param removeMemory true = slot 0 (memory), false = slot 1 (catalyst)
	 */
	public boolean removeItem(@Nullable Player player, boolean removeMemory) {
		if (player == null) return false;
		int slot = removeMemory ? 0 : 1;
		if (contents.get(slot).isEmpty()) return false;

		player.getInventory().placeItemBackInInventory(contents.get(slot).copy());
		contents.set(slot, ItemStack.EMPTY);
		refreshRecipe();
		markDirtyAndSync();
		return true;
	}

	/** Resets all tendencies to zero. Called from ClearLoomStatePacket. */
	public void clearTendency() {
		tendency.setTendency(MemoryWeavingRecipe.blank());
		refreshRecipe();
		markDirtyAndSync();
	}

	/** Drops all contents as item entities (called when the block is broken). */
	public void dropContents() {
		if (level == null || level.isClientSide) return;
		for (ItemStack stack : contents) {
			if (!stack.isEmpty()) {
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
						worldPosition.getZ(), stack);
			}
		}
		contents.clear();
	}

	// ========================== ENZYME HELPERS ==========================

	private void applyEnzyme(@Nullable Player player, ItemStack stack, EnzymeItem enzyme) {
		EnumBloodTendency tend = enzyme.getTend();
		float current = tendency.getAlignmentByTendency(tend);

		boolean subtract = player != null && player.isCrouching();
		if (subtract) {
			// Shift-click: subtract
			if (current <= 0f) return;
			setTendencyRounded(tend, Math.max(0f, current - TENDENCY_STEP));
		} else {
			// Normal click: add
			setTendencyRounded(tend, current + TENDENCY_STEP);
			// Small chance to drop a recycled enzyme
			if (level != null && level.random.nextInt(20) % 7 == 0) {
				ItemEntity drop = new ItemEntity(level,
						getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.0,
						getBlockPos().getZ() + 0.5,
						new ItemStack(ItemInit.recycled_enzyme.get()));
				level.addFreshEntity(drop);
			}
		}
		stack.shrink(1);
		refreshRecipe();
		markDirtyAndSync();
	}

	private void applyRecycledEnzyme(ItemStack stack) {
		EnumBloodTendency highest = null;
		float highestVal = 0f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float val = tendency.getAlignmentByTendency(tend);
			if (val > highestVal) {
				highestVal = val;
				highest = tend;
			}
		}
		if (highest != null && highestVal > 0f) {
			setTendencyRounded(highest, Math.max(0f, highestVal - TENDENCY_STEP));
			stack.shrink(1);
		}
		refreshRecipe();
		markDirtyAndSync();
	}

	/** Sets a tendency value, rounded to 1 decimal place to prevent float drift. */
	private void setTendencyRounded(EnumBloodTendency tend, float value) {
		tendency.setTendencyAlignment(tend, Math.round(value * 10f) / 10f);
	}

	// ========================== SLOT / CONSUME HELPERS ==========================

	private void placeInSlot(int slot, @Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		ItemStack toPlace = stack.copy();
		toPlace.setCount(1);
		contents.set(slot, toPlace);
		consumeItem(player, stack, hand);
		refreshRecipe();
		markDirtyAndSync();
	}

	private void consumeItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		if (player != null && player.getAbilities().instabuild) return;
		stack.shrink(1);
		if (stack.isEmpty() && player != null && hand != null) {
			player.setItemInHand(hand, ItemStack.EMPTY);
		}
	}

	// ========================== RECIPE MATCHING ==========================

	/** Re-evaluates which recipe (if any) matches the current block state. */
	public void refreshRecipe() {
		curRecipe = null;
		recipePath = "";
		if (level == null) return;

		Map<EnumBloodTendency, Float> ourTends = tendency.getTendency();
		for (MemoryWeavingRecipe recipe : MemoryWeavingRecipe.getAllRecipes(level)) {
			if (tendenciesMatch(recipe.getTendency(), ourTends)
					&& recipe.getIngredient().test(contents.get(1))) {
				curRecipe = recipe;
				recipePath = HLTextUtils.getItemRegistryName(
						recipe.getResultItem(level.registryAccess()).getItem());
				return;
			}
		}
	}

	private static boolean tendenciesMatch(Map<EnumBloodTendency, Float> recipe,
			Map<EnumBloodTendency, Float> block) {
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float a = recipe.getOrDefault(tend, 0f);
			float b = block.getOrDefault(tend, 0f);
			if (Math.abs(a - b) > TENDENCY_EPSILON) return false;
		}
		return true;
	}

	// ========================== CRAFTING RITUAL ==========================

	public boolean isCrafting() { return craftingPhase > 0; }
	public boolean hasValidRecipe() { return curRecipe != null; }
	public int getCraftingProgress() { return craftingProgress; }
	public int getCraftingTotalTime() { return craftingTotalTime; }
	public int getCraftingPhase() { return craftingPhase; }
	public MemoryWeavingRecipe getCurRecipe() { return curRecipe; }
	public String getRecipePath() { return recipePath; }
	public double getBloodVolume() { return volume.getBloodVolume(); }
	public double getMaxBloodVolume() { return volume.getMaxBloodVolume(); }
	public IBloodTendency getTendCapability() { return tendency; }
	public Map<EnumBloodTendency, Float> getTendency() { return tendency.getTendency(); }

	public ItemStack getResultItem() {
		return curRecipe != null && level != null
				? curRecipe.getResultItem(level.registryAccess()) : ItemStack.EMPTY;
	}

	/**
	 * Starts the crafting ritual.
	 * Called when the player right-clicks with an empty hand and a valid recipe
	 * is detected.  The only requirement beyond the recipe match is enough blood.
	 */
	public boolean startRitual(Player player) {
		if (isCrafting() || curRecipe == null || contents.get(0).isEmpty()) return false;

		float totalBloodCost = curRecipe.getBloodCost();
		if (volume.getBloodVolume() < totalBloodCost) {
			msg(player, "Not enough blood. Need " + (int) totalBloodCost + ".",
					ChatFormatting.DARK_RED, true);
			return false;
		}

		craftingTotalTime = curRecipe.getCraftTimeTicks();
		craftingProgress = craftingTotalTime;
		bloodCostPerTick = totalBloodCost / craftingTotalTime;
		craftingPhase = 1;
		craftingPlayerUUID = player.getUUID();
		distanceGraceTicks = 0;

		msg(player, "The loom awakens...", ChatFormatting.DARK_PURPLE, false);
		playSound(SoundEvents.BEACON_ACTIVATE, 0.7f, 1.5f);
		markDirtyAndSync();
		return true;
	}

	/** Cancels the active ritual. */
	public void cancelRitual(@Nullable Player player) {
		if (!isCrafting()) return;
		if (player != null) {
			msg(player, "The ritual is disrupted.", ChatFormatting.RED, true);
		}
		playSound(SoundEvents.BEACON_DEACTIVATE, 0.7f, 0.8f);
		resetCraftingState();
		markDirtyAndSync();
	}

	// ---- Tick handlers ----

	private void tickChanneling(Player player) {
		if (volume.getBloodVolume() < bloodCostPerTick) {
			msg(player, "The blood runs dry!", ChatFormatting.DARK_RED, true);
			cancelRitual(player);
			return;
		}

		volume.subtractBloodVolume(bloodCostPerTick);
		craftingProgress--;

		// Ambient particles
		if (level instanceof ServerLevel sl && level.getGameTime() % 4 == 0) {
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					sl, worldPosition, ParticleTypes.SOUL_FIRE_FLAME);
		}

		// Channeling complete → move to completion phase
		if (craftingProgress <= 0) {
			craftingPhase = 2;
		}

		// Periodic progress chat feedback
		if (craftingProgress > 0 && craftingProgress % 40 == 0) {
			int pct = (int) (((float) (craftingTotalTime - craftingProgress) / craftingTotalTime) * 100);
			msg(player, "Ritual progress: " + pct + "%", ChatFormatting.DARK_PURPLE, true);
		}

		// Sync to client every half-second
		if (level.getGameTime() % 10 == 0) {
			markDirtyAndSync();
		}
	}

	private void tickCompletion(Player player) {
		// Re-verify the recipe (in case someone tampered mid-ritual)
		refreshRecipe();
		if (curRecipe == null) {
			msg(player, "The ritual unravels. Configuration changed.", ChatFormatting.RED, true);
			resetCraftingState();
			markDirtyAndSync();
			return;
		}

		// Produce the result
		ItemStack result = curRecipe.getResultItem(level.registryAccess()).copy();

		// Consume inputs
		contents.set(0, ItemStack.EMPTY);
		contents.set(1, ItemStack.EMPTY);
		tendency.setTendency(MemoryWeavingRecipe.blank());

		// Spawn result above the block
		ItemEntity entity = new ItemEntity(level,
				worldPosition.getX() + 0.5, worldPosition.getY() + 1.5,
				worldPosition.getZ() + 0.5, result);
		entity.setDeltaMovement(0, 0.1, 0);
		level.addFreshEntity(entity);

		// Effects
		msg(player, "The memory crystallizes.", ChatFormatting.DARK_PURPLE, false);
		playSound(SoundEvents.END_PORTAL_SPAWN, 0.5f, 1.5f);
		if (level instanceof ServerLevel sl) {
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					sl, worldPosition, ParticleTypes.REVERSE_PORTAL);
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					sl, worldPosition, ParticleTypes.ENCHANT);
		}

		// Award Memory Weaving milestone
		if (player instanceof ServerPlayer serverPlayer) {
			com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointGainEvents
					.onMemoryWeavingCompleted(serverPlayer);
		}

		resetCraftingState();
		curRecipe = null;
		recipePath = "";
		markDirtyAndSync();
	}

	private void tickDistancePenalty(@Nullable Player player) {
		distanceGraceTicks++;
		if (distanceGraceTicks > MAX_DISTANCE_GRACE_TICKS) {
			if (player != null) {
				msg(player, "The ritual collapses! You strayed too far!",
						ChatFormatting.DARK_RED, false);
			}
			playSound(SoundEvents.BEACON_DEACTIVATE, 1.0f, 0.5f);
			resetCraftingState();
			markDirtyAndSync();
			return;
		}
		if (player != null && distanceGraceTicks % 20 == 0) {
			msg(player, "Return to the loom!", ChatFormatting.RED, true);
		}
	}

	private boolean isPlayerTooFar(Player player) {
		return player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > MAX_RITUAL_DISTANCE_SQ;
	}

	private void resetCraftingState() {
		craftingPhase = 0;
		craftingProgress = 0;
		craftingTotalTime = 0;
		craftingPlayerUUID = null;
		bloodCostPerTick = 0;
		distanceGraceTicks = 0;
	}

	@Nullable
	private Player findCraftingPlayer() {
		if (craftingPlayerUUID == null || level == null) return null;
		for (Player p : level.players()) {
			if (p.getUUID().equals(craftingPlayerUUID)) return p;
		}
		return null;
	}

	// ========================== PLAYER FEEDBACK ==========================

	/**
	 * Shows the player context-sensitive feedback when they right-click with
	 * an empty hand and no valid recipe is ready.
	 */
	public void provideTendencyFeedback(Player player) {
		if (contents.get(0).isEmpty()) {
			msg(player, "Insert a Hematic Memory.", ChatFormatting.GRAY, true);
			return;
		}

		if (curRecipe != null) {
			float cost = curRecipe.getBloodCost();
			if (volume.getBloodVolume() < cost) {
				msg(player, "Recipe ready but needs " + (int) cost + " blood.",
						ChatFormatting.DARK_RED, true);
			} else {
				msg(player, "Ready. Right-click to begin the ritual.",
						ChatFormatting.DARK_PURPLE, true);
			}
			return;
		}

		MemoryWeavingRecipe closest = findClosestRecipe();
		if (closest == null) {
			msg(player, "No matching recipe found.", ChatFormatting.GRAY, true);
			return;
		}

		// Build a hint string showing which tendencies need adjusting
		StringBuilder hint = new StringBuilder();
		boolean first = true;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float needed = closest.getTendency().getOrDefault(tend, 0f);
			float current = tendency.getAlignmentByTendency(tend);
			float diff = needed - current;
			if (Math.abs(diff) > 0.01f) {
				if (!first) hint.append(", ");
				hint.append(tend.name())
						.append(diff > 0 ? "+" : "")
						.append(String.format("%.1f", diff));
				first = false;
			}
		}

		if (closest.getIngredient() != Ingredient.EMPTY
				&& !closest.getIngredient().test(contents.get(1))) {
			hint.append(first ? "" : " | ");
			hint.append(contents.get(1).isEmpty() ? "Missing catalyst" : "Wrong catalyst");
		}

		if (!hint.isEmpty()) {
			player.displayClientMessage(
					Component.literal("Nearest: " + hint).withStyle(ChatFormatting.YELLOW), false);
		}
	}

	@Nullable
	private MemoryWeavingRecipe findClosestRecipe() {
		if (level == null) return null;
		MemoryWeavingRecipe closest = null;
		float closestDist = Float.MAX_VALUE;
		for (MemoryWeavingRecipe recipe : MemoryWeavingRecipe.getAllRecipes(level)) {
			float dist = 0;
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				dist += Math.abs(recipe.getTendency().getOrDefault(tend, 0f)
						- tendency.getAlignmentByTendency(tend));
			}
			if (dist < closestDist) {
				closestDist = dist;
				closest = recipe;
			}
		}
		return closest;
	}

	// ========================== UTILITY ==========================

	private void msg(Player player, String text, ChatFormatting color, boolean actionBar) {
		player.displayClientMessage(Component.literal(text).withStyle(color), actionBar);
	}

	private void playSound(net.minecraft.sounds.SoundEvent sound, float vol, float pitch) {
		if (level != null) {
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, vol, pitch);
		}
	}

	private void markDirtyAndSync() {
		if (level == null) return;
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return new AABB(getBlockPos()).inflate(5.0, 5.0, 5.0);
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
	}

	@Override
	public void sendUpdates() {
		if (level == null) return;
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	// ========================== NBT ==========================

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, this.contents);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		tag.putString(TAG_RECIPE, recipePath);
		tag.putInt(TAG_CRAFT_PROGRESS, craftingProgress);
		tag.putInt(TAG_CRAFT_TOTAL, craftingTotalTime);
		tag.putInt(TAG_CRAFT_PHASE, craftingPhase);
		tag.putFloat(TAG_BLOOD_COST_PER_TICK, bloodCostPerTick);
		if (craftingPlayerUUID != null) {
			tag.putUUID(TAG_CRAFTING_PLAYER, craftingPlayerUUID);
		}
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			Float val = tendency.getTendency().get(tend);
			tag.putFloat(tend.toString(), val != null ? val : 0f);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(2, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.contents);
		recipePath = tag.getString(TAG_RECIPE);
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		craftingProgress = tag.getInt(TAG_CRAFT_PROGRESS);
		craftingTotalTime = tag.getInt(TAG_CRAFT_TOTAL);
		craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
		bloodCostPerTick = tag.getFloat(TAG_BLOOD_COST_PER_TICK);
		if (tag.hasUUID(TAG_CRAFTING_PLAYER)) {
			craftingPlayerUUID = tag.getUUID(TAG_CRAFTING_PLAYER);
		}
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
		}
	}

	// ========================== SYNC ==========================

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public final CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.contents);
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		tag.putString(TAG_RECIPE, recipePath);
		tag.putInt(TAG_CRAFT_PROGRESS, craftingProgress);
		tag.putInt(TAG_CRAFT_TOTAL, craftingTotalTime);
		tag.putInt(TAG_CRAFT_PHASE, craftingPhase);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			Float val = tendency.getTendency().get(tend);
			tag.putFloat(tend.toString(), val != null ? val : 0f);
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag == null) return;
		recipePath = tag.getString(TAG_RECIPE);
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		craftingProgress = tag.getInt(TAG_CRAFT_PROGRESS);
		craftingTotalTime = tag.getInt(TAG_CRAFT_TOTAL);
		craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() == null) return;
		CompoundTag tag = pkt.getTag();
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		recipePath = tag.getString(TAG_RECIPE);
		craftingProgress = tag.getInt(TAG_CRAFT_PROGRESS);
		craftingTotalTime = tag.getInt(TAG_CRAFT_TOTAL);
		craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
		}
	}
}


