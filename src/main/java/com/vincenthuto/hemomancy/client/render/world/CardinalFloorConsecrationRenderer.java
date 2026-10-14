package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.floor.AnchorSores;
import com.vincenthuto.hemomancy.client.render.world.floor.CardinalFloorRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.floor.FloorMesh;
import com.vincenthuto.hemomancy.client.render.world.floor.FloorRiteResponse;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws a consecrated Cardinal Rite floor: a continuous membrane over the ground inside the brazier trench, the trench
 * itself, flesh mounds around seated braziers, a fleshy pad over the Focus mouth, and vessel tendrils that race from
 * the Focus to each socket as the floor forms. The floor is the body of any rite held on it: the rite's beats drive
 * the membrane through {@link FloorRiteResponse}. The server only reports which floor is formed; nothing here
 * affects rite matching. The static mesh is cached in {@link FloorMesh}.
 */
public final class CardinalFloorConsecrationRenderer {
	/** Tendrils root on a ring under the Focus pad instead of one crowded point. */
	private static final double TENDRIL_ROOT_RADIUS = 0.5D;
	private static final double TENDRIL_ROOT_Y = 0.99D;
	/**
	 * The ground's own break particles kicked up where the dig front is carving. These are vanilla {@code BLOCK}
	 * particles: the documented exception to the custom-particle rule, kept at the user's request (floor review
	 * pass 29) because only the ground's own texture reads as dug earth.
	 */
	private static final boolean DIG_BLOCK_DUST = true;
	private static final int DIG_DUST_PER_TICK = 3;
	private static final int DIG_DUST_CACHE_LIMIT = 128;
	private static final Map<Long, Long> DIG_DUST_TICK = new HashMap<>();
	private static final int TENDRIL_CACHE_LIMIT = 256;
	private static final Map<Long, Long> TENDRILS_SPAWNED = new HashMap<>();
	/** A pair of tendrils is drawn into the throat of a gripped staff every this many ticks. */
	private static final long SWALLOW_INTERVAL = 36L;
	private static final int SWALLOW_COUNT = 2;
	/** Swallowed tendrils start on the membrane and end in the throat's cuff, in blocks above the Focus's base. */
	private static final double SWALLOW_START_Y = 1.02D;
	private static final double SWALLOW_END_Y = 0.9D;
	private static final Map<Long, Long> SWALLOWS = new HashMap<>();

	private CardinalFloorConsecrationRenderer() {
	}

	public static boolean hasFloor(CardinalFocusBlockEntity focus) {
		return focus.getFormedFloor() != null || focus.getFadingFloor() != null;
	}

	/** How far from the Focus the membrane and sockets can reach, for render bounds. */
	public static int renderRadius(CardinalFocusBlockEntity focus) {
		var id = focus.getFormedFloor() != null ? focus.getFormedFloor() : focus.getFadingFloor();
		if (id == null) return 1;
		// A grounding rite's ring runs out past the floor into the ground for a moment.
		int ring = FloorRiteResponse.groundRingLive(focus.getBlockPos())
				? (int) Math.ceil(CardinalRiteFloorVisualRules.GROUND_RING_BEYOND) + 3 : 0;
		// A deepening rite's creep spreads past the floor's edge into the ground.
		var world = com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.at(focus.getBlockPos());
		int creep = world == null ? 0 : (int) Math.ceil(com.vincenthuto.hemomancy.client.render.world.rite.escalation
				.RiteWorldMath.creepReach(world.footprint())) + 2;
		int beyond = Math.max(ring, creep);
		return CardinalRiteFloorRegistry.get(id)
				.map(floor -> CardinalRiteFloorGeometry.halfWidth(floor) + 5 + beyond).orElse(1);
	}

