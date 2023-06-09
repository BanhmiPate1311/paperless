/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.paperless.controller;

//import com.sun.xml.internal.ws.wsdl.writer.document.Definitions;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ua_parser.Client;
import ua_parser.Parser;
import vn.mobileid.exsig.PdfProfile;
import vn.mobileid.exsig.VerifyResult;
import vn.mobileid.fms.client.JCRFile;
import vn.mobileid.paperless.object.COMNECTOR_ATTRIBUTE;
import vn.mobileid.paperless.object.PPLFile;
import vn.mobileid.paperless.object.Participants;
import vn.mobileid.paperless.object.WorkFlowList;
import vn.mobileid.paperless.process.process;

import static vn.mobileid.paperless.uat.Main.base64Decode;
import static vn.mobileid.paperless.uat.Main.computeVC;

import vn.mobileid.paperless.API.ICertificate;
import vn.mobileid.paperless.API.IServerSession;
import vn.mobileid.paperless.Model.Enum.MobileDisplayTemplate;
import vn.mobileid.paperless.Model.Request.DocumentDigests;
import vn.mobileid.paperless.Model.Response.BaseCertificateInfo;
import static vn.mobileid.paperless.controller.APIController.getSigningtoken;
import vn.mobileid.paperless.entity.SignPosition;
import vn.mobileid.paperless.service.FileJRBService;
import vn.mobileid.paperless.service.RSSPService;
import vn.mobileid.paperless.service.VCStoringService;
import vn.mobileid.paperless.utils.CommonFunction;
import vn.mobileid.paperless.utils.CommonHash;
import vn.mobileid.paperless.utils.Difinitions;

/**
 * @author Mr Spider
 */
@Slf4j
@RestController
public class RSSPController {

    // public static IServerSession session;
    private final RSSPService rsspService;
    public static String nameFile;

    @Autowired
    private VCStoringService vcStoringService;

    @Autowired
    public RSSPController() {
        rsspService = new RSSPService();
    }

