package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.FrozenDevoteeBlock;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The frozen congregation in the fight (design section 9): every figure the arena's scan finds, what it is now, and
 * everything that moves it. Held Projection wakes a figure in a proper fight and pins it with hyphae in a forced one;
 * forced fights raise hostiles at each floor and to Call to Prayer; Recall stills an ally; allies shelter and grip;
 * the Beholding deed's figures watch; the Long Night and the Endless Hour darken the rime lanterns; when his body fails
 * the faithful walk home. Since the 2026-11-06 revision the congregation is also his source: frozen and stilled figures
 * feed Veins of Frost, allies carve warm holes in the dark, and in the Eclipse every frozen, stilled and pinned figure
 * stands up as a torn umbral shape (an owned hazard) that reaches; pinned ones' arms are held. Every block goes through the arena, every devotee is owned by it; nothing here outlives the
 * trial. Rebuilt from a fresh scan after a mid-fight load (the arena has already put the chamber back).
 */
public final class VelorumCongregation {
	private static final String FIGURES = "velorum_frozen_devotees";
	private static final String LANTERNS = "velorum_rime_lanterns";
	/** The rank pillars' lantern course, chamber-local (themes/velorum.py PILLAR_LAMP). */
	private static final int PILLAR_LAMP_Y = 6;
	/** The vault's crown lamps hang this high or higher, chamber-local; they stay lit through the Endless Hour. */
	private static final double VAULT_LAMP_Y = 12.0D;
	/** How close to a figure's cell a held Projection must land. */
	private static final double FIGURE_REACH = 1.5D;
	/** The figure's face height over its base, by pose (kneeling, bowed, reaching, prostrate, striding). */
	private static final double[] FACE_HEIGHT = {1.2D, 0.9D, 1.2D, 0.4D, 1.7D};

	/** One figure and what it is now. */
	public static final class Figure {
		final BlockPos base;
		final Direction facing;
		final int pose;
		final BlockState original;
		VelorumCongregationRules.State state = VelorumCongregationRules.State.FROZEN;
		float fed;
		int idle;
		long lockoutUntil;
		boolean recalled;
		@Nullable
		UUID devotee;

		Figure(BlockPos base, Direction facing, int pose, BlockState original) {
			this.base = base;
			this.facing = facing;
			this.pose = pose;
			this.original = original;
		}

		public BlockPos base() {
			return base;
		}

		public VelorumCongregationRules.State state() {
			return state;
		}

		public Vec3 face() {
			return Vec3.atBottomCenterOf(base).add(0.0D, FACE_HEIGHT[Math.max(0, Math.min(4, pose))], 0.0D);
		}
	}

	private final Map<Long, Figure> figures = new LinkedHashMap<>();
	private final List<BlockPos> pillarLanterns = new ArrayList<>();
	private final List<BlockPos> lanterns = new ArrayList<>();
	/** Every rime lantern the scan found, the votive stands and crown lamps too: the Eclipse puts them all out. */
	private final List<BlockPos> everyLantern = new ArrayList<>();
	private final Set<UUID> sheltered = new HashSet<>();
	private final Set<UUID> warmed = new HashSet<>();
	private final List<Shape> shapes = new ArrayList<>();
	private final Set<UUID> watched = new HashSet<>();
	private int grippers;
	private int risenAtFloor;

