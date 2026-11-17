"""Generates the Crimson Troupe's templates from the Circus site kit (chunk 15). Re-run from the repository root:
python tools/worldgen/circus_sites.py

The builders live in the circus package beside this file: kit.py (the Site canvas, Frames and every set-piece
assembly: the big top, the backyard, the midway, the camps' apparatus and the wrecked stage wagon), sampler.py (the kit
sampler the tests stand on), checks.py (states, metals, multiblocks, mounts and flats), preview.py (cutaway sheets)
and build.py. The set-piece blocks themselves are modelled in tools/model_export/circus_set_pieces.py. Output is the
same bytes on every run and is only templates (and, once the sites are rebuilt on the kit, their pools).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from circus import build  # noqa: E402

if __name__ == '__main__':
    build.main()
