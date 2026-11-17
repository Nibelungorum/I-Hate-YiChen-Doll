package github.ihatechpack.yichendoll.client.render.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import github.ihatechpack.yichendoll.client.shader.CosmicWingsRenderTypes;
import github.ihatechpack.yichendoll.client.shader.CosmicWingsShaders;
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
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renders the cosmic wings independently of armor or flight state. */
public final class CosmicWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart body;

    public CosmicWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.body = createLayer().bakeRoot().getChild("body");
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        for (var model : event.getSkins()) {
            if (event.getSkin(model) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new CosmicWingsLayer(renderer));
            }
        }
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var body = mesh.getRoot().addOrReplaceChild("body", new CubeListBuilder(), PartPose.ZERO);
        CubeDeformation deformation = new CubeDeformation(0.0F);
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(0.0F, -11.6F, 0.0F, 0.0F, 32.0F, 32.0F, deformation),
                PartPose.offsetAndRotation(-1.5F, 0.0F, 2.0F,
                        0.0F, (float) (Math.PI * 0.4), 0.0F));
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(0.0F, -11.6F, 0.0F, 0.0F, 32.0F, 32.0F, deformation),
                PartPose.offsetAndRotation(1.5F, 0.0F, 2.0F,
                        0.0F, (float) (-Math.PI * 0.4), 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        for (ItemStack stack : CuriosBridge.get().activeStacks(player, "m_xm_free")) {
            if (DollWings.isVisible(stack)) return;
        }

        ItemStack wingSource = ItemStack.EMPTY;
        for (ItemStack stack : CuriosBridge.get().activeStacks(player, "m_xm_")) {
            if (DollWings.isVisible(stack)) {
                wingSource = stack;
                break;
            }
        }
        if (wingSource.isEmpty()) return;

        int themeColor = DollWings.themeColor(wingSource);
        int red = Math.min(255, 38 + ((themeColor >> 16) & 0xFF));
        int green = Math.min(255, 38 + ((themeColor >> 8) & 0xFF));
        int blue = Math.min(255, 38 + (themeColor & 0xFF));
        themeColor = (red << 16) | (green << 8) | blue;

        body.copyFrom(this.getParentModel().body);

        CosmicWingsShaders.updateUniforms(player, themeColor);
        long time = player.level().getGameTime();
        double pulse = Math.sin(time / 10.0D) * 0.5D + 0.5D;
        double pulseMagnitude = pulse * pulse * pulse * pulse * pulse * pulse;
        int glowColor = FastColor.ARGB32.color((int) (pulseMagnitude * 0.5D * 255),
                (themeColor >> 16) & 0xFF, (themeColor >> 8) & 0xFF, themeColor & 0xFF);

        pose.pushPose();
        VertexConsumer base = buffers.getBuffer(CosmicWingsRenderTypes.BASE);
        int baseColor = 0xFF000000 | themeColor;
        body.render(pose, base, light, OverlayTexture.NO_OVERLAY, baseColor);

        VertexConsumer cosmic = CosmicWingsShaders.wingMask.wrap(
                buffers.getBuffer(CosmicWingsRenderTypes.COSMIC));
        body.render(pose, cosmic, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        VertexConsumer glow = buffers.getBuffer(CosmicWingsRenderTypes.GLOW);
        body.render(pose, glow, light, OverlayTexture.NO_OVERLAY, glowColor);
        pose.popPose();
    }
}
