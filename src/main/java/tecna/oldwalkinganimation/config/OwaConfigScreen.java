//? if >=26.1 || neoforge {
package tecna.oldwalkinganimation.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
//? if >=1.21.2 && <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class OwaConfigScreen extends Screen {
    private static final int FOOTER_HEIGHT = 36;
    private static final int PREVIEW_WIDTH_MAX = 160;
    private static final int PREVIEW_WIDTH_MIN = 110;
    private static final int MIN_CONTENT_WIDTH = 280;
    private static final int PREVIEW_BUTTON_H = 20;
    private final Screen parent;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final Map<String, CategoryTab> tabs = new LinkedHashMap<>();
    //? if <1.20.3 {
    /*// 1.20.2 mojmap: ContainerObjectSelectionList extends AbstractContainerEventHandler, not
    // AbstractWidget, so Tab.visitChildren(Consumer<AbstractWidget>) can't accept the list.
    // Register/unregister directly with the screen instead. TabManager calls visitChildren twice
    // per tab change (unload + load), so we toggle on each call.
    private final java.util.Set<OwaOptionsList> owa$registeredLists = new java.util.HashSet<>();
    void owa$toggleListRegistration(OwaOptionsList list) {
        if (owa$registeredLists.remove(list)) {
            this.removeWidget(list);
        } else {
            this.addRenderableWidget(list);
            owa$registeredLists.add(list);
        }
    }
    *///?}
    private final PreviewPanel preview = new PreviewPanel();
    private TabNavigationBar tabNav;
    private CycleButton<Boolean> advancedToggle;
    private int selectedIndex;
    private String owa$selectedCategory;
    private int previewX0, previewY0, previewX1, previewY1;
    private int owa$tabBottom = 32;
    private final List<Button> modeButtons = new ArrayList<>();

    public OwaConfigScreen(Screen parent) {
        super(Component.literal("Old Walking Animation Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        tabs.clear();
        for (String cat : entriesByCategory().keySet()) {
            tabs.put(cat, new CategoryTab(cat, this));
        }
        Tab[] tabArr = tabs.values().toArray(new Tab[0]);

        this.tabNav = TabNavigationBar.builder(this.tabManager, this.width).addTabs(tabArr).build();
        this.addRenderableWidget(this.tabNav);
        this.tabNav.arrangeElements();

        int tabBottom = this.tabNav.getRectangle().bottom();
        this.owa$tabBottom = tabBottom;

        // Scale footer button widths so the centered layout (Advanced + Reset + Done) fits within
        // the screen even at narrow widths. 26px reserved on the right for the QA corner toggle.
        int footerSpacing = 8;
        int footerAvail = Math.max(180, this.width - 32 - 26);
        int footerNominal = 130 + footerSpacing + 120 + footerSpacing + 120;
        int adv = 130, rst = 120, dn = 120;
        if (footerAvail < footerNominal) {
            adv = Math.max(60, footerAvail * 130 / footerNominal);
            rst = Math.max(50, footerAvail * 120 / footerNominal);
            dn  = Math.max(50, footerAvail * 120 / footerNominal);
        }
        LinearLayout footer = LinearLayout.horizontal().spacing(footerSpacing);
        //? if >=1.21.11 {
        this.advancedToggle = new CycleButton.Builder<Boolean>(
                        v -> Component.literal(v ? "On" : "Off"),
                        () -> PresetManager.advancedMode())
                .withValues(Boolean.TRUE, Boolean.FALSE)
                .create(0, 0, adv, 20, Component.literal("Advanced"),
                        (cb, val) -> {
                            PresetManager.setAdvancedMode(val);
                            rebuildList();
                        });
        //?} else {
        /*this.advancedToggle = new CycleButton.Builder<Boolean>(
                        v -> Component.literal(v ? "On" : "Off"))
                .withValues(Boolean.TRUE, Boolean.FALSE)
                .create(0, 0, adv, 20, Component.literal("Advanced"),
                        (cb, val) -> {
                            PresetManager.setAdvancedMode(val);
                            rebuildList();
                        });
        *///?}
        this.advancedToggle.setValue(PresetManager.advancedMode());
        footer.addChild(this.advancedToggle);
        footer.addChild(Button.builder(Component.literal("Reset All"), b -> {
            MidnightConfig.resetAll();
            PresetManager.resetAllToDefaults();
            rebuildList();
        }).width(rst).build());
        footer.addChild(Button.builder(Component.literal("Done"), b -> onClose()).width(dn).build());

        // "Quick Access" toggle in the bottom-right corner of the screen (in the footer band but
        // NOT part of the centered footer layout, so it doesn't push the Advanced/Reset/Done
        // buttons offscreen on narrow widths). Toggles the OWA Config shortcut button on title
        // and pause screens.
        Button qa = Button.builder(
                Component.literal(Config.showQuickAccess ? "✓" : "✗"),
                b -> {
                    Config.showQuickAccess = !Config.showQuickAccess;
                    b.setMessage(Component.literal(Config.showQuickAccess ? "✓" : "✗"));
                    MidnightConfig.save();
                })
                .bounds(this.width - 24, this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2, 20, 20)
                .build();
        qa.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Toggle the OWA Config shortcut button on the title and pause screens.")));
        addRenderableWidget(qa);
        footer.arrangeElements();
        footer.setX((this.width - footer.getWidth()) / 2);
        footer.setY(this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2);
        footer.visitChildren(child -> { if (child instanceof AbstractWidget w) addRenderableWidget(w); });

        int contentTop = tabBottom;
        int contentHeight = this.height - contentTop - FOOTER_HEIGHT;
        int available = this.width - MIN_CONTENT_WIDTH;
        int previewReserve = 0;
        if (available >= PREVIEW_WIDTH_MIN) {
            previewReserve = Math.min(PREVIEW_WIDTH_MAX, available);
        }
        this.tabManager.setTabArea(new ScreenRectangle(0, contentTop, this.width - previewReserve, contentHeight));

        buildPreviewArea(contentTop, contentHeight, previewReserve);

        // Restore the previously-selected tab across init() calls (window resize triggers init,
        // which would otherwise reset to the first tab). owa$selectedCategory is updated each
        // render from tabManager.getCurrentTab().
        if (owa$selectedCategory != null) {
            int i = 0;
            for (String cat : tabs.keySet()) {
                if (cat.equals(owa$selectedCategory)) { selectedIndex = i; break; }
                i++;
            }
        }
        if (selectedIndex < 0 || selectedIndex >= tabArr.length) selectedIndex = 0;
        this.tabNav.selectTab(selectedIndex, false);
    }

    private void buildPreviewArea(int contentTop, int contentHeight, int previewReserve) {
        modeButtons.clear();
        if (previewReserve <= 0) {
            previewX0 = previewY0 = previewX1 = previewY1 = 0;
            return;
        }

        int pad = 6;
        int panelLeft = this.width - previewReserve + pad;
        int panelRight = this.width - pad;

        PreviewPanel.Mode[] modes = PreviewPanel.Mode.values();
        int rows = (modes.length + 1) / 2;
        int buttonsBlockH = rows * (PREVIEW_BUTTON_H + 4) - 4;
        int buttonsTop = contentTop + contentHeight - buttonsBlockH;

        previewX0 = panelLeft;
        previewY0 = contentTop + pad;
        previewX1 = panelRight;
        previewY1 = buttonsTop - pad;

        int btnW = (panelRight - panelLeft - 4) / 2;
        for (int i = 0; i < modes.length; i++) {
            int row = i / 2;
            int col = i % 2;
            PreviewPanel.Mode m = modes[i];
            Button b = Button.builder(Component.literal(m.label), bb -> preview.setMode(m))
                    .bounds(panelLeft + col * (btnW + 4),
                            buttonsTop + row * (PREVIEW_BUTTON_H + 4),
                            btnW, PREVIEW_BUTTON_H)
                    .build();
            modeButtons.add(b);
            addRenderableWidget(b);
        }
        refreshModeButtons();
    }

    private void refreshModeButtons() {
        PreviewPanel.Mode cur = preview.getMode();
        for (int i = 0; i < modeButtons.size(); i++) {
            modeButtons.get(i).active = PreviewPanel.Mode.values()[i] != cur;
        }
    }

    private static final int TINT_HEADER = 0xA0101018;
    private static final int TINT_LIST = 0x60101018;
    private static final int TINT_PREVIEW = 0xA0181018;
    private static final int TINT_FOOTER = 0xA0101018;

    //? if >=26.1 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor gge, int mouseX, int mouseY, float pt) {
        owa$trackSelectedTab();
        owa$drawSectionTints(gge);
        super.extractRenderState(gge, mouseX, mouseY, pt);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            preview.render(gge, previewX0, previewY0, previewX1, previewY1, mouseX, mouseY);
        }
        // Vanilla footer separator drawn last so neither the option list rows nor the preview's
        // mode buttons render on top of it. Placed AT contentBottom so the 2px texture sits in
        // the first two rows of the footer area, just below the mode buttons.
        int contentBottom = this.height - FOOTER_HEIGHT;
        gge.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR,
            0, contentBottom, 0f, 0f, this.width, 2, 32, 2);
        refreshModeButtons();
    }

    private void owa$drawSectionTints(GuiGraphicsExtractor gge) {
        int contentBottom = this.height - FOOTER_HEIGHT;
        int contentRight = previewX1 > previewX0 ? (this.width - (previewX1 - previewX0) - 12) : this.width;
        gge.fill(0, 0, this.width, owa$tabBottom, TINT_HEADER);
        gge.fill(0, owa$tabBottom, contentRight, contentBottom, TINT_LIST);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            gge.fill(contentRight, owa$tabBottom, this.width, contentBottom, TINT_PREVIEW);
        }
        gge.fill(0, contentBottom, this.width, this.height, TINT_FOOTER);
    }
    //?} else {
    /*@Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float pt) {
        owa$drawSectionTints(gg);
        super.render(gg, mouseX, mouseY, pt);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            preview.render(gg, previewX0, previewY0, previewX1, previewY1, mouseX, mouseY);
        }
        // Vanilla footer separator drawn last so option list rows / mode buttons don't render on
        // top. Screen.FOOTER_SEPARATOR was added in 1.20.5 mojmap; on older versions fall back to
        // the legacy 1px black line.
        int contentBottom = this.height - FOOTER_HEIGHT;
        //? if >=1.21.6
        /^gg.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, contentBottom, 0f, 0f, this.width, 2, 32, 2);^/
        //? if >=1.21.2 && <1.21.6
        /^gg.blit(RenderType::guiTextured, Screen.FOOTER_SEPARATOR, 0, contentBottom, 0f, 0f, this.width, 2, 32, 2);^/
        //? if >=1.20.5 && <1.21.2
        /^gg.blit(Screen.FOOTER_SEPARATOR, 0, contentBottom, 0f, 0f, this.width, 2, 32, 2);^/
        //? if <1.20.5
        /^gg.fill(0, contentBottom, this.width, contentBottom + 1, 0xFF000000);^/
        refreshModeButtons();
    }
    *///?}

    //? if <26.1 {
    /*private void owa$drawSectionTints(GuiGraphics gg) {
        int contentBottom = this.height - FOOTER_HEIGHT;
        int contentRight = previewX1 > previewX0 ? (this.width - (previewX1 - previewX0) - 12) : this.width;
        gg.fill(0, 0, this.width, owa$tabBottom, TINT_HEADER);
        gg.fill(0, owa$tabBottom, contentRight, contentBottom, TINT_LIST);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            gg.fill(contentRight, owa$tabBottom, this.width, contentBottom, TINT_PREVIEW);
        }
        gg.fill(0, contentBottom, this.width, this.height, TINT_FOOTER);
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean beforeDrag) {
        if (super.mouseClicked(event, beforeDrag)) return true;
        double mx = event.x();
        double my = event.y();
        if (event.buttonInfo().button() == 0 && previewX1 > previewX0 && previewY1 > previewY0
            && mx >= previewX0 && mx < previewX1 && my >= previewY0 && my < previewY1
            && preview.mouseClicked(mx, my)) return true;
        return false;
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button == 0 && previewX1 > previewX0 && previewY1 > previewY0
            && mx >= previewX0 && mx < previewX1 && my >= previewY0 && my < previewY1
            && preview.mouseClicked(mx, my)) return true;
        return false;
    }
    *///?}

    // Read current tab from TabManager each frame and remember its category, so init() can
    // restore it after a window resize.
    private void owa$trackSelectedTab() {
        if (tabManager == null) return;
        Object cur = tabManager.getCurrentTab();
        if (cur instanceof CategoryTab ct) owa$selectedCategory = ct.category;
    }

    void rebuildList() {
        for (CategoryTab ct : tabs.values()) ct.list.rebuild();
    }

    @Override
    public void onClose() {
        MidnightConfig.save();
        tecna.oldwalkinganimation.OverlayTextureRefresher.refresh();
        preview.dispose();
        if (this.minecraft != null) this.minecraft.setScreen(this.parent);
    }

    static String displayName(String category) {
        return switch (category) {
            case "features" -> "Features";
            case "animation" -> "Animation";
            case "firstperson" -> "First Person";
            case "damage" -> "Damage";
            case "steves" -> "Classic Humans";
            default -> category;
        };
    }

    private static final Set<String> MIRROR_TO_FEATURES = Set.of(
            "bounce", "arms", "damage", "damageFlash", "enableArm", "enableSteve",
            "bodyRot", "headBob");
    // Fields that are persisted via @Entry but rendered through dedicated UI (e.g. the footer
    // toggle button) instead of the normal options list.
    private static final Set<String> HIDDEN_FROM_LIST = Set.of("showQuickAccess");

    static Map<String, List<Field>> entriesByCategory() {
        Map<String, List<Field>> out = new LinkedHashMap<>();
        Class<?> cls = MidnightConfig.getConfigClass();
        if (cls == null) return out;
        for (Field f : cls.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
            if (e == null) continue;
            if (HIDDEN_FROM_LIST.contains(f.getName())) continue;
            out.computeIfAbsent(e.category(), k -> new ArrayList<>()).add(f);
            if (MIRROR_TO_FEATURES.contains(f.getName())) {
                out.computeIfAbsent("features", k -> new ArrayList<>()).add(f);
            }
        }
        return out;
    }

    static String labelFor(Field f, MidnightConfig.Entry e) {
        String key = "oldwalkinganimation.midnightconfig." + f.getName();
        if (I18n.exists(key)) return I18n.get(key);
        return e.name().isEmpty() ? prettifyFieldName(f.getName()) : e.name();
    }

    static Component tooltipFor(Field f) {
        String key = "oldwalkinganimation.midnightconfig." + f.getName() + ".tooltip";
        if (I18n.exists(key)) return Component.literal(I18n.get(key));
        return null;
    }

    private static String prettifyFieldName(String raw) {
        if (raw.isEmpty()) return raw;
        StringBuilder out = new StringBuilder();
        out.append(Character.toUpperCase(raw.charAt(0)));
        for (int i = 1; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isUpperCase(c)) out.append(' ');
            out.append(c);
        }
        return out.toString();
    }

    static class CategoryTab implements Tab {
        private final OwaConfigScreen screen;
        private final String category;
        final OwaOptionsList list;

        CategoryTab(String cat, OwaConfigScreen screen) {
            this.screen = screen;
            this.category = cat;
            this.list = new OwaOptionsList(Minecraft.getInstance(), screen, cat);
        }

        @Override public Component getTabTitle() { return Component.literal(displayName(category)); }
        //? if >=26.1 || >=1.21.6 {
        @Override public Component getTabExtraNarration() { return Component.literal(displayName(category)); }
        //?}
        //? if >=1.20.3 {
        @Override public void visitChildren(Consumer<AbstractWidget> consumer) { consumer.accept(list); }
        //?} else {
        /*@Override public void visitChildren(Consumer<AbstractWidget> consumer) { screen.owa$toggleListRegistration(list); }
        *///?}
        @Override public void doLayout(ScreenRectangle rect) {
            //? if >=1.20.6 {
            list.updateSizeAndPosition(rect.width(), rect.height(), rect.top());
            //?} else {
            /*list.applyTabLayout(rect.width(), rect.height(), rect.top());
            *///?}
        }
    }
}
//?} else {
/*package tecna.oldwalkinganimation.config;

import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class OwaConfigScreen extends Screen {
    private static final int FOOTER_HEIGHT = 36;
    private static final int PREVIEW_WIDTH_MAX = 160;
    private static final int PREVIEW_WIDTH_MIN = 110;
    private static final int MIN_CONTENT_WIDTH = 280;
    private static final int PREVIEW_BUTTON_H = 20;
    private final Screen parent;
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    private final Map<String, CategoryTab> tabs = new LinkedHashMap<>();
    //? if <1.20.3 {
    /^// 1.20.2: EntryListWidget isn't a ClickableWidget, so CategoryTab.forEachChild can't push it
    // through Consumer<ClickableWidget>. Register/unregister via this set instead - TabManager
    // calls forEachChild twice per change (unload + load), so we toggle on each call.
    private final java.util.Set<OwaOptionsList> owa$registeredLists = new java.util.HashSet<>();
    void owa$toggleListRegistration(OwaOptionsList list) {
        if (owa$registeredLists.remove(list)) {
            this.remove(list);
        } else {
            this.addDrawable(list);
            this.addSelectableChild(list);
            owa$registeredLists.add(list);
        }
    }
    ^///?}
    private final PreviewPanel preview = new PreviewPanel();
    private TabNavigationWidget tabNav;
    private CyclingButtonWidget<Boolean> advancedToggle;
    private int selectedIndex;
    private String owa$selectedCategory;
    private int previewX0, previewY0, previewX1, previewY1;
    private int owa$tabBottom = 32;
    private final List<ButtonWidget> modeButtons = new ArrayList<>();

    public OwaConfigScreen(Screen parent) {
        super(Text.literal("Old Walking Animation Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        tabs.clear();
        for (String cat : entriesByCategory().keySet()) {
            tabs.put(cat, new CategoryTab(cat, this));
        }
        Tab[] tabArr = tabs.values().toArray(new Tab[0]);

        this.tabNav = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabArr).build();
        this.addDrawableChild(this.tabNav);
        this.tabNav.selectTab(0, false);
        this.tabNav.init();

        int tabBottom = this.tabNav.getNavigationFocus().getBottom();
        this.owa$tabBottom = tabBottom;

        // Scale footer button widths so the centered layout fits even at narrow widths. 26px
        // reserved on the right for the QA corner toggle.
        int footerSpacing = 8;
        int footerAvail = Math.max(180, this.width - 32 - 26);
        int footerNominal = 130 + footerSpacing + 120 + footerSpacing + 120;
        int adv = 130, rst = 120, dn = 120;
        if (footerAvail < footerNominal) {
            adv = Math.max(60, footerAvail * 130 / footerNominal);
            rst = Math.max(50, footerAvail * 120 / footerNominal);
            dn  = Math.max(50, footerAvail * 120 / footerNominal);
        }
        DirectionalLayoutWidget footer = DirectionalLayoutWidget.horizontal().spacing(footerSpacing);
        //? if >=1.21.11 {
        this.advancedToggle = CyclingButtonWidget.<Boolean>builder(
                        v -> Text.literal(v ? "On" : "Off"),
                        () -> PresetManager.advancedMode())
                .values(Boolean.TRUE, Boolean.FALSE)
                .build(0, 0, adv, 20, Text.literal("Advanced"),
                        (cb, val) -> {
                            PresetManager.setAdvancedMode(val);
                            rebuildList();
                        });
        //?} else {
        /^this.advancedToggle = CyclingButtonWidget.<Boolean>builder(
                        v -> Text.literal(v ? "On" : "Off"))
                .values(Boolean.TRUE, Boolean.FALSE)
                .initially(PresetManager.advancedMode())
                .build(0, 0, adv, 20, Text.literal("Advanced"),
                        (cb, val) -> {
                            PresetManager.setAdvancedMode(val);
                            rebuildList();
                        });
        ^///?}
        footer.add(this.advancedToggle);
        footer.add(ButtonWidget.builder(Text.literal("Reset All"), b -> {
            MidnightConfig.resetAll();
            PresetManager.resetAllToDefaults();
            rebuildList();
        }).width(rst).build());
        footer.add(ButtonWidget.builder(Text.literal("Done"), b -> close()).width(dn).build());

        // "Quick Access" toggle in the bottom-right corner of the screen (in the footer band but
        // NOT part of the centered footer layout, so it doesn't push the Advanced/Reset/Done
        // buttons offscreen on narrow widths). Toggles the OWA Config shortcut button on title
        // and pause screens.
        ButtonWidget qa = ButtonWidget.builder(
                Text.literal(Config.showQuickAccess ? "✓" : "✗"),
                b -> {
                    Config.showQuickAccess = !Config.showQuickAccess;
                    b.setMessage(Text.literal(Config.showQuickAccess ? "✓" : "✗"));
                    MidnightConfig.save();
                })
                .dimensions(this.width - 24, this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2, 20, 20)
                .build();
        qa.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.literal("Toggle the OWA Config shortcut button on the title and pause screens.")));
        addDrawableChild(qa);
        footer.refreshPositions();
        footer.setX((this.width - footer.getWidth()) / 2);
        footer.setY(this.height - FOOTER_HEIGHT + (FOOTER_HEIGHT - 20) / 2);
        footer.forEachChild(child -> addDrawableChild(child));

        int contentTop = tabBottom;
        int contentHeight = this.height - contentTop - FOOTER_HEIGHT;
        int available = this.width - MIN_CONTENT_WIDTH;
        int previewReserve = 0;
        if (available >= PREVIEW_WIDTH_MIN) {
            previewReserve = Math.min(PREVIEW_WIDTH_MAX, available);
        }
        this.tabManager.setTabArea(new ScreenRect(0, contentTop, this.width - previewReserve, contentHeight));

        buildPreviewArea(contentTop, contentHeight, previewReserve);

        // Restore the previously-selected tab across init() calls (window resize triggers init,
        // which would otherwise reset to the first tab). owa$selectedCategory is updated each
        // render from tabManager.getCurrentTab().
        if (owa$selectedCategory != null) {
            int i = 0;
            for (String cat : tabs.keySet()) {
                if (cat.equals(owa$selectedCategory)) { selectedIndex = i; break; }
                i++;
            }
        }
        if (selectedIndex < 0 || selectedIndex >= tabArr.length) selectedIndex = 0;
        this.tabNav.selectTab(selectedIndex, false);
    }

    private void buildPreviewArea(int contentTop, int contentHeight, int previewReserve) {
        modeButtons.clear();
        if (previewReserve <= 0) {
            previewX0 = previewY0 = previewX1 = previewY1 = 0;
            return;
        }

        int pad = 6;
        int panelLeft = this.width - previewReserve + pad;
        int panelRight = this.width - pad;

        PreviewPanel.Mode[] modes = PreviewPanel.Mode.values();
        int rows = (modes.length + 1) / 2;
        int buttonsBlockH = rows * (PREVIEW_BUTTON_H + 4) - 4;
        int buttonsTop = contentTop + contentHeight - buttonsBlockH;

        previewX0 = panelLeft;
        previewY0 = contentTop + pad;
        previewX1 = panelRight;
        previewY1 = buttonsTop - pad;

        int btnW = (panelRight - panelLeft - 4) / 2;
        for (int i = 0; i < modes.length; i++) {
            int row = i / 2;
            int col = i % 2;
            PreviewPanel.Mode m = modes[i];
            ButtonWidget b = ButtonWidget.builder(Text.literal(m.label), bb -> preview.setMode(m))
                    .dimensions(panelLeft + col * (btnW + 4),
                            buttonsTop + row * (PREVIEW_BUTTON_H + 4),
                            btnW, PREVIEW_BUTTON_H)
                    .build();
            modeButtons.add(b);
            addDrawableChild(b);
        }
        refreshModeButtons();
    }

    private void refreshModeButtons() {
        PreviewPanel.Mode cur = preview.getMode();
        for (int i = 0; i < modeButtons.size(); i++) {
            modeButtons.get(i).active = PreviewPanel.Mode.values()[i] != cur;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        owa$trackSelectedTab();
        owa$drawSectionTints(context);
        super.render(context, mouseX, mouseY, delta);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            preview.render(context, previewX0, previewY0, previewX1, previewY1, mouseX, mouseY);
        }
        // Vanilla footer separator drawn last so neither the option list rows nor the preview's
        // mode buttons (whose bottom edge sits at contentBottom) render on top of it. Placed AT
        // contentBottom so the 2px texture occupies the first two rows of the footer area
        // instead of straddling the boundary like CreateWorldScreen does. The
        // (RenderPipeline, Identifier, ...) drawTexture overload only exists at yarn >=1.21.6;
        // older fabric falls back to the legacy 1px black line in owa$drawSectionTints.
        //? if >=1.21.6 {
        int contentBottom = this.height - FOOTER_HEIGHT;
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
            net.minecraft.client.gui.screen.Screen.FOOTER_SEPARATOR_TEXTURE,
            0, contentBottom, 0f, 0f, this.width, 2, 32, 2);
        //?}
        refreshModeButtons();
    }

    private static final int TINT_HEADER = 0xA0101018;
    private static final int TINT_LIST = 0x60101018;
    private static final int TINT_PREVIEW = 0xA0181018;
    private static final int TINT_FOOTER = 0xA0101018;

    private void owa$drawSectionTints(DrawContext ctx) {
        int contentBottom = this.height - FOOTER_HEIGHT;
        int contentRight = previewX1 > previewX0 ? (this.width - (previewX1 - previewX0) - 12) : this.width;
        ctx.fill(0, 0, this.width, owa$tabBottom, TINT_HEADER);
        ctx.fill(0, owa$tabBottom, contentRight, contentBottom, TINT_LIST);
        if (previewX1 > previewX0 && previewY1 > previewY0) {
            ctx.fill(contentRight, owa$tabBottom, this.width, contentBottom, TINT_PREVIEW);
        }
        ctx.fill(0, contentBottom, this.width, this.height, TINT_FOOTER);
        // 1px black line at the boundary. On >=1.21.6 the vanilla FOOTER_SEPARATOR is drawn on
        // top of this in render() (covers it). On older fabric this is the only separator.
        ctx.fill(0, contentBottom, this.width, contentBottom + 1, 0xFF000000);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        //? if >=1.21.6 {
        this.applyBlur(context);
        //?} else if >=1.21.2 {
        /^this.applyBlur();^/
        //?} else if >=1.20.6
        /^this.applyBlur(delta);^/
        this.renderInGameBackground(context);
    }

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0 && previewX1 > previewX0 && previewY1 > previewY0
            && mouseX >= previewX0 && mouseX < previewX1 && mouseY >= previewY0 && mouseY < previewY1
            && preview.mouseClicked(mouseX, mouseY)) return true;
        return false;
    }
    //?} else {
    /^@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && previewX1 > previewX0 && previewY1 > previewY0
            && mouseX >= previewX0 && mouseX < previewX1 && mouseY >= previewY0 && mouseY < previewY1
            && preview.mouseClicked(mouseX, mouseY)) return true;
        return false;
    }
    ^///?}

    // Read current tab from TabManager each frame and remember its category, so init() can
    // restore it after a window resize.
    private void owa$trackSelectedTab() {
        if (tabManager == null) return;
        Object cur = tabManager.getCurrentTab();
        if (cur instanceof CategoryTab ct) owa$selectedCategory = ct.category;
    }

    void rebuildList() {
        for (CategoryTab ct : tabs.values()) ct.list.rebuild();
    }

    @Override
    public void close() {
        MidnightConfig.save();
        tecna.oldwalkinganimation.OverlayTextureRefresher.refresh();
        preview.dispose();
        if (this.client != null) this.client.setScreen(this.parent);
    }

    static String displayName(String category) {
        return switch (category) {
            case "features" -> "Features";
            case "animation" -> "Animation";
            case "firstperson" -> "First Person";
            case "damage" -> "Damage";
            case "steves" -> "Classic Humans";
            default -> category;
        };
    }

    private static final Set<String> MIRROR_TO_FEATURES = Set.of(
            "bounce", "arms", "damage", "damageFlash", "enableArm", "enableSteve",
            "bodyRot", "headBob");
    // Fields that are persisted via @Entry but rendered through dedicated UI (e.g. the footer
    // toggle button) instead of the normal options list.
    private static final Set<String> HIDDEN_FROM_LIST = Set.of("showQuickAccess");

    static Map<String, List<Field>> entriesByCategory() {
        Map<String, List<Field>> out = new LinkedHashMap<>();
        Class<?> cls = MidnightConfig.getConfigClass();
        if (cls == null) return out;
        for (Field f : cls.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            MidnightConfig.Entry e = f.getAnnotation(MidnightConfig.Entry.class);
            if (e == null) continue;
            if (HIDDEN_FROM_LIST.contains(f.getName())) continue;
            out.computeIfAbsent(e.category(), k -> new ArrayList<>()).add(f);
            if (MIRROR_TO_FEATURES.contains(f.getName())) {
                out.computeIfAbsent("features", k -> new ArrayList<>()).add(f);
            }
        }
        return out;
    }

    static String labelFor(Field f, MidnightConfig.Entry e) {
        String key = "oldwalkinganimation.midnightconfig." + f.getName();
        String fallback = e.name().isEmpty() ? prettifyFieldName(f.getName()) : e.name();
        return OwaLang.get(key, fallback);
    }

    static Text tooltipFor(Field f) {
        String key = "oldwalkinganimation.midnightconfig." + f.getName() + ".tooltip";
        if (!OwaLang.has(key)) return null;
        return Text.literal(OwaLang.get(key, ""));
    }

    private static String prettifyFieldName(String raw) {
        if (raw.isEmpty()) return raw;
        StringBuilder out = new StringBuilder();
        out.append(Character.toUpperCase(raw.charAt(0)));
        for (int i = 1; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isUpperCase(c)) out.append(' ');
            out.append(c);
        }
        return out.toString();
    }

    static class CategoryTab implements Tab {
        private final OwaConfigScreen screen;
        private final String category;
        final OwaOptionsList list;

        CategoryTab(String cat, OwaConfigScreen screen) {
            this.screen = screen;
            this.category = cat;
            this.list = new OwaOptionsList(MinecraftClient.getInstance(), screen, cat);
        }

        @Override public Text getTitle() { return Text.literal(displayName(category)); }
        //? if >=1.21.6
        @Override public Text getNarratedHint() { return Text.literal(displayName(category)); }
        //? if >=1.20.3 {
        @Override public void forEachChild(Consumer<ClickableWidget> consumer) { consumer.accept(list); }
        //?} else
        /^@Override public void forEachChild(Consumer<ClickableWidget> consumer) { screen.owa$toggleListRegistration(list); }^/
        @Override public void refreshGrid(ScreenRect rect) {
            //? if >=1.20.3 {
            list.setDimensionsAndPosition(rect.width(), rect.height(), rect.getLeft(), rect.getTop());
            //?} else
            /^list.applyTabLayout(rect.width(), rect.height(), rect.getTop());^/
            //? if >=1.21.9
            list.setScrollY(list.getScrollY());
        }
    }
}
*///?}
