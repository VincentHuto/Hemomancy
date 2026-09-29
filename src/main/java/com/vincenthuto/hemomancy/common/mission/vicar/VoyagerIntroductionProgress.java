package com.vincenthuto.hemomancy.common.mission.vicar;

public record VoyagerIntroductionProgress(boolean observed, boolean reported) {
    public boolean ready() {
        return observed && !reported;
    }
}
