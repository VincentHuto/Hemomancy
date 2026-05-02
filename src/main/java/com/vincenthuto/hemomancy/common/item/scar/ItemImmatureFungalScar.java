package com.vincenthuto.hemomancy.common.item.scar;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/**
 * A single item representing any immature fungal scar culture partway through
 * maturation in the {@code MycelialCrucible}. All per-variant identity is
 * carried in custom data so one registry entry covers every scar type.
 *
 * <p>Custom-data keys:
 * <ul>
 *   <li>{@value #TAG_TENDENCY}         — tendency name string</li>
 *   <li>{@value #TAG_MATURE_THRESHOLD} — enzyme-power units required</li>
 *   <li>{@value #TAG_MATURE_PROGRESS}  — enzyme-power units accumulated</li>
 *   <li>{@value #TAG_TARGET_SCAR_ID}   — registry ID of the finished scar</li>
 * </ul>
 *
 * <p>When {@code MatureProgress >= MatureThreshold} AND the stack is placed in
 * the crucible with sufficient enzymes, the block entity converts it to the
 * finished {@link ItemFungalScar}.
 */
public class ItemImmatureFungalScar extends Item {

    public static final String TAG_MATURE_PROGRESS  = "MatureProgress";
    public static final String TAG_MATURE_THRESHOLD = "MatureThreshold";
    public static final String TAG_TENDENCY          = "Tendency";
    public static final String TAG_TARGET_SCAR_ID    = "TargetScarId";

    public ItemImmatureFungalScar(Properties properties) {
        super(properties);
    }

    // ── Tendency ──────────────────────────────────────────────────────────────

    /** Reads the stored tendency from the stack's custom data, or {@code null} if unset. */
    public static EnumBloodTendency getTendency(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String name = tag.getString(TAG_TENDENCY);
        if (name.isEmpty()) return null;
        try {
            return EnumBloodTendency.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ── Progress helpers ──────────────────────────────────────────────────────

    public int getMatureProgress(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt(TAG_MATURE_PROGRESS);
    }

    public int getMatureThreshold(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getInt(TAG_MATURE_THRESHOLD);
    }

    public void setMatureProgress(ItemStack stack, int progress) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(TAG_MATURE_PROGRESS, progress);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Stamps all variant metadata onto a fresh immature-scar stack produced at
     * Phase 1 end.
     *
     * @param stack         the stack to initialise
     * @param tendency      the scar's tendency alignment
     * @param threshold     total enzyme-power required for maturation
     * @param targetScarId  registry ID of the finished {@link ItemFungalScar} this will become
     */
    public void initThreshold(ItemStack stack, EnumBloodTendency tendency, int threshold,
            ResourceLocation targetScarId) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(TAG_TENDENCY, tendency.name());
        tag.putInt(TAG_MATURE_THRESHOLD, threshold);
        tag.putString(TAG_TARGET_SCAR_ID, targetScarId.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public boolean isMature(ItemStack stack) {
        int threshold = getMatureThreshold(stack);
        return threshold > 0 && getMatureProgress(stack) >= threshold;
    }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TAG_TARGET_SCAR_ID)) {
            String path = ResourceLocation.parse(tag.getString(TAG_TARGET_SCAR_ID)).getPath();
            return Component.translatable("item.hemomancy.immature_scar." + path);
        }
        return super.getName(stack);
    }

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
        EnumBloodTendency tendency = getTendency(stack);
        if (tendency != null) {
            tooltip.add(Component.literal("Tendency: " + tendency.name())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (threshold > 0) {
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
}
