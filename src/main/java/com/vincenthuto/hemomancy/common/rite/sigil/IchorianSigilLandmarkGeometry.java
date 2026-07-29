package com.vincenthuto.hemomancy.common.rite.sigil;

import java.util.List;

/**
 * Renderer-independent recipes for the small anatomical landmarks shared by
 * dormant Cardinal Rite sigils and their awakened brood.
 */
public final class IchorianSigilLandmarkGeometry {
	private IchorianSigilLandmarkGeometry() {
	}

	public static Recipe forRole(IchorianSigilAnatomy.Role role, long seed) {
		float irregular = signed(seed) * 0.08F;
		return switch (role) {
			case EYE -> new Recipe("EYE",
					List.of(
							p(Layer.TISSUE, 0, 0, 0, 1.00F, 0.78F, 0.62F),
							p(Layer.IRIS, 0, 0, -0.48F, 0.56F, 0.50F, 0.20F),
							p(Layer.PUPIL, 0, 0, -0.64F, 0.22F, 0.25F, 0.10F),
							p(Layer.HIGHLIGHT, -0.13F, 0.14F, -0.71F, 0.08F, 0.08F, 0.05F)),
					List.of(p(Layer.IRIS, 0, 0, -0.32F, 0.68F, 0.61F, 0.24F)));
			case ORGAN -> recipe("ORGAN",
					p(Layer.TISSUE, 0, 0, 0, 0.90F, 1.18F, 0.82F),
					p(Layer.TISSUE, -0.55F, 0.10F, irregular, 0.56F, 0.72F, 0.54F),
					p(Layer.TISSUE, 0.48F, -0.16F, -irregular, 0.50F, 0.65F, 0.48F),
					p(Layer.ICHOR, 0, -0.04F, -0.34F, 0.48F, 0.58F, 0.28F));
			case JOINT -> recipe("JOINT",
					p(Layer.TISSUE, 0, 0, 0, 0.78F, 0.75F, 0.76F),
					p(Layer.TISSUE, -0.62F, 0, 0, 0.42F, 0.48F, 0.46F),
					p(Layer.TISSUE, 0.62F, 0, 0, 0.42F, 0.48F, 0.46F),
					p(Layer.ICHOR, 0, 0, -0.42F, 0.31F, 0.31F, 0.18F));
			case VALVE -> recipe("VALVE",
					p(Layer.TISSUE, -0.34F, 0, 0, 0.66F, 0.92F, 0.65F),
					p(Layer.TISSUE, 0.34F, 0, 0, 0.66F, 0.92F, 0.65F),
					p(Layer.ICHOR, 0, 0, -0.38F, 0.24F, 0.48F, 0.18F));
			case LIMB_TIP -> recipe("LIMB_TIP",
					p(Layer.TISSUE, 0, 0.18F, 0, 0.62F, 1.20F, 0.58F),
					p(Layer.TISSUE, 0, -0.62F, 0, 0.28F, 0.56F, 0.28F),
					p(Layer.ICHOR, 0, 0.12F, -0.31F, 0.24F, 0.47F, 0.16F));
			case HOOK -> recipe("HOOK",
					p(Layer.TISSUE, 0, 0.14F, 0, 0.48F, 1.10F, 0.46F),
					p(Layer.TISSUE, 0.34F, -0.66F, 0, 0.52F, 0.34F, 0.35F),
					p(Layer.ICHOR, 0.06F, 0.08F, -0.28F, 0.19F, 0.42F, 0.14F));
			case RIB -> recipe("RIB",
					p(Layer.TISSUE, 0, 0, 0, 1.30F, 0.38F, 0.58F),
					p(Layer.TISSUE, irregular, 0, 0.22F, 0.72F, 0.48F, 0.46F),
					p(Layer.ICHOR, 0, 0, -0.36F, 0.68F, 0.15F, 0.17F));
			case GANGLION -> recipe("GANGLION",
					p(Layer.TISSUE, 0, 0, 0, 0.78F, 0.82F, 0.72F),
					p(Layer.TISSUE, -0.62F, 0.22F, 0, 0.30F, 0.34F, 0.30F),
					p(Layer.TISSUE, 0.58F, -0.20F, 0, 0.28F, 0.32F, 0.28F),
					p(Layer.ICHOR, 0, 0, -0.40F, 0.29F, 0.31F, 0.16F));
			case MEMBRANE_TIP -> recipe("MEMBRANE_TIP",
					p(Layer.TISSUE, 0, 0, 0, 1.18F, 0.34F, 0.66F),
					p(Layer.MEMBRANE, 0, 0, -0.32F, 0.78F, 0.20F, 0.24F),
					p(Layer.ICHOR, 0, 0, -0.48F, 0.31F, 0.09F, 0.12F));
		};
	}

	public static Recipe boundaryAnchor(long seed) {
		float irregular = signed(seed) * 0.12F;
		return new Recipe("BOUNDARY_ANCHOR:" + Long.toUnsignedString(seed, 16),
				List.of(
						p(Layer.TISSUE, 0, 0, 0, 0.96F, 0.82F, 0.92F),
						p(Layer.TISSUE, -0.62F, 0.18F + irregular, 0.08F, 0.38F, 0.42F, 0.36F),
						p(Layer.TISSUE, 0.52F, -0.20F, -irregular, 0.30F, 0.34F, 0.31F),
						p(Layer.ICHOR, 0, 0, -0.45F, 0.28F, 0.25F, 0.17F)),
				List.of(p(Layer.TISSUE, 0, 0, 0, 1.12F, 0.98F, 1.08F)));
	}

	private static Recipe recipe(String name, Primitive... core) {
		return new Recipe(name, List.of(core),
				List.of(p(Layer.ICHOR, 0, 0, 0, 0.38F, 0.38F, 0.38F)));
	}

	private static Primitive p(Layer layer, float x, float y, float z,
			float scaleX, float scaleY, float scaleZ) {
		return new Primitive(layer, x, y, z, scaleX, scaleY, scaleZ);
	}

	private static float signed(long seed) {
		long mixed = seed ^ (seed >>> 32);
		return ((mixed & 0xFFFFL) / 32767.5F) - 1.0F;
	}

	public enum Layer {
		TISSUE, ICHOR, MEMBRANE, IRIS, PUPIL, HIGHLIGHT
	}

	public record Primitive(Layer layer, float x, float y, float z,
			float scaleX, float scaleY, float scaleZ) {
	}

	public record Recipe(String signature, List<Primitive> core, List<Primitive> glow) {
		public Recipe {
			core = List.copyOf(core);
			glow = List.copyOf(glow);
		}
	}
}
