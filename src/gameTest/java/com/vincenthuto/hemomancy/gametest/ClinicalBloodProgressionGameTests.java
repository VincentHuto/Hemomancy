package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ClinicalBloodDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;

import java.lang.reflect.RecordComponent;
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

    @GameTest(template = "empty") public static void degreeTwoBriefingAndTeachingValidateOwnerAndEvidence(GameTestHelper h) {
        var p = player(h, 2);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel());
        alchemist.setPos(p.position()); artificer.setPos(p.position());
        h.assertTrue(FirstSeparationAssignment.canBrief(p), "First Separation is still locked behind Degree 2");
        var dialogue = alchemist.progressionDialogue(p);
        h.assertTrue(dialogue.getStartNode().options().stream()
                .anyMatch(o -> "first_separation_offer".equals(o.nextNodeId())),
                "D2 briefing offer is unreachable");
        var offer = dialogue.getNode("first_separation_offer");
        h.assertTrue(offer != null && offer.lines().size() == 2,
                "The First Separation offer must explain the assignment before acceptance");
        h.assertTrue(offer.options().stream().anyMatch(o ->
                        HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF.equals(o.eventId())
                                && "first_separation_briefing".equals(o.nextNodeId())),
                "The explained offer does not retain the briefing action");
        var decorated = DialogueHubFactory.decorate(dialogue, "alchemist", p);
        h.assertTrue(decorated.presentation().topics(DialogueCategory.QUESTS).stream()
                        .anyMatch(topic -> "first_separation_offer".equals(topic.targetNodeId())),
                "The dialogue hub does not open the authored First Separation offer");
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

	@GameTest(template = "empty") public static void firstBloodcraftProofsPromoteOnlyAfterVicarReturn(GameTestHelper h) {
		var p = player(h, 1);
		FirstBloodcraftAssignment.recordStructure(p, new ItemStack(BlockInit.hematic_iron_block.get()));
		FirstBloodcraftAssignment.recordVenousStone(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 300);
		h.assertTrue(!FirstBloodcraftAssignment.canClaim(p), "Incomplete D1 lessons allowed promotion");
		FirstBloodcraftAssignment.recordFormation(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 200);
		h.assertTrue(FirstBloodcraftAssignment.canClaim(p), "All four D1 proofs did not unlock the Vicar return");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 1,
				"Completing proofs promoted the player before speaking to the Vicar");
		h.assertTrue(FirstBloodcraftAssignment.promote(p), "The Vicar could not grant Degree 2");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 2,
				"The Vicar return did not grant Degree 2");
		h.succeed();
	}

	@GameTest(template = "empty") public static void firstSeparationSamplingProofSurvivesUsingTheVial(GameTestHelper h) {
		var p = player(h, 2);
		FirstSeparationAssignment.markBriefed(p);
		var vial = new ItemStack(ItemInit.bloody_vial.get());
		p.setItemInHand(InteractionHand.MAIN_HAND, vial);
		ItemInit.bloody_vial.get().onLeftClickEntity(vial, p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"The First Separation did not remember a successful living sample");
		p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"Using the sampled vial erased completed ledger progress");
		var restored = player(h, 2);
		restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
				p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(restored),
				"The completed sample step did not survive persisted player restoration");
		h.succeed();
	}

	@GameTest(template = "empty") public static void assignmentLedgerPacketRoundTripsEveryField(GameTestHelper h) {
		try {
			RecordComponent[] components = OpenHarbingerAssignmentLedgerPacket.class.getRecordComponents();
			Class<?>[] parameterTypes = new Class<?>[components.length];
			Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; i++) {
				parameterTypes[i] = components[i].getType();
				values[i] = ledgerPacketValue(components[i].getType(), i);
			}
			var constructor = OpenHarbingerAssignmentLedgerPacket.class.getDeclaredConstructor(parameterTypes);
			var original = (OpenHarbingerAssignmentLedgerPacket) constructor.newInstance(values);
			FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
			try {
				OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.encode(buffer, original);
				h.assertTrue(original.equals(OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.decode(buffer)),
						"The assignment ledger packet changed fields during its network round trip");
				h.assertTrue(buffer.readableBytes() == 0,
						"The assignment ledger packet left unread network data");
			} finally {
				buffer.release();
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not construct the assignment ledger packet", exception);
		}
		h.succeed();
	}

	private static Object ledgerPacketValue(Class<?> type, int index) {
		if (type == int.class) return index * 17 + 3;
		if (type == boolean.class) return index % 3 == 0;
		if (type == FirstSeparationLedgerProgress.class) {
			return new FirstSeparationLedgerProgress(true, false, true, false, true, false, true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress(
					275, true, false, true, false);
		}
		throw new IllegalArgumentException("No packet test value for " + type.getName());
	}

    @GameTest(template = "empty") public static void personalExaminationCountsUniqueSourcesAndUnlocksOrderedCollection(GameTestHelper h) {
        var p = player(h, 2);
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
