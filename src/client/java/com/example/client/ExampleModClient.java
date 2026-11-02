package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExampleModClient implements ClientModInitializer {

    // Коды клавиш
    public static final int KEY_PRESS = 1;
    public static final int KEY_ESCAPE = 256;
    public static final int KEY_DELETE = 261;
    public static final int KEY_RIGHT_SHIFT = 344;
    public static final int KEY_LEFT_SHIFT = 340;
    public static final int KEY_LEFT_CONTROL = 341;
    public static final int KEY_RIGHT_CONTROL = 345;
    public static final int KEY_LEFT_ALT = 342;
    public static final int KEY_SPACE = 32;
    public static final int KEY_TAB = 258;
    public static final int KEY_CAPS_LOCK = 280;
    public static final int KEY_Z = 90;
    public static final int KEY_V = 86;
    public static final int KEY_B = 66;

    // Настройки PvP функций
    public static boolean swordVisuals = true;
    public static int swingMode = 0; // 0: Плавный PvP, 1: Жесткий Слэш, 2: Спин 360, 3: Классика
    public static int scaleMode = 1; // 0: Мини (60%), 1: PvP (75%), 2: Средний (90%), 3: Большой (115%)
    public static int posMode = 0;   // 0: Далеко PvP, 1: Низко, 2: По центру, 3: Стандарт
    public static boolean noHurtCam = true;
    public static boolean autoSprint = true;
    public static boolean fullBright = true;

    // Бинды по умолчанию (Меню = Right Shift, Перл = Z, Визуалы = V, Анти-тряска = B)
    public static int menuBind = KEY_RIGHT_SHIFT;
    public static int pearlBind = KEY_Z;
    public static int visualsBind = KEY_V;
    public static int hurtCamBind = KEY_B;
    public static int sprintBind = 0;
    public static int brightBind = 0;

    private static final Map<Integer, Boolean> keyStates = new HashMap<>();

    private static Method glfwGetCurrentContext;
    private static Method glfwGetKey;
    private static Method glfwGetMouseButton;
    private static Method glfwGetKeyName;

    static {
        try {
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            glfwGetCurrentContext = glfw.getMethod("glfwGetCurrentContext");
            glfwGetKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            glfwGetMouseButton = glfw.getMethod("glfwGetMouseButton", long.class, int.class);
            glfwGetKeyName = glfw.getMethod("glfwGetKeyName", int.class, int.class);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ExampleModClient::onClientTick);
    }

    public static void onClientTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;

        // 1. Авто-спринт
        if (autoSprint && mc.options.forwardKey.isPressed() && !mc.player.isSneaking() && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }

        // 2. FullBright (Вечное ночное зрение)
        if (fullBright) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
        } else if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }

        // 3. NoHurtCam (Отключение тряски камеры при ударах)
        if (noHurtCam) {
            mc.player.hurtTime = 0;
        }

        long window = getWindow();
        if (window == 0L || mc.currentScreen != null) return;

        // 4. Открытие менюшки на Right Shift
        if (isJustPressed(window, menuBind)) {
            mc.setScreen(new TuftuMenuScreen());
            return;
        }

        // 5. Срабатывание биндов в игре
        if (isJustPressed(window, pearlBind)) {
            throwPearl(mc);
        }
        if (isJustPressed(window, visualsBind)) {
            swordVisuals = !swordVisuals;
            mc.player.sendMessage(Text.literal("§b[Tuftu Visuals] §fВизуалы меча: " + (swordVisuals ? "§aВКЛ" : "§cВЫКЛ")), true);
        }
        if (isJustPressed(window, hurtCamBind)) {
            noHurtCam = !noHurtCam;
            mc.player.sendMessage(Text.literal("§b[Tuftu Visuals] §fNoHurtCam: " + (noHurtCam ? "§aВКЛ" : "§cВЫКЛ")), true);
        }
        if (isJustPressed(window, sprintBind)) {
            autoSprint = !autoSprint;
            mc.player.sendMessage(Text.literal("§b[Tuftu Visuals] §fAutoSprint: " + (autoSprint ? "§aВКЛ" : "§cВЫКЛ")), true);
        }
        if (isJustPressed(window, brightBind)) {
            fullBright = !fullBright;
            mc.player.sendMessage(Text.literal("§b[Tuftu Visuals] §fFullBright: " + (fullBright ? "§aВКЛ" : "§cВЫКЛ")), true);
        }
    }

    private static long getWindow() {
        try {
            return glfwGetCurrentContext != null ? (long) glfwGetCurrentContext.invoke(null) : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private static int getKeyState(long window, int key) {
        try {
            return glfwGetKey != null ? (int) glfwGetKey.invoke(null, window, key) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int getMouseState(long window, int button) {
        try {
            return glfwGetMouseButton != null ? (int) glfwGetMouseButton.invoke(null, window, button) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isJustPressed(long window, int key) {
        if (key == 0) return false;
        boolean down = (key < 0) ? (getMouseState(window, -key) == KEY_PRESS) : (getKeyState(window, key) == KEY_PRESS);
        boolean wasDown = keyStates.getOrDefault(key, false);
        keyStates.put(key, down);
        return down && !wasDown;
    }

    private static void throwPearl(MinecraftClient mc) {
        int prevSlot = getSelectedSlot(mc);
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
                setSelectedSlot(mc, i);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                setSelectedSlot(mc, prevSlot);
                mc.player.sendMessage(Text.literal("§d[Tuftu Visuals] §fЭндер-Перл брошен!"), true);
                return;
            }
        }
        mc.player.sendMessage(Text.literal("§c[Tuftu Visuals] Нет Эндер-Перла в хотбаре!"), true);
    }

    private static int getSelectedSlot(MinecraftClient mc) {
        try {
            Method m = mc.player.getInventory().getClass().getMethod("getSelectedSlot");
            return (int) m.invoke(mc.player.getInventory());
        } catch (Exception e) {
            try {
                Field f = mc.player.getInventory().getClass().getDeclaredField("selectedSlot");
                f.setAccessible(true);
                return f.getInt(mc.player.getInventory());
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    private static void setSelectedSlot(MinecraftClient mc, int slot) {
        try {
            Method m = mc.player.getInventory().getClass().getMethod("setSelectedSlot", int.class);
            m.invoke(mc.player.getInventory(), slot);
        } catch (Exception e) {
            try {
                Field f = mc.player.getInventory().getClass().getDeclaredField("selectedSlot");
                f.setAccessible(true);
                f.setInt(mc.player.getInventory(), slot);
            } catch (Exception ignored) {
            }
        }
    }

    public static String getKeyName(int key) {
        if (key == 0) return "§7НЕТ";
        if (key < 0) return "§bMOUSE " + (-key + 1);
        switch (key) {
            case KEY_RIGHT_SHIFT: return "§bR-SHIFT";
            case KEY_LEFT_SHIFT: return "§bL-SHIFT";
            case KEY_LEFT_CONTROL: return "§bL-CTRL";
            case KEY_RIGHT_CONTROL: return "§bR-CTRL";
            case KEY_LEFT_ALT: return "§bL-ALT";
            case KEY_SPACE: return "§bSPACE";
            case KEY_TAB: return "§bTAB";
            case KEY_CAPS_LOCK: return "§bCAPS";
            default:
                try {
                    String name = glfwGetKeyName != null ? (String) glfwGetKeyName.invoke(null, key, 0) : null;
                    if (name != null) return "§b" + name.toUpperCase();
                } catch (Exception ignored) {
                }
                return "§bKEY_" + key;
        }
    }

    // ================= МЕНЮШКА НА RIGHT SHIFT =================
    public static class TuftuMenuScreen extends Screen {
        private int waitingForBind = 0;

        public TuftuMenuScreen() {
            super(Text.literal("§b§lTuftu Visuals PvP Menu"));
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = this.height / 2 - 95;

            String[] swingNames = {"§dПлавный PvP", "§cЖесткий Слэш", "§eСпин 360°", "§7Классика"};
            String[] scaleNames = {"§bМини (60%)", "§aPvP (75%)", "§eСредний (90%)", "§cБольшой (115%)"};
            String[] posNames = {"§aДалеко PvP", "§bНизко", "§eПо центру", "§7Стандарт"};

            // 1. Визуалы меча + Бинд
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Визуалы Меча: " + (swordVisuals ? "§a§lВКЛ" : "§c§lВЫКЛ")),
                    btn -> { swordVisuals = !swordVisuals; refresh(); }
            ).dimensions(centerX - 155, startY, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 2 ? "§e> Нажмите кнопку <" : "Бинд: " + getKeyName(visualsBind)),
                    btn -> { waitingForBind = 2; refresh(); }
            ).dimensions(centerX + 50, startY, 105, 20).build());

            // 2. Анимация удара меча
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Анимация удара: " + swingNames[swingMode]),
                    btn -> { swingMode = (swingMode + 1) % swingNames.length; refresh(); }
            ).dimensions(centerX - 155, startY + 24, 310, 20).build());

            // 3. Размер и Позиция меча
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Размер: " + scaleNames[scaleMode]),
                    btn -> { scaleMode = (scaleMode + 1) % scaleNames.length; refresh(); }
            ).dimensions(centerX - 155, startY + 48, 152, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Позиция: " + posNames[posMode]),
                    btn -> { posMode = (posMode + 1) % posNames.length; refresh(); }
            ).dimensions(centerX + 3, startY + 48, 152, 20).build());

            // 4. Бинд на Эндер-Перл
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§d§lБросок Эндер-Перла (Авто-свап)"),
                    btn -> { waitingForBind = 1; refresh(); }
            ).dimensions(centerX - 155, startY + 76, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 1 ? "§e> Нажмите кнопку <" : "Бинд: " + getKeyName(pearlBind)),
                    btn -> { waitingForBind = 1; refresh(); }
            ).dimensions(centerX + 50, startY + 76, 105, 20).build());

            // 5. NoHurtCam + Бинд
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("NoHurtCam (Анти-тряска): " + (noHurtCam ? "§a§lВКЛ" : "§c§lВЫКЛ")),
                    btn -> { noHurtCam = !noHurtCam; refresh(); }
            ).dimensions(centerX - 155, startY + 100, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 6 ? "§e> Нажмите кнопку <" : "Бинд: " + getKeyName(hurtCamBind)),
                    btn -> { waitingForBind = 6; refresh(); }
            ).dimensions(centerX + 50, startY + 100, 105, 20).build());

            // 6. AutoSprint + Бинд
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("AutoSprint (Авто-бег): " + (autoSprint ? "§a§lВКЛ" : "§c§lВЫКЛ")),
                    btn -> { autoSprint = !autoSprint; refresh(); }
            ).dimensions(centerX - 155, startY + 124, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 3 ? "§e> Нажмите кнопку <" : "Бинд: " + getKeyName(sprintBind)),
                    btn -> { waitingForBind = 3; refresh(); }
            ).dimensions(centerX + 50, startY + 124, 105, 20).build());

            // 7. FullBright + Бинд
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("FullBright (Яркость): " + (fullBright ? "§a§lВКЛ" : "§c§lВЫКЛ")),
                    btn -> { fullBright = !fullBright; refresh(); }
            ).dimensions(centerX - 155, startY + 148, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 4 ? "§e> Нажмите кнопку <" : "Бинд: " + getKeyName(brightBind)),
                    btn -> { waitingForBind = 4; refresh(); }
            ).dimensions(centerX + 50, startY + 148, 105, 20).build());

            // 8. Кнопка открытия меню + Закрыть
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(waitingForBind == 5 ? "§e> Нажмите кнопку меню <" : "Кнопка меню: " + getKeyName(menuBind)),
                    btn -> { waitingForBind = 5; refresh(); }
            ).dimensions(centerX - 155, startY + 176, 180, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§a§lГотово"),
                    btn -> this.close()
            ).dimensions(centerX + 30, startY + 176, 125, 20).build());
        }

        private void refresh() {
            this.clearChildren();
            this.init();
        }

        @Override
        public void tick() {
            if (waitingForBind == 0) return;
            long window = getWindow();
            if (window == 0L) return;

            if (getKeyState(window, KEY_ESCAPE) == KEY_PRESS || getKeyState(window, KEY_DELETE) == KEY_PRESS) {
                if (waitingForBind != 5) applyBind(0);
                waitingForBind = 0;
                refresh();
                return;
            }

            for (int key = 32; key <= 348; key++) {
                if (getKeyState(window, key) == KEY_PRESS) {
                    applyBind(key);
                    keyStates.put(key, true);
                    waitingForBind = 0;
                    refresh();
                    return;
                }
            }

            for (int m = 2; m <= 4; m++) {
                if (getMouseState(window, m) == KEY_PRESS) {
                    applyBind(-m);
                    keyStates.put(-m, true);
                    waitingForBind = 0;
                    refresh();
                    return;
                }
            }
        }

        private void applyBind(int key) {
            if (waitingForBind == 1) pearlBind = key;
            else if (waitingForBind == 2) visualsBind = key;
            else if (waitingForBind == 3) sprintBind = key;
            else if (waitingForBind == 4) brightBind = key;
            else if (waitingForBind == 5 && key != 0) menuBind = key;
            else if (waitingForBind == 6) hurtCamBind = key;
        }
    }
}
