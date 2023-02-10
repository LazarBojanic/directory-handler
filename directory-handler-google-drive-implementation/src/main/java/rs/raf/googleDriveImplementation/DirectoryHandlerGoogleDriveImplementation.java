package rs.raf.googleDriveImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import rs.raf.config.DirectoryHandlerConfig;
import rs.raf.enums.ConfigUpdateType;
import rs.raf.config.DirectoryWithMaxFileCount;
import rs.raf.enums.OrderType;
import rs.raf.enums.SearchType;
import rs.raf.enums.SortingType;
import rs.raf.model.FilteredGoogleDriveFile;
import rs.raf.specification.DirectoryHandlerManager;
import rs.raf.specification.IDirectoryHandlerSpecification;
import rs.raf.util.GoogleDriveComparators;

import java.io.*;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import rs.raf.exception.DirectoryHandlerExceptions.*;

public class DirectoryHandlerGoogleDriveImplementation implements IDirectoryHandlerSpecification<File, FilteredGoogleDriveFile> {
    private static final String APPLICATION_NAME = "directory-handler-lbojanic";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_SCRIPTS);
    public static List<File> allFilesList;
    private static Drive googleDriveClient;
    private static HttpTransport HTTP_TRANSPORT = null;
    private static DirectoryHandlerGoogleDriveImplementation instance;
    static {
        try {
            DirectoryHandlerManager.registerDirectoryHandler(DirectoryHandlerGoogleDriveImplementation.getInstance());
        }
        catch (GeneralSecurityException | IOException | InvalidParametersException | NoFileAtPathException |
               BadPathException e) {
            throw new RuntimeException(e);
        }
    }
    protected Path workingDirectory = null;
    private String TOKENS_DIRECTORY_PATH = null;
    private String CREDENTIALS_FILE_PATH = null;
    public DirectoryHandlerGoogleDriveImplementation() {
        super();
    }
    @Override
    public void copyFiles(String filePathsString, String copyDestinationDirectoryString, final boolean overwrite) throws IOException, BadPathException, NoFileAtPathException, InvalidParametersException, NonExistentRepositoryException, MaxFileCountExceededException {
        filePathsString = replaceSlashesInPath(filePathsString);
        copyDestinationDirectoryString = replaceSlashesInPath(copyDestinationDirectoryString);
        if (badPathCheck(copyDestinationDirectoryString)) {
            throw new BadPathException(copyDestinationDirectoryString);
        }
        if (noFileAtPathCheck(copyDestinationDirectoryString)) {
            throw new NoFileAtPathException(copyDestinationDirectoryString);
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
            googleDriveClient.files().update(getFileIdByPath(filePathString), null)
                    .setAddParents(getFileIdByPath(copyDestinationDirectoryString))
                    .setFields("id, name, parents, mimeType")
                    .execute();
        }
    }
    @Override
    public void createConfig(final String repositoryName, final String configString) throws IOException, NoFileAtPathException, BadPathException, InvalidParametersException, NonExistentRepositoryException, ValueInConfigCannotBeLessThanOneException {
        Path configPath = workingDirectory.resolve("temp").resolve("config.json");
        Files.createFile(configPath);
        if (configString == null) {
            saveConfig(repositoryName, new DirectoryHandlerConfig());
        }
        else {
            saveConfig(repositoryName, generateConfigFromString(configString));
        }
        uploadFile(String.format("%s/config.json", repositoryName), configPath.toFile());
        clearTemp();
    }
    @Override
    public void createDirectories(String directoryPathsString) throws NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, InvalidParametersException, NonExistentRepositoryException {
        directoryPathsString = replaceSlashesInPath(directoryPathsString);
        List<String> directoryPathsList = List.of(directoryPathsString.split("-more-"));
        for (String directoryPathString : directoryPathsList) {
            if (badPathCheck(directoryPathString)) {
                throw new BadPathException(directoryPathString);
            }
            String repositoryName = directoryPathString.split("/")[0];
            if (nonExistentRepositoryCheck(repositoryName)) {
                throw new NonExistentRepositoryException(repositoryName);
            }
            DirectoryHandlerConfig config = getConfig(repositoryName);
            String parentDirectory = replaceSlashesInPath(Paths.get(directoryPathString).getParent().toString());
            String directoryName = Paths.get(directoryPathString).getFileName().toString();
            if (noFileAtPathCheck(parentDirectory)) {
                createDirectories(parentDirectory);
            }
            if (maxFileCountExceededCheck(config, parentDirectory)) {
                throw new MaxFileCountExceededException(parentDirectory);
            }
            File fileMetadata = new File();
            fileMetadata.setName(directoryName);
            fileMetadata.setParents(Collections.singletonList(getFileIdByPath(parentDirectory)));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            googleDriveClient.files().create(fileMetadata).setFields("id, name, parents, mimeType").execute();
        }
    }
    @Override
    public void createFiles(String filePathsString) throws NoFileAtPathException, IOException, BadPathException, MaxFileCountExceededException, InvalidParametersException, NonExistentRepositoryException, FileExtensionException, MaxRepositorySizeExceededException {
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
            String fileName = Paths.get(filePathString).getFileName().toString();
            if (noFileAtPathCheck(parentDirectory)) {
                createDirectories(parentDirectory);
            }
            if (maxFileCountExceededCheck(config, parentDirectory)) {
                throw new MaxFileCountExceededException(parentDirectory);
            }
            if (excludedExtensionsCheck(config, filePathString)) {
                throw new FileExtensionException(filePathString);
            }
            String sampleText = "sampleText";
            if (maxRepositorySizeExceededCheck(config, repositoryName, sampleText.getBytes().length)) {
                throw new MaxRepositorySizeExceededException(repositoryName);
            }
            Path tempFilePath = workingDirectory.resolve("temp").resolve("tempFile.txt");
            FileUtils.writeStringToFile(tempFilePath.toFile(), "sampleText", "UTF-8");
            FileContent mediaContent = new FileContent("media/text", tempFilePath.toFile());
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(getFileIdByPath(parentDirectory)));
            fileMetadata.setMimeType("application/octet-stream");
            googleDriveClient.files().create(fileMetadata, mediaContent).setFields("id, name, parents, mimeType").execute();
            clearTemp();
        }
    }
    @Override
    public void createRepository(final String repositoryName, final String configString) throws NonExistentRepositoryException, NoFileAtPathException, IOException, BadPathException, InvalidParametersException, ValueInConfigCannotBeLessThanOneException {
        if (badPathCheck(repositoryName)) {
            throw new BadPathException(repositoryName);
        }
        File fileMetadata = new File();
        fileMetadata.setName(repositoryName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        googleDriveClient.files().create(fileMetadata).setFields("id, name, mimeType").execute();
        createConfig(repositoryName, configString);
    }
    @Override
    public void deleteFiles(String filePathsString) throws NoFileAtPathException, BadPathException, IOException {
        filePathsString = replaceSlashesInPath(filePathsString);
        List<String> filePathsList = List.of(filePathsString.split("-more-"));
        for (String filePathString : filePathsList) {
            if (badPathCheck(filePathString)) {
                throw new BadPathException(filePathString);
            }
            if (noFileAtPathCheck(filePathString)) {
                throw new NoFileAtPathException(filePathString);
            }
            googleDriveClient.files().delete(getFileIdByPath(filePathString)).execute();
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
            Drive.Files.Get getFile = googleDriveClient.files().get(getFileIdByPath(filePathString));
            if (getFile.execute().getMimeType().equals("application/vnd.google-apps.folder")) {
                throw new UnsupportedOperationException();
            }
            OutputStream outputStream = new ByteArrayOutputStream();
            getFile.executeMediaAndDownloadTo(outputStream);
            String fileName = String.valueOf(Paths.get(filePathString).getFileName());
            java.io.File destinationFile = downloadDestinationDirectoryPath.resolve(fileName).toFile();
            InputStream inputStream = new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray());
            if (overwrite) {
                FileUtils.copyInputStreamToFile(inputStream, destinationFile);
            }
            else {
                Path tempFilePath = workingDirectory.resolve("temp").resolve(fileName);
                java.io.File tempFile = tempFilePath.toFile();
                FileUtils.copyInputStreamToFile(inputStream, tempFile);
                String suffix = "";
                int i = 0;
                while (true) {
                    try {
                        if(fileName.contains(".")){
                            Files.copy(tempFilePath, downloadDestinationDirectoryPath.resolve(fileName.substring(0, fileName.indexOf(".")) + suffix + fileName.substring(fileName.indexOf("."))));
                        }
                        else{
                            Files.copy(tempFilePath, downloadDestinationDirectoryPath.resolve(fileName + suffix));
                        }
                        break;
                    }
                    catch (IOException e) {
                        i++;
                        suffix = String.valueOf(i);
                    }
                }
            }
            inputStream.close();
            outputStream.close();
        }
    }
    @Override
    public DirectoryHandlerConfig getConfig(final String repositoryName) throws NoFileAtPathException, IOException, BadPathException, NonExistentRepositoryException, InvalidParametersException {
        if (nonExistentRepositoryCheck(repositoryName)) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        downloadFiles(String.format("%s/config.json", repositoryName), "temp", true);
        Path configPath = workingDirectory.resolve("temp").resolve("config.json");
        String configJson = FileUtils.readFileToString(configPath.toFile(), "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(configJson, DirectoryHandlerConfig.class);
    }
    @Override
    public long getDirectorySize(String directoryPathString) throws BadPathException, NoFileAtPathException, IOException {
        directoryPathString = replaceSlashesInPath(directoryPathString);
        if (badPathCheck(directoryPathString)) {
            throw new BadPathException(directoryPathString);
        }
        if (noFileAtPathCheck(directoryPathString)) {
            throw new NoFileAtPathException(directoryPathString);
        }
        long directorySize = 0;
        if (directoryPathString == null) {
            for (File file : allFilesList) {
                if (!file.getMimeType().equals("application/vnd.google-apps.folder")) {
                    if (file.getSize() != null) {
                        directorySize += file.getSize();
                    }
                }
            }
        }
        else {
            FileList result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false", getFileIdByPath(directoryPathString))).setFields("files(id, name, parents, mimeType)").setSpaces("drive").execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                for (File file : files) {
                    if (file.getSize() != null) {
                        directorySize += file.getSize();
                    }
                }
            }
        }
        return directorySize;
    }
    @Override
    public int getFileCount(String directoryPathString) throws BadPathException, NoFileAtPathException, IOException {
        directoryPathString = replaceSlashesInPath(directoryPathString);
        if (badPathCheck(directoryPathString)) {
            throw new BadPathException(directoryPathString);
        }
        if (noFileAtPathCheck(directoryPathString)) {
            throw new NoFileAtPathException(directoryPathString);
        }
        FileList result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and trashed = false", getFileIdByPath(directoryPathString))).setFields("files(id, name, parents, mimeType)").setSpaces("drive").execute();
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.size();
        }
        return 0;
    }
    @Override
    public long getFileSize(String filePathString) throws BadPathException, NoFileAtPathException, IOException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        if (noFileAtPathCheck(filePathString)) {
            throw new NoFileAtPathException(filePathString);
        }
        long fileSize;
        String fileName = String.valueOf(Paths.get(filePathString).getFileName());
        downloadFiles(filePathString, "temp", true);
        fileSize = FileUtils.sizeOf(workingDirectory.resolve(Paths.get("temp")).resolve(fileName).toFile());
        clearTemp();
        return fileSize;
    }
    @Override
    public List<File> getFilesForDateRange(final String directoryPathString, final String startDate, final String endDate, final boolean dateCreated, final boolean dateModified, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, ParseException, NoFileAtPathException, IOException, BadPathException {
        Date rangeStartDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
        Date rangeEndDate = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);
        if (rangeStartDate.compareTo(rangeEndDate) > 0) {
            throw new InvalidParametersException(startDate + "; " + endDate);
        }
        List<File> fileList = new ArrayList<>();
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (File file : directoryToSearchList) {
            if (dateCreated && !dateModified) {
                Date fileCreationDate = new Date(file.getCreatedTime().getValue());
                if (fileCreationDate.compareTo(rangeStartDate) >= 0 && fileCreationDate.compareTo(rangeEndDate) <= 0) {
                    fileList.add(file);
                }
            }
            if (!dateCreated && dateModified) {
                Date fileModificationDate = new Date(file.getModifiedTime().getValue());
                if (fileModificationDate.compareTo(rangeStartDate) >= 0 && fileModificationDate.compareTo(rangeEndDate) <= 0) {
                    fileList.add(file);
                }
            }
            if (dateCreated && dateModified) {
                Date fileCreationDate = new Date(file.getCreatedTime().getValue());
                Date fileModificationDate = new Date(file.getModifiedTime().getValue());
                if ((fileCreationDate.compareTo(rangeStartDate) >= 0 && fileCreationDate.compareTo(rangeEndDate) <= 0) || (fileModificationDate.compareTo(rangeStartDate) >= 0 && fileModificationDate.compareTo(rangeEndDate) <= 0)) {
                    fileList.add(file);
                }
            }
            if (!dateCreated && !dateModified) {
                throw new InvalidParametersException("dateCreated: false; dateModified: false");
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForExcludedExtensions(final String directoryPathString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<File> fileList = new ArrayList<>();
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (File file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    fileList.add(file);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForExtensions(final String directoryPathString, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<File> fileList = new ArrayList<>();
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (File file : directoryToSearchList) {
            for (String extension : searchExtensionsList) {
                if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    fileList.add(file);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForExtensionsAndExcludedExtensions(final String directoryPathString, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<File> fileList = new ArrayList<>();
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (File file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    for (String extension : searchExtensionsList) {
                        if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                            fileList.add(file);
                        }
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForSearchName(final String directoryPathString, final String search, final SearchType searchType, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        List<File> fileList = new ArrayList<>();
        for (File file : directoryToSearchList) {
            if (searchType.equals(SearchType.CONTAINS)) {
                if (file.getName().contains(search)) {
                    fileList.add(file);
                }
            }
            else if (searchType.equals(SearchType.STARTS_WITH)) {
                if (file.getName().startsWith(search)) {
                    fileList.add(file);
                }
            }
            else if (searchType.equals(SearchType.ENDS_WITH)) {
                if (file.getName().endsWith(search)) {
                    fileList.add(file);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForSearchNameAndExcludedExtensions(final String directoryPathString, final String search, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        List<File> fileList = new ArrayList<>();
        for (File file : directoryToSearchList) {
            for (String extension : searchExcludedExtensionsList) {
                if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    if (file.getName().toLowerCase().contains(search.toLowerCase())) {
                        fileList.add(file);
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForSearchNameAndExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        List<File> fileList = new ArrayList<>();
        for (File file : directoryToSearchList) {
            for (String extension : searchExtensionsList) {
                if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    if (file.getName().toLowerCase().contains(search.toLowerCase())) {
                        fileList.add(file);
                    }
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    @Override
    public List<File> getFilesForSearchNameAndExtensionsAndExcludedExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchExtensionsList = List.of(searchExtensionsString.split(","));
        List<String> searchExcludedExtensionsList = List.of(searchExcludedExtensionsString.split(","));
        List<File> fileList = new ArrayList<>();
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        for (File file : directoryToSearchList) {
            for (String excludedExtension : searchExcludedExtensionsList) {
                if (!file.getName().toLowerCase().endsWith(excludedExtension.toLowerCase())) {
                    for (String extension : searchExtensionsList) {
                        if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                            if (file.getName().toLowerCase().contains(search.toLowerCase())) {
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
    public List<File> getFilesInDirectory(final String directoryPathString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, IOException, BadPathException, NoFileAtPathException {
        String directoryId;
        if (directoryPathString == null) {
            return getFileListInDirectoryById(null, recursive, includeFiles, includeDirectories, sortingType, orderType);
        }
        else {
            if (badPathCheck(directoryPathString)) {
                throw new BadPathException(directoryPathString);
            }
            if (noFileAtPathCheck(directoryPathString)) {
                throw new NoFileAtPathException(directoryPathString);
            }
            directoryId = getFileIdByPath(directoryPathString);
        }
        return getFileListInDirectoryById(directoryId, recursive, includeFiles, includeDirectories, sortingType, orderType);
    }
    @Override
    public List<File> getFilesWithNames(final String directoryPathString, final String searchListString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<String> searchList = List.of(searchListString.split(","));
        List<File> directoryToSearchList = getFilesInDirectory(directoryPathString, recursive, includeFiles, includeDirectories, SortingType.NONE, OrderType.ASCENDING);
        List<File> foundFiles = new ArrayList<>();
        for (File file : directoryToSearchList) {
            for (String search : searchList) {
                if (file.getName().equals(search)) {
                    foundFiles.add(file);
                }
            }
        }
        return sortList(foundFiles, sortingType, orderType);
    }
    @Override
    public void moveFiles(String filePathsString, String moveDestinationDirectoryString, boolean overwrite) throws NoFileAtPathException, IOException, BadPathException, InvalidParametersException, NonExistentRepositoryException, MaxFileCountExceededException {
        filePathsString = replaceSlashesInPath(filePathsString);
        moveDestinationDirectoryString = replaceSlashesInPath(moveDestinationDirectoryString);
        if (badPathCheck(moveDestinationDirectoryString)) {
            throw new BadPathException(moveDestinationDirectoryString);
        }
        if (noFileAtPathCheck(moveDestinationDirectoryString)) {
            throw new NoFileAtPathException(moveDestinationDirectoryString);
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
            File file = googleDriveClient.files().get(getFileIdByPath(filePathString)).setFields("id, name, parents, mimeType").execute();
            StringBuilder previousParents = new StringBuilder();
            for (String parent : file.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }
            googleDriveClient
                    .files()
                    .update(getFileIdByPath(filePathString), null)
                    .setAddParents(getFileIdByPath(moveDestinationDirectoryString))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, name, parents, mimeType")
                    .execute();
        }
    }
    @Override
    public void renameFile(String filePathString, final String newFileName) throws NoFileAtPathException, IOException, BadPathException, InvalidParametersException, NonExistentRepositoryException, FileExtensionException {
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
        String fileName = String.valueOf(Paths.get(filePathString).getFileName());
        downloadFiles(filePathString, "temp", true);
        Path fileToRenamePath = workingDirectory.resolve("temp").resolve(fileName);
        java.io.File fileToRename = fileToRenamePath.toFile();
        Path renamedFilePath = fileToRenamePath.resolveSibling(newFileName);
        java.io.File renamedFile = renamedFilePath.toFile();
        boolean renameSuccessful = fileToRename.renameTo(renamedFile);
        if (!renameSuccessful) {
            throw new NullPointerException();
        }
        deleteFiles(filePathString);
        uploadFile(filePathString, renamedFile);
        clearTemp();
    }
    @Override
    public void setWorkingDirectory(final String rootPathString) throws NoFileAtPathException, GeneralSecurityException, InvalidParametersException, IOException, BadPathException {
        if (rootPathString.equals("default")) {
            workingDirectory = Paths.get(System.getProperty("user.dir")).resolve("DirectoryHandlerGoogleDrive");
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
        TOKENS_DIRECTORY_PATH = workingDirectory.resolve("tokens").toString();
        CREDENTIALS_FILE_PATH = Paths.get(System.getProperty("user.dir")).resolve("credentials.json").toString();
        initGoogleDrive();
    }
    @Override
    public void updateConfig(final String repositoryName, final String configString, final ConfigUpdateType configUpdateType) throws NoFileAtPathException, NonExistentRepositoryException, IOException, BadPathException, InvalidParametersException, ValueInConfigCannotBeLessThanOneException {
        DirectoryHandlerConfig currentConfig = getConfig(repositoryName);
        if (repositoryName.equals("")) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        DirectoryHandlerConfig pendingConfig = generateConfigFromString(configString);
        DirectoryHandlerConfig updatedConfig = currentConfig;
        Path configPath = workingDirectory.resolve("temp").resolve("config.json");
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
        FileUtils.writeStringToFile(configPath.toFile(), configJson, "UTF-8");
        deleteFiles(String.format("%s/config.json", repositoryName));
        uploadFile(String.format("%s/config.json", repositoryName), workingDirectory.resolve("temp").resolve("config.json").toFile());
        clearTemp();
    }
    @Override
    public void writeToFile(String filePathString, final String textToWrite) throws BadPathException, NoFileAtPathException, IOException, InvalidParametersException, NonExistentRepositoryException, FileExtensionException, MaxRepositorySizeExceededException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        if (noFileAtPathCheck(filePathString)) {
            throw new NoFileAtPathException(filePathString);
        }
        String repositoryName = filePathString.split("/")[0];
        DirectoryHandlerConfig config = getConfig(repositoryName);
        if (excludedExtensionsCheck(config, filePathString)) {
            throw new FileExtensionException(filePathString);
        }
        if (maxRepositorySizeExceededCheck(config, repositoryName, textToWrite.getBytes().length)) {
            throw new MaxRepositorySizeExceededException(repositoryName);
        }
        downloadFiles(filePathString, "temp", true);
        String fileName = Paths.get(filePathString).getFileName().toString();
        String parentPathString = replaceSlashesInPath(Paths.get(filePathString).getParent().toString());
        String parentId = getFileIdByPath(parentPathString);
        File fileMetadata = new File();
        String editedName = fileName + " (Edited)";
        fileMetadata.setName(editedName);
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType("application/octet-stream");
        java.io.File tempFile = workingDirectory.resolve(Paths.get("temp")).resolve(fileName).toFile();
        FileUtils.writeStringToFile(tempFile, textToWrite, "UTF-8", true);
        FileContent mediaContent = new FileContent("media/text", tempFile);
        googleDriveClient.files().create(fileMetadata, mediaContent).setFields("id, name, parents, mimeType").execute();
        deleteFiles(filePathString);
        clearTemp();
        renameFile(parentPathString + "/" + editedName, fileName);
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
    @Override
    public List<FilteredGoogleDriveFile> filterFileList(final List<File> fileList, final String filtersString) throws BadFiltersException {
        List<FilteredGoogleDriveFile> filteredLocalFileList = new ArrayList<>();
        if(badFiltersCheck(filtersString)){
            throw new BadFiltersException(filtersString);
        }
        String[] filters = filtersString.split(",");
        for(File file : fileList){
            FilteredGoogleDriveFile filteredLocalFile = new FilteredGoogleDriveFile();
            for(String filter : filters){
                switch (filter) {
                    case "name" -> {
                        if(file.getName() != null){
                            filteredLocalFile.setName(file.getName());
                        }
                        else{
                            filteredLocalFile.setName("undefined");
                        }
                    }
                    case "size" -> filteredLocalFile.setSize(String.valueOf(file.size()));
                    case "dateCreated" -> {
                        if(file.getCreatedTime() != null){
                            filteredLocalFile.setDateCreated(String.valueOf(file.getCreatedTime()));
                        }
                        else{
                            filteredLocalFile.setDateCreated("undefined");
                        }

                    }
                    case "dateModified" -> {
                        if(file.getModifiedTime() != null){
                            filteredLocalFile.setDateModified(String.valueOf(file.getModifiedTime()));
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
    public static DirectoryHandlerGoogleDriveImplementation getInstance() throws GeneralSecurityException, IOException, InvalidParametersException, NoFileAtPathException, BadPathException {
        if (instance == null) {
            instance = new DirectoryHandlerGoogleDriveImplementation();
        }
        return instance;
    }
    protected void authorizeGoogleDriveClient() throws IOException, GeneralSecurityException {
        InputStream credentialsInputStream = new FileInputStream(CREDENTIALS_FILE_PATH);
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credentials = getCredentials(credentialsInputStream, CREDENTIALS_FILE_PATH, HTTP_TRANSPORT, JSON_FACTORY, SCOPES, TOKENS_DIRECTORY_PATH);
        googleDriveClient = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials).setApplicationName(APPLICATION_NAME).build();
        credentialsInputStream.close();
    }
    protected boolean badPathCheck(String filePathString) {
        filePathString = replaceSlashesInPath(filePathString);
        try {
            Paths.get(filePathString);
        }
        catch (InvalidPathException e) {
            return true;
        }
        return false;
    }
    protected void clearTemp() throws IOException {
        java.io.File tempDirectory = workingDirectory.resolve("temp").toFile();
        if (tempDirectory.exists()) {
            FileUtils.cleanDirectory(tempDirectory);
        }
        else {
            Files.createDirectory(workingDirectory.resolve("temp"));
        }
    }
    protected boolean excludedExtensionsCheck(final DirectoryHandlerConfig config, String filePathString) {
        filePathString = replaceSlashesInPath(filePathString);
        if (config.getExcludedExtensions() != null && config.getExcludedExtensions().size() > 0) {
            for (String excludedExtension : config.getExcludedExtensions()) {
                if (filePathString.endsWith(excludedExtension)) {
                    return true;
                }
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
    protected Credential getCredentials(final InputStream inputStream, final String CREDENTIALS_FILE_PATH, final Object HTTP_TRANSPORT, final Object JSON_FACTORY, final List SCOPES, final String TOKENS_DIRECTORY_PATH) throws IOException {
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load((JsonFactory) JSON_FACTORY, new InputStreamReader(inputStream));
        inputStream.close();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder((HttpTransport) HTTP_TRANSPORT, (JsonFactory) JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(5555).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    protected List<File> getCurrentDirectoryFiles(final String currentDirectoryId) throws IOException {
        FileList result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and trashed = false", currentDirectoryId)).setFields("files(id, name, parents, mimeType)").setSpaces("drive").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return files;
    }
    protected String getFileIdByPath(String filePathString) throws IOException, NoFileAtPathException, BadPathException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        String[] directories = filePathString.split("/");
        List<File> currentFileList;
        String currentDirectoryId = "root";
        boolean found;
        int i = 0;
        while (i < directories.length) {
            currentFileList = getCurrentDirectoryFiles(currentDirectoryId);
            found = false;
            for (File file : currentFileList) {
                if (file.getName().equals(directories[i])) {
                    currentDirectoryId = file.getId();
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NoFileAtPathException(filePathString);
            }
            i++;
        }
        return currentDirectoryId;
    }
    protected List<File> getFileListInDirectoryById(final String directoryId, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, IOException {
        List<File> fileList = new ArrayList<>();
        if (!includeFiles && !includeDirectories) {
            throw new InvalidParametersException("Include files: false; Include directories: false");
        }
        if (directoryId == null) {
            FileList result = null;
            if (recursive) {
                if (includeFiles && !includeDirectories) {
                    result = googleDriveClient.files().list().setQ("mimeType != 'application/vnd.google-apps.folder' and trashed = false").setFields("nextPageToken, files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (!includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false").setFields("nextPageToken, files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ("trashed = false").setFields("nextPageToken, files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                List<File> files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    return new ArrayList<>();
                }
                else {
                    fileList.addAll(files);
                }
            }
            else {
                if (includeFiles && !includeDirectories) {
                    result = googleDriveClient.files().list().setQ("'root' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false").setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (!includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false").setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ("'root' in parents and trashed = false").setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                List<File> files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    return null;
                }
                else {
                    fileList.addAll(files);
                }
            }
        }
        else {
            if (recursive) {
                List<File> currentFileList = getFileListInDirectoryById(directoryId, false, true, true, sortingType, OrderType.ASCENDING);
                for (File file : currentFileList) {
                    if (file.getParents() != null && file.getParents().contains(directoryId)) {
                        if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                            fileList.add(file);
                            fileList.addAll(getFileListInDirectoryById(file.getId(), true, true, true, sortingType, OrderType.ASCENDING));
                        }
                        else {
                            fileList.add(file);
                        }
                    }
                }
            }
            else {
                FileList result = null;
                if (includeFiles && !includeDirectories) {
                    result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false", directoryId)).setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (!includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false", directoryId)).setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                if (includeFiles && includeDirectories) {
                    result = googleDriveClient.files().list().setQ(String.format("'%s' in parents and trashed = false", directoryId)).setFields("files(id, name, parents, mimeType, size, createdTime, modifiedTime)").setSpaces("drive").execute();
                }
                List<File> files = result.getFiles();
                if (files == null || files.isEmpty()) {
                    return new ArrayList<>();
                }
                else {
                    fileList.addAll(files);
                }
            }
        }
        return sortList(fileList, sortingType, orderType);
    }
    protected void initGoogleDrive() throws GeneralSecurityException, IOException, NoFileAtPathException, InvalidParametersException, BadPathException {
        authorizeGoogleDriveClient();
        allFilesList = getFilesInDirectory(null, true, true, true, SortingType.NONE, OrderType.ASCENDING);
        clearTemp();
    }
    protected boolean maxFileCountExceededCheck(final DirectoryHandlerConfig config, String parentDirectoryPathString) throws BadPathException, NoFileAtPathException, IOException {
        parentDirectoryPathString = replaceSlashesInPath(parentDirectoryPathString);
        List<DirectoryWithMaxFileCount> directoriesWithMaxFileCounts = config.getDirectoriesWithMaxFileCount();
        if (directoriesWithMaxFileCounts != null && directoriesWithMaxFileCounts.size() > 0) {
            for (DirectoryWithMaxFileCount directoryWithMaxFileCount : directoriesWithMaxFileCounts) {
                if (directoryWithMaxFileCount.getDirectoryName().equals(parentDirectoryPathString)) {
                    if (getFileCount(parentDirectoryPathString) + 1 > directoryWithMaxFileCount.getMaxFileCount()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    protected boolean maxRepositorySizeExceededCheck(final DirectoryHandlerConfig config, final String repositoryName, long amountOfBytesToAdd) throws NoFileAtPathException, BadPathException, IOException {
        return getDirectorySize(repositoryName) + amountOfBytesToAdd > config.getMaxRepositorySize();
    }
    protected boolean noFileAtPathCheck(String filePathString) throws IOException, BadPathException {
        filePathString = replaceSlashesInPath(filePathString);
        try {
            getFileIdByPath(filePathString);
        }
        catch (NoFileAtPathException e) {
            return true;
        }
        return false;
    }
    protected boolean nonExistentRepositoryCheck(final String repositoryName) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException {
        List<File> repositories = getFilesInDirectory(null, false, false, true, SortingType.NONE, OrderType.ASCENDING);
        boolean found = false;
        for (File repository : repositories) {
            if (repository.getName().equals(repositoryName)) {
                found = true;
                break;
            }
        }
        return !found;
    }
    protected String replaceSlashesInPath(final String filePathString) {
        return StringUtils.replaceChars(filePathString, "\\", "/");
    }
    protected void saveConfig(final String repositoryName, final DirectoryHandlerConfig directoryHandlerConfig) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException, NonExistentRepositoryException {
        if (nonExistentRepositoryCheck(repositoryName)) {
            throw new NonExistentRepositoryException(repositoryName);
        }
        Path configPath = workingDirectory.resolve("temp").resolve("config.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(configPath.toFile(), directoryHandlerConfig);
    }
    protected List<File> sortList(List<File> listToSort, final SortingType sortingType, OrderType orderType) throws InvalidParametersException {
        if (listToSort == null) {
            throw new NullPointerException();
        }
        if (sortingType == SortingType.NONE) {
            return listToSort;
        }
        if (sortingType == SortingType.NAME) {
            listToSort.sort(new GoogleDriveComparators.NameComparator());
        }
        else if (sortingType == SortingType.SIZE) {
            listToSort.sort(new GoogleDriveComparators.SizeComparator());
        }
        else if (sortingType == SortingType.DATE_CREATED) {
            listToSort.sort(new GoogleDriveComparators.CreationDateComparator());
        }
        else if (sortingType == SortingType.DATE_MODIFIED) {
            listToSort.sort(new GoogleDriveComparators.ModificationDateComparator());
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
    protected void uploadFile(String filePathString, final java.io.File file) throws IOException, NoFileAtPathException, BadPathException {
        filePathString = replaceSlashesInPath(filePathString);
        if (badPathCheck(filePathString)) {
            throw new BadPathException(filePathString);
        }
        String fileName = file.getName();
        String parentDirectory = replaceSlashesInPath(Paths.get(filePathString).getParent().toString());
        if (noFileAtPathCheck(parentDirectory)) {
            throw new NoFileAtPathException(parentDirectory);
        }
        String parentId = getFileIdByPath(parentDirectory);
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType("application/octet-stream");
        FileContent mediaContent = new FileContent("application/octet-stream", file);
        googleDriveClient.files().create(fileMetadata, mediaContent).setFields("id, name, parents, mimeType").execute();
    }
}