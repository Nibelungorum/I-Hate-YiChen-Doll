package github.ihatechpack.yichendoll.integration.curios.client.loaded;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** One visible Curios head doll, preferred over a doll in the vanilla helmet slot. */
public class DollItemRenderer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;

    public DollItemRenderer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    public static ItemStack headDoll(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).map(inventory -> {
            var head = inventory.getCurios().get("head");
            if (head == null || !head.isVisible()) return ItemStack.EMPTY;
            for (int i = 0; i < head.getStacks().getSlots(); i++) {
                if (!inventory.isSlotActive("head", i) || i >= head.getRenders().size()
                        || !head.getRenders().get(i)) continue;
                ItemStack stack = head.getCosmeticStacks().getStackInSlot(i);
                if (stack.isEmpty()) stack = head.getStacks().getStackInSlot(i);
                if (stack.getItem() instanceof ItemDoll) return stack;
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = headDoll(entity);
        if (stack.isEmpty()) return;
        pose.pushPose();
        this.getParentModel().getHead().translateAndRotate(pose);
        pose.translate(0, -0.25, 0);
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.625F, -0.625F, -0.625F);
        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, pose, buffer, light);
        pose.popPose();
    }
}
