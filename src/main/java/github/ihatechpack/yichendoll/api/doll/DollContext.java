package github.ihatechpack.yichendoll.api.doll;

import github.ihatechpack.yichendoll.registry.DollRegistery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Callback context. The stack is a copy, not a live Curios inventory slot. */
public record DollContext(ServerPlayer player, DollRegistery.Doll doll, ItemStack stack, int count) {}
