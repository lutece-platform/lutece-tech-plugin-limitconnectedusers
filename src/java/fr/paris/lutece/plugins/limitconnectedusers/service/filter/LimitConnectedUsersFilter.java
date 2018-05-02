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
package fr.paris.lutece.plugins.limitconnectedusers.service.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitSessionService;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.portal.ThemesService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.constants.Markers;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;


/**
 * Session filter for verify active session
 *
 */
public  class LimitConnectedUsersFilter implements Filter
{
    private static final String ACTIVATE_LIMIT_CONNECTED_USERS_FILTER = "activate";
    private static final String PROPERTY_MAX_CONNECTED_USERS = "limitconnectedusers.maxConnectedUsers";
    
    private static final String KEY_LIMIT_CONNECTED_USERS_MESSAGE= "limitconnectedusers.site_property.limit_message.textblock";
    private static final String KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_MESSAGE= "limitconnectedusers.site_property.limit_notification_message.textblock";
    private static final String KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_SENDER_NAME= "limitconnectedusers.site_property.limit_notification_sender_name";
    private static final String KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_SUBJECT= "limitconnectedusers.site_property.limit_notification_subject.textblock";
    private static final String KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_MAILING_LIST= "limitconnectedusers.site_property.limit_notification_mailing_list.textblock";
    private static final int DEFAULT_NB_MAX = 200;

    //i18n_message
    private static final String I18N_MESSAGE_TITLE_MAX_CONNECTED_USERS = "limitconnectedusers.title.max_connected_users";
    private static final String I18N_MESSAGE_MESSAGE_MAX_CONNECTED_USERS = "limitconnectedusers.message.max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_subject_max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_name_max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_MAIL_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_mail_max_connected_users";

    //Template
    private static final String TEMPLATE_MAIL_MESSAGE = "skin/plugins/limitconnectedusers/notify_mail_limited_connected_users.html";
    private static final String TEMPLATE_USER_MESSAGE = "skin/plugins/limitconnectedusers/user_message_limited_connected_users.html";

    //MARK
    private static final String MARK_ALERT_DATE = "alert_date";
    private static final String MARK_MESSAGE = "message";
    private static final String MARK_PLUGIN_THEME = "plugin_theme";
    private static final String MARK_THEME = "theme";
    private static final String MARK_HTML_CONTENT_TYPE = "text/html";
    

    /** filter config */
    protected FilterConfig _filterConfig = null;
    private int _nMaxConnectedUsers;
    private boolean _bActivate = false;
   
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
    	
        if ( _bActivate && request instanceof HttpServletRequest  )
        {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            HttpSession session = httpRequest.getSession( true );

            Set<String> sessionsActives = LimitSessionService.getService(  ).getSessionsActive(  );
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
                        String strNotificationMailingList=DatastoreService.getDataValue(KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_MAILING_LIST,null);
                        if ( !StringUtils.isEmpty(strNotificationMailingList) )
                        {
                            sendAlertMail( httpRequest,strNotificationMailingList );
                        }
                    }
                    HashMap<String, Object> model = new HashMap<String, Object>(  );	
                    String strMessage = DatastoreService.getDataValue(KEY_LIMIT_CONNECTED_USERS_MESSAGE, I18nService.getLocalizedString(I18N_MESSAGE_MESSAGE_MAX_CONNECTED_USERS, request.getLocale(  )));
                    
                    model.put(MARK_MESSAGE, strMessage);
                    model.put( Markers.PAGE_TITLE,
                            I18nService.getLocalizedString( I18N_MESSAGE_TITLE_MAX_CONNECTED_USERS, request.getLocale(  ) ) );
                    model.put( Markers.BASE_URL, AppPathService.getBaseUrl( httpRequest ));
                    model.put( MARK_PLUGIN_THEME, null );
                    model.put( MARK_THEME, ThemesService.getGlobalThemeObject(  ) );
                    
                    HtmlTemplate templateMessage = AppTemplateService.getTemplate( TEMPLATE_USER_MESSAGE, request.getLocale(  ), model );
                    // Write the resource content
                    addHeaderResponse(resp);
                    resp.setContentType(MARK_HTML_CONTENT_TYPE);
                    PrintWriter out = response.getWriter() ;
                    out.print(templateMessage.getHtml());
                    return;                    
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
   
    }



    /**
     * send a alert mail
     * @param request  @HttpServletRequest
     * @param strMailingList the mailing list
     */
    private void sendAlertMail( HttpServletRequest request,String strMailingList )
    {
        String strDate = DateUtil.getDateTimeString( new Date(  ).getTime(  ) );
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strNotificationMessage = DatastoreService.getDataValue(KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_MESSAGE, I18nService.getLocalizedString(I18N_MESSAGE_MAIL_SENDER_MAIL_MAX_CONNECTED_USERS, request.getLocale(  )));
        
        model.put( MARK_ALERT_DATE, strDate );
        model.put(MARK_MESSAGE, strNotificationMessage);
        
        HtmlTemplate templateMail = AppTemplateService.getTemplate( TEMPLATE_MAIL_MESSAGE, request.getLocale(  ), model );
        
        String strNotificationSenderName= DatastoreService.getDataValue(KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_SENDER_NAME, I18nService.getLocalizedString(I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS, request.getLocale(  )));
        String strNotificationSubject= DatastoreService.getDataValue(KEY_LIMIT_CONNECTED_USERS_NOTIFICATION_SUBJECT, I18nService.getLocalizedString(I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS, request.getLocale(  )));
        MailService.sendMailHtml(strMailingList , null, null,strNotificationSenderName,MailService.getNoReplyEmail(),strNotificationSubject,templateMail.getHtml(  ), true );
    }

    
    
    /**
     * addHeaderResponse
     * @param resp HttpServletResponse
     */
    private void addHeaderResponse(HttpServletResponse resp)
    {
    	
    	  resp.setHeader("Cache-Control","no-cache"); //HTTP 1.1
          resp.setHeader("Pragma","no-cache"); //HTTP 1.0
          resp.setDateHeader ("Expires", 0); //prevents caching at the proxy server
          
    }

    
}
