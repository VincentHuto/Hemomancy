package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/** A post-initiation lodestone that responds when aimed toward a Harbinger Outpost. */
public class CovenantWaybillItem extends Item {
	private static final TagKey<Structure> TARGETS = TagKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("covenant_waybill_targets"));
	private static final int SEARCH_RADIUS_CHUNKS = 160;
	private static final String TARGET_DIMENSION = "LodestoneTargetDimension";
	private static final String TARGET_X = "LodestoneTargetX";
	private static final String TARGET_Z = "LodestoneTargetZ";
	private static final String CHECKED_DIMENSION = "LodestoneCheckedDimension";

	public CovenantWaybillItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.success(stack);
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(stack);
		attune(stack, serverLevel, player, true);
		return InteractionResultHolder.sidedSuccess(stack, false);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) return;
		if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) return;
		if (target(stack, level) == null && !wasCheckedInCurrentDimension(stack, level)) {
			attune(stack, serverLevel, player, false);
		}
	}

	private static void attune(ItemStack stack, ServerLevel level, Player player, boolean showFeedback) {
		BlockPos target = level.findNearestMapStructure(
				TARGETS, player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(CHECKED_DIMENSION, level.dimension().location().toString());
		if (target == null) {
			tag.remove(TARGET_DIMENSION);
			tag.remove(TARGET_X);
			tag.remove(TARGET_Z);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			if (showFeedback) {
				player.displayClientMessage(Component.translatable("item.hemomancy.covenant_waybill.not_found")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
			}
			return;
		}

		tag.putString(TARGET_DIMENSION, level.dimension().location().toString());
		tag.putInt(TARGET_X, target.getX());
		tag.putInt(TARGET_Z, target.getZ());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		if (showFeedback) {
			player.displayClientMessage(Component.translatable("item.hemomancy.covenant_waybill.attuned")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	public static BlockPos target(ItemStack stack, Level level) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!level.dimension().location().toString().equals(tag.getString(TARGET_DIMENSION))
				|| !tag.contains(TARGET_X) || !tag.contains(TARGET_Z)) return null;
		return new BlockPos(tag.getInt(TARGET_X), 0, tag.getInt(TARGET_Z));
	}

	private static boolean wasCheckedInCurrentDimension(ItemStack stack, Level level) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return level.dimension().location().toString().equals(tag.getString(CHECKED_DIMENSION));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.covenant_waybill.tooltip")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.hemomancy.covenant_waybill.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

}
