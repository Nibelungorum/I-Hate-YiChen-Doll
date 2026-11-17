package github.ihatechpack.yichendoll.client.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;

/** Binds the wing mask, animated glint and quantum star atlas. */
public final class QuantumWingsTextureStateShard extends RenderStateShard.TextureStateShard {
    private static final ResourceLocation GLINT = Res.rl("textures/shaders/quantum_wings/glint.png");

    public QuantumWingsTextureStateShard(ResourceLocation mask) {
        super(mask, false, false);
    }

    @Override
    public void setupRenderState() {
        super.setupRenderState();
        var shader = QuantumWingsShaders.shader();
        // Cover the full wing silhouette with the animated quantum star layers.
        shader.getUniform("RenderStars").set(1);
        shader.getUniform("StarScale").set(new float[]{1.0F, 0.5F});
        RenderSystem.setShaderTexture(1, GLINT);
        RenderSystem.setShaderTexture(2, QuantumWingsShaders.STAR_ATLAS);

        var atlas = Minecraft.getInstance().getTextureAtlas(QuantumWingsShaders.STAR_ATLAS);
        for (int i = 1; i <= 4; i++) {
            var sprite = atlas.apply(Res.rl("shaders/quantum_wings/" + i));
            shader.getUniform("QuantumStarUV" + i).setSafe(
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
        }
    }
}
