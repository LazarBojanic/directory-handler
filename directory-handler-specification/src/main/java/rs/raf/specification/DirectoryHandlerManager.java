package rs.raf.specification;

import rs.raf.exception.DirectoryHandlerExceptions.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Helper class for referencing an implementation.
 */
public class DirectoryHandlerManager {
    /**
     * Directory handler.
     */
    private static IDirectoryHandlerSpecification directoryHandler;
    /**
     * Registers directory handler.
     *
     * @param iDirectoryHandlerSpecification specification.
     */
    public static void registerDirectoryHandler(final IDirectoryHandlerSpecification iDirectoryHandlerSpecification) {
        directoryHandler = iDirectoryHandlerSpecification;
    }
    /**
     * Gets directory handler.
     *
     * @param rootPathString path to the root directory.
     * @return IDirectoryHandlerSpecification specification.
     * @throws IOException                for IO reasons.
     * @throws BadPathException           if path is in a bad format.
     * @throws NoFileAtPathException      if no file exists at path.
     * @throws InvalidParametersException if parameter(s) are invalid.
     * @throws GeneralSecurityException   if Google Drive fails to authorize.
     */
    public static IDirectoryHandlerSpecification getDirectoryHandler(final String rootPathString) throws NoFileAtPathException, GeneralSecurityException, InvalidParametersException, IOException, BadPathException {
        directoryHandler.setWorkingDirectory(rootPathString);
        return directoryHandler;
    }
}