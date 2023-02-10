package rs.raf.main.exception;

public class Exceptions {
    /**
     * Occurs if command is invalid.
     */
    public static class InvalidCommandException extends Exception {
        /**
         * InvalidCommandException constructor.
         *
         * @param command command.
         */
        public InvalidCommandException(final String command) {
            super(String.format("Invalid command %s!", command));
        }
    }
}