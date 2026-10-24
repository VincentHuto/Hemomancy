package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedPhase;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure geometry for the Unstained Progress path: one left-to-right line from Lethean Baptism
 * through the purity stages and the three bridge rites into the clarity stages.
 * <p>
 * Positions come from each node's index in the full path, so omitted clarity stages never
 * move the visible nodes. Drawing, tooltips and hit-testing all read this class.
 */
public final class UnstainedProgressLayout {

	public static final int PITCH = 62;
	public static final int FIRST_X = 32;
	public static final int BASELINE_Y = 68;
	public static final int STAGE_SIZE = 30;
	public static final int RITE_SIZE = 20;
	/** Label top sits this far below the baseline (below the largest diamond plus a gap). */
	public static final int LABEL_DY = STAGE_SIZE / 2 + 4;
	public static final int LABEL_LINE_H = 10;
	public static final int LABEL_TEXT_H = 9;
	public static final int PIP_SIZE = 6;
	public static final int PIP_PITCH = 9;
	/** Pip centre below the baseline, under the two-line rite labels. */
	public static final int PIP_DY = 46;
	/** Segment header: title, progress bar and percent readout above the stages. */
	public static final int HEADER_DY = -58;
	public static final int HEADER_W = 160;
	public static final int HEADER_H = 32;
	public static final int HEADER_BAR_DY = 13;
	public static final int HEADER_BAR_H = 6;
	public static final int HEADER_PERCENT_DY = 23;
	public static final int PENALTY_DY = 58;
	/** The penalty line at its widest, {@code -100%}. */
	public static final String PENALTY_KEY = "screen.hemomancy.unstained_progress.blood_magic_penalty_2";
	public static final String VEILED_KEY = "screen.hemomancy.unstained_progress_layout.veiled";
	public static final int PAD = 8;
	public static final float FIT_ZOOM_MIN = 0.5F;
	public static final float FIT_ZOOM_MAX = 1.0F;

	public enum Kind { RITE, PURITY_STAGE, CLARITY_STAGE }

	/** Resolves a translation key with arguments: {@code I18n::get} on the client. */
	@FunctionalInterface
	public interface Text {
		String get(String key, Object... args);
	}

	public enum Status {
		REACHED, CURRENT, NEXT, LOCKED, VEILED,
		/** Passed without being walked: the vow route skips Baptism's purification and Closed Vein. */
		BYPASSED;

		public boolean done() {
			return this == REACHED || this == CURRENT || this == BYPASSED;
		}
	}

	public enum Rite {
		LETHEAN_BAPTISM("lethean_baptism"),
		CLOSED_VEIN("closed_vein"),
		CONSECRATED_COPPER("consecrated_copper"),
		CLARITY_ASCENSION("clarity_ascension");

		private final String id;

		Rite(String id) {
			this.id = id;
		}

		public String id() { return id; }
		public String titleKey() { return "screen.hemomancy.unstained_progress_layout.rite." + id; }
		public Component title() { return Component.translatable(titleKey()); }
		public List<String> labelLines(Text text) {
			return List.of(text.get(titleKey() + ".line1"), text.get(titleKey() + ".line2"));
		}

		/** The three rites between Purified and the clarity stages. */
		public boolean isBridge() { return this != LETHEAN_BAPTISM; }
	}

	public record Rect(int x, int y, int w, int h) {
		public int right() { return x + w; }
		public int bottom() { return y + h; }

		public Rect grow(int by) {
			return new Rect(x - by, y - by, w + by * 2, h + by * 2);
		}

		public boolean intersects(Rect o) {
			return x < o.right() && o.x < right() && y < o.bottom() && o.y < bottom();
		}

		public boolean contains(double px, double py) {
			return px >= x && px <= right() && py >= y && py <= bottom();
		}

		public Rect union(Rect o) {
			int nx = Math.min(x, o.x);
			int ny = Math.min(y, o.y);
			return new Rect(nx, ny, Math.max(right(), o.right()) - nx, Math.max(bottom(), o.bottom()) - ny);
		}
	}