	/** One scan of the chamber: the figures (not the first who stilled with him) and the rime lanterns. */
	public void resolve(SaintArena arena) {
		figures.clear();
		pillarLanterns.clear();
		lanterns.clear();
		everyLantern.clear();
		shapes.clear();
		for (BlockPos pos : arena.find(FIGURES, state -> state.is(BlockInit.frozen_devotee.get())
				&& state.getValue(FrozenDevoteeBlock.HALF) == DoubleBlockHalf.LOWER)) {
			Vec3 local = arena.toLocal(Vec3.atBottomCenterOf(pos));
			if (arena.chamberFound() && local.x >= SaintChamberLayout.BACK - 0.5D) continue;
			BlockState state = arena.saint().level().getBlockState(pos);
			figures.put(pos.asLong(), new Figure(pos, state.getValue(FrozenDevoteeBlock.FACING),
					state.getValue(FrozenDevoteeBlock.POSE), state));
		}
		for (BlockPos pos : arena.find(LANTERNS, state -> state.is(BlockInit.rime_lantern.get()))) {
			everyLantern.add(pos);
			// never dimmed, so the chamber is never black (design section 9): the aisle-end votive stands (a lantern on a
			// rimed wall post) and the vault's crown lamps, which stand in this chamber for the sea lanterns the design names
			Vec3 local = arena.toLocal(Vec3.atBottomCenterOf(pos));
			if (arena.saint().level().getBlockState(pos.below()).getBlock() instanceof net.minecraft.world.level.block.WallBlock) continue;
			if (arena.chamberFound() && local.y >= VAULT_LAMP_Y) continue;
			lanterns.add(pos);
			int z = (int) Math.floor(local.z);
			if ((int) Math.round(local.y) == PILLAR_LAMP_Y
					&& (z == SaintChamberLayout.RANK_NORTH_Z || z == SaintChamberLayout.RANK_SOUTH_Z)) {
				pillarLanterns.add(pos);
			}
		}
	}

	public List<Figure> figures() {
		return List.copyOf(figures.values());
	}

	// ── each tick ────────────────────────────────────────────────────

	public void tick(VelorumEntity velorum) {
		ServerLevel level = (ServerLevel) velorum.level();
		List<VelorumDevoteeEntity> awake = devotees(velorum);
		sheltered.clear();
		warmed.clear();
		watched.clear();
		grippers = 0;
		List<Player> challengers = velorum.arena() == null ? List.of() : velorum.arena().challengers();
		for (VelorumDevoteeEntity devotee : awake) {
			if (!devotee.awake()) continue;
			if (!devotee.hostile()) {
				if (devotee.distanceTo(velorum) <= VelorumCongregationRules.GRIP_RANGE + velorum.getBbWidth() * 0.5D) grippers++;
				for (Player player : challengers) {
					if (player.distanceTo(devotee) <= VelorumCongregationRules.SHELTER_RANGE) sheltered.add(player.getUUID());
					if (player.distanceTo(devotee) <= VelorumDarkRules.WARMTH) warmed.add(player.getUUID());
				}
			}
		}
		if (VelorumDeedRules.beholding(velorum.deeds().mask())) {
			for (Player player : challengers) {
				for (Figure figure : figures.values()) {
					if (figure.state != VelorumCongregationRules.State.FROZEN && figure.state != VelorumCongregationRules.State.PINNED) continue;
					Vec3 face = figure.face();
					Direction look = figure.facing;
					double length = VelorumDeedRules.gazeLength(velorum.dark().inDark(player));
					if (!VelorumDeedRules.inGaze(face, look.getStepX(), look.getStepZ(), player.position(), length)) continue;
					Vec3 eye = face.add(look.getStepX() * 0.7D, 0.0D, look.getStepZ() * 0.7D);
					if (!clear(level, eye, player.getEyePosition(), velorum)) continue;
					watched.add(player.getUUID());
					break;
				}
			}
		}
		for (Figure figure : figures.values()) {
			if (figure.fed > 0.0F) {
				figure.idle++;
				figure.fed = VelorumCongregationRules.feed(figure.fed, false, figure.idle);
			}
		}
	}

	public boolean shelters(Player player) {
		return sheltered.contains(player.getUUID());
	}

	/** Within 1.5 blocks of an awake ally: their thawed blood is warm, and the dark does not hold there. */
	public boolean warms(Player player) {
		return warmed.contains(player.getUUID());
	}

	// ── the congregation as his source ───────────────────────────────

	/** Where a figure stands now: its block's foot, or the stilled devotee standing in its place. */
	private Vec3 standing(VelorumEntity velorum, Figure figure) {
		if (figure.devotee != null && velorum.level() instanceof ServerLevel server
				&& server.getEntity(figure.devotee) instanceof VelorumDevoteeEntity devotee && devotee.isAlive()) {
			return devotee.position();
		}
		return Vec3.atBottomCenterOf(figure.base);
	}

