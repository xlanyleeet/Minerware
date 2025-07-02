package org.gr_code.minerware.builders;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.gr_code.minerware.manager.type.Utils;

public class ComponentBuilder {

    private final TextComponent textComponent;

    private ComponentBuilder(TextComponent textComponent){
        this.textComponent = textComponent;
    }

    public static ComponentBuilder newComponentBuilder(String string){
        return new ComponentBuilder(new TextComponent(Utils.translate(string)));
    }

    public ComponentBuilder withClickEvent(ClickEvent.Action paramAction, String string) {
        textComponent.setClickEvent(new ClickEvent(paramAction, string));
        return this;
    }

    public ComponentBuilder withHoverEvent(HoverEvent.Action action, String string){
        textComponent.setHoverEvent(new HoverEvent(action, new net.md_5.bungee.api.chat.ComponentBuilder(Utils.translate(string)).create()));
        return this;
    }

    public TextComponent build() {
        return textComponent;
    }
}


