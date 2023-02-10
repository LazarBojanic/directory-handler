package rs.raf.exception;

/**
 * Directory Handler exceptions.
 */
public class DirectoryHandlerExceptions {
    /**
     * Occurs if max file count for directory is exceeded.
     */
    public static class MaxFileCountExceededException extends Exception {
        /**
         * MaxFileCountExceededException constructor.
         *
         * @param filePathString File path.
         */
        public MaxFileCountExceededException(final String filePathString) {
            super(String.format("Max file count for directory %s exceeded!", filePathString));
        }
    }
    /**
     * Occurs if max repository size is exceeded.
     */
    public static class MaxRepositorySizeExceededException extends Exception {
        /**
         * MaxRepositorySizeExceededException constructor.
         *
         * @param repositoryName Repository name.
         */
        public MaxRepositorySizeExceededException(final String repositoryName) {
            super(String.format("Max size for repository %s exceeded!", repositoryName));
        }
    }
    /**
     * Occurs if a file is attempted to be created with an excluded extension specified in the config.
     */
    public static class FileExtensionException extends Exception {
        /**
         * FileExtensionException constructor.
         *
         * @param fileName File name.
         */
        public FileExtensionException(final String fileName) {
            super(String.format("Cannot create file with the extension %s due to its exclusion in the configuration file!", fileName.substring(fileName.lastIndexOf("."))));
        }
    }
    /**
     * Occurs if path starts with a non-existent repository name.
     */
    public static class NonExistentRepositoryException extends Exception {
        /**
         * NonExistentRepositoryException constructor.
         *
         * @param repositoryName Repository name.
         */
        public NonExistentRepositoryException(final String repositoryName) {
            super(String.format("No repository with name %s!", repositoryName));
        }
    }
    /**
     * Occurs if path is in bad format.
     */
    public static class BadPathException extends Exception {
        /**
         * BadPathException constructor.
         *
         * @param filePathString File path.
         */
        public BadPathException(final String filePathString) {
            super(String.format("%s is not a valid path!", filePathString));
        }
    }
    /**
     * Occurs if no file exists at path.
     */
    public static class NoFileAtPathException extends Exception {
        /**
         * NoFileAtPathException constructor.
         *
         * @param filePathString File path.
         */
        public NoFileAtPathException(final String filePathString) {
            super(String.format("Not file at path %s!", filePathString));
        }
    }
    /**
     * Occurs if parameter is in bad format.
     */
    public static class InvalidParametersException extends Exception {
        /**
         * InvalidParameterException constructor.
         *
         * @param parameters Parameters.
         */
        public InvalidParametersException(final String parameters) {
            super(String.format("Invalid parameter(s) %s!", parameters));
        }
    }
    /**
     * Occurs if value specified in config is less than one.
     */
    public static class ValueInConfigCannotBeLessThanOneException extends Exception {
        /**
         * ValueInConfigCannotBeLessThanOneException constructor.
         *
         * @param value value.
         */
        public ValueInConfigCannotBeLessThanOneException(final String value) {
            super(String.format("Value %s cannot be less than 1!", value));
        }
    }
    /**
     * Occurs if value specified in config is less than one.
     */
    public static class BadFiltersException extends Exception {
        /**
         * BadFiltersException constructor.
         *
         * @param filters filters.
         */
        public BadFiltersException(final String filters) {
            super(String.format("Bad filters %s!", filters));
        }
    }
}