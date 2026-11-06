package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.common.doll.DollFlight;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.item.ItemStack;

/** Like DE's armor layer, exposes the vanilla elytra model without requiring a chest elytra. */
public final class DollElytraLayer extends ElytraLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public DollElytraLayer(PlayerRenderer renderer, EntityModelSet models) {
        super(renderer, models);
    }

    @Override
    public boolean shouldRender(ItemStack stack, AbstractClientPlayer player) {
        return !super.shouldRender(stack, player) && !stack.canElytraFly(player) && DollFlight.canGlide(player);
    }
}
