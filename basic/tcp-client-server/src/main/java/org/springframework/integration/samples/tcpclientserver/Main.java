/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.tcpclientserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.util.TestingUtilities;
import org.springframework.util.SocketUtils;

/**
 * Demonstrates the use of a gateway as an entry point into the integration flow.
 * The message generated by the gateway is sent over tcp by the outbound gateway
 * to the inbound gateway. In turn the inbound gateway sends the message to an
 * echo service and the echoed response comes back over tcp and is returned to
 * the test case for verification.
 *
 * The test uses explicit transformers to convert the byte array payloads to
 * Strings.
 *
 * Several other samples are provided as JUnit test-cases:
 *
 * <ul>
 *     <li>TcpClientServerDemoWithConversionServiceTest</li>
 *     <li>TcpServerConnectionDeserializeTest</li>
 *     <li>TcpServerCustomSerializerTest</li>
 * </ul>
 *
 * @author Gunnar Hillert
 * @author Artem Bilan
 *
 */
public final class Main {

	private static final String AVAILABLE_SERVER_SOCKET = "availableServerSocket";

	/**
	 * Prevent instantiation.
	 */
	private Main() {}

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		final Scanner scanner = new Scanner(System.in);

		System.out.println("\n========================================================="
				+ "\n                                                         "
				+ "\n    Welcome to the Spring Integration                    "
				+ "\n          TCP-Client-Server Sample!                      "
				+ "\n                                                         "
				+ "\n    For more information please visit:                   "
				+ "\n    https://www.springsource.org/spring-integration/                    "
				+ "\n                                                         "
				+ "\n=========================================================");

		final GenericXmlApplicationContext context = Main.setupContext();
		final SimpleGateway gateway = context.getBean(SimpleGateway.class);
		final AbstractServerConnectionFactory crLfServer = context.getBean(AbstractServerConnectionFactory.class);

		System.out.print("Waiting for server to accept connections...");
		TestingUtilities.waitListening(crLfServer, 10000L);
		System.out.println("running.\n\n");

		System.out.println("Please enter some text and press <enter>: ");
		System.out.println("\tNote:");
		System.out.println("\t- Entering FAIL will create an exception");
		System.out.println("\t- Entering q will quit the application");
		System.out.print("\n");
		System.out.println("\t--> Please also check out the other samples, " +
				"that are provided as JUnit tests.");
		System.out.println("\t--> You can also connect to the server on port '" + crLfServer.getPort() + "' using Telnet.\n\n");

		while (true) {

			final String input = scanner.nextLine();

			if ("q".equals(input.trim())) {
				break;
			}
			else {
				final String result = gateway.send(input);
				System.out.println(result);
			}
		}

		System.out.println("Exiting application...bye.");
		System.exit(0);

	}

	public static GenericXmlApplicationContext setupContext() {
		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

		if (System.getProperty(AVAILABLE_SERVER_SOCKET) == null) {
			System.out.print("Detect open server socket...");
			int availableServerSocket = SocketUtils.findAvailableTcpPort(5678);

			final Map<String, Object> sockets = new HashMap<>();
			sockets.put(AVAILABLE_SERVER_SOCKET, availableServerSocket);

			final MapPropertySource propertySource = new MapPropertySource("sockets", sockets);

			context.getEnvironment().getPropertySources().addLast(propertySource);
		}

		System.out.println("using port " + context.getEnvironment().getProperty(AVAILABLE_SERVER_SOCKET));

		context.load("classpath:META-INF/spring/integration/tcpClientServerDemo-context.xml");
		context.registerShutdownHook();
		context.refresh();

		return context;
	}

}
