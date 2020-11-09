package minegame159.meteorclient.modules.render.hud;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.Render2DEvent;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WLabel;
import minegame159.meteorclient.gui.widgets.WTable;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.modules.render.hud.modules.*;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.AlignmentX;
import minegame159.meteorclient.utils.AlignmentY;
import minegame159.meteorclient.utils.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class HUD extends ToggleModule {
    private static final HudRenderer RENDERER = new HudRenderer();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgActiveModules = settings.createGroup("Active Modules");
    private final SettingGroup sgInvViewer = settings.createGroup("Inventory Viewer");
    private final SettingGroup sgPlayerModel = settings.createGroup("Player Model");
    private final SettingGroup sgArmor = settings.createGroup("Armor");

    private final ActiveModulesHud activeModulesHud = new ActiveModulesHud(this);

    // General
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Scale of the HUD.")
            .defaultValue(1)
            .min(1)
            .max(3)
            .sliderMin(1)
            .sliderMax(3)
            .build()
    );

    private final Setting<Color> primaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("primary-color")
            .description("Primary color of text.")
            .defaultValue(new Color(255, 255, 255))
            .build()
    );

    private final Setting<Color> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("secondary-color")
            .description("Secondary color of text.")
            .defaultValue(new Color(175, 175, 175))
            .build()
    );

    private final Setting<Color> welcomeColor = sgGeneral.add(new ColorSetting.Builder()
            .name("welcome-color")
            .description("Color of welcome text.")
            .defaultValue(new Color(120, 43, 153))
            .build()
    );

    // Active Modules
    private final Setting<ActiveModulesHud.Sort> activeModulesSort = sgActiveModules.add(new EnumSetting.Builder<ActiveModulesHud.Sort>()
            .name("active-modules-sort")
            .description("How to sort active modules.")
            .defaultValue(ActiveModulesHud.Sort.ByBiggest)
            .onChanged(sort -> activeModulesHud.recalculate())
            .build()
    );

    // Inventory Viewer
    private final Setting<InventoryViewerHud.Background> invViewerBackground = sgInvViewer.add(new EnumSetting.Builder<InventoryViewerHud.Background>()
            .name("inventory-viewer-background")
            .description("Background of iventory viewer.")
            .defaultValue(InventoryViewerHud.Background.Light)
            .build()
    );

    private final Setting<Color> invViewerColor = sgInvViewer.add(new ColorSetting.Builder()
            .name("inventory-viewer-color")
            .description("Color of background on Flat mode.")
            .defaultValue(new Color(0, 0, 0, 64))
            .build()
    );

    private final Setting<Double> invViewerScale = sgInvViewer.add(new DoubleSetting.Builder()
            .name("inventory-viewer-scale")
            .description("Scale of inventory viewer.")
            .defaultValue(2)
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    // Player Model
    private final Setting<Boolean> playerModelBackground = sgPlayerModel.add(new BoolSetting.Builder()
            .name("player-model")
            .description("Displays a background behind your player model.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Color> playerModelColor = sgPlayerModel.add(new ColorSetting.Builder()
            .name("player-model-background-color")
            .description("Color of background.")
            .defaultValue(new Color(0, 0, 0, 64))
            .build()
    );

    private final Setting<Double> playerModelScale = sgPlayerModel.add(new DoubleSetting.Builder()
            .name("player-model-background-scale")
            .description("Scale of player model.")
            .defaultValue(2)
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    // Armor
    private final Setting<ArmorHud.Durability> armorDurability = sgArmor.add(new EnumSetting.Builder<ArmorHud.Durability>()
            .name("armor-durability")
            .description("How to display armor durability.")
            .defaultValue(ArmorHud.Durability.Default)
            .build()
    );

    private final Setting<Double> armorScale = sgArmor.add(new DoubleSetting.Builder()
            .name("armor-scale")
            .description("Scale of armor.")
            .defaultValue(2)
            .min(2)
            .max(4)
            .sliderMin(2)
            .sliderMax(4)
            .build()
    );

    public final List<HudModule> modules = new ArrayList<>();

    public HUD() {
        super(Category.Render, "new-HUD", "In game overlay.");

        init();
    }

    private void init() {
        modules.clear();
        RENDERER.setScale(scale());

        // Top Left
        HudModuleLayer topLeft = new HudModuleLayer(RENDERER, modules, AlignmentX.Left, AlignmentY.Top, 2, 2);
        topLeft.add(new WatermarkHud(this));
        topLeft.add(new FpsHud(this));
        topLeft.add(new PingHud(this));
        topLeft.add(new TpsHud(this));
        topLeft.add(new SpeedHud(this));
        topLeft.add(new BiomeHud(this));
        topLeft.add(new TimeHud(this));
        topLeft.add(new DurabilityHud(this));
        topLeft.add(new BreakingBlockHud(this));
        topLeft.add(new LookingAtHud(this));

        // Top Center
        HudModuleLayer topCenter = new HudModuleLayer(RENDERER, modules, AlignmentX.Center, AlignmentY.Top, 0, 2);
        topCenter.add(new InventoryViewerHud(this));
        topCenter.add(new WelcomeHud(this));
        topCenter.add(new LagNotifierHud(this));

        // Top Right
        HudModuleLayer topRight = new HudModuleLayer(RENDERER, modules, AlignmentX.Right, AlignmentY.Top, 2, 2);
        topRight.add(activeModulesHud);

        // Bottom Left
        HudModuleLayer bottomLeft = new HudModuleLayer(RENDERER, modules, AlignmentX.Left, AlignmentY.Bottom, 2, 2);
        bottomLeft.add(new PlayerModelHud(this));

        // Bottom Center
        HudModuleLayer bottomCenter = new HudModuleLayer(RENDERER, modules, AlignmentX.Center, AlignmentY.Bottom, 48, 64);
        bottomCenter.add(new ArmorHud(this));

        // Bottom Right
        HudModuleLayer bottomRight = new HudModuleLayer(RENDERER, modules, AlignmentX.Right, AlignmentY.Bottom, 2, 2);
        bottomRight.add(new PositionHud(this));
        bottomRight.add(new RotationHud(this));
        bottomRight.add(new PotionTimersHud(this));
    }

    @EventHandler
    public final Listener<Render2DEvent> onRender = new Listener<>(event -> {
        if (mc.options.debugEnabled) return;

        RENDERER.begin(scale());

        for (HudModule module : modules) {
            if (module.active || mc.currentScreen instanceof HudEditorScreen) {
                module.update(RENDERER);
                module.render(RENDERER);
            }
        }

        RENDERER.end();
    });

    @Override
    public WWidget getWidget() {
        WTable table = new WTable();

        WButton reset = table.add(new WButton("Reset")).getWidget();
        reset.action = this::init;
        table.add(new WLabel("Resets positions, do this after changing scale"));
        table.row();

        WButton editor = table.add(new WButton("Editor")).getWidget();
        editor.action = () -> mc.openScreen(new HudEditorScreen());
        table.add(new WLabel("Right click elements to toggle"));

        return table;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        ListTag modulesTag = new ListTag();
        for (HudModule module : modules) modulesTag.add(module.toTag());
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public ToggleModule fromTag(CompoundTag tag) {
        if (tag.contains("modules")) {
            ListTag modulesTag = tag.getList("modules", 10);

            for (Tag t : modulesTag) {
                CompoundTag moduleTag = (CompoundTag) t;

                HudModule module = getModule(moduleTag.getString("name"));
                if (module != null) module.fromTag(moduleTag);
            }
        }

        return super.fromTag(tag);
    }

    private HudModule getModule(String name) {
        for (HudModule module : modules) {
            if (module.name.equals(name)) return module;
        }

        return null;
    }

    public double scale() {
        return scale.get();
    }
    public Color primaryColor() {
        return primaryColor.get();
    }
    public Color secondaryColor() {
        return secondaryColor.get();
    }
    public Color welcomeColor() {
        return welcomeColor.get();
    }

    public ActiveModulesHud.Sort activeModulesSort() {
        return activeModulesSort.get();
    }

    public InventoryViewerHud.Background invViewerBackground() {
        return invViewerBackground.get();
    }
    public Color invViewerColor() {
        return invViewerColor.get();
    }
    public double invViewerScale() {
        return invViewerScale.get();
    }

    public boolean playerModelBackground() {
        return playerModelBackground.get();
    }
    public Color playerModelColor() {
        return playerModelColor.get();
    }
    public double playerModelScale() {
        return playerModelScale.get();
    }

    public ArmorHud.Durability armorDurability() {
        return armorDurability.get();
    }
    public double armorScale() {
        return armorScale.get();
    }
}