	/**
	 * The figures that feed Veins of Frost near {@code around}: frozen and stilled, within {@code reach}, nearest
	 * first, at most four. Pinned figures (the hyphae shut their veins) and awake ones never feed.
	 */
	public List<Vec3> feeding(VelorumEntity velorum, Vec3 around, double reach) {
		List<Vec3> feeding = new ArrayList<>();
		for (Figure figure : figures.values()) {
			if (!VelorumCongregationRules.feedsVeins(figure.state)) continue;
			Vec3 at = standing(velorum, figure);
			if (Math.hypot(at.x - around.x, at.z - around.z) <= reach) feeding.add(at);
		}
		feeding.sort(Comparator.comparingDouble(at -> Math.hypot(at.x - around.x, at.z - around.z)));
		return feeding.size() > VelorumCongregationRules.MAX_VEINS
				? List.copyOf(feeding.subList(0, VelorumCongregationRules.MAX_VEINS)) : feeding;
	}

	/** Every frozen or stilled figure where it stands: the Shadow Step rises beside one of them. */
	public List<Vec3> standingFigures(VelorumEntity velorum) {
		List<Vec3> standing = new ArrayList<>();
		for (Figure figure : figures.values()) {
			if (VelorumCongregationRules.feedsVeins(figure.state)) standing.add(standing(velorum, figure));
		}
		return standing;
	}

	/** The figures' veins flare black-red through their rime as he draws on them. */
	public void flareVeins(VelorumEntity velorum, List<Vec3> feeding) {
		for (Vec3 at : feeding) VelorumEffects.veinFlare(velorum, BlockPos.containing(at));
		if (!feeding.isEmpty()) VelorumMoves.sound(velorum, feeding.get(0), SoundEvents.POWDER_SNOW_BREAK, 1.0F, 0.5F);
	}

	public boolean watches(Player player) {
		return watched.contains(player.getUUID());
	}

	/** Two or more allies within reach of him: he cannot glide, and turns at half his rate. */
	public boolean gripping() {
		return grippers >= VelorumCongregationRules.GRIP_COUNT;
	}

	/** The faces of the figures watching a challenger now (the Beholding deed's glints). */
	public List<Vec3> watchingFaces() {
		List<Vec3> faces = new ArrayList<>();
		for (Figure figure : figures.values()) {
			if (figure.state == VelorumCongregationRules.State.FROZEN || figure.state == VelorumCongregationRules.State.PINNED) {
				faces.add(figure.face());
			}
		}
		return faces;
	}

	// ── held Projection on a figure ──────────────────────────────────

	/**
	 * A held Projection aimed at a figure (either cell) or a stilled devotee: wakes it in a proper fight, pins it in a
	 * forced one, paying the lesser of the rule's rate and the item's through its named source. PASS when it aims at
	 * neither.
	 */
	public SaintChannelRouter.Result project(VelorumEntity velorum, SaintChannelRouter.Request request) {
		ServerLevel level = (ServerLevel) velorum.level();
		Figure figure = aimedFigure(request);
		VelorumDevoteeEntity stilled = figure == null ? aimedStilled(velorum, request) : null;
		if (figure == null && stilled == null) return SaintChannelRouter.Result.PASS;
		Player player = request.player();
		boolean forced = velorum.forced();
		if (stilled != null) {
			if (forced) return SaintChannelRouter.Result.PASS;
			figure = figureOf(stilled);
			if (figure == null) return SaintChannelRouter.Result.PASS;
			if (figure.state == VelorumCongregationRules.State.AWAKE) figure.state = VelorumCongregationRules.State.STILLED;
		}
		long now = level.getGameTime();
		if (forced) {
			if (figure.state != VelorumCongregationRules.State.FROZEN) return SaintChannelRouter.Result.PASS;
			SaintLines.teach(player, "message.hemomancy.velorum.hint.forced_figure");
			double paid = pay(request, VelorumCongregationRules.PIN_RATE, VelorumMoves.PINNING);
			if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
			figure.fed = VelorumCongregationRules.feed(figure.fed, true, 0);
			figure.idle = 0;
			VelorumEffects.pinning(velorum, figure.base, player);
			if (figure.fed >= VelorumCongregationRules.PIN_TICKS) {
				figure.state = VelorumCongregationRules.State.PINNED;
				figure.fed = 0.0F;
				VelorumMoves.sound(velorum, Vec3.atCenterOf(figure.base), SoundInit.RITE_SEAL.get(), 1.0F, 0.6F);
			}
			return SaintChannelRouter.Result.taken(paid);
		}
		SaintLines.teach(player, "message.hemomancy.velorum.hint.proper_figure");
		if (!VelorumCongregationRules.wakeable(figure.state, now, figure.lockoutUntil)) {
			return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.figure_resting"));
		}
		if (allies(velorum) >= VelorumCongregationRules.cap(velorum.arena().challengerCount())) {
			return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.figure_cap"));
		}
		double paid = pay(request, VelorumCongregationRules.WAKE_RATE, VelorumMoves.WAKING);
		if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
		figure.fed = VelorumCongregationRules.feed(figure.fed, true, 0);
		figure.idle = 0;
		if ((int) figure.fed % 10 == 1) VelorumMoves.sound(velorum, Vec3.atCenterOf(figure.base), SoundInit.RITE_ANCHOR_FILL.get(), 0.8F, 0.7F);
		VelorumEffects.waking(velorum, figure.base, figure.fed / VelorumCongregationRules.wakeTicks(figure.recalled));
		if (figure.fed >= VelorumCongregationRules.wakeTicks(figure.recalled)) {
			figure.fed = 0.0F;
			if (stilled != null) {
				stilled.rewake();
				figure.state = VelorumCongregationRules.State.AWAKE;
			} else {
				wake(velorum, figure, false);
			}
			VelorumMoves.sound(velorum, Vec3.atCenterOf(figure.base), SoundEvents.GLASS_BREAK, 1.0F, 0.6F);
		}
		return SaintChannelRouter.Result.taken(paid);
	}

