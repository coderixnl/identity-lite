package nl.coderix.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import nl.coderix.IMorphedPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
    private void identityLite$extractMorphState(Entity entity, float f, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof IMorphedPlayer morphedPlayer) {
            LivingEntity morph = morphedPlayer.getMorphEntity();
            if (morph != null) {
                // Sync properties for rendering
                morph.tickCount = entity.tickCount;
                morph.setPos(entity.getX(), entity.getY(), entity.getZ());
                morph.xo = entity.xo;
                morph.yo = entity.yo;
                morph.zo = entity.zo;
                morph.xOld = entity.xOld;
                morph.yOld = entity.yOld;
                morph.zOld = entity.zOld;
                morph.setYRot(entity.getYRot());
                morph.yRotO = entity.yRotO;
                morph.setXRot(entity.getXRot());
                morph.xRotO = entity.xRotO;
                
                if (entity instanceof LivingEntity livingEntity) {
                    morph.yBodyRot = livingEntity.yBodyRot;
                    morph.yBodyRotO = livingEntity.yBodyRotO;
                    morph.yHeadRot = livingEntity.yHeadRot;
                    morph.yHeadRotO = livingEntity.yHeadRotO;
                    morph.attackAnim = livingEntity.attackAnim;
                    morph.swinging = livingEntity.swinging;
                }

                // Extract and return the morph state instead of the player state
                EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
                EntityRenderState state = dispatcher.extractEntity(morph, f);
                cir.setReturnValue(state);
            }
        }
    }
}
