/*************************************************************************
 *                                                                       *
 *  SignServer: The OpenSource Automated Signing Server                  *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.signserver.module.odfsigner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.signserver.common.GenericSignRequest;
import org.signserver.common.GenericSignResponse;
import org.signserver.common.RemoteRequestContext;
import org.signserver.common.SignServerException;
import org.signserver.common.SignServerUtil;
import org.signserver.common.StaticWorkerStatus;
import org.signserver.common.WorkerConfig;
import org.signserver.common.WorkerIdentifier;
import org.signserver.common.WorkerStatus;
import org.signserver.ejb.interfaces.ProcessSessionRemote;
import org.signserver.ejb.interfaces.WorkerSession;
import org.signserver.testutils.ModulesTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for odfsigner. Worker ID of 5678 is hard coded here and used from
 * module-configs/odfsigner/junittest-part-config.properties
 *
 * Test case : signs odt file with certificate defined in
 * module-configs/odfsigner/junittest-part-config.properties
 *
 * @author Aziz Göktepe
 * @version $Id$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ODFSignerTest extends ModulesTestCase {

    private final WorkerSession workerSession = getWorkerSession();
    private final ProcessSessionRemote processSession = getProcessSession();

    /**
     * WORKERID used in this test case as defined in
     * junittest-part-config.properties
     */
    private static final int WORKERID = 5678;

    /**
     * predefined odt file in base64 format.
     */
    private static final String TEST_ODF_DOC = "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ZT0idXJuOm9hc2lzOm5hbWVzOnRjOm9wZW5kb2N1bWVudDp4bWxuczpvZmZpY2U6MS4wIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIiB4bWxuczptZXRhPSJ1cm46b2FzaXM6bmFtZXM6dGM6b3BlbmRvY3VtZW50OnhtbG5zOm1ldGE6MS4wIiB4bWxuczpvb289Imh0dHA6Ly9vcGVub2ZmaWNlLm9yZy8yMDA0L29mZmljZSIgb2ZmaWNlOnZlcnNpb249IjEuMiI+PG9mZmljZTptZXRhPjxtZXRhOmluaXRpYWwtY3JlYXRvcj5jYXNleSByYXliYWNrPC9tZXRhOmluaXRpYWwtY3JlYXRvcj48bWV0YTpjcmVhdGlvbi1kYXRlPjIwMDktMDgtMTVUMTM6MTc6MTIuNTA8L21ldGE6Y3JlYXRpb24tZGF0ZT48bWV0YTpkb2N1bWVudC1zdGF0aXN0aWMgbWV0YTp0YWJsZS1jb3VudD0iMCIgbWV0YTppbWFnZS1jb3VudD0iMCIgbWV0YTpvYmplY3QtY291bnQ9IjAiIG1ldGE6cGFnZS1jb3VudD0iMiIgbWV0YTpwYXJhZ3JhcGgtY291bnQ9IjY0IiBtZXRhOndvcmQtY291bnQ9IjMxOSIgbWV0YTpjaGFyYWN0ZXItY291bnQ9IjE0ODMiLz48ZGM6ZGF0ZT4yMDA5LTA4LTE1VDEzOjI0OjAwLjUzPC9kYzpkYXRlPjxkYzpjcmVhdG9yPmNhc2V5IHJheWJhY2s8L2RjOmNyZWF0b3I+PG1ldGE6ZWRpdGluZy1kdXJhdGlvbj5QVDAwSDA2TTQ4UzwvbWV0YTplZGl0aW5nLWR1cmF0aW9uPjxtZXRhOmVkaXRpbmctY3ljbGVzPjE8L21ldGE6ZWRpdGluZy1jeWNsZXM+PG1ldGE6Z2VuZXJhdG9yPk9wZW5PZmZpY2Uub3JnLzMuMSRXaW4zMiBPcGVuT2ZmaWNlLm9yZ19wcm9qZWN0LzMxMG0xMSRCdWlsZC05Mzk5PC9tZXRhOmdlbmVyYXRvcj48L29mZmljZTptZXRhPjwvb2ZmaWNlOmRvY3VtZW50LW1l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";

    @After
    public void setUp() throws Exception {
        SignServerUtil.installBCProvider();
    }

    @Test
    public void test00SetupDatabase() throws Exception {
        addSigner("org.signserver.module.odfsigner.ODFSigner", WORKERID, "TestODFSigner", true);
    }

    @Test
    public void test01SignOdt() throws Exception {
        int reqid = 13;

        GenericSignRequest signRequest = new GenericSignRequest(reqid, Base64.decode(TEST_ODF_DOC.getBytes()));

        GenericSignResponse res = (GenericSignResponse) processSession.process(
                new WorkerIdentifier(WORKERID), signRequest, new RemoteRequestContext());
        byte[] data = res.getProcessedData();

        // Answer to right question
        assertEquals(reqid, res.getRequestID());

        // Output for manual inspection
        File file = new File(getSignServerHome() + File.separator + "tmp"
                + File.separator + "signedTestOdfDoc.odt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }

        // TODO : add validation of document (core validation + reference
        // validaton) when odfvalidator is finished

        // Check certificate
        assertNotNull(res.getSignerCertificate());
    }

    @Test
    public void test02GetStatus() throws Exception {
        StaticWorkerStatus stat = (StaticWorkerStatus) workerSession.getStatus(new WorkerIdentifier(WORKERID));
        assertEquals(stat.getTokenStatus(), WorkerStatus.STATUS_ACTIVE);
    }

    /**
     * Test that INCLUDE_CERTIFICATE_LEVELS is not gives a config error.
     */
    @Test
    public void test03IncludeCertificateLevelsNotSupported() throws Exception {
        try {
            workerSession.setWorkerProperty(WORKERID, WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS, "2");
            workerSession.reloadConfiguration(WORKERID);

            final List<String> errors = workerSession.getStatus(new WorkerIdentifier(WORKERID)).getFatalErrors();

            assertTrue("Should contain error", errors.contains(WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS + " is not supported."));
        } finally {
            workerSession.removeWorkerProperty(WORKERID, WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS);
            workerSession.reloadConfiguration(WORKERID);
        }
    }

    /**
     * Tests that Signer refuses to sign if worker has configuration errors.
     */
    @Test
    public void test04NoSigningWhenWorkerMisconfigued() throws Exception {
        int reqid = 13;

        workerSession.setWorkerProperty(WORKERID, WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS, "2");
        workerSession.reloadConfiguration(WORKERID);

        GenericSignRequest signRequest = new GenericSignRequest(reqid, Base64.decode(TEST_ODF_DOC.getBytes()));
        try {
            processSession.process(new WorkerIdentifier(WORKERID), signRequest, new RemoteRequestContext());
        } catch (SignServerException expected) {
            assertTrue("exception message", expected.getMessage().contains("Worker is misconfigured"));
        }
    }

    @Test
    public void test99TearDownDatabase() throws Exception {
        removeWorker(WORKERID);
    }
}
