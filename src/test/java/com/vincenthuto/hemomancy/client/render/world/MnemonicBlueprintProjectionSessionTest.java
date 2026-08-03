package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicBlueprintProjectionSessionTest {
	@Test
	void dimensionChangeClearsAnActiveProjection() {
		Object overworld = new Object();
		Object nether = new Object();
		MnemonicBlueprintProjectionSession<Object> session = new MnemonicBlueprintProjectionSession<>();
		session.activate(overworld);

		assertTrue(session.clearIfWorldChanged(nether));
		assertFalse(session.isActive());
	}

	@Test
	void stayingInTheSameClientWorldRetainsTheProjection() {
		Object overworld = new Object();
		MnemonicBlueprintProjectionSession<Object> session = new MnemonicBlueprintProjectionSession<>();
		session.activate(overworld);

		assertFalse(session.clearIfWorldChanged(overworld));
		assertTrue(session.isActive());
	}

	@Test
	void logoutClearsAnActiveProjection() {
		MnemonicBlueprintProjectionSession<Object> session = new MnemonicBlueprintProjectionSession<>();
		session.activate(new Object());

		assertTrue(session.disconnect());
		assertFalse(session.isActive());
	}

	@Test
	void completingTheStructureEndsTheProjectionSession() {
		MnemonicBlueprintProjectionSession<Object> session = new MnemonicBlueprintProjectionSession<>();
		session.activate(new Object());

		assertTrue(session.clearIfComplete(0));
		assertFalse(session.isActive());
	}

	@Test
	void missingBlocksKeepTheProjectionSessionActive() {
		MnemonicBlueprintProjectionSession<Object> session = new MnemonicBlueprintProjectionSession<>();
		session.activate(new Object());

		assertFalse(session.clearIfComplete(1));
		assertTrue(session.isActive());
	}
}
