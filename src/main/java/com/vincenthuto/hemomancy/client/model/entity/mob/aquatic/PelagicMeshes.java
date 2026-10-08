package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from tools/model_export/pelagic_ecology.py. Animation lives in PelagicCreatureModel. */
public final class PelagicMeshes {
    private PelagicMeshes() {}
    public static LayerDefinition create(String id) {
        return switch (id) {
            case "chiton" -> chiton();
            case "pyrosome" -> pyrosome();
            case "pelagic_herring" -> pelagic_herring();
            case "siphonophore" -> siphonophore();
            case "bloody_belly_comb_jelly" -> bloody_belly_comb_jelly();
            case "hagfish" -> hagfish();
            case "tidepool_anemone" -> tidepool_anemone();
            case "bone_worm_colony" -> bone_worm_colony();
            case "giant_tube_worm_colony" -> giant_tube_worm_colony();
            default -> throw new IllegalArgumentException("Unknown Pelagic rig " + id);
        };
    }
    private static LayerDefinition chiton() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-4.5F, -1.5F, -4F, 9F, 1.5F, 8F)
                .texOffs(37, 1).addBox(-3.5F, -1.5F, -6F, 7F, 1.5F, 12F)
                .texOffs(77, 1).addBox(-2.25F, -1.5F, -7.5F, 4.5F, 1.5F, 15F), PartPose.offsetAndRotation(0.0000000F, 24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate0 = body.addOrReplaceChild("plate0", CubeListBuilder.create()
                .texOffs(1, 21).addBox(-2.0F, -2F, -1F, 4F, 2F, 2.25F)
                .texOffs(16, 21).addBox(-1.0F, -2.75F, -0.75F, 2F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -6.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate1 = body.addOrReplaceChild("plate1", CubeListBuilder.create()
                .texOffs(25, 21).addBox(-3.0F, -2F, -1F, 6F, 2F, 2.25F)
                .texOffs(44, 21).addBox(-2.0F, -2.75F, -0.75F, 4F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -4.2500000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate2 = body.addOrReplaceChild("plate2", CubeListBuilder.create()
                .texOffs(57, 21).addBox(-4.0F, -2F, -1F, 8F, 2F, 2.25F)
                .texOffs(80, 21).addBox(-3.0F, -2.75F, -0.75F, 6F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -2.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate3 = body.addOrReplaceChild("plate3", CubeListBuilder.create()
                .texOffs(97, 21).addBox(-4.25F, -2F, -1F, 8.5F, 2F, 2.25F)
                .texOffs(1, 29).addBox(-3.25F, -2.75F, -0.75F, 6.5F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -0.7500000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate4 = body.addOrReplaceChild("plate4", CubeListBuilder.create()
                .texOffs(19, 29).addBox(-4.25F, -2F, -1F, 8.5F, 2F, 2.25F)
                .texOffs(43, 29).addBox(-3.25F, -2.75F, -0.75F, 6.5F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, 1.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate5 = body.addOrReplaceChild("plate5", CubeListBuilder.create()
                .texOffs(61, 29).addBox(-4.0F, -2F, -1F, 8F, 2F, 2.25F)
                .texOffs(84, 29).addBox(-3.0F, -2.75F, -0.75F, 6F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, 2.7500000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate6 = body.addOrReplaceChild("plate6", CubeListBuilder.create()
                .texOffs(101, 29).addBox(-3.0F, -2F, -1F, 6F, 2F, 2.25F)
                .texOffs(1, 37).addBox(-2.0F, -2.75F, -0.75F, 4F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, 4.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plate7 = body.addOrReplaceChild("plate7", CubeListBuilder.create()
                .texOffs(14, 37).addBox(-2.0F, -2F, -1F, 4F, 2F, 2.25F)
                .texOffs(29, 37).addBox(-1.0F, -2.75F, -0.75F, 2F, 0.75F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, 6.2500000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition mouth = body.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(38, 37).addBox(-1.5F, -0.25F, -1F, 3F, 0.5F, 1.5F), PartPose.offsetAndRotation(0.0000000F, -0.7500000F, -7.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition pyrosome() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring0 = body.addOrReplaceChild("ring0", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-3.5F, -3.75F, -3.5F, 1.25F, 4F, 7.0F)
                .texOffs(20, 1).addBox(2.25F, -3.75F, -3.5F, 1.25F, 4F, 7.0F)
                .texOffs(39, 1).addBox(-2.25F, -3.75F, -3.5F, 4.5F, 4F, 1.25F)
                .texOffs(53, 1).addBox(-2.25F, -3.75F, 2.25F, 4.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring1 = body.addOrReplaceChild("ring1", CubeListBuilder.create()
                .texOffs(67, 1).addBox(-4F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(88, 1).addBox(2.75F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(109, 1).addBox(-2.75F, -3.75F, -4F, 5.5F, 4F, 1.25F)
                .texOffs(1, 16).addBox(-2.75F, -3.75F, 2.75F, 5.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring2 = body.addOrReplaceChild("ring2", CubeListBuilder.create()
                .texOffs(17, 16).addBox(-4F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(38, 16).addBox(2.75F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(59, 16).addBox(-2.75F, -3.75F, -4F, 5.5F, 4F, 1.25F)
                .texOffs(75, 16).addBox(-2.75F, -3.75F, 2.75F, 5.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -7.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring3 = body.addOrReplaceChild("ring3", CubeListBuilder.create()
                .texOffs(91, 16).addBox(-4F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(1, 31).addBox(2.75F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(22, 31).addBox(-2.75F, -3.75F, -4F, 5.5F, 4F, 1.25F)
                .texOffs(38, 31).addBox(-2.75F, -3.75F, 2.75F, 5.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -10.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring4 = body.addOrReplaceChild("ring4", CubeListBuilder.create()
                .texOffs(54, 31).addBox(-4F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(75, 31).addBox(2.75F, -3.75F, -4F, 1.25F, 4F, 8F)
                .texOffs(96, 31).addBox(-2.75F, -3.75F, -4F, 5.5F, 4F, 1.25F)
                .texOffs(1, 46).addBox(-2.75F, -3.75F, 2.75F, 5.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -14.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition ring5 = body.addOrReplaceChild("ring5", CubeListBuilder.create()
                .texOffs(17, 46).addBox(-3.5F, -3.75F, -3.5F, 1.25F, 4F, 7.0F)
                .texOffs(36, 46).addBox(2.25F, -3.75F, -3.5F, 1.25F, 4F, 7.0F)
                .texOffs(55, 46).addBox(-2.25F, -3.75F, -3.5F, 4.5F, 4F, 1.25F)
                .texOffs(69, 46).addBox(-2.25F, -3.75F, 2.25F, 4.5F, 4F, 1.25F), PartPose.offsetAndRotation(0.0000000F, -17.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition pelagic_herring() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-1.5F, -2F, -4F, 3F, 4F, 7.5F)
                .texOffs(24, 1).addBox(-1F, -1.5F, -6F, 2F, 3F, 2.25F), PartPose.offsetAndRotation(0.0000000F, 21.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(35, 1).addBox(-0.75F, -1F, -0.25F, 1.5F, 2F, 2F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.2500000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition forkn1 = tail.addOrReplaceChild("forkn1", CubeListBuilder.create()
                .texOffs(44, 1).addBox(-0.25F, -1.5F, -0.25F, 0.5F, 3F, 3F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 1.5000000F, -0.4886922F, 0.0000000F, 0.0000000F));
        PartDefinition finn1 = body.addOrReplaceChild("finn1", CubeListBuilder.create()
                .texOffs(53, 1).addBox(-0.25F, 0F, 0F, 0.5F, 1.5F, 3F), PartPose.offsetAndRotation(-1.2500000F, 0.5000000F, -2.0000000F, 0.0000000F, -0.4886922F, 0.3490659F));
        PartDefinition fork1 = tail.addOrReplaceChild("fork1", CubeListBuilder.create()
                .texOffs(62, 1).addBox(-0.25F, -1.5F, -0.25F, 0.5F, 3F, 3F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 1.5000000F, 0.4886922F, 0.0000000F, 0.0000000F));
        PartDefinition fin1 = body.addOrReplaceChild("fin1", CubeListBuilder.create()
                .texOffs(71, 1).addBox(-0.25F, 0F, 0F, 0.5F, 1.5F, 3F), PartPose.offsetAndRotation(1.2500000F, 0.5000000F, -2.0000000F, 0.0000000F, 0.4886922F, -0.3490659F));
        PartDefinition dorsal = body.addOrReplaceChild("dorsal", CubeListBuilder.create()
                .texOffs(80, 1).addBox(-0.25F, -2F, -1.25F, 0.5F, 2F, 3.5F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -0.5000000F, -0.2792527F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition siphonophore() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-2F, -6F, -2F, 4F, 5F, 4F)
                .texOffs(19, 1).addBox(-0.75F, -1.5F, -0.75F, 1.5F, 14F, 1.5F), PartPose.offsetAndRotation(0.0000000F, 12.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition bell0 = body.addOrReplaceChild("bell0", CubeListBuilder.create()
                .texOffs(27, 1).addBox(0.25F, -1F, -1.25F, 2.5F, 3F, 2.5F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition bell1 = body.addOrReplaceChild("bell1", CubeListBuilder.create()
                .texOffs(39, 1).addBox(-2.75F, -1F, -1.25F, 2.5F, 3F, 2.5F), PartPose.offsetAndRotation(0.0000000F, 2.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition bell2 = body.addOrReplaceChild("bell2", CubeListBuilder.create()
                .texOffs(51, 1).addBox(0.25F, -1F, -1.25F, 2.5F, 3F, 2.5F), PartPose.offsetAndRotation(0.0000000F, 5.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition bell3 = body.addOrReplaceChild("bell3", CubeListBuilder.create()
                .texOffs(63, 1).addBox(-2.75F, -1F, -1.25F, 2.5F, 3F, 2.5F), PartPose.offsetAndRotation(0.0000000F, 7.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition bell4 = body.addOrReplaceChild("bell4", CubeListBuilder.create()
                .texOffs(75, 1).addBox(0.25F, -1F, -1.25F, 2.5F, 3F, 2.5F), PartPose.offsetAndRotation(0.0000000F, 10.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament0_0 = body.addOrReplaceChild("filament0_0", CubeListBuilder.create()
                .texOffs(87, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(1.5000000F, 9.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament0_1 = filament0_0.addOrReplaceChild("filament0_1", CubeListBuilder.create()
                .texOffs(91, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament0_2 = filament0_1.addOrReplaceChild("filament0_2", CubeListBuilder.create()
                .texOffs(95, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(99, 1).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament0_3 = filament0_2.addOrReplaceChild("filament0_3", CubeListBuilder.create()
                .texOffs(106, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(110, 1).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament1_0 = body.addOrReplaceChild("filament1_0", CubeListBuilder.create()
                .texOffs(117, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.7500000F, 9.0000000F, 1.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament1_1 = filament1_0.addOrReplaceChild("filament1_1", CubeListBuilder.create()
                .texOffs(121, 1).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament1_2 = filament1_1.addOrReplaceChild("filament1_2", CubeListBuilder.create()
                .texOffs(1, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(5, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament1_3 = filament1_2.addOrReplaceChild("filament1_3", CubeListBuilder.create()
                .texOffs(12, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(16, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament2_0 = body.addOrReplaceChild("filament2_0", CubeListBuilder.create()
                .texOffs(23, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(-0.7500000F, 9.0000000F, 1.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament2_1 = filament2_0.addOrReplaceChild("filament2_1", CubeListBuilder.create()
                .texOffs(27, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament2_2 = filament2_1.addOrReplaceChild("filament2_2", CubeListBuilder.create()
                .texOffs(31, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(35, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament2_3 = filament2_2.addOrReplaceChild("filament2_3", CubeListBuilder.create()
                .texOffs(42, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(46, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament3_0 = body.addOrReplaceChild("filament3_0", CubeListBuilder.create()
                .texOffs(53, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(-1.5000000F, 9.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament3_1 = filament3_0.addOrReplaceChild("filament3_1", CubeListBuilder.create()
                .texOffs(57, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament3_2 = filament3_1.addOrReplaceChild("filament3_2", CubeListBuilder.create()
                .texOffs(61, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(65, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament3_3 = filament3_2.addOrReplaceChild("filament3_3", CubeListBuilder.create()
                .texOffs(72, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(76, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament4_0 = body.addOrReplaceChild("filament4_0", CubeListBuilder.create()
                .texOffs(83, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(-0.7500000F, 9.0000000F, -1.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament4_1 = filament4_0.addOrReplaceChild("filament4_1", CubeListBuilder.create()
                .texOffs(87, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament4_2 = filament4_1.addOrReplaceChild("filament4_2", CubeListBuilder.create()
                .texOffs(91, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(95, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament4_3 = filament4_2.addOrReplaceChild("filament4_3", CubeListBuilder.create()
                .texOffs(102, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(106, 20).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament5_0 = body.addOrReplaceChild("filament5_0", CubeListBuilder.create()
                .texOffs(113, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.7500000F, 9.0000000F, -1.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament5_1 = filament5_0.addOrReplaceChild("filament5_1", CubeListBuilder.create()
                .texOffs(117, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament5_2 = filament5_1.addOrReplaceChild("filament5_2", CubeListBuilder.create()
                .texOffs(121, 20).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(1, 34).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition filament5_3 = filament5_2.addOrReplaceChild("filament5_3", CubeListBuilder.create()
                .texOffs(8, 34).addBox(-0.25F, -0.5F, -0.25F, 0.5F, 10F, 0.5F)
                .texOffs(12, 34).addBox(-0.75F, 7.5F, -0.5F, 1.5F, 1F, 1F), PartPose.offsetAndRotation(0.0000000F, 9.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition bloody_belly_comb_jelly() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-3.5F, -4.5F, -3.5F, 7F, 9F, 7F)
                .texOffs(31, 1).addBox(-2.5F, -6F, -2.5F, 5F, 1.5F, 5F)
                .texOffs(53, 1).addBox(-2.5F, 4.5F, -2.5F, 5F, 1.5F, 5F), PartPose.offsetAndRotation(0.0000000F, 17.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition gut = body.addOrReplaceChild("gut", CubeListBuilder.create()
                .texOffs(75, 1).addBox(-1.5F, -2.5F, -1.5F, 3F, 5.5F, 3F)
                .texOffs(89, 1).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb0 = body.addOrReplaceChild("comb0", CubeListBuilder.create()
                .texOffs(95, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(-3.5000000F, 0.0000000F, -2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb1 = body.addOrReplaceChild("comb1", CubeListBuilder.create()
                .texOffs(99, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(-3.5000000F, 0.0000000F, 2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb2 = body.addOrReplaceChild("comb2", CubeListBuilder.create()
                .texOffs(103, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(3.5000000F, 0.0000000F, -2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb3 = body.addOrReplaceChild("comb3", CubeListBuilder.create()
                .texOffs(107, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(3.5000000F, 0.0000000F, 2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb4 = body.addOrReplaceChild("comb4", CubeListBuilder.create()
                .texOffs(111, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(-2.0000000F, 0.0000000F, -3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb5 = body.addOrReplaceChild("comb5", CubeListBuilder.create()
                .texOffs(115, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(2.0000000F, 0.0000000F, -3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb6 = body.addOrReplaceChild("comb6", CubeListBuilder.create()
                .texOffs(119, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(-2.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition comb7 = body.addOrReplaceChild("comb7", CubeListBuilder.create()
                .texOffs(123, 1).addBox(-0.25F, -4.5F, -0.25F, 0.5F, 9F, 0.5F), PartPose.offsetAndRotation(2.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition lobe0 = body.addOrReplaceChild("lobe0", CubeListBuilder.create()
                .texOffs(1, 20).addBox(-1.5F, -0.5F, -2.5F, 3F, 3F, 5F), PartPose.offsetAndRotation(-2.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, -0.2617994F));
        PartDefinition lobe1 = body.addOrReplaceChild("lobe1", CubeListBuilder.create()
                .texOffs(19, 20).addBox(-1.5F, -0.5F, -2.5F, 3F, 3F, 5F), PartPose.offsetAndRotation(2.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.2617994F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition hagfish() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-2F, -1.5F, -3F, 4F, 3F, 5F), PartPose.offsetAndRotation(0.0000000F, 21.5000000F, -7.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail0 = body.addOrReplaceChild("tail0", CubeListBuilder.create()
                .texOffs(21, 1).addBox(-1.75F, -1.25F, -0.5F, 3.5F, 2.5F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 1.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail1 = tail0.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(39, 1).addBox(-1.5F, -1F, -0.5F, 3.0F, 2.5F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(56, 1).addBox(-1.25F, -1.0F, -0.5F, 2.5F, 2.5F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(72, 1).addBox(-1.0F, -1F, -0.5F, 2.0F, 2F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(87, 1).addBox(-0.75F, -0.75F, -0.5F, 1.5F, 2F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(101, 1).addBox(-0.5F, -1F, -0.5F, 1.0F, 2F, 4.25F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 3.5000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition barbel0 = body.addOrReplaceChild("barbel0", CubeListBuilder.create()
                .texOffs(114, 1).addBox(-0.25F, -0.25F, -3F, 0.5F, 0.5F, 3.5F), PartPose.offsetAndRotation(-1.2500000F, 0.2500000F, -2.5000000F, -0.3490659F, -0.4363323F, 0.0000000F));
        PartDefinition barbel1 = body.addOrReplaceChild("barbel1", CubeListBuilder.create()
                .texOffs(1, 12).addBox(-0.25F, -0.25F, -3F, 0.5F, 0.5F, 3.5F), PartPose.offsetAndRotation(1.2500000F, 0.2500000F, -2.5000000F, -0.3490659F, 0.4363323F, 0.0000000F));
        PartDefinition barbel2 = body.addOrReplaceChild("barbel2", CubeListBuilder.create()
                .texOffs(11, 12).addBox(-0.25F, -0.25F, -3F, 0.5F, 0.5F, 3.5F), PartPose.offsetAndRotation(-1.2500000F, 0.2500000F, -2.5000000F, 0.3490659F, -0.4363323F, 0.0000000F));
        PartDefinition barbel3 = body.addOrReplaceChild("barbel3", CubeListBuilder.create()
                .texOffs(21, 12).addBox(-0.25F, -0.25F, -3F, 0.5F, 0.5F, 3.5F), PartPose.offsetAndRotation(1.2500000F, 0.2500000F, -2.5000000F, 0.3490659F, 0.4363323F, 0.0000000F));
        PartDefinition slime = body.addOrReplaceChild("slime", CubeListBuilder.create()
                .texOffs(31, 12).addBox(-4F, -2F, -4F, 8F, 4F, 10F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition tidepool_anemone() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-4F, -2F, -4F, 8F, 2F, 8F)
                .texOffs(35, 1).addBox(-1.5F, -2.5F, -1.5F, 3F, 0.5F, 3F), PartPose.offsetAndRotation(0.0000000F, 24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plume0 = body.addOrReplaceChild("plume0", CubeListBuilder.create()
                .texOffs(49, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(3.0000000F, -1.5000000F, 0.0000000F, -0.0000000F, 0.0000000F, 0.4363323F));
        PartDefinition plume1 = body.addOrReplaceChild("plume1", CubeListBuilder.create()
                .texOffs(55, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(2.0000000F, -1.5000000F, 2.0000000F, -0.3085335F, 0.0000000F, 0.3085335F));
        PartDefinition plume2 = body.addOrReplaceChild("plume2", CubeListBuilder.create()
                .texOffs(61, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, 3.0000000F, -0.4363323F, 0.0000000F, 0.0000000F));
        PartDefinition plume3 = body.addOrReplaceChild("plume3", CubeListBuilder.create()
                .texOffs(67, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(-2.0000000F, -1.5000000F, 2.0000000F, -0.3085335F, 0.0000000F, -0.3085335F));
        PartDefinition plume4 = body.addOrReplaceChild("plume4", CubeListBuilder.create()
                .texOffs(73, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(-3.0000000F, -1.5000000F, 0.0000000F, -0.0000000F, 0.0000000F, -0.4363323F));
        PartDefinition plume5 = body.addOrReplaceChild("plume5", CubeListBuilder.create()
                .texOffs(79, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(-2.0000000F, -1.5000000F, -2.0000000F, 0.3085335F, 0.0000000F, -0.3085335F));
        PartDefinition plume6 = body.addOrReplaceChild("plume6", CubeListBuilder.create()
                .texOffs(85, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(0.0000000F, -1.5000000F, -3.0000000F, 0.4363323F, 0.0000000F, -0.0000000F));
        PartDefinition plume7 = body.addOrReplaceChild("plume7", CubeListBuilder.create()
                .texOffs(91, 1).addBox(-0.5F, -5F, -0.5F, 1F, 5.25F, 1F), PartPose.offsetAndRotation(2.0000000F, -1.5000000F, -2.0000000F, 0.3085335F, 0.0000000F, 0.3085335F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition bone_worm_colony() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube0 = body.addOrReplaceChild("tube0", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-1F, -3F, -1F, 2F, 3F, 2F), PartPose.offsetAndRotation(-3.0000000F, 0.0000000F, -2.0000000F, 0.0000000F, 0.0000000F, -0.0698132F));
        PartDefinition plume0 = tube0.addOrReplaceChild("plume0", CubeListBuilder.create()
                .texOffs(11, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(21, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -3.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube1 = body.addOrReplaceChild("tube1", CubeListBuilder.create()
                .texOffs(31, 1).addBox(-1F, -4F, -1F, 2F, 4F, 2F), PartPose.offsetAndRotation(2.0000000F, 0.0000000F, -3.0000000F, 0.0000000F, 0.0000000F, -0.0349066F));
        PartDefinition plume1 = tube1.addOrReplaceChild("plume1", CubeListBuilder.create()
                .texOffs(41, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(51, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -4.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube2 = body.addOrReplaceChild("tube2", CubeListBuilder.create()
                .texOffs(61, 1).addBox(-1F, -5F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plume2 = tube2.addOrReplaceChild("plume2", CubeListBuilder.create()
                .texOffs(71, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(81, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -5.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube3 = body.addOrReplaceChild("tube3", CubeListBuilder.create()
                .texOffs(91, 1).addBox(-1F, -3F, -1F, 2F, 3F, 2F), PartPose.offsetAndRotation(4.0000000F, 0.0000000F, 3.0000000F, 0.0000000F, 0.0000000F, 0.0349066F));
        PartDefinition plume3 = tube3.addOrReplaceChild("plume3", CubeListBuilder.create()
                .texOffs(101, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(111, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -3.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube4 = body.addOrReplaceChild("tube4", CubeListBuilder.create()
                .texOffs(1, 11).addBox(-1F, -4F, -1F, 2F, 4F, 2F), PartPose.offsetAndRotation(-4.0000000F, 0.0000000F, 3.0000000F, 0.0000000F, 0.0000000F, 0.0698132F));
        PartDefinition plume4 = tube4.addOrReplaceChild("plume4", CubeListBuilder.create()
                .texOffs(11, 11).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(21, 11).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -4.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }

    private static LayerDefinition giant_tube_worm_colony() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0000000F, 24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube0 = body.addOrReplaceChild("tube0", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-1F, -18F, -1F, 2F, 18F, 2F), PartPose.offsetAndRotation(-3.0000000F, 0.0000000F, -2.0000000F, 0.0000000F, 0.0000000F, -0.0698132F));
        PartDefinition plume0 = tube0.addOrReplaceChild("plume0", CubeListBuilder.create()
                .texOffs(11, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(21, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -18.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube1 = body.addOrReplaceChild("tube1", CubeListBuilder.create()
                .texOffs(31, 1).addBox(-1F, -24F, -1F, 2F, 24F, 2F), PartPose.offsetAndRotation(2.0000000F, 0.0000000F, -3.0000000F, 0.0000000F, 0.0000000F, -0.0349066F));
        PartDefinition plume1 = tube1.addOrReplaceChild("plume1", CubeListBuilder.create()
                .texOffs(41, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(51, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -24.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube2 = body.addOrReplaceChild("tube2", CubeListBuilder.create()
                .texOffs(61, 1).addBox(-1F, -21F, -1F, 2F, 21F, 2F), PartPose.offsetAndRotation(0.0000000F, 0.0000000F, 2.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition plume2 = tube2.addOrReplaceChild("plume2", CubeListBuilder.create()
                .texOffs(71, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(81, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -21.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube3 = body.addOrReplaceChild("tube3", CubeListBuilder.create()
                .texOffs(91, 1).addBox(-1F, -15F, -1F, 2F, 15F, 2F), PartPose.offsetAndRotation(4.0000000F, 0.0000000F, 3.0000000F, 0.0000000F, 0.0000000F, 0.0349066F));
        PartDefinition plume3 = tube3.addOrReplaceChild("plume3", CubeListBuilder.create()
                .texOffs(101, 1).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(111, 1).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -15.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        PartDefinition tube4 = body.addOrReplaceChild("tube4", CubeListBuilder.create()
                .texOffs(1, 30).addBox(-1F, -12F, -1F, 2F, 12F, 2F), PartPose.offsetAndRotation(-4.0000000F, 0.0000000F, 3.0000000F, 0.0000000F, 0.0000000F, 0.0698132F));
        PartDefinition plume4 = tube4.addOrReplaceChild("plume4", CubeListBuilder.create()
                .texOffs(11, 30).addBox(-1.5F, -3F, -0.5F, 3F, 3.5F, 1F)
                .texOffs(21, 30).addBox(-0.5F, -3F, -1.5F, 1F, 3.5F, 3F), PartPose.offsetAndRotation(0.0000000F, -12.0000000F, 0.0000000F, 0.0000000F, 0.0000000F, 0.0000000F));
        return LayerDefinition.create(mesh, 128, 256);
    }
}
