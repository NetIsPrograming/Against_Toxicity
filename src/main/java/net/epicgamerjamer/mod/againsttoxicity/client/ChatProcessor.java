package net.epicgamerjamer.mod.againsttoxicity.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatProcessor {
    @Unique
    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    public String msg;
    public String name;
    String address = (Objects.requireNonNull((Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())).getServerInfo()).address);

    public ChatProcessor(@NotNull String m) {
        msg = m.replace("\\", "")
                .replace("/", "")
                .replace("[", "")
                .replace("]", "")
                .replace("{", "")
                .replace("}", "")
                .replace("(", "")
                .replace(")", "")
                .replace("?", "")
                .replace("!", "")
                .replace("*", "")
                .replace(".", "")
                .replace(";", "")
                .replace(":", "")
                .replace("'", "")
                .replace("\"", "")
                .replace("|", "")
                .replace("@", "")
                .replace(",", "")
                .replace(".", "")
                .toLowerCase();
        name = NameHelper.getUsername(m);
        if (config.isDebug()) System.out.println("[AgainstToxicity] ChatProcessor - \"msg\" = " + msg);
        if (config.isDebug()) System.out.println("[AgainstToxicity] ChatProcessor - \"name\" = " + name);
    } // Constructor; also removes characters that screw up the ChatProcessor
    public int processChat() {
        for (int i = 0; i < new Lists().getIgnore().length; i++) {
            if (msg.contains(new Lists().getIgnore()[i])) {
                return 0;
            }
        }

        if (checkSlurs()) {
            return 2;
        } else if (checkToxic()) {
            return 1;
        } else {
            return 0;
        }
    } // Determines the toxicity level of a message; 2 means it has slurs, 1 means its toxic but no slurs, 0 means not toxic
    public boolean isPrivate() {
        if (!config.isPrivateDefault()) {
            String[] list = config.getPrivateServers();
            for (String s : list) {
                if (address.contains(s)) {
                    return true;
                }
            }
        } // true if server is in private overrides

        String[] pmList = {
                "-> you",
                "-> me",
                "<--"
        };
        for (String s : pmList) {
            if (msg.toLowerCase().contains(s)) {
                return true;
            }
        }
        // true if toxic message is determined to be a pm
        if (config.isPrivateDefault()) {
            String[] list = config.getPublicServers();
            for (String s : list) {
                if (address.contains(s)) {
                    return false;
                }
            }
            return true;
        } // false if server is in the public overrides, true if not

        return false; // false if none of the conditions are met (shouldn't occur but just in case)
    } // Checks certain conditions to determine whether to send the message privately or publicly
    private boolean checkToxic() {
        String[] list = new Lists().getToxicList(); // Single words; prevents false positives ("assist" flagged by "ass")
        String[] list2 = new Lists().getToxicList2(); // Phrases; doesn't flag without space ("urbad" = false, "ur bad" = true)
        String[] words = msg.toLowerCase().split(" "); // Converts message to array of lowercase strings

        // Matches whole words online
        for (String s : list) {
            for (String word : words) {
                if (s.matches(word)) {
                    return true;
                }
            }
        }

        // Matches phrases, must include spaces
        for (String s : list2) {
            if (msg.toLowerCase().contains(s)) {
                return true;
            }
        }

        return false;
    } // Return true if the 1+ word(s) matches an entry in list, OR true if the message contains any phrase in list2
    private boolean checkSlurs() {
        Pattern regex = Pattern.compile(String.join("|", new Lists().getSlurList()), Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(msg.replace(" ", "").replace(name.toLowerCase(), ""));

        return matcher.find();
    } // Return true if the chat message has a slur, ignores spaces (VERY sensitive)
}