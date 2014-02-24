package fr.paris.lutece.plugins.limitconnectedusers.mbeans;

import java.io.IOException;

public interface ConnectedUsersMBean {
	
	   /**
     * Users sessions count
     * @return The sessions count
     * @throws IOException If an error occurs
     */
    int getConnectedUsersCount(  ) throws IOException;

}
