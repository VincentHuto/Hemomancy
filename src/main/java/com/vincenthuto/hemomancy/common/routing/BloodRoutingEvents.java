package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.HematicSutureNeedleItem;
import com.vincenthuto.hemomancy.common.tile.functional.HematicSutureNodeBlockEntity;
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

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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
            BlockEntity be = level.getBlockEntity(entry.getKey());
            if (be instanceof HematicSutureNodeBlockEntity) {
                continue;
            }
            BloodRoutingHelper.routeLinkedTarget(level, entry.getKey(), entry.getValue());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BloodRoutingSavedData.get(level).removeLink(event.getPos());
        }
    }
}
