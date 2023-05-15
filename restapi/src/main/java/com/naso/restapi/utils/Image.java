package com.naso.restapi.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class Image {
    public static String loadImage(String url, String foldername) throws IOException {
        Path path = Paths.get("");

        String filePath = path.toAbsolutePath().toString();

        char delimitter;

        if(filePath.charAt(0)=='/'){
            delimitter = '/';
        } else {
            delimitter = '\\';
        }

        filePath = filePath.substring(0, filePath.indexOf(delimitter + "server"));

        File folder = new File(filePath + delimitter + "client" + delimitter + "public" + delimitter + foldername);

        if (!folder.exists()) {
            folder.mkdir();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String filename = sdf.format(System.currentTimeMillis()) + ".jpg";
        File imgFile = new File(filePath + delimitter + "client" + delimitter + "public" + delimitter + foldername, filename);

        byte[] imgData = Base64.getDecoder().decode(url);

        OutputStream stream = new FileOutputStream(imgFile);

        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        stream.write(imgData);
        stream.close();

        return foldername + '/' + filename;
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.delete()) {
            System.out.println("File was deleted");
        } else {
            System.out.println("Wrong filepath!");
        }
    }
}
