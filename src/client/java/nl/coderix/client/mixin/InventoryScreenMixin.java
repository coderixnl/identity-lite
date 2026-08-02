package nl.coderix.client.mixin;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import nl.coderix.IMorphedPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @ModifyVariable(
            method = "extractEntityInInventoryFollowsMouse",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0,
            name = "entity")
    private static LivingEntity identityLite$renderMorphInInventory(LivingEntity entity) {
        if (entity instanceof IMorphedPlayer morphedPlayer) {
            LivingEntity morph = morphedPlayer.getMorphEntity();
            if (morph != null) {
                return morph;
            }
        }
        return entity;
    }
}
