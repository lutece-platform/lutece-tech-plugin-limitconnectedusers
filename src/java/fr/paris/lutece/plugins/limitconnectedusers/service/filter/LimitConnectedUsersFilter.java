/*
 * Copyright (c) 2002-2008, Mairie de Paris
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

/*
 * Creation date : 14 août 09 (by sBecker)
 */
package fr.paris.lutece.plugins.limitconnectedusers.service.filter;

import fr.paris.lutece.plugins.limitconnectedusers.service.LimitConnectedUsersConstants;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.io.IOException;

import java.util.Date;
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
 * Filtre de servlet permettant de vérifier le nombre de session active
 * @author merlinfe
 */
public abstract class LimitConnectedUsersFilter implements Filter
{
    // /** Identifiant de session pour l'utilisateur courant */
    // private static final String SESSION_FRONT_OFFICE_USER = "utilisateur";
    private static final String ACTIVATE_LIMIT_CONNECTED_USERS_FILTER = "activate";
    private static final String PROPERTY_MAX_CONNECTED_USERS = "limitconnectedusers.maxConnectedUers";
    private static final String PROPERTY_NOTIFIED_MAILING_LIST = "limitconnectedusers.notifiedMailingList";
    private static final int DEFAULT_NB_MAX = 200;
    private static final String MARK_NB_MAX_CONNECTED_USERS = "nb_max_connected_users";

    //i18n_message
    private static final String I18N_MESSAGE_TITLE_MAX_CONNECTED_USERS = "limitconnectedusers.title.max_connected_users";
    private static final String I18N_MESSAGE_MESSAGE_MAX_CONNECTED_USERS = "limitconnectedusers.message.max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_subject_max_connected_users";
    private static final String I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS = "limitconnectedusers.mail.sender_name_max_connected_users";

    //Template
    private static final String TEMPLATE_MAIL_MESSAGE = "skins/plugins/limitconnectedusers/notify_mail_limited_connected_users.html";

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
    public void destroy(  )
    {
        //
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
        throws IOException, ServletException
    {
        if ( _bActivate && request instanceof HttpServletRequest )
        {
            // Récupération de la session de l'utilisateur
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession( true );

            // Liste des sessions actives dans le ServletContext
            List<String> sessionsActives = (List<String>) session.getServletContext(  )
                                                                 .getAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_LISTE_SESSIONS_ACTIVES );
            Boolean bNbMaximumUsersReached = (Boolean) session.getServletContext(  )
                                                              .getAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_IS_NB_MAXIMUM_USERS_REACHED );

            if ( sessionsActives != null )
            {
                /*
                 * On ajoute la session de l'utilisateur à la liste des sessions actives dans le ServletContext
                 */
                if ( !sessionsActives.contains( session.getId(  ) ) &&
                        ( sessionsActives.size(  ) < _nMaxConnectedUsers ) )
                {
                    sessionsActives.add( session.getId(  ) );
                }
                else if ( !sessionsActives.contains( session.getId(  ) ) )
                {
                    Map<String, String> parameters = new HashMap<String, String>(  );
                    parameters.put( MARK_NB_MAX_CONNECTED_USERS, String.valueOf( _nMaxConnectedUsers ) );

                    if ( ( bNbMaximumUsersReached == null ) || !bNbMaximumUsersReached )
                    {
                        session.getServletContext(  )
                               .setAttribute( LimitConnectedUsersConstants.CONTEXT_ATTRIBUTE_IS_NB_MAXIMUM_USERS_REACHED,
                            Boolean.TRUE );

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

        String strSenderEmail = MailService.getNoReplyEmail(  );
        HtmlTemplate templateMail = AppTemplateService.getTemplate( TEMPLATE_MAIL_MESSAGE, request.getLocale(  ), model );
        MailService.sendMailHtml( _strNotifiedMailingList, null, null,
            I18nService.getLocalizedString( I18N_MESSAGE_MAIL_SENDER_NAME_MAX_CONNECTED_USERS, request.getLocale(  ) ),
            strSenderEmail,
            I18nService.getLocalizedString( I18N_MESSAGE_MAIL_SENDER_SUBJECT_MAX_CONNECTED_USERS, request.getLocale(  ) ),
            templateMail.getHtml(  ), true );
    }
}
