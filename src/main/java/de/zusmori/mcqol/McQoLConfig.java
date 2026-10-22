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

	// Sicht
	public static boolean lowFire = true;
	/** Wie weit das Feuer nach unten verschoben wird (0.0 - 0.5). */
	public static float fireOffset = 0.3F;
	public static boolean smallTotem = true;
	/** Größe der Totem-Animation (0.1 - 1.0, Vanilla = 1.0). */
	public static float totemScale = 0.5F;
	public static boolean noFog = true;
	public static boolean clearWater = true;

	// Animationen
	public static boolean noBreakAnimation = true;
	public static boolean noHurtCam = true;

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
				lowFire = bool(p, "visual.low_fire", lowFire);
				fireOffset = clamp(flt(p, "visual.fire_offset", fireOffset), 0F, 0.5F);
				smallTotem = bool(p, "visual.small_totem", smallTotem);
				totemScale = clamp(flt(p, "visual.totem_scale", totemScale), 0.1F, 1F);
				noFog = bool(p, "visual.no_fog", noFog);
				clearWater = bool(p, "visual.clear_water", clearWater);
				noBreakAnimation = bool(p, "animation.no_break_animation", noBreakAnimation);
				noHurtCam = bool(p, "animation.no_hurt_cam", noHurtCam);
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
		p.setProperty("visual.low_fire", "" + lowFire);
		p.setProperty("visual.fire_offset", "" + fireOffset);
		p.setProperty("visual.small_totem", "" + smallTotem);
		p.setProperty("visual.totem_scale", "" + totemScale);
		p.setProperty("visual.no_fog", "" + noFog);
		p.setProperty("visual.clear_water", "" + clearWater);
		p.setProperty("animation.no_break_animation", "" + noBreakAnimation);
		p.setProperty("animation.no_hurt_cam", "" + noHurtCam);
		try (Writer writer = Files.newBufferedWriter(file())) {
			p.store(writer, "zusmori's mcQoL");
		} catch (IOException e) {
			McQoLClient.LOGGER.warn("Konnte mcqol.properties nicht speichern", e);
		}
	}

	private static float flt(Properties p, String key, float def) {
		try {
			return Float.parseFloat(p.getProperty(key, Float.toString(def)).trim());
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

	private static boolean bool(Properties p, String key, boolean def) {
		return Boolean.parseBoolean(p.getProperty(key, Boolean.toString(def)).trim());
	}
}
