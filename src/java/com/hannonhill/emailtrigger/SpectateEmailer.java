/*
 * Created on Mar 10, 2015 by nadirayasmeen
 * 
 * Copyright(c) 2000-2010 Hannon Hill Corporation.  All rights reserved.
 */
package com.hannonhill.emailtrigger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hannonhill.cascade.api.asset.common.Identifier;
import com.hannonhill.cascade.api.asset.common.StructuredDataNode;
import com.hannonhill.cascade.api.asset.home.Page;
import com.hannonhill.cascade.api.operation.Read;
import com.hannonhill.cascade.api.operation.result.ReadOperationResult;
import com.hannonhill.cascade.model.dom.identifier.EntityType;
import com.hannonhill.cascade.model.dom.identifier.EntityTypes;
import com.hannonhill.www.ws.ns.AssetOperationService.Asset;
import com.hannonhill.www.ws.ns.AssetOperationService.AssetOperationHandler;
import com.hannonhill.www.ws.ns.AssetOperationService.AssetOperationHandlerServiceLocator;
import com.hannonhill.www.ws.ns.AssetOperationService.Authentication;
import com.hannonhill.www.ws.ns.AssetOperationService.EntityTypeString;
import com.hannonhill.www.ws.ns.AssetOperationService.OperationResult;
import com.hannonhill.www.ws.ns.AssetOperationService.ReadResult;
import com.hannonhill.www.ws.ns.AssetOperationService.StructuredDataNodes;

public class SpectateEmailer {
	private String soapServiceEndpoint = "http://localhost:8080/ws/services/AssetOperationService?wsdl";
	private String user = "";
	private String pass = "";
	private String host = "";
	private String webUrl = "";
	private List<String> campaignNames = new ArrayList<String>();
	private String apiKey = null;
	private String domain = "https://my.spectate.com";
	private Map<String, String> allCampaigns = new HashMap<String,String>(); // id, name																// address
	private Email outReachEmail = new Email();
	private static final Logger LOG = Logger.getLogger(SpectateTrigger.class);
	private Page pageAPIObject = null;

