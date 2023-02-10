package rs.raf.util;

import rs.raf.model.LocalFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class LocalComparators {
    public static class CreationDateComparator implements Comparator<LocalFile> {
        @Override
        public int compare(LocalFile file1, LocalFile file2) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date file1CreationDate;
            Date file2CreationDate;
            try {
                file1CreationDate = dateFormat.parse(dateFormat.format(file1.getFileMetadata().creationTime().toMillis()));
                file2CreationDate = dateFormat.parse(dateFormat.format(file2.getFileMetadata().creationTime().toMillis()));
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return file1CreationDate.compareTo(file2CreationDate);
        }
    }
    public static class ModificationDateComparator implements Comparator<LocalFile> {
        @Override
        public int compare(LocalFile file1, LocalFile file2) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date file1ModificationDate;
            Date file2ModificationDate;
            try {
                file1ModificationDate = dateFormat.parse(dateFormat.format(file1.getFileMetadata().lastModifiedTime().toMillis()));
                file2ModificationDate = dateFormat.parse(dateFormat.format(file2.getFileMetadata().lastModifiedTime().toMillis()));
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return file1ModificationDate.compareTo(file2ModificationDate);
        }
    }
    public static class NameComparator implements Comparator<LocalFile> {
        @Override
        public int compare(LocalFile file1, LocalFile file2) {
            return file1.getFile().getName().compareTo(file2.getFile().getName());
        }
    }
    public static class SizeComparator implements Comparator<LocalFile> {
        @Override
        public int compare(LocalFile file1, LocalFile file2) {
            Long file1Size = file1.getFileMetadata().size();
            Long file2Size = file2.getFileMetadata().size();
            return file1Size.compareTo(file2Size);
        }
    }
}