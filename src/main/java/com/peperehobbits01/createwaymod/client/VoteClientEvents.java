package com.peperehobbits01.createwaymod.client;

import com.peperehobbits01.createwaymod.CreateWayMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = CreateWayMod.MODID, value = Dist.CLIENT)
public class VoteClientEvents {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
            || VoteScreen.isSingleplayer()) {
            return;
        }

        AbstractContainerScreen<?> inventoryScreen = (AbstractContainerScreen<?>) screen;
        int x = Math.min(inventoryScreen.getGuiLeft() + inventoryScreen.getXSize() + 4, screen.width - 44);
        int y = inventoryScreen.getGuiTop() + 4;
        Button voteButton = Button.builder(Component.translatable("screen.createwaymod.vote.button"),
                button -> VoteScreen.open())
            .bounds(x, y, 42, 20)
            .build();
        event.addListener(voteButton);
        VOTE_BUTTONS.put(screen, voteButton);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Button voteButton = VOTE_BUTTONS.get(event.getScreen());
        if (voteButton != null) {
            GuiGraphics graphics = event.getGuiGraphics();
            voteButton.render(graphics, event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
    }

    private static final Map<Screen, Button> VOTE_BUTTONS = new WeakHashMap<>();
}
