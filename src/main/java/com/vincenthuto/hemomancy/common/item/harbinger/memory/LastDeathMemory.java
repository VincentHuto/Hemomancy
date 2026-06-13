package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.HashMap;
import java.util.Map;

public class LastDeathMemory {
    public static final String KEY_PRESENT = "present";
    public static final String KEY_DIMENSION = "dimension";
    public static final String KEY_POS = "pos";
    public static final String KEY_TIME = "time";

    private boolean present;
    private String dimensionId = "";
    private long packedPosition;
    private long gameTime;

    public void remember(String dimensionId, long packedPosition, long gameTime) {
        this.present = true;
        this.dimensionId = dimensionId == null ? "" : dimensionId;
        this.packedPosition = packedPosition;
        this.gameTime = gameTime;
    }

    public void clear() {
        this.present = false;
        this.dimensionId = "";
        this.packedPosition = 0L;
        this.gameTime = 0L;
    }

    public boolean hasMemory() {
        return present && !dimensionId.isBlank();
    }

    public String dimensionId() {
        return dimensionId;
    }

    public long packedPosition() {
        return packedPosition;
    }

    public long gameTime() {
        return gameTime;
    }

    public boolean isSameDimension(String dimensionId) {
        return hasMemory() && this.dimensionId.equals(dimensionId);
    }

    public Map<String, String> serialize() {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_PRESENT, Boolean.toString(present));
        data.put(KEY_DIMENSION, dimensionId);
        data.put(KEY_POS, Long.toString(packedPosition));
        data.put(KEY_TIME, Long.toString(gameTime));
        return data;
    }

    public void deserialize(Map<String, String> data) {
        present = Boolean.parseBoolean(data.getOrDefault(KEY_PRESENT, "false"));
        dimensionId = data.getOrDefault(KEY_DIMENSION, "");
        packedPosition = parseLong(data.get(KEY_POS));
        gameTime = parseLong(data.get(KEY_TIME));
    }

    public static long packPosition(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    public static int unpackX(long packed) {
        int value = (int) (packed >> 38);
        return value >= 0x2000000 ? value - 0x4000000 : value;
    }

    public static int unpackY(long packed) {
        int value = (int) (packed & 0xFFF);
        return value >= 0x800 ? value - 0x1000 : value;
    }

    public static int unpackZ(long packed) {
        int value = (int) ((packed >> 12) & 0x3FFFFFF);
        return value >= 0x2000000 ? value - 0x4000000 : value;
    }

    private static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
