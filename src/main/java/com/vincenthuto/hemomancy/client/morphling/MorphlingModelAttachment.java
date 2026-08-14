package com.vincenthuto.hemomancy.client.morphling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Defines a 3D model attachment rendered on the player while a morphling is
 * equipped. Extend this class (or use {@link SimpleBodyAttachment}) to add
 * custom geometry on top of the color-overlay effect.
 *
 * <p>Lifecycle: {@link #render} is called once per frame from
 * {@link com.vincenthuto.hemomancy.client.render.layer.player.MorphlingMutationLayer}
 * after the translucent color overlay has been submitted. The pose stack is
 * already reset to the entity root at the time of the call.
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
     * Calling {@link #renderOnParts} translates and rotates the pose stack so
     * that (0, 0, 0) is at the origin of each matching body part.
     * For paired limb sections, simple attachment geometry is authored for the
     * positive-X side and mirrored onto the opposite limb.
     */
    public enum AttachmentPoint {
        HEAD, BODY, ARMS, LEGS;

        public void renderOnParts(PoseStack poseStack, HumanoidModel<?> model, Runnable renderer) {
            switch (this) {
                case HEAD -> renderOnPart(poseStack, model.head, false, renderer);
                case BODY -> renderOnPart(poseStack, model.body, false, renderer);
                case ARMS -> {
                    renderOnPart(poseStack, model.rightArm, true, renderer);
                    renderOnPart(poseStack, model.leftArm, false, renderer);
                }
                case LEGS -> {
                    renderOnPart(poseStack, model.rightLeg, true, renderer);
                    renderOnPart(poseStack, model.leftLeg, false, renderer);
                }
            }
        }

        private static void renderOnPart(PoseStack poseStack, ModelPart part, boolean mirrorX, Runnable renderer) {
            poseStack.pushPose();
            part.translateAndRotate(poseStack);
            if (mirrorX) {
                poseStack.scale(-1.0f, 1.0f, 1.0f);
            }
            renderer.run();
            poseStack.popPose();
        }
    }

    /**
     * Render type used by simple model attachments.
     *
     * <p>Opaque atlases with transparent empty UV space should usually use
     * {@link #CUTOUT_NO_CULL}. Use translucent modes only for intentionally
     * glassy, ghostly, or fading geometry.
     */
    public enum AttachmentRenderType {
        CUTOUT_NO_CULL {
            @Override
            RenderType create(ResourceLocation texture) {
                return RenderType.entityCutoutNoCull(texture);
            }
        },
        CUTOUT {
            @Override
            RenderType create(ResourceLocation texture) {
                return RenderType.entityCutout(texture);
            }
        },
        SOLID {
            @Override
            RenderType create(ResourceLocation texture) {
                return RenderType.entitySolid(texture);
            }
        },
        TRANSLUCENT {
            @Override
            RenderType create(ResourceLocation texture) {
                return RenderType.entityTranslucent(texture);
            }

            @Override
            boolean supportsVariableAlpha() {
                return true;
            }
        },
        TRANSLUCENT_EMISSIVE {
            @Override
            RenderType create(ResourceLocation texture) {
                return RenderType.entityTranslucentEmissive(texture);
            }

            @Override
            boolean supportsVariableAlpha() {
                return true;
            }
        };

        abstract RenderType create(ResourceLocation texture);

        boolean supportsVariableAlpha() {
            return false;
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
     * @param alpha           final overlay opacity (0–1), already scaled by maturity and pulse
     */
    public abstract void render(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, LivingEntity entity, HumanoidModel<?> parentModel,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float alpha);

    /**
     * Maturity-aware render entrypoint. Custom attachments can override this
     * when their geometry, animation, or replacement behavior should change as
     * the morphling grows.
     */
    public void render(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, LivingEntity entity, HumanoidModel<?> parentModel,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float alpha,
            int maturity) {
        render(poseStack, buffer, packedLight, entity, parentModel,
                limbSwing, limbSwingAmount, partialTicks,
                ageInTicks, netHeadYaw, headPitch, alpha);
    }

    /**
     * Player model parts this attachment replaces while rendering.
     *
     * <p>The mutation render hooks hide these parts before the vanilla player
     * model draws, then restore them after all player layers finish. Custom
     * attachments can override this to hide multiple parts for full-body
     * transformations.
     */
    public Set<AttachmentPoint> hiddenPlayerParts() {
        return Set.of();
    }

    /**
     * Maturity-aware replacement hook. By default this preserves the legacy
     * unconditional replacement behavior from {@link #hiddenPlayerParts()}.
     */
    public Set<AttachmentPoint> hiddenPlayerParts(int maturity) {
        return hiddenPlayerParts();
    }

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
     * @param texture     texture resource location used by the attachment render type
     */
    public static SimpleBodyAttachment of(AttachmentPoint point,
            float offX, float offY, float offZ, float scale,
            Supplier<EntityModel<?>> modelSupply, ResourceLocation texture) {
        return new SimpleBodyAttachment(point, offX, offY, offZ, scale, modelSupply, texture);
    }

    /**
     * Ready-to-use attachment base for attaching a single {@link EntityModel} to
     * one of the player's body parts with a fixed offset, uniform scale, and a
     * selectable texture render type. Simple attachments render with cutout-no-cull by
     * default so they read as solid extra geometry. Extend this class if you need more control (e.g.
     * per-frame animation, emissive pass, multi-part rendering).
     */
    public static class SimpleBodyAttachment extends MorphlingModelAttachment {

        protected final AttachmentPoint point;
        protected final float offX, offY, offZ, scale;
        private final Supplier<EntityModel<?>> modelSupply;
        private EntityModel<?> modelCache;
        protected final ResourceLocation texture;
        private boolean hideAttachedPart;
        private int hideAttachedPartAtMaturity = 0;
        private boolean fadeWithOverlay;
        private AttachmentRenderType renderType = AttachmentRenderType.CUTOUT_NO_CULL;
        private int visibleFromMaturity = 2;
        private int fullScaleMaturity = 4;
        private float initialMaturityScale = 0.38f;
        private float fullMaturityScale = 1.0f;
        private float primalMaturityScale = 1.12f;

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

        /**
         * Hide the humanoid body part this attachment is parented to, allowing
         * the attachment to visually replace that limb/head/body part.
         */
        public SimpleBodyAttachment hideAttachedPart() {
            this.hideAttachedPart = true;
            this.hideAttachedPartAtMaturity = 0;
            return this;
        }

        /**
         * Hide the parent humanoid section only once this maturity level is
         * reached. This lets lower stages show protrusions over the vanilla
         * anatomy, while Primal can become true replacement anatomy.
         */
        public SimpleBodyAttachment hideAttachedPartAt(int maturity) {
            this.hideAttachedPart = true;
            this.hideAttachedPartAtMaturity = Mth.clamp(maturity, 0, 5);
            return this;
        }

        /**
         * Let this attachment share the glow layer's maturity/pulse opacity.
         * Use this only for spectral or intentionally translucent geometry.
         */
        public SimpleBodyAttachment fadeWithOverlay() {
            this.fadeWithOverlay = true;
            return this;
        }

        /**
         * Select the render type for this attachment's texture.
         */
        public SimpleBodyAttachment renderType(AttachmentRenderType renderType) {
            this.renderType = Objects.requireNonNull(renderType, "renderType");
            return this;
        }

        /**
         * First maturity level at which this attachment renders. The default is
         * Developing (2), keeping Unfed/Fledgling as tint-only visual stages.
         */
        public SimpleBodyAttachment visibleFrom(int maturity) {
            this.visibleFromMaturity = Mth.clamp(maturity, 0, 5);
            return this;
        }

        /**
         * Controls how simple attachment geometry scales as the morphling grows.
         *
         * @param visibleFrom first maturity level that renders any geometry
         * @param fullAt      maturity level where the normal authored silhouette is reached
         * @param initial     scale multiplier at the first visible level
         * @param full        scale multiplier at the full silhouette level
         * @param primal      scale multiplier at Primal maturity
         */
        public SimpleBodyAttachment growthScale(int visibleFrom, int fullAt,
                float initial, float full, float primal) {
            this.visibleFromMaturity = Mth.clamp(visibleFrom, 0, 5);
            this.fullScaleMaturity = Mth.clamp(fullAt, this.visibleFromMaturity, 5);
            this.initialMaturityScale = Math.max(0.0f, initial);
            this.fullMaturityScale = Math.max(0.0f, full);
            this.primalMaturityScale = Math.max(0.0f, primal);
            return this;
        }

        @Override
        public Set<AttachmentPoint> hiddenPlayerParts() {
            return hideAttachedPart ? Set.of(point) : Set.of();
        }

        @Override
        public Set<AttachmentPoint> hiddenPlayerParts(int maturity) {
            return hideAttachedPart && maturity >= hideAttachedPartAtMaturity ? Set.of(point) : Set.of();
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
            render(poseStack, buffer, packedLight, entity, parentModel,
                    limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                    netHeadYaw, headPitch, alpha, fullScaleMaturity);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, LivingEntity entity, HumanoidModel<?> parentModel,
                float limbSwing, float limbSwingAmount, float partialTicks,
                float ageInTicks, float netHeadYaw, float headPitch, float alpha,
                int maturity) {
            if (maturity < visibleFromMaturity) {
                return;
            }

            EntityModel<?> attachmentModel = model();
            setupAttachmentAnim(attachmentModel, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            float attachmentAlpha = fadeWithOverlay ? alpha : 1.0f;
            AttachmentRenderType activeRenderType = attachmentAlpha < 0.999f && !renderType.supportsVariableAlpha()
                    ? AttachmentRenderType.TRANSLUCENT
                    : renderType;
            VertexConsumer consumer = buffer.getBuffer(activeRenderType.create(texture));
            float growthScale = scale * growthScaleFor(maturity);
            point.renderOnParts(poseStack, parentModel, () -> {
                poseStack.translate(offX / 16f, offY / 16f, offZ / 16f);
                poseStack.scale(growthScale, growthScale, growthScale);
                attachmentModel.renderToBuffer(poseStack, consumer, packedLight,
                        OverlayTexture.NO_OVERLAY, packColor(1f, 1f, 1f, attachmentAlpha));
            });
        }

        private float growthScaleFor(int maturity) {
            if (maturity >= 5) {
                return primalMaturityScale;
            }
            if (maturity >= fullScaleMaturity) {
                return fullMaturityScale;
            }
            int span = Math.max(1, fullScaleMaturity - visibleFromMaturity);
            float progress = Mth.clamp((maturity - visibleFromMaturity) / (float) span, 0.0f, 1.0f);
            return Mth.lerp(progress, initialMaturityScale, fullMaturityScale);
        }

        @SuppressWarnings("unchecked")
        private static void setupAttachmentAnim(EntityModel<?> model, LivingEntity entity,
                float limbSwing, float limbSwingAmount, float ageInTicks,
                float netHeadYaw, float headPitch) {
            ((EntityModel<LivingEntity>) model).setupAnim(entity, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch);
        }
    }
}
