package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.shared.ChamberGateRenderer;
import com.vincenthuto.hemomancy.client.render.tile.shared.LidlessDoorRenderer;
import com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot;
import com.vincenthuto.hemomancy.common.encounter.ChamberRoomShape;
import com.vincenthuto.hemomancy.common.encounter.HuskOutsideSnapshot;
import com.vincenthuto.hemomancy.common.tile.shared.ChamberGateBlockEntity;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;

/**
 * The Chamber of Will seen through a husk's open eye: a stand-in of the owner's room, drawn from the husk's snapshot
 * into a half-size texture from a camera placed in the Chamber the way the viewer stands at the husk
 * ({@link ChamberProxyRules}), which the eye then samples by screen position. One eye a frame, the nearest asked for:
 * the husk renderer asks while it draws, the pass runs at the next frame's sky stage, before any block entity.
 */
public final class ChamberProxyView {
	private static final int FULL_BRIGHT = 0xF000F0;
	private static final double REACH = 48.0D;
	private static final Vec3 EYE = new Vec3(0.5D, 1.77D, 0.5D);

	private record Request(BlockPos husk, Direction facing, @Nullable ChamberProxySnapshot snapshot, double distance, double depth,
			@Nullable HuskOutsideSnapshot outside) {
	}

	private record Live(BlockPos husk, int texture) {
	}

	/** A camera that can be placed: the sky effects read its rotation, nothing reads its level. */
	private static final class ProxyCamera extends Camera {
		void place(Vec3 position, float yaw, float pitch) {
			setPosition(position);
			setRotation(yaw, pitch);
		}
	}

	@Nullable
	private static Request pending;
	@Nullable
	private static Live live;
	@Nullable
	private static TextureTarget target;
	@Nullable
	private static VertexBuffer blocks;
	private static long bakedStamp = Long.MIN_VALUE;
	@Nullable
	private static BlockPos bakedCell;
	@Nullable
	private static ArborOfWillEntity standIn;
	private static final ProxyCamera CAMERA = new ProxyCamera();

	private ChamberProxyView() {
	}

	/** A husk whose open eye is being drawn asks for the view; the nearest asker this frame gets it next frame. */
	public static void request(SanguineMonolithBlockEntity husk, Direction facing) {
		ChamberProxySnapshot snapshot = husk.proxy();
		if (snapshot == null) return;
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		double distance = camera.distanceTo(Vec3.atCenterOf(husk.getBlockPos()));
		if (distance > REACH) return;
		if (pending == null || distance < pending.distance()) pending = new Request(husk.getBlockPos(), facing, snapshot, distance, 0.0D, null);
	}

	/**
	 * The local player is walking in through this husk: the view is drawn from the walk's camera, carried {@code depth}
	 * blocks in through the gate. It takes the frame over any eye.
	 */
	public static void requestCrossing(SanguineMonolithBlockEntity husk, Direction facing, double depth) {
		ChamberProxySnapshot snapshot = husk.proxy();
		if (snapshot == null) return;
		pending = new Request(husk.getBlockPos(), facing, snapshot, -1.0D, depth, null);
	}

	/**
	 * The local player is walking back out through this gate: the overworld around its husk is drawn from a camera
	 * carried {@code depth} blocks out of the husk along its facing. The texture is keyed by the gate's position.
	 */
	/** A gate whose open eye is being drawn asks for the overworld around its husk, from where the viewer stands. */
	public static void requestGate(ChamberGateBlockEntity gate) {
		HuskOutsideSnapshot outside = gate.outside();
		if (outside == null) return;
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		double distance = camera.distanceTo(Vec3.atCenterOf(gate.getBlockPos()));
		if (distance > REACH) return;
		if (pending == null || distance < pending.distance()) pending = new Request(gate.getBlockPos(), outside.facing(), null, distance, 0.0D, outside);
	}

	public static void requestReturn(ChamberGateBlockEntity gate, double depth) {
		HuskOutsideSnapshot outside = gate.outside();
		if (outside == null) return;
		pending = new Request(gate.getBlockPos(), outside.facing(), null, -1.0D, depth, outside);
	}

