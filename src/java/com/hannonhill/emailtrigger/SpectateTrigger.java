/*
 * Created on Jan 17, 2015 by Nadira Yasmeen
 *
 */
package com.hannonhill.emailtrigger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cms.publish.PublishTrigger;
import com.cms.publish.PublishTriggerEntityTypes;
import com.cms.publish.PublishTriggerException;
import com.cms.publish.PublishTriggerInformation;
import com.hannonhill.cascade.api.asset.common.Identifier;
import com.hannonhill.cascade.api.asset.common.StructuredDataNode;
import com.hannonhill.cascade.api.asset.home.Page;
import com.hannonhill.cascade.api.operation.Read;
import com.hannonhill.cascade.api.operation.result.ReadOperationResult;
import com.hannonhill.cascade.model.dom.identifier.EntityType;
import com.hannonhill.cascade.model.dom.identifier.EntityTypes;

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
	private String siteId = "cde5bdfe94ba7976308d456098a63f49";
	private String siteName = "Outreach";
	private String webUrl = "";
	private List<String> campaignNames = new ArrayList<String>();
	private String apiKey = null;
	private String domain = "https://my.spectate.com";
	private Map<String, String> allCampaigns = new HashMap<String,String>(); // id, name																// address
	private Map<String, Campaign> campaigns = new HashMap<String, Campaign>();
	private Email outReachEmail = new Email();
	private static final Logger LOG = Logger.getLogger(SpectateTrigger.class);

	public void invoke() throws PublishTriggerException {
        try {
        // this is where the logic for the trigger lives.
        // we switch on the entity type and this allows us to determine if a
        // page or file is being published
        switch (information.getEntityType()) {
        case PublishTriggerEntityTypes.TYPE_FILE:
            LOG.info("File publish. Skipping trigger.");
            break;
        case PublishTriggerEntityTypes.TYPE_PAGE:
            LOG.info("Publishing page with path " + information.getEntityPath() + " and id " + information.getEntityId());

            SpectateTrigger t = new SpectateTrigger();

            String apiKey = this.parameters.get("apiKey");
            if (apiKey == null || apiKey.equals(""))
            {
                LOG.info("No apiKey present. Skipping rest of trigger.");
                return;
            }
            t.setApiKey(apiKey);

            String url = this.parameters.get("url"); 
            if (url != null && !url.equals(""))
            {
                t.setHost(url);
                t.setUrlString(url + "/ws/services/AssetOperationService?wsdl");
            }
            
            String webUrl = this.parameters.get("webUrl");
            if (webUrl != null && webUrl.length() > 0)
                t.setWebUrl(webUrl);

            String user = this.parameters.get("user");
            if (user != null && user.length() > 0)
                t.setUser(user);

            try {
                LOG.info("Start creating Spectate Data if necessary...");
                t.getOutreachInfo(information.getEntityId(), information.getEntityPath());
            } catch (Exception e) {
                LOG.info("Error occurred when gathering data to send to Spectate", e);
            }
            if (!t.outReachEmail.getName().equals("")) {
                // populate campaigns
                t.getSelectedCampaigns();
                // send email
                t.outReachEmail.sendEmail(t.getDomain() + "/marketing/emails?api_key=" + t.getApiKey());
                
            }
            break;
		}
        }
        catch (Exception e)
        {
            LOG.error("Something went wrong", e);
        }
	}

	/*
	 * Gather Page parameters
	 */
	private void getOutreachInfo(final String id, String path) throws Exception {
        Read readPage = new Read();
        Identifier identifier = new Identifier()
        {
            
            public EntityType getType()
            {
                return EntityTypes.TYPE_PAGE;
            }
            
            public String getId()
            {
                return id;
            }
        };
        
        readPage.setToRead(identifier);
        readPage.setUsername(getUser());
        ReadOperationResult result = (ReadOperationResult) readPage.perform();
        Page page = (Page) result.getAsset();
        
        if (page != null) {
            LOG.info("Page: " + page.getIdentifer() + " was successfully read from the API");
            
            // return unless page has a DD called "Outreach"
            String ddPath = page.getDataDefinitionPath();
            if (ddPath == null || !ddPath.equals("Outreach"))
            {
                LOG.info("Page's DD is: " + ddPath + ". Skipping rest of trigger.");
                return;
            }
            
            // return unless email field is set to "Yes"
            StructuredDataNode email = page.getStructuredDataNode("email");
            if (email == null || !Arrays.asList(email.getTextValues()).contains("Yes"))
            {
                LOG.info("This page is not set to send email on publish. Skipping rest of trigger.");
                return;
            }

            LOG.info("Page: " + page.getIdentifer() + " uses the Outreach data definition and is marked to send email on publish");

			String title = page.getMetadata().getTitle();
			String content = "";
			String template = "";
			String fromEmail = "";
			String testRecepients = "";
			String abstractContent = "";
			String footer = StringEscapeUtils.escapeJson("<em>Copyright &#169; 2015 Washoe County, All rights reserved.</em>\n    <br />\n\t\t{{ unsubscribe_link }}\n\t\t<br />\n\t\t<strong>Our mailing address is:</strong>\n\t\t<br />\n\t\t{{ spam_compliance_address }}\n");
			StructuredDataNode[] nodes = page.getStructuredData();
			String day = null;
			String time = null;
			
			for (StructuredDataNode node : nodes) {
				String name = node.getIdentifier();
				String value = node.getTextValue();
				String[] values = node.getTextValues();
				LOG.debug("Current SD node: " + name + " : " + value + " : " + Arrays.toString(values));

				if (name.equals("abstract")) {
					abstractContent = value;
                    LOG.info("Set 'abstract' content to: " + value);
				}
				else if (name.equals("template")) {
					template=value;
                    LOG.info("Set 'template' content to: " + value);
				}
				else if (name.equals("fromEmail")) {
					fromEmail=value;
                    LOG.info("Set 'fromEmail' to: " + value);
				}
				else if (name.equals("content")) {
					content = value;
					LOG.info("Set 'content' to: " + value);
				}
				else if(name.equals("testers")){
					testRecepients = value;
					LOG.info("Set 'testRecipients' to: " + value);
				}
				// campaign
				else if (name.equals("list")) {
					for (String n : values) {
						if (!n.isEmpty()) {
							LOG.info("Adding Campaign: " + n);
							campaignNames.add(n);
						}
					}
				}
			}
            outReachEmail.setName(title);
            outReachEmail.setSubject(title);
            outReachEmail.setTextBody(content);
            outReachEmail.setTestRecepients(testRecepients);
            outReachEmail.setMainContent(getRenderedContent(page));
            if (day != null)
            	outReachEmail.setScheduledAtDate(day);
            if (time != null)
            	outReachEmail.setScheduledAtTime(time);
            
            LOG.debug("Skipping scheduled info for now. Always using 'send_now' option");
            outReachEmail.setStatus("draft");
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
            outReachEmail.setCustomType("supplied");
        }
        else
        {
            LOG.debug("Page with identifier: " + identifier +  " not read succesfully. Will skip rest of trigger.");
        }
	}

	private String getParameter(JSONObject jsonObject, String parent, String child) {
        if (parent == null)
            return jsonObject.getJSONObject(child).toString();

        return jsonObject.getJSONObject(parent).get(child).toString();
	}

	private void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	private String getApiKey() {
		return this.apiKey;
	}

	// Gets ALL campaigns
	private String getCampaigns() throws IOException {
        LOG.info("Retrieving campaigns from Spectate");
		String reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=1");
		JSONObject campaigns = new JSONObject(reply);
		String total = campaigns.get("total_entries").toString();
		reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=" + total);
		
		LOG.info(total + " total campaign objects retrieved from Spectate");
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

	private void getSelectedCampaigns() throws IOException {
		// get ALL campaigns
		try {
			this.getAllCampaigns(this.getCampaigns());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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

	private String getDomain() {
		return this.domain;
	}
	private String getRenderedContent(Page page) throws IOException{
        String path = page.getPath();
		StringBuffer content = new StringBuffer();

		String pageURLString = getWebUrl() +  path + "-spectate.html";
		
		LOG.debug("Read page's content at live url: " + pageURLString);
		
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
        
        LOG.info("Page content successfully read as: " + content);
        String fullContent = StringEscapeUtils.escapeJson(content.toString());
        LOG.info("Page content JSON-escaped as: " + fullContent);
        return fullContent;
	}

	private JSONObject getLead(String id) throws IOException {
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

	private String getLeadEmail(String id) throws IOException {
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
