package fr.paris.lutece.plugins.limitconnectedusers.service;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * LimitSessionService
 *
 */
public class LimitSessionService {

	
	private static LimitSessionService _singleton;
	private List<String> _listSessionsActive=new ArrayList<String>();
	private boolean _bNbMaximumUsersReached;   
	
	/**
	 * 
	 * @return LimitSessionService
	 */
	public static LimitSessionService getService()
	{
		if( _singleton == null)
		{
			_singleton=new LimitSessionService();
			
		}
		
		return _singleton;
	}
	
	
	/**
	 * 
	 * @return the list of session active
	 */
	public List<String> getSessionsActive()
	{
		return _listSessionsActive;
	}
	
	

	/**
	 * 
	 * @return true if the nb maximum users reached
	 */
	public boolean isNbMaximumUsersReached() {
		return _bNbMaximumUsersReached;
	}

	/**
	 * 
	 * @param _bNbMaximumUsersReached true if the nb maximum users reached
	 */
	public void setNbMaximumUsersReached(boolean _bNbMaximumUsersReached) {
		this._bNbMaximumUsersReached = _bNbMaximumUsersReached;
	}

}
