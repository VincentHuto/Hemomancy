package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomFigures;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShow;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules;
import com.vincenthuto.hemomancy.client.render.world.circus.ShowMarks;
import com.vincenthuto.hemomancy.client.render.world.circus.StringLights;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.TroupePoleBlock;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Circus perception presentation (D-29; the phantom show of 2026-11-16): a newcomer catches unsettling fragments,
 * an attuned visitor sits in the audience of the whole performance. It holds the synced acclimation and inside flag,
 * reads the pavilion's own marks round the player every two seconds, eases between stages, and hands the world pass
 * to {@link PhantomShow}. Client-only: it decides no collision, damage or route.
 */
public final class CircusPerceptionOverlay {
	/** FX-28: a near-black edge, so the vignette still shows against the pavilion's red. */
	private static final int VIGNETTE = 0x0C0709;
	private static final ParticleColor BONE = new ParticleColor(232, 218, 192);
	private static final ParticleColor EMBER = new ParticleColor(255, 200, 140);
	private static final ParticleColor CRIMSON_SPORE = new ParticleColor(150, 40, 58);
	/** The drawn-in glow's mean life in ticks, so it covers the ground the old drift did. */
	private static final double DRAWN_IN_TICKS = 22.0D;
	private static final int SCAN_RADIUS = 24;
	private static final int SCAN_TICKS = 40;
	/** Once the ring is found: the read covers the whole 65-block big top round it, and is redone this often. */
	private static final int ANCHORED_RADIUS = 34;
	private static final int RESCAN_TICKS = 200;
	private static final double ANCHOR_REACH = 40.0D;
	private static final double CUE_RANGE = 28.0D;
	/** Ticks a stage change takes to ease in. */
	private static final float STAGE_EASE = 40.0F;
	private static int acclimation;
	private static boolean active;
	private static boolean ruined;
	private static ShowMarks marks = ShowMarks.EMPTY;
	private static List<StringLights.Run> runs = List.of();
	private static List<AABB> creatures = List.of();
	private static long scannedAt;
	private static BlockPos scannedFrom;
	private static PhantomShowRules.Look shownFrom = PhantomShowRules.look(CircusProgressRules.Stage.UNAWARE, false);
	private static PhantomShowRules.Look shownTo = shownFrom;
	private static float changedAt = -STAGE_EASE;

	private CircusPerceptionOverlay() {
	}

	public static void setState(int score, boolean inside) {
		setState(score, inside, false);
	}

	public static void setState(int score, boolean inside, boolean ruinedSite) {
		acclimation = CircusProgressRules.clamp(score);
		active = inside;
		ruined = inside && ruinedSite;
	}

	public static void clear() {
		acclimation = 0;
		active = false;
		ruined = false;
		marks = ShowMarks.EMPTY;
		runs = List.of();
		creatures = List.of();
		scannedFrom = null;
		shownFrom = shownTo = PhantomShowRules.look(CircusProgressRules.Stage.UNAWARE, false);
		changedAt = -STAGE_EASE;
		PhantomFigures.clear();
	}

	public static CircusProgressRules.Stage stage() {
		return CircusProgressRules.stage(acclimation);
	}

	public static boolean isActive() {
		return active;
	}

	public static boolean isRuined() {
		return ruined;
	}

	public static ShowMarks marks() {
		return marks;
	}

	/** Whether the show is up: inside, past Unaware, and with a ring in the marks. */
	public static boolean showing() {
		return active && stage().hasPresentation() && marks.hasRing();
	}

	/**
	 * The show this frame: the look it is leaving, the look it is going to, and how far (eased) over
	 * {@link #STAGE_EASE} ticks. The casts of both are drawn, weighted, so nothing pops at a stage change.
	 */
	public record Frame(PhantomShowRules.Look from, PhantomShowRules.Look to, float t) {
		public PhantomShowRules.Look blended() {
			return PhantomShowRules.blend(from, to, t);
		}
	}

