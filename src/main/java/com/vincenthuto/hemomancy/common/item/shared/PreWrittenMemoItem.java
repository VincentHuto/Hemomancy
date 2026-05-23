package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * A pre-written field-note item found in the world (e.g. dropped by a dying
 * NPC). On right-click it attempts to transfer one or more fixed memos into
 * the player's pending field notes. The item is consumed once at least one memo
 * is successfully recorded (or was already known).
 *
 * <p>Use {@link #create(Item, ResourceLocation...)} to build a configured stack.</p>
 */
public class PreWrittenMemoItem extends Item {

    private static final String TAG_MEMO_IDS = "MemoIds";
    private static final int TAG_STRING = 8;

    public PreWrittenMemoItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    /**
     * Creates a stack pre-configured with the given memo IDs stored in NBT.
     * Multiple IDs can be supplied so that each player path (Harbinger /
     * Unstained) may capture a path-appropriate version of the memo.
     */
    public static ItemStack create(Item item, ResourceLocation... memoIds) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = new ListTag();
        for (ResourceLocation id : memoIds) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put(TAG_MEMO_IDS, list);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /** Returns all memo IDs encoded in the stack. */
    public static List<ResourceLocation> getMemoIds(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = tag.getList(TAG_MEMO_IDS, TAG_STRING);
        List<ResourceLocation> ids = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
            if (rl != null) ids.add(rl);
        }
        return ids;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        List<ResourceLocation> ids = getMemoIds(stack);
        if (ids.isEmpty()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.hemomancy.memo.invalid").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        boolean consumed = false;
        for (ResourceLocation memoId : ids) {
            MemoHelper.CaptureResult result = MemoHelper.captureMemo(serverPlayer, memoId);
            if (result == MemoHelper.CaptureResult.CAPTURED
                    || result == MemoHelper.CaptureResult.ALREADY_NOTED
                    || result == MemoHelper.CaptureResult.ALREADY_KNOWN) {
                consumed = true;
                break;
            }
        }

        if (consumed && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
