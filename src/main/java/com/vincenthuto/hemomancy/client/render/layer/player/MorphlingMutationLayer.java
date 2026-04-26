package com.vincenthuto.hemomancy.client.render.layer.player;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment;
import com.vincenthuto.hemomancy.client.morphling.MorphlingMutationRegistry;
import com.vincenthuto.hemomancy.client.morphling.MorphlingVisualMutation;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders a colored translucent overlay on the player's model when they have
 * a morphling equipped. Each morphling type defines its own tint color, pulse
 * speed, and whether to use an animated swirl or a plain glowing skin overlay.
 * The effect's intensity scales with the morphling's maturity level.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingMutationLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    // Alpha multiplier per maturity level (0 = Unfed … 4 = Apex).
    // Apex is the anchor — the mutation's design alpha is applied at full
    // strength only at Apex, lower maturities render proportionally dimmer.
    private static final float[] MATURITY_ALPHA_SCALE = { 0.45f, 0.58f, 0.72f, 0.86f, 1.00f };

    private static int packColor(float red, float green, float blue, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public MorphlingMutationLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {

        if (!HemoClientConfig.RENDER_MORPHLING_MUTATION_LAYER.get())
            return;

        if (!(entity instanceof Player player))
            return;

        HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
            ItemStack morphlingStack = cap.getEquippedMorphling();
            if (morphlingStack.isEmpty())
                return;

            MorphlingVisualMutation mutation = MorphlingMutationRegistry.get(morphlingStack);
            if (mutation == null)
                return;

            int maturity = MorphlingItem.getMaturityLevel(morphlingStack);
            float maturityScale = MATURITY_ALPHA_SCALE[maturity];

            // Sine-wave pulse — amplitude is ±30 % of the alpha
            float pulse = 1.0f;
            if (mutation.pulseSpeed > 0f) {
                pulse = 0.70f + 0.30f * (float) Math.sin(ageInTicks * mutation.pulseSpeed);
            }

            float finalAlpha = mutation.alpha * maturityScale * pulse;

            M model = this.getParentModel();
            poseStack.pushPose();

            VertexConsumer consumer;
            if (mutation.swirlTexture != null) {
                float scrollU = (ageInTicks * mutation.swirlSpeed) % 1.0f;
                float scrollV = (ageInTicks * mutation.swirlSpeed * 0.5f) % 1.0f;
                consumer = buffer.getBuffer(RenderTypeInit.energySwirl(mutation.swirlTexture, scrollU, scrollV));
            } else if (mutation.emissive) {
                consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(getSkinTexture(player)));
            } else {
                consumer = buffer.getBuffer(RenderType.entityTranslucent(getSkinTexture(player)));
            }

            model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    packColor(mutation.r, mutation.g, mutation.b, finalAlpha));

            poseStack.popPose();

            // Render any additional 3-D model attachment (wings, plating, tendrils, etc.)
            // Render any additional 3-D model attachment (wings, plating, tendrils, etc.)
            // SimpleBodyAttachment already does its own pushPose/popPose internally.
            MorphlingModelAttachment attachment = mutation.modelAttachment;
            if (attachment != null) {
                attachment.render(poseStack, buffer, packedLight, entity,
                        (HumanoidModel<?>) model,
                        limbSwing, limbSwingAmount, partialTicks,
                        ageInTicks, netHeadYaw, headPitch,
                        finalAlpha);
            }
        });
    }

    private static ResourceLocation getSkinTexture(Player player) {
        if (player instanceof AbstractClientPlayer acp) {
            return acp.getSkin().texture();
        }
        return DefaultPlayerSkin.get(player.getUUID()).texture();
    }
}