    // download get mapping with signingToken path variable
    @GetMapping("{signingToken}/download")
    public ResponseEntity<?> downloadFile(@PathVariable String signingToken,
            @RequestParam("access_token") String accessToken) {
        try {
            process connectDB = new process();
            String pDMS_PROPERTY = FileJRBService.getPropertiesFMS();
            String sUUID_Last = "";
            InputStream inputStreamFile = null;
            WorkFlowList[][] rsWFList = new WorkFlowList[1][];
            connectDB.USP_GW_PPL_WORKFLOW_GET(rsWFList, signingToken);
            if (rsWFList != null && rsWFList[0].length > 0) {
                if (rsWFList[0][0].WORKFLOW_STATUS != Difinitions.CONFIG_PPL_WORKFLOW_STATUS_PENDING) {
                    Participants[][] rsParticipant = new Participants[1][];
                    connectDB.USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(rsParticipant, accessToken);
                    if (rsParticipant[0] != null && rsParticipant[0].length > 0) {
                        if (rsParticipant[0][0].SIGNER_STATUS != Difinitions.CONFIG_WORKFLOW_PARTICIPANTS_SIGNER_STATUS_ID_PENDING
                                && rsParticipant[0][0].PPL_WORKFLOW_ID == rsWFList[0][0].ID) {
                            PPLFile[][] rsFile = new PPLFile[1][];
                            connectDB.USP_GW_PPL_WORKFLOW_GET_LAST_FILE(rsFile, signingToken);
                            if (rsFile != null && rsFile[0].length > 0) {
                                sUUID_Last = rsFile[0][0].FILE_UUID;
                            }

                        }
                    }
                }
            }
            if (!"".equals(sUUID_Last)) {
                JCRFile jrbFile = FileJRBService.downloadFMS(sUUID_Last, pDMS_PROPERTY);
                if (jrbFile.getStream() != null) {
                    inputStreamFile = jrbFile.getStream();
                }
            }
            if (inputStreamFile != null) {
                // trả về stream input file để download kèm header content type và content
                // length để browser hiểu
                HttpHeaders headers = new HttpHeaders();
//                headers.add("Content-Disposition", "attachment; filename=" + "file.pdf");
                headers.add("Content-Disposition", "attachment; filename=" + nameFile + "_signed.pdf");
                // jrbFile.getFileName());
                InputStreamResource inputStreamResource = new InputStreamResource(inputStreamFile);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .headers(headers)
                        .body(inputStreamResource);
            } else {
                // trả về lỗi không tìm thấy file để download
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }
        } catch (Exception e) {
            log.error("Error when download file", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signFile")
    public ResponseEntity<?> signFile(
            //            @RequestParam("username") String usernameRequest,
            @RequestParam("lang") String lang,
            @RequestParam("tab") String tab,
            @RequestParam("filename") String fileName,
            @RequestParam("signingToken") String signingTokenRequest,
            //            @RequestParam("password") String passwordRequest,
            @RequestParam("signerToken") String signerTokenRequest,
            @RequestParam("value") String valueRequest,
            @RequestParam("type") String typeRequest,
            @RequestParam("name") String nameRequest,
            //            @RequestParam("tabName") String tabNameRequest,
            @RequestParam("requestID") String requestID,
            //            @RequestPart("file") MultipartFile fileRequest,
            @RequestParam("connector_name") String sConnectorRequest,
            @RequestParam("tabName") String signingOption,
            HttpServletRequest request)
            throws FileNotFoundException, IOException, Exception, Throwable {
        try {
            boolean error = false;
            // String sConnectorRequest = "SMART_ID_MOBILE_ID";
            log.info("sConnectorRequest: {}", sConnectorRequest);
            log.info("signingTokenRequest: {}", signingTokenRequest);
//            log.info("fileName: {}", fileRequest.getOriginalFilename());
            process connectDB = new process();
            // int[] sStatusWFCheck = new int[2];
            // String[] sPostbackWFCheck = new String[2];
            // int[] sStatusParticipantsCheck = new int[1];

            // String sCallWFCheck =
            // connectDB.USP_PPL_WORKFLOW_GET_STATUS(signingTokenRequest,
            // sStatusWFCheck,sPostbackWFCheck);
            WorkFlowList[][] rsWFList = new WorkFlowList[1][];
            connectDB.USP_GW_PPL_WORKFLOW_GET(rsWFList, signingTokenRequest);
            String sResult = "0";
            if (rsWFList[0] != null && rsWFList[0].length > 0) {
                if (rsWFList[0][0].WORKFLOW_STATUS == Difinitions.CONFIG_PPL_WORKFLOW_STATUS_PENDING) {
                    // String sCallParticipantCheck = connectDB
                    // .USP_PPL_WORKFLOW_PARTICIPANTS_GET_STATUS(signerTokenRequest,
                    // sStatusParticipantsCheck);
                    Participants[][] rsParticipant = new Participants[1][];
                    connectDB.USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(rsParticipant, signerTokenRequest);
                    if (rsParticipant[0] != null && rsParticipant[0].length > 0) {
                        if (rsParticipant[0][0].SIGNER_STATUS == Difinitions.CONFIG_WORKFLOW_PARTICIPANTS_SIGNER_STATUS_ID_PENDING) {
                            // String fileName = fileRequest.getOriginalFilename();//lay tu client len
                            log.info("nameFile: {}", fileName);
                            String fileName1 = fileName.replace(".pdf", "");
                            nameFile = fileName.replace(".pdf", "");
                            String fileName_Signed = fileName1 + "_" + RSSPService.generateNumberDays() + "_signed"
                                    + ".pdf";
                            log.info("Login");
                            String codeNumber = "";
                            String sType = Difinitions.CONFIG_PREFIX_UID_PHONE_CONTRACT;
                            if ("0".equals(nameRequest)) {
                                if (valueRequest.trim().contains(Difinitions.CONFIG_PHONE_PREFIX_COUNTRY_VIETNAMESE)) {
                                    valueRequest = valueRequest.trim().replace(Difinitions.CONFIG_PHONE_PREFIX_COUNTRY_VIETNAMESE, "0");
                                }
                                codeNumber = Difinitions.CONFIG_PREFIX_UID_PHONE_CONTRACT + ":" + valueRequest.trim();
                                System.out.println(codeNumber);
                            } else {
                                sType = Difinitions.CONFIG_PREFIX_UID_PERSONAL_ID;
                                codeNumber = typeRequest + ":" + valueRequest.trim();
                                System.out.println(codeNumber);
                            }

                            // String phoneNumber = "CITIZEN-IDENTITY-CARD:079083011315";
                            String pDMS_PROPERTY = FileJRBService.getPropertiesFMS();
                            // call store get file last, lay dc UUID, call len FMS lay Ã­putstream ve
                            String sUUID_Last = "";
                            int sFileID_Last = 0;
                            PPLFile[][] rsFile = new PPLFile[1][];
                            connectDB.USP_GW_PPL_WORKFLOW_GET_LAST_FILE(rsFile, signingTokenRequest);
                            if (rsFile != null && rsFile[0].length > 0) {
                                sFileID_Last = rsFile[0][0].ID;
                                sUUID_Last = rsFile[0][0].FILE_UUID;
                            }
                            String sPathFile = "";
                            // String sFileNotSigned = sPathFile + RSSPService.generateNumberDays() +
                            // "_notsigned.pdf";
                            JCRFile jrbFile = FileJRBService.downloadFMS(sUUID_Last, pDMS_PROPERTY);
                            InputStream inputStreamNotSigned = null;
                            if (jrbFile.getStream() != null) {
                                inputStreamNotSigned = jrbFile.getStream();
                                // byte[] bytes =
                                // org.apache.pdfbox.io.IOUtils.toByteArray(inputStreamNotSigned);
                                // System.out.println("Byte: " + bytes.length);
                                // FileOutputStream fs = new FileOutputStream(new File(sFileNotSigned));
                                // BufferedOutputStream bs = new BufferedOutputStream(fs);
                                // bs.write(bytes);
                                // bs.close();
                                // bs = null;
                            }

                            // File file = new File(sFileNotSigned); // đường dẫn đến tệp cần tạo
                            // try {
                            // OutputStream outputStream = new FileOutputStream(file);
                            //
                            // byte[] buffer = new byte[1024]; // tạo bộ đệm để đọc dữ liệu từ InputStream
                            // int bytesRead;
                            // while ((bytesRead = inputStreamNotSigned.read(buffer)) != -1) {
                            // outputStream.write(buffer, 0, bytesRead); // ghi dữ liệu từ InputStream vào
                            // FileOutputStream
                            // }
                            //
                            // // đóng OutputStream để giải phóng tài nguyên
                            // outputStream.close(); // close cái này thôi
                            // inputStreamNotSigned.close();
                            // } catch (IOException ex) {
                            // System.out.println("Lỗi khi chuyển đổi InputStream sang File: " +
                            // ex.getMessage());
                            // }
                            String sPageSign = "";
                            String baseUrl = "";
                            String relyingParty = "";
                            String relyingPartyUser = "";
                            String relyingPartyPassword = "";
                            String relyingPartySignature = "";
                            String relyingPartyKeyStoreValue = "";
                            String relyingPartyKeyStorePassword = "";
                            String[] sResultConnector = new String[2];
                            String pIdentierConnector = connectDB.getIdentierConnector(sConnectorRequest, sResultConnector);
                            String prefixCode = sResultConnector[1];
                            ObjectMapper objectMapper = new ObjectMapper();
                            COMNECTOR_ATTRIBUTE proParse = objectMapper.readValue(sResultConnector[0], COMNECTOR_ATTRIBUTE.class);
                            for (COMNECTOR_ATTRIBUTE.Attribute attribute : proParse.getAttributes()) {
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_URI)) {
                                    baseUrl = attribute.getValue();
                                }
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_NAME)) {
                                    relyingParty = attribute.getValue();
                                }
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_USERNAME)) {
                                    relyingPartyUser = attribute.getValue();
                                }
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_PASSWORD)) {
                                    relyingPartyPassword = attribute.getValue();
                                }
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_SIGNATURE)) {
                                    relyingPartySignature = attribute.getValue();
                                }
                                if (attribute.getName()
                                        .equals(Difinitions.CONFIG_CONECTOR_RSSP_MOBILE_ID_KEYSTORE_FILE_URL)) {
//                                    relyingPartyKeyStoreValue = attribute.getValue();
                                    relyingPartyKeyStoreValue = "D:/project/file/PAPERLESS.p12";
                                }
                                if (attribute.getName()
                                        .equals(Difinitions.CONFIG_CONECTOR_DMS_MOBILE_ID_KEYSTORE_PASSWORD)) {
                                    relyingPartyKeyStorePassword = attribute.getValue();
                                }
                                if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_DMS_MOBILE_ID_PAGE_SIGN)) {
                                    sPageSign = CommonFunction.CheckTextNull(attribute.getValue());
                                }
                                if (attribute.getName()
                                        .equals(Difinitions.CONFIG_CONECTOR_DMS_MOBILE_ID_URL_SAVEFILE_TEMP)) {
                                    sPathFile = CommonFunction.CheckTextNull(attribute.getValue());
                                }
                            }
                            IServerSession session = rsspService.Handshake_func(baseUrl, relyingParty, relyingPartyUser,
                                    relyingPartyPassword, relyingPartySignature, relyingPartyKeyStoreValue,
                                    relyingPartyKeyStorePassword, lang);
                            log.info("Login xong");

                            log.info("get credentialID");
                            String credentialID = session.listCertificates(codeNumber).get(0).baseCredentialInfo()
                                    .getCredentialID();
                            log.info("get credentialID xong");

                            log.info("get certChain");
                            ICertificate crt = session.certificateInfo(credentialID);
                            BaseCertificateInfo info = crt.baseCredentialInfo();
                            String certChain = info.getCertificates()[0];
                            log.info("get certChain xong");

                            log.info("get documentDigest");
                            String[] sHashList = new String[1];

                            // xac dinh vi tri ky
                            int countSign = 0;
                            Participants[][] rsCountPaticipan = new Participants[1][];
                            connectDB.USP_GW_PPL_WORKFLOW_PARTICIPANTS_LIST(rsCountPaticipan, signingTokenRequest);
                            if (rsCountPaticipan[0] != null && rsCountPaticipan[0].length > 0) {
                                for (int i = 0; i < rsCountPaticipan[0].length; i++) {
                                    if (rsCountPaticipan[0][i].SIGNER_STATUS != Difinitions.CONFIG_WORKFLOW_PARTICIPANTS_SIGNER_STATUS_ID_PENDING) {
                                        countSign = countSign + 1;
                                    }
                                }
                            }
                            if ("".equals(sPageSign)) {
                                sPageSign = "1";
                            }
                            String sReasonSign = CommonFunction.CheckTextNull(rsParticipant[0][0].SIGNING_PURPOSE);
                            if ("".equals(sReasonSign)) {
                                sReasonSign = "Signature";
                            }
                            String sReasonKhung = CommonFunction.CheckTextNull(rsParticipant[0][0].CUSTOM_REASON);
                            if ("".equals(sReasonKhung)) {
                                if (!"".equals(sReasonSign)) {
                                    sReasonKhung = "Purpose: " + sReasonSign;
                                }
                            }
                            System.out.println("sReasonSign: " + sReasonSign);
                            System.out.println("sReasonKhung: " + sReasonKhung);

                            // Chuyển inputstream của file cần ký sang byte[]
                            byte[] pdfData = null;
                            try {
                                pdfData = IOUtils.toByteArray(inputStreamNotSigned);
                            } catch (IOException e) {
                                // xử lý lỗi nếu có
                                e.printStackTrace();
                            }
