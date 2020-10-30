/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shortest_reducts;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maximus
 */
public class ShortestReducts {

    static int sizeFB = 0;
    static int reducts_size = Integer.MAX_VALUE;
    static long queue_size = 0;
    private static final double MEGABYTE = 1024 * 1024;
    static Runtime runtime = Runtime.getRuntime();
    static double afterUsedMem;
    static double biforeUsedMem;
    static String output_dir = "./";

    public static double bytesToMegabytes(double bytes) {
        return bytes / MEGABYTE;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        File file=new File("mb1500x120.bol");

        File file = null;
        if (args.length > 0) {
            file = new File(args[0]);
            try {
                if (args.length > 1) {
                    reducts_size = Integer.parseInt(args[1]);
                    if (args.length > 2) {
                        String str_path = args[2];
                        String javaPath = str_path.replace("\\", "/"); // Create a new variable
//                        System.out.println(javaPath);
                        File path = new File(javaPath);
                        if (path.isDirectory()) {
                            output_dir = javaPath;
                        }
                        if (args.length > 3) {
                            queue_size = Long.parseLong(args[3]);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        //}
        findShortestReductsInFile(file);
    }
//    public static void main(String[] args) {
////        File curDir = new File(".");
////        getAllReductsInDir(curDir);
//          File disFile = new File("Diabetes.bol");
//          findShortestReductsInFile(disFile);
//    }

    public static void findShortestReductsInFile(File f_name) {
        try {
            boolean[][] bm = null;
            try {
                bm = DiscernibilityMatrix.loadFromFile(f_name.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(ShortestReducts.class.getName()).log(Level.SEVERE, null, ex);
            }
            String fileName = f_name.getName();
            String bmName = fileName.replaceFirst("[.][^.]+$", "");
            QNCG.findShortestReducts(bm, bmName);
        } catch (IOException ex) {
            Logger.getLogger(ShortestReducts.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getAllReductsInDir(File curDir) {

        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            String name = f.getName();
            if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0) {
                String extension = name.substring(name.lastIndexOf(".") + 1);
                if (f.isFile() && extension.equals("bol")) {
                    System.out.println(name);
                    findShortestReductsInFile(f);
                }
            }
        }
    }
}
