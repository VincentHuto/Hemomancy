package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.circus.*;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.PuppeteerTrialRiteController;
import com.vincenthuto.hemomancy.common.summon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class CrimsonTroupeGameTests {
    private CrimsonTroupeGameTests() {}
    private static ServerPlayer pupil(GameTestHelper helper, int degree) { return StationUpgradeGameTests.claimant(helper, degree); }
    private static ItemStack crossbar(ServerPlayer player) {
        var stack = new ItemStack(ItemInit.marionette_crossbar.get());
        MarionetteCrossbarItem.bindCrossbar(stack, player); MarionetteCrossbarItem.addThread(stack, 200);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack); return stack;
    }
    private static <T extends CircusPerformerEntity> T teacher(GameTestHelper helper, ServerPlayer pupil, EntityType<T> type) {
        T teacher = type.create(helper.getLevel()); teacher.setPos(pupil.position().add(1, 0, 0));
        helper.getLevel().addFreshEntity(teacher); return teacher;
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void realProfessorDispatchRequiresDegreeBloodAndProximity(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 3);
        var fire = teacher(helper, player, EntityInit.circus_fire_eater.get());
        try {
            DialogueOptionPacket.dispatch(player, "circus_school_teach", fire.getId());
            helper.assertTrue(!CircusApprenticeshipProgress.instructionSatisfied(player, "cinder_bellows"), "D3 learned Bellows");
            HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(4);
            HemoCapabilityAccess.getBloodVolume(player).orElseThrow().setActive(false);
            DialogueOptionPacket.dispatch(player, "circus_school_teach", fire.getId());
            helper.assertTrue(!CircusApprenticeshipProgress.instructionSatisfied(player, "cinder_bellows"), "inactive blood learned Bellows");
            HemoCapabilityAccess.getBloodVolume(player).orElseThrow().setActive(true);
            player.setPos(player.position().add(20, 0, 0));
            helper.assertTrue(DialogueOptionPacket.dispatch(player, "circus_school_teach", fire.getId()) == null, "remote lesson packet accepted");
            player.setPos(fire.position());
            var result = DialogueOptionPacket.dispatch(player, "circus_school_teach", fire.getId());
            helper.assertTrue(result != null && result.wasRewardDelivered(), "legitimate professor did not teach");
            helper.assertTrue(player.getRecipeBook().contains(Hemomancy.rloc("cinder_bellows_frame")), "frame recipe not taught");
            helper.assertTrue(!CircusApprenticeshipProgress.knows(player, "cinder_bellows") && !CircusApprenticeshipProgress.practicalComplete(player, "cinder_bellows"), "instruction forged unlock or practical");
            helper.succeed();
        } finally { fire.discard(); player.discard(); }
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ordealGateAndLegacyKnowledgeStaySeparate(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 4); var crossbar = crossbar(player);
        var fire = teacher(helper, player, EntityInit.circus_fire_eater.get());
        try {
            helper.assertTrue(!PuppeteerTrialRiteController.canBegin(player, crossbar, "cinder_bellows", 0, false), "untaught ordeal accepted");
            DialogueOptionPacket.dispatch(player, "circus_school_teach", fire.getId());
            helper.assertTrue(PuppeteerTrialRiteController.canBegin(player, crossbar, "cinder_bellows", 0, false), "taught ordeal refused");
            var hulk = PuppeteerSummonDefinitions.byName("gorebound_hulk").orElseThrow();
            HemoCapabilityAccess.requireKnownSummons(player).learn(hulk);
            helper.assertTrue(CircusApprenticeshipProgress.instructionSatisfied(player, hulk.name())
                    && !CircusApprenticeshipProgress.practicalComplete(player, hulk.name()), "legacy shape lost or fake mastery awarded");
            helper.succeed();
        } finally { fire.discard(); player.discard(); }
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void demonstrationsBypassOwnershipCombatAndFinale(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 4);
        var fire = teacher(helper, player, EntityInit.circus_fire_eater.get());
        var stage = EntityInit.cinder_bellows.get().create(helper.getLevel());
        try {
            stage.setPos(fire.position().add(2, 0, 0)); stage.setNoAi(true); stage.setInvulnerable(true);
            stage.getPersistentData().putUUID(CircusDemonstrations.TEACHER, fire.getUUID()); helper.getLevel().addFreshEntity(stage);
            var before = new EntityTickEvent.Pre(stage); CircusDemonstrations.beforeTick(before);
            helper.assertTrue(before.isCanceled() && stage.isAlive(), "stage body entered ownership reconciliation");
            helper.assertTrue(stage.hemomancy$getOwnerUUID() == null && !stage.hemomancy$isTrialSummon()
                    && !stage.canAttack(player), "stage body acquired an owner, trial, or visitor target");
            var pupilBody = PuppeteerSummonFactory.create(PuppeteerSummonDefinitions.byName("gorebound_hulk").orElseThrow(),
                    helper.getLevel(), player, UUID.randomUUID(), 0).orElseThrow();
            helper.assertTrue(!pupilBody.canAttack(stage), "owned puppet targets an invulnerable demonstration");
            pupilBody.discard();
            var faculty = teacher(helper, player, EntityInit.circus_strongman.get());
            helper.assertTrue(!faculty.participatesInFinale() && fire.participatesInFinale(), "faculty expanded the original finale cast");
            faculty.discard(); fire.discard(); CircusDemonstrations.beforeTick(new EntityTickEvent.Pre(stage));
            helper.assertTrue(stage.isRemoved(), "orphan demonstration survived"); helper.succeed();
        } finally { stage.discard(); fire.discard(); player.discard(); }
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void guestOverflowAndClonePreserveExactPayment(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 4), clone = pupil(helper, 4);
        try {
            for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().items.set(i, new ItemStack(Items.STONE, 64));
            CircusSchoolQuests.guestReward(player);
            helper.assertTrue(CircusApprenticeshipProgress.state(player).getInt("guest.pending") == 4, "full inventory lost payment");
            CircusPlayerProgress.onPlayerClone(new PlayerEvent.Clone(clone, player, true));
            helper.assertTrue(CircusApprenticeshipProgress.state(clone).getInt("guest.pending") == 4 && !CircusSchoolQuests.guestEligible(clone), "clone lost escrow or daily lock");
            for (int i = 0; i < clone.getInventory().items.size(); i++) clone.getInventory().items.set(i, new ItemStack(Items.STONE, 64));
            clone.getInventory().items.set(0, ItemStack.EMPTY);
            helper.assertTrue(CircusSchoolQuests.claim(clone) && clone.getInventory().items.get(0).is(ItemInit.puppeteering_thread.get())
                    && clone.getInventory().items.get(0).getCount() == 4 && !CircusSchoolQuests.claim(clone), "reserved payment duplicated or changed");
            helper.succeed();
        } finally { player.discard(); clone.discard(); }
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void primerRequiresARealActiveBodyAndOneInspectedCrossbar(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 4); Mob body = null;
        try {
            var definition = PuppeteerSummonDefinitions.byName("veinwing_vulture").orElseThrow();
            HemoCapabilityAccess.requireKnownSummons(player).learn(definition);
            var crossbar = crossbar(player); MarionetteCrossbarItem.prepareSelectedSummon(crossbar, player, definition.name());
            helper.assertTrue(CircusSchoolQuests.inspect(player), "charged Crossbar rejected");
            UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
            CircusSchoolQuests.observeControl(player, id, "command", definition.name());
            helper.assertTrue(!CircusApprenticeshipProgress.state(player).getBoolean("primer.command"), "command without a body counted");
            body = PuppeteerSummonFactory.create(definition, helper.getLevel(), player, id, 0).orElseThrow(); helper.getLevel().addFreshEntity(body);
            CircusSchoolQuests.observeControl(player, id, "call", definition.name());
            PuppeteerCrossbarCommands.setMode(player, crossbar, PuppeteerCommandMode.GUARD);
            CircusSchoolQuests.observeControl(player, UUID.randomUUID(), "recall", definition.name());
            helper.assertTrue(!CircusApprenticeshipProgress.state(player).getBoolean("primer.recall"), "another Crossbar completed primer");
            CircusSchoolQuests.observeControl(player, id, "recall", definition.name());
            helper.assertTrue(CircusSchoolQuests.report(player) && !CircusSchoolQuests.report(player), "primer reward missing or repeated");
            helper.succeed();
        } finally { if (body != null) body.discard(); player.discard(); }
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void newBodySaveAndAllyFilteringUseTheSharedLifecycle(GameTestHelper helper) {
        ServerPlayer player = pupil(helper, 4); var id = UUID.randomUUID();
        var definition = PuppeteerSummonDefinitions.byName("cinder_bellows").orElseThrow();
        var body = PuppeteerSummonFactory.create(definition, helper.getLevel(), player, id, 0).orElseThrow();
        var other = PuppeteerSummonFactory.create(PuppeteerSummonDefinitions.byName("gorebound_hulk").orElseThrow(), helper.getLevel(), player, id, 0).orElseThrow();
        var loaded = EntityInit.cinder_bellows.get().create(helper.getLevel());
        try {
            helper.assertTrue(!body.canAttack(player) && !body.canAttack(other), "Bellows targets allies");
            var tag = new CompoundTag(); body.saveWithoutId(tag); loaded.load(tag);
            helper.assertTrue(id.equals(loaded.hemomancy$getCrossbarUUID()) && player.getUUID().equals(loaded.hemomancy$getOwnerUUID())
                    && loaded.getMaxHealth() == 30 && loaded.getBreathCycle() == 0, "new body loses ownership or resumes an unsafe breath after reload");
            helper.succeed();
        } finally { body.discard(); other.discard(); loaded.discard(); player.discard(); }
    }
}
