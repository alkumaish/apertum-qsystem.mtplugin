/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.mtplugin.sender.plugins.pinger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.extra.IPing;
import ru.apertum.qsystem.mtplugin.sender.NetProperty;
import ru.apertum.qsystem.mtplugin.sender.ZoneServerList;
import ru.apertum.qsystem.mtplugin.sender.plugins.IZoneBoardSenderPluginUID;

/**
 *
 * @author egorov
 */
public class Pinger implements IPing, IZoneBoardSenderPluginUID {

    @Override
    public String getDescription() {
        final Properties settings = new Properties();
        final InputStream inStream = settings.getClass().getResourceAsStream("/mtplugin.properties");
        try {
            settings.load(inStream);
        } catch (IOException ex) {
            throw new RuntimeException("Проблемы с чтением версии. ", ex);
        }
        return "Плагин \"ZoneboardPlugin\" v.=" + settings.getProperty("version") + " дата=" + settings.getProperty("date") + " для опроса зонального сервера вывода информации на существование и совместимость.";
    }

    @Override
    public int ping() {
        //System.out.println("ZoneBoardSenderPlugin: IPing");
        final Properties settings = new Properties();
        final InputStream inStream = settings.getClass().getResourceAsStream("/mtplugin.properties");
        try {
            settings.load(inStream);
        } catch (IOException ex) {
            throw new RuntimeException("Проблемы с чтением версии. ", ex);
        }
        int i = 0;
        for (NetProperty prop : ZoneServerList.getInstance().getAddrs()) {
            int res = pinger(prop, settings.getProperty("version"));
            if (res == 1) {
                i++;
            }
        }
        return i;
    }

    @Override
    public long getUID() {
        return UID;
    }

    /**
     * пингануть используя основной код из главной проги
     * @param netProperty параметры соединения с зональном сервером.
     * @param version
     * @return 
     */
    public int pinger(INetProperty netProperty, String version) {
        //QLog.l().logger().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = version;
        String res;
        try {
            res = NetCommander.send(netProperty, "ping", params);
        } catch (Exception ex) {// вывод исключений
            //  QLog.l().logger().error("Проблема с command. ", ex);
            return -100500;
        }
        final Gson gson = new Gson();
        final RpcGetInt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInt.class);
        } catch (JsonSyntaxException ex) {
            //  QLog.l().logger().error("Не возможно интерпритировать ответ.", ex);
            return -100500;
        }
        return rpc.getResult();
    }
}
