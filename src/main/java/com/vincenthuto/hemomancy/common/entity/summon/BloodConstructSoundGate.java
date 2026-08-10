package com.vincenthuto.hemomancy.common.entity.summon;

final class BloodConstructSoundGate {
	private boolean expirationClaimed;
	private boolean dissolutionClaimed;

	CueRequest claimExpiration(float configuredVolume) {
		if (expirationClaimed) {
			return CueRequest.SILENT;
		}
		expirationClaimed = true;
		return new CueRequest(true, configuredVolume);
	}

	CueRequest claimDissolution(float configuredVolume) {
		if (dissolutionClaimed) {
			return CueRequest.SILENT;
		}
		dissolutionClaimed = true;
		return new CueRequest(true, configuredVolume);
	}

	record CueRequest(boolean shouldPlay, float volume) {
		private static final CueRequest SILENT = new CueRequest(false, 0.0F);
	}
}
