package com.pding.paymentservice.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Component
public class TokenSigner {

    @Value("${bunny.storage.pullZone}")
    private String bunnyStoragePullZone;

    @Value("${bunny.storage.access.key}")
    private String bunnyStorageAccessKey;

    @Value("${bunny.stream.authKey}")
    private String bunnyStreamAccessKey;

    @Value("${bunny.stream.preview.url.host}")
    private String bunnyPreviewUrlHost;

    @Value("${bunny.storage.folder}")
    private String storageFolder;


    public String signPlaybackUrl(String libraryId, String videoId, Integer _expireAfterHours) {
        if (libraryId == null || videoId == null) return null;

        Integer expireAfterHours = Objects.requireNonNullElse(_expireAfterHours, 5);
        String expires = System.currentTimeMillis() / 1000L + Long.parseLong(((expireAfterHours * 3600) + "")) + "";
        String token = emberUrlToken(videoId, expires);
        return getPlaybackUrl(libraryId, videoId) + "?token=" + token + "&expires=" + expires;
    }

    public String getPreviewUrl(String videoId) {
        return "https://" + bunnyPreviewUrlHost + ".b-cdn.net/" + videoId + "/preview.webp";
    }

    public String signThumbnailUrl(String path, Integer expireAfterHours) {
        if (path == null) return null;
        try {
            return signUrl(getUrl(path), (expireAfterHours * 3600) + "", null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String signImageUrl(String path, Integer expireAfterHours) {
        if (path == null) return null;
        try {
            return signUrl(getUrl(path), (expireAfterHours * 3600) + "", null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private String addCountries(String url, String a, String b) throws Exception {
        String tempUrl = url;
        if (!a.isEmpty()) {
            URL temp = new URL(tempUrl);
            tempUrl += ((temp.getQuery() == null) ? "?" : "&") + "token_countries=" + a;
        }
        if (!b.isEmpty()) {
            URL tempTwo = new URL(tempUrl);
            tempUrl += ((tempTwo.getQuery() == null) ? "?" : "&") + "token_countries_blocked=" + b;
        }
        return tempUrl;
    }

    public <K extends Comparable, V> Map<K, V> sortByKeys(Map<K, V> map) {
        return new TreeMap<>(map);
    }

    private static String encodeValue(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
    }

    private String getUrl(String path) {
        return "https://" + bunnyStoragePullZone + ".b-cdn.net/" + storageFolder + path;
    }

    private String getPlaybackUrl(String libraryId, String videoId) {
        return "https://iframe.mediadelivery.net/embed/" + libraryId + "/" + videoId;
    }

    private String emberUrlToken(String videoId, String expirationHours) {
        if (videoId == null) return null;
        String data = bunnyStreamAccessKey + videoId + expirationHours;
        try {
            return calculateSHA256(data);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String signUrl(String urlIn, String expirationTimeIn, String userIpIn,
                           Boolean isDirectoryIn, String pathAllowedIn, String countriesAllowedIn, String countriesBlockedIn)
            throws Exception {
        String expires = "", parameterData = "", parameterDataUrl = "", expirationTime = "3600", signaturePath = "",
                countriesAllowed, countriesBlocked, hashableBase = "", userIp = "", token = "";
        boolean isDirectory = false;
        if (isDirectoryIn != null) {
            isDirectory = isDirectoryIn;
        }
        if (countriesAllowedIn == null) {
            countriesAllowed = "";
        } else {
            countriesAllowed = countriesAllowedIn;
        }
        if (countriesBlockedIn == null) {
            countriesBlocked = "";
        } else {
            countriesBlocked = countriesBlockedIn;
        }
        String url = addCountries(urlIn, countriesAllowed, countriesBlocked);
        URL temp = new URL(url);
        if (expirationTimeIn != null) {
            expirationTime = expirationTimeIn;
        }
        if (userIpIn != null) {
            userIp = userIpIn;
        }
        expires = System.currentTimeMillis() / 1000L + Long.parseLong(expirationTime) + "";
        List<NameValuePair> parametersList = URLEncodedUtils.parse(new URI(url), Charset.forName("ASCII"));
        Map<String, String> parametersMap = new HashMap<String, String>();
        for (NameValuePair param : parametersList)
            parametersMap.put(param.getName(), param.getValue());
        if (pathAllowedIn != null) {
            signaturePath = pathAllowedIn;
            parametersMap.put("token_path", signaturePath);
        } else {
            signaturePath = temp.getPath();
        }
        parametersMap = this.sortByKeys(parametersMap);
        if (parametersMap.size() > 0) {
            for (Map.Entry<String, String> param : parametersMap.entrySet()) {
                if (parameterData.length() > 0)
                    parameterData += "&";
                parameterData += param.getKey() + "=" + param.getValue();
                parameterDataUrl += "&" + param.getKey() + "=" + encodeValue(param.getValue());

            }
        }
        hashableBase = bunnyStorageAccessKey + signaturePath + expires + parameterData + ((userIp.length() > 0) ? userIp : "");
        token = new String(Base64.encodeBase64(new DigestUtils("SHA-256").digest(hashableBase.getBytes())));
        token = token.replace("\n", "").replace("+", "-").replace("/", "_").replace("=", "");
        if (isDirectory) {
            return temp.getProtocol() + "://" + temp.getHost() + "/bcdn_token=" + token + parameterDataUrl + "&expires="
                    + expires + temp.getPath();
        } else {
            return temp.getProtocol() + "://" + temp.getHost() + temp.getPath() + "?token=" + token + parameterDataUrl
                    + "&expires=" + expires;
        }
    }

    public TokenSigner() {
        // Nothing
    }

    private static String calculateSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // Convert byte array to hexadecimal representation
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public String composeImagesPath(String fileName) {
        if (fileName == null) return null;
        return "/images/" + fileName;
    }

    public String generateUnsignedImageUrl(String path) {
        if (path == null) return null;
        return getUrl(path);
    }

}
