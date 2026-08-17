package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MuscleMemoryHandRenderer {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/hardened_skin.png");
    private static BloodArmModel<AbstractClientPlayer> overlayModel;

    private MuscleMemoryHandRenderer() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderArm(RenderArmEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        AbstractClientPlayer player = event.getPlayer();
        long now = player.level().getGameTime();
        var state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        var active = state.enabledMemory(EnumVeinSections.ARMS);
        if (active.isEmpty()) {
            return;
        }

        HumanoidArm arm = event.getArm();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        if (overlayModel == null) {
            overlayModel = new BloodArmModel<>(minecraft.getEntityModels().bakeLayer(BloodArmModel.blood_arm));
        }
        overlayModel.setAllVisible(false);
        ModelPart sourceArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        ModelPart modelArm = arm == HumanoidArm.RIGHT ? overlayModel.rightArm : overlayModel.leftArm;
        modelArm.visible = true;
        modelArm.copyFrom(sourceArm);
        modelArm.xRot = 0.0F;
        VertexConsumer consumer = event.getMultiBufferSource()
                .getBuffer(RenderType.entityTranslucent(TEXTURE));
        int color = state.isActive(active.get(), now) ? -1 : 0x99FFFFFF;
        modelArm.render(poseStack, consumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, color);
        poseStack.popPose();
        event.setCanceled(true);
    }
}
