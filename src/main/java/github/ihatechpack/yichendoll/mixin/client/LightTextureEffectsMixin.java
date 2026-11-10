package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureEffectsMixin {
    @ModifyReturnValue(method = "getDarknessGamma", at = @At("RETURN"))
    private float yichendoll$ignoreDarkness(float original) {
        return DollEffectProtection.isWearing(Minecraft.getInstance().player, "yuanxi_19_new") ? 0 : original;
    }
}
