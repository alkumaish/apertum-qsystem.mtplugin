/*
 * Copyright (C) 2011 Evgeniy Egorov
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
package ru.apertum.qsystem.mtplugin.sender.plugins.events;

import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.cmd.RpcToZoneServer;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.extra.IChangeCustomerStateEvent;
import ru.apertum.qsystem.mtplugin.sender.NetProperty;
import ru.apertum.qsystem.mtplugin.sender.Sender;
import ru.apertum.qsystem.mtplugin.sender.ZoneServerList;
import ru.apertum.qsystem.mtplugin.sender.plugins.IZoneBoardSenderPluginUID;

/**
 * Плагин во время смены статуса клиенту отсылает статистику в сервер
 * отображений зональных табло по сети
 * @author Evgeniy Egorov
 */
public class EventSender implements IChangeCustomerStateEvent, IZoneBoardSenderPluginUID {

    @Override
    public void change(String userPoint, String customerPrefix, int customerNumber, CustomerState cs) {
    }

    @Override
    public void change(QCustomer qc, CustomerState cs, Long newServiceId) {
        //System.out.println("ZoneBoardSenderPlugin: IChangeCustomerStateEvent");
        //if (!PingResult.getInstance().isReady()) {
        // QLog.l().logger().error("Версия плагина \"ZoneboardPlugin\" не сообветствует версии зональго сервера ввыда инфмации.");
        //    return;
        //}
        // Создаем событие
        // Отсылаем событие
        String cmdName = "kill";
        switch (cs) {
            case STATE_INVITED:
                cmdName = "show";
                break;
            case STATE_INVITED_SECONDARY:
                cmdName = "show";
                break;
            case STATE_WORK:
                cmdName = "work";
                break;
            case STATE_WORK_SECONDARY:
                cmdName = "work";
                break;
            case STATE_DEAD:
                break;
            case STATE_FINISH:
                break;
            case STATE_POSTPONED:
                break;
            case STATE_REDIRECT:
                break;
            default:// нужная вещь. чтобы отсечь состояния, которые не при чем в зональном табло
                return;

        }

        final RpcToZoneServer params = new RpcToZoneServer(new RpcToZoneServer.Data(qc.getUser().getName(), qc.getUser().getPoint(), qc.getPrefix(), qc.getNumber(), qc.getUser().getAdressRS()));
        params.setMethod(cmdName);

        for (final NetProperty prop : ZoneServerList.getInstance().getAddrs()) {
            final Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        new Sender().sendRpc(prop, params);
                    } catch (Exception ex) {// вывод исключений
                        //  QLog.l().logger().error("Проблема с командой. ", ex);
                        System.err.println(ex);
                    }
                }
            });
            thread.start();
        }

    }

    @Override
    public String getDescription() {
        return "Плагин \"ZoneboardPlugin\" во время смены статуса клиенту отсылает статистику в зональный сервер отображения данных через сеть";
    }

    @Override
    public long getUID() {
        return UID;
    }
}
