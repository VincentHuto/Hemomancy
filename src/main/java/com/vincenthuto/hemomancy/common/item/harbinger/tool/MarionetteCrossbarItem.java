package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.summon.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MarionetteCrossbarItem extends Item {
	public static final int RADIAL_HOLD_TICKS = 10;
	private static final int USE_DURATION = 72000;
	public static final String TAG_CROSSBAR_ID = "crossbar_id";
	public static final String TAG_SELECTED_SUMMON = "selected_summon";
	public static final String TAG_THREAD = "thread";
	public static final String TAG_BOUND_OWNER = "bound_owner";
	public static final String TAG_THREAD_CAPACITY = "thread_capacity";
	public static final String TAG_COMMAND_MODE = "command_mode";
	public static final String TAG_GUARD_POSITION = "guard_position";
	public static final String TAG_GUARD_DIMENSION = "guard_dimension";

	public MarionetteCrossbarItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		CompoundTag tag = getData(stack);
		String selected = tag.getString(TAG_SELECTED_SUMMON);
		int thread = tag.getInt(TAG_THREAD);
		tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.thread",
				thread, getThreadCapacity(stack)).withStyle(ChatFormatting.DARK_RED));
		if (!selected.isBlank()) {
			tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.selected",
					Component.translatable("entity.hemomancy." + selected)).withStyle(ChatFormatting.RED));
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.unbound")
					.withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(Component.translatable(tag.hasUUID(TAG_BOUND_OWNER)
				? "tooltip.hemomancy.marionette_crossbar.attuned"
				: "tooltip.hemomancy.marionette_crossbar.unattuned")
				.withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.controls")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.clamp(Math.round(13.0F * getThread(stack) / getThreadCapacity(stack)), 0, 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float fill = Math.max(0.0F, Math.min(1.0F, getThread(stack) / (float) getThreadCapacity(stack)));
		int red = 96 + Math.round(128 * fill);
		int green = 8 + Math.round(24 * fill);
		int blue = 16 + Math.round(20 * fill);
		return (red << 16) | (green << 8) | blue;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, level, entity, itemSlot, isSelected);
		if (level.isClientSide || !(entity instanceof Player player)) {
			return;
		}
		if (isBoundTo(stack, player)) {
			ensureCrossbarId(stack);
			updateThreadCapacity(stack, player);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!isBoundTo(stack, player)) {
			if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
				if (getBoundOwner(stack) == null) bindCrossbar(stack, serverPlayer);
				else validateControl(stack, player, true);
			}
			return InteractionResultHolder.consume(stack);
		}
		if (!level.isClientSide) {
			ensureCrossbarId(stack);
		}
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		int elapsed = getUseDuration(stack, entity) - timeLeft;
		if (!level.isClientSide && entity instanceof ServerPlayer player && elapsed < RADIAL_HOLD_TICKS) {
			callOrRecallSelectedSummon(stack, player);
		}
	}

	public static Optional<ItemStack> findCrossbar(Player player, UUID crossbarId) {
		if (player == null || crossbarId == null) {
			return Optional.empty();
		}
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.getItem() instanceof MarionetteCrossbarItem && crossbarId.equals(readCrossbarId(stack))
					&& isBoundTo(stack, player)) {
				return Optional.of(stack);
			}
		}
		return Optional.empty();
	}

	public static Optional<ItemStack> findFirstCrossbar(Player player) {
		if (player == null) {
			return Optional.empty();
		}
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			if (held.getItem() instanceof MarionetteCrossbarItem && isBoundTo(held, player)) {
				return Optional.of(held);
			}
		}
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.getItem() instanceof MarionetteCrossbarItem && isBoundTo(stack, player)) {
				return Optional.of(stack);
			}
		}
		return Optional.empty();
	}

	public static Optional<ItemStack> findFirstItem(Player player, Item item) {
		if (player == null) {
			return Optional.empty();
		}
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.getItem() == item) {
				return Optional.of(stack);
			}
		}
		return Optional.empty();
	}

	public static Optional<ItemStack> findEquippedCrossbar(Player player, UUID crossbarId) {
		if (player == null || crossbarId == null) {
			return Optional.empty();
		}
		ItemStack mainHand = player.getMainHandItem();
		if (isOwnedOrMigratedMatchingCrossbar(mainHand, crossbarId, player)) {
			return Optional.of(mainHand);
		}
		ItemStack offHand = player.getOffhandItem();
		if (isOwnedOrMigratedMatchingCrossbar(offHand, crossbarId, player)) {
			return Optional.of(offHand);
		}
		return Optional.empty();
	}

	public static int getThread(ItemStack stack) {
		return getData(stack).getInt(TAG_THREAD);
	}

	public static int getThreadCapacity(ItemStack stack) {
		int stored = getData(stack).getInt(TAG_THREAD_CAPACITY);
		return Math.max(PuppeteerSummonRules.THREAD_CAPACITY, stored);
	}

	public static void updateThreadCapacity(ItemStack stack, Player owner) {
		if (!isBoundTo(stack, owner)) {
			return;
		}
		int capacity = PuppeteerSummonRules.threadCapacity(SkillPointHelper.getBoundCommandLevel(owner));
		int currentCapacity = getThreadCapacity(stack);
		int currentThread = getThread(stack);
		int clampedThread = PuppeteerSummonRules.clampThreadToCapacity(currentThread, capacity);
		if (currentCapacity == capacity && currentThread == clampedThread) {
			return;
		}
		CompoundTag tag = getData(stack);
		tag.putInt(TAG_THREAD_CAPACITY, capacity);
		tag.putInt(TAG_THREAD, clampedThread);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static int addThread(ItemStack stack, int amount) {
		CompoundTag tag = getData(stack);
		int thread = Math.min(getThreadCapacity(stack), Math.max(0, tag.getInt(TAG_THREAD)) + Math.max(0, amount));
		tag.putInt(TAG_THREAD, thread);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return thread;
	}

	public static boolean consumeThread(ItemStack stack, int amount) {
		CompoundTag tag = getData(stack);
		int thread = tag.getInt(TAG_THREAD);
		if (amount <= 0) {
			return true;
		}
		if (thread < amount) {
			return false;
		}
		tag.putInt(TAG_THREAD, thread - amount);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return true;
	}

	public static String getSelectedSummonName(ItemStack stack) {
		return getData(stack).getString(TAG_SELECTED_SUMMON);
	}

	public static void setSelectedSummonName(ItemStack stack, String summonName) {
		CompoundTag tag = getData(stack);
		tag.putString(TAG_SELECTED_SUMMON, summonName == null ? "" : summonName);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean bindCrossbar(ItemStack crossbar, ServerPlayer player) {
		UUID currentOwner = getBoundOwner(crossbar);
		if (!PuppeteerSummonRules.canAttuneCrossbar(currentOwner, player.getUUID())) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.crossbar.foreign")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		CompoundTag tag = getData(crossbar);
		int capacity = PuppeteerSummonRules.threadCapacity(SkillPointHelper.getBoundCommandLevel(player));
		tag.putUUID(TAG_BOUND_OWNER, player.getUUID());
		tag.putInt(TAG_THREAD_CAPACITY, capacity);
		tag.putInt(TAG_THREAD, PuppeteerSummonRules.clampThreadToCapacity(tag.getInt(TAG_THREAD), capacity));
		crossbar.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		ensureCrossbarId(crossbar);
		HemoCapabilityAccess.getKnownSummons(player)
				.ifPresent(knownSummons -> KnownSummonEvents.sync(player, knownSummons));
		player.playSound(SoundEvents.CHAIN_PLACE, 0.5F, 0.8F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.bind.success"), true);
		return true;
	}

	public static PuppeteerCommandMode getCommandMode(ItemStack stack) {
		return PuppeteerCommandMode.fromSerializedName(getData(stack).getString(TAG_COMMAND_MODE));
	}

	public static void setCommandMode(ItemStack stack, PuppeteerCommandMode mode) {
		CompoundTag tag = getData(stack);
		tag.putString(TAG_COMMAND_MODE, (mode == null ? PuppeteerCommandMode.FOLLOW : mode).serializedName());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static Optional<BlockPos> getGuardPosition(ItemStack stack) {
		CompoundTag tag = getData(stack);
		return tag.contains(TAG_GUARD_POSITION) ? Optional.of(BlockPos.of(tag.getLong(TAG_GUARD_POSITION)))
				: Optional.empty();
	}

	public static Optional<ResourceKey<Level>> getGuardDimension(ItemStack stack) {
		String stored = getData(stack).getString(TAG_GUARD_DIMENSION);
		ResourceLocation id = ResourceLocation.tryParse(stored);
		return id == null ? Optional.empty() : Optional.of(ResourceKey.create(Registries.DIMENSION, id));
	}

	public static void setGuardAnchor(ItemStack stack, BlockPos position, ResourceKey<Level> dimension) {
		if (position == null || dimension == null) {
			clearGuardAnchor(stack);
			return;
		}
		CompoundTag tag = getData(stack);
		tag.putLong(TAG_GUARD_POSITION, position.asLong());
		tag.putString(TAG_GUARD_DIMENSION, dimension.location().toString());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clearGuardAnchor(ItemStack stack) {
		CompoundTag tag = getData(stack);
		tag.remove(TAG_GUARD_POSITION);
		tag.remove(TAG_GUARD_DIMENSION);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean selectSummon(ItemStack stack, ServerPlayer player, String summonName) {
		if (!validateControl(stack, player, true)) {
			return false;
		}
		if (summonName == null || summonName.isBlank()) {
			return false;
		}
		Optional<PuppeteerSummonDefinition> definition = PuppeteerSummonDefinitions.byName(summonName);
		if (definition.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.invalid")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		boolean known = HemoCapabilityAccess.getKnownSummons(player)
				.map(k -> k.isKnown(definition.get()))
				.orElse(false);
		if (!known) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.unknown")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		setSelectedSummonName(stack, summonName);
		player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.35F, 0.8F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.select.success",
				Component.translatable(definition.get().translationKey())), true);
		return true;
	}

	public static boolean prepareSelectedSummon(ItemStack stack, ServerPlayer player, String summonName) {
		if (!validateControl(stack, player, true)) {
			return false;
		}
		if (summonName != null && !summonName.isBlank()
				&& !summonName.equals(getSelectedSummonName(stack))
				&& !selectSummon(stack, player, summonName)) {
			return false;
		}
		String selected = getSelectedSummonName(stack);
		if (selected == null || selected.isBlank()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.none")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		player.displayClientMessage(Component.translatable("hemomancy.summon.spindle.prepared",
				Component.translatable("entity.hemomancy." + selected)).withStyle(ChatFormatting.RED), true);
		return true;
	}

	public static UUID ensureCrossbarId(ItemStack stack) {
		UUID existing = readCrossbarId(stack);
		if (existing != null) {
			return existing;
		}
		UUID created = UUID.randomUUID();
		CompoundTag tag = getData(stack);
		tag.putUUID(TAG_CROSSBAR_ID, created);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return created;
	}

	public static UUID getCrossbarId(ItemStack stack) {
		return readCrossbarId(stack);
	}

	public static void callOrRecallSelectedSummon(ItemStack stack, ServerPlayer player) {
		if (!validateControl(stack, player, true)) {
			return;
		}
		String selected = getSelectedSummonName(stack);
		if (selected == null || selected.isBlank()) {
			List<String> known = HemoCapabilityAccess.getKnownSummons(player)
					.map(k -> k.getKnownSummonNames())
					.orElse(List.of());
			if (!known.isEmpty()) {
				selected = known.get(0);
				setSelectedSummonName(stack, selected);
			}
		}
		if (selected == null || selected.isBlank()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.none")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		Optional<PuppeteerSummonDefinition> defOpt = PuppeteerSummonDefinitions.byName(selected);
		if (defOpt.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.invalid")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		PuppeteerSummonDefinition definition = defOpt.get();
		boolean known = HemoCapabilityAccess.getKnownSummons(player)
				.map(k -> k.isKnown(definition))
				.orElse(false);
		if (!known) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.unknown")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		UUID crossbarId = ensureCrossbarId(stack);
		List<Mob> alreadyActive = activeSummonsForCrossbar(player, crossbarId, selected);
		if (!alreadyActive.isEmpty()) {
			for (Mob mob : alreadyActive) {
				mob.discard();
			}
			player.playSound(SoundEvents.CHAIN_BREAK, 0.45F, 0.9F);
			player.displayClientMessage(Component.translatable("hemomancy.summon.recalled",
					Component.translatable(definition.translationKey())), true);
			return;
		}
		List<Mob> activeBodies = activeSummonsForOwner(player);
		int shapedBodyCap = PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(player));
		int claimedBonusCap = BoundSummonBehavior.claimedWillBonusCap(player);
		int shapedBodies = (int) activeBodies.stream()
				.filter(body -> !BoundSummonBehavior.isClaimedWill(body))
				.count();
		if (!PuppeteerSummonRules.canRetainBody(false, activeBodies.size(), shapedBodies,
				shapedBodyCap, claimedBonusCap)) {
			int totalCap = shapedBodyCap + claimedBonusCap;
			String messageKey = activeBodies.size() >= totalCap
					? "hemomancy.summon.cap.reached"
					: "hemomancy.summon.cap.shaped";
			int displayedCap = activeBodies.size() >= totalCap ? totalCap : shapedBodyCap;
			player.displayClientMessage(Component.translatable(messageKey, displayedCap)
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		int summonCost = summonThreadCost(player, definition);
		if (getThread(stack) < summonCost) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.thread.low")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		Optional<Mob> mobOpt = PuppeteerSummonFactory.create(definition, player.level(), player,
				crossbarId, SkillPointHelper.getLivingSinewLevel(player));
		if (mobOpt.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.failed")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (!consumeThread(stack, summonCost)) {
			return;
		}
		Mob mob = mobOpt.get();
		if (!player.level().addFreshEntity(mob)) {
			addThread(stack, summonCost);
			mob.discard();
			player.displayClientMessage(Component.translatable("hemomancy.summon.failed")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		player.getCooldowns().addCooldown(stack.getItem(), 20);
		player.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 0.55F, 0.75F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.called",
				Component.translatable(definition.translationKey())), true);
	}

	public static List<Mob> activeSummonsForOwner(Player player) {
		if (player == null) {
			return List.of();
		}
		if (player instanceof ServerPlayer serverPlayer) {
			List<Mob> active = new ArrayList<>();
			for (ServerLevel level : serverPlayer.server.getAllLevels()) {
				for (Entity entity : level.getAllEntities()) {
					if (entity instanceof Mob mob
							&& mob.isAlive()
							&& mob instanceof BoundPuppeteerSummon bound
							&& player.getUUID().equals(bound.hemomancy$getOwnerUUID())) {
						active.add(mob);
					}
				}
			}
			return active;
		}
		return player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(128.0),
				mob -> mob.isAlive()
						&& mob instanceof BoundPuppeteerSummon bound
						&& player.getUUID().equals(bound.hemomancy$getOwnerUUID()));
	}

	public static List<Mob> activeSummonsForCrossbar(Player player, UUID crossbarId, String summonName) {
		List<Mob> active = new ArrayList<>();
		if (player == null || crossbarId == null) {
			return active;
		}
		for (Mob mob : activeSummonsForOwner(player)) {
			if (mob instanceof BoundPuppeteerSummon bound
					&& crossbarId.equals(bound.hemomancy$getCrossbarUUID())
					&& (summonName == null || summonName.equals(bound.hemomancy$getSummonName()))) {
				active.add(mob);
			}
		}
		return active;
	}

	public static int focusTarget(ServerPlayer player, ItemStack crossbar, LivingEntity target) {
		if (!validateControl(crossbar, player, true) || !(target instanceof Enemy) || target == player
				|| isFriendlyBoundSummon(target)) {
			return 0;
		}
		UUID crossbarId = ensureCrossbarId(crossbar);
		double tetherRange = PuppeteerSummonRules.effectiveCommandRange(SkillPointHelper.getFarTetherLevel(player),
				SkillPointHelper.getBoundCommandLevel(player), BoundSummonBehavior.hasEquippedMorphling(player)
						&& BoundSummonBehavior.hasActiveOwnedTether(player));
		if (!PuppeteerSummonRules.withinTetherRange(player.distanceToSqr(target), tetherRange)) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.focus.out_of_range")
					.withStyle(ChatFormatting.GRAY), true);
			return 0;
		}
		int focused = 0;
		for (Mob summon : player.serverLevel().getEntitiesOfClass(Mob.class,
				player.getBoundingBox().inflate(tetherRange * 3.0), Mob::isAlive)) {
			if (summon instanceof BoundPuppeteerSummon bound
					&& player.getUUID().equals(bound.hemomancy$getOwnerUUID())
					&& crossbarId.equals(bound.hemomancy$getCrossbarUUID())
					&& summon.canAttack(target)) {
				BoundSummonBehavior.setFocusedTarget(summon, target);
				focused++;
			}
		}
		if (focused > 0) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.focused",
					target.getDisplayName(), focused).withStyle(ChatFormatting.RED), true);
		}
		return focused;
	}

	private static boolean isFriendlyBoundSummon(LivingEntity target) {
		return target instanceof BoundPuppeteerSummon bound
				&& !bound.hemomancy$isTrialSummon()
				&& bound.hemomancy$getOwnerUUID() != null;
	}

	public static UUID getBoundOwner(ItemStack stack) {
		CompoundTag tag = getData(stack);
		return tag.hasUUID(TAG_BOUND_OWNER) ? tag.getUUID(TAG_BOUND_OWNER) : null;
	}

	public static boolean isBoundTo(ItemStack stack, Player player) {
		return player != null && player.getUUID().equals(getBoundOwner(stack));
	}

	public static boolean validateControl(ItemStack stack, Player player, boolean notify) {
		UUID owner = getBoundOwner(stack);
		boolean valid = owner != null && player != null && owner.equals(player.getUUID());
		if (!valid && notify && player != null) {
			String key = owner == null
					? "hemomancy.summon.crossbar.unattuned"
					: "hemomancy.summon.crossbar.foreign";
			player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
		}
		return valid;
	}

	public static int summonThreadCost(Player player, PuppeteerSummonDefinition definition) {
		return PuppeteerSummonRules.adjustedThreadCost(definition.threadSummonCost(),
				SkillPointHelper.getThreadEconomyLevel(player));
	}

	private static CompoundTag getData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static UUID readCrossbarId(ItemStack stack) {
		CompoundTag tag = getData(stack);
		return tag.hasUUID(TAG_CROSSBAR_ID) ? tag.getUUID(TAG_CROSSBAR_ID) : null;
	}

	private static boolean isMatchingCrossbar(ItemStack stack, UUID crossbarId) {
		return stack.getItem() instanceof MarionetteCrossbarItem && crossbarId.equals(readCrossbarId(stack));
	}

	private static boolean isOwnedOrMigratedMatchingCrossbar(ItemStack stack, UUID crossbarId, Player player) {
		if (!isMatchingCrossbar(stack, crossbarId)) {
			return false;
		}
		UUID owner = getBoundOwner(stack);
		if (owner == null) {
			CompoundTag tag = getData(stack);
			int capacity = PuppeteerSummonRules.threadCapacity(SkillPointHelper.getBoundCommandLevel(player));
			tag.putUUID(TAG_BOUND_OWNER, player.getUUID());
			tag.putInt(TAG_THREAD_CAPACITY, capacity);
			tag.putInt(TAG_THREAD, PuppeteerSummonRules.clampThreadToCapacity(tag.getInt(TAG_THREAD), capacity));
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
		return isBoundTo(stack, player);
	}
}
