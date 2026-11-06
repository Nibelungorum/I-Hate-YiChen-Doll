package github.ihatechpack.yichendoll.mixin.compat.ae2lt;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import github.ihatechpack.yichendoll.integration.ae2lt.CelestweaveBypass;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CelestweaveBypass.DeathState {
    @Unique
    private boolean yichendoll$bypassedDeath;

    @Override
    @Accessor("dead")
    public abstract boolean yichendoll$hasDied();

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
        return CelestweaveBypass.duringDamage((LivingEntity) (Object) this, source, () -> original.call(source, amount));
    }

    @WrapMethod(method = "die")
    private void yichendoll$scopeDeath(DamageSource source, Operation<Void> original) {
        CelestweaveBypass.duringDeath((LivingEntity) (Object) this, source, () -> original.call(source));
    }
}
