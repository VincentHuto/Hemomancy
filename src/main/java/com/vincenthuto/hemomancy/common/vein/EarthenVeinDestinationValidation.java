package com.vincenthuto.hemomancy.common.vein;

final class EarthenVeinDestinationValidation {
	private EarthenVeinDestinationValidation() {
	}

	static boolean shouldPrune(boolean veinPresent, boolean temporary, boolean stented, boolean uuidMatches) {
		return !veinPresent || temporary || !stented || !uuidMatches;
	}
}
