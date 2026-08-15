package com.vincenthuto.hemomancy.client.screen.skilltree.util;

public final class TraceLayerInvalidation {
	private boolean dirty = true;
	private long observedRevision = Long.MIN_VALUE;

	public void markDirty() {
		dirty = true;
	}

	public boolean consume(long revision) {
		if (!dirty && observedRevision == revision) return false;
		dirty = false;
		observedRevision = revision;
		return true;
	}
}
