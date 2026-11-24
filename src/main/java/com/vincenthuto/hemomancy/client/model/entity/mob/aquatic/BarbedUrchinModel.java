package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BarbedUrchinEntity;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class BarbedUrchinModel extends HierarchicalModel<BarbedUrchinEntity> {
    private final ModelPart body = BarbedUrchinMesh.createRoot();
    private final ModelPart root = new ModelPart(List.of(), Map.of("barbed_urchin", body));
    private final ModelPart[] crown = spines("crown", 5);
    private final ModelPart[] shoulder = spines("shoulder", 10);
    private final ModelPart[] equator = spines("equatorial", 10);
    private final ModelPart[] lower = spines("lower", 10);
    private final ModelPart[] feet = new ModelPart[5];

    public BarbedUrchinModel() {
        ModelPart tubeFeet = body.getChild("tube_feet");
        for (int i = 0; i < feet.length; i++) {
            feet[i] = tubeFeet.getChild("tube_foot_" + (i + 1));
        }
    }

    private ModelPart[] spines(String name, int count) {
        ModelPart ring = body.getChild(name + "_spines");
        ModelPart[] hinges = new ModelPart[count];
        for (int i = 0; i < count; i++) {
            int number = i + 1;
            String spine = name + "_spine_" + (number < 10 ? "0" : "") + number;
            hinges[i] = ring.getChild(spine).getChild(spine + "_spine_hinge");
        }
        return hinges;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BarbedUrchinEntity entity, float swing, float swingAmount,
                          float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0.0F, 1.0F);
        pose(entity.getVisualPuff(partialTick), age + entity.getId() * 1.9F, swing, swingAmount);
    }

    void pose(float puff, float age, float swing, float swingAmount) {
        float folded = 1.0F - puff * 0.5F;
        float wave = age * 0.16F;
        animateRing(crown, folded, -26.0F, 0.65F, wave);
        animateRing(shoulder, folded, 22.0F, 1.15F, wave + 0.8F);
        animateRing(equator, folded, 39.0F, 1.5F, wave + 1.6F);
        animateRing(lower, folded, 25.0F, 1.0F, wave + 2.4F);

        float stride = Mth.clamp(swingAmount * 3.0F, 0.0F, 1.0F) * folded;
        for (int i = 0; i < feet.length; i++) {
            float phase = swing * 2.5F + i * Mth.TWO_PI / feet.length;
            ModelPart foot = feet[i];
            foot.xRot = foot.getInitialPose().xRot + Mth.sin(phase) * 0.15F * stride;
            foot.y = foot.getInitialPose().y - Math.max(0.0F, Mth.cos(phase)) * 0.6F * stride;
        }
    }

    private static void animateRing(ModelPart[] hinges, float folded, float foldAngle, float travel, float wave) {
        for (int i = 0; i < hinges.length; i++) {
            ModelPart hinge = hinges[i];
            float ripple = Mth.sin(wave + i * Mth.TWO_PI / hinges.length) * 3.0F * folded;
            hinge.xRot = hinge.getInitialPose().xRot + (foldAngle * folded + ripple) * Mth.DEG_TO_RAD;
            hinge.z = hinge.getInitialPose().z - travel * folded;
        }
    }
}
