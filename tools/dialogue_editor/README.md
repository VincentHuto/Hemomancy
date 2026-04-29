# Dialogue Tree Editor

A self-contained visual tool for viewing and editing Hemomancy NPC dialogue trees without touching Java directly.

```
tools/dialogue_editor/
├── index.html          ← Open this in any browser — no server needed
├── export_to_json.py   ← Java file → JSON (run once before editing)
├── import_from_json.py ← JSON → Java (run after editing to write back)
└── README.md           ← This file
```

---

## Quick start

### 1 — Export a Java dialogue file to JSON

```bash
# From the repo root:
python tools/dialogue_editor/export_to_json.py \
    src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java \
    vicar.json
```

Repeat for any file you want to edit:
- `HarbingerAlchemistDialogueTrees.java`
- `ZealotDialogueTrees.java`
- `GuardianDialogueTrees.java`
- `HarbingerHermitDialogueTrees.java`
- etc.

### 2 — Open the editor

Just open `tools/dialogue_editor/index.html` in your browser (double-click or drag onto a browser window).

**Load JSON** → select the `.json` file you exported.  
**Load lang** (optional) → select `src/main/resources/assets/hemomancy/lang/en_us.json` to see English translations inline.

### 3 — Edit

- **Left panel** — lists every tree (method) in the file; click to switch.
- **Graph** — nodes shown as boxes with bezier arrows between them.  Drag nodes to rearrange.  Click a node to select it.
- **Right panel** — edit the selected node: change its ID, add/remove/reorder lines and options, change where each option leads, set event IDs.
- **⟳ Relayout** — resets automatic positioning based on tree structure.
- **＋ Node** — adds a blank new node at the bottom.

### 4 — Export and write back to Java

Click **💾 Export JSON** to save the modified JSON, then:

```bash
python tools/dialogue_editor/import_from_json.py \
    vicar.json \
    src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java
```

The script backs up the original file as `*.bak` before writing.

---

## Limitations

| Situation | Behaviour |
|---|---|
| Method with multiple `if`-branches (e.g. `alreadyOnPath`) | Exported as `method__0`, `method__1` … one entry per branch. Import regenerates each branch independently. |
| Options built via `List<DialogueOption>` variable | Fully resolved by the exporter — should round-trip cleanly. |
| `itemInquiry` and similar switch-dispatch helpers | Exported and importable like any other tree. |
| Conditional logic / Java control flow | **Never touched** — only the `.builder(...)...build()` chains are replaced. |

---

## JSON format (reference)

```json
{
  "source_file": "HarbingerVicarDialogueTrees.java",
  "speaker":     "entity.hemomancy.harbinger_vicar",
  "icon":        "hemomancy:textures/entity/harbinger_vicar/harbinger_vicar.png",
  "trees": [
    {
      "method":     "uninitiated",
      "params":     ["entityId"],
      "theme":      "BLOOD",
      "start_node": "greeting",
      "speaker":    "entity.hemomancy.harbinger_vicar",
      "icon":       "hemomancy:textures/...",
      "nodes": [
        {
          "id":    "greeting",
          "lines": ["hemomancy.vicar.uninitiated.line1"],
          "options": [
            {"text": "hemomancy.dialogue.vicar.option.leave", "next": null,          "event": null},
            {"text": "hemomancy.dialogue.vicar.option.who",   "next": "some_node",   "event": null},
            {"text": "hemomancy.dialogue.vicar.option.act",   "next": null,          "event": "my_event"}
          ]
        }
      ]
    }
  ]
}
```

**Theme** values: `BLOOD` (red/crimson), `UNSTAINED` (blue/white), `FUNGAL` (amber/orange).  
**next**: node ID to navigate to, or `null` to end the conversation.  
**event**: optional event key fired when the option is chosen (see `DialogueEventHandler`).
