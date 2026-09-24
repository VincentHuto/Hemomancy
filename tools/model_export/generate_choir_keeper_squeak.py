"""Generate the Choir Keeper's short End-bird squeak as an Ogg asset."""

import math
import random
import subprocess
import tempfile
import wave
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "src/main/resources/assets/hemomancy/sounds/entity/choir_keeper/squeak.ogg"
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
rate = 44100
duration = 0.42
randomizer = random.Random(2911)
phase = 0.0
samples = bytearray()
for index in range(round(rate * duration)):
    time = index / rate
    progress = time / duration
    pitch = 780 + 710 * math.sin(math.pi * min(progress * 1.35, 1.0)) - 270 * progress
    phase += 2 * math.pi * pitch / rate
    envelope = min(1.0, time / 0.025) * max(0.0, 1.0 - progress) ** 1.7
    warble = 1.0 + 0.12 * math.sin(2 * math.pi * 29 * time)
    voice = math.sin(phase) * 0.64 + math.sin(2 * phase) * 0.19
    breath = (randomizer.random() * 2 - 1) * 0.085
    value = max(-1.0, min(1.0, (voice * warble + breath) * envelope * 0.67))
    samples.extend(round(value * 32767).to_bytes(2, "little", signed=True))

with tempfile.TemporaryDirectory(prefix="choir_keeper_squeak_") as temp:
    wav = Path(temp) / "squeak.wav"
    with wave.open(str(wav), "wb") as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(rate)
        stream.writeframes(samples)
    subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                    "-i", str(wav), "-c:a", "libvorbis", "-q:a", "5", str(OUTPUT)], check=True)
print(f"Wrote {OUTPUT}")
