package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SemiSentientConstructBlock;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * The Drudge Submission Device.
 * <p>
 * When right-clicked on a {@link SemiSentientConstructBlock SSC}
 * it finds every {@link DrudgeEntity} whose {@code homePos} matches that block
 * and teleports them back to the SSC's position.
 * <p>
 * Drudges that are rogue are also recalled — the device overrides the rogue
 * state so the owner can retrieve an out-of-control construct.
 */
public class DrudgeSubmissionDeviceItem extends Item {

    /** Search radius in blocks — large enough to retrieve wandering drudges. */
    private static final double SEARCH_RADIUS = 128.0;

    public DrudgeSubmissionDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click a Semi-Sentient Construct to recall all bound drudges.")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal("Even rogue constructs answer the call.")
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /**
     * Recalls all drudges bound to {@code sscPos} to that position.
     * Called by {@code SemiSentientConstructBlock.useItemOn}.
     *
     * @return the number of drudges that were recalled
     */
    public int recallDrudges(Level level, BlockPos sscPos, Player player) {
        if (level.isClientSide) return 0;

        AABB searchBox = new AABB(sscPos).inflate(SEARCH_RADIUS);
        List<DrudgeEntity> candidates = level.getEntitiesOfClass(DrudgeEntity.class, searchBox,
                d -> sscPos.equals(d.getHomePos()));

        int recalled = 0;
        for (DrudgeEntity drudge : candidates) {
            // Clear rogue state so the drudge can operate again after returning
            if (drudge.isRogue()) {
                drudge.setRogue(false);
            }
            drudge.teleportTo(
                    sscPos.getX() + 0.5,
                    sscPos.getY() + 1.0,
                    sscPos.getZ() + 0.5);
            recalled++;
        }

        if (recalled > 0) {
            level.playSound(null, sscPos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.5f, 0.6f);
            player.displayClientMessage(
                    Component.literal("§7" + recalled + " construct(s) recalled to the station."),
                    false);
        } else {
            player.displayClientMessage(
                    Component.literal("§8No constructs are bound to this station within range."),
                    false);
        }

        return recalled;
    }
}
