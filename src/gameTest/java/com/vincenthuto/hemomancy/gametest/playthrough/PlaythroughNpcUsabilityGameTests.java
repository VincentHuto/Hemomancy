package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtDiscoveryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueRewardClaims;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FirstDrawsDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MnemonistStarterMemoryChoice;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MorphlingHandlingDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.OverworldFungalSurveyDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PallidVesselDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.route.DialogueEventRouter;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeMatch;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.dialogue.PacketSyncNpcProgressionMarkers;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.common.worldgen.structure.HarbingerOutpostStructure;
import com.vincenthuto.hemomancy.gametest.CapturedPlayer;
import com.vincenthuto.hemomancy.gametest.playthrough.PlaythroughNpcs.Role;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.phys.AABB;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Band {@code npc_usability}: can a real player find, talk to, understand and progress with every Harbinger NPC from
 * a brand-new player to the fifth Degree.
 * <p>
 * The sweeps build each state through {@link PlaythroughStates} (ARRANGE), then act only as a player does: a right
 * click ({@code talkTo}), an offered option ({@code choose}), a forged payload ({@code forge}, negative cases), and the
 * trade request. Each sweep collects every finding before it asserts, logs each one as
 * {@code [playthrough] <scenario>: FINDING ...}, and names its speaker in the test name so a failure says who.
 * <p>
 * Swept states: Degree 0 with and without the charm, crossed with blood inactive and active, then Degrees 1 to 5 with
 * blood active and inactive. Unstained and Degree 6+ content are out of scope.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughNpcUsabilityGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_npc_usability";
	private static final String SWEEP_BATCH = "playthrough_npc_usability_sweep";
	private static final int Y = PlaythroughSites.STAND_Y;
	private static final BlockPos NPC = new BlockPos(8, Y, 8);
	private static final BlockPos STAND = new BlockPos(10, Y, 8);
	private static final BlockPos WING = new BlockPos(14, Y, 12);
	private static final BlockPos SHADOW = new BlockPos(28, Y, 21);

	/** The Harbinger speakers a D0-D5 player meets that carry progression events (the Wayfarer carries none). */
	private static final Role[] HARBINGER_WING = {Role.VICAR, Role.ALCHEMIST, Role.ARTIFICER, Role.MNEMONIST,
			Role.ANCHORITE, Role.HERMIT, Role.VOYAGER};
	private static final List<String> TRADE_SPEAKERS = List.of("hermit", "vicar", "alchemist", "artificer",
			"mnemonist", "cicatrix_anchorite", "voyager", "votary_wayfarer");

	/** Choices that end what the player can do with this NPC in this state; chosen last, and nothing after them. */
	private static final Set<String> TERMINAL = Set.of("vicar_begin_initiation", "vicar_release_charm",
			"hermit_farewell_die");
	/** Events the choose sweep skips: a Chamber visit moves the player out of the level, a bearing starts a search. */
	private static final Set<String> HAZARDS = Set.of(HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER,
			VoyagerIntroductionDialogue.BEARING);
	/** Events more than one Harbinger speaks, so a "wrong" speaker in this band may be a right one. */
	private static final Set<String> MULTI_SPEAKER = Set.of("give_stained_church_map",
			HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD,
			HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD,
			HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD);

	private PlaythroughNpcUsabilityGameTests() {
	}

	// =================================================================================================================
	// States
	// =================================================================================================================

	/** One swept player state. Degree 1 and above always carry the charm. */
	private record SweepState(String label, int degree, boolean charm, boolean active) {
		@Override
		public String toString() {
			return label;
		}
	}

	private static final List<SweepState> STATES = List.of(
			new SweepState("D0 no charm, blood inactive", 0, false, false),
			new SweepState("D0 no charm, blood active", 0, false, true),
			new SweepState("D0 charm, blood active", 0, true, true),
			new SweepState("D0 charm, blood inactive", 0, true, false),
			new SweepState("D1 blood active", 1, true, true),
			new SweepState("D1 blood inactive", 1, true, false),
			new SweepState("D2 blood active", 2, true, true),
			new SweepState("D2 blood inactive", 2, true, false),
			new SweepState("D3 blood active", 3, true, true),
			new SweepState("D3 blood inactive", 3, true, false),
			new SweepState("D4 blood active", 4, true, true),
			new SweepState("D4 blood inactive", 4, true, false),
			new SweepState("D5 blood active", 5, true, true),
			new SweepState("D5 blood inactive", 5, true, false));

	private static List<SweepState> activeStates() {
		List<SweepState> states = new ArrayList<>();
		states.add(STATES.getFirst());
		for (SweepState state : STATES) if (state.charm() && state.active()) states.add(state);
		return states;
	}

	/** A new player brought to {@code state} (ARRANGE). A charm-bearer's heart comes from {@code ownHermit} when given. */
	private static PlaythroughPlayer playerIn(GameTestHelper helper, BlockPos standAt, SweepState state,
			@Nullable Entity ownHermit) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, standAt);
		reach(player, state, ownHermit);
		return player;
	}

	private static void reach(PlaythroughPlayer player, SweepState state, @Nullable Entity ownHermit) {
		PlaythroughStates.freshD0(player); // ARRANGE
		if (state.charm()) PlaythroughStates.charmBearerD0(player, ownHermit); // ARRANGE
		switch (state.degree()) { // ARRANGE
			case 1 -> PlaythroughStates.neophyteD1(player);
			case 2 -> PlaythroughStates.votaryD2(player);
			case 3 -> PlaythroughStates.initiateD3(player);
			case 4 -> PlaythroughStates.adeptD4(player);
			case 5 -> PlaythroughStates.illuminatusD5(player);
			default -> {
			}
		}
		setBloodActive(player, state.active()); // ARRANGE
	}

	/** ARRANGE: wakes or stills the player's blood without touching anything else. */
	private static void setBloodActive(PlaythroughPlayer player, boolean active) {
		var blood = HemoCapabilityAccess.getBloodVolume(player.player());
		if (blood.isActive() == active) return;
		blood.setActive(active);
		BloodVolumeEvents.syncVolume(player.player(), blood);
	}

	@Nullable
	private static Entity ownHermit(Role role, Entity npc) {
		return role == Role.HERMIT ? npc : null;
	}

	private static String slug(Role role) {
		return switch (role) {
			case VOTARY_WAYFARER -> "votaryWayfarer";
			default -> role.name().toLowerCase(Locale.ROOT);
		};
	}

	// =================================================================================================================
	// langKeysSweep_<npc> (H)
	// =================================================================================================================

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_hermit(GameTestHelper helper) {
		langKeysSweep(helper, Role.HERMIT);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_vicar(GameTestHelper helper) {
		langKeysSweep(helper, Role.VICAR);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_alchemist(GameTestHelper helper) {
		langKeysSweep(helper, Role.ALCHEMIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_artificer(GameTestHelper helper) {
		langKeysSweep(helper, Role.ARTIFICER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_mnemonist(GameTestHelper helper) {
		langKeysSweep(helper, Role.MNEMONIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_anchorite(GameTestHelper helper) {
		langKeysSweep(helper, Role.ANCHORITE);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_voyager(GameTestHelper helper) {
		langKeysSweep(helper, Role.VOYAGER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void langKeysSweep_votaryWayfarer(GameTestHelper helper) {
		langKeysSweep(helper, Role.VOTARY_WAYFARER);
	}

	/** Every swept state: a right click opens a tree, every key in it resolves, and nothing in it is a dead end. */
	private static void langKeysSweep(GameTestHelper helper, Role role) {
		String scenario = "langKeysSweep_" + slug(role);
		Entity npc = PlaythroughNpcs.spawn(helper, role, helper.absolutePos(NPC));
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (SweepState state : STATES) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(helper, helper.absolutePos(STAND), state, ownHermit(role, npc));
				try {
					Optional<DialogueView> opened = player.tryTalkTo(npc);
					if (opened.isEmpty()) {
						findings.add(state + ": a right click opened nothing; said " + player.chat());
						return;
					}
					DialogueView view = opened.get();
					for (String missing : view.missingLangKeys()) findings.add(state + ": no translation for " + missing);
					for (String deadEnd : view.deadEnds()) findings.add(state + ": dead end " + deadEnd);
					for (String literal : view.literalLines()) {
						PlaythroughLog.note(helper, scenario, state + ": literal line (not a key) " + literal);
					}
					PlaythroughLog.note(helper, scenario, state + ": " + view.describe());
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	// =================================================================================================================
	// offeredEventsRouteSweep_<npc> (H)
	// =================================================================================================================

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_hermit(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.HERMIT);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_vicar(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.VICAR);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_alchemist(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.ALCHEMIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_artificer(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.ARTIFICER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_mnemonist(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.MNEMONIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_anchorite(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.ANCHORITE);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_voyager(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.VOYAGER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void offeredEventsRouteSweep_votaryWayfarer(GameTestHelper helper) {
		offeredEventsRouteSweep(helper, Role.VOTARY_WAYFARER);
	}

	/**
	 * Every event the NPC offers in every swept state has an owner: the dialogue payload's own Memo, Liber or
	 * succession handling, or a route in {@code DialogueEventRouter}. An event nothing owns is an option that does
	 * nothing. The router logs no "unhandled" line, so ownership is read from the routes themselves (see
	 * {@link RouteProbe}).
	 */
	private static void offeredEventsRouteSweep(GameTestHelper helper, Role role) {
		String scenario = "offeredEventsRouteSweep_" + slug(role);
		Entity npc = PlaythroughNpcs.spawn(helper, role, helper.absolutePos(NPC));
		RouteProbe probe = new RouteProbe(helper);
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (SweepState state : STATES) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(helper, helper.absolutePos(STAND), state, ownHermit(role, npc));
				try {
					Optional<DialogueView> opened = player.tryTalkTo(npc);
					if (opened.isEmpty()) return;
					List<String> owners = new ArrayList<>();
					for (String eventId : opened.get().reachableEvents()) {
						String owner = probe.owner(eventId);
						if (owner == null) findings.add(state + ": offered " + eventId + " but no route takes it");
						else owners.add(eventId + "->" + owner);
					}
					PlaythroughLog.note(helper, scenario, state + ": " + owners);
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * Which handler owns an event id. The payload handles Memo, Liber and succession events before the router; the
	 * router tries its routes in order and stops at the first that returns true. The routes are tried, by reflection,
	 * on a speakerless D0 shadow player standing apart from the test's player, so whatever a route does lands on the
	 * shadow (the drops it makes are removed). Every route matches on the event id before it checks anything else.
	 */
	private static final class RouteProbe {
		private final GameTestHelper helper;
		private final CapturedPlayer shadow;
		private final List<?> routes;
		private final Method handle;

		RouteProbe(GameTestHelper helper) {
			this.helper = helper;
			this.shadow = CapturedPlayer.create(helper);
			BlockPos at = helper.absolutePos(SHADOW);
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

		@Nullable
		String owner(String eventId) {
			if (MemoHelper.isMemoEvent(eventId)) return "payload memo";
			if (!LiberEntryDefinitions.forDialogueEvent(eventId).isEmpty()) return "payload liber";
			if (eventId.startsWith("succession_")) return "payload succession";
			DialogueEvent event = new DialogueEvent(shadow.player, eventId, Integer.MAX_VALUE);
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
	// nothingOfferedIsDeadAtThatStateSweep (E)
	// =================================================================================================================

	/**
	 * Each offered event, chosen in turn the way a curious player clicks through a speaker, does something the player
	 * can see: an item, a line, an advancement, a payload (a screen, a sync), a saved change, a degree, a new node, or
	 * a different tree on the next right click. A choice with none of these is a silent no-op.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = SWEEP_BATCH)
	public static void nothingOfferedIsDeadAtThatStateSweep(GameTestHelper helper) {
		String scenario = "nothingOfferedIsDeadAtThatStateSweep";
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Role role : Role.values()) {
			for (SweepState state : STATES) {
				clock.then(() -> chooseEverything(helper, scenario, role, state, findings)).idle(1);
			}
		}
		clock.then(() -> wrongHermitFarewellProbe(helper, scenario))
				.then(() -> finish(helper, scenario, findings))
				.run();
	}

	private static void chooseEverything(GameTestHelper helper, String scenario, Role role, SweepState state,
			List<String> findings) {
		Entity npc = PlaythroughNpcs.spawn(helper, role, helper.absolutePos(NPC));
		PlaythroughPlayer player = playerIn(helper, helper.absolutePos(STAND), state, ownHermit(role, npc));
		String where = role + " at " + state;
		try {
			Optional<DialogueView> first = player.tryTalkTo(npc);
			if (first.isEmpty()) return;
			List<String> events = new ArrayList<>(first.get().reachableEvents());
			events.sort((a, b) -> Boolean.compare(TERMINAL.contains(a), TERMINAL.contains(b)));
			for (String eventId : events) {
				if (HAZARDS.contains(eventId)) {
					PlaythroughLog.note(helper, scenario, where + ": skipped " + eventId + " (leaves the test level)");
					continue;
				}
				if (!npc.isAlive() || npc.isRemoved()) break;
				Optional<DialogueView> before = player.tryTalkTo(npc);
				if (before.isEmpty() || !before.get().offers(eventId)) continue;
				boolean navigates = before.get().option(eventId).map(option -> option.nextNodeId() != null).orElse(false);
				Outcome outcome;
				try {
					outcome = player.choose(npc, eventId);
				} catch (RuntimeException exception) {
					findings.add(where + ": choosing " + eventId + " threw " + exception);
					continue;
				}
				String after = player.tryTalkTo(npc).map(DialogueView::describe).orElse("<nothing opened>");
				boolean treeChanged = !after.equals(before.get().describe());
				if (!navigates && !treeChanged && !observable(outcome)) {
					String finding = where + ": " + eventId + " is a silent no-op; " + outcome.describe();
					if (role == Role.HERMIT && "hermit_farewell_die".equals(eventId)) {
						// DECISION: D-8 a Hermit whose heart this player did not take from him (or whose charm is
						// off) still offers the farewell, which does nothing. Logged for the user's ruling.
						PlaythroughLog.note(helper, scenario, "DECISION D-8 " + finding);
					} else if (!harbingerReachable(state)) {
						PlaythroughLog.note(helper, scenario, "unreachable state, not failed: " + finding);
					} else {
						findings.add(finding);
					}
				}
				if (TERMINAL.contains(eventId)) break;
			}
		} finally {
			player.offline();
			npc.discard();
		}
	}

	/**
	 * Whether a Harbinger player can stand in {@code state}. From the first Degree the blood only stills when the
	 * player begins purifying, and every NPC then serves its purifying tree instead, so "Dn, blood inactive" is a
	 * swept edge rather than a state a real Harbinger reaches.
	 */
	private static boolean harbingerReachable(SweepState state) {
		return state.degree() == 0 || state.active();
	}

	/**
	 * DECISION: D-8 a D0 player whose heart came from another temple's Hermit is told "you have claimed my heart" and
	 * offered a farewell that does nothing. Logged here, not asserted: the fix is the user's call.
	 */
	private static void wrongHermitFarewellProbe(GameTestHelper helper, String scenario) {
		HarbingerHermitEntity hermit = PlaythroughNpcs.hermit(helper, helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		try {
			PlaythroughStates.charmBearerD0(player, (Entity) null); // ARRANGE: the heart came from another temple
			DialogueView view = player.talkTo(hermit);
			if (!view.offers("hermit_farewell_die")) {
				PlaythroughLog.note(helper, scenario, "D-8 wrong Hermit: no farewell offered; " + view.describe());
				return;
			}
			Outcome farewell = player.choose(hermit, "hermit_farewell_die");
			PlaythroughLog.note(helper, scenario, "D-8 wrong Hermit: lines " + view.lineKeys() + "; farewell "
					+ (observable(farewell) || hermit.isFarewellDying() ? "did something" : "is a silent no-op")
					+ ": " + farewell.describe());
		} finally {
			player.offline();
			hermit.discard();
		}
	}

	// =================================================================================================================
	// unofferedEventsRefusedSweep (E) and wrongSpeakerAndEntityZeroSweep (X F-3)
	// =================================================================================================================

	/** A progression event and the Harbinger who speaks it. */
	private record Progression(Role role, String eventId) {
		@Override
		public String toString() {
			return eventId + "@" + role;
		}
	}

	private static List<Progression> progression() {
		List<Progression> events = new ArrayList<>();
		add(events, Role.HERMIT, "hermit_heart_offered", "hermit_farewell_die");
		add(events, Role.VICAR, "vicar_begin_initiation", "vicar_release_charm",
				HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT,
				HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD,
				HarbingerVicarDialogueTrees.EVENT_LIBER_BLUEPRINT, HarbingerVicarDialogueTrees.EVENT_IRON_BLUEPRINT,
				HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE, "give_sanguine_monolith_blueprint",
				"give_stained_church_map", VoyagerIntroductionDialogue.REPORT, VoyagerIntroductionDialogue.BEARING);
		add(events, Role.ALCHEMIST, FirstDrawsDialogue.ACCEPT,
				HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF,
				HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM, "alchemist_replace_concentrated_blood",
				HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF,
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD,
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER, PallidVesselDialogue.BRIEF,
				PallidVesselDialogue.REPORT, MorphlingHandlingDialogue.INSPECT, OverworldFungalSurveyDialogue.REPORT,
				SpecimenJarLessonDialogue.TEACH, HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD);
		for (HarbingerAlchemistDialogueTrees.RedTaxonomySample sample
				: HarbingerAlchemistDialogueTrees.RedTaxonomySample.values()) {
			events.add(new Progression(Role.ALCHEMIST, sample.eventId()));
		}
		add(events, Role.ARTIFICER, HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB,
				HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME,
				HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS,
				HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD,
				HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING,
				HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT);
		for (MnemonistStarterMemoryChoice choice : MnemonistStarterMemoryChoice.values()) {
			events.add(new Progression(Role.MNEMONIST, choice.eventId()));
		}
		add(events, Role.MNEMONIST, HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE,
				HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN,
				HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT,
				HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL,
				HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL,
				HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON);
		add(events, Role.ANCHORITE, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D4,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D5,
				HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D6);
		add(events, Role.VOYAGER, VoyagerIntroductionDialogue.OBSERVE, CourtDiscoveryDialogue.VOYAGER_LEARN,
				CourtDiscoveryDialogue.VOYAGER_CHART);
		return events;
	}

	private static void add(List<Progression> events, Role role, String... eventIds) {
		for (String eventId : eventIds) events.add(new Progression(role, eventId));
	}

	/** Right-clicks each wing resident and returns what each offers now. */
	private static Map<Role, List<String>> offeredByRole(PlaythroughPlayer player, PlaythroughSites.OutpostWing wing) {
		Map<Role, List<String>> offered = new EnumMap<>(Role.class);
		for (Role role : wing.residents().keySet()) {
			offered.put(role, player.tryTalkTo(wing.get(role)).map(DialogueView::reachableEvents).orElse(List.of()));
		}
		return offered;
	}

	/**
	 * In every swept state, each progression event its own speaker does not offer is forged to that speaker (the
	 * server never checks the event was in the tree it sent). None may give an item, drop one, earn an advancement,
	 * change the degree or report a delivered reward.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void unofferedEventsRefusedSweep(GameTestHelper helper) {
		String scenario = "unofferedEventsRefusedSweep";
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, helper.absolutePos(WING), HARBINGER_WING);
		List<Progression> events = progression();
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (SweepState state : STATES) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(helper, wing.playerSpot(), state, null);
				try {
					Map<Role, List<String>> offered = offeredByRole(player, wing);
					int forged = 0;
					for (Progression event : events) {
						if (offered.getOrDefault(event.role(), List.of()).contains(event.eventId())) continue;
						Entity speaker = wing.get(event.role());
						forged++;
						List<String> into = findings;
						if (SpecimenJarLessonDialogue.TEACH.equals(event.eventId()) && state.degree() >= 3) {
							// DECISION: the Specimen Jar lesson is offered only from the Degree 2 tree's
							// living_bestiary_intro node, but SpecimenJarLesson.canTeach (and so a forged event)
							// holds from Degree 2 on. A player who reaches D3 untaught is never offered it again.
							// Options: offer it at D3+ too / gate canTeach to D2 / keep. Logged, not failed.
							into = new ArrayList<>();
						}
						forgeExpectingNothing(helper, scenario, player, event.eventId(), speaker.getId(),
								state + ": unoffered " + event, into);
						if (into != findings) {
							for (String note : into) PlaythroughLog.note(helper, scenario, "DECISION specimen jar " + note);
						}
					}
					PlaythroughLog.note(helper, scenario, state + ": forged " + forged + " unoffered events; offered "
							+ offered);
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * FIX: F-3. Each progression event is forged to a Harbinger who does not speak it, and with entityId 0 (no speaker),
	 * in each state with active blood where the real speaker could grant it. Neither may change anything a player can
	 * see. Events more than one Harbinger speaks skip the wrong-speaker half.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void wrongSpeakerAndEntityZeroSweep(GameTestHelper helper) {
		String scenario = "wrongSpeakerAndEntityZeroSweep";
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, helper.absolutePos(WING), HARBINGER_WING);
		List<Progression> events = progression();
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (SweepState state : activeStates()) {
			clock.then(() -> {
				PlaythroughPlayer wrong = playerIn(helper, wing.playerSpot(), state, null);
				PlaythroughPlayer nobody = playerIn(helper, wing.playerSpot(), state, null);
				try {
					offeredByRole(wrong, wing);
					offeredByRole(nobody, wing);
					for (Progression event : events) {
						if (!MULTI_SPEAKER.contains(event.eventId())) {
							Role other = event.role() == Role.VICAR ? Role.ALCHEMIST : Role.VICAR;
							forgeExpectingNothing(helper, scenario, wrong, event.eventId(), wing.get(other).getId(),
									state + ": " + event + " sent to the " + other, findings);
						}
						forgeExpectingNothing(helper, scenario, nobody, event.eventId(), 0,
								state + ": " + event + " sent with entityId 0", findings);
					}
				} finally {
					wrong.offline();
					nobody.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	private static void forgeExpectingNothing(GameTestHelper helper, String scenario, PlaythroughPlayer player,
			String eventId, int entityId, String where, List<String> findings) {
		Outcome outcome;
		try {
			outcome = player.forge(eventId, entityId);
		} catch (RuntimeException exception) {
			findings.add(where + ": threw " + exception);
			return;
		}
		String change = change(outcome);
		if (change != null) findings.add(where + ": " + change);
		else if (!"{}".equals(outcome.savedDiff())) {
			PlaythroughLog.note(helper, scenario, where + ": no reward, but the save changed " + outcome.savedDiff());
		}
	}

	// =================================================================================================================
	// One-time claims: repeatClaimSweep, ackAndPlainDoubleSend, deathReclaimSweep, rewardLandingSurvey
	// =================================================================================================================

	/** A one-time claim: its speaker, the event (the first of {@code events} offered), and its ARRANGE. */
	private record Claim(String name, Role role, List<String> events, Consumer<PlaythroughPlayer> arrange) {
		@Override
		public String toString() {
			return name;
		}
	}

	private static List<Claim> claims() {
		List<String> starter = new ArrayList<>();
		for (MnemonistStarterMemoryChoice choice : MnemonistStarterMemoryChoice.values()) starter.add(choice.eventId());
		return List.of(
				new Claim("first_bloodcraft", Role.VICAR,
						List.of(HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD), player -> {
							PlaythroughStates.neophyteD1(player);
							readyFirstBloodcraft(player);
						}),
				new Claim("hermit_road_report", Role.VICAR, List.of(HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT),
						player -> {
							PlaythroughStates.neophyteD1(player);
							grant(player, HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT);
						}),
				new Claim("first_draws", Role.ALCHEMIST, List.of(FirstDrawsDialogue.ACCEPT), PlaythroughStates::neophyteD1),
				new Claim("first_separation_brief", Role.ALCHEMIST,
						List.of(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF), PlaythroughStates::votaryD2),
				new Claim("first_separation_claim", Role.ALCHEMIST,
						List.of(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM), player -> {
							PlaythroughStates.votaryD2(player);
							readyFirstSeparation(player);
						}),
				new Claim("mnemonist_starter", Role.MNEMONIST, List.copyOf(starter), PlaythroughStates::votaryD2),
				new Claim("artificer_worn_vow", Role.ARTIFICER,
						List.of(HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD), player -> {
							PlaythroughStates.votaryD2(player);
							grant(player, ArtificerAssignments.WORN_VOW_BRIEFED);
							grant(player, HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED);
							grant(player, HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE);
						}),
				new Claim("woven_vessel", Role.MNEMONIST, List.of(HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN),
						player -> {
							PlaythroughStates.initiateD3(player);
							player.give(new ItemStack(ItemInit.hematic_memory.get()));
							player.give(new ItemStack(Items.BOOK));
							player.give(new ItemStack(Items.INK_SAC));
							player.give(new ItemStack(Items.PAPER, 3));
						}),
				new Claim("masons_respite_directive", Role.VICAR,
						List.of(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE), PlaythroughStates::adeptD4),
				new Claim("vein_mason_first_lesson", Role.ANCHORITE,
						List.of(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON), PlaythroughStates::adeptD4));
	}

	/** ARRANGE: the four D1 proofs, through their recorders, unclaimed. */
	private static void readyFirstBloodcraft(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		FirstBloodcraftAssignment.recordAbsorption(server, 500.0D);
		FirstBloodcraftAssignment.recordFormation(server);
		FirstBloodcraftAssignment.recordVenousStone(server);
		FirstBloodcraftAssignment.recordStructure(server, new ItemStack(ItemInit.liber_sanguinum.get()));
	}

	/** ARRANGE: briefed, its vials delivered, the assigned spin started and finished, the tools unclaimed. */
	private static void readyFirstSeparation(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		FirstSeparationAssignment.markBriefed(server);
		FirstSeparationAssignment.giveBriefingSupplies(server);
		grant(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
		grant(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
	}

	private static void grant(PlaythroughPlayer player, ResourceLocation advancement) {
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), advancement);
	}

	/** A prepared claim: the player in its state, its speaker beside them, and the event they were shown. */
	private record ClaimRun(PlaythroughPlayer player, Entity npc, String eventId) {
		void close() {
			player.offline();
			npc.discard();
		}
	}

	@Nullable
	private static ClaimRun prepareClaim(GameTestHelper helper, Claim claim, List<String> findings) {
		Entity npc = PlaythroughNpcs.spawn(helper, claim.role(), helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		claim.arrange().accept(player); // ARRANGE
		Optional<DialogueView> view = player.tryTalkTo(npc);
		if (view.isPresent()) {
			for (String eventId : claim.events()) {
				if (view.get().offers(eventId)) return new ClaimRun(player, npc, eventId);
			}
		}
		findings.add(claim + ": not offered once its preconditions hold; the " + claim.role() + " showed "
				+ view.map(DialogueView::describe).orElse("nothing"));
		player.offline();
		npc.discard();
		return null;
	}

	/** One send, with what it added to PendingDelivery measured around it. */
	private record Take(Outcome outcome, int pendingAdded) {
		int reward() {
			return gained(outcome) + pendingAdded;
		}

		@Override
		public String toString() {
			return "reward " + reward() + " (pending +" + pendingAdded + ") " + outcome.describe();
		}
	}

	private static Take take(PlaythroughPlayer player, Supplier<Outcome> send) {
		int before = pendingOwed(player.player());
		Outcome outcome = send.get();
		return new Take(outcome, Math.max(0, pendingOwed(player.player()) - before));
	}

	private static int pendingOwed(ServerPlayer player) {
		int owed = 0;
		for (PendingDelivery.Claim claim : PendingDelivery.claims()) {
			for (ItemStack stack : claim.owed().apply(player)) owed += stack.getCount();
		}
		return owed;
	}

	private static boolean offeredNow(PlaythroughPlayer player, Entity npc, String eventId) {
		return player.tryTalkTo(npc).map(view -> view.offers(eventId)).orElse(false);
	}

	/**
	 * Every one-time claim chosen twice in one tick (a double click on the same screen), then again after a relog:
	 * the first gives its reward and the others give nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void repeatClaimSweep(GameTestHelper helper) {
		String scenario = "repeatClaimSweep";
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Claim claim : claims()) {
			clock.then(() -> {
				ClaimRun run = prepareClaim(helper, claim, findings);
				if (run == null) return;
				try {
					PlaythroughPlayer player = run.player();
					Take first = take(player, () -> player.choose(run.npc(), run.eventId()));
					Take second = take(player, () -> player.choose(run.npc(), run.eventId()));
					player.relog();
					Take third = take(player, () -> offeredNow(player, run.npc(), run.eventId())
							? player.choose(run.npc(), run.eventId()) : player.forge(run.eventId(), run.npc().getId()));
					PlaythroughLog.note(helper, scenario, claim + ": first " + first.reward() + ", same tick "
							+ second.reward() + ", after relog " + third.reward());
					if (first.reward() <= 0) findings.add(claim + ": the first claim gave nothing; " + first);
					if (second.reward() != 0) findings.add(claim + ": a second claim in the same tick paid again; " + second);
					if (third.reward() != 0) findings.add(claim + ": a claim after relog paid again; " + third);
				} finally {
					run.close();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * Every one-time claim sent by a player who cannot take it: dead (health 0, not yet respawned, with the screen
	 * still open) and in spectator mode. Neither may give a reward, an advancement or a degree.
	 */
	// FIX: DialogueOptionPacket#dispatch checked neither life nor spectator mode; it now refuses both.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void deadOrSpectatorChoiceRefused(GameTestHelper helper) {
		String scenario = "deadOrSpectatorChoiceRefused";
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Claim claim : claims()) {
			clock.then(() -> {
				ClaimRun run = prepareClaim(helper, claim, findings);
				if (run == null) return;
				try {
					PlaythroughPlayer player = run.player();
					ServerPlayer server = player.player();
					server.setHealth(0.0F); // ARRANGE: dead, the dialogue screen still open
					Take dead = take(player, () -> player.forge(run.eventId(), run.npc().getId()));
					server.setHealth(server.getMaxHealth());
					server.setGameMode(net.minecraft.world.level.GameType.SPECTATOR); // ARRANGE
					Take spectating = take(player, () -> player.forge(run.eventId(), run.npc().getId()));
					server.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
					PlaythroughLog.note(helper, scenario, claim + ": dead " + dead.reward() + ", spectator " + spectating.reward());
					for (Take attempt : List.of(dead, spectating)) {
						Outcome outcome = attempt.outcome();
						if (attempt.reward() != 0 || !outcome.earned().isEmpty() || outcome.degreeAfter() != outcome.degreeBefore()) {
							findings.add(claim + ": " + (attempt == dead ? "a dead" : "a spectating") + " player was served; "
									+ attempt);
						}
					}
				} finally {
					run.close();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * Each reward that needs an acknowledged claim sent both ways, in both orders: the acknowledged payload then the
	 * plain option payload, and the plain one first. Only the first may pay.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void ackAndPlainDoubleSend(GameTestHelper helper) {
		String scenario = "ackAndPlainDoubleSend";
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Claim claim : claims()) {
			for (boolean ackFirst : new boolean[]{true, false}) {
				clock.then(() -> {
					ClaimRun run = prepareClaim(helper, claim, findings);
					if (run == null) return;
					try {
						if (!DialogueRewardClaims.requiresAcknowledgement(run.eventId())) return;
						PlaythroughPlayer player = run.player();
						int id = run.npc().getId();
						Take first = take(player, () -> ackFirst ? player.choose(run.npc(), run.eventId())
								: player.forgeOption(run.eventId(), id));
						Take second = take(player, () -> ackFirst ? player.forgeOption(run.eventId(), id)
								: player.forgeClaim(run.eventId(), id));
						String order = ackFirst ? "ack then plain" : "plain then ack";
						PlaythroughLog.note(helper, scenario, claim + " " + order + ": " + first.reward() + " then "
								+ second.reward());
						if (first.reward() <= 0) findings.add(claim + " " + order + ": the first send gave nothing; " + first);
						if (second.reward() != 0) findings.add(claim + " " + order + ": the second send paid again; " + second);
					} finally {
						run.close();
					}
				}).idle(1);
			}
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * FIX: F-4. Every one-time claim, then a real death and respawn: the claim is not offered again and a forged one
	 * pays nothing. Covers the root-key claims (the Mnemonist starter memory, the Artificer's Worn Vow scrap) that the
	 * death clone drops today.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void deathReclaimSweep(GameTestHelper helper) {
		String scenario = "deathReclaimSweep";
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Claim claim : claims()) {
			clock.then(() -> {
				ClaimRun run = prepareClaim(helper, claim, findings);
				if (run == null) return;
				try {
					PlaythroughPlayer player = run.player();
					Take first = take(player, () -> player.choose(run.npc(), run.eventId()));
					Outcome death = player.die();
					if (!player.player().isDeadOrDying()) {
						findings.add(claim + ": real damage did not kill the player; " + death);
						return;
					}
					player.respawn();
					boolean reoffered = offeredNow(player, run.npc(), run.eventId());
					Take again = take(player, () -> player.forge(run.eventId(), run.npc().getId()));
					PlaythroughLog.note(helper, scenario, claim + ": first " + first.reward() + ", re-offered after death "
							+ reoffered + ", forged after death " + again.reward());
					if (first.reward() <= 0) findings.add(claim + ": the first claim gave nothing; " + first);
					if (reoffered) findings.add(claim + ": offered again after death");
					if (again.reward() != 0) findings.add(claim + ": a claim after death paid again; " + again);
				} finally {
					run.close();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/**
	 * DECISION: D-3 one-time NPC rewards drop unowned at the NPC's feet. For every claim, records where the reward
	 * lands (inventory, PendingDelivery, the player's feet, the NPC's feet) and expects only the first two.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void rewardLandingSurvey(GameTestHelper helper) {
		String scenario = "rewardLandingSurvey";
		List<String> findings = new ArrayList<>();
		List<String> table = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (Claim claim : claims()) {
			clock.then(() -> {
				ClaimRun run = prepareClaim(helper, claim, findings);
				if (run == null) return;
				try {
					PlaythroughPlayer player = run.player();
					Take first = take(player, () -> player.choose(run.npc(), run.eventId()));
					Outcome outcome = first.outcome();
					int inventory = 0;
					for (int count : outcome.inventoryDelta().values()) if (count > 0) inventory += count;
					int atNpc = dropsNear(outcome, Outcome.Near.NPC);
					int atPlayer = dropsNear(outcome, Outcome.Near.PLAYER);
					String row = claim + ": inventory +" + inventory + ", pending +" + first.pendingAdded()
							+ ", player's feet " + atPlayer + ", NPC's feet " + atNpc + " " + outcome.drops();
					table.add(row);
					if (atNpc + atPlayer > 0) findings.add(row);
				} finally {
					run.close();
				}
			}).idle(1);
		}
		clock.then(() -> {
			for (String row : table) PlaythroughLog.note(helper, scenario, "landing " + row);
			finish(helper, scenario, findings);
		}).run();
	}

	// =================================================================================================================
	// attentionPointsToTheNextMainStep (X F-12)
	// =================================================================================================================

	private record MainStep(String label, Role owner, Consumer<PlaythroughPlayer> arrange) {
	}

	/**
	 * FIX: F-12. Along the Main route, once the player has read each speaker's lore, the NPC owning the next Main
	 * step shows NOTICE or URGENT over its head. The others are logged (the scenario expects NONE; Side work may light
	 * them, which the log shows for the user to rule on).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void attentionPointsToTheNextMainStep(GameTestHelper helper) {
		String scenario = "attentionPointsToTheNextMainStep";
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, helper.absolutePos(WING), Role.VICAR,
				Role.ALCHEMIST, Role.ARTIFICER, Role.MNEMONIST);
		List<MainStep> steps = List.of(
				new MainStep("D0 charm-bearer: the Vicar's initiation", Role.VICAR,
						player -> PlaythroughStates.charmBearerD0(player, (Entity) null)),
				new MainStep("D1 with the four proofs: the Vicar's claim", Role.VICAR, player -> {
					PlaythroughStates.neophyteD1(player);
					readyFirstBloodcraft(player);
				}),
				new MainStep("D2: the Alchemist's First Separation", Role.ALCHEMIST, PlaythroughStates::votaryD2),
				new MainStep("D3: the Mnemonist's Woven Vessel", Role.MNEMONIST, PlaythroughStates::initiateD3),
				new MainStep("D4: the Vicar's Mason's Respite directive", Role.VICAR, PlaythroughStates::adeptD4));
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (MainStep step : steps) {
			clock.then(() -> {
				PlaythroughPlayer player = PlaythroughPlayer.create(helper, wing.playerSpot());
				try {
					step.arrange().accept(player); // ARRANGE
					for (Role role : wing.residents().keySet()) readLore(player, wing.get(role));
					Map<Role, DialogueAttention> sigils = new EnumMap<>(Role.class);
					for (Role role : wing.residents().keySet()) {
						player.tryTalkTo(wing.get(role));
						sigils.put(role, Dialogue.attention(wing.get(role), player.player()));
					}
					PlaythroughLog.note(helper, scenario, step.label() + ": " + sigils);
					if (sigils.get(step.owner()) == DialogueAttention.NONE) {
						findings.add(step.label() + ": the " + step.owner() + " shows no sigil; " + sigils);
					}
					for (Map.Entry<Role, DialogueAttention> sigil : sigils.entrySet()) {
						if (sigil.getKey() != step.owner() && sigil.getValue() != DialogueAttention.NONE) {
							PlaythroughLog.note(helper, scenario, step.label() + ": also lit, the " + sigil.getKey() + " "
									+ sigil.getValue() + " "
									+ player.tryTalkTo(wing.get(sigil.getKey())).map(DialogueView::describe).orElse(""));
						}
					}
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/** Opens every unread lore card the speaker shows, the way the hub screen records a read. */
	private static void readLore(PlaythroughPlayer player, Entity npc) {
		Optional<DialogueView> view = player.tryTalkTo(npc);
		if (view.isEmpty()) return;
		for (DialogueTopic topic : view.get().topics()) {
			if (topic.lore() && topic.unread() && topic.state().enabled()) player.openTopic(npc, topic.id());
		}
	}

	// =================================================================================================================
	// degreeHintMatchesTheChapterGate (D D-7)
	// =================================================================================================================

	private static final Map<HarbingerChapterMilestone, List<String>> MILESTONE_WORDS = Map.of(
			HarbingerChapterMilestone.FIRST_BLOODCRAFT, List.of("first bloodcraft", "four proofs"),
			HarbingerChapterMilestone.FIRST_DISTILLATION, List.of("separation", "distillation", "concentrated blood"),
			HarbingerChapterMilestone.WOVEN_VESSEL, List.of("woven vessel", "mnemonist", "weave", "loom"),
			HarbingerChapterMilestone.VEIN_MASON, List.of("vein-mason", "mason", "effigy"));

	/**
	 * DECISION: D-7 the Vicar's D3 hint says "Bloodline system" and the D4 hint says "skill tree"; neither names the
	 * real gate. At D1 to D4 the Vicar's degree hint should name the chapter milestone the next rank needs
	 * ({@code HarbingerChapterMilestone.requiredForTargetDegree}): its title or a word that points at it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void degreeHintMatchesTheChapterGate(GameTestHelper helper) {
		String scenario = "degreeHintMatchesTheChapterGate";
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		List<String> findings = new ArrayList<>();
		for (SweepState state : STATES) {
			if (state.degree() < 1 || state.degree() > 4 || !state.active()) continue;
			PlaythroughPlayer player = playerIn(helper, helper.absolutePos(STAND), state, null);
			try {
				DialogueView view = player.talkTo(vicar);
				HarbingerChapterMilestone milestone = HarbingerChapterMilestone.requiredForTargetDegree(state.degree() + 1);
				String chapter = milestone == null ? ""
						: english("milestone.hemomancy.chapter." + milestone.name().toLowerCase(Locale.ROOT));
				List<String> hints = view.lineKeys().stream().filter(key -> key.endsWith(".degree_hint")).toList();
				boolean names = false;
				for (String hint : hints) names |= namesMilestone(english(hint), chapter, milestone);
				PlaythroughLog.note(helper, scenario, state + ": needs " + milestone + " (\"" + chapter + "\"); hints "
						+ hints + " name it: " + names);
				if (milestone == null) continue;
				if (hints.isEmpty()) findings.add(state + ": the Vicar shows no degree hint; " + view.describe());
				else if (!names) {
					findings.add(state + ": the degree hint " + hints + " does not name " + chapter + ": "
							+ hints.stream().map(PlaythroughNpcUsabilityGameTests::english).toList());
				}
			} finally {
				player.offline();
			}
		}
		finish(helper, scenario, findings);
		helper.succeed();
	}

	private static boolean namesMilestone(String text, String chapter, @Nullable HarbingerChapterMilestone milestone) {
		String lower = text.toLowerCase(Locale.ROOT);
		if (!chapter.isEmpty() && lower.contains(chapter.toLowerCase(Locale.ROOT))) return true;
		if (milestone == null) return false;
		for (String word : MILESTONE_WORDS.getOrDefault(milestone, List.of())) if (lower.contains(word)) return true;
		return false;
	}

	// =================================================================================================================
	// newcomerGuidanceOnlyBeforeTheHermit (X F-6)
	// =================================================================================================================

	/**
	 * FIX: F-6. At D0 the outpost NPCs show the newcomer card until the player has talked to a Hermit. Standing within
	 * 48 blocks of one (the sigil read every half second) is not talking to him, and once the card is gone it stays
	 * gone after a death.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void newcomerGuidanceOnlyBeforeTheHermit(GameTestHelper helper) {
		Entity alchemist = PlaythroughNpcs.alchemist(helper, helper.absolutePos(NPC));
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(new BlockPos(8, Y, 12)));
		HarbingerHermitEntity hermit = PlaythroughNpcs.hermit(helper, helper.absolutePos(new BlockPos(27, Y, 20)));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.freshD0(player); // ARRANGE

		helper.assertTrue(newcomerCard(player, alchemist), "a fresh D0 player sees the Alchemist's newcomer card; "
				+ player.talkTo(alchemist).describe());
		helper.assertTrue(newcomerCard(player, vicar), "a fresh D0 player sees the Vicar's newcomer card; "
				+ player.talkTo(vicar).describe());

		int markers = player.sent(PacketSyncNpcProgressionMarkers.class).size();
		player.tickOnce();
		PlaythroughLog.note(helper, "newcomerGuidanceOnlyBeforeTheHermit", "standing "
				+ String.format("%.1f", player.player().distanceTo(hermit)) + " blocks from the Hermit, the sigil read sent "
				+ (player.sent(PacketSyncNpcProgressionMarkers.class).size() - markers) + " marker payload(s)");
		helper.assertTrue(newcomerCard(player, alchemist),
				"standing near a Hermit without talking to him keeps the Alchemist's newcomer card");

		player.approach(hermit);
		player.talkTo(hermit);
		player.moveTo(helper.absolutePos(STAND));
		helper.assertFalse(newcomerCard(player, alchemist), "after talking to the Hermit the newcomer card is gone; "
				+ player.talkTo(alchemist).describe());

		Outcome death = player.die();
		helper.assertTrue(player.player().isDeadOrDying(), "real damage kills the player; " + death);
		player.respawn();
		helper.assertFalse(newcomerCard(player, alchemist), "the newcomer card stays gone after a death; "
				+ player.talkTo(alchemist).describe());
		helper.assertFalse(newcomerCard(player, vicar), "the Vicar's newcomer card stays gone after a death");
		helper.succeed();
	}

	private static boolean newcomerCard(PlaythroughPlayer player, Entity npc) {
		DialogueView view = player.talkTo(npc);
		return view.hasNode("newcomer_vicar") || view.hasNode("newcomer_path");
	}

	// =================================================================================================================
	// tradeBoxSweep_<npc> (H)
	// =================================================================================================================

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_hermit(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.HERMIT);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_vicar(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.VICAR);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_alchemist(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.ALCHEMIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_artificer(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.ARTIFICER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_mnemonist(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.MNEMONIST);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_anchorite(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.ANCHORITE);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_voyager(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.VOYAGER);
	}

	@GameTest(template = EMPTY, timeoutTicks = 400, batch = SWEEP_BATCH)
	public static void tradeBoxSweep_votaryWayfarer(GameTestHelper helper) {
		tradeBoxSweep(helper, Role.VOTARY_WAYFARER);
	}

	/**
	 * Every swept state: the Trade box opens; every card, paid for with what it asks (ARRANGE), trades or refuses with
	 * a reason key that exists; rapport moves only for this speaker, and never on a refusal; and the Vicar's tithe at
	 * 0 mL refuses cleanly.
	 */
	private static void tradeBoxSweep(GameTestHelper helper, Role role) {
		String scenario = "tradeBoxSweep_" + slug(role);
		Entity npc = PlaythroughNpcs.spawn(helper, role, helper.absolutePos(NPC));
		String speaker = ((NpcTradeSpeaker) npc).tradeSpeakerId();
		List<String> findings = new ArrayList<>();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		for (SweepState state : STATES) {
			clock.then(() -> {
				PlaythroughPlayer player = playerIn(helper, helper.absolutePos(STAND), state, ownHermit(role, npc));
				try {
					Optional<DialogueView> opened = player.tryTalkTo(npc);
					if (opened.isEmpty()) {
						findings.add(state + ": a right click opened nothing, so no Trade box");
						return;
					}
					DialogueView view = opened.get();
					if (!view.tradeBoard().present() || view.topics(DialogueCategory.TRADE).isEmpty()) {
						findings.add(state + ": the Trade box is missing or empty; " + view.describe());
						return;
					}
					if (role == Role.VICAR && state.active()) titheAtZero(player, npc, state, findings);
					if (state.active()) PlaythroughStates.blood(player, 2000.0D); // ARRANGE
					List<String> rows = new ArrayList<>();
					for (NpcTradeCard card : view.tradeCards()) {
						int[] slots = pay(player, card); // ARRANGE: the goods the card asks for
						Map<String, Integer> before = rapport(player);
						NpcTradeResultPacket result = player.trade(npc, card.offerId(), slots);
						Map<String, Integer> after = rapport(player);
						String where = state + ": " + card.offerId();
						if (result == null) {
							findings.add(where + " was dropped unanswered; " + player.last().describe());
							continue;
						}
						rows.add(card.offerId() + "=" + result.outcome() + "(" + result.reasonKey() + ")");
						if (!LangKeys.exists(result.reasonKey())) {
							findings.add(where + " answered with " + result.reasonKey() + ", which has no translation");
						}
						for (String other : TRADE_SPEAKERS) {
							if (!other.equals(speaker) && !Objects.equals(before.get(other), after.get(other))) {
								findings.add(where + " moved " + other + "'s rapport " + before.get(other) + "->" + after.get(other));
							}
						}
						if (result.outcome() == NpcTradeService.Outcome.REFUSED
								&& !Objects.equals(before.get(speaker), after.get(speaker))) {
							findings.add(where + " refused but moved rapport " + before.get(speaker) + "->" + after.get(speaker));
						}
					}
					PlaythroughLog.note(helper, scenario, state + ": " + rows);
				} finally {
					player.offline();
				}
			}).idle(1);
		}
		clock.then(() -> finish(helper, scenario, findings)).run();
	}

	/** The tithe asked of a player with no blood: a refusal with a key, no blood taken, nothing given. */
	private static void titheAtZero(PlaythroughPlayer player, Entity vicar, SweepState state, List<String> findings) {
		ResourceLocation tithe = Hemomancy.rloc("vicar/tithe");
		PlaythroughStates.blood(player, 0.0D); // ARRANGE
		NpcTradeResultPacket result = player.trade(vicar, tithe);
		Outcome outcome = player.last();
		String where = state + ": the tithe at 0 mL";
		if (result == null) {
			findings.add(where + " was dropped unanswered");
			return;
		}
		if (result.outcome() != NpcTradeService.Outcome.REFUSED) findings.add(where + " was accepted: " + result.outcome());
		if (!LangKeys.exists(result.reasonKey())) findings.add(where + " refused with " + result.reasonKey() + ", no translation");
		if (outcome.bloodSpent() != 0.0D || gained(outcome) > 0) findings.add(where + " changed things: " + outcome.describe());
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
			int slot = putInFreeSlot(player, stack);
			if (slot >= 0) slots.add(slot);
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

	/** ARRANGE: puts {@code stack} in the first free main slot; -1 when there is none. */
	private static int putInFreeSlot(PlaythroughPlayer player, ItemStack stack) {
		var inventory = player.player().getInventory();
		int slot = inventory.getFreeSlot();
		if (slot < 0) return -1;
		inventory.setItem(slot, stack);
		return slot;
	}

	// =================================================================================================================
	// dissolvingHermitRefusesTrade (X F-15)
	// =================================================================================================================

	/** FIX: F-15. A trade asked of a Hermit during his 250-tick farewell is refused, and the bread stays put. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void dissolvingHermitRefusesTrade(GameTestHelper helper) {
		HarbingerHermitEntity hermit = PlaythroughNpcs.hermit(helper, helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.charmBearerD0(player, hermit); // ARRANGE: this Hermit's heart, claimed at his temple
		player.approach(hermit);
		DialogueView view = player.talkTo(hermit);
		helper.assertTrue(view.offers("hermit_farewell_die"), "the Hermit offers his farewell; " + view.describe());
		if (view.offers("hermit_accept_guidance")) player.choose(hermit, "hermit_accept_guidance");
		Outcome farewell = player.choose(hermit, "hermit_farewell_die");
		helper.assertTrue(hermit.isFarewellDying(), "the farewell starts the Hermit's dissolve; " + farewell.describe());

		int slot = putInFreeSlot(player, new ItemStack(Items.BREAD, 3)); // ARRANGE
		NpcTradeResultPacket result = player.trade(hermit, Hemomancy.rloc("hermit/bread"), slot);
		Outcome trade = player.last();
		PlaythroughLog.note(helper, "dissolvingHermitRefusesTrade", "trade during the farewell: "
				+ (result == null ? "dropped" : result.outcome() + " " + result.reasonKey()) + "; " + trade.describe());
		helper.assertTrue(result == null || result.outcome() == NpcTradeService.Outcome.REFUSED,
				"a dissolving Hermit refuses the trade; " + trade.describe());
		helper.assertTrue(player.count(Items.BREAD) == 3 && gained(trade) == 0,
				"the refused trade takes no bread and gives nothing; " + trade.describe());
		helper.succeed();
	}

	// =================================================================================================================
	// misbegottenAndSuccessorRefuse (E)
	// =================================================================================================================

	/**
	 * A misbegotten Alchemist and a successor Alchemist serving another bloodline both refuse a D2 player's First
	 * Separation briefing (offered or forged) and the blood trade.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void misbegottenAndSuccessorRefuse(GameTestHelper helper) {
		String scenario = "misbegottenAndSuccessorRefuse";
		List<String> findings = new ArrayList<>();
		PlaythroughPlayer officiant = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));

		ProfessionalHarbingerEntity misbegotten = PlaythroughNpcs.misbegotten(helper, Role.ALCHEMIST,
				helper.absolutePos(NPC), officiant);
		PlaythroughPlayer visitor = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.votaryD2(visitor); // ARRANGE
		PlaythroughStates.blood(visitor, 2000.0D); // ARRANGE
		refusesBusiness(helper, scenario, visitor, misbegotten, "the misbegotten Alchemist", findings);

		UUID lineId = UUID.randomUUID();
		Bloodline line = new Bloodline("Playthrough Line", officiant.uuid(), lineId,
				new ArrayList<>(List.of(officiant.uuid()))); // ARRANGE: the officiant's bloodline
		BloodlineSavedData lines = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		lines.registerBloodline(line);
		PlaythroughCleanup.onEnd(helper, () -> lines.disbandBloodline(lineId));
		HemoCapabilityAccess.getBloodVolume(officiant.player()).setBloodLine(line);
		ProfessionalHarbingerEntity successor = PlaythroughNpcs.successor(helper, Role.ALCHEMIST,
				helper.absolutePos(new BlockPos(8, Y, 16)), officiant);
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, helper.absolutePos(new BlockPos(10, Y, 16)));
		PlaythroughStates.votaryD2(stranger); // ARRANGE
		PlaythroughStates.blood(stranger, 2000.0D); // ARRANGE
		refusesBusiness(helper, scenario, stranger, successor, "a successor Alchemist of another bloodline", findings);

		finish(helper, scenario, findings);
		helper.succeed();
	}

	private static void refusesBusiness(GameTestHelper helper, String scenario, PlaythroughPlayer player,
			Entity npc, String who, List<String> findings) {
		String brief = HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF;
		Optional<DialogueView> view = player.tryTalkTo(npc);
		PlaythroughLog.note(helper, scenario, who + " served " + view.map(DialogueView::describe).orElse("nothing"));
		Outcome briefing = view.isPresent() && view.get().offers(brief)
				? player.choose(npc, brief) : player.forge(brief, npc.getId());
		if (FirstSeparationAssignment.isBriefed(player.player()) || gained(briefing) > 0) {
			findings.add(who + " briefed First Separation; " + briefing.describe());
		}
		NpcTradeResultPacket result = player.trade(npc, Hemomancy.rloc("alchemist/fresh_draw"));
		Outcome trade = player.last();
		if (result != null && result.outcome() != NpcTradeService.Outcome.REFUSED) {
			findings.add(who + " traded: " + result.outcome() + "; " + trade.describe());
		}
		if (trade.bloodSpent() != 0.0D || gained(trade) > 0) findings.add(who + "'s trade changed things: " + trade.describe());
	}

	// =================================================================================================================
	// replacementNpcKeepsProgress (E)
	// =================================================================================================================

	/**
	 * An Alchemist killed mid-assignment by a Creative player's attacks, and a new one spawned: the First Separation
	 * the first one briefed carries on, and the new one pays the claim.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void replacementNpcKeepsProgress(GameTestHelper helper) {
		Entity first = PlaythroughNpcs.alchemist(helper, helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.votaryD2(player); // ARRANGE
		DialogueView view = player.talkTo(first);
		helper.assertTrue(view.offers(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF),
				"a D2 player is offered the First Separation briefing; " + view.describe());
		Outcome brief = player.choose(first, HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF);
		helper.assertTrue(FirstSeparationAssignment.isBriefed(player.player()), "the briefing is recorded; " + brief);

		String killedBy = creativeKill(player, (LivingEntity) first);
		helper.assertTrue(killedBy != null, "a Creative player can kill the Alchemist");
		PlaythroughLog.note(helper, "replacementNpcKeepsProgress", "the first Alchemist fell to " + killedBy);

		Entity second = PlaythroughNpcs.alchemist(helper, helper.absolutePos(new BlockPos(8, Y, 14)));
		player.moveTo(helper.absolutePos(new BlockPos(10, Y, 14)));
		DialogueView fresh = player.talkTo(second);
		helper.assertFalse(fresh.offers(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF),
				"the new Alchemist does not brief the player again; " + fresh.describe());
		grant(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED); // ARRANGE: the assigned spin
		grant(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE); // ARRANGE
		DialogueView ready = player.talkTo(second);
		helper.assertTrue(ready.offers(HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM),
				"the new Alchemist takes the claim; " + ready.describe());
		Outcome claim = player.choose(second, HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM);
		claim.deliveredIs(true).gave(ItemInit.living_syringe.get(), 1);
		helper.succeed();
	}

	/**
	 * Kills {@code npc} as a Creative player does: hits with a full attack charge, then (when bare hits are too slow
	 * for a test) a Creative player's attack damage. Returns what killed it, or null when it lived.
	 */
	@Nullable
	private static String creativeKill(PlaythroughPlayer player, LivingEntity npc) {
		player.creative();
		try {
			for (int hit = 0; hit < 60 && npc.isAlive(); hit++) {
				if (hit % 10 == 0) player.approach(npc);
				npc.invulnerableTime = 0;
				player.attack(npc);
			}
			if (!npc.isAlive()) return "Creative attacks";
			npc.invulnerableTime = 0;
			npc.hurt(player.player().damageSources().playerAttack(player.player()), npc.getMaxHealth() * 10.0F);
			return npc.isAlive() ? null : "Creative player attack damage";
		} finally {
			player.survival();
		}
	}

	// =================================================================================================================
	// uniqueNpcLossIsRecoverable (D D-10)
	// =================================================================================================================

	/**
	 * DECISION: D-10 the Anchorite is unique to his Respite and nothing replaces him. A D4 player who has taken the
	 * Vicar's map finds him killed: expect some route to the first lesson (an Anchorite back at the Respite, or another
	 * teacher offering the lesson). Logs what is there.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void uniqueNpcLossIsRecoverable(GameTestHelper helper) {
		String scenario = "uniqueNpcLossIsRecoverable";
		PlaythroughSites.MasonsRespite respite = PlaythroughSites.masonsRespite(helper, PlaythroughSites.stand(helper, 8, 8));
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 22, 18));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 18));
		PlaythroughStates.adeptD4(player); // ARRANGE
		DialogueView atVicar = player.talkTo(vicar);
		if (atVicar.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE)) {
			player.choose(vicar, HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
		}
		player.moveTo(PlaythroughSites.stand(helper, 10, 8));
		DialogueView before = player.talkTo(respite.anchorite());
		PlaythroughLog.note(helper, scenario, "before: the Anchorite offers the first lesson "
				+ before.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON));
		String killedBy = creativeKill(player, respite.anchorite());
		helper.assertTrue(killedBy != null, "a Creative player can kill the Anchorite");
		PlaythroughClock.start(helper)
				.idle(40)
				.then(() -> {
					List<HarbingerCicatrixAnchoriteEntity> alive = helper.getLevel().getEntitiesOfClass(
							HarbingerCicatrixAnchoriteEntity.class, new AABB(respite.centre()).inflate(48.0D),
							Entity::isAlive);
					boolean lessonAtRespite = false;
					for (HarbingerCicatrixAnchoriteEntity anchorite : alive) {
						player.approach(anchorite);
						lessonAtRespite |= player.tryTalkTo(anchorite).map(view -> view.offers(
								HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON)).orElse(false);
					}
					player.approach(vicar);
					DialogueView after = player.talkTo(vicar);
					boolean lessonAtVicar = after.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON);
					List<String> masonEvents = after.reachableEvents().stream()
							.filter(event -> event.contains("mason") || event.contains("anchorite")).toList();
					PlaythroughLog.note(helper, scenario, "after the Anchorite fell (" + killedBy + "): living Anchorites "
							+ alive.size() + ", lesson at the Respite " + lessonAtRespite + ", lesson at the Vicar "
							+ lessonAtVicar + ", Vicar's mason events " + masonEvents);
					helper.assertTrue(lessonAtRespite || lessonAtVicar,
							"a D4 player whose Anchorite was killed still has a route to the Vein-Mason chapter; "
									+ "living Anchorites " + alive.size() + ", Vicar offers " + after.reachableEvents());
				})
				.run();
	}

	// =================================================================================================================
	// outpostSpawnsAllResidents (D D-23)
	// =================================================================================================================

	/** A bounding box for {@code PiecesContainer}: the outpost's afterPlace only reads the pieces' box. */
	private static final class MarkerBox extends StructurePiece {
		MarkerBox(BoundingBox box) {
			super(StructurePieceType.JIGSAW, 0, box);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		}

		@Override
		public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
				RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
		}
	}

	/**
	 * DECISION: D-23 an outpost resident whose marker is obstructed silently fails to spawn. The outpost's own
	 * {@code afterPlace} runs over the four resident markers (light blocks 11 to 14, as the template authors them),
	 * one of them with a block over its head. Expect every resident present; log any that is missing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void outpostSpawnsAllResidents(GameTestHelper helper) {
		String scenario = "outpostSpawnsAllResidents";
		ServerLevel level = helper.getLevel();
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("harbinger_outpost"));
		helper.assertTrue(structure instanceof HarbingerOutpostStructure, "the Harbinger Outpost structure is registered");
		Map<EntityType<?>, BlockPos> markers = new LinkedHashMap<>();
		markers.put(EntityInit.harbinger_vicar.get(), PlaythroughSites.stand(helper, 10, 8));
		markers.put(EntityInit.harbinger_artificer.get(), PlaythroughSites.stand(helper, 21, 8));
		markers.put(EntityInit.harbinger_alchemist.get(), PlaythroughSites.stand(helper, 10, 17));
		markers.put(EntityInit.harbinger_mnemonist.get(), PlaythroughSites.stand(helper, 21, 17));
		int lightLevel = 11;
		for (BlockPos marker : markers.values()) {
			level.setBlock(marker, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel++), 3);
			PlaythroughCleanup.onEnd(helper, () -> level.setBlock(marker, Blocks.AIR.defaultBlockState(), 3));
		}
		BlockPos obstructed = markers.get(EntityInit.harbinger_mnemonist.get());
		level.setBlock(obstructed.above(), Blocks.STONE.defaultBlockState(), 3); // the obstruction under test
		PlaythroughCleanup.onEnd(helper, () -> level.setBlock(obstructed.above(), Blocks.AIR.defaultBlockState(), 3));

		BoundingBox box = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(1, Y - 3, 1)),
				helper.absolutePos(new BlockPos(30, Y + 6, 22)));
		((HarbingerOutpostStructure) structure).afterPlace(level, level.structureManager(),
				level.getChunkSource().getGenerator(), level.getRandom(), box, new ChunkPos(obstructed),
				new PiecesContainer(List.of(new MarkerBox(box))));

		List<String> missing = new ArrayList<>();
		Map<String, Integer> found = new LinkedHashMap<>();
		for (EntityType<?> type : markers.keySet()) {
			List<Entity> residents = level.getEntitiesOfClass(Entity.class, helper.getBounds(),
					entity -> entity.getType() == type && entity.isAlive());
			for (Entity resident : residents) PlaythroughCleanup.onEnd(helper, resident::discard);
			found.put(type.getDescriptionId(), residents.size());
			if (residents.isEmpty()) missing.add(type.getDescriptionId() + " (marker " + markers.get(type).toShortString()
					+ (markers.get(type).equals(obstructed) ? ", obstructed" : "") + ")");
		}
		PlaythroughLog.note(helper, scenario, "residents after afterPlace: " + found + "; missing " + missing);
		helper.assertTrue(missing.isEmpty(), "every outpost resident spawns (or is moved to a clear spot); missing "
				+ missing);
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

	private static int dropsNear(Outcome outcome, Outcome.Near near) {
		int count = 0;
		for (Outcome.Drop drop : outcome.drops()) if (drop.near(near)) count += drop.stack().getCount();
		return count;
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
	private static void finish(GameTestHelper helper, String scenario, List<String> findings) {
		for (String finding : findings) PlaythroughLog.note(helper, scenario, "FINDING " + finding);
		PlaythroughLog.note(helper, scenario, findings.size() + " finding(s)");
		if (findings.isEmpty()) return;
		int shown = Math.min(findings.size(), 10);
		helper.fail(scenario + " found " + findings.size() + ": " + String.join(" | ", findings.subList(0, shown))
				+ (findings.size() > shown ? " | ... (see [playthrough] log lines)" : ""));
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
			try (InputStream in = PlaythroughNpcUsabilityGameTests.class.getResourceAsStream(
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
