package de.zusmori.mcqol;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Einstellungen in .minecraft/config/mcqol.properties */
public final class McQoLConfig {
	// Spear Swap
	public static boolean spearEnabled = true;
	/** Linksklick gedrückt halten = automatisch weiter lungen, sobald die Angriffsleiste voll ist. */
	public static boolean spearAutoRepeat = true;
	/** Slot (1-9), auf den geswapped wird, wenn kein Hotbar-Slot leer ist. */
	public static int spearFallbackSlot = 6;

	// Auto-Elytra
	public static boolean elytraEnabled = true;
	/** Auf dem Boden: automatisch springen und losfliegen, wenn man eine Rakete benutzt. */
	public static boolean elytraLaunchFromGround = true;
	/** Nach der Landung wieder die Brustplatte anziehen. */
	public static boolean elytraRestoreChestplate = true;

	private McQoLConfig() {}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("mcqol.properties");
	}

	public static void load() {
		Path path = file();
		if (Files.exists(path)) {
			Properties p = new Properties();
			try (Reader reader = Files.newBufferedReader(path)) {
				p.load(reader);
				spearEnabled = bool(p, "spear.enabled", spearEnabled);
				spearAutoRepeat = bool(p, "spear.auto_repeat", spearAutoRepeat);
				spearFallbackSlot = Integer.parseInt(p.getProperty("spear.fallback_slot", "" + spearFallbackSlot).trim());
				elytraEnabled = bool(p, "elytra.enabled", elytraEnabled);
				elytraLaunchFromGround = bool(p, "elytra.launch_from_ground", elytraLaunchFromGround);
				elytraRestoreChestplate = bool(p, "elytra.restore_chestplate", elytraRestoreChestplate);
			} catch (IOException | NumberFormatException e) {
				McQoLClient.LOGGER.warn("Konnte mcqol.properties nicht lesen, nutze Standardwerte", e);
			}
		}
		save(); // schreibt fehlende Einträge dazu
	}

	public static void save() {
		Properties p = new Properties();
		p.setProperty("spear.enabled", "" + spearEnabled);
		p.setProperty("spear.auto_repeat", "" + spearAutoRepeat);
		p.setProperty("spear.fallback_slot", "" + spearFallbackSlot);
		p.setProperty("elytra.enabled", "" + elytraEnabled);
		p.setProperty("elytra.launch_from_ground", "" + elytraLaunchFromGround);
		p.setProperty("elytra.restore_chestplate", "" + elytraRestoreChestplate);
		try (Writer writer = Files.newBufferedWriter(file())) {
			p.store(writer, "zusmori's mcQoL");
		} catch (IOException e) {
			McQoLClient.LOGGER.warn("Konnte mcqol.properties nicht speichern", e);
		}
	}

	private static boolean bool(Properties p, String key, boolean def) {
		return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)).trim());
	}
}
