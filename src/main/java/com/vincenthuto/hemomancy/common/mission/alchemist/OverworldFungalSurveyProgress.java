package com.vincenthuto.hemomancy.common.mission.alchemist;

public record OverworldFungalSurveyProgress(boolean visited, int specimens, boolean reported) {
    public boolean ready() {
        return visited && specimens >= 2 && !reported;
    }
}
