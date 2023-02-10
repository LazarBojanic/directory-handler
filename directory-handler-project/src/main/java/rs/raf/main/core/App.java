package rs.raf.main.core;

import rs.raf.enums.ConfigUpdateType;
import rs.raf.enums.OrderType;
import rs.raf.enums.SearchType;
import rs.raf.enums.SortingType;
import rs.raf.exception.DirectoryHandlerExceptions;
import rs.raf.specification.DirectoryHandlerManager;
import rs.raf.specification.IDirectoryHandlerSpecification;

import static rs.raf.exception.DirectoryHandlerExceptions.*;
import static rs.raf.main.exception.Exceptions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;

public class App {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public void run(final String[] args) throws NoFileAtPathException, NonExistentRepositoryException, InvalidParametersException, IOException, MaxFileCountExceededException, BadPathException, InvalidCommandException, MaxRepositorySizeExceededException, FileExtensionException, ValueInConfigCannotBeLessThanOneException, ParseException, GeneralSecurityException, BadFiltersException {
        IDirectoryHandlerSpecification directoryHandler;
        if (args.length == 2) {
            if (args[0].equals("local")) {
                try {
                    Class.forName("rs.raf.localImplementation.DirectoryHandlerLocalImplementation");
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (args[0].equals("drive")) {
                try {
                    Class.forName("rs.raf.googleDriveImplementation.DirectoryHandlerGoogleDriveImplementation");
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                throw new RuntimeException();
            }
            directoryHandler = DirectoryHandlerManager.getDirectoryHandler(args[1]);
        }
        else {
            throw new RuntimeException();
        }
        while (true) {
            String command = reader.readLine();
            String[] splitCommand = command.split(" ");
            if (splitCommand[0].equals("copyFiles")) {
                if (splitCommand.length == 4) {
                    if (splitCommand[3].equals("true")) {
                        directoryHandler.copyFiles(splitCommand[1], splitCommand[2], true);
                    }
                    else if (splitCommand[3].equals("false")) {
                        directoryHandler.copyFiles(splitCommand[1], splitCommand[2], false);
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("createConfig")) {
                if (splitCommand.length == 3) {
                    if (splitCommand[2].equals("default")) {
                        directoryHandler.createConfig(splitCommand[1], null);
                    }
                    else {
                        directoryHandler.createConfig(splitCommand[1], splitCommand[2]);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("createDirectories")) {
                if (splitCommand.length == 2) {
                    directoryHandler.createDirectories(splitCommand[1]);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("createFiles")) {
                if (splitCommand.length == 2) {
                    directoryHandler.createFiles(splitCommand[1]);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("createRepository")) {
                if (splitCommand.length == 3) {
                    if (splitCommand[2].equals("default")) {
                        directoryHandler.createRepository(splitCommand[1], null);
                    }
                    else {
                        directoryHandler.createRepository(splitCommand[1], splitCommand[2]);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("deleteFiles")) {
                if (splitCommand.length == 2) {
                    directoryHandler.deleteFiles(splitCommand[1]);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("downloadFiles")) {
                if (splitCommand.length == 4) {
                    if (splitCommand[2].equals("default")) {
                        if (splitCommand[3].equals("true")) {
                            directoryHandler.downloadFiles(splitCommand[1], null, true);
                        }
                        else if (splitCommand[3].equals("false")) {
                            directoryHandler.downloadFiles(splitCommand[1], null, false);
                        }
                        else {
                            throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                        }
                    }
                    else {
                        if (splitCommand[3].equals("true")) {
                            directoryHandler.downloadFiles(splitCommand[1], splitCommand[2], true);
                        }
                        else if (splitCommand[3].equals("false")) {
                            directoryHandler.downloadFiles(splitCommand[1], splitCommand[2], false);
                        }
                        else {
                            throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                        }
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getConfig")) {
                if (splitCommand.length == 2) {
                    System.out.println(directoryHandler.getConfig(splitCommand[1]));
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getDirectorySize")) {
                if (splitCommand.length == 2) {
                    System.out.println(directoryHandler.getDirectorySize(splitCommand[1]));
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFileCount")) {
                if (splitCommand.length == 2) {
                    System.out.println(directoryHandler.getFileCount(splitCommand[1]));
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesInDirectory")) {
                if (splitCommand.length == 8) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[2].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[2].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[2]);
                    }
                    if (splitCommand[3].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[3].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                    if (splitCommand[4].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    sortingType = switch (splitCommand[5]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    };
                    orderType = switch (splitCommand[6]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesInDirectory(splitCommand[1], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[7]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFileSize")) {
                if (splitCommand.length == 2) {
                    System.out.println(directoryHandler.getFileSize(splitCommand[1]));
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForDateRange")) {
                if (splitCommand.length == 12) {
                    boolean dateCreated;
                    boolean dateModified;
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[4].equals("true")) {
                        dateCreated = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        dateCreated = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        dateModified = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        dateModified = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    }
                    if (splitCommand[7].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[7].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    }
                    if (splitCommand[8].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[8].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    }
                    sortingType = switch (splitCommand[9]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[9]);
                    };
                    orderType = switch (splitCommand[10]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[10]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForDateRange(splitCommand[1], splitCommand[2], splitCommand[3], dateCreated, dateModified, recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[11]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForExcludedExtensions")) {
                if (splitCommand.length == 9) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[3].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[3].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                    if (splitCommand[4].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    sortingType = switch (splitCommand[6]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    };
                    orderType = switch (splitCommand[7]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForExcludedExtensions(splitCommand[1], splitCommand[2], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[8]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForExtensions")) {
                if (splitCommand.length == 9) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[3].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[3].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                    if (splitCommand[4].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    sortingType = switch (splitCommand[6]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    };
                    orderType = switch (splitCommand[7]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForExtensions(splitCommand[1], splitCommand[2], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[8]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForExtensionsAndExcludedExtensions")) {
                if (splitCommand.length == 10) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[4].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    }
                    sortingType = switch (splitCommand[7]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    orderType = switch (splitCommand[8]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForExtensionsAndExcludedExtensions(splitCommand[1], splitCommand[2], splitCommand[3], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[9]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForSearchName")) {
                if (splitCommand.length == 10) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    SearchType searchType;
                    OrderType orderType;
                    searchType = switch (splitCommand[3]) {
                        case "contains" -> SearchType.CONTAINS;
                        case "startsWith" -> SearchType.STARTS_WITH;
                        case "endsWith" -> SearchType.ENDS_WITH;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    };
                    if (splitCommand[4].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    sortingType = switch (splitCommand[7]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    orderType = switch (splitCommand[8]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForSearchName(splitCommand[1], splitCommand[2], searchType, recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[9]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForSearchNameAndExcludedExtensions")) {
                if (splitCommand.length == 10) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[4].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    }
                    sortingType = switch (splitCommand[7]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    orderType = switch (splitCommand[8]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForSearchNameAndExcludedExtensions(splitCommand[1], splitCommand[2], splitCommand[3], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[9]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForSearchNameAndExtensions")) {
                if (splitCommand.length == 10) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[4].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    }
                    sortingType = switch (splitCommand[7]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    orderType = switch (splitCommand[8]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForSearchNameAndExtensions(splitCommand[1], splitCommand[2], splitCommand[3], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[9]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesForSearchNameAndExtensionsAndExcludedExtensions")) {
                if (splitCommand.length == 11) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[5].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    if (splitCommand[6].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[6].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    }
                    if (splitCommand[7].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[7].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    }
                    sortingType = switch (splitCommand[8]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[8]);
                    };
                    orderType = switch (splitCommand[9]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[9]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesForSearchNameAndExtensionsAndExcludedExtensions(splitCommand[1], splitCommand[2], splitCommand[3], splitCommand[4], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[10]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("getFilesWithNames")) {
                if (splitCommand.length == 9) {
                    boolean recursive;
                    boolean includeFiles;
                    boolean includeDirectories;
                    SortingType sortingType;
                    OrderType orderType;
                    if (splitCommand[3].equals("true")) {
                        recursive = true;
                    }
                    else if (splitCommand[3].equals("false")) {
                        recursive = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                    if (splitCommand[4].equals("true")) {
                        includeFiles = true;
                    }
                    else if (splitCommand[4].equals("false")) {
                        includeFiles = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[4]);
                    }
                    if (splitCommand[5].equals("true")) {
                        includeDirectories = true;
                    }
                    else if (splitCommand[5].equals("false")) {
                        includeDirectories = false;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[5]);
                    }
                    sortingType = switch (splitCommand[6]) {
                        case "none" -> SortingType.NONE;
                        case "name" -> SortingType.NAME;
                        case "dateCreated" -> SortingType.DATE_CREATED;
                        case "dateModified" -> SortingType.DATE_MODIFIED;
                        case "size" -> SortingType.SIZE;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[6]);
                    };
                    orderType = switch (splitCommand[7]) {
                        case "ascending" -> OrderType.ASCENDING;
                        case "descending" -> OrderType.DESCENDING;
                        default -> throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[7]);
                    };
                    List fileList = directoryHandler.filterFileList(directoryHandler.getFilesWithNames(splitCommand[1], splitCommand[2], recursive, includeFiles, includeDirectories, sortingType, orderType), splitCommand[8]);
                    for (Object o : fileList) {
                        System.out.println(o);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("moveFiles")) {
                if (splitCommand.length == 4) {
                    if (splitCommand[3].equals("true")) {
                        directoryHandler.moveFiles(splitCommand[1], splitCommand[2], true);
                    }
                    else if (splitCommand[3].equals("false")) {
                        directoryHandler.moveFiles(splitCommand[1], splitCommand[2], false);
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("renameFile")) {
                if (splitCommand.length == 3) {
                    directoryHandler.renameFile(splitCommand[1], splitCommand[2]);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("updateConfig")) {
                String repositoryName;
                String configString;
                ConfigUpdateType configUpdateType;
                if (splitCommand.length == 4) {
                    repositoryName = splitCommand[1];
                    configString = splitCommand[2];
                    if (splitCommand[3].equals("replace")) {
                        configUpdateType = ConfigUpdateType.REPLACE;
                    }
                    else if (splitCommand[3].equals("add")) {
                        configUpdateType = ConfigUpdateType.ADD;
                    }
                    else {
                        throw new DirectoryHandlerExceptions.InvalidParametersException(splitCommand[3]);
                    }
                    directoryHandler.updateConfig(repositoryName, configString, configUpdateType);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (splitCommand[0].equals("writeToFile")) {
                if (splitCommand.length == 3) {
                    directoryHandler.writeToFile(splitCommand[1], splitCommand[2]);
                }
                else {
                    throw new InvalidCommandException(command);
                }
                System.out.println("Done");
            }
            else if (command.equals("help")) {
                help();
            }
            else if (command.equals("exit")) {
                System.exit(0);
            }
            else {
                throw new InvalidCommandException(command);
            }
        }
    }
    public void help() {
        System.out.println("copyFiles <filePathsString> <copyDestinationString> <overwrite>");
        System.out.println("createConfig <repositoryName> <configString>");
        System.out.println("createDirectories <directoryPathsString>");
        System.out.println("createFiles <createDirectories>");
        System.out.println("createRepository <repositoryName> <copyDestinationString> <configString>");
        System.out.println("deleteFiles <filePathsString>");
        System.out.println("downloadFiles <filePathsString> <downloadDestinationDirectoryString> <overwrite>");
        System.out.println("getConfig <repositoryName>");
        System.out.println("getDirectorySize <directoryPathString>");
        System.out.println("getFileCount <directoryPathString>");
        System.out.println("getFilesInDirectory <directoryPathString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFileSize <filePathString>");
        System.out.println("getFilesForDateRange <directoryPathString> <startDate> <endDate> <dateCreated> <dateModified> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForExcludedExtensions <directoryPathString> <searchExcludedExtensionsString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForExtensions <directoryPathString> <searchExtensionsString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForExtensionsAndExcludedExtensions <searchExtensionsString> <searchExcludedExtensionsString> <directoryPathString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForSearchName <directoryPathString> <search> <searchType> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForSearchNameAndExcludedExtensions <directoryPathString> <search> <searchExcludedExtensionsString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForSearchNameAndExtensions <directoryPathString> <search> <searchExtensionsString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesForSearchNameAndExtensionsAndExcludedExtensions <directoryPathString> <search> <searchExtensionsString> <searchExcludedExtensionsString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("getFilesWithNames <directoryPathString> <searchListString> <recursive> <includeFiles> <includeDirectories> <sortingType> <orderType> <filters>");
        System.out.println("moveFiles <filePathsString> <moveDestinationDirectoryString> <overwrite>");
        System.out.println("renameFile <filePathString> <newFileName>");
        System.out.println("updateConfig <repositoryName> <configString> <configUpdateType>");
        System.out.println("writeToFile <filePathString> <textToWrite>");
    }
}