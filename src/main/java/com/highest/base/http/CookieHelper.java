package com.highest.base.http;

import com.highest.utils.DateUtils;
import com.highest.utils.HFStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.*;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CookieHelper {

    public static List<Cookie> parseCookieHeaders(String url, List<String> cookieHeaders) {
        try {
            CookieSpec cookieSpec = new BrowserCompatSpec();
            URI uri = new URI(url);

            List<Cookie> lstCookies = new ArrayList<>();

            int port = (uri.getPort() < 0) ? 80 : uri.getPort();
            boolean secure = "https".equals(uri.getScheme());
            CookieOrigin origin = new CookieOrigin(uri.getHost(), port,
                    uri.getPath(), secure);

            for (String cookieHeader : cookieHeaders) {
                BasicHeader header = new BasicHeader(SM.SET_COOKIE, cookieHeader);
                try {
                    lstCookies.addAll(cookieSpec.parse(header, origin));
                } catch (MalformedCookieException ex) {
                    ex.printStackTrace();
                }
            }
            return lstCookies;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeCookie(Cookie cookie) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(cookie.getName());
            oos.writeObject(cookie.getValue());
            oos.writeObject(cookie.getComment());
            oos.writeObject(cookie.getDomain());
            oos.writeObject(cookie.getExpiryDate());
            oos.writeObject(cookie.getPath());
            oos.writeInt(cookie.getVersion());
            oos.writeBoolean(cookie.isSecure());

            String cookieSerialize = HFStringUtils.byteArrayToHexString(os.toByteArray());
            oos.close();

            return cookieSerialize;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Cookie decodeCookie(String cookieBytesHex) {
        try {
            byte[] bytes = HFStringUtils.hexStringToByteArray(cookieBytesHex);
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(is);

            String name = (String) ois.readObject();
            String value = (String) ois.readObject();

            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setComment((String) ois.readObject());
            cookie.setDomain((String) ois.readObject());
            cookie.setExpiryDate((Date) ois.readObject());
            cookie.setPath((String) ois.readObject());
            cookie.setVersion(ois.readInt());
            cookie.setSecure(ois.readBoolean());

            ois.close();
            return cookie;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String toString(Cookie cookie) {
        try {
            String cookieHeader = "";

            cookieHeader += cookie.getName() + "=" + cookie.getValue() + ";";
            cookieHeader += "Domain=" + cookie.getDomain() + ";";
            cookieHeader += "Path=" + cookie.getPath() + ";";
            if (cookie.getExpiryDate() != null) {
                cookieHeader += "Expires=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cookie.getExpiryDate()) + ";";
            }
            if (cookie.isSecure()) {
                cookieHeader += "Secure;";
            }
            return cookieHeader;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static Cookie parseString(String rawCookie) {
        try {
            String[] rawCookieParams = rawCookie.split(";");

            String[] tokens = new String[] {
                    "secure",
                    "expires",
                    "max-age",
                    "domain",
                    "path",
                    "comment"
            };
            String lowerCaseRawCookie = rawCookie.toLowerCase();
            int valuePos = -1;
            for (int i = 0; i < tokens.length; i++) {
                int pos = lowerCaseRawCookie.indexOf(tokens[i] + "=");
                if (pos < 0)
                    continue;
                if (valuePos < 0)
                    valuePos = pos;
                else
                    valuePos = Math.min(pos, valuePos);
            }

            String rawCookieNameAndValue = valuePos < 0 ? rawCookie : rawCookie.substring(0, valuePos);
            int pos = rawCookieNameAndValue.indexOf('=');
            if (pos < 0) {
                return null;
            }

            String cookieName = rawCookieNameAndValue.substring(0, pos).trim();
            String cookieValue = StringUtils.stripEnd(rawCookieNameAndValue.substring(pos + 1).trim(), ";");

            BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);
            for (int i = 1; i < rawCookieParams.length; i++) {
                String params = rawCookieParams[i].trim();
                pos = params.indexOf('=');
                if (pos < 0) {
                    continue;
                }
                String rawCookieParamNameAndValue[] = new String[] {
                        params.substring(0, pos),
                        params.substring(pos + 1)
                };

                String paramName = rawCookieParamNameAndValue[0].trim();

                if (paramName.equalsIgnoreCase("secure")) {
                    cookie.setSecure(true);
                } else {
                    if (rawCookieParamNameAndValue.length != 2) {
                        return null;
                    }

                    String paramValue = rawCookieParamNameAndValue[1].trim();

                    if (paramName.equalsIgnoreCase("expires")) {
                        Date expiryDate = DateUtils.parseDateStr(paramValue);
                        if (expiryDate != null) {
                            if (expiryDate.getTime() <= 0) {
                                expiryDate = new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000);
                            }
                            cookie.setExpiryDate(expiryDate);
                        }
                    } else if (paramName.equalsIgnoreCase("max-age")) {
                        long maxAge = Long.parseLong(paramValue);
                        Date expiryDate = new Date(System.currentTimeMillis() + maxAge);
                        cookie.setExpiryDate(expiryDate);
                    } else if (paramName.equalsIgnoreCase("domain")) {
                        cookie.setDomain(paramValue);
                    } else if (paramName.equalsIgnoreCase("path")) {
                        cookie.setPath(paramValue);
                    } else if (paramName.equalsIgnoreCase("comment")) {
                        cookie.setPath(paramValue);
                    }
                }
            }

            return cookie;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
