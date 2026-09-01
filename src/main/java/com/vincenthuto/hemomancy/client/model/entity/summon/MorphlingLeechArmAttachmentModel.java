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

public class MorphlingLeechArmAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_leech_arm_attachment"), "main");

    private final ModelPart root;
    private final ModelPart upperLeech;
    private final ModelPart lowerLeech;

    public MorphlingLeechArmAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.upperLeech = this.root.getChild("upper_leech");
        this.lowerLeech = this.root.getChild("lower_leech");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("upper_leech",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(2.6f, 2.0f, -2.2f, 3.2f, 4.6f, 4.4f, new CubeDeformation(0.18f))
                        .texOffs(20, 0)
                        .addBox(5.2f, 3.2f, -1.4f, 1.6f, 2.4f, 2.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("lower_leech",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(2.3f, 6.8f, -1.8f, 2.8f, 4.8f, 3.6f, new CubeDeformation(0.14f))
                        .texOffs(18, 14)
                        .addBox(4.7f, 8.4f, -1.0f, 1.6f, 2.0f, 2.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("tendrils",
                CubeListBuilder.create()
                        .texOffs(34, 0)
                        .addBox(5.8f, 5.4f, -0.4f, 3.2f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(34, 4)
                        .addBox(5.0f, 10.2f, 0.2f, 3.6f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 8)
                        .addBox(4.9f, 7.0f, -2.2f, 2.4f, 0.7f, 0.7f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = Mth.sin(ageInTicks * 0.12f) * 0.08f;
        upperLeech.zScale = 1.0f + pulse;
        lowerLeech.xScale = 1.0f - pulse * 0.5f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
