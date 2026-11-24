package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentSlitherRules;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ScarletSerpentModel extends HierarchicalModel<ScarletSerpentEntity> {
    private final ModelPart serpent = ScarletSerpentMesh.createRoot();
    private final ModelPart root = new ModelPart(List.of(), Map.of("scarlet_serpent", serpent));
    private final ModelPart neck = serpent.getChild("raised_neck");
    private final ModelPart head = neck.getChild("head");
    private final ModelPart jaw = head.getChild("lower_jaw");
    private final ModelPart collar = neck.getChild("hood_collar");
    private final ModelPart leftHood = collar.getChild("left_hood_hinge");
    private final ModelPart rightHood = collar.getChild("right_hood_hinge");
    private final List<ModelPart> segments = new ArrayList<>();

    public ScarletSerpentModel() {
        ModelPart parent = serpent.getChild("body_chain");
        for (int i = 1; i <= ScarletSerpentSlitherRules.BODY_SEGMENT_COUNT; i++) {
            parent = parent.getChild("body_" + (i < 10 ? "0" : "") + i);
            segments.add(parent);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ScarletSerpentEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        pose(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                entity.getHoodProgress(partialTick), Math.max(0.0F, entity.getStrikeProgress(partialTick)));
    }

    void pose(float swing, float swingAmount, float age, float yaw, float pitch, float hood, float strike) {
        serpent.getAllParts().forEach(ModelPart::resetPose);
        float movement = smoothStep(Mth.clamp(swingAmount / .36F, 0.0F, 1.0F));
        float flare = smoothStep(hood);
        float display = Math.max(flare, strike);
        float coil = display * (1.0F - .75F * movement);
        float step = swing * .85F + age * .09F;
        float lunge = strike * strike;
        float sway = Mth.sin(step + .35F);
        float low = 1.0F - display;

        neck.xRot += low * .78F + .21F * lunge + Mth.sin(age * .19F) * .025F * display;
        neck.yRot += sway * Mth.lerp(movement, .04F, .12F);
        head.yRot += Mth.clamp(yaw, -55.0F, 55.0F) * Mth.DEG_TO_RAD * .43F;
        head.xRot += Mth.clamp(pitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD * .33F - low * .48F - lunge * .12F;
        jaw.xRot = Mth.lerp(display, .02F, jaw.xRot) + lunge * .24F;
        leftHood.yRot -= low * 1.37F;
        rightHood.yRot += low * 1.37F;
        leftHood.zRot += low * .19F + display * sway * .02F;
        rightHood.zRot -= low * .19F + display * sway * .02F;

        float wave = Mth.lerp(movement, .16F, 1.0F);
        for (int i = 0; i < segments.size(); i++) {
            ModelPart segment = segments.get(i);
            segment.yRot = segment.yRot * coil + ScarletSerpentSlitherRules.segmentLocalYaw(i, step, wave);
        }
        // Lift the last joint slightly. Pitch farther up the chain would carry the tail through the ground.
        segments.getLast().xRot += ScarletSerpentSlitherRules.segmentLocalPitch(segments.size() - 1, step, wave);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
