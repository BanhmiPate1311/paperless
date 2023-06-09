/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.paperless.process;

import static com.sun.corba.se.spi.presentation.rmi.StubAdapter.request;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import vn.mobileid.paperless.object.BATCH;
import vn.mobileid.paperless.object.ConnectorName;
import vn.mobileid.paperless.object.ENTERPRISE;
import vn.mobileid.paperless.object.FileList;
import vn.mobileid.paperless.object.PPLFile;
import vn.mobileid.paperless.object.PREFIX_UID;
import vn.mobileid.paperless.object.Participants;
import vn.mobileid.paperless.object.WorkFlowList;

import vn.mobileid.paperless.controller.APIController;
import vn.mobileid.paperless.utils.CommonFunction;
import vn.mobileid.paperless.utils.Difinitions;

/**
 *
 * @author PHY
 */
public class process {

    public Connection OpenDatabase() throws Exception {
        String Driver_Sql = "com.mysql.jdbc.Driver";
        String Url_Sql = "jdbc:mysql:loadbalance://192.168.2.250:3306/PAPERLESS_UAT?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false";
        String Username_SQL = "webview";
        String Password_SQL = "T@mic@8x";
        String DBConnect_Timeout = "120";
        Class.forName(Driver_Sql);
        DriverManager.setLoginTimeout(Integer.parseInt(DBConnect_Timeout));
        Connection connInner = DriverManager.getConnection(Url_Sql, Username_SQL, Password_SQL);
        return connInner;
    }

    public void CloseDatabase(Connection[] temp) throws Exception {
        if (temp[0] != null) {
            temp[0].close();
        }
    }

    public void USP_GW_PPL_FILE_GET(PPLFile[][] response, String pUPLOAD_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<PPLFile> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_FILE_GET(?,?) }");
            proc_stmt.setString(1, pUPLOAD_TOKEN);

            proc_stmt.registerOutParameter(2, java.sql.Types.INTEGER);
            proc_stmt.execute();
            convrtr = String.valueOf(proc_stmt.getInt(2));

            System.out.println("USP_PPL_FILE_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                PPLFile tempItem = new PPLFile();
                tempItem.ID = rs.getInt("ID");
                tempItem.FILE_NAME = rs.getString("FILE_NAME");
                tempItem.FILE_UUID = rs.getString("FILE_UUID");
                tempItem.ENTERPRISE_ID = rs.getInt("ENTERPRISE_ID");

                tempList.add(tempItem);
            }
            response[0] = new PPLFile[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }


