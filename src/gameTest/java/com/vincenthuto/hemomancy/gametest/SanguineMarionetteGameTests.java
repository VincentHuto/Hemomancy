package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.damage.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SanguineMarionetteGameTests {
    private static ServerPlayer caster(GameTestHelper h) {
        ServerPlayer player = DuctilisGameTests.player(h);
        var known = HemoCapabilityAccess.requireKnownManipulations(player);
        known.getKnownManips().put(ManipulationInit.blood_binding.get(), new ManipLevel(3, 0));
        known.getKnownManips().put(ManipulationInit.sanguine_marionette.get(), new ManipLevel(3, 0));
        known.setEquippedManipNames(java.util.List.of("sanguine_marionette"));
        known.setSelectedManip(ManipulationInit.sanguine_marionette.get());
        var volume = HemoCapabilityAccess.requireBloodVolume(player);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        player.setYRot(0);
        player.setXRot(0);
        for (int x = 0; x < 18; x++) for (int z = 0; z < 18; z++)
            h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        return player;
    }

    private static Mob body(GameTestHelper h, int x, int z) {
        var mob = h.spawn(EntityType.HUSK, new BlockPos(x, 2, z));
        mob.setPersistenceRequired();
        mob.setOnGround(true);
        return mob;
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=190)
    public static void eightSecondChannelPaysEightPulsesAndReleasesAutomatically(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        double cost = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
                .collect(player, ManipulationInit.sanguine_marionette.get()).effectiveCost();
        ManipulationChannelManager.start(player);
        for (int tick = 1; tick <= 160; tick++) h.runAfterDelay(tick, () ->
                ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player)));
        h.runAfterDelay(165, () -> {
            try {
                h.assertTrue(!ManipulationChannelManager.isChanneling(player.getUUID()) && HematicCommandManager.marionette(player) == null,
                        "Marionette exceeded its eight-second maximum");
                h.assertTrue(Math.abs(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() - (1000 - 8 * cost)) < .001,
                        "Pulse ledger: actual=" + HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() + ", expected=" + (1000 - 8 * cost) + ", cost=" + cost);
                h.assertTrue(!((com.vincenthuto.hemomancy.mixin.core.GoalSelectorAccessor)puppet.targetSelector)
                        .hemomancy$disabledFlags().contains(Goal.Flag.TARGET), "Timed expiry left AI disabled");
                h.succeed();
            } finally { ManipulationChannelManager.stop(player, false); puppet.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=50)
    public static void insufficientBloodEndsTheChannelAndReleasesTheBody(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        ManipulationChannelManager.start(player);
        HemoCapabilityAccess.requireBloodVolume(player).setBloodVolume(0);
        h.runAfterDelay(20, () -> ManipulationChannelManager.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player)));
        h.runAfterDelay(25, () -> {
            try {
                h.assertTrue(!ManipulationChannelManager.isChanneling(player.getUUID()) && HematicCommandManager.marionette(player) == null,
                        "Unpaid tether retained control");
                h.succeed();
            } finally { ManipulationChannelManager.stop(player, false); puppet.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=45)
    public static void lingeringPotionRetainsProtectionAfterRelease(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        var pet = h.spawn(EntityType.WOLF, new BlockPos(4, 2, 3));
        pet.tame(player);
        pet.setNoAi(true);
        var enemy = h.spawn(EntityType.COW, new BlockPos(5, 2, 3));
        enemy.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, puppet), "Acquisition failed");
        var potion = new net.minecraft.world.entity.projectile.ThrownPotion(h.getLevel(), puppet);
        h.getLevel().addFreshEntity(potion);
        var cloud = new net.minecraft.world.entity.AreaEffectCloud(h.getLevel(), player.getX(), player.getY(), player.getZ());
        cloud.setOwner(puppet);
        cloud.setRadius(5);
        cloud.setWaitTime(0);
        cloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 200));
        HematicCommandManager.copyAttackOwner(potion, cloud);
        HematicCommandManager.releaseMarionette(player.getUUID());
        potion.discard();
        h.getLevel().addFreshEntity(cloud);
        h.runAfterDelay(7, () -> {
            try {
                h.assertTrue(!player.hasEffect(net.minecraft.world.effect.MobEffects.POISON)
                        && !pet.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Released poison cloud harmed an ally");
                h.assertTrue(enemy.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Protection erased permitted enemy effects");
                h.succeed();
            } finally { cloud.discard(); enemy.discard(); pet.discard(); puppet.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=45)
    public static void creeperCloudKeepsCommandProtectionAfterItsBodyDies(GameTestHelper h) {
        ServerPlayer player = caster(h);
        var creeper = h.spawn(EntityType.CREEPER, new BlockPos(4, 2, 3));
        creeper.setNoAi(true);
        var pet = h.spawn(EntityType.WOLF, new BlockPos(4, 2, 3));
        pet.tame(player);
        pet.setNoAi(true);
        var enemy = h.spawn(EntityType.COW, new BlockPos(4, 2, 4));
        enemy.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, creeper), "Creeper acquisition failed");
        var settings = new net.minecraft.nbt.CompoundTag();
        settings.putByte("ExplosionRadius", (byte)0);
        settings.putShort("Fuse", (short)1);
        creeper.readAdditionalSaveData(settings);
        creeper.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 200));
        creeper.ignite();
        creeper.tick();
        h.runAfterDelay(16, () -> {
            try {
                h.assertTrue(creeper.isRemoved() && HematicCommandManager.marionette(player) == null, "Exploded body retained command");
                h.assertTrue(!player.hasEffect(net.minecraft.world.effect.MobEffects.POISON)
                        && !pet.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Creeper cloud lost its caster after the body died");
                h.assertTrue(enemy.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Creeper cloud stopped affecting permitted enemies");
                h.succeed();
            } finally {
                h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.AreaEffectCloud.class,
                        creeper.getBoundingBox().inflate(5)).forEach(net.minecraft.world.entity.Entity::discard);
                pet.discard(); enemy.discard(); player.discard();
            }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=45)
    public static void delayedEvokerFangsAndVexKeepProtectionAfterRelease(GameTestHelper h) {
        ServerPlayer player = caster(h);
        var evoker = h.spawn(EntityType.EVOKER, new BlockPos(2, 2, 6));
        evoker.setNoAi(true);
        var pet = h.spawn(EntityType.WOLF, new BlockPos(4, 2, 3));
        pet.tame(player); pet.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, evoker), "Evoker acquisition failed");
        var fangs = new net.minecraft.world.entity.projectile.EvokerFangs(h.getLevel(), pet.getX(), pet.getY(), pet.getZ(), 0, 2, evoker);
        var vex = h.spawn(EntityType.VEX, new BlockPos(4, 3, 3));
        vex.setOwner(evoker); vex.setNoAi(true);
        // Native Evoker sets ownership before adding its Vex.
        var spawnedVex = new net.minecraft.world.entity.monster.Vex(EntityType.VEX, h.getLevel());
        spawnedVex.setOwner(evoker); spawnedVex.setPos(vex.position()); spawnedVex.setNoAi(true);
        vex.discard(); h.getLevel().addFreshEntity(spawnedVex);
        h.getLevel().addFreshEntity(fangs);
        float before = pet.getHealth();
        HematicCommandManager.releaseMarionette(player.getUUID());
        pet.hurt(h.getLevel().damageSources().mobAttack(spawnedVex), 3);
        h.runAfterDelay(12, () -> {
            try {
                h.assertTrue(pet.getHealth() == before, "Delayed native fangs or Vex lost command protection");
                h.succeed();
            } finally { fangs.discard(); spawnedVex.discard(); evoker.discard(); pet.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=40)
    public static void releaseResetsAnOrderedCreeperFuseAndRejectsOwnedPets(GameTestHelper h) {
        ServerPlayer player = caster(h);
        var creeper = h.spawn(EntityType.CREEPER, new BlockPos(2, 2, 6));
        creeper.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, creeper), "Creeper acquisition failed");
        creeper.setSwellDir(1);
        for (int tick = 0; tick < 15; tick++) creeper.tick();
        h.assertTrue(creeper.getSwelling(1) > .4, "Fixture did not prime a partial fuse");
        HematicCommandManager.releaseMarionette(player.getUUID());
        h.assertTrue(creeper.getSwelling(1) == 0 && creeper.getSwellDir() < 0, "Release retained the ordered fuse");
        ServerPlayer rival = DuctilisGameTests.player(h);
        var pet = h.spawn(EntityType.WOLF, new BlockPos(4, 2, 3));
        pet.tame(rival);
        h.assertTrue(!HematicCommandManager.acquireMarionette(player, pet), "Marionette captured another player's pet");
        creeper.discard(); pet.discard(); player.discard(); rival.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=40)
    public static void channelPaysFirstPulseAndRestoresFlagsOnRelease(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        puppet.targetSelector.disableControlFlag(Goal.Flag.TARGET);
        double cost = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
                .collect(player, ManipulationInit.sanguine_marionette.get()).effectiveCost();
        h.assertTrue(ManipulationInit.sanguine_marionette.get().getCost() == 50, "Base pulse cost is not 50 mL");
        ManipulationChannelManager.start(player);
        h.assertTrue(ManipulationChannelManager.isChanneling(player.getUUID()), "Eligible tether did not start");
        h.assertTrue(HematicCommandManager.marionette(player) == puppet, "Tether selected the wrong body");
        h.assertTrue(Math.abs(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() - (1000 - cost)) < .001, "First pulse did not charge the existing cost ledger exactly once");
        h.assertTrue(!puppet.hasEffect(EffectInit.blood_binding) && !puppet.isNoAi(), "Mobile tether froze or disabled the puppet");
        ManipulationChannelManager.stop(player);
        h.assertTrue(HematicCommandManager.marionette(player) == null, "Release retained control");
        h.assertTrue(ManipulationInit.sanguine_marionette.get().getCooldownTicks() == 120, "Base cooldown is not six seconds");
        long cooldown = (long)(120 * HemoCapabilityAccess.requireKnownManipulations(player)
                .getManipLevel(ManipulationInit.sanguine_marionette.get()).getCooldownMultiplier());
        h.assertTrue(ManipulationInit.sanguine_marionette.get().getRemainingCooldownTicks(player) == cooldown,
                "Release did not preserve mastery scaling on the six-second cooldown");
        h.assertTrue(((com.vincenthuto.hemomancy.mixin.core.GoalSelectorAccessor)puppet.targetSelector)
                .hemomancy$disabledFlags().contains(Goal.Flag.TARGET), "Release lost the original AI flag");
        puppet.discard(); player.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=80)
    public static void moveOrderUsesNavigationAndAttackOrderUsesNaturalMelee(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        var enemy = h.spawn(EntityType.COW, new BlockPos(8, 2, 6));
        enemy.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, puppet), "Acquisition failed");
        h.assertTrue(HematicCommandManager.orderMove(player, h.absolutePos(new BlockPos(6, 2, 6))), "Reachable move order was rejected");
        Vec3 start = puppet.position();
        h.runAfterDelay(25, () -> {
            h.assertTrue(puppet.position().distanceToSqr(start) > 1, "Puppet did not move with its natural navigation");
            h.assertTrue(HematicCommandManager.orderAttack(player, enemy), "Permitted attack order was rejected");
        });
        h.runAfterDelay(65, () -> {
            h.assertTrue(enemy.getHealth() < enemy.getMaxHealth(), "Puppet did not use its natural melee attack");
            HematicCommandManager.releaseMarionette(player.getUUID());
            h.assertTrue(puppet.getTarget() == null && !puppet.isNoAi(), "Release left the attack order or disabled AI");
            puppet.discard(); enemy.discard(); player.discard(); h.succeed();
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=100)
    public static void brainPuppetFollowsMoveAndAttackOrdersWithoutRetargetingItsCaster(GameTestHelper h) {
        ServerPlayer player = caster(h);
        var puppet = h.spawn(EntityType.PIGLIN, new BlockPos(2, 2, 6));
        puppet.setImmuneToZombification(true);
        puppet.setOnGround(true);
        puppet.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
        var enemy = h.spawn(EntityType.COW, new BlockPos(8, 2, 6));
        enemy.setNoAi(true);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, puppet), "Piglin acquisition failed");
        h.assertTrue(HematicCommandManager.orderMove(player, h.absolutePos(new BlockPos(6, 2, 6))), "Piglin move order rejected");
        Vec3 start = puppet.position();
        h.runAfterDelay(25, () -> {
            h.assertTrue(puppet.position().distanceToSqr(start) > 1, "Brain puppet ignored its move order");
            h.assertTrue(HematicCommandManager.orderAttack(player, enemy), "Piglin attack order rejected");
        });
        h.runAfterDelay(85, () -> {
            try {
                h.assertTrue(enemy.getHealth() < enemy.getMaxHealth(), "Brain puppet discarded the permitted attack order");
                h.assertTrue(puppet.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET)
                        .filter(target -> target == player).isEmpty(), "Brain puppet retargeted its caster");
                HematicCommandManager.releaseMarionette(player.getUUID());
                h.assertTrue(puppet.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).isEmpty(),
                        "Release retained the Brain attack order");
                h.succeed();
            } finally { HematicCommandManager.releaseMarionette(player.getUUID()); puppet.discard(); enemy.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=40)
    public static void sharedBodyLimitAndOwnershipExcludeCaptivesAndBloodlessMobs(GameTestHelper h) {
        ServerPlayer player = caster(h);
        ServerPlayer rival = DuctilisGameTests.player(h);
        Mob first = body(h, 2, 6), second = body(h, 4, 6);
        var skeleton = h.spawn(EntityType.SKELETON, new BlockPos(6, 2, 6));
        h.assertTrue(!HematicCommandManager.acquireMarionette(player, skeleton), "Bloodless mob was captured");
        h.assertTrue(HematicCommandManager.impress(player, first), "Impressment failed");
        h.assertTrue(HematicCommandManager.acquireMarionette(player, second), "Marionette did not replace Impressment");
        h.assertTrue(!HematicCommandManager.isImpressed(first, player), "Two bodies remained controlled");
        h.assertTrue(!HematicCommandManager.acquireMarionette(rival, second), "Another caster stole the puppet");
        first.addEffect(new net.minecraft.world.effect.MobEffectInstance(EffectInit.blood_binding, 100));
        h.assertTrue(!HematicCommandManager.acquireMarionette(player, first), "Stationary captive became a mobile puppet");
        HematicCommandManager.releaseMarionette(player.getUUID());
        first.discard(); second.discard(); skeleton.discard(); player.discard(); rival.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=40)
    public static void aReleasedPuppetsProjectileCannotHitItsFormerCasterOrPet(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        var pet = h.spawn(EntityType.WOLF, new BlockPos(4, 2, 3));
        pet.tame(player);
        h.assertTrue(HematicCommandManager.acquireMarionette(player, puppet), "Acquisition failed");
        Arrow arrow = new Arrow(h.getLevel(), puppet, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
        h.getLevel().addFreshEntity(arrow);
        HematicCommandManager.releaseMarionette(player.getUUID());
        float playerHealth = player.getHealth(), petHealth = pet.getHealth();
        player.hurt(h.getLevel().damageSources().arrow(arrow, puppet), 5);
        pet.hurt(h.getLevel().damageSources().arrow(arrow, puppet), 5);
        h.assertTrue(player.getHealth() == playerHealth && pet.getHealth() == petHealth, "Released projectile lost ally protection");
        arrow.discard(); pet.discard(); puppet.discard(); player.discard(); h.succeed();
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="marionette", timeoutTicks=40)
    public static void interruptionCancelsControlWithoutStoppingPlayerMovement(GameTestHelper h) {
        ServerPlayer player = caster(h);
        Mob puppet = body(h, 2, 6);
        ManipulationChannelManager.start(player);
        player.setDeltaMovement(.3, .1, .2);
        var enemy = body(h, 8, 8);
        SchoolStates.apply(enemy, player, SchoolState.DISRUPTED, 10);
        enemy.discard();
        h.assertTrue(!ManipulationChannelManager.isChanneling(player.getUUID()) && HematicCommandManager.marionette(player) == null,
                "Disruption retained the channel or body");
        h.assertTrue(player.getDeltaMovement().lengthSqr() > .1, "Disrupted removed player movement");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(!player.isUsingItem(), "Disrupted allowed starting item use");
        puppet.discard(); player.discard(); h.succeed();
    }
}
