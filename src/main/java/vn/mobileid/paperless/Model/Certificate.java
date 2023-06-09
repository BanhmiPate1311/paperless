package vn.mobileid.paperless.Model;

import vn.mobileid.paperless.API.ICertificate;
import vn.mobileid.paperless.API.IServerSession;
import vn.mobileid.paperless.Model.Enum.MobileDisplayTemplate;
import vn.mobileid.paperless.Model.Enum.SignAlgo;
import vn.mobileid.paperless.Model.Enum.SignedPropertyType;
import vn.mobileid.paperless.Model.Request.DocumentDigests;
import vn.mobileid.paperless.Model.Response.BaseCertificateInfo;
import vn.mobileid.paperless.Model.Response.CertificateInfo;

import java.util.HashMap;
import java.util.List;

public class Certificate implements ICertificate {

    private BaseCertificateInfo certificate;
    private String agreementUUID;
    private IServerSession serverSession;

    public Certificate() {
    }

    public Certificate(BaseCertificateInfo cert, String agreementUUID, IServerSession serverSession) {
        this.certificate = cert;
        this.agreementUUID = agreementUUID;
        this.serverSession = serverSession;
    }

    public BaseCertificateInfo baseCredentialInfo() {
        return certificate;
    }

    @Override
    public CertificateInfo credentialInfo() throws Exception {
        ICertificate icrt = this.serverSession.certificateInfo(this.agreementUUID, certificate.credentialID);
        if (icrt.baseCredentialInfo() instanceof CertificateInfo) {
            return (CertificateInfo) icrt.baseCredentialInfo();
        }
        System.out.println("Type of certificate is not [CertificateInfo]");
        return (CertificateInfo) icrt.baseCredentialInfo();
    }

    @Override
    public CertificateInfo credentialInfo(String cetificate, boolean certInfoEnabled, boolean authInfoEnabled) throws Exception {
        ICertificate icrt = this.serverSession.certificateInfo(this.agreementUUID, certificate.credentialID, cetificate, certInfoEnabled, authInfoEnabled);
        if (icrt.baseCredentialInfo() instanceof CertificateInfo) {
            return (CertificateInfo) icrt.baseCredentialInfo();
        }
        System.out.println("Type of certificate is not [CertificateInfo]");
        return (CertificateInfo) icrt.baseCredentialInfo();
    }
    
    @Override
    public String sendOTP(String notificationTemplate, String notificationSubject) throws Throwable {
        return this.serverSession.sendOTP(this.agreementUUID, this.certificate.getCredentialID(), notificationTemplate, notificationSubject);
    }
    
    @Override
    public String authorize(int numSignatures, DocumentDigests doc, SignAlgo signAlgo, String authorizeCode) throws Throwable{
        return this.serverSession.authorize(this.agreementUUID, this.certificate.credentialID, numSignatures, doc, signAlgo, authorizeCode);
    }
    @Override
    public String authorize(int numSignatures, DocumentDigests doc, SignAlgo signAlgo, String otpRequestID, String otp) throws Throwable{
        return this.serverSession.authorize(this.agreementUUID, this.certificate.credentialID, numSignatures, doc, signAlgo, otpRequestID, otp);
    }
    @Override
    public String authorize(int numSignatures, DocumentDigests doc, SignAlgo signAlgo, MobileDisplayTemplate displayTemplate) throws Throwable{
        return this.serverSession.authorize(this.agreementUUID, this.certificate.credentialID, numSignatures, doc, signAlgo, displayTemplate);
    }

    @Override
    public List<byte[]> signHash(String credentialID, DocumentDigests documentDigest, SignAlgo signAlgo, String SAD) throws Exception{
        //return this.serverSession.signHash(this.agreementUUID, this.certificate.credentialID, documentDigest, signAlgo, SAD);
        return this.serverSession.signHash(null, credentialID, documentDigest, signAlgo, SAD);
    } 

    @Override
    public List<byte[]> signDoc(HashMap<SignedPropertyType, Object> signedProps, List<byte[]> docs, String SAD) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
