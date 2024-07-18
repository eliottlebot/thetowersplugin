package fr.pikili.towers.towersplugin.map;

import org.bukkit.Bukkit;

import java.io.*;

public class FileUtil {

    public static void copy(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copy(new File(source, child), new File(destination, child));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to copy file: " + source.getAbsolutePath() + " to " + destination.getAbsolutePath());
                throw e;
            }
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            if (children != null) {
                for (String child : children) {
                    delete(new File(file, child));
                }
            }
        }
        if (!file.delete()) {
            Bukkit.getLogger().severe("Failed to delete file or directory: " + file.getAbsolutePath());
        }
    }
}
