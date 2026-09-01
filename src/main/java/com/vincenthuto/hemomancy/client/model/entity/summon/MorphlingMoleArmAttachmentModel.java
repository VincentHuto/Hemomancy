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

public class MorphlingMoleArmAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_mole_arm_attachment"), "main");

    private final ModelPart root;
    private final ModelPart shovelClaws;

    public MorphlingMoleArmAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.shovelClaws = this.root.getChild("shovel_claws");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("forearm_plate",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(2.1f, 4.8f, -2.5f, 3.4f, 6.4f, 5.0f, new CubeDeformation(0.14f))
                        .texOffs(20, 0)
                        .addBox(4.7f, 6.2f, -1.8f, 1.8f, 3.8f, 3.6f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("palm_spade",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(3.0f, 10.1f, -3.1f, 4.0f, 2.4f, 6.2f, new CubeDeformation(0.05f))
                        .texOffs(26, 16)
                        .addBox(5.6f, 9.0f, -2.2f, 1.6f, 2.0f, 4.4f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("shovel_claws",
                CubeListBuilder.create()
                        .texOffs(0, 30)
                        .addBox(6.5f, 10.6f, -2.8f, 2.6f, 0.8f, 0.9f, CubeDeformation.NONE)
                        .texOffs(0, 34)
                        .addBox(6.8f, 10.8f, -0.5f, 2.8f, 0.8f, 1.0f, CubeDeformation.NONE)
                        .texOffs(0, 38)
                        .addBox(6.5f, 10.6f, 1.9f, 2.6f, 0.8f, 0.9f, CubeDeformation.NONE)
                        .texOffs(18, 30)
                        .addBox(5.4f, 11.7f, -2.1f, 1.8f, 0.7f, 0.8f, CubeDeformation.NONE)
                        .texOffs(18, 34)
                        .addBox(5.8f, 11.9f, 1.3f, 1.8f, 0.7f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        shovelClaws.zRot = Mth.sin(ageInTicks * 0.10f + limbSwing) * (0.025f + limbSwingAmount * 0.05f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
