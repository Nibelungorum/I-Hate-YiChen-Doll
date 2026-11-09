package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Vanilla checks the chest item here before it ever calls Player.tryToStartFallFlying. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerFlightMixin {
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean yichendoll$canGlide(boolean original) {
        return original || DollFlight.canGlide((LocalPlayer) (Object) this);
    }
}
