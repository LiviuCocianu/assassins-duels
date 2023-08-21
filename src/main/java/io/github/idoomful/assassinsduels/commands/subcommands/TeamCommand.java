package io.github.idoomful.assassinsduels.commands.subcommands;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TeamCommand implements SubcommandModel {
    @Override
    public void execute(DMain plugin, CommandSender sender, String[] args) {
        Player arg = sender instanceof Player ? (Player) sender : null;

        if (sender instanceof Player) {
            if (args.length > 1) {
                Player player = (Player) sender;
                DuelsPlayer dplayer = DuelsPlayer.fetch(player.getUniqueId());

                if(player.getName().equals(args[1])) {
                    player.sendMessage(MessagesYML.Errors.EXECUTE_ON_THEMSELVES.withPrefix(player));
                    return;
                }

                // If the target is offline
                if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(pl -> pl.getName().equalsIgnoreCase(args[1]))) {
                    switch(args[1]) {
                        case "decline":
                            if (dplayer.isInvited(DuelsPlayer.InviteType.TEAM)) {
                                DuelsPlayer dtarget = dplayer.getInvitation(DuelsPlayer.InviteType.TEAM);
                                dplayer.clearInvitation(DuelsPlayer.InviteType.TEAM);

                                dtarget.getPlayer().sendMessage(MessagesYML.INVITE_NOTIF_DECLINED_SENDER.withPrefix(dtarget.getPlayer())
                                        .replace("$name$", player.getName()));
                                player.sendMessage(MessagesYML.INVITE_NOTIF_DECLINED_TARGET.withPrefix(player)
                                        .replace("$name$", dtarget.getName()));

                                SettingsYML.SFX.INVITE_DECLINE_SOUND.playSound(player);
                            } else {
                                player.sendMessage(MessagesYML.Errors.NO_INVITE.withPrefix(player));
                            }
                            return;
                        case "leave":
                            if(dplayer.isPartOfTeam()) {
                                dplayer.leaveTeam();
                                player.sendMessage(MessagesYML.LEFT_TEAM.withPrefix(arg));
                            } else {
                                player.sendMessage(MessagesYML.Errors.NO_TEAM.withPrefix(arg));
                            }
                            return;
                        case "kick":
                            if (args.length > 2) {
                                if(dplayer.hasTeam()) {
                                    if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(pl -> pl.getName().equalsIgnoreCase(args[2]))) {
                                        player.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(player).replace("$player$", args[2]));
                                        return;
                                    }

                                    Player target = Bukkit.getPlayer(args[2]);
                                    DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                                    if(player.getName().equals(target.getName())) {
                                        player.sendMessage(MessagesYML.Errors.EXECUTE_ON_THEMSELVES.withPrefix(player));
                                        return;
                                    }

                                    if(dplayer.getTeammates().stream().noneMatch(m -> m.equals(dtarget))) {
                                        player.sendMessage(MessagesYML.Errors.NOT_IN_TEAM.withPrefix(arg)
                                                .replace("$name$", target.getName()));
                                        return;
                                    }

                                    DuelsPlayer.fetch(player.getUniqueId()).getTeammates().remove(dtarget);

                                    player.sendMessage(MessagesYML.KICKED_TEAMMATE.withPrefix(arg)
                                            .replace("$name$", target.getName()));
                                    target.sendMessage(MessagesYML.KICKED_TEAMMATE_TARGET.withPrefix(arg)
                                            .replace("$name$", player.getName()));
                                } else {
                                    if(dplayer.isPartOfTeam()) {
                                        player.sendMessage(MessagesYML.Errors.NOT_LEADER.withPrefix(player));
                                    } else {
                                        player.sendMessage(MessagesYML.Errors.NO_OWN_TEAM.withPrefix(arg));
                                    }
                                }
                            } else {
                                sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                                        .replace("$format$", "/duels team kick <player>"));
                            }
                            return;
                        case "list":
                            if(dplayer.hasTeam() || dplayer.isPartOfTeam()) {
                                if(!dplayer.hasTeam()) {
                                    if(!dplayer.isPartOfTeam())
                                        player.sendMessage(MessagesYML.Errors.NO_OWN_TEAM.withPrefix(arg));
                                } else {
                                    ArrayList<String> members = new ArrayList<>();
                                    members.add(Utils.color("&n" + player.getName()));
                                    dplayer.getTeammates().forEach(dp -> members.add(dp.getName()));
                                    String list = Utils.listStrings(members);

                                    player.sendMessage(MessagesYML.TEAM_LIST_LEADER.withPrefix(player)
                                            .replace("$list$", list));
                                    return;
                                }

                                if(!dplayer.isPartOfTeam()) {
                                    player.sendMessage(MessagesYML.Errors.NO_TEAM.withPrefix(arg));
                                } else {
                                    ArrayList<String> members = new ArrayList<>();
                                    members.add(Utils.color("&n" + dplayer.getTeamLeader().getName()));
                                    dplayer.getTeamLeader().getTeammates().forEach(dp -> members.add(dp.getName()));
                                    String list = Utils.listStrings(members);

                                    player.sendMessage(MessagesYML.TEAM_LIST_MEMBER.withPrefix(player)
                                            .replace("$list$", list));
                                    return;
                                }
                            } else {
                                player.sendMessage(MessagesYML.Errors.NO_TEAM.withPrefix(player));
                            }
                            return;
                        default:
                            player.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(player).replace("$player$", args[1]));
                    }

                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                DuelsPlayer dtarget = DuelsPlayer.fetch(target.getUniqueId());

                if(dplayer.getTeammates().stream().anyMatch(m -> m.getName().equals(target.getName()))) {
                    player.sendMessage(MessagesYML.Errors.CANNOT_INVITE_TEAMMATE.withPrefix(arg));
                    return;
                }

                // If the target is not in a team and the sender is not part of
                // another team other than theirs.
                if (!dtarget.hasTeam() && !dtarget.isPartOfTeam() && !dplayer.isPartOfTeam()) {
                    // If the sender is the one being invited
                    // Presume the sender is in the position to accept/reject
                    if (dplayer.isInvited(DuelsPlayer.InviteType.TEAM)) {
                        DuelsPlayer inviter = dplayer.getInvitation(DuelsPlayer.InviteType.TEAM);

                        if (!inviter.getPlayer().isOnline()) {
                            dplayer.clearInvitation(DuelsPlayer.InviteType.TEAM);
                            plugin.getPlayers().put(target.getUniqueId(), dplayer);
                            player.sendMessage(MessagesYML.Errors.INVITE_EXPIRED_OFFLINE.withPrefix(player)
                                    .replace("$name$", inviter.getName()));
                            return;
                        }

                        dplayer.acceptTeamInvitation();

                        inviter.getPlayer().sendMessage(MessagesYML.INVITE_NOTIF_ACCEPTED_SENDER.withPrefix(inviter.getPlayer())
                                .replace("$name$", player.getName()));
                        player.sendMessage(MessagesYML.INVITE_NOTIF_ACCEPTED_TARGET.withPrefix(player)
                                .replace("$name$", inviter.getName()));

                        SettingsYML.SFX.INVITE_ACCEPT_SOUND.playSound(inviter.getPlayer());
                        SettingsYML.SFX.INVITE_ACCEPT_SOUND.playSound(player);
                    } else {
                        // Set an invitation for target from the sender
                        dtarget.setInvitationOf(dplayer, DuelsPlayer.InviteType.TEAM);

                        for (String line : MessagesYML.TEAM_REQUEST_NOTIF.colorLines(player)) {
                            String l = line.replace("$inviter$", player.getName());
                            target.sendMessage(l);
                        }

                        SettingsYML.SFX.TEAM_INVITE_RECEIVE_SOUND.playSound(target);

                        // If target is online and still didn't accept, clear the invitation.
                        // It wouldn't make sense to clear it if the target is offline
                        // as the object will be deleted on disconnect.
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            if(dtarget.isInvited(DuelsPlayer.InviteType.TEAM)) {
                                if (plugin.getPlayers().containsKey(target.getUniqueId())) {
                                    dtarget.clearInvitation(DuelsPlayer.InviteType.TEAM);

                                    // If the inviter is still online, notify them
                                    if (plugin.getPlayers().containsKey(player.getUniqueId())) {
                                        player.sendMessage(MessagesYML.Errors.INVITE_EXPIRED.withPrefix(player)
                                                .replace("$name$", target.getName()));
                                    }
                                }
                            }
                        }, SettingsYML.Teams.INVITATION_EXPIRY.getInt() * 20);

                        player.sendMessage(MessagesYML.INVITE_NOTIF_SENT.withPrefix(player).replace("$name$", args[1]));
                    }
                } else {
                    player.sendMessage(MessagesYML.Errors.HAS_TEAM.withPrefix(player));
                }
            } else {
                sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                        .replace("$format$", "/duels team <player>"));
            }
        } else {
            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
        }
    }
}
