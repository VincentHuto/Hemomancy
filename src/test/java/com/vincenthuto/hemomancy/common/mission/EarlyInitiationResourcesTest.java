package com.vincenthuto.hemomancy.common.mission;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import com.google.gson.*;
import static org.junit.jupiter.api.Assertions.*;
class EarlyInitiationResourcesTest {
 @Test void earlyRanksHaveNoRiteRecipe() {
  for (String name : new String[]{"sanguine_initiation", "votary_rite"})
   assertFalse(Files.exists(Path.of("src/main/resources/data/hemomancy/recipe/cardinal_rite/" + name + ".json")));
 }
 @Test void alchemistExplainsSettlingAndRest() throws Exception {
  var lang=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
  assertTrue(lang.has("hemomancy.initiation.blood_rest"));
  String line=lang.get("hemomancy.initiation.blood_rest").getAsString().toLowerCase();
  assertTrue(line.contains("settle") && line.contains("rest"));
 }
}
