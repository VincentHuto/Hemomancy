package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.mojang.blaze3d.platform.Lighting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.HarbingerChromeRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.Supplier;

public final class BestiaryEntityPreview {
	public static final float ZOOM_MIN = 0.5F;
	public static final float ZOOM_MAX = 2.75F;
	private static final int ANIMATION_LOOP_TICKS = 240;
	private static final float PREVIEW_GUI_DEPTH = 150.0F;

	private static final Map<ResourceLocation, Supplier<? extends EntityType<? extends LivingEntity>>> PREVIEW_TYPES = Map.ofEntries(
			Map.entry(Hemomancy.rloc("barbed_urchin"), EntityInit.barbed_urchin),
			Map.entry(Hemomancy.rloc("blood_lantern_jelly"), EntityInit.blood_lantern_jelly),
			Map.entry(Hemomancy.rloc("chalybeate_snail"), EntityInit.chalybeate_snail),
			Map.entry(Hemomancy.rloc("chitinite"), EntityInit.chitinite),
			Map.entry(Hemomancy.rloc("crimson_doe"), EntityInit.crimson_doe),
			Map.entry(Hemomancy.rloc("desiccant"), EntityInit.desiccant),
			Map.entry(Hemomancy.rloc("fervent_chitinite"), EntityInit.fervent_chitinite),
			Map.entry(Hemomancy.rloc("hematic_burrower"), EntityInit.hematic_burrower),
			Map.entry(Hemomancy.rloc("hemojelly"), EntityInit.hemojelly),
			Map.entry(Hemomancy.rloc("hemolymphopoda"), EntityInit.hemolymphopoda),
			Map.entry(Hemomancy.rloc("lantern_tick"), EntityInit.lantern_tick),
			Map.entry(Hemomancy.rloc("morphling_polyp"), EntityInit.morphling_polyp),
			Map.entry(Hemomancy.rloc("prism_cuttle"), EntityInit.prism_cuttle),
			Map.entry(Hemomancy.rloc("scarlet_serpent"), EntityInit.scarlet_serpent),
			Map.entry(Hemomancy.rloc("tooth_pecks"), EntityInit.tooth_pecks),
			Map.entry(Hemomancy.rloc("venom_rib_centipede"), EntityInit.venom_rib_centipede),
			Map.entry(Hemomancy.rloc("venous_strider"), EntityInit.venous_strider),
			Map.entry(Hemomancy.rloc("verdigris_moth"), EntityInit.verdigris_moth));

	private BestiaryEntityPreview() {
	}

	public static boolean hasPreview(ResourceLocation id) {
		return id != null && PREVIEW_TYPES.containsKey(id);
	}

	public static boolean hasPreviewPath(String path) {
		return path != null && hasPreview(Hemomancy.rloc(path));
	}

	public static float clampZoom(float zoom) {
		return Mth.clamp(zoom, ZOOM_MIN, ZOOM_MAX);
	}

	static float animationAgeTicks(long gameTime, float partialTick) {
		return gameTime + partialTick;
	}

	static float previewAnimationTick(long gameTime, float partialTick) {
		return gameTime % ANIMATION_LOOP_TICKS + partialTick;
	}

	static float previewGuiDepth() {
		return PREVIEW_GUI_DEPTH;
	}

	public static void render(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state,
			BestiaryTabState.Entry entry, int left, int top, int right, int bottom) {
		LivingEntity entity = previewEntity(state, entry);
		if (entity == null) {
			drawPreviewFallback(gfx, ctx, left, top, right, bottom);
			return;
		}

		int centerX = Math.round((left + right) / 2.0F + state.previewPanX);
		int centerY = Math.round(top + (bottom - top) * 0.58F + state.previewPanY);
		int scale = Math.round(previewEntityScale(entity, bottom - top) * state.previewZoom);
		float yaw = 180.0F + (float) Math.toDegrees(state.previewRotationAngle);
		float pitch = (float) Math.toDegrees(state.previewPitchAngle);
		PreviewRotations rotations = capturePreviewRotations(entity);
		boolean scissorEnabled = false;
		try {
			float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
			float animationAge = previewAnimationTick(entity.level().getGameTime(), partialTick);
			applyPreviewRotation(entity, yaw, pitch);
			applyPreviewAnimationAge(entity, animationAge);
			gfx.enableScissor(left, top, right, bottom);
			scissorEnabled = true;
			float entityScale = entity.getScale();
			Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
			Quaternionf pose = new Quaternionf()
					.rotateZ((float) Math.PI)
					.rotateX(state.previewPitchAngle);
			renderEntityInDeepPreview(gfx, centerX, centerY, scale / entityScale, translate, pose,
					new Quaternionf(), entity);
			gfx.disableScissor();
			scissorEnabled = false;
		} catch (Throwable ignored) {
			if (scissorEnabled) {
				gfx.disableScissor();
			}
			drawPreviewFallback(gfx, ctx, left, top, right, bottom);
		} finally {
			restorePreviewRotations(entity, rotations);
		}
	}

