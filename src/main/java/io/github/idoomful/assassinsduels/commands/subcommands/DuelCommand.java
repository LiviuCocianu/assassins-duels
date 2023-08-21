package io.github.idoomful.assassinsduels.commands.subcommands;

import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.configuration.MessagesYML;
import io.github.idoomful.assassinsduels.configuration.SettingsYML;
import io.github.idoomful.assassinsduels.match.Match;
import io.github.idoomful.assassinsduels.match.MatchManager;
import io.github.idoomful.assassinsduels.player.DuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements SubcommandModel {
    @Override
    public void execute(DMain plugin, CommandSender sender, String[] args) {
        final String pluginName = plugin.getDescription().getName();
        final String pluginNameLower = pluginName.toLowerCase();

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
                    if(args[1].equalsIgnoreCase("decline")) {
                        if (dplayer.isInvited(DuelsPlayer.InviteType.DUEL)) {
                            DuelsPlayer dtarget = dplayer.getInvitation(DuelsPlayer.InviteType.DUEL);
                            dplayer.clearInvitation(DuelsPlayer.InviteType.DUEL);

                            dtarget.getPlayer().sendMessage(MessagesYML.INVITE_NOTIF_DECLINED_SENDER.withPrefix(dtarget.getPlayer())
                                    .replace("$name$", player.getName()));
                            player.sendMessage(MessagesYML.INVITE_NOTIF_DECLINED_TARGET.withPrefix(player)
                                    .replace("$name$", dtarget.getName()));

                            SettingsYML.SFX.INVITE_DECLINE_SOUND.playSound(player);
                        } else {
                            player.sendMessage(MessagesYML.Errors.NO_INVITE.withPrefix(player));
                        }
                        return;
                    }

                    player.sendMessage(MessagesYML.Errors.NOT_ONLINE.withPrefix(player).replace("$player$", args[1]));
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                DuelsPlayer dtarget = plugin.getPlayers().get(target.getUniqueId());

                if(!dtarget.isPartOfTeam()) {
                    if (dplayer.isInvited(DuelsPlayer.InviteType.DUEL)) {
                        DuelsPlayer inviter = dplayer.getInvitation(DuelsPlayer.InviteType.DUEL);

                        if (!inviter.getPlayer().isOnline()) {
                            dplayer.clearInvitation(DuelsPlayer.InviteType.DUEL);
                            plugin.getPlayers().put(target.getUniqueId(), dplayer);
                            player.sendMessage(MessagesYML.Errors.INVITE_EXPIRED_OFFLINE.withPrefix(player)
                                    .replace("$name$", inviter.getName()));
                            return;
                        }

                        String playerIP = player.getAddress().getHostName();
                        String targetIP = target.getAddress().getHostName();

                        if(playerIP.equals(targetIP)
                                && (!player.hasPermission(pluginNameLower + ".developer")
                                && !target.hasPermission(pluginNameLower + ".developer"))) {
                            player.sendMessage(MessagesYML.Errors.DUEL_ALT_DETECTED.withPrefix(player));
                            return;
                        }

                        int senderTeamSize = dplayer.getTeammates().size() + 1;
                        int targetTeamSize = dtarget.getTeammates().size() + 1;

                        if(senderTeamSize == targetTeamSize) {
                            Match available = MatchManager.getAvailableMatch(MatchManager.getDuelTypeFor(senderTeamSize));

                            dplayer.clearInvitation(DuelsPlayer.InviteType.DUEL);

                            inviter.getPlayer().sendMessage(MessagesYML.INVITE_NOTIF_ACCEPTED_SENDER.withPrefix(inviter.getPlayer())
                                    .replace("$name$", player.getName()));
                            player.sendMessage(MessagesYML.INVITE_NOTIF_ACCEPTED_TARGET.withPrefix(player)
                                    .replace("$name$", inviter.getName()));

                            SettingsYML.SFX.INVITE_ACCEPT_SOUND.playSound(inviter.getPlayer());
                            SettingsYML.SFX.INVITE_ACCEPT_SOUND.playSound(player);

                            if(available != null) {
                                available.joinDueller(dplayer.getPlayer());
                                if(dplayer.hasTeam())
                                    dplayer.getTeammates().forEach(tm -> available.joinDueller(tm.getPlayer()));

                                available.getGroups().get(1).setLeader(dplayer);

                                available.joinDueller(dtarget.getPlayer());
                                if(dtarget.hasTeam())
                                    dtarget.getTeammates().forEach(tm -> available.joinDueller(tm.getPlayer()));

                                available.getGroups().get(2).setLeader(dtarget);

                                available.start();
                            } else {
                                target.sendMessage(MessagesYML.Errors.NO_AVAILABLE_MATCH.withPrefix(target));
                                player.sendMessage(MessagesYML.Errors.NO_AVAILABLE_MATCH.withPrefix(player));
                                return;
                            }
                        } else if(senderTeamSize < targetTeamSize) {
                            player.sendMessage(MessagesYML.Errors.INSUFFICIENT_TEAMMATES_SENDER.withPrefix(player)
                                    .replace("$name$", target.getName())
                                    .replace("$count$", (targetTeamSize - senderTeamSize) + "")
                            );
                        } else {
                            player.sendMessage(MessagesYML.Errors.INSUFFICIENT_TEAMMATES_TARGET.withPrefix(player)
                                    .replace("$name$", target.getName())
                                    .replace("$you$", senderTeamSize + "")
                                    .replace("$them$", targetTeamSize + "")
                            );
                        }
                    } else {
                        dtarget.setInvitationOf(dplayer, DuelsPlayer.InviteType.DUEL);

                        for (String line : MessagesYML.DUEL_REQUEST_NOTIF.colorLines(player)) {
                            String type = MatchManager.getDuelTypeFor(dplayer.getTeammates().size()).name().toLowerCase();

                            String l = line
                                    .replace("$inviter$", player.getName())
                                    .replace("$type$", type);

                            target.sendMessage(l);
                        }

                        SettingsYML.SFX.DUEL_INVITE_RECEIVE_SOUND.playSound(target);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            if(dtarget.isInvited(DuelsPlayer.InviteType.DUEL)) {
                                if (plugin.getPlayers().containsKey(target.getUniqueId())) {
                                    dtarget.clearInvitation(DuelsPlayer.InviteType.DUEL);

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
                    DuelsPlayer teamleader = dtarget.anyTeamleader();
                    player.sendMessage(MessagesYML.Errors.CANNOT_INVITE.withPrefix(player)
                            .replace("$name$", teamleader.getName()));
                }
            } else {
                sender.sendMessage(MessagesYML.Errors.INCORRECT_COMMAND.withPrefix(arg)
                        .replace("$format$", "/duels duel <player>"));
            }
        } else {
            sender.sendMessage(MessagesYML.Errors.NOT_PLAYER.withPrefix(null));
        }
    }
}
