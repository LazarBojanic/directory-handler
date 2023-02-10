package rs.raf.util;

import com.google.api.services.drive.model.File;

import java.util.Comparator;
import java.util.Date;

public class GoogleDriveComparators {
    public static class CreationDateComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            Date file1CreationTime = new Date(file1.getCreatedTime().getValue());
            Date file2CreationTime = new Date(file2.getCreatedTime().getValue());
            return file1CreationTime.compareTo(file2CreationTime);
        }
    }
    public static class ModificationDateComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            Date file1ModificationTime = new Date(file1.getModifiedTime().getValue());
            Date file2ModificationTime = new Date(file2.getModifiedTime().getValue());
            return file1ModificationTime.compareTo(file2ModificationTime);
        }
    }
    public static class NameComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            return file1.getName().compareTo(file2.getName());
        }
    }
    public static class SizeComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            Long file1Size = file1.getSize();
            Long file2Size = file2.getSize();
            return file1Size.compareTo(file2Size);
        }
    }
}