	/** Reads {@link AnimationTime#partialTick()} itself, so the show holds still under {@code /tick freeze}. */
	public static Frame frame() {
		Minecraft minecraft = Minecraft.getInstance();
		float now = minecraft.level == null ? 0.0F : AnimationTime.gameTime(minecraft.level, AnimationTime.partialTick());
		PhantomShowRules.Look target = PhantomShowRules.look(active ? stage() : CircusProgressRules.Stage.UNAWARE, ruined);
		if (!target.equals(shownTo)) {
			shownFrom = PhantomShowRules.blend(shownFrom, shownTo, eased(now));
			shownTo = target;
			changedAt = now;
		}
		return new Frame(shownFrom, shownTo, eased(now));
	}

	public static PhantomShowRules.Look look() {
		return frame().blended();
	}

	private static float eased(float now) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((now - changedAt) / STAGE_EASE));
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		CircusProgressRules.Stage stage = stage();
		if (!active || !stage.hasPresentation() || minecraft.level == null || minecraft.player == null) {
			creatures = List.of();
			return;
		}
		scanMarks(minecraft);
		gatherCreatures(minecraft);
		int interval = stage.particleIntervalTicks();
		if (minecraft.player.tickCount % interval != 0) return;
		var random = minecraft.level.random;
		double radius = 2.0D + random.nextDouble() * 3.0D;
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double x = minecraft.player.getX() + Math.cos(angle) * radius;
		double y = minecraft.player.getY() + 0.2D + random.nextDouble() * 2.4D;
		double z = minecraft.player.getZ() + Math.sin(angle) * radius;
		CircusProgressRules.Stage shown = ruined ? CircusProgressRules.Stage.DISTURBED : stage;
		switch (shown) {
			case DISTURBED -> minecraft.level.addParticle(HemoParticleData.sporiticSpore(CRIMSON_SPORE),
					x, y, z, (random.nextDouble() - 0.5D) * 0.03D, 0.01D, (random.nextDouble() - 0.5D) * 0.03D);
			// A drawn-in glow flies from spawn plus velocity to spawn, so the drift is handed over reversed.
			case ACCLIMATING -> {
				double dx = (minecraft.player.getX() - x) * 0.01D, dz = (minecraft.player.getZ() - z) * 0.01D;
				minecraft.level.addParticle(HemoParticleData.willAbsorptionGlow(BONE),
						x + dx * DRAWN_IN_TICKS, y + 0.01D * DRAWN_IN_TICKS, z + dz * DRAWN_IN_TICKS,
						-dx * DRAWN_IN_TICKS, -0.01D * DRAWN_IN_TICKS, -dz * DRAWN_IN_TICKS);
			}
			// Attuned: warm embers drift down from a lit bulb overhead.
			case ATTUNED -> {
				if (runs.isEmpty()) break;
				var run = runs.get(random.nextInt(runs.size()));
				Vec3 bulb = run.points().get(random.nextInt(run.points().size()));
				if (bulb.distanceTo(minecraft.player.position()) > CUE_RANGE) break;
				minecraft.level.addParticle(HemoParticleData.emberSpark(EMBER, 0.026F, 0.6F, 50),
						bulb.x, bulb.y - 0.1D, bulb.z, (random.nextDouble() - 0.5D) * 0.01D, -0.012D,
						(random.nextDouble() - 0.5D) * 0.01D);
			}
			default -> throw new IllegalStateException("Unaware Circus presentation cannot emit particles");
		}
		if (minecraft.player.tickCount % (interval * 12) == 0) {
			var sound = switch (shown) {
				case DISTURBED -> SoundEvents.AMBIENT_CAVE.value();
				case ACCLIMATING -> SoundEvents.ENCHANTMENT_TABLE_USE;
				case ATTUNED -> SoundEvents.AMETHYST_BLOCK_CHIME;
				default -> throw new IllegalStateException();
			};
			minecraft.level.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, 0.28F,
					0.85F + random.nextFloat() * 0.3F, false);
		}
	}

	/** A soft theatre vignette that widens as the house goes dark. */
	public static void render(GuiGraphics graphics) {
		if (!showing()) return;
		float strength = look().vignette();
		int alpha = (int) (strength * 150.0F);
		if (alpha <= 0) return;
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int edge = (int) (Math.min(width, height) * (0.12F + strength * 0.2F));
		int color = alpha << 24 | VIGNETTE;
		graphics.fillGradient(0, 0, width, edge, color, VIGNETTE);
		graphics.fillGradient(0, height - edge, width, height, VIGNETTE, color);
		for (int x = 0; x < edge; x++) {
			float fade = 1.0F - x / (float) edge;
			int side = (int) (alpha * fade * fade) << 24 | VIGNETTE;
			graphics.fill(x, 0, x + 1, height, side);
			graphics.fill(width - x - 1, 0, width - x, height, side);
		}
	}

	/** The phantom show, from the world pass. */
	public static void renderWorld(PoseStack poseStack, float partialTick) {
		if (!showing()) return;
		PhantomShow.render(poseStack, AnimationTime.partialTick(), marks, runs, frame(), creatures);
	}

	/** Living things near the player other than the player, so no phantom stands over one. */
	private static void gatherCreatures(Minecraft minecraft) {
		List<AABB> found = new ArrayList<>();
		for (LivingEntity living : minecraft.level.getEntitiesOfClass(LivingEntity.class,
				minecraft.player.getBoundingBox().inflate(CUE_RANGE), living -> living != minecraft.player && living.isAlive()))
			found.add(living.getBoundingBox());
		creatures = found;
	}

	/**
	 * Reads the pavilion's marks. The first read is round the player; once the ring is found the read is anchored on
	 * the ring and covers the whole big top, so walking about never changes which seats, lights or trapezes there are.
	 * It is read again only every ten seconds (a block may have changed) or when the player is far from the ring.
	 */
	private static void scanMarks(Minecraft minecraft) {
		BlockPos player = minecraft.player.blockPosition();
		long now = minecraft.level.getGameTime();
		boolean anchored = marks.hasRing()
				&& marks.ring().centre().distanceTo(minecraft.player.position()) < ANCHOR_REACH;
		if (anchored && now - scannedAt < RESCAN_TICKS && now >= scannedAt) return;
		if (!anchored && now - scannedAt < SCAN_TICKS && scannedFrom != null && scannedFrom.closerThan(player, 6.0D)
				&& now >= scannedAt) return;
		BlockPos centre = anchored ? BlockPos.containing(marks.ring().centre()) : player;
		int reach = anchored ? ANCHORED_RADIUS : SCAN_RADIUS;
		int below = anchored ? 3 : 8;
		scannedAt = now;
		scannedFrom = player;
		List<ShowMarks.Seat> seats = new ArrayList<>();
		List<BlockPos> trapezes = new ArrayList<>();
		List<BlockPos> curb = new ArrayList<>();
		List<BlockPos> rails = new ArrayList<>();
		Map<Long, BlockPos> collars = new HashMap<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minY = centre.getY() - below, maxY = centre.getY() + 24;
		for (int cx = (centre.getX() - reach) >> 4; cx <= (centre.getX() + reach) >> 4; cx++)
			for (int cz = (centre.getZ() - reach) >> 4; cz <= (centre.getZ() + reach) >> 4; cz++) {
				if (!minecraft.level.hasChunk(cx, cz)) continue;
				LevelChunk chunk = minecraft.level.getChunk(cx, cz);
				for (int sy = minY >> 4; sy <= maxY >> 4; sy++) {
					int index = chunk.getSectionIndexFromSectionY(sy);
					if (index < 0 || index >= chunk.getSectionsCount()) continue;
					LevelChunkSection section = chunk.getSection(index);
					if (section.hasOnlyAir()) continue;
					for (int ly = 0; ly < 16; ly++) {
						int y = (sy << 4) + ly;
						if (y < minY || y > maxY) continue;
						for (int lx = 0; lx < 16; lx++) {
							int x = (cx << 4) + lx;
							if (Math.abs(x - centre.getX()) > reach) continue;
							for (int lz = 0; lz < 16; lz++) {
								int z = (cz << 4) + lz;
								if (Math.abs(z - centre.getZ()) > reach) continue;
								BlockState state = section.getBlockState(lx, ly, lz);
								if (state.isAir()) continue;
								readMark(minecraft, cursor.set(x, y, z), state, seats, trapezes, curb, rails, collars);
							}
						}
					}
				}
			}
		Comparator<BlockPos> near = Comparator.comparingDouble(p -> p.distSqr(player));
		seats.sort(Comparator.comparingDouble(seat -> seat.pos().distSqr(player)));
		List<Vec3> poleTops = collars.values().stream().sorted(near).map(p -> Vec3.atCenterOf(p).add(0.0D, 0.5D, 0.0D))
				.toList();
		marks = new ShowMarks(seats, ShowMarks.stableOrder(trapezes), poleTops, railLines(minecraft, rails),
				ShowMarks.ring(curb));
		runs = StringLights.runs(marks);
	}

	private static void readMark(Minecraft minecraft, BlockPos pos, BlockState state, List<ShowMarks.Seat> seats,
			List<BlockPos> trapezes, List<BlockPos> curb, List<BlockPos> rails, Map<Long, BlockPos> collars) {
		if (state.is(BlockInit.circus_seating.get())) {
			seats.add(new ShowMarks.Seat(pos.immutable(), state.getValue(HorizontalDirectionalBlock.FACING).toYRot(),
					false));
		} else if (state.is(BlockInit.circus_gallery_rail.get())) {
			Direction out = state.getValue(HorizontalDirectionalBlock.FACING);
			rails.add(pos.immutable());
			BlockPos deck = pos.relative(out.getOpposite());
			if (minecraft.level.getBlockState(deck).isAir() && minecraft.level.getBlockState(deck.above()).isAir())
				seats.add(new ShowMarks.Seat(deck.immutable(), out.toYRot(), true));
		} else if (state.is(BlockInit.circus_trapeze.get())) {
			trapezes.add(pos.immutable());
		} else if (state.is(BlockInit.circus_ring_curb.get())) {
			curb.add(pos.immutable());
		} else if ((state.is(BlockInit.circus_king_pole.get()) || state.is(BlockInit.circus_quarter_pole.get()))
				&& state.getValue(TroupePoleBlock.SECTION) == TroupePoleBlock.Section.COLLAR) {
			// The highest collar of each pole column.
			long column = BlockPos.asLong(pos.getX(), 0, pos.getZ());
			BlockPos known = collars.get(column);
			if (known == null || known.getY() < pos.getY()) collars.put(column, pos.immutable());
		}
	}

	/** Gallery rails grouped into straight runs: the same facing and level, one block apart along the rail. */
	private static List<List<Vec3>> railLines(Minecraft minecraft, List<BlockPos> rails) {
		Map<String, List<BlockPos>> lines = new HashMap<>();
		for (BlockPos pos : rails) {
			Direction out = minecraft.level.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
			int across = out.getAxis() == Direction.Axis.Z ? pos.getZ() : pos.getX();
			lines.computeIfAbsent(out + "|" + pos.getY() + "|" + across, ignored -> new ArrayList<>()).add(pos);
		}
		List<List<Vec3>> result = new ArrayList<>();
		for (List<BlockPos> line : lines.values()) {
			line.sort(Comparator.comparingInt(pos -> pos.getX() + pos.getZ()));
			List<Vec3> run = new ArrayList<>();
			BlockPos last = null;
			for (BlockPos pos : line) {
				if (last != null && last.distManhattan(pos) > 1) {
					if (run.size() > 1) result.add(spaced(run));
					run = new ArrayList<>();
				}
				run.add(Vec3.atCenterOf(pos).add(0.0D, 0.45D, 0.0D));
				last = pos;
			}
			if (run.size() > 1) result.add(spaced(run));
		}
		return result;
	}

	/** Every third rail top, plus the last, so a garland swags between posts rather than lying flat. */
	private static List<Vec3> spaced(List<Vec3> run) {
		List<Vec3> posts = new ArrayList<>();
		for (int i = 0; i < run.size(); i += 3) posts.add(run.get(i));
		if (!posts.getLast().equals(run.getLast())) posts.add(run.getLast());
		return posts;
	}
}
