/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.mtplugin.sender;

import java.net.InetAddress;
import ru.apertum.qsystem.common.model.INetProperty;

/**
 *
 * @author Evgeniy Egorov
 */
public class NetProperty implements INetProperty {

    private final Integer port;
    private final InetAddress addr;

    public NetProperty(Integer port, InetAddress addr) {
        this.port = port;
        this.addr = addr;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public InetAddress getAddress() {
        return addr;
    }
}
