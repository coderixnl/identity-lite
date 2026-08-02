package nl.coderix.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import nl.coderix.IMorphedPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void identityLite$getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getDimensions(pose));
            }
        }
    }

    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getDefaultDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getDimensions(pose));
            }
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getEyeHeight(Pose pose, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getEyeHeight());
            }
        }
    }
}
