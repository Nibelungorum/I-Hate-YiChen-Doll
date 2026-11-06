package github.ihatechpack.yichendoll.common;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.integration.curios.core.CuriosExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID)
public final class MXmDollManager {
    private static final Map<UUID, Long> WEARERS = new HashMap<>();
    private static final int CHECK_INTERVAL = 40;

    private MXmDollManager() {}

    public static boolean isWearing(Entity entity) {
        return entity instanceof ServerPlayer && WEARERS.containsKey(entity.getUUID());
    }

    public static void update(Player player, boolean wearing) {
        if (!(player instanceof ServerPlayer)) return;
        if (wearing) {
            WEARERS.put(player.getUUID(), player.level().getGameTime());
        } else {
            WEARERS.remove(player.getUUID());
        }
    }

    public static boolean needsRefresh(Player player) {
        if (!(player instanceof ServerPlayer)) return false;
        Long lastCheck = WEARERS.get(player.getUUID());
        long now = player.level().getGameTime();
        return lastCheck == null || now < lastCheck || now - lastCheck >= CHECK_INTERVAL;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        CuriosExtension.refreshMXmDoll(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        CuriosExtension.refreshMXmDoll(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        WEARERS.remove(event.getOriginal().getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WEARERS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WEARERS.clear();
    }
}
