package github.ihatechpack.yichendoll.integration.curios.core;

import github.ihatechpack.yichendoll.api.ModEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CuriosExtension {
    private static final String ID = "curios";
    private static final boolean IS_LOADED;

    static {
        IS_LOADED = ModList.get().isLoaded(ID);
    }

    // 注册和调用提前使用IS_LOADED分离 单独调用这个类不会在Curios没加载时报错Class Not Found

    @OnlyIn(Dist.CLIENT)
    @ModEvent(side = ModEvent.Side.ClientSide)
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        if (!IS_LOADED) return;

        CuriosCompatInner.registerRenderer(event);
    }

    @ModEvent(side = ModEvent.Side.ServerSide)
    public static void registerDollCapabilities(final RegisterCapabilitiesEvent event) {
        if (!IS_LOADED) return;

        CuriosCompatInner.registerDollItemCurioTick(event);
    }

    public static void refreshMXmDoll(Player player) {
        if (!IS_LOADED) return;

        CuriosCompatInner.refreshMXmDoll(player, null, ItemStack.EMPTY);
    }

    @ModEvent(side = ModEvent.Side.ServerSide)
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (!IS_LOADED) return;

        CuriosCompatInner.registerDollItemPredicate();
    }
}
