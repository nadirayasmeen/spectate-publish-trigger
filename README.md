## History

Author: Nadira Yasmeen

Version History:

- 1.0 Nadira Yasmeen, Bradley Wagner

## Project set-up

### With Git and Maven only

This assumes you have Maven 3+ and Git installed. 

- Maven can be downloaded at: http://maven.apache.org/download.html
- Git can be downloaded at: http://git-scm.com/download

Clone the project:

1. Clone this repository
2. Change into the directory for the newly created project
3. Use Git to checkout the appropriate branch of this project for your Cascade installation. For example, if you're running Cascade version 6.8.3, checkout 6.8.x: `git checkout 6.8.x`

Update the Web Services stubs:

1. Open the WSDL from your Cascade Server instance by going to: http://<your-cascade-url>/ws/services/AssetOperationService?wsdl
2. Save this as a file "asset-operation.wsdl".
3. Replace the "asset-operation.wsdl" file in src/java/wsdl inside the eclipse project with your own file.
4. Open a command-line/terminal window to run maven. 
5. Navigate to to the base directory where the project was unzipped to (e.g. java/workspace/Cascade Webservices) and type the command "mvn generate-sources"
You should see a successful ant build similar to:

		$ mvn generate-sources
        [INFO] Scanning for projects...
        [INFO]                                                                         
        [INFO] ------------------------------------------------------------------------
        [INFO] Building Cascade-Java-Web-Services-Example-Project 6.8.3
        [INFO] ------------------------------------------------------------------------
        [INFO] 
        [INFO] --- axistools-maven-plugin:1.4:wsdl2java (default) @ Cascade-Java-Web-Services-Example-Project ---
        [INFO] about to add compile source root
        [INFO] Processing wsdl: /Users/bradley/cascade/Webservices-Java-Sample-Project/src/java/wsdl/asset-operation.wsdl
        Jul 18, 2011 3:33:52 PM org.apache.axis.utils.JavaUtils isAttachmentSupported
        WARNING: Unable to find required classes (javax.activation.DataHandler and javax.mail.internet.MimeMultipart). Attachment support is disabled.
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------
        [INFO] Total time: 3.940s
        [INFO] Finished at: Mon Jul 18 15:33:55 EDT 2011
        [INFO] Final Memory: 3M/81M
        [INFO] ------------------------------------------------------------------------

### In Eclipse

This assumes you have Eclipse 3.6+, m2e Maven plugin, Maven, and Git installed.

- Eclipse can be downloaded at: http://download.eclipse.org
- Maven can be downloaded at: http://maven.apache.org/download.html
- m2e Maven plugin comes with Eclipse 3.7+ and can be installed for older versions by going to Help > Eclipse Marketplace and typing 'm2e'
- Git can be downloaded at: http://git-scm.com/download

It is recommended you set up your environment in the following manner:

1. Create a "java" directory. Unzip Eclipse into it.
2. Ensue the "maven" executable is in your path. Type "mvn" on the command line if it's been correctly added to your path:

        $ mvn -v
        Apache Maven 3.0.2 (r1056850; 2011-01-08 19:58:10-0500)
        Java version: 1.6.0_24, vendor: Apple Inc.
        Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
        Default locale: en_US, platform encoding: MacRoman
        OS name: "mac os x", version: "10.6.8", arch: "x86_64", family: "mac"
  
3. Create a "workspace" folder in your java folder. This will be the root of your Eclipse workspace. 
   When you start Eclipse for the first time, tell it to use this folder as the workspace folder. If
   you are already using Eclipse and have already designated a workspace folder, you can continue using that folder.

Then you need to install 2 additional connectors from the m2e Marketplace in Eclipse:

1. File > Import > Check out Maven Projects from SCM > Next
2. Click "m2e Marketplace" link
3. Select the "Axis Tools m2e" and "m2e-egit" connectors from the m2e Marketplace
4. Click Finish
5. Follow the prompts to install these connectors
6. Restart Eclipse once these are done installing

Once Eclipse restarts:

1. File > Import > Check out Maven Projects from SCM > Next
2. Select "git" from the dropdown
3. Enter git URL for this repo from Github into the Git URL field
4. Click Next
5. Click Finish

This will import and build the project in Eclipse.

Once built, use Git to checkout the appropriate branch of this project for your Cascade installation. For example, if you're running Cascade version 6.8.3, checkout 6.8.x: `git checkout 6.8.x`

To update the generated Web Services stubs to correspond to your version of Cascade:

1. Open the WSDL from your Cascade Server instance by going to: http://<your-cascade-url>/ws/services/AssetOperationService?wsdl
2. Save this as a file "asset-operation.wsdl".
3. Replace the "asset-operation.wsdl" file in src/java/wsdl inside the eclipse project with your own file.
4. In Eclipse, right-click the project and click Refresh. Maven should run and regenerate your stubs based on this updated WSDL file.
5. If for some reason it doesn't, open a command-line/terminal window to run maven.
6. Navigate to to the base directory where the project was created to (e.g. java/workspace/Cascade Webservices) and type the command `mvn generate-sources`. You should see a successful maven build.

    $ mvn generate-sources
        ....
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------
        [INFO] Total time: 3.940s
        [INFO] Finished at: Mon Jul 18 15:33:55 EDT 2011
        [INFO] Final Memory: 3M/81M
        [INFO] ------------------------------------------------------------------------
        
7. Then, refresh Eclipse and your project should be built.

