/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.proxy.server.DummyServer;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;

public final class SingleToggleCommand extends ProxyCommandInfo {

    public SingleToggleCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, null);
    }

    @Override
    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission("toggle") || sender.hasPermission("maintenance.singleserver.toggle");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 1) {
            if (checkPermission(sender, "toggle")) return;
            final boolean maintenance = args[0].equalsIgnoreCase("on");
            if (maintenance == plugin.isMaintenance()) {
                sender.sendMessage(getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
                return;
            }

            plugin.setMaintenance(maintenance);
        } else if (args.length == 2) {
            if (checkPermission(sender, "singleserver.toggle")) return;
            final boolean maintenance = args[0].equalsIgnoreCase("on");
            Server server = plugin.getServer(args[1]);
            if (server == null) {
                if (maintenance) {
                    sender.sendMessage(getMessage("serverNotFound"));
                    return;
                }
                // Let servers be removed from the maintenance list, even if they don't exist on the proxy
                server = new DummyServer(args[1]);
                if (!plugin.isMaintenance(server)) {
                    sender.sendMessage(getMessage("serverNotFound"));
                    return;
                }
            }

            if (plugin.setMaintenanceToServer(server, maintenance)) {
                if (!sender.isPlayer() || !server.getName().equals(plugin.getServer(sender))) {
                    sender.sendMessage(getMessage(maintenance ? "singleMaintenanceActivated" : "singleMaintenanceDeactivated").replace("%SERVER%", server.getName()));
                }
            } else {
                sender.sendMessage(getMessage(maintenance ? "singleServerAlreadyEnabled" : "singleServerAlreadyDisabled").replace("%SERVER%", server.getName()));
            }
        } else
            sender.sendMessage(getHelpMessage());
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length != 2 || !sender.hasMaintenancePermission("singleserver.toggle")) return Collections.emptyList();
        return args[0].equalsIgnoreCase("off") ? plugin.getCommandManager().getMaintenanceServersCompletion(args[1].toLowerCase())
                : plugin.getCommandManager().getServersCompletion(args[1].toLowerCase());
    }
}
