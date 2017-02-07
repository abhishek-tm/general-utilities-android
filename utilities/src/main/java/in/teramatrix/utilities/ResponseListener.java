package in.teramatrix.utilities;

/**
 * An interface to publish results in the caller classes. By implementing this, end user of the module can access final results.
 *
 * @author Mohsin Khan
 * @date 3/21/2016
 */
public interface ResponseListener {
    /**
     * This method will be invoked on Http Request failure
     * @param e Cause of failure
     */
    void onRequestFailure(Exception e);
}
