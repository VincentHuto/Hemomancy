# Step 10, item 2: render-type caching profile

Measured 2026-10-22 on the owner's Windows machine, before (`29a5576ae`, a worktree) and after (item 2), two runs each.

## Scene and method

- Harness: `src/gameTest/.../RenderTypeProfileReview` (run `renderTypeProfileClient`;
  `-PprofileLabel=<label>`; save `RenderTypeProfile`, restored from one template before every run).
- Scene: 81 Hematic Stakes (a 9 × 9 grid, two blocks apart) in front of a still camera, and a hotbar of nine
  monolith-surface items (fragment, imbued cloth, Memory of Vesper, cornerstone, stake, Sanguine Monolith, Covenant
  Throne). Each of these asked `HemoRenderTypes` for a new render type on every draw before item 2.
- Window: 600 ticks (30 s) after a 200-tick warm-up. Measured at the same time:
  - frame times (`RenderFrameEvent.Post`),
  - a Spark 1.10.124 CPU profile (`sparkc profiler start --timeout 30 --save-to-file`; Spark was dropped into the run's
    `mods` folder and is not part of the build),
  - a JFR recording with the `profile` settings, read for `jdk.ObjectAllocationSample` on the Render thread.
- Spark's allocation mode needs async-profiler, which does not run on Windows, so allocation comes from JFR.

## Results

| Run | Frames | Mean frame | p95 frame | Spark: `HemoRenderTypes.*` | Spark: `RenderType.create` | JFR: allocated under `HemoRenderTypes` | JFR: allocated under `RenderType.create` |
|---|---:|---:|---:|---:|---:|---:|---:|
| before 1 | 3,348 | 8.797 ms | 11.735 ms | 92 ms (0.31 %) | 0 ms | 94.7 MB | 26.1 MB |
| before 2 | 3,353 | 8.785 ms | 11.787 ms | 156 ms (0.52 %) | 4 ms | 93.6 MB | 27.8 MB |
| after 1 | 3,324 | 8.865 ms | 11.759 ms | 164 ms (0.55 %) | 0 ms | 12.3 MB | 0 (none sampled) |
| after 2 | 3,343 | 8.814 ms | 11.753 ms | 152 ms (0.51 %) | 0 ms | 21.1 MB | 0 (none sampled) |

Spark percentages are of the Render thread's 30 s. JFR weights are sampled estimates over the same 30 s.

## Reading

- Render types are no longer built per draw: allocation under `RenderType.create` drops from about 27 MB per 30 s to
  nothing sampled. What remains under `HemoRenderTypes` (12–21 MB) is the small uniforms record and boxed seed each
  call allocates.
- CPU and frame time do not change measurably in this scene. Building a type was already cheap in CPU terms (below
  Spark's 4 ms sampling resolution), and each stake still has its own seed, so it still gets its own batch.
- Spark's other frames for comparison (Render thread): `BufferSource.endBatch` 5.9–6.2 % before and after,
  `BufferSource.getBuffer` 4.2–4.5 %, `HematicStakeRenderer.render` 5.6–6.0 %.

## Files (not committed)

- Spark: `build/render-type-profile/config/spark/profile-2026-10-22_23.06.20.sparkprofile` and `_23.07.52` (after);
  `build/render-type-profile/baseline/profile-2026-10-22_22.52.31.sparkprofile` and `_22.54.01` (before). Open them in
  the Spark viewer.
- JFR: `build/render-type-profile/render-type-profile/{afterP1,afterP2}.jfr` and
  `build/render-type-profile/baseline/{beforeP1,beforeP2}.jfr`.
