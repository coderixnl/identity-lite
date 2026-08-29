package nl.coderix.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import nl.coderix.network.MorphRequestPayload;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MorphScreen extends Screen {
    private final List<MorphOption> availableMorphs = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ICONS_PER_ROW = 6;
    private static final int ICON_SIZE = 40;
    private static final int PADDING = 10;

    public MorphScreen() {
        super(Component.literal("Выбор моба"));
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft.level != null && availableMorphs.isEmpty()) {
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                try {
                    net.minecraft.world.entity.Entity entity = type.create(minecraft.level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    if (entity instanceof LivingEntity living) {
                        living.setId(1000000 + availableMorphs.size());
                        availableMorphs.add(new MorphOption(type, living, false));

                        net.minecraft.world.entity.Entity babyEntity = type.create(minecraft.level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        if (babyEntity instanceof Mob babyMob) {
                            babyMob.setBaby(true);
                            if (babyMob.isBaby()) {
                                babyMob.setId(1000000 + availableMorphs.size());
                                availableMorphs.add(new MorphOption(type, babyMob, true));
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        int startX = width / 2 - 100;
        this.addRenderableWidget(Button.builder(Component.literal("Превратиться обратно в игрока"), button -> {
            ClientPlayNetworking.send(new MorphRequestPayload("", false));
            this.minecraft.setScreenAndShow(null);
        }).bounds(startX, 10, 200, 20).build());
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        if (availableMorphs.isEmpty()) return;

        int startX = (width - (ICONS_PER_ROW * (ICON_SIZE + PADDING))) / 2;
        int startY = 40;

        int visibleRows = (height - 60) / (ICON_SIZE + PADDING);

        for (int i = 0; i < availableMorphs.size(); i++) {
            int row = i / ICONS_PER_ROW - scrollOffset;
            int col = i % ICONS_PER_ROW;

            if (row < 0 || row >= visibleRows) continue;

            int x = startX + col * (ICON_SIZE + PADDING);
            int y = startY + row * (ICON_SIZE + PADDING);

            // Draw box
            context.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0x80000000);

            MorphOption option = availableMorphs.get(i);
            LivingEntity entity = option.displayEntity();
            try {
                net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(
                        context, x, y, x + ICON_SIZE, y + ICON_SIZE, 15, 0.05f, (float)mouseX, (float)mouseY, entity
                );
            } catch (Exception e) {
                context.centeredText(this.font, Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(option.type()).getPath()), x + ICON_SIZE / 2, y + ICON_SIZE / 2 - 4, 0xFFFFFF);
            }

            if (option.baby()) {
                context.text(this.font, Component.literal("Д"), x + 2, y + 2, 0xFFFF55, true);
            }

            if (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE) {
                context.outline(x, y, ICON_SIZE, ICON_SIZE, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.@NonNull MouseButtonEvent event, boolean isSecondary) {
        if (super.mouseClicked(event, isSecondary)) return true;

        if (availableMorphs.isEmpty()) return false;

        double mouseX = event.x();
        double mouseY = event.y();

        int startX = (width - (ICONS_PER_ROW * (ICON_SIZE + PADDING))) / 2;
        int startY = 40;
        int visibleRows = (height - 60) / (ICON_SIZE + PADDING);

        for (int i = 0; i < availableMorphs.size(); i++) {
            int row = i / ICONS_PER_ROW - scrollOffset;
            int col = i % ICONS_PER_ROW;

            if (row < 0 || row >= visibleRows) continue;

            int x = startX + col * (ICON_SIZE + PADDING);
            int y = startY + row * (ICON_SIZE + PADDING);

            if (mouseX >= x && mouseX <= x + ICON_SIZE && mouseY >= y && mouseY <= y + ICON_SIZE) {
                MorphOption option = availableMorphs.get(i);
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(option.type()).toString();
                ClientPlayNetworking.send(new MorphRequestPayload(id, option.baby()));
                this.minecraft.setScreenAndShow(null);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxOffset = Math.max(0, (availableMorphs.size() + ICONS_PER_ROW - 1) / ICONS_PER_ROW - (height - 60) / (ICON_SIZE + PADDING));
        scrollOffset -= (int) Math.signum(verticalAmount);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record MorphOption(EntityType<?> type, LivingEntity displayEntity, boolean baby) {
    }
}
