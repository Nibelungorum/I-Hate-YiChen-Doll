package github.ihatechpack.yichendoll.integration.jade.loaded;

import github.ihatechpack.yichendoll.common.block.BlockDoll;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ModPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BlockComponentProvider.INSTANCE, BlockDoll.class);
        // registration.registerEntityComponent(EntityComponentProvider.INSTANCE, DollEntity.class);
    }
}
