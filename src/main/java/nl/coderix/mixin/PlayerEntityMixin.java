package nl.coderix.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import nl.coderix.IMorphedPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements IMorphedPlayer {

    @Unique
    @Nullable
    private EntityType<?> identityLite$morphType = null;

    @Unique
    @Nullable
    private LivingEntity identityLite$morphEntity = null;

    @Override
    public @Nullable EntityType<?> getMorph() {
        return this.identityLite$morphType;
    }

    @Override
    public void setMorph(@Nullable EntityType<?> type) {
        this.identityLite$morphType = type;
        if (type != null) {
            Player self = (Player) (Object) this;
            this.identityLite$morphEntity = (LivingEntity) type.create(self.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (this.identityLite$morphEntity != null) {
                this.identityLite$morphEntity.setId(self.getId());
            }
        } else {
            this.identityLite$morphEntity = null;
        }
        ((Player) (Object) this).refreshDimensions();
    }

    @Override
    public @Nullable LivingEntity getMorphEntity() {
        return this.identityLite$morphEntity;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void identityLite$writeNbt(ValueOutput nbt, CallbackInfo ci) {
        if (this.identityLite$morphType != null) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(this.identityLite$morphType);
            nbt.putString("IdentityLiteMorph", id.toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void identityLite$readNbt(ValueInput nbt, CallbackInfo ci) {
        String idStr = nbt.getStringOr("IdentityLiteMorph", "");
        if (!idStr.isEmpty()) {
            try {
                Identifier identifier = Identifier.parse(idStr);
                if (identifier != null) {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(identifier);
                    this.setMorph(type);
                }
            } catch (Exception e) {
                // Ignore parsing errors for older or invalid NBT
            }
        }
    }}
