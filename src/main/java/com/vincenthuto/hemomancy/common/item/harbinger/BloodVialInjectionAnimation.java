package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketBloodVialInjection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Transient presentation sessions; injection effects and inventory changes stay in BloodVialItem. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodVialInjectionAnimation {
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private BloodVialInjectionAnimation() { }

    public static void start(Player player, InteractionHand hand, ItemStack vial) {
        if (player.level().isClientSide) return;
        var profile = BloodSampleData.profile(vial, false);
        var session = new Session(player.level(), player.level().getGameTime(), hand,
                (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT), vial,
                profile.tendencies().isEmpty() ? EnumBloodTendency.ANIMUS : profile.tendencies().getFirst(),
                player.getInventory().selected, -1);
        ACTIVE.put(player.getUUID(), session);
        send(player, session, PacketBloodVialInjection.Phase.START, vial);
    }

    public static void finish(Player player, ItemStack result) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null || session.finishedAt >= 0) return;
        ACTIVE.put(player.getUUID(), new Session(session.level, session.startedAt, session.hand, session.right,
                result, session.tendency, session.selectedSlot, player.level().getGameTime()));
        send(player, session, PacketBloodVialInjection.Phase.IMPACT, result);
    }

    public static void cancel(Player player) {
        if (player.level().isClientSide) return;
        Session session = ACTIVE.remove(player.getUUID());
        if (session != null) send(player, session, PacketBloodVialInjection.Phase.CANCEL, ItemStack.EMPTY);
    }

    private static void send(Player player, Session session, PacketBloodVialInjection.Phase phase, ItemStack vial) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PacketBloodVialInjection(player.getId(),
                session.startedAt, phase, session.hand, session.right, vial, session.tendency));
    }

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Session session = ACTIVE.get(player.getUUID());
        if (session == null) return;
        if (!player.isAlive() || player.isSpectator() || player.level() != session.level
                || player.getItemInHand(session.hand) != session.vial
                || session.hand == InteractionHand.MAIN_HAND && player.getInventory().selected != session.selectedSlot) {
            cancel(player);
        } else if (session.finishedAt >= 0) {
            if (player.swinging || player.isUsingItem()) cancel(player);
            else if (player.level().getGameTime() - session.finishedAt >= 8) ACTIVE.remove(player.getUUID());
        } else if (!player.isUsingItem() || player.getUsedItemHand() != session.hand
                || player.getUseItem() != session.vial
                || player.level().getGameTime() - session.startedAt > BloodInjectionRules.USE_TICKS + 2) cancel(player);
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ACTIVE.clear(); }

    private record Session(Level level, long startedAt, InteractionHand hand, boolean right,
                           ItemStack vial, EnumBloodTendency tendency, int selectedSlot, long finishedAt) { }
}
