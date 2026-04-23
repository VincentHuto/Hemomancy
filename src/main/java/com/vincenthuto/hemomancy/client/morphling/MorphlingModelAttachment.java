package com.vincenthuto.hemomancy.client.morphling;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.vincenthuto.hemomancy.Hemomancy;

/**
 * Defines a 3D model attachment rendered on the player while a morphling is
 * equipped. Extend this class (or use {@link SimpleBodyAttachment}) to add
 * custom geometry on top of the color-overlay effect.
 *
 * <p>Lifecycle: {@link #render} is called once per frame from
 * {@link com.vincenthuto.hemomancy.client.render.layer.player.MorphlingMutationLayer}
 * after the translucent color overlay has been submitted. The pose stack is
 * already reset to the entity root at the time of the call.
 *
 * <p>Usage example:
 * <pre>{@code
 * // In MorphlingMutationRegistry.init():
 * register(ItemInit.morphling_bat.get(),
 *     MorphlingVisualMutation.builder(0.24f, 0.10f, 0.42f, 0.38f)
 *         .pulse(0.06f).emissive()
 *         .attach(SimpleBodyAttachment.of(
 *             AttachmentPoint.BODY, 0f, 0f, 0f, 1f,
 *             Lazy.of(() -> new BatWingsModel(
 *                 Minecraft.getInstance().getEntityModels().bakeLayer(BatWingsModel.LAYER_LOCATION))),
 *             Hemomancy.rloc("textures/models/morphling/bat_wings.png")))
 *         .build());
 * }</pre>
 */
@OnlyIn(Dist.CLIENT)
public abstract class MorphlingModelAttachment {

    protected static int packColor(float red, float green, float blue, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Which part of the player's humanoid model this attachment parents to.
     * Calling {@link #applyTo} translates and rotates the pose stack so that
     * (0, 0, 0) is at the origin of that body part.
     */
    public enum AttachmentPoint {
        HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG;

        @SuppressWarnings("unchecked")
        public void applyTo(PoseStack poseStack, HumanoidModel<?> model) {
            switch (this) {
                case HEAD      -> model.head.translateAndRotate(poseStack);
                case BODY      -> model.body.translateAndRotate(poseStack);
                case RIGHT_ARM -> model.rightArm.translateAndRotate(poseStack);
                case LEFT_ARM  -> model.leftArm.translateAndRotate(poseStack);
                case RIGHT_LEG -> model.rightLeg.translateAndRotate(poseStack);
                case LEFT_LEG  -> model.leftLeg.translateAndRotate(poseStack);
            }
        }
    }

    /**
     * Render this attachment. Called every frame while the owning morphling is
     * equipped. The pose stack is at the entity root; push/pop around your work.
     *
     * @param poseStack       current pose stack (entity root)
     * @param buffer          buffer source for vertex consumers
     * @param packedLight     combined sky/block light value
     * @param entity          the player entity being rendered
     * @param parentModel     the player's humanoid model (already animated)
     * @param limbSwing       limb swing angle
     * @param limbSwingAmount limb swing speed
     * @param partialTicks    partial tick for interpolation
     * @param ageInTicks      total age in ticks (use for animation)
     * @param netHeadYaw      head yaw for head-space calculations
     * @param headPitch       head pitch
     * @param alpha           final opacity (0–1), already scaled by maturity and pulse
     */
    public abstract void render(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, LivingEntity entity, HumanoidModel<?> parentModel,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float alpha);

    // -------------------------------------------------------------------------
    // Convenience factory
    // -------------------------------------------------------------------------

    /**
     * Creates a simple single-model attachment anchored to one body part.
     * The model is supplied lazily so it is only baked on first render.
     *
     * @param point       which body part to parent to
     * @param offX        X offset applied after parenting (in model units)
     * @param offY        Y offset applied after parenting
     * @param offZ        Z offset applied after parenting
     * @param scale       uniform scale applied after the offset
     * @param modelSupply lazy supplier that bakes and returns the model
     * @param texture     texture resource location used with entityTranslucent
     */
    public static SimpleBodyAttachment of(AttachmentPoint point,
            float offX, float offY, float offZ, float scale,
            Supplier<EntityModel<?>> modelSupply, ResourceLocation texture) {
        return new SimpleBodyAttachment(point, offX, offY, offZ, scale, modelSupply, texture);
    }

    // -------------------------------------------------------------------------
    // SimpleBodyAttachment
    // -------------------------------------------------------------------------

    /**
     * Ready-to-use attachment base for attaching a single {@link EntityModel} to
     * one of the player's body parts with a fixed offset, uniform scale, and a
     * plain translucent texture. Extend this class if you need more control (e.g.
     * per-frame animation, emissive pass, multi-part rendering).
     *
     * <pre>{@code
     * // Instantiate inline using the factory:
     * MorphlingModelAttachment.of(
     *     AttachmentPoint.BODY, 0f, -4f, 2f, 0.5f,
     *     Lazy.of(() -> new MyModel(Minecraft.getInstance()
     *         .getEntityModels().bakeLayer(MyModel.LAYER_LOCATION))),
     *     Hemomancy.rloc("textures/models/morphling/my_model.png"))
     * }</pre>
     */
    public static class SimpleBodyAttachment extends MorphlingModelAttachment {

        protected final AttachmentPoint point;
        protected final float offX, offY, offZ, scale;
        private final Supplier<EntityModel<?>> modelSupply;
        private EntityModel<?> modelCache;
        protected final ResourceLocation texture;

        protected SimpleBodyAttachment(AttachmentPoint point,
                float offX, float offY, float offZ, float scale,
                Supplier<EntityModel<?>> modelSupply, ResourceLocation texture) {
            this.point = point;
            this.offX = offX;
            this.offY = offY;
            this.offZ = offZ;
            this.scale = scale;
            this.modelSupply = modelSupply;
            this.texture = texture;
        }

        private EntityModel<?> model() {
            if (modelCache == null) modelCache = modelSupply.get();
            return modelCache;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, LivingEntity entity, HumanoidModel<?> parentModel,
                float limbSwing, float limbSwingAmount, float partialTicks,
                float ageInTicks, float netHeadYaw, float headPitch, float alpha) {
            poseStack.pushPose();
            point.applyTo(poseStack, parentModel);
            poseStack.translate(offX / 16f, offY / 16f, offZ / 16f);
            poseStack.scale(scale, scale, scale);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
            model().renderToBuffer(poseStack, consumer, packedLight,
                    OverlayTexture.NO_OVERLAY, packColor(1f, 1f, 1f, alpha));
            poseStack.popPose();
        }
    }
}
