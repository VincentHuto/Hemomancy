package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static com.vincenthuto.hemomancy.client.player.WaistPivotRules.*;
import static org.junit.jupiter.api.Assertions.*;

class WaistPivotRulesTest {
    @Test void bottomEdgeStaysPutForEveryAllowedPitch() {
        for(int deg=-14;deg<=14;deg++) {
            float t=(float)Math.toRadians(deg);
            float[] o=offsets(t);
            // Model space: +y is down, the body cube hangs 12 units below its origin and rotates about x.
            double y=12*Math.cos(t)+o[0], z=12*Math.sin(t)+o[1];
            assertEquals(12,y,.05,"pitch "+deg); assertEquals(0,z,.05,"pitch "+deg);
        }
        assertArrayEquals(new float[]{0,0},offsets(0));
    }
    @Test void torsoEasesThroughAnAttackSwingInsteadOfDropping() {
        assertEquals(1F,swingWeight(0F),1e-6F);
        assertTrue(swingWeight(.1F)>.8F,"first swing frame must not drop the lean");
        assertTrue(swingWeight(.5F)<swingWeight(.25F),"lean eases out toward mid-swing");
        assertEquals(1F,swingWeight(1F),1e-5F);
        for(float a=0;a<=1;a+=.05F) assertTrue(swingWeight(a)>=0 && swingWeight(a)<=1);
    }
    @Test void torsoLayerIgnoresOnlyTheSwingWhenCheckingArms() throws IOException {
        String src=Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/player/CastingPlayerPose.java"));
        assertTrue(src.contains("WaistPivotRules.swingWeight("),"torso weight must ease through attack swings");
        assertFalse(src.contains("!player.isUsingItem() && player.getAttackAnim(partial)<=0)"),"torso still drops to zero on attack");
    }
    @Test void pitchIsClamped() {
        assertEquals(Math.toRadians(14),clamp((float)Math.toRadians(30)),1e-6);
        assertEquals(Math.toRadians(-14),clamp((float)Math.toRadians(-30)),1e-6);
        assertEquals(Math.toRadians(7),clamp((float)Math.toRadians(7)),1e-6);
    }
    @Test void headCountersThePitchSoAimIsKept() {
        assertEquals(-.2F,headCompensation(.2F),1e-6);
    }
    @Test void headRidesTheNeckForwardWithoutSinkingIntoTheShoulders() {
        // Captures (mortem-perfectus-held, funeral_bell-held, ground_touch-held): dropping the head with the neck and tilting
        // the shoulders' back edge up sank it about a pixel into the torso. It keeps its height and only follows forward.
        float pitch=(float)Math.toRadians(14);
        float[] head=headOffsets(pitch,0,0), neck=partOffsets(pitch,0,0);
        assertEquals(0,head[0],1e-6,"head keeps its height");
        assertEquals(neck[1],head[1],1e-6,"head follows the neck forward");
        assertArrayEquals(new float[]{0,0},headOffsets(0,0,0));
    }
    @Test void partOffsetsAreAbsoluteSoReapplyingIsIdempotent() {
        float pitch=(float)Math.toRadians(12);
        float[] o=offsets(pitch), once=partOffsets(pitch,3.2F,0), twice=partOffsets(pitch,3.2F,0);
        assertArrayEquals(once,twice);
        assertEquals(3.2F+o[0],once[0],1e-6); assertEquals(o[1],once[1],1e-6);
        // Arms keep their yaw-driven z and add the pivot on top of it.
        assertEquals(4.5F+o[1],partOffsets(pitch,2,4.5F)[1],1e-6);
        assertArrayEquals(new float[]{2,4.5F},partOffsets(0,2,4.5F));
    }
    @Test void pivotAssignsPartZInsteadOfAccumulating() throws IOException {
        // Vanilla setupAnim resets y every frame but never body.z or head.z, so an accumulated z slides the torso away.
        String pose=read("src/main/java/com/vincenthuto/hemomancy/client/player/CastingPlayerPose.java");
        String state=read("src/main/java/com/vincenthuto/hemomancy/client/player/PlayerAnimationRegistry.java");
        assertTrue(pose.contains("model.body.z="),"pivot assigns body.z");
        assertTrue(pose.contains("model.head.z="),"pivot assigns head.z");
        assertFalse(pose.contains(".z+="),"no part z accumulates");
        assertTrue(state.contains("model.body.z=0"),"body.z reset each frame");
        assertTrue(state.contains("model.head.z=0"),"head.z reset each frame");
        // The registry applies its poses in list order: the reset entry comes before the casting pose and every claim.
        int reset=state.indexOf("always(\"offset_reset\""),apply=state.indexOf("always(\"casting\"");
        assertTrue(reset>=0 && reset<apply,"reset precedes every pose and early return");
    }
    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path)).replace(" ","").replace("	","");
    }
    @Test void stanceWeightIsContinuousAcrossSpeed() {
        float previous=CastingPlayerPose.stanceWeight(true,false,0,0);
        for(float v=0;v<=.1F;v+=.001F) {
            float w=CastingPlayerPose.stanceWeight(true,false,v,0);
            assertTrue(Math.abs(w-previous)<=.05F,"speed "+v);
            previous=w;
        }
    }
}
