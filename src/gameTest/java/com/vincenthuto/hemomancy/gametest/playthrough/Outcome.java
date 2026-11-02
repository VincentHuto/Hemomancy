package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardResultPacket;
import com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * What one player action changed, as the player would notice it: lines said to them (with their translation keys),
 * advancements earned and lost, the inventory delta, item drops near them and near the NPC, degree and blood, the
 * payloads sent to them, and the dialogue event a choice dispatched. Taken as a before/after diff around the action.
 * The assertion helpers return the outcome so they chain.
 */
public final class Outcome {
	/** How far a drop may be from the player or the NPC to count as theirs. */
	public static final double DROP_RADIUS = 8.0D;

	/** A chat or action-bar line: its text, every translation key in it, and whether it was on the action bar. */
	public record ChatLine(String text, List<String> keys, boolean overlay) {
		public static ChatLine of(Component component, boolean overlay) {
			List<String> keys = new ArrayList<>();
			collectKeys(component, keys);
			return new ChatLine(component.getString(), List.copyOf(keys), overlay);
		}

		private static void collectKeys(Component component, List<String> keys) {
			if (component.getContents() instanceof TranslatableContents translatable) {
				keys.add(translatable.getKey());
				for (Object arg : translatable.getArgs()) if (arg instanceof Component inner) collectKeys(inner, keys);
			}
			for (Component sibling : component.getSiblings()) collectKeys(sibling, keys);
		}

		/** Whether the line is, or contains, {@code key}. */
		public boolean has(String key) {
			return keys.contains(key);
		}

		@Override
		public String toString() {
			return (overlay ? "[bar] " : "") + text + (keys.isEmpty() ? "" : " " + keys);
		}
	}

	/** Who a drop landed beside. */
	public enum Near {PLAYER, NPC, ANY}

	/** An item entity that appeared during the action. */
	public record Drop(ItemStack stack, Vec3 pos, double toPlayer, double toNpc) {
		public boolean near(Near near) {
			return switch (near) {
				case ANY -> true;
				case PLAYER -> toPlayer <= toNpc;
				case NPC -> toNpc < toPlayer;
			};
		}

