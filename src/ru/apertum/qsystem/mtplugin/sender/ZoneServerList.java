/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.mtplugin.sender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import ru.apertum.qsystem.server.ServerProps;

/**
 *
 * @author Evgeniy Egorov
 */
public class ZoneServerList {

    final ArrayList<NetProperty> addrs = new ArrayList<>();

    public ArrayList<NetProperty> getAddrs() {
        return addrs;
    }

    private ZoneServerList() {
        for (String str : ServerProps.getInstance().getProps().getZoneBoardServAddrList()) {
            try {
                addrs.add(new NetProperty(ServerProps.getInstance().getProps().getZoneBoardServPort(), InetAddress.getByName(str)));
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    public static ZoneServerList getInstance() {
        return ZoneServerListHolder.INSTANCE;
    }

    private static class ZoneServerListHolder {

        private static final ZoneServerList INSTANCE = new ZoneServerList();
    }
}
