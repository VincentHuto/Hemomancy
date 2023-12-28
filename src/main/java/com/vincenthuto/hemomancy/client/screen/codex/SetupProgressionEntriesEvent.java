package com.vincenthuto.hemomancy.client.screen.codex;

import net.minecraftforge.eventbus.api.Event;

public class SetupProgressionEntriesEvent extends Event {
	public SetupProgressionEntriesEvent() {
	}

	@Override
	public boolean isCancelable() {
		return false;
	}
}