	private static LivingEntity previewEntity(BestiaryTabState state, BestiaryTabState.Entry entry) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || entry == null || !entry.discovered()) {
			return null;
		}
		ResourceLocation id = entry.previewId();
		Supplier<? extends EntityType<? extends LivingEntity>> type = PREVIEW_TYPES.get(id);
		if (type == null) {
			return null;
		}
		String key = id.toString();
		if (state.previewEntity != null && key.equals(state.previewKey) && state.previewLevel == mc.level) {
			return state.previewEntity;
		}
		LivingEntity entity = type.get().create(mc.level);
		if (entity != null) {
			if (entity instanceof Mob mob) {
				mob.setNoAi(true);
			}
			entity.setYRot(180.0F);
			entity.setXRot(0.0F);
		}
		state.previewKey = key;
		state.previewLevel = mc.level;
		state.previewEntity = entity;
		return entity;
	}

	private static int previewEntityScale(LivingEntity entity, int availableH) {
		float entityH = Math.max(entity.getBbHeight(), 0.75F);
		int fitScale = Mth.floor((availableH - 22) / entityH);
		return Mth.clamp(fitScale, 18, 54);
	}

	private static void drawPreviewFallback(GuiGraphics gfx, ProgressScreenContext ctx, int left, int top,
			int right, int bottom) {
		int centerX = (left + right) / 2;
		int centerY = (top + bottom) / 2;
		ScreenDrawUtils.drawSimpleBorder(gfx, left, top, right - left, bottom - top, 0xFF24442D);
		HarbingerChromeRenderer.drawFrame(gfx, left, top, right - left, bottom - top, 0xFF77AA66,
				HarbingerChromeRenderer.State.ACTIVE);
		gfx.drawCenteredString(ctx.font(), Component.literal("Preview unavailable"), centerX, centerY - 4, 0xFF777777);
	}

	private record PreviewRotations(float yBodyRot, float yRot, float xRot, int tickCount,
			float yHeadRotO, float yHeadRot) {
	}

	private static PreviewRotations capturePreviewRotations(LivingEntity entity) {
		return new PreviewRotations(entity.yBodyRot, entity.getYRot(), entity.getXRot(), entity.tickCount,
				entity.yHeadRotO, entity.yHeadRot);
	}

	private static void applyPreviewRotation(LivingEntity entity, float yaw, float pitch) {
		entity.yBodyRot = yaw;
		entity.setYRot(yaw);
		entity.setXRot(Mth.clamp(pitch, -75.0F, 75.0F));
		entity.yHeadRot = yaw;
		entity.yHeadRotO = yaw;
	}

	private static void applyPreviewAnimationAge(LivingEntity entity, float animationAge) {
		int tick = Mth.floor(animationAge);
		entity.tickCount = tick;
	}

	private static void renderEntityInDeepPreview(GuiGraphics gfx, float x, float y, float scale,
			Vector3f translate, Quaternionf pose, Quaternionf cameraOrientation, LivingEntity entity) {
		gfx.pose().pushPose();
		gfx.pose().translate(x, y, PREVIEW_GUI_DEPTH);
		gfx.pose().scale(scale, scale, -scale);
		gfx.pose().translate(translate.x, translate.y, translate.z);
		gfx.pose().mulPose(pose);
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		dispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
		dispatcher.setRenderShadow(false);
		try {
			dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, gfx.pose(), gfx.bufferSource(), 15728880);
			gfx.flush();
		} finally {
			dispatcher.setRenderShadow(true);
			gfx.pose().popPose();
			Lighting.setupFor3DItems();
		}
	}

	private static void restorePreviewRotations(LivingEntity entity, PreviewRotations rotations) {
		entity.yBodyRot = rotations.yBodyRot();
		entity.setYRot(rotations.yRot());
		entity.setXRot(rotations.xRot());
		entity.tickCount = rotations.tickCount();
		entity.yHeadRotO = rotations.yHeadRotO();
		entity.yHeadRot = rotations.yHeadRot();
	}
}
