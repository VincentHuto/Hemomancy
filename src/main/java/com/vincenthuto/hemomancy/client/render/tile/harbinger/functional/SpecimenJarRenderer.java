package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hutoslib.math.Vector3;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Draws the creature kept in a Specimen Jar, and holds the display logic the Specimen Vat shares (core-loop Track 5).
 * Each vessel keeps its own display entity, rebuilt only when its specimen changes, so many jars of different
 * creatures do not rebuild each other's every frame. Past {@link #ANIMATE_RANGE} blocks the display stops animating,
 * and nothing is drawn past {@link #VIEW_DISTANCE}. A husk lies on its side in the dark; a failing creature rattling
 * its glass shakes.
 */
public class SpecimenJarRenderer implements BlockEntityRenderer<SpecimenJarBlockEntity> {
	private static final String DISPLAY_ENTITY_TAG = "hemomancy:specimen_display";
	private static final float INNER_JAR_FIT = 0.52F;
	private static final float MAX_SPECIMEN_SCALE = 0.75F;
	private static final float DEFAULT_SPECIMEN_SCALE = 0.45F;
	public static final int VIEW_DISTANCE = 48;
	public static final double ANIMATE_RANGE = 16.0D;
	private static final int HUSK_LIGHT = LightTexture.pack(4, 4);
	private static final float HUSK_SHRINK = 0.66F;
	/** Far enough into the death animation that the body lies fully over; the display entity never ticks it on. */
	private static final int HUSK_DEATH_TIME = 19;
	/** One display per vessel; weak, so a vessel that goes away takes its display with it. */
	private static final Map<BlockEntity, Display> DISPLAYS = new WeakHashMap<>();

	/** The item renderer's own display (one jar item at a time). */
	private Entity cachedEntity;
	private long cachedAnimationTick = Long.MIN_VALUE;
	private String cachedKey = "";

	private static final class Display {
		int revision = Integer.MIN_VALUE;
		Entity entity;
		long animationTick = Long.MIN_VALUE;
	}

	public SpecimenJarRenderer(BlockEntityRendererProvider.Context context) {
	}

	public static boolean isDisplayEntity(Entity entity) {
		return entity.getPersistentData().getBoolean(DISPLAY_ENTITY_TAG);
	}

	@Override
	public int getViewDistance() {
		return VIEW_DISTANCE;
	}

	@Override
	public AABB getRenderBoundingBox(SpecimenJarBlockEntity jar) {
		return BroodThreadRenderer.bounds(jar, new AABB(jar.getBlockPos()));
	}

	@Override
	public void render(SpecimenJarBlockEntity jar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		KeptSpecimen kept = jar.kept();
		if (!kept.occupied()) {
			return;
		}
		BroodThreadRenderer.draw(jar, partialTicks, poseStack, buffer);
		Entity entity = display(jar, kept);
		if (entity == null) {
			return;
		}
		Direction facing = jar.getBlockState().getValue(SpecimenJarBlock.FACING);
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F - facing.toYRot()).toMoj());
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		float scale = specimenFitScale(entity);
		drawKept(jar, kept, entity, scale, 0.12D, partialTicks, poseStack, buffer, combinedLight);
		poseStack.popPose();
	}

	/**
	 * Draws a vessel's creature at {@code scale}, standing {@code floor} blocks above the vessel's base in the current
	 * pose (already turned to the vessel's facing and centred at x, z 0.5): animated near the camera, still further
	 * off, lying dark as a husk, shaking while it rattles.
	 */
	public static void drawKept(BlockEntity vessel, KeptSpecimen kept, Entity entity, float scale, double floor,
			float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		drawKept(vessel, kept, entity, scale, floor, null, 0.0F, partialTicks, poseStack, buffer, light);
	}

	/**
	 * As above, but {@code centring} (in the creature's own units, after the half turn and the pitch) replaces the
	 * per-creature offset table when the vessel has measured where its creature's model actually lies, and
	 * {@code pitch} tips a long creature nose-up about its side-to-side axis. A husk ignores both and lies down.
	 */
	public static void drawKept(BlockEntity vessel, KeptSpecimen kept, Entity entity, float scale, double floor,
			Vec3 centring, float pitch, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
		Level level = vessel.getLevel();
		if (level == null) return;
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		boolean near = camera.distanceToSqr(Vec3.atCenterOf(vessel.getBlockPos())) <= ANIMATE_RANGE * ANIMATE_RANGE;
		Display display = DISPLAYS.get(vessel);
		if (near && !kept.husk() && display != null) updateRenderAnimation(entity, level, display);
		poseStack.pushPose();
		poseStack.translate(0.5D, floor, 0.5D);
		float shake = kept.shake(level.getGameTime(), partialTicks);
		if (shake > 0.0F) {
			float time = AnimationTime.gameTime(level, partialTicks);
			poseStack.translate(Math.sin(time * 2.7F) * 0.035F * shake, 0.0D, Math.cos(time * 3.1F) * 0.03F * shake);
		}
		// What is left of a husk has fallen, shrunk and darkened: the death pose and tint, at most two thirds the size.
		float shown = kept.husk() ? scale * HUSK_SHRINK : scale;
		// A young just born quickens: it swells and settles in its glass for a moment and a half (P6).
		float quickening = kept.quickening(level.getGameTime(), partialTicks);
		if (quickening > 0.0F) {
			float time = AnimationTime.gameTime(level, partialTicks);
			shown *= 1.0F + 0.18F * quickening * Math.abs(net.minecraft.util.Mth.sin(time * 0.55F));
		}
		if (entity instanceof LivingEntity living) living.deathTime = kept.husk() ? HUSK_DEATH_TIME : 0;
		poseStack.scale(shown, shown, shown);
		if (centring == null || kept.husk()) applySpecimenVisualOffset(entity, poseStack);
		else {
			poseStack.translate(centring.x, centring.y, centring.z);
			if (pitch != 0.0F) poseStack.mulPose(Vector3.XP.rotationDegrees(pitch).toMoj());
		}
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F).toMoj());
		// The death pose tips the body over at its feet; shift it back so the fallen body lies over the floor's middle.
		if (kept.husk()) poseStack.translate(-entity.getBbHeight() * 0.5D, 0.0D, 0.0D);
		Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F,
				near && !kept.husk() ? partialTicks : 0.0F, poseStack, buffer, kept.husk() ? HUSK_LIGHT : light);
		poseStack.popPose();
	}

	/** The vessel's display entity, rebuilt only when its specimen changes. */
	public static Entity display(BlockEntity vessel, KeptSpecimen kept) {
		Level level = vessel.getLevel();
		if (level == null) return null;
		Display display = DISPLAYS.computeIfAbsent(vessel, key -> new Display());
		if (display.revision != kept.revision() || display.entity == null) {
			display.revision = kept.revision();
			display.animationTick = Long.MIN_VALUE;
			display.entity = createDisplayEntity(level, kept.specimen());
		}
		return display.entity;
	}

	/** For the item renderer: one jar item's creature, cached by its saved data. */
	public void renderSpecimen(Level level, CompoundTag specimen, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight) {
		Entity entity = getOrCreateEntity(level, specimen);
		if (entity == null) {
			return;
		}
		if (cachedAnimationTick != level.getGameTime()) {
			cachedAnimationTick = level.getGameTime();
			freeze(entity, level);
		}
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

	public static float specimenFitScale(Entity entity) {
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
        float visualFootprint = Math.max(longBodiedVisualLength(entity), broadSpecimenVisualWidth(entity));
        if (entity instanceof OsteophageEntity bird && bird.isFlying()) {
            visualFootprint = Math.max(visualFootprint, 4.75F);
        }
        return Math.max(Math.max(boundsFootprint, vanillaFootprint), visualFootprint);
	}

	private static float longBodiedVisualLength(Entity entity) {
		return longBodiedVisualLength(entityPath(entity), entity.getBbWidth(), entity.getBbHeight());
	}

	static float longBodiedVisualLength(String path, float width, float height) {
        if (path.equals("osteophage")) return 2.6F;
		if (path.equals("siphonophore")) return 3.5F;
		if (path.equals("hagfish")) return 1.8F;
		if (path.equals("vampire_squid")) return 1.6F;
		if (path.equals("chiton")) return 1.05F;
		if (path.equals("pelagic_herring")) return .85F;
        if (path.equals("ice_fish")) return 1.3F;
		if (path.contains("centipede")) {
			return Math.max(width * 2.65F, height * 6.0F);
		}
		if (isLongBodiedPath(path)) {
			return Math.max(width * 1.9F, height * 2.6F);
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
		if (path.equals("siphonophore")) poseStack.translate(0, 2.3, 0);
		poseStack.translate(0.0D, 0.0D, specimenVisualDepthOffset(path, entity.getBbWidth()));
	}

	static double specimenVisualDepthOffset(String path, float width) {
		if (path.equals("hagfish")) return -.2;
		if (path.contains("prism_cuttle")) {
			return width * 0.18D;
		}
		if (path.contains("centipede")) {
			return -width * 0.45D;
		}
		return isLongBodiedPath(path) ? -width * 0.15D : 0.0D;
	}

	private static boolean isLongBodiedPath(String path) {
		return path.contains("prism_cuttle") || path.contains("chthonian") || path.contains("serpent") || path.contains("snake")
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
		cachedEntity = createDisplayEntity(level, specimen);
		cachedKey = key;
		return cachedEntity;
	}

	private static Entity createDisplayEntity(Level level, CompoundTag specimen) {
		if (specimen == null || specimen.isEmpty()) return null;
		CompoundTag renderTag = specimen.copy();
		ListTag pos = new ListTag();
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		renderTag.put("Pos", pos);
		Optional<Entity> created = EntityType.create(renderTag, level);
		Entity entity = created.orElse(null);
		if (entity != null) {
			// This disposable client copy never updates physical water state or writes back to the vessel.
			entity.getPersistentData().putBoolean(DISPLAY_ENTITY_TAG, true);
			entity.setPos(0.0D, 0.0D, 0.0D);
			entity.setYRot(0.0F);
			entity.setXRot(0.0F);
			freeze(entity, level);
		}
		return entity;
	}

	private static void updateRenderAnimation(Entity entity, Level level, Display display) {
		long gameTime = level.getGameTime();
		if (display.animationTick == gameTime) {
			return;
		}
		display.animationTick = gameTime;
		freeze(entity, level);
	}

	private static void freeze(Entity entity, Level level) {
		int visualTick = (int) (level.getGameTime() % Integer.MAX_VALUE);
		entity.tickCount = visualTick;
		entity.setPos(0.0D, 0.0D, 0.0D);
		entity.setOldPosAndRot();
		entity.setYRot(0.0F);
		entity.setXRot(0.0F);
		// Loading the save set the previous-tick rotation to the creature's last look; left there, every frame lerps
		// from that look back to rest and snaps again at the next tick, so the head twitches.
		entity.yRotO = 0.0F;
		entity.xRotO = 0.0F;
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
		living.hurtTime = 0;
		living.attackAnim = 0.0F;
		living.oAttackAnim = 0.0F;
		living.swinging = false;
		living.walkAnimation.setSpeed(0.0F);
		living.walkAnimation.update(0.0F, 1.0F);
	}
}
