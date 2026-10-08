"""Compare geology/biomes and report decoration differences between fresh chunk orders."""
import json
import sys
from pathlib import Path

left, right = (json.loads(Path(path).read_text()) for path in sys.argv[1:3])
assert (left["seed"], left["preset"]) == (right["seed"], right["preset"])
key = lambda site: (site["layer"], site["x"], site["z"])
a, b = ({key(site): site for site in report["sites"]} for report in (left, right))
assert a.keys() == b.keys(), "The runs sampled different sites"
differences = []
for site in a:
    for digest in ("noiseSha256", "biomesSha256"):
        assert a[site][digest] == b[site][digest], f"Generation order changed {digest} at {site}"
    if a[site]["blocksSha256"] != b[site]["blocksSha256"]:
        differences.append(site)
print(f'Seed {left["seed"]}: {len(a)} geology and biome hashes match across chunk orders.')
print(f'{len(differences)} finished patches differ after vanilla/existing feature decoration: {differences}')
