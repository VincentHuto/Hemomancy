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
import com.vincenthuto.hemomancy.common.recipe.RecallerRecipe;
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

public class VisceralRecallerBlockEntity extends BlockEntity implements IBloodTile {
	public static final String TAG_BLOOD_LEVEL = "bloodLevel";
	public static final String TAG_BLOOD_TENDENCY = "tendency";
	public static final String TAG_RECIPE = "recipe";
	public static final String TAG_CRAFT_PROGRESS = "craftProgress";
	public static final String TAG_CRAFT_TOTAL = "craftTotal";
	public static final String TAG_CRAFT_PHASE = "craftPhase";
	private static final String TAG_CRAFTING_PLAYER = "craftingPlayer";
	private static final String TAG_BLOOD_COST_PER_TICK = "bloodCostPerTick";
	private static final String TAG_ATTUNEMENT_COUNT = "attunementCount";
	private static final String TAG_REQUIRED_ATTUNEMENTS = "requiredAttunements";
	private static final String TAG_ATTUNEMENT_TIMER = "attunementTimer";

	/** Maximum squared distance a player can be from the recaller during a ritual. */
	private static final double MAX_RITUAL_DISTANCE_SQ = 36.0; // 6 blocks
	/** Ticks the player has to perform an attunement before a penalty is applied. */
	private static final int ATTUNEMENT_WINDOW_TICKS = 100; // 5 seconds
	/** Ticks the player can be out of range before the ritual collapses. */
	private static final int MAX_DISTANCE_PENALTY_TICKS = 60; // 3 seconds
	/** Multiplier for the blood penalty when the player misses an attunement window. */
	private static final float ATTUNEMENT_TIMEOUT_PENALTY_MULTIPLIER = 20f;

