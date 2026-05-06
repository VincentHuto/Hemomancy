package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Humanoid model for the Harbinger Hermit — a robed, hooded figure
 * with a hunched posture and long, flowing robes suggesting age and wisdom.
 */
public class HarbingerHermitModel<T extends HarbingerHermitEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("harbinger_hermit"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Deep hood — larger and slightly forward-tilted to shadow the face
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1F, 0.0F, 0.0F)); // slight forward tilt

        // Hat layer — empty
        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create(),
                PartPose.ZERO);

        // Heavy robed body — wide and long, with layered robe extensions
        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                        // Inner robe layer
                        .texOffs(0, 32).addBox(-5.0F, 8.0F, -3.0F, 10.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Arms — tucked inward as if clasped, with wide sleeves
        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, -0.2F, 0.0F, 0.05F));
        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)),
                PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, -0.2F, 0.0F, -0.05F));

        // Legs — hidden beneath the heavy robe
        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public HarbingerHermitModel(ModelPart root) {
        super(root);
    }
}
