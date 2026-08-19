package com.termux.app.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.droidshell.app.R;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** Applies the selected DroidShell chrome style without changing terminal rendering. */
public final class DroidShellStyleManager {

    public static final String PREFERENCES_FILE = "droidshell_ui_preferences";
    public static final String STYLE_KEY = "droidshell_ui_style";
    public static final String QUICK_ACTIONS_KEY = "droidshell_quick_actions";
    public static final String DEFAULT_STYLE = "obsidian";

    private DroidShellStyleManager() {
    }

    public static String getStyle(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .getString(STYLE_KEY, DEFAULT_STYLE);
    }

    public static void setStyle(Context context, String style) {
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .edit().putString(STYLE_KEY, style).apply();
    }

    public static Set<String> getQuickActions(Context context) {
        Set<String> defaults = new LinkedHashSet<>(Arrays.asList(
            "new_session", "keyboard", "toolbar", "read_only", "menu"));
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .getStringSet(QUICK_ACTIONS_KEY, defaults);
    }

    public static void setQuickActions(Context context, Set<String> actions) {
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            .edit().putStringSet(QUICK_ACTIONS_KEY, new LinkedHashSet<>(actions)).apply();
    }

    public static void apply(Activity activity) {
        Style style = Style.fromName(getStyle(activity));

        View root = activity.findViewById(R.id.activity_termux_root_view);
        View drawer = activity.findViewById(R.id.left_drawer);
        View toolbar = activity.findViewById(R.id.terminal_toolbar_view_pager);
        if (root != null) root.setBackgroundColor(style.background);
        if (drawer != null) drawer.setBackgroundColor(style.surface);
        if (toolbar != null) toolbar.setBackgroundColor(style.surface);

        applyTextColor(activity.findViewById(R.id.settings_button), style.text);
        applyTextColor(activity.findViewById(R.id.drawer_title), style.text);
        applyTextColor(activity.findViewById(R.id.drawer_subtitle), style.mutedText);
        applyTextColor(activity.findViewById(R.id.drawer_sessions_label), style.mutedText);
        applyTextColor(activity.findViewById(R.id.toggle_keyboard_button), style.text);
        applyTextColor(activity.findViewById(R.id.new_session_button), style.accent);

        View logoBadge = activity.findViewById(R.id.drawer_logo_badge);
        if (logoBadge != null) logoBadge.setBackgroundTintList(ColorStateList.valueOf(style.accent));
        applyTextColor(activity.findViewById(R.id.drawer_logo_letter), style.background);

        applyFloatingButton(activity.findViewById(R.id.open_drawer_button), style.surface, style.text);
        applyFloatingButton(activity.findViewById(R.id.quick_actions_button), style.accent, style.background);
    }

    /** Applies the same visual language to the settings screen chrome. */
    public static void applySettings(Activity activity) {
        Style style = Style.fromName(getStyle(activity));
        View content = activity.findViewById(android.R.id.content);
        View toolbar = activity.findViewById(com.termux.shared.R.id.toolbar);
        if (content != null) content.setBackgroundColor(style.background);
        if (toolbar != null) toolbar.setBackgroundColor(style.surface);
    }

    private static void applyTextColor(View view, int color) {
        if (view instanceof TextView) ((TextView) view).setTextColor(color);
    }

    private static void applyFloatingButton(View view, int background, int icon) {
        if (!(view instanceof FloatingActionButton)) return;
        FloatingActionButton button = (FloatingActionButton) view;
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setImageTintList(ColorStateList.valueOf(icon));
    }

    private enum Style {
        OBSIDIAN("obsidian", "#0B0F14", "#151C24", "#55D6FF", "#E5F2F7"),
        MATERIAL("material", "#121212", "#1F1F1F", "#80CBC4", "#FFFFFF"),
        CYBERPUNK("cyberpunk", "#08050D", "#1C1028", "#00E5FF", "#FFEA00"),
        MINIMAL("minimal", "#111111", "#202020", "#FFFFFF", "#E0E0E0"),
        AMOLED("amoled", "#000000", "#090909", "#6DEB89", "#FFFFFF");

        final String name;
        final int background;
        final int surface;
        final int accent;
        final int text;
        final int mutedText;

        Style(String name, String background, String surface, String accent, String text) {
            this.name = name;
            this.background = Color.parseColor(background);
            this.surface = Color.parseColor(surface);
            this.accent = Color.parseColor(accent);
            this.text = Color.parseColor(text);
            this.mutedText = Color.argb(180, Color.red(this.text), Color.green(this.text), Color.blue(this.text));
        }

        static Style fromName(String name) {
            for (Style style : values()) {
                if (style.name.equals(name)) return style;
            }
            return OBSIDIAN;
        }
    }
}
