package com.example.cubedatImageSearch;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShituUtils {
	private static final String TAG = "ShituUtils";
	private static final int TIME_OUT = 10 * 1000; // ��ʱʱ��
	private static final String CHARSET = "utf-8"; // ���ñ���
	public static String resolvePostResponse(String postResponseUrl){
		String requestUri = "http://image.baidu.com" + postResponseUrl;
		HttpGet httpRequest = new HttpGet(requestUri);
		String strResult = null;
		try {

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				strResult = EntityUtils.toString(httpResponse
						.getEntity());

				Document document = Jsoup.parseBodyFragment(strResult);
				strResult = document.getElementsByClass("guess-info-not-found-title").text();
				if (strResult == ""){
					strResult = document.getElementsByClass("guess-info-text").text();
					if(strResult == ""){
						strResult = document.getElementsByClass("error-msg").text();
					}
				}
				return strResult;
			} else {
				strResult = "Error Response";
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	
	public static String postFile(String filePath, String requestURL) {
		int res=0;
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString(); // �߽��ʶ �������
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // ��������
		File file = new File(filePath);
		try {
			
			URL url = new URL(requestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setChunkedStreamingMode(1024 * 1024);// 1024K  
			conn.setDoInput(true); // ����������
			conn.setDoOutput(true); // ���������
			conn.setUseCaches(false); // ������ʹ�û���
			conn.setRequestMethod("POST"); // ����ʽ
			conn.setRequestProperty("Charset", CHARSET); // ���ñ���
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="+ BOUNDARY);
			if (file != null) {
				/**
				 * ���ļ���Ϊ��ʱִ���ϴ�
				 */
				OutputStream outputSteam=conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * �����ص�ע�⣺ name�����ֵΪ����������Ҫkey ֻ�����key �ſ��Եõ���Ӧ���ļ�
				 * filename���ļ������֣�������׺��
				 */
				
				sb.append("Content-Disposition: form-data; name=\"filedata\"; filename=\""
						+ file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * ��ȡ��Ӧ�� 200=�ɹ� ����Ӧ�ɹ�����ȡ��Ӧ����
				 */
				
				res = conn.getResponseCode();
				 
				Log.e(TAG, "response code:" + res);
				if (res == 200) {
					Log.e(TAG, "request success");
					InputStream input = conn.getInputStream();
					StringBuffer sb1 = new StringBuffer();
					int ss;
					while ((ss = input.read()) != -1) {
						sb1.append((char) ss);
					}
					result = sb1.toString();
					Log.e(TAG, "result : " + result);
				} else {
					Log.e(TAG, "request error");
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String baiduShitu(String imageUrl){
		String uriAPI = null;
		try {
			uriAPI = "http://image.baidu.com/n/pc_search?queryImageUrl="+URLEncoder.encode(imageUrl,"utf-8")+"&fm=result&pos=&uptype=paste";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpGet httpRequest = new HttpGet(uriAPI);
		String strResult = null;
		try {

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				strResult = EntityUtils.toString(httpResponse
						.getEntity());

				Document document = Jsoup.parseBodyFragment(strResult);
				strResult = document.getElementsByClass("guess-info-not-found-title").text();
				if (strResult == ""){
					strResult = document.getElementsByClass("guess-info-text").text();
					if(strResult == ""){
						strResult = document.getElementsByClass("error-msg").text();
					}
				}
				return strResult;
			} else {
				strResult = "Error Response";
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	
	
	public static List<String> subStringAll(String s, String sp1, String sp2) {
		List<String> results = new ArrayList<String>();
		String exp = sp1 + "([\\w/\\.]*)" + sp2;
		Pattern p = Pattern.compile(exp);
		Matcher m = p.matcher(s);
		while (!m.hitEnd() && m.find()) {
			results.add(m.group(1));
		}
		return results;
	}
	
	public static String subStringOne(String s, String sp1, String sp2) {
		List<String> results = new ArrayList<String>();
		String exp = sp1 + "(.*)" + sp2;
		Pattern p = Pattern.compile(exp);
		Matcher m = p.matcher(s);
		while (!m.hitEnd() && m.find()) {
			results.add(m.group(1));
		}
		return results.toArray(new String[0])[0];
	}
	public static String eregi_replace(String strFrom, String strTo, String strTarget) {
		String strPattern = "(?i)" + strFrom;
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strTarget);
		if (m.find()) {
			return strTarget.replaceAll(strFrom, strTo);
		} else {
			return strTarget;
		}
	}
}