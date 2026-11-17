package github.ihatechpack.yichendoll.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterMaterialAtlasesEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/** Registers the quantum shader used by the wing overlay. */
public final class QuantumWingsShaders {
    static final ResourceLocation STAR_ATLAS = Res.rl("textures/atlas/quantum_wings.png");

    private static ShaderInstance shader;
    private static ShaderInstance baseShader;

    private QuantumWingsShaders() {}

    static ShaderInstance shader() {
        return shader;
    }

    static ShaderInstance baseShader() {
        return baseShader;
    }

    public static void registerAtlas(RegisterMaterialAtlasesEvent event) {
        event.register(STAR_ATLAS, Res.rl("quantum_wings"));
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    Res.rl("quantum_wings"), DefaultVertexFormat.NEW_ENTITY),
                    instance -> shader = instance);
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    Res.rl("quantum_wings_base"), DefaultVertexFormat.NEW_ENTITY),
                    instance -> baseShader = instance);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to register quantum wings shader", exception);
        }
    }
}
