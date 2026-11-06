package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Adds the charged doll as a glide source without replacing vanilla elytra behavior. */
@Mixin(Player.class)
public abstract class PlayerFlightMixin {
    @ModifyExpressionValue(method = "tryToStartFallFlying", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean yichendoll$canGlide(boolean original) {
        return original || DollFlight.canGlide((Player) (Object) this);
    }
}
