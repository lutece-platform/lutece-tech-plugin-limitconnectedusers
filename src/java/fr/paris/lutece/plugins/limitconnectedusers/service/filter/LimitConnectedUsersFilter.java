/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.limitconnectedusers.service.filter;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitSessionService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Session filter for verify active session
 *
 */
public abstract class LimitConnectedUsersFilter implements Filter
{
    private static final String ACTIVATE_LIMIT_CONNECTED_USERS_FILTER = "activate";
    private static final String PROPERTY_MAX_CONNECTED_USERS = "limitconnectedusers.maxConnectedUsers";
    private static final String PROPERTY_NOTIFIED_MAILING_LIST = "limitconnectedusers.notifiedMailingList";
    private static final int DEFAULT_NB_MAX = 200;
    private static final String MARK_NB_MAX_CONNECTED_USERS = "nb_max_connected_users";

    //i18n_message
    private static final String I18N_MESSAGE_TITLE_MAX_CONNECTED_USERS = "limitconnectedusers.title.max_connected_users";
    private static final String I18N_MESSAGE_MESSAGE_MAX_CONNECTED_USERS = "limitconnectedusers.message.max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_subject_max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_name_max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_MAIL_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_mail_max_connected_users";

    //Template
    private static final String TEMPLATE_MAIL_MESSAGE = "skin/plugins/limitconnectedusers/notify_mail_limited_connected_users.html";

    //MARK
    private static final String MARK_ALERT_DATE = "alert_date";

    /** filter config */
    protected FilterConfig _filterConfig = null;
    private int _nMaxConnectedUsers;
    private boolean _bActivate = false;
    private String _strNotifiedMailingList = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(  )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
        throws IOException, ServletException
    {
        if ( _bActivate && request instanceof HttpServletRequest )
        {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession( true );

            List<String> sessionsActives = LimitSessionService.getService(  ).getSessionsActive(  );
            Boolean bNbMaximumUsersReached = LimitSessionService.getService(  ).isNbMaximumUsersReached(  );

            if ( sessionsActives != null )
            {
                if ( !sessionsActives.contains( session.getId(  ) ) &&
                        ( sessionsActives.size(  ) < _nMaxConnectedUsers ) )
                {
                    sessionsActives.add( session.getId(  ) );
                }
                else if ( !sessionsActives.contains( session.getId(  ) ) )
                {
                    if ( !bNbMaximumUsersReached )
                    {
                        LimitSessionService.getService(  ).setNbMaximumUsersReached( true );

                        if ( ( _strNotifiedMailingList != null ) && !_strNotifiedMailingList.trim(  ).equals( "" ) )
                        {
                            sendAlertMail( httpRequest );
                        }
                    }

                    request.getRequestDispatcher( "/" +
                        getMessageRelativeUrl( httpRequest, I18N_MESSAGE_MESSAGE_MAX_CONNECTED_USERS, null,
                            I18N_MESSAGE_TITLE_MAX_CONNECTED_USERS ) ).forward( request, response );
                }
            }
        }

        filterChain.doFilter( request, response );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init( FilterConfig filterConfig )
    {
        _filterConfig = filterConfig;

        String paramValue = _filterConfig.getInitParameter( ACTIVATE_LIMIT_CONNECTED_USERS_FILTER );

        if ( paramValue != null )
        {
            _bActivate = new Boolean( paramValue );
        }

        _nMaxConnectedUsers = AppPropertiesService.getPropertyInt( PROPERTY_MAX_CONNECTED_USERS, DEFAULT_NB_MAX );
        _strNotifiedMailingList = AppPropertiesService.getProperty( PROPERTY_NOTIFIED_MAILING_LIST );
    }

    /**
    * Forward the error message url depends site or admin implementation
    * @param request @HttpServletRequest
    * @param strMessageKey the message key
    * @param messageArgs the message args
    * @param strTitleKey the title of the message
    * @return url
    */
    protected abstract String getMessageRelativeUrl( HttpServletRequest request, String strMessageKey,
        Object[] messageArgs, String strTitleKey );

    /**
     * send a alert mail
     * @param request  @HttpServletRequest
     */
    private void sendAlertMail( HttpServletRequest request )
    {
        String strDate = DateUtil.getDateTimeString( new Date(  ).getTime(  ) );
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_ALERT_DATE, strDate );

        HtmlTemplate templateMail = AppTemplateService.getTemplate( TEMPLATE_MAIL_MESSAGE, request.getLocale(  ), model );
        MailService.sendMailHtml( _strNotifiedMailingList, null, null,
            I18nService.getLocalizedString( I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS, request.getLocale(  ) ),
            I18nService.getLocalizedString( I18N_MESSAGE_MAIL_SENDER_MAIL_MAX_CONNECTED_USERS, request.getLocale(  ) ),
            I18nService.getLocalizedString( I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS, request.getLocale(  ) ),
            templateMail.getHtml(  ), true );
    }

    /**
     * return the parameters Map
     * @param request the HttpServletRequest
     * @return the parameters Map
     */
    protected Map<String, Object> getParameterMap( HttpServletRequest request )
    {
        Map<String, Object> mapModifyParam = new HashMap<String, Object>(  );
        String paramName = StringUtils.EMPTY;

        // Get request parameters and store them in a HashMap
        if ( request != null )
        {
            Enumeration<?> enumParam = request.getParameterNames(  );

            while ( enumParam.hasMoreElements(  ) )
            {
                paramName = (String) enumParam.nextElement(  );
                mapModifyParam.put( paramName, request.getParameter( paramName ) );
            }
        }

        return mapModifyParam;
    }
}
