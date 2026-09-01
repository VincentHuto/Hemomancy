package com.vincenthuto.hemomancy.client.render.layer.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AvatarManifestationTransition {
	static final int DURATION_TICKS = 20;
	private static final Map<UUID, AvatarManifestationTransition> TRACKED = new HashMap<>();

	private String observedForm = "";
	private String renderedForm = "";
	private Phase phase = Phase.NONE;
	private long startedAt;

	public static Sample sample(UUID playerId, String activeForm, long gameTick, float partialTick) {
		AvatarManifestationTransition transition = TRACKED.computeIfAbsent(playerId,
				ignored -> new AvatarManifestationTransition());
		Sample sample = transition.update(activeForm, gameTick, partialTick);
		if (!sample.renders()) TRACKED.remove(playerId);
		return sample;
	}

	Sample update(String activeForm, long gameTick, float partialTick) {
		String form = activeForm == null ? "" : activeForm;
		if (!form.equals(observedForm)) {
			observedForm = form;
			startedAt = gameTick;
			if (form.isEmpty()) {
				phase = renderedForm.isEmpty() ? Phase.NONE : Phase.DISMISSING;
			} else {
				renderedForm = form;
				phase = Phase.SUMMONING;
			}
		}

		float progress = Math.clamp((gameTick + partialTick - startedAt) / DURATION_TICKS, 0.0F, 1.0F);
		if (progress >= 1.0F) {
			if (phase == Phase.SUMMONING) phase = Phase.ACTIVE;
			else if (phase == Phase.DISMISSING) {
				phase = Phase.NONE;
				renderedForm = "";
			}
		}
		return new Sample(renderedForm, phase, progress);
	}

	public enum Phase {
		NONE,
		SUMMONING,
		ACTIVE,
		DISMISSING
	}

	public record Sample(String form, Phase phase, float progress) {
		public boolean renders() {
			return phase != Phase.NONE && !form.isEmpty();
		}

		public boolean warping() {
			return phase == Phase.SUMMONING || phase == Phase.DISMISSING;
		}

		public float emergenceScale(float avatarScale) {
			if (phase != Phase.SUMMONING) return 1.0F;
			float skinScale = avatarScale > 1.0F ? 0.65F : 0.75F;
			float burst = Math.clamp((progress - 0.7F) / 0.25F, 0.0F, 1.0F);
			float remaining = burst - 1.0F;
			float overshoot = 1.0F + 2.70158F * remaining * remaining * remaining
					+ 1.70158F * remaining * remaining;
			return skinScale + (1.0F - skinScale) * overshoot;
		}

		public float swimOffset() {
			return phase == Phase.SUMMONING
					? (float) Math.sin(progress * Math.PI * 4.0) * (1.0F - summonBurst()) * 0.045F
					: 0.0F;
		}

		public float meltProgress() {
			return phase == Phase.DISMISSING ? progress : 0.0F;
		}

		public float playerVisualScale(float serverScale, float avatarScale) {
			return phase == Phase.DISMISSING ? serverScale : serverScale / avatarScale;
		}

		public float presence() {
			return switch (phase) {
				case SUMMONING -> summonBurst();
				case DISMISSING -> 1.0F - smooth(progress);
				case ACTIVE -> 1.0F;
				case NONE -> 0.0F;
			};
		}

		private static float smooth(float value) {
			return value * value * (3.0F - 2.0F * value);
		}

		private float summonBurst() {
			float burst = Math.clamp((progress - 0.7F) / 0.25F, 0.0F, 1.0F);
			return 1.0F - (float) Math.pow(1.0F - burst, 3.0F);
		}
	}
}
