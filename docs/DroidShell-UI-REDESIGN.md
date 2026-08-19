# DroidShell UI Redesign

## Baseline

The visual redesign starts from the `ui-redesign` branch. Phase 0 does not
change application behavior or terminal input/output.

Current UI reference files:

- `app/src/main/res/layout/activity_termux.xml`
- `app/src/main/res/layout/item_terminal_sessions_list.xml`
- `app/src/main/res/layout/item_terminal_sessions_list_group.xml`
- `app/src/main/res/layout/view_terminal_toolbar_extra_keys.xml`
- `app/src/main/res/layout/view_terminal_toolbar_text_input.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/styles.xml`
- `app/src/main/res/values/attrs.xml`

Current layout characteristics:

- Terminal content occupies the main area.
- The session drawer is 240 dp wide.
- The terminal toolbar uses a 37.5 dp ViewPager.
- Session rows use a simple MaterialTextView.
- The drawer contains settings, session list, keyboard toggle and new-session
  actions.
- Terminal colors are controlled separately by the terminal theme and
  Termux:Styling-compatible files.

## Safety Rules

- Do not modify `terminal-emulator` or `terminal-view` behavior during the
  visual phases.
- Do not change session switching, keyboard input, shell execution or plugin
  intents while changing layouts.
- Keep each phase in a separate commit.
- Build the APK after every phase.
- Test the existing read-only mode, gestures, sessions and bootstrap before
  merging the next phase.
- Keep the current visual behavior as the fallback theme.

## Planned Visual System

The first implementation will add a selectable visual style stored in app
preferences. The initial styles will be:

- Obsidian Console: dark blue-black surfaces with cyan accent.
- Material You: Android dynamic colors when available.
- Cyberpunk: cyan, green and magenta accents.
- Minimal Pro: neutral dark surfaces and restrained accents.
- AMOLED: black surfaces for OLED displays.

The selected style will affect DroidShell chrome first: drawer, toolbar,
buttons, session rows, menus and settings. Terminal text colors remain
independent so existing terminal color configurations are not overwritten.

## Phase Order

1. Theme manager, design tokens and style selector.
2. Session drawer and session cards.
3. Terminal toolbar and extra keys.
4. Settings and bootstrap screen.
5. Accessibility, responsive layouts, builds and device tests.
