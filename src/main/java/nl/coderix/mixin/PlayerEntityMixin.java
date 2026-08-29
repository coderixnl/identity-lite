package nl.coderix.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

    @Unique
    private boolean identityLite$babyMorph = false;

    @Override
    public @Nullable EntityType<?> getMorph() {
        return this.identityLite$morphType;
    }

    @Override
    public void setMorph(@Nullable EntityType<?> type, boolean baby) {
        this.identityLite$morphType = type;
        if (type != null) {
            Player self = (Player) (Object) this;
            this.identityLite$morphEntity = (LivingEntity) type.create(self.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (this.identityLite$morphEntity != null) {
                if (this.identityLite$morphEntity instanceof Mob mob) {
                    mob.setBaby(baby);
                }
                this.identityLite$babyMorph = this.identityLite$morphEntity.isBaby();
                this.identityLite$morphEntity.setId(self.getId());
            } else {
                this.identityLite$babyMorph = false;
            }
        } else {
            this.identityLite$morphEntity = null;
            this.identityLite$babyMorph = false;
        }
        ((Player) (Object) this).refreshDimensions();
    }

    @Override
    public boolean isBabyMorph() {
        return this.identityLite$babyMorph;
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
            nbt.putBoolean("IdentityLiteBabyMorph", this.identityLite$babyMorph);
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
                    this.setMorph(type, nbt.getBooleanOr("IdentityLiteBabyMorph", false));
                }
            } catch (Exception e) {
                // Ignore parsing errors for older or invalid NBT
            }
        }
    }}
