package com.rigmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RigScreen extends Screen {
    private TextFieldWidget hostMin, hostMax, viewerMin, viewerMax, dropItem, dropModel;
    private ButtonWidget captureBtn, rerollBtn;

    public RigScreen() {
        super(Text.literal("Rig Settings"));
    }

    private TextFieldWidget field(int x, int y, int w, String value, String hint) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 18, Text.literal(hint));
        f.setMaxLength(128);
        f.setText(value == null ? "" : value);
        f.setPlaceholder(Text.literal(hint));
        addDrawableChild(f);
        return f;
    }

    @Override
    protected void init() {
        RigConfig c = RigConfig.INSTANCE;
        int cx = width / 2;
        int x = cx - 100;
        int w = 200;

        hostMin   = field(x,       38, 96, String.valueOf(c.hostMin),   "7");
        hostMax   = field(x + 104, 38, 96, String.valueOf(c.hostMax),   "9");
        viewerMin = field(x,       74, 96, String.valueOf(c.viewerMin), "1");
        viewerMax = field(x + 104, 74, 96, String.valueOf(c.viewerMax), "6");
        dropItem  = field(x, 110, w, c.dropItem,  "minecraft:paper");
        dropModel = field(x, 146, w, c.dropModel, "(optional) pack:model_id");

        captureBtn = addDrawableChild(ButtonWidget.builder(captureLabel(), b -> capture())
            .dimensions(x, 172, w, 20).build());

        addDrawableChild(ButtonWidget.builder(enabledLabel(), b -> {
            RigState.setEnabled(!RigState.enabled);
            b.setMessage(enabledLabel());
            if (rerollBtn != null) rerollBtn.setMessage(rerollLabel());
        }).dimensions(x, 196, 98, 20).build());

        rerollBtn = addDrawableChild(ButtonWidget.builder(rerollLabel(), b -> {
            RigState.roll();
            b.setMessage(rerollLabel());
        }).dimensions(x + 102, 196, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear captured item"), b -> {
            RigState.dropTemplate = ItemStack.EMPTY;
            captureBtn.setMessage(captureLabel());
        }).dimensions(x, 220, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(x + 102, 220, 98, 20).build());
    }

    private void capture() {
        if (client == null || client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) return;
        RigState.dropTemplate = held.copy();
        captureBtn.setMessage(captureLabel());
    }

    private Text captureLabel() {
        return Text.literal(RigState.dropTemplate.isEmpty()
            ? "Use held item as fallback look"
            : "Fallback look = captured item \u2713");
    }

    private Text rerollLabel() {
        return Text.literal("Re-roll (H" + RigState.pick('H') + " V" + RigState.pick('V') + ")");
    }

    private Text enabledLabel() {
        return Text.literal("Rig: " + (RigState.enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = width / 2;
        int x = cx - 100;
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, 10, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Host can drop (from - to)", x, 27, 0xFFFFFF55);
        ctx.drawTextWithShadow(textRenderer, "Viewer can drop (from - to)", x, 63, 0xFFFFFF55);
        ctx.drawTextWithShadow(textRenderer, "Fallback look: item id", x, 99, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Item model (resource pack)", x, 135, 0xFFAAAAAA);
    }

    private static int parse(TextFieldWidget f, int fallback) {
        try {
            return Math.max(1, Math.min(9, Integer.parseInt(f.getText().trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void close() {
        RigConfig c = RigConfig.INSTANCE;
        c.hostMin = parse(hostMin, c.hostMin);
        c.hostMax = parse(hostMax, c.hostMax);
        c.viewerMin = parse(viewerMin, c.viewerMin);
        c.viewerMax = parse(viewerMax, c.viewerMax);
        c.dropItem = dropItem.getText();
        c.dropModel = dropModel.getText();
        RigConfig.save();
        super.close();
    }
}
