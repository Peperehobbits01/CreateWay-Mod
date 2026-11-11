package com.peperehobbits01.createwaymod.client;

import com.peperehobbits01.createwaymod.CreateWayMod;
import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        if (!(screen instanceof InventoryScreen) || VoteScreen.isSingleplayer()
            || !CreateWayConfig.isVoteSystemEnabled()) {
            return;
        }

        AbstractContainerScreen<?> inventoryScreen = (AbstractContainerScreen<?>) screen;
        int x = inventoryScreen.getGuiLeft() + 126;
        int y = screen.height / 2 - 22;
        Button voteButton = Button.builder(Component.empty(),
                button -> VoteScreen.open())
            .bounds(x, y, 20, 20)
            .build();
        updateVoteButton(voteButton, VoteClientPayloads.isVotingOpen());
        event.addListener(voteButton);
        VOTE_BUTTONS.put(screen, voteButton);
    }

    public static void setVotingOpen(boolean votingOpen) {
        VOTE_BUTTONS.values().forEach(button -> updateVoteButton(button, votingOpen));
    }

    private static void updateVoteButton(Button button, boolean votingOpen) {
        button.active = votingOpen;
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable(votingOpen
                ? "screen.createwaymod.vote.tooltip"
                : "screen.createwaymod.vote.closed_tooltip")
        ));
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Button voteButton = VOTE_BUTTONS.get(event.getScreen());
        if (voteButton != null) {
            GuiGraphics graphics = event.getGuiGraphics();
            graphics.renderItem(new ItemStack(Items.ENDER_EYE), voteButton.getX() + 2, voteButton.getY() + 2);
            if (!voteButton.active) {
                graphics.fill(
                    voteButton.getX() + 2,
                    voteButton.getY() + 2,
                    voteButton.getX() + 18,
                    voteButton.getY() + 18,
                    0x80666666
                );
            }
        }
    }

    private static final Map<Screen, Button> VOTE_BUTTONS = new WeakHashMap<>();
}