		@Override
		public String toString() {
			String npc = toNpc == Double.MAX_VALUE ? "-" : String.format("%.1f", toNpc);
			return stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem()) + String.format(
					" at %.1f/%s from player/npc", toPlayer, npc);
		}
	}

	private final String action;
	private final GameTestHelper helper;
	private final List<ChatLine> chat;
	private final Set<String> earned;
	private final Set<String> lost;
	private final Map<String, Integer> inventory;
	private final Map<Item, Integer> byItem;
	private final List<Drop> drops;
	private final int degreeBefore;
	private final int degreeAfter;
	private final double bloodBefore;
	private final double bloodAfter;
	private final boolean activeBefore;
	private final boolean activeAfter;
	private final List<CustomPacketPayload> payloads;
	private final List<DialogueEvent> events;
	private final String savedDiff;
	private final List<String> notes;
	private final List<DialogueTree> whispersQueued;

	private Outcome(Probe probe, ServerPlayer player) {
		this.action = probe.action;
		this.helper = probe.helper;
		this.chat = List.copyOf(probe.owner.chatLog.subList(probe.chatMark, probe.owner.chatLog.size()));
		Set<String> after = PlaythroughFingerprints.earned(player);
		Set<String> gained = new TreeSet<>(after);
		gained.removeAll(probe.earned);
		Set<String> gone = new TreeSet<>(probe.earned);
		gone.removeAll(after);
		this.earned = gained;
		this.lost = gone;
		this.inventory = delta(probe.inventory, inventory(player));
		this.byItem = itemDelta(probe.byItem, byItem(player));
		this.drops = newDrops(probe, player);
		this.degreeBefore = probe.degree;
		this.degreeAfter = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		this.bloodBefore = probe.blood;
		this.bloodAfter = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		this.activeBefore = probe.active;
		this.activeAfter = HemoCapabilityAccess.getBloodVolume(player).isActive();
		this.payloads = List.copyOf(probe.owner.payloadLog.subList(probe.payloadMark, probe.owner.payloadLog.size()));
		this.events = DialogueEventProbe.since(player.getUUID(), probe.eventMark);
		this.savedDiff = PlaythroughFingerprints.diff(probe.saved, PlaythroughFingerprints.saved(player));
		this.notes = List.copyOf(probe.notes);
		this.whispersQueued = queuedSince(player, probe.whisperMark);
	}

	/** A copy of {@code base} whose chat is {@code chat}. */
	private Outcome(Outcome base, List<ChatLine> chat) {
		this.action = base.action;
		this.helper = base.helper;
		this.chat = List.copyOf(chat);
		this.earned = base.earned;
		this.lost = base.lost;
		this.inventory = base.inventory;
		this.byItem = base.byItem;
		this.drops = base.drops;
		this.degreeBefore = base.degreeBefore;
		this.degreeAfter = base.degreeAfter;
		this.bloodBefore = base.bloodBefore;
		this.bloodAfter = base.bloodAfter;
		this.activeBefore = base.activeBefore;
		this.activeAfter = base.activeAfter;
		this.payloads = base.payloads;
		this.events = base.events;
		this.savedDiff = base.savedDiff;
		this.notes = base.notes;
		this.whispersQueued = base.whispersQueued;
	}

	private static int inboxSize(ServerPlayer player) {
		// Reading the attachment would create it, and an action that changed nothing would then show a save change.
		if (!com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.hasPendingWhispers(player)) return 0;
		return com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.entries(player).size();
	}

	private static List<DialogueTree> queuedSince(ServerPlayer player, int mark) {
		if (!com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.hasPendingWhispers(player)) return List.of();
		var entries = com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers.entries(player);
		if (entries.size() <= mark) return List.of();
		List<DialogueTree> trees = new ArrayList<>();
		for (int i = Math.max(0, mark); i < entries.size(); i++) {
			byte[] bytes = entries.getCompound(i).getByteArray("tree");
			FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
			try {
				trees.add(DialogueTree.fromNetwork(buffer));
			} catch (RuntimeException exception) {
				// An entry this harness cannot read is left out; the test sees one fewer queued whisper.
			} finally {
				buffer.release();
			}
		}
		return List.copyOf(trees);
	}

	/** Starts watching an action. {@code npc} may be null; drops are then measured against the player only. */
	public static Probe begin(PlaythroughPlayer player, @Nullable Entity npc, String action) {
		return new Probe(player, npc, action);
	}

	/** The state taken before an action; {@link #finish()} diffs it. */
	public static final class Probe {
		private final PlaythroughPlayer owner;
		private final GameTestHelper helper;
		private final Entity npc;
		private final String action;
		private final int chatMark;
		private final int payloadMark;
		private final int eventMark;
		private final Set<String> earned;
		private final Map<String, Integer> inventory;
		private final Map<Item, Integer> byItem;
		private final Set<UUID> dropsBefore = new HashSet<>();
		private final int degree;
		private final double blood;
		private final boolean active;
		private final CompoundTag saved;
		private final List<String> notes = new ArrayList<>();
		private final int whisperMark;

		private Probe(PlaythroughPlayer owner, @Nullable Entity npc, String action) {
			this.owner = owner;
			this.helper = owner.helper();
			this.npc = npc;
			this.action = action;
			ServerPlayer player = owner.player();
			this.chatMark = owner.chatLog.size();
			this.payloadMark = owner.payloadLog.size();
			this.eventMark = DialogueEventProbe.mark(player.getUUID());
			this.earned = PlaythroughFingerprints.earned(player);
			this.inventory = Outcome.inventory(player);
			this.byItem = Outcome.byItem(player);
			for (ItemEntity drop : dropsAround(player)) dropsBefore.add(drop.getUUID());
			this.degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			this.blood = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			this.active = HemoCapabilityAccess.getBloodVolume(player).isActive();
			this.saved = PlaythroughFingerprints.saved(player);
			this.whisperMark = inboxSize(player);
		}

		/** A note the outcome carries, such as a fallback the harness took. */
		public Probe note(String text) {
			notes.add(text);
			return this;
		}

		private List<ItemEntity> dropsAround(ServerPlayer player) {
			List<ItemEntity> found = new ArrayList<>(player.serverLevel().getEntitiesOfClass(ItemEntity.class,
					new AABB(player.blockPosition()).inflate(DROP_RADIUS)));
			if (npc != null && npc.level() == player.level()) {
				for (ItemEntity drop : player.serverLevel().getEntitiesOfClass(ItemEntity.class,
						new AABB(npc.blockPosition()).inflate(DROP_RADIUS))) {
					if (!found.contains(drop)) found.add(drop);
				}
			}
			return found;
		}

		public Outcome finish() {
			Outcome outcome = new Outcome(this, owner.player());
			owner.recordOutcome(outcome);
			return outcome;
		}
	}

	// --- Reading ---------------------------------------------------------------------------------------------------

	public String action() {
		return action;
	}

	public List<ChatLine> chat() {
		return chat;
	}

	/** Every translation key said, in order. */
	public List<String> saidKeys() {
		List<String> keys = new ArrayList<>();
		for (ChatLine line : chat) keys.addAll(line.keys());
		return keys;
	}

	public Set<String> earned() {
		return earned;
	}

	public Set<String> lost() {
		return lost;
	}

	/** The inventory delta by item and components (an {@code item{patch}} key when components differ from default). */
	public Map<String, Integer> inventoryDelta() {
		return inventory;
	}

	/** The net change in how many of {@code item} the player holds (inventory, armour, offhand and cursor). */
	public int delta(ItemLike item) {
		return byItem.getOrDefault(item.asItem(), 0);
	}

	public List<Drop> drops() {
		return drops;
	}

	/** How many of {@code item} dropped beside {@code near} during the action. */
	public int dropped(ItemLike item, Near near) {
		return drops.stream().filter(drop -> drop.stack().is(item.asItem()) && drop.near(near))
				.mapToInt(drop -> drop.stack().getCount()).sum();
	}

	public int degreeBefore() {
		return degreeBefore;
	}

	public int degreeAfter() {
		return degreeAfter;
	}

	public double bloodBefore() {
		return bloodBefore;
	}

	public double bloodAfter() {
		return bloodAfter;
	}

	/** Blood spent (positive) or gained (negative). */
	public double bloodSpent() {
		return bloodBefore - bloodAfter;
	}

	public boolean bloodActiveAfter() {
		return activeAfter;
	}

	public boolean bloodActiveBefore() {
		return activeBefore;
	}

	public List<CustomPacketPayload> payloads() {
		return payloads;
	}

	public <T extends CustomPacketPayload> List<T> sent(Class<T> type) {
		return payloads.stream().filter(type::isInstance).map(type::cast).toList();
	}

	/** The acknowledged-claim reply, when the action sent one: whether the server says the reward was delivered. */
	public Optional<Boolean> delivered() {
		List<DialogueRewardResultPacket> results = sent(DialogueRewardResultPacket.class);
		return results.isEmpty() ? Optional.empty() : Optional.of(results.getLast().delivered());
	}

	/** Every dialogue event posted for this player during the action. */
	public List<DialogueEvent> events() {
		return events;
	}

	/** The last dialogue event posted, if the action dispatched one (a guard that dropped it posts none). */
	public Optional<DialogueEvent> event() {
		return events.isEmpty() ? Optional.empty() : Optional.of(events.getLast());
	}

	/** Whether the dispatched event's route said it delivered a reward. */
	public boolean rewardDelivered() {
		return event().map(DialogueEvent::wasRewardDelivered).orElse(false);
	}

	/** What changed in the player's save, two levels deep ({@code {}} when nothing did). */
	public String savedDiff() {
		return savedDiff;
	}

	public List<String> notes() {
		return notes;
	}

	/** The whispers queued in the player's inbox during the action, oldest first. */
	public List<DialogueTree> whispersQueued() {
		return whispersQueued;
	}

	/**
	 * This outcome with every line carrying one of {@code keys} left out of its chat, so {@link #didNotSay} and
	 * {@link #saidNothing} stay true to this player's action when a test running alongside broadcasts (a Fane
	 * founded, a blood moon) to every player in the level.
	 */
	public Outcome ignoringBroadcasts(String... keys) {
		List<ChatLine> kept = new ArrayList<>();
		for (ChatLine line : chat) {
			boolean broadcast = false;
			for (String key : keys) broadcast |= line.has(key);
			if (!broadcast) kept.add(line);
		}
		return new Outcome(this, kept);
	}

	/** How many of {@code item} dropped within 3 blocks of {@code pos} during the action (near the player or NPC). */
	public int droppedNear(BlockPos pos, ItemLike item) {
		Vec3 centre = Vec3.atCenterOf(pos);
		return drops.stream().filter(drop -> drop.stack().is(item.asItem()) && drop.pos().distanceTo(centre) <= 3.0D)
				.mapToInt(drop -> drop.stack().getCount()).sum();
	}

	public String describe() {
		return action + ": said=" + chat + " earned=" + earned + (lost.isEmpty() ? "" : " lost=" + lost)
				+ " inventory=" + inventory + " drops=" + drops + " degree=" + degreeBefore + "->" + degreeAfter
				+ String.format(" blood=%.0f->%.0f", bloodBefore, bloodAfter)
				+ (events.isEmpty() ? "" : " event=" + event().map(e -> e.getEventId() + (e.wasRewardDelivered()
						? "(delivered)" : "")).orElse(""))
				+ delivered().map(d -> " ack=" + d).orElse("") + (notes.isEmpty() ? "" : " notes=" + notes);
	}

	@Override
	public String toString() {
		return describe();
	}

	// --- Asserting -------------------------------------------------------------------------------------------------

	/** The player's holdings of {@code item} rose by exactly {@code count}. */
	public Outcome gave(ItemLike item, int count) {
		helper.assertTrue(delta(item) == count, "expected +" + count + " " + key(item) + "; " + describe());
		return this;
	}

	/** Nothing new in the inventory and nothing dropped. */
	public Outcome gaveNothing() {
		boolean gained = inventory.values().stream().anyMatch(value -> value > 0);
		helper.assertTrue(!gained && drops.isEmpty(), "expected nothing given; " + describe());
		return this;
	}

	/** Exactly {@code count} of {@code item} dropped beside {@code near}. */
	public Outcome dropped(ItemLike item, int count, Near near) {
		helper.assertTrue(dropped(item, near) == count,
				"expected " + count + " " + key(item) + " dropped near " + near + "; " + describe());
		return this;
	}

	/** Exactly {@code count} of {@code item} dropped within 3 blocks of {@code pos}. */
	public Outcome droppedNear(BlockPos pos, ItemLike item, int count) {
		helper.assertTrue(droppedNear(pos, item) == count, "expected " + count + " " + key(item) + " dropped near "
				+ pos.toShortString() + "; " + describe());
		return this;
	}

	/**
	 * A whisper was queued in the player's inbox during the action whose tree shows {@code key}: a line, an option
	 * text or the speaker name.
	 */
	public Outcome whisperQueued(String key) {
		boolean found = false;
		for (DialogueTree tree : whispersQueued) {
			if (key.equals(tree.speakerName())) found = true;
			for (DialogueNode node : tree.nodes().values()) {
				for (String line : node.lines()) found |= Dialogue.lineKey(line).map(key::equals).orElse(false);
				for (DialogueOption option : node.options()) found |= key.equals(option.text());
			}
		}
		helper.assertTrue(found, "expected a whisper showing " + key + " queued; queued " + whispersQueued.size()
				+ "; " + describe());
		return this;
	}

	/** Some line said to the player carried {@code key}. */
	public Outcome said(String key) {
		helper.assertTrue(chat.stream().anyMatch(line -> line.has(key)), "expected " + key + " said; " + describe());
		return this;
	}

	/** No line carried {@code key}. */
	public Outcome didNotSay(String key) {
		helper.assertTrue(chat.stream().noneMatch(line -> line.has(key)), "expected " + key + " not said; " + describe());
		return this;
	}

	/** Nothing was said to the player at all. */
	public Outcome saidNothing() {
		helper.assertTrue(chat.isEmpty(), "expected silence; " + describe());
		return this;
	}

	/** The player earned {@code path} (a Hemomancy advancement path such as {@code hemomancy/the_first_awakening}). */
	public Outcome earned(String path) {
		helper.assertTrue(earned.contains(path), "expected advancement " + path + "; " + describe());
		return this;
	}

	public Outcome degreeIs(int degree) {
		helper.assertTrue(degreeAfter == degree, "expected degree " + degree + "; " + describe());
		return this;
	}

	/** The acknowledged claim reply says {@code delivered}. */
	public Outcome deliveredIs(boolean delivered) {
		helper.assertTrue(delivered().orElse(!delivered) == delivered,
				"expected claim delivered=" + delivered + "; " + describe());
		return this;
	}

	/**
	 * Nothing about the player changed: their save is the same (bar volatile entity keys and {@code ignoredKeys}, top
	 * or {@code top.inner} level), nothing dropped and no advancement moved. Lines may still have been said.
	 */
	public Outcome unchanged(String... ignoredKeys) {
		String diff = savedDiff;
		Map<String, String> changes = parseDiff(diff);
		for (String ignored : ignoredKeys) changes.keySet().removeIf(key -> key.equals(ignored) || key.startsWith(ignored + "."));
		helper.assertTrue(changes.isEmpty() && drops.isEmpty() && earned.isEmpty() && lost.isEmpty(),
				"expected no change; save changes=" + changes.keySet() + "; " + describe());
		return this;
	}

	// --- Snapshots -------------------------------------------------------------------------------------------------

	static Map<String, Integer> inventory(ServerPlayer player) {
		Map<String, Integer> counts = new TreeMap<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) add(counts, player.getInventory().getItem(slot));
		add(counts, player.containerMenu.getCarried());
		return counts;
	}

	static Map<Item, Integer> byItem(ServerPlayer player) {
		Map<Item, Integer> counts = new LinkedHashMap<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
		}
		ItemStack carried = player.containerMenu.getCarried();
		if (!carried.isEmpty()) counts.merge(carried.getItem(), carried.getCount(), Integer::sum);
		return counts;
	}

	private static void add(Map<String, Integer> counts, ItemStack stack) {
		if (stack.isEmpty()) return;
		String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		if (!stack.getComponentsPatch().isEmpty()) key += stack.getComponentsPatch();
		counts.merge(key, stack.getCount(), Integer::sum);
	}

	private static <K> Map<K, Integer> diffCounts(Map<K, Integer> before, Map<K, Integer> after, Map<K, Integer> into) {
		Set<K> keys = new java.util.LinkedHashSet<>(before.keySet());
		keys.addAll(after.keySet());
		for (K key : keys) {
			int change = after.getOrDefault(key, 0) - before.getOrDefault(key, 0);
			if (change != 0) into.put(key, change);
		}
		return into;
	}

	private static Map<String, Integer> delta(Map<String, Integer> before, Map<String, Integer> after) {
		return diffCounts(before, after, new TreeMap<>());
	}

	private static Map<Item, Integer> itemDelta(Map<Item, Integer> before, Map<Item, Integer> after) {
		return diffCounts(before, after, new LinkedHashMap<>());
	}

	private static List<Drop> newDrops(Probe probe, ServerPlayer player) {
		List<Drop> drops = new ArrayList<>();
		for (ItemEntity drop : probe.dropsAround(player)) {
			if (probe.dropsBefore.contains(drop.getUUID()) || drop.isRemoved()) continue;
			double toNpc = probe.npc == null ? Double.MAX_VALUE : drop.position().distanceTo(probe.npc.position());
			drops.add(new Drop(drop.getItem().copy(), drop.position(), drop.position().distanceTo(player.position()), toNpc));
		}
		return drops;
	}

	private static Map<String, String> parseDiff(String diff) {
		Map<String, String> changes = new TreeMap<>();
		if (diff == null || diff.length() <= 2) return changes;
		// PlayerFingerprints prints a TreeMap: {a=1, b.c=2}. Keys never hold "=", values may hold ", ".
		String body = diff.substring(1, diff.length() - 1);
		int depth = 0;
		int start = 0;
		for (int i = 0; i <= body.length(); i++) {
			char c = i < body.length() ? body.charAt(i) : ',';
			if (c == '{' || c == '[' || c == '(') depth++;
			else if (c == '}' || c == ']' || c == ')') depth--;
			if (c == ',' && depth == 0 && (i == body.length() || body.startsWith(", ", i))) {
				String entry = body.substring(start, i).trim();
				int equals = entry.indexOf('=');
				if (equals > 0) changes.put(entry.substring(0, equals), entry.substring(equals + 1));
				start = i + 1;
			}
		}
		return changes;
	}

	private static String key(ItemLike item) {
		return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
	}
}
