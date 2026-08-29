package nl.coderix;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public interface IMorphedPlayer {
    EntityType<?> getMorph();

    default void setMorph(EntityType<?> type) {
        setMorph(type, false);
    }

    void setMorph(EntityType<?> type, boolean baby);

    boolean isBabyMorph();

    LivingEntity getMorphEntity();
}
