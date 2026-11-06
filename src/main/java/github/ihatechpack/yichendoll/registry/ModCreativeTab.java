package github.ihatechpack.yichendoll.registry;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/12 13:18
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IHateYiChenDoll.MOD_ID);

    public static DeferredHolder<CreativeModeTab,CreativeModeTab> MOD_TAB = REGISTER.register(IHateYiChenDoll.MOD_ID, () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.ihateyichendoll.name"))
            .icon(() -> BuiltInRegistries.ITEM.get(Res.rl("howxu")).getDefaultInstance())
            .displayItems((par, output) -> {
                ModItems.ITEMS.forEach((_s,itemDeferredHolder) -> {output.accept(itemDeferredHolder.get());});
            }).build());

}
