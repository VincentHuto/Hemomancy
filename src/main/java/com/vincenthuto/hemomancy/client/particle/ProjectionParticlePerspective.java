package com.vincenthuto.hemomancy.client.particle;

public final class ProjectionParticlePerspective {
	private ProjectionParticlePerspective() {
	}

	public static boolean allowsThirdPersonEmission(boolean localPlayer, boolean firstPersonCamera) {
		return !localPlayer || !firstPersonCamera;
	}
}
