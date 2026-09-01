package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MorphlingSerpentLegAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_serpent_leg_attachment"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public MorphlingSerpentLegAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("upper_coil",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(2.1f, 1.2f, -2.7f, 2.8f, 2.2f, 5.4f, new CubeDeformation(0.1f))
                        .texOffs(20, 0)
                        .addBox(1.5f, 3.0f, 1.4f, 4.4f, 2.0f, 2.2f, new CubeDeformation(0.08f)),
                PartPose.ZERO);

        root.addOrReplaceChild("lower_coil",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(2.0f, 6.0f, -2.5f, 2.6f, 2.0f, 5.0f, new CubeDeformation(0.08f))
                        .texOffs(20, 14)
                        .addBox(1.4f, 8.0f, -3.2f, 4.0f, 2.0f, 2.0f, new CubeDeformation(0.06f)),
                PartPose.ZERO);

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(4.2f, 9.4f, -3.6f, 3.2f, 2.2f, 2.6f, new CubeDeformation(0.08f))
                        .texOffs(54, 0)
                        .addBox(6.8f, 10.0f, -3.1f, 1.6f, 1.0f, 1.6f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(40, 8)
                        .addBox(3.2f, 0.0f, 2.8f, 1.6f, 1.6f, 3.4f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = Mth.sin(ageInTicks * 0.1f) * 0.08f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
