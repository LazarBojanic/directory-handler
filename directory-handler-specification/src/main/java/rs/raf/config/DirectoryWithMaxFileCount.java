package rs.raf.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Directory with max file count object.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryWithMaxFileCount {
    /**
     * Directory name.
     */
    private String directoryName;
    /**
     * Max file count.
     */
    private int maxFileCount;
    /**
     * Directory with max file count to string.
     */
    @Override
    public String toString() {
        return "DirectoryWithMaxFileCount{" +
                "directoryName='" + directoryName + '\'' +
                ", maxFileCount=" + maxFileCount +
                '}';
    }
}