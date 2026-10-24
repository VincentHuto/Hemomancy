package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.arguments.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/** /hemo morphling stage: read or move the maturity of a held or equipped morphling. */
final class MorphlingSubcommand {
	private MorphlingSubcommand() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("morphling")
				.then(Commands.literal("stage")
						.then(Commands.literal("get")
								.executes(ctx -> getMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> getMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
						.then(Commands.literal("set")
								.then(Commands.argument("stage", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											for (int i = 0; i < MorphlingItem.MATURITY_NAMES.length; i++) {
												builder.suggest(String.valueOf(i));
												builder.suggest(MorphlingItem.MATURITY_NAMES[i].toLowerCase(Locale.ROOT));
											}
											return builder.buildFuture();
										})
										.executes(ctx -> setMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(),
												StringArgumentType.getString(ctx, "stage")))
										.then(Commands.argument("player", EntityArgument.player())
												.executes(ctx -> setMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
														StringArgumentType.getString(ctx, "stage"))))))
						.then(Commands.literal("next")
								.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), 1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 1))))
						.then(Commands.literal("previous")
								.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), -1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), -1))))
						.then(Commands.literal("prev")
								.executes(ctx -> cycleMorphlingStage(ctx.getSource(), ctx.getSource().getPlayerOrException(), -1))
								.then(Commands.argument("player", EntityArgument.player())
										.executes(ctx -> cycleMorphlingStage(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), -1)))));
	}

	private static int getMorphlingStage(CommandSourceStack source, ServerPlayer player) {
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		ItemStack stack = target.stack();
		int stage = MorphlingItem.getMaturityLevel(stack);
		source.sendSuccess(() -> Component.translatable("commands.hemomancy.morphling.stage",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				Component.translatable("commands.hemomancy.morphling.target_label", target.description()).withStyle(ChatFormatting.GRAY),
				stack.getHoverName().copy().withStyle(ChatFormatting.DARK_GREEN),
				Component.literal(stage + " - ").append(MorphlingItem.getMaturityName(stage))
						.withStyle(MorphlingItem.MATURITY_COLORS[stage])),
				false);
		return 1;
	}

	private static int setMorphlingStage(CommandSourceStack source, ServerPlayer player, String rawStage) {
		int stage = parseMorphlingStage(rawStage);
		if (stage < 0) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.morphling.unknown_morphling_stage_use_0_5",
					rawStage));
			return 0;
		}
		return setMorphlingStage(source, player, stage);
	}

	private static int setMorphlingStage(CommandSourceStack source, ServerPlayer player, int stage) {
		stage = Math.max(0, Math.min(stage, PrimalMorphlingRules.PRIMAL_LEVEL));
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		ItemStack original = target.stack().copy();
		ItemStack updated = original.copy();
		applyMorphlingStage(updated, stage);
		boolean sourceUpdated = false;
		if (target.source() == MorphlingCommandRules.Source.EQUIPPED) {
			var cap = HemoCapabilityAccess.getEquippedMorphling(player)
					;
			sourceUpdated = writeBackEquippedMorphlingSource(player, original, updated);
			cap.setEquippedMorphling(updated);
			LastRiteHelper.armForMorphling(player, updated);
			EquippedMorphlingEvents.syncToClient(player);
		} else {
			InteractionHand hand = target.source() == MorphlingCommandRules.Source.MAIN_HAND
					? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			player.setItemInHand(hand, updated);
			player.getInventory().setChanged();
			player.containerMenu.broadcastChanges();
		}
		int actualStage = MorphlingItem.getMaturityLevel(updated);
		boolean jarUpdated = sourceUpdated;
		source.sendSuccess(() -> HemoText.translatable("commands.hemomancy.morphling.set_to_stage",
				Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD),
				target.description(),
				Component.literal(actualStage + " - ").append(MorphlingItem.getMaturityName(actualStage))
						.withStyle(MorphlingItem.MATURITY_COLORS[actualStage]),
				target.source() != MorphlingCommandRules.Source.EQUIPPED
						? Component.empty()
						: jarUpdated
								? Component.translatable("commands.hemomancy.morphling.and_updated_its_jar_item").withStyle(ChatFormatting.GRAY)
								: Component.translatable("commands.hemomancy.morphling.no_matching_jar_item_was_found").withStyle(ChatFormatting.YELLOW)),
				true);
		return 1;
	}

	private static int cycleMorphlingStage(CommandSourceStack source, ServerPlayer player, int delta) {
		MorphlingCommandTarget target = getMorphlingCommandTarget(source, player);
		if (target == null) {
			return 0;
		}
		int current = MorphlingItem.getMaturityLevel(target.stack());
		int next = Math.floorMod(current + delta, PrimalMorphlingRules.PRIMAL_LEVEL + 1);
		return setMorphlingStage(source, player, next);
	}

	private static MorphlingCommandTarget getMorphlingCommandTarget(CommandSourceStack source, ServerPlayer player) {
		var morphCap = HemoCapabilityAccess.getEquippedMorphling(player);
		ItemStack equipped = morphCap.hasMorphling() && isMorphling(morphCap.getEquippedMorphling())
				? morphCap.getEquippedMorphling() : ItemStack.EMPTY;
		ItemStack mainHand = player.getMainHandItem();
		ItemStack offhand = player.getOffhandItem();
		MorphlingCommandRules.Source selected = MorphlingCommandRules.chooseSource(!equipped.isEmpty(),
				isMorphling(mainHand), isMorphling(offhand));
		MorphlingCommandTarget target = switch (selected) {
			case EQUIPPED -> new MorphlingCommandTarget(equipped, selected, Component.translatable("commands.hemomancy.morphling.target.equipped"));
			case MAIN_HAND -> new MorphlingCommandTarget(mainHand, selected, Component.translatable("commands.hemomancy.morphling.target.main_hand"));
			case OFF_HAND -> new MorphlingCommandTarget(offhand, selected, Component.translatable("commands.hemomancy.morphling.target.off_hand"));
			case NONE -> null;
		};
		if (target == null) {
			source.sendFailure(HemoText.translatable("commands.hemomancy.morphling.has_no_equipped_or_held_morphling",
					player.getName().getString()));
		}
		return target;
	}

	private static boolean isMorphling(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof MorphlingItem;
	}

	private static void applyMorphlingStage(ItemStack stack, int stage) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		int enzymeStage = Math.min(stage, PrimalMorphlingRules.APEX_LEVEL);
		tag.putFloat("EnzymePower", MorphlingItem.MATURITY_THRESHOLDS[enzymeStage]);
		tag.putInt("EnzymeFeedings", stage == 0 ? 0 : Math.max(tag.getInt("EnzymeFeedings"), 1));
		if (stage >= PrimalMorphlingRules.PRIMAL_LEVEL) {
			tag.putBoolean(MorphlingItem.PRIMALIZED_KEY, true);
		} else {
			tag.remove(MorphlingItem.PRIMALIZED_KEY);
		}
		tag.remove(MorphlingItem.WILD_BOUND_KEY);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		MorphlingItem.resetBondingProgress(stack, stage);
	}

	private record MorphlingCommandTarget(ItemStack stack, MorphlingCommandRules.Source source,
			Component description) {
	}

	private static boolean writeBackEquippedMorphlingSource(ServerPlayer player, ItemStack original, ItemStack updated) {
		boolean changed = false;
		ItemStack inventoryJar = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		if (!inventoryJar.isEmpty()) {
			changed |= updateMorphlingJarStack(inventoryJar, original, updated);
		}
		ItemStack scarJar = HemoCapabilityAccess.getEquipment(player).getStackInSlot(7);
		if (!(scarJar.getItem() instanceof ItemMorphlingJar)) {
			scarJar = ItemStack.EMPTY;
		}
		if (!scarJar.isEmpty() && scarJar != inventoryJar) {
			changed |= updateMorphlingJarStack(scarJar, original, updated);
		}
		if (changed) {
			player.getInventory().setChanged();
			player.containerMenu.broadcastChanges();
		}
		return changed;
	}

	private static boolean updateMorphlingJarStack(ItemStack jarStack, ItemStack original, ItemStack updated) {
		var rawHandler = jarStack.getCapability(Capabilities.ItemHandler.ITEM);
		if (!(rawHandler instanceof MorphlingJarItemHandler handler)) {
			return false;
		}
		handler.load();

		int fallbackSlot = -1;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack candidate = handler.getStackInSlot(i);
			if (candidate.isEmpty()) {
				continue;
			}
			if (ItemStack.isSameItemSameComponents(candidate, original)) {
				handler.setStackInSlot(i, updated.copy());
				handler.save();
				return true;
			}
			if (fallbackSlot == -1 && candidate.is(original.getItem())) {
				fallbackSlot = i;
			} else if (fallbackSlot >= 0 && candidate.is(original.getItem())) {
				fallbackSlot = -2;
			}
		}

		if (fallbackSlot >= 0) {
			handler.setStackInSlot(fallbackSlot, updated.copy());
			handler.save();
			return true;
		}
		return false;
	}

	private static int parseMorphlingStage(String rawStage) {
		String stage = rawStage.toLowerCase(Locale.ROOT);
		try {
			int parsed = Integer.parseInt(stage);
			return parsed >= 0 && parsed <= PrimalMorphlingRules.PRIMAL_LEVEL ? parsed : -1;
		} catch (NumberFormatException ignored) {
		}
		for (int i = 0; i < MorphlingItem.MATURITY_NAMES.length; i++) {
			if (MorphlingItem.MATURITY_NAMES[i].equalsIgnoreCase(stage)) {
				return i;
			}
		}
		return switch (stage) {
			case "develop", "dev" -> 2;
			case "max" -> PrimalMorphlingRules.PRIMAL_LEVEL;
			default -> -1;
		};
	}
}
