package de.zusmori.mcqol.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Verbindet die Einstellungsseite mit Mod Menu (Zahnrad-Knopf in der Mod-Liste). */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return McQoLConfigScreen::new;
	}
}
