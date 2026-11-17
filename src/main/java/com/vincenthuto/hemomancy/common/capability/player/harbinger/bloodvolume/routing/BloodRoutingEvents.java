package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.HematicSutureNeedleItem;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HematicSutureNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodRoutingEvents {

    private BloodRoutingEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HematicSutureNeedleItem needle)) {
            return;
        }

        InteractionResult result = needle.bindAt(event.getLevel(), event.getPos(), player);
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % BloodRoutingHelper.ROUTE_INTERVAL_TICKS != 0) {
            return;
        }

        BloodRoutingSavedData data = BloodRoutingSavedData.get(level);
        for (Map.Entry<BlockPos, DirectBloodLinkData> entry : data.entries()) {
            // An unloaded link stays unloaded: getBlockEntity would pull its chunk in twice a second (RT-2).
            if (!level.isLoaded(entry.getKey())) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(entry.getKey());
            if (be instanceof HematicSutureNodeBlockEntity) {
                continue;
            }
            keepLinkStatus(level, entry.getKey(), entry.getValue());
            BloodRoutingHelper.routeLinkedTarget(level, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Keeps the tank's copy of its suture's state, resending the block only when it changed, so the machine's own
     * screen can say why it stalled. A filler's tank is its main block's. Returns whether the state changed.
     */
    public static boolean keepLinkStatus(ServerLevel level, BlockPos pos, DirectBloodLinkData link) {
        BlockEntity be = level.getBlockEntity(pos);
        BlockEntity tank = be == null ? null : ReservoirBlood.tank(be);
        if (!(tank instanceof IBloodReservoir)) {
            return false;
        }
        if (!ReservoirBlood.settled(tank).setLink(BloodRoutingHelper.linkStatus(level, pos, link))) {
            return false;
        }
        BlockEntitySync.markAndSync(tank);
        return true;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // A permission check leaves the block standing, so its links stay.
        if (event.getLevel() instanceof ServerLevel level
                && !com.vincenthuto.hemomancy.common.util.PlayerBlockEdits.isCheckingBreak()) {
            BloodRoutingSavedData.get(level).removeLink(event.getPos());
        }
    }
}
