package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
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
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
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
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SomaticLoomBlockEntity extends BlockEntity implements IBloodReservoir, IMultiBlockEntity {

	private static final String TAG_CONTENT_SIZE = "contentSize";
	private static final String TAG_BLOOD_LEVEL = "bloodLevel";
	private static final String TAG_RECIPE = "recipe";
	private static final String TAG_CRAFT_PHASE = "craftPhase";
	private static final String TAG_RITUAL_BLOOD_CHARGED = "ritualBloodCharged";
	private static final String TAG_ACTIVE_ORB = "activeOrb";
	private static final String TAG_LAST_ORB_DRAG_GAME_TIME = "lastOrbDragGameTime";
	private static final String TAG_STORED_ENZYMES = "storedEnzymes";
	private static final String TAG_RITUAL_ORBS = "ritualOrbs";

	public static final int PHASE_IDLE = 0;
	public static final int PHASE_AWAITING_BLOOD = 1;
	public static final int PHASE_WEAVING_ORBS = 2;

	private static final int ENZYME_CAPACITY = 64;
	private static final int ORB_DRIFT_GRACE_TICKS = 100;
	private static final double ORB_ABSORPTION_MARGIN = 0.18D;
	private static final double ORB_DRAG_MAX_STEP = 0.055D;
	private static final double ORB_DRAG_EASE = 0.24D;
	private static final double ORB_WANDER_STEP = 0.03D;
	private static final double ORB_WANDER_XZ_RANGE = 10.0D;
	private static final double ORB_WANDER_Y_MIN = 0.35D;
	private static final double ORB_WANDER_Y_MAX = 3.0D;
	private static final int ORB_WANDER_MIN_TARGET_TICKS = 45;
	private static final int ORB_WANDER_RANDOM_TARGET_TICKS = 75;
	private static final double ORB_TARGETING_RANGE = 12.0D;
	private static final double ORB_TARGETING_RADIUS = 1.35D;

	/** Slot 0 = hematic memory; slots 1..n = catalyst items. */
	public NonNullList<ItemStack> contents = createMutableContents(1);

	IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(this).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(this).orElseThrow(IllegalStateException::new);

	private final EnumMap<EnumBloodTendency, Integer> storedEnzymes =
			MemoryWeavingRecipe.blankEnzymeRequirements();
	private final List<RitualOrb> ritualOrbs = new ArrayList<>();

	private MemoryWeavingRecipe curRecipe = null;
	private String recipePath = "";
	private int craftingPhase = PHASE_IDLE;
	private double ritualBloodCharged = 0.0D;
	private int activeOrb = -1;
	private long lastOrbDragGameTime = -1L;

	public SomaticLoomBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.somatic_loom.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			SomaticLoomBlockEntity self) {
		if (self.craftingPhase == PHASE_WEAVING_ORBS) {
			self.tickClientOrbMotion();
		}
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			SomaticLoomBlockEntity self) {
		if (self.craftingPhase == PHASE_WEAVING_ORBS) {
			self.tickOrbDrift();
		} else if (self.craftingPhase == PHASE_AWAITING_BLOOD && level.getGameTime() % 20L == 0L) {
			self.refreshRecipe();
		}
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		if (stack.isEmpty()) {
			return false;
		}

		if (stack.getItem() instanceof EnzymeItem enzyme) {
			depositEnzyme(player, stack, enzyme, player != null && player.isShiftKeyDown());
			return true;
		}

		if (stack.getItem() instanceof RecycledEnzymeItem) {
			msg(player, "Recycled enzymes cannot be loaded into the loom.", ChatFormatting.GRAY, true);
			return false;
		}

		if (contents.get(0).isEmpty() && stack.getItem() == ItemInit.hematic_memory.get()) {
			placeMemory(player, stack, hand);
			return true;
		}

		if (stack.getItem() == ItemInit.lethean_dew.get()) {
			msg(player, "The stored enzymes are already bound into the loom.", ChatFormatting.GRAY, true);
			return false;
		}

		placeCatalyst(player, stack, hand);
		return true;
	}

	public boolean removeItem(@Nullable Player player, boolean removeMemory) {
		if (player == null) return false;
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
		markDirtyAndSync();
		return true;
	}

	public void clearTendency() {
		clearActiveRitual(false);
		refreshRecipe();
		markDirtyAndSync();
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
		int room = ENZYME_CAPACITY - current;
		if (room <= 0) {
			msg(player, tend.name() + " enzyme reservoir is full.", ChatFormatting.DARK_RED, true);
			return;
		}
		int amount = bulk ? Math.min(room, stack.getCount()) : 1;
		storedEnzymes.put(tend, current + amount);
		stack.shrink(amount);
		msg(player, tend.name() + " enzymes: " + storedEnzymes.get(tend) + "/" + ENZYME_CAPACITY,
				ChatFormatting.DARK_PURPLE, true);
		resetEditableRitualProgress();
		refreshRecipe();
		markDirtyAndSync();
	}

	private void placeMemory(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		ItemStack toPlace = stack.copy();
		toPlace.setCount(1);
		contents.set(0, toPlace);
		consumeItem(player, stack, hand);
		resetEditableRitualProgress();
		refreshRecipe();
		provideMissingEnzymeFeedback(player);
		markDirtyAndSync();
	}

	private void placeCatalyst(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {
		ItemStack toPlace = stack.copy();
		toPlace.setCount(1);
		contents.add(toPlace);
		consumeItem(player, stack, hand);
		resetEditableRitualProgress();
		refreshRecipe();
		provideMissingEnzymeFeedback(player);
		markDirtyAndSync();
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
		curRecipe = null;
		recipePath = "";
		if (level == null || contents.get(0).isEmpty()) {
			if (craftingPhase == PHASE_AWAITING_BLOOD) {
				clearActiveRitual(false);
			}
			return;
		}

		for (MemoryWeavingRecipe recipe : MemoryWeavingRecipe.getAllRecipes(level)) {
			if (catalystsMatch(recipe) && hasStoredEnzymesFor(recipe)) {
				curRecipe = recipe;
				recipePath = HLTextUtils.getItemRegistryName(
						recipe.getResultItem(level.registryAccess()).getItem());
				if (craftingPhase == PHASE_IDLE) {
					craftingPhase = PHASE_AWAITING_BLOOD;
				}
				return;
			}
		}

		if (craftingPhase == PHASE_AWAITING_BLOOD) {
			clearActiveRitual(false);
		}
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

	private boolean hasStoredEnzymesFor(MemoryWeavingRecipe recipe) {
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			if (storedEnzymes.getOrDefault(tend, 0) < recipe.getEnzymeRequirement(tend)) {
				return false;
			}
		}
		return true;
	}

	public boolean isCrafting() {
		return craftingPhase != PHASE_IDLE;
	}

	public boolean isAwaitingBlood() {
		return craftingPhase == PHASE_AWAITING_BLOOD && curRecipe != null;
	}

	public boolean isWeavingOrbs() {
		return craftingPhase == PHASE_WEAVING_ORBS;
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

	public List<RitualOrb> getRitualOrbs() {
		return List.copyOf(ritualOrbs);
	}

	public ItemStack getResultItem() {
		return curRecipe != null && level != null
				? curRecipe.getResultItem(level.registryAccess()) : ItemStack.EMPTY;
	}

	public boolean startRitual(Player player) {
		refreshRecipe();
		if (curRecipe == null) {
			return false;
		}
		craftingPhase = PHASE_AWAITING_BLOOD;
		msg(player, "The loom darkens, waiting for blood.", ChatFormatting.DARK_RED, true);
		markDirtyAndSync();
		return true;
	}

	public boolean tryChargeRitualBlood(Player player, double maxAmount, boolean livingStaff) {
		if (player == null || level == null) return false;
		refreshRecipe();
		if (curRecipe == null || craftingPhase != PHASE_AWAITING_BLOOD) {
			return false;
		}

		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (playerVolume == null || !playerVolume.isActive() || playerVolume.getBloodVolume() <= 0) {
			msg(player, "No blood answers the loom.", ChatFormatting.DARK_RED, true);
			return false;
		}

		double needed = Math.max(0.0D, curRecipe.getBloodCost() - ritualBloodCharged);
		if (needed <= 0.0D) {
			beginOrbWeaving(player);
			return true;
		}

		double amount = Math.min(Math.min(maxAmount, needed), playerVolume.getBloodVolume());
		if (amount <= 0.0D) {
			return false;
		}
		playerVolume.subtractBloodVolume(amount);
		ritualBloodCharged += amount;

		if (livingStaff) {
			playSound(SoundEvents.BEACON_POWER_SELECT, 0.35f, 0.8f);
		}
		if (ritualBloodCharged >= curRecipe.getBloodCost() - 0.001D) {
			beginOrbWeaving(player);
		} else if (level.getGameTime() % 10L == 0L) {
			msg(player, "Blood charge: " + (int) ritualBloodCharged + "/" + (int) curRecipe.getBloodCost(),
					ChatFormatting.DARK_RED, true);
		}
		markDirtyAndSync();
		return true;
	}

	public void cancelRitual(@Nullable Player player) {
		if (!isCrafting()) return;
		if (player != null) {
			msg(player, "The weave collapses. Spent blood and enzymes are lost.", ChatFormatting.RED, true);
		}
		playSound(SoundEvents.BEACON_DEACTIVATE, 0.7f, 0.8f);
		clearActiveRitual(true);
		markDirtyAndSync();
	}

	private void beginOrbWeaving(Player player) {
		if (curRecipe == null || level == null) return;
		craftingPhase = PHASE_WEAVING_ORBS;
		activeOrb = -1;
		lastOrbDragGameTime = -1L;
		ritualOrbs.clear();
		spawnRitualOrbs(curRecipe);
		msg(player, "The memory strands scatter. Draw them home.", ChatFormatting.DARK_PURPLE, true);
		playSound(SoundEvents.END_PORTAL_FRAME_FILL, 0.8f, 0.55f);
		if (level instanceof ServerLevel serverLevel) {
			PacketHandler.sendMonolithShatterBurst(Vec3.atCenterOf(worldPosition), 48.0D, serverLevel);
		}
	}

	private void spawnRitualOrbs(MemoryWeavingRecipe recipe) {
		if (level == null) return;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int requirement = recipe.getEnzymeRequirement(tend);
			if (requirement <= 0) continue;
			for (int cost : splitOrbCosts(requirement)) {
				double angle = level.random.nextDouble() * Math.PI * 2.0D;
				double radius = 2.0D + (requirement / (double) MemoryWeavingRecipe.MAX_ENZYMES_PER_TENDENCY) * 5.75D;
				radius += (level.random.nextDouble() - 0.5D) * 0.6D;
				radius = Mth.clamp(radius, 2.0D, 8.0D);
				double y = 1.2D + level.random.nextDouble() * 1.6D;
				Vec3 start = new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
				ritualOrbs.add(new RitualOrb(tend, cost, start));
			}
		}
	}

	private static List<Integer> splitOrbCosts(int requirement) {
		int orbCount = MemoryWeavingRecipe.getOrbCountForRequirement(requirement);
		List<Integer> costs = new ArrayList<>(orbCount);
		for (int i = 0; i < orbCount; i++) {
			costs.add(1);
		}
		return costs;
	}

	private void tickOrbDrift() {
		long gameTime = level == null ? -1L : level.getGameTime();
		if (activeOrb >= 0 && gameTime - lastOrbDragGameTime > 1L) {
			if (activeOrb < ritualOrbs.size()) {
				ritualOrbs.get(activeOrb).clearDragDepth();
			}
			activeOrb = -1;
		}
		for (int i = 0; i < ritualOrbs.size(); i++) {
			RitualOrb orb = ritualOrbs.get(i);
			if (orb.completed) continue;
			if (i == activeOrb) {
				orb.idleTicks = 0;
				continue;
			}
			orb.idleTicks++;
			boolean needsWanderSync = tickWanderingOrb(orb);
			if (needsWanderSync) {
				markDirtyAndSync();
			}
		}
	}

	private boolean tickWanderingOrb(RitualOrb orb) {
		boolean needsWanderSync = level instanceof ServerLevel serverLevel
				&& orb.updateWanderTargetWithinRitualBounds(serverLevel);
		orb.wanderTowardTarget();
		return needsWanderSync;
	}

	private void tickClientOrbMotion() {
		tickOrbDrift();
	}

	public boolean dragSelectedOrb(Player player, double pullStrength) {
		if (player == null || level == null || craftingPhase != PHASE_WEAVING_ORBS) {
			return false;
		}
		int selected = getActiveOrTargetedOrb(player);
		if (selected < 0) {
			clearActiveOrbDrag();
			activeOrb = -1;
			lastOrbDragGameTime = -1L;
			return false;
		}
		int previousActive = activeOrb;
		activeOrb = selected;
		lastOrbDragGameTime = level.getGameTime();
		RitualOrb orb = ritualOrbs.get(selected);
		if (previousActive != selected || orb.dragDepth <= 0.0D) {
			orb.captureDragDepth(player, worldPosition);
		}
		Vec3 dragTarget = findOrbDragTarget(player, orb, selected);
		if (dragTarget == null) {
			orb.clearDragDepth();
			activeOrb = -1;
			lastOrbDragGameTime = -1L;
			return false;
		}
		int neededSpend = orb.requiredCost();
		if (orb.enzymeSpent < neededSpend && !consumeStoredEnzyme(orb.tendency, 1)) {
			msg(player, "The loom lacks " + orb.tendency.name() + " enzymes.", ChatFormatting.DARK_RED, true);
			return false;
		}
		if (orb.enzymeSpent < neededSpend) {
			orb.enzymeSpent++;
		}

		Vec3 offset = orb.offset();
		if (isOrbWithinLoomAbsorptionBounds(offset)) {
			completeOrb(player, selected);
			return true;
		}
		Vec3 desired = dragTarget.subtract(offset).scale(ORB_DRAG_EASE);
		double step = Math.min(desired.length(), ORB_DRAG_MAX_STEP * Math.max(0.5D, pullStrength));
		Vec3 next = desired.lengthSqr() < 0.0001D ? offset : offset.add(desired.normalize().scale(step));
		next = clampOrbOffsetToRitualBounds(next);
		orb.setOffset(next);
		orb.idleTicks = 0;
		spawnOrbDragTrail(orb);
		if (isOrbWithinLoomAbsorptionBounds(next)) {
			completeOrb(player, selected);
		}
		markDirtyAndSync();
		return true;
	}

	private boolean isOrbWithinLoomAbsorptionBounds(Vec3 offset) {
		Vec3 worldOrbCenter = Vec3.atCenterOf(worldPosition).add(offset);
		return new AABB(worldPosition).inflate(ORB_ABSORPTION_MARGIN).contains(worldOrbCenter);
	}

	private int getActiveOrTargetedOrb(Player player) {
		if (activeOrb >= 0 && activeOrb < ritualOrbs.size() && !ritualOrbs.get(activeOrb).completed) {
			return activeOrb;
		}
		return findTargetedOrb(player);
	}

	@Nullable
	private Vec3 findOrbDragTarget(Player player, RitualOrb orb, int selected) {
		if (selected < 0 || selected >= ritualOrbs.size()) {
			return null;
		}
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 center = Vec3.atCenterOf(worldPosition);
		double along = orb.dragDepth > 0.0D ? orb.dragDepth
				: center.add(orb.offset()).subtract(eye).dot(look);
		along = Mth.clamp(along, 1.5D, ORB_TARGETING_RANGE);
		Vec3 targetOffset = eye.add(look.scale(along)).subtract(center);
		return clampOrbOffsetToRitualBounds(targetOffset);
	}

	private static Vec3 clampOrbOffsetToRitualBounds(Vec3 offset) {
		return new Vec3(
				Mth.clamp(offset.x, -ORB_WANDER_XZ_RANGE, ORB_WANDER_XZ_RANGE),
				Mth.clamp(offset.y, ORB_WANDER_Y_MIN, ORB_WANDER_Y_MAX),
				Mth.clamp(offset.z, -ORB_WANDER_XZ_RANGE, ORB_WANDER_XZ_RANGE));
	}

	private int findTargetedOrb(Player player) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 center = Vec3.atCenterOf(worldPosition);
		int best = -1;
		double bestDistance = Double.MAX_VALUE;
		for (int i = 0; i < ritualOrbs.size(); i++) {
			RitualOrb orb = ritualOrbs.get(i);
			if (orb.completed) continue;
			Vec3 orbPos = center.add(orb.offset());
			double along = orbPos.subtract(eye).dot(look);
			if (along < 0 || along > ORB_TARGETING_RANGE) continue;
			Vec3 closestPoint = eye.add(look.scale(along));
			double rayDistance = closestPoint.distanceTo(orbPos);
			if (rayDistance <= ORB_TARGETING_RADIUS && rayDistance < bestDistance) {
				bestDistance = rayDistance;
				best = i;
			}
		}
		return best;
	}

	private void completeOrb(Player player, int index) {
		RitualOrb orb = ritualOrbs.get(index);
		while (orb.enzymeSpent < orb.requiredCost()) {
			if (!consumeStoredEnzyme(orb.tendency, 1)) {
				msg(player, "The final strand frays without enough enzyme.", ChatFormatting.DARK_RED, true);
				return;
			}
			orb.enzymeSpent++;
		}
		orb.completed = true;
		playOrbAbsorptionFeedback(orb);
		orb.setOffset(Vec3.ZERO);
		orb.clearDragDepth();
		activeOrb = -1;
		lastOrbDragGameTime = -1L;
		if (allOrbsCompleted()) {
			tickCompletion(player);
		}
	}

	private void spawnOrbDragTrail(RitualOrb orb) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		Vec3 center = Vec3.atCenterOf(worldPosition);
		Vec3 previous = center.add(orb.previousOffset());
		Vec3 current = center.add(orb.offset());
		if (previous.distanceToSqr(current) < 0.0004D) {
			return;
		}
		for (int i = 1; i <= 3; i++) {
			Vec3 point = previous.lerp(current, i / 4.0D);
			serverLevel.sendParticles(createOrbDust(orb), point.x, point.y, point.z,
					1, 0.025D, 0.025D, 0.025D, 0.005D);
		}
		if (level.getGameTime() % 3L == 0L) {
			serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, current.x, current.y, current.z,
					1, 0.02D, 0.02D, 0.02D, 0.02D);
		}
	}

	private void playOrbAbsorptionFeedback(RitualOrb orb) {
		if (level == null) {
			return;
		}
		Vec3 pos = Vec3.atCenterOf(worldPosition).add(orb.offset());
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME,
				SoundSource.BLOCKS, 0.85F, 1.25F);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_POWER_SELECT,
				SoundSource.BLOCKS, 0.45F, 1.65F);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z,
					12, 0.18D, 0.18D, 0.18D, 0.045D);
			serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z,
					18, 0.28D, 0.28D, 0.28D, 0.06D);
			serverLevel.sendParticles(createOrbDust(orb), pos.x, pos.y, pos.z,
					28, 0.24D, 0.24D, 0.24D, 0.035D);
		}
	}

	private static DustParticleOptions createOrbDust(RitualOrb orb) {
		var color = orb.tendency.getColor();
		return new DustParticleOptions(new Vector3f(
				color.getRed() / 255.0F,
				color.getGreen() / 255.0F,
				color.getBlue() / 255.0F), 1.2F);
	}

	private boolean consumeStoredEnzyme(EnumBloodTendency tendency, int amount) {
		int current = storedEnzymes.getOrDefault(tendency, 0);
		if (current < amount) {
			return false;
		}
		storedEnzymes.put(tendency, current - amount);
		return true;
	}

	private boolean allOrbsCompleted() {
		return !ritualOrbs.isEmpty() && ritualOrbs.stream().allMatch(orb -> orb.completed);
	}

	private void tickCompletion(Player player) {
		MemoryWeavingRecipe completedRecipe = curRecipe;
		if (completedRecipe == null || !catalystsMatch(completedRecipe)) {
			msg(player, "The ritual unravels. Configuration changed.", ChatFormatting.RED, true);
			clearActiveRitual(true);
			markDirtyAndSync();
			return;
		}

		ItemStack result = completedRecipe.getResultItem(level.registryAccess()).copy();
		contents.set(0, ItemStack.EMPTY);
		consumeCatalystsForRecipe(completedRecipe);

		spawnMemoryResult(result);

		msg(player, "The memory crystallizes.", ChatFormatting.DARK_PURPLE, false);
		playSound(SoundEvents.END_PORTAL_SPAWN, 0.5f, 1.5f);
		if (player instanceof ServerPlayer serverPlayer) {
			SkillPointGainEvents.onMemoryWeavingCompleted(serverPlayer);
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
			if (HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer)) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
			}
		}

		clearActiveRitual(false);
		refreshRecipe();
		markDirtyAndSync();
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
		if (loseCharge) {
			ritualBloodCharged = 0.0D;
		} else {
			ritualBloodCharged = 0.0D;
		}
		activeOrb = -1;
		lastOrbDragGameTime = -1L;
		ritualOrbs.clear();
		curRecipe = null;
		recipePath = "";
	}

	private void clearActiveOrbDrag() {
		if (activeOrb >= 0 && activeOrb < ritualOrbs.size()) {
			ritualOrbs.get(activeOrb).clearDragDepth();
		}
	}

	public void provideTendencyFeedback(Player player) {
		if (contents.get(0).isEmpty()) {
			msg(player, "Insert a Hematic Memory.", ChatFormatting.GRAY, true);
			return;
		}
		if (curRecipe != null) {
			if (craftingPhase == PHASE_AWAITING_BLOOD) {
				msg(player, "Project blood into the loom: " + (int) ritualBloodCharged + "/"
						+ (int) curRecipe.getBloodCost(), ChatFormatting.DARK_RED, true);
			} else if (craftingPhase == PHASE_WEAVING_ORBS) {
				msg(player, "Draw the colored memory strands home with a Living Staff.",
						ChatFormatting.DARK_PURPLE, true);
			}
			return;
		}

		if (getCatalysts().isEmpty()) {
			msg(player, "Add catalyst items to shape the memory.", ChatFormatting.GRAY, true);
			return;
		}

		MemoryWeavingRecipe catalystMatch = findCatalystMatchedRecipe();
		if (catalystMatch == null) {
			msg(player, "No recipe accepts this catalyst pattern.", ChatFormatting.YELLOW, true);
			return;
		}
		provideMissingEnzymeFeedback(player);
	}

	private boolean provideMissingEnzymeFeedback(@Nullable Player player) {
		MemoryWeavingRecipe recipe = findCatalystMatchedRecipe();
		if (recipe == null) return false;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int needed = recipe.getEnzymeRequirement(tend);
			int stored = storedEnzymes.getOrDefault(tend, 0);
			if (stored < needed) {
				msg(player, missingEnzymeMessage(EnumBloodTendency.getRepEnzyme(tend).getDescription(), stored, needed),
						ChatFormatting.YELLOW, true);
				return true;
			}
		}
		return false;
	}

	private static Component missingEnzymeMessage(Component enzymeName, int stored, int needed) {
		return Component.literal("Missing " + (needed - stored) + " × ")
				.append(enzymeName)
				.append(Component.literal(" (" + stored + "/" + needed + " stored)."));
	}

	@Nullable
	private MemoryWeavingRecipe findCatalystMatchedRecipe() {
		if (level == null) return null;
		for (MemoryWeavingRecipe recipe : MemoryWeavingRecipe.getAllRecipes(level)) {
			if (catalystsMatch(recipe)) {
				return recipe;
			}
		}
		return null;
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

	private static double horizontalDistance(Vec3 offset) {
		return Math.sqrt(offset.x * offset.x + offset.z * offset.z);
	}

	private void msg(@Nullable Player player, String text, ChatFormatting color, boolean actionBar) {
		msg(player, Component.literal(text), color, actionBar);
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

	private void markDirtyAndSync() {
		if (level == null) return;
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this).inflate(ORB_WANDER_XZ_RANGE);
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

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		saveShared(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		loadShared(tag, registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveShared(tag, registries);
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
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		tag.putString(TAG_RECIPE, recipePath);
		tag.putInt(TAG_CRAFT_PHASE, craftingPhase);
		tag.putDouble(TAG_RITUAL_BLOOD_CHARGED, ritualBloodCharged);
		tag.putInt(TAG_ACTIVE_ORB, activeOrb);
		tag.putLong(TAG_LAST_ORB_DRAG_GAME_TIME, lastOrbDragGameTime);

		CompoundTag enzymeTag = new CompoundTag();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			enzymeTag.putInt(tend.name(), storedEnzymes.getOrDefault(tend, 0));
		}
		tag.put(TAG_STORED_ENZYMES, enzymeTag);

		ListTag orbs = new ListTag();
		for (RitualOrb orb : ritualOrbs) {
			orbs.add(orb.save());
		}
		tag.put(TAG_RITUAL_ORBS, orbs);
	}

	private void loadShared(CompoundTag tag, HolderLookup.Provider registries) {
		int size = Math.max(1, tag.getInt(TAG_CONTENT_SIZE));
		contents = createMutableContents(size);
		ContainerHelper.loadAllItems(tag, contents, registries);
		volume.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
		recipePath = tag.getString(TAG_RECIPE);
		craftingPhase = tag.getInt(TAG_CRAFT_PHASE);
		ritualBloodCharged = tag.getDouble(TAG_RITUAL_BLOOD_CHARGED);
		activeOrb = tag.contains(TAG_ACTIVE_ORB) ? tag.getInt(TAG_ACTIVE_ORB) : -1;
		lastOrbDragGameTime = tag.contains(TAG_LAST_ORB_DRAG_GAME_TIME)
				? tag.getLong(TAG_LAST_ORB_DRAG_GAME_TIME)
				: -1L;

		CompoundTag enzymeTag = tag.getCompound(TAG_STORED_ENZYMES);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			storedEnzymes.put(tend, Mth.clamp(enzymeTag.getInt(tend.name()), 0, ENZYME_CAPACITY));
		}

		ListTag orbs = tag.getList(TAG_RITUAL_ORBS, Tag.TAG_COMPOUND);
		if (level != null && level.isClientSide && !ritualOrbs.isEmpty()) {
			mergeSyncedRitualOrbs(orbs);
		} else {
			ritualOrbs.clear();
			for (int i = 0; i < orbs.size(); i++) {
				ritualOrbs.add(RitualOrb.load(orbs.getCompound(i)));
			}
		}
	}

	private void mergeSyncedRitualOrbs(ListTag orbs) {
		List<RitualOrb> localOrbs = new ArrayList<>(ritualOrbs);
		ritualOrbs.clear();
		for (int i = 0; i < orbs.size(); i++) {
			RitualOrb synced = RitualOrb.load(orbs.getCompound(i));
			if (i < localOrbs.size()) {
				RitualOrb local = localOrbs.get(i);
				synced.adoptServerOffsetSmoothly(local);
			}
			ritualOrbs.add(synced);
		}
	}

	private static NonNullList<ItemStack> createMutableContents(int size) {
		NonNullList<ItemStack> items = NonNullList.createWithCapacity(Math.max(1, size));
		for (int i = 0; i < Math.max(1, size); i++) {
			items.add(ItemStack.EMPTY);
		}
		return items;
	}

	public static class RitualOrb {
		private final EnumBloodTendency tendency;
		private final int enzymeCost;
		private final Vec3 startOffset;
		private double x;
		private double y;
		private double z;
		private double previousX;
		private double previousY;
		private double previousZ;
		private double dragDepth = -1.0D;
		private double wanderTargetX;
		private double wanderTargetY;
		private double wanderTargetZ;
		private int wanderTargetTicks;
		private int idleTicks;
		private int enzymeSpent;
		private boolean completed;

		private RitualOrb(EnumBloodTendency tendency, int enzymeCost, Vec3 startOffset) {
			this.tendency = tendency;
			this.enzymeCost = enzymeCost;
			this.startOffset = startOffset;
			primeOffset(startOffset);
		}

		public EnumBloodTendency tendency() {
			return tendency;
		}

		public int enzymeCost() {
			return enzymeCost;
		}

		public boolean completed() {
			return completed;
		}

		public Vec3 offset() {
			return new Vec3(x, y, z);
		}

		public Vec3 previousOffset() {
			return new Vec3(previousX, previousY, previousZ);
		}

		public Vec3 renderOffset(float partialTicks) {
			double t = Mth.clamp(partialTicks, 0.0F, 1.0F);
			return new Vec3(
					Mth.lerp(t, previousX, x),
					Mth.lerp(t, previousY, y),
					Mth.lerp(t, previousZ, z));
		}

		public Vec3 startOffset() {
			return startOffset;
		}

		private void setOffset(Vec3 offset) {
			previousX = x;
			previousY = y;
			previousZ = z;
			x = offset.x;
			y = offset.y;
			z = offset.z;
		}

		private void primeOffset(Vec3 offset) {
			x = offset.x;
			y = offset.y;
			z = offset.z;
			previousX = x;
			previousY = y;
			previousZ = z;
			setWanderTarget(offset, 0);
		}

		private void captureDragDepth(Player player, BlockPos loomPos) {
			Vec3 eye = player.getEyePosition();
			Vec3 look = player.getLookAngle().normalize();
			Vec3 center = Vec3.atCenterOf(loomPos);
			dragDepth = Mth.clamp(center.add(offset()).subtract(eye).dot(look), 1.5D, ORB_TARGETING_RANGE);
		}

		private void clearDragDepth() {
			dragDepth = -1.0D;
		}

		private void adoptServerOffsetSmoothly(RitualOrb localOrb) {
			previousX = localOrb.x;
			previousY = localOrb.y;
			previousZ = localOrb.z;
			if (dragDepth <= 0.0D) {
				dragDepth = localOrb.dragDepth;
			}
		}

		private boolean updateWanderTargetWithinRitualBounds(ServerLevel level) {
			boolean needsWanderSync = false;
			Vec3 target = wanderTarget();
			if (wanderTargetTicks-- <= 0 || target.distanceToSqr(offset()) < 0.12D) {
				chooseNewWanderTarget(level);
				needsWanderSync = true;
			}
			return needsWanderSync;
		}

		private void chooseNewWanderTarget(ServerLevel level) {
			double x = level.random.nextDouble() * ORB_WANDER_XZ_RANGE * 2.0D - ORB_WANDER_XZ_RANGE;
			double y = ORB_WANDER_Y_MIN
					+ level.random.nextDouble() * (ORB_WANDER_Y_MAX - ORB_WANDER_Y_MIN);
			double z = level.random.nextDouble() * ORB_WANDER_XZ_RANGE * 2.0D - ORB_WANDER_XZ_RANGE;
			int ticks = ORB_WANDER_MIN_TARGET_TICKS + level.random.nextInt(ORB_WANDER_RANDOM_TARGET_TICKS + 1);
			setWanderTarget(clampOrbOffsetToRitualBounds(new Vec3(x, y, z)), ticks);
		}

		private Vec3 wanderTarget() {
			return new Vec3(wanderTargetX, wanderTargetY, wanderTargetZ);
		}

		private void setWanderTarget(Vec3 target, int ticks) {
			Vec3 clamped = clampOrbOffsetToRitualBounds(target);
			wanderTargetX = clamped.x;
			wanderTargetY = clamped.y;
			wanderTargetZ = clamped.z;
			wanderTargetTicks = Math.max(0, ticks);
		}

		private void wanderTowardTarget() {
			Vec3 offset = offset();
			Vec3 desired = wanderTarget().subtract(offset);
			if (desired.lengthSqr() < 0.0004D) {
				setOffset(clampOrbOffsetToRitualBounds(wanderTarget()));
				return;
			}
			double step = Math.min(desired.length(), ORB_WANDER_STEP);
			setOffset(clampOrbOffsetToRitualBounds(offset.add(desired.normalize().scale(step))));
		}

		private int requiredCost() {
			double startDistance = horizontalDistance(startOffset);
			double currentDistance = horizontalDistance(offset());
			int driftPenalty = Math.max(0, (int) Math.floor((currentDistance - startDistance) * 0.75D));
			return enzymeCost + Math.min(8, driftPenalty);
		}

		private CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putString("tendency", tendency.name());
			tag.putInt("enzymeCost", enzymeCost);
			tag.putDouble("startX", startOffset.x);
			tag.putDouble("startY", startOffset.y);
			tag.putDouble("startZ", startOffset.z);
			tag.putDouble("x", x);
			tag.putDouble("y", y);
			tag.putDouble("z", z);
			tag.putDouble("previousX", previousX);
			tag.putDouble("previousY", previousY);
			tag.putDouble("previousZ", previousZ);
			tag.putDouble("dragDepth", dragDepth);
			tag.putDouble("wanderTargetX", wanderTargetX);
			tag.putDouble("wanderTargetY", wanderTargetY);
			tag.putDouble("wanderTargetZ", wanderTargetZ);
			tag.putInt("wanderTargetTicks", wanderTargetTicks);
			tag.putInt("idleTicks", idleTicks);
			tag.putInt("enzymeSpent", enzymeSpent);
			tag.putBoolean("completed", completed);
			return tag;
		}

		private static RitualOrb load(CompoundTag tag) {
			EnumBloodTendency tendency = EnumBloodTendency.valueOf(tag.getString("tendency"));
			Vec3 start = new Vec3(tag.getDouble("startX"), tag.getDouble("startY"), tag.getDouble("startZ"));
			RitualOrb orb = new RitualOrb(tendency, tag.getInt("enzymeCost"), start);
			orb.x = tag.getDouble("x");
			orb.y = tag.getDouble("y");
			orb.z = tag.getDouble("z");
			orb.previousX = tag.contains("previousX") ? tag.getDouble("previousX") : orb.x;
			orb.previousY = tag.contains("previousY") ? tag.getDouble("previousY") : orb.y;
			orb.previousZ = tag.contains("previousZ") ? tag.getDouble("previousZ") : orb.z;
			orb.dragDepth = tag.contains("dragDepth") ? tag.getDouble("dragDepth") : -1.0D;
			orb.wanderTargetX = tag.contains("wanderTargetX") ? tag.getDouble("wanderTargetX") : orb.startOffset.x;
			orb.wanderTargetY = tag.contains("wanderTargetY") ? tag.getDouble("wanderTargetY") : orb.startOffset.y;
			orb.wanderTargetZ = tag.contains("wanderTargetZ") ? tag.getDouble("wanderTargetZ") : orb.startOffset.z;
			orb.wanderTargetTicks = tag.getInt("wanderTargetTicks");
			orb.idleTicks = tag.getInt("idleTicks");
			orb.enzymeSpent = tag.getInt("enzymeSpent");
			orb.completed = tag.getBoolean("completed");
			return orb;
		}
	}
}
