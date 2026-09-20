package com.vincenthuto.hemomancy.client.sound;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpectrogramAnalysisTest {
    @Test void waveformMeasuresTheWholeFrameAtHighSampleRates() {
        var pcm=new float[9600];pcm[2500]=.75F;
        assertEquals(.75F,SpectrogramAnalysis.analyze(pcm,96000).amplitude(0));
    }
    @Test void oppositeStereoChannelsDoNotCancelTheMeasuredSound() {
        float[][] pcm=new float[2][24000];
        for(int i=0;i<24000;i++){pcm[0][i]=(float)(.5*Math.sin(2*Math.PI*1000*i/24000));pcm[1][i]=-pcm[0][i];}
        var analysis=SpectrogramAnalysis.analyzeChannels(pcm,24000);
        assertTrue(analysis.energy(8,SpectrogramAnalysis.bandFor(1000))>.8F);
        assertTrue(analysis.amplitude(8)>.45F);
    }
    @Test void silenceHasNoEnergyOrWaveform() {
        var analysis = SpectrogramAnalysis.analyze(new float[24000], 24000);
        for (int frame = 0; frame < analysis.frames(); frame++) {
            assertEquals(0, analysis.amplitude(frame));
            for (int band = 0; band < SpectrogramAnalysis.BANDS; band++) assertEquals(0, analysis.energy(frame, band));
        }
    }
    @Test void resolvedTonesOccupyTheirOwnFrequencyBands() {
        for (int hz : new int[]{250, 1000, 6000}) {
            float[] pcm = new float[24000];
            for (int i = 0; i < pcm.length; i++) pcm[i] = (float)(.5 * Math.sin(2 * Math.PI * hz * i / 24000));
            var analysis = SpectrogramAnalysis.analyze(pcm, 24000);
            int strongest = 0;
            for (int b = 1; b < SpectrogramAnalysis.BANDS; b++) if (analysis.energy(8,b) > analysis.energy(8,strongest)) strongest = b;
            assertEquals(hz, SpectrogramAnalysis.frequency(strongest), hz * .17);
            assertTrue(analysis.amplitude(8) > .45);
        }
    }
    @Test void samplesOutsideClipAreSilentAndPitchMovesTheReadHead() {
        var analysis = SpectrogramAnalysis.analyze(new float[24000], 24000);
        assertEquals(0, analysis.energy(-1, 0));
        assertEquals(0, analysis.energy(analysis.frames(), 0));
        assertEquals(analysis.frameAt(.5, 1), analysis.frameAt(.25, 2));
        assertEquals(2000, SpectrogramAnalysis.playedFrequency(1000, 2));
    }
}
