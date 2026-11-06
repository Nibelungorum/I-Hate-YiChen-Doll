package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.common.item.EnergyDollItem;
import github.ihatechpack.yichendoll.registry.ModDataComponents;
import github.ihatechpack.yichendoll.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

/** Server-authoritative FE accounting; lists contain the actual equipped item stacks. */
public final class DollEnergy {
    public static final int CAPACITY = 1_000_000;

    private DollEnergy() {}

    public static int stored(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(ModDataComponents.ENERGY.get(), 0), 0, CAPACITY);
    }

    private static void set(ItemStack stack, int amount) {
        stack.set(ModDataComponents.ENERGY.get(), Math.clamp(amount, 0, CAPACITY));
    }

    public static long available(List<ItemStack> stacks) {
        long total = 0;
        for (ItemStack stack : stacks) total += stored(stack);
        return total;
    }

    public static boolean consume(List<ItemStack> stacks, int amount) {
        if (amount < 0 || available(stacks) < amount) return false;
        for (ItemStack stack : stacks) {
            int used = Math.min(stored(stack), amount);
            if (used > 0) set(stack, stored(stack) - used);
            amount -= used;
            if (amount == 0) break;
        }
        return amount == 0;
    }

    public static int protectDurability(ServerPlayer player, ItemStack damaged, int damage) {
        if (damage <= 0) return damage;
        boolean inInventory = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == damaged) {
                inInventory = true;
                break;
            }
        }
        if (!inInventory) return damage;
        List<ItemStack> stacks = DollRuntime.stacks(player, "qingjiao");
        int protectedDamage = (int) Math.min(damage, available(stacks) / 10);
        // Split the payment to avoid overflow for unusually large durability hits.
        long cost = (long) protectedDamage * 10;
        for (ItemStack stack : stacks) {
            int used = (int) Math.min(stored(stack), cost);
            if (used > 0) set(stack, stored(stack) - used);
            cost -= used;
            if (cost == 0) break;
        }
        return damage - protectedDamage;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Item[] items = ModItems.ITEMS.values().stream().map(holder -> holder.get())
                .filter(item -> item instanceof EnergyDollItem).toArray(Item[]::new);
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new IEnergyStorage() {
            @Override
            public int receiveEnergy(int amount, boolean simulate) {
                int received = Math.min(Math.max(0, amount), CAPACITY - stored(stack));
                if (!simulate && received > 0) set(stack, stored(stack) + received);
                return received;
            }

            @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
            @Override public int getEnergyStored() { return stored(stack); }
            @Override public int getMaxEnergyStored() { return CAPACITY; }
            @Override public boolean canExtract() { return false; }
            @Override public boolean canReceive() { return true; }
        }, items);
    }
}
