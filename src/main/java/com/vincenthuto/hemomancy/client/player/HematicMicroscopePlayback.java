package com.vincenthuto.hemomancy.client.player;

public final class HematicMicroscopePlayback {
    private final long startedAt;
    private boolean examining;
    private long releasedAt = -1;
    private float releasePose;

    public HematicMicroscopePlayback(long startedAt, boolean examining) {
        this.startedAt = startedAt;
        this.examining = examining;
    }
    public void complete() { examining = false; }
    public void release(long now) {
        if (releasedAt >= 0) return;
        releasePose = poseTick(now, 0);
        releasedAt = now;
        examining = false;
    }
    public boolean awaitingReleaseMetadata(long now) { return released() && now - releasedAt < 3; }
    public boolean released() { return releasedAt >= 0; }
    public boolean shouldLoop(long now) { return examining && !released() && now - startedAt >= 12; }
    public float poseTick(long now, float partialTick) {
        if (!released()) return Math.max(0, Math.min(16, now - startedAt + partialTick));
        return releasePose * Math.max(0, 1 - (now - releasedAt + partialTick) / 6F);
    }
    public float overlayAlpha(long now, float partialTick) {
        return released() ? 0 : HematicMicroscopePose.overlayAlpha(poseTick(now, partialTick));
    }
    public boolean expired(long now) { return released() && now - releasedAt >= 6; }
}
