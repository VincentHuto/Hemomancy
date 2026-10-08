package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import java.util.*;

public final class PelagicSurfaceRules {
    private PelagicSurfaceRules() {}
    private static SurfaceRules.RuleSource state(Block block) { return SurfaceRules.state(block.defaultBlockState()); }
    private static SurfaceRules.RuleSource patches(Block base, Block exposure, double threshold) {
        return SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, threshold), state(exposure)), state(base));
    }
    public static SurfaceRules.RuleSource makeRules() {
        List<SurfaceRules.RuleSource> rules = new ArrayList<>();
        for (var layer : PelagicLayer.values()) {
            if (layer == PelagicLayer.REEF && !com.vincenthuto.hemomancy.config.HemoCommonConfig.enablePelagicWorldgen()) continue;
            SurfaceRules.RuleSource top = switch (layer) {
                case SHORE -> patches(Blocks.STONE, Blocks.ANDESITE, .15);
                case REEF -> patches(Blocks.SAND, BlockInit.calcified_erythrocoral.get(), .35);
                case OPEN -> patches(Blocks.GRAVEL, Blocks.STONE, .25);
                case TWILIGHT -> patches(Blocks.TUFF, Blocks.CALCITE, .38);
                case MIDNIGHT -> patches(Blocks.DEEPSLATE, Blocks.TUFF, .42);
                case CARRION -> patches(Blocks.CLAY, Blocks.GRAVEL, .15);
                case HYDROTHERMAL -> patches(Blocks.DEEPSLATE, Blocks.SMOOTH_BASALT, .4);
            };
            Block under = switch (layer) {
                case SHORE, REEF, OPEN -> Blocks.STONE;
                case TWILIGHT -> Blocks.TUFF;
                case CARRION -> Blocks.CLAY;
                default -> Blocks.DEEPSLATE;
            };
            rules.add(SurfaceRules.ifTrue(SurfaceRules.isBiome(PelagicBiomes.key(layer)), SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, top), SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, state(under)))));
        }
        return SurfaceRules.sequence(rules.toArray(SurfaceRules.RuleSource[]::new));
    }
}
