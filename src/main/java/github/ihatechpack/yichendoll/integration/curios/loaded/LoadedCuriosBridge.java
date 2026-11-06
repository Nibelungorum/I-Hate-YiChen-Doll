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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;

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
        if (isFunctionalSlot(slot.identifier()) && !slot.cosmetic() && slot.entity() instanceof ServerPlayer player) {
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
                if (!isFunctionalSlot(entry.getKey())) continue;
                var slots = entry.getValue().getStacks();
                for (int index = 0; index < slots.getSlots(); index++) {
                    if (!inventory.isSlotActive(entry.getKey(), index)) continue;
                    // Curios may call onEquip/onUnequip before the changed stack is visible.
                    ItemStack stack = changed != null && changed.identifier().equals(entry.getKey())
                            && changed.index() == index ? replacement : slots.getStackInSlot(index);
                    if (!(stack.getItem() instanceof ItemDoll item)) continue;
                    var doll = ((BlockDoll) item.getBlock()).get_doll();
                    var stacks = new ArrayList<ItemStack>();
                    if (equipment.containsKey(doll.id())) stacks.addAll(equipment.get(doll.id()).stacks());
                    stacks.add(stack);
                    equipment.put(doll.id(), new EquippedDoll(doll, stacks));
                }
            }
        });
        return equipment;
    }

    private static boolean isFunctionalSlot(String identifier) {
        return "head".equals(identifier) || "doll".equals(identifier);
    }

    @Override
    public List<ItemStack> activeStacks(LivingEntity entity, String dollId) {
        List<ItemStack> result = new ArrayList<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(inventory -> {
            for (var entry : inventory.getCurios().entrySet()) {
                if (!isFunctionalSlot(entry.getKey())) continue;
                var slots = entry.getValue().getStacks();
                for (int index = 0; index < slots.getSlots(); index++) {
                    if (!inventory.isSlotActive(entry.getKey(), index)) continue;
                    ItemStack stack = slots.getStackInSlot(index);
                    if (stack.getItem() instanceof ItemDoll item
                            && ((BlockDoll) item.getBlock()).get_doll().id().equals(dollId)) result.add(stack);
                }
            }
        });
        return result;
    }
}
