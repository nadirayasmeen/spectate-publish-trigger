/*
 * Created on Jan 16, 2015 by nadirayasmeen
 * 
 * Copyright(c) 2000-2010 Hannon Hill Corporation.  All rights reserved.
 */
package com.hannonhill.emailtrigger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.tidy.*;
import org.jsoup.Jsoup;

public class Email {
	
	private String name = "";
	private String body_type = "";
	private String from_name = "";
	private String from_email = "";
	private String subject = "";
	private String content = "";
	private String header = "";
	private String subHeader = "";
	private String text_body = "";
	private String testRecepients = "";
	private String abstractContent = "";
	private String scheduled_at_date = "";
	private String scheduled_at_time = "";
	private String custom_type = "";
	private List<Integer> campaign_ids = new ArrayList<Integer>();
	private boolean test_emails = false;
	private String footer = "";
	private String status;
	private int custom_email_id;
	private String custom_html_body = "";
	private String main_content = "";
	private String layout_type = "";
	private String from_type = "";
	private boolean fullEmail = true;
	private String template = "";
	private String headContent = "";
	
	
	public Email(){
		//defaults
		setFromType("generic");
		setStatus("draft");
		setBodyType("html_only");
		setSubject("No Subject");
		
	}
	public Email(String name, String subject, List<String> recepients, String content, String header, String abstractContent){
		this.name = name;
		this.subject = subject;
		this.content = content;
		this.header = header;
		this.abstractContent = abstractContent;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * @return Returns the content.
	 */
	public String getTestReceipients(){
		return this.testRecepients;
	}
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return Returns the header.
	 */
	public String getHeader() {
		return header;
	}
	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	/**
	 * @return Returns the abstractContent.
	 */
	public String getAbstractContent() {
		return abstractContent;
	}
	/**
	 * @return Returns the body_type.
	 */
	public String getBodyType() {
		return body_type;
	}
	/**
	 * @param body_type the body_type to set
	 */
	public void setBodyType(String body_type) {
	//	if(!body_type.equals("text_only") || !body_type.equals("html_only") || !body_type.equals("text_and_html"))
		//	this.body_type = "html_only";
		//else
			this.body_type = body_type;
	}
	/**
	 * @return Returns the from_name.
	 */
	public String getFromName() {
		return from_name;
	}
	/**
	 * @param from_name the from_name to set
	 */
	public void setFromName(String from_name) {
		this.from_name = from_name;
	}
	/**
	 * @return Returns the from_email.
	 */
	public String getFromEmail() {
		return from_email;
	}
	/**
	 * @param from_email the from_email to set
	 */
	public void setFromEmail(String from_email) {
		this.from_email = from_email;
	}
	/**
	 * @return Returns the subHeader.
	 */
	public String getSubHeader() {
		return subHeader;
	}
	/**
	 * @param subHeader the subHeader to set
	 */
	public void setSubHeader(String subHeader) {
		this.subHeader = subHeader;
	}
	/**
	 * @return Returns the text_body.
	 */
	public String getTextBody() {
		return text_body;
	}
	/**
	 * @param text_body the text_body to set
	 */
	public void setTextBody(String text_body) {
		StringBuffer textBody = new StringBuffer();
		//Strip out any HTML
		String textOnly = Jsoup.parse(text_body).text();
		if(!textOnly.contains("{{ unsubscribe_link }}")){
			textBody.append(textOnly + " {{ unsubscribe_link }}");
			this.text_body = textBody.toString();
		}
		else
			this.text_body = textBody.toString();
	}
	public boolean getFullEmail(){
		return this.fullEmail;
	}
	/**
	 * @return Returns the scheduled_at_date.
	 */
	public String getScheduledAtDate() {
		return scheduled_at_date;
	}
	/**
	 * @param day the scheduled_at_date to set
	 */
	public void setScheduledAtDate(String day) {
		this.scheduled_at_date = day;
	}
	/**
	 * @return Returns the scheduled_at_time.
	 */
	public String getScheduledAtTime() {
		return scheduled_at_time;
	}
	public String generateJSON() throws UnsupportedEncodingException{
		//for each non-empty parameter create key/value pair
		StringBuffer jsonString = new StringBuffer();
		//String status = "draft";
		String status = getStatus();
		
		if(!getScheduledAtDate().equalsIgnoreCase("")){
			setStatus("send_later");
		}
	
		String params = "{\"email\":{\"name\":\""+ getName() +"\"" +
		 		",\"subject\":\""+ getSubject() +"\"" +
		 		",\"heading\":\""+ getHeader() +"\"" +
		 	//	",\"subheading\":\""+ getSubHeader() +"\"" +
		 		",\"footer\":\""+getFooter()+"\"" +
		 		",\"from_type\":\"generic\"" + //Generic or User
		 		//Generic
		 		",\"from_name\":\""+getFromName()+"\"" +
		 		",\"from_email\":\""+getFromEmail()+"\"" +
			 	//User
		 		//	",\"from_user_id\":\"25\"" + //user integer id
		 		",\"body_type\":\""+ getBodyType() +"\"" + //text_only, html_only, text_and_html
		 		",\"campaign_ids\":"+outputCampaignIds()+""+
		 		",\"main_content\":\""+  getTextBody()+"{{unsubscribe_link}}"+"\"" +
		 		//",\"text_body\":\""+ getTextBody()+"\"" + //text_body
		 		",\"text_body\":\"\"" + //text_body
		 		
		 		//layout type for HTML
		 		",\"layout_type\":\"custom\"" + //two_column_content_left, two_column_content_right, one_column, custom
		 		",\"custom_type\":\"supplied\"" + //supplied, existing
		 		",\"html_body\":null" +
		 //		",\"custom_email_id\":\"1\"" + //id of custom email
	//	",\"custom_email_id\":2333" + //id of custom email
		 //		",\"custom_html_body\":\"<html><head><title>{{ name }}</title></head><body>{{ main_content }}{{ unsubscribe_link }}</body></html>\"" +
		 	
		 	",\"custom_html_body\":\"" + getCustomHTMLBody()+"\"" +
		 		//	",\"suppressed_campaigns_attributes\":{\"id\":\"4680\"}"+
		 		",\"graphic_id\":null" +
		 		",\"subheading\":\"\"" +
		 		",\"logo_id\":null" +
		 		",\"secondary_content\":\"\"" +
		 		",\"extended_content\":null" +
		 		",\"polished_html_body\":null" +
		 		",\"polished_text_body\":null" +
		 		 ",\"test_emails\":\""+getTestReceipients()+"\"" + //comma separated listed of emails
		 		//",\"scheduled_at_date\":\""+getScheduledAtDate()+" \"" + //scheduled at date date/time yyyy-mm-dd
		 		//",\"scheduled_at_time\":\""+getScheduledAtTime()+" \"" + //scheduled at date date/time 12:00 AM
		 	",\"status\":\""+getStatus()+"\"" + //STATUS: draft, test, send_now, send_later, personal Send Later, Send Now, Save as Draft			 				 					 		
		 		"}}";
		//return jsonString.toString();
		return params;
		
	}
	public void sendEmail(String url) throws Exception{
		String json = this.generateJSON();
		System.out.println(json);
		WebService.httpPost(url, json);
	}
	public String getJSONPair(String key, String val){
		if(!val.isEmpty() || val != null){
			return "\""+key+"\":" + "\""+val+"\",";
		}		
		return "";
	}
	/**
	 * @param time the scheduled_at_time to set
	 */
	public void setScheduledAtTime(String time) {
		this.scheduled_at_time = time;
	}
	/**
	 * @return Returns the template.
	 */
	public String getTemplate() {
		return template;
	}
	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	public String outputCampaignIds(){
		StringBuffer c = new StringBuffer();
		int count = 1;
		
		c.append("[");
		for(int i: getCampaignIds()){
			c.append(i);
			if(count < getCampaignIds().size())
				c.append(",");
				count++;
		}
		c.append("]");
		return c.toString();
	}
	/**
	 * @return Returns the custom_type.
	 */
	public String getCustomType() {
		return custom_type;
	}
	/**
	 * @param custom_type the custom_type to set
	 */
	public void setCustomType(String custom_type) {
		this.custom_type = custom_type;
	}
	public void setFromType(String from_type) {
		if(!from_type.equals("generic") || !from_type.equals("user_id"))
			this.from_type = "generic";
		else
			this.from_type = from_type;
	}
	public String getFromType(){		
		return from_type;
	}
	/**
	 * @return Returns the campaign_ids.
	 */
	public List<Integer> getCampaignIds() {
		return campaign_ids;
	}
	/**
	 * @param campaign_ids the campaign_ids to set
	 */
	public void setCampaignIds(List<Integer> campaign_ids) {
		this.campaign_ids = campaign_ids;
	}
	/**
	 * @return Returns the test_emails.
	 */
	public boolean isTestEmails() {
		return test_emails;
	}
	/**
	 * @param test_emails the test_emails to set
	 */
	public void setTestEmails(boolean test_emails) {
		this.test_emails = test_emails;
	}
	/**
	 * @return Returns the footer.
	 */
	public String getFooter() {
		return footer;
	}
	/**
	 * @param footer the footer to set
	 */
	public void setFooter(String footer) {
		this.footer = footer;
	}
	/**
	 * @return Returns the body_type.
	 */
	public String getBody_type() {
		return body_type;
	}
	/**
	 * @param body_type the body_type to set
	 */
	public void setBody_type(String body_type) {
		this.body_type = body_type;
	}
	/**
	 * @return Returns the from_name.
	 */
	public String getFrom_name() {
		return from_name;
	}
	/**
	 * @param from_name the from_name to set
	 */
	public void setFrom_name(String from_name) {
		this.from_name = from_name;
	}
	/**
	 * @return Returns the from_email.
	 */
	public String getFrom_email() {
		return from_email;
	}
	/**
	 * @param from_email the from_email to set
	 */
	public void setFrom_email(String from_email) {
		this.from_email = from_email;
	}
	/**
	 * @return Returns the text_body.
	 */
	public String getText_body() {
		return text_body;
	}
	/**
	 * @param text_body the text_body to set
	 */
	public void setText_body(String text_body) {
		this.text_body = text_body;
	}
	/**
	 * @return Returns the testRecepients.
	 */
	public String getTestRecepients() {
		return testRecepients;
	}
	/**
	 * @param testRecepients the testRecepients to set
	 */
	public void setTestRecepients(String testRecepients) {
		this.testRecepients = testRecepients;
	}
	/**
	 * @return Returns the scheduled_at_date.
	 */
	public String getScheduled_at_date() {
		return scheduled_at_date;
	}
	/**
	 * @param scheduled_at_date the scheduled_at_date to set
	 */
	public void setScheduled_at_date(String scheduled_at_date) {
		this.scheduled_at_date = scheduled_at_date;
	}
	/**
	 * @return Returns the scheduled_at_time.
	 */
	public String getScheduled_at_time() {
		return scheduled_at_time;
	}
	/**
	 * @param scheduled_at_time the scheduled_at_time to set
	 */
	public void setScheduled_at_time(String scheduled_at_time) {
		this.scheduled_at_time = scheduled_at_time;
	}
	/**
	 * @return Returns the custom_type.
	 */
	public String getCustom_type() {
		return custom_type;
	}
	/**
	 * @param custom_type the custom_type to set
	 */
	public void setCustom_type(String custom_type) {
		this.custom_type = custom_type;
	}
	/**
	 * @return Returns the campaign_ids.
	 */
	public List<Integer> getCampaign_ids() {
		return campaign_ids;
	}
	/**
	 * @param campaign_ids the campaign_ids to set
	 */
	public void setCampaign_ids(List<Integer> campaign_ids) {
		this.campaign_ids = campaign_ids;
	}
	/**
	 * @return Returns the test_emails.
	 */
	public boolean isTest_emails() {
		return test_emails;
	}
	/**
	 * @param test_emails the test_emails to set
	 */
	public void setTest_emails(boolean test_emails) {
		this.test_emails = test_emails;
	}
	/**
	 * @return Returns the custom_email_id.
	 */
	public int getCustom_email_id() {
		return custom_email_id;
	}
	/**
	 * @param custom_email_id the custom_email_id to set
	 */
	public void setCustom_email_id(int custom_email_id) {
		this.custom_email_id = custom_email_id;
	}
	/**
	 * @return Returns the custom_html_body.
	 */
	public String getHTMLbody() {
		return custom_html_body;
	}
	/**
	 * @param custom_html_body the custom_html_body to set
	 */
	public void setHTMLBody(String custom_html_body) {
		this.custom_html_body = custom_html_body;
	}
	/**
	 * @return Returns the main_content.
	 */
	public String getMain_content() {
		return main_content;
	}
	/**
	 * @param main_content the main_content to set
	 */
	public void setMain_content(String main_content) {
		this.main_content = main_content;
	}
	/**
	 * @return Returns the layout_type.
	 */
	public String getLayout_type() {
		return layout_type;
	}
	/**
	 * @param layout_type the layout_type to set
	 */
	public void setLayout_type(String layout_type) {
		this.layout_type = layout_type;
	}
	/**
	 * @return Returns the from_type.
	 */
	public String getFrom_type() {
		return from_type;
	}
	/**
	 * @param from_type the from_type to set
	 */
	public void setFrom_type(String from_type) {
		this.from_type = from_type;
	}
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return Returns the custom_email_id.
	 */
	public int getCustomEmailId() {
		return custom_email_id;
	}
	/**
	 * @param custom_email_id the custom_email_id to set
	 */
	public void setCustomEmailId(int custom_email_id) {
		this.custom_email_id = custom_email_id;
	}
	/**
	 * @return Returns the fullEmail.
	 */
	public boolean isFullEmail() {
		return fullEmail;
	}
	/**
	 * @param fullEmail the fullEmail to set
	 */
	public void setFullEmail(boolean fullEmail) {
		this.fullEmail = fullEmail;
	}
	/**
	 * @return Returns the custom_html_body.
	 */
	public String getCustomHTMLBody() {
		return custom_html_body;
	}
	/**
	 * @param custom_html_body the custom_html_body to set
	 */
	public void setCustomHTMLBody(String custom_html_body) {
		this.custom_html_body = custom_html_body;
	}
	/**
	 * @return Returns the main_content.
	 */
	public String getMainContent() {
		return main_content;
	}

	/**
	 * @param main_content the main_content to set
	 */
	public void setMainContent(String main_content) {
	
		StringBuffer mainBody = new StringBuffer();
		
		if(!main_content.contains("{{ unsubscribe_link }}")){
			mainBody.append(main_content + " {{ unsubscribe_link }}");
			this.main_content = main_content;
		}
		else
			this.main_content = main_content;
	}
	/**
	 * @return Returns the layout_type.
	 */
	public String getLayoutType() {
		return layout_type;
	}
	public String getHeadContent() {
		return headContent;
	}
	public void setHeadContent(String headContent) {
		this.headContent = headContent;
	}
	/**
	 * @param layout_type the layout_type to set
	 */
	public void setLayoutType(String layout_type) {
		this.layout_type = layout_type;
	}
	/**
	 * @param abstractContent the abstractContent to set
	 */
	public void setAbstractContent(String abstractContent) {
		this.abstractContent = abstractContent;
	}
	
}
