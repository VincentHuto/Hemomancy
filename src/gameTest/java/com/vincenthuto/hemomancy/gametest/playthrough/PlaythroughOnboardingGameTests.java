package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Band d0_onboarding: a brand-new Harbinger player from the Blood Temple to Degree 1. The player meets the Hermit,
 * takes his blessing through dialogue, touches the Mortal Display (charm, woken blood, the Harbinger Lodestone),
 * optionally sees the Hermit's farewell, tries the lodestone, and chooses initiation or release at the Vicar. The
 * 200-tick ceremony is ticked for real; nothing jumps game time. Everything under test goes through the entry point a
 * client triggers ({@code interact}, the dialogue payload handlers, {@code useItemOn}, {@code useItem}, a held
 * channel, real damage, respawn and relog). Preconditions that are not under test are marked {@code // ARRANGE}.
 * <p>
 * Tests that need a player inside the level (a founder whose projection must hit a recruit, or an entity id that the
 * dialogue guards look up) join it with {@link #joinLevel} and run in their own batch so the extra players do not
 * leak into the parallel tests of the main batch.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughOnboardingGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_onboarding";
	private static final String LEVEL_BATCH = "playthrough_onboarding_level";

	// Events
	private static final String HEART_OFFERED = "hermit_heart_offered";
	private static final String ACCEPT_GUIDANCE = "hermit_accept_guidance";
	private static final String FAREWELL = "hermit_farewell_die";
	private static final String BEGIN = "vicar_begin_initiation";
	private static final String RELEASE = "vicar_release_charm";

	// Keys the player is shown
	private static final String WHAT_HEART = "hemomancy.dialogue.hermit.option.what_heart";
	private static final String ACCEPT_HEART = "hemomancy.dialogue.hermit.option.accept_heart";
	private static final String SAID_HEART_OFFERED = "hemomancy.dialogue.event.hermit_heart_offered";
	private static final String SAID_ACCEPT_GUIDANCE = "hemomancy.dialogue.event.hermit_accept_guidance";
	private static final String SAID_FAREWELL = "hemomancy.dialogue.event.hermit_farewell_die";
	private static final String SAID_PASSING = "hemomancy.dialogue.event.hermit_passing";
	private static final String CLAIMED_YOU_LINE = "hemomancy.hermit.uninitiated.line2";
	private static final String CHARM_EQUIPPED = "hemomancy.mortal_display.charm_equipped";
	private static final String SPEAK_WITH_HERMIT = "message.hemomancy.mortal_display_block.speak_with_this_temple_s_hermit";
	private static final String ALREADY_CLAIMED = "message.hemomancy.mortal_display_block.the_heart_has_already_been_claimed";
	private static final String NO_LIVING_OATH = "message.hemomancy.mortal_display_block.this_heart_has_no_living_oath";
	private static final String LODESTONE_NOT_FOUND = "item.hemomancy.harbinger_lodestone.not_found";
	private static final String LODESTONE_ATTUNED = "item.hemomancy.harbinger_lodestone.attuned";
	private static final String RELEASED = "hemomancy.initiation.released";
	private static final String OFFICIANT_BUSY = "hemomancy.initiation.officiant_busy";
	private static final String DEGREE_LINE = "hemomancy.initiation.degree";
	private static final String WELCOME = "hemomancy.initiation.welcome";
	private static final String CONTROLS = "hemomancy.tutorial.current_controls";
	private static final String BLOODLINE_LINE = "hemomancy.initiation.bloodline";
	private static final String NEWCOMER_TITLE = "hemomancy.dialogue.newcomer.ask_path";
	private static final String NEWCOMER_NODE = "newcomer_path";
	private static final String INITIATION_NODE = "formal_introduction";

	/** TempleOathRules keeps this key private; the test reads both homes it is written to. */
	private static final String CLAIMED_HEART_KEY = "hemomancy.claimed_heart_hermit";
	private static final String LODESTONE_CHECKED = "LodestoneCheckedDimension";
	private static final String LODESTONE_TARGET_X = "LodestoneTargetX";

	private static final String AWAKENING = Hemomancy.rloc("hemomancy/the_first_awakening").getPath();
	private static final String STARTER_CLAIMED = Hemomancy.rloc("hemomancy/initiation_blood_claimed").getPath();
	private static final String DEGREE_ONE = HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE.getPath();
	private static final String LEDGER_GRANTED = HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED.getPath();
	private static final ResourceLocation HERMIT_BREAD = Hemomancy.rloc("hermit/bread");

	private static final int CEREMONY = EarlyInitiation.DURATION + 2;

	private PlaythroughOnboardingGameTests() {
	}

	// --- Layout --------------------------------------------------------------------------------------------------

	/** Temple A: Display at (8, 6), Hermit at (10, 6), stand at (6, 6). */
	private static PlaythroughSites.BloodTemple templeA(GameTestHelper helper) {
		return PlaythroughSites.bloodTemple(helper, PlaythroughSites.stand(helper, 8, 6));
	}

	/** Temple B: Display at (8, 16), Hermit at (10, 16), stand at (6, 16). */
	private static PlaythroughSites.BloodTemple templeB(GameTestHelper helper) {
		return PlaythroughSites.bloodTemple(helper, PlaythroughSites.stand(helper, 8, 16));
	}

	private static HarbingerVicarEntity vicar(GameTestHelper helper) {
		return PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 20, 11));
	}

	// --- H: the happy road ---------------------------------------------------------------------------------------

	/**
	 * A fresh player right-clicks the temple's Hermit. He offers his heart (what_heart -> heart_lore -> accept_heart),
	 * every key in the hub resolves, nothing is a dead end, the Trade box carries his bread card, and his sigil asks
	 * for attention.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void freshPlayerIsOfferedTheHermitsHeart(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		PlaythroughStates.freshD0(player); // ARRANGE: asserts the fresh baseline
		HarbingerHermitEntity hermit = temple.hermit();

		DialogueView view = player.talkTo(hermit);
		PlaythroughLog.note(helper, "fresh Hermit: " + view.describe());
		view.assertLangKeys(helper, "the Hermit to a fresh player").assertNoDeadEnds(helper, "the Hermit to a fresh player");
		helper.assertTrue(view.hasNode("heart_lore"), "the Hermit's heart_lore node is reachable: " + view.describe());
		boolean whatHeartLeadsToLore = view.optionByText(WHAT_HEART).map(o -> "heart_lore".equals(o.nextNodeId())).orElse(false)
				|| view.topics().stream().anyMatch(t -> WHAT_HEART.equals(t.titleKey()) && "heart_lore".equals(t.targetNodeId()));
		helper.assertTrue(whatHeartLeadsToLore, "'" + WHAT_HEART + "' leads to heart_lore: " + view.describe());
		boolean loreOffersHeart = view.node("heart_lore").map(node -> node.options().stream()
				.anyMatch(o -> HEART_OFFERED.equals(o.eventId()) && ACCEPT_HEART.equals(o.text()))).orElse(false);
		helper.assertTrue(loreOffersHeart, "heart_lore offers accept_heart (" + HEART_OFFERED + ")");
		helper.assertTrue(view.offers(HEART_OFFERED), "the heart can be chosen: " + view.describe());
		helper.assertTrue(view.tradeCard(HERMIT_BREAD).isPresent(), "the Trade box shows " + HERMIT_BREAD + ": "
				+ view.tradeCards().stream().map(card -> card.offerId().toString()).toList());
		DialogueAttention sigil = Dialogue.attention(hermit, player.player());
		helper.assertTrue(sigil == DialogueAttention.NOTICE, "the Hermit's sigil is NOTICE for a fresh player, not " + sigil);
		helper.succeed();
	}

	/**
	 * The blessing through dialogue, then an empty-hand touch on the Mortal Display: the charm sits in the vascular
	 * slot, the blood wakes, the_first_awakening is earned, exactly one Harbinger Lodestone is given, charm_equipped is
	 * said, the Display is claimed by the player, and the claimed Hermit is recorded in both the root and the
	 * PlayerPersisted homes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void blessingThenDisplayTouchAwakensBlood(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		HarbingerHermitEntity hermit = temple.hermit();

		player.talkTo(hermit);
		player.choose(hermit, HEART_OFFERED).said(SAID_HEART_OFFERED);
		helper.assertTrue(hermit.getUUID().equals(TempleOathRules.blessedHermit(player.player())),
				"the blessing names this Hermit");
		helper.assertTrue(player.player().getMainHandItem().isEmpty(), "the touch is empty-handed");

		Outcome touch = player.useOn(temple.display());
		touch.said(CHARM_EQUIPPED).earned(AWAKENING).gave(ItemInit.harbinger_lodestone.get(), 1);
		helper.assertTrue(player.hasCharm(), "the charm sits in slot 5; " + touch);
		helper.assertTrue(player.bloodActive(), "the blood is awake; " + touch);
		helper.assertTrue(player.count(ItemInit.harbinger_lodestone.get()) == 1, "exactly one lodestone is carried");
		helper.assertTrue(temple.displayEntity(helper.getLevel()).isClaimedBy(player.uuid()), "the Display is claimed by the player");
		assertClaimedInBothHomes(helper, player, hermit.getUUID());
		helper.succeed();
	}

	/**
	 * After the claim the player returns: "How do I begin?" -> accept guidance -> farewell. Both lines are said, the
	 * Hermit stops talking while he dissolves, shows no sigil, and is gone within the farewell's 250 ticks.
	 */
	@GameTest(template = EMPTY, timeoutTicks = HarbingerHermitEntity.FAREWELL_DEATH_DURATION + 120, batch = BATCH)
	public static void hermitFarewellDissolvesTheHermit(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		HarbingerHermitEntity hermit = temple.hermit();
		claimAtTemple(player, temple);

		DialogueView view = player.talkTo(hermit);
		PlaythroughLog.note(helper, "claimed Hermit: " + view.describe());
		view.assertLangKeys(helper, "the Hermit after the claim").assertNoDeadEnds(helper, "the Hermit after the claim");
		helper.assertTrue(view.offers(ACCEPT_GUIDANCE), "the Hermit offers guidance after the claim: " + view.describe());
		player.choose(hermit, ACCEPT_GUIDANCE).said(SAID_ACCEPT_GUIDANCE);
		helper.assertTrue(view.offers(FAREWELL), "guidance leads to the farewell: " + view.describe());
		player.choose(hermit, FAREWELL).said(SAID_FAREWELL).said(SAID_PASSING);
		helper.assertTrue(hermit.isFarewellDying(), "the farewell starts the dissolution");
		helper.assertTrue(player.tryTalkTo(hermit).isEmpty(), "a dissolving Hermit opens no dialogue");
		DialogueAttention sigil = Dialogue.attention(hermit, player.player());
		helper.assertTrue(sigil == DialogueAttention.NONE, "a dissolving Hermit shows no sigil, not " + sigil);

		PlaythroughClock.start(helper)
				.until(HarbingerHermitEntity.FAREWELL_DEATH_DURATION + 20, hermit::isRemoved,
						"the Hermit is removed after his farewell")
				.then(() -> PlaythroughLog.note(helper, "the Hermit dissolved; the player keeps charm="
						+ player.hasCharm() + " blood=" + player.bloodActive()))
				.run();
	}

	/** The Harbinger Lodestone used in the flat test world: not_found is said and the checked dimension is written. */
	@GameTest(template = EMPTY, timeoutTicks = 80, batch = BATCH)
	public static void lodestoneSaysNotFoundInTheTestWorld(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		player.wield(ItemInit.harbinger_lodestone.get());
		Outcome use = player.use(InteractionHand.MAIN_HAND);
		CompoundTag tag = player.player().getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		String dimension = player.player().level().dimension().location().toString();
		PlaythroughLog.note(helper, "lodestone use: " + use + " data=" + tag);
		helper.assertTrue(player.player().getMainHandItem().is(ItemInit.harbinger_lodestone.get()), "the lodestone is still held");
		helper.assertTrue(dimension.equals(tag.getString(LODESTONE_CHECKED)), "the lodestone records the checked dimension: " + tag);
		helper.assertTrue(use.chat().stream().noneMatch(line -> line.has(LODESTONE_ATTUNED)),
				"the flat test world has no outpost to attune to; " + use);
		use.said(LODESTONE_NOT_FOUND);
		helper.assertFalse(tag.contains(LODESTONE_TARGET_X), "no target is written when none is found: " + tag);
		helper.succeed();
	}

	/**
	 * A player who claimed the heart talks to the Vicar at D0: formal_introduction offers accept and remove, there is
	 * no newcomer card (they met the Hermit), and every key resolves.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void vicarOffersInitiationToCharmBearer(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		player.approach(vicar);
		DialogueView view = player.talkTo(vicar);
		PlaythroughLog.note(helper, "charm-bearer Vicar: " + view.describe());
		PlaythroughLog.note(helper, "initiation card: category=" + view.category(BEGIN) + " state=" + view.questState(BEGIN)
				+ " attention=" + view.attention(BEGIN) + " sigil=" + Dialogue.attention(vicar, player.player()));
		view.assertLangKeys(helper, "the Vicar to a charm-bearer").assertNoDeadEnds(helper, "the Vicar to a charm-bearer");
		helper.assertTrue(view.hasNode(INITIATION_NODE), "the formal introduction is reachable: " + view.describe());
		List<String> choices = view.node(INITIATION_NODE).map(node -> node.options().stream()
				.map(DialogueOption::eventId).filter(id -> id != null).toList()).orElse(List.of());
		helper.assertTrue(choices.contains(BEGIN) && choices.contains(RELEASE),
				"the formal introduction offers accept and remove: " + choices);
		helper.assertTrue(view.offers(BEGIN) && view.offers(RELEASE), "both choices are enabled: " + view.describe());
		helper.assertFalse(hasNewcomerCard(view), "a player who met the Hermit sees no newcomer card: " + view.describe());
		helper.succeed();
	}

	/**
	 * The initiation chosen through dialogue and 200 real player ticks: D1, the conduit, four flasks and the ledger,
	 * both utilities known and equipped, the two Liber entries, the three advancements, the Vicar's AI and flag
	 * restored.
	 */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 100, batch = BATCH)
	public static void initiationCeremonyTicksToDegreeOne(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		PlaythroughClock.start(helper)
				.then(() -> {
					beginCeremony(player, vicar);
					helper.assertTrue(vicar.isInitiating() && vicar.isNoAi(), "the Vicar stands still to officiate");
				})
				.step(player.tickPlayer(CEREMONY))
				.then(() -> {
					Outcome ceremony = player.last();
					PlaythroughLog.note(helper, "ceremony: " + ceremony);
					ceremony.degreeIs(1).earned(DEGREE_ONE).earned(STARTER_CLAIMED).earned(LEDGER_GRANTED)
							.said(DEGREE_LINE).said(WELCOME).said(CONTROLS)
							.gave(ItemInit.sanguine_conduit.get(), 1).gave(ItemInit.bloody_flask.get(), 4)
							.gave(ItemInit.harbinger_assignment_ledger.get(), 1);
					var known = HemoCapabilityAccess.getKnownManipulations(player.player());
					for (String utility : List.of("blood_absorption", "blood_projection")) {
						helper.assertTrue(player.knows(utility), utility + " is known at D1");
						helper.assertTrue(known.getEquippedManipNames().contains(utility),
								utility + " is equipped at D1: " + known.getEquippedManipNames());
					}
					helper.assertTrue(LiberKnowledgeHelper.hasEntry(player.player(), LiberEntryDefinitions.HEMOMANCY)
							&& LiberKnowledgeHelper.hasEntry(player.player(), LiberEntryDefinitions.THE_HARBINGERS),
							"the Liber's Hemomancy and Harbingers entries are unlocked");
					helper.assertTrue(!vicar.isInitiating() && !vicar.isNoAi(), "the Vicar's AI and flag are restored");
					helper.assertTrue(player.count(ItemInit.harbinger_lodestone.get()) == 1, "the lodestone is kept");
				})
				.run();
	}

	// --- E: edge cases ---------------------------------------------------------------------------------------------

	/** Touching the Display before speaking with the Hermit is refused with the Hermit line, and nothing is claimed. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void displayBeforeBlessingIsRefused(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());

		Outcome touch = player.useOn(temple.display());
		touch.said(SPEAK_WITH_HERMIT).didNotSay(CHARM_EQUIPPED).gaveNothing();
		helper.assertFalse(player.hasCharm(), "no charm without the blessing; " + touch);
		helper.assertFalse(player.bloodActive(), "the blood stays asleep; " + touch);
		helper.assertFalse(temple.displayEntity(helper.getLevel()).isClaimed(), "the Display is not claimed");
		helper.assertTrue(touch.earned().isEmpty(), "no advancement; " + touch);
		helper.succeed();
	}

	/** A fresh player who goes to the Vicar first, with no charm, sees the newcomer card and no initiation. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void vicarFirstWithoutCharmShowsNewcomerCard(GameTestHelper helper) {
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 17, 11));
		PlaythroughStates.freshD0(player); // ARRANGE: asserts the fresh baseline

		DialogueView view = player.talkTo(vicar);
		PlaythroughLog.note(helper, "fresh Vicar: " + view.describe());
		view.assertLangKeys(helper, "the Vicar to a newcomer").assertNoDeadEnds(helper, "the Vicar to a newcomer");
		helper.assertTrue(hasNewcomerCard(view), "the newcomer card '" + NEWCOMER_TITLE + "' is shown: " + view.describe());
		helper.assertFalse(view.offers(BEGIN) || view.offers(RELEASE), "no initiation without the charm: " + view.describe());
		helper.assertFalse(view.hasNode(INITIATION_NODE), "no formal introduction without the charm");
		helper.succeed();
	}

	/**
	 * Damage mid-ceremony cancels it: still D0 with the charm and woken blood, no rewards or advancements, the Vicar
	 * moves again. The option is offered again and a second ceremony completes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 160, batch = BATCH)
	public static void ceremonyCancelledByDamage(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		PlaythroughClock.start(helper)
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(40))
				.then(() -> {
					Outcome hurt = player.damage(1.0F);
					helper.assertTrue(hurt.notes().contains("hurt"), "the damage lands; " + hurt);
					player.tickOnce();
					assertCeremonyCancelled(helper, player, vicar, "after damage");
					helper.assertTrue(hurt.earned().isEmpty() && hurt.drops().isEmpty(), "no reward on cancel; " + hurt);
				})
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(CEREMONY))
				.then(() -> {
					Outcome second = player.last();
					second.degreeIs(1).earned(DEGREE_ONE).gave(ItemInit.bloody_flask.get(), 4);
				})
				.run();
	}

	/**
	 * Three ways a ceremony is lost without completing: a teleport of 5 blocks (beyond the 4-block departure radius
	 * the code checks; the band map's "more than 2 blocks" is not what EarlyInitiation measures), a relog, and the
	 * Vicar removed. Each leaves the player D0 with no rewards, and the Vicar's AI is restored where he still exists. A
	 * fresh begin succeeding after each is the proof that the old ceremony is gone (begin refuses while one runs).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 260, batch = BATCH)
	public static void ceremonyCancelledByTeleportLogoutAndVicarLoss(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		HarbingerVicarEntity spare = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 22, 20));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		PlaythroughClock.start(helper)
				// 1. A pearl-like teleport away from the ceremony spot, still within 8 blocks of the Vicar.
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(20))
				.then(() -> {
					Vec3 from = player.player().position();
					Vec3 away = from.subtract(vicar.position()).multiply(1, 0, 1).normalize().scale(5.0D);
					player.player().teleportTo(from.x + away.x, from.y, from.z + away.z);
					player.tickOnce();
					PlaythroughLog.note(helper, "teleport: moved 5 blocks, now " + String.format("%.1f",
							player.player().distanceTo(vicar)) + " from the Vicar");
					assertCeremonyCancelled(helper, player, vicar, "after a 5-block teleport");
				})
				// 2. A relog mid-ceremony.
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(20))
				.then(() -> {
					player.relog();
					assertCeremonyCancelled(helper, player, vicar, "after a relog");
					helper.assertTrue(player.bloodActive() && player.hasCharm(), "the relog keeps the charm and blood");
				})
				// 3. The Vicar removed mid-ceremony.
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(20))
				.then(() -> {
					vicar.discard();
					player.tickOnce();
					assertCeremonyCancelled(helper, player, null, "after the Vicar was removed");
				})
				.then(() -> {
					// Another Vicar can receive the player at once: the lost ceremony left nothing behind.
					beginCeremony(player, spare);
					PlaythroughLog.note(helper, "a second Vicar received the player after the first was removed");
				})
				.run();
	}

	/**
	 * Releasing the charm at the Vicar empties slot 5, puts the blood to sleep and says released. The first temple is
	 * spent (its Display says already claimed), but a second fresh temple's blessing, claim and Vicar initiation still
	 * reach D1.
	 */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 100, batch = BATCH)
	public static void releaseCharmSpendsTempleButSecondTempleWorks(GameTestHelper helper) {
		PlaythroughSites.BloodTemple first = templeA(helper);
		PlaythroughSites.BloodTemple second = templeB(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, first.stand());
		claimAtTemple(player, first);

		PlaythroughClock.start(helper)
				.then(() -> {
					player.approach(vicar);
					player.talkTo(vicar);
					Outcome release = player.choose(vicar, RELEASE).said(RELEASED);
					helper.assertFalse(player.hasCharm(), "slot 5 is empty after release; " + release);
					helper.assertTrue(HemoCapabilityAccess.getEquipment(player.player()).getStackInSlot(5).isEmpty(),
							"the vascular slot holds nothing");
					helper.assertFalse(player.bloodActive(), "the blood is asleep after release; " + release);
					helper.assertTrue(TempleOathRules.claimedHeartHermit(player.player()) == null, "the oath is cleared");
					DialogueView after = player.talkTo(vicar);
					helper.assertFalse(after.offers(BEGIN), "no initiation once released: " + after.describe());

					player.moveTo(first.stand());
					player.useOn(first.display()).said(ALREADY_CLAIMED).didNotSay(CHARM_EQUIPPED);
					DialogueView oldHermit = player.talkTo(first.hermit());
					// DECISION: D-8 the first Hermit offers his (spent) heart again; recorded, not asserted.
					PlaythroughLog.note(helper, "released player at the spent temple's Hermit: " + oldHermit.describe());

					claimAtTemple(player, second);
					helper.assertTrue(player.hasCharm() && player.bloodActive(), "the second temple's heart is claimed");
				})
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(CEREMONY))
				.then(() -> player.last().degreeIs(1).earned(DEGREE_ONE))
				.run();
	}

	/**
	 * The blessing lives in root persistent data and dies with the player: after a death the Display refuses, and
	 * asking the Hermit again restores the blessing so the touch works.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void deathBetweenBlessingAndTouch(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		HarbingerHermitEntity hermit = temple.hermit();

		player.talkTo(hermit);
		player.choose(hermit, HEART_OFFERED).said(SAID_HEART_OFFERED);
		Outcome death = player.die();
		helper.assertTrue(player.player().isDeadOrDying(), "real damage kills the player; " + death);
		player.respawn();
		PlaythroughLog.note(helper, "blessing after death: " + TempleOathRules.blessedHermit(player.player()));

		Outcome refused = player.useOn(temple.display());
		refused.said(SPEAK_WITH_HERMIT).didNotSay(CHARM_EQUIPPED);
		helper.assertFalse(player.hasCharm(), "the Display refuses after the death; " + refused);

		DialogueView again = player.talkTo(hermit);
		helper.assertTrue(again.offers(HEART_OFFERED), "the Hermit offers his heart again after a death: " + again.describe());
		player.choose(hermit, HEART_OFFERED).said(SAID_HEART_OFFERED);
		Outcome touch = player.useOn(temple.display());
		touch.said(CHARM_EQUIPPED);
		helper.assertTrue(player.hasCharm() && player.bloodActive(), "the renewed blessing lets the touch work; " + touch);
		helper.succeed();
	}

	/**
	 * Claim, die, respawn: the charm and the woken blood survive, the Vicar offers initiation, and the newcomer card
	 * ("our path starts at a Hermit's Blood Temple") does not contradict it.
	 */
	// FIX: F-6 the met-Hermit flag is set on talking to the Hermit, under PlayerPersisted, so a death does not bring
	// the newcomer card back.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void deathAfterClaimKeepsInitiationOffer(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		player.die();
		player.respawn();
		helper.assertTrue(player.hasCharm(), "the charm survives death");
		helper.assertTrue(player.bloodActive(), "the woken blood survives death");
		helper.assertTrue(TempleOathRules.hasClaimedHeartFrom(player.player(), temple.hermit().getUUID()),
				"the claimed Hermit survives death");

		player.approach(vicar);
		DialogueView view = player.talkTo(vicar);
		PlaythroughLog.note(helper, "Vicar after death: " + view.describe());
		helper.assertTrue(view.offers(BEGIN), "initiation is still offered after a death: " + view.describe());
		helper.assertFalse(hasNewcomerCard(view), "no contradictory newcomer card after a death: " + view.describe());
		helper.succeed();
	}

	/** Claim, then relog: the blood is awake and the Vicar still offers initiation. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void relogAfterClaimReactivatesBlood(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		claimAtTemple(player, temple);

		player.relog();
		helper.assertTrue(player.hasCharm(), "the charm survives a relog");
		helper.assertTrue(player.bloodActive(), "the blood is awake after a relog");
		player.approach(vicar);
		DialogueView view = player.talkTo(vicar);
		helper.assertTrue(view.offers(BEGIN), "initiation is still offered after a relog: " + view.describe());
		PlaythroughLog.note(helper, "Vicar after relog: " + view.describe() + " newcomer=" + hasNewcomerCard(view));
		helper.succeed();
	}

	/**
	 * Two players blessed at one temple: P1 claims, P2's touch is told the heart is already claimed. P2 then asks the
	 * Hermit again.
	 */
	// DECISION: D-8 the Hermit has no reference to his Display, so he blesses P2 again and says "Touch the Mortal
	// Display and take my heart" for a heart that is gone. The expected (player) outcome is asserted; the line given is
	// logged.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void twoPlayersOneTemple(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerHermitEntity hermit = temple.hermit();
		PlaythroughPlayer first = PlaythroughPlayer.create(helper, temple.stand());
		PlaythroughPlayer second = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 8));

		first.talkTo(hermit);
		second.talkTo(hermit);
		first.choose(hermit, HEART_OFFERED);
		second.choose(hermit, HEART_OFFERED);
		first.useOn(temple.display()).said(CHARM_EQUIPPED);
		Outcome late = second.useOn(temple.display());
		late.said(ALREADY_CLAIMED).didNotSay(CHARM_EQUIPPED);
		helper.assertFalse(second.hasCharm(), "the second player gets no charm; " + late);

		DialogueView again = second.talkTo(hermit);
		PlaythroughLog.note(helper, "P2 re-asks the Hermit of a spent temple: " + again.describe());
		Outcome told = again.offers(HEART_OFFERED) ? second.choose(hermit, HEART_OFFERED) : null;
		PlaythroughLog.note(helper, "P2 is told: " + (told == null ? "(heart not offered)" : told.chat()));
		helper.assertTrue(told == null || !told.saidKeys().contains(SAID_HEART_OFFERED),
				"D-8: a Hermit whose heart is claimed should not tell P2 to take it; " + told);
		helper.succeed();
	}

	/**
	 * P2 talks to the Vicar while the Vicar is officiating for P1. Either the "Initiate me" option is hidden, or P2
	 * chooses it and is told why it is refused (a system line whose keys exist), not left in silence. Either way P2's
	 * state does not change and P1's ceremony still completes.
	 */
	// FIX: F-13 style: EarlyInitiation.begin used to refuse a busy Vicar in silence; it now says
	// hemomancy.initiation.officiant_busy.
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 80, batch = BATCH)
	public static void twoPlayersOneVicar(GameTestHelper helper) {
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer first = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 17, 11));
		PlaythroughPlayer second = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 14));
		PlaythroughStates.charmBearerD0(first, (Entity) null); // ARRANGE: the temple is not under test
		PlaythroughStates.charmBearerD0(second, (Entity) null); // ARRANGE

		PlaythroughClock.start(helper)
				.then(() -> beginCeremony(first, vicar))
				.step(first.tickPlayer(20))
				.then(() -> {
					DialogueView view = second.talkTo(vicar);
					PlaythroughLog.note(helper, "P2 sees the officiating Vicar: " + view.describe());
					view.assertLangKeys(helper, "P2's tree at the officiating Vicar");
					if (!view.offers(BEGIN)) {
						// Hiding the option while the Vicar officiates is an equally good fix for the silent refusal.
						helper.assertTrue(second.hasCharm() && second.bloodActive() && second.degree() == 0,
								"P2 keeps the charm and blood and stays D0");
						PlaythroughLog.note(helper, "P2 is not offered initiation while the Vicar officiates");
						return;
					}
					Outcome attempt = second.choose(vicar, BEGIN);
					PlaythroughLog.note(helper, "P2 begins during P1's ceremony: " + attempt);
					helper.assertFalse(attempt.rewardDelivered(), "P2's ceremony does not start; " + attempt);
					helper.assertTrue(attempt.degreeAfter() == 0 && attempt.earned().isEmpty() && attempt.drops().isEmpty()
							&& attempt.inventoryDelta().isEmpty(), "P2's state is unchanged; " + attempt);
					helper.assertTrue(second.hasCharm() && second.bloodActive(), "P2 keeps the charm and blood");
					attempt.said(OFFICIANT_BUSY);
					for (String key : attempt.saidKeys()) helper.assertTrue(LangKeys.exists(key), "the refusal key exists: " + key);
				})
				.step(first.tickPlayer(CEREMONY - 20))
				.then(() -> {
					helper.assertTrue(first.degree() == 1, "P1's ceremony completes despite P2's attempt; " + first.last());
					helper.assertTrue(second.degree() == 0, "P2 stays D0");
				})
				.run();
	}

	/**
	 * Every main slot is full except one holding 14 of 16 Bloody Flasks. The ceremony's four starter flasks must all
	 * reach the player: held, or dropped at their feet.
	 */
	// FIX: F-2 regression guard. No code change was needed: in 1.21.1 Inventory.add returns false when it only partly
	// inserts, so EarlyInitiation.give already drops the remainder.
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 80, batch = BATCH)
	public static void partialStackStarterFlasksAreNotLost(GameTestHelper helper) {
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 17, 11));
		PlaythroughStates.charmBearerD0(player, (Entity) null); // ARRANGE: the temple is not under test
		player.fillInventory(0); // ARRANGE: every main slot full but slot 0
		player.player().getInventory().items.set(0, new ItemStack(ItemInit.bloody_flask.get(), 14)); // ARRANGE

		PlaythroughClock.start(helper)
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(CEREMONY))
				.then(() -> {
					Outcome ceremony = player.last();
					int held = ceremony.delta(ItemInit.bloody_flask.get());
					int dropped = ceremony.dropped(ItemInit.bloody_flask.get(), Outcome.Near.ANY);
					PlaythroughLog.note(helper, "partial stack: held +" + held + ", dropped " + dropped + "; " + ceremony);
					ceremony.degreeIs(1);
					helper.assertTrue(held + dropped == 4, "all 4 starter flasks reach the player (held " + held
							+ ", dropped " + dropped + "); " + ceremony);
					helper.assertTrue(ceremony.dropped(ItemInit.sanguine_conduit.get(), Outcome.Near.ANY) == 1,
							"the conduit drops when it cannot fit; " + ceremony);
					helper.assertTrue(ceremony.dropped(ItemInit.harbinger_assignment_ledger.get(), Outcome.Near.ANY) == 1,
							"the ledger drops when it cannot fit; " + ceremony);
				})
				.run();
	}

	/**
	 * A Creative player with a full inventory completes initiation. The player-expected outcome is the starter kit
	 * delivered (held or dropped) or still owed (initiation_blood_claimed not recorded).
	 */
	// DECISION: D-16 Inventory.add discards in Creative, so the conduit, flasks and ledger vanish while
	// initiation_blood_claimed is still recorded. The outcome is logged.
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 80, batch = BATCH, required = false)
	public static void creativeFullInventoryStarterKit(GameTestHelper helper) {
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 17, 11));
		PlaythroughStates.charmBearerD0(player, (Entity) null); // ARRANGE: the temple is not under test
		player.fillInventory(); // ARRANGE: every main slot full
		player.creative(); // ARRANGE

		PlaythroughClock.start(helper)
				.then(() -> beginCeremony(player, vicar))
				.step(player.tickPlayer(CEREMONY))
				.then(() -> {
					Outcome ceremony = player.last();
					int flasks = ceremony.delta(ItemInit.bloody_flask.get())
							+ ceremony.dropped(ItemInit.bloody_flask.get(), Outcome.Near.ANY);
					int conduits = ceremony.delta(ItemInit.sanguine_conduit.get())
							+ ceremony.dropped(ItemInit.sanguine_conduit.get(), Outcome.Near.ANY);
					boolean claimed = player.has(STARTER_CLAIMED);
					PlaythroughLog.note(helper, "creative full inventory: flasks reached=" + flasks + " conduits reached="
							+ conduits + " initiation_blood_claimed=" + claimed + "; " + ceremony);
					ceremony.degreeIs(1);
					helper.assertTrue(flasks == 4 || !claimed, "D-16: the starter flasks are delivered or still owed (reached "
							+ flasks + ", claimed " + claimed + ")");
					helper.assertTrue(conduits == 1, "D-16: the conduit reaches the Creative player (reached " + conduits + ")");
				})
				.run();
	}

	/**
	 * Each D0 event sent from 9 blocks (past the 8-block dialogue reach) is dropped: no event is dispatched and
	 * nothing changes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void forgedOnboardingEventsOutOfReach(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		HarbingerHermitEntity hermit = temple.hermit();

		player.moveTo(PlaythroughSites.farFrom(hermit, temple.stand()));
		helper.assertTrue(player.player().distanceTo(hermit) > 8.0F, "the player stands out of the Hermit's reach");
		Outcome blessing = player.forge(HEART_OFFERED, hermit.getId());
		PlaythroughLog.note(helper, "far blessing: " + blessing + " diff=" + blessing.savedDiff());
		helper.assertTrue(blessing.event().isEmpty(), "a blessing from 9 blocks is dropped; " + blessing);
		helper.assertTrue(TempleOathRules.blessedHermit(player.player()) == null, "no blessing is recorded");
		blessing.didNotSay(SAID_HEART_OFFERED);

		PlaythroughStates.charmBearerD0(player, hermit); // ARRANGE: the claim itself is not under test here
		player.moveTo(PlaythroughSites.farFrom(vicar, PlaythroughSites.stand(helper, 14, 11)));
		helper.assertTrue(player.player().distanceTo(vicar) > 8.0F, "the player stands out of the Vicar's reach");
		Outcome begin = player.forge(BEGIN, vicar.getId());
		PlaythroughLog.note(helper, "far begin: " + begin + " diff=" + begin.savedDiff());
		helper.assertTrue(begin.event().isEmpty(), "an initiation from 9 blocks is dropped; " + begin);
		helper.assertFalse(vicar.isInitiating() || vicar.isNoAi(), "the Vicar does not start officiating");
		Outcome release = player.forge(RELEASE, vicar.getId());
		PlaythroughLog.note(helper, "far release: " + release + " diff=" + release.savedDiff());
		helper.assertTrue(release.event().isEmpty(), "a release from 9 blocks is dropped; " + release);
		release.didNotSay(RELEASED);
		helper.assertTrue(player.hasCharm() && player.bloodActive(), "the charm and blood are untouched");
		helper.assertTrue(player.degree() == 0, "still D0");
		helper.succeed();
	}

	// --- Decisions about the temple ------------------------------------------------------------------------------

	/**
	 * A blessed player breaks the Display with a pickaxe, picks it up, places it back and touches it. The
	 * player-expected outcome is a temple that still works.
	 */
	// DECISION: D-8 the Display is survival-breakable and its loot drops a plain item with no Hermit link, so the
	// re-placed Display says no_living_oath and the temple is dead. The observed line is logged.
	@GameTest(template = EMPTY, timeoutTicks = 80, batch = BATCH, required = false)
	public static void brokenMortalDisplayLeavesNoOath(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		HarbingerHermitEntity hermit = temple.hermit();
		player.talkTo(hermit);
		player.choose(hermit, HEART_OFFERED).said(SAID_HEART_OFFERED);

		player.give(new ItemStack(Items.DIAMOND_PICKAXE)); // ARRANGE: a tool any player has by now
		player.wield(Items.DIAMOND_PICKAXE);
		Outcome mined = breakBlock(player, temple.display());
		PlaythroughLog.note(helper, "break the Display: " + mined);
		if (!helper.getLevel().getBlockState(temple.display()).isAir()) {
			PlaythroughLog.note(helper, "the Display could not be broken; the temple keeps its link");
			emptyHand(player);
			Outcome touch = player.useOn(temple.display());
			helper.assertTrue(player.hasCharm(), "an unbreakable Display still works; " + touch);
			helper.succeed();
			return;
		}
		Outcome pickup = player.pickUpNear(temple.display(), BlockInit.mortal_display.get());
		PlaythroughLog.note(helper, "pick up: " + pickup);
		helper.assertTrue(player.count(BlockInit.mortal_display.get()) == 1, "the broken Display drops as an item; " + pickup);
		Outcome placed = player.place(temple.display(), BlockInit.mortal_display.get());
		helper.assertTrue(helper.getLevel().getBlockState(temple.display()).is(BlockInit.mortal_display.get()),
				"the Display is placed back; " + placed);
		emptyHand(player);
		Outcome touch = player.useOn(temple.display());
		PlaythroughLog.note(helper, "touch the re-placed Display: " + touch.chat() + " no_living_oath="
				+ touch.saidKeys().contains(NO_LIVING_OATH));
		helper.assertTrue(player.hasCharm(), "D-8: a re-placed Display should still give the heart; " + touch);
		helper.succeed();
	}

	/**
	 * A player who claimed temple A talks to temple B's Hermit. The player-expected outcome is that Hermit B does
	 * not say "You have claimed my heart" and offers no farewell that cannot work.
	 */
	// DECISION: D-8 any active-blood D0 player gets the claimed-heart tree from every Hermit, and Hermit B's farewell
	// silently does nothing. The tree and the farewell's effect are logged.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void wrongHermitClaimsAndSilentFarewell(GameTestHelper helper) {
		PlaythroughSites.BloodTemple first = templeA(helper);
		PlaythroughSites.BloodTemple other = templeB(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, first.stand());
		claimAtTemple(player, first);

		player.moveTo(other.stand());
		HarbingerHermitEntity stranger = other.hermit();
		DialogueView view = player.talkTo(stranger);
		boolean claimsHeart = view.lineKeys().contains(CLAIMED_YOU_LINE);
		boolean offersFarewell = view.offers(FAREWELL);
		PlaythroughLog.note(helper, "Hermit B to temple A's claimant: " + view.describe() + " lines=" + view.lineKeys());
		Outcome farewell = player.forge(FAREWELL, stranger.getId());
		PlaythroughLog.note(helper, "Hermit B's farewell: said=" + farewell.chat() + " dissolving=" + stranger.isFarewellDying()
				+ " offered=" + offersFarewell);
		helper.assertFalse(stranger.isFarewellDying(), "Hermit B does not dissolve for another temple's claimant");
		helper.assertFalse(claimsHeart, "D-8: Hermit B should not say '" + CLAIMED_YOU_LINE + "'");
		helper.assertFalse(offersFarewell, "D-8: Hermit B should offer no farewell that does nothing");
		helper.succeed();
	}

	// --- The founder route (players in the level) ----------------------------------------------------------------

	/**
	 * A charm-bearer forges vicar_begin_initiation naming a nearby D5 founder's entity id. No ceremony starts, the
	 * recruit joins no bloodline, and the founder's Blood Projection is not swallowed by an officiating he never
	 * began.
	 */
	// FIX: F-1 vicar_begin_initiation and vicar_release_charm require a HarbingerVicarEntity speaker.
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 80, batch = LEVEL_BATCH)
	public static void forgedFounderInitiationIsRefused(GameTestHelper helper) {
		PlaythroughPlayer founder = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 12));
		PlaythroughPlayer recruit = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 11, 12));
		Bloodline line = foundLine(founder, "Forged reception line"); // ARRANGE: a D5 founder with a line
		PlaythroughStates.charmBearerD0(recruit, (Entity) null); // ARRANGE: a charm-bearer
		joinLevel(founder); // ARRANGE: the dialogue guards find the founder by entity id only in the level
		joinLevel(recruit);

		PlaythroughClock.start(helper)
				.then(() -> {
					Outcome forged = recruit.forge(BEGIN, founder.player().getId());
					PlaythroughLog.note(helper, "forged founder begin: " + forged);
					helper.assertFalse(forged.rewardDelivered(), "a forged founder initiation does not begin; " + forged);
					// PROBE: the founder looks at the sky; an officiating founder's projection is swallowed (true).
					founder.aim(founder.player().getEyePosition().add(0.0D, 20.0D, 0.0D));
					boolean swallowed = EarlyInitiation.tryProject(founder.player());
					PlaythroughLog.note(helper, "founder projection swallowed after the forge: " + swallowed);
					helper.assertFalse(swallowed, "the founder's Blood Projection is not swallowed by a forged ceremony");
				})
				.step(recruit.tickPlayer(CEREMONY))
				.then(() -> {
					helper.assertTrue(recruit.degree() == 0, "the recruit stays D0; " + recruit.last());
					helper.assertFalse(line.hasMember(recruit.uuid()), "the recruit joins no bloodline");
					helper.assertFalse(isMemberOf(recruit, line), "the recruit's blood names no bloodline");
					helper.assertTrue(recruit.count(ItemInit.sanguine_conduit.get()) == 0, "no conduit is given");
				})
				.run();
	}

	/**
	 * A D5 founder holds Blood Projection on a charm-bearer for the ceremony's length (the recruit ticking alongside).
	 * The recruit reaches D1, joins the line and is told so. The probe records whether consent was asked.
	 */
	// DECISION: D-9 founder reception needs no recruit consent: no dialogue or prompt reaches the recruit, and a
	// projection pins them for 10 s. Logged.
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 120, batch = LEVEL_BATCH, required = false)
	public static void founderProjectionReceivesRecruit(GameTestHelper helper) {
		PlaythroughPlayer founder = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 6));
		PlaythroughPlayer recruit = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 11, 6));
		Bloodline line = foundLine(founder, "Projection reception line"); // ARRANGE: a D5 founder with a line
		PlaythroughStates.charmBearerD0(recruit, (Entity) null); // ARRANGE: a charm-bearer
		joinLevel(founder); // ARRANGE: the projection's entity trace only sees players in the level
		joinLevel(recruit);
		int promptsBefore = recruit.sent(OpenDialoguePacket.class).size();
		Outcome[] founderHold = new Outcome[1];

		PlaythroughClock.start(helper)
				.step(reception(founder, recruit, EarlyInitiation.DURATION + 5, founderHold))
				.then(() -> {
					Outcome received = recruit.last();
					int prompts = recruit.sent(OpenDialoguePacket.class).size() - promptsBefore;
					PlaythroughLog.note(helper, "reception: " + received);
					PlaythroughLog.note(helper, "founder hold: " + founderHold[0] + " (blood spent "
							+ (founderHold[0] == null ? "?" : String.format("%.0f", founderHold[0].bloodSpent())) + ")");
					PlaythroughLog.note(helper, "D-9 consent: " + prompts + " dialogue prompt(s) reached the recruit before"
							+ " reception; consent " + (prompts == 0 ? "was not asked" : "may have been asked"));
					received.degreeIs(1).said(BLOODLINE_LINE).earned(DEGREE_ONE);
					helper.assertTrue(line.hasMember(recruit.uuid()), "the recruit is a member of the founder's line");
					helper.assertTrue(isMemberOf(recruit, line), "the recruit's blood names the founder's line");
					helper.assertTrue(prompts > 0, "D-9: the recruit should be asked before a founder receives them");
				})
				.run();
	}

	// --- Band helpers (candidates for the harness) -----------------------------------------------------------------

	/**
	 * The real D0 claim: stand at the temple, talk to the Hermit, choose his heart, touch the Display empty-handed.
	 * Used where the claim is a precondition that should still be played rather than fixtured (the Hermit talk sets
	 * the met-Hermit state the Vicar reads).
	 */
	// --- Review follow-ups: the Display's refusals, a full pack, initiating before the farewell, founder cancels ------

	/**
	 * A blessed player whose vascular slot is already taken, and one whose blood is already awake, touch the Display:
	 * each is told the heart cannot attach, and neither gets the charm or the lodestone.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void displayRefusesOccupiedSlotOrAwakeBlood(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		for (boolean occupied : List.of(true, false)) {
			PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
			player.talkTo(temple.hermit());
			player.choose(temple.hermit(), HEART_OFFERED).said(SAID_HEART_OFFERED);
			if (occupied) {
				HemoCapabilityAccess.getEquipment(player.player()).setStackInSlot(5, new ItemStack(Items.STRING)); // ARRANGE
			} else {
				var blood = HemoCapabilityAccess.getBloodVolume(player.player()); // ARRANGE: blood woken some other way
				blood.setActive(true);
				BloodVolumeEvents.syncVolume(player.player(), blood);
			}
			emptyHand(player);
			Outcome touch = player.useOn(temple.display());
			PlaythroughLog.note(helper, (occupied ? "slot taken" : "blood awake") + ": " + touch.describe());
			touch.said("hemomancy.initiation.cannot_attach");
			helper.assertFalse(player.hasCharm(), "no charm is fitted; " + touch);
			helper.assertTrue(player.count(ItemInit.harbinger_lodestone.get()) == 0, "no lodestone is given; " + touch);
			helper.assertFalse(temple.displayEntity(helper.getLevel()).isClaimedBy(player.uuid()),
					"the Display is not claimed by a refused touch");
			player.offline();
		}
		helper.succeed();
	}

	/**
	 * A D1 player (or any player whose blood is awake) touching an unclaimed Display is told to speak with the
	 * temple's Hermit, who can never bless them. Expected: they are told the heart cannot attach to awakened blood.
	 */
	// DECISION: D-8b an awakened player at an unclaimed Display is sent to a Hermit who cannot bless them.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void awakenedPlayerAtUnclaimedDisplay(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		PlaythroughStates.neophyteD1(player); // ARRANGE
		emptyHand(player);
		Outcome touch = player.useOn(temple.display());
		DialogueView hermit = player.talkTo(temple.hermit());
		PlaythroughLog.note(helper, "D-8b D1 at an unclaimed Display: " + touch.describe() + "; the Hermit then offers "
				+ hermit.reachableEvents());
		helper.assertFalse(touch.saidKeys().contains(SPEAK_WITH_HERMIT) && !hermit.offers(HEART_OFFERED),
				"D-8b expected: the player is not sent to a Hermit who cannot bless them");
		helper.succeed();
	}

	/** The Display touched with every other slot full: the lodestone arrives exactly once, held or at the feet. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void displayTouchWithFullPackGivesOneLodestone(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		player.talkTo(temple.hermit());
		player.choose(temple.hermit(), HEART_OFFERED);
		int hand = player.player().getInventory().selected;
		player.fillInventory(hand); // ARRANGE: every slot full but the empty hand the touch needs
		Outcome touch = player.useOn(temple.display());
		int held = player.count(ItemInit.harbinger_lodestone.get());
		int dropped = touch.dropped(ItemInit.harbinger_lodestone.get(), Outcome.Near.ANY);
		PlaythroughLog.note(helper, "full-pack touch: held " + held + ", dropped " + dropped + "; " + touch.describe());
		helper.assertTrue(player.hasCharm(), "the charm is fitted; " + touch);
		helper.assertTrue(held + dropped == 1, "exactly one lodestone, held or dropped: held " + held + ", dropped " + dropped);
		helper.succeed();
	}

	/**
	 * The player claims the heart and goes straight to the Vicar without the Hermit's farewell. At D1 the Hermit's
	 * tree has no farewell; nothing he offers may be a silent no-op (each choice says or gives something).
	 */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 80, batch = BATCH)
	public static void initiatedBeforeTheFarewell(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = templeA(helper);
		HarbingerVicarEntity vicar = vicar(helper);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		PlaythroughClock.start(helper)
				.then(() -> {
					claimAtTemple(player, temple);
					beginCeremony(player, vicar);
				})
				.step(player.tickPlayer(CEREMONY))
				.then(() -> {
					helper.assertTrue(player.degree() == 1, "the ceremony completes without the farewell");
					player.moveTo(temple.stand());
					DialogueView view = player.talkTo(temple.hermit());
					view.assertLangKeys(helper, "the Hermit to a D1 player").assertNoDeadEnds(helper, "the Hermit at D1");
					PlaythroughLog.note(helper, "the Hermit at D1 without the farewell: " + view.describe()
							+ "; dissolving=" + temple.hermit().isFarewellDying());
					List<String> silent = new ArrayList<>();
					for (String event : view.reachableEvents()) {
						Outcome chosen = player.choose(temple.hermit(), event);
						if (chosen.saidKeys().isEmpty() && !chosen.rewardDelivered() && chosen.inventoryDelta().isEmpty()) {
							silent.add(event);
						}
					}
					helper.assertTrue(silent.isEmpty(), "no Hermit option at D1 is a silent no-op: " + silent);
				})
				.run();
	}

	/** The founder's reception cancelled by the recruit already belonging to another line. */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 120, batch = LEVEL_BATCH)
	public static void founderReceptionRefusesAMemberOfAnotherLine(GameTestHelper helper) {
		founderReceptionCancelled(helper, "the recruit already in another line", (founder, recruit) -> {
			PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 20));
			Bloodline theirs = foundLine(other, "Another line"); // ARRANGE
			var volume = HemoCapabilityAccess.getBloodVolume(recruit.player());
			volume.setBloodLine(theirs);
			theirs.addMember(recruit.uuid());
			BloodVolumeEvents.syncVolume(recruit.player(), volume);
		}, -1, null);
	}

	/** The founder's reception cancelled by the founder walking more than 8 blocks away mid-ceremony. */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 120, batch = LEVEL_BATCH)
	public static void founderReceptionCancelledByDistance(GameTestHelper helper) {
		founderReceptionCancelled(helper, "the founder walked away", null, 40,
				(founder, recruit) -> founder.moveTo(recruit.player().blockPosition().offset(10, 0, 0)));
	}

	/** The founder's reception cancelled by the recruit taking damage mid-ceremony. */
	@GameTest(template = EMPTY, timeoutTicks = CEREMONY + 120, batch = LEVEL_BATCH)
	public static void founderReceptionCancelledByDamage(GameTestHelper helper) {
		founderReceptionCancelled(helper, "the recruit was hurt", null, 40, (founder, recruit) -> recruit.damage(2.0F));
	}

	/**
	 * A founder holds Blood Projection on a charm-bearer for the whole ceremony, with {@code before} applied first
	 * and {@code disrupt} at tick {@code at}. The recruit must end D0 and outside the founder's line; the founder's
	 * blood spend while officiating is logged.
	 */
	private static void founderReceptionCancelled(GameTestHelper helper, String label,
			java.util.function.BiConsumer<PlaythroughPlayer, PlaythroughPlayer> before, int at,
			java.util.function.BiConsumer<PlaythroughPlayer, PlaythroughPlayer> disrupt) {
		PlaythroughPlayer founder = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 6));
		PlaythroughPlayer recruit = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 11, 6));
		Bloodline line = foundLine(founder, "Cancelled reception line"); // ARRANGE
		PlaythroughStates.charmBearerD0(recruit, (Entity) null); // ARRANGE
		if (before != null) before.accept(founder, recruit);
		joinLevel(founder); // ARRANGE
		joinLevel(recruit);
		Outcome[] founderHold = new Outcome[1];
		PlaythroughClock.Step reception = reception(founder, recruit, EarlyInitiation.DURATION + 5, founderHold);
		PlaythroughClock.start(helper)
				.step(ticks -> {
					if (ticks == at && disrupt != null) disrupt.accept(founder, recruit);
					return reception.tick(ticks);
				})
				.then(() -> {
					PlaythroughLog.note(helper, label + ": recruit " + recruit.last() + "; founder " + founderHold[0]
							+ " (blood spent " + (founderHold[0] == null ? "?" : String.format("%.0f", founderHold[0].bloodSpent()))
							+ ")");
					helper.assertTrue(recruit.degree() == 0, label + ": the recruit stays D0, not " + recruit.degree());
					helper.assertFalse(line.hasMember(recruit.uuid()), label + ": the recruit is not taken into the line");
				})
				.run();
	}

	private static Outcome claimAtTemple(PlaythroughPlayer player, PlaythroughSites.BloodTemple temple) {
		GameTestHelper helper = player.helper();
		player.moveTo(temple.stand());
		DialogueView view = player.talkTo(temple.hermit());
		helper.assertTrue(view.offers(HEART_OFFERED), "the Hermit offers his heart: " + view.describe());
		player.choose(temple.hermit(), HEART_OFFERED);
		emptyHand(player);
		Outcome touch = player.useOn(temple.display());
		helper.assertTrue(player.hasCharm() && player.bloodActive(), "the temple claim fits the charm; " + touch);
		return touch;
	}

	/**
	 * Puts whatever the main hand holds into a free inventory slot, as a player scrolls to an empty hotbar slot, so
	 * the next touch is empty-handed. Does nothing when the hand is already empty or no slot is free.
	 */
	private static void emptyHand(PlaythroughPlayer player) {
		ItemStack held = player.player().getMainHandItem();
		if (held.isEmpty()) return;
		var inventory = player.player().getInventory();
		int free = inventory.getFreeSlot();
		if (free < 0) return;
		inventory.items.set(free, held.copy());
		player.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	/** Walks up to the Vicar, opens his dialogue and chooses the initiation, which must begin. */
	private static Outcome beginCeremony(PlaythroughPlayer player, HarbingerVicarEntity vicar) {
		GameTestHelper helper = player.helper();
		player.approach(vicar);
		DialogueView view = player.talkTo(vicar);
		helper.assertTrue(view.offers(BEGIN), "the Vicar offers initiation: " + view.describe());
		Outcome begun = player.choose(vicar, BEGIN);
		helper.assertTrue(begun.rewardDelivered(), "the ceremony begins; " + begun);
		return begun;
	}

	/** A lost ceremony leaves the player as they were and the Vicar (when there is one) free. */
	private static void assertCeremonyCancelled(GameTestHelper helper, PlaythroughPlayer player,
			HarbingerVicarEntity vicar, String when) {
		helper.assertTrue(player.degree() == 0, "still D0 " + when);
		helper.assertTrue(player.hasCharm(), "the charm stays on " + when);
		helper.assertTrue(player.bloodActive(), "the blood stays awake " + when);
		helper.assertFalse(player.has(DEGREE_ONE), "no degree advancement " + when);
		helper.assertTrue(player.count(ItemInit.sanguine_conduit.get()) == 0
				&& player.count(ItemInit.bloody_flask.get()) == 0, "no starter kit " + when);
		if (vicar != null && !vicar.isRemoved()) {
			helper.assertFalse(vicar.isInitiating(), "the Vicar is no longer officiating " + when);
			helper.assertFalse(vicar.isNoAi(), "the Vicar's AI is restored " + when);
		}
	}

	private static boolean hasNewcomerCard(DialogueView view) {
		return view.hasNode(NEWCOMER_NODE)
				|| view.topics().stream().anyMatch(topic -> NEWCOMER_TITLE.equals(topic.titleKey()));
	}

	private static void assertClaimedInBothHomes(GameTestHelper helper, PlaythroughPlayer player, UUID hermit) {
		CompoundTag root = player.player().getPersistentData();
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		helper.assertTrue(root.hasUUID(CLAIMED_HEART_KEY) && hermit.equals(root.getUUID(CLAIMED_HEART_KEY)),
				"the claimed Hermit is in root persistent data");
		helper.assertTrue(durable.hasUUID(CLAIMED_HEART_KEY) && hermit.equals(durable.getUUID(CLAIMED_HEART_KEY)),
				"the claimed Hermit is in PlayerPersisted");
	}

	/**
	 * Mining finished: {@code ServerPlayerGameMode#destroyBlock}, the server's break entry point once the dig
	 * completes (break event, harvest check and drops). The dig time is not under test.
	 */
	private static Outcome breakBlock(PlaythroughPlayer player, BlockPos pos) {
		Outcome.Probe probe = Outcome.begin(player, null, "break " + player.player().level().getBlockState(pos).getBlock()
				+ " at " + pos.toShortString());
		player.aim(Vec3.atCenterOf(pos));
		boolean broken = player.player().gameMode.destroyBlock(pos);
		probe.note(broken ? "broken" : "not broken");
		return probe.finish();
	}

	/**
	 * ARRANGE: puts the captured player into the level (as {@code BloodInjectionGameTests} does), for entry points that
	 * find players through the level: entity traces and {@code level.getEntity(id)}. The player's cleanup discards
	 * them, which takes them out again.
	 */
	private static void joinLevel(PlaythroughPlayer player) {
		player.helper().getLevel().addNewPlayer(player.player());
	}

	/**
	 * ARRANGE: a D5 founder who leads a registered bloodline, with active blood, 1000 mL and a Blood Projection. The
	 * line is disbanded when the test ends.
	 */
	private static Bloodline foundLine(PlaythroughPlayer founder, String name) {
		GameTestHelper helper = founder.helper();
		ServerPlayer server = founder.player();
		PlaythroughStates.charmBearerD0(founder, (Entity) null);
		helper.assertTrue(DegreeProgression.advance(server, 5), "the founder reaches D5");
		HemoCapabilityAccess.getInitiatoryDegree(server).setHasFoundedBloodline(true);
		BloodlineSavedData data = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		Bloodline line = new Bloodline(name, server.getUUID(), UUID.randomUUID(), new ArrayList<>());
		data.registerBloodline(line);
		PlaythroughCleanup.onEnd(helper, () -> data.disbandBloodline(line.getBloodlineUUID()));
		var volume = HemoCapabilityAccess.getBloodVolume(server);
		volume.setBloodLine(line);
		volume.setActive(true);
		BloodVolumeEvents.syncVolume(server, volume);
		PlaythroughStates.blood(founder, 1000.0D);
		founder.give(new ItemStack(ItemInit.blood_projection.get()));
		return line;
	}

	private static boolean isMemberOf(PlaythroughPlayer player, Bloodline line) {
		var personal = HemoCapabilityAccess.getBloodVolume(player.player()).getBloodLine();
		return personal != null && personal.isValid() && personal.getBloodlineUUID().equals(line.getBloodlineUUID());
	}

	/**
	 * The founder holds Blood Projection on the recruit (a real use, then the living use tick each server tick) while
	 * the recruit's player tick runs alongside, for {@code ticks} ticks. The recruit's outcome is {@code recruit.last()};
	 * the founder's hold outcome lands in {@code founderHold[0]}.
	 */
	private static PlaythroughClock.Step reception(PlaythroughPlayer founder, PlaythroughPlayer recruit, int ticks,
			Outcome[] founderHold) {
		return new PlaythroughClock.Step() {
			private Outcome.Probe probe;

			@Override
			public boolean tick(int spent) {
				Vec3 target = recruit.player().getBoundingBox().getCenter();
				if (spent == 0) {
					probe = Outcome.begin(recruit, null, "received by a founder x" + ticks);
					founder.startHold(ItemInit.blood_projection.get(), target);
					founder.helper().assertTrue(founder.player().isUsingItem(), "the founder starts projecting");
				}
				if (spent >= ticks) {
					founderHold[0] = founder.releaseHold();
					probe.finish();
					return true;
				}
				founder.aim(target);
				founder.holdTick();
				recruit.tickOnce();
				return false;
			}

			@Override
			public String label() {
				return "founder reception x" + ticks;
			}
		};
	}
}
