package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoDefinition;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class FieldNotesBookItem extends ItemGuideBook {
    public FieldNotesBookItem(Properties prop, ResourceLocation loc) {
        super(prop, loc);
    }

    private static ItemStack findInk(Player player) {
        if (MemoHelper.isInk(player.getOffhandItem())) {
            return player.getOffhandItem();
        }
        for (ItemStack stack : player.getInventory().items) {
            if (MemoHelper.isInk(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String pathTooltipKey(MemoDefinition.MemoPath path) {
        if (path == MemoDefinition.MemoPath.HARBINGER) {
            return "item.hemomancy.field_notes.tooltip.path_harbinger";
        }
        if (path == MemoDefinition.MemoPath.UNSTAINED) {
            return "item.hemomancy.field_notes.tooltip.path_unstained";
        }
        return "item.hemomancy.field_notes.tooltip.path_empty";
    }

    private static String refillMessageKey(MemoDefinition.MemoPath path) {
        if (path == MemoDefinition.MemoPath.UNSTAINED) {
            return "message.hemomancy.memo.field_notes_refilled_unstained";
        }
        return "message.hemomancy.memo.field_notes_refilled";
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && MemoHelper.getRemainingMemos(stack) < MemoHelper.MAX_FIELD_NOTES_MEMOS) {
            ItemStack ink = findInk(player);
            if (!ink.isEmpty()) {
                MemoHelper.RefillResult result = MemoHelper.refillFieldNotes(stack, ink);
                if (result == MemoHelper.RefillResult.WRONG_INK_PATH) {
                    player.displayClientMessage(Component.translatable("message.hemomancy.memo.wrong_refill_ink")
                            .withStyle(ChatFormatting.GRAY), true);
                    return InteractionResultHolder.success(stack);
                }
                if (!player.getAbilities().instabuild) {
                    ink.shrink(1);
                }
                player.displayClientMessage(Component.translatable(refillMessageKey(MemoHelper.getInkPath(stack)))
                        .withStyle(ChatFormatting.DARK_RED), true);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.memos",
                        MemoHelper.getMemoCount(stack), MemoHelper.getRemainingMemos(stack))
                .withStyle(ChatFormatting.GRAY));
        MemoDefinition.MemoPath path = MemoHelper.getInkPath(stack);
        tooltip.add(Component.translatable(pathTooltipKey(path))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return MemoHelper.getInkPath(stack) != null
                || MemoHelper.getRemainingMemos(stack) < MemoHelper.MAX_FIELD_NOTES_MEMOS;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * MemoHelper.getRemainingMemos(stack) / MemoHelper.MAX_FIELD_NOTES_MEMOS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        MemoDefinition.MemoPath path = MemoHelper.getInkPath(stack);
        if (path == MemoDefinition.MemoPath.UNSTAINED) {
            return 0xBFDDE3;
        }
        if (path == MemoDefinition.MemoPath.HARBINGER) {
            return 0x8B1A1A;
        }
        return 0x6B6258;
    }

}
