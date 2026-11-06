package github.ihatechpack.yichendoll.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only suppresses a vanilla head doll; normal helmets and unrelated headgear keep their rendering. */
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void yichendoll$preferCuriosHead(PoseStack pose, MultiBufferSource buffer, int light,
                                            LivingEntity entity, float limbSwing, float limbSwingAmount,
                                            float partialTicks, float ageInTicks, float netHeadYaw,
                                            float headPitch, CallbackInfo ci) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemDoll
                && CuriosClientBridge.get().hasHeadDoll(entity)) ci.cancel();
    }
}
