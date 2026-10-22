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

/** Settings screen. Opens from Mod Menu or with the O key. */
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

		// Left column: combat, movement, animations
		int y = top;
		toggle(left, y, "Spear Swap", McQoLConfig.spearEnabled,
				"Lunge with no cooldown",
				v -> { McQoLConfig.spearEnabled = v; SpearSwap.reset(); });
		y += ROW;
		toggle(left, y, "Hold to Lunge", McQoLConfig.spearAutoRepeat,
				"Hold left click to keep lunging",
				v -> McQoLConfig.spearAutoRepeat = v);
		y += ROW;
		toggle(left, y, "Auto Elytra", McQoLConfig.elytraEnabled,
				"Use a rocket to put on your elytra",
				v -> { McQoLConfig.elytraEnabled = v; AutoElytra.reset(); });
		y += ROW;
		toggle(left, y, "Ground Launch", McQoLConfig.elytraLaunchFromGround,
				"Jump and fly when on the ground",
				v -> McQoLConfig.elytraLaunchFromGround = v);
		y += ROW;
		toggle(left, y, "Swap Back", McQoLConfig.elytraRestoreChestplate,
				"Put your chestplate back on after landing",
				v -> McQoLConfig.elytraRestoreChestplate = v);
		y += ROW;
		toggle(left, y, "No Break Cracks", McQoLConfig.noBreakAnimation,
				"Hide cracks when mining",
				v -> McQoLConfig.noBreakAnimation = v);
		y += ROW;
		toggle(left, y, "No Hurt Shake", McQoLConfig.noHurtCam,
				"No camera shake when hit",
				v -> McQoLConfig.noHurtCam = v);

		// Right column: visuals
		y = top;
		toggle(right, y, "Low Fire", McQoLConfig.lowFire,
				"Move fire lower on screen",
				v -> McQoLConfig.lowFire = v);
		y += ROW;
		slider(right, y, "Fire Height", 0.0, 0.5, McQoLConfig.fireOffset,
				v -> McQoLConfig.fireOffset = (float) (double) v, true);
		y += ROW;
		toggle(right, y, "Small Totem", McQoLConfig.smallTotem,
				"Make the totem pop smaller",
				v -> McQoLConfig.smallTotem = v);
		y += ROW;
		slider(right, y, "Totem Size", 0.1, 1.0, McQoLConfig.totemScale,
				v -> McQoLConfig.totemScale = (float) (double) v, false);
		y += ROW;
		toggle(right, y, "No Fog", McQoLConfig.noFog,
				"Remove fog",
				v -> McQoLConfig.noFog = v);
		y += ROW;
		toggle(right, y, "Clear Water", McQoLConfig.clearWater,
				"See far underwater",
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

	private void slider(int x, int y, String name, double min, double max, double value, Consumer<Double> setter, boolean relative) {
		this.addRenderableWidget(new ValueSlider(x, y, name, min, max, value, setter, relative));
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

	/** Slider that sets a value between min and max and shows it in percent. */
	private static class ValueSlider extends AbstractSliderButton {
		private final String name;
		private final double min;
		private final double max;
		private final Consumer<Double> setter;
		/** true = percent of max, false = the value itself in percent. */
		private final boolean relative;

		ValueSlider(int x, int y, String name, double min, double max, double value, Consumer<Double> setter, boolean relative) {
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), (value - min) / (max - min));
			this.name = name;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.relative = relative;
			this.updateMessage();
		}

		private double real() {
			return this.min + this.value * (this.max - this.min);
		}

		@Override
		protected void updateMessage() {
			double shown = this.relative ? this.real() / this.max : this.real();
			this.setMessage(Component.literal(this.name + ": " + Math.round(shown * 100) + "%"));
		}

		@Override
		protected void applyValue() {
			this.setter.accept(this.real());
		}
	}
}
