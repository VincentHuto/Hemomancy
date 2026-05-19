package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.SporiticThuribleItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.Optional;

public class SporiticThuribleItem extends Item implements HemoClientItemExtensionsProvider {
	public static final String TAG_LIT = "Lit";
	public static final String TAG_SPORE_ID = "SporeId";
	public static final String TAG_BURN_TICKS = "BurnTicks";
	public static final String TAG_MAX_BURN_TICKS = "MaxBurnTicks";
	public static final String TAG_BURN_END_GAME_TIME = "BurnEndGameTime";

	public SporiticThuribleItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		Optional<SporiticThuribleSpore> activeSpore = getStoredSpore(stack);
		if (isLit(stack) && activeSpore.isPresent()) {
			tooltip.add(Component.translatable("item.hemomancy.sporitic_thurible.tooltip.lit",
							Component.translatable("item.hemomancy." + activeSpore.orElseThrow().itemPath()))
					.withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.translatable("item.hemomancy.sporitic_thurible.tooltip.burn_time",
							formatBurnTime(getBurnTicks(stack)))
					.withStyle(ChatFormatting.DARK_GRAY));
		} else {
			tooltip.add(Component.translatable("item.hemomancy.sporitic_thurible.tooltip.unlit")
					.withStyle(ChatFormatting.GRAY));
		}
		tooltip.add(Component.translatable("item.hemomancy.sporitic_thurible.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != InteractionHand.OFF_HAND) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.offhand")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
			}
			return InteractionResultHolder.fail(stack);
		}
		if (level.isClientSide) {
			return InteractionResultHolder.success(stack);
		}

		if (isLit(stack)) {
			extinguish(stack);
			level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.75f, 0.75f);
			player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.extinguish")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.success(stack);
		}

		if (!canUseBlood(player)) {
			player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.inactive_blood")
					.withStyle(ChatFormatting.RED), true);
			return InteractionResultHolder.fail(stack);
		}
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (degree < SporiticThuribleRules.REQUIRED_DEGREE) {
			player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.degree",
							SporiticThuribleRules.REQUIRED_DEGREE)
					.withStyle(ChatFormatting.RED), true);
			return InteractionResultHolder.fail(stack);
		}

		ItemStack mainHand = player.getMainHandItem();
		Optional<SporiticThuribleSpore> catalyst = SporiticThuribleSpore.byItemStack(mainHand);
		if (catalyst.isEmpty()) {
			player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.catalyst")
					.withStyle(ChatFormatting.RED), true);
			return InteractionResultHolder.fail(stack);
		}

		if (!player.getAbilities().instabuild) {
			mainHand.shrink(1);
		}
		light(stack, catalyst.orElseThrow(), level.getGameTime());
		level.playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8f, 0.65f);
		player.displayClientMessage(Component.translatable("item.hemomancy.sporitic_thurible.message.light",
						Component.translatable("item.hemomancy." + catalyst.orElseThrow().itemPath()))
				.withStyle(ChatFormatting.DARK_RED), true);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropSporiticThurible.INSTANCE;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return isLit(stack) && getMaxBurnTicks(stack) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0f * (float) SporiticThuribleRules.burnFraction(getBurnTicks(stack), getMaxBurnTicks(stack)));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return getStoredSpore(stack)
				.map(SporiticThuribleSpore::color)
				.orElse(0x8A2BE2);
	}

	public static boolean isLit(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_LIT);
	}

	public static void light(ItemStack stack, SporiticThuribleSpore spore) {
		light(stack, spore, 0L);
	}

	public static void light(ItemStack stack, SporiticThuribleSpore spore, long currentGameTime) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(TAG_LIT, true);
		tag.putString(TAG_SPORE_ID, spore.itemId().toString());
		tag.putInt(TAG_BURN_TICKS, SporiticThuribleRules.DEFAULT_BURN_TICKS);
		tag.putInt(TAG_MAX_BURN_TICKS, SporiticThuribleRules.DEFAULT_BURN_TICKS);
		tag.putLong(TAG_BURN_END_GAME_TIME, currentGameTime + SporiticThuribleRules.DEFAULT_BURN_TICKS);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void extinguish(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(TAG_LIT, false);
		tag.remove(TAG_SPORE_ID);
		tag.remove(TAG_BURN_TICKS);
		tag.remove(TAG_MAX_BURN_TICKS);
		tag.remove(TAG_BURN_END_GAME_TIME);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean tickBurn(ItemStack stack) {
		return tickBurn(stack, currentGameTime());
	}

	public static boolean tickBurn(ItemStack stack, long currentGameTime) {
		if (!isLit(stack)) {
			return false;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		int maxBurnTicks = tag.getInt(TAG_MAX_BURN_TICKS);
		if (maxBurnTicks <= 0) {
			extinguish(stack);
			return false;
		}
		if (!tag.contains(TAG_BURN_END_GAME_TIME)) {
			int legacyBurnTicks = Math.max(0, tag.getInt(TAG_BURN_TICKS));
			tag.putLong(TAG_BURN_END_GAME_TIME, currentGameTime + legacyBurnTicks);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
		if (getBurnTicks(stack, currentGameTime) <= 0) {
			extinguish(stack);
			return false;
		}
		return true;
	}

	public static int getBurnTicks(ItemStack stack) {
		return getBurnTicks(stack, currentGameTime());
	}

	public static int getBurnTicks(ItemStack stack, long currentGameTime) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (tag.contains(TAG_BURN_END_GAME_TIME)) {
			return SporiticThuribleRules.remainingBurnTicks(currentGameTime, tag.getLong(TAG_BURN_END_GAME_TIME));
		}
		return tag.getInt(TAG_BURN_TICKS);
	}

	public static int getMaxBurnTicks(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_MAX_BURN_TICKS);
	}

	public static Optional<SporiticThuribleSpore> getStoredSpore(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains(TAG_SPORE_ID)) {
			return Optional.empty();
		}
		return SporiticThuribleSpore.byStoredId(tag.getString(TAG_SPORE_ID));
	}

	private static boolean canUseBlood(Player player) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.map(IBloodVolume::isActive)
				.orElse(false);
	}

	private static String formatBurnTime(int burnTicks) {
		int totalSeconds = Math.max(0, (int) Math.ceil(burnTicks / 20.0));
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
	}

	private static long currentGameTime() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level != null ? minecraft.level.getGameTime() : 0L;
	}
}

class RenderPropSporiticThurible implements IClientItemExtensions {
	public static final RenderPropSporiticThurible INSTANCE = new RenderPropSporiticThurible();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new SporiticThuribleItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
