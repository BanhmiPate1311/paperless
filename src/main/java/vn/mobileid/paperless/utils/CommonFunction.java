/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.paperless.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import vn.mobileid.paperless.object.CertificateJson;
import vn.mobileid.paperless.object.CertificateObject;
import vn.mobileid.paperless.object.PostbackJson;
import vn.mobileid.paperless.object.SignerInfoJson;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

import static vn.mobileid.paperless.API.Crypto.*;

/**
 *
 * @author PHY
 */
public class CommonFunction {

    final public static String HASH_SHA256 = "SHA-256";
    final public static String HASH_SHA1 = "SHA-1";
    final public static OkHttpClient httpClient = new OkHttpClient();

    public static String generateNumberDays() {
        String result;
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        result = String.valueOf(year).substring(2) + String.valueOf(month) + String.valueOf(day)
                + String.valueOf(hour) + String.valueOf(minute) + String.valueOf(second)
                + String.valueOf(millis);
        return result;
    }

    public static byte[] hashData(byte[] data, String algorithm) {
        byte[] result = null;
        try {
            if (algorithm.compareToIgnoreCase(HASH_MD5) == 0) {
                algorithm = HASH_MD5;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA1) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA1_) == 0) {
                algorithm = HASH_SHA1;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA256) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA256_) == 0) {
                algorithm = HASH_SHA256;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA384) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA384_) == 0) {
                algorithm = HASH_SHA384;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA512) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA512_) == 0) {
                algorithm = HASH_SHA512;
            } else {
                algorithm = HASH_SHA256;
            }
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            result = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // log.error(e.getMessage());
        }
        return result;
    }

    // get upload token
    // String uuid = UUID.randomUUID().toString();
    // String uploadToken = GatewayUtils.getCryptoHash(uuid,
    // GatewayUtils.HASH_SHA1);
    public static String getCryptoHash(String input) {
        try {
            String algorithm = HASH_SHA1;
            if (algorithm.compareToIgnoreCase(HASH_MD5) == 0) {
                algorithm = HASH_MD5;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA1) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA1_) == 0) {
                algorithm = HASH_SHA1;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA256) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA256_) == 0) {
                algorithm = HASH_SHA256;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA384) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA384_) == 0) {
                algorithm = HASH_SHA384;
            } else if (algorithm.compareToIgnoreCase(HASH_SHA512) == 0
                    || algorithm.compareToIgnoreCase(HASH_SHA512_) == 0) {
                algorithm = HASH_SHA512;
            } else {
                algorithm = HASH_SHA256;
            }
            // MessageDigest classes Static getInstance method is called with MD5 hashing
            MessageDigest msgDigest = MessageDigest.getInstance(algorithm);

            // digest() method is called to calculate message digest of the input
            // digest() return array of byte.
            byte[] inputDigest = msgDigest.digest(input.getBytes());

            // Convert byte array into signum representation
            // BigInteger class is used, to convert the resultant byte array into its signum
            // representation
            BigInteger inputDigestBigInt = new BigInteger(1, inputDigest);

            // Convert the input digest into hex value
            String hashtext = inputDigestBigInt.toString(16);

            // Add preceding 0's to pad the hashtext to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } // Catch block to handle the scenarios when an unsupported message digest
        // algorithm is provided.
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertInputStreamToBase64(InputStream input) {
        try {
            byte[] byteArray = IOUtils.toByteArray(input);
            String sBase = Base64.getEncoder().encodeToString(byteArray);
            return sBase;
        } catch (Exception e) {
            return "";
        }
    }

    public static String JsonCertificateObject(String sCertificate, String sType, String sCode, String signingTime,
            String signingOption,
            String sAction, String sToken, String sSigner, String sStatus, String sFile, String sFileSigest,
            String sSignature_id, String sCountryCode) {
        String sJson = "";
        try {
            Object[] info = new Object[3];
            String[] time = new String[2];
            int[] intRes = new int[1];
            CertificateObject certObj = null;
            SignerInfoJson signerJson = new SignerInfoJson();
            VoidCertificateComponents(sCertificate, info, time, intRes);
            if (intRes[0] == 0) {
                certObj = new CertificateObject();
                certObj.subject = info[0].toString();
                certObj.issuer = info[1].toString();
                certObj.valid_from = time[0];
                certObj.valid_to = time[1];
                certObj.value = sCertificate;
            }
            // signerJson.type = sType;
            signerJson.code = sCode;
            signerJson.certificate = certObj;
            signerJson.signing_time = signingTime;
            signerJson.signing_option = signingOption;
            signerJson.country_code = sCountryCode;
            CertificateJson certJson = new CertificateJson();
            certJson.action = sAction;
            certJson.token = sToken;
            certJson.signer = sSigner;
            certJson.signer_info = signerJson;
            certJson.status = sStatus;
            certJson.file = sFile;
            certJson.file_digest = sFileSigest;
            certJson.valid_to = time[1];
            certJson.signature_id = sSignature_id;
            ObjectMapper oMapperParse = new ObjectMapper();
            sJson = oMapperParse.writeValueAsString(certJson);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sJson;
    }

    public static String PostBackJsonCertificateObject(String url, String sCertificate, String sType, String sCode,
            String signingTime, String signingOption,
            String sAction, String sToken, String sSigner, String sStatus, String sFile, String sFileSigest,
            String sSignature_id, String sCountryCode) {
        String sResult = "0";
        try {
            Object[] info = new Object[3];
            String[] time = new String[2];
            int[] intRes = new int[1];
            CertificateObject certObj = null;
            SignerInfoJson signerJson = new SignerInfoJson();
            VoidCertificateComponents(sCertificate, info, time, intRes);
            if (intRes[0] == 0) {
                certObj = new CertificateObject();
                certObj.subject = info[0].toString();
                certObj.issuer = info[1].toString();
                certObj.valid_from = time[0];
                certObj.valid_to = time[1];
                certObj.value = sCertificate;
            }
            // signerJson.type = sType;
            signerJson.code = sCode;
            signerJson.country_code = sCountryCode;
            // signerJson.certificate = certObj;
            signerJson.signing_time = signingTime;
            signerJson.signing_option = signingOption;
            CertificateJson certJson = new CertificateJson();
            certJson.action = sAction;
            certJson.token = sToken;
            certJson.signer = sSigner;
            certJson.signer_info = signerJson;
            certJson.status = sStatus;
            certJson.file = sFile;
            certJson.file_digest = sFileSigest;
            certJson.valid_to = time[1];
            certJson.signature_id = sSignature_id;
            ObjectMapper oMapperParse = new ObjectMapper();
            String sJson = oMapperParse.writeValueAsString(certJson);
            System.err.println("UrlPostBack: " + url);
            System.err.println("Requet: " + sJson);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), sJson);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = httpClient.newCall(request).execute();
            System.out.println("requestbody PostBackJsonCertificateObject " + response.toString());
            // HttpPost request = new HttpPost(url);
            // StringEntity params = new StringEntity(sJson);
            // request.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            // request.setEntity(params);
            // HttpResponse response = httpClient.execute(request);
            // System.out.println("requestbody PostBackJsonCertificateObject " + response.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            sResult = e.getMessage();
        }
        return sResult;
    }

    public static String PostBackJsonObject(String url, String sCertificate, String sType, String sCode,
            String signingOption, String sAction, String sToken,
            String sSigner, String sStatus, String sFile, String sCountryCode, String file_digest) {
        String sResult = "0";
        try {
            // SignerInfoJson signerJson = new SignerInfoJson();
            // signerJson.type = sType;
            // signerJson.code = sCode;
            // signerJson.signing_option = signingOption;
            // signerJson.country_code = sCountryCode;
            Object[] info = new Object[3];
            String[] time = new String[2];
            int[] intRes = new int[1];
            CertificateObject certObj = null;
            SignerInfoJson signerJson = new SignerInfoJson();
            VoidCertificateComponents(sCertificate, info, time, intRes);
            PostbackJson certJson = new PostbackJson();
            certJson.action = sAction;
            certJson.token = sToken;
            certJson.status = sStatus;
            certJson.file = sFile;
            certJson.file_digest = file_digest;
            certJson.valid_to = CommonFunction.CheckTextNull(time[1]);
            // certJson.signer_info = signerJson;
            ObjectMapper oMapperParse = new ObjectMapper();
            String sJson = oMapperParse.writeValueAsString(certJson);
            System.err.println("UrlPostBack: " + url);
            System.err.println("Request: " + sJson);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), sJson);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = httpClient.newCall(request).execute();
            System.out.println("requestbody PostBackJsonCertificateObject " + response.toString());
            // HttpPost request = new HttpPost(url);
            // StringEntity params = new StringEntity(sJson);
            // request.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            // request.setEntity(params);
            // HttpResponse response = httpClient.execute(request);
            // System.out.println("requestbody PostBackJsonObject " + response.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            sResult = e.getMessage();
        }
        return sResult;
    }

    public static void VoidCertificateComponents(String certstr, Object[] info, String[] time, int[] intRes) {
        try {
            if (certstr.toUpperCase().contains("BEGIN CERTIFICATE")) {
                certstr = certstr.replace("-----BEGIN CERTIFICATE-----", "");
            }
            if (certstr.toUpperCase().contains("END CERTIFICATE")) {
                certstr = certstr.replace("-----END CERTIFICATE-----", "");
            }
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            CertificateFactory certFactory1 = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(certstr));
            X509Certificate cert = (X509Certificate) certFactory1.generateCertificate(in);
            info[0] = cert.getSubjectDN();
            info[0] = info[0].toString().replace("\\", "");
            info[1] = cert.getIssuerDN();
            info[2] = cert.getSerialNumber().toString(16);
            time[0] = formatter.format(cert.getNotBefore());
            time[1] = formatter.format(cert.getNotAfter());
            intRes[0] = 0;
        } catch (Exception e) {
            System.out.print("VoidCertificateComponents: " + e.getMessage());
            intRes[0] = 1;
        }
    }

    public static String CheckTextNull(String sValue) {
        if (sValue == null) {
            sValue = "";
        } else {
            if (Difinitions.CONFIG_EXCEPTION_STRING_ERROR_NULL.equals(sValue.trim().toUpperCase())) {
                sValue = "";
            }
        }
        return sValue.trim();
    }

    public static final String OID_CN = "2.5.4.3";

    public static String getCommonNameInDN(String dn) {
        X500Name subject = new X500Name(dn);
        RDN[] rdn = subject.getRDNs();
        for (int j = 0; j < rdn.length; j++) {
            AttributeTypeAndValue[] attributeTypeAndValue = rdn[j].getTypesAndValues();
            if (attributeTypeAndValue[0].getType().toString().equals(OID_CN)) {
                return attributeTypeAndValue[0].getValue().toString();
            }
        }
        return "";
    }

    public boolean deleteFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.delete()) {
            System.out.println(file.getName() + " is deleted!");
        }
        return false;
    }
}
