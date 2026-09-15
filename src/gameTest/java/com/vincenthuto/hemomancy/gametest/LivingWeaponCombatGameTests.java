package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeRotPools;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("living_weapon_validation")
@PrefixGameTestTemplate(false)
public final class LivingWeaponCombatGameTests {
    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons", timeoutTicks = 180)
    public static void axePoolsRequireCriticalsReachTheGroundAndPreservePulseTimingWhenMerged(GameTestHelper h) {
        ServerPlayer p = player(h);
        h.getLevel().addNewPlayer(p);
        Cow first = target(h, 2, 2), second = target(h, 6, 2), inside = target(h, 4, 2), outside = target(h, 9, 2);
        first.setPos(first.position().add(0, 3, 0));
        second.setPos(second.position().add(0, 3, 0));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_axe.get()));
        float pulse = com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules
                .damageMultiplier(p, inside, EnumBloodTendency.MORTEM, null);
        var tick = new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(() -> true, h.getLevel().getServer());
        try {
            axeHit(p, first, false);
            LivingAxeRotPools.tick(tick);
            h.assertTrue(inside.getHealth() == 100, "An ordinary axe strike created a pool");
            axeHit(p, first, true);
            LivingAxeRotPools.tick(tick);
            h.assertTrue(inside.getHealth() == 100 - pulse,
                    "The critical pool did not reach the ground below its airborne victim: " + inside.getHealth());
            h.assertTrue(SchoolStates.has(inside, SchoolState.NECROSIS), "Pool did not apply Necrosis");
            inside.invulnerableTime = 0;
            axeHit(p, second, true);
            LivingAxeRotPools.tick(tick);
            h.assertTrue(inside.getHealth() == 100 - pulse, "Refreshing an overlapping pool caused an extra immediate pulse");
            h.assertTrue(outside.getHealth() == 100, "Pool exceeded its radius");
            h.runAfterDelay(22, () -> h.assertTrue(inside.getHealth() == 100 - 2 * pulse,
                    "Merged pool lost its twenty-tick pulse cadence: " + inside.getHealth()));
            h.runAfterDelay(125, () -> {
                try {
                    h.assertTrue(inside.getHealth() == 100 - 6 * pulse, "Pool did not expire after six pulses: " + inside.getHealth());
                    h.succeed();
                } finally { p.discard(); first.discard(); second.discard(); inside.discard(); outside.discard(); }
            });
        } catch (RuntimeException | Error failure) {
            p.discard(); first.discard(); second.discard(); inside.discard(); outside.discard();
            throw failure;
        }
    }

    private static void axeHit(ServerPlayer p, LivingEntity target, boolean critical) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                new net.neoforged.neoforge.event.entity.player.CriticalHitEvent(p, target, critical ? 1.5F : 1, critical));
        target.invulnerableTime = 0;
        hit(p, target, "living_axe", EnumBloodTendency.MORTEM, 4, 1);
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void spearBurstExcludesAllies(GameTestHelper h) {
        ServerPlayer p = player(h);
        Cow primary = target(h, 2, 2), ally = target(h, 4, 2);
        var board = h.getLevel().getScoreboard();
        var team = board.addPlayerTeam("weapon_" + UUID.randomUUID().toString().substring(0, 8));
        try {
            board.addPlayerToTeam(p.getScoreboardName(), team);
            board.addPlayerToTeam(ally.getScoreboardName(), team);
            ItemStack spear = new ItemStack(ItemInit.living_spear.get());
            p.setItemInHand(InteractionHand.MAIN_HAND, spear);
            LivingSpearItem.setLuxCharge(spear, 30);
            hit(p, primary, "living_spear", EnumBloodTendency.LUX, 4, 1);
            h.assertTrue(ally.getHealth() == 100, "Lux burst damaged an ally");
            h.assertTrue(!SchoolStates.has(ally, SchoolState.ILLUMINATED), "Lux burst illuminated an excluded ally");
            h.succeed();
        } finally { board.removePlayerTeam(team); p.discard(); primary.discard(); ally.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void bloodBoltStopsAfterThreeHopsAndDoesNotChainAgainOnPiercingHit(GameTestHelper h) {
        ServerPlayer p = player(h);
        Cow primary = target(h, 2, 2), first = target(h, 5, 2), second = target(h, 8, 2);
        Cow third = target(h, 11, 2), extra = target(h, 14, 2);
        TestBolt bolt = new TestBolt(h.getLevel(), p, new ItemStack(ItemInit.living_crossbow.get()));
        try {
            bolt.setPos(primary.position().add(-1, .8, 0));
            bolt.setDeltaMovement(2, 0, 0); bolt.setBaseDamage(8);
            SchoolDamage.captureProjectile(bolt); h.getLevel().addFreshEntity(bolt);
            bolt.impact(primary);
            h.assertTrue(Math.abs(first.getHealth() - 88) < .01, "First hop falloff changed");
            h.assertTrue(Math.abs(second.getHealth() - 92) < .01, "Second hop falloff changed");
            h.assertTrue(Math.abs(third.getHealth() - 96) < .01, "Third hop falloff changed");
            h.assertTrue(extra.getHealth() == 100, "A fourth hop escaped the chain limit");
            first.invulnerableTime = second.invulnerableTime = third.invulnerableTime = 0;
            bolt.impact(extra);
            h.assertTrue(Math.abs(third.getHealth() - 96) < .01, "The piercing bolt started a second chain");
            h.succeed();
        } finally { p.discard(); primary.discard(); first.discard(); second.discard(); third.discard(); extra.discard(); bolt.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void synchronizedAxeSwingUsesTwentyTicksAndLeavesOtherWeaponsAlone(GameTestHelper h) throws Exception {
        ServerPlayer p = player(h);
        try {
            var duration = LivingEntity.class.getDeclaredMethod("getCurrentSwingDuration");
            duration.setAccessible(true);
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_axe.get()));
            p.swingingArm = InteractionHand.MAIN_HAND;
            h.assertTrue((int)duration.invoke(p) == 20, "Axe is still using the six-tick vanilla swing");
            p.setItemInHand(InteractionHand.OFF_HAND, p.getMainHandItem());
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_blade.get()));
            h.assertTrue((int)duration.invoke(p) == 6, "An idle offhand axe changed the main-hand swing");
            p.swingingArm = InteractionHand.OFF_HAND;
            h.assertTrue((int)duration.invoke(p) == 20, "Offhand axe did not use its overhead timing");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void unstainedSwingsUseWeaponTimingInEitherHandAndRespectHaste(GameTestHelper h) throws Exception {
        ServerPlayer p = player(h);
        try {
            var duration = LivingEntity.class.getDeclaredMethod("getCurrentSwingDuration");
            duration.setAccessible(true);
            var weapons = new net.minecraft.world.item.Item[]{ItemInit.absolution_dagger.get(),
                    ItemInit.annettas_absolution_dagger.get(), ItemInit.silthmere_glaive.get(),
                    ItemInit.unstained_warhammer.get()};
            int[] ticks = {8, 8, 16, 20};
            for (int i = 0; i < weapons.length; i++) {
                for (InteractionHand hand : InteractionHand.values()) {
                    p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    p.setItemInHand(hand, new ItemStack(weapons[i]));
                    p.swingingArm = hand;
                    h.assertTrue((int) duration.invoke(p) == ticks[i], "Wrong Unstained swing duration for " + weapons[i] + " in " + hand);
                }
            }
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 100, 1));
            h.assertTrue((int) duration.invoke(p) == 13, "Hammer must retain vanilla haste scaling like the axe");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void chargedSpearAddsTheFullBurstToPrimaryAndNearbyTargets(GameTestHelper h) {
        ServerPlayer p = player(h);
        Cow primary = target(h, 2, 2), nearby = target(h, 4, 2), outside = target(h, 9, 2);
        try {
            ItemStack spear = new ItemStack(ItemInit.living_spear.get());
            p.setItemInHand(InteractionHand.MAIN_HAND, spear);
            LivingSpearItem.setLuxCharge(spear, 30);
            hit(p, primary, "living_spear", EnumBloodTendency.LUX, 4, 1);
            h.assertTrue(Math.abs(primary.getHealth() - 88) < .01, "Primary missed some burst damage: " + primary.getHealth());
            h.assertTrue(Math.abs(nearby.getHealth() - 92) < .01, "Nearby target missed burst: " + nearby.getHealth());
            h.assertTrue(outside.getHealth() == 100, "Burst exceeded five blocks");
            h.assertTrue(LivingSpearItem.getLuxCharge(spear) == 0, "Burst did not consume charge");
            h.assertTrue(SchoolStates.has(primary, SchoolState.ILLUMINATED), "Primary was not illuminated");
            h.assertTrue(SchoolStates.has(nearby, SchoolState.ILLUMINATED), "Nearby target was not illuminated");
            h.succeed();
        } finally { p.discard(); primary.discard(); nearby.discard(); outside.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void spearChargePersistsAndFailedOrUncooledHitsDoNotConsumeIt(GameTestHelper h) {
        ServerPlayer p = player(h);
        Cow primary = target(h, 2, 2);
        try {
            ItemStack spear = new ItemStack(ItemInit.living_spear.get());
            LivingSpearItem.setLuxCharge(spear, 30);
            spear = ItemStack.parseOptional(h.getLevel().registryAccess(),
                    (net.minecraft.nbt.CompoundTag)spear.save(h.getLevel().registryAccess()));
            p.setItemInHand(InteractionHand.MAIN_HAND, spear);
            h.assertTrue(LivingSpearItem.getLuxCharge(spear) == 30, "Charge was lost when the item was saved");
            primary.setInvulnerable(true);
            hit(p, primary, "living_spear", EnumBloodTendency.LUX, 4, 1);
            h.assertTrue(LivingSpearItem.getLuxCharge(spear) == 30, "Failed hit consumed charge");
            primary.setInvulnerable(false);
            hit(p, primary, "living_spear", EnumBloodTendency.LUX, 4, .5F);
            h.assertTrue(LivingSpearItem.getLuxCharge(spear) == 30, "Uncooled hit consumed charge");
            h.succeed();
        } finally { p.discard(); primary.discard(); }
    }

    @GameTest(templateNamespace = "living_weapon_validation", template = "empty", batch = "living_weapons")
    public static void bloodBoltUsesCalculatedImpactDamageEvenWhenPrimaryIsNearlyDead(GameTestHelper h) {
        ServerPlayer p = player(h);
        Cow primary = target(h, 2, 2), next = target(h, 5, 2);
        TestBolt bolt = new TestBolt(h.getLevel(), p, new ItemStack(ItemInit.living_crossbow.get()));
        try {
            primary.setHealth(2);
            bolt.setPos(primary.position().add(-1, .8, 0));
            bolt.setDeltaMovement(2, 0, 0);
            bolt.setBaseDamage(8);
            SchoolDamage.captureProjectile(bolt);
            h.getLevel().addFreshEntity(bolt);
            bolt.impact(primary);
            h.assertTrue(Math.abs(next.getHealth() - 88) < .01,
                    "First hop should deal 75% of the 16-damage impact, health=" + next.getHealth());
            h.succeed();
        } finally { p.discard(); primary.discard(); next.discard(); bolt.discard(); }
    }

    private static void hit(ServerPlayer player, LivingEntity target, String ability,
            EnumBloodTendency school, float amount, float strength) {
        SchoolHitContext context = SchoolHitContext.direct(Hemomancy.rloc(ability), school, null, player)
                .withCharge(strength).withRole(SchoolHitContext.Role.DAMAGE_ONLY);
        target.hurt(SchoolDamage.attributed(player.damageSources().playerAttack(player), context, player), amount);
    }

    private static Cow target(GameTestHelper h, double x, double z) {
        Cow cow = EntityType.COW.create(h.getLevel());
        cow.setPos(h.absoluteVec(new Vec3(x, 4, z)));
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        cow.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        cow.setHealth(100);
        h.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "weapon_test"), false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setPos(h.absoluteVec(new Vec3(2, 4, 0)));
        HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
        HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(5000);
        return player;
    }

    private static final class TestBolt extends BloodBoltEntity {
        TestBolt(Level level, LivingEntity owner, ItemStack weapon) { super(level, owner, weapon); }
        @Override public byte getPierceLevel() { return 2; }
        void impact(LivingEntity target) { onHitEntity(new EntityHitResult(target)); }
    }
}
