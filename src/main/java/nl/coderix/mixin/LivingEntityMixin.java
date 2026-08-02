package nl.coderix.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import nl.coderix.IMorphedPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getDimensionsLiving(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getDimensions(pose));
            }
        }
    }

    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getDefaultDimensionsLiving(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getDimensions(pose));
            }
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getEyeHeightLiving(Pose pose, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getEyeHeight());
            }
        }
    }

    @Inject(method = "getBaseEyeHeight", at = @At("HEAD"), cancellable = true, require = 0)
    private void identityLite$getBaseEyeHeightLiving(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof IMorphedPlayer morphedPlayer) {
            if (morphedPlayer.getMorphEntity() != null) {
                cir.setReturnValue(morphedPlayer.getMorphEntity().getEyeHeight());
            }
        }
    }
}
