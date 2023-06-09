/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.paperless.service;

import com.itextpdf.text.pdf.AcroFields;
import java.io.*;
import java.util.*;

import com.itextpdf.text.pdf.BaseFont;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import java.security.Security;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import vn.mobileid.exsig.*;
import vn.mobileid.paperless.API.ICertificate;
import vn.mobileid.paperless.API.IServerSession;
import vn.mobileid.paperless.API.Property;
import vn.mobileid.paperless.API.SigningMethodAsyncImp;
import vn.mobileid.paperless.API.Utils;
import vn.mobileid.paperless.Model.Enum.HashAlgorithmOID;
import vn.mobileid.paperless.Model.Enum.MobileDisplayTemplate;
import vn.mobileid.paperless.Model.Enum.SignAlgo;
import vn.mobileid.paperless.Model.Request.DocumentDigests;
import vn.mobileid.paperless.Model.Response.BaseCertificateInfo;
import vn.mobileid.paperless.Model.Response.CertificateInfo;
import vn.mobileid.paperless.Model.SessionFactory;
import vn.mobileid.paperless.SignFile.SignFileFactory;
import vn.mobileid.paperless.config.RSSPConfig;
import vn.mobileid.paperless.entity.SignPosition;
import vn.mobileid.paperless.utils.Difinitions;


/**
 *
 * @author Mr Spider
 */
@Slf4j
@Service
public class RSSPService {

    public static String hashCodeFile;

    // public static String PATH_TO_FILE_CONFIG = "D:/project/file/rssp.ssl2";

    public static Properties prop = new Properties();

    public IServerSession Handshake_func(String baseUrl, String relyingParty, String relyingPartyUser,
                                         String relyingPartyPassword, String relyingPartySignature, String relyingPartyKeyStoreValue,
                                         String relyingPartyKeyStorePassword, String lang) throws Exception {
        RSSPConfig rsspConfig = new RSSPConfig();
        Property property = rsspConfig.loadRSSPConfig(baseUrl, relyingParty, relyingPartyUser, relyingPartyPassword,
                relyingPartySignature, relyingPartyKeyStoreValue, relyingPartyKeyStorePassword);

        SessionFactory factory = new SessionFactory(property, lang);
        return factory.getServerSession();

    }

    public void getListCertificates(IServerSession session) throws Exception {
        // từ session lấy được khi login, tiến hành gọi hàm listCertificates để trả về
        // danh sách các chứng chỉ ICertificate và gán vào biến crts
        // session là instance of ServerSession
        List<ICertificate> crts = session.listCertificates();
        // in các chứng chỉ ra maàn hình console
        for (ICertificate cc : crts) {
            System.out.println("credentialID      : " + cc.baseCredentialInfo().getCredentialID());
            System.out.println("status            : " + cc.baseCredentialInfo().getStatus());
            System.out.println("status Description: " + cc.baseCredentialInfo().getStatusDesc());
        }

    }

    // ____________credentials/info____________
    public String getCertificateInfo(ICertificate crt, String credentialID, IServerSession session) throws Exception {
        // session là instance of ServerSession
        crt = session.certificateInfo(credentialID);
        BaseCertificateInfo info = crt.baseCredentialInfo();
        String certChain = info.getCertificates()[0];
        System.out.println("certificates: " + certChain);
        return certChain;
    }

    public String authorize(DocumentDigests doc, ICertificate crt, int numSignatures,MobileDisplayTemplate template) throws Throwable {

        // Do authorize for certificate with AuthMode is PIN, we call
        // credentials/authorize
        String sad = crt.authorize(numSignatures, doc, null, template);
        System.out.println("SAD: " + sad);

        // Do authorize for certificate with AuthMode is OTP SMS/EMAIL, we call
        // credentials/authorize
        // String sad = crt.authorize(numSignatures, doc, null, otpRequestID, otp);
        // System.out.println("SAD: " + sad);
        return sad;
    }

