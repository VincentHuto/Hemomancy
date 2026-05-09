package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonFactory;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MarionetteCrossbarItem extends Item {
	public static final String TAG_CROSSBAR_ID = "crossbar_id";
	public static final String TAG_SELECTED_SUMMON = "selected_summon";
	public static final String TAG_THREAD = "thread";

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
				thread, PuppeteerSummonRules.THREAD_CAPACITY).withStyle(ChatFormatting.DARK_RED));
		if (!selected.isBlank()) {
			tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.selected",
					Component.translatable("entity.hemomancy." + selected)).withStyle(ChatFormatting.RED));
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.unbound")
					.withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(Component.translatable("tooltip.hemomancy.marionette_crossbar.controls")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, level, entity, itemSlot, isSelected);
		if (level.isClientSide || !(entity instanceof Player player) || player.tickCount % 1200 != 0) {
			return;
		}
		UUID crossbarId = ensureCrossbarId(stack);
		List<Mob> active = activeSummonsForCrossbar(player, crossbarId, null);
		if (active.isEmpty()) {
			return;
		}
		int upkeep = 0;
		for (Mob mob : active) {
			if (mob instanceof BoundPuppeteerSummon bound) {
				upkeep += PuppeteerSummonDefinitions.byName(bound.hemomancy$getSummonName())
						.map(PuppeteerSummonDefinition::threadUpkeepPerMinute)
						.orElse(0);
			}
		}
		if (upkeep > 0 && !consumeThread(stack, upkeep)) {
			for (Mob mob : active) {
				mob.discard();
			}
			player.displayClientMessage(Component.translatable("hemomancy.summon.upkeep.failed")
					.withStyle(ChatFormatting.GRAY), true);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		ensureCrossbarId(stack);
		if (level.isClientSide) {
			return InteractionResultHolder.success(stack);
		}
		if (player.isShiftKeyDown()) {
			cycleSelectedSummon(stack, player);
			return InteractionResultHolder.consume(stack);
		}
		callOrRecallSelectedSummon(stack, (ServerPlayer) player);
		return InteractionResultHolder.consume(stack);
	}

	public static Optional<ItemStack> findCrossbar(Player player, UUID crossbarId) {
		if (player == null || crossbarId == null) {
			return Optional.empty();
		}
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.getItem() instanceof MarionetteCrossbarItem && crossbarId.equals(readCrossbarId(stack))) {
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
			if (held.getItem() instanceof MarionetteCrossbarItem) {
				return Optional.of(held);
			}
		}
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.getItem() instanceof MarionetteCrossbarItem) {
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
		if (isMatchingCrossbar(mainHand, crossbarId)) {
			return Optional.of(mainHand);
		}
		ItemStack offHand = player.getOffhandItem();
		if (isMatchingCrossbar(offHand, crossbarId)) {
			return Optional.of(offHand);
		}
		return Optional.empty();
	}

	public static int getThread(ItemStack stack) {
		return getData(stack).getInt(TAG_THREAD);
	}

	public static int addThread(ItemStack stack, int amount) {
		CompoundTag tag = getData(stack);
		int thread = PuppeteerSummonRules.refilledThread(tag.getInt(TAG_THREAD), amount);
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

	public static void bindCrossbar(ItemStack crossbar, ServerPlayer player) {
		List<String> known = HemoCapabilityAccess.getKnownSummons(player)
				.map(k -> k.getKnownSummonNames())
				.orElse(List.of());
		if (known.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.bind.none")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		String selected = getSelectedSummonName(crossbar);
		if (selected == null || selected.isBlank() || !known.contains(selected)) {
			selected = known.get(0);
			setSelectedSummonName(crossbar, selected);
		}
		ensureCrossbarId(crossbar);
		HemoCapabilityAccess.getKnownSummons(player)
				.ifPresent(knownSummons -> KnownSummonEvents.sync(player, knownSummons));
		player.playSound(SoundEvents.CHAIN_PLACE, 0.5F, 0.8F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.bind.success",
				Component.translatable("entity.hemomancy." + selected)), true);
	}

	public static void windCrossbar(ItemStack crossbar, ItemStack threadStack, ServerPlayer player) {
		int before = getThread(crossbar);
		if (before >= PuppeteerSummonRules.THREAD_CAPACITY) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.refill.full")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		int after = addThread(crossbar, PuppeteerSummonRules.THREAD_PER_ITEM);
		if (!player.getAbilities().instabuild) {
			threadStack.shrink(1);
		}
		player.playSound(SoundEvents.WOOL_PLACE, 0.45F, 0.75F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.refill.success",
				after, PuppeteerSummonRules.THREAD_CAPACITY), true);
	}

	public static void unlockNextSummon(ItemStack quintessence, ServerPlayer player) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		Optional<PuppeteerSummonDefinition> next = HemoCapabilityAccess.getKnownSummons(player)
				.flatMap(known -> PuppeteerSummonDefinitions.all().stream()
						.filter(definition -> definition.requiredDegree() <= degree)
						.filter(definition -> !known.isKnown(definition))
						.findFirst());
		if (next.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.unlock.none")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		PuppeteerSummonDefinition definition = next.get();
		if (KnownSummonEvents.grantSummon(player, definition)) {
			if (!player.getAbilities().instabuild) {
				quintessence.shrink(1);
			}
			player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 0.65F, 0.65F);
			player.displayClientMessage(Component.translatable("hemomancy.summon.unlock.success",
					Component.translatable(definition.translationKey())).withStyle(ChatFormatting.RED), false);
		}
	}

	public static boolean selectSummon(ItemStack stack, ServerPlayer player, String summonName) {
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

	private void cycleSelectedSummon(ItemStack stack, Player player) {
		List<String> knownNames = HemoCapabilityAccess.getKnownSummons(player)
				.map(known -> known.getKnownSummonNames())
				.orElse(List.of());
		if (knownNames.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.select.none")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		String current = getSelectedSummonName(stack);
		int idx = knownNames.indexOf(current);
		String next = knownNames.get((idx + 1) % knownNames.size());
		setSelectedSummonName(stack, next);
		player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.35F, 0.8F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.select.success",
				Component.translatable("entity.hemomancy." + next)), true);
	}

	public static void callOrRecallSelectedSummon(ItemStack stack, ServerPlayer player) {
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
		int activeCap = PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(player));
		if (activeSummonsForOwner(player).size() >= activeCap) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.cap.reached", activeCap)
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (getThread(stack) < definition.threadSummonCost()) {
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
		if (!consumeThread(stack, definition.threadSummonCost())) {
			return;
		}
		Mob mob = mobOpt.get();
		player.level().addFreshEntity(mob);
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
							&& mob instanceof BoundPuppeteerSummon bound
							&& player.getUUID().equals(bound.hemomancy$getOwnerUUID())) {
						active.add(mob);
					}
				}
			}
			return active;
		}
		return player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(128.0),
				mob -> mob instanceof BoundPuppeteerSummon bound
						&& player.getUUID().equals(bound.hemomancy$getOwnerUUID()));
	}

	private static List<Mob> activeSummonsForCrossbar(Player player, UUID crossbarId, String summonName) {
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
}
