package github.ihatechpack.yichendoll.mixin.compat.ae2lt;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import github.ihatechpack.yichendoll.common.doll.behavior.MXmFreeDollBehavior;
import github.ihatechpack.yichendoll.integration.ae2lt.CelestweaveBypass;
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
        if (MXmFreeDollBehavior.isWearing(target)) return;
        CelestweaveBypass.duringAttack(target, MXmFreeDollBehavior.isWearing(player),
                () -> original.call(level, player, stack, target, damage));
    }
}
