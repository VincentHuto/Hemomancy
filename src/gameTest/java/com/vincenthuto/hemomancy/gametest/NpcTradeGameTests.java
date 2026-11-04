package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.ActKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueScreenMode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCards;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeGive;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeGiveRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeOffer;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRegistry;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRequestLimiter;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRequirements;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService.Outcome;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSources;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeRequestPacket;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * NPC trading in a loaded server (docs/archive/tracks/npc_trade/07): the shipped catalogue loads against the live registries and
 * tags, and each kind of trade runs through {@link NpcTradeService#attempt} or the request payload with a real player.
 * A refused trade leaves the inventory, the blood, the rapport and the stock as they were.
 */
@GameTestHolder("npc_trade_validation")
@PrefixGameTestTemplate(false)
public final class NpcTradeGameTests {
	private static final String EMPTY = "empty";
	/** Every shipped card (docs/archive/tracks/npc_trade/05 and 06 Status). */
	private static final int SHIPPED_CARDS = 58;
	private static final BlockPos NPC = new BlockPos(2, 2, 2);
	private static final BlockPos STAND = new BlockPos(4, 2, 2);

	private NpcTradeGameTests() {
	}

	// --- Setup -------------------------------------------------------------------------------------------------

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

	private static Entity spawn(GameTestHelper h, EntityType<?> type) {
		Entity entity = h.spawn(type, NPC);
		entity.setNoGravity(true);
		return entity;
	}

	private static CourtMemberEntity court(GameTestHelper h, CourtRole role) {
		BlockPos at = h.absolutePos(NPC);
		String mark = role.marks().isEmpty() ? "threshold" : role.marks().getFirst();
		h.assertTrue(CourtResidentSpawner.place(h.getLevel(), role, at, Direction.SOUTH.toYRot(), mark),
				role + " has a member type");
		return h.getLevel().getEntitiesOfClass(CourtMemberEntity.class, new AABB(at).inflate(1.5D),
				member -> member.role() == role).stream().max(Comparator.comparingInt(Entity::getId)).orElseThrow();
	}

	private static NpcTradeOffer offer(GameTestHelper h, String speaker, String name) {
		ResourceLocation id = Hemomancy.rloc(speaker + "/" + name);
		var offer = NpcTradeRegistry.byId(id);
		h.assertTrue(offer.isPresent(), id + " is loaded");
		return offer.get();
	}

	/** The one item an {@code items} card wants. */
	private static Item wanted(NpcTradeOffer offer) {
		NpcTradeWant.Items items = (NpcTradeWant.Items) offer.want();
		return BuiltInRegistries.ITEM.get(items.match().items().getFirst());
	}

	private static int wantedCount(NpcTradeOffer offer) {
		return ((NpcTradeWant.Items) offer.want()).count();
	}

	private static ItemStack stack(ServerPlayer player, NpcTradeGive give) {
		return give.toStack(player.registryAccess());
	}

	private static int count(ServerPlayer player, Item item) {
		return player.getInventory().countItem(item);
	}

	private static int count(ServerPlayer player, NpcTradeGive give) {
		return count(player, stack(player, give).getItem());
	}

	private static long day(GameTestHelper h) {
		return NpcTradeRules.day(h.getLevel().getDayTime());
	}

	private static int usesLeft(GameTestHelper h, ServerPlayer player, NpcTradeOffer offer) {
		return HemoCapabilityAccess.npcTrades(player).usesLeft(offer.id(), offer.stock().uses(), day(h),
				offer.stock().restockDays());
	}

	/** One trade through the service, past the request rate limit, and its reply. */
	private static NpcTradeResultPacket trade(GameTestHelper h, CapturedPlayer captured, Entity speaker,
			NpcTradeOffer offer, int... slots) {
		NpcTradeRequestLimiter.logout(captured.player);
		int before = captured.sent(NpcTradeResultPacket.class).size();
		NpcTradeService.attempt(captured.player, speaker, offer.id(), slots);
		List<NpcTradeResultPacket> replies = captured.sent(NpcTradeResultPacket.class);
		h.assertTrue(replies.size() == before + 1, offer.id() + ": the service answers every request it takes");
		return replies.getLast();
	}

	/** One request through the payload handler, as the network delivers it; null when the guards drop it unanswered. */
	private static NpcTradeResultPacket request(CapturedPlayer captured, NpcTradeRequestPacket packet) {
		NpcTradeRequestLimiter.logout(captured.player);
		int before = captured.sent(NpcTradeResultPacket.class).size();
		NpcTradeRequestPacket.handle(packet, context(captured.player));
		List<NpcTradeResultPacket> replies = captured.sent(NpcTradeResultPacket.class);
		return replies.size() > before ? replies.getLast() : null;
	}

	private static IPayloadContext context(ServerPlayer player) {
		return (IPayloadContext) Proxy.newProxyInstance(NpcTradeGameTests.class.getClassLoader(),
				new Class<?>[]{IPayloadContext.class}, (proxy, method, args) -> {
					if (method.getName().equals("player")) return player;
					if (method.getName().equals("enqueueWork")) {
						((Runnable) args[0]).run();
						return CompletableFuture.completedFuture(null);
					}
					throw new UnsupportedOperationException(method.getName());
				});
	}

	/** Interacts like a right click and returns the dialogue the speaker opened. */
	private static DialogueTree open(GameTestHelper h, CapturedPlayer captured, Entity speaker, String label) {
		int before = captured.sent(OpenDialoguePacket.class).size();
		speaker.interact(captured.player, InteractionHand.MAIN_HAND);
		List<OpenDialoguePacket> opened = captured.sent(OpenDialoguePacket.class);
		h.assertTrue(opened.size() > before, label + " opens a dialogue");
		return opened.getLast().tree();
	}

	private static List<String> topicTexts(DialogueTree tree, DialogueCategory category) {
		return tree.presentation().topics(category).stream().map(DialogueTopic::titleKey).toList();
	}

	// --- The catalogue -----------------------------------------------------------------------------------------

	/** Every shipped card loads with the live item registry, give decoding and tags bound; none is rejected. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theShippedCatalogueLoadsCleanAgainstTheLiveRegistries(GameTestHelper h) {
		h.assertTrue(NpcTradeRegistry.errors() == 0, NpcTradeRegistry.errors() + " trade files were rejected; see the log");
		Map<ResourceLocation, NpcTradeOffer> all = NpcTradeRegistry.all();
		h.assertTrue(all.size() == SHIPPED_CARDS, all.size() + " cards loaded, expected " + SHIPPED_CARDS);
		var neverSold = ItemTags.create(NpcTradeGiveRules.NEVER_SOLD);
		var registries = h.getLevel().registryAccess();
		for (NpcTradeOffer offer : all.values()) {
			h.assertTrue(NpcTradeSides.side(offer.speaker()).isPresent(), offer.id() + " has a side");
			List<NpcTradeGive> gives = new ArrayList<>(offer.give());
			gives.addAll(offer.giveFine());
			for (NpcTradeGive give : gives) {
				ItemStack stack = give.toStack(registries);
				h.assertFalse(stack.isEmpty(), offer.id() + " gives a real " + give.item());
				h.assertTrue(stack.getCount() == give.count() && stack.getCount() <= stack.getMaxStackSize(),
						offer.id() + " gives one stack of " + give.item());
				h.assertFalse(stack.is(neverSold), offer.id() + " sells a never_sold item " + give.item());
				h.assertFalse(NpcTradeGiveRules.isBannedById(give.item()), offer.id() + " sells progression");
			}
		}
		h.assertTrue(new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc("fungal_spine"))).is(neverSold),
				"the never_sold tag is bound");
		h.assertTrue(new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc("blood_bolt"))).is(NpcTradeSides.STAINED),
				"the stained tag is bound");
		h.assertTrue(new ItemStack(Items.RABBIT_FOOT).is(ItemTags.create(Hemomancy.rloc("trade/curiosity"))),
				"the curiosity tag is bound");
		h.succeed();
	}

	/**
	 * Every hub speaker opens its hub with the four boxes and its Trade box filled (or its refusal), and the whole
	 * tree survives the dialogue payload's codec. This is where a broken catalogue would show.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "npc_trade_hubs")
	public static void everyHubSpeakerOpensItsFourBoxesWithItsCards(GameTestHelper h) {
		List<Supplier<? extends EntityType<?>>> harbingers = List.of(EntityInit.harbinger_vicar::get,
				EntityInit.harbinger_alchemist::get, EntityInit.harbinger_artificer::get,
				EntityInit.harbinger_mnemonist::get, EntityInit.harbinger_hermit::get,
				EntityInit.harbinger_cicatrix_anchorite::get, EntityInit.harbinger_voyager::get,
				EntityInit.harbinger_votary_wayfarer::get);
		List<Supplier<? extends EntityType<?>>> unstained = List.of(EntityInit.unstained_acolyte::get,
				EntityInit.unstained_zealot::get, EntityInit.unstained_guardian::get, EntityInit.unstained_scout::get);
		List<Supplier<? extends EntityType<?>>> circus = List.of(EntityInit.circus_ringmaster::get,
				EntityInit.circus_acrobat::get, EntityInit.circus_fire_eater::get, EntityInit.circus_knife_thrower::get,
				EntityInit.circus_stilt_walker::get, EntityInit.circus_strongman::get,
				EntityInit.circus_beast_tamer::get, EntityInit.circus_threadkeeper::get,
				EntityInit.circus_understudy::get);
		List<String> seen = new ArrayList<>();
		try (CapturedPlayer adept = player(h, 2, true); CapturedPlayer newcomer = player(h, 0, false)) {
			CircusPlayerProgress.addAcclimation(newcomer.player, 500);
			for (var type : harbingers) seen.add(checkHub(h, adept, spawn(h, type.get())));
			for (var type : unstained) seen.add(checkHub(h, newcomer, spawn(h, type.get())));
			for (var type : circus) seen.add(checkHub(h, newcomer, spawn(h, type.get())));
			for (CourtRole role : EntityInit.court_members.keySet()) seen.add(checkHub(h, adept, court(h, role)));
		}
		h.assertTrue(seen.size() == 8 + 4 + 9 + EntityInit.court_members.size(), "every hub speaker was opened: " + seen);
		h.succeed();
	}

	private static String checkHub(GameTestHelper h, CapturedPlayer captured, Entity entity) {
		String speaker = ((com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker) entity).tradeSpeakerId();
		DialogueTree tree = open(h, captured, entity, speaker);
		h.assertTrue(tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB, speaker + " opens a hub");
		h.assertTrue(tree.presentation().categories().equals(List.of(DialogueCategory.QUESTS,
				DialogueCategory.INQUIRIES, DialogueCategory.CONVERSATION, DialogueCategory.TRADE)),
				speaker + " shows the four boxes in order");
		NpcTradeBoard board = tree.presentation().trade();
		if (!NpcTradeRegistry.offers(speaker).isEmpty()) {
			h.assertTrue(board.speaker().equals(speaker), speaker + " carries its own Trade box");
			h.assertTrue(tree.presentation().hasTopics(DialogueCategory.TRADE), speaker + "'s Trade box opens");
			h.assertTrue(!board.cards().isEmpty() || board.refusalKey().isPresent(), speaker + " shows cards");
		}
		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
		OpenDialoguePacket.STREAM_CODEC.encode(buf, new OpenDialoguePacket(tree));
		DialogueTree decoded = OpenDialoguePacket.STREAM_CODEC.decode(buf).tree();
		h.assertTrue(decoded.presentation().topics().size() == tree.presentation().topics().size()
				&& decoded.presentation().tradeCards().size() == board.cards().size(), speaker + " survives the codec");
		buf.release();
		entity.discard();
		return speaker;
	}

	// --- Items trades ------------------------------------------------------------------------------------------

	/** The Alchemist takes the wanted stack, the goods arrive, and rapport rises with him alone. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anItemsTradeTakesThePriceAndPaysTheGoods(GameTestHelper h) {
		Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
		NpcTradeOffer offer = offer(h, "alchemist", "spent_enzyme");
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			Item want = wanted(offer);
			player.getInventory().setItem(3, new ItemStack(want, wantedCount(offer) + 2));
			NpcTradeGive give = offer.give().getFirst();
			int had = count(player, give);
			NpcTradeResultPacket reply = trade(h, captured, alchemist, offer, 3);
			h.assertTrue(reply.outcome() != Outcome.REFUSED, "the trade goes through: " + reply.reasonKey());
			h.assertTrue(player.getInventory().getItem(3).getCount() == 2, "exactly the asked count is taken");
			h.assertTrue(count(player, give) == had + give.count(), "the goods arrive in the inventory");
			var ledger = HemoCapabilityAccess.npcTrades(player);
			h.assertTrue(ledger.rapport("alchemist") == offer.rapport(), "rapport rises by the card's rapport");
			h.assertTrue(reply.rapport() == ledger.rapport("alchemist"), "the reply carries the new rapport");
			for (String other : NpcTradeSides.all().keySet()) {
				if (!other.equals("alchemist")) h.assertTrue(ledger.rapport(other) == 0, other + " is untouched (Q-5)");
			}
		}
		h.succeed();
	}

	/** A limited card counts its uses; once spent it is sold out. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aLimitedCardCountsItsUsesAndSellsOut(GameTestHelper h) {
		Entity artificer = spawn(h, EntityInit.harbinger_artificer.get());
		NpcTradeOffer offer = offer(h, "artificer", "worn_tools");
		int uses = offer.stock().uses();
		h.assertTrue(offer.stock().limited() && uses > 0, "worn tools is a limited card");
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			for (int i = 0; i <= uses; i++) player.getInventory().setItem(i, new ItemStack(wanted(offer)));
			for (int i = 0; i < uses; i++) {
				NpcTradeResultPacket reply = trade(h, captured, artificer, offer, i);
				h.assertTrue(reply.outcome() != Outcome.REFUSED, "use " + (i + 1) + " goes through: " + reply.reasonKey());
				h.assertTrue(usesLeft(h, player, offer) == uses - i - 1, "use " + (i + 1) + " is counted");
			}
			NpcTradeResultPacket soldOut = trade(h, captured, artificer, offer, uses);
			h.assertTrue(soldOut.reasonKey().equals(NpcTradeService.REFUSED_SOLD_OUT), "sold out: " + soldOut.reasonKey());
			h.assertFalse(player.getInventory().getItem(uses).isEmpty(), "a sold-out card takes nothing");
			NpcTradeCard card = soldOut.board().card(offer.id()).orElseThrow();
			h.assertTrue(card.state() == NpcTradeCard.State.SOLD_OUT && card.daysToRestock() > 0,
					"the board shows the card sold out with its restock wait");
		}
		h.succeed();
	}

	/** Goods that won't fit refuse the trade before anything is taken. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFullInventoryRefusesAndTakesNothing(GameTestHelper h) {
		Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
		NpcTradeOffer offer = offer(h, "alchemist", "spent_enzyme");
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			// One left over keeps the price's slot occupied, so the goods have nowhere to go.
			player.getInventory().setItem(0, new ItemStack(wanted(offer), wantedCount(offer) + 1));
			for (int i = 1; i < 36; i++) player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
			NpcTradeResultPacket reply = trade(h, captured, alchemist, offer, 0);
			h.assertTrue(reply.reasonKey().equals(NpcTradeService.REFUSED_NO_ROOM), "no room: " + reply.reasonKey());
			h.assertTrue(player.getInventory().getItem(0).getCount() == wantedCount(offer) + 1, "the price is kept");
			h.assertTrue(count(player, offer.give().getFirst()) == 0, "no goods were dropped or given");
			h.assertTrue(HemoCapabilityAccess.npcTrades(player).rapport("alchemist") == 0, "no rapport for a refusal");
			h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
					player.getBoundingBox().inflate(3.0D)).isEmpty(), "nothing waits at the player's feet");
		}
		h.succeed();
	}

	/** A POOR tool is refused and kept; a FINE one pays {@code give_fine}; a FAIR one pays {@code give}. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void appraisalRefusesPoorAndPaysFineForFine(GameTestHelper h) {
		Entity artificer = spawn(h, EntityInit.harbinger_artificer.get());
		NpcTradeOffer offer = offer(h, "artificer", "worn_tools");
		NpcTradeGive give = offer.give().getFirst();
		NpcTradeGive fine = offer.giveFine().getFirst();
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			ItemStack poor = new ItemStack(wanted(offer));
			poor.setDamageValue(poor.getMaxDamage() * 9 / 10);
			player.getInventory().setItem(0, poor);
			NpcTradeResultPacket refused = trade(h, captured, artificer, offer, 0);
			h.assertTrue(refused.reasonKey().equals(NpcTradeService.REFUSED_POOR), "poor: " + refused.reasonKey());
			h.assertTrue(player.getInventory().getItem(0) == poor && !poor.isEmpty(), "the worn tool is kept");
			h.assertTrue(usesLeft(h, player, offer) == offer.stock().uses(), "a refusal spends no stock");

			player.getInventory().setItem(1, new ItemStack(wanted(offer)));
			NpcTradeResultPacket fineReply = trade(h, captured, artificer, offer, 1);
			h.assertTrue(fineReply.outcome() == Outcome.DONE_FINE, "a whole tool grades fine: " + fineReply.reasonKey());
			h.assertTrue(count(player, fine) == fine.count(), "give_fine arrives: " + count(player, fine));

			ItemStack fair = new ItemStack(wanted(offer));
			fair.setDamageValue(fair.getMaxDamage() / 2);
			player.getInventory().setItem(2, fair);
			NpcTradeResultPacket fairReply = trade(h, captured, artificer, offer, 2);
			h.assertTrue(fairReply.outcome() == Outcome.DONE, "a half-worn tool grades fair: " + fairReply.reasonKey());
			h.assertTrue(count(player, give) == fine.count() + give.count(), "the ordinary give arrives");
		}
		h.succeed();
	}

	// --- Blood -------------------------------------------------------------------------------------------------

	/**
	 * A blood card pays through the ledger as {@code npc_tithe}, sells out for the day, and restocks once its days
	 * pass. Runs alone in its batch because it moves the level's clock.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40, batch = "npc_trade_clock")
	public static void aBloodTradePaysTheTitheSellsOutAndRestocks(GameTestHelper h) {
		Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
		NpcTradeOffer offer = offer(h, "alchemist", "fresh_draw");
		int amount = ((NpcTradeWant.Blood) offer.want()).amount();
		long dayTime = h.getLevel().getDayTime();
		try (CapturedPlayer captured = player(h, 1, true); CapturedPlayer dormant = player(h, 1, false)) {
			ServerPlayer player = captured.player;
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			double before = volume.getBloodVolume();
			NpcTradeResultPacket paid = trade(h, captured, alchemist, offer);
			h.assertTrue(paid.outcome() != Outcome.REFUSED, "the tithe is paid: " + paid.reasonKey());
			h.assertTrue(Math.abs(before - volume.getBloodVolume() - amount) < 0.01D,
					"the volume drops by the price: " + (before - volume.getBloodVolume()));
			h.assertTrue(BloodFlowLedger.collect(player, volume).contribution(NpcTradeSources.TITHE.id()).isPresent(),
					"the ledger records npc_tithe");
			h.assertTrue(count(player, offer.give().getFirst()) == offer.give().getFirst().count(), "the goods arrive");

			double afterFirst = volume.getBloodVolume();
			NpcTradeResultPacket soldOut = trade(h, captured, alchemist, offer);
			h.assertTrue(soldOut.reasonKey().equals(NpcTradeService.REFUSED_SOLD_OUT), "sold out: " + soldOut.reasonKey());
			h.assertTrue(volume.getBloodVolume() == afterFirst, "a sold-out card draws nothing");

			h.getLevel().setDayTime(dayTime + (long) offer.stock().restockDays() * com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeStock.TICKS_PER_DAY);
			h.assertTrue(usesLeft(h, player, offer) == offer.stock().uses(), "the card restocks after its days");
			NpcTradeResultPacket restocked = trade(h, captured, alchemist, offer);
			h.assertTrue(restocked.outcome() != Outcome.REFUSED, "it sells again: " + restocked.reasonKey());

			var dormantVolume = HemoCapabilityAccess.getBloodVolume(dormant.player);
			NpcTradeResultPacket inactive = trade(h, dormant, alchemist, offer);
			h.assertTrue(inactive.reasonKey().equals(NpcTradeRequirements.LOCKED_BLOOD), "inactive: " + inactive.reasonKey());
			h.assertTrue(dormantVolume.getBloodVolume() == 5000.0D, "inactive blood is never drawn");
		} finally {
			h.getLevel().setDayTime(dayTime);
		}
		h.succeed();
	}

	// --- The Court ---------------------------------------------------------------------------------------------

	/** A Court custom card takes one offering and records the custom once, exactly as the dialogue offering does. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aCourtCustomRecordsStandingOnceLikeTheDialogueOffering(GameTestHelper h) {
		CourtMemberEntity gardener = court(h, CourtRole.GARDENER);
		NpcTradeOffer offer = offer(h, CourtRole.GARDENER.entityId(), "custom");
		try (CapturedPlayer traded = player(h, 2, true); CapturedPlayer offered = player(h, 2, true)) {
			for (CapturedPlayer captured : List.of(traded, offered)) {
				CourtLife.recordAct(captured.player, CourtHouse.GIVING_LIGHT, CourtLife.VISIT_ACT, ActKind.VISIT);
				captured.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			}
			NpcTradeResultPacket first = trade(h, traded, gardener, offer, 0);
			h.assertTrue(first.outcome() != Outcome.REFUSED, "the custom is taken: " + first.reasonKey());
			h.assertTrue(traded.player.getInventory().getItem(0).getCount() == 1, "one loaf taken");
			h.assertTrue(CourtHospitality.handle(offered.player, gardener, CourtMemberDialogue.OFFER), "the dialogue offering");
			Standing byTrade = CourtLife.standing(traded.player, CourtHouse.GIVING_LIGHT);
			h.assertTrue(byTrade == Standing.FRIEND, "the custom raises standing: " + byTrade);
			h.assertTrue(byTrade == CourtLife.standing(offered.player, CourtHouse.GIVING_LIGHT),
					"the card and the dialogue offering record the same standing");
			NpcTradeResultPacket again = trade(h, traded, gardener, offer, 0);
			h.assertTrue(again.outcome() != Outcome.REFUSED, "the custom is accepted again: " + again.reasonKey());
			h.assertTrue(traded.player.getInventory().getItem(0).isEmpty(), "the second loaf is taken");
			h.assertTrue(CourtLife.standing(traded.player, CourtHouse.GIVING_LIGHT) == Standing.FRIEND,
					"standing counts the custom once");
			h.assertTrue(offer.give().isEmpty(), "a custom pays in standing, never goods (Q-1)");
		}
		h.succeed();
	}

	// --- Stale requests ----------------------------------------------------------------------------------------

	/** The payload's guards drop a request from far away, from a dead player or with repeated slots; a wrong speaker is refused. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void staleRequestsAreDroppedOrRefusedAndTakeNothing(GameTestHelper h) {
		Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
		Entity artificer = h.spawn(EntityInit.harbinger_artificer.get(), new BlockPos(2, 2, 5));
		NpcTradeOffer offer = offer(h, "alchemist", "spent_enzyme");
		int price = wantedCount(offer);
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			player.getInventory().setItem(0, new ItemStack(wanted(offer), price));

			NpcTradeResultPacket wrongSpeaker = request(captured, new NpcTradeRequestPacket(artificer.getId(), offer.id(),
					new int[]{0}));
			h.assertTrue(wrongSpeaker != null && wrongSpeaker.reasonKey().equals(NpcTradeService.REFUSED_UNKNOWN),
					"another speaker's card is refused");
			h.assertTrue(request(captured, new NpcTradeRequestPacket(alchemist.getId(), offer.id(), new int[]{0, 0})) == null,
					"repeated slots are dropped");
			h.assertTrue(request(captured, new NpcTradeRequestPacket(alchemist.getId(), offer.id(), new int[]{36})) == null,
					"a slot outside the main inventory is dropped");
			h.assertTrue(request(captured, new NpcTradeRequestPacket(alchemist.getId(),
					Hemomancy.rloc("alchemist/not_a_card"), new int[]{0})).reasonKey().equals(NpcTradeService.REFUSED_UNKNOWN),
					"an unknown card is refused");

			BlockPos far = h.absolutePos(STAND).offset(40, 0, 0);
			player.moveTo(far.getX() + 0.5D, far.getY(), far.getZ() + 0.5D);
			h.assertTrue(request(captured, new NpcTradeRequestPacket(alchemist.getId(), offer.id(), new int[]{0})) == null,
					"a speaker out of reach is dropped");
			BlockPos stand = h.absolutePos(STAND);
			player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);

			player.setHealth(0.0F);
			h.assertTrue(request(captured, new NpcTradeRequestPacket(alchemist.getId(), offer.id(), new int[]{0})) == null,
					"a dead player is dropped");
			player.setHealth(player.getMaxHealth());

			h.assertTrue(player.getInventory().getItem(0).getCount() == price, "nothing was taken");
			h.assertTrue(HemoCapabilityAccess.npcTrades(player).rapport("alchemist") == 0, "no rapport was recorded");

			NpcTradeResultPacket honest = request(captured, new NpcTradeRequestPacket(alchemist.getId(), offer.id(),
					new int[]{0}));
			h.assertTrue(honest != null && honest.outcome() != Outcome.REFUSED, "the same request, in order, trades");
			NpcTradeService.attempt(player, alchemist, offer.id(), new int[]{0});
			h.assertTrue(captured.sent(NpcTradeResultPacket.class).getLast() == honest,
					"a second request inside the rate limit is dropped without a reply");
		}
		h.succeed();
	}

	// --- The Unstained -----------------------------------------------------------------------------------------

	/** The Zealot trades with a D0 visitor, shows a Harbinger only his refusal, and refuses a forged request. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theUnstainedTradeWithDegreeZeroAndRefuseAHarbinger(GameTestHelper h) {
		Entity zealot = spawn(h, EntityInit.unstained_zealot.get());
		NpcTradeOffer offer = offer(h, "zealot", "clean_copper");
		try (CapturedPlayer visitor = player(h, 0, false); CapturedPlayer harbinger = player(h, 1, true)) {
			visitor.player.getInventory().setItem(0, new ItemStack(wanted(offer), wantedCount(offer)));
			NpcTradeResultPacket traded = trade(h, visitor, zealot, offer, 0);
			h.assertTrue(traded.outcome() != Outcome.REFUSED, "a D0 visitor trades: " + traded.reasonKey());
			h.assertTrue(count(visitor.player, offer.give().getFirst()) == offer.give().getFirst().count(), "the dew arrives");

			NpcTradeBoard board = NpcTradeCards.build(harbinger.player, "zealot");
			h.assertTrue(board.cards().isEmpty(), "a Harbinger sees no cards");
			h.assertTrue(board.refusalKey().orElse("").equals(NpcTradeSides.refusalKey("zealot")), "only the refusal");
			DialogueTree hub = open(h, harbinger, zealot, "zealot");
			h.assertTrue(hub.presentation().topics(DialogueCategory.TRADE).stream().map(DialogueTopic::id).toList()
					.equals(List.of(com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory.TRADE_REFUSAL_TOPIC)),
					"the hub's Trade box holds the refusal alone");

			harbinger.player.getInventory().setItem(0, new ItemStack(wanted(offer), wantedCount(offer)));
			NpcTradeResultPacket forged = request(harbinger, new NpcTradeRequestPacket(zealot.getId(), offer.id(),
					new int[]{0}));
			h.assertTrue(forged != null && forged.outcome() == Outcome.REFUSED
					&& forged.reasonKey().equals(NpcTradeSides.refusalKey("zealot")), "a forged request is refused");
			h.assertTrue(harbinger.player.getInventory().getItem(0).getCount() == wantedCount(offer), "nothing is taken");
			h.assertTrue(count(harbinger.player, offer.give().getFirst()) == 0, "nothing is given");
			h.assertTrue(HemoCapabilityAccess.npcTrades(harbinger.player).rapport("zealot") == 0, "no rapport");
		}
		h.succeed();
	}

	// --- The Circus --------------------------------------------------------------------------------------------

	/** A D0 visitor with dormant blood trades a curiosity with the Ringmaster; his quest options stay in Quest Work. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theRingmasterTradesCuriositiesAndKeepsHisQuestsInQuestWork(GameTestHelper h) {
		Entity ringmaster = spawn(h, EntityInit.circus_ringmaster.get());
		NpcTradeOffer offer = offer(h, NpcTradeSides.CIRCUS_RINGMASTER, "curiosity");
		try (CapturedPlayer captured = player(h, 0, false)) {
			ServerPlayer player = captured.player;
			CircusPlayerProgress.addAcclimation(player, 500);
			player.getInventory().setItem(0, new ItemStack(Items.RABBIT_FOOT));
			NpcTradeGive curtains = offer.give().getFirst();
			NpcTradeResultPacket reply = trade(h, captured, ringmaster, offer, 0);
			h.assertTrue(reply.outcome() != Outcome.REFUSED, "a curiosity is taken: " + reply.reasonKey());
			h.assertTrue(count(player, Items.RABBIT_FOOT) == 0, "the rabbit's foot is taken");
			h.assertTrue(count(player, curtains) >= curtains.count(), "the curtains arrive");
			h.assertFalse(HemoCapabilityAccess.getBloodVolume(player).isActive(), "trading wakes no blood");

			String trial = "hemomancy.circus.option.trial";
			List<String> quests = topicTexts(open(h, captured, ringmaster, "ringmaster"), DialogueCategory.QUESTS);
			h.assertTrue(quests.contains(trial), "the trial is Quest Work: " + quests);
			CircusPlayerProgress.completeChallenge(player, 0, 0);
			quests = topicTexts(open(h, captured, ringmaster, "ringmaster"), DialogueCategory.QUESTS);
			h.assertTrue(quests.containsAll(List.of("hemomancy.circus.option.accept", "hemomancy.circus.option.reject")),
					"accept and reject are Quest Work: " + quests);
			CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.SUCCESSION);
			quests = topicTexts(open(h, captured, ringmaster, "ringmaster"), DialogueCategory.QUESTS);
			h.assertTrue(quests.contains("hemomancy.circus.option.finale"), "the finale is Quest Work: " + quests);
			h.assertFalse(topicTexts(open(h, captured, ringmaster, "ringmaster"), DialogueCategory.CONVERSATION)
					.contains(trial), "no quest leaks into Conversation");
		}
		h.succeed();
	}

	/** A banked spectacle opens the Ringmaster's act card; the trade spends it, and an unbanked act stays locked. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theRingmasterSpendsABankedSpectacle(GameTestHelper h) {
		Entity ringmaster = spawn(h, EntityInit.circus_ringmaster.get());
		NpcTradeOffer offer = offer(h, NpcTradeSides.CIRCUS_RINGMASTER, "spectacle");
		String act = ((NpcTradeWant.Act) offer.want()).act();
		try (CapturedPlayer captured = player(h, 0, false); CapturedPlayer dead = player(h, 0, false)) {
			ServerPlayer player = captured.player;
			CircusPlayerProgress.addAcclimation(player, 500);
			NpcTradeResultPacket unbanked = trade(h, captured, ringmaster, offer);
			h.assertTrue(unbanked.reasonKey().equals(((NpcTradeWant.Act) offer.want()).lockKey()),
					"no act banked: " + unbanked.reasonKey());
			h.assertTrue(com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeActs.witnessed(player, act),
					"a living visitor banks the act");
			dead.player.setHealth(0.0F);
			h.assertFalse(com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeActs.witnessed(dead.player, act),
					"a dead player banks nothing");
			h.assertTrue(NpcTradeCards.build(player, NpcTradeSides.CIRCUS_RINGMASTER).card(offer.id()).orElseThrow()
					.state() == NpcTradeCard.State.OPEN, "the act card opens once an act is banked");
			NpcTradeResultPacket spent = trade(h, captured, ringmaster, offer);
			h.assertTrue(spent.outcome() != Outcome.REFUSED, "the spectacle is spent: " + spent.reasonKey());
			for (NpcTradeGive give : offer.give()) {
				h.assertTrue(count(player, give) >= give.count(), "the act pays " + give.item());
			}
			h.assertTrue(HemoCapabilityAccess.npcTrades(player).acts(act) == 0, "the banked act is gone");
			h.assertTrue(HemoCapabilityAccess.npcTrades(player).rapport(NpcTradeSides.CIRCUS_RINGMASTER) == offer.rapport(),
					"the act card's rapport is paid");
		}
		h.succeed();
	}

	/** A rapport lore line is a locked teaser card below its tier and an openable lore card at it. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void rapportLoreUnlocksAtItsTier(GameTestHelper h) {
		Entity alchemist = spawn(h, EntityInit.harbinger_alchemist.get());
		var lines = com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueRapportLore.lines("alchemist");
		h.assertFalse(lines.isEmpty(), "the Alchemist has rapport lore");
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			for (var line : lines) {
				DialogueTopic topic = loreTopic(h, open(h, captured, alchemist, "alchemist"), line.textKey());
				h.assertTrue(topic.state() == com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopicState.LOCKED
						&& topic.targetNodeId().equals(
								com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory.LOCKED_TARGET)
						&& topic.summaryKey().equals(line.teaserKey()), line.slug() + " is a locked teaser at rapport 0");
			}
			var ledger = HemoCapabilityAccess.npcTrades(player);
			ledger.addRapport("alchemist", NpcTradeRules.tierFloor(NpcTradeRules.TRUSTED));
			DialogueTree trusted = open(h, captured, alchemist, "alchemist");
			for (var line : lines) {
				DialogueTopic topic = loreTopic(h, trusted, line.textKey());
				h.assertTrue(topic.state().enabled() && topic.lore() && topic.unread()
						&& trusted.nodes().containsKey(topic.targetNodeId()), line.slug() + " opens at Trusted");
			}
		}
		h.succeed();
	}

	private static DialogueTopic loreTopic(GameTestHelper h, DialogueTree tree, String textKey) {
		var topic = tree.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.filter(candidate -> candidate.titleKey().equals(textKey)).findFirst();
		h.assertTrue(topic.isPresent(), textKey + " is a Conversation card");
		return topic.get();
	}

	/** Today's daily card is fresh until the Trade box is opened; the seen mark clears it for the day. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDailyCardIsFreshUntilSeen(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 2, true)) {
			ServerPlayer player = captured.player;
			List<NpcTradeCard> daily = NpcTradeCards.build(player, "alchemist").cards().stream()
					.filter(NpcTradeCard::daily).toList();
			h.assertTrue(daily.size() == 1, "one alchemist_daily card is on today's board: " + daily.size());
			h.assertTrue(daily.getFirst().fresh(), "it is fresh before the box is opened");
			h.assertTrue(NpcTradeCards.markRotationSeen(player, "alchemist") == 1, "opening the box marks it seen");
			h.assertFalse(NpcTradeCards.build(player, "alchemist").card(daily.getFirst().offerId()).orElseThrow().fresh(),
					"seen today, it is no longer fresh");
			h.assertTrue(NpcTradeCards.markRotationSeen(player, "alchemist") == 0, "a second open marks nothing");
		}
		h.succeed();
	}
}
