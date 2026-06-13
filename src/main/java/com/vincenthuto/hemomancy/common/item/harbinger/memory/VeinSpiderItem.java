package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.List;
import java.util.Optional;

public class VeinSpiderItem extends HematicMemoryToolItem {
    private static final String FROM_DIM = "VeinSpiderFromDim";
    private static final String FROM_POS = "VeinSpiderFromPos";
    private static final String TO_DIM = "VeinSpiderToDim";
    private static final String TO_POS = "VeinSpiderToPos";

    public VeinSpiderItem(Properties properties) {
        super(properties, 4);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if (!canUseHematicTool(context.getPlayer())) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos();
        if (level.getCapability(Capabilities.ItemHandler.BLOCK, pos, context.getClickedFace()) == null) {
            if (!level.isClientSide) {
                context.getPlayer().displayClientMessage(Component.translatable("item.hemomancy.vein_spider.no_inventory")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            VeinSpiderRules.EndpointAction action = VeinSpiderRules.endpointAction(
                    tag.contains(FROM_DIM), tag.contains(TO_DIM), context.getPlayer().isShiftKeyDown());
            long packed = VeinSpiderRules.packPosition(pos.getX(), pos.getY(), pos.getZ());
            String dim = level.dimension().location().toString();
            if (action == VeinSpiderRules.EndpointAction.STORE_FIRST) {
                storeEndpoint(stack, dim, packed, true);
                context.getPlayer().displayClientMessage(Component.translatable("item.hemomancy.vein_spider.first")
                        .withStyle(ChatFormatting.DARK_RED), true);
            } else {
                storeEndpoint(stack, dim, packed, false);
                Optional<VeinSpiderRules.Link> link = readLink(stack);
                if (link.isPresent() && !VeinSpiderRules.isValidLink(link.get())) {
                    clear(stack);
                    context.getPlayer().displayClientMessage(Component.translatable("item.hemomancy.vein_spider.invalid")
                            .withStyle(ChatFormatting.RED), true);
                } else {
                    context.getPlayer().displayClientMessage(Component.translatable("item.hemomancy.vein_spider.linked")
                            .withStyle(ChatFormatting.DARK_RED), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return readLink(stack).filter(VeinSpiderRules::isValidLink).isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(readLink(stack).isPresent()
                ? "item.hemomancy.vein_spider.tooltip.linked" : "item.hemomancy.vein_spider.tooltip.empty")
                .withStyle(ChatFormatting.DARK_RED));
    }

    static Optional<VeinSpiderRules.Link> readLink(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(FROM_DIM) || !tag.contains(TO_DIM)) {
            return Optional.empty();
        }
        return Optional.of(new VeinSpiderRules.Link(tag.getString(FROM_DIM), tag.getLong(FROM_POS),
                tag.getString(TO_DIM), tag.getLong(TO_POS)));
    }

    private static void storeEndpoint(ItemStack stack, String dimension, long packed, boolean first) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(first ? FROM_DIM : TO_DIM, dimension);
        tag.putLong(first ? FROM_POS : TO_POS, packed);
        if (first) {
            tag.remove(TO_DIM);
            tag.remove(TO_POS);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void clear(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(FROM_DIM);
        tag.remove(FROM_POS);
        tag.remove(TO_DIM);
        tag.remove(TO_POS);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
