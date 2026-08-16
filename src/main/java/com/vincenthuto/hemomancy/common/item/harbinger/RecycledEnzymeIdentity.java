package com.vincenthuto.hemomancy.common.item.harbinger;

public record RecycledEnzymeIdentity(int tendencyIndex, float potency) {
	public static RecycledEnzymeIdentity fromSeed(long seed) {
		long mixed = seed ^ (seed >>> 33) ^ 0x9E3779B97F4A7C15L;
		int tendency = Math.floorMod((int) mixed, 8);
		float amount = 3F + Math.floorMod((int) (mixed >>> 32), 9) * 0.5F;
		return new RecycledEnzymeIdentity(tendency, amount);
	}
}
