package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueBriefs;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueLayout;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CoreLoopLessons;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * With hemomancy.lessonReview (run lessonReviewClient, then drop {"op":"create","name":"LessonReview","seed":1} into
 * the operator inbox), spawns the six outpost speakers round a blood-awake reviewer and, for each, sets the Degree that
 * shows all their core-loop lessons (the Hermit 3, the rest 6), opens their hub the way a click does and captures the
 * Conversation box, each lesson node and each of its branches, choosing every branch as the screen does (so the
 * lesson's event reaches the server). Each node logs whether the speaker's text fits the box
 * without scrolling. A last pass turns on
 * dialogue brief mode, captures a Mnemonist lesson in briefs and checks every lesson key has a brief. Screenshots go to
 * build/lesson-review/screenshots; logs LESSON_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LessonClientReview {
	private static final int STAGE = 40;
	private static final int START = 100;
	private static final int STEP = 12;
	private static final List<String> SPEAKERS = List.of(CoreLoopLessons.HERMIT, CoreLoopLessons.VICAR,
			CoreLoopLessons.ALCHEMIST, CoreLoopLessons.MNEMONIST, CoreLoopLessons.ARTIFICER, CoreLoopLessons.VEIN_MASON);

	private record Step(boolean server, Consumer<ServerPlayer> serverAction, Consumer<Minecraft> clientAction) {}

	private static final Map<String, Integer> SPAWNED = new HashMap<>();
	private static List<Step> steps;
	private static int ticks;
	private static int shot;
	private static int scrolled;

	private LessonClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.lessonReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("lesson-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		mc.getToasts().clear();
		if (steps == null) steps = script();
		if (ticks == STAGE) {
			// Scale 3 is the narrowest box a 1280x720 player gets by default.
			mc.options.guiScale().set(3);
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
				report(scrolled == 0, scrolled + " lesson nodes have more text than the box shows");
				Hemomancy.LOGGER.info("LESSON_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static List<Step> script() {
		List<Step> s = new ArrayList<>();
		for (String speaker : SPEAKERS) speaker(s, speaker);
		// Brief mode: one lesson node in briefs, and every lesson key checked for a brief.
		s.add(client(mc -> {
			mc.setScreen(null);
			HemoClientConfig.DIALOGUE_BRIEF_MODE.set(true);
		}));
		s.add(server(player -> interact(player, CoreLoopLessons.MNEMONIST)));
		CoreLoopLessons.Lesson shown = CoreLoopLessons.forSpeaker(CoreLoopLessons.MNEMONIST).getFirst();
		s.add(client(mc -> openLesson(mc, shown)));
		s.add(client(mc -> capture(mc, "brief-mode-" + shown.id(), false)));
		s.add(client(mc -> {
			select(mc, 0);
		}));
		s.add(client(mc -> capture(mc, "brief-mode-" + shown.id() + "-" + shown.branches().getFirst().id(), false)));
		s.add(client(mc -> {
			String missing = Component.translatable("hemomancy.dialogue.brief.missing").getString();
			List<String> without = new ArrayList<>();
			for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
				List<String> options = new ArrayList<>(List.of(lesson.rootOption(), lesson.backOption()));
				for (CoreLoopLessons.Branch branch : lesson.branches()) options.add(lesson.branchOption(branch));
				for (String key : options) if (DialogueBriefs.option(key).getString().contains(missing)) without.add(key);
				List<String> lines = new ArrayList<>(lesson.lineKeys());
				for (CoreLoopLessons.Branch branch : lesson.branches()) lines.addAll(lesson.lineKeys(branch));
				for (String key : lines) if (DialogueBriefs.line(key).getString().contains(missing)) without.add(key);
			}
			report(without.isEmpty(), "every lesson key has a brief " + without);
			HemoClientConfig.DIALOGUE_BRIEF_MODE.set(false);
			mc.setScreen(null);
		}));
		return s;
	}

	/** One speaker: their hub's Conversation box, then every lesson and each of its branches. */
	private static void speaker(List<Step> s, String speaker) {
		int degree = speaker.equals(CoreLoopLessons.HERMIT) ? 3 : 6;
		s.add(client(mc -> mc.setScreen(null)));
		s.add(server(player -> {
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(degree);
			interact(player, speaker);
		}));
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			report(screen != null, speaker + "'s hub opened");
			if (screen == null) return;
			screen.openCategory(DialogueCategory.CONVERSATION);
			for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
				DialogueTopic topic = topic(tree(screen), lesson);
				report(topic != null && topic.lore() && topic.unread(), lesson.event() + " is an unread lore card");
			}
		}));
		s.add(client(mc -> capture(mc, speaker + "-conversation", false)));
		// Scroll the box to the first lesson card (Conversation cards are 44 px tall with a 6 px gap), as a player would.
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			if (screen == null) return;
			List<DialogueTopic> topics = tree(screen).presentation().topics(DialogueCategory.CONVERSATION);
			String first = CoreLoopLessons.forSpeaker(speaker).getFirst().rootOption();
			int at = 0;
			while (at < topics.size() && !topics.get(at).titleKey().equals(first)) at++;
			setField(screen, "scrollOffset", at * 50);
		}));
		s.add(client(mc -> capture(mc, speaker + "-conversation-lessons", false)));
		// Lessons sit after the speaker's own cards, so scroll the box to its end to show them.
		s.add(client(mc -> {
			DialogueScreen screen = screen(mc);
			if (screen != null) setField(screen, "scrollOffset", Integer.MAX_VALUE / 2);
		}));
		s.add(client(mc -> capture(mc, speaker + "-conversation-end", false)));
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
			s.add(client(mc -> openLesson(mc, lesson)));
			s.add(client(mc -> capture(mc, speaker + "-" + lesson.id(), true)));
			for (int i = 0; i < lesson.branches().size(); i++) {
				int option = i;
				String branch = lesson.branches().get(i).id();
				s.add(client(mc -> select(mc, option)));
				s.add(client(mc -> capture(mc, speaker + "-" + lesson.id() + "-" + branch, true)));
				// The branch's first option goes back to the lesson, as a player's click would.
				s.add(client(mc -> select(mc, 0)));
			}
		}
	}

	private static void stage(ServerPlayer player) {
		MinecraftServer server = player.server;
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] {"gamemode survival", "difficulty peaceful", "gamerule doMobSpawning false",
				"time set day", "gamerule doDaylightCycle false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.getInventory().clearContent();
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(true);
		ServerLevel level = player.serverLevel();
		for (int i = 0; i < SPEAKERS.size(); i++) spawn(level, player, SPEAKERS.get(i), i);
		Hemomancy.LOGGER.info("LESSON_REVIEW staged");
	}

	private static void spawn(ServerLevel level, ServerPlayer player, String speaker, int index) {
		EntityType<?> type = switch (speaker) {
			case CoreLoopLessons.HERMIT -> EntityInit.harbinger_hermit.get();
			case CoreLoopLessons.VICAR -> EntityInit.harbinger_vicar.get();
			case CoreLoopLessons.ALCHEMIST -> EntityInit.harbinger_alchemist.get();
			case CoreLoopLessons.MNEMONIST -> EntityInit.harbinger_mnemonist.get();
			case CoreLoopLessons.ARTIFICER -> EntityInit.harbinger_artificer.get();
			default -> EntityInit.harbinger_cicatrix_anchorite.get();
		};
		Entity entity = type.create(level);
		if (entity == null) {
			report(false, speaker + " spawned");
			return;
		}
		// A ring three blocks out, so every speaker stays inside the eight-block reach the lesson events need.
		double angle = Math.PI * 2 * index / SPEAKERS.size();
		entity.moveTo(player.getX() + 3 * Math.cos(angle), player.getY(), player.getZ() + 3 * Math.sin(angle), 0F, 0F);
		if (entity instanceof Mob mob) {
			mob.setNoAi(true);
			mob.setPersistenceRequired();
		}
		level.addFreshEntity(entity);
		SPAWNED.put(speaker, entity.getId());
	}

	private static void interact(ServerPlayer player, String speaker) {
		Integer id = SPAWNED.get(speaker);
		Entity entity = id == null ? null : player.serverLevel().getEntity(id);
		if (entity == null) {
			report(false, speaker + " is there to talk to");
			return;
		}
		entity.interact(player, InteractionHand.MAIN_HAND);
	}

	/** Opens a lesson's card the way a click on it does. */
	private static void openLesson(Minecraft mc, CoreLoopLessons.Lesson lesson) {
		DialogueScreen screen = screen(mc);
		if (screen == null) {
			report(false, lesson.event() + ": no dialogue open");
			return;
		}
		DialogueTopic topic = topic(tree(screen), lesson);
		if (topic == null) {
			report(false, lesson.event() + " has no card");
			return;
		}
		invoke(screen, "openTopic", DialogueTopic.class, topic);
		// A click resets the scroll after opening (DialogueScreen.activate); calling openTopic directly does not.
		invoke(screen, "resetScroll");
	}

	/** Chooses option {@code index} of the node on screen, as a click does (its event goes to the server). */
	private static void select(Minecraft mc, int index) {
		DialogueScreen screen = screen(mc);
		if (screen == null) return;
		invoke(screen, "selectOption", int.class, index);
		invoke(screen, "resetScroll");
	}

	private static DialogueTopic topic(DialogueTree tree, CoreLoopLessons.Lesson lesson) {
		if (tree == null) return null;
		return tree.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.filter(candidate -> candidate.titleKey().equals(lesson.rootOption())).findFirst().orElse(null);
	}

	private static DialogueTree tree(DialogueScreen screen) {
		return (DialogueTree) field(screen, "tree");
	}

	private static Object field(DialogueScreen screen, String name) {
		try {
			Field field = DialogueScreen.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(screen);
		} catch (ReflectiveOperationException e) {
			report(false, "read " + name + ": " + e);
			return null;
		}
	}

	private static void setField(DialogueScreen screen, String name, Object value) {
		try {
			Field field = DialogueScreen.class.getDeclaredField(name);
			field.setAccessible(true);
			field.set(screen, value);
		} catch (ReflectiveOperationException e) {
			report(false, "write " + name + ": " + e);
		}
	}

	private static Object staticField(String name) {
		try {
			Field field = DialogueScreen.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("read " + name, e);
		}
	}

	private static void invoke(DialogueScreen screen, String name, Class<?> type, Object argument) {
		try {
			Method method = DialogueScreen.class.getDeclaredMethod(name, type);
			method.setAccessible(true);
			method.invoke(screen, argument);
		} catch (ReflectiveOperationException e) {
			report(false, "call " + name + ": " + e);
		}
	}

	private static void invoke(DialogueScreen screen, String name) {
		try {
			Method method = DialogueScreen.class.getDeclaredMethod(name);
			method.setAccessible(true);
			method.invoke(screen);
		} catch (ReflectiveOperationException e) {
			report(false, "call " + name + ": " + e);
		}
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

	/**
	 * Captures the screen. A lesson node also reports whether the speaker's text fits the visible content box when it
	 * opens, measured the way the screen lays it out (the questions under it may scroll, as every node's do).
	 */
	private static void capture(Minecraft mc, String name, boolean node) {
		DialogueScreen screen = screen(mc);
		if (node && screen != null) {
			Object current = field(screen, "currentNode");
			Object layout = field(screen, "layout");
			if (current instanceof DialogueNode open && layout instanceof DialogueLayout box) {
				int pad = (Integer) staticField("CONTENT_PAD");
				int lineHeight = (Integer) staticField("LINE_HEIGHT");
				int width = box.content().width() - pad * 2;
				int height = pad;
				for (String key : open.lines()) {
					height += mc.font.split(Component.translatable(key), width).size() * lineHeight + 3;
				}
				boolean fits = height <= box.content().height();
				if (!fits) scrolled++;
				report(fits, name + " (" + open.id() + ") text " + height + " of " + box.content().height() + " px");
			} else {
				report(false, name + ": no node to measure");
			}
		}
		String file = String.format("lesson-%03d-%s.png", ++shot, name);
		Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("LESSON_REVIEW captured {}", file);
	}

	private static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("LESSON_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