	private static double pay(SaintChannelRouter.Request request, float rate, com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting.Source source) {
		double amount = Math.min(rate, request.ratePerTick());
		if (amount <= 0.0D) return 0.0D;
		return SaintChannelRouter.pay(request, amount, source).paid();
	}

	@Nullable
	private Figure aimedFigure(SaintChannelRouter.Request request) {
		if (request.block() == null) return null;
		BlockPos hit = request.block().getBlockPos();
		Figure figure = figures.get(hit.asLong());
		if (figure == null) figure = figures.get(hit.below().asLong());
		if (figure == null || figure.state == VelorumCongregationRules.State.AWAKE) return null;
		if (figure.state == VelorumCongregationRules.State.STILLED) return null;
		return request.block().getLocation().distanceTo(Vec3.atCenterOf(figure.base)) <= FIGURE_REACH + 1.0D ? figure : null;
	}

	@Nullable
	private VelorumDevoteeEntity aimedStilled(VelorumEntity velorum, SaintChannelRouter.Request request) {
		if (request.entity() == null) return null;
		Entity entity = request.entity().getEntity();
		if (entity instanceof VelorumDevoteeEntity devotee && devotee.state() == VelorumDevoteeEntity.State.STILLED
				&& devotee.saintId().equals(velorum.getUUID())) return devotee;
		return null;
	}

	@Nullable
	private Figure figureOf(VelorumDevoteeEntity devotee) {
		for (Figure figure : figures.values()) if (devotee.getUUID().equals(figure.devotee)) return figure;
		return null;
	}

	// ── waking and rising ────────────────────────────────────────────

	/** The figure's block taken up (upper cell first, no drops) and a devotee in its pose in its place. */
	private void wake(VelorumEntity velorum, Figure figure, boolean hostile) {
		SaintArena arena = velorum.arena();
		if (arena == null) return;
		arena.clearColumn(figure.base, 2);
		VelorumDevoteeEntity devotee = VelorumDevoteeEntity.wake((ServerLevel) velorum.level(), velorum, figure.base,
				figure.facing, figure.pose, hostile);
		arena.own(devotee, figure.base);
		figure.devotee = devotee.getUUID();
		figure.state = VelorumCongregationRules.State.AWAKE;
		VelorumEffects.thaw(velorum, figure.base);
		if (!hostile) {
			// warm blood into frozen cruor: the veins run bright as the rime sheds
			VelorumEffects.veinFlare(velorum, figure.base);
			velorum.firstWoken();
		}
	}

	public int allies(VelorumEntity velorum) {
		int count = 0;
		for (VelorumDevoteeEntity devotee : devotees(velorum)) {
			if (!devotee.hostile() && devotee.state() != VelorumDevoteeEntity.State.STILLED) count++;
		}
		return count;
	}

