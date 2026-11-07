package com.vincenthuto.hemomancy.client.render.entity.boss.putriciel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielFireAnchors;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.vincenthuto.hemomancy.client.render.layer.mob.PutricielBurnLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.PutricielCharLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.PutricielEmberLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.PutricielSporeGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Putriciel at render scale 2.0 (the rig is generated at 0.7, so the atlas lands at 16 px per block): the base model,
 * the char layer (the growths burned, each at its own staggered char), the spore glow, the ember layer (the Flammeus
 * fire, never in the rot) and the burning layer (what burns under the body fire, one region at a time). Each frame it
 * captures the posed attachment points, the limb trails and the seventy fire anchors for the world geometry, and
 * through the ending it sinks the body into the floor where it kneels (the rise's sink played backwards). While the
 * fire burns it lights the body it is on.
 */
public class PutricielRenderer extends MobRenderer<PutricielEntity, PutricielModel<PutricielEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/putriciel/putriciel.png");
	/** How far the body sinks into the tomb by the ending's last beat. */
	private static final float ENDING_SINK = 1.6F;
	/**
	 * Block light never below this on the body: the rot puts the chamber's pyres out, and unlit the blackened iron and
	 * olive mould read as a black outline (Hemorath's pattern).
	 */
	private static final int LIGHT_FLOOR = 8;
	/** The floor while the fire burns on the body (revision R1.7): the fire lights the body it is on. */
	private static final int FIRE_LIGHT_FLOOR = 13;

	private final Vector3f[] anchorPoints = new Vector3f[PutricielFireAnchors.COUNT];
	private final Vector3f[] anchorNormals = new Vector3f[PutricielFireAnchors.COUNT];

	public PutricielRenderer(Context context) {
		super(context, new PutricielModel<>(context.bakeLayer(PutricielModel.LAYER_LOCATION)), 1.2F);
		this.addLayer(new PutricielCharLayer<>(this));
		this.addLayer(new PutricielSporeGlowLayer<>(this));
		this.addLayer(new PutricielEmberLayer<>(this));
		this.addLayer(new PutricielBurnLayer<>(this));
	}

	@Override
	public ResourceLocation getTextureLocation(PutricielEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLightLevel(PutricielEntity entity, BlockPos pos) {
		float fire = PutricielAnimState.of(entity).selfLight();
		int floor = Math.round(Mth.lerp(fire, LIGHT_FLOOR, FIRE_LIGHT_FLOOR));
		return Math.max(floor, super.getBlockLightLevel(entity, pos));
	}

	@Override
	protected void scale(PutricielEntity entity, PoseStack poseStack, float partialTick) {
		SaintRiseRender.sink(poseStack, entity, partialTick);
		// the pose is already flipped here, so down is +Y: the ending sinks the body into the floor
		poseStack.translate(0.0F, endingSink(entity), 0.0F);
		poseStack.scale(PutricielVisualRules.SCALE, PutricielVisualRules.SCALE, PutricielVisualRules.SCALE);
	}

	/** Blocks the ending has sunk the body by. */
	private static float endingSink(PutricielEntity entity) {
		float ending = PutricielAnimState.of(entity).endingTime();
		return ending < 0.0F ? 0.0F : PutricielEndingRules.sink(ending) * ENDING_SINK;
	}

	/**
	 * After the body is drawn, records its posed points (relative to the body, as drawn: the rise's and the ending's
	 * sink included), the world trails of the palm, the censers and the staff head, and the body fire's anchors with
	 * their world normals and velocities, so every effect that follows a limb or stands on the body follows the
	 * animation's own pose.
	 */
	@Override
	public void render(PutricielEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
		super.render(entity, yaw, partial, poses, buffers, light);
		float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
		float sunk = SaintRise.depth(entity.riseTime(partial), entity.getBbHeight()) + endingSink(entity);
		Vector3f[] sacPoints = model.sacPoints();
		Vec3[] sacs = new Vec3[sacPoints.length];
		for (int i = 0; i < sacPoints.length; i++) sacs[i] = offset(sacPoints[i], bodyYaw, sunk);
		Vector3f[] censerPoints = model.censers();
		Vec3[] censers = new Vec3[censerPoints.length];
		for (int i = 0; i < censerPoints.length; i++) censers[i] = offset(censerPoints[i], bodyYaw, sunk);
		PutricielAnimState state = PutricielAnimState.of(entity);
		Vec3 origin = entity.getPosition(partial);
		float age = AnimationTime.age(entity, partial);
		state.attachments(offset(model.core(), bodyYaw, sunk), offset(model.palm(), bodyYaw, sunk),
				offset(model.faceAnchor(), bodyYaw, sunk), offset(model.staffFootPoint(), bodyYaw, sunk),
				offset(model.staffHeadPoint(), bodyYaw, sunk), sacs, censers, origin, age);
		model.fireAnchors(anchorPoints, anchorNormals);
		Vec3[] points = new Vec3[anchorPoints.length];
		Vec3[] normals = new Vec3[anchorNormals.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = offset(anchorPoints[i], bodyYaw, sunk);
			normals[i] = direction(anchorNormals[i], bodyYaw);
		}
		state.recordFireAnchors(points, normals, origin, age);
	}

	private static Vec3 offset(Vector3f point, float yaw, float sunk) {
		float scale = PutricielVisualRules.SCALE;
		point.set(-point.x * scale, (1.501F - point.y) * scale - sunk, point.z * scale);
		return new Vec3(point.rotateY((180 - yaw) * Mth.DEG_TO_RAD));
	}

	/** A model-space direction carried into the world as {@link #offset} carries a point: flipped, then turned with the body. */
	private static Vec3 direction(Vector3f normal, float yaw) {
		Vector3f world = new Vector3f(-normal.x, -normal.y, normal.z).rotateY((180 - yaw) * Mth.DEG_TO_RAD);
		return new Vec3(world).normalize();
	}
}
