package rs.raf.localImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import rs.raf.enums.ConfigUpdateType;
import rs.raf.config.DirectoryHandlerConfig;
import rs.raf.config.DirectoryWithMaxFileCount;
import rs.raf.enums.OrderType;
import rs.raf.model.FilteredLocalFile;
import rs.raf.model.LocalFile;
import rs.raf.enums.SearchType;
import rs.raf.enums.SortingType;
import rs.raf.specification.DirectoryHandlerManager;
import rs.raf.specification.IDirectoryHandlerSpecification;
import rs.raf.util.LocalComparators;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import rs.raf.exception.DirectoryHandlerExceptions.*;

public class DirectoryHandlerLocalImplementation implements IDirectoryHandlerSpecification<LocalFile, FilteredLocalFile> {
    protected static Path workingDirectory = null;
    private static DirectoryHandlerLocalImplementation instance;
    static {
        DirectoryHandlerManager.registerDirectoryHandler(DirectoryHandlerLocalImplementation.getInstance());
    }
    public DirectoryHandlerLocalImplementation() {
        super();
    }
    @Override
    public void copyFiles(String filePathsString, String copyDestinationDirectoryString, final boolean overwrite) throws BadPathException, MaxFileCountExceededException, IOException, NoFileAtPathException, NonExistentRepositoryException, InvalidParametersException {
        filePathsString = replaceSlashesInPath(filePathsString);
        copyDestinationDirectoryString = replaceSlashesInPath(copyDestinationDirectoryString);
        Path copyDestinationDirectoryPath;
        if (badPathCheck(copyDestinationDirectoryString)) {
            throw new BadPathException(copyDestinationDirectoryString);
        }
        copyDestinationDirectoryPath = workingDirectory.resolve(Paths.get(copyDestinationDirectoryString));
        if (noFileAtPathCheck(copyDestinationDirectoryString)) {
            createDirectories(copyDestinationDirectoryString);
        }
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            if (noFileAtPathCheck(filePathString)) {
                throw new NoFileAtPathException(filePathString);
            }
            String repositoryName = copyDestinationDirectoryString.split("/")[0];
            DirectoryHandlerConfig config = getConfig(repositoryName);
            if (maxFileCountExceededCheck(config, copyDestinationDirectoryString)) {
                throw new MaxFileCountExceededException(copyDestinationDirectoryString);
            }
            String fileName = String.valueOf(Paths.get(filePathString).getFileName());
            Path originalPath = workingDirectory.resolve(Paths.get(filePathString));
            File fileToCopy = originalPath.toFile();
            File destinationFile = copyDestinationDirectoryPath.resolve(fileName).toFile();
            if (overwrite) {
                if (fileToCopy.isFile()) {
                    FileUtils.copyFile(fileToCopy, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
                else {
                    FileUtils.copyDirectory(fileToCopy, destinationFile);
                }
            }
            else {
                if (fileToCopy.isFile()) {
                    String suffix = "";
                    int i = 0;
                    while (true) {
                        try {
                            if(fileName.contains(".")){
                                Files.copy(originalPath, copyDestinationDirectoryPath.resolve(fileName.substring(0, fileName.indexOf(".")) + suffix + fileName.substring(fileName.indexOf("."))));
                            }
                            else{
                                Files.copy(originalPath, copyDestinationDirectoryPath.resolve(fileName + suffix));
                            }
                            break;
                        }
                        catch (IOException e) {
                            i++;
                            suffix = String.valueOf(i);
                        }
                    }
                }
                else {
                    if (copyDestinationDirectoryPath.toFile().listFiles() != null) {
                        List<String> fileNames = new ArrayList<>();
                        for (File file : Objects.requireNonNull(copyDestinationDirectoryPath.toFile().listFiles())) {
                            fileNames.add(file.getName());
                        }
                        String suffix = "";
                        int i = 0;
                        while (true) {
                            for (String fileNameNoOverwrite : fileNames) {
                                if (fileName.equals(fileNameNoOverwrite)) {
                                    i++;
                                    suffix = String.valueOf(i);
                                    break;
                                }
                            }
                            boolean exists = false;
                            String newDirectoryName = fileName + suffix;
                            for (String fileNameNoOverwrite : fileNames) {
                                if ((newDirectoryName).equals(fileNameNoOverwrite)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                FileUtils.copyDirectory(fileToCopy, copyDestinationDirectoryPath.resolve(newDirectoryName).toFile());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public void createConfig(final String repositoryName, final String configString) throws IOException, NonExistentRepositoryException, InvalidParametersException, NoFileAtPathException, BadPathException, ValueInConfigCannotBeLessThanOneException {
        if (nonExistentRepositoryCheck(repositoryName)) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        Path configPath = workingDirectory.resolve(repositoryName).resolve("config.json");
        Files.createFile(configPath);
        if (configString == null) {
            saveConfig(repositoryName, new DirectoryHandlerConfig());
        }
        else {
            saveConfig(repositoryName, generateConfigFromString(configString));
        }
    }
    @Override
    public void createDirectories(String directoryPathsString) throws BadPathException, NoFileAtPathException, IOException, MaxFileCountExceededException, NonExistentRepositoryException, InvalidParametersException {
        directoryPathsString = replaceSlashesInPath(directoryPathsString);
        List<String> directoryPathsList = List.of(directoryPathsString.split("-more-"));
        for (String directoryPathString : directoryPathsList) {
            if (badPathCheck(directoryPathString)) {
                throw new BadPathException(directoryPathString);
            }
            String repositoryName = directoryPathsString.split("/")[0];
            if (nonExistentRepositoryCheck(repositoryName)) {
                throw new NonExistentRepositoryException(repositoryName);
            }
            DirectoryHandlerConfig config = getConfig(repositoryName);
            String parentDirectory = replaceSlashesInPath(Paths.get(directoryPathString).getParent().toString());
            if (noFileAtPathCheck(parentDirectory)) {
                createDirectories(parentDirectory);
            }
            if (maxFileCountExceededCheck(config, parentDirectory)) {
                throw new MaxFileCountExceededException(parentDirectory);
            }
            if (noFileAtPathCheck(parentDirectory)) {
                createDirectories(parentDirectory);
            }
            Files.createDirectory(workingDirectory.resolve(Paths.get(directoryPathString)));
        }
    }
    @Override
    public void createFiles(String filePathsString) throws IOException, BadPathException, MaxFileCountExceededException, NoFileAtPathException, FileExtensionException, NonExistentRepositoryException, InvalidParametersException, MaxRepositorySizeExceededException {
        filePathsString = replaceSlashesInPath(filePathsString);
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            String repositoryName = filePathString.split("/")[0];
            if (nonExistentRepositoryCheck(repositoryName)) {
                throw new NonExistentRepositoryException(repositoryName);
            }
            DirectoryHandlerConfig config = getConfig(repositoryName);
            String parentDirectory = replaceSlashesInPath(Paths.get(filePathString).getParent().toString());
            if (noFileAtPathCheck(parentDirectory)) {
                createDirectories(parentDirectory);
            }
            if (maxFileCountExceededCheck(config, parentDirectory)) {
                throw new MaxFileCountExceededException(parentDirectory);
            }
            if (excludedExtensionsCheck(config, filePathString)) {
                throw new FileExtensionException(filePathString);
            }
            Files.createFile(workingDirectory.resolve(Paths.get(filePathString)));
            writeToFile(filePathString, "sampleText");
        }
    }
    @Override
    public void createRepository(final String repositoryName, final String configString) throws BadPathException, NonExistentRepositoryException, IOException, InvalidParametersException, NoFileAtPathException, ValueInConfigCannotBeLessThanOneException {
        if (badPathCheck(repositoryName)) {
            throw new BadPathException(repositoryName);
        }
        Files.createDirectory(workingDirectory.resolve(repositoryName));
        createConfig(repositoryName, configString);
    }
    @Override
    public void deleteFiles(String filePathsString) throws IOException, BadPathException, NoFileAtPathException {
        filePathsString = replaceSlashesInPath(filePathsString);
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            if (noFileAtPathCheck(filePathString)) {
                throw new NoFileAtPathException(filePathString);
            }
            File file = workingDirectory.resolve(Paths.get(filePathString)).toFile();
            if (file.isFile()) {
                FileUtils.delete(file);
            }
            else {
                FileUtils.deleteDirectory(file);
            }
        }
    }
    @Override
    public void downloadFiles(String filePathsString, String downloadDestinationDirectoryString, final boolean overwrite) throws NoFileAtPathException, IOException, BadPathException {
        filePathsString = replaceSlashesInPath(filePathsString);
        Path downloadDestinationDirectoryPath;
        if (downloadDestinationDirectoryString == null) {
            downloadDestinationDirectoryPath = workingDirectory.resolve("Downloads");
        }
        else {
            if (badPathCheck(downloadDestinationDirectoryString)) {
                throw new BadPathException(downloadDestinationDirectoryString);
            }
            if (Paths.get(downloadDestinationDirectoryString).isAbsolute()) {
                downloadDestinationDirectoryPath = Paths.get(downloadDestinationDirectoryString);
            }
            else {
                downloadDestinationDirectoryString = replaceSlashesInPath(downloadDestinationDirectoryString);
                downloadDestinationDirectoryPath = workingDirectory.resolve(Paths.get(downloadDestinationDirectoryString));
            }
        }
        if (!Files.exists(downloadDestinationDirectoryPath)) {
            Files.createDirectories(downloadDestinationDirectoryPath);
        }
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            if (noFileAtPathCheck(filePathString)) {
                throw new NoFileAtPathException(filePathString);
            }
            String fileName = String.valueOf(Paths.get(filePathString).getFileName());
            Path originalPath = workingDirectory.resolve(Paths.get(filePathString));
            File fileToDownload = originalPath.toFile();
            File destinationFile = downloadDestinationDirectoryPath.resolve(fileName).toFile();
            if (overwrite) {
                if (fileToDownload.isFile()) {
                    FileUtils.copyFile(fileToDownload, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
                else {
                    FileUtils.copyDirectory(fileToDownload, destinationFile);
                }
            }
            else {
                if (fileToDownload.isFile()) {
                    String suffix = "";
                    int i = 0;
                    while (true) {
                        try {
                            if(fileName.contains(".")){
                                Files.copy(originalPath, downloadDestinationDirectoryPath.resolve(fileName.substring(0, fileName.indexOf(".")) + suffix + fileName.substring(fileName.indexOf("."))));
                            }
                            else{
                                Files.copy(originalPath, downloadDestinationDirectoryPath.resolve(fileName + suffix));
                            }
                            break;
                        }
                        catch (IOException e) {
                            i++;
                            suffix = String.valueOf(i);
                        }
                    }
                }
                else {
                    if (downloadDestinationDirectoryPath.toFile().listFiles() != null) {
                        List<String> fileNames = new ArrayList<>();
                        for (File file : Objects.requireNonNull(downloadDestinationDirectoryPath.toFile().listFiles())) {
                            fileNames.add(file.getName());
                        }
                        String suffix = "";
                        int i = 0;
                        while (true) {
                            for (String fileNameNoOverwrite : fileNames) {
                                if (fileName.equals(fileNameNoOverwrite)) {
                                    i++;
                                    suffix = String.valueOf(i);
                                    break;
                                }
                            }
                            boolean exists = false;
                            String newDirectoryName = fileName + suffix;
                            for (String fileNameNoOverwrite : fileNames) {
                                if ((newDirectoryName).equals(fileNameNoOverwrite)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                FileUtils.copyDirectory(fileToDownload, downloadDestinationDirectoryPath.resolve(newDirectoryName).toFile());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public DirectoryHandlerConfig getConfig(final String repositoryName) throws IOException, NonExistentRepositoryException, InvalidParametersException, NoFileAtPathException, BadPathException {
        if (nonExistentRepositoryCheck(repositoryName)) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        Path configPath = workingDirectory.resolve(repositoryName).resolve("config.json");
        String configJson = FileUtils.readFileToString(configPath.toFile(), "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(configJson, DirectoryHandlerConfig.class);
    }
    @Override
    public long getDirectorySize(String directoryPathString) throws BadPathException, NoFileAtPathException {
        directoryPathString = replaceSlashesInPath(directoryPathString);
        if (badPathCheck(directoryPathString)) {
            throw new BadPathException(directoryPathString);
        }
        if (noFileAtPathCheck(directoryPathString)) {
            throw new NoFileAtPathException(directoryPathString);
        }
        return FileUtils.sizeOfDirectory(workingDirectory.resolve(Paths.get(directoryPathString)).toFile());
    }
    @Override
    public int getFileCount(String directoryPathString) throws BadPathException, NoFileAtPathException {
        directoryPathString = replaceSlashesInPath(directoryPathString);
        if (badPathCheck(directoryPathString)) {
            throw new BadPathException(directoryPathString);
        }
        if (noFileAtPathCheck(directoryPathString)) {
            throw new NoFileAtPathException(directoryPathString);
        }
        int fileCount = 0;
        File directory = workingDirectory.resolve(Paths.get(directoryPathString)).toFile();
        List<File> fileList = null;
        if (directory.listFiles() != null) {
            fileList = List.of(Objects.requireNonNull(directory.listFiles()));
        }
        for (int i = 0; i < Objects.requireNonNull(fileList).size(); i++) {
            fileCount++;
        }
        return fileCount;
    }
    @Override
    public long getFileSize(String filePathString) throws BadPathException, NoFileAtPathException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        if (noFileAtPathCheck(filePathString)) {
            throw new NoFileAtPathException(filePathString);
        }
        return FileUtils.sizeOf(workingDirectory.resolve(Paths.get(filePathString)).toFile());
    }
    @Override
    public List<LocalFile> getFilesForDateRange(final String directoryPathString, final String startDate, final String endDate, final boolean dateCreated, final boolean dateModified, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException, ParseException {
        Date rangeStartDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
        Date rangeEndDate = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);
        if (rangeStartDate.compareTo(rangeEndDate) > 0) {
            throw new InvalidParametersException("startDate, endDate! End date must be larger than start date");
        }
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        for (LocalFile file : directoryToSearchList) {
            if (dateCreated && !dateModified) {
                Date fileCreationDate = dateFormat.parse(dateFormat.format(file.getFileMetadata().creationTime().toMillis()));
                if (fileCreationDate.compareTo(rangeStartDate) >= 0 && fileCreationDate.compareTo(rangeEndDate) <= 0) {
                    fileList.add(file);
                }
            }
            if (!dateCreated && dateModified) {
                Date fileModificationDate = dateFormat.parse(dateFormat.format(file.getFileMetadata().lastModifiedTime().toMillis()));
                if (fileModificationDate.compareTo(rangeStartDate) >= 0 && fileModificationDate.compareTo(rangeEndDate) <= 0) {
                    fileList.add(file);
                }
            }
            if (dateCreated && dateModified) {
                Date fileCreationDate = dateFormat.parse(dateFormat.format(file.getFileMetadata().creationTime().toMillis()));
                Date fileModificationDate = dateFormat.parse(dateFormat.format(file.getFileMetadata().lastModifiedTime().toMillis()));
                if ((fileCreationDate.compareTo(rangeStartDate) >= 0 && fileCreationDate.compareTo(rangeEndDate) <= 0) || (fileModificationDate.compareTo(rangeStartDate) >= 0 && fileModificationDate.compareTo(rangeEndDate) <= 0)) {
                    fileList.add(file);
                }
            }
            if (!dateCreated && !dateModified) {
                throw new InvalidParametersException("dateCreated, dateAdded! You must specify which date type to include in search");
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForExcludedExtensions(final String directoryPathString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getFile().getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    fileList.add(file);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForExtensions(final String directoryPathString, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String extension : searchExtensionsList) {
                if (file.getFile().getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    fileList.add(file);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForExtensionsAndExcludedExtensions(final String directoryPathString, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getFile().getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    for (String extension : searchExtensionsList) {
                        if (file.getFile().getName().toLowerCase().endsWith(extension.toLowerCase())) {
                            fileList.add(file);
                        }
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForSearchName(final String directoryPathString, final String search, final SearchType searchType, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            if (searchType.equals(SearchType.CONTAINS)) {
                if (file.getFile().getName().toLowerCase().contains(search.toLowerCase())) {
                    fileList.add(file);
                }
            }
            else if (searchType.equals(SearchType.STARTS_WITH)) {
                if (file.getFile().getName().toLowerCase().startsWith(search.toLowerCase())) {
                    fileList.add(file);
                }
            }
            else if (searchType.equals(SearchType.ENDS_WITH)) {
                if (file.getFile().getName().toLowerCase().endsWith(search.toLowerCase())) {
                    fileList.add(file);
                }
            }
            else {
                throw new InvalidParametersException(searchType.name());
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForSearchNameAndExcludedExtensions(final String directoryPathString, final String search, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getFile().getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    if (file.getFile().getName().toLowerCase().contains(search.toLowerCase())) {
                        fileList.add(file);
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForSearchNameAndExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String extension : searchExtensionsList) {
                if (file.getFile().getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    if (file.getFile().getName().toLowerCase().contains(search.toLowerCase())) {
                        fileList.add(file);
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesForSearchNameAndExtensionsAndExcludedExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<LocalFile> fileList = new ArrayList<>();
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (LocalFile file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getFile().getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    for (String extension : searchExtensionsList) {
                        if (file.getFile().getName().toLowerCase().endsWith(extension.toLowerCase())) {
                            if (file.getFile().getName().toLowerCase().contains(search.toLowerCase())) {
                                fileList.add(file);
                            }
                        }
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesInDirectory(final String directoryPathString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws BadPathException, NoFileAtPathException, IOException, InvalidParametersException {
        if (!includeFiles && !includeDirectories) {
            throw new InvalidParametersException("Include files: false; Include directories: false");
        }
        File directory;
        if (directoryPathString == null) {
            directory = workingDirectory.toFile();
        }
        else {
            if (badPathCheck(directoryPathString)) {
                throw new BadPathException(directoryPathString);
            }
            if (noFileAtPathCheck(directoryPathString)) {
                throw new NoFileAtPathException(directoryPathString);
            }
            directory = workingDirectory.resolve(Paths.get(directoryPathString)).toFile();
        }
        List<File> fileList = new ArrayList<>();
        List<LocalFile> localFileList = new ArrayList<>();
        if (!includeFiles && includeDirectories) {
            if (recursive) {
                fileList = (List<File>) FileUtils.listFilesAndDirs(directory, DirectoryFileFilter.DIRECTORY, TrueFileFilter.INSTANCE);
            }
            else {
                fileList = (List<File>) FileUtils.listFilesAndDirs(directory, DirectoryFileFilter.DIRECTORY, null);
            }
        }
        if (includeFiles && !includeDirectories) {
            fileList = (List<File>) FileUtils.listFiles(directory, null, recursive);
        }
        if (includeFiles && includeDirectories) {
            if (recursive) {
                fileList = (List<File>) FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            }
            else {
                fileList = (List<File>) FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, null);
            }
        }
        for (File file : fileList) {
            LocalFile localFile = new LocalFile(file);
            localFileList.add(localFile);
        }
        return sortList(localFileList, sortingType, orderType);
    }
    @Override
    public List<LocalFile> getFilesWithNames(final String directoryPathString, final String searchListString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchList = List.of(searchListString.split(","));
        List<LocalFile> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        List<LocalFile> foundFiles = new ArrayList<>();
        for (LocalFile file : directoryToSearchList) {
            for (String search : searchList) {
                if (file.getFile().getName().equals(search)) {
                    foundFiles.add(file);
                }
            }
        }
        return sortList(foundFiles, sortingType, orderType);
    }
    @Override
    public void moveFiles(String filePathsString, String moveDestinationDirectoryString, boolean overwrite) throws IOException, BadPathException, NoFileAtPathException, NonExistentRepositoryException, MaxFileCountExceededException, InvalidParametersException {
        filePathsString = replaceSlashesInPath(filePathsString);
        moveDestinationDirectoryString = replaceSlashesInPath(moveDestinationDirectoryString);
        Path moveDestinationDirectoryPath;
        if (badPathCheck(moveDestinationDirectoryString)) {
            throw new BadPathException(moveDestinationDirectoryString);
        }
        moveDestinationDirectoryPath = workingDirectory.resolve(Paths.get(moveDestinationDirectoryString));
        if (noFileAtPathCheck(moveDestinationDirectoryString)) {
            createDirectories(moveDestinationDirectoryString);
        }
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            if (noFileAtPathCheck(filePathString)) {
                throw new NoFileAtPathException(filePathString);
            }
            String repositoryName = moveDestinationDirectoryString.split("/")[0];
            DirectoryHandlerConfig config = getConfig(repositoryName);
            if (maxFileCountExceededCheck(config, moveDestinationDirectoryString)) {
                throw new MaxFileCountExceededException(moveDestinationDirectoryString);
            }
            String fileName = String.valueOf(Paths.get(filePathString).getFileName());
            Path originalPath = workingDirectory.resolve(Paths.get(filePathString));
            File fileToMove = originalPath.toFile();
            File destinationFile = moveDestinationDirectoryPath.resolve(fileName).toFile();
            if (overwrite) {
                if (fileToMove.isFile()) {
                    FileUtils.moveFile(fileToMove, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
                else {
                    FileUtils.moveDirectory(fileToMove, destinationFile);
                }
            }
            else {
                if (fileToMove.isFile()) {
                    String suffix = "";
                    int i = 0;
                    while (true) {
                        try {
                            if(fileName.contains(".")){
                                Files.copy(originalPath, moveDestinationDirectoryPath.resolve(fileName.substring(0, fileName.indexOf(".")) + suffix + fileName.substring(fileName.indexOf("."))));
                            }
                            else{
                                Files.copy(originalPath, moveDestinationDirectoryPath.resolve(fileName + suffix));
                            }
                            break;
                        }
                        catch (IOException e) {
                            i++;
                            suffix = String.valueOf(i);
                        }
                    }
                }
                else {
                    if (moveDestinationDirectoryPath.toFile().listFiles() != null) {
                        List<String> fileNames = new ArrayList<>();
                        for (File file : Objects.requireNonNull(moveDestinationDirectoryPath.toFile().listFiles())) {
                            fileNames.add(file.getName());
                        }
                        String suffix;
                        int i = 0;
                        while (true) {
                            for (String fileNameNoOverwrite : fileNames) {
                                if (fileName.equals(fileNameNoOverwrite)) {
                                    i++;
                                    break;
                                }
                            }
                            suffix = String.valueOf(i);
                            boolean exists = false;
                            String newDirectoryName = fileName + suffix;
                            for (String fileNameNoOverwrite : fileNames) {
                                if ((newDirectoryName).equals(fileNameNoOverwrite)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                FileUtils.moveDirectory(fileToMove, moveDestinationDirectoryPath.resolve(newDirectoryName).toFile());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public void renameFile(String filePathString, final String newFileName) throws BadPathException, NoFileAtPathException, NonExistentRepositoryException, IOException, FileExtensionException, InvalidParametersException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        if (noFileAtPathCheck(filePathString)) {
            throw new NoFileAtPathException(filePathString);
        }
        String repositoryName = filePathString.split("/")[0];
        DirectoryHandlerConfig config = getConfig(repositoryName);
        if (excludedExtensionsCheck(config, newFileName)) {
            throw new FileExtensionException(newFileName);
        }
        Path fileSourcePath = workingDirectory.resolve(Paths.get(filePathString));
        Path fileDestinationPath;
        try {
            fileDestinationPath = fileSourcePath.resolveSibling(newFileName);
        }
        catch (InvalidPathException e) {
            throw new BadPathException(filePathString + String.format(" (Renamed to %s) ", newFileName));
        }
        Files.move(fileSourcePath, fileDestinationPath);
    }
    @Override
    public void setWorkingDirectory(final String rootPathString) {
        if (rootPathString.equals("default")) {
            workingDirectory = Paths.get(System.getProperty("user.dir")).resolve("LocalRepositories");
        }
        else if (Paths.get(rootPathString).isAbsolute()) {
            workingDirectory = Paths.get(rootPathString);
        }
        else {
            throw new RuntimeException();
        }
        if (!Files.exists(workingDirectory)) {
            try {
                Files.createDirectory(workingDirectory);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void updateConfig(final String repositoryName, final String configString, final ConfigUpdateType configUpdateType) throws NonExistentRepositoryException, IOException, ValueInConfigCannotBeLessThanOneException, NoFileAtPathException, BadPathException, InvalidParametersException {
        DirectoryHandlerConfig currentConfig = getConfig(repositoryName);
        DirectoryHandlerConfig pendingConfig = generateConfigFromString(configString);
        DirectoryHandlerConfig updatedConfig = currentConfig;
        String configPathString = String.format("%s/config.json", repositoryName);
        Path configPath = workingDirectory.resolve(Paths.get(configPathString));
        ObjectMapper objectMapper = new ObjectMapper();
        if (configUpdateType.equals(ConfigUpdateType.REPLACE)) {
            if (pendingConfig.getMaxRepositorySize() >= 1) {
                updatedConfig.setMaxRepositorySize(pendingConfig.getMaxRepositorySize());
            }
            else {
                throw new ValueInConfigCannotBeLessThanOneException("maxRepositorySize=" + pendingConfig.getMaxRepositorySize());
            }
            if (pendingConfig.getExcludedExtensions().size() > 0) {
                updatedConfig.setExcludedExtensions(pendingConfig.getExcludedExtensions());
            }
            if (pendingConfig.getDirectoriesWithMaxFileCount().size() > 0) {
                for (DirectoryWithMaxFileCount pendingDirectoryWithMaxFileCount : pendingConfig.getDirectoriesWithMaxFileCount()) {
                    if (pendingDirectoryWithMaxFileCount.getMaxFileCount() < 1) {
                        throw new ValueInConfigCannotBeLessThanOneException("maxFileCount=" + pendingDirectoryWithMaxFileCount.getMaxFileCount());
                    }
                }
                updatedConfig.setDirectoriesWithMaxFileCount(pendingConfig.getDirectoriesWithMaxFileCount());
            }
        }
        else if (configUpdateType.equals(ConfigUpdateType.ADD)) {
            if (pendingConfig.getMaxRepositorySize() >= 1) {
                updatedConfig.setMaxRepositorySize(updatedConfig.getMaxRepositorySize() + pendingConfig.getMaxRepositorySize());
            }
            else {
                throw new ValueInConfigCannotBeLessThanOneException("maxRepositorySize=" + pendingConfig.getMaxRepositorySize());
            }
            if (pendingConfig.getExcludedExtensions().size() > 0) {
                for (String pendingExcludedExtension : pendingConfig.getExcludedExtensions()) {
                    boolean found = false;
                    for (String excludedExtension : updatedConfig.getExcludedExtensions()) {
                        if (pendingExcludedExtension.equals(excludedExtension)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        List<String> currentExcludedExtensions = updatedConfig.getExcludedExtensions();
                        currentExcludedExtensions.add(pendingExcludedExtension);
                        updatedConfig.setExcludedExtensions(currentExcludedExtensions);
                    }
                }
            }
            if (pendingConfig.getDirectoriesWithMaxFileCount().size() > 0) {
                List<DirectoryWithMaxFileCount> newListOfDirectoriesWithMaxFileCount = new ArrayList<>();
                DirectoryWithMaxFileCount directoryWithMaxFileCountToAdd;
                for (DirectoryWithMaxFileCount pendingDirectoryWithMaxFileCount : pendingConfig.getDirectoriesWithMaxFileCount()) {
                    for (DirectoryWithMaxFileCount directoryWithMaxFileCount : updatedConfig.getDirectoriesWithMaxFileCount()) {
                        if (pendingDirectoryWithMaxFileCount.getDirectoryName().equals(directoryWithMaxFileCount.getDirectoryName())) {
                            if (pendingDirectoryWithMaxFileCount.getMaxFileCount() < 1) {
                                throw new ValueInConfigCannotBeLessThanOneException("maxFileCount=" + pendingDirectoryWithMaxFileCount.getMaxFileCount());
                            }
                            directoryWithMaxFileCountToAdd = new DirectoryWithMaxFileCount(directoryWithMaxFileCount.getDirectoryName(),
                                    directoryWithMaxFileCount.getMaxFileCount() + pendingDirectoryWithMaxFileCount.getMaxFileCount());
                        }
                        else {
                            directoryWithMaxFileCountToAdd = new DirectoryWithMaxFileCount(directoryWithMaxFileCount.getDirectoryName(), pendingDirectoryWithMaxFileCount.getMaxFileCount());
                        }
                        newListOfDirectoriesWithMaxFileCount.add(directoryWithMaxFileCountToAdd);
                    }
                }
                updatedConfig.setDirectoriesWithMaxFileCount(newListOfDirectoriesWithMaxFileCount);
            }
        }
        else {
            throw new InvalidParametersException(configUpdateType.toString());
        }
        String configJson = objectMapper.writeValueAsString(updatedConfig);
        deleteFiles(configPathString);
        FileUtils.writeStringToFile(configPath.toFile(), configJson, "UTF-8");
    }
    @Override
    public void writeToFile(String filePathString, final String textToWrite) throws NonExistentRepositoryException, IOException, MaxRepositorySizeExceededException, InvalidParametersException, NoFileAtPathException, BadPathException {
        filePathString = replaceSlashesInPath(filePathString);
        String repositoryName = filePathString.split("/")[0];
        DirectoryHandlerConfig config = getConfig(repositoryName);
        if (maxRepositorySizeExceededCheck(config, repositoryName, textToWrite)) {
            throw new MaxRepositorySizeExceededException(repositoryName);
        }
        FileUtils.writeStringToFile(workingDirectory.resolve(Paths.get(filePathString)).toFile(), textToWrite, "UTF-8", true);
    }
    @Override
    public List<FilteredLocalFile> filterFileList(final List<LocalFile> fileList, final String filtersString) throws BadFiltersException {
        List<FilteredLocalFile> filteredLocalFileList = new ArrayList<>();
        if(badFiltersCheck(filtersString)){
            throw new BadFiltersException(filtersString);
        }
        String[] filters = filtersString.split(",");
        for(LocalFile localFile : fileList){
            FilteredLocalFile filteredLocalFile = new FilteredLocalFile();
            for(String filter : filters){
                switch (filter) {
                    case "name" -> filteredLocalFile.setName(localFile.getFile().getName());
                    case "size" -> filteredLocalFile.setSize(String.valueOf(localFile.getFileMetadata().size()));
                    case "dateCreated" -> {
                        if(localFile.getFileMetadata().creationTime() != null){
                            filteredLocalFile.setDateCreated(String.valueOf(String.valueOf(localFile.getFileMetadata().creationTime())));
                        }
                        else{
                            filteredLocalFile.setDateCreated("undefined");
                        }

                    }
                    case "dateModified" -> {
                        if(localFile.getFileMetadata().lastModifiedTime() != null){
                            filteredLocalFile.setDateModified(String.valueOf(localFile.getFileMetadata().lastModifiedTime()));
                        }
                        else{
                            filteredLocalFile.setDateCreated("undefined");
                        }
                    }
                }
            }
            filteredLocalFileList.add(filteredLocalFile);
        }
        return filteredLocalFileList;
    }
    public static DirectoryHandlerLocalImplementation getInstance() {
        if (instance == null) {
            instance = new DirectoryHandlerLocalImplementation();
        }
        return instance;
    }
    protected boolean badFiltersCheck(final String filtersString){
        String[] filters = filtersString.split(",");
        boolean badFilter = false;
        for (String filter : filters) {
            badFilter = switch (filter) {
                case "name", "size", "dateCreated", "dateModified" -> false;
                default -> true;
            };
        }
        return badFilter;
    }
    protected boolean badPathCheck(String filePathString) {
        filePathString = replaceSlashesInPath(filePathString);
        try {
            workingDirectory.resolve(Paths.get(filePathString));
        }
        catch (InvalidPathException e) {
            return true;
        }
        return false;
    }
    protected boolean excludedExtensionsCheck(final DirectoryHandlerConfig config, String filePathString) {
        filePathString = replaceSlashesInPath(filePathString);
        for (String excludedExtension : config.getExcludedExtensions()) {
            if (filePathString.endsWith(excludedExtension)) {
                return true;
            }
        }
        return false;
    }
    protected DirectoryHandlerConfig generateConfigFromString(final String configString) throws InvalidParametersException, BadPathException, ValueInConfigCannotBeLessThanOneException {
        long maxRepositorySize = 1073741824;
        List<String> excludedExtensions = new ArrayList<>();
        List<DirectoryWithMaxFileCount> directoriesWithMaxFileCount = new ArrayList<>();
        String[] configParameters = configString.split(";");
        for (String configParameter : configParameters) {
            String configKey;
            String configValue;
            if (configParameter.contains("=")) {
                String[] configKeyAndValue = configParameter.split("=");
                if (configKeyAndValue.length == 2) {
                    configKey = configKeyAndValue[0];
                    configValue = configKeyAndValue[1];
                    if (configKey.equals("maxRepositorySize")) {
                        try {
                            maxRepositorySize = Long.parseLong(configValue);
                        }
                        catch (NumberFormatException e) {
                            throw new InvalidParametersException(configValue);
                        }
                        if (maxRepositorySize < 1) {
                            throw new ValueInConfigCannotBeLessThanOneException("maxRepositorySize=" + maxRepositorySize);
                        }
                    }
                    else if (configKey.equals("excludedExtensions")) {
                        String[] excludedExtensionsParameter = configValue.split(",");
                        for (String excludedExtension : excludedExtensionsParameter) {
                            if (StringUtils.isAlpha(excludedExtension)) {
                                excludedExtensions.add(excludedExtension);
                            }
                            else {
                                throw new InvalidParametersException(excludedExtension);
                            }
                        }
                    }
                    else if (configKey.equals("directoriesWithMaxFileCount")) {
                        String[] directoriesWithMaxFileCountParameters = configValue.split(",");
                        for (String directoriesWithMaxFileCountParameter : directoriesWithMaxFileCountParameters) {
                            DirectoryWithMaxFileCount directoryWithMaxFileCount;
                            if (directoriesWithMaxFileCountParameter.contains("-")) {
                                String[] directoryWithMaxFileCountPair = directoriesWithMaxFileCountParameter.split("-");
                                String directoryName = directoryWithMaxFileCountPair[0];
                                int maxFileCount;
                                if (!badPathCheck(directoryName)) {
                                    try {
                                        maxFileCount = Integer.parseInt(directoryWithMaxFileCountPair[1]);
                                    }
                                    catch (NumberFormatException e) {
                                        throw new InvalidParametersException(directoryWithMaxFileCountPair[1]);
                                    }
                                    if (maxFileCount < 1) {
                                        throw new ValueInConfigCannotBeLessThanOneException("maxFileCount=" + maxFileCount);
                                    }
                                    directoryWithMaxFileCount = new DirectoryWithMaxFileCount(directoryName, maxFileCount);
                                }
                                else {
                                    throw new BadPathException(directoryName);
                                }
                            }
                            else {
                                throw new InvalidParametersException(directoriesWithMaxFileCountParameter);
                            }
                            directoriesWithMaxFileCount.add(directoryWithMaxFileCount);
                        }
                    }
                    else {
                        throw new InvalidParametersException(configKey);
                    }
                }
                else {
                    throw new InvalidParametersException(Arrays.toString(configKeyAndValue));
                }
            }
            else {
                throw new InvalidParametersException(configParameter);
            }
        }
        return new DirectoryHandlerConfig(maxRepositorySize, excludedExtensions, directoriesWithMaxFileCount);
    }
    protected boolean maxFileCountExceededCheck(final DirectoryHandlerConfig config, String parentDirectoryPathString) {
        parentDirectoryPathString = replaceSlashesInPath(parentDirectoryPathString);
        if (config.getDirectoriesWithMaxFileCount().size() > 0) {
            for (DirectoryWithMaxFileCount directoryWithMaxFileCount : config.getDirectoriesWithMaxFileCount()) {
                if (directoryWithMaxFileCount.getDirectoryName().equals(parentDirectoryPathString)) {
                    File file = workingDirectory.resolve(parentDirectoryPathString).toFile();
                    if (Objects.requireNonNull(file.listFiles()).length + 1 > directoryWithMaxFileCount.getMaxFileCount()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    protected boolean maxRepositorySizeExceededCheck(final DirectoryHandlerConfig config, final String repositoryName, final String textToWrite) throws NoFileAtPathException, BadPathException {
        return getDirectorySize(repositoryName) + textToWrite.getBytes().length > config.getMaxRepositorySize();
    }
    protected boolean noFileAtPathCheck(String filePathString) {
        filePathString = replaceSlashesInPath(filePathString);
        return !Files.exists(workingDirectory.resolve(Paths.get(filePathString)));
    }
    protected boolean nonExistentRepositoryCheck(final String repositoryName) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<LocalFile> repositories = getFilesInDirectory(null, false, false, true, SortingType.NAME, OrderType.ASCENDING);
        boolean found = false;
        for (LocalFile repository : repositories) {
            if (repository.getFile().getName().equals(repositoryName)) {
                found = true;
            }
        }
        return !found;
    }
    protected String replaceSlashesInPath(final String filePathString) {
        return StringUtils.replaceChars(filePathString, "\\", "/");
    }
    protected void saveConfig(final String repositoryName, final DirectoryHandlerConfig directoryHandlerConfig) throws NonExistentRepositoryException, IOException, InvalidParametersException, NoFileAtPathException, BadPathException {
        if (nonExistentRepositoryCheck(repositoryName)) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        Path configPath = workingDirectory.resolve(repositoryName).resolve("config.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(configPath.toFile(), directoryHandlerConfig);
    }
    protected List<LocalFile> sortList(List<LocalFile> listToSort, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException {
        if (listToSort == null) {
            throw new NullPointerException();
        }
        if (sortingType == SortingType.NONE) {
            return listToSort;
        }
        if (sortingType == SortingType.NAME) {
            listToSort.sort(new LocalComparators.NameComparator());
        }
        else if (sortingType == SortingType.SIZE) {
            listToSort.sort(new LocalComparators.SizeComparator());
        }
        else if (sortingType == SortingType.DATE_CREATED) {
            listToSort.sort(new LocalComparators.CreationDateComparator());
        }
        else if (sortingType == SortingType.DATE_MODIFIED) {
            listToSort.sort(new LocalComparators.ModificationDateComparator());
        }
        else {
            throw new InvalidParametersException(sortingType.toString());
        }
        if (orderType.equals(OrderType.ASCENDING)) {
            return listToSort;
        }
        else if (orderType.equals(OrderType.DESCENDING)) {
            Collections.reverse(listToSort);
            return listToSort;
        }
        else {
            throw new InvalidParametersException(orderType.toString());
        }
    }
}