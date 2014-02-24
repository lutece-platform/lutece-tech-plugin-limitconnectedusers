package fr.paris.lutece.plugins.limitconnectedusers.mbeans;

import fr.paris.lutece.util.jmx.mbeans.MBeanExporter;

/**
 * 
 * ConnectedUsersMBeanExporter
 *
 */
public class ConnectedUsersMBeanExporter implements MBeanExporter
{
    private static final String MBEAN_NAME = "type=ConnectedUsers";
    /**
     * {@inheritDoc }
     */
    @Override
    public String getMBeanName(  )
    {
        return MBEAN_ROOT_NAME + MBEAN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public Object getMBean(  )
    {
        return new ConnectedUsers(  );
    }
}
