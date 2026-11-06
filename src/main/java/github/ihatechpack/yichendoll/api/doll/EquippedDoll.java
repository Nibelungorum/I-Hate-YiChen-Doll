package github.ihatechpack.yichendoll.api.doll;

import github.ihatechpack.yichendoll.registry.DollRegistery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Aggregated active slots for one doll type; never retains a player entity. */
public record EquippedDoll(DollRegistery.Doll doll, ItemStack stack, int count) {
    public EquippedDoll {
        stack = stack.copy();
    }

    @Override
    public ItemStack stack() {
        return stack.copy();
    }

    public DollContext context(ServerPlayer player) {
        return new DollContext(player, doll, stack(), count);
    }
}
