package com.vincenthuto.hemomancy.common.item.scar;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/**
 * An immature fungal scar culture partway through maturation in the
 * {@code MycelialCrucible}. Carries {@code MatureProgress} and
 * {@code MatureThreshold} in its custom data so progress persists across
 * sessions.
 *
 * <p>When {@code MatureProgress >= MatureThreshold} AND the stack is placed in
 * the crucible with sufficient enzymes, the block entity converts it to the
 * finished {@link ItemFungalScar}.
 */
public class ItemImmatureFungalScar extends Item {

    public static final String TAG_MATURE_PROGRESS  = "MatureProgress";
    public static final String TAG_MATURE_THRESHOLD = "MatureThreshold";
    public static final String TAG_TENDENCY          = "Tendency";

    private final EnumBloodTendency tendency;
    /** Default maturation threshold (enzyme-power units). */
    private final int defaultThreshold;

    public ItemImmatureFungalScar(Properties properties, EnumBloodTendency tendency, int defaultThreshold) {
        super(properties);
        this.tendency = tendency;
        this.defaultThreshold = defaultThreshold;
    }

    public EnumBloodTendency getTendency() {
        return tendency;
    }

    public int getDefaultThreshold() {
        return defaultThreshold;
    }

    // ── Progress helpers ──────────────────────────────────────────────────────

    public int getMatureProgress(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt(TAG_MATURE_PROGRESS);
    }

    public int getMatureThreshold(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int stored = tag.getInt(TAG_MATURE_THRESHOLD);
        return stored > 0 ? stored : defaultThreshold;
    }

    public void setMatureProgress(ItemStack stack, int progress) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(TAG_MATURE_PROGRESS, progress);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Initialises threshold from the default if not already set. */
    public void initThreshold(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(TAG_MATURE_THRESHOLD)) {
            tag.putInt(TAG_MATURE_THRESHOLD, defaultThreshold);
        }
        tag.putString(TAG_TENDENCY, tendency.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean isMature(ItemStack stack) {
        return getMatureProgress(stack) >= getMatureThreshold(stack);
    }

    // ── Display ───────────────────────────────────────────────────────────────

//    @Override
//    public Rarity getRarity(ItemStack stack) {
//        return Rarity.UNCOMMON;
//    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isMature(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int progress  = getMatureProgress(stack);
        int threshold = getMatureThreshold(stack);
        tooltip.add(Component.literal("Tendency: " + tendency.name())
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Maturation: " + progress + " / " + threshold)
                .withStyle(progress >= threshold ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        if (progress < threshold) {
            tooltip.add(Component.literal("Feed aligned enzymes in the Mycelial Crucible to mature.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("Ready to harvest — return to the Crucible.")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
        }
    }
}
