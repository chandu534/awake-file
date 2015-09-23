package org.kawanfw.file.test.api.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.junit.Test;
import org.kawanfw.commons.api.server.SshAuthCommonsConfigurator;

public class SshAuthCommonsConfiguratorTest {

    @Test
    public void test() throws IOException, SQLException {
	String username = "admin";
	String password = null;
	
	JPasswordField pf = new JPasswordField();
	pf.setFocusable(true);
	pf.requestFocus();
	int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

	char [] chars = null;
	
	if (okCxl == JOptionPane.OK_OPTION) {
	  password = new String(pf.getPassword());
	  chars =  password.toCharArray();
	}
	
	System.out.println(new Date());
	boolean connected = new SshAuthCommonsConfigurator().login(username, chars);
	System.out.println(new Date());
	System.out.println("connected: " + connected);

    }

    public static void main(String[] args) throws Exception {

	new SshAuthCommonsConfiguratorTest().test();
    }
    
}
