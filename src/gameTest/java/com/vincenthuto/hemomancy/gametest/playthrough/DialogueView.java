package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueScreenMode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopicState;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * One dialogue as the player was served it, flattened: the start node, every node reachable through
 * {@code nextNodeId} (the hub's synthetic {@code __hub_*} nodes included), the hub's cards per box and its Trade box.
 * Nothing here talks to the server; {@link PlaythroughPlayer#talkTo} builds a fresh view each time.
 */
public final class DialogueView {
	private final DialogueTree tree;
	private final Map<String, DialogueNode> reachable = new LinkedHashMap<>();

	public DialogueView(DialogueTree tree) {
		this.tree = tree;
		Deque<String> queue = new ArrayDeque<>();
		queue.add(tree.startNodeId());
		for (DialogueTopic topic : tree.presentation().topics()) queue.add(topic.targetNodeId());
		while (!queue.isEmpty()) {
			String id = queue.poll();
			if (id == null || reachable.containsKey(id)) continue;
			DialogueNode node = tree.getNode(id);
			if (node == null) continue;
			reachable.put(id, node);
			for (DialogueOption option : node.options()) if (option.nextNodeId() != null) queue.add(option.nextNodeId());
		}
	}

	public DialogueTree tree() {
		return tree;
	}

	/** The speaker's entity id as the tree carries it (0 for a whisper). */
	public int entityId() {
		return tree.entityId();
	}

	public boolean isHub() {
		return tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB;
	}

	/** The nodes a player can reach, in discovery order. */
	public Map<String, DialogueNode> nodes() {
		return reachable;
	}

	public Optional<DialogueNode> node(String id) {
		return Optional.ofNullable(reachable.get(id));
	}

	public boolean hasNode(String id) {
		return reachable.containsKey(id);
	}

	/** Every event a player can choose: reachable and enabled. */
	public List<String> reachableEvents() {
		Set<String> events = new LinkedHashSet<>();
		for (DialogueNode node : reachable.values()) {
			for (DialogueOption option : node.options()) {
				if (option.eventId() != null && !option.eventId().isEmpty() && option.presentation().enabled()) {
					events.add(option.eventId());
				}
			}
		}
		return List.copyOf(events);
	}

	/** Every event shown, choosable or not (a disabled "not yet" option included). */
	public List<String> shownEvents() {
		Set<String> events = new LinkedHashSet<>();
		for (DialogueNode node : reachable.values()) {
			for (DialogueOption option : node.options()) {
				if (option.eventId() != null && !option.eventId().isEmpty()) events.add(option.eventId());
			}
		}
		return List.copyOf(events);
	}

	/** Whether the player can choose {@code eventId} in this tree. */
	public boolean offers(String eventId) {
		return reachableEvents().contains(eventId);
	}

	/** The first reachable option carrying {@code eventId}, enabled or not. */
	public Optional<DialogueOption> option(String eventId) {
		for (DialogueNode node : reachable.values()) {
			for (DialogueOption option : node.options()) if (eventId.equals(option.eventId())) return Optional.of(option);
		}
		return Optional.empty();
	}

	/** The first reachable option whose text is {@code textKey}. */
	public Optional<DialogueOption> optionByText(String textKey) {
		for (DialogueNode node : reachable.values()) {
			for (DialogueOption option : node.options()) if (textKey.equals(option.text())) return Optional.of(option);
		}
		return Optional.empty();
	}

	/** Every hub card, all boxes, in served order. */
	public List<DialogueTopic> topics() {
		return tree.presentation().topics();
	}

	public List<DialogueTopic> topics(DialogueCategory category) {
		return tree.presentation().topics(category);
	}

	public Optional<DialogueTopic> topicById(String topicId) {
		return topics().stream().filter(topic -> topic.id().equals(topicId)).findFirst();
	}

	/** The card through which {@code eventId} is reached, the first in served order. */
	public Optional<DialogueTopic> topic(String eventId) {
		for (DialogueTopic topic : topics()) {
			if (eventsUnder(topic.targetNodeId()).contains(eventId)) return Optional.of(topic);
		}
		return Optional.empty();
	}

	/** The box {@code eventId} is filed in (QUESTS, CONVERSATION, INQUIRIES, TRADE), if any card reaches it. */
	public Optional<DialogueCategory> category(String eventId) {
		return topic(eventId).map(DialogueTopic::category);
	}

	/** The card state the event's card shows (TURN_IN, ACTIVE, ...). */
	public Optional<DialogueTopicState> questState(String eventId) {
		return topic(eventId).map(DialogueTopic::state);
	}

	/** The sigil the event's card shows. */
	public Optional<DialogueAttention> attention(String eventId) {
		return topic(eventId).map(DialogueTopic::attention);
	}

	public NpcTradeBoard tradeBoard() {
		return tree.presentation().trade();
	}

	public List<NpcTradeCard> tradeCards() {
		return tree.presentation().tradeCards();
	}

	public Optional<NpcTradeCard> tradeCard(ResourceLocation offerId) {
		return tradeBoard().card(offerId);
	}

	/** Every line key of every reachable node, in order. */
	public List<String> lineKeys() {
		List<String> keys = new ArrayList<>();
		for (DialogueNode node : reachable.values()) {
			for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(keys::add);
		}
		return keys;
	}

	/** The literal lines served (text, not a key), each prefixed by its node. */
	public List<String> literalLines() {
		List<String> literals = new ArrayList<>();
		for (DialogueNode node : tree.nodes().values()) {
			for (String line : node.lines()) {
				if (line.startsWith(DialogueNode.LITERAL_PREFIX)) literals.add(node.id() + ": " + line);
			}
			for (DialogueOption option : node.options()) {
				if (option.text().startsWith(DialogueNode.LITERAL_PREFIX)) literals.add(node.id() + " option: " + option.text());
			}
		}
		return literals;
	}

	/**
	 * Every translation key this dialogue shows that has no translation: node lines, option texts, details and
	 * prompts, locked teasers, card titles, summaries and groups, progress rewards, the Trade box's refusal and its
	 * cards' titles, lines and lock reasons, and the speaker's name. Each entry names where the key is.
	 */
	public List<String> missingLangKeys() {
		List<String> missing = new ArrayList<>();
		check(missing, "speaker name", tree.speakerName());
		for (DialogueNode node : tree.nodes().values()) {
			for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(key -> check(missing, node.id() + " line", key));
			for (DialogueOption option : node.options()) {
				Dialogue.lineKey(option.text()).ifPresent(key -> check(missing, node.id() + " option", key));
				var shown = option.presentation();
				if (shown.detailKey() != null) check(missing, node.id() + " option detail", shown.detailKey());
				if (shown.promptKey() != null) check(missing, node.id() + " option prompt", shown.promptKey());
				if (shown.rapportTier() > 0) {
					check(missing, node.id() + " locked teaser", option.text() + DialogueHubFactory.TEASER_SUFFIX);
				}
			}
		}
		for (DialogueTopic topic : topics()) {
			check(missing, "card " + topic.id() + " title", topic.titleKey());
			check(missing, "card " + topic.id() + " summary", topic.summaryKey());
			if (topic.groupKey() != null) check(missing, "card " + topic.id() + " group", topic.groupKey());
			if (topic.progress() != null && topic.progress().rewardKey() != null) {
				check(missing, "card " + topic.id() + " reward", topic.progress().rewardKey());
			}
		}
		NpcTradeBoard board = tradeBoard();
		board.refusalKey().ifPresent(key -> check(missing, "trade refusal", key));
		for (NpcTradeCard card : board.cards()) {
			check(missing, "trade " + card.offerId() + " title", card.titleKey());
			check(missing, "trade " + card.offerId() + " line", card.lineKey());
			card.lockKey().ifPresent(key -> check(missing, "trade " + card.offerId() + " lock", key));
		}
		return missing;
	}

	public DialogueView assertLangKeys(GameTestHelper helper, String label) {
		List<String> missing = missingLangKeys();
		helper.assertTrue(missing.isEmpty(), label + " shows keys with no translation: " + missing);
		return this;
	}

	/**
	 * Every way the player could get stuck: an option or a card pointing at a node that was not served, or a
	 * reachable node with no enabled option (nothing to continue with and no way to leave).
	 */
	public List<String> deadEnds() {
		List<String> problems = new ArrayList<>();
		for (DialogueNode node : tree.nodes().values()) {
			for (DialogueOption option : node.options()) {
				if (option.nextNodeId() != null && tree.getNode(option.nextNodeId()) == null) {
					problems.add(node.id() + " -> missing node " + option.nextNodeId() + " (" + option.text() + ")");
				}
			}
		}
		for (DialogueTopic topic : topics()) {
			String target = topic.targetNodeId();
			if (DialogueHubFactory.TRADE_TARGET.equals(target) || DialogueHubFactory.LOCKED_TARGET.equals(target)) continue;
			if (tree.getNode(target) == null) problems.add("card " + topic.id() + " -> missing node " + target);
		}
		// An item inquiry card is read-only by design: its node carries the item_hint options minus the old Leave, and
		// the hub screen's footer Back returns from it. Only outside a hub is an optionless node a dead end.
		Set<String> inquiryTargets = new LinkedHashSet<>();
		if (isHub()) for (DialogueTopic topic : topics(DialogueCategory.INQUIRIES)) inquiryTargets.add(topic.targetNodeId());
		// A one-node statement tree (an idle or refusal line) is closed by the footer's Leave, which the screen draws on
		// every view that has no Back; nothing in it asks the player to go on.
		boolean statement = !isHub() && tree.nodes().size() == 1;
		for (DialogueNode node : reachable.values()) {
			if (inquiryTargets.contains(node.id()) || statement) continue;
			if (node.options().stream().noneMatch(option -> option.presentation().enabled())) {
				problems.add(node.id() + " has no enabled option to continue or leave");
			}
		}
		return problems;
	}

	public DialogueView assertNoDeadEnds(GameTestHelper helper, String label) {
		List<String> problems = deadEnds();
		helper.assertTrue(problems.isEmpty(), label + " has dead ends: " + problems);
		return this;
	}

	/** Every node the tree carries that no option or card leads to (the start node excepted). */
	public List<String> unreachableNodes() {
		List<String> unreachable = new ArrayList<>();
		for (String id : tree.nodes().keySet()) if (!reachable.containsKey(id)) unreachable.add(id);
		return unreachable;
	}

	/**
	 * Every reachable node left with no options at all, the item inquiry cards and a one-node statement tree excepted
	 * (read-only by design; the footer's Leave closes them): a node a player arrives at and can only close.
	 */
	public List<String> emptiedNodes() {
		Set<String> inquiryTargets = new LinkedHashSet<>();
		if (isHub()) for (DialogueTopic topic : topics(DialogueCategory.INQUIRIES)) inquiryTargets.add(topic.targetNodeId());
		List<String> emptied = new ArrayList<>();
		if (!isHub() && tree.nodes().size() == 1) return emptied; // a statement the footer's Leave closes
		for (DialogueNode node : reachable.values()) {
			if (!inquiryTargets.contains(node.id()) && node.options().isEmpty()) emptied.add(node.id());
		}
		return emptied;
	}

	/**
	 * The hub's root options that no card draws. The hub screen shows cards, not the root's options, so an option
	 * added to the root after {@code DialogueHubFactory.decorate} built the cards is hidden. Leave and item inquiry
	 * options are not cards by design and are skipped. Empty outside a hub.
	 */
	public List<String> rootOptionsWithoutCard() {
		List<String> hidden = new ArrayList<>();
		if (!isHub()) return hidden;
		DialogueNode root = tree.getNode(tree.startNodeId());
		if (root == null) return hidden;
		for (DialogueOption option : root.options()) {
			String text = option.text() == null ? "" : option.text();
			String lower = (text + " " + (option.nextNodeId() == null ? "" : option.nextNodeId())).toLowerCase(java.util.Locale.ROOT);
			if (option.nextNodeId() == null && option.eventId() == null && lower.contains("leave")) continue;
			if (lower.contains("item_hint") || lower.contains("ask_about_item")) continue;
			boolean drawn = false;
			for (DialogueTopic topic : topics()) {
				if (text.equals(topic.titleKey()) || option.nextNodeId() != null && option.eventId() == null
						&& option.nextNodeId().equals(topic.targetNodeId())
						|| option.eventId() != null && eventsUnder(topic.targetNodeId()).contains(option.eventId())) {
					drawn = true;
					break;
				}
			}
			if (!drawn) hidden.add(text + (option.eventId() == null ? "" : " [" + option.eventId() + "]"));
		}
		return hidden;
	}

	/** Fails when a hub root option has no card ({@link #rootOptionsWithoutCard()}). */
	public DialogueView assertEveryRootOptionHasACard(GameTestHelper helper, String label) {
		List<String> hidden = rootOptionsWithoutCard();
		helper.assertTrue(hidden.isEmpty(), label + " has root options the hub draws no card for: " + hidden);
		return this;
	}

	/** A one-line summary for logs and failure messages: start node, cards by box, choosable events. */
	public String describe() {
		StringBuilder text = new StringBuilder("start=").append(tree.startNodeId());
		for (DialogueCategory category : DialogueCategory.values()) {
			List<String> cards = topics(category).stream()
					.map(topic -> topic.titleKey() + "[" + topic.state() + "," + topic.attention() + "]").toList();
			if (!cards.isEmpty()) text.append(' ').append(category).append('=').append(cards);
		}
		return text.append(" events=").append(reachableEvents()).toString();
	}

	@Override
	public String toString() {
		return describe();
	}

	private Set<String> eventsUnder(String nodeId) {
		Set<String> events = new LinkedHashSet<>();
		Set<String> seen = new LinkedHashSet<>();
		Deque<String> queue = new ArrayDeque<>();
		queue.add(nodeId);
		while (!queue.isEmpty()) {
			String id = queue.poll();
			// The way back to the hub is not part of the card.
			if (id == null || !seen.add(id) || (id.equals(tree.startNodeId()) && !id.equals(nodeId))) continue;
			DialogueNode node = tree.getNode(id);
			if (node == null) continue;
			for (DialogueOption option : node.options()) {
				if (option.eventId() != null) events.add(option.eventId());
				if (option.nextNodeId() != null) queue.add(option.nextNodeId());
			}
		}
		return events;
	}

	private static void check(List<String> missing, String where, String key) {
		if (key == null || key.isEmpty()) return;
		if (!LangKeys.exists(key)) missing.add(where + ": " + key);
	}
}
