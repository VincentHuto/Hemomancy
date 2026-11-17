package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hutoslib.client.book.BookReaderSession;
import com.vincenthuto.hutoslib.client.screen.guide.BookReaderScreen;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * With hemomancy.coreLoopTeachingReview, proves core-loop Track 11's teaching surface in a disposable world and stops:
 * the toast of a core-loop first ("Sculpted"), every Liber Sanguinium page the core loop wrote (first leaf, and the last
 * leaf where a page runs over), and the Hemomancy advancement tab centred on the core-loop firsts. Run with
 * coreLoopTeachingReviewClient and drop {"op":"create","name":"CoreLoopTeach","seed":"1"} into operator/inbox after
 * startup. Shots land in build/core-loop-teaching-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CoreLoopTeachingClientReview {
	private static final String PROPERTY = "hemomancy.coreLoopTeachingReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_STEP_TICK = 100;
	private static final int STEP_TICKS = 30;
	/** The Liber pages core-loop Tracks 1 to 12 (and round two P4) added, by chapter-relative path. */
	private static final List<String> PAGES = List.of(
			"manipulations/pages/the_two_verbs", "manipulations/pages/lignum_mortis", "manipulations/pages/pyretic_forge",
			"manipulations/pages/tendon_line", "manipulations/pages/blood_binding", "manipulations/pages/brazier_transmutation",
			"manipulations/pages/the_ground_network", "manipulations/pages/living_veins_and_basins",
			"manipulations/pages/ground_sculptings", "manipulations/pages/strands", "manipulations/pages/strand_forms",
			"manipulations/pages/the_draw_and_the_mark", "manipulations/pages/the_staff_grows",
			"manipulations/pages/mining_sculptings", "manipulations/pages/living_land", "manipulations/pages/venous_cling",
			"manipulations/pages/second_sculptings", "manipulations/pages/second_sculptings_walls",
			"temperament/pages/temper", "the_hematic_order/pages/living_blood", "the_hematic_order/pages/fed_ground",
			"the_hematic_order/pages/chum", "the_hematic_order/pages/menagerie", "the_hematic_order/pages/unread_humours",
			"the_hematic_order/pages/the_body_and_the_day", "the_hematic_order/pages/the_world_answers",
			"the_hematic_order/pages/the_brood", "the_hematic_order/pages/strain_beds");
	private static final List<ResourceLocation> FIRSTS = List.of(HarbingerAdvancementGranter.ADV_SCULPTED,
			HarbingerAdvancementGranter.ADV_SET_THROUGH, HarbingerAdvancementGranter.ADV_CARRIED,
			HarbingerAdvancementGranter.ADV_KEPT, HarbingerAdvancementGranter.ADV_CUT_AND_LIT,
			HarbingerAdvancementGranter.ADV_FIRST_RING, HarbingerAdvancementGranter.ADV_FIRST_CRACK,
			HarbingerAdvancementGranter.ADV_CLINGING, HarbingerAdvancementGranter.ADV_MEAT_IS_BLOOD,
			HarbingerAdvancementGranter.ADV_FIRST_BROOD, HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED);
	/** Page path to its full entry id, found on the server thread from the loaded book. */
	private static final Map<String, ResourceLocation> IDS = new ConcurrentHashMap<>();
	private static final List<Runnable> STEPS = new ArrayList<>();
	private static int ticks;
	private static int step;
	private static int captured;
	private static volatile boolean staged;
	/** Leaves the last opened page runs to; a page on one leaf needs no second shot. */
	private static int lastLeaves;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("core-loop-teaching-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		ticks++;
		if (ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_STEP_TICK || !staged) {
			mc.getToasts().clear();
			return;
		}
		int local = (ticks - FIRST_STEP_TICK) % STEP_TICKS;
		int index = (ticks - FIRST_STEP_TICK) / STEP_TICKS;
		if (local != 0) {
			if (local == STEP_TICKS - 8 && index < shots().size()) shoot(mc, shots().get(index).name());
			return;
		}
		if (index >= shots().size()) {
			Hemomancy.LOGGER.info("CORE_LOOP_TEACHING_REVIEW captured {}", captured);
			mc.stop();
			return;
		}
		try {
			shots().get(index).action().run();
		} catch (Exception failure) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW step {} failed", shots().get(index).name(), failure);
		}
	}

	private record Shot(String name, Runnable action) {
	}

	private static List<Shot> shots;

	private static List<Shot> shots() {
		if (shots != null) return shots;
		Minecraft mc = Minecraft.getInstance();
		List<Shot> list = new ArrayList<>();
		// A first earned in play: the toast names it.
		list.add(new Shot("toast-sculpted", () -> {
			mc.setScreen(null);
			MinecraftServer server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> HarbingerAdvancementGranter.grantIfNotDone(server.getPlayerList().getPlayer(uuid),
					HarbingerAdvancementGranter.ADV_SCULPTED));
		}));
		for (String page : PAGES) {
			String name = page.substring(page.lastIndexOf('/') + 1);
			list.add(new Shot("liber-" + name, () -> openPage(mc, page, false)));
			list.add(new Shot("liber-" + name + "-last", () -> openPage(mc, page, true)));
		}
		list.add(new Shot("advancements-tab", () -> openAdvancements(mc)));
		shots = list;
		return shots;
	}

	private static void shoot(Minecraft mc, String name) {
		if (name.endsWith("-last") && (!(mc.screen instanceof BookReaderScreen) || lastLeaves <= 1)) return;
		Screenshot.grab(mc.gameDirectory, "core-loop-teaching-" + String.format("%02d", step++) + "-" + name + ".png",
				mc.getMainRenderTarget(), message -> {});
		captured++;
		Hemomancy.LOGGER.info("CORE_LOOP_TEACHING_REVIEW shot {}", name);
	}

	/** Opens the Liber Sanguinium through its item, then the page at its first or last leaf. */
	private static void openPage(Minecraft mc, String page, boolean lastLeaf) {
		ResourceLocation id = IDS.get(page);
		if (id == null) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW no loaded entry for {}", page);
			return;
		}
		if (!(mc.screen instanceof BookReaderScreen)) {
			((ItemGuideBook) ItemInit.liber_sanguinum.get()).use(mc.level, mc.player, InteractionHand.MAIN_HAND);
		}
		if (!(mc.screen instanceof BookReaderScreen reader)) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW the Liber did not open for {}", page);
			return;
		}
		try {
			var field = BookReaderScreen.class.getDeclaredField("session");
			field.setAccessible(true);
			BookReaderSession session = (BookReaderSession) field.get(reader);
			var target = session.target(id);
			boolean defined = BookPlaceboReloadListener.INSTANCE.findTarget(id).isPresent();
			boolean revealed = target.isPresent() && target.get().page() != null
					&& target.get().book().canReveal(mc.player, target.get().page().getPresentation().revealLevel());
			boolean opened = session.open(id, null, lastLeaf);
			if (opened) {
				var rebuild = BookReaderScreen.class.getDeclaredMethod("rebuild");
				rebuild.setAccessible(true);
				rebuild.invoke(reader);
			} else {
				Hemomancy.LOGGER.info("CORE_LOOP_TEACHING_REVIEW {} defined={} target={} revealed={}", id, defined,
						target.isPresent(), revealed);
			}
			var leaves = BookReaderScreen.class.getDeclaredField("leaves");
			leaves.setAccessible(true);
			lastLeaves = ((List<?>) leaves.get(reader)).size();
			Hemomancy.LOGGER.info("CORE_LOOP_TEACHING_REVIEW page {} opened={} last={} leaves={}", page, opened, lastLeaf,
					((List<?>) leaves.get(reader)).size());
		} catch (ReflectiveOperationException failure) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW could not reach the reader", failure);
		}
	}

	/** The advancement screen on the Hemomancy tab, scrolled so the core-loop firsts sit in the middle. */
	private static void openAdvancements(Minecraft mc) {
		var advancements = mc.player.connection.getAdvancements();
		AdvancementsScreen screen = new AdvancementsScreen(advancements);
		mc.setScreen(screen);
		AdvancementHolder sculpted = advancements.get(HarbingerAdvancementGranter.ADV_SCULPTED);
		if (sculpted == null) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW the client has no Sculpted advancement");
			return;
		}
		var node = advancements.getTree().get(sculpted);
		if (node != null) advancements.setSelectedTab(node.root().holder(), true);
		try {
			var tabField = AdvancementsScreen.class.getDeclaredField("selectedTab");
			tabField.setAccessible(true);
			AdvancementTab tab = (AdvancementTab) tabField.get(screen);
			AdvancementWidget widget = tab == null ? null : tab.getWidget(sculpted);
			if (widget != null) {
				var scrollX = AdvancementTab.class.getDeclaredField("scrollX");
				var scrollY = AdvancementTab.class.getDeclaredField("scrollY");
				var centered = AdvancementTab.class.getDeclaredField("centered");
				scrollX.setAccessible(true);
				scrollY.setAccessible(true);
				centered.setAccessible(true);
				// The tab's window is 234 by 113: put Sculpted left of centre so its children show to the right.
				centered.setBoolean(tab, true);
				scrollX.setDouble(tab, 40 - widget.getX());
				scrollY.setDouble(tab, 30 - widget.getY());
			}
		} catch (ReflectiveOperationException failure) {
			Hemomancy.LOGGER.error("CORE_LOOP_TEACHING_REVIEW could not scroll the tab", failure);
		}
	}

	/** Survival, peaceful, every Liber page known and the book in hand, and every Hemomancy advancement but Sculpted earned. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false",
				"gamerule announceAdvancements false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setDegreeNumber(7);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents.syncDegree(player, degree);
		HemoCapabilityAccess.getBloodVolume(player).setActive(true);
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.liber_sanguinum.get()));
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		var book = BookPlaceboReloadListener.INSTANCE.getBookByTitle(Hemomancy.rloc("libersanguinium"));
		book.getChapters().forEach(chapter -> chapter.getPages().forEach(page -> {
			knowledge.unlockEntry(page.getId(), CommonDiscoverySource.OTHER);
			for (String wanted : PAGES) if (page.getId().getPath().endsWith(wanted)) IDS.put(wanted, page.getId());
		}));
		PacketDistributor.sendToPlayer(player, new PacketSyncLiberKnowledge(knowledge));
		for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
			if (!holder.id().getNamespace().equals(Hemomancy.MOD_ID) || holder.id().equals(HarbingerAdvancementGranter.ADV_SCULPTED)) continue;
			HarbingerAdvancementGranter.grantIfNotDone(player, holder.id());
		}
		Hemomancy.LOGGER.info("CORE_LOOP_TEACHING_REVIEW staged; {} of {} pages found; firsts {}", IDS.size(), PAGES.size(), FIRSTS);
		staged = true;
	}
}
