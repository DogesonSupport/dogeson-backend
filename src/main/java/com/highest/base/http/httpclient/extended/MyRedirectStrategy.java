package com.highest.base.http.httpclient.extended;

import com.highest.base.http.RedirectMode;
import com.highest.base.http.httpclient.entity.RedirectFetchException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class MyRedirectStrategy extends DefaultRedirectStrategy {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RedirectMode _redirectMode;

    // Keys used to access data in the Http execution context.
    private static final String PERM_REDIRECT_CONTEXT_KEY = "perm-redirect";
    private static final String REDIRECT_COUNT_CONTEXT_KEY = "redirect-count";

    public MyRedirectStrategy(RedirectMode redirectMode) {
        super();

        _redirectMode = redirectMode;
    }

    @Override
    public URI getLocationURI(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
        URI result = super.getLocationURI(request, response, context);

        // HACK - some sites return a redirect with an explicit port number
        // that's the same as
        // the default port (e.g. 80 for http), and then when you use this
        // to make the next
        // request, the presence of the port in the domain triggers another
        // redirect, so you
        // fail with a circular redirect error. Avoid that by converting the
        // port number to
        // -1 in that case.
        //
        // Detailed scenrio:
        // http://www.test.com/MyPage ->
        // http://www.test.com:80/MyRedirectedPage ->
        // http://www.test.com/MyRedirectedPage
        // We can save bandwidth:
        if (result.getScheme().equalsIgnoreCase("http") && (result.getPort() == 80)) {
            try {
                result = new URI(result.getScheme(), result.getUserInfo(), result.getHost(), -1, result.getPath(), result.getQuery(), result.getFragment());
            } catch (URISyntaxException e) {
                logger.warn("Unexpected exception removing port from URI", e);
            }
        }

        // Keep track of the number of redirects.
        Integer count = (Integer) context.getAttribute(REDIRECT_COUNT_CONTEXT_KEY);
        if (count == null) {
            count = new Integer(0);
        }

        context.setAttribute(REDIRECT_COUNT_CONTEXT_KEY, count + 1);

        // Record the last permanent redirect
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
            context.setAttribute(PERM_REDIRECT_CONTEXT_KEY, result);
        }

        RedirectFetchException.RedirectExceptionReason reason = null;

        if (_redirectMode == RedirectMode.FOLLOW_NONE) {
            switch (statusCode) {
                case HttpStatus.SC_MOVED_TEMPORARILY:
                    reason = RedirectFetchException.RedirectExceptionReason.TEMP_REDIRECT_DISALLOWED;
                    break;
                case HttpStatus.SC_MOVED_PERMANENTLY:
                    reason = RedirectFetchException.RedirectExceptionReason.PERM_REDIRECT_DISALLOWED;
                    break;
                case HttpStatus.SC_TEMPORARY_REDIRECT:
                    reason = RedirectFetchException.RedirectExceptionReason.TEMP_REDIRECT_DISALLOWED;
                    break;
                case HttpStatus.SC_SEE_OTHER:
                    reason = RedirectFetchException.RedirectExceptionReason.SEE_OTHER_DISALLOWED;
                    break;
                default:
            }
        }

        if (_redirectMode == RedirectMode.FOLLOW_TEMP) {
            switch (statusCode) {
                case HttpStatus.SC_MOVED_PERMANENTLY:
                    reason = RedirectFetchException.RedirectExceptionReason.PERM_REDIRECT_DISALLOWED;
                    break;
                case HttpStatus.SC_SEE_OTHER:
                    reason = RedirectFetchException.RedirectExceptionReason.SEE_OTHER_DISALLOWED;
                    break;
                default:
            }
        }

        if (reason != null)
            throw new MyRedirectException("RedirectMode disallowed redirect: " + _redirectMode, result, reason);

        return result;
    }

    private static class MyRedirectException extends RedirectException {

        private URI _uri;
        private RedirectFetchException.RedirectExceptionReason _reason;

        public MyRedirectException(String message, URI uri, RedirectFetchException.RedirectExceptionReason reason) {
            super(message);
            _uri = uri;
            _reason = reason;
        }

        public URI getUri() {
            return _uri;
        }

        public RedirectFetchException.RedirectExceptionReason getReason() {
            return _reason;
        }
    }
}
