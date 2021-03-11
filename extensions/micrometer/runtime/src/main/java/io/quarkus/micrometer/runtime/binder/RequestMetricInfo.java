package io.quarkus.micrometer.runtime.binder;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Timer;

public class RequestMetricInfo {
    static final Logger log = Logger.getLogger(RequestMetricInfo.class);

    public static final String HTTP_REQUEST_PATH = "HTTP_REQUEST_PATH";
    public static final String HTTP_REQUEST_PATH_MATCHED = "HTTP_REQUEST_MATCHED_PATH";

    /** Do not measure requests until/unless a uri path is set */
    protected final boolean measure;

    /** URI path used as a tag value for non-error requests */
    protected final String path;

    /** True IFF the path was revised by a matcher expression */
    protected final boolean pathMatched;

    /** Store the sample used to measure the request */
    protected Timer.Sample sample;

    public RequestMetricInfo setSample(Timer.Sample sample) {
        this.sample = sample;
        return this;
    }

    public Timer.Sample getSample() {
        return sample;
    }

    /**
     * Normalize and filter request path against match patterns
     *
     * @param uri Uri for request
     * @param ignorePatterns
     * @param matchPatterns
     * @return final uri for tag, or null to skip measurement
     */
    protected String getNormalizedUriPath(Map<Pattern, String> matchPatterns, List<Pattern> ignorePatterns, String uri) {
        System.out.println("HERE, NOW WHAT: uri =" + uri);
        // Normalize path
        String path = normalizePath(uri);
        if (path.length() > 1) {
            String origPath = path;
            // Look for configured matches, then inferred templates
            path = applyMatchPatterns(origPath, matchPatterns);
            if (path.equals(origPath)) {
                path = normalizePath(applyTemplateMatching(origPath));
            }
        }
        System.out.println("DONE .............. =" + path);
        return filterIgnored(path, ignorePatterns);
    }

    /** Subclassess should override with appropriate mechanisms for finding templated urls */
    protected String applyTemplateMatching(String path) {
        return path;
    }

    public boolean isMeasure() {
        return measure;
    }

    public boolean isPathMatched() {
        return pathMatched;
    }

    private static String extractPath(String uri) {
        if (uri.isEmpty()) {
            return uri;
        }
        int i;
        if (uri.charAt(0) == '/') {
            i = 0;
        } else {
            i = uri.indexOf("://");
            if (i == -1) {
                i = 0;
            } else {
                i = uri.indexOf('/', i + 3);
                if (i == -1) {
                    // contains no /
                    return "/";
                }
            }
        }

        int queryStart = uri.indexOf('?', i);
        if (queryStart == -1) {
            queryStart = uri.length();
        }
        return uri.substring(i, queryStart);
    }

    public String getHttpRequestPath() {
        return path;
    }

    protected static String normalizePath(String uri) {
        if (uri == null || uri.isEmpty() || ROOT.equals(uri)) {
            return ROOT;
        }
        // Label value consistency: result should begin with a '/' and should not end with one
        String workingPath = MULTIPLE_SLASH_PATTERN.matcher('/' + uri).replaceAll("/");
        workingPath = TRAILING_SLASH_PATTERN.matcher(workingPath).replaceAll("");
        if (workingPath.isEmpty()) {
            return ROOT;
        }
        return workingPath;
    }
}
