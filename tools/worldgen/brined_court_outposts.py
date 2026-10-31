"""Generates the Brined Court's six inner-house outposts (Track 8: Tide Refuge, Wake nursery, Line Camp joined and
parted, Screened Cleft, the Bell, Whale Fall). Re-run from the repository root:
python tools/worldgen/brined_court_outposts.py

The builders live in the brined_court package beside this file: outposts.py (on Track 7's kit.py canvas), checks.py
and outpost_build.py (templates, pools, structures). Set-piece block models come from tools/model_export/
(holdfast_root_mass, nest_cup, rope_pod, current_strand, fold_veil, bell_sleeper, marrow_plume, house_marker,
court_outpost_cubes); the signs' blocks (sunburst_bloom, relay_marker, messenger_buoy) are placed by the Court sign
feature, not by templates.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from brined_court import outpost_build  # noqa: E402

if __name__ == '__main__':
    outpost_build.main()
