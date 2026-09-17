package com.vincenthuto.hemomancy.common.item.component;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographCatalogue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ClairaudiographCatalogueTest {
    private static com.google.gson.JsonObject definition(String sound){return JsonParser.parseString("{\"entity\":\"removed:creature\",\"sounds\":["+sound+"]}").getAsJsonObject();}
    private static final String SOUND="{\"kind\":\"ambient\",\"sound\":\"minecraft:entity.pig.ambient\"}";
    @Test void missingEntityIdsRemainUsableData(){var c=ClairaudiographCatalogue.parse(definition(SOUND));assertEquals("removed:creature",c.getFirst().recording().source());assertEquals(100,c.getFirst().interval());}
    @Test void duplicateChoicesAreRejected(){assertThrows(IllegalArgumentException.class,()->ClairaudiographCatalogue.parse(definition(SOUND+","+SOUND)));}
    @Test void invalidBoundsAndKindsAreRejected(){
        for(String s:new String[]{SOUND.replace("ambient\",", "unknown\","),SOUND.replace("}",",\"pitch\":0.2}"),SOUND.replace("}",",\"replay_interval_ticks\":0}"),SOUND.replace("}",",\"replay_interval_ticks\":12001}")})
            assertThrows(IllegalArgumentException.class,()->ClairaudiographCatalogue.parse(definition(s)));
    }
}
