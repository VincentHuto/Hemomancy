package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.succession.ProfessionalHarbingerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

/** Sparse skin patches and exposed flesh, over the donor profession's authored clothes. */
public final class SuccessionIdentityLayer<T extends ProfessionalHarbingerEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final ModelPart patches;
    private final ModelPart seams;
    public SuccessionIdentityLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("patches", CubeListBuilder.create()
                .texOffs(8, 8).addBox(-3, -7, -4.12f, 3, 4, .15f)
                .texOffs(20, 20).addBox(-4.15f, 3, -2.15f, 3, 5, .2f)
                .texOffs(44, 20).addBox(4.05f, 4, -2.1f, 2, 6, .2f), PartPose.ZERO);
        patches = LayerDefinition.create(mesh, 64, 64).bakeRoot();
        var stitching = new MeshDefinition();
        stitching.getRoot().addOrReplaceChild("seams", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.15f, -7.15f, -4.3f, .3f, 4.4f, .15f)
                .addBox(-3.15f, -3.1f, -4.3f, 3.4f, .3f, .15f)
                .addBox(-4.3f, 2.8f, -2.3f, .3f, 5.5f, .15f)
                .addBox(4, 3.8f, -2.3f, .3f, 6.5f, .15f), PartPose.ZERO);
        seams = LayerDefinition.create(stitching, 16, 16).bakeRoot();
    }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, T npc,
            float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (!npc.isMisbegotten() && !npc.isSuccessor()) return;
        pose.pushPose();
        if (npc.isMisbegotten()) {
            ResourceLocation skin = DefaultPlayerSkin.get(npc.getUUID()).texture();
            var owner = npc.skinOwner();
            if (owner != null && Minecraft.getInstance().getConnection() != null) {
                var info = Minecraft.getInstance().getConnection().getPlayerInfo(owner);
                skin = info == null ? DefaultPlayerSkin.get(owner).texture() : info.getSkin().texture();
            }
            boolean torn = npc.getHealth() < npc.getMaxHealth() * .5f || npc.swinging && npc.tickCount % 4 < 2;
            patches.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(torn
                    ? Hemomancy.rloc("textures/block/bog_flesh_ripe.png") : skin)), light, OverlayTexture.NO_OVERLAY);
            seams.render(pose, buffers.getBuffer(RenderType.eyes(Hemomancy.rloc("textures/block/hematic_iron_block.png"))), 15728880, OverlayTexture.NO_OVERLAY, 0xFFC52A38);
            // A professional implement pierces the shortened arm; imitation is shown in the opposite hand.
            var item = switch (com.vincenthuto.hemomancy.common.succession.SuccessionProfessions.profession(npc)) {
                case "alchemist" -> com.vincenthuto.hemomancy.common.init.ItemInit.bloody_vial.get();
                case "mnemonist" -> com.vincenthuto.hemomancy.common.init.ItemInit.mnemonic_ambergris.get();
                case "artificer" -> com.vincenthuto.hemomancy.common.init.ItemInit.vivianite_scalpel.get();
                case "cicatrix_anchorite" -> com.vincenthuto.hemomancy.common.init.ItemInit.hematic_suture_needle.get();
                default -> com.vincenthuto.hemomancy.common.init.ItemInit.living_staff.get();
            };
            pose.pushPose(); pose.translate(-.34, .52, -.12);
            pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(npc.swinging ? -55 : -22)); pose.scale(.65f, .85f, .65f);
            Minecraft.getInstance().getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(item),
                    net.minecraft.world.item.ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, npc.level(), npc.getId());
            pose.popPose();
            if (!npc.getMainHandItem().isEmpty()) {
                pose.pushPose(); pose.translate(.35, .55, -.24); pose.scale(.5f, .5f, .5f);
                Minecraft.getInstance().getItemRenderer().renderStatic(npc.getMainHandItem(), net.minecraft.world.item.ItemDisplayContext.FIXED,
                        light, OverlayTexture.NO_OVERLAY, pose, buffers, npc.level(), npc.getId()); pose.popPose();
            }
        } else {
            // An individual, stable facial/material tint; the role's professional dress remains readable.
            int[] palette = {0xFFD4AB94, 0xFFAC7866, 0xFFE6C8A3, 0xFF895F50, 0xFFC2927F};
            patches.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(Hemomancy.rloc("textures/block/bog_flesh.png"))),
                    light, OverlayTexture.NO_OVERLAY, palette[Math.floorMod(npc.appearanceSeed(), palette.length)]);
        }
        pose.popPose();
    }
}
