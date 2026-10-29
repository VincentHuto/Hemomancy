package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.MausoleumWardClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes.MausoleumWardUniforms;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes.WardStrike;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.MausoleumWardRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchors;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The consecrated ward over a Saint's mausoleum, drawn from each loaded, settled {@link WardAnchorBlockEntity} near
 * the camera: a faint layer of runes over every face of consecrated stone past the threshold within
 * {@link MausoleumWardRules#WARD_RADIUS} of the camera, the threshold itself as a sheet across the corridor, and the
 * face a strike landed on when it lies outside that layer (the outer halls). All of one Saint's geometry goes into
 * one {@link HemoRenderTypes#mausoleumWard} type, told apart by vertex colour, so each Saint costs one draw a frame;
 * with no anchor loaded the pass returns before it allocates anything.
 *
 * <p>The ward's faces are found once per chunk section and kept per anchor. Consecrated stone cannot change in
 * survival and the client hears of no block change, so each section is found again every {@link #REBUILD_TICKS}
 * (staggered so sections do not all rebuild on one frame), and at once when a strike lands in it.
 */
public final class MausoleumWardRenderer {
	private static final int RADIUS = MausoleumWardRules.WARD_RADIUS;
	/** A face is drawn while its centre is this close, squared: the radius plus half a block's diagonal. */
	private static final double FACE_REACH_SQR = (RADIUS + 0.75) * (RADIUS + 0.75);
	/** Anchors, sheets and strikes are looked at from this far. */
	private static final double ANCHOR_REACH = 48.0;
	private static final double SHEET_REACH_SQR = 32.0 * 32.0;
	private static final int REBUILD_TICKS = 100;
	private static final int REBUILD_STAGGER = 40;
	/** Sections found per frame at most, so walking into a complex never stalls a frame. */
	private static final int BUILDS_PER_FRAME = 6;
	/** Sections kept per anchor before the far ones are dropped. */
	private static final int SECTIONS_KEPT = 96;
	private static final int KIND_SHEET = 128;
	private static final int KIND_STRUCK = 255;
	/** How hard the sheet presses on a viewer who may cross, against one who may not. */
	private static final float PRESS_PASSING = 0.3F;
	private static final EnumSaintType[] SAINTS = EnumSaintType.values();
	private static final Direction[] DIRECTIONS = Direction.values();

	private static final Map<WardAnchorBlockEntity, Long2ObjectOpenHashMap<Section>> FACES = new WeakHashMap<>();
	private static int buildsThisFrame;

	private MausoleumWardRenderer() {
	}

	/** The ward faces of one chunk section of one anchor: solid blocks and the face of each that the ward covers. */
	private static final class Section {
		private final long[] blocks;
		private final byte[] faces;
		private long rebuildAt;

		Section(long[] blocks, byte[] faces, long rebuildAt) {
			this.blocks = blocks;
			this.faces = faces;
			this.rebuildAt = rebuildAt;
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		Collection<WardAnchorBlockEntity> anchors = WardAnchors.loaded(level);
		if (anchors.isEmpty()) {
			if (!FACES.isEmpty()) FACES.clear();
			return;
		}
		if (FACES.size() > anchors.size()) FACES.keySet().retainAll(anchors);
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		long now = level.getGameTime();
		buildsThisFrame = 0;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		PoseStack.Pose pose = poseStack.last();
		WardStrike[] strikes = null;
		for (EnumSaintType saint : SAINTS) {
			RenderType type = null;
			VertexConsumer consumer = null;
			for (WardAnchorBlockEntity anchor : anchors) {
				if (anchor.saint() != saint || !anchor.settled() || anchor.isRemoved() || !near(anchor, camera)) continue;
				if (consumer == null) {
					if (strikes == null) strikes = strikes(camera, partialTick);
					type = HemoRenderTypes.mausoleumWard(saint, uniforms(mc, level, saint, camera, partialTick, strikes));
					consumer = buffers.getBuffer(type);
				}
				drawWard(level, anchor, consumer, pose, camera, now);
				drawSheet(mc, anchor, consumer, pose, camera);
				drawStruck(level, anchor, consumer, pose, camera);
			}
			if (type != null) buffers.endBatch(type);
		}
	}

	/** The loaded, settled anchor whose hallowed pieces hold {@code pos}, or else the nearest one in reach. */
	@Nullable
	public static WardAnchorBlockEntity anchorAt(Level level, BlockPos pos) {
		Vec3 point = Vec3.atCenterOf(pos);
		WardAnchorBlockEntity nearest = null;
		double best = ANCHOR_REACH * ANCHOR_REACH;
		for (WardAnchorBlockEntity anchor : WardAnchors.loaded(level)) {
			if (!anchor.settled()) continue;
			if (anchor.inHallowed(point)) return anchor;
			double distance = anchor.getBlockPos().distToCenterSqr(point);
			if (distance < best) {
				best = distance;
				nearest = anchor;
			}
		}
		return nearest;
	}

	/** A strike landed in {@code pos}'s section: find its faces again on the next frame. */
	public static void invalidate(BlockPos pos) {
		long key = SectionPos.asLong(pos);
		for (Long2ObjectOpenHashMap<Section> sections : FACES.values()) {
			Section section = sections.get(key);
			if (section != null) section.rebuildAt = 0L;
		}
	}

	public static void clear() {
		FACES.clear();
	}

	private static boolean near(WardAnchorBlockEntity anchor, Vec3 camera) {
		for (BoundingBox box : anchor.hallowedBoxes()) {
			if (distanceSqr(box, camera) <= ANCHOR_REACH * ANCHOR_REACH) return true;
		}
		return false;
	}

	private static double distanceSqr(BoundingBox box, Vec3 point) {
		double dx = Math.max(0.0, Math.max(box.minX() - point.x, point.x - (box.maxX() + 1)));
		double dy = Math.max(0.0, Math.max(box.minY() - point.y, point.y - (box.maxY() + 1)));
		double dz = Math.max(0.0, Math.max(box.minZ() - point.z, point.z - (box.maxZ() + 1)));
		return dx * dx + dy * dy + dz * dz;
	}

	/** The four youngest strikes in reach, camera-relative; empty slots are {@link WardStrike#NONE}. */
	private static WardStrike[] strikes(Vec3 camera, float partialTick) {
		WardStrike[] out = {WardStrike.NONE, WardStrike.NONE, WardStrike.NONE, WardStrike.NONE};
		List<MausoleumWardClientState.Strike> live = MausoleumWardClientState.strikes();
		int slot = 0;
		for (int i = live.size() - 1; i >= 0 && slot < out.length; i--) {
			MausoleumWardClientState.Strike strike = live.get(i);
			Vec3 origin = strike.origin();
			if (origin.distanceToSqr(camera) > ANCHOR_REACH * ANCHOR_REACH) continue;
			out[slot++] = new WardStrike((float) (origin.x - camera.x), (float) (origin.y - camera.y),
					(float) (origin.z - camera.z), strike.life(partialTick), strike.power(), strike.threshold());
		}
		return out;
	}

	private static MausoleumWardUniforms uniforms(Minecraft mc, ClientLevel level, EnumSaintType saint, Vec3 camera,
			float partialTick, WardStrike[] strikes) {
		Vec3 player = mc.player == null ? camera.add(0.0, -1000.0, 0.0)
				: mc.player.getPosition(partialTick).add(0.0, mc.player.getBbHeight() * 0.5, 0.0);
		HitResult hit = mc.hitResult;
		Vec3 focus = hit != null && hit.getType() != HitResult.Type.MISS ? hit.getLocation()
				: camera.add(mc.gameRenderer.getMainCamera().getLookVector().x() * 6.0,
						mc.gameRenderer.getMainCamera().getLookVector().y() * 6.0,
						mc.gameRenderer.getMainCamera().getLookVector().z() * 6.0);
		return new MausoleumWardUniforms(AnimationTime.gameTime(level, partialTick), MausoleumWardRules.wardColor(saint),
				(float) (player.x - camera.x), (float) (player.y - camera.y), (float) (player.z - camera.z),
				(float) (focus.x - camera.x), (float) (focus.y - camera.y), (float) (focus.z - camera.z),
				strikes[0], strikes[1], strikes[2], strikes[3]);
	}

	// ------------------------------------------------------------------ the ward layer

	private static void drawWard(ClientLevel level, WardAnchorBlockEntity anchor, VertexConsumer consumer,
			PoseStack.Pose pose, Vec3 camera, long now) {
		List<BoundingBox> boxes = anchor.wardBoxes();
		if (boxes.isEmpty()) return;
		Long2ObjectOpenHashMap<Section> sections = FACES.computeIfAbsent(anchor, key -> new Long2ObjectOpenHashMap<>());
		int minX = SectionPos.blockToSectionCoord(Mth.floor(camera.x - RADIUS));
		int maxX = SectionPos.blockToSectionCoord(Mth.floor(camera.x + RADIUS));
		int minY = SectionPos.blockToSectionCoord(Mth.floor(camera.y - RADIUS));
		int maxY = SectionPos.blockToSectionCoord(Mth.floor(camera.y + RADIUS));
		int minZ = SectionPos.blockToSectionCoord(Mth.floor(camera.z - RADIUS));
		int maxZ = SectionPos.blockToSectionCoord(Mth.floor(camera.z + RADIUS));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int sx = minX; sx <= maxX; sx++) {
			for (int sz = minZ; sz <= maxZ; sz++) {
				for (int sy = minY; sy <= maxY; sy++) {
					if (!touchesWard(boxes, sx, sy, sz)) continue;
					long key = SectionPos.asLong(sx, sy, sz);
					Section section = sections.get(key);
					if ((section == null || now >= section.rebuildAt) && buildsThisFrame < BUILDS_PER_FRAME
							&& level.getChunkSource().hasChunk(sx, sz)) {
						buildsThisFrame++;
						if (sections.size() >= SECTIONS_KEPT) dropFar(sections, camera);
						section = build(level, boxes, sx, sy, sz, now + REBUILD_TICKS
								+ Math.floorMod(Long.hashCode(key) * 31, REBUILD_STAGGER));
						sections.put(key, section);
					}
					if (section != null) emit(section, consumer, pose, camera, pos);
				}
			}
		}
	}

	private static boolean touchesWard(List<BoundingBox> boxes, int sx, int sy, int sz) {
		int x0 = SectionPos.sectionToBlockCoord(sx);
		int y0 = SectionPos.sectionToBlockCoord(sy);
		int z0 = SectionPos.sectionToBlockCoord(sz);
		for (BoundingBox box : boxes) {
			if (box.maxX() >= x0 && box.minX() <= x0 + 15 && box.maxY() >= y0 && box.minY() <= y0 + 15
					&& box.maxZ() >= z0 && box.minZ() <= z0 + 15) return true;
		}
		return false;
	}

	/**
	 * Every face of solid stone in the ward within one section: a solid block in a ward box whose neighbour on that
	 * side is not solid and is in a ward box too, so the layer lines the inside of the warded pieces only.
	 */
	private static Section build(ClientLevel level, List<BoundingBox> boxes, int sx, int sy, int sz, long rebuildAt) {
		LongArrayList blocks = new LongArrayList();
		ByteArrayList faces = new ByteArrayList();
		int x0 = SectionPos.sectionToBlockCoord(sx);
		int y0 = SectionPos.sectionToBlockCoord(sy);
		int z0 = SectionPos.sectionToBlockCoord(sz);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos beyond = new BlockPos.MutableBlockPos();
		for (int b = 0; b < boxes.size(); b++) {
			BoundingBox box = boxes.get(b);
			int fromX = Math.max(x0, box.minX()), toX = Math.min(x0 + 15, box.maxX());
			int fromY = Math.max(y0, box.minY()), toY = Math.min(y0 + 15, box.maxY());
			int fromZ = Math.max(z0, box.minZ()), toZ = Math.min(z0 + 15, box.maxZ());
			for (int x = fromX; x <= toX; x++) {
				for (int y = fromY; y <= toY; y++) {
					for (int z = fromZ; z <= toZ; z++) {
						pos.set(x, y, z);
						if (inBox(boxes, b, pos) || !level.getBlockState(pos).isSolidRender(level, pos)) continue;
						for (Direction direction : DIRECTIONS) {
							beyond.setWithOffset(pos, direction);
							if (!inBox(boxes, boxes.size(), beyond)
									|| level.getBlockState(beyond).isSolidRender(level, beyond)) continue;
							blocks.add(pos.asLong());
							faces.add((byte) direction.ordinal());
						}
					}
				}
			}
		}
		return new Section(blocks.toLongArray(), faces.toByteArray(), rebuildAt);
	}

	/** Whether {@code pos} lies in one of the first {@code count} boxes. */
	private static boolean inBox(List<BoundingBox> boxes, int count, BlockPos pos) {
		for (int i = 0; i < count; i++) {
			if (boxes.get(i).isInside(pos)) return true;
		}
		return false;
	}

	private static void dropFar(Long2ObjectOpenHashMap<Section> sections, Vec3 camera) {
		int cx = SectionPos.blockToSectionCoord(Mth.floor(camera.x));
		int cy = SectionPos.blockToSectionCoord(Mth.floor(camera.y));
		int cz = SectionPos.blockToSectionCoord(Mth.floor(camera.z));
		sections.long2ObjectEntrySet().removeIf(entry -> {
			long key = entry.getLongKey();
			return Math.abs(SectionPos.x(key) - cx) > 3 || Math.abs(SectionPos.y(key) - cy) > 3
					|| Math.abs(SectionPos.z(key) - cz) > 3;
		});
	}

	private static void emit(Section section, VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera,
			BlockPos.MutableBlockPos pos) {
		for (int i = 0; i < section.blocks.length; i++) {
			pos.set(section.blocks[i]);
			Direction direction = DIRECTIONS[section.faces[i]];
			double dx = pos.getX() + 0.5 + direction.getStepX() * 0.5 - camera.x;
			double dy = pos.getY() + 0.5 + direction.getStepY() * 0.5 - camera.y;
			double dz = pos.getZ() + 0.5 + direction.getStepZ() * 0.5 - camera.z;
			if (dx * dx + dy * dy + dz * dz > FACE_REACH_SQR) continue;
			BlockShellFaces.emitFace(consumer, pose, pos, direction, camera, faceColor(pos, direction, 0));
		}
	}

	/**
	 * A face's vertex colour: red its kind, green a seed from the face's plane and side, blue its direction ordinal
	 * * 36, so the shader picks a different rune for the same block coordinate on another wall.
	 */
	private static int faceColor(BlockPos pos, Direction direction, int kind) {
		int plane = direction.getAxis().choose(pos.getX(), pos.getY(), pos.getZ())
				+ (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0);
		int seed = (plane * 37 + direction.ordinal() * 11) & 0xFF;
		return 0xFF000000 | kind << 16 | seed << 8 | direction.ordinal() * 36;
	}

	// ------------------------------------------------------------------ the threshold sheet

	/**
	 * The threshold slab's outer face, across the corridor: a faint sheet that presses back hardest on a viewer who
	 * may not cross, and flares where a strike turned someone back.
	 */
	private static void drawSheet(Minecraft mc, WardAnchorBlockEntity anchor, VertexConsumer consumer,
			PoseStack.Pose pose, Vec3 camera) {
		AABB threshold = anchor.threshold();
		if (threshold == null || threshold.getCenter().distanceToSqr(camera) > SHEET_REACH_SQR) return;
		Direction inward = anchor.inward();
		Direction.Axis along = inward.getAxis();
		Direction.Axis across = along == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		boolean positive = inward.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		// Just outside the slab, so the sheet never fights the floor line or the walls for depth.
		double plane = positive ? threshold.min(along) - 0.003 : threshold.max(along) + 0.003;
		// U runs to the right of someone facing in, so the falling rune columns read the right way from outside.
		int right = inward.getClockWise().getAxisDirection().getStep();
		float press = mc.player != null
				&& ProgressionGates.hasDegree(mc.player, MausoleumWardRules.WARD_DEGREE) ? PRESS_PASSING : 1.0F;
		int color = 0xFF000000 | KIND_SHEET << 16 | Math.round(press * 255.0F) << 8;
		double a0 = threshold.min(across), a1 = threshold.max(across);
		double y0 = threshold.minY, y1 = threshold.maxY;
		sheetVertex(consumer, pose, along, plane, a0, y0, right, camera, color, inward);
		sheetVertex(consumer, pose, along, plane, a1, y0, right, camera, color, inward);
		sheetVertex(consumer, pose, along, plane, a1, y1, right, camera, color, inward);
		sheetVertex(consumer, pose, along, plane, a0, y1, right, camera, color, inward);
	}

	private static void sheetVertex(VertexConsumer consumer, PoseStack.Pose pose, Direction.Axis along, double plane,
			double acrossAt, double y, int right, Vec3 camera, int color, Direction inward) {
		double x = along == Direction.Axis.X ? plane : acrossAt;
		double z = along == Direction.Axis.X ? acrossAt : plane;
		consumer.addVertex(pose.pose(), (float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z))
				.setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
				.setUv((float) (acrossAt * right * 0.37), (float) (y * 0.37))
				.setLight(LightTexture.FULL_BRIGHT)
				.setNormal(pose, -inward.getStepX(), 0.0F, -inward.getStepZ());
	}

	// ------------------------------------------------------------------ struck faces outside the layer

	/**
	 * The faces strikes landed on that the layer does not draw (the outer halls, or past the drawn radius), so every
	 * refused blow flashes where it fell. Threshold strikes flare on the sheet instead.
	 */
	private static void drawStruck(ClientLevel level, WardAnchorBlockEntity anchor, VertexConsumer consumer,
			PoseStack.Pose pose, Vec3 camera) {
		if (!MausoleumWardClientState.hasStrikes()) return;
		for (MausoleumWardClientState.Strike strike : MausoleumWardClientState.strikes()) {
			if (strike.threshold()) continue;
			BlockPos pos = strike.pos();
			if (anchorAt(level, pos) != anchor) continue;
			Vec3 origin = strike.origin();
			if (origin.distanceToSqr(camera) > ANCHOR_REACH * ANCHOR_REACH || inLayer(level, anchor, strike, camera)) continue;
			BlockShellFaces.emitFace(consumer, pose, pos, strike.face(), camera, faceColor(pos, strike.face(), KIND_STRUCK));
		}
	}

	private static boolean inLayer(ClientLevel level, WardAnchorBlockEntity anchor, MausoleumWardClientState.Strike strike,
			Vec3 camera) {
		BlockPos pos = strike.pos();
		BlockPos beyond = pos.relative(strike.face());
		return strike.origin().distanceToSqr(camera) <= FACE_REACH_SQR
				&& anchor.inWard(Vec3.atCenterOf(pos)) && anchor.inWard(Vec3.atCenterOf(beyond))
				&& level.getBlockState(pos).isSolidRender(level, pos)
				&& !level.getBlockState(beyond).isSolidRender(level, beyond);
	}
}
