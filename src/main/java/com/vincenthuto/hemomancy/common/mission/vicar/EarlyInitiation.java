package com.vincenthuto.hemomancy.common.mission.vicar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.*;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePool;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hutoslib.common.tendril.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class EarlyInitiation {
    public static final int DURATION = 200;
    private static final Map<UUID, Ceremony> ACTIVE = new HashMap<>();
    private record Ceremony(LivingEntity vicar, Vec3 position, long started, boolean noAi, UUID bloodline) {}
    private EarlyInitiation() {}

    public static boolean attached(Player player) {
        return HemoCapabilityAccess.getEquipment(player).map(e -> e.getStackInSlot(5).is(ItemInit.charm_of_vascularium.get())).orElse(false);
    }
    public static boolean eligible(Player player) {
        return player.isAlive() && !player.isSpectator() && HemoCapabilityAccess.getPlayerDegreeNumber(player) == 0
                && attached(player) && HemoCapabilityAccess.getBloodVolume(player).map(v -> v.isActive()).orElse(false);
    }
    public static boolean near(Player player, Entity npc) {
        return npc != null && npc.isAlive() && npc.level() == player.level() && player.distanceToSqr(npc) <= 64;
    }
    public static void activate(ServerPlayer player) {
        if (!attached(player)) return;
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> { v.setActive(true); BloodVolumeEvents.syncVolume(player, v); });
        HarbingerAdvancementGranter.grantIfNotDone(player, Hemomancy.rloc("hemomancy/the_first_awakening"));
    }
    public static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
    /** Consume projection while officiating, or begin reception of the first unobstructed player hit. */
    public static boolean tryProject(ServerPlayer founder) {
        if (ACTIVE.values().stream().anyMatch(c -> c.vicar == founder)) return true;
        if (foundedBloodline(founder) == null) return false;
        double reach = SanguineProjectionTargeting.PROJECTION_REACH;
        Vec3 eye = founder.getEyePosition();
        var blockHit = SanguineProjectionTargeting.pick(
                founder.level(), founder, reach, true);
        Vec3 end = blockHit.getLocation();
        var hit = ProjectileUtil.getEntityHitResult(
                founder.level(), founder, eye, end,
                founder.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1),
                entity -> entity.isPickable() && !entity.isSpectator());
        return hit != null && hit.getEntity() instanceof ServerPlayer recruit && begin(recruit, founder);
    }
    private static Bloodline foundedBloodline(ServerPlayer founder) {
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(founder);
        var volume = HemoCapabilityAccess.getBloodVolume(founder).orElseThrow();
        if (!founder.isAlive() || founder.isRemoved() || founder.isSpectator() || founder.isSleeping()
                || degree.getDegreeNumber() < 5 || !degree.hasFoundedBloodline()
                || degree.isFounderIntegrationSevered() || !volume.isActive()) return null;
        var line = BloodlineSavedData.get(founder.serverLevel().getServer().overworld())
                .getBloodline(volume.getBloodLine().getBloodlineUUID());
        return line != null && line.canManage(founder.getUUID()) && line.hasMember(founder.getUUID()) ? line : null;
    }
    private static boolean canJoin(ServerPlayer recruit, UUID bloodline) {
        var data = BloodlineSavedData.get(recruit.serverLevel().getServer().overworld());
        var existing = data.getBloodlineForPlayer(recruit.getUUID());
        var personal = HemoCapabilityAccess.getBloodVolume(recruit).orElseThrow().getBloodLine();
        return (existing == null || existing.getBloodlineUUID().equals(bloodline))
                && (!personal.isValid() || personal.getBloodlineUUID().equals(bloodline));
    }
    public static boolean begin(ServerPlayer player, Entity officiant) {
        if (!(officiant instanceof LivingEntity vicar) || !near(player, vicar) || !eligible(player)
                || player.isPassenger() || player.isSleeping() || ACTIVE.containsKey(player.getUUID())
                || ACTIVE.values().stream().anyMatch(c -> c.vicar == vicar)) return false;
        UUID bloodline = null;
        boolean noAi = false;
        if (vicar instanceof HarbingerVicarEntity npc) {
            if (npc.getTarget() != null) return false;
            noAi = npc.isNoAi();
            npc.getNavigation().stop();
            npc.setInitiating(true);
            npc.setNoAi(true);
        } else if (vicar instanceof ServerPlayer founder) {
            var line = foundedBloodline(founder);
            if (line == null || !canJoin(player, line.getBloodlineUUID())) return false;
            bloodline = line.getBloodlineUUID();
        } else return false;
        ACTIVE.put(player.getUUID(), new Ceremony(vicar, player.position(), player.level().getGameTime(), noAi, bloodline));
        player.stopUsingItem();
        return true;
    }
    public static boolean release(ServerPlayer player, Entity npc) {
        if (!(npc instanceof HarbingerVicarEntity) || !near(player, npc) || !eligible(player) || ACTIVE.containsKey(player.getUUID())) return false;
        HemoCapabilityAccess.getEquipment(player).ifPresent(e -> e.setStackInSlot(5, ItemStack.EMPTY));
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> { v.setActive(false); BloodVolumeEvents.syncVolume(player, v); });
        TempleOathRules.clear(player);
        player.displayClientMessage(Component.translatable("hemomancy.initiation.released"), false);
        return true;
    }
    private static void restore(Ceremony ceremony) {
        if (ceremony.vicar instanceof HarbingerVicarEntity npc) {
            npc.setNoAi(ceremony.noAi);
            npc.setInitiating(false);
        }
    }
    public static void cancel(Player player) {
        ACTIVE.entrySet().removeIf(entry -> {
            if (!entry.getKey().equals(player.getUUID()) && entry.getValue().vicar != player) return false;
            restore(entry.getValue());
            return true;
        });
    }
    private static boolean validOfficiant(ServerPlayer player, Ceremony c) {
        if (c.vicar.isRemoved()) return false;
        if (c.vicar instanceof HarbingerVicarEntity npc) return npc.getTarget() == null;
        if (c.vicar instanceof ServerPlayer founder) {
            var line = foundedBloodline(founder);
            return line != null && line.getBloodlineUUID().equals(c.bloodline) && canJoin(player, c.bloodline);
        }
        return false;
    }
    private static void bindBloodline(ServerPlayer player, Ceremony ceremony) {
        var data = BloodlineSavedData.get(player.serverLevel().getServer().overworld());
        var line = data.addMember(ceremony.bloodline, player.getUUID());
        syncMembership(player, line);
        for (var member : player.serverLevel().getServer().getPlayerList().getPlayers()) {
            if (member != player && line.hasMember(member.getUUID())) syncMembership(member, line);
        }
        SkillPointGainEvents.onBloodlineJoined(player);
        player.displayClientMessage(Component.translatable("hemomancy.initiation.bloodline", line.getName()), false);
    }
    private static void syncMembership(ServerPlayer player, Bloodline line) {
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
            volume.setBloodLine(line);
            BloodVolumeEvents.syncVolume(player, volume);
        });
        PacketHandler.sendToPlayer(player, new PacketSyncBloodlinePool(
                line.getBloodVolume(), line.getMaxBloodVolume(), line.getPlayerUUIDS().size()));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Ceremony c = ACTIVE.get(player.getUUID());
        if (c == null) return;
        if (!eligible(player) || !near(player, c.vicar) || player.isPassenger() || player.isSleeping()
                || player.position().distanceToSqr(c.position) > 16 || !validOfficiant(player, c)) { cancel(player); return; }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        player.connection.teleport(c.position.x, c.position.y, c.position.z, player.getYRot(), player.getXRot());
        if (c.vicar instanceof HarbingerVicarEntity npc) {
            npc.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
            npc.setYBodyRot(npc.getYRot());
        }
        long elapsed = player.level().getGameTime() - c.started;
        if (elapsed % 12 == 0) visuals(player, c, elapsed);
        if (elapsed < DURATION) return;
        cancel(player);
        if (!DegreeProgression.advance(player, 1)) return;
        if (c.bloodline != null) bindBloodline(player, c);
        give(player, new ItemStack(ItemInit.sanguine_conduit.get()));
        var starter = Hemomancy.rloc("hemomancy/initiation_blood_claimed");
        if (!HarbingerAdvancementGranter.hasAdvancement(player, starter)) {
            HarbingerAdvancementGranter.grantIfNotDone(player, starter);
            give(player, new ItemStack(ItemInit.bloody_flask.get(), 4));
        }
        if (!player.getInventory().contains(new ItemStack(ItemInit.harbinger_assignment_ledger.get())))
            give(player, new ItemStack(ItemInit.harbinger_assignment_ledger.get()));
        HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED);
        player.displayClientMessage(Component.translatable("hemomancy.initiation.welcome"), false);
        player.displayClientMessage(Component.translatable("hemomancy.tutorial.current_controls",
                Component.keybind("key.hemomancy.bloodformation.desc"), Component.keybind("key.hemomancy.bloodcrafting.desc"),
                Component.keybind("key.hemomancy.cyclemanip.desc")), false);
    }
    private static void visuals(ServerPlayer player, Ceremony c, long elapsed) {
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI * 2 / 10 + elapsed * .025;
            Vec3 root = c.position.add(Math.cos(angle) * .7, .05, Math.sin(angle) * .7);
            Vec3 tip = c.position.add(Math.cos(angle + 1.3) * .5, 1.9, Math.sin(angle + 1.3) * .5);
            var config = TendrilEffectConfig.defaults().withColors(i % 2 == 0 ? 0xF8B40016 : 0xF8060109, 0xB8600010)
                    .withLifecycle(5, 8, 5).withShape(18, 2, .065F, .08F).withBranching(1, 1, .1F, .3F)
                    .withWrithe(.12F, .07F, .7F, .1F).withRange(8F);
            TendrilEffectSpawner.spawn(player.serverLevel(), player, new TendrilAnchor.Point(root), new TendrilAnchor.Point(tip), config);
        }
        Vec3 forward = player.position().subtract(c.vicar.position()).multiply(1, 0, 1).normalize();
        Vec3 hand = c.vicar.position().add(forward.scale(.45)).add(-forward.z * .3, 1.25, forward.x * .3);
        TendrilEffectSpawner.spawn(player.serverLevel(), player, new TendrilAnchor.Point(hand),
                new TendrilAnchor.Point(player.position().add(0, 1.1, 0)),
                com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects.bloodDrainConfig(8, elapsed).withLifecycle(3, 10, 3));
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) cancel(player);
        else ACTIVE.entrySet().removeIf(entry -> {
            Ceremony c = entry.getValue();
            if (c.vicar != event.getEntity()) return false;
            restore(c); return true;
        });
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { cancel(event.getEntity()); }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && HemoCapabilityAccess.getPlayerDegreeNumber(player) == 0
                && attached(player) && TempleOathRules.claimedHeartHermit(player) != null) activate(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {
        ACTIVE.values().forEach(EarlyInitiation::restore); ACTIVE.clear();
    }
}
