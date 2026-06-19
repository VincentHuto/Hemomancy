# Dialogue Editor Redesign

**Date:** 2026-06-19  
**Status:** Approved

## Problem

The current `tools/dialogue_editor` has three concrete pain points:

1. `src/frontend/main.ts` is an 870-line monolith mixing state, rendering, API calls, and event binding — hard to navigate and extend.
2. The inspector panel (390px fixed) is cramped — translation text is only visible on hover, options are squeezed, and there is no way to delete a line.
3. The graph shows one tree at a time and requires clicking through a sidebar list to switch between degree-stage trees for the same NPC — disorienting when working across many stages.

Future triggers (animation, sound) have no data model or UI surface yet.

## Chosen Approach

Module split + multi-tree canvas + wide inspector + sidecar metadata. No external framework added.

---

## Architecture — Module Split

`src/frontend/main.ts` is broken into six focused files:

| File | Responsibility |
|------|---------------|
| `state.ts` | Single mutable state object + typed getters (`currentFile`, `currentTree`, `currentNode`) |
| `api.ts` | All `fetch` calls: `loadWorkspace`, `preview`, `applyPreview`, `loadMetadata`, `saveMetadata` |
| `graph.ts` | Multi-tree canvas rendering, SVG bezier edges, drag logic |
| `inspector.ts` | Node editing panel: lines, options, triggers |
| `sidebar.ts` | File group / node list navigation |
| `main.ts` | Wires everything together, `render()` orchestrator, top-level toolbar event handlers |

`state.ts` exports a single plain object (`state`) and pure getter functions. All render modules import from `state` and call `render()` via a passed callback or a named re-export from `main.ts`. No reactive framework — same mental model as now, just not buried in one file.

`api.ts` is the only place that calls `fetch`. It returns typed promises. The rest of the app never touches `fetch` directly.

---

## Multi-Tree Canvas

All trees for the selected NPC file render on one `<div class="graph">`. Trees stack vertically with an 80px gap between them. Each tree is preceded by a **label banner** — a thin horizontal row showing `method`, visibility badge, and node count — anchored at `x=0` at that tree's Y origin.

### Layout Algorithm

Each tree runs the existing BFS depth layout independently. The tree's Y origin is the sum of all previous trees' heights plus gaps. Each tree's height is `(max row count in any column) * 190 + 80`. The next tree's Y origin starts after that.

Every tree's start node anchors at `x=30`. Depth columns are `330px` apart (unchanged). Trees with the same depth structure visually align.

`dispatchOnly` trees render only their label banner (no node canvas), same behavior as now but in-flow rather than replacing the entire view.

Drag positions persist per `graphKey` (`file::method::variant`) — same key as before, so existing manual positions are preserved.

Canvas `min-height` is computed as the sum of all tree heights rather than a fixed value.

### Node Card Structure

Cards are replaced with a richer expandable design:

```
┌──────────────────────────────┐
│  root                        │  ← dark header bar, node ID
├──────────────────────────────┤
│  Lines                  [+]  │  ← collapsed by default, click + to add
│  Options                [+]  │
│    ○  "Who are you?"         │──○  bezier to who_are_you
│    ○  "Leave"                │──○  bezier to end (null)
│  Triggers               [+]  │
├──────────────────────────────┤
│           [+]                │  ← reserved for future section types
└──────────────────────────────┘
```

- **Connection ports** are small `<div>` circles absolutely positioned on the right edge of each option row and the left edge of each node card.
- **Bezier curves** are SVG `<path>` elements rendered in an absolutely-positioned `<svg>` overlay covering the full canvas. Each curve runs from the option's output port to the target node's input port. Replaces the current rotated `<div>` lines.
- **Clicking a row** (Lines section, a specific option row, Triggers section) sets `state.selectedRow` and populates the inspector panel — the card stays compact. `selectedRow` has type `{ nodeId: string; section: 'lines' | 'option' | 'triggers'; optionIndex?: number } | null`.
- The Lines and Triggers section headers collapse/expand inline to show a summary (first line preview, trigger count).

---

## Inspector Panel

Fixed width increases from `390px` to `520px`. The panel is contextual — its content depends on what row is selected in the graph card.

### No selection

```
Select a node row to edit it.
```

### Lines selected

Shows all line keys for the node, each with its translation inline below. Includes a delete button per line. The node rename input lives here (moved out of the lines section to avoid accidental edits).

```
NODE ID   [root                    ]

LINES                      [+ Add Line]
┌─────────────────────────────────────┐
│ hemomancy.acolyte.not_on_path.line1 │  [×]
│ "She watches from the silver deep…" │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ hemomancy.acolyte.not_on_path.line2 │  [×]
│ "Her gaze does not waver."          │
└─────────────────────────────────────┘
```

### Option selected (option index N)

Shows all fields for that one option in full width. Animation Trigger and Sound Trigger appear after Event, clearly separated.

```
OPTION 1

Text Key   [hemomancy.dialogue.acolyte.option.who_are_you]
           "Who are you?"

Goes To    [who_are_you ▼]
Event      [                                             ]
Animation  [                                             ]
Sound      [                                             ]

                                          [Delete Option]
```

### Triggers selected (node-level, future use)

Placeholder panel with a note that node-level triggers are not yet implemented.

---

## Sidecar Metadata

### Storage

One JSON file per NPC, stored in `tools/dialogue_editor/`:

```
tools/dialogue_editor/
  AcolyteMetadata.json
  HarbingerAlchemistMetadata.json
  HarbingerHermitMetadata.json
  ...
```

### Format

```json
{
  "version": 1,
  "options": {
    "corruptedStage::root::1": {
      "animationTrigger": "acolyte_kneel",
      "soundTrigger": "hemomancy:npc/acolyte/kneel"
    }
  }
}
```

Keys are `treeMethod::nodeId::optionIndex`. Values are partial — only populated fields are written.

### Server Routes

Two new routes added to `httpServer.ts`:

- `GET /api/metadata/:speaker` — reads `<speaker>Metadata.json`, returns `{}` if not found.
- `POST /api/metadata/:speaker` — writes the full metadata object back.

The speaker slug is derived from the filename stem (`AcolyteDialogueTrees` → `Acolyte`).

### Data Model

`DialogueOptionModel` in `types.ts` gains two optional fields:

```ts
animationTrigger?: string;
soundTrigger?: string;
```

These are populated from the sidecar on workspace load. They are saved immediately via `POST /api/metadata/:speaker` whenever the user edits them — independently of the main preview/apply cycle, which only covers Java/lang/inquiry files. They never enter `renderOption()` or `parseOptions()` in `dialogueParser.ts`.

---

## CSS Changes

- Inspector column widens: `390px` → `520px` (grid-template-columns in `.layout`).
- `.card-node` gets a new structured layout with collapsible section rows and port circles.
- Canvas `<svg>` overlay added for bezier edges (replaces `.edge` divs).
- `.inspector` gets distinct contextual block styles for Lines / Option / Triggers views.
- Theme variables unchanged.

---

## Out of Scope

- Java `DialogueOption` constructor changes (animation/sound stay sidecar-only until ready).
- Any changes to `dialogueParser.ts`, `workspace.ts`, `validation.ts`, or `diff.ts`.
- Adding new NPC types or tree themes.
- Undo/redo history.
