/*
 * Created on Jan 17, 2015 by Nadira Yasmeen
 *
 */
package com.hannonhill.emailtrigger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.rpc.ServiceException;

import org.apache.commons.lang3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cms.publish.PublishTrigger;
import com.cms.publish.PublishTriggerEntityTypes;
import com.cms.publish.PublishTriggerException;
import com.cms.publish.PublishTriggerInformation;
import com.hannonhill.www.ws.ns.AssetOperationService.AssetOperationHandler;
import com.hannonhill.www.ws.ns.AssetOperationService.AssetOperationHandlerServiceLocator;
import com.hannonhill.www.ws.ns.AssetOperationService.Authentication;
import com.hannonhill.www.ws.ns.AssetOperationService.EntityTypeString;
import com.hannonhill.www.ws.ns.AssetOperationService.Identifier;
import com.hannonhill.www.ws.ns.AssetOperationService.Page;
import com.hannonhill.www.ws.ns.AssetOperationService.PageConfiguration;
import com.hannonhill.www.ws.ns.AssetOperationService.Path;
import com.hannonhill.www.ws.ns.AssetOperationService.ReadResult;
import com.hannonhill.www.ws.ns.AssetOperationService.StructuredData;
import com.hannonhill.www.ws.ns.AssetOperationService.StructuredDataNode;

/**
 * Creates a Spectate Email from a Page in Cascade Server
 * 
 * @author Nadira Yasmeen
 */
