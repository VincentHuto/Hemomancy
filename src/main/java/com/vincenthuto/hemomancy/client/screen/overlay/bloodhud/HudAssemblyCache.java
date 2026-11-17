package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout;

import java.util.LinkedHashMap;
import java.util.Map;

/** The few most recently shown HUD states' assemblies; each holds about half a megabyte of grown binding. */
public final class HudAssemblyCache {
    private final Map<Integer, HudAssembly> assemblies;

    public HudAssemblyCache(int capacity) {
        this.assemblies = new LinkedHashMap<>(capacity + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, HudAssembly> eldest) {
                return size() > capacity;
            }
        };
    }

    public HudAssembly get(BloodHudLayout layout, HudMasks masks) {
        return assemblies.computeIfAbsent(layout.code(), code -> HudAssembly.build(layout, masks));
    }

    public int size() {
        return assemblies.size();
    }

    public void clear() {
        assemblies.clear();
    }
}
