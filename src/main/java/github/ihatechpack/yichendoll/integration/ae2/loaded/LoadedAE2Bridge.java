package github.ihatechpack.yichendoll.integration.ae2.loaded;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import github.ihatechpack.yichendoll.common.item.MeNetworkDollItem;
import github.ihatechpack.yichendoll.integration.ae2.AE2Bridge;
import github.ihatechpack.yichendoll.integration.ae2.MeNetworkStats;
import github.ihatechpack.yichendoll.integration.appflux.AppliedFluxBridge;
import github.ihatechpack.yichendoll.registry.ModDataComponents;
import github.ihatechpack.yichendoll.registry.ModItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

/** Native AP binding and one bounded-cost statistics refresh per grid, not per doll/player. */
public final class LoadedAE2Bridge implements AE2Bridge {
    private record Cached(long tick, MeNetworkStats stats) {}
    private final Map<IGrid, Cached> snapshots = new WeakHashMap<>();

    @Override
    public void initialize() {
        IGridLinkableHandler handler = new IGridLinkableHandler() {
            @Override public boolean canLink(ItemStack stack) {
                return stack.getItem() instanceof MeNetworkDollItem;
            }

            @Override public void link(ItemStack stack, GlobalPos pos) {
                stack.set(ModDataComponents.ME_LINK.get(), pos);
            }

            @Override public void unlink(ItemStack stack) {
                stack.remove(ModDataComponents.ME_LINK.get());
            }
        };
        ModItems.ITEMS.values().forEach(holder -> {
            if (holder.get() instanceof MeNetworkDollItem) GridLinkables.register(holder.get(), handler);
        });
    }

    private IGrid resolveGrid(ServerPlayer player, ItemStack stack) {
        GlobalPos link = stack.get(ModDataComponents.ME_LINK.get());
        if (link == null) return null;
        var level = player.getServer().getLevel(link.dimension());
        if (level == null || !level.hasChunkAt(link.pos())
                || !(level.getBlockEntity(link.pos()) instanceof IWirelessAccessPoint accessPoint)
                || !accessPoint.isActive()) return null;
        IGrid grid = accessPoint.getGrid();
        if (grid == null || grid.isEmpty() || !grid.getEnergyService().isNetworkPowered()) return null;
        return grid;
    }

    @Override
    public boolean consumeFE(ServerPlayer player, ItemStack stack, long amount) {
        if (amount <= 0) return false;
        IGrid grid = resolveGrid(player, stack);
        return grid != null && AppliedFluxBridge.get().consumeFE(
                grid.getStorageService().getInventory(), player, amount);
    }

    @Override
    public MeNetworkStats sample(ServerPlayer player, ItemStack stack) {
        IGrid grid = resolveGrid(player, stack);
        if (grid == null) return MeNetworkStats.EMPTY;
        long tick = player.getServer().getTickCount();
        Cached previous = snapshots.get(grid);
        if (previous != null && tick >= previous.tick() && tick / 40 == previous.tick() / 40) return previous.stats();

        var available = grid.getStorageService().getCachedInventory();
        int types = 0;
        for (var entry : available) {
            if (entry.getLongValue() > 0 && types < Integer.MAX_VALUE) types++;
        }
        double usage = grid.getEnergyService().getAvgPowerUsage();
        if (!Double.isFinite(usage)) usage = usage > 0 ? Integer.MAX_VALUE : 0;
        MeNetworkStats stats = new MeNetworkStats(Math.clamp(usage, 0, Integer.MAX_VALUE),
                types, Math.max(0, grid.size()), AppliedFluxBridge.get().storedFE(available));
        snapshots.put(grid, new Cached(tick, stats));
        return stats;
    }

    @Override
    public void clear() {
        snapshots.clear();
    }
}
