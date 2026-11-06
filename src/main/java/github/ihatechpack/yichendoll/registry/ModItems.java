package github.ihatechpack.yichendoll.registry;


import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.common.item.MeNetworkDollItem;
import github.ihatechpack.yichendoll.common.item.EnergyDollItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 13:26
 */
public class ModItems {

    private static final DeferredRegister<Item> REGISTER = DeferredRegister.Items.createItems(IHateYiChenDoll.MOD_ID);
    public static final LinkedHashMap<String, DeferredHolder<Item, Item>> ITEMS = new LinkedHashMap<>();

    public static void register(IEventBus bus) {
        DollRegistery.DOLLS.forEach(doll -> {
            ITEMS.put(doll.id(), REGISTER.register(doll.id(), () -> createDoll(doll)));
        });
        // Arrays.stream(Res.howxu).forEach(str -> {
        //     ITEMS.put(str, REGISTER.register(str, () -> new ItemDoll(ModBlocks.BLOCKS.get(str).get(),2,3)));
        // });
        // Arrays.stream(Res.hualeibao).forEach(str -> {
        //     ITEMS.put(str, REGISTER.register(str, () -> new ItemDoll(ModBlocks.BLOCKS.get(str).get(),1,1)));
        // });
        // Arrays.stream(Res.yichens).forEach(str -> {
        //     ITEMS.put(str, REGISTER.register(str, () -> new ItemDoll(ModBlocks.BLOCKS.get(str).get(),2,2)));
        // });
        // Arrays.stream(Res.tech_guys).forEach(str -> {
        //     ITEMS.put(str, REGISTER.register(str, () -> new ItemDoll(ModBlocks.BLOCKS.get(str).get(),1,0)));
        // });
        // Arrays.stream(Res.specials).forEach(str -> {
        //     ITEMS.put(str, REGISTER.register(str, () -> new ItemDoll(ModBlocks.BLOCKS.get(str).get(),2,1)));
        // });
        REGISTER.register(bus);
    }

    private static ItemDoll createDoll(DollRegistery.Doll doll) {
        var block = ModBlocks.BLOCKS.get(doll.id()).get();
        return switch (doll.id()) {
            case "m_xm_", "budai", "budai_see_u", "yuanxi_19" -> new MeNetworkDollItem(block, doll.level());
            case "howxu", "qingjiao" -> new EnergyDollItem(block, doll.level());
            default -> new ItemDoll(block, doll.level());
        };
    }
}
