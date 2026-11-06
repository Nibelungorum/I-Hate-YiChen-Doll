package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.api.doll.EquippedDoll;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import github.ihatechpack.yichendoll.integration.ae2.AE2Bridge;
import java.util.Map;
import java.util.UUID;

/** Single server-thread pipeline shared by every doll behavior and combat query. */
@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID)
public final class DollRuntime {
    private static final Map<UUID, EquipmentState<EquippedDoll>> PLAYERS = new HashMap<>();

    private DollRuntime() {}

    public static boolean isWearing(Entity entity, String dollId) {
        return count(entity, dollId) > 0;
    }

    public static int count(Entity entity, String dollId) {
        if (!(entity instanceof ServerPlayer)) return 0;
        EquipmentState<EquippedDoll> state = PLAYERS.get(entity.getUUID());
        EquippedDoll equipped = state == null ? null : state.snapshot().get(dollId);
        return equipped == null ? 0 : equipped.count();
    }

    public static List<ItemStack> stacks(ServerPlayer player, String dollId) {
        EquipmentState<EquippedDoll> state = PLAYERS.get(player.getUUID());
        EquippedDoll equipped = state == null ? null : state.snapshot().get(dollId);
        return equipped == null ? List.of() : equipped.stacks();
    }

    /** Called by the equipment bridge, including before Curios commits a changed slot. */
    public static void update(ServerPlayer player, Map<String, EquippedDoll> equipment) {
        EquipmentState<EquippedDoll> state = PLAYERS.computeIfAbsent(
                player.getUUID(), id -> new EquipmentState<>());
        state.replace(equipment, player.getServer().getTickCount(),
                equipped -> equipped.doll().behavior().onEquip(equipped.context(player)),
                equipped -> equipped.doll().behavior().onUnequip(equipped.context(player)));
    }

    private static void refresh(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            update(serverPlayer, CuriosBridge.get().equipment(serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        EquipmentState<EquippedDoll> state = PLAYERS.computeIfAbsent(
                player.getUUID(), id -> new EquipmentState<>());
        if (state.needsRefresh(player.getServer().getTickCount())) {
            refresh(player);
        }
        // Iterate a stable set of types, but use current counts after another behavior changes equipment.
        for (String id : state.snapshot().keySet()) {
            EquippedDoll equipped = state.snapshot().get(id);
            if (equipped != null) {
                equipped.doll().behavior().tick(equipped.context(player));
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        refresh(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refresh(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        forget(event.getOriginal());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        forget(event.getEntity());
    }

    private static void forget(Player player) {
        EquipmentState<EquippedDoll> state = PLAYERS.remove(player.getUUID());
        if (state == null) return;
        if (player instanceof ServerPlayer serverPlayer) {
            state.snapshot().values().forEach(equipped ->
                    equipped.doll().behavior().onUnequip(equipped.context(serverPlayer)));
        }
        state.clear();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PLAYERS.clear();
        DollFlight.clear();
        AE2Bridge.get().clear();
    }
}
