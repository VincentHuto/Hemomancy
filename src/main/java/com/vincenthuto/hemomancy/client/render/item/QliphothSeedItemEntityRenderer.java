package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders the Qliphoth Seed item entity and attaches short HutosLib tendril
 * effects to it, as if the seed is seeking purchase in the ground.
 */
public class QliphothSeedItemEntityRenderer extends EntityRenderer<ItemEntity> {

    private static final int ROOTS_PER_PULSE = 12;
    private static final int ROOT_PULSE_INTERVAL = 6;
    private static final int ROOT_ENTITY_CACHE_LIMIT = 512;
    private static final double FULL_CIRCLE = Math.PI * 2.0D;
    private static final double ROOT_GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final double ROOT_PULSE_SPIN = Math.PI * (3.0D - Math.sqrt(5.0D));

    private final Map<Integer, Long> nextRootPulseByEntity = new HashMap<>();

    public QliphothSeedItemEntityRenderer(Context ctx) {
        super(ctx);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferIn, int packedLightIn) {

        ItemStack itemstack = entityIn.getItem();
        if (itemstack.isEmpty()) {
            super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.22D, 0.0D);
        float f3 = (entityIn.getAge() + partialTicks) / 20.0F + entityIn.bobOffs;
        poseStack.mulPose(Vector3.YP.rotation(f3).toMoj());
        QliphothSeedItemRenderer.renderSeedBody(ItemDisplayContext.GROUND, poseStack, bufferIn,
                packedLightIn, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        spawnHutoslibRoots(entityIn, partialTicks);

        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    private void spawnHutoslibRoots(ItemEntity entity, float partialTicks) {
        long gameTime = entity.level().getGameTime();
        int entityId = entity.getId();
        long nextPulse = nextRootPulseByEntity.getOrDefault(entityId, Long.MIN_VALUE);
        if (gameTime < nextPulse) {
            return;
        }
        if (nextRootPulseByEntity.size() > ROOT_ENTITY_CACHE_LIMIT) {
            nextRootPulseByEntity.entrySet().removeIf(entry -> entry.getValue() + 200L < gameTime);
        }
        nextRootPulseByEntity.put(entityId, gameTime + ROOT_PULSE_INTERVAL);

        long pulse = gameTime / ROOT_PULSE_INTERVAL;
        for (int i = 0; i < ROOTS_PER_PULSE; i++) {
            long rootSeed = rootSeed(entity, pulse, i);
            Vec3 startOffset = rootStartOffset(gameTime, i, pulse);
            Vec3 endOffset = rootEndOffset(gameTime, i, pulse);
            TendrilEffectConfig config = rootConfig(rootSeed);
            TendrilAnchor start = new TendrilAnchor.Entity(entityId, TendrilAnchor.AnchorPoint.CENTER, startOffset);
            TendrilAnchor end = new TendrilAnchor.Entity(entityId, TendrilAnchor.AnchorPoint.CENTER, endOffset);
            TendrilRenderer.INSTANCE.add(new TendrilEffectData(start, end, config, rootSeed), partialTicks);
        }
    }

    private static Vec3 rootStartOffset(long gameTime, int index, long pulse) {
        double angle = animatedAngle(gameTime, index, pulse) + 0.18D;
        return new Vec3(Math.cos(angle) * 0.035D, 0.10D, Math.sin(angle) * 0.035D);
    }

    private static Vec3 rootEndOffset(long gameTime, int index, long pulse) {
        double angle = animatedAngle(gameTime, index, pulse);
        double sideAngle = angle + Math.PI * 0.5D;
        double sway = Math.sin(gameTime * 0.13D + index * 1.71D) * 0.08D;
        double reach = rootReach(index, pulse);
        return new Vec3(
                Math.cos(angle) * reach + Math.cos(sideAngle) * sway,
                -rootDrop(index, pulse),
                Math.sin(angle) * reach + Math.sin(sideAngle) * sway);
    }

    private static double animatedAngle(long gameTime, int index, long pulse) {
        double baseAngle = index * ROOT_GOLDEN_ANGLE;
        double pulseRotation = pulse * ROOT_PULSE_SPIN;
        double wobble = Math.sin(gameTime * 0.075D + index * 2.37D) * 0.18D;
        return Math.floorMod((long) ((baseAngle + pulseRotation + wobble) * 1_000_000.0D),
                (long) (FULL_CIRCLE * 1_000_000.0D)) / 1_000_000.0D;
    }

    private static double rootReach(int index, long pulse) {
        return 0.46D + 0.18D * (0.5D + 0.5D * Math.sin(index * 1.91D + pulse * 0.73D));
    }

    private static double rootDrop(int index, long pulse) {
        return 0.08D + 0.08D * (0.5D + 0.5D * Math.cos(index * 2.17D + pulse * 0.61D));
    }

    private static TendrilEffectConfig rootConfig(long seed) {
        return TendrilEffectConfig.defaults()
                .withMode(TendrilEffectConfig.Mode.FREEFORM)
                .withColors(0xF0050003, 0xB8D10B1A)
                .withBlendColors(false)
                .withLifecycle(4, 7, 8)
                .withShape(16, 1, 0.024F, 0.045F)
                .withBranching(1, 1, 0.12F, 0.45F)
                .withWrithe(0.04F, 0.09F, 0.08F, 0.0F)
                .withFixedSeed(true, seed);
    }

    private static long rootSeed(ItemEntity entity, long pulse, int index) {
        long seed = 0x6A09E667F3BCC909L;
        seed ^= (long) entity.getId() * 0x9E3779B97F4A7C15L;
        seed ^= pulse * 0xBF58476D1CE4E5B9L;
        seed ^= (long) index * 0x94D049BB133111EBL;
        return seed;
    }
}
