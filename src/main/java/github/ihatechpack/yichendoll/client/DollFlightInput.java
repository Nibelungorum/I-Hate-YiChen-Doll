package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import github.ihatechpack.yichendoll.network.FlightBoostPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sync held sprint input, not a one-shot impulse. Key remapping is respected. */
@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID, value = Dist.CLIENT)
public final class DollFlightInput {
    private static boolean boosting;
    private static int heartbeat;

    private DollFlightInput() {}

    public static boolean isBoosting(Player player) {
        return player == Minecraft.getInstance().player && boosting;
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) {
            boosting = false;
            heartbeat = 0;
            return;
        }
        boolean down = minecraft.screen == null && minecraft.options.keySprint.isDown()
                && (!player.onGround() || minecraft.options.keyJump.isDown())
                && DollFlight.canStartGliding(player);
        if (down && !player.isFallFlying()) DollFlight.startGliding(player);
        if (down != boosting || down && ++heartbeat >= 10) {
            PacketDistributor.sendToServer(new FlightBoostPayload(down));
            heartbeat = 0;
        }
        boosting = down;
    }
}
