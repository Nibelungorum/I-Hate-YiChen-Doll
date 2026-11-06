package github.ihatechpack.yichendoll.integration.jade.loaded;

import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.common.block.BlockDoll;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.LinkedList;
import java.util.List;

public enum BlockComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation ID = Res.rl("doll_block");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig pluginConfig) {
        if (accessor.getBlock() instanceof BlockDoll blockDoll) {
            // int it = ((ItemDoll)ModItems.ITEMS.get(blockDoll.getReg_name()).get()).getTooltip_size();
            // ChatFormatting formatting = switch (((ItemDoll)ModItems.ITEMS.get(blockDoll.getReg_name()).get()).getRarity()){
            //     case 1 -> ChatFormatting.AQUA;
            //     case 2 -> ChatFormatting.GREEN;
            //     case 3 -> ChatFormatting.GOLD;
            //     default -> ChatFormatting.LIGHT_PURPLE;
            // };
            // for (int i = 0; i < it; i++) {
            //     tooltip.add(Component.translatable("item."+ IHateYiChenDoll.MOD_ID+".doll_tip."+ blockDoll.getReg_name()+"."+i).withStyle(formatting));
            // }
            List<Component> inner = new LinkedList<>();
            blockDoll.get_doll().appendHoverText(inner);
            tooltip.addAll(inner);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