	public int hostiles(VelorumEntity velorum) {
		int count = 0;
		for (VelorumDevoteeEntity devotee : devotees(velorum)) {
			if (devotee.hostile() && devotee.state() != VelorumDevoteeEntity.State.STILLED) count++;
		}
		return count;
	}

	/** Whether a forced fight has a frozen, unpinned figure to raise and room for one more hostile. */
	public boolean canRaise(VelorumEntity velorum) {
		if (velorum.arena() == null) return false;
		if (hostiles(velorum) >= VelorumCongregationRules.cap(velorum.arena().challengerCount())) return false;
		for (Figure figure : figures.values()) if (figure.state == VelorumCongregationRules.State.FROZEN) return true;
		return false;
	}

	/** Raises up to {@code count} of the unpinned frozen figures nearest any challenger against the challengers. */
	public void raise(VelorumEntity velorum, int count) {
		SaintArena arena = velorum.arena();
		if (arena == null) return;
		List<Player> challengers = arena.challengers();
		List<Figure> frozen = new ArrayList<>();
		for (Figure figure : figures.values()) if (figure.state == VelorumCongregationRules.State.FROZEN) frozen.add(figure);
		frozen.sort(Comparator.comparingDouble(figure -> nearest(challengers, Vec3.atCenterOf(figure.base))));
		int room = VelorumCongregationRules.cap(arena.challengerCount()) - hostiles(velorum);
		for (int i = 0; i < Math.min(count, Math.min(room, frozen.size())); i++) wake(velorum, frozen.get(i), true);
	}

	/** A forced fight's floor: two rise, once per floor. */
	public void floorRise(VelorumEntity velorum, int floor) {
		if (!velorum.forced() || floor < risenAtFloor) return;
		risenAtFloor = floor + 1;
		raise(velorum, VelorumCongregationRules.RISES_PER_FLOOR);
	}

	// ── Recall ───────────────────────────────────────────────────────

