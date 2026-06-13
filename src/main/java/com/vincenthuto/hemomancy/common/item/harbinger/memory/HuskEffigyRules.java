package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.UUID;

public final class HuskEffigyRules {
    private HuskEffigyRules() {
    }

    public enum Profile {
        ZOMBIE("zombie", 20 * 20, 25),
        HUSK("husk", 20 * 25, 30),
        SPIDER("spider", 20 * 18, 40);

        private final String id;
        private final int lifetimeTicks;
        private final int bloodCost;

        Profile(String id, int lifetimeTicks, int bloodCost) {
            this.id = id;
            this.lifetimeTicks = lifetimeTicks;
            this.bloodCost = bloodCost;
        }

        public String getId() {
            return id;
        }

        public int getLifetimeTicks() {
            return lifetimeTicks;
        }

        public int getBloodCost() {
            return bloodCost;
        }

        public static Profile byId(String id) {
            for (Profile profile : values()) {
                if (profile.id.equals(id)) {
                    return profile;
                }
            }
            return ZOMBIE;
        }
    }

    public static boolean isOwnerTarget(UUID ownerUuid, UUID targetUuid) {
        return ownerUuid != null && targetUuid != null && ownerUuid.equals(targetUuid);
    }

    public static boolean shouldRejectTarget(UUID ownerUuid, UUID targetUuid) {
        return isOwnerTarget(ownerUuid, targetUuid);
    }
}
