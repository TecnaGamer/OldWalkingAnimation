//? if >=26.1 || neoforge {
package tecna.oldwalkinganimation.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tecna.oldwalkinganimation.OwaSteveManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OwaOptionsList extends ContainerObjectSelectionList<OwaOptionsList.RowEntry> {
    private static final int ROW_HEIGHT = 24;

    private final Screen owner;
    private final String category;

    public OwaOptionsList(Minecraft mc, Screen screen, String category) {
        //? if >=1.20.3 {
        super(mc, screen.width, screen.height, 0, ROW_HEIGHT);
        //?} else {
        /*super(mc, screen.width, screen.height, 0, screen.height, ROW_HEIGHT);
        *///?}
        this.owner = screen;
        this.category = category;
        //? if <1.20.5 {
        /*// 1.20.2-1.20.4 render the legacy dirt-tile background behind list contents by default;
        // the tab UI sits on top of it producing the harsh checkerboard. Suppress it.
        // 1.20.5 removed the helper and the default; nothing to do there.
        this.setRenderBackground(false);
        *///?}
        populate();
    }

    //? if <1.20.6 {
    /*// updateSizeAndPosition was added in 1.20.6. For 1.20.3-1.20.5 use AbstractWidget.setRectangle
    // (signature: width, height, x, y). For 1.20.2 the AbstractSelectionList still exposes the old
    // x0/y0/x1/y1 protected fields directly; set them by reflection.
    public void applyTabLayout(int w, int h, int top) {
        this.width = w;
        this.height = h;
        //? if >=1.20.3 {
        this.setRectangle(w, h, 0, top);
        //?} else {
        /^try {
            for (java.lang.reflect.Field f : net.minecraft.client.gui.components.AbstractSelectionList.class.getDeclaredFields()) {
                if (f.getType() != int.class) continue;
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                String n = f.getName();
                if (n.equals("x0") || n.equals("left")) f.setInt(this, 0);
                else if (n.equals("x1") || n.equals("right")) f.setInt(this, w);
                else if (n.equals("y0") || n.equals("top")) f.setInt(this, top);
                else if (n.equals("y1") || n.equals("bottom")) f.setInt(this, top + h);
            }
        } catch (Throwable ignored) {}
        ^///?}
    }
    *///?}

    public void rebuild() {
        //? if >=1.21.4 || >=26.1 {
        double scroll = this.scrollAmount();
        //?} else {
        /*double scroll = this.getScrollAmount();
        *///?}
        this.clearEntries();
        populate();
        this.setScrollAmount(scroll);
    }

    private void populate() {
        OwaConfigScreen owa = (owner instanceof OwaConfigScreen s) ? s : null;
        List<Field> fields = OwaConfigScreen.entriesByCategory().getOrDefault(category, List.of());
        List<FeatureGroup> groups = FeatureRegistry.forCategory(category);

        if ("steves".equals(category)) {
            Button removeBtn = Button.builder(Component.literal("Remove All Classic Humans"),
                    b -> OwaSteveManager.clear()).size(200, 20).build();
            addEntry(new ButtonRow(removeBtn));
        }

        Set<String> groupedFieldNames = new HashSet<>();
        for (FeatureGroup g : groups) groupedFieldNames.addAll(g.fields);

        for (FeatureGroup g : groups) {
            addEntry(new GroupHeaderRow(g, owa));
            boolean showAdvanced = PresetManager.isCustom(g) || PresetManager.advancedMode();
            for (String fname : g.fields) {
                Field f = findField(fields, fname);
                if (f == null) continue;
                if (!showAdvanced && !owa$forceVisible(f)) continue;
                if (!owa$shouldShow(f)) continue;
                addWidgetFor(f, owa);
            }
        }

        for (Field f : fields) {
            if (groupedFieldNames.contains(f.getName())) continue;
            if (!owa$shouldShow(f)) continue;
            addWidgetFor(f, owa);
        }
    }

    // Hide fields whose visibility depends on a non-boolean parent (boolean dependsOn is handled
    // by isEnabled which only greys out, doesn't hide). Currently used for the custom skins
    // text input - only meaningful when steveSkinMode is "custom".
    private static boolean owa$shouldShow(Field f) {
        if ("steveCustomSkinSources".equals(f.getName())) {
            return "custom".equals(tecna.oldwalkinganimation.config.Config.steveSkinMode);
        }
        // steveSkinMode is the String backing field for the Skin preset cycle button. The cycle
        // already controls it; showing the raw textbox here is redundant (and gives 2 textboxes
        // on the Custom preset since steveCustomSkinSources also appears).
        if ("steveSkinMode".equals(f.getName())) return false;
        return true;
    }

    // Some fields should appear inside a group even when the Advanced toggle is off and the
    // selected preset isn't "custom". Used so the custom skins input pops up directly under
    // the Skin Mode group as soon as the user picks the Custom Skin preset.
    private static boolean owa$forceVisible(Field f) {
        if ("steveCustomSkinSources".equals(f.getName())) {
            return "custom".equals(tecna.oldwalkinganimation.config.Config.steveSkinMode);
        }
        return false;
    }

    private static Field findField(List<Field> fields, String name) {
        for (Field f : fields) if (f.getName().equals(name)) return f;
        return null;
    }

    private void addWidgetFor(Field f, OwaConfigScreen owa) {
        MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
        if (e == null) return;
        AbstractWidget w = null;
        if (f.getType() == boolean.class) w = makeCheckbox(f, e, owa);
        else if (f.getType() == float.class && e.isSlider()) w = makeFloatSlider(f, e, owa);
        else if (f.getType() == int.class && e.isSlider()) w = makeIntSlider(f, e, owa);
        else if (f.getType() == String.class) w = makeStringInput(f, e, owa);
        if (w != null) {
            applyTooltip(w, f);
            addEntry(new WidgetRow(f, w, makeResetButton(f, owa)));
        }
    }

    private static net.minecraft.client.gui.components.EditBox makeStringInput(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            String cur = (String) f.get(null);
            if (cur == null) cur = "";
            Component label = Component.literal(OwaConfigScreen.labelFor(f, e));
            net.minecraft.client.gui.components.EditBox box =
                    new net.minecraft.client.gui.components.EditBox(Minecraft.getInstance().font, 0, 0, 200, 20, label);
            box.setMaxLength(2048);
            box.setValue(cur);
            String placeholderKey = "oldwalkinganimation.midnightconfig." + f.getName() + ".placeholder";
            if (net.minecraft.client.resources.language.I18n.exists(placeholderKey)) {
                box.setHint(Component.translatable(placeholderKey));
            }
            box.setResponder(val -> {
                try { f.set(null, val); } catch (Exception ex) { ex.printStackTrace(); }
                PresetManager.onFieldChanged(f);
            });
            return box;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void applyTooltip(AbstractWidget w, Field f) {
        Component tt = OwaConfigScreen.tooltipFor(f);
        if (tt != null) w.setTooltip(Tooltip.create(tt));
    }

    private static Button makeResetButton(Field f, OwaConfigScreen owa) {
        Button b = Button.builder(Component.literal("Reset"), btn -> {
            PresetManager.resetField(f);
            if (owa != null) owa.rebuildList();
        }).size(40, 20).build();
        b.setTooltip(Tooltip.create(Component.literal("Reset to the current preset's value")));
        return b;
    }

    private static Checkbox makeCheckbox(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            boolean cur = f.getBoolean(null);
            Component label = Component.literal(OwaConfigScreen.labelFor(f, e));
            //? if >=1.20.3 {
            return Checkbox.builder(label, Minecraft.getInstance().font)
                    .selected(cur)
                    .onValueChange((cb, val) -> {
                        try { f.setBoolean(null, val); } catch (Exception ex) { ex.printStackTrace(); }
                        PresetManager.onFieldChanged(f);
                        if (owa != null) owa.rebuildList();
                    })
                    .build();
            //?} else {
            /*return new Checkbox(0, 0, 20, 20, label, cur, true) {
                @Override public void onPress() {
                    super.onPress();
                    try { f.setBoolean(null, this.selected()); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                    if (owa != null) owa.rebuildList();
                }
            };
            *///?}
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static AbstractSliderButton makeFloatSlider(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            float cur = f.getFloat(null);
            float min = e.min();
            float max = e.max();
            String label = OwaConfigScreen.labelFor(f, e);
            return new AbstractSliderButton(0, 0, 156, 20,
                    Component.literal(label + ": " + cur), (cur - min) / (max - min)) {
                @Override protected void updateMessage() {
                    float v = min + (float) value * (max - min);
                    this.setMessage(Component.literal(label + ": " + String.format("%.2f", v)));
                }
                @Override protected void applyValue() {
                    float v = min + (float) value * (max - min);
                    try { f.setFloat(null, v); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static AbstractSliderButton makeIntSlider(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            int cur = f.getInt(null);
            float min = e.min();
            float max = e.max();
            String label = OwaConfigScreen.labelFor(f, e);
            return new AbstractSliderButton(0, 0, 156, 20,
                    Component.literal(label + ": " + cur), (cur - min) / (max - min)) {
                @Override protected void updateMessage() {
                    int v = (int) (min + value * (max - min));
                    this.setMessage(Component.literal(label + ": " + v));
                }
                @Override protected void applyValue() {
                    int v = (int) (min + value * (max - min));
                    try { f.setInt(null, v); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isEnabled(Field f) {
        try {
            Field modTog = f.getDeclaringClass().getField("enableMod");
            modTog.setAccessible(true);
            if (!f.equals(modTog) && !modTog.getBoolean(null)) return false;
        } catch (ReflectiveOperationException ignored) {}
        MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
        if (e == null || e.dependsOn().isEmpty()) return true;
        try {
            Field dep = f.getDeclaringClass().getField(e.dependsOn());
            dep.setAccessible(true);
            if (dep.getType() != boolean.class) return true;
            if (!dep.getBoolean(null)) return false;
            return isEnabled(dep);
        } catch (ReflectiveOperationException ignored) {}
        return true;
    }

    @Override public int getRowWidth() { return Math.min(260, this.getWidth() - 24); }

    //? if <1.21.4 {
    // The vanilla AbstractSelectionList.getScrollbarPosition() returns this.width/2 + 124, hardcoded
    // for 220-wide rows. Our rows are 260 wide so the row's right edge crosses the scrollbar and
    // overlaps the reset button. Pin the scrollbar to the list's right edge so it sits flush with
    // the options-box border regardless of list width. (1.21.4+ replaced this with scrollBarX()
    // whose default already places the scrollbar past getRowRight().)
    //? if >=1.20.3 {
    @Override protected int getScrollbarPosition() { return this.getX() + this.width - 6; }
    //?} else {
    /*@Override protected int getScrollbarPosition() { return this.x1 - 6; }
    *///?}
    //?}

    // 1.20.5+ AbstractSelectionList draws 2-px gradient separator lines at the list's top and
    // bottom edge. They overlap our tinted section borders and read as duplicated horizontal
    // lines. We render our own separators via owa$drawSectionTints, so suppress these.
    //? if >=26.1 {
    @Override protected void extractListSeparators(GuiGraphicsExtractor gge) {}
    //?} else if >=1.20.5 {
    /*@Override protected void renderListSeparators(net.minecraft.client.gui.GuiGraphics gg) {}
    *///?}

    public abstract static class RowEntry extends ContainerObjectSelectionList.Entry<RowEntry> {}

    public static class HeaderRow extends RowEntry {
        private final StringWidget label;
        HeaderRow(Component text) {
            this.label = new StringWidget(220, 20, text, Minecraft.getInstance().font);
        }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(); }
        @Override public List<? extends GuiEventListener> children() { return List.of(); }
        //? if >=26.1 {
        @Override public void extractContent(GuiGraphicsExtractor gge, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(getContentX());
            label.setY(getContentY() + 4);
            label.extractRenderState(gge, mouseX, mouseY, pt);
        }
        //?} else if >=1.21.9 {
        /*@Override public void renderContent(GuiGraphics gge, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(getContentX());
            label.setY(getContentY() + 4);
            label.render(gge, mouseX, mouseY, pt);
        }
        *///?} else {
        /*@Override public void render(GuiGraphics gg, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(x);
            label.setY(y + 4);
            label.render(gg, mouseX, mouseY, pt);
        }
        *///?}
    }

    public static class GroupHeaderRow extends RowEntry {
        private final FeatureGroup group;
        private final OwaConfigScreen owa;
        private final StringWidget label;
        private final CycleButton<Preset> cycle;

        GroupHeaderRow(FeatureGroup g, OwaConfigScreen owa) {
            this.group = g;
            this.owa = owa;
            // Left-aligned (so center-mode doesn't push the text rightward into the cycle) AND
            // widened to 160 so long names like "Classic Human Physics" fully render without
            // ellipsis truncation. The cycle was shrunk from 150 → 130 to leave room.
            // alignLeft was removed in mojmap 26.1+ (TextWidget defaults to left-aligned there).
            //? if <1.21.9 {
            this.label = new StringWidget(160, 20,
                    Component.literal(g.name), Minecraft.getInstance().font).alignLeft();
            //?} else {
            /*this.label = new StringWidget(160, 20,
                    Component.literal(g.name), Minecraft.getInstance().font);
            *///?}
            Preset cur = PresetManager.selectedFor(g);
            //? if >=1.21.11 {
            this.cycle = new CycleButton.Builder<Preset>(
                    p -> Component.literal(p == null ? "" : p.name),
                    () -> PresetManager.selectedFor(g))
                    .withValues(new ArrayList<>(g.presets))
                    .create(0, 0, 130, 20, Component.literal("Preset"),
                            (cb, val) -> {
                                PresetManager.selectPreset(g, val);
                                if (owa != null) owa.rebuildList();
                            });
            //?} else {
            /*this.cycle = new CycleButton.Builder<Preset>(
                    p -> Component.literal(p == null ? "" : p.name))
                    .withValues(new ArrayList<>(g.presets))
                    .create(0, 0, 130, 20, Component.literal("Preset"),
                            (cb, val) -> {
                                PresetManager.selectPreset(g, val);
                                if (owa != null) owa.rebuildList();
                            });
            *///?}
            this.cycle.setValue(cur);
        }

        @Override public List<? extends NarratableEntry> narratables() { return List.of(cycle); }
        @Override public List<? extends GuiEventListener> children() { return List.of(cycle); }
        //? if >=26.1 {
        @Override public void extractContent(GuiGraphicsExtractor gge, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(getContentX());
            label.setY(getContentY() + 4);
            label.extractRenderState(gge, mouseX, mouseY, pt);
            cycle.setX(getContentX() + getContentWidth() - 130);
            cycle.setY(getContentY());
            cycle.extractRenderState(gge, mouseX, mouseY, pt);
        }
        //?} else if >=1.21.9 {
        /*@Override public void renderContent(GuiGraphics gge, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(getContentX());
            label.setY(getContentY() + 4);
            label.render(gge, mouseX, mouseY, pt);
            cycle.setX(getContentX() + getContentWidth() - 130);
            cycle.setY(getContentY());
            cycle.render(gge, mouseX, mouseY, pt);
        }
        *///?} else {
        /*@Override public void render(GuiGraphics gg, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float pt) {
            label.setX(x);
            label.setY(y + 4);
            label.render(gg, mouseX, mouseY, pt);
            cycle.setX(x + entryWidth - 130);
            cycle.setY(y);
            cycle.render(gg, mouseX, mouseY, pt);
        }
        *///?}
    }

    public static class ButtonRow extends RowEntry {
        private final Button button;
        ButtonRow(Button b) { this.button = b; }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(button); }
        @Override public List<? extends GuiEventListener> children() { return List.of(button); }
        //? if >=26.1 {
        @Override public void extractContent(GuiGraphicsExtractor gge, int mouseX, int mouseY, boolean hovered, float pt) {
            int w = button.getWidth();
            button.setX(getContentX() + (getContentWidth() - w) / 2);
            button.setY(getContentY());
            button.extractRenderState(gge, mouseX, mouseY, pt);
        }
        //?} else if >=1.21.9 {
        /*@Override public void renderContent(GuiGraphics gge, int mouseX, int mouseY, boolean hovered, float pt) {
            int w = button.getWidth();
            button.setX(getContentX() + (getContentWidth() - w) / 2);
            button.setY(getContentY());
            button.render(gge, mouseX, mouseY, pt);
        }
        *///?} else {
        /*@Override public void render(GuiGraphics gg, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float pt) {
            int w = button.getWidth();
            button.setX(x + (entryWidth - w) / 2);
            button.setY(y);
            button.render(gg, mouseX, mouseY, pt);
        }
        *///?}
    }

    public static class WidgetRow extends RowEntry {
        private final Field field;
        private final AbstractWidget widget;
        private final Button reset;
        WidgetRow(Field field, AbstractWidget w, Button reset) { this.field = field; this.widget = w; this.reset = reset; }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(widget, reset); }
        @Override public List<? extends GuiEventListener> children() { return List.of(widget, reset); }
        //? if >=26.1 {
        @Override public void extractContent(GuiGraphicsExtractor gge, int mouseX, int mouseY, boolean hovered, float pt) {
            boolean enabled = isEnabled(field);
            widget.active = enabled;
            widget.setAlpha(enabled ? 1.0f : 0.5f);
            widget.setX(getContentX());
            widget.setY(getContentY());
            widget.extractRenderState(gge, mouseX, mouseY, pt);
            reset.active = enabled && !PresetManager.fieldAtPresetValue(field);
            reset.setX(getContentX() + getContentWidth() - 40);
            reset.setY(getContentY());
            reset.extractRenderState(gge, mouseX, mouseY, pt);
        }
        //?} else if >=1.21.9 {
        /*@Override public void renderContent(GuiGraphics gge, int mouseX, int mouseY, boolean hovered, float pt) {
            boolean enabled = isEnabled(field);
            widget.active = enabled;
            widget.setAlpha(enabled ? 1.0f : 0.5f);
            widget.setX(getContentX());
            widget.setY(getContentY());
            widget.render(gge, mouseX, mouseY, pt);
            reset.active = enabled && !PresetManager.fieldAtPresetValue(field);
            reset.setX(getContentX() + getContentWidth() - 40);
            reset.setY(getContentY());
            reset.render(gge, mouseX, mouseY, pt);
        }
        *///?} else {
        /*@Override public void render(GuiGraphics gg, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float pt) {
            boolean enabled = isEnabled(field);
            widget.active = enabled;
            widget.setAlpha(enabled ? 1.0f : 0.5f);
            widget.setX(x);
            widget.setY(y);
            widget.render(gg, mouseX, mouseY, pt);
            reset.active = enabled && !PresetManager.fieldAtPresetValue(field);
            reset.setX(x + entryWidth - 40);
            reset.setY(y);
            reset.render(gg, mouseX, mouseY, pt);
        }
        *///?}
    }
}
//?} else {
/*package tecna.oldwalkinganimation.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import tecna.oldwalkinganimation.OwaSteveManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OwaOptionsList extends ElementListWidget<OwaOptionsList.RowEntry> {
    private static final int ROW_HEIGHT = 24;

    private final Screen owner;
    private final String category;

    public OwaOptionsList(MinecraftClient mc, Screen screen, String category) {
        //? if >=1.20.3 {
        super(mc, screen.width, screen.height, 0, ROW_HEIGHT);
        //?} else {
        /^super(mc, screen.width, screen.height, 0, screen.height, ROW_HEIGHT);^/
        //?}
        this.owner = screen;
        this.category = category;
        //? if <=1.20.4
        /^this.setRenderBackground(false);^/
        populate();
    }

    // 1.20.2/1.20.4 fabric: ElementListWidget.updateSizeAndPosition was added in 1.20.6, so we
    // walk parent EntryListWidget int fields and write width/height/top/bottom by name.
    //? if <1.20.6 {
    /^public void applyTabLayout(int w, int h, int top) {
        try {
            for (java.lang.reflect.Field f : net.minecraft.client.gui.widget.EntryListWidget.class.getDeclaredFields()) {
                if (f.getType() != int.class) continue;
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                String n = f.getName();
                if (n.equals("width")) f.setInt(this, w);
                else if (n.equals("height")) f.setInt(this, h);
                else if (n.equals("top") || n.equals("y0")) f.setInt(this, top);
                else if (n.equals("bottom") || n.equals("y1")) f.setInt(this, top + h);
                else if (n.equals("left") || n.equals("x0")) f.setInt(this, 0);
                else if (n.equals("right") || n.equals("x1")) f.setInt(this, w);
            }
        } catch (Throwable ignored) {}
    }
    ^///?}

    public void rebuild() {
        //? if >=1.21.4 {
        double scroll = this.getScrollY();
        this.clearEntries();
        populate();
        this.setScrollY(scroll);
        //?} else {
        /^double scroll = this.getScrollAmount();
        this.clearEntries();
        populate();
        this.setScrollAmount(scroll);
        ^///?}
    }

    private void populate() {
        OwaConfigScreen owa = (owner instanceof OwaConfigScreen) ? (OwaConfigScreen) owner : null;
        List<Field> fields = OwaConfigScreen.entriesByCategory().getOrDefault(category, List.of());
        List<FeatureGroup> groups = FeatureRegistry.forCategory(category);

        if ("steves".equals(category)) {
            ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("Remove All Classic Humans"),
                    b -> OwaSteveManager.clear()).size(200, 20).build();
            addEntry(new ButtonRow(removeBtn));
        }

        Set<String> groupedFieldNames = new HashSet<>();
        for (FeatureGroup g : groups) groupedFieldNames.addAll(g.fields);

        for (FeatureGroup g : groups) {
            addEntry(new GroupHeaderRow(g, owa));
            boolean showAdvanced = PresetManager.isCustom(g) || PresetManager.advancedMode();
            for (String fname : g.fields) {
                Field f = findField(fields, fname);
                if (f == null) continue;
                if (!showAdvanced && !owa$forceVisible(f)) continue;
                if (!owa$shouldShow(f)) continue;
                addWidgetFor(f, owa);
            }
        }

        for (Field f : fields) {
            if (groupedFieldNames.contains(f.getName())) continue;
            if (!owa$shouldShow(f)) continue;
            addWidgetFor(f, owa);
        }
    }

    // Hide fields whose visibility depends on a non-boolean parent (boolean dependsOn is handled
    // by isEnabled which only greys out, doesn't hide). Currently used for the custom skins
    // text input - only meaningful when steveSkinMode is "custom".
    private static boolean owa$shouldShow(Field f) {
        if ("steveCustomSkinSources".equals(f.getName())) {
            return "custom".equals(tecna.oldwalkinganimation.config.Config.steveSkinMode);
        }
        // steveSkinMode is the String backing field for the Skin preset cycle button. The cycle
        // already controls it; showing the raw textbox here is redundant (and gives 2 textboxes
        // on the Custom preset since steveCustomSkinSources also appears).
        if ("steveSkinMode".equals(f.getName())) return false;
        return true;
    }

    // Some fields should appear inside a group even when the Advanced toggle is off and the
    // selected preset isn't "custom". Used so the custom skins input pops up directly under
    // the Skin Mode group as soon as the user picks the Custom Skin preset.
    private static boolean owa$forceVisible(Field f) {
        if ("steveCustomSkinSources".equals(f.getName())) {
            return "custom".equals(tecna.oldwalkinganimation.config.Config.steveSkinMode);
        }
        return false;
    }

    private static Field findField(List<Field> fields, String name) {
        for (Field f : fields) if (f.getName().equals(name)) return f;
        return null;
    }

    private void addWidgetFor(Field f, OwaConfigScreen owa) {
        MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
        if (e == null) return;
        ClickableWidget w = null;
        if (f.getType() == boolean.class) w = makeCheckbox(f, e, owa);
        else if (f.getType() == float.class && e.isSlider()) w = makeFloatSlider(f, e, owa);
        else if (f.getType() == int.class && e.isSlider()) w = makeIntSlider(f, e, owa);
        else if (f.getType() == String.class) w = makeStringInput(f, e, owa);
        if (w != null) {
            applyTooltip(w, f);
            addEntry(new WidgetRow(f, w, makeResetButton(f, owa)));
        }
    }

    private static net.minecraft.client.gui.widget.TextFieldWidget makeStringInput(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            String cur = (String) f.get(null);
            if (cur == null) cur = "";
            Text label = Text.literal(OwaConfigScreen.labelFor(f, e));
            net.minecraft.client.gui.widget.TextFieldWidget box =
                    new net.minecraft.client.gui.widget.TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 200, 20, label);
            box.setMaxLength(2048);
            box.setText(cur);
            String placeholderKey = "oldwalkinganimation.midnightconfig." + f.getName() + ".placeholder";
            if (net.minecraft.client.resource.language.I18n.hasTranslation(placeholderKey)) {
                box.setPlaceholder(Text.translatable(placeholderKey));
            }
            box.setChangedListener(val -> {
                try { f.set(null, val); } catch (Exception ex) { ex.printStackTrace(); }
                PresetManager.onFieldChanged(f);
            });
            return box;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void applyTooltip(ClickableWidget w, Field f) {
        Text tt = OwaConfigScreen.tooltipFor(f);
        if (tt != null) w.setTooltip(Tooltip.of(tt));
    }

    private static ButtonWidget makeResetButton(Field f, OwaConfigScreen owa) {
        return ButtonWidget.builder(Text.literal("Reset"), btn -> {
            PresetManager.resetField(f);
            if (owa != null) owa.rebuildList();
        }).size(40, 20).build();
    }

    private static CheckboxWidget makeCheckbox(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            boolean cur = f.getBoolean(null);
            Text label = Text.literal(OwaConfigScreen.labelFor(f, e));
            //? if >=1.20.3 {
            return CheckboxWidget.builder(label, MinecraftClient.getInstance().textRenderer)
                    .checked(cur)
                    .callback((cb, val) -> {
                        try { f.setBoolean(null, val); } catch (Exception ex) { ex.printStackTrace(); }
                        PresetManager.onFieldChanged(f);
                        if (owa != null) owa.rebuildList();
                    })
                    .build();
            //?} else {
            /^return new CheckboxWidget(0, 0, 20, 20, label, cur, true) {
                @Override public void onPress() {
                    super.onPress();
                    try { f.setBoolean(null, this.isChecked()); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                    if (owa != null) owa.rebuildList();
                }
            };
            ^///?}
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static SliderWidget makeFloatSlider(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            float cur = f.getFloat(null);
            float min = e.min();
            float max = e.max();
            String label = OwaConfigScreen.labelFor(f, e);
            return new SliderWidget(0, 0, 156, 20,
                    Text.literal(label + ": " + cur), (cur - min) / (max - min)) {
                @Override protected void updateMessage() {
                    float v = min + (float) value * (max - min);
                    this.setMessage(Text.literal(label + ": " + String.format("%.2f", v)));
                }
                @Override protected void applyValue() {
                    float v = min + (float) value * (max - min);
                    try { f.setFloat(null, v); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static SliderWidget makeIntSlider(Field f, MidnightConfig.Entry e, OwaConfigScreen owa) {
        try {
            f.setAccessible(true);
            int cur = f.getInt(null);
            float min = e.min();
            float max = e.max();
            String label = OwaConfigScreen.labelFor(f, e);
            return new SliderWidget(0, 0, 156, 20,
                    Text.literal(label + ": " + cur), (cur - min) / (max - min)) {
                @Override protected void updateMessage() {
                    int v = (int) (min + value * (max - min));
                    this.setMessage(Text.literal(label + ": " + v));
                }
                @Override protected void applyValue() {
                    int v = (int) (min + value * (max - min));
                    try { f.setInt(null, v); } catch (Exception ex) { ex.printStackTrace(); }
                    PresetManager.onFieldChanged(f);
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isEnabled(Field f) {
        try {
            Field modTog = f.getDeclaringClass().getField("enableMod");
            modTog.setAccessible(true);
            if (!f.equals(modTog) && !modTog.getBoolean(null)) return false;
        } catch (ReflectiveOperationException ignored) {}
        MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
        if (e == null || e.dependsOn().isEmpty()) return true;
        try {
            Field dep = f.getDeclaringClass().getField(e.dependsOn());
            dep.setAccessible(true);
            if (dep.getType() != boolean.class) return true;
            if (!dep.getBoolean(null)) return false;
            return isEnabled(dep);
        } catch (ReflectiveOperationException ignored) {}
        return true;
    }

    //? if >=1.20.3 {
    @Override public int getRowWidth() { return Math.min(260, this.getWidth() - 24); }
    //?} else
    /^@Override public int getRowWidth() { return Math.min(260, this.width - 24); }^/

    // Pin the scrollbar to the list's right edge (vs default, which tracks row right). The yarn
    // API renamed at 1.20.5: getScrollbarPositionX -> getScrollbarX.
    //? if >=1.20.5 {
    @Override protected int getScrollbarX() { return this.getX() + this.width - 6; }
    //?} else if >=1.20.3 {
    /^@Override protected int getScrollbarPositionX() { return this.getX() + this.width - 6; }
    ^///?} else
    /^@Override protected int getScrollbarPositionX() { return this.right - 6; }^/

    // Suppress vanilla 2-px gradient separator lines at the list's top/bottom edge (added in
    // 1.20.5 yarn) - they overlap our owa$drawSectionTints borders and read as duplicated lines.
    //? if >=1.20.5 {
    @Override protected void drawHeaderAndFooterSeparators(net.minecraft.client.gui.DrawContext ctx) {}
    //?}

    public abstract static class RowEntry extends ElementListWidget.Entry<RowEntry> {}

    public static class HeaderRow extends RowEntry {
        private final TextWidget label;
        HeaderRow(Text text) {
            this.label = new TextWidget(220, 20, text, MinecraftClient.getInstance().textRenderer);
        }
        @Override public List<? extends Selectable> selectableChildren() { return List.of(); }
        @Override public List<? extends Element> children() { return List.of(); }
        //? if >=1.21.9 {
        @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            label.setX(this.getX());
            label.setY(this.getY() + 4);
            label.render(context, mouseX, mouseY, tickDelta);
        }
        //?} else {
        /^@Override public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            label.setX(x);
            label.setY(y + 4);
            label.render(context, mouseX, mouseY, tickDelta);
        }
        ^///?}
    }

    public static class GroupHeaderRow extends RowEntry {
        private final FeatureGroup group;
        private final OwaConfigScreen owa;
        private final TextWidget label;
        private final CyclingButtonWidget<Preset> cycle;

        GroupHeaderRow(FeatureGroup g, OwaConfigScreen owa) {
            this.group = g;
            this.owa = owa;
            // Left-aligned (so center-mode doesn't push the text rightward into the cycle) AND
            // widened to 160 so long names like "Classic Human Physics" fully render without
            // ellipsis truncation. The cycle was shrunk from 150 → 130 to leave room.
            // alignLeft was removed in yarn 1.21.9+ (TextWidget defaults to left-aligned there).
            //? if <1.21.9 {
            this.label = new TextWidget(160, 20,
                    Text.literal(g.name), MinecraftClient.getInstance().textRenderer).alignLeft();
            //?} else {
            /^this.label = new TextWidget(160, 20,
                    Text.literal(g.name), MinecraftClient.getInstance().textRenderer);
            ^///?}
            //? if >=1.21.11 {
            this.cycle = CyclingButtonWidget.<Preset>builder(
                            p -> Text.literal(p == null ? "" : p.name),
                            () -> PresetManager.selectedFor(g))
                    .values(new ArrayList<>(g.presets))
                    .build(0, 0, 130, 20, Text.literal("Preset"),
                            (cb, val) -> {
                                PresetManager.selectPreset(g, val);
                                if (owa != null) owa.rebuildList();
                            });
            //?} else {
            /^this.cycle = CyclingButtonWidget.<Preset>builder(
                            p -> Text.literal(p == null ? "" : p.name))
                    .values(new ArrayList<>(g.presets))
                    .initially(PresetManager.selectedFor(g))
                    .build(0, 0, 130, 20, Text.literal("Preset"),
                            (cb, val) -> {
                                PresetManager.selectPreset(g, val);
                                if (owa != null) owa.rebuildList();
                            });
            ^///?}
        }

        @Override public List<? extends Selectable> selectableChildren() { return List.of(cycle); }
        @Override public List<? extends Element> children() { return List.of(cycle); }
        //? if >=1.21.9 {
        @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();
            int entryWidth = this.getWidth();
            label.setX(x);
            label.setY(y + 4);
            label.render(context, mouseX, mouseY, tickDelta);
            cycle.setX(x + entryWidth - 130);
            cycle.setY(y);
            cycle.render(context, mouseX, mouseY, tickDelta);
        }
        //?} else {
        /^@Override public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            label.setX(x);
            label.setY(y + 4);
            label.render(context, mouseX, mouseY, tickDelta);
            cycle.setX(x + entryWidth - 130);
            cycle.setY(y);
            cycle.render(context, mouseX, mouseY, tickDelta);
        }
        ^///?}
    }

    public static class ButtonRow extends RowEntry {
        private final ButtonWidget button;
        ButtonRow(ButtonWidget b) { this.button = b; }
        @Override public List<? extends Selectable> selectableChildren() { return List.of(button); }
        @Override public List<? extends Element> children() { return List.of(button); }
        //? if >=1.21.9 {
        @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int w = button.getWidth();
            button.setX(this.getX() + (this.getWidth() - w) / 2);
            button.setY(this.getY());
            button.render(context, mouseX, mouseY, tickDelta);
        }
        //?} else {
        /^@Override public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int w = button.getWidth();
            button.setX(x + (entryWidth - w) / 2);
            button.setY(y);
            button.render(context, mouseX, mouseY, tickDelta);
        }
        ^///?}
    }

    public static class WidgetRow extends RowEntry {
        private final Field field;
        private final ClickableWidget widget;
        private final ButtonWidget reset;
        WidgetRow(Field field, ClickableWidget w, ButtonWidget reset) { this.field = field; this.widget = w; this.reset = reset; }
        @Override public List<? extends Selectable> selectableChildren() { return List.of(widget, reset); }
        @Override public List<? extends Element> children() { return List.of(widget, reset); }
        //? if >=1.21.9 {
        @Override public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean enabled = isEnabled(field);
            int x = this.getX();
            int y = this.getY();
            int entryWidth = this.getWidth();
            widget.active = enabled;
            widget.setAlpha(enabled ? 1.0f : 0.5f);
            widget.setX(x);
            widget.setY(y);
            widget.render(context, mouseX, mouseY, tickDelta);
            reset.active = enabled && !PresetManager.fieldAtPresetValue(field);
            reset.setX(x + entryWidth - 40);
            reset.setY(y);
            reset.render(context, mouseX, mouseY, tickDelta);
        }
        //?} else {
        /^@Override public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            boolean enabled = isEnabled(field);
            widget.active = enabled;
            widget.setAlpha(enabled ? 1.0f : 0.5f);
            widget.setX(x);
            widget.setY(y);
            widget.render(context, mouseX, mouseY, tickDelta);
            reset.active = enabled && !PresetManager.fieldAtPresetValue(field);
            reset.setX(x + entryWidth - 40);
            reset.setY(y);
            reset.render(context, mouseX, mouseY, tickDelta);
        }
        ^///?}
    }
}
*///?}