	/** The awake ally Recall would reach: within 14 blocks and in his sight, nearest first. */
	@Nullable
	public VelorumDevoteeEntity recallable(VelorumEntity velorum) {
		VelorumDevoteeEntity best = null;
		double bestDistance = Double.MAX_VALUE;
		for (VelorumDevoteeEntity devotee : devotees(velorum)) {
			if (devotee.hostile() || !devotee.awake()) continue;
			double distance = devotee.distanceTo(velorum);
			if (distance > VelorumCongregationRules.RECALL_RANGE || !velorum.hasLineOfSight(devotee)) continue;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = devotee;
			}
		}
		return best;
	}

	/** The recalled ally falls still where it stands; it can be rewoken there after 300 ticks, at double cost. */
	public void recall(VelorumEntity velorum, VelorumDevoteeEntity devotee) {
		devotee.fallStill();
		Figure figure = figureOf(devotee);
		if (figure == null) return;
		figure.state = VelorumCongregationRules.State.STILLED;
		figure.recalled = true;
		figure.lockoutUntil = velorum.level().getGameTime() + VelorumCongregationRules.RECALL_LOCKOUT;
	}

	/** A devotee brought to no health fell still: its record follows. */
	public void stilled(VelorumDevoteeEntity devotee) {
		Figure figure = figureOf(devotee);
		if (figure != null && figure.state == VelorumCongregationRules.State.AWAKE) figure.state = VelorumCongregationRules.State.STILLED;
	}

	// ── lights ───────────────────────────────────────────────────────

	/** The Long Night: the rank-pillar rime lanterns go dark (ice without light) and come back after {@code ticks}. */
	public void darkenPillars(VelorumEntity velorum, int ticks) {
		darken(velorum, pillarLanterns, ticks);
	}

	/** The Endless Hour: every rime lantern in the chamber goes dark for its hold. The sea lanterns stay lit. */
	public void darkenAll(VelorumEntity velorum, int ticks) {
		darken(velorum, lanterns, ticks);
	}

	private static void darken(VelorumEntity velorum, List<BlockPos> positions, int ticks) {
		SaintArena arena = velorum.arena();
		if (arena == null) return;
		BlockState dark = Blocks.PACKED_ICE.defaultBlockState();
		for (BlockPos pos : positions) arena.swapFor(pos, dark, ticks);
	}

	/** The ending: every dimmed lantern comes back now. */
	public void relight(VelorumEntity velorum) {
		SaintArena arena = velorum.arena();
		if (arena == null) return;
		for (BlockPos pos : lanterns) arena.revert(pos);
	}

	/** The Eclipse: every rime lantern in the chamber goes out, the votive stands and the crown lamps included. */
	public void darkenEverything(VelorumEntity velorum, int ticks) {
		darken(velorum, everyLantern, ticks);
	}

	/** The Eclipse relit or ended: every lantern it put out comes back. */
	public void relightEverything(VelorumEntity velorum) {
		SaintArena arena = velorum.arena();
		if (arena == null) return;
		for (BlockPos pos : everyLantern) arena.revert(pos);
	}

	public int lanternCount() {
		return everyLantern.size();
	}

	// ── the Eclipse's shapes ─────────────────────────────────────────

	/** A figure's umbral shape through the Eclipse's hold, backed by an owned hazard. */
	private static final class Shape {
		final Vec3 at;
		final boolean held;
		@Nullable
		UUID hazard;
		@Nullable
		UUID quarry;
		@Nullable
		UUID grip;
		int tellLeft;
		int gripTicks;
		int restLeft;

		Shape(Vec3 at, boolean held) {
			this.at = at;
			this.held = held;
		}
	}

	/** Out of every frozen, stilled and pinned figure a shape of liquid dark stands up for {@code holdTicks}. */
	public void beginEclipse(VelorumEntity velorum, int holdTicks) {
		shapes.clear();
		SaintArena arena = velorum.arena();
		if (arena == null || !(velorum.level() instanceof ServerLevel server)) return;
		double reach = VelorumEclipseRules.reach(VelorumDeedRules.beholding(velorum.deeds().mask()));
		for (Figure figure : figures.values()) {
			VelorumEclipseRules.Shape kind = VelorumEclipseRules.shape(figure.state);
			if (kind == VelorumEclipseRules.Shape.NONE) continue;
			Vec3 at = standing(velorum, figure);
			Vec3 foot = new Vec3(at.x, arena.floorY(), at.z);
			Shape shape = new Shape(foot, kind == VelorumEclipseRules.Shape.HELD);
			BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum,
					shape.held ? VelorumEntity.HAZARD_ECLIPSE_SHAPE_HELD : VelorumEntity.HAZARD_ECLIPSE_SHAPE,
					new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, reach, BossFootprint.Height.STANDING),
					hazardFrame(arena, foot, foot), holdTicks, holdTicks + 1);
			arena.own(hazard, null);
			shape.hazard = hazard.getUUID();
			shapes.add(shape);
		}
	}

	/**
	 * One tick of the hold, {@code holdAge} ticks in: a risen shape within reach of a challenger reaches for them (a
	 * 16-tick tell, then a grip of at most 30 that a walk of 6 from the figure tears free of), rests 60 after; and at
	 * 40, 90 and 140 the three shapes nearest each challenger throw their arms along the floor at them.
	 */
	public void tickEclipse(VelorumEntity velorum, int holdAge) {
		SaintArena arena = velorum.arena();
		if (arena == null || !(velorum.level() instanceof ServerLevel server)) return;
		List<Player> challengers = arena.challengers();
		double reach = VelorumEclipseRules.reach(VelorumDeedRules.beholding(velorum.deeds().mask()));
		for (Shape shape : shapes) {
			if (shape.held) continue;
			if (shape.restLeft > 0) shape.restLeft--;
			if (shape.quarry != null) {
				Player quarry = find(challengers, shape.quarry);
				double distance = quarry == null ? Double.MAX_VALUE : Math.hypot(quarry.getX() - shape.at.x, quarry.getZ() - shape.at.z);
				if (shape.tellLeft > 0) {
					if (--shape.tellLeft > 0) continue;
					if (quarry != null && distance <= VelorumEclipseRules.GRIP_RANGE) {
						VelorumMoves.hurt(velorum, quarry, VelorumEclipseRules.GRIP_DAMAGE);
						VelorumMoves.sound(velorum, quarry.position(), SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.0F, 0.7F);
						shape.gripTicks = 0;
						continue;
					}
				} else if (quarry != null && VelorumEclipseRules.gripHolds(distance, shape.gripTicks)) {
					shape.gripTicks++;
					VelorumMoves.slow(quarry, 5, VelorumMoves.SLOWNESS_II);
					velorum.stillness().add(velorum, quarry, VelorumEclipseRules.GRIP_FILL);
					continue;
				}
				shape.quarry = null;
				shape.restLeft = VelorumEclipseRules.SHAPE_REST;
				letGo(server, shape, VelorumEclipseRules.ARM_HOLD);
				continue;
			}
			if (holdAge < VelorumEclipseRules.SHAPE_RISE || shape.restLeft > 0) continue;
			Player nearest = null;
			double best = reach;
			for (Player player : challengers) {
				if (reached(player)) continue;
				double distance = Math.hypot(player.getX() - shape.at.x, player.getZ() - shape.at.z);
				if (distance <= best) {
					best = distance;
					nearest = player;
				}
			}
			if (nearest == null) continue;
			shape.quarry = nearest.getUUID();
			shape.tellLeft = VelorumEclipseRules.REACH_TELL;
			shape.grip = reachFor(velorum, arena, server, shape, nearest);
			VelorumEffects.shapeReach(velorum, shape.at, nearest);
			VelorumMoves.sound(velorum, shape.at, SoundInit.ENTITY_WILL_MATERIALIZE.get(), 0.8F, 0.4F);
		}
		if (VelorumEclipseRules.revealAt(holdAge) >= 0) reachTogether(velorum, arena, server, challengers);
	}

	/** The three reaching shapes nearest each challenger throw their arms along the floor at where they stand now. */
	private void reachTogether(VelorumEntity velorum, SaintArena arena, ServerLevel server, List<Player> challengers) {
		int life = VelorumEclipseRules.TOGETHER_REVEAL
				+ (int) Math.ceil(VelorumEclipseRules.ARM_LENGTH / VelorumEclipseRules.ARM_SPEED) + VelorumEclipseRules.ARM_HOLD;
		BossFootprint arm = new BossFootprint.Union(List.of(new BossFootprint.Union.Part(new BossFootprint.Lane(
				VelorumEclipseRules.ARM_LENGTH, VelorumEclipseRules.ARM_HALF_WIDTH, 0, 0.0D, 0.0D,
				VelorumEclipseRules.ARM_SPEED, 0, BossFootprint.Height.JUMPABLE), VelorumEclipseRules.TOGETHER_REVEAL)));
		for (Player player : challengers) {
			List<Shape> nearest = new ArrayList<>();
			for (Shape shape : shapes) if (!shape.held) nearest.add(shape);
			nearest.sort(Comparator.comparingDouble(shape -> Math.hypot(player.getX() - shape.at.x, player.getZ() - shape.at.z)));
			Vec3 locked = new Vec3(player.getX(), arena.floorY(), player.getZ());
			for (int i = 0; i < Math.min(VelorumEclipseRules.TOGETHER_SHAPES, nearest.size()); i++) {
				Shape shape = nearest.get(i);
				BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum, VelorumEntity.HAZARD_ECLIPSE_REACH,
						arm, hazardFrame(arena, shape.at, locked), life, 1);
				arena.own(hazard, null);
			}
		}
	}

	/** The shapes sink back into their figures. */
	public void endEclipse(VelorumEntity velorum, int fade) {
		if (velorum.level() instanceof ServerLevel server) {
			for (Shape shape : shapes) {
				if (shape.hazard != null && server.getEntity(shape.hazard) instanceof BossGroundHazardEntity hazard) hazard.expire(fade);
				letGo(server, shape, fade);
			}
		}
		shapes.clear();
	}

	/** A shape's arm stretched from its figure's foot toward the challenger for its tell and grip. */
	private static UUID reachFor(VelorumEntity velorum, SaintArena arena, ServerLevel server, Shape shape, Player quarry) {
		Vec3 at = new Vec3(quarry.getX(), arena.floorY(), quarry.getZ());
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, velorum, VelorumEntity.HAZARD_ECLIPSE_REACH,
				new BossFootprint.Tether(BossFootprint.End.BODY, BossFootprint.End.TARGET, 0, VelorumEclipseRules.ARM_HALF_WIDTH,
						VelorumEclipseRules.GRIP_RANGE, BossFootprint.Height.STANDING), hazardFrame(arena, shape.at, at),
				VelorumEclipseRules.REACH_TELL + VelorumEclipseRules.GRIP_TICKS + 1,
				VelorumEclipseRules.REACH_TELL + VelorumEclipseRules.GRIP_TICKS + 2);
		arena.own(hazard, null);
		return hazard.getUUID();
	}

	private static void letGo(ServerLevel server, Shape shape, int fade) {
		if (shape.grip != null && server.getEntity(shape.grip) instanceof BossGroundHazardEntity hazard) hazard.expire(fade);
		shape.grip = null;
	}

	/** Whether a shape's grip (past its tell) holds this challenger. */
	public boolean eclipseGripped(Player player) {
		for (Shape shape : shapes) {
			if (player.getUUID().equals(shape.quarry) && shape.tellLeft <= 0) return true;
		}
		return false;
	}

	public int shapes() {
		return shapes.size();
	}

	/** A shape grips one challenger at a time, and one challenger is reached for by one shape at a time. */
	private boolean reached(Player player) {
		for (Shape shape : shapes) if (player.getUUID().equals(shape.quarry)) return true;
		return false;
	}

	@Nullable
	private static Player find(List<Player> players, UUID id) {
		for (Player player : players) if (player.getUUID().equals(id)) return player;
		return null;
	}

	private static FootprintFrame hazardFrame(SaintArena arena, Vec3 origin, Vec3 aim) {
		return new FootprintFrame(origin, aim, aim, arena.floorY(), List.of(), LongSets.EMPTY_SET, List.of(),
				arena.bossFloor(), arena.chamber());
	}

	// ── the ending ───────────────────────────────────────────────────

	/** Every awake or stilled devotee, ally or hostile, stops, turns toward him, and walks home. */
	public void sendHome(VelorumEntity velorum) {
		for (VelorumDevoteeEntity devotee : devotees(velorum)) devotee.sendHome();
	}

	/** A devotee knelt in its place: its block comes back and the devotee goes. */
	public void arrived(VelorumEntity velorum, VelorumDevoteeEntity devotee) {
		SaintArena arena = velorum.arena();
		BlockPos home = devotee.home();
		if (arena != null && home != null) {
			arena.revert(home);
			arena.revert(home.above());
			arena.disown(devotee);
		}
		Figure figure = figureOf(devotee);
		if (figure != null) {
			figure.state = VelorumCongregationRules.State.HOME;
			figure.devotee = null;
		}
		VelorumEffects.kneelHome(velorum, devotee.position());
		devotee.discard();
	}

	public boolean everyoneHome(VelorumEntity velorum) {
		return devotees(velorum).isEmpty();
	}

	/** The devotees still standing where they were at the ending's cap: a small rime burst where each stood. */
	public void burstStragglers(VelorumEntity velorum) {
		for (VelorumDevoteeEntity devotee : devotees(velorum)) VelorumEffects.kneelHome(velorum, devotee.position());
	}

	// ── helpers ──────────────────────────────────────────────────────

	/** Every live devotee the arena owns for this Saint. */
	public List<VelorumDevoteeEntity> devotees(VelorumEntity velorum) {
		List<VelorumDevoteeEntity> devotees = new ArrayList<>();
		SaintArena arena = velorum.arena();
		if (arena == null) return devotees;
		for (Entity entity : arena.owned()) {
			if (entity instanceof VelorumDevoteeEntity devotee && devotee.isAlive()) devotees.add(devotee);
		}
		return devotees;
	}

	public SaintCompanion.Allegiance allegiance(VelorumEntity velorum) {
		return VelorumCongregationRules.allegiance(velorum.awakening());
	}

	private static double nearest(List<Player> players, Vec3 point) {
		double best = Double.MAX_VALUE;
		for (Player player : players) best = Math.min(best, player.position().distanceToSqr(point));
		return best;
	}

	private static boolean clear(ServerLevel level, Vec3 from, Vec3 to, Entity source) {
		return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source))
				.getType() == HitResult.Type.MISS;
	}
}
