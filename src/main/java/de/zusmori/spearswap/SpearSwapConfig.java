package de.zusmori.spearswap;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/** Einstellungen in .minecraft/config/spearswap.properties */
public final class SpearSwapConfig {
	/** Mod an/aus (auch per Taste K umschaltbar). */
	public static boolean enabled = true;
	/** Linksklick gedrückt halten = automatisch weiter lungen, sobald die Angriffsleiste voll ist. */
	public static boolean autoRepeat = true;
	/** Slot (1-9), auf den geswapped wird, wenn kein Hotbar-Slot leer ist. */
	public static int fallbackSlot = 6;

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("spearswap.properties");
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			save();
			return;
		}
		Properties props = new Properties();
		try (Reader reader = Files.newBufferedReader(path)) {
			props.load(reader);
			enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
			autoRepeat = Boolean.parseBoolean(props.getProperty("auto_repeat", "true"));
			fallbackSlot = Integer.parseInt(props.getProperty("fallback_slot", "6").trim());
		} catch (IOException | NumberFormatException e) {
			SpearSwapClient.LOGGER.warn("Konnte spearswap.properties nicht lesen, nutze Standardwerte", e);
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("enabled", Boolean.toString(enabled));
		props.setProperty("auto_repeat", Boolean.toString(autoRepeat));
		props.setProperty("fallback_slot", Integer.toString(fallbackSlot));
		try (Writer writer = Files.newBufferedWriter(file())) {
			props.store(writer, "zusmori's Spear Swap - fallback_slot = Slot 1-9, falls kein Hotbar-Slot leer ist");
		} catch (IOException e) {
			SpearSwapClient.LOGGER.warn("Konnte spearswap.properties nicht speichern", e);
		}
	}
}
