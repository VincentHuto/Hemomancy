package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hutoslib.client.book.BookReaderSession;
import com.vincenthuto.hutoslib.client.screen.guide.BookReaderScreen;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * With hemomancy.liberPagesReview, proves round two P9 (CQ-1) in a disposable world and stops. It stages a Harbinger by
 * the real triggers only (the Degree path to the eighth Degree, item pickups through {@code unlockForItem}, a memory
 * learned, every Hemomancy advancement but Stoked, then the purity and clarity stages for Liber Immaculatus), logs
 * every page of both books that is still hidden with the sources that would open it, and shoots: Stoked's toast, the
 * contents of the Manipulations and Temperament chapters, the first and last leaf of every older page in both books,
 * and the Hemomancy advancement tab centred three ways so all 27 core-loop firsts are seen. Run with
 * liberPagesReviewClient and drop {"op":"create","name":"LiberPages","seed":"1"} into operator/inbox after startup.
 * Shots land in build/liber-pages-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LiberPagesClientReview {
	private static final String PROPERTY = "hemomancy.liberPagesReview";
	private static final String TAG = "LIBER_PAGES_REVIEW";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_STEP_TICK = 100;
	private static final int STEP_TICKS = 30;
	private static final String SANGUINIUM = "libersanguinium";
	private static final String IMMACULATUS = "liberimmaculatus";
	/** Entry or chapter id by book-relative path, found on the server thread from the loaded books. */
	private static final Map<String, ResourceLocation> IDS = new ConcurrentHashMap<>();
	private static int ticks;
	private static int step;
	private static int captured;
	private static volatile boolean staged;
	private static int lastLeaves;
	private static List<Shot> shots;

	private record Shot(String name, Runnable action) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("liber-pages-review")) return;
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
			Hemomancy.LOGGER.info("{} captured {}", TAG, captured);
			mc.stop();
			return;
		}
		try {
			shots().get(index).action().run();
		} catch (Exception failure) {
			Hemomancy.LOGGER.error("{} step {} failed", TAG, shots().get(index).name(), failure);
		}
	}

	private static List<Shot> shots() {
		if (shots != null) return shots;
		Minecraft mc = Minecraft.getInstance();
		List<Shot> list = new ArrayList<>();
		list.add(new Shot("toast-stoked", () -> {
			mc.setScreen(null);
			MinecraftServer server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> HarbingerAdvancementGranter.grantIfNotDone(server.getPlayerList().getPlayer(uuid),
					HarbingerAdvancementGranter.ADV_STOKED));
		}));
		list.add(new Shot("contents-manipulations", () -> openChapter(mc, "manipulations")));
		list.add(new Shot("contents-temperament", () -> openChapter(mc, "temperament")));
		// DS-9: the Armature and Court chapters, then every page the DS-9 pass added.
		list.add(new Shot("contents-armature", () -> openChapter(mc, "armature")));
		list.add(new Shot("contents-brined_court", () -> openChapter(mc, "brined_court")));
		for (ResourceLocation page : ds9Pages()) addPage(list, mc, SANGUINIUM, page);
		for (ResourceLocation page : LiberEntryDefinitions.OLDER_PAGES) addPage(list, mc, SANGUINIUM, page);
		for (ResourceLocation page : LiberEntryDefinitions.OLDER_IMMACULATUS_PAGES) addPage(list, mc, IMMACULATUS, page);
		// The tab's window is 234 by 113: one shot per branch the firsts hang from, each centred on its root first.
		for (ResourceLocation centre : List.of(HarbingerAdvancementGranter.ADV_SCULPTED, HarbingerAdvancementGranter.ADV_FIRST_CRACK,
				HarbingerAdvancementGranter.ADV_CUT_AND_LIT, HarbingerAdvancementGranter.ADV_LAND_STRIKE,
				HarbingerAdvancementGranter.ADV_FIELD_OF_DARK, HarbingerAdvancementGranter.ADV_KEPT,
				HarbingerAdvancementGranter.ADV_SET_THROUGH, HarbingerAdvancementGranter.ADV_MEAT_IS_BLOOD,
				HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON)) {
			list.add(new Shot("advancements-" + centre.getPath().substring(centre.getPath().indexOf('/') + 1),
					() -> openAdvancements(mc, centre)));
		}
		shots = list;
		return shots;
	}

	private static List<ResourceLocation> ds9Pages() {
		List<ResourceLocation> pages = new ArrayList<>(List.of(LiberEntryDefinitions.ARMATURE_RACK));
		pages.addAll(LiberEntryDefinitions.forRite("cardinal_rite/armature_consecration"));
		pages.addAll(LiberEntryDefinitions.forRite("cardinal_rite/monolithic_armature"));
		LiberEntryDefinitions.CRAFT_PAGES.forEach(craft -> pages.add(craft.page()));
		pages.addAll(DS9_ENCOUNTERS);
		pages.add(LiberEntryDefinitions.COURT_HOUSES);
		return pages;
	}

	private static final List<ResourceLocation> DS9_ENCOUNTERS = List.of(LiberEntryDefinitions.VESPER,
			LiberEntryDefinitions.MYCOPHANT, LiberEntryDefinitions.NAEGLEROPHAEON, LiberEntryDefinitions.PHLEGETHOSOMA,
			LiberEntryDefinitions.BLOOM_WARDEN, LiberEntryDefinitions.KINDLED_CHAMPION);

	private static void addPage(List<Shot> list, Minecraft mc, String book, ResourceLocation page) {
		String name = page.getPath().substring(page.getPath().indexOf('/') + 1).replace("/pages/", "-");
		list.add(new Shot(book.substring(5, 9) + "-" + name, () -> openPage(mc, book, page, false)));
		list.add(new Shot(book.substring(5, 9) + "-" + name + "-last", () -> openPage(mc, book, page, true)));
	}

	private static void shoot(Minecraft mc, String name) {
		if (name.endsWith("-last") && (!(mc.screen instanceof BookReaderScreen) || lastLeaves <= 1)) return;
		Screenshot.grab(mc.gameDirectory, "liber-pages-" + String.format("%03d", step++) + "-" + name + ".png",
				mc.getMainRenderTarget(), message -> {});
		captured++;
		Hemomancy.LOGGER.info("{} shot {}", TAG, name);
	}

	private static BookReaderScreen reader(Minecraft mc, String book) {
		if (mc.screen instanceof BookReaderScreen reader && book.equals(IDS.get("open").getPath())) return reader;
		mc.setScreen(null);
		boolean immaculatus = IMMACULATUS.equals(book);
		var item = immaculatus ? ItemInit.liber_immaculatus.get() : ItemInit.liber_sanguinum.get();
		((ItemGuideBook) item).use(mc.level, mc.player, immaculatus ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		IDS.put("open", ResourceLocation.withDefaultNamespace(book));
		return mc.screen instanceof BookReaderScreen reader ? reader : null;
	}

	private static BookReaderSession session(BookReaderScreen reader) throws ReflectiveOperationException {
		var field = BookReaderScreen.class.getDeclaredField("session");
		field.setAccessible(true);
		return (BookReaderSession) field.get(reader);
	}

	private static void rebuild(BookReaderScreen reader) throws ReflectiveOperationException {
		var rebuild = BookReaderScreen.class.getDeclaredMethod("rebuild");
		rebuild.setAccessible(true);
		rebuild.invoke(reader);
		var leaves = BookReaderScreen.class.getDeclaredField("leaves");
		leaves.setAccessible(true);
		lastLeaves = ((List<?>) leaves.get(reader)).size();
	}

	/** The chapter's own list of entries: what a reader sees on choosing the chapter from the contents. */
	private static void openChapter(Minecraft mc, String chapter) {
		ResourceLocation id = IDS.get("chapter:" + chapter);
		BookReaderScreen reader = reader(mc, SANGUINIUM);
		if (id == null || reader == null) {
			Hemomancy.LOGGER.error("{} no chapter {} or no reader", TAG, chapter);
			return;
		}
		try {
			session(reader).show(BookReaderSession.View.CHAPTER, id);
			rebuild(reader);
			Hemomancy.LOGGER.info("{} chapter {} shown", TAG, chapter);
		} catch (ReflectiveOperationException failure) {
			Hemomancy.LOGGER.error("{} could not reach the reader", TAG, failure);
		}
	}

	private static void openPage(Minecraft mc, String book, ResourceLocation page, boolean lastLeaf) {
		BookReaderScreen reader = reader(mc, book);
		if (reader == null) {
			Hemomancy.LOGGER.error("{} the {} did not open for {}", TAG, book, page);
			return;
		}
		try {
			BookReaderSession session = session(reader);
			boolean opened = session.open(page, null, lastLeaf);
			if (opened) rebuild(reader);
			Hemomancy.LOGGER.info("{} page {} opened={} last={} leaves={}", TAG, page, opened, lastLeaf, lastLeaves);
		} catch (ReflectiveOperationException failure) {
			Hemomancy.LOGGER.error("{} could not reach the reader", TAG, failure);
		}
	}

	/** The advancement screen on the Hemomancy tab, scrolled so {@code centre} sits left of middle with its children. */
	private static void openAdvancements(Minecraft mc, ResourceLocation centre) {
		var advancements = mc.player.connection.getAdvancements();
		AdvancementsScreen screen = new AdvancementsScreen(advancements);
		mc.setScreen(screen);
		AdvancementHolder holder = advancements.get(centre);
		if (holder == null) {
			Hemomancy.LOGGER.error("{} the client has no {}", TAG, centre);
			return;
		}
		var node = advancements.getTree().get(holder);
		if (node != null) advancements.setSelectedTab(node.root().holder(), true);
		try {
			var tabField = AdvancementsScreen.class.getDeclaredField("selectedTab");
			tabField.setAccessible(true);
			AdvancementTab tab = (AdvancementTab) tabField.get(screen);
			AdvancementWidget widget = tab == null ? null : tab.getWidget(holder);
			if (widget != null) {
				var scrollX = AdvancementTab.class.getDeclaredField("scrollX");
				var scrollY = AdvancementTab.class.getDeclaredField("scrollY");
				var centered = AdvancementTab.class.getDeclaredField("centered");
				scrollX.setAccessible(true);
				scrollY.setAccessible(true);
				centered.setAccessible(true);
				centered.setBoolean(tab, true);
				scrollX.setDouble(tab, 30 - widget.getX());
				scrollY.setDouble(tab, 44 - widget.getY());
			}
		} catch (ReflectiveOperationException failure) {
			Hemomancy.LOGGER.error("{} could not scroll the tab", TAG, failure);
		}
	}

	/**
	 * Survival, peaceful, both books in hand, and every page opened only by what opens it in play: the Degree path to
	 * the eighth Degree, the items the openers name, a memory learned, every Hemomancy advancement but Stoked, then the
	 * Unstained stages. Whatever is still hidden after that is logged with the sources that would open it.
	 */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false",
				"gamerule announceAdvancements false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		HemoCapabilityAccess.getBloodVolume(player).setActive(true);
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.liber_sanguinum.get()));
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.liber_immaculatus.get()));
		for (int degree = 1; degree <= 8; degree++) DegreeProgression.advance(player, degree);
		for (String item : List.of("fervent_enzyme", "hematic_memory", "resonant_forge", "morphling_jar", "chitinous_husk",
				"scout_field_notes", "ghost_pipe", "pallid_icon")) {
			LiberDiscoveryEvents.unlockForItem(player, new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc(item))));
		}
		LiberKnowledgeHelper.unlockForMemory(player, ManipulationInit.hemorrhage.get());
		for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
			if (!holder.id().getNamespace().equals(Hemomancy.MOD_ID) || holder.id().equals(HarbingerAdvancementGranter.ADV_STOKED)) continue;
			HarbingerAdvancementGranter.grantIfNotDone(player, holder.id());
		}
		var progress = HemoCapabilityAccess.getUnstainedProgress(player);
		progress.setBegunPurification(true);
		progress.setPurity(100.0F);
		progress.setClarityUnlocked(true);
		progress.setClarity(80.0F);
		LiberKnowledgeHelper.unlockForUnstained(player, progress);
		// DS-9: the Court by a Voyager's route, and each boss page by the call its fight's start makes.
		com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress.markKnown(player,
				com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress.Route.VOYAGER);
		for (ResourceLocation page : DS9_ENCOUNTERS) LiberKnowledgeHelper.unlockForEncounter(player, page);
		// The two Armature lessons need armour practice; their rites' completion opens them too.
		LiberKnowledgeHelper.unlockForRite(player, "cardinal_rite/armature_consecration");
		LiberKnowledgeHelper.unlockForRite(player, "cardinal_rite/monolithic_armature");
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		List<String> hidden = new ArrayList<>();
		for (String title : List.of(SANGUINIUM, IMMACULATUS)) {
			var book = BookPlaceboReloadListener.INSTANCE.getBookByTitle(Hemomancy.rloc(title));
			book.getChapters().forEach(chapter -> {
				if (chapter.getId() != null) IDS.put("chapter:" + chapter.getId().getPath().replaceAll("^" + title + "/", "")
						.replaceAll("/.*$", ""), chapter.getId());
				chapter.getPages().forEach(page -> {
					ResourceLocation id = page.getId();
					if (id == null || knowledge.hasEntry(id)) return;
					hidden.add(id.getPath() + " <- " + LiberEntryDefinitions.get(id).map(d -> d.sources().toString()).orElse("UNREGISTERED"));
				});
			});
		}
		Hemomancy.LOGGER.info("{} staged; still hidden after every real trigger: {} {}", TAG, hidden.size(), hidden);
		IDS.put("open", ResourceLocation.withDefaultNamespace("none"));
		staged = true;
	}
}
