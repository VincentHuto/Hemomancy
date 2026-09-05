package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.manips.RadialChooseVeinScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HematicMemoryToolItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class TerrestrialSpeculumItem extends HematicMemoryToolItem {
	private static final String ORIGIN_DIMENSION = Hemomancy.MOD_ID + ":speculum_origin_dimension";
	private static final String ORIGIN_POSITION = Hemomancy.MOD_ID + ":speculum_origin_position";
	public static final int LIFETIME_TICKS = 600;

	public TerrestrialSpeculumItem(Properties properties) {
		super(properties, TerrestrialSpeculumRules.REQUIRED_DEGREE);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (context.getPlayer() == null || !canUseHematicTool(context.getPlayer())) return InteractionResult.FAIL;
		BlockPos origin = context.getClickedPos().relative(context.getClickedFace());
		boolean hasVeins = HemoCapabilityAccess.getKnownManipulations(context.getPlayer())
				.map(known -> !known.getVeinList().isEmpty()).orElse(false);
		if (!TerrestrialSpeculumRules.canManifest(HemoCapabilityAccess.getPlayerDegreeNumber(context.getPlayer()),
				canUseHematicToolSilently(context.getPlayer()), hasVeins ? 1 : 0,
				level.getBlockState(origin).canBeReplaced())) {
			if (!level.isClientSide && !hasVeins) {
				context.getPlayer().displayClientMessage(
						Component.translatable("item.hemomancy.terrestrial_speculum.no_veins")
								.withStyle(ChatFormatting.DARK_RED), true);
			}
			return InteractionResult.FAIL;
		}

		if (level.isClientSide) {
			HemoCapabilityAccess.getKnownManipulations(context.getPlayer())
					.ifPresent(known -> RadialChooseVeinScreen.openScreen(known, origin));
			return InteractionResult.SUCCESS;
		}

		ServerPlayer player = (ServerPlayer) context.getPlayer();
		removePreviousOrigin(player);
		BlockState state = BlockInit.earthen_vein.get().defaultBlockState()
				.setValue(EarthenVeinBlock.FACING, context.getHorizontalDirection().getOpposite())
				.setValue(EarthenVeinBlock.WATERLOGGED, level.getFluidState(origin).is(Fluids.WATER));
		if (!level.setBlock(origin, state, 3)
				|| !(level.getBlockEntity(origin) instanceof EarthenVeinBlockEntity vein)) {
			return InteractionResult.FAIL;
		}
		vein.makeTemporary(player.getUUID(), level.getGameTime() + LIFETIME_TICKS);
		rememberOrigin(player, origin);
		return InteractionResult.CONSUME;
	}

	public static boolean dismissTemporaryOrigin(ServerPlayer player, BlockPos origin) {
		if (!(player.level().getBlockEntity(origin) instanceof EarthenVeinBlockEntity vein)
				|| !vein.isTemporaryOwnedBy(player.getUUID())) return false;
		player.level().removeBlock(origin, false);
		clearRememberedOrigin(player, player.level().dimension(), origin);
		return true;
	}

	private static void removePreviousOrigin(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		if (!data.contains(ORIGIN_DIMENSION) || !data.contains(ORIGIN_POSITION)) return;
		ResourceLocation dimension = ResourceLocation.tryParse(data.getString(ORIGIN_DIMENSION));
		if (dimension != null) {
			ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
			BlockPos pos = BlockPos.of(data.getLong(ORIGIN_POSITION));
			if (level != null) {
				level.getChunkAt(pos);
				if (level.getBlockEntity(pos) instanceof EarthenVeinBlockEntity vein
						&& vein.isTemporaryOwnedBy(player.getUUID())) level.removeBlock(pos, false);
			}
		}
		data.remove(ORIGIN_DIMENSION);
		data.remove(ORIGIN_POSITION);
	}

	private static void rememberOrigin(ServerPlayer player, BlockPos origin) {
		player.getPersistentData().putString(ORIGIN_DIMENSION, player.level().dimension().location().toString());
		player.getPersistentData().putLong(ORIGIN_POSITION, origin.asLong());
	}

	private static void clearRememberedOrigin(ServerPlayer player, ResourceKey<Level> dimension, BlockPos origin) {
		CompoundTag data = player.getPersistentData();
		if (dimension.location().toString().equals(data.getString(ORIGIN_DIMENSION))
				&& origin.asLong() == data.getLong(ORIGIN_POSITION)) {
			data.remove(ORIGIN_DIMENSION);
			data.remove(ORIGIN_POSITION);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.terrestrial_speculum.tooltip")
				.withStyle(ChatFormatting.GRAY));
	}
}
