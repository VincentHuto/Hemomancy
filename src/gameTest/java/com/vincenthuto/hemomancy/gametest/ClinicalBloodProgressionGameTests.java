package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ClinicalBloodDialogue;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;

import java.util.UUID;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.*;

@GameTestHolder("clinical_validation")
@PrefixGameTestTemplate(false)
public final class ClinicalBloodProgressionGameTests {
    private static ServerPlayer player(GameTestHelper h, int degree) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "clinical-test"), false);
        var p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(p.server, connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        p.setGameMode(GameType.SURVIVAL);
        p.setPos(h.absolutePos(new BlockPos(0, 3, 0)).getCenter());
        p.setNoGravity(true);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(degree);
        return p;
    }
    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString(BloodVialItem.TAG_ENTITY_TYPE, source);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    private static void examine(GameTestHelper h, ServerPlayer p, ItemStack vial) {
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, vial);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 40; i++) p.doTick();
        h.assertTrue(BloodSampleData.identified(vial), "Examination did not identify the sample");
        p.releaseUsingItem();
    }

    @GameTest(template = "empty") public static void degreeOneBriefingAndTeachingValidateOwnerAndEvidence(GameTestHelper h) {
        var p = player(h, 1);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel());
        alchemist.setPos(p.position()); artificer.setPos(p.position());
        h.assertTrue(FirstSeparationAssignment.canBrief(p), "First Separation is still locked behind Degree 2");
        h.assertTrue(alchemist.progressionDialogue(p).getStartNode().options().stream()
                .anyMatch(o -> "alchemist_first_separation_brief".equals(o.eventId())), "D1 briefing is unreachable");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Empty-handed microscope lesson accepted");
        var firstVial = new ItemStack(ItemInit.bloody_vial.get());
        p.setItemInHand(InteractionHand.OFF_HAND, firstVial);
        ItemInit.bloody_vial.get().onLeftClickEntity(firstVial, p, net.minecraft.world.entity.EntityType.PIG.create(h.getLevel()));
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).collected, "Successful sampling did not immediately record collection");
        ClinicalBloodKnowledge.met(p, "alchemist");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, MICROSCOPE), "Wrong teacher unlocked microscopy");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Alchemist did not teach held sample");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("hematic_microscope")), "Microscope recipe not awarded");
        h.assertTrue(LiberKnowledgeHelper.hasEntry(p, ClinicalBloodKnowledge.entry("microscope")), "Liber lesson missing");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Repeat lesson rewarded twice");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Collection alone unlocked injection");
        h.succeed();
    }

    @GameTest(template = "empty") public static void personalExaminationCountsUniqueSourcesAndUnlocksOrderedCollection(GameTestHelper h) {
        var p = player(h, 1);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        state.learn(MICROSCOPE);
        var pig = vial("minecraft:pig");
        examine(h, p, pig);
        h.assertTrue(state.sourceCount() == 1, "First successful examination was not recorded");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Examined sample did not qualify for injection lesson");
        examine(h, p, pig.copy());
        h.assertTrue(state.sourceCount() == 1, "Duplicate source advanced count");
        h.assertTrue(!ClinicalBloodKnowledge.canLearn(p, CABINET), "Cabinet unlocked at first source");
        var cow = vial("minecraft:cow"); BloodSampleData.identify(cow);
        p.getInventory().add(cow.copy());
        h.assertTrue(state.sourceCount() == 1, "Traded identification granted personal proof");
        examine(h, p, cow);
        examine(h, p, vial("minecraft:sheep"));
        h.assertTrue(state.sourceCount() == 3 && ClinicalBloodKnowledge.teach(p, alchemist, CABINET), "Third source did not unlock Cabinet lesson");
        h.assertTrue(ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_cabinet")), "Known Cabinet recipe hidden");
        h.assertTrue(!ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_field_case")), "Untaught Field Case recipe exposed");
        h.succeed();
    }

    @GameTest(template = "empty") public static void cabinetProofComesFromSuccessfulPlayerTransfers(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        state.learn(CABINET);
        h.setBlock(new BlockPos(0, 2, 0), BlockInit.phlebotomists_cabinet.get());
        var be = (PhlebotomistsCabinetBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(0, 2, 0)));
        var menu = new PhlebotomistsCabinetMenu(1, p.getInventory(), be); p.containerMenu = menu;
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetWithdrawn, "Empty withdrawal counted");
        menu.setCarried(new ItemStack(Items.STONE)); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetInserted, "Rejected item counted as specimen storage");
        menu.setCarried(vial("minecraft:pig")); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetInserted && !state.cabinetWithdrawn, "Successful insertion was not recorded");
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetWithdrawn, "Successful withdrawal was not recorded");
        h.assertTrue(!state.canLearn(FIELD_REFERRAL, 2), "Missing craft and iron proof bypassed referral");
        ClinicalBloodKnowledge.crafted(new PlayerEvent.ItemCraftedEvent(p, new ItemStack(BlockInit.phlebotomists_cabinet.get()), new net.minecraft.world.SimpleContainer(9)));
        state.hematicIronObtained = true;
        ClinicalBloodKnowledge.met(p, "artificer");
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel()); artificer.setPos(p.position());
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Field Case bypassed Alchemist handoff");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, FIELD_REFERRAL), "Qualified referral denied");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Artificer did not teach referred field conversion");
        menu.removed(p);
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonicInquiryHintsUntilReferredFormalStudy(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var machine = new ItemStack(BlockInit.clairaudiograph.get());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel()); mnemonist.setPos(p.position());
        ClinicalBloodKnowledge.met(p, "mnemonist");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.inquiry.echo_hint"), "Untaught mnemonic inquiry leaked full workflow");
        for (String source : new String[]{"minecraft:pig", "minecraft:cow", "minecraft:sheep"}) state.recordExamination(source);
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, ECHO_REFERRAL), "D2 echo referral denied");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Formal study unlocked before D3");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, CLAIRAUDIOGRAPH), "Alchemist usurped mnemonic teaching");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Qualified mnemonic lesson denied");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("ambergris_cylinder")), "Cylinder recipe missing");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.clairaudiograph.lesson"), "Taught inquiry did not show workflow");
        h.succeed();
    }

    @GameTest(template = "empty") public static void injectionRequiresKnowledgeAndInterruptedStudyGrantsNothing(GameTestHelper h) {
        var p = player(h, 1);
        var sample = vial("minecraft:pig");
        p.setItemInHand(InteractionHand.MAIN_HAND, sample);
        sample.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(!p.isUsingItem() && BloodSampleData.isFilled(sample), "Untaught injection consumed or started");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, sample);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 20; i++) p.doTick();
        p.releaseUsingItem();
        for (int i = 0; i < 30; i++) p.doTick();
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).sourceCount() == 0, "Interrupted examination granted proof");
        h.assertTrue(!BloodSampleData.identified(sample), "Interrupted examination identified blood");
        h.succeed();
    }
}
