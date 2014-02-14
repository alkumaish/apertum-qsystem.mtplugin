/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.apertum.qsystem.mtplugin;

import java.io.IOException;
import java.net.SocketException;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.extra.IStartServer;

/**
 *
 * @author Evgeniy Egorov
 */
public class MTplugin implements IStartServer {

    @Override
    public void start() {
        try {
            RunMT.go();
        } catch (SocketException ex) {
           QLog.l().logger().error(ex);
        } catch (IOException ex) {
            QLog.l().logger().error(ex);
        }
    }

    @Override
    public String getDescription() {
        return "Плагин дублирования главного табло на доп.серввере. Доп.Сервер стоит просто так, только табло показывает. Нужен bs-зональный плагин на нормалный сервер СУО.";
    }

    @Override
    public long getUID() {
        return 3;
    }
    
}
