package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.PhlegethonticNetherRegion;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PhlegethonticRegistrationTest {
    @Test void pointedScabAssetsCoverEveryFloorAndRoofShape() throws Exception {
        Path assets=Path.of("src/main/resources/assets/hemomancy");
        var blockstate=JsonParser.parseString(Files.readString(
                assets.resolve("blockstates/pointed_blood_scorched_scab.json"))).getAsJsonObject();
        var variants=blockstate.getAsJsonObject("variants");
        assertEquals(10,variants.size());
        for(String direction:List.of("up","down"))for(String thickness:List.of("base","frustum","middle","tip","tip_merge")) {
            String shape=direction+"_"+thickness;
            String key="thickness="+thickness+",vertical_direction="+direction;
            assertEquals("hemomancy:block/pointed_blood_scorched_scab_"+shape,
                    variants.getAsJsonObject(key).get("model").getAsString());
            var image=ImageIO.read(assets.resolve("textures/block/pointed_blood_scorched_scab_"+shape+".png").toFile());
            assertEquals(16,image.getWidth());assertEquals(16,image.getHeight());
            int colored=0,darkCrimson=0;
            for(int x=0;x<16;x++)for(int y=0;y<16;y++) {
                int argb=image.getRGB(x,y),alpha=argb>>>24;
                if(alpha==0)continue;
                colored++;
                int red=argb>>16&255,green=argb>>8&255,blue=argb&255;
                if(red>green*1.25 && red>blue*1.15 && red<150)darkCrimson++;
            }
            assertTrue(colored>0 && darkCrimson>=colored/5,
                    shape+" must visibly carry the dark Blood-Scorched Scab palette");
        }
    }

    @Test void pointedScabModelsWidenFromTipToRoot() throws Exception {
        Path models=Path.of("src/main/resources/assets/hemomancy/models/block");
        for(String direction:List.of("up","down")) {
            ModelWidths base=modelWidths(models,direction,"base");
            ModelWidths middle=modelWidths(models,direction,"middle");
            ModelWidths frustum=modelWidths(models,direction,"frustum");
            ModelWidths tip=modelWidths(models,direction,"tip");

            assertEquals(12.0,base.widest());
            assertEquals(10.0,middle.widest());
            assertEquals(8.0,frustum.widest());
            assertEquals(6.0,tip.widest());
            assertTrue(base.widest()>middle.widest() && middle.widest()>frustum.widest()
                            && frustum.widest()>tip.widest(),
                    direction+" formation must widen continuously from its point to its root");
            assertTrue(tip.narrowest()<=2.0,direction+" tip must end in a visibly sharp point");
        }
    }

    private static ModelWidths modelWidths(Path models,String direction,String thickness) throws Exception {
        Path path=models.resolve("pointed_blood_scorched_scab_"+direction+"_"+thickness+".json");
        var model=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        var elements=model.getAsJsonArray("elements");
        assertNotNull(elements,path.getFileName()+" needs authored tier geometry instead of a full crossed sheet");
        double widest=0,narrowest=Double.MAX_VALUE;
        for(var entry:elements) {
            var element=entry.getAsJsonObject();
            var from=element.getAsJsonArray("from");
            var to=element.getAsJsonArray("to");
            double width=Math.max(to.get(0).getAsDouble()-from.get(0).getAsDouble(),
                    to.get(2).getAsDouble()-from.get(2).getAsDouble());
            widest=Math.max(widest,width);
            narrowest=Math.min(narrowest,width);
        }
        return new ModelWidths(widest,narrowest);
    }

    private record ModelWidths(double widest,double narrowest) {}

    @Test void untouchedBasinFloorContinuesTheOuterVenousBand() throws Exception {
        String source=Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/worldgen/terrablender/PhlegethonticSurfaceRuleData.java"));
        String floorRule=source.substring(source.indexOf("SurfaceRules.ON_FLOOR"),source.indexOf("SurfaceRules.UNDER_FLOOR"));
        assertTrue(floorRule.contains("BlockInit.venous_stone"),"The procedural outer band must meet a venous-stone biome floor");
        assertFalse(floorRule.contains("BlockInit.blood_scorched_scab"),"Scab belongs at the ichor edge, not across the untouched biome floor");
    }

    @Test void regionEmitsAllFiveNetherClimatePoints() {
        var mappings=new ArrayList<Pair<Climate.ParameterPoint,ResourceKey<Biome>>>();
        new PhlegethonticNetherRegion(ResourceLocation.parse("hemomancy:phlegethontic_nether"),1).addBiomes(null,mappings::add);
        assertEquals(5,mappings.size());
        float[][] expected={{0,0,0},{0,-.5F,0},{.4F,0,0},{0,.5F,.375F},{-.5F,0,.175F}};
        for(int i=0;i<5;i++) {
            assertEquals(Climate.parameters(expected[i][0],expected[i][1],0,0,0,0,expected[i][2]),mappings.get(i).getFirst());
            assertEquals(i==4?"hemomancy:phlegethontic_basin":"terrablender:deferred_placeholder",mappings.get(i).getSecond().location().toString());
        }
    }
    @Test void configurationCodecAndSingleVeinInjectionMatchTheResources() throws Exception {
        Path root=Path.of("src/main/resources/data/hemomancy");
        for(String name:List.of("phlegethontic_vein","phlegethontic_basin_terrain")) {
            var configured=JsonParser.parseString(Files.readString(root.resolve("worldgen/configured_feature/"+name+".json"))).getAsJsonObject();
            assertTrue(NoneFeatureConfiguration.CODEC.parse(JsonOps.INSTANCE,configured.get("config")).result().isPresent());
            var placed=JsonParser.parseString(Files.readString(root.resolve("worldgen/placed_feature/"+name+".json"))).getAsJsonObject();
            assertEquals(0,placed.getAsJsonArray("placement").size(),"Rarity must be inside reconstruction");
        }
        try(var files=Files.list(root.resolve("neoforge/biome_modifier"))) {
            assertEquals(1,files.filter(p -> {try{return Files.readString(p).contains("hemomancy:phlegethontic_vein");}catch(Exception e){throw new RuntimeException(e);}}).count());
        }
        var modifier=JsonParser.parseString(Files.readString(root.resolve("neoforge/biome_modifier/add_phlegethontic_veins.json"))).getAsJsonObject();
        assertEquals("#c:is_nether",modifier.get("biomes").getAsString());
        assertEquals("underground_decoration",modifier.get("step").getAsString());
        assertFalse(Files.readString(root.resolve("worldgen/biome/phlegethontic_basin.json")).contains("phlegethontic_vein"));
    }
}
