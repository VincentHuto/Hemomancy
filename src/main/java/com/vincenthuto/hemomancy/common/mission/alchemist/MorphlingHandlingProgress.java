package com.vincenthuto.hemomancy.common.mission.alchemist;

public record MorphlingHandlingProgress(boolean proof, boolean inspected) {
    public boolean ready() {
        return proof && !inspected;
    }
}
