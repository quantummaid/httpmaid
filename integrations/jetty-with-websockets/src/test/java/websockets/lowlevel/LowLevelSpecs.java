/*
 * Copyright (c) 2019 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package websockets.lowlevel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static websockets.givenwhenthen.Given.given;
import static websockets.givenwhenthen.configurations.lowlevel.LowLevelConfiguration.theLowLevelHttpMaidInstanceWithWebSocketsSupport;

public final class LowLevelSpecs {

    @Disabled
    @Test
    public void testAWebSocketCanConnectToHttpMaid() {
        given(theLowLevelHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Disabled
    @Test
    public void testAWebSocketMessageCanBeHandledByALowLevelHttpMaidHandler() {
        given(theLowLevelHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("foobar")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("foobar");
    }

    @Disabled
    @Test
    public void testMetaDataEntriesFromWebSocketEstablishmentCanBeSavedAndRestoredInWebSocketMessages() {
        given(theLowLevelHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/foobar").withTheHeaders("foo=bar")
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("bar");
    }

    @Disabled
    @Test
    public void testTheLoggerIsAvailableDuringWebSocketMessageProcessing() {
        given(theLowLevelHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/logger").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("qwer")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().theLogOutputStartedWith("qwer");
    }

    /*
        send to closed websocket
        no response on exception
        not defined route is rejected
        exception in handler
        meta data entries can be saved
        close
        send from outside
     */
}
