package com.vincenthuto.hemomancy.client.render.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumDevoteeAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumDevoteeModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.vincenthuto.hemomancy.client.render.layer.mob.VelorumFrostGlowLayer;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.FrozenDevoteeBlock;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Velorum's body: the generated rig posed by its Java tracks, the frost glow layer over it, and the anchors (core, claw,
 * breast seam, the staff's heel, three lanterns) recorded each frame for the world geometry that hangs off them, with
 * the staff lantern, the heel, the claw and the side lantern kept as world-space trails in {@link VelorumAnimState}
 * (the Cruor Javelins leave from {@code lanternAt}, the Gloam Cuts from {@code clawAt}).
 *
 * <p>Revision 2026-11-06 (liquid dark): the Shadow Step and the phase-3 card carry the whole body down under the floor
 * ({@link VelorumAnimState#under}; the floor's depth hides what is below it, as a rising Saint's does), and the anchors
 * go down with it. The shapes of liquid dark are drawn here with the woken devotee's body
 * ({@link VelorumDevoteeModel}) through one translucent dark render type and its eye glow, both built once: the
 * Procession's three shades walking the road out of the pool, and in the Eclipse a shape standing up out of every frozen
 * figure (stretched to 1.25 times its height) that reaches along a reach the server owns and grips along a tether. The
 * smoke at their hoods and hems and their reaching limbs are the effects' (VelorumDarkGeometry).
 */
public class VelorumRenderer extends MobRenderer<VelorumEntity, VelorumModel<VelorumEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/velorum/velorum.png");
	private static final RenderType SHADE = RenderType.entityTranslucent(
			Hemomancy.rloc("textures/entity/boss/velorum/velorum_devotee_shade.png"));
	private static final RenderType SHADE_GLOW = RenderType.eyes(
			Hemomancy.rloc("textures/entity/boss/velorum/velorum_devotee_shade_glow.png"));
	private static final float SHADOW = 0.7F;
	private static final int FULL_BRIGHT = 0x00F000F0;
	/** A shade or shape at full: liquid dark, mostly opaque. */
	private static final float SHADE_ALPHA = 0.88F;
	/** The shades rise out of the far pool over this many ticks of their walk, and break into smoke over the last six. */
	private static final float SHADE_RISE = 12.0F, SHADE_BREAK = 6.0F, SHADE_DEPTH = 2.2F;
	/** A reach or a tether belongs to the figure whose base it leaves within this many blocks. */
	private static final double REACH_FROM = 1.5D;
	/** The Eclipse grip's tell: the shape reaches for 16 ticks before it grips. */
	private static final float GRIP_TELL = 16.0F;

	private final VelorumDevoteeModel shapes;
	/** The frozen figures a boss's Eclipse stands shapes out of, found once per Eclipse. */
	private final Map<VelorumEntity, Sites> sites = new WeakHashMap<>();

	private record Site(long key, Vec3 base, float yaw, int pose) {
	}

	private record Sites(int serial, List<Site> figures) {
	}

	public VelorumRenderer(Context context) {
		super(context, new VelorumModel<>(context.bakeLayer(VelorumModel.LAYER_LOCATION)), SHADOW);
		shapes = new VelorumDevoteeModel(context.bakeLayer(VelorumDevoteeModel.LAYER_LOCATION));
		addLayer(new VelorumFrostGlowLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(VelorumEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(VelorumEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		// the Shadow Step: down through the liquid dark (the pose is flipped here, so down is +Y)
		float under = VelorumAnimState.of(entity).under(entity, partialTick);
		if (under > 0.0F) poseStack.translate(0.0F, under, 0.0F);
	}

	@Override
	protected float getFlipDegrees(VelorumEntity entity) {
		return 0.0F;
	}

	@Override
	public boolean shouldRender(VelorumEntity entity, Frustum frustum, double x, double y, double z) {
		if (super.shouldRender(entity, frustum, x, y, z)) return true;
		// the shades and the Eclipse's shapes stand far from his body
		int move = entity.view().moveId();
		return move == VelorumMove.PROCESSION.id() || move == VelorumMove.ECLIPSE.id()
				|| VelorumAnimState.of(entity).shapes() > 0.0F;
	}

	@Override
	public void render(VelorumEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
		// no red hurt tint: a blow on him reads as frost (VelorumEffects.guarded), and a held channel would otherwise keep
		// him tinted for its whole hold
		int hurt = entity.hurtTime;
		entity.hurtTime = 0;
		try {
			super.render(entity, yaw, partial, poses, buffers, light);
		} finally {
			entity.hurtTime = hurt;
		}
		VelorumAnimState state = VelorumAnimState.of(entity);
		float under = state.under(entity, partial);
		float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
		Vec3[] lanterns = java.util.Arrays.stream(model.lanterns()).map(point -> offset(point, bodyYaw, under))
				.toArray(Vec3[]::new);
		// the posed limbs in the world, kept as trails so the sweep, the toll, the javelins and the cuts follow the animation
		state.attachments(offset(model.core(), bodyYaw, under), offset(model.claw(), bodyYaw, under),
				offset(model.seam(), bodyYaw, under), offset(model.heel(), bodyYaw, under), lanterns,
				entity.getPosition(partial), AnimationTime.age(entity, partial));
		Vec3 origin = entity.getPosition(partial);
		float now = AnimationTime.gameTime(entity.level(), partial);
		procession(entity, origin, now, partial, poses, buffers);
		eclipse(entity, state, origin, now, partial, poses, buffers);
	}

	private static Vec3 offset(Vector3f point, float yaw, float under) {
		point.set(-point.x, 1.501F - point.y - under, point.z);
		return new Vec3(point.rotateY((180.0F - yaw) * Mth.DEG_TO_RAD));
	}

	// ── the Procession's shades ─────────────────────────────────────

	/** Three shades of liquid dark rise out of the pool at the road's far end and walk it, each breaking into smoke. */
	private void procession(VelorumEntity boss, Vec3 origin, float now, float partial, PoseStack poses,
			MultiBufferSource buffers) {
		SaintMoveView view = boss.view();
		if (view.moveId() != VelorumMove.PROCESSION.id() || view.openingId() != 0) return;
		float moveTime = view.moveTime(partial);
		FootprintFrame frame = view.frame();
		SaintMoveInputs inputs = view.inputs();
		if (moveTime < 0.0F || frame == null) return;
		VelorumMove move = VelorumMove.PROCESSION;
		if (!(move.footprint(inputs) instanceof BossFootprint.Path path)) return;
		List<Vec3> points = frame.points();
		if (points.size() < 2) return;
		double length = Shapes.pathLength(points);
		if (length < 0.5D) return;
		float age = moveTime - SaintControllerRules.effective(move.kind(), move.timeline(inputs)).commitTick();
		if (age < 0.0F) return;
		for (int i = 0; i < path.walkers(); i++) {
			double head = path.walker(age, i);
			if (head < 0.0D) continue;
			double at = Math.min(length, head - path.frontLength() * 0.5D);
			if (at <= 0.0D || head - path.frontLength() >= length) continue;
			float risen = (float) HemoEasing.clamp01(head / Math.max(0.01D, path.frontSpeed() * SHADE_RISE));
			float whole = (float) HemoEasing.clamp01((length - (head - path.frontLength())) / Math.max(0.01D,
					path.frontSpeed() * SHADE_BREAK));
			Vec3 pos = Shapes.pointAlong(points, at);
			Vec3 ahead = Shapes.pointAlong(points, Math.min(length, at + 0.25D)).subtract(Shapes.pointAlong(points,
					Math.max(0.0D, at - 0.25D)));
			float yaw = (float) Math.toDegrees(Math.atan2(-ahead.x, ahead.z));
			Vec3 foot = new Vec3(pos.x, frame.floorY(), pos.z);
			shapes.poseShade((float) at, now);
			draw(poses, buffers, foot.subtract(origin), yaw, 1.0F, SHADE_DEPTH * (1.0F - HemoEasing.smoothstep(risen)),
					SHADE_ALPHA * whole);
		}
	}

	// ── the Eclipse's shapes ────────────────────────────────────────

	/**
	 * In the Eclipse a shape of liquid dark stands up out of every frozen figure and every stilled devotee (the block or
	 * body stays; the shape stands out of it). A shape whose figure a reach or a tether leaves reaches along it for the
	 * tell, then grips; a pinned figure's shape has none, so it never reaches.
	 */
	private void eclipse(VelorumEntity boss, VelorumAnimState state, Vec3 origin, float now, float partial, PoseStack poses,
			MultiBufferSource buffers) {
		float rise = state.shapes();
		if (rise <= 0.0F) {
			if (boss.view().moveId() != VelorumMove.ECLIPSE.id()) sites.remove(boss);
			return;
		}
		FootprintFrame frame = boss.view().frame();
		AABB chamber = frame == null ? boss.getBoundingBox().inflate(24.0D) : frame.chamber();
		List<Site> figures = new ArrayList<>(sites(boss, chamber).figures());
		for (VelorumDevoteeEntity devotee : boss.level().getEntitiesOfClass(VelorumDevoteeEntity.class, chamber)) {
			VelorumDevoteeEntity.State s = devotee.state();
			if (s != VelorumDevoteeEntity.State.STILLED && s != VelorumDevoteeEntity.State.KNEELING) continue;
			figures.add(new Site(devotee.getUUID().getLeastSignificantBits(), devotee.getPosition(partial),
					devotee.facing().toYRot(), 0));
		}
		List<BossGroundHazardEntity> reaches = new ArrayList<>();
		for (BossGroundHazardEntity hazard : boss.level().getEntitiesOfClass(BossGroundHazardEntity.class, chamber)) {
			if (hazard.isRemoved() || hazard.owner() != boss) continue;
			BossFootprint shape = hazard.shape();
			if (shape instanceof BossFootprint.Tether || shape instanceof BossFootprint.Lane) reaches.add(hazard);
		}
		for (Site site : figures) {
			VelorumDevoteeAnimState motion = VelorumDevoteeAnimState.shape(site.key(), now);
			float reach = 0.0F, grip = 0.0F, yaw = site.yaw();
			for (BossGroundHazardEntity hazard : reaches) {
				FootprintFrame from = hazard.frame();
				if (Shapes.planar(from.origin(), site.base()) > REACH_FROM) continue;
				float held = AnimationTime.age(hazard, partial);
				Vec3 toward = hazard.shape() instanceof BossFootprint.Tether ? from.target() : from.aim();
				reach = HemoEasing.ramp(held, 0.0F, 6.0F);
				if (hazard.shape() instanceof BossFootprint.Tether) grip = HemoEasing.ramp(held, GRIP_TELL - 2.0F, GRIP_TELL + 2.0F);
				if (hazard.fading()) reach = grip = 0.0F;
				float facing = (float) Math.toDegrees(Math.atan2(-(toward.x - site.base().x), toward.z - site.base().z));
				yaw = Mth.rotLerp(Math.max(reach, grip), site.yaw(), facing);
				break;
			}
			if (grip > 0.0F) shapes.poseShapeGrip(motion, site.pose(), rise, grip, now);
			else if (reach > 0.0F) shapes.poseShapeReach(motion, site.pose(), rise, reach, now);
			else shapes.poseShapeRise(motion, site.pose(), rise, now);
			draw(poses, buffers, site.base().subtract(origin), yaw, VelorumDevoteeModel.shapeStretch(rise), 0.0F,
					SHADE_ALPHA * HemoEasing.smoothstep(Math.min(1.0F, rise * 2.5F)));
		}
	}

	/** The frozen figures in the chamber, found once per Eclipse (by the move's synced serial). */
	private Sites sites(VelorumEntity boss, AABB chamber) {
		int serial = boss.syncedMoveSerial();
		Sites known = sites.get(boss);
		if (known != null && known.serial() == serial) return known;
		List<Site> found = new ArrayList<>();
		Level level = boss.level();
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
		for (int x = Mth.floor(chamber.minX); x <= Mth.floor(chamber.maxX); x++) {
			for (int z = Mth.floor(chamber.minZ); z <= Mth.floor(chamber.maxZ); z++) {
				for (int y = Mth.floor(chamber.minY); y <= Mth.floor(chamber.maxY); y++) {
					BlockState block = level.getBlockState(at.set(x, y, z));
					if (!(block.getBlock() instanceof FrozenDevoteeBlock)) continue;
					if (block.getValue(FrozenDevoteeBlock.HALF) != DoubleBlockHalf.LOWER) continue;
					found.add(new Site(at.asLong(), Vec3.atBottomCenterOf(at), block.getValue(FrozenDevoteeBlock.FACING).toYRot(),
							block.getValue(FrozenDevoteeBlock.POSE)));
				}
			}
		}
		Sites made = new Sites(serial, List.copyOf(found));
		sites.put(boss, made);
		return made;
	}

	/**
	 * One shape of liquid dark: the posed devotee body at {@code offset} from him, facing {@code yaw}, stretched
	 * {@code stretch} times its height from its feet and sunk {@code under} blocks, in the translucent dark with its
	 * eyes and cruor hands in the glow.
	 */
	private void draw(PoseStack poses, MultiBufferSource buffers, Vec3 offset, float yaw, float stretch, float under,
			float alpha) {
		if (alpha <= 0.01F) return;
		poses.pushPose();
		poses.translate(offset.x, offset.y - under, offset.z);
		poses.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
		poses.scale(1.0F, stretch, 1.0F);
		poses.scale(-1.0F, -1.0F, 1.0F);
		poses.translate(0.0F, -1.501F, 0.0F);
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		shapes.renderToBuffer(poses, buffers.getBuffer(SHADE), FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				FastColor.ARGB32.color(a, 255, 255, 255));
		// the eyes render type is additive and ignores alpha: the strength scales the colour
		int glow = Mth.clamp((int) (alpha * 230.0F), 0, 255);
		shapes.renderToBuffer(poses, buffers.getBuffer(SHADE_GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				FastColor.ARGB32.color(255, glow, glow, glow));
		poses.popPose();
	}
}
