/*
 * SMRT Solutions
 * Data Collection Platform
 */
package com.smrtsolutions.util;

import com.smrtsolutions.survey.model.SMRTRole;
import com.smrtsolutions.survey.model.SMRTUser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author lenny
 */
public class Util {
    
    public static boolean isEmpty(String s){
        if ( s == null ) return true;
        if ( s.trim().isEmpty()) return true;
        else return false;
    }
    
     public static String encrypt(String val   ) throws Exception {
        if ( Util.isEmpty(val)) throw new Exception ("Value is empty");
        MessageDigest md = null;
        md = MessageDigest.getInstance("SHA"); //get an instance of a message digest object using the SHA algorithm
        md.update(val.getBytes("UTF-8")); //convert into byte representation using UTF-8 encoding
        byte raw[] = md.digest(); //transform to array of bytes that represents the digested (encrypted) value
        //String hash = (new BASE64Encoder()).encode(raw); //Creates a String representation of the byte array representing the digested value
        String hash = Util.encodeToString(raw);
        return hash;


  }

  private static String encodeToString( byte[] bytes )
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<bytes.length; i++ )
    {
      byte b = bytes[ i ];
      sb.append( ( int )( 0x00FF & b ) );
      if( i+1 <bytes.length )
      {
        sb.append( "-" );
      }
    }
    return sb.toString();
  }
  public static String getValue(String s){
      if ( Util.isEmpty(s)) return "";
      else return s.trim();
  }
  public static String getValue(String s, String def){
      if ( Util.isEmpty(s)) return def;
      else return s.trim();
  }
  /*
  public static boolean hasPermission(SMRTUser user, String permissions){
      String pc = Util.getValue(permissions).toUpperCase();
      System.out.println("pc=" + pc);
      for ( SMRTRole r : user.getRoles()) {
        String permission = Util.getValue(r.getPermissions()).toUpperCase();
        System.out.println("role perm=" + permission);
        String[] p = permission.split(",");
        //String pu = Util.getValue(permission).toUpperCase();
          for (String p1 : p) {
              String pi = Util.getValue(p1).toUpperCase();
              System.out.println("pi=" + pi);
              if (  pi.equals(SMRTRole.ALL) || pi.equals(pc)){
                  System.out.println("pi matched=" + pi);
                  return true;
              }
          }
      }
      return false;
  }
  
  public static boolean hasRole(SMRTUser user, String roleName){
      
      String rn = Util.getValue(roleName);
      for ( SMRTRole r : user.getRoles()) {
          if ( r.getName().equalsIgnoreCase(rn)) return true;
      }
      return false;
  }*/
  
  public static Map<String,String> parseMap(String s, String colDelim, String rowDelim){
      Map m = new HashMap();
      String[] rows = s.split(rowDelim);
      for ( String r : rows){
          if ( !Util.isEmpty(r)){
              String[] cols = r.split(colDelim);
              String n = ""; String v = "";
              if ( cols.length > 0  ) n = Util.getValue(cols[0]);
              if ( cols.length > 1  ) v = Util.getValue(cols[1]);
              
                if ( !Util.isEmpty(n)) {
                    m.put(n, v);
                }
              
          }
      }
      return m;
  }
  
  public static String asString(Map<String,String> m, String colDelim, String rowDelim){
      Set<String> keys = m.keySet();
      StringBuilder sb = new StringBuilder();
      for ( String k : keys){
          if ( !Util.isEmpty(k)){
              sb.append(k).append(colDelim).append(Util.getValue(m.get(k))).append(rowDelim); 
          }
      }
      return sb.toString();
  }
  
  public static long toLong(String s, long def) {
      if ( Util.isEmpty(s)) return def;
      return Long.parseLong(s);
  }
  
  public static boolean toBoolean(String s){
      if ( Util.isEmpty(s)) return false;
      if ( s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1") || s.equalsIgnoreCase("yes")) return true;
      return false;
  }
  
  public static List<com.smrtsolutions.survey.model.NameValuePair> toNameValuePairList(String str){
      return Util.toNameValuePairList(str, ";", "=");
  }
  
  public static List<com.smrtsolutions.survey.model.NameValuePair> toNameValuePairList(String str, String rowSep, String colSep){
      List<com.smrtsolutions.survey.model.NameValuePair> l = new ArrayList<com.smrtsolutions.survey.model.NameValuePair>();
      if ( Util.isEmpty(str)) return l;
      String[] rows = str.split(rowSep);
      for ( String r : rows){
          String[] rs = Util.getValue(r).trim().split(colSep);
          if ( rs != null ) {
              String n = ""; String v = "";
              if ( rs.length > 0 ) {
                  n = Util.getValue(rs[0]).trim();
              }
              if ( rs.length > 1 ) {
                  v = Util.getValue(rs[1]).trim();
              }
              if (!Util.isEmpty(n)) {
                  System.out.println("Name=" + n + " value=" + v);
                  l.add(new com.smrtsolutions.survey.model.NameValuePair(n,v));
              }
          }
                  
      }
      return l;
  
  }
  
  public static com.smrtsolutions.survey.model.NameValuePair findNameValuePairByValue(List<com.smrtsolutions.survey.model.NameValuePair> list, String val){
      boolean found = false;
      System.out.println("val=" + val);
      for ( com.smrtsolutions.survey.model.NameValuePair nvp : list){
          System.out.println("nvp val=" + nvp.getValue());
          if ( nvp.getValue().equals(val)){
              return nvp;
          }
      }
      return null;
  }
  
  public static String sendPostRequest(String requestUrl, String payload) {
        StringBuffer jsonString = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Basic SnA5dEZ6NkpDZEhNd3dNY1ZnNmU6TWhvZlpyZ2VwakVmWUJ6MEpXOXgwU2J0QmV3eHhs");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                    jsonString.append(line);
            }
            br.close();
            connection.disconnect();
            return jsonString.toString();
        } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
        } finally {
            jsonString = null;
        }
}
  
  public static String randompassword() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 7) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
  }
}
