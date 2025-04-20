package com.capstone.enableu.custom.dto;
import com.capstone.enableu.custom.enums.ShortcutDefault;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor (access = AccessLevel.PRIVATE)
@Getter
@Setter
public class ShortcutInfo {
    String name;
    String keyboard;

    public static List<ShortcutInfo> defaultShortcutInfos() {
        List<ShortcutInfo> shortcutInfos = new ArrayList<>();
        shortcutInfos.add(new ShortcutInfo(ShortcutDefault.VOICE_SEARCH.name(), "Ctrl + k"));
        shortcutInfos.add(new ShortcutInfo(ShortcutDefault.TYPE_SEARCH.name(), "/"));
        shortcutInfos.add(new ShortcutInfo(ShortcutDefault.NEXT_STEP.name(), "Ctrl + ArrowUp"));
        shortcutInfos.add(new ShortcutInfo(ShortcutDefault.BACK_STEP.name(), "Ctrl + ArrowDown"));
        shortcutInfos.add(new ShortcutInfo(ShortcutDefault.PLAY_PAUSE_TEXT_TO_SPEECH.name(), "Ctrl + Space"));
        return shortcutInfos;
    }
}
