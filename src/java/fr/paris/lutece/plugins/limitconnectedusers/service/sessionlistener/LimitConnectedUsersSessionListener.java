/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
package fr.paris.lutece.plugins.limitconnectedusers.service.sessionlistener;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitConnectedUsersConstants;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 * LimitConnectedUsersSessionListener
 */
public final class LimitConnectedUsersSessionListener implements HttpSessionListener
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated( HttpSessionEvent sessionEvent )
    {
        //check if active session list exist
        if ( sessionEvent.getSession(  ).getServletContext(  )
                             .getAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_LISTE_SESSIONS_ACTIVES ) == null )
        {
            sessionEvent.getSession(  ).getServletContext(  )
                        .setAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_LISTE_SESSIONS_ACTIVES,
                new ArrayList<String>(  ) );
            sessionEvent.getSession(  ).getServletContext(  )
                        .setAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_IS_NB_MAXIMUM_USERS_REACHED,
                Boolean.FALSE );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionDestroyed( HttpSessionEvent sessionEvent )
    {
        // On enlève la session de l'utilisateur à la liste des sessions actives
        List<String> sessionsActives = (List<String>) sessionEvent.getSession(  ).getServletContext(  )
                                                                  .getAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_LISTE_SESSIONS_ACTIVES );

        if ( ( sessionsActives != null ) && sessionsActives.contains( sessionEvent.getSession(  ).getId(  ) ) )
        {
            sessionsActives.remove( sessionEvent.getSession(  ).getId(  ) );

            if ( (Boolean) sessionEvent.getSession(  ).getServletContext(  )
                                           .getAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_IS_NB_MAXIMUM_USERS_REACHED ) )
            {
                sessionEvent.getSession(  ).getServletContext(  )
                            .setAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_IS_NB_MAXIMUM_USERS_REACHED,
                    Boolean.FALSE );
            }
        }
    }
}
