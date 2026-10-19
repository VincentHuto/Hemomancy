package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.AnnettaKnowlesModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Annetta's dagger and shield. The dagger is held reversed, point down from the little-finger side, as a knife
 * fighter holds it; the Unstained Shield is strapped across her left forearm rather than dangled from the hand.
 * While a cut is live the blade leaves a still-water trail sampled from its own path, so the arc on screen is the
 * arc the animation actually swept.
 */
final class AnnettaHeldItemLayer extends RenderLayer<AnnettaKnowlesEntity, AnnettaKnowlesModel> {
    /** How long, in ticks, a sampled blade position stays in the trail. */
    private static final float TRAIL_LIFE = 3.2F;
    private static final int TRAIL_MAX = 48;
    /** Points along the blade in the dagger model's own units (y up the blade is negative), hilt to tip. */
    private static final float BLADE_BASE = -9.5F;
    private static final float BLADE_TIP = -23.5F;
    /** How far, in pixels, the dagger is pushed through her fist along its length. */
    private static final float GRIP_SHIFT = 7.5F;

    private final ItemInHandRenderer itemInHandRenderer;
    private final Map<AnnettaKnowlesEntity, ArrayDeque<Sample>> trails = new WeakHashMap<>();

    private record Sample(float time, Vec3 base, Vec3 tip) {}

    AnnettaHeldItemLayer(RenderLayerParent<AnnettaKnowlesEntity, AnnettaKnowlesModel> parent, ItemInHandRenderer renderer) {
        super(parent);
        this.itemInHandRenderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AnnettaKnowlesEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack dagger = entity.getMainHandItem();
        ItemStack shield = entity.getOffhandItem();
        if (!dagger.isEmpty()) renderDagger(poseStack, buffer, packedLight, entity, dagger, ageInTicks);
        else this.trails.remove(entity);
        if (!shield.isEmpty()) {
            poseStack.pushPose();
            this.getParentModel().translateToShield(poseStack);
            this.itemInHandRenderer.renderItem(entity, shield, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
    }

    private void renderDagger(PoseStack poseStack, MultiBufferSource buffer, int light, AnnettaKnowlesEntity entity,
            ItemStack dagger, float age) {
        poseStack.pushPose();
        this.getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
        // The vanilla hand frame (ItemInHandLayer), then turned point-down for her reversed grip.
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(1.0F / 16.0F, 0.125F, -0.625F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        // Slid toward the point so the fist closes over the grip and her thumb caps the pommel.
        poseStack.translate(0.0F, GRIP_SHIFT / 16.0F, 0.05F);
        this.itemInHandRenderer.renderItem(entity, dagger, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, light);
        if (dagger.is(ItemInit.annettas_absolution_dagger.get())) {
            sampleBlade(poseStack.last().pose(), entity, age);
        }
        poseStack.popPose();
        drawTrail(buffer, entity, age);
    }

    /**
     * Where the dagger model's blade lies this frame, in world space. Mirrors the item renderer's centring and the
     * dagger renderer's third-person transform for the right hand (AbsolutionDaggerItemRenderer).
     */
    private void sampleBlade(Matrix4f handPose, AnnettaKnowlesEntity entity, float age) {
        ArrayDeque<Sample> trail = this.trails.computeIfAbsent(entity, e -> new ArrayDeque<>());
        if (!this.getParentModel().bladeTrail()) {
            trail.clear();
            return;
        }
        Matrix4f m = new Matrix4f(handPose)
                .translate(-0.5F, -0.5F, -0.5F)
                .rotateX((float) Math.PI)
                .rotateY((float) -Math.PI * 0.5F)
                .translate(-0.5F, -0.3F, -0.5F)
                .scale(0.54F);
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector3f base = m.transformPosition(new Vector3f(0.0F, BLADE_BASE / 16.0F, 0.0F));
        Vector3f tip = m.transformPosition(new Vector3f(0.0F, BLADE_TIP / 16.0F, 0.0F));
        trail.addLast(new Sample(age, camera.add(base.x, base.y, base.z), camera.add(tip.x, tip.y, tip.z)));
        while (trail.size() > TRAIL_MAX) trail.removeFirst();
    }

    /** A ribbon between successive blade positions: a wide pale glow, and a thin bright edge along the tip's path. */
    private void drawTrail(MultiBufferSource buffer, AnnettaKnowlesEntity entity, float age) {
        ArrayDeque<Sample> trail = this.trails.get(entity);
        if (trail == null) return;
        for (Iterator<Sample> it = trail.iterator(); it.hasNext(); ) {
            Sample sample = it.next();
            if (age - sample.time > TRAIL_LIFE || sample.time > age) it.remove();
        }
        if (trail.size() < 2) return;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Matrix4f identity = new Matrix4f();
        // One buffer at a time: asking the source for the second render type ends the first.
        ribbon(buffer.getBuffer(RenderTypeInit.CLAW_SLASH_GLOW), trail, camera, identity, age, 0.3F, 1.15F,
                0.62F, 0.8F, 0.95F, 0.55F);
        ribbon(buffer.getBuffer(RenderTypeInit.CLAW_SLASH_CORE), trail, camera, identity, age, 0.85F, 1.02F,
                0.9F, 0.96F, 1.0F, 0.85F);
    }

    /** The band of the blade between two fractions of its length, swept through every sample in the trail. */
    private static void ribbon(VertexConsumer consumer, ArrayDeque<Sample> trail, Vec3 camera, Matrix4f pose, float age,
            float from, float to, float r, float g, float b, float alpha) {
        Sample previous = null;
        for (Sample sample : trail) {
            if (previous != null) {
                Vec3 a0 = lerp(previous, from).subtract(camera), a1 = lerp(sample, from).subtract(camera);
                Vec3 b0 = lerp(previous, to).subtract(camera), b1 = lerp(sample, to).subtract(camera);
                quad(consumer, pose, a0, b0, b1, a1, r, g, b, fade(age - previous.time) * alpha, fade(age - sample.time) * alpha);
            }
            previous = sample;
        }
    }

    private static float fade(float ageOfSample) {
        float x = 1.0F - Math.max(0.0F, Math.min(1.0F, ageOfSample / TRAIL_LIFE));
        return x * x;
    }

    private static Vec3 lerp(Sample sample, float along) {
        return sample.base.add(sample.tip.subtract(sample.base).scale(along));
    }

    private static void quad(VertexConsumer consumer, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
            float r, float g, float bl, float alphaOld, float alphaNew) {
        consumer.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(r, g, bl, alphaOld);
        consumer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(r, g, bl, alphaOld);
        consumer.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(r, g, bl, alphaNew);
        consumer.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(r, g, bl, alphaNew);
    }
}
