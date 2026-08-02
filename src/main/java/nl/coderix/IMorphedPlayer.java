package nl.coderix;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public interface IMorphedPlayer {
    EntityType<?> getMorph();

    void setMorph(EntityType<?> type);

    LivingEntity getMorphEntity();
}