    /**
     * Update Email 'send' field to 'Sent to Spectate already' for Outreach page
     */
    public void updateSentStatus(String id, String path) throws Exception
    {
        LOG.debug("Setting Email 'send' field to: ' for page with id: " + id + " and path: " + path);
        com.hannonhill.www.ws.ns.AssetOperationService.Identifier identifier = new com.hannonhill.www.ws.ns.AssetOperationService.Identifier(id, null, EntityTypeString.page, false);
        Authentication auth = new Authentication();
        auth.setUsername(getUser());
        auth.setPassword(getPass());
        URL url = new URL(getSoapEndpoint());
        AssetOperationHandler handler = new AssetOperationHandlerServiceLocator().getAssetOperationService(url);
        ReadResult readResult = handler.read(auth, identifier);
        if (!"true".equals(readResult.getSuccess()))
            throw new Exception("Error reading page: " + id + " with path: " + path + " via WS to update its sent status. " + readResult.getMessage());
        
        LOG.debug("Succesfully read page with id: " + id + " and path: " + path + " for updating its 'send' status field");
        Asset asset = readResult.getAsset();
        if (asset == null || asset.getPage() == null)
            throw new Exception("No asset in read result when reading page: " + id + " with path: " + path + " via WS to update its sent status");
        
        com.hannonhill.www.ws.ns.AssetOperationService.Page page = asset.getPage();
        StructuredDataNodes nodes = page.getStructuredData().getStructuredDataNodes();
        com.hannonhill.www.ws.ns.AssetOperationService.StructuredDataNode[] nodesArr = nodes.getStructuredDataNode();
        for (com.hannonhill.www.ws.ns.AssetOperationService.StructuredDataNode node : nodesArr)
        {
            String nodeIdentifier = node.getIdentifier();
            if ("send".equals(nodeIdentifier))
            {
                node.setText("Sent to Spectate already");
            }
        }
        OperationResult editResult = handler.edit(auth, asset);
        if (!"true".equals(editResult.getSuccess()))
            throw new Exception("Error editing page: " + id + " with path: " + path + " via WS to update its sent status. " + readResult.getMessage());
        
        LOG.debug("Page with id: " + id + " and path: " + path + " successfully edited to update sent status.");
	}
	/*
	 * Gather Page parameters
	 */
	public void getOutreachInfo(final String id, String path) throws Exception {
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
        pageAPIObject = (Page) result.getAsset();
        setPageAPIObject(pageAPIObject);
        
        if (pageAPIObject != null) {
            LOG.debug("Page: " + pageAPIObject.getIdentifer().getId() + " was successfully read from the API");
            

            // return unless page has a DD called "Outreach"
            String ddPath = pageAPIObject.getDataDefinitionPath();
            if (ddPath == null || !ddPath.equals("Outreach"))
            {
                LOG.debug("Page's DD is: " + ddPath + ". Skipping rest of trigger.");
                return;
            }
            
            // return unless email field is set to "Yes"
            StructuredDataNode email = pageAPIObject.getStructuredDataNode("email");
            if (email == null || !Arrays.asList(email.getTextValues()).contains("Yes"))
            {
                LOG.debug("This page is not set to send email on publish. Skipping rest of trigger.");
                return;
            }
            
            // check send status
            StructuredDataNode sendStatus = pageAPIObject.getStructuredDataNode("send");
            String status = "draft";
            String sendStatusTextValue = sendStatus.getTextValue();
            if ("Sent to Spectate already".equals(sendStatusTextValue))
            {
                LOG.debug("Email for page: " + pageAPIObject.getIdentifer().getId() + " has already been sent to Spectate. Skipping rest of trigger to avoid duplicate email");
                return;
            }
            else if("Later".equals(sendStatusTextValue))
            {
                LOG.debug("Setting email status to 'send_later'");
                status = "send_later";
            }
            else if ("Now".equals(sendStatusTextValue))
            {
                LOG.debug("Setting email status to 'send_now'");
                status = "send_now";
            }
            else 
            {
                LOG.debug("Setting email status to 'draft'");
                status = "draft";
            }
            LOG.debug("Page: " + pageAPIObject.getIdentifer().getId() + " uses the Outreach data definition and is marked to send email on publish");
			String title = pageAPIObject.getMetadata().getTitle();


			String content = "";
			String fromEmail = "";
			String testRecepients = "";
			String abstractContent = "";
			String footer = StringEscapeUtils.escapeJson("<em>Copyright &#169; 2015 Washoe County, All rights reserved.</em>\n    <br />\n\t\t{{ unsubscribe_link }}\n\t\t<br />\n\t\t<strong>Our mailing address is:</strong>\n\t\t<br />\n\t\t{{ spam_compliance_address }}\n");
			StructuredDataNode[] nodes = pageAPIObject.getStructuredData();
			String day = null;
			String time = null;
			
			for (StructuredDataNode node : nodes) {
				String name = node.getIdentifier();
				String value = node.getTextValue();
				String[] values = node.getTextValues();
				LOG.debug("Current SD node: " + name + " : " + value + " : " + Arrays.toString(values));

				if (name.equals("abstract")) {
					abstractContent = value;
                    LOG.debug("Set 'abstract' content to: " + value);
				}
				else if (name.equals("fromEmail")) {
					fromEmail=value;
                    LOG.debug("Set 'fromEmail' to: " + value);
				}
				else if (name.equals("content")) {
					content = value;
					LOG.debug("Set 'content' to: " + value);
				}
				else if (name.equals("schedule") && value != null) { // Date
					day = new SimpleDateFormat("yyyy-MM-dd").format(Long.parseLong(value));
					LOG.debug("Set 'Scheduled Day' to: " + day);
					time = new SimpleDateFormat("h:mm a").format(Long.parseLong(value));
					LOG.debug("Set 'Scheduled Time' to: " + time);
						
				}
				else if(name.equals("testers")){
					testRecepients = value;
					LOG.debug("Set 'testRecipients' to: " + value);
				}
				// campaign
				else if (name.equals("list")) {
					for (String n : values) {
						if (!n.isEmpty()) {
							LOG.debug("Adding Campaign: " + n);
							campaignNames.add(n);
						}
					}
				}
			}
            outReachEmail.setName(title);
            outReachEmail.setSubject(title);
            outReachEmail.setTextBody(content);
            outReachEmail.setTestRecepients(testRecepients);
            outReachEmail.setMainContent(content);

            //if scheduled date has been set but status = now or draft, scheduled date/time needs to be overridden
            if("send_later".equals(status)){
            if (day != null)
            	outReachEmail.setScheduledAtDate(day);
            if (time != null)
            	outReachEmail.setScheduledAtTime(time);
            }
            outReachEmail.setStatus(status);
            // defaults
            outReachEmail.setFromType("generic");
            outReachEmail.setFromName("Washoe County");
            outReachEmail.setFromEmail(fromEmail);
            outReachEmail.setBodyType("html_only");
            outReachEmail.setLayoutType("custom");
            outReachEmail.setCustomHTMLBody(getRenderedContent(getPageAPIObject()));
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
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	// Gets ALL campaigns
	private String getCampaigns() throws IOException {
        LOG.debug("Retrieving campaigns from Spectate");
		String reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=1");
		JSONObject campaigns = new JSONObject(reply);
		String total = campaigns.get("total_entries").toString();
		reply = WebService.httpGet(getDomain()
				+ "/marketing/campaigns.json?api_key=" + getApiKey()
				+ "&per_page=" + total);
		
		LOG.debug(total + " total campaign objects retrieved from Spectate");
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
			getAllCampaigns().put(name, id);
		}
	}

	public void getSelectedCampaigns() throws Exception {
		// get ALL campaigns
			getAllCampaigns(getCampaigns());
			LOG.debug("Retrieving campaign ID(s) for selected campaign(s) Name.");
		for (String name : campaignNames) {
			String id = getAllCampaigns().get(name);

			if (id != null) {
				LOG.debug("Adding selected ID to list of campaign IDs.");
				// campaign exists add to selected campaigns
				getOutReachEmail().getCampaignIds().add(Integer.parseInt(id));
			}
		}
	}

	public String getDomain() {
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
        LOG.debug("Page content successfully read as: " + content);
        String fullContent = StringEscapeUtils.escapeJson(content.toString());
        LOG.debug("Page content JSON-escaped as: " + fullContent);
        return fullContent;
	}
	/**
	 * @return Returns the soapServiceEndpoint.
	 */
	public String getSoapEndpoint() {
		return soapServiceEndpoint;
	}

	/**
	 * @param soapServiceEndpoint the soapEndpoint to set
	 */
	public void setSoapEndpoint(String soapServiceEndpoint) {
		this.soapServiceEndpoint = soapServiceEndpoint;
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

	public Email getOutReachEmail() {
		return outReachEmail;
	}
	public void setOutReachEmail(Email outReachEmail) {
		this.outReachEmail = outReachEmail;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Page getPageAPIObject() {
		return pageAPIObject;
	}
	public Map<String, String> getAllCampaigns() {
		return allCampaigns;
	}
	public void setAllCampaigns(Map<String, String> allCampaigns) {
		this.allCampaigns = allCampaigns;
	}
	public void setPageAPIObject(Page pageAPIObject) {
		this.pageAPIObject = pageAPIObject;
	}
}