    public String signHashFile(DocumentDigests Doc, String sad, String credentialID, ICertificate crt,
                               String sFolderSave, String[] sSignCertificate,Date[] signingTime) throws Exception {
        // public String signHashFile(DocumentDigests Doc,String sad, String
        // credentialID, ICertificate crt,
        // InputStream[] inStreamSigned, String[] sSignCertificate) throws Exception {

        SignAlgo signAlgo = SignAlgo.RSA;
        CertificateInfo crtInfo = crt.credentialInfo("single", true, true);
        List<byte[]> signatures = crt.signHash(credentialID, Doc, signAlgo, sad);

        for (byte[] s : signatures) {
            System.out.println("PKCS#1-Signature: " + Base64.getEncoder().encodeToString(s));
            sSignCertificate[1] = Base64.getEncoder().encodeToString(s);
            SigningMethodAsyncImp signFinal = new SigningMethodAsyncImp();

            List<String> chain = new ArrayList<>();
            chain.add(crtInfo.certificates[0]);
            signFinal.certificateChain = chain;
            sSignCertificate[0] = crtInfo.certificates[0].toString();

            List<String> Signature = new ArrayList<>();
            Signature.add(Utils.base64Encode(s));
            signFinal.signatures = Signature;

            byte[] temp = signFinal.loadTemporalData(credentialID);

            List<byte[]> results = PdfProfileCMS.sign(signFinal, temp);

            // int i = 0;
            // List<InputStream> inputStreamList = new ArrayList<>();
            for (byte[] result : results) {
                /// save file signed
                // OutputStream OS = new FileOutputStream("d:/project/file/sample.signed.pdf");
                // OutputStream OS = new FileOutputStream(sFolderSave);
                // IOUtils.write(result, OS);
                // System.out.println("Signed File Successfully ! Save in:
                // d:/project/file/sample.signed.pdf");
                // OS.close();

                // byte[] bytes = org.apache.pdfbox.io.IOUtils.toByteArray(jrbFile.getStream());

                System.out.println("Byte: " + result.length);
                FileOutputStream fs = new FileOutputStream(new File(sFolderSave));
                BufferedOutputStream bs = new BufferedOutputStream(fs);
                bs.write(result);
                bs.close();
                bs = null;

                // get inputstream
                // inputStreamList.add(new ByteArrayInputStream(result));
                // inStreamSigned[0] = new ByteArrayInputStream(result);
                break;

                // i++;
            }
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            InputStream filePdf = new FileInputStream(sFolderSave);
            PdfReader reader = new PdfReader(filePdf,null);
            AcroFields af = reader.getAcroFields();
            ArrayList<String> names = af.getSignatureNames();
            for(String name : names) {
                PdfPKCS7 pdfPKCS7 = af.verifySignature(name);
                Date signTime = pdfPKCS7.getSignDate().getTime();
                signingTime[0] = signTime;
                System.out.println("signTime: " + signTime);
            }
//            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//            InputStream inputStream = new FileInputStream(new File(sFolderSave));
//            PdfReader reader = new FdfReader(inputStream);
//            AcroFields af = reader.getAcroFields();
//            ArrayList<String> names = af.getSignatureNames();
//            for(String name:names){
//                PdfPKCS7 pdfPKCF7 = af.verifySignature(name);
//                Date signTime = pdfPKCF7.getSignDate().getTime();
//                System.out.println("signTime: " + signTime);
//            }
            // InputStream value_inputStream = new
            // SequenceInputStream(Collections.enumeration(inputStreamList));
            // inStreamSigned[0] = value_inputStream;
        }
        String kq = "OK";
        return kq;

    }

