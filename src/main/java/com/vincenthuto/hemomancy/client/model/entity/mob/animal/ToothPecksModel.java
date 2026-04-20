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

// Flat molar-shaped body + 8 spider legs.
// As feedCount rises (0-5), blood lumps appear on the body and the
// renderer scales the whole entity up to show it growing engorged.
public class ToothPecksModel<T extends ToothPecksEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("tooth_pecks"), "main");

    private final ModelPart root;
    private final ModelPart body;

    // Progressive blood lumps — each revealed at feedCount >= n
    private final ModelPart lump1, lump2, lump3, lump4, lump5;

    private final ModelPart leg_rf1, leg_rm1, leg_rb1, leg_rr1;
    private final ModelPart leg_lf1, leg_lm1, leg_lb1, leg_lr1;

    public ToothPecksModel(ModelPart root) {
        this.root = root;
        ModelPart full = root.getChild("full");
        this.body = full.getChild("body");

        this.lump1 = body.getChild("lump1");
        this.lump2 = body.getChild("lump2");
        this.lump3 = body.getChild("lump3");
        this.lump4 = body.getChild("lump4");
        this.lump5 = body.getChild("lump5");

        ModelPart legs = full.getChild("legs");
        this.leg_rf1 = legs.getChild("leg_rf1");
        this.leg_rm1 = legs.getChild("leg_rm1");
        this.leg_rb1 = legs.getChild("leg_rb1");
        this.leg_rr1 = legs.getChild("leg_rr1");
        this.leg_lf1 = legs.getChild("leg_lf1");
        this.leg_lm1 = legs.getChild("leg_lm1");
        this.leg_lb1 = legs.getChild("leg_lb1");
        this.leg_lr1 = legs.getChild("leg_lr1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition full = root.addOrReplaceChild("full", CubeListBuilder.create(),
                PartPose.offset(0.0F, 23.0F, 0.0F));

        // ── Body ─────────────────────────────────────────────────────────────
        // Texture layout (64×64):
        //   base 6×2×6     → texOffs( 0,  0)  24w × 8h
        //   cusp0 2×3×2    → texOffs( 0,  8)   8w × 5h
        //   cusp1 2×2×2    → texOffs( 8,  8)   8w × 4h
        //   cusp2 2×2×2    → texOffs(16,  8)   8w × 4h
        //   lump (2×1×2)   → texOffs(24,  8)   8w × 3h  ← small drip lumps
        //   lump big 4×2×4 → texOffs(32,  8)  16w × 6h  ← stage-5 large blob
        PartDefinition body = full.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-2.5F, -5.0F, -2.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 8).addBox( 0.5F, -4.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 8).addBox(-1.0F, -4.0F,  0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));

        // ── Blood lumps (children of body, start hidden) ──────────────────────
        // lump1: small drip on the right-back corner of the flat base top
        body.addOrReplaceChild("lump1",
                CubeListBuilder.create()
                        .texOffs(24, 8).addBox(0.0F, -3.0F, 0.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);

        // lump2: drip on the left-front corner of the flat base top
        body.addOrReplaceChild("lump2",
                CubeListBuilder.create()
                        .texOffs(24, 8).addBox(-2.5F, -3.0F, -3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);

        // lump3: engorged bubble swelling off the top of the tall front-left cusp
        body.addOrReplaceChild("lump3",
                CubeListBuilder.create()
                        .texOffs(24, 8).addBox(-3.0F, -6.5F, -3.0F, 2.5F, 1.5F, 2.5F, new CubeDeformation(0.15F)),
                PartPose.ZERO);

        // lump4: side drip hanging off the right edge of the base
        body.addOrReplaceChild("lump4",
                CubeListBuilder.create()
                        .texOffs(24, 8).addBox( 3.0F, -1.5F, -1.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);

        // lump5: large central blood mass on top — creature is fully engorged
        body.addOrReplaceChild("lump5",
                CubeListBuilder.create()
                        .texOffs(32, 8).addBox(-2.0F, -7.5F, -2.0F, 4.0F, 2.5F, 4.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO);

        // ── Legs ─────────────────────────────────────────────────────────────
        PartDefinition legs = full.addOrReplaceChild("legs", CubeListBuilder.create(),
                PartPose.ZERO);

        float legY = -2.0F;
        addLegPair(legs, "leg_rf", 2.5F, legY, -2.0F, -0.5F, false);
        addLegPair(legs, "leg_rm", 2.5F, legY, -0.5F, -0.3F, false);
        addLegPair(legs, "leg_rb", 2.5F, legY,  0.5F, -0.3F, false);
        addLegPair(legs, "leg_rr", 2.5F, legY,  2.0F, -0.5F, false);

        addLegPair(legs, "leg_lf", -2.5F, legY, -2.0F, -0.5F, true);
        addLegPair(legs, "leg_lm", -2.5F, legY, -0.5F, -0.3F, true);
        addLegPair(legs, "leg_lb", -2.5F, legY,  0.5F, -0.3F, true);
        addLegPair(legs, "leg_lr", -2.5F, legY,  2.0F, -0.5F, true);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLegPair(PartDefinition legs, String name,
            float x, float y, float z, float zRot, boolean leftSide) {
        float upperZRot = leftSide ? (zRot - 0.4F) : (zRot + 0.4F);
        PartDefinition upper = legs.addOrReplaceChild(name + "1",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(x, y, z, 0.0F, 0.0F, upperZRot));

        float lowerX    = leftSide ? -4.0F : 4.0F;
        float lowerZRot = leftSide ? -0.6F : 0.6F;
        upper.addOrReplaceChild(name + "2",
                CubeListBuilder.create()
                        .texOffs(10, 16).addBox(0.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(lowerX, 0.0F, 0.0F, 0.0F, 0.0F, lowerZRot));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {

        int feeds = entity.getFeedCount();

        // Reveal blood lumps progressively
        lump1.visible = feeds >= 1;
        lump2.visible = feeds >= 2;
        lump3.visible = feeds >= 3;
        lump4.visible = feeds >= 4;
        lump5.visible = feeds >= 5;

        // Walking leg swing
        float swing = Mth.cos(limbSwing * 0.8F) * 0.5F * limbSwingAmount;
        leg_rf1.zRot = (float) Math.toRadians(-30) + swing;
        leg_rm1.zRot = (float) Math.toRadians(-25) - swing;
        leg_rb1.zRot = (float) Math.toRadians(-25) + swing;
        leg_rr1.zRot = (float) Math.toRadians(-30) - swing;
        leg_lf1.zRot = (float) Math.toRadians( 30) - swing;
        leg_lm1.zRot = (float) Math.toRadians( 25) + swing;
        leg_lb1.zRot = (float) Math.toRadians( 25) - swing;
        leg_lr1.zRot = (float) Math.toRadians( 30) + swing;

        // Bite bob while latched
        body.y = entity.isLatched() ? (-3.0F + Mth.sin(ageInTicks * 0.4F) * 0.15F) : -3.0F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        root.getChild("full").render(pose, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
