package de.zusmori.mcqol.config;

import java.util.function.Consumer;

import de.zusmori.mcqol.McQoLConfig;
import de.zusmori.mcqol.elytra.AutoElytra;
import de.zusmori.mcqol.spear.SpearSwap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Einstellungsseite, erreichbar über Mod Menu (oder per Taste O im Spiel). */
public class McQoLConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW = 24;

	private final @Nullable Screen parent;

	public McQoLConfigScreen(@Nullable Screen parent) {
		super(Component.literal("zusmori's mcQoL"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - BUTTON_WIDTH - 5;
		int right = this.width / 2 + 5;
		int top = 40;

		// Linke Spalte: Kampf & Bewegung, Animationen
		int y = top;
		toggle(left, y, "Spear Swap", McQoLConfig.spearEnabled,
				"Speer-Lunge ohne Cooldown per Linksklick (Taste K)",
				v -> { McQoLConfig.spearEnabled = v; SpearSwap.reset(); });
		y += ROW;
		toggle(left, y, "Linksklick halten", McQoLConfig.spearAutoRepeat,
				"Linksklick gedrückt halten = Dauer-Lunge",
				v -> McQoLConfig.spearAutoRepeat = v);
		y += ROW;
		toggle(left, y, "Auto-Elytra", McQoLConfig.elytraEnabled,
				"Rakete benutzen zieht automatisch die Elytra an (Taste J)",
				v -> { McQoLConfig.elytraEnabled = v; AutoElytra.reset(); });
		y += ROW;
		toggle(left, y, "Vom Boden starten", McQoLConfig.elytraLaunchFromGround,
				"Am Boden automatisch abspringen und losfliegen",
				v -> McQoLConfig.elytraLaunchFromGround = v);
		y += ROW;
		toggle(left, y, "Brustplatte zurück", McQoLConfig.elytraRestoreChestplate,
				"Nach der Landung wieder die Brustplatte anziehen",
				v -> McQoLConfig.elytraRestoreChestplate = v);
		y += ROW;
		toggle(left, y, "Keine Abbau-Risse", McQoLConfig.noBreakAnimation,
				"Blendet die Risse beim Abbauen von Blöcken aus",
				v -> McQoLConfig.noBreakAnimation = v);
		y += ROW;
		toggle(left, y, "Kein Kamerawackeln", McQoLConfig.noHurtCam,
				"Kamera wackelt nicht mehr, wenn du Schaden bekommst (rotes Aufblinken bleibt)",
				v -> McQoLConfig.noHurtCam = v);

		// Rechte Spalte: Sicht
		y = top;
		toggle(right, y, "Low Fire", McQoLConfig.lowFire,
				"Feuer am Bildschirm wird nach unten verschoben",
				v -> McQoLConfig.lowFire = v);
		y += ROW;
		slider(right, y, "Feuer tiefer", 0.0, 0.5, McQoLConfig.fireOffset, true,
				v -> McQoLConfig.fireOffset = (float) (double) v);
		y += ROW;
		toggle(right, y, "Kleines Totem", McQoLConfig.smallTotem,
				"Die Totem-Animation wird kleiner angezeigt",
				v -> McQoLConfig.smallTotem = v);
		y += ROW;
		slider(right, y, "Totem-Größe", 0.1, 1.0, McQoLConfig.totemScale, false,
				v -> McQoLConfig.totemScale = (float) (double) v);
		y += ROW;
		toggle(right, y, "Kein Nebel", McQoLConfig.noFog,
				"Entfernt den Nebel in der Ferne",
				v -> McQoLConfig.noFog = v);
		y += ROW;
		toggle(right, y, "Klare Sicht unter Wasser", McQoLConfig.clearWater,
				"Unter Wasser weit sehen, der blaue Farbton bleibt",
				v -> McQoLConfig.clearWater = v);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
				.bounds(this.width / 2 - 100, top + 7 * ROW + 8, 200, BUTTON_HEIGHT)
				.build());
	}

	private void toggle(int x, int y, String name, boolean value, String tooltip, Consumer<Boolean> setter) {
		this.addRenderableWidget(CycleButton.onOffBuilder(value)
				.withTooltip(v -> Tooltip.create(Component.literal(tooltip)))
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal(name), (button, v) -> setter.accept(v)));
	}

	private void slider(int x, int y, String name, double min, double max, double value, boolean percent, Consumer<Double> setter) {
		this.addRenderableWidget(new ValueSlider(x, y, name, min, max, value, percent, setter));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		McQoLConfig.save();
		this.minecraft.gui.setScreen(this.parent);
	}

	/** Schieberegler, der einen Wert zwischen min und max einstellt. */
	private static class ValueSlider extends AbstractSliderButton {
		private final String name;
		private final double min;
		private final double max;
		private final boolean percent;
		private final Consumer<Double> setter;

		ValueSlider(int x, int y, String name, double min, double max, double value, boolean percent, Consumer<Double> setter) {
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), (value - min) / (max - min));
			this.name = name;
			this.min = min;
			this.max = max;
			this.percent = percent;
			this.setter = setter;
			this.updateMessage();
		}

		private double real() {
			return this.min + this.value * (this.max - this.min);
		}

		@Override
		protected void updateMessage() {
			String shown = this.percent
					? Math.round(this.real() / this.max * 100) + "%"
					: Math.round(this.real() * 100) + "%";
			this.setMessage(Component.literal(this.name + ": " + shown));
		}

		@Override
		protected void applyValue() {
			this.setter.accept(this.real());
		}
	}
}
