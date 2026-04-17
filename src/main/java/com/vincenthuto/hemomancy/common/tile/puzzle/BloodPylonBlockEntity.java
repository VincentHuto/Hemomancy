package com.vincenthuto.hemomancy.common.tile.puzzle;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BloodPylonBlockEntity extends BlockEntity {

    private static final double DRAIN_RADIUS = 4.0;
    private static final double DRAIN_AMOUNT = 50.0;

    private int tickCounter = 0;

    public BloodPylonBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.blood_pylon.get(), pos, state);
    }

    private static final int ALTAR_SCAN_RADIUS = 15;

    public static void serverTick(Level level, BlockPos pos, BlockState state, BloodPylonBlockEntity te) {
        te.tickCounter++;
        if (te.tickCounter % 20 != 0) return;

        // Only drain while a nearby trial altar is active
        if (!isTrialActive(level, pos)) return;

        AABB range = new AABB(pos).inflate(DRAIN_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);
        for (Player player : players) {
            player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
                if (!volume.isEmpty() && volume.drain(DRAIN_AMOUNT)) {
                    if (player instanceof ServerPlayer sp) {
                        sp.displayClientMessage(
                            Component.translatable("message.hemomancy.blood_pylon.draining")
                                .withStyle(ChatFormatting.DARK_RED),
                            true);
                    }
                }
            });
        }
    }

    private static boolean isTrialActive(Level level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-ALTAR_SCAN_RADIUS, -ALTAR_SCAN_RADIUS, -ALTAR_SCAN_RADIUS),
                center.offset(ALTAR_SCAN_RADIUS, ALTAR_SCAN_RADIUS, ALTAR_SCAN_RADIUS))) {
            if (level.getBlockEntity(p) instanceof BloodTrialAltarBlockEntity altar && altar.isActive()) {
                return true;
            }
        }
        return false;
    }
}
