# Review launchers

Single review clients are Gradle runs, generated from the table in `gradle/review-clients.gradle`
(`./gradlew.bat runLoomReviewClient`, ...). The launchers here set up reviews that need a server plus
several clients, or that repurpose the base `client`, `server` or `alphaJourneyClient` runs. Pass an init
script with `-I`, from the project root:

| Launcher | Sets up | Example |
|---|---|---|
| `casting-animation-review.init.gradle` | server + `castingCaster` / `castingObserver` clients (`hemomancy.castingReview*`) | `./gradlew.bat runServer -I tools/review/casting-animation-review.init.gradle` |
| `school-combat-acceptance.init.gradle` | server + `schoolCaster` / `schoolObserver` / `schoolLate` (`hemomancy.schoolAcceptance*`) | `-PschoolScene=<n>` |
| `manipulation-remote-acceptance.init.gradle` | server + `area6Caster` / `area6Observer` (`hemomancy.remoteAcceptance*`) | |
| `manipulation-visceral-review.init.gradle` | `client` for the visceral, thermal, lux/umbra and charge reviews | `-PthermalReview`, `-PluxUmbraReview`, ... |
| `manipulation-visual-review.init.gradle` | `client` on the disposable `Area5Review` save (`hemomancy.visualWorldReview`) | |
| `ferric-ductilis-review.init.gradle` | `client` for the Ferric review (`hemomancy.ferricReview*`) | `-PferricObserver` for the observer |
| `animus-mortem-review.init.gradle` | `client` for the Animus Mortem review (`hemomancy.animusMortem*`) | `-PbloodObserver` for the observer |
| `gameplay-campaign.init.gradle` | `alphaJourneyClient` as a scripted campaign player | `-PgameplayRole=<role>` |
| `review-blood-observer.ps1`, `review-ferric-observer.ps1`, `review-thermal-observer.ps1` | a second, observer client beside a running multiplayer review, reusing its launch arguments | `pwsh tools/review/review-thermal-observer.ps1 -Label casts` |

Screenshots land in each run's game directory under `build/`. Don't edit code under review while a run is
going: a review client builds whatever source is on disk when it starts.
