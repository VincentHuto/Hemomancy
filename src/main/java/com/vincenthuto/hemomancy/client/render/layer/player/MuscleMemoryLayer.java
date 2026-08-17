package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MuscleMemoryLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel>
        extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/hardened_skin.png");
    private final BloodArmModel<T> model;

    public MuscleMemoryLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        model = new BloodArmModel<>(Minecraft.getInstance().getEntityModels()
                .bakeLayer(BloodArmModel.blood_arm));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T living,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!(living instanceof Player player) || living.hasEffect(MobEffects.INVISIBILITY)) {
            return;
        }
        if (!(getParentModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        model.setAllVisible(false);
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        long now = player.level().getGameTime();
        boolean arms = state.enabledMemory(EnumVeinSections.ARMS).isPresent();
        boolean legs = state.enabledMemory(EnumVeinSections.LEGS).isPresent();
        boolean head = state.enabledMemory(EnumVeinSections.HEAD).isPresent();
        boolean heart = state.enabledMemory(EnumVeinSections.HEART).isPresent();
        boolean body = state.enabledMemory(EnumVeinSections.BODY).isPresent();
        if (!arms && !legs && !head && !heart && !body) return;
        model.rightArm.visible = arms;
        model.leftArm.visible = arms;
        model.rightLeg.visible = legs;
        model.leftLeg.visible = legs;
        model.head.visible = head;
        model.body.visible = heart || body;
        model.rightArm.copyFrom(humanoid.rightArm);
        model.leftArm.copyFrom(humanoid.leftArm);
        model.rightLeg.copyFrom(humanoid.rightLeg);
        model.leftLeg.copyFrom(humanoid.leftLeg);
        model.head.copyFrom(humanoid.head);
        model.body.copyFrom(humanoid.body);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        boolean triggered = java.util.Arrays.stream(MuscleMemory.values()).anyMatch(memory ->
                state.isEnabled(memory) && ((arms && memory.section() == EnumVeinSections.ARMS)
                        || (legs && memory.section() == EnumVeinSections.LEGS)
                        || (head && memory.section() == EnumVeinSections.HEAD)
                        || (heart && memory.section() == EnumVeinSections.HEART)
                        || (body && memory.section() == EnumVeinSections.BODY)));
        int alpha = triggered ? 0xFF000000 : 0x55000000;
        int color = alpha | (heart && !body ? 0x00FF7070 : 0x00FFFFFF);
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
