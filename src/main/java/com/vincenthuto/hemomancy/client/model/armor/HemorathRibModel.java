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

public class HemorathRibModel<T extends LivingEntity> extends EntityModel<T> {

    public static final ModelLayerLocation hemorath_rib = new ModelLayerLocation(
            Hemomancy.rloc("hemorath_rib"), "main");
    private final ModelPart body;
    private final ModelPart horn;
    private final ModelPart horn2;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone7;
    private final ModelPart bone8;
    private final ModelPart bone9;
    private final ModelPart bone10;
    private final ModelPart rope2;
    private final ModelPart bone3;
    private final ModelPart bone4;

    public HemorathRibModel(ModelPart root) {
        this.body = root.getChild("body");
        this.horn = this.body.getChild("horn");
        this.horn2 = this.horn.getChild("horn2");
        this.bone5 = this.horn2.getChild("bone5");
        this.bone6 = this.bone5.getChild("bone6");
        this.bone = this.bone6.getChild("bone");
        this.bone2 = this.bone.getChild("bone2");
        this.bone7 = this.bone2.getChild("bone7");
        this.bone8 = this.bone7.getChild("bone8");
        this.bone9 = this.bone8.getChild("bone9");
        this.bone10 = this.bone9.getChild("bone10");
        this.rope2 = this.horn.getChild("rope2");
        this.bone3 = this.rope2.getChild("bone3");
        this.bone4 = this.rope2.getChild("bone4");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition horn = body.addOrReplaceChild("horn", CubeListBuilder.create(), PartPose.offsetAndRotation(5.75F, 12.5722F, 0.25F, 0.7418F, 0.0F, 0.0F));

        PartDefinition horn2 = horn.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(3, 26).addBox(-1.5F, -3.875F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F))
                .texOffs(9, 4).addBox(-1.5F, -1.875F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F))
                .texOffs(9, 11).addBox(-1.5F, -2.875F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -0.0972F, -0.25F, -0.5119F, -0.0385F, 0.013F));

        PartDefinition bone5 = horn2.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, -4.0396F, 1.6735F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, 4.625F, 0.0F, 0.829F, 0.0F, 0.0F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.3036F, -1.3772F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.8F)), PartPose.offsetAndRotation(0.0F, -0.5336F, 3.1745F, 0.0558F, 0.0201F, 0.0297F));

        PartDefinition bone = bone6.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.7628F, -0.678F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.9F)), PartPose.offsetAndRotation(0.0F, 2.6964F, 0.1228F, 0.8727F, 0.0F, 0.0F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.2609F, -1.7772F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.1F, 0.829F, 0.0F, 0.0F));

        PartDefinition bone7 = bone2.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(0, 0).addBox(-1.55F, -1.75F, -1.35F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-1.1F)), PartPose.offsetAndRotation(0.25F, 3.4725F, -0.0649F, 0.631F, 0.1592F, -0.3173F));

        PartDefinition bone8 = bone7.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.7361F, 0.6492F, 0.9586F, 1.1937F, 0.5559F, -0.4748F));

        PartDefinition bone9 = bone8.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2056F, 0.7293F, -0.9293F, -0.117F, 0.1637F, -0.4045F));

        PartDefinition bone10 = bone9.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1871F, 0.373F, 0.0025F, 0.673F, 0.297F, -0.4627F));

        PartDefinition rope2 = horn.addOrReplaceChild("rope2", CubeListBuilder.create().texOffs(3, 19).addBox(1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-0.1F, -0.8389F, 0.0333F, -0.2269F, 0.0F, 0.0F));

        PartDefinition bone3 = rope2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(4, 20).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, 0.8727F, 0.0F));

        PartDefinition bone4 = rope2.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(10, 21).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, 0.0646F));

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