package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Only skips the conflicting head doll's draw call; the rest of the layer still runs. */
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void yichendoll$preferCuriosHead(ItemInHandRenderer renderer, LivingEntity entity, ItemStack stack,
                                           ItemDisplayContext displayContext, boolean leftHand, PoseStack pose,
                                           MultiBufferSource buffer, int light, Operation<Void> original) {
        if (!(stack.getItem() instanceof ItemDoll) || !CuriosClientBridge.get().hasHeadDoll(entity)) {
            original.call(renderer, entity, stack, displayContext, leftHand, pose, buffer, light);
        }
    }
}
