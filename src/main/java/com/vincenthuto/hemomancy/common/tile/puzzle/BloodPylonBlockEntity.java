package com.vincenthuto.hemomancy.common.tile.puzzle;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BloodPylonBlockEntity extends BlockEntity {

    private static final double DRAIN_RADIUS = 4.0;
    private static final float DRAIN_AMOUNT = 1.0f;

    private int tickCounter = 0;

    public BloodPylonBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.blood_pylon.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BloodPylonBlockEntity te) {
        te.tickCounter++;
        if (te.tickCounter % 20 != 0) return;

        AABB range = new AABB(pos).inflate(DRAIN_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, range);
        for (Player player : players) {
            if (player.getHealth() > 2.0f) {
                player.hurt(level.damageSources().magic(), DRAIN_AMOUNT);
            }
        }
    }
}
