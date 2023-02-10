package rs.raf.specification;

import rs.raf.enums.ConfigUpdateType;
import rs.raf.config.DirectoryHandlerConfig;
import rs.raf.enums.OrderType;
import rs.raf.enums.SearchType;
import rs.raf.enums.SortingType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import rs.raf.exception.DirectoryHandlerExceptions.*;

/**
 * Methods to use when using DirectoryHandler
 */
public interface IDirectoryHandlerSpecification<T1, T2> {
    /**
     * Copies one or more files at the path(s) specified to the destination directory path with the option of overwriting. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathsString                Path(s) of the file to copy. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @param copyDestinationDirectoryString Path to the destination directory. Paths are slash delimited and should start with a repository name.
     * @param overwrite                      whether to overwrite existing files or directories.
     * @throws IOException                    for IO reasons.
     * @throws BadPathException               if path is in a bad format.
     * @throws NoFileAtPathException          if no file exists at path.
     * @throws MaxFileCountExceededException  if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     */
    void copyFiles(String filePathsString, String copyDestinationDirectoryString, final boolean overwrite) throws IOException, BadPathException, NoFileAtPathException, MaxFileCountExceededException, InvalidParametersException, NonExistentRepositoryException;
    /**
     * Creates a config in the specified repository. Config can be parsed as null to create a default config, or as a custom config string.
     *
     * @param repositoryName name of the repository to create the config in
     * @param configString   String representation of custom config. Parameters in config string are delimited by ;. Parameters are split into keys and values by =. Possible parameter keys: maxRepositorySize, excludedExtensions, directoriesWithMaxFileCount.
     *                       maxRepositorySize is a long of bytes that is the maximum size of specified repository. Example: 1024.
     *                       excludedExtensions is a comma delimited string of extensions which the program will not allow files to be created with. Example: exe,bat
     *                       directoriesWithMaxFileCount is a comma delimited list of - delimited string which represent a key value pair of a directory name and the max file count that would be allowed to store inside. Example: dir1-25,dir2-50
     *                       Full config string example: maxRepositorySize=2048;excludedExtensions=jar,zip;directoriesWithMaxFileCount=exampleDir1-75,exampleDir2-100
     *                       Not all parameters have to be specified. Can be parsed as null in order to create a default config with maxRepositorySize=1073741824.
     * @throws IOException                               for IO reasons.
     * @throws BadPathException                          if path is in a bad format.
     * @throws NoFileAtPathException                     if no file exists at path.
     * @throws InvalidParametersException                if parameter(s) are invalid.
     * @throws NonExistentRepositoryException            if path starts with a non-existent repository name.
     * @throws ValueInConfigCannotBeLessThanOneException if value to put in config is less than one.
     */
    void createConfig(final String repositoryName, final String configString) throws IOException, NoFileAtPathException, BadPathException, InvalidParametersException, NonExistentRepositoryException, ValueInConfigCannotBeLessThanOneException;
    /**
     * Creates one or more directories at the path(s) specified. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * If parent directories don't exist, they will be created in accordance with config rules.
     *
     * @param directoryPathsString Path(s) of the directories to create. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @throws IOException                    for IO reasons.
     * @throws BadPathException               if path is in a bad format.
     * @throws NoFileAtPathException          if no file exists at path.
     * @throws MaxFileCountExceededException  if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     */
    void createDirectories(String directoryPathsString) throws NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, NonExistentRepositoryException, InvalidParametersException;
    /**
     * Creates one or more files at the path(s) specified. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * If parent directories don't exist, they will be created in accordance with config rules.
     *
     * @param filePathsString Path(s) of the files to create. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @throws IOException                        for IO reasons.
     * @throws BadPathException                   if path is in a bad format.
     * @throws NoFileAtPathException              if no file exists at path.
     * @throws MaxFileCountExceededException      if max file count for directory is exceeded.
     * @throws InvalidParametersException         if parameter(s) are invalid.
     * @throws NonExistentRepositoryException     if path starts with a non-existent repository name.
     * @throws FileExtensionException             if file has an extension that is forbidden in the config file.
     * @throws MaxRepositorySizeExceededException if max repository size is exceeded.
     */
    void createFiles(String filePathsString) throws IOException, BadPathException, MaxFileCountExceededException, FileExtensionException, MaxRepositorySizeExceededException, NoFileAtPathException, InvalidParametersException, NonExistentRepositoryException;
    /**
     * Creates a repository with the specified name and config
     *
     * @param repositoryName name of the repository to create
     * @param configString   String representation of custom config. Parameters in config string are delimited by ;. Parameters are split into keys and values by =. Possible parameter keys: maxRepositorySize, excludedExtensions, directoriesWithMaxFileCount.
     *                       maxRepositorySize is a long of bytes that is the maximum size of specified repository. Example: 1024.
     *                       excludedExtensions is a comma delimited string of extensions which the program will not allow files to be created with. Example: exe,bat
     *                       directoriesWithMaxFileCount is a comma delimited list of - delimited string which represent a key value pair of a directory name and the max file count that would be allowed to store inside. Example: dir1-25,dir2-50
     *                       Full config string example: maxRepositorySize=2048;excludedExtensions=jar,zip;directoriesWithMaxFileCount=exampleDir1-75,exampleDir2-100
     *                       Not all parameters have to be specified. Can be parsed as null in order to create a default config with maxRepositorySize=1073741824.
     * @throws IOException                               for IO reasons.
     * @throws BadPathException                          if path is in a bad format.
     * @throws NoFileAtPathException                     if no file exists at path.
     * @throws InvalidParametersException                if parameter(s) are invalid.
     * @throws NonExistentRepositoryException            if path starts with a non-existent repository name.
     * @throws InvalidParametersException                if config is in bad format.
     * @throws ValueInConfigCannotBeLessThanOneException if value to put in config is less than one.
     */
    void createRepository(final String repositoryName, final String configString) throws NonExistentRepositoryException, NoFileAtPathException, IOException, BadPathException, InvalidParametersException, ValueInConfigCannotBeLessThanOneException;
    /**
     * Deletes one or more files or directories at the specified path(s). Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathsString Path(s) to the files or directories to delete. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @throws IOException           for IO reasons.
     * @throws BadPathException      if path is in a bad format.
     * @throws NoFileAtPathException if no file exists at path.
     */
    void deleteFiles(String filePathsString) throws NoFileAtPathException, BadPathException, IOException;
    /**
     * Deletes one or more files or directories at the specified path(s). Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name. Paths are slash delimited and should start with a repository name. Downloading a Google Drive directory is not supported.
     *
     * @param filePathsString                    Path(s) to the files or directories to download. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @param downloadDestinationDirectoryString Path to the destination directory. Can be absolute or relative.
     * @param overwrite                          whether to overwrite existing files or directories.
     * @throws IOException                    for IO reasons.
     * @throws BadPathException               if path is in a bad format.
     * @throws NoFileAtPathException          if no file exists at path.
     * @throws MaxFileCountExceededException  if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     */
    void downloadFiles(String filePathsString, String downloadDestinationDirectoryString, boolean overwrite) throws NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, InvalidParametersException, NonExistentRepositoryException;
    /**
     * Filters the file list based on comma delimited string of filters.
     *
     * @param fileList file list to filter.
     * @param filtersString comma delimited string of metadata information to display for each file. can be: name, size, dateCreated, dateModified.
     * @return filtered list.
     * @throws BadFiltersException if filters are in a bad format.
     */
    List<T2> filterFileList(final List<T1> fileList, final String filtersString) throws BadFiltersException;
    /**
     * Gets the config from the specified repository.
     *
     * @param repositoryName name of the repository to get the config from.
     * @return DirectoryHandlerConfig from the specified repository.
     * @throws IOException                    for IO reasons.
     * @throws BadPathException               if path is in a bad format.
     * @throws NoFileAtPathException          if no file exists at path.
     * @throws MaxFileCountExceededException  if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     */
    DirectoryHandlerConfig getConfig(final String repositoryName) throws NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, NonExistentRepositoryException, InvalidParametersException;
    /**
     * Gets the size of a directory at the specified path. Paths are slash delimited and should start with a repository name.
     *
     * @param directoryPathString Path to the directory of which to get the size of. Paths are slash delimited and should start with a repository name.
     * @return long size of directory in bytes.
     * @throws IOException           for IO reasons.
     * @throws BadPathException      if path is in a bad format.
     * @throws NoFileAtPathException if no file exists at path.
     */
    long getDirectorySize(String directoryPathString) throws BadPathException, NoFileAtPathException, IOException;
    /**
     * Gets the file count of a directory at the specified path. Paths are slash delimited and should start with a repository name.
     *
     * @param directoryPathString Path to the directory of which to get the file count of. Paths are slash delimited and should start with a repository name.
     * @return int file count of directory.
     * @throws IOException           for IO reasons.
     * @throws BadPathException      if path is in a bad format.
     * @throws NoFileAtPathException if no file exists at path.
     */
    int getFileCount(String directoryPathString) throws BadPathException, NoFileAtPathException, IOException;
    /**
     * Gets the size of a file at the specified path. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathString Path to the file of which to get the size of. Paths are slash delimited and should start with a repository name.
     * @return long size of file in bytes.
     * @throws IOException                    for IO reasons.
     * @throws BadPathException               if path is in a bad format.
     * @throws NoFileAtPathException          if no file exists at path.
     * @throws MaxFileCountExceededException  if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     */
    long getFileSize(String filePathString) throws MaxFileCountExceededException, BadPathException, NoFileAtPathException, IOException, InvalidParametersException, NonExistentRepositoryException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString Path of the directory to search.
     * @param startDate           dd/MM/yyyy formatted string of the low date limit for search.
     * @param endDate             dd/MM/yyyy formatted string of the upper date limit for search.
     * @param dateCreated         whether to consider file creation date in search.
     * @param dateModified        whether to consider file modification date in search.
     * @param recursive           whether to search recursively.
     * @param includeFiles        whether to include files.
     * @param includeDirectories  whether to include directories.
     * @param sortingType         SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType           OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws ParseException             if unable to parse strings as dates.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForDateRange(final String directoryPathString, final String startDate, final String endDate, final boolean dateCreated, final boolean dateModified, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, ParseException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString            Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param searchExcludedExtensionsString comma delimited string of the extensions to exclude from search.
     * @param recursive                      whether to search recursively.
     * @param includeFiles                   whether to include files.
     * @param includeDirectories             whether to include directories.
     * @param sortingType                    SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType                      OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForExcludedExtensions(final String directoryPathString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters
     *
     * @param directoryPathString    Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param searchExtensionsString comma delimited string of the extensions to include in search.
     * @param recursive              whether to search recursively.
     * @param includeFiles           whether to include files.
     * @param includeDirectories     whether to include directories.
     * @param sortingType            SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType              OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForExtensions(final String directoryPathString, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters
     *
     * @param directoryPathString            Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param searchExtensionsString         comma delimited string of the extensions to include in search.
     * @param searchExcludedExtensionsString comma delimited string of the extensions to exclude from search.
     * @param recursive                      whether to search recursively.
     * @param includeFiles                   whether to include files.
     * @param includeDirectories             whether to include directories.
     * @param sortingType                    SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType                      OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForExtensionsAndExcludedExtensions(final String directoryPathString, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param search              search string for file name.
     * @param searchType          SearchType enum element of the search type. Can be: STARTS_WITH, ENDS_WITH, CONTAINS.
     * @param recursive           whether to search recursively.
     * @param includeFiles        whether to include files.
     * @param includeDirectories  whether to include directories.
     * @param sortingType         SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType           OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForSearchName(final String directoryPathString, final String search, final SearchType searchType, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString            Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param search                         search string for file name.
     * @param searchExcludedExtensionsString comma delimited string of the extensions to exclude from search.
     * @param recursive                      whether to search recursively.
     * @param includeFiles                   whether to include files.
     * @param includeDirectories             whether to include directories.
     * @param sortingType                    SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType                      OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForSearchNameAndExcludedExtensions(final String directoryPathString, final String search, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString    Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param search                 search string for file name.
     * @param searchExtensionsString comma delimited string of the extensions to include in search.
     * @param recursive              whether to search recursively.
     * @param includeFiles           whether to include files.
     * @param includeDirectories     whether to include directories.
     * @param sortingType            SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType              OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForSearchNameAndExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString            Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param search                         search string for file name.
     * @param searchExtensionsString         comma delimited string of the extensions to include in search.
     * @param searchExcludedExtensionsString comma delimited string of the extensions to exclude from search.
     * @param recursive                      whether to search recursively.
     * @param includeFiles                   whether to include files.
     * @param includeDirectories             whether to include directories.
     * @param sortingType                    SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType                      OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesForSearchNameAndExtensionsAndExcludedExtensions(final String directoryPathString, final String search, final String searchExtensionsString, final String searchExcludedExtensionsString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString Path of directory to search. Paths are slash delimited and should start with a repository name.
     * @param recursive           Whether to search recursively.
     * @param includeFiles        Whether to include files.
     * @param includeDirectories  Whether to include directories.
     * @param sortingType         SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE
     * @param orderType           OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesInDirectory(final String directoryPathString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, IOException, BadPathException, NoFileAtPathException;
    /**
     * Gets the file list for specified search parameters.
     *
     * @param directoryPathString Path of the directory to search. Paths are slash delimited and should start with a repository name.
     * @param searchListString    comma delimited string of the file names to search for.
     * @param recursive           whether to search recursively.
     * @param includeFiles        whether to include files.
     * @param includeDirectories  whether to include directories.
     * @param sortingType         SortingType enum element of the method of sorting to apply. Possible sorting types (SortingType): NONE, NAME, DATE_CREATED, DATE_MODIFIED, SIZE.
     * @param orderType           OrderType enum element of the order type. Can be ASCENDING, DESCENDING.
     * @return List of files in search result.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     */
    List<T1> getFilesWithNames(final String directoryPathString, final String searchListString, final boolean recursive, final boolean includeFiles, final boolean includeDirectories, final SortingType sortingType, final OrderType orderType) throws InvalidParametersException, NoFileAtPathException, IOException, BadPathException;
    /**
     * Move one or more files at the path(s) specified to the destination directory path with the option of overwriting. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathsString                Path(s) of the file to move. Multiple paths are delimited with -more-. Paths are slash delimited and should start with a repository name.
     * @param moveDestinationDirectoryString Path to the destination directory. Paths are slash delimited and should start with a repository name.
     * @param overwrite                      whether to overwrite existing files or directories.
     * @throws IOException                        for IO reasons.
     * @throws BadPathException                   if path is in a bad format.
     * @throws NoFileAtPathException              if no file exists at path.
     * @throws MaxFileCountExceededException      if max file count for directory is exceeded.
     * @throws InvalidParametersException         if parameter(s) are invalid.
     * @throws NonExistentRepositoryException     if path starts with a non-existent repository name.
     * @throws MaxRepositorySizeExceededException if max repository size is exceeded.
     */
    void moveFiles(String filePathsString, String moveDestinationDirectoryString, boolean overwrite) throws NoFileAtPathException, IOException, BadPathException, InvalidParametersException, NonExistentRepositoryException, MaxFileCountExceededException, MaxRepositorySizeExceededException;
    /**
     * Renames a file or directory at the path specified. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathString Path of the file to rename. Paths are slash delimited and should start with a repository name.
     * @param newFileName    new file name.
     * @throws NoFileAtPathException          for IO reasons.
     * @throws IOException                    if path is in a bad format.
     * @throws MaxFileCountExceededException  if no file exists at path.
     * @throws BadPathException               if max file count for directory is exceeded.
     * @throws InvalidParametersException     if parameter(s) are invalid.
     * @throws NonExistentRepositoryException if path starts with a non-existent repository name.
     * @throws FileExtensionException         if config has invalid parameters.
     *                                        NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, InvalidParameterException, NonExistentRepositoryException, FileExtensionException;
     */
    void renameFile(String filePathString, final String newFileName) throws NoFileAtPathException, IOException, MaxFileCountExceededException, BadPathException, InvalidParametersException, NonExistentRepositoryException, FileExtensionException;
    /**
     * Sets the root directory of the current session (can contain multiple repositories).
     *
     * @param rootPathString path to the root directory.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     * @throws GeneralSecurityException   if Google Drive fails to authenticate.
     */
    void setWorkingDirectory(final String rootPathString) throws NoFileAtPathException, GeneralSecurityException, InvalidParametersException, IOException, BadPathException;
    /**
     * Updates the config of specified repository with the specified config.
     *
     * @param repositoryName   name of the repository whose config to update.
     * @param configString     String representation of custom config. Parameters in config string are delimited by ;. Parameters are split into keys and values by =. Possible parameter keys: maxRepositorySize, excludedExtensions, directoriesWithMaxFileCount.
     *                         maxRepositorySize is a long of bytes that is the maximum size of specified repository. Example: 1024.
     *                         excludedExtensions is a comma delimited string of extensions which the program will not allow files to be created with. Example: exe,bat
     *                         directoriesWithMaxFileCount is a comma delimited list of - delimited string which represent a key value pair of a directory name and the max file count that would be allowed to store inside. Example: dir1-25,dir2-50
     *                         Full config string example: maxRepositorySize=2048;excludedExtensions=jar,zip;directoriesWithMaxFileCount=exampleDir1-75,exampleDir2-100
     *                         Not all parameters have to be specified. Can be parsed as null in order to create a default config with maxRepositorySize=1073741824.
     * @param configUpdateType ConfigUpdateTypes enum element of the method of config updating to apply. Possible sorting types (SortingType): REPLACE (replaces existing config values), ADD (adds to existing config values).
     * @throws IOException                               for IO reasons.
     * @throws BadPathException                          if path is in a bad format.
     * @throws NoFileAtPathException                     if no file exists at path.
     * @throws MaxFileCountExceededException             if max file count for directory is exceeded.
     * @throws InvalidParametersException                if parameter(s) are invalid.
     * @throws NonExistentRepositoryException            if path starts with a non-existent repository name.
     * @throws ValueInConfigCannotBeLessThanOneException if value specified in config is less than one.
     */
    void updateConfig(final String repositoryName, final String configString, final ConfigUpdateType configUpdateType) throws NoFileAtPathException, NonExistentRepositoryException, IOException, MaxFileCountExceededException, BadPathException, InvalidParametersException, ValueInConfigCannotBeLessThanOneException;
    /**
     * Writes the specified text to specified file path. Paths are slash delimited and should start with a repository name.
     *
     * @param filePathString Path to the file. Paths are slash delimited and should start with a repository name.
     * @param textToWrite    text to write.
     * @throws IOException                        for IO reasons.
     * @throws BadPathException                   if path is in a bad format.
     * @throws NoFileAtPathException              if no file exists at path.
     * @throws MaxFileCountExceededException      if max file count for directory is exceeded.
     * @throws InvalidParametersException         if parameter(s) are invalid.
     * @throws NonExistentRepositoryException     if path starts with a non-existent repository name.
     * @throws FileExtensionException             if file has an extension that is forbidden in the config file.
     * @throws MaxRepositorySizeExceededException if max repository size is exceeded.
     */
    void writeToFile(String filePathString, final String textToWrite) throws BadPathException, NoFileAtPathException, IOException, MaxFileCountExceededException, InvalidParametersException, NonExistentRepositoryException, FileExtensionException, MaxRepositorySizeExceededException;
}