package co.casterlabs.commons.io.sink;

/**
 * Be sure to catch this exception if you use
 * {@link InsertionStrategy#THROW_ON_OVERRUN} or
 * {@link ExtractionStrategy#THROW_ON_UNDERRUN}.
 */
public class SinkBuffereringError extends Error {

}