    public DocumentDigests getDoc(byte[] pdfData, String credentialID, SignPosition signPosition, String certChain,
                                  String sReasonSign,String khungKy, String[] sHashList) throws Exception {

//        PdfProfileCMS profile = new PdfProfileCMS(Algorithm.SHA256);
//        profile.setReason(sReasonSign);
////        profile.setTextContent("Ký bởi: {signby} \nNgày ký: {date}");
//        profile.setTextContent("Ký bởi: {signby} \nLý do: {reason} \nNgày ký: {date}");
//        profile.setVisibleSignature(signPosition.getPageNumber(), signPosition.position.getPos());
//        // profile.setVisibleSignature("-30,-100", "170,70", titleSignature);
//        profile.setSigningTime(Calendar.getInstance(),"dd/MM/yyyy HH:mm:ss");
//        profile.setBorder(Color.BLACK);
//        profile.setCheckText(false);
//        profile.setCheckMark(false);
//        profile.setSignerCertificate(certChain);
        System.out.println("get doc: " );
        PdfEsealCMS profileCMS = new PdfEsealCMS(PdfForm.B, Algorithm.SHA256);
        profileCMS.setReason(sReasonSign);
        profileCMS.setSigningTime(Calendar.getInstance(),"dd/MM/yyyy HH:mm:ss");
        profileCMS.createEseal(1, signPosition.position.getX1(), signPosition.position.getY1(), "{signby}", "{date}\n"+khungKy);
        profileCMS.setSignerCertificate(certChain);



        List<byte[]> src = new ArrayList<>();
        src.add(pdfData);


        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream("file/Roboto-Bold.ttf");
        // convert inputstream to bytearray
        byte[] fontTitle = IOUtils.toByteArray(input);
//        File fTitle = new File("file/Roboto-Bold.ttf");
//        byte[] fontTitle = FileUtils.readFileToByteArray(fTitle);

        profileCMS.setEsealTitleFont(
                fontTitle,
                BaseFont.IDENTITY_H,
                true,
                0,
                0,
                TextAlignment.ALIGN_LEFT,
                Color.YELLOW);

//        File fontContent = new File("file/Roboto-Regular.ttf");
//        byte[] fontData = FileUtils.readFileToByteArray(fontContent);
        InputStream input2 = loader.getResourceAsStream("file/Roboto-Regular.ttf");
        byte[] fontData = IOUtils.toByteArray(input2);

        profileCMS.setEsealContentFont(
                fontData,
                BaseFont.IDENTITY_H,
                true,
                0,
                0,
                TextAlignment.ALIGN_LEFT,
                Color.YELLOW);
        // pdffile.close(); // su dung xong và ko còn sd cho nào nua thi xoa

//        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        InputStream input = loader.getResourceAsStream("file/D-Times.ttf");
//
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int nRead;
//        byte[] data = new byte[4];
//        while ((nRead = input.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
//        buffer.flush();
//        byte[] font = buffer.toByteArray();
//        profile.setFont(
//                font,
//                BaseFont.IDENTITY_H,
//                true,
//                14,
//                1,
//                TextAlignment.ALIGN_LEFT,
//                Color.BLACK);

        SigningMethodAsyncImp signInit = new SigningMethodAsyncImp();
        log.info("signInit: {}", signInit);
        byte[] temporalData = profileCMS.createTemporalFile(signInit, src);
        List<String> hashList = signInit.hashList;
        sHashList[0] = hashList.get(0);
        // hashCodeFile = hashList.get(0);

        // String hashCode ="pzhklXu4qa7Nojc8hTISj366kWok4O9hNFjmvDrMOIU=";
        // List<byte[]> list = new ArrayList<>();
        // list.add(base64Decode(hashList.get(0)));
        // String codeVC = computeVC(list);
        // System.out.println("VC Code is cho~ nay` a': " + codeVC);

        log.info("hashList: {}", hashList);
        signInit.saveTemporalData(credentialID, temporalData);

        HashAlgorithmOID hashAlgo = HashAlgorithmOID.SHA_256;
        DocumentDigests Doc = new DocumentDigests();
        Doc.hashAlgorithmOID = hashAlgo;
        Doc.hashes = new ArrayList<>();
        Doc.hashes.add(Utils.base64Decode(hashList.get(0)));

        return Doc;
    }

    public static SignFileFactory.SignType getSignType() throws Throwable {
        return SignFileFactory.SignType.PAdES;
    }

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

    public int calculateHeight(byte[] pdfData) throws Exception {
//        byte[] pdfData = IOUtils.readFully(inputStream,inputStream.available(),true);
        PdfReader inputPdfReader = new PdfReader(pdfData);
        int height= (int) inputPdfReader.getPageSize(1).getHeight();
        int width= (int) inputPdfReader.getPageSize(1).getWidth();

        inputPdfReader.close();

//        PdfReader reader = new PdfReader(inputStream);
//        int height= (int) reader.getPageSize(1).getHeight();
//
//        // Đảm bảo đóng tài nguyên sau khi hoàn thành
//        reader.close();
        return height;
    }


}
