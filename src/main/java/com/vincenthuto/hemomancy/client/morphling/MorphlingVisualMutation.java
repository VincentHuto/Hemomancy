package com.vincenthuto.hemomancy.client.morphling;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Describes how an equipped morphling visually mutates the player's appearance.
 *
 * <p>Two independent layers are supported and can be combined freely:
 * <ol>
 *   <li><b>Color overlay</b> – the player's animated humanoid model is re-drawn
 *       with a translucent tint (plain, emissive, or animated energy-swirl).
 *       Intensity scales with maturity.</li>
 *   <li><b>Model attachment</b> – an optional {@link MorphlingModelAttachment}
 *       renders additional 3-D geometry (wings, tendrils, plating, etc.) parented
 *       to one of the player's body parts.  Attach with {@link Builder#attach}.</li>
 * </ol>
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingVisualMutation {

    /** Tint color components (0.0–1.0 each). */
    public final float r, g, b;

    /**
     * Base overlay opacity at Apex maturity (0.0–1.0).
     * Lower maturities render proportionally dimmer.
     */
    public final float alpha;

    /**
     * Pulse animation speed in radians per tick.
     * 0 means no pulsing — the alpha stays constant.
     */
    public final float pulseSpeed;

    /**
     * When true the overlay renders at full brightness regardless of scene
     * lighting, making the effect glow in the dark.
     */
    public final boolean emissive;

    /**
     * When non-null the overlay uses the energySwirl render type with this
     * scrolling texture instead of the plain player skin silhouette.
     */
    @Nullable
    public final ResourceLocation swirlTexture;

    /** UV scroll speed for the swirl texture (units per tick). */
    public final float swirlSpeed;

    /**
     * Optional 3-D model attachment rendered on top of the color overlay.
     * Null means no extra geometry beyond the color tint.
     */
    @Nullable
    public final MorphlingModelAttachment modelAttachment;

    private MorphlingVisualMutation(float r, float g, float b, float alpha,
            float pulseSpeed, boolean emissive,
            @Nullable ResourceLocation swirlTexture, float swirlSpeed,
            @Nullable MorphlingModelAttachment modelAttachment) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
        this.pulseSpeed = pulseSpeed;
        this.emissive = emissive;
        this.swirlTexture = swirlTexture;
        this.swirlSpeed = swirlSpeed;
        this.modelAttachment = modelAttachment;
    }

    public static Builder builder(float r, float g, float b, float alpha) {
        return new Builder(r, g, b, alpha);
    }

    public static class Builder {
        private final float r, g, b, alpha;
        private float pulseSpeed = 0f;
        private boolean emissive = false;
        private ResourceLocation swirlTexture = null;
        private float swirlSpeed = 0.01f;
        private MorphlingModelAttachment modelAttachment = null;

        Builder(float r, float g, float b, float alpha) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = alpha;
        }

        /** Animate the overlay alpha with a sine wave at the given speed (rad/tick). */
        public Builder pulse(float speed) {
            this.pulseSpeed = speed;
            return this;
        }

        /** Render the overlay at full brightness so the effect glows in the dark. */
        public Builder emissive() {
            this.emissive = true;
            return this;
        }

        /** Use an animated scrolling texture overlay instead of the skin silhouette. */
        public Builder swirl(ResourceLocation texture, float speed) {
            this.swirlTexture = texture;
            this.swirlSpeed = speed;
            return this;
        }

        /**
         * Attach a custom 3-D model to the player while this morphling is equipped.
         * The attachment receives the same scaled alpha as the color overlay so
         * model opacity also grows with maturity.
         *
         * <p>Use {@link MorphlingModelAttachment#of} for simple single-model
         * attachments, or extend {@link MorphlingModelAttachment} for full control.
         */
        public Builder attach(MorphlingModelAttachment attachment) {
            this.modelAttachment = attachment;
            return this;
        }

        public MorphlingVisualMutation build() {
            return new MorphlingVisualMutation(r, g, b, alpha, pulseSpeed, emissive,
                    swirlTexture, swirlSpeed, modelAttachment);
        }
    }
}
