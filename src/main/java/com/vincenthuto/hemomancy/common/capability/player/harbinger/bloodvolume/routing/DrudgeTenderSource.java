package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.tile.functional.HematicSutureNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class DrudgeTenderSource {

    private DrudgeTenderSource() {
    }

    public static double tendNearbyRoutes(ServerLevel level, BlockPos home, @Nullable UUID owner, double range) {
        BloodRoutingSavedData routes = BloodRoutingSavedData.get(level);
        for (Map.Entry<BlockPos, DirectBloodLinkData> entry : routes.entries()) {
            DirectBloodLinkData link = entry.getValue();
            if (owner != null && !owner.equals(link.owner())) {
                continue;
            }
            if (!isWithinRange(home, entry.getKey(), range)) {
                continue;
            }

            if (level.getBlockEntity(entry.getKey()) instanceof HematicSutureNodeBlockEntity) {
                double routed = BloodRoutingHelper.routeAdjacentTargets(level, entry.getKey(), link);
                if (routed > 0) {
                    return routed;
                }
            } else {
                double routed = BloodRoutingHelper.routeLinkedTarget(level, entry.getKey(), link);
                if (routed > 0) {
                    return routed;
                }
            }
        }
        return 0;
    }

    private static boolean isWithinRange(BlockPos home, BlockPos target, double range) {
        double dx = home.getX() - target.getX();
        double dy = home.getY() - target.getY();
        double dz = home.getZ() - target.getZ();
        return dx * dx + dy * dy + dz * dz <= range * range;
    }
}
