/**
 * 
 */
package org.kawanfw.file.examples.api.client;

import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.kawanfw.commons.api.client.SessionParameters;
import org.kawanfw.file.api.client.RemoteSession;

/**
 * @author Nicolas de Pomereu
 *
 */
public class SessionParametersExamples {

    /**
     * 
     */
    public SessionParametersExamples() {
	// TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

	String url = "https://www.acme.org/ServerFileManager";
	String username = "myUsername";
	char[] password = { 'm', 'y', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	SessionParameters sessionParameters = new SessionParameters();

	// Sets the timeout until a connection is established to 10 seconds
	sessionParameters.setConnectTimeout(10);

	// Sets the read timeout to 60 seconds
	sessionParameters.setReadTimeout(10);

	// We will use no proxy
	Proxy proxy = null;
	PasswordAuthentication passwordAuthentication = null;

	RemoteSession remoteSession = new RemoteSession(url, username,
		password, proxy, passwordAuthentication, sessionParameters);
	// Etc.

    }

}
