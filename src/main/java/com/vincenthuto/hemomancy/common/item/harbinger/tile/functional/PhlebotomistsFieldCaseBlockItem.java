package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.component.FieldCaseContents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.Block;
import java.util.List;

public class PhlebotomistsFieldCaseBlockItem extends BlockItem {
    public PhlebotomistsFieldCaseBlockItem(Block block, Properties properties) { super(block, properties.stacksTo(1)); }
    @Override public InteractionResult place(BlockPlaceContext context) {
        var stack = context.getItemInHand();
        var contents = stack.getOrDefault(DataComponentInit.FIELD_CASE_CONTENTS.get(), FieldCaseContents.EMPTY);
        if (!contents.valid() || stack.getCount() != 1) return InteractionResult.FAIL;
        boolean creativeFilled = context.getPlayer() != null && context.getPlayer().getAbilities().instabuild && contents.totalCount() > 0;
        var result = super.place(context);
        if (result.consumesAction() && creativeFilled) stack.shrink(1);
        return result;
    }
    @Override public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var contents = stack.getOrDefault(DataComponentInit.FIELD_CASE_CONTENTS.get(), FieldCaseContents.EMPTY);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild || contents.totalCount() == 0)
            return InteractionResult.PASS;
        // Run NeoForge's placement snapshots/events before creative game-mode count restoration.
        var result = stack.useOn(context);
        if (result.consumesAction() && stack.isEmpty()) context.getPlayer().setItemInHand(context.getHand(), ItemStack.EMPTY);
        return result;
    }
    @Override public boolean canFitInsideContainerItems() { return false; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var contents = stack.getOrDefault(DataComponentInit.FIELD_CASE_CONTENTS.get(), FieldCaseContents.EMPTY);
        tooltip.add(Component.translatable("tooltip.hemomancy.field_case.capacity", contents.totalCount()));
        tooltip.add(Component.translatable("tooltip.hemomancy.field_case.place"));
        tooltip.add(Component.translatable("tooltip.hemomancy.field_case.pickup"));
        if (!contents.valid()) tooltip.add(Component.translatable("tooltip.hemomancy.field_case.invalid").withStyle(net.minecraft.ChatFormatting.RED));
    }
}