//

                            int pageHeight = rsspService.calculateHeight(pdfData);
                            System.out.println("height: " + pageHeight);

                            SignPosition signPosition = new SignPosition(sPageSign, countSign, pageHeight);

                            // get user-agent
                            String userAgent = request.getHeader("User-Agent");
                            Parser parser = new Parser();
                            Client c = parser.parse(userAgent);
                            // set app interface
                            String rpName = "{\"OPERATING SYSTEM\":\"" + c.os.family + " " + c.os.major + "\",\"BROWSER\":\"" + c.userAgent.family + " " + c.userAgent.major + "\",\"RP NAME\":\"" + relyingParty + "\"}";

                            String fileType2 = fileName.substring(fileName.lastIndexOf(".") + 1);
                            String message = " {\"FILE NAME\":\"" + fileName + "\", \"FILE TYPE\":\"" + fileType2 + "\"}";

                            MobileDisplayTemplate template = new MobileDisplayTemplate();
                            template.setScaIdentity("PAPERLESS GATEWAY");
                            template.setMessageCaption("DOCUMENT SIGNING");
                            template.setNotificationMessage("PAPERLESS GATEWAY ACTIVITES");
                            template.setMessage(message);
                            template.setRpName(rpName);
                            template.setVcEnabled(true);
                            template.setAcEnabled(true);

                            DocumentDigests doc = rsspService.getDoc(pdfData, credentialID, signPosition,
                                    certChain, sReasonSign, sReasonKhung, sHashList);
                            System.out.println("AAAA: " + sHashList[0]);
                            List<byte[]> list = new ArrayList<>();
                            list.add(base64Decode(sHashList[0]));
                            String codeVC = computeVC(list);
                            vcStoringService.store(requestID, codeVC);
                            System.out.println("VC Code is: " + codeVC);

                            log.info("get sad");
                            String sad = rsspService.authorize(doc, crt, 1, template);
                            // InputStream inStreamSigned = null;
