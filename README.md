# zusmori's mcQoL

Kleine Quality-of-Life-Mod für **Minecraft 26.3 (Fabric)**. Läuft nur im Client und ist für Singleplayer gedacht.

## Features

### Spear Swap (Taste **K** an/aus)
Speer-Lunge ohne den langen Speer-Cooldown, per Attribute Swap.
1. Speer in die Hotbar legen, mindestens einen Hotbar-Slot frei lassen.
2. Speer auswählen und **linksklicken**: Stoß mit dem Speer, danach Wechsel auf den leeren Slot.
3. Ab jetzt **linksklicken oder Linksklick halten**: Speer nehmen, zustechen, zurück, alles im selben Tick.
4. Einen anderen Slot auswählen beendet den Modus.

### Auto-Elytra (Taste **J** an/aus)
1. Einfach eine **Rakete benutzen** (Rechtsklick), egal ob am Boden oder in der Luft.
2. Die Elytra wird automatisch angezogen, egal ob sie in der Hotbar, im Inventar oder in der Zweithand liegt. Deine Brustplatte kommt so lange an ihren Platz.
3. Am Boden springst du automatisch ab, fängst an zu gleiten und die Rakete zündet.
4. **Nach der Landung** wird die Brustplatte wieder angezogen und die Elytra zurückgelegt.

## Einstellungen
`.minecraft/config/mcqol.properties`

| Eintrag | Standard | Bedeutung |
|---|---|---|
| `spear.enabled` | true | Spear Swap an/aus |
| `spear.auto_repeat` | true | Linksklick halten = Dauer-Lunge |
| `spear.fallback_slot` | 6 | Slot 1–9, falls kein Hotbar-Slot frei ist |
| `elytra.enabled` | true | Auto-Elytra an/aus |
| `elytra.launch_from_ground` | true | Am Boden automatisch abspringen |
| `elytra.restore_chestplate` | true | Nach der Landung Brustplatte wieder anziehen |

## Voraussetzungen
Fabric Loader 0.19.5+, Fabric API für 26.3, Java 25.

## Bauen
GitHub Actions baut die Mod bei jedem Push. Die fertige .jar liegt im Branch `builds`.
Lokal: `gradlew.bat build` → `build/libs/zusmoris-mcqol-<version>.jar`
