/*
 * Created on Jan 17, 2015 by Nadira Yasmeen
 *
 */
package com.hannonhill.emailtrigger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.cms.publish.PublishTrigger;
import com.cms.publish.PublishTriggerEntityTypes;
import com.cms.publish.PublishTriggerException;
import com.cms.publish.PublishTriggerInformation;

/**
 * Creates a Spectate Email from a Page in Cascade Server
 * 
 * @author Nadira Yasmeen
 */
public class SpectateTrigger implements PublishTrigger {
	private Map<String, String> parameters = new HashMap<String, String>();
	private PublishTriggerInformation information;
	private static final Logger LOG = Logger.getLogger(SpectateTrigger.class);

	public void invoke() throws PublishTriggerException {
		SpectateEmailer emailer = new SpectateEmailer();
		try {
			// this is where the logic for the trigger lives.
			// we switch on the entity type and this allows us to determine if a
			// page or file is being published
			switch (information.getEntityType()) {
			case PublishTriggerEntityTypes.TYPE_FILE:
				LOG.debug("File publish. Skipping trigger.");
				break;
			case PublishTriggerEntityTypes.TYPE_PAGE:
				LOG.debug("Publishing page with path "
						+ information.getEntityPath() + " and id "
						+ information.getEntityId());

				if (!publishingSpectateConfiguration(information, parameters)) {
					LOG.debug("Not publishing a Spectate page configuration. Skipping rest of trigger");
					return;
				}

				String apiKey = this.parameters.get("apiKey");
				if (apiKey == null || apiKey.equals("")) {
					LOG.debug("No apiKey present. Skipping rest of trigger.");
					return;
				}
				emailer.setApiKey(apiKey);

				String url = this.parameters.get("url");
				if (url != null && !url.equals("")) {
					emailer.setHost(url);
					emailer.setSoapEndpoint(url
							+ "/ws/services/AssetOperationService?wsdl");
				}

				String webUrl = this.parameters.get("webUrl");
				if (webUrl != null && webUrl.length() > 0)
					emailer.setWebUrl(webUrl);

				String user = this.parameters.get("user");
				if (user != null && user.length() > 0)
					emailer.setUser(user);

				String password = this.parameters.get("pass");
				if (password != null && !password.isEmpty())
					emailer.setPass(password);

				try {
					LOG.debug("Start creating Spectate Data if necessary...");
					emailer.getOutreachInfo(information.getEntityId(),
							information.getEntityPath());
				} catch (Exception e) {
					LOG.debug(
							"Error occurred when gathering data to send to Spectate",
							e);
				}
				if (!emailer.getOutReachEmail().getName().equals("")) {
					// populate campaigns
					emailer.getSelectedCampaigns();
					// send email
					String jsonResponse = emailer.getOutReachEmail().sendEmail(
							emailer.getDomain()
									+ "/marketing/emails.json?api_key="
									+ emailer.getApiKey());
					LOG.debug("Email create response for: "
							+ emailer.getPageAPIObject().getIdentifer().getId()
							+ " was: " + jsonResponse);

					if (jsonResponse != null && !jsonResponse.isEmpty()) {
						JSONObject jsonResponseObject = new JSONObject(
								jsonResponse);
						Integer id = jsonResponseObject.getJSONObject("email")
								.getInt("id");
						LOG.debug("Email created with id: "
								+ String.valueOf(id));
						// set the field in Cascade
						emailer.updateSentStatus(information.getEntityId(),
								information.getEntityPath(), "");
					}
					// Error Sending to Spectate
				}
				break;
			}
		} catch (Exception e) {
			LOG.error("Something went wrong. Logging error and continuing", e);
			try {
				LOG.info("Setting email status to Error sending to Spectate.");
				emailer.updateSentStatus(information.getEntityId(),
						information.getEntityPath(), e.getMessage());
			} catch (Exception e1) {
				LOG.error("Error setting email status to Error sending to Spectate.", e1);
			}
		}
	}

	/**
	 * @param information
	 * @param parameters
	 * @return Returns <tt>true</tt> if it is a Spectate page configuration and
	 *         a publish request, else <tt>false</tt>. All other page
	 *         configurations should be skipped
	 * @throws Exception
	 */
	private boolean publishingSpectateConfiguration(
			PublishTriggerInformation information,
			Map<String, String> parameters) throws Exception {
		String pageConfigurationId = parameters.get("pageConfigurationId");
		return information.getPageConfigurationId().equals(pageConfigurationId)
				&& !information.isUnpublish();
	}

	public void setPublishInformation(PublishTriggerInformation information) {
		// store this in an instance member so invoke() has access to it
		this.information = information;
	}

	public void setParameter(String name, String value) {
		// let's just store our parameters in a Map for access later
		parameters.put(name, value);
	}

}