public class SpectateTrigger implements PublishTrigger {
	private Map<String, String> parameters = new HashMap<String, String>();
	private PublishTriggerInformation information;
	private String urlString = "https://wcwashoecmsdev.admin.washoecounty.us:8443/ws/services/AssetOperationService?wsdl";
	private String user = "admin";
	private String pass = "admin";
	private String host = "";
	private String siteId = "cde5bdfe94ba7976308d456098a63f49"; //access?? cde5bdfe94ba7976308d456098a63f49 - 
	private String siteName = "Outreach";
	private String webUrl = "";
	private List<String> campaignNames = new ArrayList<String>();
	private String apiKey = "zDGx49GoEN9QSojPIc6S";
	private String domain = "https://my.spectate.com";
	private Map<String, String> allCampaigns = new HashMap(); // id, name																// address
	private Map<String, Campaign> campaigns = new HashMap<String, Campaign>();
	private Email outReachEmail = new Email();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cms.publish.PublishTrigger#invoke()
	 */
	public void invoke() throws PublishTriggerException {
		// this is where the logic for the trigger lives.
		// we switch on the entity type and this allows us to determine if a
		// page or file is being published
		switch (information.getEntityType()) {
		case PublishTriggerEntityTypes.TYPE_FILE:
		//	System.out.println("Publishing file with path "
			//		+ information.getEntityPath() + " and id "
				//	+ information.getEntityId());
			break;
		case PublishTriggerEntityTypes.TYPE_PAGE:
	
		
			
			//only run on outreach pages
			System.out.println("Publishing page with path "
					+ information.getEntityPath() + " and id "
					+ information.getEntityId());
			
			SpectateTrigger t = new SpectateTrigger();
			if(this.parameters.get("apiKey").length() > 0){
				t.setApiKey(this.parameters.get("apiKey"));
			}
			if(this.parameters.get("url").length() > 0){
				t.setHost(this.parameters.get("url"));
				t.setUrlString(this.parameters.get("url")+"/ws/services/AssetOperationService?wsdl");
			}
			if(this.parameters.get("webUrl").length() > 0){
				t.setWebUrl(this.parameters.get("webUrl"));
			}
			try {
				System.out.println("Creating Spectate Data...");
				t.getOutreachInfo(information.getEntityId(),
						information.getEntityPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (!t.outReachEmail.getName().equals("")) {
			
				try {
					// populate campaigns
					t.getSelectedCampaigns();
					// send email
					t.outReachEmail.sendEmail(t.getDomain()
							+ "/marketing/emails?api_key=" + t.getApiKey());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			break;
		}
	}

	public void getDestinationInfo(String id, String path)	{
		Path p = new Path(path, getSiteId(), getSiteName());
		p.setPath(path);
		p.setSiteName(getSiteName());
		Identifier identifier = new Identifier(id, p, EntityTypeString.destination,
				false);
	
		URL url = null;
		try {
			url = new URL(getUrlString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Authentication auth = new Authentication();
		ReadResult result = new ReadResult();
		auth.setUsername(getUser());
		auth.setPassword(getPass());
		AssetOperationHandler handler;
		try {
			handler = new AssetOperationHandlerServiceLocator()
					.getAssetOperationService(url);
			result = handler.read(auth, identifier);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.getSuccess().equals("true")) {
			com.hannonhill.www.ws.ns.AssetOperationService.Destination dest = result
					.getAsset().getDestination();
			System.out.println("Setting Web URL to : " + dest.getWebUrl());
			setWebUrl(dest.getWebUrl());
		}
	}
	/*
	 * Gather Page parameters
	 */
	public void getOutreachInfo(String id, String path) throws IOException {
		//Path p = new Path(path, siteId, siteName);
		//p.setPath(path);
		//p.setSiteName(siteName);
		//Identifier identifier = new Identifier(id, p, EntityTypeString.page,
			//	false);
		Identifier identifier = new Identifier();
		//identifier.setPath(p);
		identifier.setRecycled(false);
		identifier.setId(id);
		identifier.setType(EntityTypeString.page);
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Authentication auth = new Authentication();
		ReadResult result = new ReadResult();
		auth.setUsername(getUser());
		auth.setPassword(getPass());
		AssetOperationHandler handler;
		try {
			handler = new AssetOperationHandlerServiceLocator()
					.getAssetOperationService(url);
			result = handler.read(auth, identifier);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (result.getSuccess().equals("true")) {
			String contentType = result.getAsset().getPage().getContentTypePath();
			
			if(!contentType.contains("Outreach")){
		//		return null;
			}
			
			
			Boolean email = false;
			com.hannonhill.www.ws.ns.AssetOperationService.Page page = result
					.getAsset().getPage();

			String title = page.getMetadata().getTitle();
			String content = "";
			String template = "";
			String fromEmail = "";
			String testRecepients = "";
			String abstractContent = "";
			String footer = StringEscapeUtils.escapeJson("<em>Copyright &#169; 2015 Washoe County, All rights reserved.</em>\n    <br />\n\t\t{{ unsubscribe_link }}\n\t\t<br />\n\t\t<strong>Our mailing address is:</strong>\n\t\t<br />\n\t\t{{ spam_compliance_address }}\n");
			StructuredData structuredData = page.getStructuredData();
			StructuredDataNode[] nodes = structuredData.getStructuredDataNodes().getStructuredDataNode();
			String day = null;
			String time = null;
			
			//get rendered page content
			outReachEmail.setMainContent(getRenderedContent(page));

			for (int i = 0; i < nodes.length; i++) {
				String name = nodes[i].getIdentifier();
				String value = nodes[i].getText();
				System.out.println(name + " : " + value);

				if (name.equals("abstract")) {
					abstractContent = value;
				} else if (name.equals("header")) {

				} 
				else if (name.equals("template")) {
					template=value;
				}
				else if (name.equals("fromEmail")) {
					fromEmail=value;
				}
				else if (name.equals("content")) {
					content = value;
				} else if (name.equals("add")) {

				} else if (name.equals("distribution")) {

				} else if (name.equals("related")) {

				} else if (name.equals("email")) {
					if (value.equals("::CONTENT-XML-CHECKBOX::Yes"))
						email = true;
				}
				else if(name.equals("testers")){
					testRecepients = value;
				}
				// campaign
				else if (name.equals("list")) {
					String[] names = value.split("::CONTENT-XML-CHECKBOX::");
					for (String n : names) {

						if (!n.isEmpty()) {
							System.out.println("Adding Campaign: " + n);
							campaignNames.add(n);

						}
					}
				} 
				/*else if (name.equals("schedule")) { // Date
					Date t = new Date(Long.parseLong(value));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					day = sdf.format(t);
					System.out.println("Scheduled Day: " + sdf.format(t));
					
						t = new Date(Long.parseLong(value));
						sdf = new SimpleDateFormat("h:mm a");
						time = sdf.format(t);
						System.out.println("Scheduled Time: " + sdf.format(t));
				
				}*/
			}
				if (email) {
		
					outReachEmail.setName(title);
					outReachEmail.setSubject(title);
					outReachEmail.setTextBody(content);
					outReachEmail.setTestRecepients(testRecepients);
					outReachEmail.setMainContent(content);
					outReachEmail.setMainContent(getRenderedContent(page));
					if (day != null)
						outReachEmail.setScheduledAtDate(day);
					if (time != null)
						outReachEmail.setScheduledAtTime(time);
				//	outReachEmail.setCustomHTMLBody(getRenderedContent(page));
					outReachEmail.setStatus("send_now");
					// defaults
					outReachEmail.setFromType("generic");
					outReachEmail.setFromName("Washoe County");
					outReachEmail.setFromEmail(fromEmail);
					
					//if html? create header/footer/main_content
					//HTML
					//outReachEmail.setBodyType("text_and_html");
					outReachEmail.setBodyType("html_only");
				//	outReachEmail.setBodyType("text_only");
					outReachEmail.setLayoutType("custom");
					outReachEmail.setCustomHTMLBody(getRenderedContent(page));
					//outReachEmail.setHTMLBody(null);
					outReachEmail.setHeader("");
					outReachEmail.setFooter(footer);
					//outReachEmail.setFooter("<p>{{ spam_compliance_address }}</p><p>&nbsp;</p><p><span style=\"font-family: arial,helvetica,sans-serif; font-size: xx-small;\"><!--{{ unsubscribe_link }}</span>--></span></p>");
					//outReachEmail.setFooter("{{ unsubscribe_link }}"); //KEEP
					outReachEmail.setCustomType("supplied");
		//			outReachEmail.setCustomEmailId(2333);
		
			}
		}//read page
		//return null
	}

	private String getParameter(JSONObject jsonObject, String parent,
			String child) {
		if (parent == null)
			return jsonObject.getJSONObject(child).toString();
		else
			return jsonObject.getJSONObject(parent).get(child).toString();
	}

	@SuppressWarnings("resource")
	private void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	private String getApiKey() {
		return this.apiKey;
	}

	// Gets ALL campaigns
	private String getCampaigns() throws IOException, ParseException {
		String reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=1");
		JSONObject campaigns = new JSONObject(reply);
		String total = campaigns.get("total_entries").toString();
		reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=" + total);
		return reply;
	}

	// Stores all campaigns in a map for quick retrieval later...
	private void getAllCampaigns(String campaigns) {
		JSONArray jsonObject = new JSONObject(campaigns)
				.getJSONArray("campaigns");
		for (int i = 0; i < jsonObject.length(); i++) {
			JSONObject o = new JSONObject(jsonObject.get(i).toString())
					.getJSONObject("campaign");
			String name = o.get("name").toString();
			String id = o.get("id").toString();
			allCampaigns.put(name, id);
		}
	}

	private void createEmail() throws UnsupportedEncodingException {
		outReachEmail.generateJSON();
	}

	private void getSelectedCampaigns() throws IOException, ParseException {
		// get ALL campaigns
		try {
			this.getAllCampaigns(this.getCampaigns());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String name : campaignNames) {
			String id = allCampaigns.get(name);

			if (id != null) {
				List<String> leads = new ArrayList<String>();
				List<String> emailAddresses = new ArrayList<String>();
				// get leads for this campaign
				try {
					leads = getLeads(id);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!leads.isEmpty()) {
					// get Emails for each
					for (String l : leads) {
						emailAddresses.add(getLeadEmail(l));
					}
				}

				// campaign exists add to selected campaigns
				outReachEmail.getCampaignIds().add(Integer.parseInt(id));
				campaigns.put(id, new Campaign(id, name, emailAddresses));
			}
		}
	}
	private String getCampaignID(String name) throws JSONException,
			IOException, ParseException {
		String leadURL = getDomain() + "/marketing/campaigns?api_key="
				+ getApiKey();
		JSONObject campaigns = new JSONObject(this.getCampaigns());
		return "";
	}

	private String getDomain() {
		return this.domain;
	}
	private String getRenderedContent(Page page) throws IOException{
		String id = page.getId();
		String path = page.getPath();
		String configId = "";
		StringBuffer content = new StringBuffer();

		String pageURLString = getWebUrl() +  path + "-spectate.html";
		URL pageURL = new URL(pageURLString);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(pageURL.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
        	content.append(inputLine);
        in.close();

		//Document doc = Jsoup.parse(content.toString());

		//Element head = doc.select("style").first(); //style works
		//Element body = doc.select("body").first();	
		//String jsonHead = StringEscapeUtils.escapeJson(head.html());
		//String jsonBody = StringEscapeUtils.escapeJson(body.html());
		//String jsonStyle = StringEscapeUtils.escapeJson(doc.select("style").first().html());
		String fullContent = StringEscapeUtils.escapeJson(content.toString());
		
		String templateString = fullContent;
		
		
		return templateString;
		//return content.toString();
	}

	private JSONObject getLead(String id) throws IOException, ParseException {
		String leadURL = getDomain() + "/leads_visitors/leads/" + id
				+ ".json?api_key=" + getApiKey();
		String reply = WebService.httpGet(leadURL);
		JSONObject jsonObject = new JSONObject(reply);
		return jsonObject;
	}

	// returns a string of ids to look up
	private List<String> getLeads(String campaignID) throws IOException {
		Document doc = Jsoup.connect(
				this.getDomain()
						+ "/marketing/campaigns/datatables/leads.html?api_key="
						+ this.getApiKey() + "&id=" + campaignID).get();
		Elements leads = doc.select("#leads_datatable_body tr:has(input)"); // read
																			// form
																			// input
		List<String> ids = new ArrayList<String>();

		for (Element e : leads) {
			String id = e.select("td input").attr("value");
			String name = e.select("td:eq(1) a").get(0).text();
			ids.add(id);
		}

		return ids;
	}

	private String getLeadEmail(String id) throws IOException, ParseException {
		String email = getParameter(getLead(id), "lead", "email");
		return email;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cms.publish.PublishTrigger#setParameter(java.lang.String,
	 * java.lang.String)
	 */
	public void setParameter(String name, String value) {
		// let's just store our parameters in a Map for access later
		parameters.put(name, value);
	}

	/**
	 * @return Returns the urlString.
	 */
	public String getUrlString() {
		return urlString;
	}

	/**
	 * @param urlString the urlString to set
	 */
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	/**
	 * @return Returns the user.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return Returns the pass.
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @param pass the pass to set
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/**
	 * @return Returns the siteId.
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return Returns the siteName.
	 */
	public String getSiteName() {
		return siteName;
	}

	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		if(!webUrl.endsWith("/"))
			this.webUrl = webUrl + "/";
		else
		this.webUrl = webUrl;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cms.publish.PublishTrigger#setPublishInformation(com.cms.publish.
	 * PublishTriggerInformation)
	 */
	public void setPublishInformation(PublishTriggerInformation information) {
		// store this in an instance member so invoke() has access to it
		this.information = information;
	}

	public static void main(String[] args) throws Exception {
		/*
		SpectateTrigger t = new SpectateTrigger();
		t.setHost("http://localhost:8080");
		t.setUrlString(t.getHost() + "/ws/services/AssetOperationService?wsdl");
		t.setSiteId("dfd138510a0000074dc8365b699721cd");
		t.setApiKey("zDGx49GoEN9QSojPIc6S");
		t.setWebUrl("http://localhost:8888/Outreach/outreach");
		t.setUser("admin");
		t.setPass("admin");
		t.setSiteName("Outreach");
		t.getOutreachInfo("dfd157b00a0000074dc8365b8f07b179",
				"/2015/01/2015-01-12-phone-scams");
		if (!t.outReachEmail.getName().equals("")) {
			// populate campaigns
			t.getSelectedCampaigns();
			// send email
			t.outReachEmail.sendEmail(t.getDomain()
					+ "/marketing/emails?api_key=" + t.getApiKey());
		}
	
		*/
	}
	
	
}
