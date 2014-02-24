package fr.paris.lutece.plugins.limitconnectedusers.mbeans;

import java.io.IOException;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitSessionService;

/**
 * 
 * ConnectUsers
 *
 */
public class ConnectedUsers implements ConnectedUsersMBean
{
    /**
     * {@inheritDoc }
     */
    @Override
   public  int getConnectedUsersCount(  ) throws IOException
    {
      
        return LimitSessionService.getService().getSessionsActive().size();
    }
}