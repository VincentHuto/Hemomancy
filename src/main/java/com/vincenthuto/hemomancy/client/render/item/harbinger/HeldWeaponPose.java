package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.LivingStaffMorphClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Where a held weapon is actually drawn, and its real shape, so conjure visuals sit on the weapon itself. Each frame
 * the pose a held item is drawn with (the same transforms the Living Staff morph and vanilla use) is recorded per
 * player and arm; the item's faces are captured once per item and display context by rendering it into a recorder.
 * Both renders run in camera-relative world axes (1.21 first-person hands carry the inverse view rotation), so a
 * face point is the recorded pose applied to the captured point, plus the camera.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HeldWeaponPose {
    private static final long STALE_NANOS = 150_000_000L;
    private static final int MAX_FACES = 600;

    private record Key(UUID player, HumanoidArm arm) {}
    private record Posed(Matrix4f pose, boolean firstPerson, long nanos) {}
    private record ShapeKey(Item item, ItemDisplayContext context) {}

    private static final Map<Key, Posed> POSES = new HashMap<>();
    private static final Map<ShapeKey, List<Vector3f[]>> SHAPES = new HashMap<>();
    private static final Map<ShapeKey, Vector3f> TOPS = new HashMap<>();
    /** How far below an item's highest point still counts as its top: two model pixels. */
    private static final float TOP_BAND = 2 / 16f;

    /** A weapon in the world: its faces and grip as offsets from {@code origin}. */
    public record Held(List<Vec3[]> faces, Vec3 grip) {}

    private HeldWeaponPose() {
    }

    /** Third person: records the held-item pose for one arm from the posed player model (the morph layer's transforms). */
    public static void recordThirdPerson(Player player, HumanoidModel<?> model, PoseStack poses, HumanoidArm arm) {
        poses.pushPose();
        model.translateToHand(arm, poses);
        poses.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        poses.translate((arm == HumanoidArm.LEFT ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
        Matrix4f pose = new Matrix4f(poses.last().pose());
        poses.popPose();
        // Podium figurines and inventory dolls draw this same player elsewhere; recording them would replace the real
        // pose (in first person, the only one there is) whenever one is on screen.
        if (worldRender(pose, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition(), player.position()))
            POSES.put(new Key(player.getUUID(), arm), new Posed(pose, false, System.nanoTime()));
    }

    /** Whether a third-person held pose is the player's own world render: its grip lies within reach of where they stand. */
    static boolean worldRender(Matrix4f pose, Vec3 camera, Vec3 feet) {
        Vector3f grip = pose.transformPosition(new Vector3f());
        return camera.add(grip.x, grip.y, grip.z).distanceToSqr(feet) <= 9;
    }

    /** First person: records the held-item pose of each hand as the hand render is about to draw it. */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void renderHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        HumanoidArm arm = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == event.getHand()
                && event.getItemStack().getUseAnimation() == UseAnim.BOW)
            applyBowDrawTransform(poses, player, arm, event.getItemStack(), event.getEquipProgress(), event.getPartialTick());
        else
            LivingStaffMorphClientEvents.applyHeldItemTransform(poses, arm, event.getEquipProgress(), event.getSwingProgress());
        POSES.put(new Key(player.getUUID(), arm), new Posed(new Matrix4f(poses.last().pose()), true, System.nanoTime()));
        poses.popPose();
    }

    /** Vanilla's first-person bow draw (ItemInHandRenderer), which a drawn staff channelling its utility is held with. */
    private static void applyBowDrawTransform(PoseStack poses, Player player, HumanoidArm arm, ItemStack stack,
            float equipProgress, float partial) {
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poses.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
        poses.translate(side * -0.2785682F, 0.18344387F, 0.15731531F);
        poses.mulPose(Axis.XP.rotationDegrees(-13.935F));
        poses.mulPose(Axis.YP.rotationDegrees(side * 35.3F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * -9.785F));
        float held = stack.getUseDuration(player) - (player.getUseItemRemainingTicks() - partial + 1.0F);
        float draw = Math.min(1.0F, (held / 20.0F * (held / 20.0F) + held / 20.0F * 2.0F) / 3.0F);
        if (draw > 0.1F) poses.translate(0.0F, Mth.sin((held - 0.1F) * 1.3F) * (draw - 0.1F) * 0.004F, 0.0F);
        poses.translate(0.0F, 0.0F, draw * 0.04F);
        poses.scale(1.0F, 1.0F, 1.0F + draw * 0.2F);
        poses.mulPose(Axis.YN.rotationDegrees(side * 45.0F));
    }

    /**
     * The weapon in {@code player}'s {@code arm} as drawn this frame, as offsets from {@code origin}, or null when that
     * hand was not drawn just now (or holds nothing). First person spreads points across the view like the hand
     * projection does (see {@link FirstPersonPalm}).
     */
    public static Held held(Player player, HumanoidArm arm, ItemStack stack, Vec3 origin) {
        Posed posed = posed(player, arm, stack);
        if (posed == null) return null;
        List<Vector3f[]> shape = shape(stack, context(posed, arm));
        if (shape.isEmpty()) return null;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(origin);
        List<Vec3[]> faces = new ArrayList<>(shape.size());
        for (Vector3f[] face : shape) {
            Vec3[] placed = new Vec3[4];
            for (int i = 0; i < 4; i++) placed[i] = place(posed, face[i], camera);
            faces.add(placed);
        }
        return new Held(faces, place(posed, new Vector3f(), camera));
    }

    /**
     * The top of the item in {@code player}'s {@code arm} as drawn this frame (a staff's head), in world space, or null
     * when that hand was not drawn just now.
     */
    public static Vec3 top(Player player, HumanoidArm arm, ItemStack stack) {
        Posed posed = posed(player, arm, stack);
        if (posed == null) return null;
        ShapeKey key = new ShapeKey(stack.getItem(), context(posed, arm));
        Vector3f top = TOPS.computeIfAbsent(key, k -> top(shape(stack, k.context())));
        return top == null ? null : place(posed, top, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
    }

    /** The middle of the item's uppermost faces in its held space, where held items stand upright. */
    private static Vector3f top(List<Vector3f[]> shape) {
        if (shape.isEmpty()) return null;
        float highest = Float.NEGATIVE_INFINITY;
        for (Vector3f[] face : shape) for (Vector3f point : face) highest = Math.max(highest, point.y);
        Vector3f sum = new Vector3f();
        int count = 0;
        for (Vector3f[] face : shape)
            for (Vector3f point : face)
                if (point.y >= highest - TOP_BAND) { sum.add(point); count++; }
        // The very top is the head's crown; settle a little down into the head.
        return sum.div(count).sub(0, TOP_BAND, 0);
    }

    private static Posed posed(Player player, HumanoidArm arm, ItemStack stack) {
        Posed posed = POSES.get(new Key(player.getUUID(), arm));
        if (posed == null || stack.isEmpty() || System.nanoTime() - posed.nanos > STALE_NANOS) return null;
        var mc = Minecraft.getInstance();
        boolean firstPerson = player == mc.player && mc.options.getCameraType().isFirstPerson();
        return posed.firstPerson == firstPerson ? posed : null;
    }

    private static ItemDisplayContext context(Posed posed, HumanoidArm arm) {
        return posed.firstPerson
                ? arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                : arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    private static Vec3 place(Posed posed, Vector3f point, Vec3 camera) {
        Vector3f at = posed.pose.transformPosition(point, new Vector3f());
        return camera.add(posed.firstPerson ? FirstPersonPalm.posedOffset(at) : new Vec3(at.x, at.y, at.z));
    }

    /** The item's faces in its held space for a display context, captured once by rendering it into a recorder. */
    static List<Vector3f[]> shape(ItemStack stack, ItemDisplayContext context) {
        return SHAPES.computeIfAbsent(new ShapeKey(stack.getItem(), context), key -> {
            Recorder recorder = new Recorder();
            var mc = Minecraft.getInstance();
            try {
                mc.getItemRenderer().renderStatic(stack, context, 0xF000F0, OverlayTexture.NO_OVERLAY, new PoseStack(), recorder,
                        mc.level, 0);
            } catch (RuntimeException failed) {
                Hemomancy.LOGGER.debug("Could not capture the shape of {}", stack, failed);
                return List.of();
            }
            return recorder.faces();
        });
    }

    /** Largest single face a held weapon has, in blocks squared, and how far its faces reach from its middle. */
    private static final float MAX_FACE_AREA = 0.2f, MAX_REACH = 1.6f;

    private static float median(List<Float> values) {
        List<Float> sorted = new ArrayList<>(values);
        java.util.Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    /** Collects every quad an item render emits, untransformed by anything but the render's own pose. */
    private static final class Recorder implements MultiBufferSource, VertexConsumer {
        private final List<Vector3f> points = new ArrayList<>();

        @Override public VertexConsumer getBuffer(RenderType type) { return this; }
        @Override public VertexConsumer addVertex(float x, float y, float z) { points.add(new Vector3f(x, y, z)); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }

        List<Vector3f[]> faces() {
            List<Vector3f[]> faces = new ArrayList<>();
            // Glints and overlays repeat the same quads; keep each face once.
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (int i = 0; i + 3 < points.size() && faces.size() < MAX_FACES; i += 4) {
                Vector3f a = points.get(i), b = points.get(i + 1), c = points.get(i + 2), d = points.get(i + 3);
                Vector3f center = new Vector3f(a).add(b).add(c).add(d).mul(0.25f);
                long key = ((long) Math.round(center.x * 512) * 73856093L) ^ ((long) Math.round(center.y * 512) * 19349663L)
                        ^ ((long) Math.round(center.z * 512) * 83492791L);
                if (!seen.add(key)) continue;
                float area = new Vector3f(b).sub(a).cross(new Vector3f(d).sub(a)).length();
                // Degenerate faces, and the large flat planes some custom renderers add (shadows, flat sprites),
                // are not part of the weapon's body.
                if (area < 1e-6f || area > MAX_FACE_AREA) continue;
                faces.add(new Vector3f[] { a, b, c, d });
            }
            if (faces.isEmpty()) return List.of();
            // Drop strays far from the weapon's body (a median centre resists a few outliers).
            List<Float> xs = new ArrayList<>(), ys = new ArrayList<>(), zs = new ArrayList<>();
            for (Vector3f[] face : faces) {
                Vector3f centre = new Vector3f(face[0]).add(face[1]).add(face[2]).add(face[3]).mul(0.25f);
                xs.add(centre.x); ys.add(centre.y); zs.add(centre.z);
            }
            Vector3f median = new Vector3f(median(xs), median(ys), median(zs));
            List<Vector3f[]> kept = new ArrayList<>(faces.size());
            for (Vector3f[] face : faces)
                if (new Vector3f(face[0]).add(face[1]).add(face[2]).add(face[3]).mul(0.25f).distance(median) <= MAX_REACH) kept.add(face);
            return List.copyOf(kept);
        }
    }

    public static void clear() {
        POSES.clear();
        SHAPES.clear();
        TOPS.clear();
    }
}
