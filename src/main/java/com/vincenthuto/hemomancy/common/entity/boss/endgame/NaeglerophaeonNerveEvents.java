package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Fresh cuts are checked on the boss's next tick, after the block actually becomes air. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class NaeglerophaeonNerveEvents {
    private static final double NOTICE_RADIUS=96;

    private NaeglerophaeonNerveEvents() {}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if(event.getLevel() instanceof ServerLevel level
                && (event.getState().is(BlockInit.nerve_fiber.get())
                || event.getState().is(BlockInit.nerve_bundle.get())))
            announceGap(level,event.getPos(),true);
    }

    public static void announceGap(ServerLevel level,BlockPos gap,boolean playerCut) {
        AABB area=new AABB(gap).inflate(NOTICE_RADIUS);
        for(NaeglerophaeonEntity boss:level.getEntitiesOfClass(NaeglerophaeonEntity.class,area))
            boss.noticeFiberBreak(gap,playerCut);
    }
}
