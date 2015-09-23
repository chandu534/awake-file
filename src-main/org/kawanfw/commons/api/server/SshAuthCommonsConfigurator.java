/**
 * 
 */
package org.kawanfw.commons.api.server;

import java.io.IOException;

import org.kawanfw.commons.api.server.util.Ssh;

/**
 * A concrete {@code CommonsConfigurator} that extends {@code DefaultCommonsConfigurator} and allows zero-code client 
 * {@code (usernname, password)} authentication using SSH.
 * 
 * @author Nicolas de Pomereu
 * @since 3.0
 */
public class SshAuthCommonsConfigurator extends DefaultCommonsConfigurator
	implements CommonsConfigurator {

    /**
     * Allows using SSH to authenticate the remote {@code (usernname, password)}  couple
     * sent by the client side
     * <p>
     * Returns the result of {@link Ssh#login(String, char[])} method.
     * 
     * @param username
     *            the username sent by the client login
     * @param password
     *            the password to connect to the server
     * 
     * @return <code>true</code> if the (login, password) couple is
     *         correct/valid as a SSH user on this host. If false, the client
     *         side will not be authorized to send any command.
     * @throws IOException
     *             if wrapped {@code Ssh.login(String, char[])} throws an
     *             I/O Exception.
     */
    @Override
    public boolean login(String username, char[] password) throws IOException {
	return Ssh.login(username, password);
    }

}
