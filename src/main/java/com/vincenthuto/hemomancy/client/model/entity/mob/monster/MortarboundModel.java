package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MortarboundModel extends EntityModel<MortarboundEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("mortarbound"), "main");
    private final ModelPart leftArm, rightArm, cap, fringe, tail, wallGrowth, wallAnchor;
    private final ModelPart[] body;
    private float emergence;

    public MortarboundModel(ModelPart root) {
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.cap = root.getChild("cap");
        this.fringe = cap.getChild("fringe");
        this.tail = root.getChild("tail");
        this.wallGrowth = root.getChild("wall_growth");
        this.wallAnchor = root.getChild("wall_anchor");
        this.body = new ModelPart[] { root.getChild("torso"), root.getChild("neck"), root.getChild("head"), cap,
                leftArm, rightArm, tail, root.getChild("wall_root"), root.getChild("ink_left"), root.getChild("ink_right") };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6, -9, -3, 12, 25, 6), PartPose.ZERO);
        torso.addOrReplaceChild("sculk_veins", CubeListBuilder.create().texOffs(40, 0)
                .addBox(-3, -5, -4, 1, 10, 1).texOffs(40, 0)
                .addBox(2, -3, -4, 1, 11, 1).texOffs(40, 0)
                .addBox(-2, 0, -4, 4, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(68, 0)
                .addBox(-3, -3, -3, 6, 6, 5), PartPose.offset(0, -10, 0));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(96, 0)
                .addBox(-5, -6, -5, 10, 12, 8), PartPose.offset(0, -15, 0));
        head.addOrReplaceChild("face_void", CubeListBuilder.create().texOffs(136, 0)
                .addBox(-3, -2, -6, 6, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(156, 0)
                .addBox(-3, -1, -7, 1, 1, 1).texOffs(156, 0)
                .addBox(2, -1, -7, 1, 1, 1), PartPose.ZERO);
        PartDefinition cap = root.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 36)
                .addBox(-7, -3, -6, 14, 6, 12), PartPose.offset(0, -21, 0));
        cap.addOrReplaceChild("fringe", CubeListBuilder.create().texOffs(56, 36)
                .addBox(-9, 3, -7, 18, 3, 14), PartPose.ZERO);
        cap.addOrReplaceChild("gills", CubeListBuilder.create().texOffs(122, 36)
                .addBox(-6, 3, -5, 12, 2, 10), PartPose.ZERO);
        cap.addOrReplaceChild("shag", CubeListBuilder.create()
                .texOffs(56, 36).addBox(-7, 1, -7, 2, 9, 1)
                .texOffs(56, 36).addBox(-1, 2, -7, 2, 11, 1)
                .texOffs(56, 36).addBox(5, 0, -7, 2, 8, 1), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 64)
                .addBox(0, 0, -2, 4, 18, 5).texOffs(24, 64)
                .addBox(2, 18, -3, 5, 5, 5), PartPose.offset(6, -8, 0));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 64)
                .addBox(-4, 0, -2, 4, 18, 5).texOffs(76, 64)
                .addBox(-7, 18, -3, 5, 5, 5), PartPose.offset(-6, -8, 0));
        leftArm.addOrReplaceChild("fingers", CubeListBuilder.create().texOffs(140, 64)
                .addBox(4, 22, -4, 1, 10, 1).texOffs(140, 64)
                .addBox(7, 22, -3, 1, 9, 1).texOffs(140, 64)
                .addBox(9, 21, -1, 1, 8, 1), PartPose.ZERO);
        rightArm.addOrReplaceChild("fingers", CubeListBuilder.create().texOffs(140, 64)
                .addBox(-5, 22, -4, 1, 10, 1).texOffs(140, 64)
                .addBox(-8, 22, -3, 1, 9, 1).texOffs(140, 64)
                .addBox(-10, 21, -1, 1, 8, 1), PartPose.ZERO);
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(102, 64)
                .addBox(-5, 0, -3, 10, 8, 6), PartPose.offset(0, 16, 0));
        root.addOrReplaceChild("wall_root", CubeListBuilder.create().texOffs(0, 98)
                .addBox(-4, 18, 2, 8, 5, 7.5F), PartPose.ZERO);
        root.addOrReplaceChild("wall_anchor", CubeListBuilder.create().texOffs(36, 98)
                .addBox(-8, 19, 7.75F, 16, 4.75F, 2), PartPose.ZERO);
        root.addOrReplaceChild("ink_left", CubeListBuilder.create().texOffs(150, 64)
                .addBox(-6, -12, -5, 1, 25, 1), PartPose.ZERO);
        root.addOrReplaceChild("ink_right", CubeListBuilder.create().texOffs(158, 64)
                .addBox(5, -10, -5, 1, 28, 1), PartPose.ZERO);
        root.addOrReplaceChild("wall_growth", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-12, -14, 6, 24, 38, 1)
                .texOffs(176, 42).addBox(-16, -8, 6, 4, 28, 1)
                .texOffs(188, 42).addBox(12, -5, 6, 4, 23, 1)
                .texOffs(200, 42).addBox(-4, -24, 5, 8, 10, 2), PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override public void setupAnim(MortarboundEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        emergence = entity.emergence(ageInTicks - entity.tickCount);
        float crawl = (float)Math.sin(ageInTicks * .18F) * .08F;
        leftArm.zRot = -.08F + crawl;
        rightArm.zRot = .08F - crawl;
        leftArm.xRot = entity.attackTicks() > 0 ? -.8F : .08F;
        rightArm.xRot = entity.attackTicks() > 0 ? -.8F : -.08F;
        cap.zRot = (float)Math.sin(ageInTicks * .045F) * .04F;
        fringe.xRot = (float)Math.sin(ageInTicks * .07F) * .04F;
        tail.zRot = -crawl * .5F;
    }

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        pose.pushPose();
        pose.translate(0, 1.5F, .5F);
        pose.scale(1F - .3125F * emergence, 1F - .8125F * emergence, 1F - .5F * emergence);
        pose.translate(0, -1.5F, -.5F);
        wallGrowth.render(pose, buffer, light, overlay, color);
        pose.popPose();
        wallAnchor.render(pose, buffer, light, overlay, color);
        if (emergence <= 0F) return;
        pose.pushPose();
        pose.translate(0, .75, (1F - emergence) * .37F);
        pose.scale(.3F + .7F * emergence, .15F + .85F * emergence, .06F + .94F * emergence);
        pose.translate(0, -.75, 0);
        for (ModelPart part : body) part.render(pose, buffer, light, overlay, color);
        pose.popPose();
    }
}
