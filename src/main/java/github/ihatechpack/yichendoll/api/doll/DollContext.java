package github.ihatechpack.yichendoll.api.doll;

import github.ihatechpack.yichendoll.registry.DollRegistery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Server-thread callback view. Stacks are live equipped items; never retain this context across ticks. */
public record DollContext(ServerPlayer player, DollRegistery.Doll doll, List<ItemStack> stacks) {
    public DollContext {
        stacks = List.copyOf(stacks);
    }

    public ItemStack stack() {
        return stacks.getFirst();
    }

    public int count() {
        return stacks.size();
    }
}
