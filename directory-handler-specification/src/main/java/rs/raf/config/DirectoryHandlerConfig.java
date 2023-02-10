package rs.raf.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Config object of DirectoryHandler.
 */
@Getter
@Setter
@AllArgsConstructor
public class DirectoryHandlerConfig {
    /**
     * Max repository size.
     */
    private long maxRepositorySize;
    /**
     * Excluded Extensions.
     */
    private List<String> excludedExtensions;
    /**
     * Directories with max file count.
     */
    private List<DirectoryWithMaxFileCount> directoriesWithMaxFileCount;
    /**
     * DirectoryHandlerConfig constructor.
     */
    public DirectoryHandlerConfig() {
        this.maxRepositorySize = 1073741824;
        this.excludedExtensions = new ArrayList<>();
        this.directoriesWithMaxFileCount = new ArrayList<>();
    }
    /**
     * DirectoryHandlerConfig to string.
     */
    @Override
    public String toString() {
        return "DirectoryHandlerConfig{" +
                "maxRepositorySize=" + maxRepositorySize +
                ", excludedExtensions=" + excludedExtensions +
                ", directoriesWithMaxFileCount=" + directoriesWithMaxFileCount +
                '}';
    }
}