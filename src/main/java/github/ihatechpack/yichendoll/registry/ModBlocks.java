package github.ihatechpack.yichendoll.registry;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.common.block.BlockDoll;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 13:11
 */
public class ModBlocks {
    private static final DeferredRegister<Block> REGISTER = DeferredRegister.Blocks.createBlocks(IHateYiChenDoll.MOD_ID);
    public static final LinkedHashMap<String, DeferredHolder<Block,Block>> BLOCKS = new LinkedHashMap<>();

    public static void register(IEventBus bus) {

        DollRegistery.DOLLS.forEach(doll -> {
            BLOCKS.put(doll.id(), REGISTER.register(doll.id(), () -> new BlockDoll(doll)));
        });

        // Arrays.stream(Res.howxu).forEach(str -> {
        //     BLOCKS.put(str,REGISTER.register(str,() -> new BlockDoll(str)));
        // });
        // Arrays.stream(Res.hualeibao).forEach(str -> {
        //     BLOCKS.put(str,REGISTER.register(str,() -> new BlockDoll(str)));
        // });
        // Arrays.stream(Res.yichens).forEach(str -> {
        //     BLOCKS.put(str,REGISTER.register(str,() -> new BlockDoll(str)));
        // });
        // Arrays.stream(Res.tech_guys).forEach(str -> {
        //     BLOCKS.put(str,REGISTER.register(str,() -> new BlockDoll(str)));
        // });
        // Arrays.stream(Res.specials).forEach(str -> {
        //     BLOCKS.put(str,REGISTER.register(str,() -> new BlockDoll(str)));
        // });
        REGISTER.register(bus);
    }
}
