package github.ihatechpack.yichendoll.registry;


import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
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
            ITEMS.put(doll.id(), REGISTER.register(doll.id(),()-> new ItemDoll(ModBlocks.BLOCKS.get(doll.id()).get(),doll.level())));
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
}
