package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingHelper;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.routing.DirectBloodLinkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HematicSutureNodeBlockEntity extends BlockEntity {

    public HematicSutureNodeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.hematic_suture_node.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HematicSutureNodeBlockEntity node) {
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HematicSutureNodeBlockEntity node) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getGameTime() % BloodRoutingHelper.ROUTE_INTERVAL_TICKS != 0) {
            return;
        }
        DirectBloodLinkData link = BloodRoutingSavedData.get(serverLevel).getLink(pos);
        if (link != null && BloodRoutingHelper.routeAdjacentTargets(serverLevel, pos, link) > 0) {
            node.setChanged();
        }
    }
}
