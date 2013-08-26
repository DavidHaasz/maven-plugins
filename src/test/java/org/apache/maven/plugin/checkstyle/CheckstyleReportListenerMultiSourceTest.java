package org.apache.maven.plugin.checkstyle;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CheckstyleReportListenerMultiSourceTest
    extends TestCase
{
    private Map<SeverityLevel, CheckstyleReportListener> listenerMap;

    /**
     * {@inheritDoc}
     */
    protected void setUp()
        throws Exception
    {
        listenerMap = new HashMap<SeverityLevel, CheckstyleReportListener>();

        CheckstyleReportListener listener = new CheckstyleReportListener( new File( "/source/path" ) );
        listener.addSourceDirectory( new File( "/source/path2" ) );
        listener.setSeverityLevelFilter( SeverityLevel.INFO );
        listenerMap.put( listener.getSeverityLevelFilter(), listener );

        listener = new CheckstyleReportListener( new File( "/source/path" ) );
        listener.addSourceDirectory( new File( "/source/path2" ) );
        listener.setSeverityLevelFilter( SeverityLevel.WARNING );
        listenerMap.put( listener.getSeverityLevelFilter(), listener );

        listener = new CheckstyleReportListener( new File( "/source/path" ) );
        listener.addSourceDirectory( new File( "/source/path2" ) );
        listener.setSeverityLevelFilter( SeverityLevel.ERROR );
        listenerMap.put( listener.getSeverityLevelFilter(), listener );

        listener = new CheckstyleReportListener( new File( "/source/path" ) );
        listener.addSourceDirectory( new File( "/source/path2" ) );
        listener.setSeverityLevelFilter( SeverityLevel.IGNORE );
        listenerMap.put( listener.getSeverityLevelFilter(), listener );
    }

    public void testListeners()
    {
        fireAuditStarted( null );

        AuditEvent event = new AuditEvent( this, "/source/path/file1", null );
        fireFileStarted( event );
        LocalizedMessage message =
            new LocalizedMessage( 0, 0, "", "", null, SeverityLevel.INFO, null, getClass(), null );
        fireAddError( new AuditEvent( this, "/source/path/file1", message ) );
        fireFileFinished( event );

        event = new AuditEvent( this, "/source/path2/file2", null );
        fireFileStarted( event );
        message = new LocalizedMessage( 0, 0, "", "", null, SeverityLevel.WARNING, null, getClass(), null );
        fireAddError( new AuditEvent( this, "/source/path2/file2", message ) );
        fireAddError( new AuditEvent( this, "/source/path2/file2", message ) );
        fireFileFinished( event );

        event = new AuditEvent( this, "/source/path/file3", null );
        fireFileStarted( event );
        message = new LocalizedMessage( 0, 0, "", "", null, SeverityLevel.ERROR, null, getClass(), null );
        fireAddError( new AuditEvent( this, "/source/path/file3", message ) );
        fireAddError( new AuditEvent( this, "/source/path/file3", message ) );
        fireAddError( new AuditEvent( this, "/source/path/file3", message ) );
        fireFileFinished( event );

        event = new AuditEvent( this, "/source/path2/file4", null );
        fireFileStarted( event );
        message = new LocalizedMessage( 0, 0, "", "", null, SeverityLevel.IGNORE, null, getClass(), null );
        fireAddError( new AuditEvent( this, "/source/path2/file4", message ) );
        fireAddError( new AuditEvent( this, "/source/path2/file4", message ) );
        fireAddError( new AuditEvent( this, "/source/path2/file4", message ) );
        fireAddError( new AuditEvent( this, "/source/path2/file4", message ) );
        fireFileFinished( event );

        fireAuditFinished( null );

        CheckstyleReportListener listener = listenerMap.get( SeverityLevel.INFO );
        CheckstyleResults results = listener.getResults();
        assertEquals( "Test total files", 4, results.getFiles().size() );
        assertEquals( "Test file count", 4, results.getFileCount() );
        assertEquals( "test file violations", 1, results.getFileViolations( "file1" ).size() );
        assertEquals( "test file severities", 1, results.getSeverityCount( "file1", SeverityLevel.INFO ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file1", SeverityLevel.WARNING ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file1", SeverityLevel.ERROR ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file1", SeverityLevel.IGNORE ) );

        listener = listenerMap.get( SeverityLevel.WARNING );
        results = listener.getResults();
        assertEquals( "Test total files", 4, results.getFiles().size() );
        assertEquals( "Test file count", 4, results.getFileCount() );
        assertEquals( "test file violations", 2, results.getFileViolations( "file2" ).size() );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file2", SeverityLevel.INFO ) );
        assertEquals( "test file severities", 2, results.getSeverityCount( "file2", SeverityLevel.WARNING ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file2", SeverityLevel.ERROR ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file2", SeverityLevel.IGNORE ) );

        listener = listenerMap.get( SeverityLevel.ERROR );
        results = listener.getResults();
        assertEquals( "Test total files", 4, results.getFiles().size() );
        assertEquals( "Test file count", 4, results.getFileCount() );
        assertEquals( "test file violations", 3, results.getFileViolations( "file3" ).size() );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file3", SeverityLevel.INFO ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file3", SeverityLevel.WARNING ) );
        assertEquals( "test file severities", 3, results.getSeverityCount( "file3", SeverityLevel.ERROR ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file3", SeverityLevel.IGNORE ) );

        listener = listenerMap.get( SeverityLevel.IGNORE );
        results = listener.getResults();
        assertEquals( "Test total files", 4, results.getFiles().size() );
        assertEquals( "Test file count", 4, results.getFileCount() );
        assertEquals( "test file violations", 0, results.getFileViolations( "file4" ).size() );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file4", SeverityLevel.INFO ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file4", SeverityLevel.WARNING ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file4", SeverityLevel.ERROR ) );
        assertEquals( "test file severities", 0, results.getSeverityCount( "file4", SeverityLevel.IGNORE ) );
    }

    private void fireAuditStarted( AuditEvent event )
    {
        for ( CheckstyleReportListener listener : listenerMap.values() )
        {
            listener.auditStarted( event );
        }
    }

    private void fireAuditFinished( AuditEvent event )
    {
        for ( CheckstyleReportListener listener : listenerMap.values() )
        {
            listener.auditFinished( event );
        }
    }

    private void fireFileStarted( AuditEvent event )
    {
        for ( CheckstyleReportListener listener : listenerMap.values() )
        {
            listener.fileStarted( event );
        }
    }

    private void fireFileFinished( AuditEvent event )
    {
        for ( CheckstyleReportListener listener : listenerMap.values() )
        {
            listener.fileFinished( event );
        }
    }

    private void fireAddError( AuditEvent event )
    {
        for ( CheckstyleReportListener listener : listenerMap.values() )
        {
            listener.addError( event );
        }
    }
}
