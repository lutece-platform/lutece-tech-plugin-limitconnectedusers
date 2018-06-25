/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
package fr.paris.lutece.plugins.limitconnectedusers.web;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitSessionService;
import fr.paris.lutece.plugins.limitconnectedusers.service.filter.LimitConnectedUsersFilter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.dashboard.DashboardComponent;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

/**
 * SystemInfo Dashboard Component
 */
public class LimitConnectedUsersDashboardComponent extends DashboardComponent
{
    private static final String TEMPLATE_DASHBOARD = "/admin/plugins/limitconnectedusers/limitconnectedusers_dashboard.html";
    
    private static final String MARK_NB_SESSIONS = "nb_sessions";
    
    private static final String MARK_CONNECTEDUSERS = "connected_users";
    private static final String MARK_CONNECTEDUSERS_LIMIT = "connected_users_limit";

    /**
     * The HTML code of the component
     * 
     * @param user  The Admin User
     * @param request The HTTP request
     * 
     * @return The dashboard component
     */
    @Override
    public String getDashboardData( AdminUser user, HttpServletRequest request )
    {
        
        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_NB_SESSIONS, LimitSessionService.getService( ).getNbSessions( ) );
        model.put( MARK_CONNECTEDUSERS, LimitSessionService.getService( ).getSessionsActive( ).size( ) );
        model.put( MARK_CONNECTEDUSERS_LIMIT, AppPropertiesService.getPropertyLong( LimitConnectedUsersFilter.PROPERTY_MAX_CONNECTED_USERS, LimitConnectedUsersFilter.DEFAULT_NB_MAX) );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_DASHBOARD, user.getLocale( ), model );

        return t.getHtml( );
    }

}
