package vn.mobileid.paperless.Model;

import vn.mobileid.paperless.Model.Request.CredentialListRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.mobileid.paperless.Model.Request.DocumentDigests;
import vn.mobileid.paperless.Model.Enum.MobileDisplayTemplate;
import vn.mobileid.paperless.Model.Enum.SignAlgo;
import vn.mobileid.paperless.Model.Request.AuthorizeRequest;
import vn.mobileid.paperless.Model.Request.LoginRequest;
import vn.mobileid.paperless.Model.Request.SearchConditions;
import vn.mobileid.paperless.Model.Request.CredentialInfoRequest;
import vn.mobileid.paperless.Model.Request.SignHashRequest;
import vn.mobileid.paperless.Model.Response.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import vn.mobileid.paperless.API.HttpResponse;
import vn.mobileid.paperless.API.HttpUtils;
import vn.mobileid.paperless.API.ICertificate;
import vn.mobileid.paperless.API.IServerSession;
import vn.mobileid.paperless.API.Property;
import vn.mobileid.paperless.API.Utils;
import vn.mobileid.paperless.Model.Request.CredentialSendOTPRequest;

public class ServerSession implements IServerSession {

    private String bearer;
    private String refreshToken;
    private Property property;
    private String lang;
    private String username;
    private String password;
    private int retryLogin = 0;

