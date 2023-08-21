package io.github.idoomful.assassinsduels.configuration;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public enum MessagesYML {
    PREFIX("prefix"),
    RELOAD("reload"),
    ARENA_SETUP("arena-setup"),
    ARENA_SETUP_1("arena-setup-1"),
    ARENA_SETUP_2("arena-setup-2"),
    ARENA_SETUP_3("arena-setup-3"),
    SETUP_COMPLETE("setup-complete"),
    SETUP_CANCEL("setup-cancel"),
    POSITION_SAVED("position-saved"),
    ARENA_DELETED("arena-deleted"),
    ARENA_PRIORITY("arena-priority"),
    ARENA_RENAME("arena-rename"),
    ARENA_LIST("arena-list"),
    ENTRY_DELETED("entry-deleted"),
    TEAM_LIST_LEADER("team-list-leader"),
    TEAM_LIST_MEMBER("team-list-member"),
    TEAM_REQUEST_NOTIF("team-request-notif"),
    DUEL_REQUEST_NOTIF("duel-request-notif"),
    INVITE_NOTIF_SENT("invite-notif-sent"),
    INVITE_NOTIF_ACCEPTED_SENDER("invite-notif-accepted-sender"),
    INVITE_NOTIF_ACCEPTED_TARGET("invite-notif-accepted-target"),
    INVITE_NOTIF_DECLINED_SENDER("invite-notif-declined-sender"),
    INVITE_NOTIF_DECLINED_TARGET("invite-notif-declined-target"),
    KICKED_TEAMMATE("kicked-teammate"),
    KICKED_TEAMMATE_TARGET("kicked-teammate-target"),
    KICKED_DUEL_PLAYER("kicked-duel-player"),
    KICKED_SPECTATOR_PLAYER("kicked-spectator-player"),
    JOINED_DUEL_PLAYER("joined-duel-player"),
    LEFT_TEAM("left-team"),
    MATCH_STARTING("match-starting"),
    MATCH_COUNTDOWN("match-countdown"),
    MATCH_START("match-start"),
    PLAYER_ELIMINATED("player-eliminated"),
    PLAYER_SPECTATOR("player-spectator"),
    JOINED_SPECTATOR("joined-spectator"),
    BET_SUBMIT("bet-submit"),
    PLAYER_WON("player-won"),
    PLAYER_WON_BETS("player-won-bets"),
    TEAM_WON("team-won"),
    TEAM_WON_BETS("team-won-bets"),
    RETURNED_BETS("returned-bets"),
    NO_MATCH_WARNING("no-match-warning"),
    LEFT_SPECTATOR("left-spectator"),
    CANCELED_MATCH("canceled-match"),
    STARTED_MATCH("started-match"),
    PAUSED_MATCH("paused-match"),
    RESUMED_MATCH("resumed-match"),
    SKIP_NOTIFY("skip-notify"),
    SKIPPED_COUNTDOWN("skipped-countdown");

    String output;
    FileConfiguration messages;

    MessagesYML(String output) {
        messages = DMain.getInstance().getConfigs().getFile("messages");
        this.output = "messages." + output;
    }

    public String withPrefix(Player player) {
        String text = MessagesYML.PREFIX.color(player) + messages.getString(output);
        return Utils.placeholder(player, text);
    }

    public String color(Player player) {
        return Utils.placeholder(player, messages.getString(output));
    }

    public List<String> colorLines(Player player) {
        return Utils.placeholder(player, messages.getStringList(output));
    }

    public void reload() {
        DMain.getInstance().getConfigs().reloadConfigs();
        messages = DMain.getInstance().getConfigs().getFile("messages");
    }

    public enum Syntax {
        ELEMENT_PREFIX("element-prefix"),
        SEPARATOR("separator");

        String output;
        FileConfiguration messages;

        Syntax(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "syntax." + output;
        }

        public String color(Player player) {
            return Utils.placeholder(player, messages.getString(output));
        }

        public enum MatchStatus {
            STANDBY("standby"),
            STARTING("starting"),
            ONGOING("ongoing"),
            FINISHED("finished"),
            PAUSED("paused");

            String output;
            FileConfiguration messages;

            MatchStatus(String output) {
                messages = MessagesYML.PREFIX.messages;
                this.output = "syntax.match-status." + output;
            }

            public String color(Player player) {
                return Utils.placeholder(player, messages.getString(output));
            }
        }
    }

    public enum Errors {
        NO_PERMISSION("no-permission"),
        INCORRECT_COMMAND("incorrect-command"),
        NOT_PLAYER("not-player"),
        NOT_ONLINE("not-online"),
        NOT_NUMBER("not-number"),
        NUMBER_LIMIT("number-limit"),
        INVALID_ARENA_TYPE("invalid-arena-type"),
        INVALID_ARENA("invalid-arena"),
        INVALID_PLAYER("invalid-player"),
        ARENA_EXISTS("arena-exists"),
        NO_ARENAS("no-arenas"),
        NO_NEGATIVE("no-negative"),
        HAS_TEAM("has-team"),
        NO_OWN_TEAM("no-own-team"),
        NO_TEAM("no-team"),
        NOT_LEADER("not-leader"),
        NOT_IN_TEAM("not-in-team"),
        NOT_IN_MATCH("not-in-match"),
        PLAYER_NOT_IN_MATCH("player-not-in-match"),
        BET_NOT_IN_MATCH("bet-not-in-match"),
        BET_CANNOT_LEAVE("bet-cannot-leave"),
        NOT_SPECTATOR("not-spectator"),
        ALREADY_SPECTATOR("already-spectator"),
        CANNOT_INVITE_TEAMMATE("cannot-invite-teammate"),
        CANNOT_INVITE("cannot-invite"),
        NO_INVITE("no-invite"),
        INVITE_EXPIRED("invite-expired"),
        INVITE_EXPIRED_OFFLINE("invite-expired-offline"),
        INSUFFICIENT_TEAMMATES_SENDER("insufficient-teammates-sender"),
        INSUFFICIENT_TEAMMATES_TARGET("insufficient-teammates-target"),
        NO_AVAILABLE_MATCH("no-available-match"),
        TEAMMATE_HIT("teammate-hit"),
        PAUSE_HIT("pause-hit"),
        PLAYER_NOT_DUELLER("player-not-dueller"),
        NOT_DUELLER("not-dueller"),
        ALREADY_BET("already-bet"),
        CANNOT_BET("cannot-bet"),
        EXECUTE_ON_THEMSELVES("execute-on-themselves"),
        NO_MATCH("no-match"),
        NOT_ONGOING("not-ongoing"),
        NOT_STARTING("not-starting"),
        DUEL_ALT_DETECTED("duel-alt-detected"),
        COMMANDS_NOT_ALLOWED("commands-not-allowed"),
        ALREADY_PLAYING("already-playing"),
        NO_PARTICIPANTS("no-participants");

        String output;
        FileConfiguration messages;

        Errors(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "errors." + output;
        }

        public String withPrefix(Player player) {
            String text = MessagesYML.PREFIX.color(player) + messages.getString(output);
            return Utils.placeholder(player, text);
        }

        public String color(Player player) {
            String text = messages.getString(output);
            return Utils.placeholder(player, text);
        }
    }

    public enum Lists {
        PLAYER_HELP("player-help"),
        ADMIN_HELP("admin-help"),
        HELP("help"),
        ARENA_INFO("arena-info"),
        MATCH_INFO("match-info");

        String output;
        FileConfiguration messages;

        Lists(String output) {
            messages = MessagesYML.PREFIX.messages;
            this.output = "lists." + output;
        }

        public List<String> getStringList(Player player) {
            return Utils.placeholder(player, messages.getStringList(output));
        }
    }
}
