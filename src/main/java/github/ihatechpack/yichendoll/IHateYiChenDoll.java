package github.ihatechpack.yichendoll;

import com.mojang.logging.LogUtils;
import github.ihatechpack.yichendoll.registry.ModCreativeTab;
import github.ihatechpack.yichendoll.registry.ModSounds;
import github.ihatechpack.yichendoll.registry.ModBlocks;
import github.ihatechpack.yichendoll.registry.ModItems;
import github.ihatechpack.yichendoll.datagen.DataGenRegistration;
import github.ihatechpack.yichendoll.integration.curios.core.CuriosExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 13:08
 */
@Mod(IHateYiChenDoll.MOD_ID)
public final class IHateYiChenDoll {
    public static final String MOD_ID = "ihateyichendoll";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static IHateYiChenDoll instance;

    public IHateYiChenDoll(IEventBus modBus){
        // single instance mode
        instance = this;
        // common reg
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModCreativeTab.REGISTER.register(modBus);
        ModSounds.REGISTER.register(modBus);
        DataGenRegistration.init(modBus);
        // registry jade
        // registry curios
        modBus.addListener(CuriosExtension::commonSetup);
        modBus.addListener(CuriosExtension::registerDollCapabilities);
    }
}
