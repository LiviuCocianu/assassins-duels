package io.github.idoomful.assassinsduels.commands;

import org.bukkit.command.CommandSender;

public interface CommandModel {
    void execute(CommandSender player, String[] args);
}
