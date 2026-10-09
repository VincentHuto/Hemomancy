package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.DrudgeAction;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteEvents;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Hematic Riposte's server window: frontal melee and projectiles only, refunding once and leaving wards unspent. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HematicRiposteGameTests {
    private static final String EMPTY = "bastion/mobs/empty";

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte")
    public static void frontalMeleeIsNegatedStunnedAndRefunded(GameTestHelper h) {
        ServerPlayer p = player(h);
        Zombie zombie = zombie(h, p, 1.5);
        try {
            BloodManipulation riposte = cast(h, p);
            h.assertTrue(riposte.getRemainingCooldownTicks(p) > 30, "Cast did not start the whiff cooldown");
            var hit = melee(p, zombie, 6);
            NeoForge.EVENT_BUS.post(hit);
            h.assertTrue(hit.isCanceled(), "Frontal melee inside the window was not parried");
            h.assertTrue(zombie.hasEffect(EffectInit.paralysis), "Parried attacker was not stunned");
            h.assertTrue(riposte.getRemainingCooldownTicks(p) <= HematicRiposteRules.REFUND_COOLDOWN,
                    "Successful parry did not shorten the cooldown");
            h.succeed();
        } finally { zombie.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte")
    public static void hitsFromBehindLandAndKeepTheWhiffCooldown(GameTestHelper h) {
        ServerPlayer p = player(h);
        Zombie zombie = zombie(h, p, -1.5);
        try {
            BloodManipulation riposte = cast(h, p);
            var hit = melee(p, zombie, 6);
            NeoForge.EVENT_BUS.post(hit);
            h.assertTrue(!hit.isCanceled(), "A hit from behind was parried");
            h.assertTrue(!zombie.hasEffect(EffectInit.paralysis), "Unparried attacker was stunned");
            h.assertTrue(riposte.getRemainingCooldownTicks(p) > 30, "A missed parry refunded the cooldown");
            h.succeed();
        } finally { zombie.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte", timeoutTicks = 40)
    public static void windowClosesAfterTheSwipe(GameTestHelper h) {
        ServerPlayer p = player(h);
        Zombie zombie = zombie(h, p, 1.5);
        cast(h, p);
        h.runAfterDelay(HematicRiposteRules.PARRY_CLOSE + 2, () -> {
            try {
                var hit = melee(p, zombie, 6);
                NeoForge.EVENT_BUS.post(hit);
                h.assertTrue(!hit.isCanceled(), "Window stayed open after the swipe");
                h.succeed();
            } finally { zombie.discard(); p.discard(); }
        });
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte")
    public static void parriedHitsLeaveSanguineWardUnspent(GameTestHelper h) {
        ServerPlayer p = player(h);
        Zombie zombie = zombie(h, p, 1.5);
        try {
            ManipulationReactiveEvents.refreshSanguineWard(p);
            cast(h, p);
            var parried = melee(p, zombie, 6);
            NeoForge.EVENT_BUS.post(parried);
            h.assertTrue(parried.isCanceled(), "Ward fixture hit was not parried");
            var later = new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().generic(), 10));
            ManipulationReactiveEvents.onIncomingDamage(later);
            h.assertTrue(Math.abs(later.getAmount() - 4) < .002, "Parried hit spent the Ward pool: " + later.getAmount());
            h.succeed();
        } finally { zombie.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte")
    public static void frontalArrowIsSwatedAsideWithItsOwnerKept(GameTestHelper h) {
        ServerPlayer p = player(h);
        var skeleton = EntityType.SKELETON.create(h.getLevel());
        skeleton.setPos(p.position().add(0, 0, 8));
        h.getLevel().addFreshEntity(skeleton);
        Arrow arrow = EntityType.ARROW.create(h.getLevel());
        try {
            cast(h, p);
            arrow.setOwner(skeleton);
            arrow.setPos(p.getEyePosition().add(0, 0, .6));
            arrow.setDeltaMovement(0, 0, -1.5);
            h.getLevel().addFreshEntity(arrow);
            var impact = new ProjectileImpactEvent(arrow, new EntityHitResult(p));
            NeoForge.EVENT_BUS.post(impact);
            h.assertTrue(impact.isCanceled(), "Frontal arrow was not swatted");
            h.assertTrue(arrow.getOwner() == skeleton, "Swat transferred arrow ownership");
            Vec3 shooterLine = skeleton.position().subtract(p.position()).normalize();
            h.assertTrue(arrow.getDeltaMovement().normalize().dot(shooterLine) < .5, "Swat sent the arrow back at its shooter");
            h.assertTrue(arrow.getDeltaMovement().length() >= HematicRiposteRules.SWAT_MIN_SPEED - 1e-6, "Swatted arrow stalled");
            h.succeed();
        } finally { arrow.discard(); skeleton.discard(); p.discard(); }
    }

    @GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "hematic_riposte")
    public static void drudgesCannotRiposte(GameTestHelper h) {
        h.assertTrue(ManipulationInit.hematic_riposte.get().getDrudgeAction()
                .filter(action -> action == DrudgeAction.DRUDGE_UNSUPPORTED).isPresent(), "Riposte gained a Drudge action");
        h.succeed();
    }

    private static BloodManipulation cast(GameTestHelper h, ServerPlayer p) {
        BloodManipulation riposte = ManipulationInit.hematic_riposte.get();
        select(p, riposte);
        h.assertTrue(riposte.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0),
                "Riposte cast was refused");
        h.assertTrue(HematicRiposteEvents.isParrying(p), "Cast did not open the parry window");
        return riposte;
    }

    private static LivingIncomingDamageEvent melee(ServerPlayer p, Zombie zombie, float amount) {
        return new LivingIncomingDamageEvent(p, new DamageContainer(p.damageSources().mobAttack(zombie), amount));
    }

    /** A zombie {@code dz} blocks along +Z, which the fixture player faces. */
    private static Zombie zombie(GameTestHelper h, ServerPlayer p, double dz) {
        Zombie zombie = EntityType.ZOMBIE.create(h.getLevel());
        zombie.setPos(p.position().add(0, 0, dz));
        zombie.setNoAi(true);
        h.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static void select(ServerPlayer p, BloodManipulation m) {
        var known = HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(m, new ManipLevel(0, 0));
        known.setEquippedManipNames(List.of(m.getName()));
        known.setSelectedManip(m);
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "riposte"), false);
        ServerPlayer p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        p.setPos(h.absoluteVec(new Vec3(2.5, 2, 2.5)));
        p.setYRot(0);
        p.setYBodyRot(0);
        p.setYHeadRot(0);
        var blood = HemoCapabilityAccess.requireBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        for (var tendency : EnumBloodTendency.values())
            HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(tendency, 100);
        return p;
    }
}
