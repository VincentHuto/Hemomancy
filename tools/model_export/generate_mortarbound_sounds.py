"""Build the Mortarbound's five short sculk and wet-ink foley cues."""
from pathlib import Path
import math
import random
import subprocess
import wave

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/hemomancy/sounds/entity/mortarbound"
SCRATCH = ROOT / "build/mortarbound_audio"
OUT.mkdir(parents=True, exist_ok=True)
SCRATCH.mkdir(parents=True, exist_ok=True)
RATE = 22050

def render(name, duration, seed, pitch, scrape):
    random_source = random.Random(seed)
    samples = []
    filtered = 0.0
    total = int(duration * RATE)
    for i in range(total):
        t = i / RATE
        envelope = min(1, t * 25) * min(1, (duration - t) * 9)
        white = random_source.uniform(-1, 1)
        filtered = filtered * .88 + white * .12
        wet = math.sin(2 * math.pi * (pitch * t + 25 * t * t)) * .15
        ticks = (1 if math.sin(2 * math.pi * (11 + scrape * t) * t) > .93 else 0) * white * .18
        value = max(-1, min(1, (filtered * scrape + wet + ticks) * envelope * .7))
        samples.append(int(value * 32767))
    wav = SCRATCH / f"{name}.wav"
    with wave.open(str(wav), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(RATE)
        handle.writeframes(b"".join(value.to_bytes(2, "little", signed=True) for value in samples))
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", str(wav), "-c:a", "libvorbis", "-qscale:a", "4", str(OUT / f"{name}.ogg")], check=True)

for args in (("ambient", 1.7, 11, 48, .5), ("crawl", .42, 23, 83, .8),
             ("attack", .9, 37, 91, 1.2), ("hurt", .6, 41, 141, .9), ("death", 2.1, 53, 37, .8)):
    render(*args)
