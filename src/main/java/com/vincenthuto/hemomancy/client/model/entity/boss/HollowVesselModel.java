package com.vincenthuto.hemomancy.client.model.entity.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HollowVesselEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Dedicated model for the Hollow Vessel (Saint Hemorath's preserved corpus).
 * <p>
 * Visually distinct from the Harbinger Vicar: the Vessel is emaciated and
 * imposing — elongated torso and limbs convey the body drained of blood yet
 * still animate, arms hanging low with the weight of decay, draped in funerary
 * wrappings that shift as it stalks the player.
 * <p>
 * UV layout is 64×64, compatible with the dedicated hollow_vessel texture.
 */
public class HollowVesselModel<T extends HollowVesselEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("hollow_vessel"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Sunken, preserved head — intentionally 9×9×9 (slightly wider than the standard
        // 8×8×8) to evoke the swollen, mummified look of a desiccated corpus.
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Hat layer — required by HumanoidModel, left empty
        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create(),
                PartPose.ZERO);

        // Emaciated torso — taller and narrower than a living humanoid;
        // a second, looser burial-wrap band droops from the lower abdomen.
        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 36).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Elongated arms — dangling forward slightly, as if reaching
        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.5F, 2.0F, 0.0F, 0.25F, 0.0F, 0.12F));
        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.5F, 2.0F, 0.0F, 0.25F, 0.0F, -0.12F));

        // Long, stilted legs — wide stance, slightly splayed
        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.2F, 14.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.2F, 14.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public HollowVesselModel(ModelPart root) {
        super(root);
    }
}
