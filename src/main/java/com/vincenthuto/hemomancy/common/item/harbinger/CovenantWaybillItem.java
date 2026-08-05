package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/** A persistent post-initiation route to the nearest generated Harbinger Outpost. */
public class CovenantWaybillItem extends Item {
	private static final TagKey<Structure> TARGETS = TagKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("covenant_waybill_targets"));
	private static final int SEARCH_RADIUS_CHUNKS = 160;

	public CovenantWaybillItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.success(stack);
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(stack);

		BlockPos target = serverLevel.findNearestMapStructure(
				TARGETS, player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
		if (target == null) {
			player.displayClientMessage(Component.translatable("item.hemomancy.covenant_waybill.not_found")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
			return InteractionResultHolder.sidedSuccess(stack, false);
		}

		int dx = target.getX() - player.blockPosition().getX();
		int dz = target.getZ() - player.blockPosition().getZ();
		int distance = Mth.floor(Math.sqrt((double) dx * dx + (double) dz * dz));
		player.displayClientMessage(Component.translatable("item.hemomancy.covenant_waybill.points",
				directionName(dx, dz), distance, target.getX(), target.getZ())
				.withStyle(ChatFormatting.DARK_RED), false);
		return InteractionResultHolder.sidedSuccess(stack, false);
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

	static Component directionName(int dx, int dz) {
		if (dx == 0 && dz == 0) return Component.translatable("direction.hemomancy.here");
		String[] keys = {
				"direction.hemomancy.east", "direction.hemomancy.southeast",
				"direction.hemomancy.south", "direction.hemomancy.southwest",
				"direction.hemomancy.west", "direction.hemomancy.northwest",
				"direction.hemomancy.north", "direction.hemomancy.northeast"
		};
		int index = Mth.floor(Math.atan2(dz, dx) / (Math.PI / 4.0D) + 0.5D) & 7;
		return Component.translatable(keys[index]);
	}
}
