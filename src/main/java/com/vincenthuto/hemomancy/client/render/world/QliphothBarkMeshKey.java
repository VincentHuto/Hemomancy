package com.vincenthuto.hemomancy.client.render.world;

/** Stable inputs selecting one immutable Qliphoth large-wood mesh. */
record QliphothBarkMeshKey(int stage, boolean severed) {
    static QliphothBarkMeshKey create(int stage, boolean severed) {
        return new QliphothBarkMeshKey(Math.max(0, Math.min(9, stage)), severed);
    }
}
