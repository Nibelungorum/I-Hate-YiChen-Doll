package github.ihatechpack.yichendoll.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import github.ihatechpack.yichendoll.Res;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import java.io.IOException;

/** Shader instance and block-atlas sprites used only by the cosmic wings. */
public final class CosmicWingsShaders {
    private static final int COSMIC_SPRITE_COUNT = 10;
    private static final float[] COSMIC_UVS = new float[COSMIC_SPRITE_COUNT * 4];

    private static ShaderInstance shader;
    private static AbstractUniform time;
    private static AbstractUniform yaw;
    private static AbstractUniform pitch;
    private static AbstractUniform externalScale;
    private static AbstractUniform opacity;
    private static AbstractUniform themeColor;
    private static AbstractUniform cosmicUVs;

    public static TextureAtlasSprite wingMask;

    private CosmicWingsShaders() {}

    static ShaderInstance shader() {
        return shader;
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    Res.rl("cosmic_wings"), DefaultVertexFormat.NEW_ENTITY), instance -> {
                shader = instance;
                time = shader.safeGetUniform("time");
                yaw = shader.safeGetUniform("yaw");
                pitch = shader.safeGetUniform("pitch");
                externalScale = shader.safeGetUniform("externalScale");
                opacity = shader.safeGetUniform("opacity");
                themeColor = shader.safeGetUniform("themeColor");
                cosmicUVs = shader.safeGetUniform("cosmicuvs");
                shader.apply();
            });
        } catch (IOException exception) {
            throw new RuntimeException("Failed to register cosmic wings shader", exception);
        }
    }

    public static void onAtlasStitched(TextureAtlasStitchedEvent event) {
        if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) return;
        for (int i = 0; i < COSMIC_SPRITE_COUNT; i++) {
            TextureAtlasSprite sprite = event.getAtlas().getSprite(Res.rl("misc/cosmic/cosmic_" + i));
            COSMIC_UVS[i * 4] = sprite.getU0();
            COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }
        wingMask = event.getAtlas().getSprite(Res.rl("entity/cosmic_wings_mask"));
    }

    public static void updateUniforms(AbstractClientPlayer player, int rgb) {
        time.set(player.level().getGameTime() % Integer.MAX_VALUE);
        yaw.set((float) (player.getYRot() * Math.PI / 180.0D));
        pitch.set((float) (-player.getXRot() * Math.PI / 180.0D));
        externalScale.set(1.0F);
        opacity.set(1.25F);
        themeColor.set(((rgb >> 16) & 0xFF) / 255.0F,
                ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
        cosmicUVs.set(COSMIC_UVS);
    }
}
