/*
 * Copyright (c) 2002-2021, City of Paris
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests LimitSessionService.
 */
public class LimitSessionServiceTest
{
    private static final int THREADS = 8;
    private static final int SESSIONS_PER_THREAD = 20000;

    /**
     * Checks that sessions registered by concurrent requests are all kept.
     *
     * @throws InterruptedException
     *             if a thread is interrupted
     */
    @Test
    public void testConcurrentRegistration( ) throws InterruptedException
    {
        Set<String> sessions = new LimitSessionService( ).getSessionsActive( );
        List<Thread> threads = new ArrayList<>( );
        for ( int t = 0; t < THREADS; t++ )
        {
            int nThread = t;
            threads.add( new Thread( ( ) -> {
                for ( int i = 0; i < SESSIONS_PER_THREAD; i++ )
                {
                    sessions.add( nThread + "-" + i );
                }
            } ) );
        }
        threads.forEach( Thread::start );
        for ( Thread thread : threads )
        {
            thread.join( );
        }
        assertEquals( THREADS * SESSIONS_PER_THREAD, sessions.size( ) );
    }
}
