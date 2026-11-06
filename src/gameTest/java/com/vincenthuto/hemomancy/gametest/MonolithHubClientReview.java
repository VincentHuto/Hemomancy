package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueHubLayout;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueLayout;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * With hemomancy.monolithHubReview (run monolithHubReviewClient, then drop
 * {"op":"create","name":"MonolithHubReview","seed":1} into the operator inbox), stands a Sanguine Monolith in front of the
 * reviewer and right-clicks it at Degree 5, 6 and 7. For each it captures the first open (the screen goes to the first
 * unread card by itself), then, with the lore read, the hub and each of its boxes. At Degree 5 it also runs a real trade
 * through the payload and opens the stone with an item in hand. Screenshots go to build/monolith-hub-review/screenshots;
 * logs MONOLITH_HUB_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MonolithHubClientReview {
	private static final int STAGE = 40;
	private static final int START = 100;
	private static final int STEP = 20;
	private static final List<String> LORE = List.of("what_are_you_illuminatus", "what_are_you_sanctified", "what_are_you_archon", "what_it_measures", "who_stood_here", "why_now",
			"where_found", "does_it_know_me", "the_others", "ripening", "what_opens", "what_remains");

	private record Step(boolean server, Consumer<ServerPlayer> serverAction, Consumer<Minecraft> clientAction) {}

	private static List<Step> steps;
	private static BlockPos stone;
	private static int ticks;
	private static int shot;

	private MonolithHubClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.monolithHubReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("monolith-hub-review")) return;
		if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		mc.getToasts().clear();
		if (steps == null) steps = script();
		if (ticks == STAGE) {
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

	private static List<Step> script() {
		List<Step> s = new ArrayList<>();
		degree(s, 5, "d5");
		// A real trade through the payload, as the Trade box sends it, then the card's new state.
		s.add(client(mc -> PacketHandler.sendToServer(new NpcTradeRequestPacket(SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID,
				Hemomancy.rloc("monolith/measure"), new int[0]))));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			report(screen != null && screen.tradeBoard().rapport() == 1, "the reply reached the open stone's Trade box");
			report(mc.player.getInventory().countItem(ItemInit.sanguine_salve.get()) == 2, "two salves came away");
			capture(mc, "d5-trade-done");
		}));
		// An item in hand opens the same hub.
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> {
			player.getInventory().selected = 0;
			useStone(player, player.getMainHandItem());
		}));
		s.add(client(mc -> {
			report(screen(mc) != null, "an item in hand opens the hub");
			capture(mc, "d5-item-in-hand");
		}));
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> player.getInventory().selected = 8));
		degree(s, 6, "d6");
		degree(s, 7, "d7");
		// The glowing Important tab, twice half a breath apart; a click on it; Press further inside; then its warning.
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> useStone(player, ItemStack.EMPTY)));
		s.add(client(mc -> capture(mc, "d7-button-a")));
		s.add(client(mc -> capture(mc, "d7-button-b")));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			if (screen == null) return;
			var tab = DialogueHubLayout.calculate(DialogueLayout.calculate(screen.width, screen.height)).important();
			report(screen.mouseClicked(tab.x() + tab.width() / 2.0D, tab.y() + tab.height() / 2.0D, 0),
					"a click on the Important tab lands");
		}));
		s.add(client(mc -> capture(mc, "d7-important")));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			// "Read full greeting" is the first target, Press further the second.
			if (screen != null) screen.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_2, 0, 0);
		}));
		s.add(client(mc -> capture(mc, "d7-press-warning")));
		return s;
	}

	/** One Degree: the first open with the lore unread, then the hub and its four boxes with the lore read. */
	private static void degree(List<Step> s, int degree, String name) {
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> {
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(degree);
			useStone(player, ItemStack.EMPTY);
		}));
		s.add(client(mc -> {
			report(screen(mc) != null, name + ": the stone opens");
			capture(mc, name + "-first-open");
		}));
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> {
			for (String slug : LORE) HemoCapabilityAccess.dialogueKnowledge(player).markRead(Hemomancy.rloc("monolith/lore/" + slug));
			useStone(player, ItemStack.EMPTY);
		}));
		s.add(client(mc -> capture(mc, name + "-hub")));
		for (DialogueCategory category : List.of(DialogueCategory.QUESTS, DialogueCategory.INQUIRIES,
				DialogueCategory.CONVERSATION, DialogueCategory.TRADE)) {
			s.add(client(mc -> {
				DialogueScreen screen = screen(mc);
				if (screen != null) screen.openCategory(category);
			}));
			s.add(client(mc -> capture(mc, name + "-" + category.name().toLowerCase(java.util.Locale.ROOT))));
		}
	}

	private static void stage(ServerPlayer player) {
		MinecraftServer server = player.server;
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] {"gamemode survival", "difficulty peaceful", "gamerule doMobSpawning false",
				"time set day", "gamerule doDaylightCycle false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.getInventory().clearContent();
		player.getInventory().setItem(0, new ItemStack(ItemInit.monolith_fragment.get(), 2));
		player.getInventory().setItem(1, new ItemStack(Items.NETHER_STAR));
		player.getInventory().setItem(2, new ItemStack(ItemInit.qliphoth_seed.get()));
		// An empty hand to talk with.
		player.getInventory().selected = 8;
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(true);
		ServerLevel level = player.serverLevel();
		Direction facing = player.getDirection();
		stone = player.blockPosition().relative(facing, 3);
		for (int y = 0; y < 3; y++) level.setBlockAndUpdate(stone.above(y), Blocks.AIR.defaultBlockState());
		level.setBlockAndUpdate(stone, BlockInit.sanguine_monolith.get().defaultBlockState());
		Hemomancy.LOGGER.info("MONOLITH_HUB_REVIEW staged");
	}

	private static void useStone(ServerPlayer player, ItemStack held) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(stone), Direction.UP, stone, false);
		var state = player.level().getBlockState(stone);
		if (held.isEmpty()) state.useWithoutItem(player.level(), player, hit);
		else state.useItemOn(held, player.level(), player, InteractionHand.MAIN_HAND, hit);
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
		String file = String.format("monolith-hub-%02d-%s.png", ++shot, name);
		Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("MONOLITH_HUB_REVIEW captured {}", file);
	}

	private static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("MONOLITH_HUB_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
