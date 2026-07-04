# Fungal Scar Consolidation

Fungal scars are now a compact eight-scar roster grown through the Mycelial Crucible and equipped through the fungal scar slot. The removed scar identities are migrated out of old saves instead of remaining as dormant player-facing items.

## Active roster

| Scar | Tendency | Current role |
|---|---|---|
| `rhizovitta_communis` | Animus | Rooted sustain on fungal-network ground plus guarded manipulation-cost refund. |
| `talaromyces_minus` | Ferric | Hyphal vein-mining only; it no longer grants Haste. |
| `noctifly_agaric` | Animus | Existing fungal elytra behavior. |
| `antiphonomyces_resonans` | Ductilis | Existing manipulation echo behavior. |
| `putrivora_resolvens` | Mortem | Digests poison, wither, hunger, and blood-loss effects into limited blood recovery. |
| `oculiflora_reticularis` | Tenebris | Reveal-only local network sight. |
| `cryostroma_perdurans` | Congeatio | Stillness-based conservative resilience without a death-save. |
| `saprovitta_vestigium` | Flammeus | Existing feeding-wake trail behavior. |

## Migration map

Old saves are swept on server login and player tick until removed item ids are replaced:

| Removed id | Replacement |
|---|---|
| `respergillus` | `noctifly_agaric` |
| `lumina_devorans` | `oculiflora_reticularis` |
| `anastocordyceps_nexus` | `oculiflora_reticularis` |
| `thanomyces_resurgens` | `cryostroma_perdurans` |
| `sanguiflora_cadens` | `putrivora_resolvens` |

Removed per-stack scar data, including the old Thanomyces cooldown, is intentionally discarded during migration.

## Oculiflora v1 boundary

Oculiflora is client-only and reveal-only in this stage. It can draw local signals already known to the client: equipped scar state, synced Qliphoth bloom data, nearby entities, and nearby morphic nectar/fluid positions.

It does not add sight-to-tap, terrain-drain behavior, ore reveal, Saint chamber reveal, or new networking. Those belong in deferred follow-up work after the reveal pass is stable.

## Blood guardrails

Any passive blood gain introduced by fungal scars must route through `CirculationIncomeHelper` using `IncomeChannel.SCAR`, so the shared circulation guardrails remain authoritative.
