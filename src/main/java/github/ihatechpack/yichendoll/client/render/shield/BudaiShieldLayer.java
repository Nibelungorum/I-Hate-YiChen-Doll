package github.ihatechpack.yichendoll.client.render.shield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import github.ihatechpack.yichendoll.client.shader.BudaiShieldRenderTypes;
import github.ihatechpack.yichendoll.client.shader.BudaiShieldShaders;
import github.ihatechpack.yichendoll.common.item.BudaiDollItem;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draws only Budai's animated outer energy layer; no armor or reactor geometry. */
public final class BudaiShieldLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final PlayerModel<AbstractClientPlayer> model;

    public BudaiShieldLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                            boolean slim) {
        super(renderer);
        this.model = new PlayerModel<>(PlayerModel.createMesh(new CubeDeformation(0.75F), slim)
                .getRoot().bake(64, 64), slim);
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new BudaiShieldLayer(renderer, skin.equals("slim")));
            }
        }
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        boolean visible = false;
        for (ItemStack stack : CuriosBridge.get().activeStacks(player, "budai")) {
            if (BudaiDollItem.shieldVisible(stack)) {
                visible = true;
                break;
            }
        }
        if (!visible) return;

        ClientHooks.copyModelProperties(this.getParentModel(), this.model);
        BudaiShieldShaders.updateUniforms(player, partialTicks);
        VertexConsumer consumer = buffers.getBuffer(BudaiShieldRenderTypes.ENERGY);
        this.model.renderToBuffer(pose, consumer, light,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F), 0xFFFFFFFF);
    }
}
