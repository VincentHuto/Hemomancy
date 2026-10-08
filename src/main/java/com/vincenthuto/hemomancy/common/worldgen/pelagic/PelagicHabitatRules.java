package com.vincenthuto.hemomancy.common.worldgen.pelagic;

/** Registry-independent ecological constraints; world reads live in PelagicHabitat. */
public final class PelagicHabitatRules {
    private PelagicHabitatRules() {}

    public enum Species {
        CHITON(PelagicLayer.SHORE, 50, 73), HEMOLYMPHOPODA(PelagicLayer.SHORE, 52, 68),
        URCHIN(PelagicLayer.REEF, 34, 61), HERRING(PelagicLayer.OPEN, 30, 61),
        PYROSOME(PelagicLayer.OPEN, 28, 59), WHALE(PelagicLayer.OPEN, 24, 58),
        CUTTLE(PelagicLayer.TWILIGHT, 7, 38), SIPHONOPHORE(PelagicLayer.TWILIGHT, 7, 38),
        LANTERN(PelagicLayer.MIDNIGHT, -12, 18), COMB_JELLY(PelagicLayer.MIDNIGHT, -15, 18),
        HAGFISH(PelagicLayer.CARRION, -33, -6), SNAIL(PelagicLayer.HYDROTHERMAL, -53, -27),
        VAMPIRE_SQUID(PelagicLayer.CARRION, -53, -6);

        public final PelagicLayer home;
        public final int minY, maxY;
        Species(PelagicLayer home, int minY, int maxY) {
            this.home = home; this.minY = minY; this.maxY = maxY;
        }
    }

    public record Site(PelagicLayer layer, int y, boolean loaded, boolean water, boolean clear,
                       int floorDistance, boolean wetRock, boolean fossil, boolean vent) {}

    public static boolean inRange(Species species, PelagicLayer layer, int y) {
        if (layer == null) return false;
        // Established reef visitors retain a small population in the reef's actual water column.
        if (layer == PelagicLayer.REEF && (species == Species.CUTTLE || species == Species.LANTERN))
            return y >= 38 && y <= 56;
        if (y < species.minY || y > species.maxY) return false;
        return layer == species.home || switch (species) {
            case HERRING, PYROSOME, WHALE -> layer == PelagicLayer.REEF || layer == PelagicLayer.TWILIGHT;
            case CUTTLE, SIPHONOPHORE -> layer == PelagicLayer.OPEN || layer == PelagicLayer.MIDNIGHT;
            case LANTERN, COMB_JELLY -> layer == PelagicLayer.TWILIGHT || layer == PelagicLayer.CARRION;
            case HAGFISH -> layer == PelagicLayer.HYDROTHERMAL || layer == PelagicLayer.MIDNIGHT;
            case SNAIL -> layer == PelagicLayer.CARRION;
            case VAMPIRE_SQUID -> layer == PelagicLayer.HYDROTHERMAL;
            default -> false;
        };
    }

    public static boolean suitable(Species species, Site site) {
        if (!site.loaded || !site.clear || !inRange(species, site.layer, site.y)) return false;
        if (species == Species.CHITON) return site.wetRock && site.floorDistance == 1;
        if (species == Species.HEMOLYMPHOPODA)
            return (site.water || site.wetRock) && site.floorDistance <= 2;
        if (!site.water) return false;
        return switch (species) {
            case URCHIN -> site.floorDistance <= 2;
            case HAGFISH -> site.floorDistance <= 3 && site.fossil;
            case SNAIL -> site.floorDistance <= 2 && site.vent;
            default -> true;
        };
    }

    public static double depthCorrection(Species species, double y) {
        int low = switch (species.home) {
            case SHORE -> 57; case REEF -> 39; case OPEN -> 37; case TWILIGHT -> 16;
            case MIDNIGHT -> -5; case CARRION -> -27; case HYDROTHERMAL -> -50;
        };
        int high = low + (species.home == PelagicLayer.SHORE ? 5 : 13);
        // This swimmer occupies both deep layers, rather than returning to a single home band.
        if (species == Species.VAMPIRE_SQUID) { low = -48; high = -10; }
        if (y > high) return Math.max(-.018, (high - y) * .003);
        if (y < low) return Math.min(.012, (low - y) * .002);
        return 0;
    }
}
