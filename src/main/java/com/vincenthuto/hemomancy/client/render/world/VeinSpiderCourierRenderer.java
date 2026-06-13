package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.data.VeinSpiderCourierClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class VeinSpiderCourierRenderer {
    private static final double ARC_HEIGHT = 0.55D;

    private VeinSpiderCourierRenderer() {
    }

    public static void render(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        var entries = VeinSpiderCourierClientData.entries();
        if (entries.isEmpty()) {
            return;
        }

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        float time = level.getGameTime() + partialTick;
        List<RenderedCourierItem> items = new ArrayList<>(entries.size());

        for (VeinSpiderCourierClientData.Entry entry : entries) {
            float progress = smooth(entry.progress(partialTick));
            float alpha = entry.alpha(partialTick);
            Vec3 from = Vec3.atCenterOf(entry.from()).add(0.0D, 0.18D, 0.0D);
            Vec3 to = Vec3.atCenterOf(entry.to()).add(0.0D, 0.18D, 0.0D);
            Vec3 pos = from.lerp(to, progress).add(0.0D, Math.sin(progress * Math.PI) * ARC_HEIGHT, 0.0D);
            Vec3 forward = to.subtract(from);
            if (forward.lengthSqr() < 0.0001D) {
                forward = new Vec3(0.0D, 0.0D, 1.0D);
            } else {
                forward = forward.normalize();
            }
            Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
            Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
            drawGuideLine(pose, lines, from.subtract(camera), to.subtract(camera), alpha);
            drawSpider(pose, lines, pos.subtract(camera), forward, right, up, alpha, time + entry.seed() * 0.001F);
            items.add(new RenderedCourierItem(pos, entry, time));
        }

        buffer.endBatch(RenderType.lines());
        for (RenderedCourierItem item : items) {
            renderCarriedItem(mc, buffer, poseStack, camera, item.pos(), item.entry(), item.time());
        }
        if (!items.isEmpty()) {
            buffer.endBatch();
        }
    }

    private static void drawGuideLine(PoseStack.Pose pose, VertexConsumer consumer, Vec3 from, Vec3 to, float alpha) {
        int lineAlpha = (int) (55 * alpha);
        Vec3 normal = to.subtract(from).normalize();
        consumer.addVertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z)
                .setColor(145, 10, 20, lineAlpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z)
                .setColor(210, 24, 34, lineAlpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static void drawSpider(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 forward,
                                   Vec3 right, Vec3 up, float alpha, float time) {
        int bodyAlpha = (int) (210 * alpha);
        Vec3 head = center.add(forward.scale(0.12D));
        Vec3 abdomen = center.add(forward.scale(-0.12D));
        line(pose, consumer, head.add(up.scale(0.015D)), abdomen.add(up.scale(0.015D)), bodyAlpha, 245, 26, 38);
        line(pose, consumer, center.add(right.scale(-0.105D)), center.add(right.scale(0.105D)), bodyAlpha, 245, 26, 38);
        line(pose, consumer, abdomen.add(right.scale(-0.08D)), abdomen.add(right.scale(0.08D)), bodyAlpha, 245, 26, 38);

        for (int i = 0; i < 4; i++) {
            double along = -0.12D + i * 0.08D;
            double stride = Math.sin(time * 0.55F + i * 1.7F) * 0.035D;
            Vec3 root = center.add(forward.scale(along));
            Vec3 leftKnee = root.add(right.scale(-0.13D)).add(forward.scale(stride));
            Vec3 leftFoot = leftKnee.add(right.scale(-0.15D)).add(up.scale(-0.04D));
            Vec3 rightKnee = root.add(right.scale(0.13D)).add(forward.scale(-stride));
            Vec3 rightFoot = rightKnee.add(right.scale(0.15D)).add(up.scale(-0.04D));
            line(pose, consumer, root, leftKnee, bodyAlpha, 215, 18, 30);
            line(pose, consumer, leftKnee, leftFoot, bodyAlpha, 170, 8, 18);
            line(pose, consumer, root, rightKnee, bodyAlpha, 215, 18, 30);
            line(pose, consumer, rightKnee, rightFoot, bodyAlpha, 170, 8, 18);
        }
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, Vec3 a, Vec3 b, int alpha,
                             int red, int green, int blue) {
        Vec3 normal = b.subtract(a).normalize();
        consumer.addVertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static void renderCarriedItem(Minecraft mc, MultiBufferSource.BufferSource buffer, PoseStack poseStack,
                                          Vec3 camera, Vec3 pos, VeinSpiderCourierClientData.Entry entry, float time) {
        poseStack.pushPose();
        Vec3 renderPos = pos.add(0.0D, 0.16D + Math.sin(time * 0.22F) * 0.025D, 0.0D).subtract(camera);
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.22F, 0.22F, 0.22F);
        mc.getItemRenderer().renderStatic(entry.stack(), ItemDisplayContext.GROUND,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, entry.seed());
        poseStack.popPose();
    }

    private static float smooth(float progress) {
        return Mth.clamp(progress * progress * (3.0F - 2.0F * progress), 0.0F, 1.0F);
    }

    private record RenderedCourierItem(Vec3 pos, VeinSpiderCourierClientData.Entry entry, float time) {
    }
}