	/**
	 * Hands the view's target over, if it holds {@code key}'s view: the caller owns it from here (the veil over a
	 * dimension change keeps the walk's last frame through the level unload), and the view makes itself a new one.
	 */
	@Nullable
	public static TextureTarget detachTarget(BlockPos key) {
		if (live == null || !live.husk().equals(key) || target == null) return null;
		TextureTarget kept = target;
		target = null;
		live = null;
		return kept;
	}

	/** The texture holding this husk's view this frame, or 0 when there is none yet. */
	public static int textureFor(BlockPos husk) {
		return live != null && live.husk().equals(husk) ? live.texture() : 0;
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
		Request request = pending;
		pending = null;
		live = null;
		if (request == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		RenderTarget main = mc.getMainRenderTarget();
		// A shader pack draws the world into its own targets; this pass would land in the wrong one.
		if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != main.frameBufferId) return;
		ensureTarget(main);
		if (request.outside() != null) {
			renderOutside(mc, main, request, event);
			return;
		}
		ChamberProxySnapshot snapshot = request.snapshot();
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		Camera real = event.getCamera();
		Vec3 huskEye = Vec3.atLowerCornerOf(request.husk()).add(EYE);
		BlockPos gate = snapshot.cell().offset(0, 1, ChamberRoomShape.reach(snapshot.radius()));
		Vec3 gateEye = Vec3.atLowerCornerOf(gate).add(EYE);
		ChamberProxyRules.Placement placement = ChamberProxyRules.place(request.facing().toYRot(),
				triple(huskEye), triple(gateEye), triple(real.getPosition()), real.getYRot(), real.getXRot(), request.depth());
		Vec3 camera = new Vec3(placement.x(), placement.y(), placement.z());
		CAMERA.place(camera, placement.yaw(), placement.pitch());
		Matrix4f modelView = new Matrix4f().rotation(CAMERA.rotation().conjugate(new Quaternionf()));
		Matrix4f projection = event.getProjectionMatrix();

		float fogStart = RenderSystem.getShaderFogStart();
		float fogEnd = RenderSystem.getShaderFogEnd();
		float[] fogColor = RenderSystem.getShaderFogColor().clone();
		var fogShape = RenderSystem.getShaderFogShape();
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();
		target.bindWrite(true);
		RenderSystem.depthMask(true);
		RenderSystem.clearColor(0.01F, 0.0F, 0.005F, 1.0F);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
		try {
			RenderSystem.setShaderFogStart(Float.MAX_VALUE);
			RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			// The sky as the level draws it: the camera's rotation handed over, the model-view stack left alone.
			ChamberSkyThemeRegistry.withTheme(snapshot.theme(), () -> renderSky(mc.level, snapshot, partialTick, modelView, projection));
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			renderBlocks(mc, snapshot.blocks(), snapshot.cell(), snapshot.stamp(), camera, modelView, projection);
			// The rest as the level draws its entities and block entities: the rotation on the model-view stack.
			modelViewStack.mul(modelView);
			RenderSystem.applyModelViewMatrix();
			MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
			float time = AnimationTime.gameTime(mc.level, partialTick);
			float seed = LidlessDoorRenderer.seed(gate);
			PoseStack pose = new PoseStack();
			pose.pushPose();
			pose.translate(gate.getX() - camera.x, gate.getY() - camera.y, gate.getZ() - camera.z);
			// The gate's ribs, but not its eye: that eye is the window this view is seen through.
			ChamberGateRenderer.drawRibs(pose, buffers, snapshot.radius(), time, seed);
			pose.popPose();
			buffers.endBatch();
			renderArbor(mc, snapshot, camera, partialTick, buffers);
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setShaderFogStart(fogStart);
			RenderSystem.setShaderFogEnd(fogEnd);
			RenderSystem.setShaderFogColor(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
			RenderSystem.setShaderFogShape(fogShape);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			main.bindWrite(true);
		}
		live = new Live(request.husk(), target.getColorTextureId());
	}

	/** The view's own target, for review tooling that wants to look at what the eye samples. */
	@Nullable
	public static RenderTarget target() {
		return target;
	}

	/** The walk back: the overworld around the husk under its sky's colour at its hour, from the walk's camera. */
	private static void renderOutside(Minecraft mc, RenderTarget main, Request request, RenderLevelStageEvent event) {
		HuskOutsideSnapshot outside = request.outside();
		Camera real = event.getCamera();
		Vec3 gateEye = Vec3.atLowerCornerOf(request.husk()).add(EYE);
		Vec3 huskEye = Vec3.atLowerCornerOf(outside.husk()).add(EYE);
		ChamberProxyRules.Placement placement = ChamberProxyRules.placeOutside(outside.facing().toYRot(), triple(gateEye),
				triple(huskEye), triple(real.getPosition()), real.getYRot(), real.getXRot(), request.depth());
		Vec3 camera = new Vec3(placement.x(), placement.y(), placement.z());
		CAMERA.place(camera, placement.yaw(), placement.pitch());
		Matrix4f modelView = new Matrix4f().rotation(CAMERA.rotation().conjugate(new Quaternionf()));
		Matrix4f projection = event.getProjectionMatrix();
		float fogStart = RenderSystem.getShaderFogStart();
		float fogEnd = RenderSystem.getShaderFogEnd();
		float[] fogColor = RenderSystem.getShaderFogColor().clone();
		var fogShape = RenderSystem.getShaderFogShape();
		float bright = HuskOutsideSnapshot.skyBrightness(outside.timeOfDay());
		int sky = outside.skyColor();
		// The sky overhead, and the haze at the horizon the level's own fog would show: the snapshot ends a dozen blocks
		// out, so the blocks fog out into that haze and the sky grades down into it, and nothing ends against a void.
		float zenithR = ((sky >> 16) & 0xFF) / 255.0F * bright;
		float zenithG = ((sky >> 8) & 0xFF) / 255.0F * bright;
		float zenithB = (sky & 0xFF) / 255.0F * bright;
		float hazeR = (zenithR * 0.45F + 0.78F * 0.55F) * (0.3F + 0.7F * bright);
		float hazeG = (zenithG * 0.45F + 0.84F * 0.55F) * (0.3F + 0.7F * bright);
		float hazeB = (zenithB * 0.45F + 0.92F * 0.55F) * (0.3F + 0.7F * bright);
		target.bindWrite(true);
		RenderSystem.depthMask(true);
		RenderSystem.clearColor(hazeR, hazeG, hazeB, 1.0F);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
		try {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			skyDome(modelView, zenithR, zenithG, zenithB, hazeR, hazeG, hazeB);
			RenderSystem.setShaderFogStart(HuskOutsideSnapshot.RADIUS * 0.35F);
			RenderSystem.setShaderFogEnd(HuskOutsideSnapshot.RADIUS * 0.92F);
			RenderSystem.setShaderFogColor(hazeR, hazeG, hazeB, 1.0F);
			RenderSystem.setShaderFogShape(com.mojang.blaze3d.shaders.FogShape.SPHERE);
			// The blocks are baked full bright with no face shading, so they are held well under the sky's brightness.
			float lit = bright * 0.6F + 0.08F;
			RenderSystem.setShaderColor(lit, lit, lit, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			renderBlocks(mc, outside.blocks(), outside.husk(), outside.stamp(), camera, modelView, projection);
		} finally {
			RenderSystem.setShaderFogStart(fogStart);
			RenderSystem.setShaderFogEnd(fogEnd);
			RenderSystem.setShaderFogColor(fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
			RenderSystem.setShaderFogShape(fogShape);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			main.bindWrite(true);
		}
		live = new Live(request.husk(), target.getColorTextureId());
	}

	/**
	 * A graded sky about the camera, drawn before the blocks with no depth: the zenith colour overhead running down into
	 * the haze a little above the horizon, the haze below it, and a dimmer haze underfoot. Vertices are camera-relative
	 * directions, turned by the camera's rotation here, so the model-view stack is left alone.
	 */
	private static void skyDome(Matrix4f modelView, float zr, float zg, float zb, float hr, float hg, float hb) {
		final int segments = 24;
		final float reach = 48.0F;
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float[] rings = { 1.0F, 0.14F, -0.08F, -1.0F };
		float[][] colours = { { zr, zg, zb }, { hr, hg, hb }, { hr, hg, hb }, { hr * 0.55F, hg * 0.55F, hb * 0.55F } };
		org.joml.Vector3f v = new org.joml.Vector3f();
		for (int band = 0; band < rings.length - 1; band++) {
			float y0 = rings[band];
			float y1 = rings[band + 1];
			float r0 = (float) Math.sqrt(Math.max(0.0F, 1.0F - y0 * y0));
			float r1 = (float) Math.sqrt(Math.max(0.0F, 1.0F - y1 * y1));
			float[] c0 = colours[band];
			float[] c1 = colours[band + 1];
			for (int i = 0; i < segments; i++) {
				double a0 = Math.PI * 2.0 * i / segments;
				double a1 = Math.PI * 2.0 * (i + 1) / segments;
				domeVertex(builder, modelView, v, (float) Math.cos(a0) * r0, y0, (float) Math.sin(a0) * r0, reach, c0);
				domeVertex(builder, modelView, v, (float) Math.cos(a1) * r0, y0, (float) Math.sin(a1) * r0, reach, c0);
				domeVertex(builder, modelView, v, (float) Math.cos(a1) * r1, y1, (float) Math.sin(a1) * r1, reach, c1);
				domeVertex(builder, modelView, v, (float) Math.cos(a0) * r1, y1, (float) Math.sin(a0) * r1, reach, c1);
			}
		}
		MeshData mesh = builder.build();
		if (mesh != null) BufferUploader.drawWithShader(mesh);
		RenderSystem.enableCull();
	}

	private static void domeVertex(BufferBuilder builder, Matrix4f modelView, org.joml.Vector3f v, float x, float y, float z,
			float reach, float[] colour) {
		v.set(x * reach, y * reach, z * reach);
		modelView.transformPosition(v);
		builder.addVertex(v.x, v.y, v.z).setColor(colour[0], colour[1], colour[2], 1.0F);
	}

	private static double[] triple(Vec3 v) {
		return new double[] { v.x, v.y, v.z };
	}

	private static void ensureTarget(RenderTarget main) {
		int width = Math.max(1, main.width / 2);
		int height = Math.max(1, main.height / 2);
		if (target != null && target.width == width && target.height == height) return;
		if (target != null) target.destroyBuffers();
		target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		target.setFilterMode(GL11.GL_LINEAR);
	}

	private static void renderSky(ClientLevel level, ChamberProxySnapshot snapshot, float partialTick, Matrix4f modelView,
			Matrix4f projection) {
		ChamberSkyTheme theme = ChamberSkyThemeRegistry.byId(snapshot.theme());
		ChamberThemeEffects effects = ChamberSkyThemeRegistry.effectsById(snapshot.theme());
		int ticks = (int) (level.getGameTime() % 1_000_000L);
		float f = (ticks + partialTick) * theme.motionMultiplier();
		PoseStack sky = new PoseStack();
		sky.mulPose(modelView);
		ChamberThemeRenderContext context = new ChamberThemeRenderContext(level, ticks, partialTick, modelView, CAMERA,
				projection, false, () -> {
				}, sky, Tesselator.getInstance(), theme, f, 128.0F,
				ChamberOfWillRenderHelpers.membranePulse(f) * theme.pulseStrength(), snapshot.pomes());
		effects.renderSky(context);
	}

	/** A snapshot's blocks about their anchor, baked once per stamp into one buffer and drawn with the cutout block shader. */
	private static void renderBlocks(Minecraft mc, long[] packed, BlockPos cell, long stamp, Vec3 camera, Matrix4f modelView,
			Matrix4f projection) {
		if (blocks == null || bakedStamp != stamp || !cell.equals(bakedCell)) bake(mc, packed, cell, stamp);
		if (blocks == null) return;
		Matrix4f pose = new Matrix4f(modelView).translate((float) (cell.getX() - camera.x), (float) (cell.getY() - camera.y),
				(float) (cell.getZ() - camera.z));
		RenderType type = RenderType.cutoutMipped();
		type.setupRenderState();
		blocks.bind();
		blocks.drawWithShader(pose, projection, RenderSystem.getShader());
		VertexBuffer.unbind();
		type.clearRenderState();
	}

	private static void bake(Minecraft mc, long[] packedBlocks, BlockPos cell, long stamp) {
		if (blocks != null) blocks.close();
		blocks = null;
		bakedStamp = stamp;
		bakedCell = cell;
		if (packedBlocks.length == 0) return;
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
		RandomSource random = RandomSource.create(42L);
		try (ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 20)) {
			BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			PoseStack pose = new PoseStack();
			for (long packed : packedBlocks) {
				BlockState state = Block.stateById(ChamberProxySnapshot.stateId(packed));
				if (state.isAir()) continue;
				BakedModel model = dispatcher.getBlockModel(state);
				// Tinted quads (grass, leaves) take the block's default tint: there is no level here to read a biome from.
				int tint = mc.getBlockColors().getColor(state, null, null, 0);
				float red = tint == -1 ? 1.0F : ((tint >> 16) & 0xFF) / 255.0F;
				float green = tint == -1 ? 1.0F : ((tint >> 8) & 0xFF) / 255.0F;
				float blue = tint == -1 ? 1.0F : (tint & 0xFF) / 255.0F;
				pose.pushPose();
				pose.translate(ChamberProxySnapshot.dx(packed), ChamberProxySnapshot.dy(packed), ChamberProxySnapshot.dz(packed));
				for (RenderType layer : model.getRenderTypes(state, random, ModelData.EMPTY)) {
					dispatcher.getModelRenderer().renderModel(pose.last(), builder, state, model, red, green, blue, FULL_BRIGHT,
							OverlayTexture.NO_OVERLAY, ModelData.EMPTY, layer);
				}
				pose.popPose();
			}
			MeshData mesh = builder.build();
			if (mesh == null) return;
			VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			buffer.bind();
			buffer.upload(mesh);
			VertexBuffer.unbind();
			blocks = buffer;
		}
	}

	private static void renderArbor(Minecraft mc, ChamberProxySnapshot snapshot, Vec3 camera, float partialTick,
			MultiBufferSource.BufferSource buffers) {
		if (standIn == null || standIn.level() != mc.level) {
			standIn = EntityInit.arbor_of_will.get().create(mc.level);
			if (standIn == null) return;
		}
		standIn.configureStandIn(snapshot.degree(), snapshot.pomes(), snapshot.radius());
		// Its clock is the level's, so the arbor breathes at the level's pace whatever the frame rate.
		standIn.tickCount = (int) (mc.level.getGameTime() % 1_000_000L);
		BlockPos cell = snapshot.cell();
		Vec3 at = new Vec3(cell.getX() + 0.5D, cell.getY() + 1.0D, cell.getZ() + 0.5D);
		standIn.setPos(at.x, at.y, at.z);
		EntityRenderer<? super ArborOfWillEntity> renderer = mc.getEntityRenderDispatcher().getRenderer(standIn);
		PoseStack pose = new PoseStack();
		pose.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
		renderer.render(standIn, 0.0F, partialTick, pose, buffers, FULL_BRIGHT);
		buffers.endBatch();
	}

	/** After a resource reload the atlas has moved: the room's blocks are baked again on the next draw. */
	public static void invalidateBake() {
		bakedStamp = Long.MIN_VALUE;
	}

	/** With the level: the target, the baked room and the stand-in are gone. */
	public static void clear() {
		pending = null;
		live = null;
		if (target != null) target.destroyBuffers();
		target = null;
		if (blocks != null) blocks.close();
		blocks = null;
		bakedStamp = Long.MIN_VALUE;
		bakedCell = null;
		standIn = null;
	}
}
