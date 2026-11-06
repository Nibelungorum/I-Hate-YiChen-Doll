package github.ihatechpack.yichendoll.mixin.compat.ae2lt;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.integration.ae2lt.CelestweaveBypass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** The bypass context and death state belong to server players, never every LivingEntity. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements CelestweaveBypass.DeathState {
    @Unique private boolean yichendoll$bypassedDeath;
    @Unique private boolean yichendoll$deathAccepted;

    @Override
    public boolean yichendoll$hasDied() {
        return yichendoll$deathAccepted;
    }

    @Override
    public boolean yichendoll$isBypassedDeath() {
        return yichendoll$bypassedDeath;
    }

    @Override
    public void yichendoll$setBypassedDeath(boolean bypassed) {
        yichendoll$bypassedDeath = bypassed;
    }

    @WrapMethod(method = "hurt")
    private boolean yichendoll$scopeDamage(DamageSource source, float amount, Operation<Boolean> original) {
        return CelestweaveBypass.duringDamage((ServerPlayer) (Object) this, source, () -> original.call(source, amount));
    }

    @WrapMethod(method = "die")
    private void yichendoll$scopeDeath(DamageSource source, Operation<Void> original) {
        yichendoll$deathAccepted = false;
        CelestweaveBypass.duringDeath((ServerPlayer) (Object) this, source, () -> original.call(source));
    }

    @WrapOperation(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/common/CommonHooks;onLivingDeath(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean yichendoll$deathDecision(LivingEntity entity, DamageSource source, Operation<Boolean> original) {
        boolean canceled = original.call(entity, source);
        yichendoll$deathAccepted = !canceled;
        return canceled;
    }
}
