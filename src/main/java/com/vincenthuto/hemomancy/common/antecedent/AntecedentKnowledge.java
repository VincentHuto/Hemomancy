package com.vincenthuto.hemomancy.common.antecedent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.network.PacketAntecedentResearch;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class AntecedentKnowledge {
    private AntecedentKnowledge() {}
    public static boolean record(ServerPlayer player, AntecedentResearch.Evidence evidence) {
        if (!HemoCapabilityAccess.antecedent(player).record(evidence)) return false;
        MemoHelper.captureMemo(player, Hemomancy.rloc("antecedent_" + evidence.key()));
        sync(player);
        return true;
    }
    public static void sync(ServerPlayer player) {
        PacketHandler.sendToPlayer(player,new PacketAntecedentResearch(HemoCapabilityAccess.antecedent(player).serializeNBT(player.registryAccess())));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) { if(event.getEntity() instanceof ServerPlayer p) sync(p); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) { if(event.getEntity() instanceof ServerPlayer p) sync(p); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { if(event.getEntity() instanceof ServerPlayer p) sync(p); }
}
