package com.vincenthuto.hemomancy.client.morphling;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Describes how an equipped morphling visually mutates the player's appearance.
 * The mutation is rendered as a translucent colored overlay on the player model,
 * scaled in intensity by the morphling's current maturity level.
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

    private MorphlingVisualMutation(float r, float g, float b, float alpha,
            float pulseSpeed, boolean emissive,
            @Nullable ResourceLocation swirlTexture, float swirlSpeed) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
        this.pulseSpeed = pulseSpeed;
        this.emissive = emissive;
        this.swirlTexture = swirlTexture;
        this.swirlSpeed = swirlSpeed;
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

        /** Render at full brightness so the effect glows in the dark. */
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

        public MorphlingVisualMutation build() {
            return new MorphlingVisualMutation(r, g, b, alpha, pulseSpeed, emissive, swirlTexture, swirlSpeed);
        }
    }
}
