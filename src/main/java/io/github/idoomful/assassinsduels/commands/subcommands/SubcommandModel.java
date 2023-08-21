package io.github.idoomful.assassinsduels.commands.subcommands;

import io.github.idoomful.assassinsduels.DMain;
import org.bukkit.command.CommandSender;

public interface SubcommandModel {
    void execute(DMain plugin, CommandSender sender, String[] args);
}
