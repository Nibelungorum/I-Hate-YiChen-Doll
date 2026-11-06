package github.ihatechpack.yichendoll.mixin.lightningtech;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import github.ihatechpack.yichendoll.common.MXmDollManager;
import github.ihatechpack.yichendoll.integration.lightningtech.CelestweaveBypass;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.moakiee.ae2lt.logic.railgun.OverloadExecutionService", remap = false)
public abstract class OverloadExecutionServiceMixin {
    @WrapMethod(method = "onHit")
    private static void yichendoll$scopeExecution(ServerLevel level, ServerPlayer player, ItemStack stack,
                                                 LivingEntity target, double damage, Operation<Void> original) {
        if (MXmDollManager.isWearing(target)) return;
        CelestweaveBypass.duringAttack(target, MXmDollManager.isWearing(player),
                () -> original.call(level, player, stack, target, damage));
    }
}
