/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote.sftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.util.IOHelper;
import org.junit.Test;

public class SftpKeyConsumeTest extends SftpServerTestSupport {

    @Test
    public void testSftpSimpleConsume() throws Exception {
        if (!canTest()) {
            return;
        }

        String expected = "Hello World";

        // create file using regular file
        template.sendBodyAndHeader("file://" + FTP_ROOT_DIR, expected, Exchange.FILE_NAME, "hello.txt");

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedHeaderReceived(Exchange.FILE_NAME, "hello.txt");
        mock.expectedBodiesReceived(expected);

        context.getRouteController().startRoute("foo");

        assertMockEndpointsSatisfied();
    }

    private byte[] getBytesFromFile(String filename) throws IOException {
        InputStream input;
        input = new FileInputStream(new File(filename));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOHelper.copyAndCloseInput(input, output);
        return output.toByteArray();
    }

    @BindToRegistry("privateKey")
    public byte[] addPrivateKey() throws Exception {

        return getBytesFromFile("./src/test/resources/id_rsa");
    }
    
    @BindToRegistry("knownHosts")
    public byte[] addKnownHosts() throws Exception {

        return getBytesFromFile("./src/test/resources/id_rsa");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sftp://localhost:" + getPort() + "/" + FTP_ROOT_DIR
                        + "?username=admin&knownHosts=#knownHosts&privateKey=#privateKey&privateKeyPassphrase=secret&delay=10s&strictHostKeyChecking=yes&disconnect=true")
                    .routeId("foo").noAutoStartup()
                    .to("mock:result");
            }
        };
    }
}
