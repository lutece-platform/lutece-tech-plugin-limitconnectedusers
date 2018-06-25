/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.limitconnectedusers.service;

import java.util.HashSet;
import java.util.Set;


/**
 *
 * LimitSessionService
 *
 */
public class LimitSessionService
{
    private static LimitSessionService _singleton;
    private Set<String> _listSessionsActive = new HashSet<String>();
    private boolean _bNbMaximumUsersReached;
    private int _nbSessions = 0;

    /**
     *
     * @return LimitSessionService
     */
    public static LimitSessionService getService(  )
    {
        if ( _singleton == null )
        {
            _singleton = new LimitSessionService(  );
        }

        return _singleton;
    }

    /**
     *
     * @return the list of session active
     */
    public Set<String> getSessionsActive(  )
    {
        return _listSessionsActive;
    }

    /**
     *
     * @return true if the nb maximum users reached
     */
    public boolean isNbMaximumUsersReached(  )
    {
        return _bNbMaximumUsersReached;
    }

    /**
     *
     * @param _bNbMaximumUsersReached true if the nb maximum users reached
     */
    public void setNbMaximumUsersReached( boolean _bNbMaximumUsersReached )
    {
        this._bNbMaximumUsersReached = _bNbMaximumUsersReached;
    }
    
        
    /**
     * get absolute server Session number
     * @return session nb
     */   
    public int getNbSessions() {
        return _nbSessions;
    }

    /**
     * add a session
     */
    public synchronized void addSession( ) {
        this._nbSessions ++ ;
    }
    
    /**
     * remove a session
     */
    public synchronized void removeSession( ) {
        this._nbSessions -- ;
    }
}
