package github.ihatechpack.yichendoll.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/** Owns the animated shader used only by Budai's energy shield. */
public final class BudaiShieldShaders {
    private static ShaderInstance shader;
    private static AbstractUniform time;

    private BudaiShieldShaders() {}

    static ShaderInstance shader() {
        return shader;
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    Res.rl("budai_shield"), DefaultVertexFormat.NEW_ENTITY), instance -> {
                shader = instance;
                time = shader.safeGetUniform("Time");
                shader.apply();
            });
        } catch (IOException exception) {
            throw new RuntimeException("Failed to register Budai shield shader", exception);
        }
    }

    public static void updateUniforms(AbstractClientPlayer player, float partialTicks) {
        time.set((player.level().getGameTime() + partialTicks) / 20.0F);
    }
}
