package github.ihatechpack.yichendoll.client.render.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import github.ihatechpack.yichendoll.client.shader.QuantumWingsRenderTypes;
import github.ihatechpack.yichendoll.common.doll.DollWings;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Quantum nano-style wings with a dyeable base and animated quantum overlay. */
public final class QuantumWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart body;

    public QuantumWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.body = createLayer().bakeRoot().getChild("body");
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        for (var model : event.getSkins()) {
            if (event.getSkin(model) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new QuantumWingsLayer(renderer));
            }
        }
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var body = mesh.getRoot().addOrReplaceChild("body", new CubeListBuilder(), PartPose.ZERO);
        CubeDeformation deformation = new CubeDeformation(0.0F);
        float wingRotation = 0.2F;
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -12.0F, 3.5F, 32.0F, 32.0F, 0.0F, deformation),
                PartPose.rotation(0.0F, -wingRotation, 0.0F));
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -12.0F, 3.5F, -32.0F, 32.0F, 0.0F, deformation),
                PartPose.rotation(0.0F, wingRotation, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack wingSource = ItemStack.EMPTY;
        for (ItemStack stack : CuriosBridge.get().activeStacks(player, "m_xm_free")) {
            if (DollWings.isVisible(stack)) {
                wingSource = stack;
                break;
            }
        }
        if (wingSource.isEmpty()) return;

        body.copyFrom(this.getParentModel().body);
        int color = 0xFF000000 | DollWings.themeColor(wingSource);
        pose.pushPose();
        body.render(pose, buffers.getBuffer(QuantumWingsRenderTypes.BASE),
                light, OverlayTexture.NO_OVERLAY, color);
        body.render(pose, buffers.getBuffer(QuantumWingsRenderTypes.QUANTUM),
                light, OverlayTexture.NO_OVERLAY, color);
        pose.popPose();
    }
}
