package de.zusmori.mcqol.test;

import de.zusmori.mcqol.McQoLConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Starts a test world on GitHub and takes screenshots of the totem
 * (in hand and the pop) at full size and at small size.
 */
public class McQoLClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext ctx) {
		try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamemode survival @a");
			server.runCommand("time set day");
			server.runCommand("weather clear");
			server.runCommand("gamerule doDaylightCycle false");

			for (float scale : new float[] {1.0F, 0.3F}) {
				String name = Math.round(scale * 100) + "pct";
				ctx.runOnClient(mc -> McQoLConfig.totemScale = scale);

				// Totem in hand
				server.runCommand("item replace entity @a weapon.mainhand with minecraft:totem_of_undying");
				ctx.waitTicks(20);
				ctx.takeScreenshot("hand_" + name);

				// Totem pop
				server.runCommand("damage @p 1000 minecraft:generic");
				ctx.waitTicks(10);
				ctx.takeScreenshot("pop_" + name);
				ctx.waitTicks(60);
				server.runCommand("effect clear @a");
				server.runCommand("effect give @a minecraft:instant_health 1 10");
				ctx.waitTicks(10);
			}
		}
	}
}