    public void USP_GW_ENTERPRISE_INFO_GET(ENTERPRISE[][] response, int pENTERPRISE_ID, String NAME) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<ENTERPRISE> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_ENTERPRISE_INFO_GET(?,?,?) }");
            proc_stmt.setInt(1, pENTERPRISE_ID);
            if (!"".equals(NAME)) {
                proc_stmt.setString(2, NAME);
            } else {
                proc_stmt.setString(2, null);
            }

            proc_stmt.registerOutParameter(3, java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString(3);

            System.out.println("USP_ENTERPRISE_INFO_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                ENTERPRISE tempItem = new ENTERPRISE();
                tempItem.METADATA_GATEWAY_VIEW = rs.getString("METADATA_GATEWAY_VIEW");
                tempItem.LOGO = rs.getString("LOGO");

                tempList.add(tempItem);
            }
            response[0] = new ENTERPRISE[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    public void USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET(Participants[][] response, String pSIGNER_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<Participants> tempList = new ArrayList<>();
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_PARTICIPANTS_GET(?,?) }");
            proc_stmt.setString(1, pSIGNER_TOKEN);

            proc_stmt.registerOutParameter(2, java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString(2);

            System.out.println("USP_PPL_WORKFLOW_PARTICIPANTS_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                Participants tempItem = new Participants();
                tempItem.ID = rs.getInt("ID");
                tempItem.PPL_WORKFLOW_ID = rs.getInt("PPL_WORKFLOW_ID");
                tempItem.FIRST_NAME = rs.getString("FIRST_NAME");
                tempItem.LAST_NAME = rs.getString("LAST_NAME");
                tempItem.SIGNER_STATUS = rs.getInt("SIGNER_STATUS");
                tempItem.CERTIFICATE = rs.getString("CERTIFICATE");
                tempItem.SIGNING_OPTIONS = rs.getString("SIGNING_OPTIONS");
                tempItem.SIGNER_ID = rs.getString("SIGNER_ID");
                tempItem.CUSTOM_REASON = rs.getString("CUSTOM_REASON");
                tempItem.SIGNING_PURPOSE = rs.getString("SIGNING_PURPOSE");
                tempList.add(tempItem);
            }
            response[0] = new Participants[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    // Insert file PDF
    public static String getIDENTIFIER() throws Exception {
        process conect = new process();
        ConnectorName[][] object = new ConnectorName[1][];
        String pCONNECTOR_NAME = Difinitions.CONFIG_CONECTOR_DMS_MOBILE_ID;
        conect.USP_GW_CONNECTOR_GET(object, pCONNECTOR_NAME);
        String intt = null;
        System.out.println(object.length);
        if (object.length > 0) {
            for (int i = 0; i < object.length; i++) {
                System.out.println(object[0][i].IDENTIFIER);
                return intt = object[0][i].IDENTIFIER;
            }
        }
        return intt;
    }

    public String USP_GW_PPL_FILE_ADD(int pENTERPRISE_ID, String pFILE_NAME, int pFILE_SIZE, int pFILE_STATUS, String pURL, String pFILE_TYPE,
                                      String pMIME_TYPE,
                                      String pDIGEST, String pCONTENT, String pFILE_UUID, String pDMS_PROPERTY, String pUPLOAD_TOKEN,
                                      String pHMAC,
                                      String pCREATED_BY, int[] pFILE_ID) throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_FILE_ADD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }");

            proc_stmt.setInt("pENTERPRISE_ID", pENTERPRISE_ID); // pENTERPRISE_ID
            proc_stmt.setString("pFILE_NAME", pFILE_NAME); // file name
            // file size
            proc_stmt.setInt("pFILE_SIZE", pFILE_SIZE);

            // file status
            proc_stmt.setInt("pFILE_STATUS", pFILE_STATUS);

            // file URL
            if (!"".equals(pURL)) {
                proc_stmt.setString("pURL", pURL);
            } else {
                proc_stmt.setString("pURL", null);
            }

            // file type
            proc_stmt.setString("pFILE_TYPE", pFILE_TYPE);

            // file mime type
            proc_stmt.setString("pMIME_TYPE", pMIME_TYPE);

            proc_stmt.setString("pDIGEST", pDIGEST); // file digest

            // file content
            if (!"".equals(pCONTENT)) {
                proc_stmt.setString("pCONTENT", pCONTENT);
            } else {
                proc_stmt.setString("pCONTENT", null);
            }

            // uuid file
            proc_stmt.setString("pFILE_UUID", pFILE_UUID);

            // file DMS property
            proc_stmt.setString("pDMS_PROPERTY", pDMS_PROPERTY);

            // upload token
            proc_stmt.setString("pUPLOAD_TOKEN", pUPLOAD_TOKEN);

            // hmac
            if (!"".equals(pHMAC)) {
                proc_stmt.setString("pHMAC", pHMAC);
            } else {
                proc_stmt.setString("pHMAC", null);
            }
            // create by
            if (!"".equals(pCREATED_BY)) {
                proc_stmt.setString("pCREATED_BY", pCREATED_BY);
            } else {
                proc_stmt.setString("pCREATED_BY", null);
            }

            proc_stmt.registerOutParameter("pFILE_ID", java.sql.Types.BIGINT); // giá trị out file_id
            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR); // giá trị out response code

            System.out.println("USP_PPL_FILE_ADD: " + proc_stmt); // kiem tra
            proc_stmt.execute();
            pFILE_ID[0] = proc_stmt.getInt("pFILE_ID");
            convrtr = String.valueOf(proc_stmt.getInt("pRESPONSE_CODE"));
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    public void USP_GW_CONNECTOR_GET(ConnectorName[][] response, String pCONNECTOR_NAME) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<ConnectorName> tempList = new ArrayList<>();
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_CONNECTOR_GET(?,?) }");
            proc_stmt.setString(1, pCONNECTOR_NAME);

            proc_stmt.registerOutParameter(2, java.sql.Types.INTEGER);
            proc_stmt.execute();
            convrtr = String.valueOf(proc_stmt.getInt(2));

            System.out.println("USP_CONNECTOR_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                ConnectorName tempItem = new ConnectorName();
                tempItem.IDENTIFIER = CommonFunction.CheckTextNull(rs.getString("IDENTIFIER"));
                tempItem.PREFIX_CODE = CommonFunction.CheckTextNull(rs.getString("PREFIX_CODE"));
                // tempItem.LOGO = rs.getString("LOGO");

                tempList.add(tempItem);
            }
            response[0] = new ConnectorName[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }
    public void USP_GW_PPL_BATCH_FILE_GET_ENTERPRISE_INFO(ENTERPRISE[][] response, String pBATCH_FILE_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<ENTERPRISE> tempList = new ArrayList<>();
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_BATCH_FILE_GET_ENTERPRISE_INFO(?,?) }");
            proc_stmt.setString(1, pBATCH_FILE_TOKEN);

            proc_stmt.registerOutParameter(2, java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString(2);

//            System.out.println("USP_CONNECTOR_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                ENTERPRISE tempItem = new ENTERPRISE();
                tempItem.LOGO = rs.getString("LOGO");
                tempItem.METADATA_GATEWAY_VIEW = rs.getString("METADATA_GATEWAY_VIEW");

                tempList.add(tempItem);
            }
            response[0] = new ENTERPRISE[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    // update signer status
    public String USP_GW_PPL_WORKFLOW_PARTICIPANTS_UPDATE_STATUS(String pSIGNER_TOKEN, int pSIGNER_STATUS, String pLAST_MODIFIED_BY)
            throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_PARTICIPANTS_UPDATE_STATUS(?,?,?,?) }");

            proc_stmt.setString("pSIGNER_TOKEN", pSIGNER_TOKEN);
            proc_stmt.setInt("pSIGNER_STATUS", pSIGNER_STATUS);
            proc_stmt.setString("pLAST_MODIFIED_BY", pLAST_MODIFIED_BY);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            System.out.println("USP_PPL_WORKFLOW_PARTICIPANTS_UPDATE_STATUS: " + proc_stmt.toString());
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    // update ppl file
    public String USP_GW_PPL_FILE_UPDATE(int pFILE_ID, int pFILE_STATUS, String pFILE_NAME,
                                         String pFILE_SIZE, String pURL, String pFILE_TYPE, String pMIME_TYPE, String pDIGEST,
                                         String pCONTENT, String pFILE_UUID, String pDMS_PROPERTY, String pHMAC,
                                         String pLAST_MOTIFIED_BY) throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_FILE_UPDATE(?,?,?,?,?,?,?,?,?,?,?,?,?,?) }");

            proc_stmt.setInt("pFILE_ID", pFILE_ID);
            proc_stmt.setInt("pFILE_STATUS", pFILE_STATUS);

            if (!"".equals(pFILE_NAME)) {
                proc_stmt.setString("pFILE_NAME", pFILE_NAME);
            } else {
                proc_stmt.setString("pFILE_NAME", null);
            }

            if (!"".equals(pFILE_SIZE)) {
                proc_stmt.setInt("pFILE_SIZE", Integer.parseInt(pFILE_SIZE));
            } else {
                proc_stmt.setString("pFILE_SIZE", null);
            }
            if (!"".equals(pURL)) {
                proc_stmt.setString("pURL", pURL);
            } else {
                proc_stmt.setString("pURL", null);
            }
            if (!"".equals(pFILE_TYPE)) {
                proc_stmt.setString("pFILE_TYPE", pFILE_TYPE);
            } else {
                proc_stmt.setString("pFILE_TYPE", null);
            }
            if (!"".equals(pMIME_TYPE)) {
                proc_stmt.setString("pMIME_TYPE", pMIME_TYPE);
            } else {
                proc_stmt.setString("pMIME_TYPE", null);
            }
            if (!"".equals(pDIGEST)) {
                proc_stmt.setString("pDIGEST", pDIGEST);
            } else {
                proc_stmt.setString("pDIGEST", null);
            }
            if (!"".equals(pCONTENT)) {
                proc_stmt.setString("pCONTENT", pCONTENT);
            } else {
                proc_stmt.setString("pCONTENT", null);
            }

            proc_stmt.setString("pFILE_UUID", pFILE_UUID);
            proc_stmt.setString("pDMS_PROPERTY", pDMS_PROPERTY);

            // if(!"".equals(pUPLOAD_TOKEN)){
            // proc_stmt.setString("pUPLOAD_TOKEN", pUPLOAD_TOKEN);
            // } else {
            // proc_stmt.setString("pUPLOAD_TOKEN", null);
            // }
            if (!"".equals(pHMAC)) {
                proc_stmt.setString("pHMAC", pHMAC);
            } else {
                proc_stmt.setString("pHMAC", null);
            }
            if (!"".equals(pLAST_MOTIFIED_BY)) {
                proc_stmt.setString("pLAST_MODIFIED_BY", pLAST_MOTIFIED_BY);
            } else {
                proc_stmt.setString("pLAST_MODIFIED_BY", null);
            }
            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            System.out.println("USP_PPL_FILE_UPDATE: " + proc_stmt);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    // insert workflow file
    public String USP_GW_PPL_WORKFLOW_FILE_ADD(int pPPL_WORKFLOW_ID, int pPPL_FILE_ID, String pTYPE,
                                               String pFILE_INFO, int pFROM_FILE_ID, String pHMAC, String pCREATED_BY) throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_FILE_ADD(?,?,?,?,?,?,?,?) }");

            proc_stmt.setInt("pPPL_WORKFLOW_ID", pPPL_WORKFLOW_ID);
            proc_stmt.setInt("pPPL_FILE_ID", pPPL_FILE_ID);
            proc_stmt.setString("pTYPE", pTYPE);
            proc_stmt.setString("pFILE_INFO", pFILE_INFO);
            if (pFROM_FILE_ID != 0) {
                proc_stmt.setInt("pFROM_FILE_ID", pFROM_FILE_ID);
            } else {
                proc_stmt.setString("pFROM_FILE_ID", null);
            }
            proc_stmt.setString("pHMAC", pHMAC);
            proc_stmt.setString("pCREATED_BY", pCREATED_BY);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            System.out.println("USP_PPL_WORKFLOW_FILE_ADD: " + proc_stmt);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    public void USP_GW_PPL_WORKFLOW_GET_LAST_FILE(PPLFile[][] response, String pSIGNING_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<PPLFile> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_GET_LAST_FILE(?,?) }");
            proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_GET_LAST_FILE: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                PPLFile tempItem = new PPLFile();
                tempItem.ID = rs.getInt("LAST_PPL_FILE_SIGNED_ID");
                tempItem.FILE_NAME = rs.getString("LAST_PPL_FILE_NAME");
                tempItem.FILE_UUID = rs.getString("LAST_PPL_FILE_UUID");

                tempList.add(tempItem);
            }
            response[0] = new PPLFile[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    // check workfllow signed?
    public String USP_GW_PPL_WORKFLOW_GET_STATUS(String pSIGNING_TOKEN, int[] sStatus, String[] sPostbackWFCheck)
            throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_GET_STATUS(?,?) }");
            proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_GET_STATUS: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                sStatus[0] = rs.getInt("ID");
                sStatus[1] = rs.getInt("WORKFLOW_STATUS");
                sPostbackWFCheck[0] = CommonFunction.CheckTextNull(rs.getString("POSTBACK_URL"));
                sPostbackWFCheck[1] = CommonFunction.CheckTextNull(rs.getString("REDIRECT_URI"));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    public void USP_GW_PPL_WORKFLOW_GET_FIRST_FILE(PPLFile[][] response, String pSIGNING_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<PPLFile> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_GET_FIRST_FILE(?,?) }");
            proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_GET_FIRST_FILE: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                PPLFile tempItem = new PPLFile();
                // tempItem.ID = rs.getInt("FIRST_PPL_FILE_ID");
                tempItem.ID = rs.getInt("PPL_FILE_ID");
                tempItem.FILE_NAME = rs.getString("FIRST_PPL_FILE_NAME");
                tempItem.FILE_UUID = rs.getString("FIRST_PPL_FILE_UUID");
                tempItem.ENTERPRISE_ID = rs.getInt("ENTERPRISE_ID");
                tempList.add(tempItem);
            }
            response[0] = new PPLFile[tempList.size()];

            response[0] = tempList.toArray(response[0]);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    public void USP_GW_PPL_WORKFLOW_PARTICIPANTS_LIST(Participants[][] response, String pSIGNING_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<Participants> tempList = new ArrayList<>();
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_PARTICIPANTS_LIST(?,?) }");
            proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_PARTICIPANTS_LIST: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                Participants tempItem = new Participants();
                tempItem.ID = rs.getInt("ID");
                tempItem.PPL_WORKFLOW_ID = rs.getInt("PPL_WORKFLOW_ID");
                tempItem.FIRST_NAME = rs.getString("FIRST_NAME");
                tempItem.LAST_NAME = rs.getString("LAST_NAME");
                tempItem.SIGNER_TOKEN = rs.getString("SIGNER_TOKEN");
                tempItem.SIGNER_STATUS = rs.getInt("SIGNER_STATUS");
                tempItem.SIGNED_TIME = rs.getTimestamp("SIGNED_TIME");
                tempItem.META_INFORMATION = rs.getString("META_INFORMATION");
                tempItem.SIGNING_PURPOSE = rs.getString("SIGNING_PURPOSE");
                tempItem.SIGNED_TYPE = rs.getString("SIGNED_TYPE");
                tempItem.CERTIFICATE = CommonFunction.CheckTextNull(rs.getString("CERTIFICATE"));
                tempList.add(tempItem);
            }
            response[0] = new Participants[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    public void USP_GW_PREFIX_PERSONAL_CODE_LIST(PREFIX_UID[][] response, String pTYPE, String pLANGUAGE_NAME) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<PREFIX_UID> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PREFIX_PERSONAL_CODE_LIST(?,?,?) }");
            if (!"".equals(pTYPE)) {
                proc_stmt.setString("pTYPE", pTYPE);
            } else {
                proc_stmt.setString("pTYPE", null);
            }
            if (!"".equals(pLANGUAGE_NAME)) {
                proc_stmt.setString("pLANGUAGE_NAME", pLANGUAGE_NAME);
            } else {
                proc_stmt.setString("pLANGUAGE_NAME", null);
            }

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");



            System.out.println("USP_PREFIX_PERSONAL_CODE_LIST: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                PREFIX_UID tempItem = new PREFIX_UID();
                tempItem.NAME = rs.getString("NAME");
                tempItem.TYPE = rs.getString("TYPE");
                tempItem.REMARK = rs.getString("REMARK");
                tempList.add(tempItem);
            }
            response[0] = new PREFIX_UID[tempList.size()];

            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }
    public void USP_GW_CONNECTOR_GET_FROM_PROVIDER(ConnectorName[][] response, String pPROVIDER) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<ConnectorName> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_CONNECTOR_GET_FROM_PROVIDER(?,?) }");
            proc_stmt.setString(1, pPROVIDER);

            proc_stmt.registerOutParameter(2, java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            // convrtr = String.valueOf(proc_stmt.getInt(2));
            convrtr = proc_stmt.getString(2);

            System.out.println("USP_CONNECTOR_GET_FROM_PROVIDER: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                ConnectorName tempItem = new ConnectorName();
                tempItem.CONNECTOR_NAME = rs.getString("CONNECTOR_NAME");
                tempItem.LOGO = rs.getString("LOGO");
                tempItem.REMARK = rs.getString("REMARK");

                tempList.add(tempItem);

            }
            response[0] = new ConnectorName[tempList.size()];
            System.out.println(response[0]);
            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }


    // check workfllow participants signed token
    public String USP_GW_PPL_WORKFLOW_PARTICIPANTS_GET_STATUS(String pSIGNER_TOKEN, int[] sStatus) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        String convrtr = "1";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_PARTICIPANTS_GET_STATUS(?,?) }");
            proc_stmt.setString("pSIGNER_TOKEN", pSIGNER_TOKEN);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_GET_STATUS: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                sStatus[0] = rs.getInt("SIGNER_STATUS");
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    // update participans
    public String USP_GW_PPL_WORKFLOW_PARTICIPANTS_UPDATE(String pSIGNER_TOKEN, String pSIGNED_TYPE,
                                                          java.sql.Timestamp pSIGNED_TIME,
                                                          String pSIGNATURE_ID, String pSIGNED_ALGORITHM, String pCERTIFICATE, String pSIGNATURE_TYPE,
                                                          String pSIGNING_OPTION, String pLAST_MODIFIED_BY) throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_PARTICIPANTS_UPDATE(?,?,?,?,?,?,?,?,?,?) }");

            proc_stmt.setString("pSIGNER_TOKEN", pSIGNER_TOKEN);
            proc_stmt.setString("pSIGNED_TYPE", pSIGNED_TYPE);
            if (pSIGNED_TIME != null) {
                proc_stmt.setObject("pSIGNED_TIME", pSIGNED_TIME);
            } else {
                proc_stmt.setString("pSIGNED_TIME", null);
            }
            proc_stmt.setString("pSIGNATURE_ID", pSIGNATURE_ID);
            proc_stmt.setString("pSIGNED_ALGORITHM", pSIGNED_ALGORITHM);
            proc_stmt.setString("pCERTIFICATE", pCERTIFICATE);
            proc_stmt.setString("pSIGNATURE_TYPE", pSIGNATURE_TYPE);
            proc_stmt.setString("pSIGNING_OPTION", pSIGNING_OPTION);
            proc_stmt.setString("pLAST_MODIFIED_BY", pLAST_MODIFIED_BY);

            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            System.out.println("USP_PPL_WORKFLOW_PARTICIPANTS_UPDATE: " + proc_stmt.toString());
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    // update WF status
    public String USP_GW_PPL_WORKFLOW_UPDATE_STATUS(String pSIGNING_TOKEN, int pWORKFLOW_STATUS, String pLAST_MODIFIED_BY) throws Exception {
        String convrtr = "1";
        Connection conns = null;
        CallableStatement proc_stmt = null;
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_UPDATE_STATUS(?,?,?,?) }");

            proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);
            proc_stmt.setInt("pWORKFLOW_STATUS", pWORKFLOW_STATUS);
            proc_stmt.setString("pLAST_MODIFIED_BY", pLAST_MODIFIED_BY);
            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            System.out.println("USP_PPL_WORKFLOW_UPDATE_STATUS: " + proc_stmt.toString());
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");
        } finally {
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
        return convrtr;
    }

    public void USP_GW_PPL_WORKFLOW_GET(WorkFlowList[][] response, String pSIGNING_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<WorkFlowList> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_WORKFLOW_GET(?,?) }");
            if (!"".equals(pSIGNING_TOKEN)) {
                proc_stmt.setString("pSIGNING_TOKEN", pSIGNING_TOKEN);
            } else {
                proc_stmt.setString("pSIGNING_TOKEN", null);
            }
            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            System.out.println("USP_PPL_WORKFLOW_GET: " + proc_stmt.toString());
            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                WorkFlowList tempItem = new WorkFlowList();
                tempItem.ID = rs.getInt("ID");
                tempItem.POSTBACK_URL = CommonFunction.CheckTextNull(rs.getString("POSTBACK_URL"));
                tempItem.WORKFLOW_STATUS = rs.getInt("WORKFLOW_STATUS");

                tempList.add(tempItem);

            }
            response[0] = new WorkFlowList[tempList.size()];
            System.out.println(response[0]);
            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }
    // batch
    //truyền batch token lấy ID
    public void USP_GW_PPL_BATCH_FILE_GET_WORKFLOW(BATCH[][] response, String pBATCH_FILE_TOKEN) throws Exception {
        CallableStatement proc_stmt = null;
        Connection conns = null;
        ResultSet rs = null;
        ArrayList<BATCH> tempList = new ArrayList<>();
        String convrtr = "0";
        try {
            conns = OpenDatabase();
            proc_stmt = conns.prepareCall("{ call USP_PPL_BATCH_FILE_GET_WORKFLOW(?,?) }");

            proc_stmt.setString("pBATCH_FILE_TOKEN", pBATCH_FILE_TOKEN);
            proc_stmt.registerOutParameter("pRESPONSE_CODE", java.sql.Types.NVARCHAR);
            proc_stmt.execute();
            convrtr = proc_stmt.getString("pRESPONSE_CODE");

            rs = proc_stmt.executeQuery();
            while (rs.next()) {
                BATCH tempItem = new BATCH();
//                tempItem.ID = rs.getInt("ID");
                tempItem.SIGNING_TOKEN = rs.getString("SIGNING_TOKEN");
                tempItem.SIGNER_TOKEN = rs.getString("SIGNER_TOKEN");
                tempList.add(tempItem);

            }
            response[0] = new BATCH[tempList.size()];
            System.out.println(response[0]);
            response[0] = tempList.toArray(response[0]);
            System.out.println(tempList);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (proc_stmt != null) {
                proc_stmt.close();
            }
            Connection[] temp_connection = new Connection[]{conns};
            CloseDatabase(temp_connection);
        }
    }

    public static String getIdentierConnector(String sConnectorName, String[] sResult) throws Exception {
        process conect = new process();
        ConnectorName[][] object = new ConnectorName[1][];
        conect.USP_GW_CONNECTOR_GET(object, sConnectorName);
        String sPropertiesFMS = "0";
        if (object.length > 0 && object != null) {
            for (int i = 0; i < object.length; i++) {
                sResult[0] = object[0][i].IDENTIFIER;
                sResult[1] = object[0][i].PREFIX_CODE;
            }
        }
        return sPropertiesFMS;
    }


}
