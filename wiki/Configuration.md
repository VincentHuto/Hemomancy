# Configuration

`HemoConfig` exposes **server**, **client**, and **common** specs. Gameplay tuning lives in `HemoServerConfig`; presentation and rendering settings live in `HemoClientConfig`; TerraBlender region settings live in `HemoCommonConfig`.

In `hemomancy-common.toml`, `[worldgen].enablePhlegethonticNetherRegion` defaults to `true`, and `phlegethonticNetherRegionWeight` defaults to `1` with an allowed range of `1–100`. Both require a restart. They control discovery of the Phlegethontic Basin in new Nether chunks. Buried ichor veins remain an independent Nether-wide feature. Existing chunks are not regenerated.

Server settings control Blood Volume regeneration, drain, and kill gain; **Blood Tendency** shifts from combat and manipulation use; **Vascular System** strain, healing, and debuffs; **Bloodline** pool sharing, healing, and fane stake budget; and **Morphling** passive drain timing and cost. Change these values when the pack needs a different progression speed or multiplayer bloodline balance.

The client config includes HUD and render-layer options, such as blood volume HUD placement and toggles for blood gourds, horns, and morphling mutation rendering. World-rendering toggles control Founding Fane boundaries, Harbinger cardinal rite perimeter fog (`renderCardinalRiteFog`), blood orbs, and Oculiflora network sight. `cardinalRiteFogVerticalOffset` raises or lowers the diffuse smoke ring. These settings adjust readability and visual intensity without changing gameplay systems.

## Hematic Succession

The server configuration section `succession` contains `per_bloodline_cap` and `global_cap`. Both default to `0`, disabling numeric caps so compatible workplaces limit residents. Profession workplace blocks are reloadable datapack lists under `data/hemomancy/succession_profession/`.
