"""Generates the wayside discovery templates: the Bog-Body Ossuary Niche (swamp) and the Sanguine Surveyor Bivouac
(crimson forest). Re-run from the repository root:
python tools/worldgen/wayside_sites.py

The builders live in the wayside package beside this file: kit.py (a Site canvas on the mausoleum kit's Room),
ossuary.py, bivouac.py, checks.py (block states, placeholder inscriptions, mounts and water) and build.py. Each
template carries two or three placeholder Blood Echo inscriptions that its structure's afterPlace swaps for a random
memo or log.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from wayside import build  # noqa: E402

if __name__ == '__main__':
    build.main()
