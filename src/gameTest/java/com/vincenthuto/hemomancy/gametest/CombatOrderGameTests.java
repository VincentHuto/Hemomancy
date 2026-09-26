package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolCombatEvents;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.function.Consumer;

@GameTestHolder("combat_order_validation")
@PrefixGameTestTemplate(false)
public final class CombatOrderGameTests {
    private static final String ARENA = "ductilis_arena";

    private static Cow target(GameTestHelper h, int x) {
        Cow cow = h.spawn(EntityType.COW, new BlockPos(x, 3, 3));
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION).setBaseValue(20);
        cow.setHealth(100);
        return cow;
    }

    private static SchoolHitContext hit(Cow owner) {
        return SchoolHitContext.direct(Hemomancy.rloc("blood_needle"), EnumBloodTendency.ANIMUS, null, owner);
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void directMeleeNeedsPostDamage(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var victim = target(h, 6);
        try {
            ManipulationReactiveEvents.armLivingCircuit(player);
            NeoForge.EVENT_BUS.post(new AttackEntityEvent(player, victim));
            h.assertTrue(!victim.hasEffect(EffectInit.conductive_mark), "Attack intent counted as a hit");
            victim.hurt(player.damageSources().playerAttack(player), 2);
            h.assertTrue(victim.hasEffect(EffectInit.conductive_mark), "Direct melee did not commit Living Circuit");
            h.succeed();
        } finally { victim.discard(); player.discard(); }
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void projectileKeepsLaunchSchoolAfterSwap(GameTestHelper h) {
        var player = DuctilisGameTests.player(h);
        var victim = target(h, 6);
        var bolt = new BloodBoltEntity(h.getLevel(), player, new ItemStack(ItemInit.living_crossbow.get()));
        try {
            h.getLevel().addFreshEntity(bolt);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_torch.get()));
            var source = SchoolDamage.wrap(victim, h.getLevel().damageSources().arrow(bolt, player));
            h.assertTrue(source.getDirectEntity() == bolt && source.getEntity() == player,
                    "Projectile owner or direct entity changed");
            victim.hurt(source, 2);
            h.assertTrue(victim.hasEffect(EffectInit.conductive_mark) && !victim.hasEffect(EffectInit.searing),
                    "Impact inherited the swapped weapon school");
            h.succeed();
        } finally { bolt.discard(); victim.discard(); player.discard(); }
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void absorptionCountsButZeroedPreDoesNot(GameTestHelper h) {
        var owner = target(h, 2);
        var absorbed = target(h, 5);
        var zeroed = target(h, 8);
        absorbed.setAbsorptionAmount(5);
        var source = SchoolDamage.attributed(zeroed.damageSources().mobAttack(owner), hit(owner), owner);
        Consumer<LivingDamageEvent.Pre> zero = event -> {
            if (event.getEntity() == zeroed && event.getSource() == source) event.setNewDamage(0);
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, zero);
        try {
            absorbed.hurt(SchoolDamage.attributed(absorbed.damageSources().mobAttack(owner), hit(owner), owner), 2);
            h.assertTrue(absorbed.getHealth() == 100 && SchoolStates.has(absorbed, SchoolState.PRESSURE),
                    "Absorption-only damage did not commit school state");
            zeroed.hurt(source, 2);
            h.assertTrue(zeroed.getHealth() == 100 && !SchoolStates.has(zeroed, SchoolState.PRESSURE),
                    "Zeroed Pre damage committed school state");
            h.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(zero);
            owner.discard(); absorbed.discard(); zeroed.discard();
        }
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void reactionPaysOnceWithoutApplyingDirectState(GameTestHelper h) {
        var owner = target(h, 2);
        var victim = target(h, 5);
        try {
            var reaction = hit(owner).child(SchoolHitContext.Kind.REACTION);
            victim.hurt(SchoolDamage.attributed(victim.damageSources().mobAttack(owner), reaction, owner), 2);
            h.assertTrue(victim.getHealth() < 100 && SchoolStates.data(victim).hasPaid(reaction.rootAttack()),
                    "Reaction did not record its root payoff");
            h.assertTrue(!SchoolStates.has(victim, SchoolState.PRESSURE), "Reaction reapplied direct state");
            h.succeed();
        } finally { owner.discard(); victim.discard(); }
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void canceledIncomingClearsPreparedHitBeforeSourceReuse(GameTestHelper h) throws Exception {
        var owner = target(h, 2);
        var victim = target(h, 5);
        var source = SchoolDamage.attributed(victim.damageSources().mobAttack(owner), hit(owner), owner);
        boolean[] cancelFirst = {true};
        Consumer<LivingIncomingDamageEvent> cancel = event -> {
            if (event.getSource() == source && cancelFirst[0]) {
                event.setCanceled(true);
                cancelFirst[0] = false;
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, cancel);
        try {
            victim.hurt(source, 2);
            var field = SchoolCombatEvents.class.getDeclaredField("PENDING");
            field.setAccessible(true);
            var pending = (Map<?, ?>) field.get(null);
            var byTarget = (Map<?, ?>) pending.get(source);
            h.assertTrue(byTarget == null || !byTarget.containsKey(victim.getUUID()),
                    "Canceled incoming damage retained a prepared hit");
            h.assertTrue(victim.getHealth() == 100 && !SchoolStates.has(victim, SchoolState.PRESSURE),
                    "Canceled incoming damage committed a hit");
            victim.hurt(source, 2);
            h.assertTrue(victim.getHealth() == 98 && SchoolStates.has(victim, SchoolState.PRESSURE),
                    "Reused source did not prepare and commit a fresh hit");
            h.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(cancel);
            owner.discard(); victim.discard();
        }
    }

    @GameTest(templateNamespace="combat_order_validation", template=ARENA, timeoutTicks=40)
    public static void canceledPlayerDeathKeepsCurrentRewardDispatch(GameTestHelper h) {
        var attacker = DuctilisGameTests.player(h);
        var victim = DuctilisGameTests.player(h);
        var blood = HemoCapabilityAccess.requireBloodVolume(attacker);
        blood.setActive(true);
        blood.setBloodVolume(500);
        double before = blood.getBloodVolume();
        boolean[] rescued = {false};
        Consumer<LivingDeathEvent> rescue = event -> {
            if (event.getEntity() == victim) {
                rescued[0] = true;
                victim.setHealth(1);
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, rescue);
        try {
            victim.setHealth(0);
            victim.die(attacker.damageSources().playerAttack(attacker));
            h.assertTrue(rescued[0] && victim.isAlive() && victim.getHealth() == 1,
                    "Canceled death did not retain the rescued player");
            h.assertTrue(blood.getBloodVolume() > before, "LOW cancellation ran before default kill reward");
            System.out.println("COMBAT_ORDER_CANCELED_DEATH bloodBefore=" + before
                    + " bloodAfter=" + blood.getBloodVolume());
            h.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(rescue);
            attacker.discard(); victim.discard();
        }
    }
}

