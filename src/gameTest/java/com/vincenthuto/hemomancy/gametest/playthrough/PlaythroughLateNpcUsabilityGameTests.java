package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtDiscoveryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DeepDarkCommissionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SuccessionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VagrantMindInquiryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.route.DialogueEventRouter;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeMatch;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.gametest.CapturedPlayer;
import com.vincenthuto.hemomancy.gametest.playthrough.PlaythroughNpcs.CircusRole;
import com.vincenthuto.hemomancy.gametest.playthrough.PlaythroughNpcs.Role;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Band {@code npc_late}: can a real player find, talk to, understand and progress with every NPC from a fresh Degree 5
 * onward, on the Harbinger path to Degree 8.
 * <p>
 * Each sweep builds a late state through {@link PlaythroughStates} (ARRANGE: the D5 to D8 checkpoints, a bloodline
 * role, a held item), then acts only as a player does: a right click ({@code talkTo}, {@code talkToBlock} for the
 * Monolith), an offered option ({@code choose}, {@code chooseAtBlock}), a forged payload ({@code forge}, negative
 * cases), the trade request and the ledger payload. Findings are collected per speaker and problem (with the states
 * that showed it), logged as {@code [playthrough] <scenario>: FINDING ...}, and fail the sweep at its end.
 * <p>
 * State grid: Degree 5 (with and without the covenant) to Degree 8 on both paths (NONE, communed, the choice pending,
 * SILENT_PENDING, Vesper beaten, SILENTIUM, APOTHEOS_PENDING, APOTHEOS), crossed with the bloodline role (none, member,
 * leader, leader with a recruit) and the hand (empty, or a Sanguine Quintessence for the leader), plus purifying and
 * Clarity. Court and Circus speakers use a smaller grid crossed with Court standing and Circus acclimation.
 * Unstained playthroughs are out of scope.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughLateNpcUsabilityGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_npc_late";
	private static final String SWEEP_BATCH = "playthrough_npc_late_sweep";
	private static final int Y = PlaythroughSites.STAND_Y;

	/** A lone speaker and where the player stands to talk to it (relative). */
	private static final BlockPos NPC = new BlockPos(8, Y, 8);
	private static final BlockPos STAND = new BlockPos(10, Y, 8);
	/** Layout of the shared pieces (relative x, z): the wing's player spot, the Monolith, the Fane heart, the leader. */
	private static final int[] WING = {14, 12};
	private static final int[] MONOLITH = {27, 3};
	private static final int[] HEART = {2, 21};
	private static final int[] LEADER = {2, 2};
	private static final int[] SPARE = {29, 21};
	private static final int[] SHADOW = {30, 12};

	private static final String RECRUIT = "recruit_harbinger";
	private static final String EXPEL = "expel_harbinger";
	private static final String BLUEPRINT = "give_sanguine_monolith_blueprint";
	private static final String STAINED_MAP = "give_stained_church_map";
	private static final String WHISPER_SILENCE = "archon_choice_silence";
	private static final String WHISPER_EIGHTH = "archon_choice_eighth_degree";
	private static final String TITHE_SOURCE = NpcTradeWant.Blood.TITHE;
	private static final String RINGMASTER_SILENT = "hemomancy.circus.ringmaster.silent";

	private static final Role[] HARBINGER_WING = {Role.VICAR, Role.ALCHEMIST, Role.ARTIFICER, Role.MNEMONIST,
			Role.ANCHORITE, Role.VOYAGER, Role.VOTARY_WAYFARER};
	private static final List<String> TRADE_SPEAKERS = List.of("hermit", "vicar", "alchemist", "artificer",
			"mnemonist", "cicatrix_anchorite", "voyager", "votary_wayfarer");

	/**
	 * Events a curious player's click-through skips: a Chamber visit leaves the level, a bearing starts a slow search,
	 * pressing the Monolith further destroys the sweep's Monolith, and a Court challenge or a Circus act starts a fight
	 * or a show. Each is played by its own band.
	 */
	private static final Set<String> HAZARDS = Set.of(HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER,
			SanguineMonolithDialogueTrees.EVENT_SHATTER, CourtMemberDialogue.CHALLENGE, "circus_school_practical",
			"circus_school_guest", CircusRingmasterEntity.EVENT_TRIAL, CircusRingmasterEntity.EVENT_FINALE);

	private PlaythroughLateNpcUsabilityGameTests() {
	}

	// =================================================================================================================
	// The late state grid
	// =================================================================================================================

	/** A late checkpoint, as {@link PlaythroughStates} builds it. */
	private enum Stage {
		D5(5), D5_COVENANT(5), D6(6), D6_LIVING(6), D7(7), D7_COMMUNED(7), D7_CHOICE(7), D7_SILENT(7), D7_VESPER(7),
		D7_APOTHEOS_PENDING(7), D8_APOTHEOS(8), D8_SILENTIUM(8), PURIFYING(5), CLARITY(5);

		private final int degree;

		Stage(int degree) {
			this.degree = degree;
		}

		boolean special() {
			return this == PURIFYING || this == CLARITY;
		}

		/** Whether the covenant proof ({@code covenant_written_in_place}) holds at this stage. */
		boolean covenant() {
			return !special() && ordinal() >= D5_COVENANT.ordinal();
		}
	}

	private enum LineRole {NONE, MEMBER, LEADER, LEADER_WITH_RECRUIT}

	private enum Hand {EMPTY, ITEM}

	/** Court standing and Circus acclimation, for the sub-faction speakers. */
	private enum Extra {NONE, COURT_GUEST, ACCLIMATED, BOTH}

	private record LateState(Stage stage, LineRole role, Hand hand, Extra extra) {
		LateState(Stage stage, LineRole role) {
			this(stage, role, Hand.EMPTY, Extra.NONE);
		}

		@Override
		public String toString() {
			return stage + "/" + role + (hand == Hand.ITEM ? "/held quintessence" : "")
					+ (extra == Extra.NONE ? "" : "/" + extra);
		}
	}

	/** Every non-special stage for each of {@code roles}, the leader once more with an item in hand, then purifying and Clarity. */
	private static List<LateState> grid(boolean hands, LineRole... roles) {
		List<LateState> states = new ArrayList<>();
		for (Stage stage : Stage.values()) {
			if (stage.special()) continue;
			for (LineRole role : roles) states.add(new LateState(stage, role));
			if (hands) states.add(new LateState(stage, LineRole.LEADER, Hand.ITEM, Extra.NONE));
		}
		states.add(new LateState(Stage.PURIFYING, LineRole.NONE));
		states.add(new LateState(Stage.CLARITY, LineRole.NONE));
		return states;
	}

	private static List<LateState> fullGrid() {
		return grid(true, LineRole.values());
	}

	/** The smaller grid for Court and Circus speakers, crossed with {@code extras}. */
	private static List<LateState> factionGrid(Extra... extras) {
		List<LateState> states = new ArrayList<>();
		for (Stage stage : List.of(Stage.D5, Stage.D6_LIVING, Stage.D7, Stage.D7_SILENT, Stage.D8_APOTHEOS,
				Stage.D8_SILENTIUM)) {
			for (LineRole role : List.of(LineRole.NONE, LineRole.LEADER)) {
				for (Extra extra : extras) states.add(new LateState(stage, role, Hand.EMPTY, extra));
			}
		}
		return states;
	}

	/**
	 * What the sweeps share in one test: the Fane heart every leader consecrates, the line members join (its leader
	 * founded with a Fane), a spare Vicar from another outpost for a leader whose speaker cannot be recruited, and the
	 * Sanguine Monolith when the test has one.
	 */
	private static final class Late {
		final GameTestHelper helper;
		final BlockPos heart;
		private PlaythroughPlayer leader;
		private Entity spare;
		@Nullable
		private BlockPos monolith;

		Late(GameTestHelper helper) {
			this.helper = helper;
			this.heart = at(helper, HEART);
		}

		PlaythroughPlayer leader() {
			if (leader == null) {
				leader = PlaythroughPlayer.create(helper, at(helper, LEADER));
				PlaythroughStates.covenantD5(leader, heart); // ARRANGE: the line members join, with its Fane
			}
			return leader;
		}

		Entity spare() {
			if (spare == null) {
				spare = PlaythroughNpcs.stampOutpost(helper, PlaythroughNpcs.vicar(helper, at(helper, SPARE)), 1);
			}
			return spare;
		}

		BlockPos monolith() {
			if (monolith == null) monolith = PlaythroughSites.monolith(leader(), at(helper, MONOLITH)); // ARRANGE
			return monolith;
		}
	}

	private static BlockPos at(GameTestHelper helper, int[] xz) {
		return PlaythroughSites.stand(helper, xz[0], xz[1]);
	}

	/** A new player brought to {@code state} (ARRANGE), standing at {@code standAt}. */
	private static PlaythroughPlayer playerIn(Late ctx, BlockPos standAt, LateState state, @Nullable Speaker speaker) {
		PlaythroughPlayer player = PlaythroughPlayer.create(ctx.helper, standAt);
		reach(ctx, player, state, speaker);
		return player;
	}

	/**
	 * ARRANGE: {@code state} through the checkpoints. A leader founds a line (and, from the covenant on, its Fane at the
	 * shared heart); anyone else gets the covenant proof alone, so no line is made for them; a member then joins the
	 * shared leader's line; a leader with a recruit records {@code speaker} (or the spare Vicar when the speaker cannot
	 * be recruited) as recruited.
	 */
	private static void reach(Late ctx, PlaythroughPlayer player, LateState state, @Nullable Speaker speaker) {
		Stage stage = state.stage();
		LineRole role = state.role();
		PlaythroughStates.illuminatusD5(player);
		boolean leads = role == LineRole.LEADER || role == LineRole.LEADER_WITH_RECRUIT;
		if (leads) {
			PlaythroughStates.foundedLineD5(player);
			if (stage.covenant()) PlaythroughStates.covenantD5(player, ctx.heart);
		} else if (stage.covenant()) {
			PlaythroughStates.covenantProof(player);
		}
		switch (stage) {
			case D6 -> PlaythroughStates.sanctifiedD6(player, ctx.heart);
			case D6_LIVING -> PlaythroughStates.livingCovenantD6(player);
			case D7 -> PlaythroughStates.archonD7(player);
			case D7_COMMUNED -> PlaythroughStates.communedD7(player);
			case D7_CHOICE -> PlaythroughStates.choicePendingD7(player);
			case D7_SILENT -> PlaythroughStates.silentPendingD7(player);
			case D7_VESPER -> PlaythroughStates.vesperBeatenD7(player);
			case D7_APOTHEOS_PENDING -> PlaythroughStates.apotheosPendingD7(player);
			case D8_APOTHEOS -> PlaythroughStates.apotheosD8(player);
			case D8_SILENTIUM -> PlaythroughStates.silentiumD8(player);
			case PURIFYING -> HemoCapabilityAccess.getUnstainedProgress(player.player()).setBegunPurification(true);
			case CLARITY -> {
				var unstained = HemoCapabilityAccess.getUnstainedProgress(player.player());
				unstained.setBegunPurification(true);
				unstained.setClarityUnlocked(true);
			}
			default -> {
			}
		}
		if (role == LineRole.MEMBER) PlaythroughStates.memberOf(player, ctx.leader());
		if (role == LineRole.LEADER_WITH_RECRUIT) {
			Entity recruit = speaker != null && speaker.recruitable() ? speaker.entity() : ctx.spare();
			PlaythroughStates.recruited(player, recruit);
		}
		switch (state.extra()) {
			case COURT_GUEST -> courtGuest(player);
			case ACCLIMATED -> PlaythroughStates.acclimated(player, 600);
			case BOTH -> {
				courtGuest(player);
				PlaythroughStates.acclimated(player, 600);
			}
			default -> {
			}
		}
		// ARRANGE: the Monolith answers only a player credited with crafting one
		if (ctx.monolith != null) MachineAccessEvents.awardMachineCrafted(player.player(), BlockInit.sanguine_monolith.get());
		if (state.hand() == Hand.ITEM) player.give(new ItemStack(ItemInit.sanguine_quintessence.get()));
	}

	/** ARRANGE: the Court known and every house's visit act recorded (the acts {@code courtStanding} records for GUEST). */
	private static void courtGuest(PlaythroughPlayer player) {
		PlaythroughStates.courtKnown(player, CourtProgress.Route.FOUND);
		for (CourtHouse house : CourtHouse.values()) {
			CourtLife.recordAct(player.player(), house, CourtLife.VISIT_ACT, CourtStandingRules.ActKind.VISIT);
		}
	}

	// =================================================================================================================
	// Speakers
	// =================================================================================================================

	/** An entity speaker, or the Monolith (a block, entity id -1). */
	private record Speaker(String label, @Nullable Entity entity, @Nullable BlockPos block) {
		int entityId() {
			return entity != null ? entity.getId() : SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID;
		}

		/** The NPCs the reference names as recruitable: the Vicar, the Alchemist and the Mnemonist. */
		boolean recruitable() {
			return entity instanceof HarbingerVicarEntity || entity instanceof HarbingerAlchemistEntity
					|| entity instanceof HarbingerMnemonistEntity;
		}

		boolean alive() {
			return entity == null || entity.isAlive() && !entity.isRemoved();
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static String slug(Role role) {
		return role == Role.VOTARY_WAYFARER ? "votaryWayfarer" : role.name().toLowerCase(Locale.ROOT);
	}

	private static Speaker spawn(GameTestHelper helper, Role role, BlockPos at) {
		return new Speaker(slug(role), PlaythroughNpcs.spawn(helper, role, at), null);
	}

	private static List<Speaker> wing(PlaythroughSites.OutpostWing wing) {
		List<Speaker> speakers = new ArrayList<>();
		for (Map.Entry<Role, Entity> resident : wing.residents().entrySet()) {
			speakers.add(new Speaker(slug(resident.getKey()), resident.getValue(), null));
		}
		return speakers;
	}

	private static Speaker monolith(Late ctx) {
		return new Speaker("monolith", null, ctx.monolith());
	}

	/** The eighteen Court roles in rows at relative z {@code z0} and {@code z0 + 4}. */
	private static List<Speaker> court(GameTestHelper helper, int z0, CourtRole... roles) {
		CourtRole[] placed = roles.length == 0 ? CourtRole.values() : roles;
		List<Speaker> speakers = new ArrayList<>();
		for (int i = 0; i < placed.length; i++) {
			BlockPos pos = PlaythroughSites.stand(helper, 2 + (i % 9) * 3, z0 + (i / 9) * 4);
			speakers.add(new Speaker("court:" + placed[i].id(), PlaythroughNpcs.court(helper, placed[i], pos), null));
		}
		return speakers;
	}

	/** The eight performers and the Ringmaster (or {@code roles}) in a row at relative z {@code z}. */
	private static List<Speaker> circus(GameTestHelper helper, int z, CircusRole... roles) {
		List<CircusRole> placed = new ArrayList<>();
		if (roles.length == 0) {
			placed.addAll(CircusRole.performers());
			placed.add(CircusRole.RINGMASTER);
		} else {
			placed.addAll(List.of(roles));
		}
		List<Speaker> speakers = new ArrayList<>();
		for (int i = 0; i < placed.size(); i++) {
			BlockPos pos = PlaythroughSites.stand(helper, 2 + i * 3, z);
			speakers.add(new Speaker("circus:" + placed.get(i).name().toLowerCase(Locale.ROOT),
					PlaythroughNpcs.circus(helper, placed.get(i), pos), null));
		}
		return speakers;
	}

	/** ARRANGE movement: within reach of {@code speaker}. */
	private static void goTo(PlaythroughPlayer player, Speaker speaker) {
		if (speaker.block() != null) player.moveTo(speaker.block().south(2));
		else if (speaker.entity() != null) player.approach(speaker.entity());
	}

	/**
	 * A right click on {@code speaker} with {@code hand} (an empty hand, or the Quintessence taken in hand), as a player
	 * does it. A Harbinger that turns on the player (a Clarity-bearer) is calmed again afterwards, so the next case
	 * starts clean.
	 */
	private static Optional<DialogueView> talk(PlaythroughPlayer player, Speaker speaker, Hand hand) {
		if (!speaker.alive()) return Optional.empty();
		goTo(player, speaker);
		boolean held = hand == Hand.ITEM && player.count(ItemInit.sanguine_quintessence.get()) > 0;
		if (held) player.wield(ItemInit.sanguine_quintessence.get());
		else PlaythroughRites.emptyHand(player);
		if (speaker.block() != null) {
			return held ? player.useOnForDialogue(speaker.block()) : player.tryTalkToBlock(speaker.block());
		}
		Optional<DialogueView> view = player.tryTalkTo(speaker.entity());
		if (speaker.entity() instanceof Mob mob && mob.getTarget() == player.player()) mob.setTarget(null);
		return view;
	}

	/** Chooses an offered {@code eventId} at {@code speaker} (talk to it first). */
	private static Outcome choose(PlaythroughPlayer player, Speaker speaker, String eventId) {
		goTo(player, speaker);
		return speaker.block() != null ? player.chooseAtBlock(speaker.block(), eventId)
				: player.choose(speaker.entity(), eventId);
	}

	/** Whether silence from {@code speaker} is the design for this state, not a dead click. */
	private static boolean silentByDesign(LateState state, Speaker speaker, PlaythroughPlayer player) {
		if (state.stage() == Stage.CLARITY) return true; // the Harbingers turn on a Clarity-bearer
		if (speaker.entity() instanceof CircusRingmasterEntity) {
			return !CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player.player()));
		}
		return false;
	}

	// =================================================================================================================
	// The shared sweep driver
	// =================================================================================================================

	/** Findings keyed by speaker and problem, with the states that showed each, so one bug is one line. */
	private static final class Findings {
		private final Map<String, List<String>> seen = new LinkedHashMap<>();

		void add(Object speaker, String problem, Object state) {
			seen.computeIfAbsent(speaker + ": " + problem, key -> new ArrayList<>()).add(String.valueOf(state));
		}

		void add(String finding) {
			seen.computeIfAbsent(finding, key -> new ArrayList<>());
		}

		List<String> lines() {
			List<String> lines = new ArrayList<>();
			for (Map.Entry<String, List<String>> entry : seen.entrySet()) {
				List<String> states = entry.getValue();
				lines.add(states.isEmpty() ? entry.getKey() : entry.getKey() + " [" + states.size() + " state(s): "
						+ String.join(", ", states.subList(0, Math.min(states.size(), 4)))
						+ (states.size() > 4 ? ", ..." : "") + "]");
			}
			return lines;
		}

		boolean isEmpty() {
			return seen.isEmpty();
		}
	}

	private interface Visitor {
		void visit(LateState state, PlaythroughPlayer player, Speaker speaker, Optional<DialogueView> view, Findings findings);
	}

	/**
	 * For every state in {@code grid}, one tick each: a new player at {@code standAt} brought to the state, a right
	 * click on every speaker in turn (with the state's hand), and {@code visitor} given what each served. The player
	 * goes offline afterwards.
	 */
	private static void sweep(Late ctx, String scenario, List<Speaker> speakers, List<LateState> grid, BlockPos standAt,
			Visitor visitor, Findings findings, @Nullable Runnable atEnd) {
		GameTestHelper helper = ctx.helper;
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(ctx::leader);
		for (LateState state : grid) {
			clock.then(() -> {
				Speaker first = speakers.isEmpty() ? null : speakers.getFirst();
				PlaythroughPlayer player = playerIn(ctx, standAt, state, first);
				try {
					for (Speaker speaker : speakers) {
						if (!speaker.alive()) {
							PlaythroughLog.note(helper, scenario, state + " " + speaker + ": not alive, skipped");
							continue;
						}
						visitor.visit(state, player, speaker, talk(player, speaker, state.hand()), findings);
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> {
			if (atEnd != null) atEnd.run();
			finish(helper, scenario, findings);
		}).run();
	}

	/** Every shown key has English; silence is a finding unless the state is meant to be met with silence. */
	private static Visitor langVisitor(GameTestHelper helper, String scenario) {
		return (state, player, speaker, view, findings) -> {
			if (view.isEmpty()) {
				if (silentByDesign(state, speaker, player)) {
					PlaythroughLog.note(helper, scenario, state + " " + speaker + ": silent by design; said " + tail(player));
					if (speaker.entity() instanceof CircusRingmasterEntity && !LangKeys.exists(RINGMASTER_SILENT)) {
						findings.add(speaker, "his silence line " + RINGMASTER_SILENT + " has no translation", state);
					}
				} else {
					findings.add(speaker, "a right click opened nothing", state);
				}
				return;
			}
			for (String missing : view.get().missingLangKeys()) {
				// A successor speaks under the personal name its Succession rite drew (a proper name, not a key).
				if (missing.startsWith("speaker name") && speaker.entity() instanceof ProfessionalHarbingerEntity npc
						&& npc.isSuccessor()) {
					continue;
				}
				findings.add(speaker, "no translation for " + missing, state);
			}
			for (String literal : view.get().literalLines()) {
				PlaythroughLog.note(helper, scenario, state + " " + speaker + ": literal line (not a key) " + literal);
			}
			PlaythroughLog.note(helper, scenario, state + " " + speaker + ": " + view.get().describe());
		};
	}

	private static String tail(PlaythroughPlayer player) {
		List<Outcome.ChatLine> chat = player.chat();
		return chat.subList(Math.max(0, chat.size() - 3), chat.size()).toString();
	}

	// =================================================================================================================
	// langKeysSweep_<npc> (E)
	// =================================================================================================================

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_vicar(GameTestHelper helper) {
		langKeysSweep(helper, Role.VICAR);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_alchemist(GameTestHelper helper) {
		langKeysSweep(helper, Role.ALCHEMIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_mnemonist(GameTestHelper helper) {
		langKeysSweep(helper, Role.MNEMONIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_artificer(GameTestHelper helper) {
		langKeysSweep(helper, Role.ARTIFICER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_anchorite(GameTestHelper helper) {
		langKeysSweep(helper, Role.ANCHORITE);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_voyager(GameTestHelper helper) {
		langKeysSweep(helper, Role.VOYAGER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_votaryWayfarer(GameTestHelper helper) {
		langKeysSweep(helper, Role.VOTARY_WAYFARER);
	}

	private static void langKeysSweep(GameTestHelper helper, Role role) {
		String scenario = "langKeysSweep_" + slug(role);
		Late ctx = new Late(helper);
		Speaker speaker = spawn(helper, role, helper.absolutePos(NPC));
		sweep(ctx, scenario, List.of(speaker), fullGrid(), helper.absolutePos(STAND), langVisitor(helper, scenario),
				new Findings(), null);
	}

	/** The Sanguine Monolith, spoken to as a block, empty-handed and with a held item (the inquiry). */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_monolith(GameTestHelper helper) {
		String scenario = "langKeysSweep_monolith";
		Late ctx = new Late(helper);
		Speaker stone = monolith(ctx);
		sweep(ctx, scenario, List.of(stone), fullGrid(), helper.absolutePos(STAND), langVisitor(helper, scenario),
				new Findings(), null);
	}

	/**
	 * Successor Vicar, Alchemist and Mnemonist born to the shared leader's line: served to that line's members at every
	 * stage, to the leader, and refused (the idle tree) to a stranger with a line of their own and to a player with none.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_successor(GameTestHelper helper) {
		String scenario = "langKeysSweep_successor";
		Late ctx = new Late(helper);
		List<Speaker> successors = successors(ctx);
		List<LateState> grid = successorGrid();
		Findings findings = new Findings();
		Visitor visitor = langVisitor(helper, scenario);
		sweep(ctx, scenario, successors, grid, helper.absolutePos(STAND), visitor, findings, () -> {
			for (Speaker speaker : successors) {
				visitor.visit(new LateState(Stage.D5_COVENANT, LineRole.LEADER), ctx.leader(), speaker,
						talk(ctx.leader(), speaker, Hand.EMPTY), findings);
			}
		});
	}

	private static List<Speaker> successors(Late ctx) {
		GameTestHelper helper = ctx.helper;
		List<Speaker> speakers = new ArrayList<>();
		Role[] roles = {Role.VICAR, Role.ALCHEMIST, Role.MNEMONIST};
		for (int i = 0; i < roles.length; i++) {
			ProfessionalHarbingerEntity npc = PlaythroughNpcs.successor(helper, roles[i],
					PlaythroughSites.stand(helper, 4 + i * 4, 16), ctx.leader()); // ARRANGE: born to the shared line
			speakers.add(new Speaker("successor " + slug(roles[i]), npc, null));
		}
		return speakers;
	}

	private static List<LateState> successorGrid() {
		List<LateState> grid = new ArrayList<>();
		for (Stage stage : Stage.values()) if (!stage.special()) grid.add(new LateState(stage, LineRole.MEMBER));
		grid.add(new LateState(Stage.D5, LineRole.LEADER)); // a stranger with a line of their own
		grid.add(new LateState(Stage.D5_COVENANT, LineRole.LEADER));
		grid.add(new LateState(Stage.D5, LineRole.NONE));
		return grid;
	}

	/** The eighteen Court roles over the faction grid, with the Court unknown and as every house's guest. */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_court(GameTestHelper helper) {
		String scenario = "langKeysSweep_court";
		Late ctx = new Late(helper);
		List<Speaker> members = court(helper, 3);
		sweep(ctx, scenario, members, factionGrid(Extra.NONE, Extra.COURT_GUEST), PlaythroughSites.stand(helper, 14, 14),
				langVisitor(helper, scenario), new Findings(), null);
	}

	/**
	 * The eight performers and the Ringmaster over the faction grid, unacclimated and at 600 (past the pact gate). The
	 * Ringmaster's silence below the gate is the design; the line he says instead must have English.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void langKeysSweep_circus(GameTestHelper helper) {
		String scenario = "langKeysSweep_circus";
		Late ctx = new Late(helper);
		List<Speaker> troupe = circus(helper, 6);
		sweep(ctx, scenario, troupe, factionGrid(Extra.NONE, Extra.ACCLIMATED), PlaythroughSites.stand(helper, 14, 14),
				langVisitor(helper, scenario), new Findings(), null);
	}

	// =================================================================================================================
	// noDeadOrUnreachableNodesSweep (E)
	// =================================================================================================================

	/**
	 * Every Harbinger speaker, the Monolith and the line's successors over the full grid: no option or card points at a
	 * missing node, no reachable node leaves the player with nothing to choose ({@code deadEnds}), and no reachable
	 * node is left with no options ({@code emptiedNodes}). Nodes a tree carries but no option reaches
	 * ({@code unreachableNodes}) are logged per speaker with the states, since a tree may carry a node for another
	 * state; an event that sits only in such nodes in every swept state is logged as never reachable.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1600, batch = SWEEP_BATCH)
	public static void noDeadOrUnreachableNodesSweep(GameTestHelper helper) {
		String scenario = "noDeadOrUnreachableNodesSweep";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		speakers.addAll(successors(ctx));
		Reachability reachability = new Reachability();
		sweep(ctx, scenario, speakers, fullGrid(), outpost.playerSpot(), deadEndVisitor(reachability), new Findings(),
				() -> reachability.log(helper, scenario));
	}

	/** Court and Circus speakers over the faction grid, as {@link #noDeadOrUnreachableNodesSweep}. */
	@GameTest(template = EMPTY, timeoutTicks = 1600, batch = SWEEP_BATCH)
	public static void noDeadOrUnreachableNodesSweep_courtAndCircus(GameTestHelper helper) {
		String scenario = "noDeadOrUnreachableNodesSweep_courtAndCircus";
		Late ctx = new Late(helper);
		List<Speaker> speakers = new ArrayList<>(court(helper, 3));
		speakers.addAll(circus(helper, 17));
		Reachability reachability = new Reachability();
		sweep(ctx, scenario, speakers, factionGrid(Extra.NONE, Extra.BOTH), PlaythroughSites.stand(helper, 14, 13),
				deadEndVisitor(reachability), new Findings(), () -> reachability.log(helper, scenario));
	}

	/** Per speaker: the events seen reachable in any state, and those seen only in unreachable nodes. */
	private static final class Reachability {
		private final Map<String, Set<String>> reached = new TreeMap<>();
		private final Map<String, Map<String, Set<String>>> hidden = new TreeMap<>();

		void record(LateState state, Speaker speaker, DialogueView view) {
			reached.computeIfAbsent(speaker.label(), key -> new LinkedHashSet<>()).addAll(view.shownEvents());
			for (String nodeId : view.unreachableNodes()) {
				DialogueNode node = view.tree().getNode(nodeId);
				if (node == null) continue;
				for (DialogueOption option : node.options()) {
					if (option.eventId() == null || option.eventId().isEmpty()) continue;
					hidden.computeIfAbsent(speaker.label(), key -> new TreeMap<>())
							.computeIfAbsent(option.eventId() + " (node " + nodeId + ")", key -> new LinkedHashSet<>())
							.add(state.toString());
				}
			}
		}

		void log(GameTestHelper helper, String scenario) {
			for (Map.Entry<String, Map<String, Set<String>>> speaker : hidden.entrySet()) {
				Set<String> seen = reached.getOrDefault(speaker.getKey(), Set.of());
				for (Map.Entry<String, Set<String>> event : speaker.getValue().entrySet()) {
					String id = event.getKey().substring(0, event.getKey().indexOf(' '));
					String verdict = seen.contains(id) ? "reachable elsewhere" : "NEVER REACHABLE in any swept state";
					if (speaker.getKey().equals("circus:threadkeeper") && !seen.contains(id)) {
						// DECISION CT-5: the Threadkeeper's An Empty Ring lesson node is unreachable. Options: add the
						// option / drop the node. Logged for the user's ruling, not failed.
						verdict += " (DECISION CT-5)";
					}
					PlaythroughLog.note(helper, scenario, "unreachable node " + speaker.getKey() + ": " + event.getKey()
							+ " in " + event.getValue().size() + " state(s); " + verdict);
				}
			}
		}
	}

	private static Visitor deadEndVisitor(Reachability reachability) {
		return (state, player, speaker, view, findings) -> {
			if (view.isEmpty()) return;
			reachability.record(state, speaker, view.get());
			Set<String> problems = new LinkedHashSet<>(view.get().deadEnds());
			for (String emptied : view.get().emptiedNodes()) problems.add(emptied + " is left with no options");
			for (String problem : problems) findings.add(speaker, "dead end: " + problem, state);
		};
	}

	// =================================================================================================================
	// successionByPlay (X LF-15)
	// =================================================================================================================

	/**
	 * FIX: LF-15 by its player path. A D5 member of a line with a Fane talks to an original Vicar: the Succession cards
	 * are on the hub. The explain card opens the teaching; the bequest card leads to the consent node, whose accept
	 * grants the bequest; the next talk shows the donate card, and its confirm draws the donation with the held Living
	 * Syringe (a plain vial is refused for a Vicar's blood). Then a
	 * successor born to the line (ARRANGE: the Rite of Hematic Succession is played by {@code runSuccessionGameTestServer})
	 * serves the line's member with no emptied node, and an outsider gets its idle line, which the footer's Leave
	 * closes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void successionByPlay(GameTestHelper helper) {
		String scenario = "successionByPlay";
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE: a line with its Fane
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, leader); // ARRANGE
		Entity vicar = PlaythroughNpcs.spawn(helper, Role.VICAR, PlaythroughSites.stand(helper, 12, 8));
		DialogueView first = member.approach(vicar).talkTo(vicar);
		first.assertLangKeys(helper, "the Vicar with Succession").assertNoDeadEnds(helper, "the Vicar with Succession");
		String explainCard = DialogueTopic.conversationId("succession_explain");
		String consentCard = DialogueTopic.conversationId("succession_consent");
		helper.assertTrue(first.topicById(explainCard).isPresent() && first.topicById(consentCard).isPresent(),
				"the hub shows the Succession explain and bequest cards: " + first.describe());
		Outcome explained = member.openTopic(vicar, explainCard);
		PlaythroughLog.note(helper, scenario, "the explain card: " + explained.describe());
		DialogueNode consent = first.tree().getNode("succession_consent");
		PlaythroughLog.note(helper, scenario, "the consent node offers " + (consent == null ? "nothing" : consent.options().stream()
				.map(DialogueOption::text).toList()) + " (accept only; leaving is the footer's Back)");
		helper.assertTrue(first.offers(SuccessionDialogue.BEQUEST), "the bequest card leads to an accept option");
		member.choose(vicar, SuccessionDialogue.BEQUEST).said("hemomancy.succession.bequest_granted");

		DialogueView second = member.talkTo(vicar);
		String donateCard = DialogueTopic.conversationId("succession_donate");
		helper.assertTrue(second.topicById(donateCard).isPresent(), "after the bequest the hub shows the donate card: "
				+ second.describe());
		second.assertNoDeadEnds(helper, "the Vicar after the bequest");
		// A Vicar's blood needs the Living Syringe (a plain vial is refused): the First Separation tools (syringe and
		// rack) a D5 carries are used; supplied only when missing (ARRANGE).
		if (member.count(ItemInit.living_syringe.get()) == 0) member.give(new ItemStack(ItemInit.living_syringe.get()));
		if (member.count(ItemInit.vial_rack.get()) == 0) member.give(new ItemStack(ItemInit.vial_rack.get()));
		member.wield(ItemInit.living_syringe.get());
		Outcome donated = member.choose(vicar, SuccessionDialogue.DONATE);
		PlaythroughLog.note(helper, scenario, "the donate card's confirm: " + donated.describe());
		donated.said("hemomancy.succession.donated");

		ProfessionalHarbingerEntity successor = PlaythroughNpcs.successor(helper, Role.VICAR, PlaythroughSites.stand(helper, 20, 8),
				leader); // ARRANGE: born to the line by the Succession rite
		DialogueView served = member.approach(successor).talkTo(successor);
		// The successor's speaker name is the proper name its Succession rite drew, not a key.
		List<String> untranslated = served.missingLangKeys().stream().filter(key -> !key.startsWith("speaker name:")).toList();
		helper.assertTrue(untranslated.isEmpty(), "the successor to its line's member shows keys with no translation: " + untranslated);
		served.assertNoDeadEnds(helper, "the successor to its line");
		helper.assertTrue(served.emptiedNodes().isEmpty(), "the successor's trimmed tree leaves no emptied node: "
				+ served.emptiedNodes());
		PlaythroughPlayer outsider = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughStates.foundedLineD5(outsider); // ARRANGE: a line of their own
		DialogueView refused = outsider.approach(successor).talkTo(successor);
		PlaythroughLog.note(helper, scenario, "the successor to an outsider: " + refused.describe());
		helper.assertTrue(refused.missingLangKeys().stream().allMatch(key -> key.startsWith("speaker name:")),
				"the successor's idle line to an outsider has English: " + refused.missingLangKeys());
		refused.assertNoDeadEnds(helper, "the successor to an outsider");
		helper.assertTrue(refused.lineKeys().contains("hemomancy.succession.unavailable"), "an outsider is told the successor will"
				+ " not serve them: " + refused.describe());
		helper.succeed();
	}

	// =================================================================================================================
	// rootOptionsHaveCards (X LF-15)
	// =================================================================================================================

	/**
	 * FIX: LF-15. The hub screen draws cards, never the root's options, and {@code SuccessionDialogue.decorate} (run by
	 * {@code DialogueSender} after the speaker's {@code DialogueHubFactory.decorate}) adds the Succession explain,
	 * bequest and donate options to the root, so a D5 founder with a Fane is never shown them. Every root option an
	 * original speaker serves must have a card. On a successor the same pass trims the recruit and release options; the
	 * cards built for them must go with them, leaving no emptied node and no card that leads to nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void rootOptionsHaveCards(GameTestHelper helper) {
		String scenario = "rootOptionsHaveCards";
		Late ctx = new Late(helper);
		PlaythroughPlayer founder = ctx.leader(); // ARRANGE: a D5 founder with a Fane, so Succession is offered
		PlaythroughStates.blood(founder, 2000.0D); // ARRANGE
		Findings findings = new Findings();
		Role[] originals = {Role.VICAR, Role.ALCHEMIST, Role.MNEMONIST, Role.ARTIFICER, Role.ANCHORITE};
		for (int i = 0; i < originals.length; i++) {
			Speaker speaker = spawn(helper, originals[i], PlaythroughSites.stand(helper, 4 + i * 5, 6));
			Optional<DialogueView> view = talk(founder, speaker, Hand.EMPTY);
			if (view.isEmpty()) {
				findings.add(speaker, "a right click opened nothing", "founder");
				continue;
			}
			DialogueNode root = view.get().tree().getNode(view.get().tree().startNodeId());
			boolean succession = root != null && root.options().stream().anyMatch(option ->
					"succession_explain".equals(option.nextNodeId()) || "succession_consent".equals(option.nextNodeId())
							|| Objects.toString(option.eventId(), "").startsWith("succession_"));
			PlaythroughLog.note(helper, scenario, speaker + ": Succession options at the root " + succession + "; "
					+ view.get().describe());
			for (String hidden : view.get().rootOptionsWithoutCard()) {
				findings.add(speaker, "root option with no card: " + hidden, "founder");
			}
		}
		for (Speaker successor : successors(ctx)) {
			Optional<DialogueView> view = talk(founder, successor, Hand.EMPTY);
			if (view.isEmpty()) {
				findings.add(successor, "a right click opened nothing for its own line's leader", "founder");
				continue;
			}
			PlaythroughLog.note(helper, scenario, successor + ": " + view.get().describe());
			for (String emptied : view.get().emptiedNodes()) findings.add(successor, "emptied node " + emptied, "founder");
			for (String deadEnd : view.get().deadEnds()) findings.add(successor, "dead end " + deadEnd, "founder");
			for (DialogueTopic topic : view.get().topics()) {
				String title = topic.titleKey();
				if (title.equals("hemomancy.dialogue.recruit.option.pledge_blood")
						|| title.equals("hemomancy.dialogue.recruit.option.release_blood")) {
					findings.add(successor, "a card for a trimmed option: " + topic.id() + " (" + title + ")", "founder");
				}
			}
			for (String hidden : view.get().rootOptionsWithoutCard()) {
				findings.add(successor, "root option with no card: " + hidden, "founder");
			}
		}
		finish(helper, scenario, findings);
		helper.succeed();
	}

	// =================================================================================================================
	// offeredEventsRouteSweep (E)
	// =================================================================================================================

	/**
	 * Every event a late speaker offers, over the grid: some route owns it (the payload's Memo, Liber and Succession
	 * handling, or a route in {@code DialogueEventRouter}), and choosing it, the way a curious player clicks through,
	 * does something the player can see (an item, a line, an advancement, a payload, a saved change, a degree, a new
	 * node, or a different tree on the next click). {@link #HAZARDS} and bearings are skipped and logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep(GameTestHelper helper) {
		String scenario = "offeredEventsRouteSweep";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		RouteProbe probe = new RouteProbe(helper);
		routeSweep(ctx, scenario, speakers, grid(false, LineRole.values()), outpost.playerSpot(), probe);
	}

	/** Court and Circus speakers (a house lead, a Gardener, the Kindling's champion and Head-tender, two faculty, a performer, the Ringmaster). */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_courtAndCircus(GameTestHelper helper) {
		String scenario = "offeredEventsRouteSweep_courtAndCircus";
		Late ctx = new Late(helper);
		List<Speaker> speakers = new ArrayList<>(court(helper, 3, CourtRole.GARDENER, CourtRole.LIGHTKEEPER,
				CourtRole.HEAD_TENDER, CourtRole.KINDLED_ELDER, CourtRole.TIDEWARDEN));
		speakers.addAll(circus(helper, 17, CircusRole.THREADKEEPER, CircusRole.UNDERSTUDY, CircusRole.FIRE_EATER,
				CircusRole.RINGMASTER));
		RouteProbe probe = new RouteProbe(helper);
		routeSweep(ctx, scenario, speakers, factionGrid(Extra.NONE, Extra.BOTH), PlaythroughSites.stand(helper, 14, 11), probe);
	}

	private static boolean hazard(String eventId) {
		return HAZARDS.contains(eventId) || eventId.endsWith("_bearing") || eventId.endsWith(".bearing");
	}

	private static void routeSweep(Late ctx, String scenario, List<Speaker> speakers, List<LateState> grid,
			BlockPos standAt, RouteProbe probe) {
		GameTestHelper helper = ctx.helper;
		Findings findings = new Findings();
		sweep(ctx, scenario, speakers, grid, standAt, (state, player, speaker, first, found) -> {
			if (first.isEmpty()) return;
			List<String> owners = new ArrayList<>();
			for (String eventId : first.get().reachableEvents()) {
				String owner = probe.owner(eventId, speaker);
				if (owner == null) found.add(speaker, "offers " + eventId + " but no route takes it", state);
				else owners.add(eventId + "->" + owner);
				if (hazard(eventId)) {
					PlaythroughLog.note(helper, scenario, state + " " + speaker + ": skipped " + eventId
							+ " (played by its own band)");
					continue;
				}
				if (!speaker.alive()) break;
				Optional<DialogueView> before = talk(player, speaker, state.hand());
				if (before.isEmpty() || !before.get().offers(eventId)) continue;
				boolean navigates = before.get().option(eventId).map(option -> option.nextNodeId() != null).orElse(false);
				Outcome outcome;
				try {
					outcome = choose(player, speaker, eventId);
				} catch (RuntimeException exception) {
					found.add(speaker, "choosing " + eventId + " threw " + exception, state);
					continue;
				}
				String after = talk(player, speaker, state.hand()).map(DialogueView::describe).orElse("<nothing opened>");
				boolean treeChanged = !after.equals(before.get().describe());
				if (!navigates && !treeChanged && !observable(outcome)) {
					found.add(speaker, eventId + " is a silent no-op", state);
					PlaythroughLog.note(helper, scenario, state + " " + speaker + ": " + eventId + " silent: "
							+ outcome.describe());
				}
			}
			PlaythroughLog.note(helper, scenario, state + " " + speaker + ": " + owners);
		}, findings, null);
	}

	/**
	 * Which handler owns an event id (as round one's probe): the payload's Memo, Liber and Succession handling first,
	 * then {@code DialogueEventRouter}'s routes in order, tried by reflection on a speakerless shadow player standing
	 * apart. Every route matches on the event id before anything else, so the shadow takes whatever a route does; the
	 * drops it makes are removed.
	 */
	private static final class RouteProbe {
		private final GameTestHelper helper;
		private final CapturedPlayer shadow;
		private final List<?> routes;
		private final Method handle;
		private final Map<String, String> owners = new HashMap<>();

		RouteProbe(GameTestHelper helper) {
			this.helper = helper;
			this.shadow = CapturedPlayer.create(helper);
			BlockPos at = at(helper, SHADOW);
			shadow.player.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
			PlaythroughCleanup.onEnd(helper, shadow::close);
			List<?> found = null;
			Method method = null;
			try {
				Field field = DialogueEventRouter.class.getDeclaredField("ROUTES");
				field.setAccessible(true);
				found = (List<?>) field.get(null);
				Class<?> routeType = Class.forName(DialogueEventRouter.class.getName() + "$DialogueRoute", false,
						DialogueEventRouter.class.getClassLoader());
				method = routeType.getDeclaredMethod("handle", DialogueEvent.class);
				method.setAccessible(true);
			} catch (ReflectiveOperationException | RuntimeException exception) {
				helper.fail("DialogueEventRouter.ROUTES could not be read to find an event's owner: " + exception);
			}
			this.routes = found;
			this.handle = method;
		}

		/**
		 * The owner of {@code eventId}: tried with no speaker first; a route that matches on its speaker's type (the
		 * Ringmaster's) is tried again with {@code speaker}'s entity id, far from the shadow, so it finds the speaker
		 * and goes no further.
		 */
		@Nullable
		String owner(String eventId, Speaker speaker) {
			String key = eventId + "@" + (speaker.entity() == null ? "block" : speaker.entity().getType().toString());
			if (owners.containsKey(key)) return owners.get(key);
			String owner = find(eventId, Integer.MAX_VALUE);
			if (owner == null && speaker.entity() != null) {
				owner = find(eventId, speaker.entityId());
				if (owner != null) owner += " (with its speaker)";
			}
			owners.put(key, owner);
			return owner;
		}

		@Nullable
		private String find(String eventId, int entityId) {
			if (MemoHelper.isMemoEvent(eventId)) return "payload memo";
			if (!LiberEntryDefinitions.forDialogueEvent(eventId).isEmpty()) return "payload liber";
			if (eventId.startsWith("succession_")) return "payload succession";
			DialogueEvent event = new DialogueEvent(shadow.player, eventId, entityId);
			try {
				for (int i = 0; i < routes.size(); i++) {
					Object taken;
					try {
						taken = handle.invoke(routes.get(i), event);
					} catch (InvocationTargetException thrown) {
						PlaythroughLog.note(helper, "routeProbe", eventId + " threw in route #" + i
								+ " with no speaker: " + thrown.getCause());
						return "route #" + i + " (threw " + thrown.getCause() + ")";
					}
					if (Boolean.TRUE.equals(taken)) return "route #" + i;
				}
				return null;
			} catch (IllegalAccessException exception) {
				throw new IllegalStateException("Could not call a dialogue route", exception);
			} finally {
				BlockPos at = shadow.player.blockPosition();
				for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4.0D))) {
					drop.discard();
				}
			}
		}
	}

	// =================================================================================================================
	// unofferedEventsRefusedSweep (E)
	// =================================================================================================================

	/** The late events each speaker can grant, by speaker label (Court and Circus by prefix). */
	private static List<String> lateEvents(Speaker speaker) {
		String label = speaker.label();
		if (label.startsWith("court:")) {
			return List.of(RECRUIT, CourtMemberDialogue.FOOD, CourtMemberDialogue.REST, CourtMemberDialogue.GIFT,
					CourtMemberDialogue.OFFER, CourtMemberDialogue.AMENDS, CourtMemberDialogue.LESSON,
					CourtMemberDialogue.JOIN, CourtMemberDialogue.CHALLENGE);
		}
		if (label.equals("circus:ringmaster")) {
			return List.of(RECRUIT, CircusRingmasterEntity.EVENT_ACCEPT, CircusRingmasterEntity.EVENT_REJECT,
					CircusRingmasterEntity.EVENT_REPAIR, CircusRingmasterEntity.EVENT_TRIAL, CircusRingmasterEntity.EVENT_FINALE);
		}
		if (label.startsWith("circus:")) {
			return List.of(RECRUIT, "circus_school_teach", "circus_school_practical", "circus_school_guest",
					"circus_school_testimony", "circus_school_inspect", "circus_school_report", "circus_school_claim");
		}
		return switch (label) {
			case "vicar" -> List.of(RECRUIT, EXPEL, BLUEPRINT, STAINED_MAP, CourtDiscoveryDialogue.VICAR_LETTER,
					HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT);
			case "alchemist" -> List.of(RECRUIT, EXPEL, HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD,
					CourtDiscoveryDialogue.ALCHEMIST_LEAD, CourtDiscoveryDialogue.ALCHEMIST_LETTER,
					VagrantMindInquiryDialogue.BIOLOGY_REPORT, DeepDarkCommissionDialogue.REPORT);
			case "mnemonist" -> List.of(RECRUIT, EXPEL, HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL,
					HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL, HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON,
					VagrantMindInquiryDialogue.MEMORY_REPORT);
			case "artificer" -> List.of(RECRUIT, EXPEL, HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_REWARD, HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING,
					HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT);
			case "anchorite" -> List.of(RECRUIT, EXPEL, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D6);
			case "voyager" -> List.of(RECRUIT, CourtDiscoveryDialogue.VOYAGER_LEARN, CourtDiscoveryDialogue.VOYAGER_CHART,
					CourtDiscoveryDialogue.VOYAGER_LETTER, VoyagerIntroductionDialogue.OBSERVE);
			case "votaryWayfarer" -> List.of(RECRUIT);
			case "monolith" -> List.of(SanguineMonolithDialogueTrees.EVENT_CORNERSTONE, SanguineMonolithDialogueTrees.EVENT_SHATTER);
			default -> List.of();
		};
	}

	/**
	 * In every state, each late event its speaker does not offer is forged to that speaker, from beside it and after a
	 * right click (so a block speaker's conversation is open, as a modified client would have it): the recruit and
	 * expel events at every speaker (the reference names only the Vicar, Alchemist and Mnemonist as recruitable), the
	 * Monolith's cornerstone and shatter, the Artificer's Weight of the Frame and D7 claims, the Vein-Mason D6 events,
	 * the Court custom and challenge events and the Circus school and Ringmaster events. The two revelation choices are
	 * forged as whispers (entity id 0) whenever the choice is not pending. None may give or drop an item, earn an
	 * advancement, change the degree, the bloodline's members or the Archon path, or report a delivered reward.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = SWEEP_BATCH)
	public static void unofferedEventsRefusedSweep(GameTestHelper helper) {
		String scenario = "unofferedEventsRefusedSweep";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		speakers.addAll(court(helper, 3, CourtRole.GARDENER, CourtRole.LIGHTKEEPER, CourtRole.HEAD_TENDER,
				CourtRole.KINDLED_ELDER));
		speakers.addAll(circus(helper, 19, CircusRole.THREADKEEPER, CircusRole.UNDERSTUDY, CircusRole.FIRE_EATER,
				CircusRole.RINGMASTER));
		Findings findings = new Findings();
		Map<String, Integer> forgedBySpeaker = new TreeMap<>();
		List<LateState> grid = grid(false, LineRole.NONE, LineRole.MEMBER, LineRole.LEADER);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(ctx::leader);
		for (LateState state : grid) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), state, null);
				try {
					for (Speaker speaker : speakers) {
						if (!speaker.alive()) continue;
						Optional<DialogueView> view = talk(player, speaker, Hand.EMPTY);
						List<String> offered = view.map(DialogueView::reachableEvents).orElse(List.of());
						for (String eventId : lateEvents(speaker)) {
							if (offered.contains(eventId)) continue;
							if (speaker.block() != null) talk(player, speaker, Hand.EMPTY); // the stone's conversation is open
							else goTo(player, speaker);
							forgedBySpeaker.merge(speaker.label(), 1, Integer::sum);
							forgeExpectingNothing(helper, scenario, player, eventId, speaker.entityId(), speaker,
									"unoffered " + eventId, state, findings);
						}
					}
					if (!player.player().getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)) {
						for (String choice : List.of(WHISPER_SILENCE, WHISPER_EIGHTH)) {
							forgeExpectingNothing(helper, scenario, player, choice, 0, "whisper",
									"forged " + choice + " with no choice pending", state, findings);
						}
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> {
			PlaythroughLog.note(helper, scenario, "forged per speaker " + forgedBySpeaker);
			finish(helper, scenario, findings);
		}).run();
	}

	/** What a forged event may change without the player noticing a reward: the line's members, the path, the degree. */
	private record Watch(int npcMembers, int playerMembers, EnumArchonPath path, int degree) {
		static Watch of(PlaythroughPlayer player) {
			Bloodline line = PlaythroughStates.lineOf(player);
			return new Watch(line == null ? 0 : line.getNpcMemberCount(), line == null ? 0 : line.getPlayerUUIDS().size(),
					HemoCapabilityAccess.getInitiatoryDegree(player.player()).getArchonPath(), player.degree());
		}
	}

	private static void forgeExpectingNothing(GameTestHelper helper, String scenario, PlaythroughPlayer player,
			String eventId, int entityId, Object speaker, String what, LateState state, Findings findings) {
		Watch before = Watch.of(player);
		Outcome outcome;
		try {
			outcome = player.forge(eventId, entityId);
		} catch (RuntimeException exception) {
			findings.add(speaker, what + " threw " + exception, state);
			return;
		}
		Watch after = Watch.of(player);
		List<String> parts = new ArrayList<>();
		String change = change(outcome);
		if (change != null) parts.add(change);
		if (before.npcMembers() != after.npcMembers()) {
			parts.add("the line's NPC members went " + before.npcMembers() + "->" + after.npcMembers());
		}
		if (before.playerMembers() != after.playerMembers()) {
			parts.add("the line's players went " + before.playerMembers() + "->" + after.playerMembers());
		}
		if (before.path() != after.path()) parts.add("the Archon path went " + before.path() + "->" + after.path());
		if (!parts.isEmpty()) findings.add(speaker, what + ": " + String.join("; ", parts), state);
		else if (!"{}".equals(outcome.savedDiff())) {
			PlaythroughLog.note(helper, scenario, state + " " + speaker + ": " + what + " gave nothing, but the save changed "
					+ outcome.savedDiff());
		}
	}

	// =================================================================================================================
	// degreeHintMatchesChapterGate (E)
	// =================================================================================================================

	private record HintCase(String label, LineRole role, Stage stage, boolean chamber, boolean throne, boolean vigil,
			Set<String> expected, @Nullable String fix) {
	}

	private static final String COVENANT = "hemomancy.vicar.covenant.";

	/**
	 * The Vicar's D5 and D6 degree hint names what the next rank's chapter gate
	 * ({@code HarbingerChapterProgression.unmetChapterForTargetDegree}) still wants, for a leader and a member: at D5
	 * the bloodline, then the Fane, then the Bloodline Covenant rite once the covenant is written; at D6 each Living
	 * Covenant part not yet done (in every combination for the leader), then the Hematic Order rite. A hint that names
	 * the rank rite while the gate is unmet, or names a done part, misleads.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void degreeHintMatchesChapterGate(GameTestHelper helper) {
		String scenario = "degreeHintMatchesChapterGate";
		Late ctx = new Late(helper);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		List<HintCase> cases = new ArrayList<>();
		cases.add(new HintCase("D5, no line", LineRole.NONE, Stage.D5, false, false, false,
				Set.of(COVENANT + "found_bloodline"), null));
		cases.add(new HintCase("D5 leader, line founded", LineRole.LEADER, Stage.D5, false, false, false,
				Set.of(COVENANT + "found_fane"), null));
		cases.add(new HintCase("D5 leader, covenant written", LineRole.LEADER, Stage.D5_COVENANT, false, false, false,
				Set.of(COVENANT + "perform_bloodline_rite"), null));
		cases.add(new HintCase("D5 member, no deposit yet", LineRole.MEMBER, Stage.D5, false, false, false,
				Set.of(COVENANT + "found_fane"), "LF-3"));
		cases.add(new HintCase("D5 member, deposit made", LineRole.MEMBER, Stage.D5_COVENANT, false, false, false,
				Set.of(COVENANT + "perform_bloodline_rite"), "LF-3"));
		for (int mask = 0; mask < 8; mask++) {
			boolean chamber = (mask & 1) != 0;
			boolean throne = (mask & 2) != 0;
			boolean vigil = (mask & 4) != 0;
			cases.add(new HintCase("D6 leader, parts " + mask, LineRole.LEADER, Stage.D6, chamber, throne, vigil,
					d6Expected(chamber, throne, vigil), null));
		}
		cases.add(new HintCase("D6 member, no parts", LineRole.MEMBER, Stage.D6, false, false, false,
				d6Expected(false, false, false), null));
		cases.add(new HintCase("D6 member, all parts", LineRole.MEMBER, Stage.D6, true, true, true,
				d6Expected(true, true, true), null));
		Findings findings = new Findings();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(ctx::leader);
		for (HintCase hint : cases) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(ctx, helper.absolutePos(STAND), new LateState(hint.stage(), hint.role()), null);
				try {
					ServerPlayer server = player.player();
					if (hint.chamber()) grant(player, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED); // ARRANGE
					if (hint.throne()) grant(player, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND); // ARRANGE
					if (hint.vigil()) grant(player, HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED); // ARRANGE
					HarbingerChapterProgression.tryCompleteLivingCovenant(server); // ARRANGE: as each part's recorder does
					DialogueView view = player.talkTo(vicar);
					List<String> keys = new ArrayList<>();
					view.node("degree_hint").ifPresent(node -> {
						for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(keys::add);
					});
					HarbingerChapterMilestone unmet = HarbingerChapterProgression.unmetChapterForTargetDegree(server,
							player.degree() + 1);
					PlaythroughLog.note(helper, scenario, hint.label() + ": gate wants " + unmet + "; hint " + keys
							+ " (expected " + hint.expected() + ")");
					String fix = hint.fix() == null ? "" : " (FIX: " + hint.fix() + ")";
					if (keys.isEmpty()) {
						findings.add("vicar", "no degree hint" + fix, hint.label());
						return;
					}
					boolean namesRite = keys.contains(COVENANT + "perform_bloodline_rite")
							|| keys.contains(COVENANT + "perform_hematic_order_rite");
					if (unmet != null && namesRite) {
						findings.add("vicar", "names the rank rite while the gate still wants " + unmet + ": " + keys + fix,
								hint.label());
					}
					if (unmet == null && !namesRite) {
						findings.add("vicar", "the gate is met but the hint does not name the rank rite: " + keys + fix,
								hint.label());
					}
					if (!new LinkedHashSet<>(keys).equals(hint.expected())) {
						findings.add("vicar", "hint " + keys + " is not " + hint.expected() + fix, hint.label());
					}
					if (hint.role() == LineRole.MEMBER && keys.contains(COVENANT + "found_bloodline")) {
						findings.add("vicar", "tells a member to found a bloodline (FIX: LF-3)", hint.label());
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	private static Set<String> d6Expected(boolean chamber, boolean throne, boolean vigil) {
		Set<String> expected = new LinkedHashSet<>();
		if (!chamber) expected.add(COVENANT + "chamber_return");
		if (!throne) expected.add(COVENANT + "bind_throne");
		if (!vigil) expected.add(COVENANT + "complete_vigil");
		if (expected.isEmpty()) expected.add(COVENANT + "perform_hematic_order_rite");
		return expected;
	}

	private static void grant(PlaythroughPlayer player, ResourceLocation advancement) {
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), advancement);
	}

	// =================================================================================================================
	// nextStepPointerSweep (E)
	// =================================================================================================================

	private record Pointer(String label, LateState state, Pattern names, @Nullable String decision) {
	}

	private static Pattern words(String regex) {
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * At each late checkpoint some speaker a player can reach (the outpost wing or the Monolith) names the real next
	 * step: in a line, an option or a card it serves. At the revelation choice the choice itself must be in front of
	 * the player after a relog (the whisper re-sent or queued). States with no pointer whose gap is already a pending
	 * question are logged against it (D7-5 the shatter, D7-8 the Fungal Spine, D7-9 the Severance, D8-3 the Apotheos
	 * rite; D5-5 the Quintessence source is logged at the Fane step); any other is a finding. The ledger's Blood Beneath card is logged beside it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = BATCH)
	public static void nextStepPointerSweep(GameTestHelper helper) {
		String scenario = "nextStepPointerSweep";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		List<Pointer> pointers = List.of(
				new Pointer("D5 with no line: the Rite of Bloodline Founding", new LateState(Stage.D5, LineRole.NONE),
						words("bloodline founding"), null),
				new Pointer("D5 line founded: the Founding Fane", new LateState(Stage.D5, LineRole.LEADER),
						words("founding fane"), null),
				new Pointer("D5 covenant written: the Rite of the Bloodline Covenant",
						new LateState(Stage.D5_COVENANT, LineRole.LEADER), words("bloodline covenant"), null),
				new Pointer("D6: the Living Covenant", new LateState(Stage.D6, LineRole.LEADER),
						words("chamber of will|covenant throne|vigil"), null),
				new Pointer("D6 Living Covenant done: the Rite of the Hematic Order",
						new LateState(Stage.D6_LIVING, LineRole.LEADER), words("rite of the hematic order"), null),
				// DECISION D7-5: no D7 NPC names the shatter, the Seed or the Bloom. Options: a Vicar or Monolith
				// line / discovery and the Liber.
				new Pointer("D7: the Monolith's shatter, the Seed and the Bloom", new LateState(Stage.D7, LineRole.LEADER),
						words("press further|shatter|qliphoth seed|bloom of the qliphoth|qliphoth bloom"), "D7-5"),
				// DECISION D7-8: after Communion no reachable speaker names the Fungal Spine (and the Spine has no item
				// inquiry), so nothing says to project with it. Options: a Vicar or Monolith line / a Spine item inquiry /
				// leave it to the delivery line and the Liber.
				new Pointer("D7 communed: the Fungal Spine", new LateState(Stage.D7_COMMUNED, LineRole.LEADER),
						words("fungal spine"), "D7-8"),
				// DECISION D7-9: a D7 Silence (SILENT_PENDING, Vesper not yet met) hears nothing that names the Severance or
				// the Living Sickle. Options: a Vicar or Mnemonist line / the whisper and the Liber only / keep.
				new Pointer("D7 Silence chosen: the Severance", new LateState(Stage.D7_SILENT, LineRole.LEADER),
						words("\\bsever(ance|ed|ing|s)?\\b|living sickle|prun(e|ing) the bloom"), "D7-9"),
				new Pointer("D7 Vesper beaten: absorb the Memory", new LateState(Stage.D7_VESPER, LineRole.LEADER),
						words("vesper.*(absorb|iron brazier)|(absorb|iron brazier).*vesper"), null),
				// DECISION D8-3: the Apotheos rite is discoverable only through an optional memo or at D8. Options:
				// unlock at APOTHEOS_PENDING / a pointer line / keep.
				new Pointer("D7 Eighth Degree chosen: the Rite of Apotheos",
						new LateState(Stage.D7_APOTHEOS_PENDING, LineRole.LEADER), words("rite of apotheos"), "D8-3"));
		Findings findings = new Findings();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(ctx::leader);
		for (Pointer pointer : pointers) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), pointer.state(), null);
				try {
					Map<String, List<String>> heard = heard(player, speakers, Hand.EMPTY);
					List<String> naming = new ArrayList<>();
					for (Map.Entry<String, List<String>> speaker : heard.entrySet()) {
						for (String text : speaker.getValue()) {
							if (pointer.names().matcher(text).find()) naming.add(speaker.getKey() + ": " + shorten(text));
						}
					}
					PlaythroughLog.note(helper, scenario, pointer.label() + ": named by " + naming.size() + " line(s) "
							+ naming.subList(0, Math.min(3, naming.size())) + "; ledger " + ledgerKey(player));
					if (pointer.state().stage() == Stage.D5 && pointer.state().role() == LineRole.LEADER) {
						// DECISION D5-5 (logged here; probed by quintessenceHasNoPointer): the Fane needs Sanguine Quintessence.
						PlaythroughLog.note(helper, scenario, "D5-5 at the Fane step: a line names where Quintessence comes "
								+ "from: " + namesQuintessenceSource(heard));
					}
					if (naming.isEmpty()) {
						if (pointer.decision() != null) {
							PlaythroughLog.note(helper, scenario, "DECISION " + pointer.decision() + " " + pointer.label()
									+ ": no speaker names it");
						} else {
							findings.add("every reachable speaker", "no pointer to the next step", pointer.label());
						}
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> {
			// The revelation choice: after a relog the whisper must be there to choose (re-sent or queued).
			PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), new LateState(Stage.D7_CHOICE, LineRole.LEADER), null);
			try {
				player.relog();
				int queued = player.pendingWhispers().size();
				// The player takes up "Listen when ready" and reads through the inbox, oldest first, until the choice
				// opens (CHAIN-2: memo whispers may come first). A memo alone in the inbox is not the choice.
				int opened = openUntilChoice(player);
				PlaythroughLog.note(helper, scenario, "D7 choice pending, after a relog: queued " + queued + "; the choice "
						+ (opened > 0 ? "opened as whisper " + opened : "never opened") + ", last whisper "
						+ player.whisper().map(DialogueView::describe).orElse("none"));
				if (opened == 0) findings.add("the whisper inbox", "the revelation choice is not in front of the player after a relog",
						"D7 choice pending");
			} finally {
				player.offline();
			}
			for (Stage end : List.of(Stage.D8_APOTHEOS, Stage.D8_SILENTIUM)) {
				PlaythroughPlayer done = playerIn(ctx, outpost.playerSpot(), new LateState(end, LineRole.LEADER), null);
				try {
					heard(done, speakers, Hand.EMPTY);
					PlaythroughLog.note(helper, scenario, end + ": the path's end; ledger " + ledgerKey(done));
				} finally {
					done.offline();
				}
			}
		}).then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * Opens the player's whisper inbox as "Listen when ready" does, closing each whisper that is not the revelation
	 * choice, up to eight times. Returns the 1-based position the choice opened at, or 0 when it never did.
	 */
	private static int openUntilChoice(PlaythroughPlayer player) {
		for (int opened = 1; opened <= 8; opened++) {
			if (player.pendingWhispers().isEmpty() && player.whisper().isEmpty()) return 0;
			player.openWhisper();
			Optional<DialogueView> view = player.whisper();
			if (view.isPresent() && (view.get().offers(WHISPER_SILENCE) || view.get().offers(WHISPER_EIGHTH))) return opened;
			Optional<java.util.UUID> id = player.openWhisperId();
			if (id.isEmpty()) return 0;
			player.closeWhisper(id.get());
		}
		return 0;
	}

	/**
	 * One next-step pointer as its own probe: at {@code pointer}'s state, some speaker a player can reach (the outpost
	 * wing or the Monolith) names the step; fails with the decision id when none does.
	 */
	private static void pointerProbe(GameTestHelper helper, String scenario, Pointer pointer) {
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		PlaythroughClock.start(helper).then(ctx::leader).then(() -> {
			PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), pointer.state(), null);
			List<String> naming = new ArrayList<>();
			try {
				for (Map.Entry<String, List<String>> speaker : heard(player, speakers, Hand.EMPTY).entrySet()) {
					for (String text : speaker.getValue()) {
						if (pointer.names().matcher(text).find()) naming.add(speaker.getKey() + ": " + shorten(text));
					}
				}
			} finally {
				player.offline();
			}
			PlaythroughLog.note(helper, scenario, pointer.label() + ": named by " + naming);
			helper.assertTrue(!naming.isEmpty(), "DECISION " + pointer.decision() + ": some reachable speaker names "
					+ pointer.label());
		}).run();
	}

	/** At D7 after Communion, some reachable speaker names the Fungal Spine the player now carries. */
	// DECISION D7-8: after Communion no reachable speaker names the Fungal Spine, and the Spine has no item inquiry.
	// Options: a Vicar or Monolith line / a Spine item inquiry / the delivery line and the Liber only.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void springPointerAfterCommunion(GameTestHelper helper) {
		pointerProbe(helper, "springPointerAfterCommunion", new Pointer("D7 communed: the Fungal Spine",
				new LateState(Stage.D7_COMMUNED, LineRole.LEADER), words("fungal spine"), "D7-8"));
	}

	/** A D7 Silence who has not met Vesper hears some reachable speaker name the Severance or the Living Sickle. */
	// DECISION D7-9: a D7 Silence (SILENT_PENDING, Vesper not yet met) hears nothing that names the Severance or the
	// Living Sickle. Options: a Vicar or Mnemonist line / the whisper and the Liber only / keep.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void severancePointerForSilence(GameTestHelper helper) {
		pointerProbe(helper, "severancePointerForSilence", new Pointer("D7 Silence chosen: the Severance",
				new LateState(Stage.D7_SILENT, LineRole.LEADER),
				words("\\bsever(ance|ed|ing|s)?\\b|living sickle|prun(e|ing) the bloom"), "D7-9"));
	}

	/** Every line and option text (English) each speaker serves now, by speaker. */
	private static Map<String, List<String>> heard(PlaythroughPlayer player, List<Speaker> speakers, Hand hand) {
		Map<String, List<String>> heard = new LinkedHashMap<>();
		for (Speaker speaker : speakers) {
			Optional<DialogueView> view = talk(player, speaker, hand);
			if (view.isEmpty()) continue;
			List<String> texts = heard.computeIfAbsent(speaker.label() + (hand == Hand.ITEM ? " (held item)" : ""),
					key -> new ArrayList<>());
			for (DialogueNode node : view.get().nodes().values()) {
				for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(key -> texts.add(english(key)));
				for (DialogueOption option : node.options()) Dialogue.lineKey(option.text()).ifPresent(key -> texts.add(english(key)));
			}
			for (DialogueTopic topic : view.get().topics()) texts.add(english(topic.titleKey()));
		}
		return heard;
	}

	private static String ledgerKey(PlaythroughPlayer player) {
		if (player.count(ItemInit.harbinger_assignment_ledger.get()) == 0) return "not carried";
		return player.ledger().describe();
	}

	private static String shorten(String text) {
		return text.length() <= 90 ? text : text.substring(0, 87) + "...";
	}

	private static final Pattern QUINTESSENCE = words("quintessence");
	private static final Pattern QUINTESSENCE_SOURCE = words("exsanguination|crystalli[sz]|sacrific|lifeblood");

	private static boolean namesQuintessenceSource(Map<String, List<String>> heard) {
		for (List<String> texts : heard.values()) {
			for (String text : texts) {
				if (QUINTESSENCE.matcher(text).find() && QUINTESSENCE_SOURCE.matcher(text).find()) return true;
			}
		}
		return false;
	}

	// =================================================================================================================
	// mainStepsAreQuests (D NPC-3)
	// =================================================================================================================

	/**
	 * DECISION NPC-3: the D5 and D6 Main guidance is the Vicar's "what degree next" lore card, whose notice sigil clears
	 * after one read, with no Quest Work card; and {@code complete_vigil} names only a recruited Vicar as the Vigil's
	 * helper. Options: a Quest card with a sigil per step / extra lines / keep. Expects, for a D5 founder (the Fane
	 * next), a D6 Sanctified leader (the Living Covenant next) and a D5 member: the guidance on a Quests card whose sigil
	 * stays lit after it is read while the step is open; and the Vigil line naming the other helpers.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void mainStepsAreQuests(GameTestHelper helper) {
		String scenario = "mainStepsAreQuests";
		Late ctx = new Late(helper);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		List<String> findings = new ArrayList<>();
		for (LateState state : List.of(new LateState(Stage.D5, LineRole.LEADER), new LateState(Stage.D6, LineRole.LEADER),
				new LateState(Stage.D5, LineRole.MEMBER))) {
			PlaythroughPlayer player = playerIn(ctx, helper.absolutePos(STAND), state, null);
			try {
				DialogueView view = player.talkTo(vicar);
				Optional<DialogueTopic> card = view.topics().stream()
						.filter(topic -> "degree_hint".equals(topic.targetNodeId())).findFirst();
				if (card.isEmpty()) {
					findings.add(state + ": no card leads to the guidance; " + view.describe());
					continue;
				}
				DialogueTopic before = card.get();
				if (before.lore() && before.unread()) player.openTopic(vicar, before.id());
				DialogueView again = player.talkTo(vicar);
				Optional<DialogueTopic> after = again.topicById(before.id());
				boolean quest = before.category() == DialogueCategory.QUESTS;
				boolean anyQuest = !again.topics(DialogueCategory.QUESTS).isEmpty();
				String row = state + ": guidance card " + before.id() + " in " + before.category() + " (lore " + before.lore()
						+ "), sigil " + before.attention() + " before reading, "
						+ after.map(topic -> topic.attention().toString()).orElse("gone") + " after; Quests cards "
						+ again.topics(DialogueCategory.QUESTS).stream().map(DialogueTopic::titleKey).toList();
				PlaythroughLog.note(helper, scenario, row);
				if (!quest || after.isEmpty() || after.get().attention() == DialogueAttention.NONE) {
					findings.add(row + (anyQuest ? "" : " (no Quest Work card at all)"));
				}
			} finally {
				player.offline();
			}
		}
		String vigil = english(COVENANT + "complete_vigil");
		boolean namesOthers = words("alchemist|mnemonist|member|any recruit|recruited harbinger").matcher(vigil).find();
		PlaythroughLog.note(helper, scenario, "complete_vigil reads: " + vigil + " (names a helper other than a Vicar: "
				+ namesOthers + ")");
		if (!namesOthers) findings.add("complete_vigil names only a recruited Vicar as the Vigil's helper");
		for (String finding : findings) PlaythroughLog.note(helper, scenario, "DECISION NPC-3 " + finding);
		helper.assertTrue(findings.isEmpty(), "DECISION NPC-3: " + String.join(" | ", findings));
		helper.succeed();
	}

	// =================================================================================================================
	// quintessenceHasNoPointer (D D5-5)
	// =================================================================================================================

	/**
	 * DECISION D5-5: no NPC says where Sanguine Quintessence comes from, though the Founding Fane, the sanctified rite
	 * and at least four more D5-D7 steps seat it. Options: a Vicar or Alchemist line / an item inquiry / the Liber only.
	 * A D5 founder (the Fane next) and a D6 Sanctified player, each carrying one, talk to every outpost speaker and the
	 * Monolith empty-handed and with it in hand; expects some line that mentions Quintessence to name its source (the
	 * Exsanguination rite, where lifeblood crystallizes into it).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void quintessenceHasNoPointer(GameTestHelper helper) {
		String scenario = "quintessenceHasNoPointer";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = new ArrayList<>(wing(outpost));
		speakers.add(monolith(ctx));
		ctx.leader();
		boolean named = false;
		for (LateState state : List.of(new LateState(Stage.D5, LineRole.LEADER, Hand.ITEM, Extra.NONE),
				new LateState(Stage.D6, LineRole.LEADER, Hand.ITEM, Extra.NONE))) {
			PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), state, null);
			try {
				Map<String, List<String>> heard = new LinkedHashMap<>(heard(player, speakers, Hand.EMPTY));
				heard.putAll(heard(player, speakers, Hand.ITEM));
				List<String> mentions = new ArrayList<>();
				for (Map.Entry<String, List<String>> speaker : heard.entrySet()) {
					for (String text : speaker.getValue()) {
						if (QUINTESSENCE.matcher(text).find()) mentions.add(speaker.getKey() + ": " + shorten(text));
					}
				}
				boolean source = namesQuintessenceSource(heard);
				named |= source;
				PlaythroughLog.note(helper, scenario, state + ": " + mentions.size() + " line(s) mention Quintessence "
						+ mentions + "; one names its source: " + source);
			} finally {
				player.offline();
			}
		}
		helper.assertTrue(named, "DECISION D5-5: no speaker a D5 or D6 player can reach says where Sanguine "
				+ "Quintessence comes from (see the [playthrough] lines)");
		helper.succeed();
	}

	// =================================================================================================================
	// monolithBlueprintOnlyAtD5 (D D5-10)
	// =================================================================================================================

	/**
	 * DECISION D5-10: the Vicar's Monolith scrap (the Working craft's blueprint) is offered only at exactly D5, and its
	 * handler refuses any other degree, so a player who reached D6 without asking can never get it from him. Options:
	 * offer at D5+ / a recipe / keep. Expects the Vicar to offer it at D6, D7 and D8 (both paths) to a player who
	 * never took one; logs the D5 control and what a forged ask gives.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void monolithBlueprintOnlyAtD5(GameTestHelper helper) {
		String scenario = "monolithBlueprintOnlyAtD5";
		Late ctx = new Late(helper);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		List<String> findings = new ArrayList<>();
		for (Stage stage : List.of(Stage.D5, Stage.D6, Stage.D6_LIVING, Stage.D7, Stage.D8_APOTHEOS, Stage.D8_SILENTIUM)) {
			PlaythroughPlayer player = playerIn(ctx, helper.absolutePos(STAND), new LateState(stage, LineRole.LEADER), null);
			try {
				DialogueView view = player.talkTo(vicar);
				boolean offered = view.offers(BLUEPRINT);
				String forged = "";
				if (!offered) {
					Outcome ask = player.forge(BLUEPRINT, vicar.getId());
					forged = "; a forged ask gave " + (gained(ask) > 0 ? ask.inventoryDelta() + " " + ask.drops() : "nothing");
				}
				PlaythroughLog.note(helper, scenario, stage + " (degree " + player.degree() + "): blueprint offered "
						+ offered + forged);
				if (!offered) findings.add(stage + ": the Vicar does not offer the Monolith blueprint");
			} finally {
				player.offline();
			}
		}
		helper.assertTrue(findings.isEmpty(), "DECISION D5-10: " + String.join(" | ", findings));
		helper.succeed();
	}

	// =================================================================================================================
	// summonStripsTheOutpost (D NPC-1)
	// =================================================================================================================

	/**
	 * DECISION NPC-1: a ledger summon moves a shared outpost NPC away for good, and several lines can recruit one NPC.
	 * Options: send home after use / successors only / one line per original / keep. Two D5 leaders pledge the same
	 * outpost Vicar through his dialogue; the first, inside her Fane, summons him with the ledger; 200 ticks later a D0
	 * newcomer arrives at the outpost. Expects one line per original and a Vicar at the outpost for the newcomer.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = BATCH, required = false)
	public static void summonStripsTheOutpost(GameTestHelper helper) {
		String scenario = "summonStripsTheOutpost";
		BlockPos post = PlaythroughSites.stand(helper, 28, 20);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, post);
		PlaythroughPlayer first = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.covenantD5(first, PlaythroughSites.stand(helper, 6, 2)); // ARRANGE: a founder with her Fane
		PlaythroughPlayer second = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 24, 16));
		PlaythroughStates.foundedLineD5(second); // ARRANGE: a second founder
		Speaker speaker = new Speaker("vicar", vicar, null);
		List<String> findings = new ArrayList<>();
		boolean[] state = new boolean[3]; // first recruited, second recruited, summoned away
		PlaythroughClock.start(helper)
				.then(() -> {
					state[0] = pledge(first, speaker);
					state[1] = pledge(second, speaker);
					PlaythroughLog.note(helper, scenario, "recruited into the first line " + state[0] + ", into the second "
							+ state[1]);
					if (state[0] && state[1]) findings.add("two lines recruited the same outpost Vicar");
					first.moveTo(PlaythroughSites.stand(helper, 4, 4));
					Outcome summon = first.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
					state[2] = vicar.distanceTo(first.player()) < 6.0F;
					PlaythroughLog.note(helper, scenario, "summon: " + summon.saidKeys() + "; the Vicar is now "
							+ String.format(Locale.ROOT, "%.1f", vicar.position().distanceTo(post.getCenter())) + " blocks from the outpost");
				})
				.idle(200)
				.then(() -> {
					PlaythroughPlayer newcomer = PlaythroughPlayer.create(helper, post.east(1));
					PlaythroughStates.freshD0(newcomer); // ARRANGE
					List<HarbingerVicarEntity> atPost = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
							new AABB(post).inflate(8.0D), Entity::isAlive);
					String served = atPost.isEmpty() ? "no Vicar within 8 blocks"
							: newcomer.tryTalkTo(atPost.getFirst()).map(DialogueView::describe).orElse("nothing opened");
					PlaythroughLog.note(helper, scenario, "200 ticks after the summon: the Vicar is "
							+ String.format(Locale.ROOT, "%.1f", vicar.position().distanceTo(post.getCenter()))
							+ " blocks from the outpost; the newcomer finds: " + served);
					if (state[2] && atPost.isEmpty()) findings.add("the summoned Vicar left the outpost with no Vicar for a D0 newcomer");
					if (!state[2]) PlaythroughLog.note(helper, scenario, "the summon did not move the Vicar to the founder");
					for (String finding : findings) PlaythroughLog.note(helper, scenario, "DECISION NPC-1 " + finding);
					helper.assertTrue(findings.isEmpty(), "DECISION NPC-1: " + String.join(" | ", findings));
				})
				.run();
	}

	/** Pledges {@code speaker} into the player's line through its dialogue; true when the line lists it after. */
	private static boolean pledge(PlaythroughPlayer player, Speaker speaker) {
		Optional<DialogueView> view = talk(player, speaker, Hand.EMPTY);
		if (view.isEmpty() || !view.get().offers(RECRUIT)) return false;
		choose(player, speaker, RECRUIT);
		Bloodline line = PlaythroughStates.lineOf(player);
		return line != null && line.hasNpcMember(speaker.entity().getUUID());
	}

	// =================================================================================================================
	// rewardLandingSurveyLate (D D-3)
	// =================================================================================================================

	private record LateClaim(String name, String speaker, List<String> events, LateState state) {
		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * DECISION: D-3 (late instances; not re-decided here). One-time late rewards that drop unowned at the NPC's feet:
	 * the Vicar's Monolith scrap (D5) and stained church map (D7), the Mnemonist's Circus waybill (D5), the Voyager's
	 * Relay Chart (D5), the Artificer's Consecration Kit (D5) and the Monolith's cornerstone (D7). For each, records
	 * where the reward lands (inventory, PendingDelivery, the player's feet, the speaker's feet) and expects only the
	 * first two. Each claim's preconditions are arranged (the kit's worked Armature, the cornerstone's own Monolith), and
	 * a claim that is still not offered fails the probe as a broken arrangement.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH, required = false)
	public static void rewardLandingSurveyLate(GameTestHelper helper) {
		String scenario = "rewardLandingSurveyLate";
		Late ctx = new Late(helper);
		List<LateClaim> claims = List.of(
				new LateClaim("monolith scrap", "vicar", List.of(BLUEPRINT), new LateState(Stage.D5, LineRole.LEADER)),
				new LateClaim("stained church map", "vicar", List.of(STAINED_MAP), new LateState(Stage.D7, LineRole.LEADER)),
				new LateClaim("circus waybill", "mnemonist", List.of(HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL),
						new LateState(Stage.D5, LineRole.LEADER)),
				new LateClaim("relay chart", "voyager", List.of(CourtDiscoveryDialogue.VOYAGER_LEARN,
						CourtDiscoveryDialogue.VOYAGER_CHART), new LateState(Stage.D5, LineRole.LEADER)),
				new LateClaim("consecration kit", "artificer", List.of(HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT),
						new LateState(Stage.D5, LineRole.LEADER)),
				new LateClaim("monolith cornerstone", "monolith", List.of(SanguineMonolithDialogueTrees.EVENT_CORNERSTONE),
						new LateState(Stage.D7, LineRole.LEADER)));
		Map<String, Speaker> speakers = new LinkedHashMap<>();
		speakers.put("vicar", spawn(helper, Role.VICAR, PlaythroughSites.stand(helper, 6, 6)));
		speakers.put("mnemonist", spawn(helper, Role.MNEMONIST, PlaythroughSites.stand(helper, 12, 6)));
		speakers.put("voyager", spawn(helper, Role.VOYAGER, PlaythroughSites.stand(helper, 18, 6)));
		speakers.put("artificer", spawn(helper, Role.ARTIFICER, PlaythroughSites.stand(helper, 24, 6)));
		speakers.put("monolith", monolith(ctx));
		List<String> findings = new ArrayList<>();
		List<String> table = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (LateClaim claim : claims) {
			clock.then(() -> {
				Speaker speaker = speakers.get(claim.speaker());
				PlaythroughPlayer player = playerIn(ctx, PlaythroughSites.stand(helper, 14, 14), claim.state(), speaker);
				try {
					if (claim.name().equals("consecration kit")) {
						// ARRANGE: an Armature worked (an armour upgrade), the kit's precondition, as the D5 band arranges it.
						HemoCapabilityAccess.stationUpgrades(player.player()).recordUse(UpgradeStation.ARMATURE,
								StationUpgradeCatalog.ARMOR_UPGRADE);
					} else if (claim.name().equals("monolith cornerstone")) {
						// ARRANGE: a Monolith of the player's own on record (its Working craft's credit), and the Armature
						// practice the cornerstone's tier (Armature tier 2) and the tier before it ask for.
						com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(player.player(),
								com.vincenthuto.hemomancy.common.init.BlockInit.sanguine_monolith.get());
						for (int tier = 1; tier <= 2; tier++) {
							for (String usage : StationUpgradeCatalog.get(UpgradeStation.ARMATURE, tier).requiredUsage()) {
								HemoCapabilityAccess.stationUpgrades(player.player()).recordUse(UpgradeStation.ARMATURE, usage);
							}
						}
					}
					Optional<DialogueView> view = talk(player, speaker, Hand.EMPTY);
					String eventId = view.flatMap(v -> claim.events().stream().filter(v::offers).findFirst()).orElse(null);
					if (eventId != null && claim.name().equals("monolith cornerstone")) {
						PlaythroughLog.note(helper, scenario, "the cornerstone's claim state: " + HemoCapabilityAccess.stationUpgrades(
								player.player()).claimState(player.player(), StationUpgradeCatalog.get(UpgradeStation.ARMATURE, 2)));
					}
					if (eventId == null) {
						// Every claim is arranged to be offered: a claim missing here means the arrangement broke, so it
						// fails loudly instead of leaving its row empty.
						String row = claim + ": not offered at " + claim.state() + "; "
								+ view.map(DialogueView::describe).orElse("nothing opened");
						table.add(row);
						findings.add("ARRANGEMENT " + row);
						return;
					}
					int pendingBefore = pendingOwed(player.player());
					Outcome outcome = choose(player, speaker, eventId);
					int pending = Math.max(0, pendingOwed(player.player()) - pendingBefore);
					int inventory = 0;
					for (int count : outcome.inventoryDelta().values()) if (count > 0) inventory += count;
					int atNpc = 0;
					int atPlayer = 0;
					for (Outcome.Drop drop : outcome.drops()) {
						boolean atSpeaker = speaker.block() != null
								? drop.pos().distanceTo(Vec3.atCenterOf(speaker.block())) < 3.0D
								: drop.near(Outcome.Near.NPC);
						if (atSpeaker) atNpc += drop.stack().getCount();
						else atPlayer += drop.stack().getCount();
					}
					String row = claim + " (" + eventId + "): inventory +" + inventory + ", pending +" + pending
							+ ", player's feet " + atPlayer + ", speaker's feet " + atNpc + " " + outcome.drops()
							+ " said " + outcome.saidKeys();
					table.add(row);
					if (atNpc + atPlayer > 0) findings.add(row);
					if (inventory + pending + atNpc + atPlayer == 0) findings.add("ARRANGEMENT (nothing was given) " + row);
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> {
			for (String row : table) PlaythroughLog.note(helper, scenario, "landing " + row);
			for (String finding : findings) PlaythroughLog.note(helper, scenario, "DECISION D-3 " + finding);
			helper.assertTrue(findings.isEmpty(), "DECISION D-3 (late): one-time rewards dropped on the ground: "
					+ String.join(" | ", findings));
		}).run();
	}

	private static int pendingOwed(ServerPlayer player) {
		int owed = 0;
		for (PendingDelivery.Claim claim : PendingDelivery.claims()) {
			for (ItemStack stack : claim.owed().apply(player)) owed += stack.getCount();
		}
		return owed;
	}

	// =================================================================================================================
	// tradeBoxSweepLate (E)
	// =================================================================================================================

	/**
	 * The Trade box past D4, for every Harbinger speaker at every late stage: no card appears that a D4 player was not
	 * shown (every card unlocks by D4 in the data); every card, paid for with what it asks (ARRANGE), trades or refuses
	 * with a reason key that has English; rapport moves only for this speaker, and never on a refusal; the Vicar's
	 * tithe, when taken, costs exactly its 500 mL through {@code BloodAccounting} (the {@code npc_tithe} source shows
	 * in {@code BloodFlowLedger}). A successor Alchemist answers a member of its own line and refuses a stranger with
	 * another line, taking nothing from them.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = SWEEP_BATCH)
	public static void tradeBoxSweepLate(GameTestHelper helper) {
		String scenario = "tradeBoxSweepLate";
		Late ctx = new Late(helper);
		PlaythroughSites.OutpostWing outpost = PlaythroughSites.outpostWing(helper, at(helper, WING), HARBINGER_WING);
		List<Speaker> speakers = wing(outpost);
		Map<String, Set<ResourceLocation>> baseline = new HashMap<>();
		Findings findings = new Findings();
		boolean[] titheTaken = {false};
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			ctx.leader();
			PlaythroughPlayer adept = PlaythroughPlayer.create(helper, outpost.playerSpot());
			try {
				PlaythroughStates.adeptD4(adept); // ARRANGE: the D4 baseline
				for (Speaker speaker : speakers) {
					Set<ResourceLocation> ids = new LinkedHashSet<>();
					talk(adept, speaker, Hand.EMPTY).ifPresent(view -> view.tradeCards().forEach(card -> ids.add(card.offerId())));
					baseline.put(speaker.label(), ids);
				}
				PlaythroughLog.note(helper, scenario, "D4 cards " + baseline);
			} finally {
				adept.offline();
			}
		}).idle(1);
		List<LateState> grid = new ArrayList<>();
		for (Stage stage : Stage.values()) grid.add(new LateState(stage, stage.special() ? LineRole.NONE : LineRole.LEADER));
		for (LateState state : grid) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(ctx, outpost.playerSpot(), state, null);
				try {
					for (Speaker speaker : speakers) {
						Optional<DialogueView> opened = talk(player, speaker, Hand.EMPTY);
						if (opened.isEmpty()) {
							if (!silentByDesign(state, speaker, player)) findings.add(speaker, "no dialogue, so no Trade box", state);
							continue;
						}
						tradeCards(helper, scenario, state, player, speaker, opened.get(),
								baseline.getOrDefault(speaker.label(), Set.of()), findings, titheTaken);
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> successorTrades(ctx, scenario, findings)).then(() -> {
			if (!titheTaken[0]) findings.add("vicar", "the tithe was never taken, so its blood accounting was not seen", "all");
			finish(helper, scenario, findings);
		}).run();
	}

	private static void tradeCards(GameTestHelper helper, String scenario, LateState state, PlaythroughPlayer player,
			Speaker speaker, DialogueView view, Set<ResourceLocation> baseline, Findings findings, boolean[] titheTaken) {
		if (!view.tradeBoard().present()) {
			PlaythroughLog.note(helper, scenario, state + " " + speaker + ": no Trade box; " + view.describe());
			return;
		}
		String tradeSpeaker = speaker.entity() instanceof NpcTradeSpeaker npc ? npc.tradeSpeakerId() : speaker.label();
		List<String> rows = new ArrayList<>();
		for (NpcTradeCard card : view.tradeCards()) {
			if (!baseline.contains(card.offerId())) {
				findings.add(speaker, "a card a D4 player was not shown: " + card.offerId(), state);
			}
			if (state.stage() != Stage.PURIFYING && state.stage() != Stage.CLARITY) PlaythroughStates.blood(player, 2000.0D); // ARRANGE
			List<ItemStack> pack = packOf(player); // ARRANGE: put back after the card, so the pack never fills up
			int[] slots = pay(player, card); // ARRANGE: the goods the card asks for
			Map<String, Integer> before = rapport(player);
			NpcTradeResultPacket result = player.trade(speaker.entity(), card.offerId(), slots);
			Outcome outcome = player.last();
			Map<String, Integer> after = rapport(player);
			restorePack(player, pack);
			if (result == null) {
				findings.add(speaker, card.offerId() + " was dropped unanswered", state);
				continue;
			}
			rows.add(card.offerId() + "=" + result.outcome() + "(" + result.reasonKey() + ")");
			if (!LangKeys.exists(result.reasonKey())) {
				findings.add(speaker, card.offerId() + " answered with " + result.reasonKey() + ", which has no translation", state);
			}
			for (String other : TRADE_SPEAKERS) {
				if (!other.equals(tradeSpeaker) && !Objects.equals(before.get(other), after.get(other))) {
					findings.add(speaker, card.offerId() + " moved " + other + "'s rapport", state);
				}
			}
			if (result.outcome() == NpcTradeService.Outcome.REFUSED && !Objects.equals(before.get(tradeSpeaker), after.get(tradeSpeaker))) {
				findings.add(speaker, card.offerId() + " refused but moved rapport", state);
			}
			if (card.offerId().equals(Hemomancy.rloc("vicar/tithe")) && result.outcome() != NpcTradeService.Outcome.REFUSED) {
				titheTaken[0] = true;
				boolean recorded = BloodFlowLedger.collect(player.player(), HemoCapabilityAccess.getBloodVolume(player.player()))
						.contribution(TITHE_SOURCE).isPresent();
				PlaythroughLog.note(helper, scenario, state + ": the tithe took " + outcome.bloodSpent() + " mL; ledger records "
						+ TITHE_SOURCE + ": " + recorded);
				if (Math.abs(Math.abs(outcome.bloodSpent()) - 500.0D) > 0.5D) {
					findings.add(speaker, "the tithe took " + outcome.bloodSpent() + " mL, not 500", state);
				}
				if (!recorded) findings.add(speaker, "the tithe's blood is not in BloodFlowLedger under " + TITHE_SOURCE, state);
			}
		}
		PlaythroughLog.note(helper, scenario, state + " " + speaker + ": " + rows);
	}

	/** A successor Alchemist of the shared line: its member is answered; a stranger with another line is refused. */
	private static void successorTrades(Late ctx, String scenario, Findings findings) {
		GameTestHelper helper = ctx.helper;
		ProfessionalHarbingerEntity successor = PlaythroughNpcs.successor(helper, Role.ALCHEMIST,
				PlaythroughSites.stand(helper, 4, 18), ctx.leader()); // ARRANGE: born to the shared line
		ResourceLocation offer = Hemomancy.rloc("alchemist/fresh_draw");
		PlaythroughPlayer member = playerIn(ctx, PlaythroughSites.stand(helper, 6, 18), new LateState(Stage.D5, LineRole.MEMBER), null);
		PlaythroughPlayer stranger = playerIn(ctx, PlaythroughSites.stand(helper, 6, 19), new LateState(Stage.D5, LineRole.LEADER), null);
		try {
			for (PlaythroughPlayer player : List.of(member, stranger)) {
				boolean own = player == member;
				PlaythroughStates.blood(player, 2000.0D); // ARRANGE
				player.approach(successor);
				String served = player.tryTalkTo(successor).map(DialogueView::describe).orElse("nothing opened");
				NpcTradeResultPacket result = player.trade(successor, offer);
				Outcome outcome = player.last();
				String who = own ? "a member of its own line" : "a stranger with another line";
				PlaythroughLog.note(helper, scenario, "successor Alchemist to " + who + ": served " + served + "; trade "
						+ (result == null ? "dropped" : result.outcome() + " " + result.reasonKey()));
				if (own && result == null) findings.add("successor alchemist", "did not answer a member of its own line", "D5 member");
				if (own && result != null && !LangKeys.exists(result.reasonKey())) {
					findings.add("successor alchemist", "answered its member with " + result.reasonKey() + ", no translation", "D5 member");
				}
				if (!own && result != null && result.outcome() != NpcTradeService.Outcome.REFUSED) {
					findings.add("successor alchemist", "traded with a stranger from another line: " + result.outcome(), "D5 stranger");
				}
				if (!own && (outcome.bloodSpent() != 0.0D || gained(outcome) > 0)) {
					findings.add("successor alchemist", "a stranger's trade changed things: " + outcome.describe(), "D5 stranger");
				}
			}
		} finally {
			member.offline();
			stranger.offline();
		}
	}

	private static List<ItemStack> packOf(PlaythroughPlayer player) {
		List<ItemStack> pack = new ArrayList<>();
		for (ItemStack stack : player.player().getInventory().items) pack.add(stack.copy());
		return pack;
	}

	private static void restorePack(PlaythroughPlayer player, List<ItemStack> pack) {
		var items = player.player().getInventory().items;
		for (int slot = 0; slot < items.size(); slot++) items.set(slot, pack.get(slot).copy());
	}

	private static Map<String, Integer> rapport(PlaythroughPlayer player) {
		Map<String, Integer> rapport = new HashMap<>();
		var ledger = HemoCapabilityAccess.npcTrades(player.player());
		for (String speaker : TRADE_SPEAKERS) rapport.put(speaker, ledger.rapport(speaker));
		return rapport;
	}

	/** ARRANGE: puts one matching stack per item part of the card's want into free slots and returns them. */
	private static int[] pay(PlaythroughPlayer player, NpcTradeCard card) {
		List<Integer> slots = new ArrayList<>();
		for (NpcTradeWant.Items part : card.want().itemParts()) {
			ItemStack stack = stackFor(part.match(), part.count());
			if (stack.isEmpty()) continue;
			var inventory = player.player().getInventory();
			int slot = inventory.getFreeSlot();
			if (slot < 0) continue;
			inventory.setItem(slot, stack);
			slots.add(slot);
		}
		return slots.stream().mapToInt(Integer::intValue).toArray();
	}

	private static ItemStack stackFor(NpcTradeMatch match, int count) {
		Item item = Items.AIR;
		if (!match.items().isEmpty()) item = BuiltInRegistries.ITEM.get(match.items().getFirst());
		if (item == Items.AIR) {
			ItemStack[] options = match.toIngredient().getItems();
			if (options.length > 0) item = options[0].getItem();
		}
		if (item == Items.AIR) return ItemStack.EMPTY;
		ItemStack stack = new ItemStack(item);
		stack.setCount(Math.max(1, Math.min(count, stack.getMaxStackSize())));
		return stack;
	}

	// =================================================================================================================
	// uniqueNpcLossLate (D D-10)
	// =================================================================================================================

	/**
	 * DECISION: D-10 (late instances; not re-decided here). Three losses a late player can meet:
	 * <ol>
	 * <li>the Respite's Anchorite killed when a D6 player needs the Vein-Mason D6 referral: expects some route (an
	 * Anchorite at the Respite again, or the Vicar or Mnemonist offering the referral);</li>
	 * <li>the Artificer killed at D7 after the Weight of the Frame brief: expects another outpost's Artificer to carry
	 * the chain on (a Weight of the Frame step or card);</li>
	 * <li>a recruited Vicar killed: expects the slot freed in the leader's line, another outpost's Vicar recruitable in
	 * his place, and the ledger summon to bring the new one. (The death mid-Vigil itself is played by the D6 band's
	 * {@code vigilHelperLost}; here the line's recovery after it.)</li>
	 * </ol>
	 * Each NPC is killed by ARRANGE ({@code PlaythroughNpcs.kill}); everything after is played.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH, required = false)
	public static void uniqueNpcLossLate(GameTestHelper helper) {
		String scenario = "uniqueNpcLossLate";
		List<String> findings = new ArrayList<>();
		// Part 1: the Anchorite at D6
		PlaythroughSites.MasonsRespite respite = PlaythroughSites.masonsRespite(helper, PlaythroughSites.stand(helper, 6, 6));
		Speaker vicarA = spawn(helper, Role.VICAR, PlaythroughSites.stand(helper, 15, 3));
		Speaker mnemonistA = spawn(helper, Role.MNEMONIST, PlaythroughSites.stand(helper, 15, 8));
		PlaythroughPlayer sanctified = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 3));
		PlaythroughStates.sanctifiedD6(sanctified, PlaythroughSites.stand(helper, 1, 1)); // ARRANGE
		Speaker anchorite = new Speaker("anchorite", respite.anchorite(), null);
		// Part 2: the Artificer at D7
		Speaker artificer = spawn(helper, Role.ARTIFICER, PlaythroughSites.stand(helper, 20, 3));
		Speaker otherArtificer = new Speaker("artificer (outpost 1)", PlaythroughNpcs.stampOutpost(helper,
				PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 28, 3)), 1), null);
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 23, 6));
		PlaythroughStates.covenantD5(archon, PlaythroughSites.stand(helper, 24, 1)); // ARRANGE
		PlaythroughStates.archonD7(archon); // ARRANGE
		// Part 3: a recruited Vicar
		Speaker recruit = spawn(helper, Role.VICAR, PlaythroughSites.stand(helper, 14, 20));
		Speaker replacement = new Speaker("vicar (outpost 1)", PlaythroughNpcs.stampOutpost(helper,
				PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 29, 20)), 1), null);
		PlaythroughPlayer founder = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 18));
		PlaythroughStates.sanctifiedD6(founder, PlaythroughSites.stand(helper, 4, 21)); // ARRANGE: the Vigil is next
		founder.joinLevel(); // the line's members are re-synced through the server's player list, as for any online player
		String referral = HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL;
		PlaythroughClock.start(helper)
				.then(() -> {
					boolean before = talk(sanctified, anchorite, Hand.EMPTY).map(view -> view.offers(referral)).orElse(false);
					PlaythroughLog.note(helper, scenario, "D6 at the Respite: the Anchorite offers the D6 referral " + before);
					PlaythroughNpcs.kill(respite.anchorite()); // ARRANGE

					Optional<DialogueView> atFirst = talk(archon, artificer, Hand.EMPTY);
					String brief = HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME;
					boolean briefed = atFirst.map(view -> view.offers(brief)).orElse(false);
					if (briefed) choose(archon, artificer, brief);
					PlaythroughLog.note(helper, scenario, "D7 at the first Artificer: Weight of the Frame brief offered "
							+ briefed + "; " + atFirst.map(DialogueView::describe).orElse("nothing opened"));
					PlaythroughNpcs.kill(artificer.entity()); // ARRANGE

					boolean recruited = pledge(founder, recruit);
					founder.moveTo(PlaythroughSites.stand(helper, 3, 18));
					PlaythroughLog.note(helper, scenario, "D6 founder recruited the outpost Vicar " + recruited);
					if (!recruited) findings.add("the D6 founder could not recruit the outpost Vicar to begin with");
					PlaythroughNpcs.kill(recruit.entity()); // ARRANGE
				})
				.idle(40)
				.then(() -> {
					List<HarbingerCicatrixAnchoriteEntity> alive = helper.getLevel().getEntitiesOfClass(
							HarbingerCicatrixAnchoriteEntity.class, new AABB(respite.centre()).inflate(48.0D), Entity::isAlive);
					boolean atRespite = false;
					for (HarbingerCicatrixAnchoriteEntity other : alive) {
						atRespite |= talk(sanctified, new Speaker("anchorite", other, null), Hand.EMPTY)
								.map(view -> view.offers(referral)).orElse(false);
					}
					boolean elsewhere = false;
					for (Speaker teacher : List.of(vicarA, mnemonistA)) {
						elsewhere |= talk(sanctified, teacher, Hand.EMPTY).map(view -> view.offers(referral)).orElse(false);
					}
					PlaythroughLog.note(helper, scenario, "after the Anchorite fell: living Anchorites " + alive.size()
							+ ", referral at the Respite " + atRespite + ", from the Vicar or Mnemonist " + elsewhere);
					if (!atRespite && !elsewhere) findings.add("a D6 player whose Anchorite was killed has no route to the D6 referral");

					Optional<DialogueView> atSecond = talk(archon, otherArtificer, Hand.EMPTY);
					List<String> frame = atSecond.map(view -> {
						List<String> found = new ArrayList<>();
						for (String event : view.shownEvents()) if (event.contains("weight_of_frame") || event.contains("d7")) found.add(event);
						for (DialogueTopic topic : view.topics()) if (topic.titleKey().contains("weight")) found.add(topic.titleKey());
						return found;
					}).orElse(List.of());
					PlaythroughLog.note(helper, scenario, "D7 at another outpost's Artificer: Weight of the Frame steps "
							+ frame + "; " + atSecond.map(DialogueView::describe).orElse("nothing opened"));
					if (frame.isEmpty()) findings.add("another outpost's Artificer shows no Weight of the Frame step at D7");

					Bloodline line = PlaythroughStates.lineOf(founder);
					boolean freed = line != null && !line.hasNpcMember(recruit.entity().getUUID());
					boolean replaced = pledge(founder, replacement);
					founder.moveTo(PlaythroughSites.stand(helper, 3, 18));
					Outcome summon = founder.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
					boolean summoned = replacement.entity().distanceTo(founder.player()) < 6.0F;
					PlaythroughLog.note(helper, scenario, "after the recruit fell: slot freed " + freed + ", replacement recruited "
							+ replaced + ", summoned " + summoned + " (" + summon.saidKeys() + ")");
					if (!freed) findings.add("the killed recruit still holds a place in the line");
					if (!replaced) findings.add("another outpost's Vicar cannot be recruited after the recruit fell");
					if (replaced && !summoned) findings.add("the ledger summon does not bring the replacement Vicar");
					for (String finding : findings) PlaythroughLog.note(helper, scenario, "DECISION D-10 " + finding);
					helper.assertTrue(findings.isEmpty(), "DECISION D-10 (late): " + String.join(" | ", findings));
				})
				.run();
	}

	// =================================================================================================================
	// bloodlineNameIsEnglish (D NPC-6)
	// =================================================================================================================

	/**
	 * DECISION NPC-6: the bloodline name is concatenated English ({@code "<name>'s Blood Line"},
	 * {@code CardinalRiteCompletionEffects} 979) stored in saved data, so a player in another language reads it in
	 * English wherever it shows: the join line, the ledger tooltip and screens, the initiation line. Options: a
	 * translatable default name with a storage change / keep. Logged only: a second player joins through the founder's
	 * signed ledger (a real {@code use}) and the name as stored and as said is logged; the probe fails until the user
	 * decides.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void bloodlineNameIsEnglish(GameTestHelper helper) {
		String scenario = "bloodlineNameIsEnglish";
		PlaythroughPlayer founder = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		Bloodline line = PlaythroughStates.foundedLineD5(founder); // ARRANGE: the line as the Founding rite names it
		PlaythroughPlayer joiner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 4));
		PlaythroughStates.illuminatusD5(joiner); // ARRANGE
		joiner.give(PlaythroughStates.signedLedger(line)); // ARRANGE: the founder hands over a copy of the ledger
		joiner.wield(ItemInit.unsigned_ancestral_ledger.get());
		Outcome join = joiner.use(net.minecraft.world.InteractionHand.MAIN_HAND);
		Bloodline joined = PlaythroughStates.lineOf(joiner);
		String stored = joined == null ? "<no line>" : joined.getName();
		PlaythroughLog.note(helper, scenario, "DECISION NPC-6 stored name \"" + stored + "\" (a translation key: "
				+ LangKeys.exists(stored) + ", the rite's format: " + stored.equals(PlaythroughStates.lineName(founder))
				+ "); the joiner was told " + join.chat());
		helper.assertTrue(LangKeys.exists(stored), "DECISION NPC-6: the line's default name is stored as a translatable key,"
				+ " not the English \"" + stored + "\"");
		helper.succeed();
	}

	// =================================================================================================================
	// Helpers
	// =================================================================================================================

	/** Items the action put in the player's hands or on the ground near them or the NPC. */
	private static int gained(Outcome outcome) {
		int gained = 0;
		for (int count : outcome.inventoryDelta().values()) if (count > 0) gained += count;
		for (Outcome.Drop drop : outcome.drops()) gained += drop.stack().getCount();
		return gained;
	}

	/** Whether a player would notice anything the action did. */
	private static boolean observable(Outcome outcome) {
		return !outcome.chat().isEmpty() || gained(outcome) > 0 || !outcome.earned().isEmpty()
				|| !outcome.lost().isEmpty() || outcome.degreeAfter() != outcome.degreeBefore()
				|| outcome.bloodActiveAfter() != outcome.bloodActiveBefore() || !outcome.payloads().isEmpty()
				|| !"{}".equals(outcome.savedDiff()) || outcome.rewardDelivered();
	}

	/** What a refused action should not have done, or null when it did none of it. */
	@Nullable
	private static String change(Outcome outcome) {
		List<String> parts = new ArrayList<>();
		if (gained(outcome) > 0) parts.add("gave " + outcome.inventoryDelta() + " dropped " + outcome.drops());
		if (!outcome.earned().isEmpty()) parts.add("earned " + outcome.earned());
		if (outcome.degreeAfter() != outcome.degreeBefore()) {
			parts.add("degree " + outcome.degreeBefore() + "->" + outcome.degreeAfter());
		}
		if (outcome.bloodActiveAfter() != outcome.bloodActiveBefore()) parts.add("blood activity changed");
		if (outcome.rewardDelivered()) parts.add("its route reported a delivered reward");
		return parts.isEmpty() ? null : String.join("; ", parts) + " (said " + outcome.saidKeys() + ")";
	}

	/** Logs every finding, then fails with the first few when there are any. */
	private static void finish(GameTestHelper helper, String scenario, Findings findings) {
		List<String> lines = findings.lines();
		for (String finding : lines) PlaythroughLog.note(helper, scenario, "FINDING " + finding);
		PlaythroughLog.note(helper, scenario, lines.size() + " finding(s)");
		if (lines.isEmpty()) return;
		int shown = Math.min(lines.size(), 8);
		helper.fail(scenario + " found " + lines.size() + ": " + String.join(" | ", lines.subList(0, shown))
				+ (lines.size() > shown ? " | ... (see [playthrough] log lines)" : ""));
	}

	private static Map<String, String> englishText;

	/**
	 * The English a player reads for {@code key}: Hemomancy's shipped {@code en_us.json} (a classpath resource, read
	 * once), then the server language, then the key itself.
	 */
	private static synchronized String english(String key) {
		if (key == null || key.isEmpty()) return "";
		if (englishText == null) {
			Map<String, String> text = new HashMap<>();
			try (InputStream in = PlaythroughLateNpcUsabilityGameTests.class.getResourceAsStream(
					"/assets/hemomancy/lang/en_us.json")) {
				if (in != null) {
					JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
							.getAsJsonObject();
					for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
						if (entry.getValue().isJsonPrimitive()) text.put(entry.getKey(), entry.getValue().getAsString());
					}
				}
			} catch (IOException exception) {
				throw new IllegalStateException("Could not read en_us.json", exception);
			}
			englishText = text;
		}
		String text = englishText.get(key);
		return text != null ? text : Language.getInstance().getOrDefault(key);
	}
}
