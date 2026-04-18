package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

// A small creature that mimics the Devil's Tooth fungus block:
// flat molar-shaped body with blood-drop spots and 8 chitinous spider legs.
public class ToothPecksModel<T extends ToothPecksEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("tooth_pecks"), "main");

    private final ModelPart root;
    private final ModelPart body;
    // Legs: 4 pairs (rf=right-front, rm=right-mid, rb=right-back, rr=right-rear)
    private final ModelPart leg_rf1, leg_rf2;
    private final ModelPart leg_rm1, leg_rm2;
    private final ModelPart leg_rb1, leg_rb2;
    private final ModelPart leg_rr1, leg_rr2;
    private final ModelPart leg_lf1, leg_lf2;
    private final ModelPart leg_lm1, leg_lm2;
    private final ModelPart leg_lb1, leg_lb2;
    private final ModelPart leg_lr1, leg_lr2;

    public ToothPecksModel(ModelPart root) {
        this.root = root;
        ModelPart full = root.getChild("full");
        this.body = full.getChild("body");

        ModelPart legs = full.getChild("legs");
        this.leg_rf1 = legs.getChild("leg_rf1");
        this.leg_rf2 = leg_rf1.getChild("leg_rf2");
        this.leg_rm1 = legs.getChild("leg_rm1");
        this.leg_rm2 = leg_rm1.getChild("leg_rm2");
        this.leg_rb1 = legs.getChild("leg_rb1");
        this.leg_rb2 = leg_rb1.getChild("leg_rb2");
        this.leg_rr1 = legs.getChild("leg_rr1");
        this.leg_rr2 = leg_rr1.getChild("leg_rr2");
        this.leg_lf1 = legs.getChild("leg_lf1");
        this.leg_lf2 = leg_lf1.getChild("leg_lf2");
        this.leg_lm1 = legs.getChild("leg_lm1");
        this.leg_lm2 = leg_lm1.getChild("leg_lm2");
        this.leg_lb1 = legs.getChild("leg_lb1");
        this.leg_lb2 = leg_lb1.getChild("leg_lb2");
        this.leg_lr1 = legs.getChild("leg_lr1");
        this.leg_lr2 = leg_lr1.getChild("leg_lr2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Root group offset — entity sits low to the ground (y=23)
        PartDefinition full = root.addOrReplaceChild("full", CubeListBuilder.create(),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        // ── Body ─────────────────────────────────────────────────────────────
        // Molar-shaped tooth body: wide flat base with 3 cusps on top.
        // Texture layout (64×64):
        //   base 6×2×6  → texOffs(0,0)  needs 24w × 8h
        //   cusp0 2×3×2 → texOffs(0,8)  needs 8w × 5h
        //   cusp1 2×2×2 → texOffs(8,8)  needs 8w × 4h
        //   cusp2 2×2×2 → texOffs(16,8) needs 8w × 4h
        PartDefinition body = full.addOrReplaceChild("body", CubeListBuilder.create()
                // wide flat base (like mushroom cap hugging the ground)
                .texOffs(0, 0).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                // front-left cusp — tallest
                .texOffs(0, 8).addBox(-2.5F, -5.0F, -2.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                // front-right cusp
                .texOffs(8, 8).addBox(0.5F, -4.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                // rear cusp
                .texOffs(16, 8).addBox(-1.0F, -4.0F, 0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // ── Legs ─────────────────────────────────────────────────────────────
        // 8 legs, 4 per side, each with an upper and lower segment.
        // Upper segment: 4×1×1, lower segment: 3×1×1
        // Leg UVs (shared): texOffs(0,16) for uppers, texOffs(10,16) for lowers
        PartDefinition legs = full.addOrReplaceChild("legs", CubeListBuilder.create(),
                PartPose.ZERO);

        float legY = -2.0F; // vertical attachment point on body

        // RIGHT SIDE (positive X) — legs angle outward and forward/back
        addLegPair(legs, "leg_rf", 2.5F, legY, -2.0F, -0.5F, false);   // right front
        addLegPair(legs, "leg_rm", 2.5F, legY, -0.5F, -0.3F, false);   // right mid-front
        addLegPair(legs, "leg_rb", 2.5F, legY, 0.5F, -0.3F, false);    // right mid-back
        addLegPair(legs, "leg_rr", 2.5F, legY, 2.0F, -0.5F, false);    // right rear

        // LEFT SIDE (negative X) — mirror of right
        addLegPair(legs, "leg_lf", -2.5F, legY, -2.0F, -0.5F, true);
        addLegPair(legs, "leg_lm", -2.5F, legY, -0.5F, -0.3F, true);
        addLegPair(legs, "leg_lb", -2.5F, legY, 0.5F, -0.3F, true);
        addLegPair(legs, "leg_lr", -2.5F, legY, 2.0F, -0.5F, true);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLegPair(PartDefinition legs, String name,
            float x, float y, float z, float zRot, boolean leftSide) {

        // upper segment — angles outward from body
        float upperZRot = leftSide ? (zRot - 0.4F) : (zRot + 0.4F);
        PartDefinition upper = legs.addOrReplaceChild(name + "1",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(x, y, z, 0.0F, 0.0F, upperZRot));

        // lower segment — bends back toward the ground (positive Z rot for right, negative for left)
        float lowerX = leftSide ? -4.0F : 4.0F;
        float lowerZRot = leftSide ? -0.6F : 0.6F;
        upper.addOrReplaceChild(name + "2",
                CubeListBuilder.create()
                        .texOffs(10, 16).addBox(0.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(lowerX, 0.0F, 0.0F, 0.0F, 0.0F, lowerZRot));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {

        float swing = Mth.cos(limbSwing * 0.8F) * 0.5F * limbSwingAmount;

        // Animate right legs forward / left legs back in alternating pairs
        leg_rf1.zRotation = (float) Math.toRadians(-30) + swing;
        leg_rm1.zRotation = (float) Math.toRadians(-25) - swing;
        leg_rb1.zRotation = (float) Math.toRadians(-25) + swing;
        leg_rr1.zRotation = (float) Math.toRadians(-30) - swing;

        leg_lf1.zRotation = (float) Math.toRadians(30) - swing;
        leg_lm1.zRotation = (float) Math.toRadians(25) + swing;
        leg_lb1.zRotation = (float) Math.toRadians(25) - swing;
        leg_lr1.zRotation = (float) Math.toRadians(30) + swing;

        // Slight bob when latched
        if (entity.isLatched()) {
            float bite = Mth.sin(ageInTicks * 0.4F) * 0.15F;
            body.y = -3.0F + bite;
        } else {
            body.y = -3.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        root.getChild("full").render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
