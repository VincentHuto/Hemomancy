package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.IceFishEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class IceFishModel extends HierarchicalModel<IceFishEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("ice_fish"), "main");
    private final ModelPart root, body, tail, tailTip, leftPectoral, rightPectoral;

    public IceFishModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        tail = body.getChild("tail");
        tailTip = tail.getChild("tail_tip");
        leftPectoral = body.getChild("left_pectoral");
        rightPectoral = body.getChild("right_pectoral");
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(IceFishEntity entity, float limbSwing, float limbAmount,
                                    float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float swim = age * (entity.isInWater() || SpecimenJarRenderer.isDisplayEntity(entity) ? .35F : .65F);
        tail.yRot = Mth.sin(swim) * .28F;
        tailTip.yRot = Mth.sin(swim - .7F) * .38F;
        leftPectoral.zRot = Mth.sin(swim * .5F) * .14F;
        rightPectoral.zRot = -leftPectoral.zRot;
    }
}
