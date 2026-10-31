"""Generates the Brined Court's two Seats (Track 7: the Lighthouse Garden and the Vent Sanctum). Re-run from the
repository root: python tools/worldgen/brined_court_seats.py

The generator lives in the brined_court package beside this file: kit.py (the sea-aware canvas Track 8 shares),
garden.py, sanctum.py, checks.py (forbidden blocks, dry air pockets) and build.py (templates, pools, structures).
Set-piece block models come from tools/model_export/ (sunburst_lamp, sunburst_frond, petrified_follower, hydra_crown,
vent_offering_basin, court_cubes).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from brined_court import build  # noqa: E402

if __name__ == '__main__':
    build.main()
