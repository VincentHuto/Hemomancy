package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.client.render.world.MnemonicBlueprintRenderer;
import com.vincenthuto.hemomancy.client.screen.item.BloodStructureHintScreen;
import com.vincenthuto.hemomancy.client.screen.item.RiteHintScreen;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class MnemonicBlueprintItem extends Item {
	public MnemonicBlueprintItem(Properties properties) {
		super(properties.stacksTo(MnemonicBlueprintStacking.maxStackSize(false)).rarity(Rarity.UNCOMMON));
	}

	public static ItemStack create(Item item, MnemonicBlueprintTarget target) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponentInit.MNEMONIC_BLUEPRINT_TARGET.get(), target);
		stack.set(DataComponents.CUSTOM_MODEL_DATA,
				new CustomModelData(MnemonicBlueprintAppearance.customModelData(target.type())));
		stack.set(DataComponents.MAX_STACK_SIZE, MnemonicBlueprintStacking.maxStackSize(true));
		return stack;
	}

	@Nullable
	public static MnemonicBlueprintTarget getTarget(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		return stack.get(DataComponentInit.MNEMONIC_BLUEPRINT_TARGET.get());
	}

	public static boolean isBlank(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof MnemonicBlueprintItem
				&& getTarget(stack) == null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			if (level.isClientSide) clearProjection(player);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		MnemonicBlueprintTarget target = getTarget(stack);
		if (level.isClientSide) {
			if (target == null) {
				player.displayClientMessage(Component.translatable("item.hemomancy.mnemonic_blueprint.blank_hint")
						.withStyle(ChatFormatting.GRAY), true);
			} else {
				openDetails(target);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) return InteractionResult.PASS;
		if (player.isShiftKeyDown()) {
			if (context.getLevel().isClientSide) clearProjection(player);
			return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
		}
		if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
		MnemonicBlueprintTarget target = getTarget(context.getItemInHand());
		if (target == null) {
			if (context.getLevel().isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.mnemonic_blueprint.blank_hint")
						.withStyle(ChatFormatting.GRAY), true);
			}
			return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
		}
		if (context.getLevel().isClientSide) {
			anchorProjection(target, context.getClickedPos().above(), player.getDirection());
		}
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		MnemonicBlueprintTarget target = getTarget(stack);
		if (target == null) {
			tooltip.add(Component.translatable("item.hemomancy.mnemonic_blueprint.tooltip.blank")
					.withStyle(ChatFormatting.GRAY));
			return;
		}
		String name = displayName(target.recipeId());
		String key = target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE
				? "item.hemomancy.mnemonic_blueprint.tooltip.rite"
				: "item.hemomancy.mnemonic_blueprint.tooltip.structure";
		tooltip.add(Component.translatable(key, Component.literal(name).withStyle(ChatFormatting.DARK_RED))
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.hemomancy.mnemonic_blueprint.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.hemomancy.mnemonic_blueprint.tooltip.place")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.hemomancy.mnemonic_blueprint.tooltip.clear")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return getTarget(stack) != null;
	}

	@Override
	public Component getName(ItemStack stack) {
		MnemonicBlueprintTarget target = getTarget(stack);
		return target == null ? super.getName(stack)
				: Component.translatable("item.hemomancy.mnemonic_blueprint.named", displayName(target.recipeId()));
	}

	private static String displayName(ResourceLocation id) {
		String path = id.getPath();
		if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
		return com.vincenthuto.hutoslib.client.HLTextUtils.toProperCase(path.replace('_', ' '));
	}

	@OnlyIn(Dist.CLIENT)
	private static void openDetails(MnemonicBlueprintTarget target) {
		if (target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE) RiteHintScreen.open(target.recipeId());
		else BloodStructureHintScreen.open(target.recipeId());
	}

	@OnlyIn(Dist.CLIENT)
	private static void anchorProjection(MnemonicBlueprintTarget target, net.minecraft.core.BlockPos center,
			Direction facing) {
		MnemonicBlueprintRenderer.anchor(target, center, facing);
	}

	@OnlyIn(Dist.CLIENT)
	private static void clearProjection(Player player) {
		boolean cleared = MnemonicBlueprintRenderer.clear();
		if (cleared) player.displayClientMessage(
				Component.translatable("item.hemomancy.mnemonic_blueprint.cleared").withStyle(ChatFormatting.GRAY), true);
	}
}
