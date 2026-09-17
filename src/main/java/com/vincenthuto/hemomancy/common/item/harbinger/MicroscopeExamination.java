package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.sounds.SoundSource;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing;
import com.vincenthuto.hemomancy.common.network.PacketHematicMicroscopeViewing.Phase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.util.IdentityHashMap;
import java.util.Map;

/** Server-thread-only, transient use sessions. No item UUIDs or persistent player state. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MicroscopeExamination {
    private static final Map<Player, Session> SESSIONS = new IdentityHashMap<>();
    private static long nextSession;
    private MicroscopeExamination() {}

    private static final class Session {
        final ItemStack instrument, sample;
        ItemStack snapshot;
        final BloodInjectionData.Snapshot definitions = BloodInjectionData.snapshot(false);
        final BloodSampleData.Profile profile;
        final long id = ++nextSession;
        final Level level;
        final long startedAt;
        final boolean right;
        final int slot;
        long releasedAt = -1;
        int ticks;
        boolean complete;
        Session(Player player, ItemStack instrument, ItemStack sample) {
            level = player.level();
            startedAt = level.getGameTime();
            right = player.getMainArm() == HumanoidArm.RIGHT;
            slot = player.getInventory().selected;
            this.instrument = instrument;
            this.sample = sample;
            snapshot = sample.copy();
            profile = BloodSampleData.profile(sample, definitions.properties());
            complete = profile.identified();
        }
        boolean pending() { return !complete && BloodSampleData.entityType(sample) != null; }
        boolean held(Player player) {
            return player.isAlive() && !player.isSpectator() && player.level() == level
                    && player.getInventory().selected == slot && player.getMainHandItem() == instrument
                    && player.getOffhandItem() == sample && ItemStack.matches(snapshot, sample)
                    && definitions == BloodInjectionData.snapshot(false);
        }
        boolean valid(Player player) {
            return held(player) && (releasedAt >= 0 ? !player.isUsingItem() && !player.swinging
                    : player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND
                    && player.getUseItem() == instrument);
        }
        PacketHematicMicroscopeViewing packet(Player player, Phase phase) {
            return new PacketHematicMicroscopeViewing(player.getId(), id, startedAt, phase, right,
                    instrument, snapshot, pending());
        }
    }

    static void begin(Player player, ItemStack instrument, ItemStack sample) {
        clear(player);
        var session = new Session(player, instrument, sample);
        SESSIONS.put(player, session);
        send(player, session, Phase.START);
    }
    static void clear(Player player) {
        var session = SESSIONS.remove(player);
        if (session != null) send(player, session, Phase.CANCEL);
    }
    static void release(Player player) {
        var session = SESSIONS.get(player);
        if (session == null || session.releasedAt >= 0) return;
        if (!session.held(player)) { clear(player); return; }
        session.releasedAt = player.level().getGameTime();
        send(player, session, Phase.RELEASE);
    }
    private static void send(Player player, Session session, Phase phase) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, session.packet(player, phase));
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof Player target) || !(event.getEntity() instanceof ServerPlayer observer)) return;
        var session = SESSIONS.get(target);
        if (session != null && session.releasedAt < 0 && session.valid(target))
            PacketDistributor.sendToPlayer(observer, session.packet(target, Phase.START));
    }

    static void tick(Player player, int remaining) {
        var session = SESSIONS.get(player);
        if (session == null || !session.valid(player)) {
            clear(player);
            player.stopUsingItem();
            return;
        }
        if (session.complete || session.releasedAt >= 0) return;
        if (remaining != HematicMicroscopeItem.USE_DURATION - session.ticks) {
            clear(player);
            player.stopUsingItem();
            return;
        }
        session.ticks++;
        if (session.ticks == 20 && session.pending()) player.level().playSound(null, player.blockPosition(), SoundInit.MICROSCOPE_FOCUS.get(), SoundSource.PLAYERS, 0.2F, 1F);
        if (session.ticks < HematicMicroscopeItem.EXAMINATION_TICKS) return;
        var current = BloodSampleData.profile(session.sample, session.definitions.properties());
        if (!current.equals(session.profile)) {
            clear(player);
            player.stopUsingItem();
            return;
        }
        // An unreadable specimen stays murky and intact, including any existing identification.
        session.complete = true;
        if (BloodSampleData.identify(session.sample)) {
            session.snapshot = session.sample.copy();
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.level().playSound(null, player.blockPosition(), SoundInit.MICROSCOPE_DISCOVERY.get(), SoundSource.PLAYERS, 0.3F, 1F);
        }
        send(player, session, Phase.COMPLETE);
    }

    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;
        var session = SESSIONS.get(player);
        if (session != null && !session.valid(player)) {
            clear(player);
            if (session.releasedAt < 0) player.stopUsingItem();
        } else if (session != null && session.releasedAt >= 0 && player.level().getGameTime() - session.releasedAt >= 6) {
            SESSIONS.remove(player);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { clear(event.getEntity()); }
    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) clear(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { SESSIONS.clear(); }
}
