package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisal;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeGive;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeMatch;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * With hemomancy.npcTradeReview (run npcTradeReviewClient, then drop {"op":"create","name":"NpcTradeReview","seed":1} into the
 * operator inbox), spawns the Alchemist, a Zealot, a Gardener and the Ringmaster, gives the reviewer a spread of items
 * (iron, gold, three iron swords at different wear) and opens each hub the way a click does. For each it captures the
 * hub, the Conversation box (lore glyphs, unread dots) and the Trade box as the server built it, then a sample board
 * covering every card state (carried, some, none, a bundle, blood or a Court custom, sold out, locked), the
 * appraised card's strip with all three grades, a refusal under it and a finished trade's thanks. The Zealot is opened
 * twice: once at D0 (cards) and once at D1 (the refusal, checked from the server's own board). Screenshots go to
 * build/npc-trade-review/screenshots; logs NPC_TRADE_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class NpcTradeClientReview {
	private static final int STAGE = 40;
	private static final int START = 100;
	private static final int STEP = 20;
	private static final ResourceLocation MATCHED = Hemomancy.rloc("review/matched");
	private static final ResourceLocation APPRAISED = Hemomancy.rloc("review/appraised");
	private static final ResourceLocation NO_REPLY = Hemomancy.rloc("review/no_reply");
	private static final String TITLE = "hemomancy.dialogue.category.trade";
	private static final String LINE = "hemomancy.dialogue.category.trade.summary";

	private record Step(boolean server, Consumer<ServerPlayer> serverAction, Consumer<Minecraft> clientAction) {}

	private static final Map<String, Integer> SPAWNED = new java.util.HashMap<>();
	private static List<Step> steps;
	private static int ticks;
	private static int shot;

	private NpcTradeClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.npcTradeReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("npc-trade-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		mc.getToasts().clear();
		if (steps == null) steps = script();
		if (ticks == STAGE) {
			// Scale 2 shows several cards at once; the movement tutorial toast would cover the box.
			mc.options.guiScale().set(2);
			mc.resizeDisplay();
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		}
		int index = ticks - START;
		if (index >= 0 && index % STEP == 0) {
			int at = index / STEP;
			if (at < steps.size()) {
				Step step = steps.get(at);
				if (step.server()) {
					server.execute(() -> step.serverAction().accept(server.getPlayerList().getPlayer(uuid)));
				} else {
					step.clientAction().accept(mc);
				}
			} else if (at == steps.size()) {
				mc.stop();
			}
		}
	}

	/** The whole run, one step every second. */
	private static List<Step> script() {
		List<Step> s = new ArrayList<>();
		hub(s, "alchemist", true, false);
		// Track 6: at Trusted the Alchemist's rapport lore opens (glyph and unread dot) and the rapport card unlocks.
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> HemoCapabilityAccess.npcTrades(player).addRapport("alchemist",
				NpcTradeRules.tierFloor(NpcTradeRules.TRUSTED))));
		s.add(server(player -> interact(player, "alchemist")));
		s.add(client(mc -> open(mc, DialogueCategory.CONVERSATION)));
		s.add(client(mc -> capture(mc, "alchemist-conversation-trusted")));
		s.add(client(mc -> open(mc, DialogueCategory.TRADE)));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			report(screen != null && screen.tradeBoard().tier() == NpcTradeRules.TRUSTED,
					"the Alchemist's board reads Trusted");
			capture(mc, "alchemist-trade-trusted");
		}));
		// The Zealot at D0: cards; then at D1 the server's own board must be the refusal.
		hub(s, "zealot", false, false);
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(1)));
		s.add(server(player -> interact(player, "zealot")));
		s.add(client(mc -> open(mc, DialogueCategory.TRADE)));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			report(screen != null && screen.tradeBoard().refusalKey().isPresent()
					&& screen.tradeBoard().cards().isEmpty(), "the Zealot refuses a D1 Harbinger with no cards");
			capture(mc, "zealot-d1-refusal");
		}));
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(0)));
		hub(s, "gardener", false, true);
		hub(s, "ringmaster", false, false);
		return s;
	}

	/** Opens one hub and captures it, its Conversation box, its Trade box and the sample states. */
	private static void hub(List<Step> s, String name, boolean blood, boolean court) {
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> interact(player, name)));
		s.add(client(mc -> {
			report(screen(mc) != null, name + "'s hub opened");
			capture(mc, name + "-hub");
		}));
		s.add(client(mc -> open(mc, DialogueCategory.CONVERSATION)));
		s.add(client(mc -> capture(mc, name + "-conversation")));
		s.add(client(mc -> open(mc, DialogueCategory.TRADE)));
		s.add(client(mc -> capture(mc, name + "-trade-server")));
		s.add(client(mc -> show(mc, sample(name, blood, court), NO_REPLY, NpcTradeService.Outcome.REFUSED, "")));
		s.add(client(mc -> capture(mc, name + "-trade-cards")));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			if (screen != null) screen.expandTradeCard(APPRAISED);
		}));
		s.add(client(mc -> capture(mc, name + "-trade-strip")));
		s.add(client(mc -> show(mc, sample(name, blood, court), APPRAISED, NpcTradeService.Outcome.REFUSED,
				NpcTradeService.REFUSED_POOR)));
		s.add(client(mc -> capture(mc, name + "-trade-refused")));
		s.add(client(mc -> show(mc, sample(name, blood, court), MATCHED, NpcTradeService.Outcome.DONE,
				NpcTradeService.DONE)));
		s.add(client(mc -> capture(mc, name + "-trade-done")));
	}

	private static void stage(ServerPlayer player) {
		MinecraftServer server = player.server;
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] {"gamemode survival", "difficulty peaceful", "gamerule doMobSpawning false",
				"time set day", "gamerule doDaylightCycle false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.getInventory().clearContent();
		player.getInventory().add(new ItemStack(Items.IRON_INGOT, 6));
		player.getInventory().add(new ItemStack(Items.GOLD_INGOT, 1));
		for (float wear : new float[] {0.05F, 0.5F, 0.9F}) {
			ItemStack sword = new ItemStack(Items.IRON_SWORD);
			sword.setDamageValue(Math.round(sword.getMaxDamage() * wear));
			player.getInventory().add(sword);
		}
		// An empty hand to talk with, so the clicks below take nothing out of the review's stacks.
		player.getInventory().selected = 8;
		CircusPlayerProgress.addAcclimation(player, 1000);
		ServerLevel level = player.serverLevel();
		spawn(level, player, "alchemist", EntityInit.harbinger_alchemist.get(), 0);
		spawn(level, player, "zealot", EntityInit.unstained_zealot.get(), 1);
		spawn(level, player, "gardener", EntityInit.court_members.get(CourtRole.GARDENER).get(), 2);
		spawn(level, player, "ringmaster", EntityInit.circus_ringmaster.get(), 3);
		Hemomancy.LOGGER.info("NPC_TRADE_REVIEW staged");
	}

	private static void spawn(ServerLevel level, ServerPlayer player, String name, EntityType<?> type, int index) {
		Entity entity = type.create(level);
		if (entity == null) {
			report(false, name + " spawned");
			return;
		}
		entity.moveTo(player.getX() + 3 + index * 2, player.getY(), player.getZ() + 3, 180F, 0F);
		if (entity instanceof Mob mob) {
			mob.setNoAi(true);
			mob.setPersistenceRequired();
		}
		level.addFreshEntity(entity);
		SPAWNED.put(name, entity.getId());
	}

	private static void interact(ServerPlayer player, String name) {
		Integer id = SPAWNED.get(name);
		Entity entity = id == null ? null : player.serverLevel().getEntity(id);
		if (entity == null) {
			report(false, name + " is there to talk to");
			return;
		}
		entity.interact(player, InteractionHand.MAIN_HAND);
	}

	/** A board covering every card state; {@code blood} adds a blood card, {@code court} a house custom. */
	private static NpcTradeBoard sample(String name, boolean blood, boolean court) {
		String speaker = switch (name) {
			case "gardener" -> CourtRole.GARDENER.entityId();
			case "ringmaster" -> NpcTradeSides.CIRCUS_RINGMASTER;
			default -> name;
		};
		NpcTradeAppraisal durability = new NpcTradeAppraisal(NpcTradeAppraisals.DURABILITY, Map.of());
		List<NpcTradeCard> cards = new ArrayList<>();
		cards.add(card("unmatched", items(tag("minecraft:logs"), 8), gives("minecraft:bread", 2), List.of(),
				NpcTradeCard.State.OPEN, Optional.empty(), 0, NpcTradeCard.UNLIMITED));
		cards.add(card("matched", items(item("minecraft:iron_ingot"), 4), gives("minecraft:bread", 3), List.of(),
				NpcTradeCard.State.OPEN, Optional.empty(), 0, 3));
		cards.add(card("appraised", new NpcTradeWant.Items(item("minecraft:iron_sword"), 1, durability),
				gives("minecraft:emerald", 2), gives("minecraft:emerald", 4), NpcTradeCard.State.OPEN,
				Optional.empty(), 0, NpcTradeCard.UNLIMITED));
		cards.add(card("bundle", new NpcTradeWant.Bundle(List.of(items(item("minecraft:gold_ingot"), 1),
				items(item("minecraft:string"), 2))), gives("minecraft:lantern", 1), List.of(),
				NpcTradeCard.State.OPEN, Optional.empty(), 0, NpcTradeCard.UNLIMITED));
		if (blood) {
			cards.add(card("blood", new NpcTradeWant.Blood(250, NpcTradeWant.Blood.TITHE),
					gives("minecraft:glass_bottle", 2), List.of(), NpcTradeCard.State.OPEN, Optional.empty(), 0,
					NpcTradeCard.UNLIMITED));
		}
		if (court) {
			cards.add(card("custom", new NpcTradeWant.Custom(CourtHospitality.customAct(CourtRole.GARDENER.house())),
					List.of(), List.of(), NpcTradeCard.State.OPEN, Optional.empty(), 0, NpcTradeCard.UNLIMITED));
		}
		cards.add(card("sold_out", items(item("minecraft:feather"), 3), gives("minecraft:arrow", 8), List.of(),
				NpcTradeCard.State.SOLD_OUT, Optional.empty(), 2, 0));
		cards.add(card("locked", items(item("minecraft:amethyst_shard"), 2), gives("minecraft:spyglass", 1),
				List.of(), NpcTradeCard.State.LOCKED, Optional.of("hemomancy.trade.locked.rapport"), 0,
				NpcTradeCard.UNLIMITED));
		return new NpcTradeBoard(speaker, cards, Optional.empty(), 18, NpcTradeRules.tier(18));
	}

	private static NpcTradeCard card(String id, NpcTradeWant want, List<NpcTradeGive> give, List<NpcTradeGive> fine,
			NpcTradeCard.State state, Optional<String> lock, int days, int usesLeft) {
		return new NpcTradeCard(Hemomancy.rloc("review/" + id), TITLE, LINE, 0, want, give, fine, state, lock, days,
				usesLeft, Optional.empty());
	}

	private static NpcTradeWant.Items items(NpcTradeMatch match, int count) {
		return new NpcTradeWant.Items(match, count, NpcTradeAppraisal.NONE);
	}

	private static NpcTradeMatch item(String id) {
		return new NpcTradeMatch(List.of(ResourceLocation.parse(id)), List.of());
	}

	private static NpcTradeMatch tag(String id) {
		return new NpcTradeMatch(List.of(), List.of(ResourceLocation.parse(id)));
	}

	private static List<NpcTradeGive> gives(String id, int count) {
		return List.of(new NpcTradeGive(ResourceLocation.parse(id), count, Optional.empty()));
	}

	/** Shows {@code board} through the same path a trade reply takes. */
	private static void show(Minecraft mc, NpcTradeBoard board, ResourceLocation offer, NpcTradeService.Outcome outcome,
			String reason) {
		DialogueScreen screen = screen(mc);
		if (screen == null) return;
		DialogueScreen.receiveTradeResult(screenEntity(screen), new DialogueScreen.TradeReply(offer, outcome, reason),
				board);
	}

	private static int screenEntity(DialogueScreen screen) {
		return screen.entityId();
	}

	private static void open(Minecraft mc, DialogueCategory category) {
		DialogueScreen screen = screen(mc);
		if (screen != null) screen.openCategory(category);
	}

	private static DialogueScreen screen(Minecraft mc) {
		return mc.screen instanceof DialogueScreen screen ? screen : null;
	}

	private static Step server(Consumer<ServerPlayer> action) {
		return new Step(true, action, null);
	}

	private static Step client(Consumer<Minecraft> action) {
		return new Step(false, null, action);
	}

	private static void capture(Minecraft mc, String name) {
		String file = String.format("npc-trade-%02d-%s.png", ++shot, name);
		Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("NPC_TRADE_REVIEW captured {}", file);
	}

	private static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("NPC_TRADE_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