//                            String sFileSigned = sPathFile + RSSPService.generateNumberDays() + "_signed.pdf";
                            String sFileSigned = "D:/project/file/sample_signed.pdf";
                            String[] sSignCertificate = new String[5];
                            Date[] signingTime = new Date[1];
                            Timestamp tsTimeSigned = null;
                            sResult = rsspService.signHashFile(doc, sad, credentialID, crt, sFileSigned,
                                    sSignCertificate, signingTime);
                            System.out.println("signingTime: " + signingTime[0]);
                            if (signingTime != null) {
                                tsTimeSigned = new Timestamp(signingTime[0].getTime());
                                System.out.println("tsTimeSigned: " + tsTimeSigned);
                            }
                            // sResult = rsspService.signHashFile(doc, sad, credentialID, crt,
                            // inStreamSigned, sSignCertificate);
                            System.out.println("sSignCertificate: " + sSignCertificate[0]);

                            // InputStream inStreamSigned = Files.newInputStream(Paths.get(sFileSigned));
                            InputStream inStreamSignedJRB = new FileInputStream(sFileSigned);
                            InputStream inStreamSignedBase = new FileInputStream(sFileSigned);
                            // log filepath of inputstream
                            /*Field field = inStreamSignedJRB.getClass().getDeclaredField("path");
                            field.setAccessible(true);
                            String pathToFile = (String) field.get(inStreamSignedJRB);
                            log.info("pathToFile: " + pathToFile);*/
                            // log.info("file size: ", inStreamSignedJRB.);

                            Path path = new File(fileName_Signed).toPath();
                            String mimeType = Files.probeContentType(path);
                            byte[] base64DecodedBytes = IOUtils.toByteArray(inStreamSignedBase);
                            // byte[] base64DecodedBytesDev = IOUtils.toByteArray(inStreamSignedBase);
                            // System.out.println("base64DecodedBytes: " + base64DecodedBytes.length);
                            String sBase64 = "";
                            try {
                                sBase64 = javax.xml.bind.DatatypeConverter.printBase64Binary(base64DecodedBytes);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            String fileDigest = org.apache.commons.codec.binary.Hex.encodeHexString(
                                    CommonFunction.hashData(base64DecodedBytes, CommonFunction.HASH_SHA256));
                            String sUUID = "";
                            UUID uuid = UUID.randomUUID();
                            String uploadToken = CommonFunction.getCryptoHash(uuid.toString());
                            String fileType = FilenameUtils.getExtension(fileName_Signed);
                            String pHMAC = "";
                            String pCREATED_BY = "";
                            String pURL = "";
                            String SIGNATURE_TYPE = "qes";
                            int enteriprise_id = 0;
                            int[] pFILE_ID = new int[1];
                            PPLFile[][] file = new PPLFile[1][];
                            connectDB.USP_GW_PPL_WORKFLOW_GET_FIRST_FILE(file, signingTokenRequest);
                            if (file != null && file[0].length > 0) {
                                enteriprise_id = file[0][0].ENTERPRISE_ID;
                            }

                            String sInsertFile = connectDB.USP_GW_PPL_FILE_ADD(enteriprise_id, fileName_Signed, base64DecodedBytes.length,
                                    Difinitions.CONFIG_PPL_FILE_STATUS_PENDING,
                                    pURL, fileType, mimeType, fileDigest, sBase64, sUUID, pDMS_PROPERTY, uploadToken,
                                    pHMAC, pCREATED_BY, pFILE_ID);
                            if ("1".equals(sInsertFile)) {
                                // Upload jackrabit
                                JCRFile jcr = FileJRBService.uploadPdf(fileName_Signed, "application/pdf", inStreamSignedJRB,
                                        pDMS_PROPERTY, sFileSigned);
                                if (jcr != null) {
//                                    String signingOption = "";
                                    String sAction = "signer_signed";
                                    String sSigner = CommonFunction.CheckTextNull(rsParticipant[0][0].SIGNER_ID);
                                    String sStatus = "ok";
                                    String sFileSigner = "";
                                    String sFileComplete = "";
                                    String sCountryCode = "vn";
                                    String pSIGNED_TIME = "";
                                    String pSIGNED_ALGORITHM = "";
                                    String sSignatureHash = signerTokenRequest + sSignCertificate[1];
                                    System.out.println("sSignatureHash: " + sSignatureHash);
                                    String sSignature_id = prefixCode + "-" + CommonHash.hashPass(sSignatureHash.getBytes());
                                    sUUID = jcr.getUuid();
                                    try {
                                        byte[] byteSigned = IOUtils.toByteArray(new FileInputStream(sFileSigned));
                                        List<VerifyResult> vrf = PdfProfile.verify(byteSigned, false);
                                        for (VerifyResult veryfy : vrf) {
                                            pSIGNED_TIME = veryfy.getSigningTimes();
                                            pSIGNED_ALGORITHM = veryfy.getAlgorithm();
                                            System.out.println("Time Signed: " + pSIGNED_TIME);
                                            System.out.println("ALGORITHM Signed: " + pSIGNED_ALGORITHM);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                    // update file
                                    String sUpdateFile = connectDB.USP_GW_PPL_FILE_UPDATE(pFILE_ID[0],
                                            Difinitions.CONFIG_PPL_FILE_STATUS_UPLOADED,
                                            "", "", "", "", "", "", "", sUUID, pDMS_PROPERTY, "", "");
                                    connectDB.USP_GW_PPL_WORKFLOW_PARTICIPANTS_UPDATE_STATUS(signerTokenRequest,
                                            Difinitions.CONFIG_WORKFLOW_PARTICIPANTS_SIGNER_STATUS_ID_SIGNED, "");
                                    int pPPL_WORKFLOW_ID = rsWFList[0][0].ID;// sStatusWFCheck[0];
                                    connectDB.USP_GW_PPL_WORKFLOW_FILE_ADD(pPPL_WORKFLOW_ID, pFILE_ID[0], Difinitions.CONFIG_WORKFLOW_FILE_SIGNED_FILE, "", sFileID_Last, "", "");

                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                        String sDateSign = "";
                                        if (tsTimeSigned != null) {
                                            sDateSign = formatter.format(tsTimeSigned);
                                        }
                                        rsWFList = new WorkFlowList[1][];
                                        connectDB.USP_GW_PPL_WORKFLOW_GET(rsWFList, signingTokenRequest);
                                        String protocol = request.getHeader("X-Forwarded-Proto");
                                        if (protocol == null) {
                                            protocol = request.getScheme(); // fallback to default scheme
                                        };
                                        sFileSigner = protocol + "://" + request.getHeader("host") + "/api/signing/"
                                                + signingTokenRequest + "/download/" + sSigner;
                                        String sJsonCertResult = CommonFunction.JsonCertificateObject(sSignCertificate[0],
                                                sType, codeNumber, sDateSign, signingOption, sAction, signingTokenRequest,
                                                sSigner, sStatus, sFileSigner, fileDigest, sSignature_id, sCountryCode);
                                        if (!"".equals(rsWFList[0][0].POSTBACK_URL)) {
                                            CommonFunction.PostBackJsonCertificateObject(rsWFList[0][0].POSTBACK_URL, sSignCertificate[0],
                                                    sType, codeNumber, sDateSign, signingOption, sAction, signingTokenRequest,
                                                    sSigner, sStatus, sFileSigner, fileDigest, sSignature_id, sCountryCode);
                                        }
                                        String signed_Type = "NORMAL";
                                        connectDB.USP_GW_PPL_WORKFLOW_PARTICIPANTS_UPDATE(signerTokenRequest,
                                                signed_Type, tsTimeSigned, sSignature_id, pSIGNED_ALGORITHM, sJsonCertResult, SIGNATURE_TYPE, tab, "");
                                        if (rsWFList[0] != null && rsWFList[0].length > 0) {
                                            if (rsWFList[0][0].WORKFLOW_STATUS != Difinitions.CONFIG_PPL_WORKFLOW_STATUS_PENDING) {
                                                connectDB.USP_GW_PPL_WORKFLOW_UPDATE_STATUS(signingTokenRequest, Difinitions.CONFIG_PPL_WORKFLOW_STATUS_COMPLETED, "");
                                                if (!"".equals(rsWFList[0][0].POSTBACK_URL)) {
                                                    sAction = "signing_completed";
                                                    log.info(
                                                            "link download : " + protocol + "://"
                                                            + request.getHeader("host") + "/api/signing/"
                                                            + signingTokenRequest
                                                            + "/download/");
                                                    sFileComplete = protocol + "://" + request.getHeader("host") + "/api/signing/"
                                                            + signingTokenRequest + "/download/";
                                                    CommonFunction.PostBackJsonObject(rsWFList[0][0].POSTBACK_URL, sSignCertificate[0], sType,
                                                            codeNumber, signingOption, sAction, signingTokenRequest, sSigner, sStatus, sFileComplete, sCountryCode, fileDigest);
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }

                                }
                            }
                        } else {
                            error = true;
                            sResult = "Signer Status invalid";// trangj thais yeu cau ky so kh hop le
                        }
                    } else {
                        error = true;
                        sResult = "Signer Status invalid";// trangj thais yeu cau ky so kh hop le
                    }
                } else {
                    error = true;
                    sResult = "Signing Status invalid";// trangj thais yeu cau ky so kh hop le
                }
            } else {
                error = true;
                sResult = "Signing Status invalid"; //// trangj thais yeu cau ky so kh hop le
            }

            // log.info("Login");
            // String phoneNumber = "CITIZEN-IDENTITY-CARD:079083011315";
            //
            // ClassLoader loader = Thread.currentThread().getContextClassLoader();
            // InputStream inputStream = loader.getResourceAsStream("sample.pdf");
            // IServerSession session = rsspService.Handshake_func();
            // log.info("Login xong");
            //
            // log.info("get credentialID");
            // String credentialID =
            // session.listCertificates(phoneNumber).get(0).baseCredentialInfo().getCredentialID();
            // log.info("get credentialID xong");
            //
            // log.info("get certChain");
            // ICertificate crt = session.certificateInfo(credentialID);
            // BaseCertificateInfo info = crt.baseCredentialInfo();
            // String certChain = info.getCertificates()[0];
            // log.info("get certChain xong");
            //
            // log.info("get documentDigest");
            // String titleSignature = "Write";
            //
            // DocumentDigests doc = rsspService.getDoc(inputStream, credentialID,
            // titleSignature, certChain);
            //
            // log.info("get sad");
            // String sad = rsspService.authorize(doc, crt, 1);
            // String kq = rsspService.signHashFile(doc, sad, credentialID, crt);
            if (error) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sResult);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(sResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } finally {
            vcStoringService.remove(requestID);
        }
    }

    // @PostMapping("/signFile")
    // public String signFile(
    // @RequestParam("username") String usernameRequest,
    // @RequestParam("signingToken") String signingTokenRequest,
    // @RequestParam("password") String passwordRequest,
    // @RequestParam("signerToken") String signerTokenRequest,
    // @RequestPart("file") MultipartFile fileRequest) throws FileNotFoundException,
    // IOException, Exception, Throwable {
    // log.info("signingTokenRequest: {}", signingTokenRequest);
    // process connectDB = new process();
    //
    // log.info("Login");
    //// String username = usernameRequest;
    //// String nameFile = fileRequest.getName();
    //// String password = passwordRequest;
    // MultipartFile pdffile = fileRequest;
    // String phoneNumber = "CITIZEN-IDENTITY-CARD:079083011315";
    //
    // ClassLoader loader = Thread.currentThread().getContextClassLoader();
    // InputStream inputStream = loader.getResourceAsStream("sample.pdf");
    //
    //// File f = new File("file\\test.pdf");
    //// InputStream inputStream = new FileInputStream(f);
    //
    // IServerSession session = rsspService.Handshake_func();
    // log.info("Login xong");
    //
    // log.info("get credentialID");
    // String credentialID =
    // session.listCertificates(phoneNumber).get(0).baseCredentialInfo().getCredentialID();
    // log.info("get credentialID xong");
    //
    // log.info("get certChain");
    // ICertificate crt = session.certificateInfo(credentialID);
    // BaseCertificateInfo info = crt.baseCredentialInfo();
    // String certChain = info.getCertificates()[0];
    // log.info("get certChain xong");
    //
    // log.info("get documentDigest");
    // String titleSignature = "Write";
    //
    // DocumentDigests doc = rsspService.getDoc(inputStream, credentialID,
    // titleSignature, certChain);
    //
    // log.info("get sad");
    // String sad = rsspService.authorize(doc, crt, 1);
    // String kq = rsspService.signHashFile(doc, sad, credentialID, crt);
    // return kq;
    //
    // }
}
