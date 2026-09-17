package com.vincenthuto.hemomancy.common.manipulation.animation;

public enum CastPhase {
    OPENING, HOLD, RELEASE, RECOVERY, CANCEL;

    public boolean sustained() { return this == OPENING || this == HOLD; }
}
