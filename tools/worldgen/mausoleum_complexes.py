"""Generates the four Saint mausoleum complexes (Section 5; rework tracks in docs/mausoleums/). Re-run from the
repository root: python tools/worldgen/mausoleum_complexes.py

The generator lives in the mausoleum package beside this file: kit.py (the architectural modules), trials.py (the
Chapel marks across a wing), pieces.py (room builders), themes/<saint>.py (palettes and kinds; the theme tracks own
these) and build.py (templates, pools, structures, tags, and the layout check).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from mausoleum import build  # noqa: E402

if __name__ == '__main__':
    build.main()
