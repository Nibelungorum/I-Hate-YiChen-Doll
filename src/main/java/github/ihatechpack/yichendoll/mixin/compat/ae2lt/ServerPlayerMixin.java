package github.ihatechpack.yichendoll.mixin.compat.ae2lt;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import github.ihatechpack.yichendoll.integration.ae2lt.CelestweaveBypass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @WrapMethod(method = "hurt")
    private boolean yichendoll$scopeDamage(DamageSource source, float amount, Operation<Boolean> original) {
        return CelestweaveBypass.duringDamage((ServerPlayer) (Object) this, source, () -> original.call(source, amount));
    }

    @WrapMethod(method = "die")
    private void yichendoll$scopeDeath(DamageSource source, Operation<Void> original) {
        CelestweaveBypass.duringDeath((ServerPlayer) (Object) this, source, () -> original.call(source));
    }
}
