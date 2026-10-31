package com.vincenthuto.hemomancy.client.model.morphling;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
 *       to one of the player's body parts. Attachments may also hide player
 *       parts when they are meant to replace a limb/head/body part. Simple
 *       attachments can gate and scale geometry by morphling maturity. Attach
 *       with {@link Builder#attach}.</li>
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
     * 3-D model attachments rendered on top of the color overlay, one per anchor (a strain can grow on several
     * body parts at once). Empty means tint only.
     */
    public final List<MorphlingModelAttachment> attachments;

    private MorphlingVisualMutation(float r, float g, float b, float alpha,
            float pulseSpeed, boolean emissive,
            @Nullable ResourceLocation swirlTexture, float swirlSpeed,
            List<MorphlingModelAttachment> attachments) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
        this.pulseSpeed = pulseSpeed;
        this.emissive = emissive;
        this.swirlTexture = swirlTexture;
        this.swirlSpeed = swirlSpeed;
        this.attachments = List.copyOf(attachments);
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
        private final List<MorphlingModelAttachment> attachments = new ArrayList<>();

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
         * Adds a custom 3-D model to the player while this morphling is equipped.
         * The attachment receives the same scaled alpha as the color overlay for
         * custom renderers that need it, plus the current morphling maturity in
         * the maturity-aware render overload. Simple model attachments render
         * opaque by default unless {@link MorphlingModelAttachment.SimpleBodyAttachment#fadeWithOverlay()}
         * is enabled, and default to tint-only early stages before Developing.
         *
         * <p>Use {@link MorphlingModelAttachment#of} for simple single-model
         * attachments, or extend {@link MorphlingModelAttachment} for full control.
         */
        public Builder attach(MorphlingModelAttachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public MorphlingVisualMutation build() {
            return new MorphlingVisualMutation(r, g, b, alpha, pulseSpeed, emissive,
                    swirlTexture, swirlSpeed, attachments);
        }
    }
}
