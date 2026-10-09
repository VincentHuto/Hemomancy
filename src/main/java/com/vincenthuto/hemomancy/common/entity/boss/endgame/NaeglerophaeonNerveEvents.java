package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** A player's cut provokes the resident; a Borer's chewed gap is only queued for repair. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class NaeglerophaeonNerveEvents {
    private static final double NOTICE_RADIUS=96;

    private NaeglerophaeonNerveEvents() {}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if(event.getLevel() instanceof ServerLevel level
                && (event.getState().is(BlockInit.nerve_fiber.get())
                || event.getState().is(BlockInit.nerve_bundle.get())))
            announceGap(level,event.getPos(),event.getPlayer());
    }

    /** {@code cutter} is null for gaps chewed by Myelin Borers. */
    public static void announceGap(ServerLevel level,BlockPos gap,Player cutter) {
        AABB area=new AABB(gap).inflate(NOTICE_RADIUS);
        for(NaeglerophaeonEntity boss:level.getEntitiesOfClass(NaeglerophaeonEntity.class,area))
            boss.noticeFiberBreak(gap,cutter);
    }
}