    public ServerSession(Property prop, String lang) throws Exception {
        this.property = prop;
        this.lang = lang;
        this.login();
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void login() throws Exception {
        System.out.println("____________auth/login____________");
        String authHeader;

        if (refreshToken != null) {
            authHeader = refreshToken;
        } else {
            retryLogin++;
            authHeader = property.getAuthorization();
        }
        System.out.println("Login-retry: " + retryLogin);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.rememberMeEnabled = true;
        loginRequest.relyingParty = property.relyingParty;
        loginRequest.lang = this.lang;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "auth/login", "POST", 50000, headers,
                Utils.gsTmp.toJson(loginRequest));

        if (!response.isStatus()) {
            throw new Exception(response.getMsg());
        }

        LoginResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), LoginResponse.class);
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            refreshToken = null;
            if (retryLogin >= 5) {
                retryLogin = 0;
                System.out.println("Response code: " + signCloudResp.error);
                System.out.println("Response Desscription: " + signCloudResp.errorDescription);
                System.out.println("Response ID: " + signCloudResp.responseID);
                System.out.println("AccessToken: " + signCloudResp.accessToken);
                throw new Exception(signCloudResp.errorDescription);
            }
            login();
        } else if (signCloudResp.error != 0) {
            System.out.println("Err code khac 0: " + signCloudResp.error);
            System.out.println("Err Desscription: " + signCloudResp.errorDescription);
            System.out.println("Response ID: " + signCloudResp.responseID);
            throw new Exception(signCloudResp.errorDescription);
        } else {
            this.bearer = "Bearer " + signCloudResp.accessToken;

            if (signCloudResp.refreshToken != null) {
                this.refreshToken = "Bearer " + signCloudResp.refreshToken;
                System.out.println("Success code: " + signCloudResp.error);
                System.out.println("Success Desscription: " + signCloudResp.errorDescription);
                System.out.println("Response ID: " + signCloudResp.responseID);
                System.out.println("AccessToken: " + signCloudResp.accessToken);
            }
        }
    }

    @Override
    public List<ICertificate> listCertificates() throws Exception {
        return listCertificates(null, null, false, false, null);
    }

    @Override
    public List<ICertificate> listCertificates(String agreementUUID) throws Exception {
        return listCertificates(agreementUUID, null, false, false, null);
    }

    @Override
    public List<ICertificate> listCertificates(String agreementUUID, String certificate, boolean certInfoEnabled,
                                               boolean authInfoEnabled, SearchConditions conditions) throws Exception {
        System.out.println("____________credentials/list____________");
        String authHeader = bearer;
        CredentialListRequest credentiallistRequest = new CredentialListRequest();
        credentiallistRequest.userID = agreementUUID;
        credentiallistRequest.agreementUUID = null;
        credentiallistRequest.certificates = certificate;
        credentiallistRequest.certInfoEnabled = certInfoEnabled;
        credentiallistRequest.authInfoEnabled = authInfoEnabled;
        credentiallistRequest.searchConditions = conditions;
        credentiallistRequest.lang = this.lang;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "credentials/list", "POST", 50000,
                headers, Utils.gsTmp.toJson(credentiallistRequest));

        if (!response.isStatus()) {
            throw new Exception(response.getMsg());
        }

        CredentialListResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), CredentialListResponse.class);
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            login();
            return listCertificates(agreementUUID, certificate, certInfoEnabled, authInfoEnabled, conditions);
        } else if (signCloudResp.error != 0) {
            System.out.println("Err Code: " + signCloudResp.error);
            System.out.println("Err Desscription: " + signCloudResp.errorDescription);
            throw new Exception(signCloudResp.errorDescription);
        }
        List<vn.mobileid.paperless.Model.Response.BaseCertificateInfo> listCert = signCloudResp.certs;
        List<ICertificate> listCertificate = new ArrayList<ICertificate>();

        for (vn.mobileid.paperless.Model.Response.BaseCertificateInfo item : listCert) {
            // ICertificate icrt = (ICertificate) new Certificate();
            ICertificate icrt = new Certificate(item, null, this);
            listCertificate.add(icrt);
        }

        return listCertificate;
    }

    @Override
    public ICertificate certificateInfo(String credentialID) throws Exception {
        return certificateInfo(null, credentialID, null, false, false);
    }

    @Override
    public ICertificate certificateInfo(String agreementUUID, String credentialID) throws Exception {
        return certificateInfo(agreementUUID, credentialID, null, false, false);
    }

    @Override
    public ICertificate certificateInfo(String agreementUUID, String credentialID, String certificate,
                                        boolean certInfoEnabled, boolean authInfoEnabled) throws Exception {
        System.out.println("____________credentials/info____________");
        CredentialInfoRequest request = new CredentialInfoRequest();
        request.agreementUUID = agreementUUID;
        request.credentialID = credentialID;
        request.certificates = certificate;
        request.certInfoEnabled = certInfoEnabled;
        request.authInfoEnabled = authInfoEnabled;
        request.lang = this.lang;
        String authHeader = bearer;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "credentials/info", "POST", 50000,
                headers, Utils.gsTmp.toJson(request));
        if (!response.isStatus()) {
            throw new Exception(response.getMsg());
        }

        CredentialInfoResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), CredentialInfoResponse.class);
        System.out.println("response: " + signCloudResp.getAuthMode());
        if (signCloudResp.cert.status.equals("EXPIRED")) {
            throw new APIException(signCloudResp.cert.status);
        }
        if(!signCloudResp.authMode.name().equals("IMPLICIT_TSE") && !signCloudResp.authMode.name().equals("IMPLICIT_BIP_CATTP")){
            System.out.println("ye ye");
            throw new APIException("not available");
        }
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            login();
            return certificateInfo(agreementUUID, credentialID, certificate, certInfoEnabled, authInfoEnabled);
        } else if (signCloudResp.error != 0) {
            // System.out.println("Err code: " + signCloudResp.error);
            // System.out.println("Err Desscription: " + signCloudResp.errorDescription);
            throw new APIException(signCloudResp.errorDescription);
        }
        System.out.println("err code: " + signCloudResp.error);
        System.out.println("error description: " + signCloudResp.errorDescription);

        ICertificate iCrt = (ICertificate) new Certificate(signCloudResp.cert, agreementUUID, this);
        signCloudResp.cert.authorizationEmail = signCloudResp.authorizationEmail;
        signCloudResp.cert.authorizationPhone = signCloudResp.authorizationPhone;
        signCloudResp.cert.sharedMode = signCloudResp.sharedMode;
        signCloudResp.cert.createdRP = signCloudResp.createdRP;
        signCloudResp.cert.authModes = signCloudResp.authModes;
        signCloudResp.cert.authMode = signCloudResp.authMode;
        signCloudResp.cert.SCAL = signCloudResp.SCAL;
        signCloudResp.cert.contractExpirationDate = signCloudResp.contractExpirationDate;
        signCloudResp.cert.defaultPassphraseEnabled = signCloudResp.defaultPassphraseEnabled;
        signCloudResp.cert.trialEnabled = signCloudResp.trialEnabled;
        return iCrt;
    }

    @Override
    public String sendOTP(String agreementUUID, String credentialID, String notificationTemplate,
                          String notificationSubject) throws Throwable {
        System.out.println("____________credentials/sendOTP____________");
        CredentialSendOTPRequest request = new CredentialSendOTPRequest();
        request.setAgreementUUID(agreementUUID);
        request.setCredentialID(credentialID);
        request.setNotificationSubject(notificationSubject);
        request.setNotificationTemplate(notificationTemplate);
        request.setLang(this.lang);

        String jsonReq = Utils.gsTmp.toJson(request);
        HttpResponse response = HttpUtils.sendPost(property.getBaseUrl() + "credentials/sendOTP", jsonReq, bearer);
        if (!response.isStatus()) {
            try {
                throw new Exception(response.getMsg());
            } catch (Exception ex) {
                Logger.getLogger(ServerSession.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Response signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), Response.class);
        if (signCloudResp.getError() == 3005 || signCloudResp.getError() == 3006) {
            login();
            return sendOTP(agreementUUID, credentialID, notificationTemplate, notificationSubject);
        } else if (signCloudResp.getError() != 0) {
            throw new APIException(signCloudResp.getError(), signCloudResp.getErrorDescription());
        }
        return signCloudResp.getResponseID();
    }

    @Override
    public String authorize(String agreementUUID, String credentialID, int numSignatures, DocumentDigests doc,
                            SignAlgo signAlgo, String authorizeCode) throws Exception {
        return authorize(agreementUUID, credentialID, numSignatures, doc, signAlgo, null, authorizeCode);
    }

    public PostBackResponse postback() throws Exception {
        String authHeader = bearer;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(
                "https://paperless-gw.mobile-id.vn/ppl-webhook/api/v1/postback-handler", "POST", 50000, headers, null);
        if (!response.isStatus()) {
            throw new Exception(response.getMsg());
        }
        PostBackResponse postBackResponse = Utils.gsTmp.fromJson(response.getMsg(), PostBackResponse.class);
        if (postBackResponse.error == 3005 || postBackResponse.error == 3006) {
            login();
            postback();
        } else if (postBackResponse.error != 0) {
            System.out.println("Err code: " + postBackResponse.error);
            System.out.println("Err Desscription: " + postBackResponse.errorDescription);
        }
        return postBackResponse;
    }

    @Override
    public String authorize(String agreementUUID, String credentialID, int numSignatures, DocumentDigests doc,
                            SignAlgo signAlgo, String otpRequestID, String otp) throws Exception {
        System.out.println("____________credentials/authorize____________");
        AuthorizeRequest request = new AuthorizeRequest();
        request.vcEnabled = true;
        request.agreementUUID = agreementUUID;
        request.credentialID = credentialID;
        request.numSignatures = numSignatures;
        request.documentDigests = doc;
        request.signAlgo = signAlgo;
        request.requestID = otpRequestID;
        request.authorizeCode = otp;
        request.lang = this.lang;
        String authHeader = bearer;

        System.out.println("documentDigests: " + request.documentDigests);
        System.out.println("authorizeCode: " + request.authorizeCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "credentials/authorize", "POST", 50000,
                headers, Utils.gsTmp.toJson(request));

        if (!response.isStatus()) {
            throw new Exception(response.getMsg());
        }
        AuthorizeResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), AuthorizeResponse.class);
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            login();
            return authorize(agreementUUID, credentialID, numSignatures, doc, signAlgo, otpRequestID, otp);
        } else if (signCloudResp.error != 0) {
            System.out.println("Err code: " + signCloudResp.error);
            System.out.println("Err Desscription: " + signCloudResp.errorDescription);
            throw new Exception(signCloudResp.errorDescription);
        }

        System.out.println("err code: " + signCloudResp.error);
        System.out.println("error description: " + signCloudResp.errorDescription);
        return signCloudResp.SAD;
    }

    @Override
    public String authorize(String agreementUUID, String credentialID, int numSignatures, DocumentDigests doc,
                            SignAlgo signAlgo, MobileDisplayTemplate displayTemplate) throws Exception {
        System.out.println("____________credentials/authorize____________");
        AuthorizeRequest request = new AuthorizeRequest();
        request.agreementUUID = agreementUUID;
        request.credentialID = credentialID;
        request.numSignatures = numSignatures;
        request.documentDigests = doc;
        request.signAlgo = signAlgo;
        request.notificationMessage = displayTemplate.notificationMessage;
        request.messageCaption = displayTemplate.messageCaption;
        request.message = displayTemplate.message;
        request.logoURI = displayTemplate.logoURI;
        request.rpIconURI = displayTemplate.rpIconURI;
        request.bgImageURI = displayTemplate.bgImageURI;
        request.rpName = displayTemplate.rpName;
        request.scaIdentity = displayTemplate.scaIdentity;
        request.vcEnabled = displayTemplate.vcEnabled;
        request.acEnabled = displayTemplate.acEnabled;
        request.lang = this.lang;

        String authHeader = bearer;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "credentials/authorize", "POST", 302000,
                headers, Utils.gsTmp.toJson(request));

        if (!response.isStatus()) {
            try {
                throw new Exception(response.getMsg());
            } catch (Exception ex) {
                Logger.getLogger(ServerSession.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        AuthorizeResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), AuthorizeResponse.class);
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            try {
                login();
            } catch (Exception ex) {
                Logger.getLogger(ServerSession.class.getName()).log(Level.SEVERE, null, ex);
            }
            return authorize(agreementUUID, credentialID, numSignatures, doc, signAlgo, displayTemplate);
        } else if (signCloudResp.error != 0) {
            System.out.println("err code: " + signCloudResp.error);
            System.out.println("error description: " + signCloudResp.errorDescription);
            throw new Exception(signCloudResp.errorDescription);
        }
        return signCloudResp.SAD;
    }

    @Override
    public List<byte[]> signHash(String agreementUUID, String credentialID, DocumentDigests documentDigest,
                                 SignAlgo signAlgo, String SAD) throws Exception {
        System.out.println("____________signatures/signHash____________");
        SignHashRequest request = new SignHashRequest();
        // request.agreementUUID = agreementUUID;
        request.credentialID = credentialID;
        request.documentDigests = documentDigest;
        request.signAlgo = signAlgo;
        request.SAD = SAD;
        request.lang = this.lang;
        String authHeader = bearer;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", authHeader);
        HttpResponse response = HttpUtils.invokeHttpRequest(property.baseUrl + "signatures/signHash", "POST", 50000,
                headers, Utils.gsTmp.toJson(request));
        if (!response.isStatus()) {
            System.out.println("lỗi" + response.getMsg());
            throw new Exception(response.getMsg());
        }

        SignHashResponse signCloudResp = Utils.gsTmp.fromJson(response.getMsg(), SignHashResponse.class);
        if (signCloudResp.error == 3005 || signCloudResp.error == 3006) {
            login();
            return signHash(agreementUUID, credentialID, documentDigest, signAlgo, SAD);
        } else if (signCloudResp.error != 0) {
            System.out.println("Err code cho lay: " + signCloudResp.error);
            System.out.println("Err Desscription: " + signCloudResp.errorDescription);
            throw new Exception(signCloudResp.errorDescription);
        }
        System.out.println("err code cho lay lay: " + signCloudResp.error);
        System.out.println("error description: " + signCloudResp.errorDescription);
        return signCloudResp.signatures;
    }

}
