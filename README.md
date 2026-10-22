# zusmori's Spear Swap

Client-Mod für **Minecraft 26.3 (Fabric)**. Erkennt Speere und macht den Lunge-Attribute-Swap automatisch per Linksklick. Gedacht für Singleplayer.

## Benutzung
1. Speer in die Hotbar, mindestens ein Hotbar-Slot frei lassen.
2. Speer auswählen und **linksklicken**: Die Mod sticht zu und wechselt auf den leeren Slot.
3. Ab jetzt **linksklicken oder Linksklick halten**: Speer nehmen, zustechen, zurück, alles im selben Tick.
4. Anderen Slot auswählen beendet den Modus. **K** schaltet die Mod an/aus.

Einstellungen: `.minecraft/config/spearswap.properties`
- `fallback_slot` – Slot 1–9, falls kein Hotbar-Slot frei ist (Standard 6)
- `auto_repeat` – Linksklick halten = Dauer-Lunge (Standard true)

## Voraussetzungen
Fabric Loader 0.19.5+, Fabric API für 26.3, Java 25.

## Bauen
GitHub Actions baut die Mod bei jedem Upload automatisch (Tab „Actions“ → Artifacts).
Lokal: `gradlew.bat build` → `build/libs/zusmoris-spear-swap-1.0.0.jar`
