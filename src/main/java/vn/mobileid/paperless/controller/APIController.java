/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.paperless.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import java.util.HashMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vn.mobileid.paperless.object.PPLFile;
import vn.mobileid.paperless.object.Participants;
import vn.mobileid.paperless.object.ConnectorName;
import vn.mobileid.paperless.process.process;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.web.bind.annotation.RequestParam;

import vn.mobileid.fms.client.JCRException;
import vn.mobileid.fms.client.JCRFile;
import vn.mobileid.paperless.object.BATCH;
import vn.mobileid.paperless.object.COMNECTOR_ATTRIBUTE;
import vn.mobileid.paperless.object.CertificateJson;
import vn.mobileid.paperless.object.ENTERPRISE;
import vn.mobileid.paperless.object.PREFIX_UID;

import vn.mobileid.paperless.service.FileJRBService;
import vn.mobileid.paperless.service.VCStoringService;
import vn.mobileid.paperless.utils.Difinitions;
import vn.mobileid.paperless.utils.CommonFunction;

/**
 *
 * @author PHY
 */
@RestController
public class APIController {

    public static String getSigningtoken;
    public static String value;
    public static int test;
    @Autowired
    private VCStoringService vcStoringService;

    @RequestMapping(value = {"/getVC"}, method = RequestMethod.GET)
    public String getVC(@RequestParam String requestID) {
        Long startTime = System.currentTimeMillis();
        try {
            while (true) {
                String VC = vcStoringService.get(requestID);
                if (VC != null) {
                    vcStoringService.remove(requestID);
                    return VC;
                } else {
                    Long endTime = System.currentTimeMillis();
                    if (endTime - startTime > 60000) {
                        return VC;
                    }
                    Thread.sleep(5000);
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = {"/checkPersonalCode"}, method = RequestMethod.POST)
    public static void checkPersonalCode(@RequestBody Map<String, String> PersonalCode) {
        System.out.println(PersonalCode);
    }

    @RequestMapping(value = {"/getSigningOption"}, method = RequestMethod.POST)
    public static ArrayList getSigningOption(@RequestBody Map<String, String> signerToken) throws Exception {
        process conect = new process();
        String signing_option = "";
        int signer_status = 0;
        String certificate = "";
        ArrayList list = new ArrayList();
        String abc = "42ae1827bad34399e2a1cd2480569dfc5a188aa5";
        Participants[][] rsFile = new Participants[1][];
        String pSIGNER_TOKEN = signerToken.get("signerToken");
        // System.out.println("signerToken:" + signerToken);
        conect.USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(rsFile, pSIGNER_TOKEN);
        if (rsFile != null && rsFile[0].length > 0) {
            signing_option = rsFile[0][0].SIGNING_OPTIONS;
            signer_status = rsFile[0][0].SIGNER_STATUS;

            list.add(signing_option);
            list.add(signer_status);
        }
        return list;
    }

    @RequestMapping(value = {"/getPrefix"}, method = RequestMethod.POST)
    public static ArrayList getPrefix(@RequestBody Map<String, String> language) throws Exception {
        String sType = Difinitions.CONFIG_PREFIX_UID_PERSONAL_ID;
        String lang = language.get("lang");
        process conect = new process();
        ArrayList list = new ArrayList();
        PREFIX_UID[][] rsFile = new PREFIX_UID[1][];
        conect.USP_GW_PREFIX_PERSONAL_CODE_LIST(rsFile, sType, lang);
        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> item = new HashMap<>();
                item.put("prefix", rsFile[0][i].NAME);
                item.put("type", rsFile[0][i].TYPE);
                item.put("remark", rsFile[0][i].REMARK);
                list.add(item);
            }

        }
        return list;

    }

    @RequestMapping(value = {"/getPrefixPhone"}, method = RequestMethod.POST)
    public static ArrayList getPrefixPhone(@RequestBody Map<String, String> language) throws Exception {
        String sType = Difinitions.CONFIG_PREFIX_UID_PHONE_ID;
        String lang = language.get("lang");
        process conect = new process();
        ArrayList list = new ArrayList();
        PREFIX_UID[][] rsFile = new PREFIX_UID[1][];
        conect.USP_GW_PREFIX_PERSONAL_CODE_LIST(rsFile, sType, lang);
        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> item = new HashMap<>();
                item.put("prefix", rsFile[0][i].NAME);
                item.put("type", rsFile[0][i].TYPE);
                item.put("remark", rsFile[0][i].REMARK);
                list.add(item);
            }

        }
        return list;

    }

    @RequestMapping(value = {"/base64Logo"}, method = RequestMethod.POST)
    public static ArrayList<Object> base64Logo(@RequestBody Map<String, String> pPROVIDER) throws Exception {
        process conect = new process();
        String connector_name;
        String logo;
        int a = 0;
        ConnectorName[][] rsFile = new ConnectorName[1][];
        String pPROVIDERs = pPROVIDER.get("param");
        System.out.println("pPROVIDERs" + pPROVIDERs);
        ArrayList<Object> list = new ArrayList<Object>();
        conect.USP_GW_CONNECTOR_GET_FROM_PROVIDER(rsFile, pPROVIDERs);

        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> map = new HashMap();
                map.put("connector_name", rsFile[0][i].CONNECTOR_NAME);
                map.put("logo", rsFile[0][i].LOGO);
                map.put("remark", rsFile[0][i].REMARK);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/usbBatch"}, method = RequestMethod.GET)
    public static ArrayList<Object> usbBatch() throws Exception {
        process conect = new process();
        String connector_name;
        String logo;
        int a = 0;
        ConnectorName[][] rsFile = new ConnectorName[1][];
        String pPROVIDERs = "USB_TOKEN_SIGNING";
        System.out.println("pPROVIDERs" + pPROVIDERs);
        ArrayList<Object> list = new ArrayList<Object>();
        conect.USP_GW_CONNECTOR_GET_FROM_PROVIDER(rsFile, pPROVIDERs);

        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> map = new HashMap();
                map.put("connector_name", rsFile[0][i].CONNECTOR_NAME);
                map.put("logo", rsFile[0][i].LOGO);
                map.put("remark", rsFile[0][i].REMARK);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/getTabSignBatch"}, method = RequestMethod.POST)
    public static ArrayList<Object> getTabSignBatch(@RequestBody Map<String, String> batchToken) throws Exception {
        process conect = new process();

        int a = 0;
        String signing_option = "";
        int signer_status = 0;
        String sSignerToken = "";
//        ConnectorName[][] rsFile = new ConnectorName[1][];

        String pUPLOAD_TOKEN = batchToken.get("file_token");
        BATCH[][] batch = new BATCH[1][];
        ArrayList<Object> list = new ArrayList<Object>();
        conect.USP_GW_PPL_BATCH_FILE_GET_WORKFLOW(batch, pUPLOAD_TOKEN);
        if (batch != null && batch[0].length > 0) {
            sSignerToken = batch[0][0].SIGNER_TOKEN;
        }
        Participants[][] participant = new Participants[1][];
        //String pSIGNER_TOKEN = signerToken.get("signerToken");
        // System.out.println("signerToken:" + signerToken);
        conect.USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(participant, batch[0][0].SIGNER_TOKEN);
        if (participant != null && participant[0].length > 0) {
            for (int i = 0; i < participant[0].length; i++) {
                Map<String, String> map = new HashMap();
                String signerStatus = String.valueOf(participant[0][i].SIGNER_STATUS);
                map.put("signing_option", participant[0][0].SIGNING_OPTIONS);
                map.put("signer_status", signerStatus);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/fileOpen"}, method = RequestMethod.POST)
    public static ArrayList<Object> fileOpen(@RequestBody Map<String, String> fileToken) throws Exception {
        process conect = new process();
        PPLFile[][] object = new PPLFile[1][];
        byte[] bytes = null;
        String base64Encoded = null;
        String sUUID = "";
        int fileSize = 0;
        String sFileSize = "";
        ArrayList<Object> list = new ArrayList<Object>();
//        String pUPLOAD_TOKEN ="354ec26a82f7865db4654fd1f43d87a19a676970";
        String pUPLOAD_TOKEN = fileToken.get("file_token");
        conect.USP_GW_PPL_FILE_GET(object, pUPLOAD_TOKEN);
        String intt = null;
        if (object.length > 0) {
            for (int i = 0; i < object.length; i++) {
                intt = object[0][i].FILE_NAME;
                sUUID = object[0][i].FILE_UUID;

                String sPropertiesFMS = FileJRBService.getPropertiesFMS();
                JCRFile jrbFile = FileJRBService.downloadFMS(sUUID, sPropertiesFMS);
                if (jrbFile != null) {
                    InputStream streamFile = jrbFile.getStream();
                    bytes = IOUtils.toByteArray(streamFile);
                    base64Encoded = Base64.getEncoder().encodeToString(bytes);
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
                    fileSize = decodedBytes.length;
                    sFileSize = Integer.toString(fileSize);
                }
                Map<String, String> map = new HashMap();
                map.put("fileName", intt);
                map.put("base64", base64Encoded);
                map.put("filesize", sFileSize);
                list.add(map);
            }
        }
        return list;
    }

    @RequestMapping(value = {"/headerFooter"}, method = RequestMethod.POST)
    public static ArrayList<Object> headerFooter(@RequestBody Map<String, String> signingToken) throws Exception {
        process conect = new process();
        String connector_name;
//        String logo;
        int a = 0;
//        String value = "";
        ENTERPRISE[][] rsFile = new ENTERPRISE[1][];
        //String pPROVIDERs = pPROVIDER.get("param");
        // System.out.println("pPROVIDERs" + pPROVIDERs);
        ArrayList<Object> list = new ArrayList<Object>();
        String Signing_Token = signingToken.get("signingToken");
        int enteriprise_id = 0;
        PPLFile[][] file = new PPLFile[1][];
        conect.USP_GW_PPL_WORKFLOW_GET_FIRST_FILE(file, Signing_Token);
        if (file != null && file[0].length > 0) {
            enteriprise_id = file[0][0].ENTERPRISE_ID;
        }
        System.out.println("ccccc:" + enteriprise_id);
        conect.USP_GW_ENTERPRISE_INFO_GET(rsFile, file[0][0].ENTERPRISE_ID, null);

        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> map = new HashMap();
                // map.put("ENTERPRISE", rsFile[0][i].ENTERPRISE);
                String value = rsFile[0][i].METADATA_GATEWAY_VIEW;
                String logo = rsFile[0][i].LOGO;
                map.put("value", value);
                map.put("logo", logo);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/headerFooterOpen"}, method = RequestMethod.POST)
    public static ArrayList<Object> headerFooterOpen(@RequestBody Map<String, String> uploadToken) throws Exception {
        process conect = new process();
        String connector_name;
//        String logo;
        int a = 0;
//        String value = "";
        ENTERPRISE[][] rsFile = new ENTERPRISE[1][];
        //String pPROVIDERs = pPROVIDER.get("param");
        // System.out.println("pPROVIDERs" + pPROVIDERs);
        ArrayList<Object> list = new ArrayList<Object>();
        String upload_Token = uploadToken.get("file_token");
        int enteriprise_id = 0;
        PPLFile[][] file = new PPLFile[1][];
        conect.USP_GW_PPL_FILE_GET(file, upload_Token);
        if (file != null && file[0].length > 0) {
            enteriprise_id = file[0][0].ENTERPRISE_ID;
        }
        System.out.println("ccccc:" + enteriprise_id);
        conect.USP_GW_ENTERPRISE_INFO_GET(rsFile, file[0][0].ENTERPRISE_ID, null);

        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> map = new HashMap();
                // map.put("ENTERPRISE", rsFile[0][i].ENTERPRISE);
                String value = rsFile[0][i].METADATA_GATEWAY_VIEW;
                String logo = rsFile[0][i].LOGO;
                map.put("value", value);
                map.put("logo", logo);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/headerfooterBatch"}, method = RequestMethod.POST)
    public static ArrayList<Object> headerfooterBatch(@RequestBody Map<String, String> batchToken) throws Exception {
        process conect = new process();
        String pBATCH_FILE_TOKEN = batchToken.get("batch_token");
        ENTERPRISE[][] rsFile = new ENTERPRISE[1][];
        //String pPROVIDERs = pPROVIDER.get("param");
        // System.out.println("pPROVIDERs" + pPROVIDERs);
        ArrayList<Object> list = new ArrayList<Object>();
        conect.USP_GW_PPL_BATCH_FILE_GET_ENTERPRISE_INFO(rsFile, pBATCH_FILE_TOKEN);

        if (rsFile != null && rsFile[0].length > 0) {
            for (int i = 0; i < rsFile[0].length; i++) {
                Map<String, String> map = new HashMap();
                // map.put("ENTERPRISE", rsFile[0][i].ENTERPRISE);
                String value = rsFile[0][i].METADATA_GATEWAY_VIEW;
                String logo = rsFile[0][i].LOGO;
                map.put("value", value);
                map.put("logo", logo);
                list.add(map);

            }
        }

        return list;
    }

    @RequestMapping(value = {"/getSigning"}, method = RequestMethod.POST)
    public static ArrayList<BATCH> getSigning(@RequestBody Map<String, String> signingToken) throws Exception {
        process conect = new process();
//        String signingToken = "";
        int id = 0;
        String sID = "";
        String lastName = "";
        byte[] bytes = null;
        String base64Encoded = null;
        int fileSize = 0;
        String sFileSize = "";
        BATCH[][] rsFile = new BATCH[1][];
//        String pBATCH_FILE_TOKEN = "835ba7fe57f09636e82cdc84f00a8df82148a0b2e5ed8b4d92653353b2047798382229156766f68a2b49b11bed862a85219d619e3bfc9a8a2724b6242275dfed";
        String pBATCH_FILE_TOKEN = signingToken.get("batch_token");
        System.out.println("bdfjd:" + pBATCH_FILE_TOKEN);
        conect.USP_GW_PPL_BATCH_FILE_GET_WORKFLOW(rsFile, pBATCH_FILE_TOKEN);
        if (rsFile != null && rsFile[0].length > 0) {
            ArrayList<Participants> listParticipants = new ArrayList<Participants>();
            for (int i = 0; i < rsFile[0].length; i++) {
                Participants[][] objectParticipants = new Participants[1][];
                PPLFile[][] objectPPLFile = new PPLFile[1][];

                conect.USP_GW_PPL_WORKFLOW_PARTICIPANTS_LIST(objectParticipants, rsFile[0][i].SIGNING_TOKEN);

                conect.USP_GW_PPL_WORKFLOW_GET_FIRST_FILE(objectPPLFile, rsFile[0][i].SIGNING_TOKEN);
                if (objectPPLFile != null && objectPPLFile[0].length > 0) {
                    String sUUID = objectPPLFile[0][0].FILE_UUID;
                    String sPropertiesFMS = FileJRBService.getPropertiesFMS();
                    JCRFile jrbFile = FileJRBService.downloadFMS(sUUID, sPropertiesFMS);
                    if (jrbFile != null) {
                        InputStream streamFile = jrbFile.getStream();
                        bytes = IOUtils.toByteArray(streamFile);
                        base64Encoded = Base64.getEncoder().encodeToString(bytes);
                        byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
                        fileSize = decodedBytes.length;
                        sFileSize = Integer.toString(fileSize);
                        rsFile[0][i].BASE64 = base64Encoded;
                        rsFile[0][i].fileName = jrbFile.getFileName();
                        rsFile[0][i].FILENAME = jrbFile.getFileName();
                        rsFile[0][i].FILESIZE = sFileSize;

                    }

                }
                if (objectParticipants != null && objectParticipants[0].length > 0) {
                    rsFile[0][i].participants = objectParticipants[0];

                }
            }

        }
        ArrayList<BATCH> tempList;
        tempList = new ArrayList<>();
        tempList.addAll(Arrays.asList(rsFile[0]));
        return tempList;
    }

    @RequestMapping(value = {"/abc"}, method = RequestMethod.POST)
    public static ArrayList<BATCH> abc(@RequestBody Map<String, String> batchToken) throws Exception {
        process conect = new process();

        String pUPLOAD_TOKEN = batchToken.get("file_token");
        BATCH[][] batch = new BATCH[1][];
        ArrayList<Object> list = new ArrayList<Object>();
        conect.USP_GW_PPL_BATCH_FILE_GET_WORKFLOW(batch, pUPLOAD_TOKEN);
        if (batch != null && batch[0].length > 0) {
            for (int i = 0; i < batch[0].length; i++) {
                Participants[][] objectParticipants = new Participants[1][];
                conect.USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(objectParticipants, batch[0][i].SIGNER_TOKEN);

                if (objectParticipants != null && objectParticipants[0].length > 0) {
                    batch[0][i].participants = objectParticipants[0];

                }
            }
        }

        ArrayList<BATCH> tempList;
        tempList = new ArrayList<>();
        tempList.addAll(Arrays.asList(batch[0]));
        return tempList;
    }

    @RequestMapping(value = {"/showFile"}, method = RequestMethod.POST)
    public static String showFile(@RequestBody Map<String, String> signingToken)
            throws JCRException, SQLException, Exception {
        process conect = new process();
        getSigningtoken = signingToken.get("signingToken");
        PPLFile[][] rsFile = new PPLFile[1][];
        // String pSIGNING_TOKEN = ;
        conect.USP_GW_PPL_WORKFLOW_GET_FIRST_FILE(rsFile, getSigningtoken);
        String sUUID = "";
        String sFileName = "";
        int fileSize = 0;
        int enteriprise_id = 0;
        String sFileSize = "";
        if (rsFile != null && rsFile[0].length > 0) {
            sUUID = rsFile[0][0].FILE_UUID;
            sFileName = rsFile[0][0].FILE_NAME;
            enteriprise_id = rsFile[0][0].ENTERPRISE_ID;
            test = rsFile[0][0].ENTERPRISE_ID;
        }
        System.out.println("gia tri uuid: " + sUUID);
        String sPropertiesFMS = FileJRBService.getPropertiesFMS();
        JCRFile jrbFile = FileJRBService.downloadFMS(sUUID, sPropertiesFMS);

        byte[] bytes = null;
        String base64Encoded = null;
        if (jrbFile.getStream() != null) {
            bytes = IOUtils.toByteArray(jrbFile.getStream());
            base64Encoded = Base64.getEncoder().encodeToString(bytes);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
            fileSize = decodedBytes.length;
            sFileSize = Integer.toString(fileSize);
        }
        Map<String, Object> json = new HashMap<>();
        json.put("name", jrbFile.getFileName());
        json.put("mimeType", jrbFile.getMimeType());
        json.put("filePath", jrbFile.getFilePath());
        json.put("base64Encoded", base64Encoded);
        json.put("fileName", sFileName);
        json.put("fileSize", sFileSize);
        json.put("enteriprise_id", enteriprise_id);

        return new ObjectMapper().writeValueAsString(json);

    }

    @RequestMapping(value = {"/participants"}, method = RequestMethod.GET)
    public static ArrayList<Object> getParticipants() throws Exception {
        process conect = new process();
        String pSIGNING_TOKEN = getSigningtoken;
        System.out.println("sbjhbs;" + pSIGNING_TOKEN);
        ArrayList<Object> intt = new ArrayList<Object>();
        Participants[][] rsParticipant = new Participants[1][];
        ObjectMapper oMapperParse = null;
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        conect.USP_GW_PPL_WORKFLOW_PARTICIPANTS_LIST(rsParticipant, pSIGNING_TOKEN);
        if (rsParticipant != null && rsParticipant[0].length > 0) {
            for (int j = 0; j < rsParticipant[0].length; j++) {
                Map<String, String> object = new HashMap();
                Map<String, Object> map = new HashMap();
                map.put("FIRST_NAME", rsParticipant[0][j].FIRST_NAME);
                map.put("LAST_NAME", rsParticipant[0][j].LAST_NAME);
                map.put("SIGNER_TOKEN", rsParticipant[0][j].SIGNER_TOKEN);
                map.put("SIGNER_STATUS", rsParticipant[0][j].SIGNER_STATUS);
                map.put("META_INFORMATION", rsParticipant[0][j].META_INFORMATION);
                map.put("SIGNING_PURPOSE", rsParticipant[0][j].SIGNING_PURPOSE);
                map.put("SIGNED_TYPE", rsParticipant[0][j].SIGNED_TYPE);
                //map.put("CERTIFICATE", rsParticipant[0][j].CERTIFICATE);
                System.out.println("rsParticipant[0][j].SIGNER_STATUS : " + rsParticipant[0][j].SIGNER_STATUS);
                if (rsParticipant[0][j].SIGNER_STATUS != Difinitions.CONFIG_WORKFLOW_PARTICIPANTS_SIGNER_STATUS_ID_PENDING) {
                    String sDateSign = "";
                    String sIssue = "";
                    String sOwner = "";
                    String sFrom = "";
                    String sTo = "";
                    if (rsParticipant[0][j].SIGNED_TIME != null) {
                        sDateSign = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(rsParticipant[0][j].SIGNED_TIME);
                    }
                    String sCertificate = CommonFunction.CheckTextNull(rsParticipant[0][j].CERTIFICATE);
                    if (!"".equals(sCertificate)) {
                        oMapperParse = new ObjectMapper();
                        CertificateJson itemParse = oMapperParse.readValue(sCertificate, CertificateJson.class);
                        if (itemParse != null) {
                            sIssue = CommonFunction.CheckTextNull(itemParse.signer_info.certificate.issuer);
                            sOwner = CommonFunction.CheckTextNull(itemParse.signer_info.certificate.subject);
                            sFrom = itemParse.signer_info.certificate.valid_from;
                            sTo = itemParse.signer_info.certificate.valid_to;
                        }
                    }
                    if (!"".equals(sIssue)) {
                        sIssue = CommonFunction.getCommonNameInDN(sIssue);
                    }
                    if (!"".equals(sOwner)) {
                        sOwner = CommonFunction.getCommonNameInDN(sOwner);
                    }
                    object.put("Signing_date", sDateSign);
                    object.put("isuer", sIssue);
                    object.put("owner", sOwner);
                    object.put("from", sFrom);
                    object.put("to", sTo);
                    map.put("cer", object);
                } else {
                    object.put("Signing_date", "");
                    object.put("Signing_time", "");
                    object.put("isuer", "");
                    object.put("owner", "");
                    object.put("from", "");
                    object.put("to", "");
                    map.put("cer", object);
                }
                System.out.println(map);
                intt.add(map);
                System.out.println("i:" + intt);
            }
        }

        return intt;
    }

    // get infomation connect rssp
    @RequestMapping(value = {"/getInfomationConnectRssp"}, method = RequestMethod.POST)
    public static String infomationConnectRssp(@RequestBody Map<String, String> providerName)
            throws JCRException, SQLException, Exception {
        byte[] bytes = null;
        String encoded = null;
        String baseUrl = "";
        String relyingParty = "";
        String relyingPartyUser = "";
        String relyingPartyPassword = "";
        String relyingPartySignature = "";
        String relyingPartyKeyStore = "";
        String relyingPartyKeyStorePassword = "";
        process conect = new process();
        ConnectorName[][] object = new ConnectorName[1][];
        conect.USP_GW_CONNECTOR_GET(object, providerName.get("Provider"));
        String sPropertiesFMS = "";
        if (object.length > 0 && object != null) {
            for (int i = 0; i < object.length; i++) {
                sPropertiesFMS = object[0][i].IDENTIFIER;
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        COMNECTOR_ATTRIBUTE proParse = objectMapper.readValue(sPropertiesFMS, COMNECTOR_ATTRIBUTE.class);
        for (COMNECTOR_ATTRIBUTE.Attribute attribute : proParse.getAttributes()) {
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_URI)) {
                baseUrl = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_NAME)) {
                relyingParty = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_USERNAME)) {
                relyingPartyUser = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_PASSWORD)) {
                relyingPartyPassword = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_SIGNATURE)) {
                relyingPartySignature = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_KEYSTORE_FILE_URL)) {
                relyingPartyKeyStore = attribute.getValue();
            }
            if (attribute.getName().equals(Difinitions.CONFIG_CONECTOR_SMART_ID_MOBILE_ID_KEYSTORE_PASSWORD)) {
                relyingPartyKeyStorePassword = attribute.getValue();
            }
        }
        // viết thêm các câu lệnh để sử dụng cấu hình:

        return encoded; //
    }

}
