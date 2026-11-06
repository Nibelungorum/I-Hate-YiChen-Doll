package github.ihatechpack.yichendoll.api.doll;

import github.ihatechpack.yichendoll.registry.DollRegistery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Immutable slot membership with live item data for charging and changing ME bindings. */
public record EquippedDoll(DollRegistery.Doll doll, List<ItemStack> stacks) {
    public EquippedDoll {
        stacks = List.copyOf(stacks);
    }

    public int count() {
        return stacks.size();
    }

    public DollContext context(ServerPlayer player) {
        return new DollContext(player, doll, stacks);
    }
}