	/** Player progress the path is drawn for. */
	public record Progress(UnstainedPhase phase, EnumPurityStage purity, boolean clarityPrepared,
			boolean clarityUnlocked, EnumClarityStage clarity, boolean begunPurification) {}

	public record Node(Kind kind, String id, int x, int y, int halfSize, Status status,
			@Nullable Rite rite, @Nullable EnumPurityStage purityStage, @Nullable EnumClarityStage clarityStage,
			List<String> labelLines) {

		public Component title() {
			if (rite != null) return rite.title();
			if (purityStage != null) return Component.translatable(stageKey(purityStage));
			return clarityStage != null ? Component.translatable(stageKey(clarityStage)) : Component.literal(id);
		}

		public boolean veiled() { return status == Status.VEILED; }
		public boolean done() { return status.done(); }

		/** Square around the node shape. */
		public Rect bounds() {
			return new Rect(x - halfSize, y - halfSize, halfSize * 2, halfSize * 2);
		}

		/** Estimated rectangle of the centred label lines under the node. */
		public Rect labelRect() {
			int w = 0;
			for (String line : labelLines) w = Math.max(w, estimateWidth(line));
			int h = (labelLines.size() - 1) * LABEL_LINE_H + LABEL_TEXT_H;
			return new Rect(x - w / 2, y + LABEL_DY, w, h);
		}
	}

	public record Link(Node from, Node to) {
		public boolean lit() { return to.done() && to.status() != Status.BYPASSED; }
	}

	public record Pip(Observance observance, Node anchor, int x, int y) {
		public Rect rect() {
			return new Rect(x - PIP_SIZE / 2, y - PIP_SIZE / 2, PIP_SIZE, PIP_SIZE);
		}
	}

	/** Opening view: zoom plus pan relative to the GUI's top-left corner. */
	public record View(float zoom, double panX, double panY) {}

	private static final List<Observance> EARN_ORDER = List.of(
			Observance.NOVITIATE_GATHER_REMEDIES,
			Observance.NOVITIATE_GENTLE_SEPARATION,
			Observance.NOVITIATE_STILLWATER_LABOR,
			Observance.NOVITIATE_CLEAN_LABOR,
			Observance.NOVITIATE_SHELTER_AFFLICTED,
			Observance.GATHER_GHOST_PIPE,
			Observance.WEAVE_WREATH,
			Observance.PREPARE_HEMOLYTIC,
			Observance.CONDENSE_STILL_WATERS,
			Observance.PLATE_THE_WARD,
			Observance.BEAR_PALLID_ICON,
			Observance.CONSECRATE_COPPER,
			Observance.OFFER_CHALICE,
			Observance.RING_THE_PALE_WATCH);

	private final List<Node> nodes;
	private final List<Link> links;
	private final List<Pip> pips;
	private final Node focus;
	private final Rect purityHeader;
	@Nullable private final Rect clarityHeader;
	private final Rect penaltyRect;
	private final Rect contentBounds;

	private UnstainedProgressLayout(List<Node> nodes, Node focus, Text text) {
		this.nodes = List.copyOf(nodes);
		this.focus = focus;
		List<Link> builtLinks = new ArrayList<>();
		for (int i = 1; i < nodes.size(); i++) builtLinks.add(new Link(nodes.get(i - 1), nodes.get(i)));
		this.links = List.copyOf(builtLinks);
		this.pips = List.copyOf(buildPips(nodes));

		Node corrupted = node(stageId(EnumPurityStage.CORRUPTED)).orElseThrow();
		Node purified = node(stageId(EnumPurityStage.PURIFIED)).orElseThrow();
		int purityCentre = (corrupted.x() + purified.x()) / 2;
		this.purityHeader = headerAt(purityCentre);
		Optional<Node> awakened = node(stageId(EnumClarityStage.AWAKENED));
		Optional<Node> enlightened = node(stageId(EnumClarityStage.ENLIGHTENED));
		this.clarityHeader = awakened.isPresent() && enlightened.isPresent()
				? headerAt((awakened.get().x() + enlightened.get().x()) / 2) : null;
		int penaltyW = estimateWidth(text.get(PENALTY_KEY, "100"));
		this.penaltyRect = new Rect(purityCentre - penaltyW / 2, BASELINE_Y + PENALTY_DY, penaltyW, LABEL_TEXT_H);

		Rect extent = purityHeader.union(penaltyRect);
		if (clarityHeader != null) extent = extent.union(clarityHeader);
		for (Node node : this.nodes) extent = extent.union(node.bounds()).union(node.labelRect());
		for (Pip pip : this.pips) extent = extent.union(pip.rect());
		this.contentBounds = new Rect(0, 0, extent.right() + PAD, extent.bottom() + PAD);
	}