	public static void render(CardinalFocusBlockEntity focus, float partialTick, PoseStack pose,
			MultiBufferSource buffers) {
		Level level = focus.getLevel();
		if (level == null) return;
		double time = level.getGameTime() + (double) partialTick;
		float now = level.getGameTime() + partialTick;
		var id = focus.getFormedFloor();
		Direction forwards = focus.getFormedForwards();
		float age = now - focus.getFormationStart();
		boolean collapsing = focus.isCollapsing(now);
		if (collapsing) {
			// A broken or replaced floor plays its formation backwards; any replacement waits until it has finished.
			id = focus.getFadingFloor();
			forwards = focus.getFadingForwards();
			age = CardinalRiteFloorVisualRules.collapseAge(now - focus.getFadingSince());
		}
		if (id == null || age <= 0.0F) return;
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(id).orElse(null);
		if (floor == null) return;

		BlockPos origin = focus.getBlockPos();
		int half = CardinalRiteFloorGeometry.halfWidth(floor);
		int style = CardinalRiteFloorVisualRules.styleIndex(floor.style());
		var palette = CardinalRiteFloorVisualRules.palette(style);
		FloorRiteResponse rite = FloorRiteResponse.at(origin, CardinalRiteFloorVisualRules.reach(half), partialTick,
				time);
		focus.setRiteTempo(rite.quick(), rite.intense());
		// A failed rite tears the membrane back toward the Focus and lets it regrow; the trench is cut into the ground
		// and stays open.
		float membraneAge = age;
		if (!collapsing && rite.tearTicks() >= 0.0F) {
			membraneAge = Math.min(age, CardinalRiteFloorVisualRules.tearAge(rite.tearTicks()));
		}
		float spread = CardinalRiteFloorVisualRules.spreadRadius(Math.min(membraneAge, 1.0e6F), half);
		float front = membraneAge >= CardinalRiteFloorVisualRules.FORM_TICKS ? 0.0F
				: CardinalRiteFloorVisualRules.frontIntensity(membraneAge);
		float beat = focus.heartbeat(time);
		float seed = (Math.floorMod(origin.asLong() * 0x9E3779B97F4A7C15L, 1000L)) * 0.1F;

		// One light sample at the Focus drives the membrane, its seats and its pad, so they darken together.
		int packedLight = LevelRenderer.getLightColor(level, origin.above());
		float floorLight = worldLight(level, packedLight, partialTick);
		// The Focus centre relative to the camera, so the shader can place the trench from camera-relative positions.
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float focusX = (float) (origin.getX() + 0.5D - camera.x);
		float focusZ = (float) (origin.getZ() + 0.5D - camera.z);
		FloorMesh mesh = FloorMesh.of(level, floor, origin, forwards);
		Matrix4f matrix = pose.last().pose();
		// One render type carries the membrane, the trench and the pad, so they draw in submission order in one batch.
		RenderType membrane = CardinalFloorRenderTypes.withRite(HemoRenderTypes.cardinalFloorMembrane(now, seed, style,
				half, spread, front, beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX, focusZ),
				rite);
		VertexConsumer consumer = buffers.getBuffer(membrane);
		mesh.emitMembrane(consumer, matrix, 255);
		if (rite.groundRingStrength() > 0.01F) {
			mesh.emitGroundRing(consumer, matrix, level, origin, rite.groundRingRadius(), 255);
		}
		if (!collapsing && rite.creepRadius() > 0.01F) {
			mesh.emitCreep(consumer, matrix, level, origin, rite.creepRadius(), 255);
		}
		if (mesh.loop() != null) {
			mesh.emitGash(consumer, matrix, age, rite.trenchGlow(mesh));
			if (DIG_BLOCK_DUST) spawnDigDust(level, origin, mesh, age);
		}
		// Depth-writing flesh: brazier seats swallow their feet, and the Focus throat grips a planted staff's.
		float grip = collapsing ? 0.0F : staffGrip(origin, partialTick);
		boolean seats = mesh.loop() != null && mesh.hasSeats();
		if (seats || grip > 0.001F) {
			VertexConsumer mound = buffers.getBuffer(CardinalFloorRenderTypes.withRite(HemoRenderTypes.cardinalFloorMound(
					now, seed, style, half, spread, front, beat, floorLight, palette.base(), palette.vein(),
					palette.glow(), focusX, focusZ), rite));
			if (seats) mesh.emitSeats(mound, matrix, beat, spread, 255);
			FloorMesh.emitThroat(mound, matrix, grip, beat, now, seed, 255);
		}
		if (grip >= 0.999F) spawnSwallowTendrils(origin, level.getGameTime(), partialTick);
		VertexConsumer surface = buffers.getBuffer(membrane);
		FloorMesh.emitFocusPad(surface, matrix, Math.round(255 * CardinalRiteFloorVisualRules.padGrowth(membraneAge)));
		// A live rite's anchors open as sores in the floor, after the membrane so they cover it.
		if (!collapsing) AnchorSores.emit(surface, matrix, level, origin);
		if (!collapsing && age < CardinalRiteFloorVisualRules.FORM_TICKS) {
			spawnFormationTendrils(origin, focus.getFormedSince(), CardinalRiteFloorGeometry.sockets(floor, origin,
					forwards), palette, partialTick);
		}
	}

