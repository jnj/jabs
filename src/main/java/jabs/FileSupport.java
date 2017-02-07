package jabs;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class FileSupport {
    public static void findFiles(File root, String suffix, List<File> outList) {
        if (root.isDirectory()) {
            File[] subdirs = root.listFiles(File::isDirectory);
            File[] files = root.listFiles(f -> f.isFile() && f.getName().endsWith(suffix));
            if (files != null) {
                Collections.addAll(outList, files);
            }
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    findFiles(subdir, suffix, outList);
                }
            }
        } else if (root.getName().endsWith(suffix)) {
            outList.add(root);
        }
    }
}