## Building and Deploying the Trigger

1. Run `mvn package` which should result in a successful Maven build and a JAR artifact in `target/spectate-email-publish-trigger-1.0.jar`
2. Copy that JAR and the following other JARs into the Tomcat's ROOT/WEB-INF/lib/ folder: jsoup-1.8.1.jar, commons-lang3-3.3.2.jar. Those are not compiled into the JAR itself.
3. Start Cascade  

## Setting up the Trigger

1. Go to Publish Triggers and add one for: `com.hannonhill.emailtrigger.SpectateTrigger`
2. Add an `apiKey` parameter with the API key of the Spectate user who will be creating the Emails
3. Add a `webUrl` which will be used to construct a path to the published page so its content can be fetched.
4. Use the following Data Definition on a Page:
```xml
<system-data-structure>
    <asset type="file" identifier="header" label="Header Image" help-text="Recommended width: 1400px"/>
    <text multi-line="true" identifier="abstract" label="Abstract" required="true"/>
    <text wysiwyg="true" identifier="content" label="Main Content"/>
    <asset type="page,file,symlink" identifier="add" label="Additional Information"/>
    <group identifier="distribution" label="Distribution Options">
        <text type="checkbox" identifier="home" label="Add to Front Page">
            <checkbox-item value="Yes" show-fields="distribution/home-start, distribution/home-end, distribution/homeImg"/>
        </text>
        <asset type="file" identifier="homeImg" label="Front Page Item Image" required="true" help-text="Recommended width: 480px, height: 154px"/>
        <text type="datetime" identifier="home-start" label="Front Page Start" required="true"/>
        <text type="datetime" identifier="home-end" label="Front Page End" required="true"/>
        <text type="checkbox" identifier="dept" label="Add to Featured Items">
            <checkbox-item value="Yes" show-fields="distribution/feature, distribution/dept-start, distribution/dept-end"/>
        </text>
        <asset type="file" identifier="feature" label="Featured Item Image" required="true" help-text="Recommended width: 418px, height: 226px"/>
        <text type="datetime" identifier="dept-start" label="Site Wide Feature Start" required="true"/>
        <text type="datetime" identifier="dept-end" label="Site Wide Feature End" required="true"/>
        <text type="checkbox" identifier="press" label="Mark as Press Release">
            <checkbox-item value="Yes" show-fields="distribution/header"/>
        </text>
        <asset type="block" identifier="header" label="Press Header Block" render-content-depth="2" required="true"/>
        <text type="checkbox" identifier="rssOption" label="Add to RSS Feed">
            <checkbox-item value="Yes" show-fields="distribution/rss"/>
        </text>
        <group identifier="rss" label="RSS Options">
            <text multi-line="true" identifier="fb" label="Facebook Message"/>
            <text multi-line="true" identifier="twitter" label="Twitter Message (140 characters max)"/>
        </group>
        <text type="multi-selector" identifier="district" label="Add to District Pages (Ctrl + click to select multiple)">
            <selector-item value="District 1" show-fields="distribution/district-start, distribution/district-end"/>
            <selector-item value="District 2" show-fields="distribution/district-start, distribution/district-end"/>
            <selector-item value="District 3" show-fields="distribution/district-start, distribution/district-end"/>
            <selector-item value="District 4" show-fields="distribution/district-start, distribution/district-end"/>
            <selector-item value="District 5" show-fields="distribution/district-start, distribution/district-end"/>
        </text>
        <text type="datetime" identifier="district-start" label="District Pages Start" required="true"/>
        <text type="datetime" identifier="district-end" label="District Pages End" required="true"/>
    </group>
    <group identifier="related" label="Related Files and Links" multiple="true">
        <text type="radiobutton" identifier="type" label="Link Type">
            <radio-item value="Internal" show-fields="related/related"/>
            <radio-item value="External" show-fields="related/title, related/ex"/>
        </text>
        <asset type="page,file,symlink" identifier="related" label="Related Files and Links" render-content-depth="2" required="true"/>
        <text identifier="title" label="Link Title" required="true"/>
        <text identifier="ex" label="External Link" required="true"/>
    </group>
    <text type="checkbox" identifier="email" label="Check to Send Blast">
        <checkbox-item value="Yes" show-fields="list, send"/>
    </text>
    <text type="checkbox" identifier="list" label="Blast Lists" required="true">
        <checkbox-item value="Fire Safety - Law Enforcement - Fire Services"/>
        <checkbox-item value="Green Communities"/>
        <checkbox-item value="Parks, Recreation, Outdoors"/>
        <checkbox-item value="Senior Issues and Information"/>
        <checkbox-item value="Animal Issues and Information"/>
        <checkbox-item value="Health Issues and Information"/>
        <checkbox-item value="Human Services Issues and Information"/>
        <checkbox-item value="Library Issues and Information"/>
        <checkbox-item value="TestCampaignToLinkWithCascade" checked="true"/>
    </text>
    <text type="dropdown" identifier="fromEmail" label="From Email">
        <dropdown-item value="nadira.yasmeen@hannonhill.com"/>
    </text>
    <text identifier="testers" label="Test Emails"/>
    <text type="radiobutton" identifier="send" label="Send" default="Save as Draft" required="true">
        <radio-item value="Now"/>
        <radio-item value="Later" show-fields="schedule"/>
        <radio-item value="Save as Draft"/>
    </text>
    <text type="datetime" identifier="schedule" label="Send At"/>
</system-data-structure>
```
