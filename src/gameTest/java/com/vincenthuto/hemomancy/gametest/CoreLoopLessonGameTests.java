package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CoreLoopLessons;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.TitrationBandolierGift;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The core-loop lessons (round two P10) in play: each outpost speaker's right click serves its lessons as unread lore
 * cards at their Degrees and none outside them, a lesson's event opens its Liber pages once and changes nothing else,
 * and the event is refused from the wrong speaker, with no speaker, out of reach, from a dead player, below or above
 * its Degrees, with sleeping blood and while purifying. The Alchemist's Temper lesson also gives one Titration
 * Bandolier (CQ-63 A), checked on its own.
 */
@GameTestHolder("core_loop_validation")
@PrefixGameTestTemplate(false)
public final class CoreLoopLessonGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "core_loop_lessons";
	private static final BlockPos NPC = new BlockPos(2, 2, 2);
	private static final BlockPos STAND = new BlockPos(4, 2, 2);

	private CoreLoopLessonGameTests() {
	}

	// --- Each speaker's cards --------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theHermitTeachesAwakeBloodUpToTheFourthDegree(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.HERMIT, 1, List.of(5));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theVicarTeachesHisLessonsAtTheirDegrees(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.VICAR, 3, List.of(0));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theAlchemistTeachesTheirLessonsAtTheirDegrees(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.ALCHEMIST, 3, List.of(0));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theMnemonistTeachesTheirLessonsAtTheirDegrees(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.MNEMONIST, 3, List.of(0));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theArtificerTeachesHisLessonsAtTheirDegrees(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.ARTIFICER, 2, List.of(0));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theVeinMasonTeachesFromTheFourthDegree(GameTestHelper h) {
		checkCards(h, CoreLoopLessons.VEIN_MASON, 4, List.of(3));
	}

	/**
	 * At {@code full} every lesson of the speaker is an unread lore card in Conversation and none is Quest Work; at each
	 * of {@code none}, and at {@code full} with sleeping blood or while purifying, the speaker lists none.
	 */
	private static void checkCards(GameTestHelper h, String speaker, int full, List<Integer> none) {
		Entity npc = spawn(h, speaker);
		try {
			try (CapturedPlayer captured = player(h, full, true)) {
				DialogueTree hub = open(h, captured, npc);
				List<DialogueTopic> conversation = hub.presentation().topics(DialogueCategory.CONVERSATION);
				for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
					DialogueTopic card = conversation.stream().filter(topic -> topic.titleKey().equals(lesson.rootOption()))
							.findFirst().orElse(null);
					h.assertTrue(card != null, lesson.event() + " is not in " + speaker + "'s Conversation box at Degree "
							+ full);
					h.assertTrue(card.lore() && card.unread(), lesson.event() + " is not an unread lore card");
					h.assertTrue(hub.getNode(lesson.node()) != null, lesson.event() + " sends no lesson node");
					h.assertTrue(hub.presentation().topics(DialogueCategory.QUESTS).stream()
							.noneMatch(topic -> topic.titleKey().equals(lesson.rootOption())), lesson.event() + " is quest work");
				}
			}
			for (int degree : none) {
				try (CapturedPlayer captured = player(h, degree, true)) {
					assertNoLessons(h, open(h, captured, npc), speaker, "at Degree " + degree);
				}
			}
			try (CapturedPlayer asleep = player(h, full, false)) {
				assertNoLessons(h, open(h, asleep, npc), speaker, "with sleeping blood");
			}
			try (CapturedPlayer purifying = player(h, full, true)) {
				HemoCapabilityAccess.getUnstainedProgress(purifying.player).setBegunPurification(true);
				assertNoLessons(h, open(h, purifying, npc), speaker, "while purifying");
			}
		} finally {
			npc.discard();
		}
		h.succeed();
	}

	// --- The events -------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theHermitsLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.HERMIT);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theVicarsLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.VICAR);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theAlchemistsLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.ALCHEMIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theMnemonistsLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.MNEMONIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theArtificersLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.ARTIFICER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theVeinMasonsLessonsOpenTheirPagesOnceAndNothingElse(GameTestHelper h) {
		checkEvents(h, CoreLoopLessons.VEIN_MASON);
	}

	/** Each lesson's event, chosen at its first Degree, opens its pages; told again, it opens nothing new. */
	private static void checkEvents(GameTestHelper h, String speaker) {
		Entity npc = spawn(h, speaker);
		try {
			for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
				try (CapturedPlayer captured = player(h, lesson.minDegree(), true)) {
					ServerPlayer player = captured.player;
					// The guard reads these; reading creates them, so they are not counted as the lesson's doing.
					ProgressionGates.isUnstained(player);
					HemoCapabilityAccess.getLiberKnowledge(player);
					// The Temper lesson's one Bandolier is checked by its own tests below.
					if (isBandolierLesson(lesson)) HemoCapabilityAccess.getBloodTemper(player).markBandolierGiven();
					CompoundTag before = PlayerFingerprints.saved(player);
					Set<String> earnedBefore = PlayerFingerprints.earned(player);
					DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
					for (ResourceLocation page : lesson.pages()) {
						h.assertTrue(LiberKnowledgeHelper.hasEntry(player, page), lesson.event() + " did not open " + page);
					}
					String changed = PlayerFingerprints.diff(before, PlayerFingerprints.saved(player));
					h.assertTrue(onlyLiber(changed), lesson.event() + " changed more than Liber knowledge: " + changed);
					h.assertTrue(PlayerFingerprints.earned(player).equals(earnedBefore), lesson.event() + " earned something");
					CompoundTag once = PlayerFingerprints.saved(player);
					DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
					h.assertTrue(PlayerFingerprints.diff(once, PlayerFingerprints.saved(player)).equals("{}"),
							lesson.event() + " told twice opened something new");
				}
			}
		} finally {
			npc.discard();
		}
		h.succeed();
	}

	// --- The Titration Bandolier (CQ-63 A) ---------------------------------------------------------------------

	/** The Alchemist's Temper lesson hands over one Bandolier, and only one: a lost one is not given again. */
	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theAlchemistsTemperLessonGivesTheBandolierOnce(GameTestHelper h) {
		Entity npc = spawn(h, CoreLoopLessons.ALCHEMIST);
		CoreLoopLessons.Lesson lesson = bandolierLesson();
		try (CapturedPlayer captured = player(h, lesson.minDegree(), true)) {
			ServerPlayer player = captured.player;
			Item bandolier = ItemInit.titration_bandolier.get();
			DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
			h.assertTrue(player.getInventory().countItem(bandolier) == 1, "the Temper lesson gave "
					+ player.getInventory().countItem(bandolier) + " Bandoliers, not one");
			h.assertTrue(HemoCapabilityAccess.getBloodTemper(player).bandolierGiven(), "the gift was not recorded");
			DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
			h.assertTrue(player.getInventory().countItem(bandolier) == 1, "a second telling gave another Bandolier");
			player.getInventory().clearOrCountMatchingItems(stack -> stack.is(bandolier), -1, player.inventoryMenu.getCraftSlots());
			DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
			h.assertTrue(player.getInventory().countItem(bandolier) == 0 && !OwedRewards.carriesOrOwed(player, bandolier),
					"a lost Bandolier was given again");
		} finally {
			npc.discard();
		}
		h.succeed();
	}

	/** Refused tellings give no Bandolier, and a full inventory keeps it owed until there is room. */
	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void theBandolierWaitsForRoomAndIsNotGivenOutOfTurn(GameTestHelper h) {
		Entity npc = spawn(h, CoreLoopLessons.ALCHEMIST);
		CoreLoopLessons.Lesson lesson = bandolierLesson();
		Item bandolier = ItemInit.titration_bandolier.get();
		try {
			try (CapturedPlayer early = player(h, lesson.minDegree() - 1, true)) {
				DialogueOptionPacket.accept(early.player, lesson.event(), npc.getId());
				h.assertTrue(!OwedRewards.carriesOrOwed(early.player, bandolier)
						&& !HemoCapabilityAccess.getBloodTemper(early.player).bandolierGiven(), "a Degree 1 player got the Bandolier");
			}
			try (CapturedPlayer asleep = player(h, lesson.minDegree(), false)) {
				DialogueOptionPacket.accept(asleep.player, lesson.event(), npc.getId());
				h.assertTrue(!OwedRewards.carriesOrOwed(asleep.player, bandolier), "sleeping blood got the Bandolier");
			}
			try (CapturedPlayer full = player(h, lesson.minDegree(), true)) {
				ServerPlayer player = full.player;
				for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
					player.getInventory().items.set(slot, new ItemStack(Items.DIRT, 64));
				}
				DialogueOptionPacket.accept(player, lesson.event(), npc.getId());
				h.assertTrue(player.getInventory().countItem(bandolier) == 0 && OwedRewards.carriesOrOwed(player, bandolier),
						"a full inventory did not keep the Bandolier owed");
				player.getInventory().items.set(0, ItemStack.EMPTY);
				PendingDelivery.deliver(player, OwedRewards.CLAIM);
				h.assertTrue(player.getInventory().countItem(bandolier) == 1, "the owed Bandolier did not land once there was room");
			}
		} finally {
			npc.discard();
		}
		h.succeed();
	}

	/** A lost Bandolier is replaced by a plain recipe. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH)
	public static void aLostBandolierCanBeCrafted(GameTestHelper h) {
		var recipe = h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("titration_bandolier"));
		h.assertTrue(recipe.isPresent(), "no recipe makes the Titration Bandolier");
		h.assertTrue(recipe.get().value().getResultItem(h.getLevel().registryAccess()).is(ItemInit.titration_bandolier.get()),
				"the recipe does not make the Bandolier");
		h.succeed();
	}

	private static boolean isBandolierLesson(CoreLoopLessons.Lesson lesson) {
		return lesson.speaker().equals(CoreLoopLessons.ALCHEMIST) && lesson.id().equals(TitrationBandolierGift.LESSON);
	}

	private static CoreLoopLessons.Lesson bandolierLesson() {
		return CoreLoopLessons.forSpeaker(CoreLoopLessons.ALCHEMIST).stream().filter(CoreLoopLessonGameTests::isBandolierLesson)
				.findFirst().orElseThrow();
	}

	// --- Refusals ---------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40, batch = BATCH)
	public static void aLessonIsRefusedOutOfTurn(GameTestHelper h) {
		List<String> problems = new ArrayList<>();
		for (String speaker : List.of(CoreLoopLessons.HERMIT, CoreLoopLessons.VICAR, CoreLoopLessons.ALCHEMIST,
				CoreLoopLessons.MNEMONIST, CoreLoopLessons.ARTIFICER, CoreLoopLessons.VEIN_MASON)) {
			Entity npc = spawn(h, speaker);
			Entity other = spawn(h, speaker.equals(CoreLoopLessons.VICAR) ? CoreLoopLessons.ALCHEMIST : CoreLoopLessons.VICAR);
			try {
				for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
					int degree = lesson.minDegree();
					refused(h, problems, lesson, "sent to another speaker", degree, true, captured ->
							DialogueOptionPacket.accept(captured.player, lesson.event(), other.getId()));
					refused(h, problems, lesson, "with no speaker", degree, true, captured ->
							DialogueOptionPacket.accept(captured.player, lesson.event(), 0));
					refused(h, problems, lesson, "from twelve blocks", degree, true, captured -> {
						BlockPos far = h.absolutePos(STAND.east(12));
						captured.player.moveTo(far.getX() + 0.5D, far.getY(), far.getZ() + 0.5D);
						DialogueOptionPacket.accept(captured.player, lesson.event(), npc.getId());
					});
					refused(h, problems, lesson, "by a dead player", degree, true, captured -> {
						captured.player.setHealth(0.0F);
						DialogueOptionPacket.accept(captured.player, lesson.event(), npc.getId());
					});
					int outside = lesson.minDegree() > 0 ? lesson.minDegree() - 1 : lesson.maxDegree() + 1;
					refused(h, problems, lesson, "at Degree " + outside, outside, true, captured ->
							DialogueOptionPacket.accept(captured.player, lesson.event(), npc.getId()));
					refused(h, problems, lesson, "with sleeping blood", degree, false, captured ->
							DialogueOptionPacket.accept(captured.player, lesson.event(), npc.getId()));
					refused(h, problems, lesson, "while purifying", degree, true, captured -> {
						HemoCapabilityAccess.getUnstainedProgress(captured.player).setBegunPurification(true);
						DialogueOptionPacket.accept(captured.player, lesson.event(), npc.getId());
					});
				}
			} finally {
				npc.discard();
				other.discard();
			}
		}
		h.assertTrue(problems.isEmpty(), String.join("; ", problems));
		h.succeed();
	}

	private interface Attempt {
		void run(CapturedPlayer captured);
	}

	private static void refused(GameTestHelper h, List<String> problems, CoreLoopLessons.Lesson lesson, String how,
			int degree, boolean bloodActive, Attempt attempt) {
		try (CapturedPlayer captured = player(h, degree, bloodActive)) {
			attempt.run(captured);
			for (ResourceLocation page : lesson.pages()) {
				if (LiberKnowledgeHelper.hasEntry(captured.player, page)) {
					problems.add(lesson.event() + " " + how + " opened " + page);
				}
			}
		}
	}

	// --- Setup ------------------------------------------------------------------------------------------------

	private static CapturedPlayer player(GameTestHelper h, int degree, boolean bloodActive) {
		CapturedPlayer captured = CapturedPlayer.create(h).online();
		BlockPos stand = h.absolutePos(STAND);
		captured.player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		HemoCapabilityAccess.getInitiatoryDegree(captured.player).setDegreeNumber(degree);
		var blood = HemoCapabilityAccess.getBloodVolume(captured.player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(bloodActive);
		return captured;
	}

	private static Entity spawn(GameTestHelper h, String speaker) {
		EntityType<?> type = switch (speaker) {
			case CoreLoopLessons.HERMIT -> EntityInit.harbinger_hermit.get();
			case CoreLoopLessons.VICAR -> EntityInit.harbinger_vicar.get();
			case CoreLoopLessons.ALCHEMIST -> EntityInit.harbinger_alchemist.get();
			case CoreLoopLessons.MNEMONIST -> EntityInit.harbinger_mnemonist.get();
			case CoreLoopLessons.ARTIFICER -> EntityInit.harbinger_artificer.get();
			default -> EntityInit.harbinger_cicatrix_anchorite.get();
		};
		Entity entity = h.spawn(type, NPC);
		entity.setNoGravity(true);
		if (entity instanceof Mob mob) mob.setNoAi(true);
		return entity;
	}

	/** Interacts like a right click and returns the hub the speaker opened. */
	private static DialogueTree open(GameTestHelper h, CapturedPlayer captured, Entity speaker) {
		int before = captured.sent(OpenDialoguePacket.class).size();
		speaker.interact(captured.player, InteractionHand.MAIN_HAND);
		List<OpenDialoguePacket> opened = captured.sent(OpenDialoguePacket.class);
		h.assertTrue(opened.size() > before, speaker.getType() + " opens a dialogue");
		return opened.getLast().tree();
	}

	private static void assertNoLessons(GameTestHelper h, DialogueTree hub, String speaker, String when) {
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
			h.assertTrue(hub.getNode(lesson.node()) == null, speaker + " offers " + lesson.event() + " " + when);
		}
	}

	/**
	 * The saved-data diff names one change, the Liber knowledge attachment. Each change is a {@code key=value} entry and
	 * the attachment's SNBT holds no {@code =}, so one {@code =} means one change.
	 */
	private static boolean onlyLiber(String diff) {
		return diff.startsWith("{neoforge:attachments.hemomancy:liber_knowledge=")
				&& diff.chars().filter(c -> c == '=').count() == 1;
	}
}
