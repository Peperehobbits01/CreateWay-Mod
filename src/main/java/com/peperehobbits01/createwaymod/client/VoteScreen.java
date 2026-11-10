package com.peperehobbits01.createwaymod.client;

import com.peperehobbits01.createwaymod.config.CreateWayConfig;
import com.peperehobbits01.createwaymod.network.CastVotePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class VoteScreen extends Screen {
    private static final int CALENDAR_COLUMNS = 7;
    private static final int CALENDAR_ROWS = 6;
    private static final int CELL_WIDTH = 32;
    private static final int CELL_HEIGHT = 18;

    private final Screen parentScreen;
    private final List<Button> dayButtons = new ArrayList<>(CALENDAR_COLUMNS * CALENDAR_ROWS);
    private final Set<LocalDate> selectedDates = new TreeSet<>();
    private LocalDate minimumDate;
    private LocalDate maximumDate;
    private YearMonth displayedMonth;
    private Button previousMonthButton;
    private Button nextMonthButton;
    private Button submitButton;

    public VoteScreen(Screen parentScreen) {
        super(Component.translatable("screen.createwaymod.vote.title"));
        this.parentScreen = parentScreen;
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isSingleplayer() && CreateWayConfig.isVoteSystemEnabled()) {
            minecraft.setScreen(new VoteScreen(minecraft.screen));
        }
    }

    public static boolean isSingleplayer() {
        return Minecraft.getInstance().hasSingleplayerServer();
    }

    @Override
    protected void init() {
        minimumDate = LocalDate.now().plusDays(7);
        maximumDate = LocalDate.now().plusMonths(2);
        displayedMonth = YearMonth.from(minimumDate);
        dayButtons.clear();

        int calendarWidth = CALENDAR_COLUMNS * CELL_WIDTH;
        int left = width / 2 - calendarWidth / 2;
        int gridY = Math.max(58, height / 2 - 40);
        int headerY = gridY - 40;

        previousMonthButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            displayedMonth = displayedMonth.minusMonths(1);
            refreshCalendar();
        }).bounds(left, headerY, 30, 20).build());

        nextMonthButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            displayedMonth = displayedMonth.plusMonths(1);
            refreshCalendar();
        }).bounds(left + calendarWidth - 30, headerY, 30, 20).build());

        for (int index = 0; index < CALENDAR_COLUMNS * CALENDAR_ROWS; index++) {
            int cell = index;
            Button dayButton = Button.builder(Component.empty(), button -> selectDate(cell))
                .bounds(left + (index % CALENDAR_COLUMNS) * CELL_WIDTH,
                    gridY + (index / CALENDAR_COLUMNS) * CELL_HEIGHT, 30, 16)
                .build();
            dayButtons.add(addRenderableWidget(dayButton));
        }

        submitButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.createwaymod.vote.submit"),
            button -> {
                if (!selectedDates.isEmpty()) {
                    PacketDistributor.sendToServer(new CastVotePayload(
                        selectedDates.stream().map(LocalDate::toString).toList()
                    ));
                    Minecraft.getInstance().setScreen(parentScreen);
                }
            }
        ).bounds(left, height - 27, calendarWidth, 20).build());

        refreshCalendar();
    }

    private void selectDate(int cell) {
        LocalDate firstDay = displayedMonth.atDay(1);
        int dayOffset = firstDay.getDayOfWeek().getValue() - 1;
        LocalDate date = firstDay.plusDays(cell - dayOffset);
        if (YearMonth.from(date).equals(displayedMonth) && !date.isBefore(minimumDate)) {
            if (!selectedDates.add(date)) {
                selectedDates.remove(date);
            }
            refreshCalendar();
        }
    }

    private void refreshCalendar() {
        previousMonthButton.active = displayedMonth.isAfter(YearMonth.from(minimumDate));
        nextMonthButton.active = displayedMonth.isBefore(YearMonth.from(maximumDate));
        submitButton.active = !selectedDates.isEmpty();

        LocalDate firstDay = displayedMonth.atDay(1);
        int dayOffset = firstDay.getDayOfWeek().getValue() - 1;
        for (int index = 0; index < dayButtons.size(); index++) {
            LocalDate date = firstDay.plusDays(index - dayOffset);
            Button button = dayButtons.get(index);
            boolean inMonth = YearMonth.from(date).equals(displayedMonth);
            boolean selected = selectedDates.contains(date);
            boolean inRange = !date.isBefore(minimumDate) && !date.isAfter(maximumDate);
            boolean selectable = inMonth && inRange
                && (selected || selectedDates.size() < CastVotePayload.MAX_DATES);
            button.visible = inMonth && !date.isAfter(maximumDate);
            button.active = selectable;
            if (inMonth) {
                String day = Integer.toString(date.getDayOfMonth());
                button.setMessage(Component.literal(selected ? "[" + day + "]" : day));
            } else {
                button.setMessage(Component.empty());
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.createwaymod.vote.instructions"),
            width / 2, 25, 0xAAAAAA);

        String monthTitle = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()));
        graphics.drawCenteredString(font, monthTitle, width / 2, dayButtons.getFirst().getY() - 34, 0xFFFFFF);

        int calendarLeft = width / 2 - CALENDAR_COLUMNS * CELL_WIDTH / 2;
        int weekdayY = dayButtons.getFirst().getY() - 15;
        for (int column = 0; column < CALENDAR_COLUMNS; column++) {
            graphics.drawCenteredString(font,
                Component.translatable("screen.createwaymod.vote.weekday." + column),
                calendarLeft + column * CELL_WIDTH + 15, weekdayY, 0xAAAAAA);
        }

        Component selectedText = Component.translatable(
            "screen.createwaymod.vote.selected_dates",
            selectedDates.size(),
            CastVotePayload.MAX_DATES
        );
        graphics.drawCenteredString(font, selectedText, width / 2, height - 47, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
