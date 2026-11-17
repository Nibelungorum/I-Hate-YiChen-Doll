package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.client.render.shield.BudaiShieldLayer;
import github.ihatechpack.yichendoll.client.render.wings.CosmicWingsLayer;
import github.ihatechpack.yichendoll.client.render.wings.QuantumWingsLayer;
import github.ihatechpack.yichendoll.client.shader.BudaiShieldShaders;
import github.ihatechpack.yichendoll.client.shader.CosmicWingsShaders;
import github.ihatechpack.yichendoll.client.shader.QuantumWingsShaders;
import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import github.ihatechpack.yichendoll.common.doll.DollTooltips;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.common.Mod;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 14:31
 */
@Mod(value = IHateYiChenDoll.MOD_ID,dist = Dist.CLIENT)
public class IHateYiChenDollClient {

    public IHateYiChenDollClient(IEventBus modBus) {
        DollTooltips.setShiftDown(Screen::hasShiftDown);
        DollFlight.setClientBoosting(DollFlightInput::isBoosting);
        modBus.addListener(CuriosClientBridge.get()::addEntityLayers);
        modBus.addListener(BudaiShieldLayer::register);
        modBus.addListener(CosmicWingsLayer::register);
        modBus.addListener(QuantumWingsLayer::register);
        modBus.addListener(BudaiShieldShaders::registerShaders);
        modBus.addListener(CosmicWingsShaders::registerShaders);
        modBus.addListener(QuantumWingsShaders::registerShaders);
        modBus.addListener(QuantumWingsShaders::registerAtlas);
        modBus.addListener(CosmicWingsShaders::onAtlasStitched);
    }

}
