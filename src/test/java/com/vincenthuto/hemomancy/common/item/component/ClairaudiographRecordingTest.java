package com.vincenthuto.hemomancy.common.item.component;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ClairaudiographRecordingTest {
 @Test void roundTripPreservesEventAndStableKind() {
  var json=JsonParser.parseString("{\"source\":\"minecraft:pig\",\"sound\":\"minecraft:entity.pig.ambient\",\"kind\":\"ambient\",\"pitch\":1.25}");
  var r=ClairaudiographRecording.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
  assertTrue(r.readable());
  assertEquals(r,ClairaudiographRecording.CODEC.parse(JsonOps.INSTANCE,ClairaudiographRecording.CODEC.encodeStart(JsonOps.INSTANCE,r).getOrThrow()).getOrThrow());
 }
 @Test void unknownKindIsRetainedButInert() {
  var r=new ClairaudiographRecording("missing:beast","minecraft:entity.pig.ambient","future_kind",1);
  assertFalse(r.readable()); assertEquals("future_kind",r.kind());
 }
 @Test void invalidPitchAndIdsAreInert() {
  for(float pitch:new float[]{Float.NaN,Float.POSITIVE_INFINITY,0.49f,2.01f}) assertFalse(new ClairaudiographRecording("minecraft:pig","minecraft:entity.pig.ambient","ambient",pitch).readable());
  assertFalse(new ClairaudiographRecording("bad id","minecraft:entity.pig.ambient","ambient",1).readable());
 }
 @Test void malformedFieldKeepsAnInertRecordingComponent() {
  var json=JsonParser.parseString("{\"source\":\"minecraft:pig\",\"sound\":\"minecraft:entity.pig.ambient\",\"kind\":\"ambient\",\"pitch\":\"broken\"}");
  var r=ClairaudiographRecording.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
  assertFalse(r.readable()); assertEquals("minecraft:pig",r.source());
 }
}
