package com.vincenthuto.hemomancy.client.screen.codex;

import net.minecraftforge.eventbus.api.Event;

public class SetupMalumCodexEntriesEvent extends Event {
	public SetupMalumCodexEntriesEvent() {
	}

	@Override
	public boolean isCancelable() {
		return false;
	}
}
