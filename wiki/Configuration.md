# Configuration

`HemoConfig` exposes active **server**, **client**, and reserved **common** specs. Gameplay tuning lives in `HemoServerConfig`; presentation and rendering settings live in `HemoClientConfig`.

Server settings control Blood Volume regeneration, drain, and kill gain; **Blood Tendency** shifts from combat and manipulation use; **Vascular System** strain, healing, and debuffs; **Bloodline** pool sharing, healing, and fane stake budget; and **Morphling** passive drain timing and cost. Change these values when the pack needs a different progression speed or multiplayer bloodline balance.

The client config includes HUD and render-layer options, such as blood volume HUD placement and toggles for blood gourds, horns, and morphling mutation rendering. World-rendering toggles control Founding Fane boundaries, Harbinger cardinal rite perimeter fog (`renderCardinalRiteFog`), blood orbs, and Oculiflora network sight. `cardinalRiteFogVerticalOffset` raises or lowers the diffuse smoke ring. These settings adjust readability and visual intensity without changing gameplay systems.
