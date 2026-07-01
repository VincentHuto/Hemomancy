package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneEntity;
import com.vincenthuto.hemomancy.common.tile.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SpecimenJarRenderer implements BlockEntityRenderer<SpecimenJarBlockEntity> {
	private static final float INNER_JAR_FIT = 0.52F;
	private static final float MAX_SPECIMEN_SCALE = 0.75F;
	private static final float DEFAULT_SPECIMEN_SCALE = 0.45F;

	private Entity cachedEntity;
	private long cachedAnimationTick = Long.MIN_VALUE;
	private String cachedKey = "";

	public SpecimenJarRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SpecimenJarBlockEntity jar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		if (!jar.hasSpecimen()) {
			return;
		}

		Direction facing = jar.getBlockState().getValue(SpecimenJarBlock.FACING);
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F - facing.toYRot()).toMoj());
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		renderSpecimen(jar.getLevel(), jar.getSpecimenCopy(), partialTicks, poseStack, buffer, combinedLight);
		poseStack.popPose();
	}

	public void renderSpecimen(Level level, CompoundTag specimen, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight) {
		Entity entity = getOrCreateEntity(level, specimen);
		if (entity == null) {
			return;
		}
		updateRenderAnimation(entity, level);

		float scale = specimenFitScale(entity);
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.12D, 0.5D);
		poseStack.scale(scale, scale, scale);
		applySpecimenVisualOffset(entity, poseStack);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F).toMoj());
		Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D,
				0.0F, partialTicks, poseStack, buffer, combinedLight);
		poseStack.popPose();
	}

	private static float specimenFitScale(Entity entity) {
		float footprint = specimenVisualFootprint(entity);
		if (footprint <= 0.0F) {
			return DEFAULT_SPECIMEN_SCALE;
		}
		return Math.min(INNER_JAR_FIT / footprint, MAX_SPECIMEN_SCALE);
	}

	private static float specimenVisualFootprint(Entity entity) {
		AABB bounds = entity.getBoundingBox();
		float boundsFootprint = (float) Math.max(bounds.getYsize(), Math.max(bounds.getXsize(), bounds.getZsize()));
		float vanillaFootprint = Math.max(entity.getBbWidth(), entity.getBbHeight());
		return Math.max(Math.max(boundsFootprint, vanillaFootprint),
				Math.max(longBodiedVisualLength(entity), broadSpecimenVisualWidth(entity)));
	}

	private static float longBodiedVisualLength(Entity entity) {
		String path = entityPath(entity);
		if (path.contains("centipede")) {
			return Math.max(entity.getBbWidth() * 2.65F, entity.getBbHeight() * 6.0F);
		}
		if (isLongBodiedPath(path)) {
			return Math.max(entity.getBbWidth() * 1.9F, entity.getBbHeight() * 2.6F);
		}
		return 0.0F;
	}

	private static float broadSpecimenVisualWidth(Entity entity) {
		String path = entityPath(entity);
		if (path.contains("chitinite")) {
			return Math.max(entity.getBbWidth() * 1.6F, entity.getBbHeight() * 3.1F);
		}
		if (path.contains("tooth_pecks")) {
			return Math.max(entity.getBbWidth() * 1.75F, entity.getBbHeight() * 2.7F);
		}
		return 0.0F;
	}

	private static void applySpecimenVisualOffset(Entity entity, PoseStack poseStack) {
		String path = entityPath(entity);
		if (path.contains("centipede")) {
			poseStack.translate(0.0D, 0.0D, -entity.getBbWidth() * 0.45D);
		} else if (isLongBodiedPath(path)) {
			poseStack.translate(0.0D, 0.0D, -entity.getBbWidth() * 0.15D);
		}
	}

	private static boolean isLongBodiedPath(String path) {
		return path.contains("chthonian") || path.contains("serpent") || path.contains("snake")
				|| path.contains("worm") || path.contains("leech") || path.contains("borer");
	}

	private static String entityPath(Entity entity) {
		ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return key == null ? "" : key.getPath();
	}

	private Entity getOrCreateEntity(Level level, CompoundTag specimen) {
		if (level == null || specimen == null || specimen.isEmpty()) {
			return null;
		}
		String key = specimen.toString();
		if (cachedEntity != null && key.equals(cachedKey)) {
			return cachedEntity;
		}
		cachedAnimationTick = Long.MIN_VALUE;
		CompoundTag renderTag = specimen.copy();
		ListTag pos = new ListTag();
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		renderTag.put("Pos", pos);
		Optional<Entity> entity = EntityType.create(renderTag, level);
		cachedEntity = entity.orElse(null);
		cachedKey = key;
		if (cachedEntity != null) {
			cachedEntity.setPos(0.0D, 0.0D, 0.0D);
			cachedEntity.setYRot(0.0F);
			cachedEntity.setXRot(0.0F);
		}
		return cachedEntity;
	}

	private void updateRenderAnimation(Entity entity, Level level) {
		long gameTime = level.getGameTime();
		if (cachedAnimationTick == gameTime) {
			return;
		}
		cachedAnimationTick = gameTime;
		int visualTick = (int) (gameTime % Integer.MAX_VALUE);
		entity.tickCount = visualTick;
		entity.setPos(0.0D, 0.0D, 0.0D);
		entity.setYRot(0.0F);
		entity.setXRot(0.0F);

		if (entity instanceof LivingEntity living) {
			freezeCapturedSpecimen(living);
		}
		if (entity instanceof FargoneEntity fargone) {
			fargone.idleAnimationState.startIfStopped(visualTick);
		}
	}

	private static void freezeCapturedSpecimen(LivingEntity living) {
		living.yBodyRot = 0.0F;
		living.yBodyRotO = 0.0F;
		living.yHeadRot = 0.0F;
		living.yHeadRotO = 0.0F;
		living.walkAnimation.setSpeed(0.0F);
		living.walkAnimation.update(0.0F, 1.0F);
	}
}