	/**
	 * Every so often the floor round a gripped staff is drawn into the throat: a couple of tendrils reach in from the
	 * membrane, arch over the pad and dive down into the cuff, so the swallowing reads from across the floor.
	 */
	private static void spawnSwallowTendrils(BlockPos origin, long gameTime, float partialTick) {
		long wave = gameTime / SWALLOW_INTERVAL;
		Long last = SWALLOWS.get(origin.asLong());
		if (last != null && last == wave) return;
		if (SWALLOWS.size() > TENDRIL_CACHE_LIMIT) SWALLOWS.clear();
		SWALLOWS.put(origin.asLong(), wave);
		Vec3 cuff = Vec3.atLowerCornerOf(origin).add(0.5D, SWALLOW_END_Y, 0.5D);
		for (int index = 0; index < SWALLOW_COUNT; index++) {
			long seed = origin.asLong() * 31L + wave * 7L + index;
			double angle = Math.floorMod(seed * 0x9E3779B97F4A7C15L, 6283L) / 1000.0D;
			double reach = 1.1D + Math.floorMod(seed * 0x632BE59BD9B4E019L, 600L) / 1000.0D;
			Vec3 heading = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
			Vec3 start = Vec3.atLowerCornerOf(origin).add(0.5D, SWALLOW_START_Y, 0.5D).add(heading.scale(reach));
			Vec3 end = cuff.add(heading.scale(0.13D));
			TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
					new TendrilAnchor.Point(end), swallowConfig(seed), seed), partialTick);
		}
	}

	private static TendrilEffectConfig swallowConfig(long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF22A0408, 0xC8B80B18)
				.withBlendColors(false)
				.withLifecycle(16, 6, 16)
				.withShape(16, 1, 0.032F, 0.12F)
				.withBranching(1, 1, 0.12F, 0.45F)
				// A negative sag arches the strand up over the pad, so it dives into the throat rather than the floor.
				.withWrithe(0.05F, 0.12F, 0.1F, -0.32F)
				.withFixedSeed(true, seed);
	}

	/**
	 * How far the Focus throat has closed round a planted staff: it draws down as the staff's binding strands form
	 * after its impact, and lets go as the staff melts away.
	 */
	private static float staffGrip(BlockPos origin, float partialTick) {
		for (ActiveRiteClientData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (rite.isUnstained() || !rite.hasPlantedStaff() || !rite.getCenter().equals(origin)) continue;
			return CardinalRiteStaffTendrilGeometry.visibilityProgress(rite.staffPlantingRenderTicks(partialTick),
					rite.cancellationRenderTicks(partialTick));
		}
		return 0.0F;
	}

	/** 0 in darkness to 1 in full light: the brighter of block light and sky light dimmed by the time of day. */
	private static float worldLight(Level level, int packedLight, float partialTick) {
		float sky = LightTexture.sky(packedLight) / 15.0F;
		if (level instanceof ClientLevel client) sky *= client.getSkyDarken(partialTick);
		return Math.max(LightTexture.block(packedLight) / 15.0F, sky);
	}

	/** Once per game tick, the ground's break particles burst up from a few points the dig front is cutting. */
	private static void spawnDigDust(Level level, BlockPos origin, FloorMesh mesh, float age) {
		long tick = level.getGameTime();
		Long last = DIG_DUST_TICK.get(origin.asLong());
		if (last != null && last == tick) return;
		if (DIG_DUST_TICK.size() > DIG_DUST_CACHE_LIMIT) DIG_DUST_TICK.clear();
		DIG_DUST_TICK.put(origin.asLong(), tick);
		int samples = mesh.samples();
		float[][] centre = mesh.loop().centre();
		List<Integer> cutting = new ArrayList<>();
		for (int i = 0; i < samples; i++) {
			float dig = CardinalRiteFloorVisualRules.digProgress(age, i, samples);
			if (mesh.grounded(i) && dig > 0.1F && dig < 0.9F) cutting.add(i);
		}
		if (cutting.isEmpty()) return;
		var random = level.random;
		for (int n = 0; n < DIG_DUST_PER_TICK; n++) {
			float[] at = centre[cutting.get(random.nextInt(cutting.size()))];
			BlockPos ground = origin.offset((int) Math.floor(at[0]), 0, (int) Math.floor(at[1]));
			var state = level.getBlockState(ground);
			if (state.isAir()) continue;
			double x = origin.getX() + at[0] + (random.nextDouble() - 0.5D) * 0.3D;
			double z = origin.getZ() + at[1] + (random.nextDouble() - 0.5D) * 0.3D;
			level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, origin.getY() + 1.02D, z,
					(random.nextDouble() - 0.5D) * 0.15D, 0.12D + random.nextDouble() * 0.12D,
					(random.nextDouble() - 0.5D) * 0.15D);
		}
	}

	/** Once per formation, a vessel races from the Focus to every socket it will feed. */
	private static void spawnFormationTendrils(BlockPos origin, long formedSince, List<BlockPos> sockets,
			CardinalRiteFloorVisualRules.Palette palette, float partialTick) {
		long key = origin.asLong();
		if (Long.valueOf(formedSince).equals(TENDRILS_SPAWNED.get(key))) return;
		if (TENDRILS_SPAWNED.size() > TENDRIL_CACHE_LIMIT) TENDRILS_SPAWNED.clear();
		TENDRILS_SPAWNED.put(key, formedSince);
		Vec3 centre = Vec3.atLowerCornerOf(origin).add(0.5D, TENDRIL_ROOT_Y, 0.5D);
		for (int index = 0; index < sockets.size(); index++) {
			Vec3 end = Vec3.atLowerCornerOf(sockets.get(index)).add(0.5D, 0.06D, 0.5D);
			// Each vessel leaves the Focus mouth on the side facing its socket, under the pad.
			Vec3 heading = new Vec3(end.x - centre.x, 0.0D, end.z - centre.z).normalize();
			Vec3 start = centre.add(heading.scale(TENDRIL_ROOT_RADIUS));
			long seed = key * 0x9E3779B97F4A7C15L ^ formedSince * 0xBF58476D1CE4E5B9L ^ index * 0x94D049BB133111EBL;
			TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
					new TendrilAnchor.Point(end), tendrilConfig(palette, seed), seed), partialTick);
		}
	}

	private static TendrilEffectConfig tendrilConfig(CardinalRiteFloorVisualRules.Palette palette, long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0000000 | (palette.base() & 0xFFFFFF), 0xD0000000 | (palette.vein() & 0xFFFFFF))
				.withBlendColors(false)
				.withLifecycle(30, 26, 18)
				.withShape(20, 1, 0.035F, 0.07F)
				.withBranching(1, 2, 0.14F, 0.4F)
				.withWrithe(0.05F, 0.1F, 0.06F, 0.0F)
				.withFixedSeed(true, seed);
	}
}
