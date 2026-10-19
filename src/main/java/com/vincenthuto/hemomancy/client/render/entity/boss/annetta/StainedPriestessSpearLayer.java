package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.client.model.item.unstained.AnnettasSanguisLanceaModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The lancea in the Priestess's hands. The model places the grip and solves her arms onto it; this draws the spear
 * there, the blood gathering on it as a throw charges, and a smear of blood along the point's path through each
 * blow so thrusts, the reap and the plunge read as sweeps of the whole weapon.
 */
final class StainedPriestessSpearLayer extends RenderLayer<StainedPriestessEntity, StainedPriestessModel> {
    /** How long a smear lingers, in ticks. */
    private static final float SMEAR_TICKS = 5.0F;
    private static final int SMEAR_SAMPLES = 24;

    private record Sample(Vec3 tip, Vec3 blade, float time) {}

    private final AnnettasSanguisLanceaModel spear;
    private final Map<StainedPriestessEntity, ArrayDeque<Sample>> smears = new WeakHashMap<>();

    StainedPriestessSpearLayer(RenderLayerParent<StainedPriestessEntity, StainedPriestessModel> parent, EntityModelSet models) {
        super(parent);
        this.spear = new AnnettasSanguisLanceaModel(models.bakeLayer(AnnettasSanguisLanceaModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, StainedPriestessEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float time, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        StainedPriestessModel model = getParentModel();
        float shown = model.spearShown;
        ArrayDeque<Sample> smear = smears.computeIfAbsent(entity, e -> new ArrayDeque<>());
        while (!smear.isEmpty() && (time - smear.peekLast().time() > SMEAR_TICKS || smear.peekLast().time() > time)) smear.pollLast();
        if (shown <= 0.01F) {
            drawSmear(buffer, smear, time);
            return;
        }
        poseStack.pushPose();
        model.translateToSpear(poseStack);
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        // A forming spear is translucent and grows from the grip outward.
        LanceaDraw.draw(poseStack, buffer, spear, light, overlay, 0.35F + 0.65F * shown, Math.min(1.0F, shown * 1.4F));
        LanceaDraw.charge(poseStack.last(), buffer, Math.max(model.spearCharge, (1.0F - shown) * 0.8F), time,
                StainedPriestessModel.SPEAR_TIP * shown);
        if (model.spearSwinging) {
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            Matrix4f pose = poseStack.last().pose();
            Vector3f tip = pose.transformPosition(new Vector3f(0.0F, 0.0F, -StainedPriestessModel.SPEAR_TIP / 16.0F));
            Vector3f blade = pose.transformPosition(new Vector3f(0.0F, 0.0F, -StainedPriestessModel.SPEAR_TIP * 0.62F / 16.0F));
            smear.addFirst(new Sample(camera.add(tip.x, tip.y, tip.z), camera.add(blade.x, blade.y, blade.z), time));
            while (smear.size() > SMEAR_SAMPLES) smear.pollLast();
        }
        poseStack.popPose();
        drawSmear(buffer, smear, time);
    }

    /** Two streaks in camera space: a hot thin one along the point, a broad dim one along the blade. */
    private static void drawSmear(MultiBufferSource buffer, ArrayDeque<Sample> smear, float time) {
        int n = smear.size();
        if (n < 2) return;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float[] tip = new float[n * 3], blade = new float[n * 3];
        float[] tipRadii = new float[n], bladeRadii = new float[n], tipAlpha = new float[n], bladeAlpha = new float[n];
        int i = 0;
        for (Sample sample : smear) {
            float fade = 1.0F - Math.min(1.0F, (time - sample.time()) / SMEAR_TICKS);
            put(tip, i, sample.tip(), camera);
            put(blade, i, sample.blade(), camera);
            float along = 1.0F - i / (float) (n - 1);
            tipRadii[i] = 0.4F + 1.8F * along * fade;
            bladeRadii[i] = 0.8F + 3.6F * along * fade;
            tipAlpha[i] = 0.55F * fade * fade;
            bladeAlpha[i] = 0.16F * fade;
            i++;
        }
        PoseStack.Pose identity = new PoseStack().last();
        var glow = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
        TendrilTube.glow(identity, glow, blade, bladeRadii, 1.0F, 0.75F, 0.05F, 0.1F, bladeAlpha);
        TendrilTube.glow(identity, glow, tip, tipRadii, 1.0F, 1.0F, 0.35F, 0.3F, tipAlpha);
    }

    private static void put(float[] line, int i, Vec3 world, Vec3 camera) {
        line[i * 3] = (float) (world.x - camera.x) * 16.0F;
        line[i * 3 + 1] = (float) (world.y - camera.y) * 16.0F;
        line[i * 3 + 2] = (float) (world.z - camera.z) * 16.0F;
    }
}
