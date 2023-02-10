package rs.raf.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilteredLocalFile {
    private String name;
    private String size;
    private String dateCreated;
    private String dateModified;
    public FilteredLocalFile(){
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