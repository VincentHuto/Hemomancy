package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;

public class PuppeteersSpindleBlock extends Block {
	public PuppeteersSpindleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide) {
			return ItemInteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof MarionetteCrossbarItem) {
			bindHeldCrossbar(stack, player);
			return ItemInteractionResult.SUCCESS;
		}
		if (stack.getItem() == ItemInit.puppeteering_thread.get()) {
			refillCrossbar(stack, player);
			return ItemInteractionResult.SUCCESS;
		}
		if (stack.getItem() == ItemInit.sanguine_quintessence.get() && player instanceof ServerPlayer serverPlayer) {
			unlockNextSummon(stack, serverPlayer);
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private static void bindHeldCrossbar(ItemStack crossbar, Player player) {
		List<String> known = HemoCapabilityAccess.getKnownSummons(player)
				.map(k -> k.getKnownSummonNames())
				.orElse(List.of());
		if (known.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.bind.none")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		String selected = MarionetteCrossbarItem.getSelectedSummonName(crossbar);
		if (selected == null || selected.isBlank() || !known.contains(selected)) {
			selected = known.get(0);
			MarionetteCrossbarItem.setSelectedSummonName(crossbar, selected);
		}
		MarionetteCrossbarItem.ensureCrossbarId(crossbar);
		if (player instanceof ServerPlayer serverPlayer) {
			HemoCapabilityAccess.getKnownSummons(serverPlayer)
					.ifPresent(knownSummons -> KnownSummonEvents.sync(serverPlayer, knownSummons));
		}
		player.playSound(SoundEvents.CHAIN_PLACE, 0.5F, 0.8F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.bind.success",
				Component.translatable("entity.hemomancy." + selected)), true);
	}

	private static void refillCrossbar(ItemStack threadStack, Player player) {
		Optional<ItemStack> crossbar = findNearbyCrossbar(player);
		if (crossbar.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.refill.no_crossbar")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		int before = MarionetteCrossbarItem.getThread(crossbar.get());
		if (before >= PuppeteerSummonRules.THREAD_CAPACITY) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.refill.full")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		int after = MarionetteCrossbarItem.addThread(crossbar.get(), PuppeteerSummonRules.THREAD_PER_ITEM);
		if (!player.getAbilities().instabuild) {
			threadStack.shrink(1);
		}
		player.playSound(SoundEvents.WOOL_PLACE, 0.45F, 0.75F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.refill.success",
				after, PuppeteerSummonRules.THREAD_CAPACITY), true);
	}

	private static void unlockNextSummon(ItemStack quintessence, ServerPlayer player) {
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

	private static Optional<ItemStack> findNearbyCrossbar(Player player) {
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
}
