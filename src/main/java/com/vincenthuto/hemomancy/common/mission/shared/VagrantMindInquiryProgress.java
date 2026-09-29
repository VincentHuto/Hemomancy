package com.vincenthuto.hemomancy.common.mission.shared;

public record VagrantMindInquiryProgress(boolean mindVisited, boolean biologyObserved,
        boolean memoryReported, boolean biologyReported) {
    public boolean memoryReady() {
        return mindVisited && !memoryReported;
    }

    public boolean biologyReady() {
        return mindVisited && biologyObserved && !biologyReported;
    }
}
