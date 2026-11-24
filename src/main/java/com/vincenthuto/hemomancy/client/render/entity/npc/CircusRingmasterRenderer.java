package com.vincenthuto.hemomancy.client.render.entity.npc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusRingmasterModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomFigures;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomPose;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class CircusRingmasterRenderer extends MobRenderer<CircusRingmasterEntity, CircusRingmasterModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster.png");
	private static final RenderType GLOW = RenderType.eyes(
			Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster_glow.png"));
	/** His finale fakes: his own body, a little see-through. */
	private static final RenderType FAKE = RenderType.entityTranslucent(TEXTURE);
	private static final float FAKE_ALPHA = 0.5F;
	/** His thread's body: flat colour, drawn solid so it reads on both stripes of the canvas. */
	private static final RenderType THREAD = RenderType.debugQuads();
	/** His thread up into the rigging, the tell that he is the real one: how high it runs, and how it sways. */
	private static final float THREAD_FROM = 1.75F, THREAD_HEIGHT = 10.0F, THREAD_SHOULDER = 0.4F, THREAD_LEAN = 3.0F,
			THREAD_CORE = 0.03F, THREAD_EDGE = 0.07F,
			THREAD_GLOW = 0.2F;
	private static final int THREAD_SEGMENTS = 12;
	/** The phantom audience's stampede: runners per lane, spaced behind the front. */
	private static final int RUNNERS = 3;
	private static final double RUNNER_GAP = 1.3D;
	private static final PhantomFigures.Body[] RUNNER_BODIES = {PhantomFigures.Body.UNDERSTUDY, PhantomFigures.Body.THREADKEEPER,
			PhantomFigures.Body.ACROBAT};
	private boolean drawingFake;

	public CircusRingmasterRenderer(EntityRendererProvider.Context context) {
		super(context, new CircusRingmasterModel(), 0.45F);
		addLayer(new EyesLayer<>(this) {
			@Override public RenderType renderType() { return GLOW; }
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CircusRingmasterEntity entity) {
		return TEXTURE;
	}

	@Override
	protected RenderType getRenderType(CircusRingmasterEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
		return drawingFake ? FAKE : super.getRenderType(entity, bodyVisible, translucent, glowing);
	}

	/**
	 * Sleight's doubles (chunk 15 T15) are not entities: they are drawn here, in his own pose, where the server says
	 * they stand. Nothing can strike them; only he bleeds. In his finale (2026-11-18) the same doubles are his fakes in
	 * the rafters, drawn see-through, with no shadow under them and no thread from the rigging: only he hangs from one.
	 */
	@Override
	public void render(CircusRingmasterEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
		double x = Mth.lerp(partialTick, entity.xo, entity.getX());
		double y = Mth.lerp(partialTick, entity.yo, entity.getY());
		double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
		boolean copy = entity.stageCopy();
		if (copy && entity.finalePhase() == FinalePhase.RAFTERS) thread(entity, poseStack, buffer, partialTick);
		for (Vec3 decoy : entity.doubles()) {
			poseStack.pushPose();
			poseStack.translate(decoy.x - x, decoy.y - y, decoy.z - z);
			if (copy) {
				drawingFake = true;
				try {
					super.render(entity, entityYaw, partialTick, poseStack, type -> new Faded(buffer.getBuffer(type), FAKE_ALPHA),
							packedLight);
				} finally {
					drawingFake = false;
				}
			} else {
				super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
			}
			poseStack.popPose();
		}
		if (copy && entity.finaleMove() == RingmasterFinaleMoves.Move.AUDIENCE_RUSH.ordinal())
			stampede(entity, poseStack, buffer, new Vec3(x, y, z), partialTick);
	}

	/**
	 * His thread: from his shoulder on the ring's side up and out into the dark of the rigging, swaying a little. It
	 * leans out over the ring so the pole he stands against never hides it.
	 */
	private static void thread(CircusRingmasterEntity entity, PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
		float time = AnimationTime.age(entity, partialTick);
		Vec3 centre = com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData.copyCentre();
		Vec3 in = new Vec3(centre.x - entity.getX(), 0.0D, centre.z - entity.getZ());
		in = in.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : in.normalize();
		Vec3 from = in.scale(THREAD_SHOULDER).add(0.0D, THREAD_FROM, 0.0D);
		Vec3 to = in.scale(THREAD_LEAN).add(0.0D, THREAD_FROM + THREAD_HEIGHT, 0.0D);
		Vec3 side = new Vec3(-in.z, 0.0D, in.x);
		Matrix4f matrix = poseStack.last().pose();
		// A dark crimson edge round a bone core, so it shows on the red stripes and the white, and a crimson glow round it.
		VertexConsumer body = buffer.getBuffer(THREAD);
		ribbon(body, matrix, from, to, side, in, time, THREAD_EDGE, 0.36F, 0.03F, 0.07F, 1.0F);
		ribbon(body, matrix, from, to, side, in, time, THREAD_CORE, 0.95F, 0.88F, 0.78F, 1.0F);
		ribbon(buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), matrix, from, to, side, in, time, THREAD_GLOW, 0.9F, 0.1F, 0.15F, 0.35F);
	}

	/** Two crossed strips from {@code from} to {@code to}, {@code width} wide, swaying along {@code side}, fading toward the top. */
	private static void ribbon(VertexConsumer out, Matrix4f matrix, Vec3 from, Vec3 to, Vec3 side, Vec3 in, float time, float width,
			float r, float g, float b, float a) {
		for (int plane = 0; plane < 2; plane++) {
			Vec3 across = (plane == 0 ? side : in).scale(width);
			for (int i = 0; i < THREAD_SEGMENTS; i++) {
				float s0 = i / (float) THREAD_SEGMENTS, s1 = (i + 1) / (float) THREAD_SEGMENTS;
				Vec3 p0 = from.lerp(to, s0).add(side.scale(sway(s0, time))), p1 = from.lerp(to, s1).add(side.scale(sway(s1, time)));
				float a0 = a * (1.0F - 0.6F * s0), a1 = a * (1.0F - 0.6F * s1);
				vertex(out, matrix, p0.subtract(across), r, g, b, a0);
				vertex(out, matrix, p0.add(across), r, g, b, a0);
				vertex(out, matrix, p1.add(across), r, g, b, a1);
				vertex(out, matrix, p1.subtract(across), r, g, b, a1);
			}
		}
	}

	private static void vertex(VertexConsumer out, Matrix4f matrix, Vec3 at, float r, float g, float b, float a) {
		out.addVertex(matrix, (float) at.x, (float) at.y, (float) at.z).setColor(r, g, b, a);
	}

	/** The thread is still where it meets him and at the rigging, and drifts between. */
	private static float sway(float s, float time) {
		return 0.18F * Mth.sin(s * Mth.PI) * Mth.sin(time * 0.08F + s * 2.0F);
	}

	/**
	 * The audience rush: three lanes of phantom runners, crouched at their lane's start from the move's first tick and
	 * dashing across the ring each on its beat, from the marks the server synced (the middle lane's centre, and the
	 * heading with the sweep's side in y). The hits are the server's; these are where they come from.
	 */
	private static void stampede(CircusRingmasterEntity entity, PoseStack poseStack, MultiBufferSource buffer, Vec3 origin,
			float partialTick) {
		Vec3 centre = entity.finaleMarkA(), mark = entity.finaleMarkB();
		Vec3 dir = new Vec3(mark.x, 0.0D, mark.z);
		if (dir.lengthSqr() < 1.0E-6D) return;
		dir = dir.normalize();
		Vec3 side = new Vec3(-dir.z, 0.0D, dir.x).scale(Math.signum(mark.y) == 0 ? 1.0D : Math.signum(mark.y));
		float t = entity.finaleMoveTime(partialTick);
		float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
		List<PhantomFigures.Figure> runners = new ArrayList<>();
		for (int k = 0; k < RingmasterFinaleMoves.RUSH_LANES; k++) {
			Vec3 start = centre.add(side.scale((k - 1) * RingmasterFinaleMoves.RUSH_SPACING))
					.subtract(dir.scale(RingmasterFinaleMoves.RUSH_LANE / 2.0D));
			float run = t - RingmasterFinaleMoves.dashAt(k);
			double end = RingmasterFinaleMoves.RUSH_LANE / RingmasterFinaleMoves.RUSH_SPEED;
			if (run > end + 3.0F) continue;
			float alpha = run < 0.0F ? 0.3F * Math.min(1.0F, t / 6.0F) : 0.7F * (float) Math.min(1.0D, (end + 3.0D - run) / 3.0D);
			for (int j = 0; j < RUNNERS; j++) {
				double along = Math.max(0.0D, Math.min(RingmasterFinaleMoves.RUSH_LANE, run * RingmasterFinaleMoves.RUSH_SPEED) - j * RUNNER_GAP);
				double across = (j - 1) * 0.55D;
				Vec3 at = start.add(dir.scale(along)).add(side.scale(across));
				long seed = k * 31L + j * 7L + entity.getId();
				float stride = run < 0.0F ? 0.0F : Mth.sin(run * 1.9F + j * 1.3F);
				PhantomPose pose = run < 0.0F
						? new PhantomPose(0.0F, 0.2F, 0.55F, 0.6F, 0.6F, 0.15F, -0.15F, 0.0F)
						: new PhantomPose(0.0F, 0.1F, 0.4F, 1.3F * stride, -1.3F * stride, 0.12F, -0.12F, 1.0F * stride);
				runners.add(new PhantomFigures.Figure(RUNNER_BODIES[(k + j) % RUNNER_BODIES.length], PhantomFigures.Kind.STANDING, at,
						yaw, pose, seed, alpha, 0.0D, false));
			}
		}
		if (runners.isEmpty()) return;
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		PhantomFigures.render(poseStack, buffer, origin, camera.getPosition(), camera.getLookVector(), runners, List.of(),
				AnimationTime.gameTime(entity.level(), partialTick));
	}

	@Override
	public boolean shouldRender(CircusRingmasterEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		return frustum.isVisible(entity.getBoundingBoxForCulling().inflate(1.0D));
	}

	/** A buffer that lays every vertex down a little see-through: his fakes. */
	private static final class Faded implements VertexConsumer {
		private final VertexConsumer delegate;
		private final float alpha;

		Faded(VertexConsumer delegate, float alpha) {
			this.delegate = delegate;
			this.alpha = alpha;
		}

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			delegate.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int a) {
			delegate.setColor(red, green, blue, Math.round(a * alpha));
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			delegate.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			delegate.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			delegate.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			delegate.setNormal(x, y, z);
			return this;
		}
	}
}
