"""Export the editable Naeglerophaeon Blockbench project for the runtime renderer.

Run from the repository root: python tools/model_export/export_naeglerophaeon_cubes.py
The .bbmodel remains the editable source; Gradle excludes .bbmodel files from jars.
"""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "src/main/resources/assets/hemomancy/models/entity/bbmodel/NaeglerophaeonCubeConcept.bbmodel"
TARGET = ROOT / "src/main/resources/assets/hemomancy/models/entity/naeglerophaeon_cube_geometry.json"

project = json.loads(SOURCE.read_text(encoding="utf-8"))
geometry = {"elements": project["elements"], "outliner": project["outliner"]}
TARGET.write_text(json.dumps(geometry, separators=(",", ":")), encoding="utf-8")
print(f"Exported {len(geometry['elements'])} cubes to {TARGET}")
