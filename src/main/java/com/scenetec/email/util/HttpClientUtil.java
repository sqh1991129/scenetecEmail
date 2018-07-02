package com.scenetec.email.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;


/**
 * httpClientUtil类，调用http和https工具类
 * @author sqh
 * @since 2018年7月2日13:43:39
 * */

public class HttpClientUtil {

	/**
	 * 发送post请求
	 * */
	public static String sendPost(String parameter,String url) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
		StringEntity entity = null;
		try {
			entity = new StringEntity(parameter,"UTF-8");
			httpPost.setEntity(entity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return null;
	}
	
	public static String sendGet(Map<String, Object> parameter,String url) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		if(!StringUtils.isEmpty(parameter)) {
			StringBuffer sb = new StringBuffer(url+"?");
			for(Map.Entry<String, Object> entry : parameter.entrySet()){
				sb.append(entry.getKey());
				sb.append("="+entry.getValue());
				sb.append("&");
			}
			int index = sb.toString().lastIndexOf("&");
			url = sb.toString().substring(0, index);
		}
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) {
		//id:wm36d93efab695e16a
		//miyue:gtD9ICIQWzgPigI_PJrS3hVmTnCBVC4oPLmbeVfLiNX4vco115vFqAq0CFE2dXEH
		//String url = "https://api.exmail.qq.com/cgi-bin/gettoken?corpid=id&corpsecret=secrect";
		String url = "https://api.exmail.qq.com/cgi-bin/department/create?access_token=whil2Fz-ok_JmuC9sbFPEbAcI0g2QwIJem7mNK66JLLQMd_RfwY8iYRkAtMCPkECciC_mLDgcCi8vWMiJrZMcw";
		JSONObject json = new JSONObject();
		json.put("name", "测试部门");
		json.put("parentid", Long.valueOf("1"));
		json.put("order", 0);
		System.out.println(sendPost(json.toJSONString(), url));
	}
}