	// ---- Crafting ritual state ----
	private int craftingProgress = 0;
	private int craftingTotalTime = 0;
	/** 0=idle, 1=channeling, 2=awaiting attunement, 3=completing */
	private int craftingPhase = 0;
	private int attunementTimer = 0;
	private int attunementCount = 0;
	private int requiredAttunements = 0;
	private UUID craftingPlayerUUID = null;
	private float bloodCostPerTick = 0;
	private int distancePenaltyTicks = 0;

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			VisceralRecallerBlockEntity self) {
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			VisceralRecallerBlockEntity self) {
		if (self.craftingPhase == 0) return;

		Player player = self.findCraftingPlayer();
		boolean tooFar = player == null
				|| player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > MAX_RITUAL_DISTANCE_SQ;

		if (tooFar) {
			self.handleDistancePenalty(player);
			return;
		}
		// Player is in range — reset distance penalty counter
		self.distancePenaltyTicks = 0;

		switch (self.craftingPhase) {
			case 1 -> self.handleChanneling(player);
			case 2 -> self.handleAwaitingAttunement(player);
			case 3 -> self.handleCompletion(player);
		}
	}

	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	String recipePath = "";
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);

	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA)
			.orElseThrow(IllegalStateException::new);

	RecallerRecipe curRecipe = null;

	public VisceralRecallerBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.visceral_artificial_recaller.get(), pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		// Effects render up to ~3 blocks above and ~4 blocks out horizontally.
		// Expand the culling AABB so Minecraft never frustum-culls the renderer
		// while the effects are still in view.
		BlockPos pos = getBlockPos();
		return new AABB(pos).inflate(5.0, 5.0, 5.0);
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {

		if (stack.getItem() == ItemInit.lethian_dew.get()) {
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}
			getTendCapability().setTendency(RecallerRecipe.blank());
			checkRecipe();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Blood container handling — fill the block's blood volume
		if (stack.getItem() instanceof BloodyFlaskItem flask) {
			if (!volume.isFull()) {
				float amount = flask.getAmount();
				volume.addBloodVolume(amount);
				if (player == null || !player.getAbilities().instabuild) {
					stack.shrink(1);
					if (stack.isEmpty() && player != null) {
						player.setItemInHand(hand, ItemStack.EMPTY);
					}
				}
				sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
				return true;
			}
			return false;
		}

		// Blood gourd handling — drain blood from the gourd into the block
		if (stack.getItem() instanceof BloodGourdItem) {
			IBloodVolume gourdVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
			if (gourdVolume != null && gourdVolume.getBloodVolume() > 0 && !volume.isFull()) {
				double transfer = Math.min(gourdVolume.getBloodVolume(), volume.getMaxBloodVolume() - volume.getBloodVolume());
				if (transfer > 0) {
					gourdVolume.subtractBloodVolume(transfer);
					volume.addBloodVolume(transfer);
					sendUpdates();
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
					return true;
				}
			}
			return false;
		}

		if (stack.getItem() instanceof EnzymeItem) {
			EnzymeItem enzyme = (EnzymeItem) stack.getItem();
			if (player != null && player.isCrouching()) {
				// Shift + enzyme: subtract 0.2 from that tendency
				float current = tendency.getAlignmentByTendency(enzyme.getTend());
				if (current > 0f) {
					float newVal = Math.max(0f, current - 0.2f);
					float rounded = Math.round(newVal * 10f) / 10f;
					tendency.setTendencyAlignment(enzyme.getTend(), rounded);
					stack.shrink(1);
				}
			} else {
				// Normal click: add 0.2 to that tendency
				tendency.addTendencyAlignment(enzyme.getTend(), 0.2f);
				// Round to 1 decimal place to avoid floating-point drift
				float rounded = Math.round(tendency.getAlignmentByTendency(enzyme.getTend()) * 10f) / 10f;
				tendency.setTendencyAlignment(enzyme.getTend(), rounded);
				stack.shrink(1);
				// Adds a recycled chance
				if (level.random.nextInt(20) % 7 == 0) {
					ItemEntity recycl = new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(),
							getBlockPos().getZ(), new ItemStack(ItemInit.recycled_enzyme.get()));
					level.addFreshEntity(recycl);
				}
			}
			checkRecipe();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Recycled enzyme: subtract 0.2 from the highest non-zero tendency
		if (stack.getItem() instanceof RecycledEnzymeItem) {
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
				float newVal = Math.max(0f, highestVal - 0.2f);
				float rounded = Math.round(newVal * 10f) / 10f;
				tendency.setTendencyAlignment(highest, rounded);
				stack.shrink(1);
			}
			checkRecipe();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Memory Slot add (slot 0)
		if (contents.get(0).isEmpty() && stack.getItem() == ItemInit.hematic_memory.get()) {
			ItemStack stackToAdd = stack.copy();
			stackToAdd.setCount(1);
			contents.set(0, stackToAdd);
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		// Item Slot add (slot 1)
		if (contents.get(1).isEmpty() && stack.getItem() != ItemInit.hematic_memory.get() && !stack.isEmpty()) {
			ItemStack stackToAdd = stack.copy();
			stackToAdd.setCount(1);
			contents.set(1, stackToAdd);
			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
				if (stack.isEmpty() && player != null) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
			}
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		return false;
	}

	/**
	 * Removes an item from the given slot and returns it to the player.
	 * @param removeMemory true = remove slot 0 (memory), false = remove slot 1 (catalyst)
	 */
	public boolean removeItem(@Nullable Player player, boolean removeMemory) {
		if (player == null) return false;

		int slot = removeMemory ? 0 : 1;
		if (!contents.get(slot).isEmpty()) {
			ItemStack copy = contents.get(slot).copy();
			player.getInventory().placeItemBackInInventory(copy);
			contents.set(slot, ItemStack.EMPTY);
			checkRecipe();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return true;
		}

		return false;
	}

	/**
	 * Drops all contents as item entities. Called when the block is broken.
	 */
	public void dropContents() {
		if (level != null && !level.isClientSide) {
			for (ItemStack stack : contents) {
				if (!stack.isEmpty()) {
					Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
				}
			}
			contents.clear();
		}
	}

	public void clearTendency() {
		getTendCapability().setTendency(RecallerRecipe.blank());
		checkRecipe();
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	/**
	 * Public entry point to re-evaluate recipe matching.
	 * Called from the block's use() before checking hasValidRecipe().
	 */
	public void recheckRecipe() {
		checkRecipe();
	}

	private void checkRecipe() {
		Map<EnumBloodTendency, Float> ourTends = this.tendency.getTendency();
		curRecipe = null;
		recipePath = "";
		if (level == null) return;
		for (RecallerRecipe recipe : RecallerRecipe.getAllRecipes(level)) {
			boolean tendsMatch = tendenciesMatch(recipe.getTendency(), ourTends);
			boolean ingredientMatch = recipe.getIngredient().test(contents.get(1));
			if (tendsMatch && ingredientMatch) {
				curRecipe = recipe;
				recipePath = HLTextUtils.getItemRegistryName(recipe.getResultItem(level.registryAccess()).getItem());
				break;
			}
		}
	}

	/**
	 * Compares two tendency maps using an epsilon to tolerate floating-point drift.
	 */
	private static boolean tendenciesMatch(Map<EnumBloodTendency, Float> a, Map<EnumBloodTendency, Float> b) {
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float va = a.getOrDefault(tend, 0f);
			float vb = b.getOrDefault(tend, 0f);
			if (Math.abs(va - vb) > 0.05f) {
				return false;
			}
		}
		return true;
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	public RecallerRecipe getCurRecipe() {
		return curRecipe;
	}

	public double getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	public ItemStack getResultItem() {
		return curRecipe != null ? curRecipe.getResultItem(level.registryAccess()) : ItemStack.EMPTY;
	}

	public IBloodTendency getTendCapability() {
		return tendency;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency.getTendency();
	}

	public boolean hasValidRecipe() {
		return curRecipe != null;
	}

	// ========================== RITUAL CRAFTING ==========================

	/** Whether a ritual is currently in progress. */
	public boolean isCrafting() {
		return craftingPhase > 0;
	}

	public int getCraftingProgress() {
		return craftingProgress;
	}

	public int getCraftingTotalTime() {
		return craftingTotalTime;
	}

	public int getCraftingPhase() {
		return craftingPhase;
	}

	/**
	 * Attempts to start the ritual. Called when the player right-clicks with an
	 * empty hand while a valid recipe is detected.
	 *
	 * @return true if the ritual was started
	 */
	public boolean startRitual(Player player) {
		if (craftingPhase != 0) return false;
		if (curRecipe == null) return false;
		if (contents.get(0).isEmpty()) return false;

		float totalBloodCost = curRecipe.getBloodCost();
		if (volume.getBloodVolume() < totalBloodCost) {
			player.displayClientMessage(
					Component.literal("The recaller thirsts. Feed it more blood to begin the ritual.")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return false;
		}

		craftingTotalTime = curRecipe.getCraftTimeTicks();
		craftingProgress = craftingTotalTime;
		bloodCostPerTick = totalBloodCost / craftingTotalTime;
		craftingPhase = 1;
		craftingPlayerUUID = player.getUUID();
		attunementCount = 0;
		requiredAttunements = curRecipe.getRequiredAttunements();
		distancePenaltyTicks = 0;

		player.displayClientMessage(
				Component.literal("The recaller awakens... Stay close and attune to its call.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
		level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7f, 1.5f);

		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		return true;
	}

	/**
	 * Player right-clicks while the ritual is in the AWAITING_ATTUNEMENT phase.
	 *
	 * @return true if the attunement was accepted
	 */
	public boolean attune(Player player) {
		if (craftingPhase != 2) return false;
		if (!player.getUUID().equals(craftingPlayerUUID)) {
			player.displayClientMessage(
					Component.literal("This ritual is not yours to attune.")
							.withStyle(ChatFormatting.RED),
					true);
			return false;
		}

		attunementCount++;
		craftingPhase = 1;
		attunementTimer = 0;

		player.displayClientMessage(
				Component.literal("The attunement resonates through the vessel.")
						.withStyle(ChatFormatting.GREEN),
				true);
		level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE,
				SoundSource.BLOCKS, 0.5f, 1.5f + attunementCount * 0.3f);

		if (level instanceof ServerLevel serverLevel) {
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					serverLevel, worldPosition, ParticleTypes.REVERSE_PORTAL);
		}

		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		return true;
	}

	/**
	 * Cancels the active ritual. Called when the player shift-right-clicks during
	 * an active ritual.
	 */
	public void cancelRitual(@Nullable Player player) {
		if (craftingPhase == 0) return;

		if (player != null) {
			player.displayClientMessage(
					Component.literal("The ritual is disrupted. The blood subsides.")
							.withStyle(ChatFormatting.RED),
					true);
		}
		level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE,
				SoundSource.BLOCKS, 0.7f, 0.8f);

		resetCraftingState();
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	/**
	 * Provides feedback to the player about the current tendency configuration
	 * when no recipe matches. Shows which tendencies are off and by how much
	 * compared to the closest matching recipe.
	 */
	public void provideTendencyFeedback(Player player) {
		if (contents.get(0).isEmpty()) {
			player.displayClientMessage(
					Component.literal("The recaller lies dormant. It requires a Hematic Memory.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					true);
			return;
		}

		if (curRecipe != null) {
			float totalBloodCost = curRecipe.getBloodCost();
			if (volume.getBloodVolume() < totalBloodCost) {
				player.displayClientMessage(
						Component.literal("A recipe resonates, but the recaller thirsts. (Need "
								+ (int) totalBloodCost + " blood)")
								.withStyle(ChatFormatting.DARK_RED),
						true);
			} else {
				player.displayClientMessage(
						Component.literal("The tendencies align. The recaller is ready.")
								.withStyle(ChatFormatting.DARK_PURPLE),
						true);
			}
			return;
		}

		RecallerRecipe closest = findClosestRecipe();
		if (closest == null) {
			player.displayClientMessage(
					Component.literal("The blood tendencies are discordant. No memory can form.")
							.withStyle(ChatFormatting.GRAY),
					true);
			return;
		}

		StringBuilder hint = new StringBuilder();
		boolean first = true;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			float needed = closest.getTendency().getOrDefault(tend, 0f);
			float current = tendency.getAlignmentByTendency(tend);
			float diff = needed - current;
			if (Math.abs(diff) > 0.01f) {
				if (!first) hint.append(", ");
				String direction = diff > 0 ? "+" : "";
				hint.append(tend.name()).append("(").append(direction)
						.append(String.format("%.1f", diff)).append(")");
				first = false;
			}
		}

		if (closest.getIngredient() != Ingredient.EMPTY
				&& !closest.getIngredient().test(contents.get(1))) {
			if (contents.get(1).isEmpty()) {
				hint.append(first ? "" : " | ").append("Missing catalyst");
			} else {
				hint.append(first ? "" : " | ").append("Wrong catalyst");
			}
		}

		if (hint.length() > 0) {
			player.displayClientMessage(
					Component.literal("Nearest recipe diverges: " + hint)
							.withStyle(ChatFormatting.YELLOW),
					false);
		}
	}

	// ---- Ritual tick handlers (server-side only) ----

	private void handleChanneling(Player player) {
		if (volume.getBloodVolume() < bloodCostPerTick) {
			player.displayClientMessage(
					Component.literal("The recaller's blood runs dry! The ritual falters!")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			cancelRitual(player);
			return;
		}

		volume.subtractBloodVolume(bloodCostPerTick);
		craftingProgress--;

		// Spawn ambient particles during channeling
		if (level instanceof ServerLevel serverLevel && level.getGameTime() % 4 == 0) {
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					serverLevel, worldPosition, ParticleTypes.SOUL_FIRE_FLAME);
		}

		// Check if an attunement checkpoint has been reached
		if (requiredAttunements > 0 && attunementCount < requiredAttunements) {
			int interval = craftingTotalTime / (requiredAttunements + 1);
			int nextCheckpoint = craftingTotalTime - (interval * (attunementCount + 1));

			if (craftingProgress <= nextCheckpoint) {
				craftingPhase = 2;
				attunementTimer = ATTUNEMENT_WINDOW_TICKS;
				player.displayClientMessage(
						Component.literal("The recaller pulses with urgency. Attune to it!")
								.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
						true);
				level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE,
						SoundSource.BLOCKS, 1.0f, 0.5f);

				sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
				return;
			}
		}

		// Check if complete
		if (craftingProgress <= 0) {
			craftingPhase = 3;
		}

		// Periodic progress feedback
		if (craftingProgress > 0 && craftingProgress % 40 == 0) {
			int percent = (int) (((float) (craftingTotalTime - craftingProgress) / craftingTotalTime) * 100);
			player.displayClientMessage(
					Component.literal("Ritual progress: " + percent + "%")
							.withStyle(ChatFormatting.DARK_PURPLE),
					true);
		}

		// Sync state to client periodically
		if (level.getGameTime() % 10 == 0) {
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}
	}

	private void handleAwaitingAttunement(Player player) {
		attunementTimer--;

		// Continue draining blood at a reduced rate while waiting
		if (volume.getBloodVolume() >= bloodCostPerTick * 0.5f) {
			volume.subtractBloodVolume(bloodCostPerTick * 0.5f);
		}

		if (attunementTimer <= 0) {
			// Timeout — blood penalty, auto-continue
			player.displayClientMessage(
					Component.literal("The ritual wavers. Precious blood is lost to your hesitation.")
							.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
					true);
			volume.subtractBloodVolume(bloodCostPerTick * ATTUNEMENT_TIMEOUT_PENALTY_MULTIPLIER);
			level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE,
					SoundSource.BLOCKS, 0.5f, 0.5f);

			attunementCount++;
			craftingPhase = 1;
			attunementTimer = 0;

			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}

		// Pulsing reminder sound
		if (attunementTimer > 0 && attunementTimer % 20 == 0) {
			level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT,
					SoundSource.BLOCKS, 0.6f, 0.5f);
		}
	}

	private void handleCompletion(Player player) {
		// Verify recipe is still valid
		checkRecipe();
		if (curRecipe == null) {
			player.displayClientMessage(
					Component.literal("The ritual unravels. The configuration has changed.")
							.withStyle(ChatFormatting.RED),
					true);
			resetCraftingState();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return;
		}

		// Produce the result
		ItemStack result = curRecipe.getResultItem(level.registryAccess()).copy();

		// Consume inputs
		contents.set(0, ItemStack.EMPTY); // hematic memory
		contents.set(1, ItemStack.EMPTY); // catalyst ingredient
		tendency.setTendency(RecallerRecipe.blank()); // reset tendencies

		// Spawn the result above the block
		ItemEntity resultEntity = new ItemEntity(level,
				worldPosition.getX() + 0.5, worldPosition.getY() + 1.5,
				worldPosition.getZ() + 0.5, result);
		resultEntity.setDeltaMovement(0, 0.1, 0);
		level.addFreshEntity(resultEntity);

		// Effects
		player.displayClientMessage(
				Component.literal("The memory crystallizes. The recaller has spoken.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
				false);
		level.playSound(null, worldPosition, SoundEvents.END_PORTAL_SPAWN,
				SoundSource.BLOCKS, 0.5f, 1.5f);

		if (level instanceof ServerLevel serverLevel) {
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					serverLevel, worldPosition, ParticleTypes.REVERSE_PORTAL);
			com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils.spawnPoof(
					serverLevel, worldPosition, ParticleTypes.ENCHANT);
		}

		resetCraftingState();
		curRecipe = null;
		recipePath = "";
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	private void handleDistancePenalty(@Nullable Player player) {
		distancePenaltyTicks++;

		if (distancePenaltyTicks > MAX_DISTANCE_PENALTY_TICKS) {
			if (player != null) {
				player.displayClientMessage(
						Component.literal("The ritual collapses! You strayed too far!")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
						false);
			}
			level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE,
					SoundSource.BLOCKS, 1.0f, 0.5f);
			resetCraftingState();
			sendUpdates();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			return;
		}

		if (player != null && distancePenaltyTicks % 20 == 0) {
			player.displayClientMessage(
					Component.literal("The connection wavers! Return to the recaller!")
							.withStyle(ChatFormatting.RED),
					true);
		}
	}

	// ---- Ritual helpers ----

	private void resetCraftingState() {
		craftingPhase = 0;
		craftingProgress = 0;
		craftingTotalTime = 0;
		attunementTimer = 0;
		attunementCount = 0;
		requiredAttunements = 0;
		craftingPlayerUUID = null;
		bloodCostPerTick = 0;
		distancePenaltyTicks = 0;
	}

	@Nullable
	private Player findCraftingPlayer() {
		if (craftingPlayerUUID == null || level == null) return null;
		for (Player p : level.players()) {
			if (p.getUUID().equals(craftingPlayerUUID)) return p;
		}
		return null;
	}

	@Nullable
	private RecallerRecipe findClosestRecipe() {
		if (level == null) return null;
		RecallerRecipe closest = null;
		float closestDist = Float.MAX_VALUE;
		for (RecallerRecipe recipe : RecallerRecipe.getAllRecipes(level)) {
			float dist = 0;
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				float recipeVal = recipe.getTendency().getOrDefault(tend, 0f);
				float ourVal = tendency.getAlignmentByTendency(tend);
				dist += Math.abs(recipeVal - ourVal);
			}
			if (dist < closestDist) {
				closestDist = dist;
				closest = recipe;
			}
		}
		return closest;
	}

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
		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag != null) {
			recipePath = tag.getString(TAG_RECIPE);
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			craftingProgress = tag.getInt(TAG_CRAFT_PROGRESS);
			craftingTotalTime = tag.getInt(TAG_CRAFT_TOTAL);
			craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	// NBT
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(2, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.contents);
		if (tag != null) {
			recipePath = tag.getString(TAG_RECIPE);
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			craftingProgress = tag.getInt(TAG_CRAFT_PROGRESS);
			craftingTotalTime = tag.getInt(TAG_CRAFT_TOTAL);
			craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
			if (tag.hasUUID(TAG_CRAFTING_PLAYER)) {
				craftingPlayerUUID = tag.getUUID(TAG_CRAFTING_PLAYER);
			}
			bloodCostPerTick = tag.getFloat(TAG_BLOOD_COST_PER_TICK);
			attunementCount = tag.getInt(TAG_ATTUNEMENT_COUNT);
			requiredAttunements = tag.getInt(TAG_REQUIRED_ATTUNEMENTS);
			attunementTimer = tag.getInt(TAG_ATTUNEMENT_TIMER);
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
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

	@Override
	public void onLoad() {
		volume.setActive(true);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, this.contents);
		if (tag != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
			tag.putString(TAG_RECIPE, recipePath);
			tag.putInt(TAG_CRAFT_PROGRESS, craftingProgress);
			tag.putInt(TAG_CRAFT_TOTAL, craftingTotalTime);
			tag.putInt(TAG_CRAFT_PHASE, craftingPhase);
			if (craftingPlayerUUID != null) {
				tag.putUUID(TAG_CRAFTING_PLAYER, craftingPlayerUUID);
			}
			tag.putFloat(TAG_BLOOD_COST_PER_TICK, bloodCostPerTick);
			tag.putInt(TAG_ATTUNEMENT_COUNT, attunementCount);
			tag.putInt(TAG_REQUIRED_ATTUNEMENTS, requiredAttunements);
			tag.putInt(TAG_ATTUNEMENT_TIMER, attunementTimer);
			for (EnumBloodTendency key : tendency.getTendency().keySet()) {
				if (tendency.getTendency().get(key) != null) {
					tag.putFloat(key.toString(), tendency.getTendency().get(key));
				} else {
					tag.putFloat(key.toString(), 0);
				}
			}
		}
	}

	@Override
	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

}

