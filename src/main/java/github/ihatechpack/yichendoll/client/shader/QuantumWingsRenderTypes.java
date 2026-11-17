package github.ihatechpack.yichendoll.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Dyeable base and animated quantum overlay, both excluded from entity outlines. */
public final class QuantumWingsRenderTypes {
    private static final ResourceLocation BASE_TEXTURE = Res.rl("textures/entity/quantum_wings.png");
    private static final ResourceLocation MASK_TEXTURE = Res.rl("textures/entity/quantum_wings_mask.png");

    public static final RenderType BASE = RenderType.create(
            Res.rl("quantum_wings_base").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(QuantumWingsShaders::baseShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(BASE_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.CULL)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    public static final RenderType QUANTUM = RenderType.create(
            Res.rl("quantum_wings_overlay").toString(), DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 1536, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(QuantumWingsShaders::shader))
                    .setTextureState(new QuantumWingsTextureStateShard(MASK_TEXTURE))
                    .setCullState(RenderStateShard.CULL)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .createCompositeState(false));

    private QuantumWingsRenderTypes() {}
}
