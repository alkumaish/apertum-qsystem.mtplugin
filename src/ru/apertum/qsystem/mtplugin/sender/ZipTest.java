/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.mtplugin.sender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

/**
 *
 * @author Evgeniy Egorov
 */
public class ZipTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, DataFormatException {
        /*
         String str = "admin";
         System.out.println(str);
         str = ZipTest.compress(str);
         System.out.println(str);
         str = ZipTest.decompress(str);
         System.out.println(str);
         */

        String inStr = " Русс яз Deflater in java is used to compress data and Inflater is used to decompress data. Deflater and Inflater use ZLIB library to compress and decompress the data. In the example we have taken a string to compress and then we have decompressed the same string. Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World!Hellow World! @";
        System.out.println(inStr);
        StringBuffer sb1 = new StringBuffer(inStr);
        for (int i = 0; i < 10; i++) {
            sb1 = sb1.append(sb1);

        }
        inStr = sb1.toString();

        System.out.println("Actual Length:" + inStr.length());

        //------------------------------------
        /*
         byte[] data = inStr.getBytes("UTF-8");

         byte[] output = new byte[data.length];

         //Compresses the data
         Deflater compresser = new Deflater();
         compresser.setInput(data);
         compresser.finish();

         int lenAfterCompress = compresser.deflate(output);
         System.out.println("!!! " + lenAfterCompress);
             
         String st = new String(output, 0, lenAfterCompress, "utf-8");
         */
        //*************************************************************************
        byte[] data = inStr.getBytes("UTF-8");
        byte[] zipped = compress(data);
        String st = new String(zipped);

        //+++++++++++++++++++++++++++++++++++++
        //System.out.println(st);
        System.out.println("Compressed data length:" + st.length());
        /*
         //Decompresses the data
         Inflater decompresser = new Inflater();
         //byte[] zipped = st.getBytes("utf-8");
         decompresser.setInput(zipped, 0, zipped.length);
         //decompresser.setInput(zipped, 0, lenAfterCompress);
         byte[] result = new byte[1024];

         int resultLength = decompresser.inflate(result);
         StringBuffer sb = new StringBuffer();
         while (resultLength != 0) {
         sb = sb.append(new String(result, 0, resultLength, "utf-8"));
         result = new byte[1024];
         resultLength = decompresser.inflate(result);

         }
         decompresser.end();
         */
        String outStr = new String(decompress(zipped), "utf-8");
        System.out.println("Decompressed data length:" + outStr.length());
        System.out.println(outStr);

    }

    public static byte[] compress(byte[] data) {
        final byte[] output = new byte[data.length];
        final Deflater compresser = new Deflater();
        compresser.setInput(data);
        compresser.finish();
        final int lenAfterCompress = compresser.deflate(output);
        byte[] output1 = new byte[lenAfterCompress];
        System.arraycopy(output, 0, output1, 0, output1.length);
        return output1;
    }

    public static byte[] decompress(byte[] zipped) throws DataFormatException, IOException {
        final Inflater decompresser = new Inflater();
        decompresser.setInput(zipped, 0, zipped.length);
        byte[] result = new byte[1024];
        int resultLength = decompresser.inflate(result);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (resultLength != 0) {
            outputStream.write(result, 0, resultLength);
            result = new byte[1024];
            resultLength = decompresser.inflate(result);
        }
        decompresser.end();
        return outputStream.toByteArray();
    }

}
