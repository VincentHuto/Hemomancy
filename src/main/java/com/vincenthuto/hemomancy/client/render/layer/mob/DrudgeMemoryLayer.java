package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.npc.DrudgeModel;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Renders the Drudge's equipped Blood Memory item flat on top of its head
 * (the "brain" part of the model), like a tablet resting on the crown.
 */
public class DrudgeMemoryLayer extends RenderLayer<DrudgeEntity, DrudgeModel<DrudgeEntity>> {

    public DrudgeMemoryLayer(RenderLayerParent<DrudgeEntity, DrudgeModel<DrudgeEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       DrudgeEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        String memoryName = entity.getEquippedMemoryName();
        if (memoryName.isEmpty()) return;

        ItemStack stack = resolveMemoryStack(memoryName);
        if (stack == null || stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        // The brain's top surface sits at approximately 0.93 blocks above entity feet.
        // Shift to center over the brain (which is +0.5/16 in X, -1/16 in Z from entity origin).
        poseStack.translate(-0.03125D, .5D, -0.125D);

        // Lay the item flat (face up) by rotating -90° around X
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));

        // Scale to a reasonable size — 0.4 fits nicely on the brain cap
        poseStack.scale(0.4F, 0.4F, 0.4F);

        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
    }

    /**
     * Looks up the first registered {@link BloodMemoryItem} whose manipulation name
     * matches {@code memoryName}. Checks all three item registers in the same order
     * that {@code dropMemoryItemFromRegister} uses.
     */
    private static ItemStack resolveMemoryStack(String memoryName) {
        ItemStack result = findInRegister(memoryName, ItemInit.BASEITEMS);
        if (result != null) return result;
        result = findInRegister(memoryName, ItemInit.HANDHELDITEMS);
        if (result != null) return result;
        return findInRegister(memoryName, ItemInit.SPECIALITEMS);
    }

    private static ItemStack findInRegister(String memoryName,
            DeferredRegister<net.minecraft.world.item.Item> register) {
        for (var holder : register.getEntries()) {
            if (holder.get() instanceof BloodMemoryItem bmi) {
                if (bmi.getManip() != null && bmi.getManip().getName().equals(memoryName)) {
                    return new ItemStack(bmi);
                }
            }
        }
        return null;
    }
}
