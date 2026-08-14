package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class MorphlingJarModel<T extends LivingEntity> extends EntityModel<T> {


    public static final ModelLayerLocation morphling_jar = new ModelLayerLocation(
            Hemomancy.rloc("morphling_jar"), "main");

    private final ModelPart body;
    private final ModelPart gourd2;
    private final ModelPart rope3;
    private final ModelPart bone9;
    private final ModelPart bone10;


    public MorphlingJarModel(ModelPart root) {

        this.body = root.getChild("body");
        this.gourd2 = this.body.getChild("gourd2");
        this.rope3 = this.gourd2.getChild("rope3");
        this.bone9 = this.rope3.getChild("bone9");
        this.bone10 = this.rope3.getChild("bone10");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition gourd2 = body.addOrReplaceChild("gourd2", CubeListBuilder.create().texOffs(12, 0).addBox(-1.5F, -2.7222F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5F, -0.7222F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-1.5F, -3.7222F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F))
                .texOffs(0, 11).addBox(-1.5F, -2.2222F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-5.75F, 12.5722F, 0.25F, 0.7418F, 0.0F, 0.0F));

        PartDefinition rope3 = gourd2.addOrReplaceChild("rope3", CubeListBuilder.create().texOffs(12, 8).addBox(-1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F))
                .texOffs(12, 11).addBox(-1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F)), PartPose.offset(0.1F, -0.8389F, 0.0333F));

        PartDefinition bone9 = rope3.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(12, 4).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(12, 6).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.3F, -0.0333F, 1.0667F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone10 = rope3.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(12, 5).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(12, 7).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.3F, -0.0333F, -1.0333F, -0.0319F, -0.0064F, -0.0646F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        body.render(poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {

    }

}