	// ── Construction ─────────────────────────────────────────────

	public static UnstainedProgressLayout build(Progress progress, Text text) {
		UnstainedPhase phase = progress.phase();
		boolean begun = phase == UnstainedPhase.PURIFYING || phase == UnstainedPhase.CURE_READY
				|| phase == UnstainedPhase.CLEANSED_UNPLEDGED || phase == UnstainedPhase.PLEDGED;
		boolean baselineRestored = phase == UnstainedPhase.CLEANSED_UNPLEDGED || phase == UnstainedPhase.PLEDGED;
		// Pledging through the five service vows never walks the purity segment or Closed Vein.
		boolean vowRoute = phase == UnstainedPhase.PLEDGED && !progress.begunPurification();
		boolean purified = begun && progress.purity() == EnumPurityStage.PURIFIED;
		boolean bridgeRevealed = purified || baselineRestored || progress.clarityPrepared() || progress.clarityUnlocked();

		List<Slot> slots = new ArrayList<>();
		slots.add(Slot.rite(Rite.LETHEAN_BAPTISM, begun));
		for (EnumPurityStage stage : EnumPurityStage.values()) {
			slots.add(vowRoute ? Slot.purity(stage, true).bypass()
					: Slot.purity(stage, begun && progress.purity().getLevel() >= stage.getLevel()));
		}
		slots.add(vowRoute ? Slot.rite(Rite.CLOSED_VEIN, true).bypass() : Slot.rite(Rite.CLOSED_VEIN, baselineRestored));
		slots.add(Slot.rite(Rite.CONSECRATED_COPPER, progress.clarityPrepared() || progress.clarityUnlocked()));
		slots.add(Slot.rite(Rite.CLARITY_ASCENSION, progress.clarityUnlocked()));
		for (EnumClarityStage stage : EnumClarityStage.values()) {
			slots.add(Slot.clarity(stage, progress.clarityUnlocked() && progress.clarity().getLevel() >= stage.getLevel()));
		}

		int lastDone = -1;
		int current = -1;
		for (int i = 0; i < slots.size(); i++) {
			if (!slots.get(i).done) continue;
			lastDone = i;
			if (slots.get(i).rite == null && !slots.get(i).bypassed) current = i;
		}
		int next = lastDone + 1 < slots.size() ? lastDone + 1 : -1;

		List<Node> nodes = new ArrayList<>();
		Node focus = null;
		Node fallbackFocus = null;
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			if (slot.clarityStage != null && !progress.clarityUnlocked()) continue;
			Status status;
			if (slot.bypassed) {
				status = Status.BYPASSED;
			} else if (slot.done) {
				status = i == current ? Status.CURRENT : Status.REACHED;
			} else if (i == next) {
				status = Status.NEXT;
			} else if (slot.rite != null && slot.rite.isBridge() && !bridgeRevealed) {
				status = Status.VEILED;
			} else {
				status = Status.LOCKED;
			}
			Node node = slot.toNode(FIRST_X + i * PITCH, BASELINE_Y, status, text);
			nodes.add(node);
			if (status == Status.NEXT) focus = node;
			if (status == Status.CURRENT && focus == null) fallbackFocus = node;
		}
		if (focus == null) focus = fallbackFocus != null ? fallbackFocus : nodes.getFirst();
		return new UnstainedProgressLayout(nodes, focus, text);
	}

	private record Slot(@Nullable Rite rite, @Nullable EnumPurityStage purityStage,
			@Nullable EnumClarityStage clarityStage, boolean done, boolean bypassed) {
		static Slot rite(Rite rite, boolean done) { return new Slot(rite, null, null, done, false); }
		static Slot purity(EnumPurityStage stage, boolean done) { return new Slot(null, stage, null, done, false); }
		static Slot clarity(EnumClarityStage stage, boolean done) { return new Slot(null, null, stage, done, false); }

		Slot bypass() { return new Slot(rite, purityStage, clarityStage, true, true); }

		Node toNode(int x, int y, Status status, Text text) {
			if (rite != null) {
				List<String> lines = status == Status.VEILED ? List.of(text.get(VEILED_KEY)) : rite.labelLines(text);
				return new Node(Kind.RITE, rite.id(), x, y, RITE_SIZE / 2, status, rite, null, null, lines);
			}
			if (purityStage != null) {
				return new Node(Kind.PURITY_STAGE, stageId(purityStage), x, y, STAGE_SIZE / 2, status,
						null, purityStage, null, List.of(text.get(stageKey(purityStage))));
			}
			return new Node(Kind.CLARITY_STAGE, stageId(clarityStage), x, y, STAGE_SIZE / 2, status,
					null, null, clarityStage, List.of(text.get(stageKey(clarityStage))));
		}
	}

	private static List<Pip> buildPips(List<Node> nodes) {
		Map<Observance, String> anchors = new EnumMap<>(Observance.class);
		for (Observance observance : EARN_ORDER) anchors.put(observance, anchorId(observance));
		List<Pip> result = new ArrayList<>();
		for (Node node : nodes) {
			List<Observance> group = EARN_ORDER.stream().filter(o -> anchors.get(o).equals(node.id())).toList();
			for (int i = 0; i < group.size(); i++) {
				int offset = (int) Math.floor((i - (group.size() - 1) / 2.0) * PIP_PITCH + 0.5);
				result.add(new Pip(group.get(i), node, node.x() + offset, node.y() + PIP_DY));
			}
		}
		result.sort((a, b) -> Integer.compare(EARN_ORDER.indexOf(a.observance()), EARN_ORDER.indexOf(b.observance())));
		return result;
	}

	/** Id of the node an observance's pip hangs under: the step at which it becomes available. */
	public static String anchorId(Observance observance) {
		return switch (observance) {
			case NOVITIATE_GATHER_REMEDIES, NOVITIATE_GENTLE_SEPARATION, NOVITIATE_STILLWATER_LABOR,
					NOVITIATE_CLEAN_LABOR, NOVITIATE_SHELTER_AFFLICTED -> Rite.LETHEAN_BAPTISM.id();
			case GATHER_GHOST_PIPE -> stageId(EnumPurityStage.CORRUPTED);
			case WEAVE_WREATH, PREPARE_HEMOLYTIC -> stageId(EnumPurityStage.TAINTED);
			case CONDENSE_STILL_WATERS, PLATE_THE_WARD -> stageId(EnumPurityStage.CLEANSING);
			case BEAR_PALLID_ICON -> stageId(EnumPurityStage.ABSOLVED);
			case CONSECRATE_COPPER -> stageId(EnumPurityStage.PURIFIED);
			case OFFER_CHALICE -> stageId(EnumClarityStage.AWAKENED);
			case RING_THE_PALE_WATCH -> stageId(EnumClarityStage.VIGILANT);
		};
	}

	public static String stageId(EnumPurityStage stage) {
		return "purity_" + stage.name().toLowerCase(java.util.Locale.ROOT);
	}

	public static String stageId(EnumClarityStage stage) {
		return "clarity_" + stage.name().toLowerCase(java.util.Locale.ROOT);
	}

	public static String stageKey(EnumPurityStage stage) {
		return "hemomancy.purity." + stage.name().toLowerCase(java.util.Locale.ROOT);
	}

	public static String stageKey(EnumClarityStage stage) {
		return "hemomancy.clarity." + stage.name().toLowerCase(java.util.Locale.ROOT);
	}

	private static Rect headerAt(int centreX) {
		return new Rect(centreX - HEADER_W / 2, BASELINE_Y + HEADER_DY, HEADER_W, HEADER_H);
	}

	// ── Queries ──────────────────────────────────────────────────

	/** Observances in the order a player earns them: vows, then purity, then clarity. */
	public static List<Observance> earnOrder() {
		return EARN_ORDER;
	}

	public List<Node> nodes() { return nodes; }
	public List<Link> links() { return links; }
	/** Pips in earn order; clarity pips are absent while the clarity stages are hidden. */
	public List<Pip> pips() { return pips; }
	public Rect purityHeader() { return purityHeader; }
	public Optional<Rect> clarityHeader() { return Optional.ofNullable(clarityHeader); }
	public Rect penaltyRect() { return penaltyRect; }
	/** Content extent from (0, 0), including labels, pips, headers and the penalty line. */
	public Rect contentBounds() { return contentBounds; }
	/** The next step, else the current stage: what the opening view keeps on screen. */
	public Node focusNode() { return focus; }

	public Optional<Node> node(String id) {
		for (Node node : nodes) if (node.id().equals(id)) return Optional.of(node);
		return Optional.empty();
	}

	public Optional<Pip> pip(Observance observance) {
		for (Pip pip : pips) if (pip.observance() == observance) return Optional.of(pip);
		return Optional.empty();
	}

	// ── Opening view ─────────────────────────────────────────────

	/**
	 * Fits the whole path into the given area (GUI-relative pixels), zoom clamped to
	 * [0.5, 1.0]. When the path is still wider than the area, it slides so the focus
	 * node and its label stay inside.
	 */
	public View fit(int areaX, int areaY, int areaW, int areaH) {
		float zoom = fitZoom(contentBounds.w(), contentBounds.h(), areaW, areaH);
		double panX = areaX + (areaW - contentBounds.w() * zoom) / 2.0 - contentBounds.x() * zoom;
		double panY = areaY + (areaH - contentBounds.h() * zoom) / 2.0 - contentBounds.y() * zoom;
		if (contentBounds.w() * zoom > areaW) {
			Rect keep = focus.bounds().union(focus.labelRect()).grow(PAD);
			double left = panX + keep.x() * zoom;
			double right = panX + keep.right() * zoom;
			if (left < areaX) {
				panX += areaX - left;
			} else if (right > areaX + areaW) {
				panX -= right - (areaX + areaW);
			}
		}
		return new View(zoom, panX, panY);
	}

	public static float fitZoom(int contentW, int contentH, int areaW, int areaH) {
		if (contentW <= 0 || contentH <= 0) return FIT_ZOOM_MAX;
		float fit = Math.min(Math.max(1, areaW) / (float) contentW, Math.max(1, areaH) / (float) contentH);
		return Math.max(FIT_ZOOM_MIN, Math.min(FIT_ZOOM_MAX, fit));
	}

	// ── Label width estimate (vanilla default font advances) ─────

	/** Width of {@code text} in the vanilla default font, without needing a Font instance. */
	public static int estimateWidth(String text) {
		int width = 0;
		for (int i = 0; i < text.length(); i++) width += advance(text.charAt(i));
		return width;
	}

	private static int advance(char c) {
		return switch (c) {
			case 'i', '!', '.', ',', '\'', ':', ';', '|' -> 2;
			case 'l', '`' -> 3;
			case 't', 'I', ' ', '[', ']' -> 4;
			case 'f', 'k', '(', ')', '{', '}', '<', '>', '*' -> 5;
			default -> 6;
		};
	}
}
