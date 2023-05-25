package rs.raf.main;

import rs.raf.main.core.App;
import rs.raf.main.exception.Exceptions.*;
import rs.raf.exception.DirectoryHandlerExceptions.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class Main{
    public static void main(String[] args) {
        try{
            App app = new App();
            app.run(args);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}