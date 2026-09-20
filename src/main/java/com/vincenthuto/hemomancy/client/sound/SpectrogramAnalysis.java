package com.vincenthuto.hemomancy.client.sound;

/** Windowed source-audio analysis, independent of rendering, device volume and OpenAL. */
public final class SpectrogramAnalysis {
    public static final int BANDS = 64;
    public static final int FRAMES_PER_SECOND = 30;
    private static final int WINDOW = 2048;
    private final byte[][] energy;
    private final float[] amplitudes;

    private SpectrogramAnalysis(byte[][] energy, float[] amplitudes) {
        this.energy = energy;
        this.amplitudes = amplitudes;
    }
    public int frames() { return energy.length; }
    public float amplitude(int frame) { return frame < 0 || frame >= frames() ? 0 : amplitudes[frame]; }
    public float energy(int frame, int band) {
        return frame < 0 || frame >= frames() || band < 0 || band >= BANDS ? 0 : Byte.toUnsignedInt(energy[frame][band]) / 255F;
    }
    public int frameAt(double seconds, float pitch) { return (int)Math.floor(seconds * pitch * FRAMES_PER_SECOND); }
    public static double frequency(int band) { return 50 * Math.pow(240, band / (double)(BANDS - 1)); }
    public static double playedFrequency(double frequency, float pitch) { return frequency * pitch; }
    public static int bandFor(double hz) { return (int)Math.round(Math.log(hz / 50) / Math.log(240) * (BANDS - 1)); }

    public static SpectrogramAnalysis analyzeChannels(float[][] channels,int sampleRate) {
        if(channels.length==0)throw new IllegalArgumentException("At least one audio channel required");
        var result=analyze(channels[0],sampleRate);
        for(int channel=1;channel<channels.length;channel++) {
            var next=analyze(channels[channel],sampleRate);
            if(next.frames()!=result.frames())throw new IllegalArgumentException("Audio channel lengths differ");
            for(int frame=0;frame<result.frames();frame++) {
                result.amplitudes[frame]=Math.max(result.amplitudes[frame],next.amplitudes[frame]);
                for(int band=0;band<BANDS;band++) if(Byte.toUnsignedInt(next.energy[frame][band])>Byte.toUnsignedInt(result.energy[frame][band]))result.energy[frame][band]=next.energy[frame][band];
            }
        }
        return result;
    }

    public static SpectrogramAnalysis analyze(float[] samples, int sampleRate) {
        if (sampleRate <= 0) throw new IllegalArgumentException("Positive sample rate required");
        int count = (int)Math.ceil(samples.length * (double)FRAMES_PER_SECOND / sampleRate);
        byte[][] result = new byte[count][BANDS];
        float[] peaks = new float[count];
        double[] real = new double[WINDOW], imaginary = new double[WINDOW];
        for (int frame = 0; frame < count; frame++) {
            int start = frame * sampleRate / FRAMES_PER_SECOND;
            int end=Math.min(samples.length,(frame+1)*sampleRate/FRAMES_PER_SECOND);
            for(int sample=start;sample<end;sample++)peaks[frame]=Math.max(peaks[frame],Math.abs(samples[sample]));
            java.util.Arrays.fill(imaginary, 0);
            for (int i = 0; i < WINDOW; i++) {
                float value = start + i < samples.length ? samples[start + i] : 0;
                real[i] = value * (.5 - .5 * Math.cos(2 * Math.PI * i / (WINDOW - 1)));
            }
            fft(real, imaginary);
            for (int band = 0; band < BANDS; band++) {
                double center = frequency(band);
                int low = Math.max(1, (int)Math.floor(center / Math.pow(240, .5 / (BANDS-1)) * WINDOW / sampleRate));
                int high = Math.min(WINDOW / 2, Math.max(low + 1, (int)Math.ceil(center * Math.pow(240, .5 / (BANDS-1)) * WINDOW / sampleRate)));
                double peak = 0;
                for (int bin = low; bin < high; bin++) peak = Math.max(peak, Math.hypot(real[bin], imaginary[bin]) * 4 / WINDOW);
                double db = peak <= 0 ? -80 : 20 * Math.log10(peak);
                result[frame][band] = (byte)Math.round(Math.clamp((db + 80) / 80, 0, 1) * 255);
            }
        }
        return new SpectrogramAnalysis(result, peaks);
    }

    private static void fft(double[] real, double[] imaginary) {
        int n = real.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) { double value = real[i]; real[i] = real[j]; real[j] = value; }
        }
        for (int length = 2; length <= n; length <<= 1) {
            double angle = -2 * Math.PI / length;
            for (int offset = 0; offset < n; offset += length) {
                for (int j = 0; j < length / 2; j++) {
                    double cosine = Math.cos(angle * j), sine = Math.sin(angle * j);
                    int a = offset + j, b = a + length / 2;
                    double r = real[b] * cosine - imaginary[b] * sine;
                    double im = real[b] * sine + imaginary[b] * cosine;
                    real[b] = real[a] - r; imaginary[b] = imaginary[a] - im;
                    real[a] += r; imaginary[a] += im;
                }
            }
        }
    }
}
