package github.ihatechpack.yichendoll.integration.curios.loaded;

import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.api.doll.EquippedDoll;
import github.ihatechpack.yichendoll.common.block.BlockDoll;
import github.ihatechpack.yichendoll.common.doll.DollRuntime;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import github.ihatechpack.yichendoll.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.LinkedHashMap;
import java.util.Map;

/** Curios-only adapter: slot filtering and callbacks, with no doll-specific effects. */
public final class LoadedCuriosBridge implements CuriosBridge {
    @Override
    public void commonSetup() {
        CuriosApi.registerCurioPredicate(Res.rl("item_doll"),
                slotResult -> slotResult.stack().getItem() instanceof ItemDoll);
    }

    @Override
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        Item[] dolls = ModItems.ITEMS.values().stream().map(holder -> holder.get()).toArray(Item[]::new);
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void onEquip(SlotContext slotContext, ItemStack previous) {
                ICurio.super.onEquip(slotContext, previous);
                changed(slotContext, stack);
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack replacement) {
                ICurio.super.onUnequip(slotContext, replacement);
                changed(slotContext, replacement);
            }
        }, dolls);
    }

    private void changed(SlotContext slot, ItemStack replacement) {
        if (!slot.cosmetic() && slot.entity() instanceof ServerPlayer player) {
            DollRuntime.update(player, equipment(player, slot, replacement));
        }
    }

    @Override
    public Map<String, EquippedDoll> equipment(ServerPlayer player) {
        return equipment(player, null, ItemStack.EMPTY);
    }

    private Map<String, EquippedDoll> equipment(ServerPlayer player, SlotContext changed, ItemStack replacement) {
        Map<String, EquippedDoll> equipment = new LinkedHashMap<>();
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            for (var entry : inventory.getCurios().entrySet()) {
                var slots = entry.getValue().getStacks();
                for (int index = 0; index < slots.getSlots(); index++) {
                    if (!inventory.isSlotActive(entry.getKey(), index)) continue;
                    // Curios may call onEquip/onUnequip before the changed stack is visible.
                    ItemStack stack = changed != null && changed.identifier().equals(entry.getKey())
                            && changed.index() == index ? replacement : slots.getStackInSlot(index);
                    if (!(stack.getItem() instanceof ItemDoll item)) continue;
                    var doll = ((BlockDoll) item.getBlock()).get_doll();
                    equipment.compute(doll.id(), (k, previous) -> new EquippedDoll(doll, stack,
                            previous == null ? 1 : previous.count() + 1));
                }
            }
        });
        return equipment;
    }
}
