package rs.raf.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class FilteredGoogleDriveFile {
    private String name;
    private String size;
    private String dateCreated;
    private String dateModified;
    public FilteredGoogleDriveFile(){
        this.name = "undefined";
        this.size = "undefined";
        this.dateCreated = "undefined";
        this.dateModified = "undefined";
    }
    @Override
    public String toString() {
        return "Name: " + name + " Size: " + size + " Date created: " + dateCreated + " Date modified: " + dateModified + "\n";
    }
}