/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
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

package websockets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static websockets.givenwhenthen.Given.*;
import static websockets.givenwhenthen.configurations.artificial.ArtificialConfiguration.theExampleHttpMaidInstanceWithWebSocketsSupport;
import static websockets.givenwhenthen.configurations.chat.ChatConfiguration.theExampleChatServerHttpMaidInstance;

public final class WebSocketsSpecs {

    @Test
    public void testAWebSocketCanConnectToHttpMaid() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testAWebSocketCannotConnectToAPathNotSpecifiedForWebSockets() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/invalid").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    @Disabled
    public void testARequestToANormalRouteStillWorks() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aNormalGETRequestIsIssuedToThePath("/normal").withoutHeaders()
                .then().theResponseBodyWas("{\"stringValue\":\"just a normal response\"}");
    }

    @Test
    public void testAWebSocketCannotConnectToAPathOnlySpecifiedForNormalRequests() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/normal").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    public void testAWebSocketCanConnectToAPathThatIsSpecifiedForNormalAndWebSocketRequests() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/both").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Disabled
    @Test
    public void testANormalRequestBeIssuedToAPathThatIsSpecifiedForNormalAndWebSocketRequests() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aNormalGETRequestIsIssuedToThePath("/both").withoutHeaders()
                .then().theResponseBodyWas("{\"stringValue\":\"this is both\"}");
    }

    @Test
    public void testAWebSocketCannotConnectUnauthorizedIfAuthorizationIsRequired() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/authorized").withoutHeaders()
                .then().theWebSocketConnectionCouldNotBeEstablished();
    }

    @Test
    public void testAWebSocketCanBeAuthenticatedByQueryParameters() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/authorized?username=admin").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Test
    public void testAWebSocketCanBeAuthenticatedByHeaderValues() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/authorized").withTheHeaders("username=admin")
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Disabled
    @Test
    public void testTheFramesOfAWebSocketAreForwardedToAUseCase() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/count").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedUntilTheCounterOfTheCountUseCaseReaches(1)
                .then().theCounterOfTheCountUseCaseWas(1);
    }

    @Disabled
    @Test
    public void testMultipleFramesOfAWebsocketCanBeForwardedToAUseCase() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/count").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedUntilTheCounterOfTheCountUseCaseReaches(3)
                .then().theCounterOfTheCountUseCaseWas(3);
    }

    @Disabled
    @Test
    public void testOneWebSocketCanSendMessagesToMulipleUseCases() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"useCase\": \"A\" }")
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"useCase\": \"B\" }")
                .andWhen().itIsWaitedUntilUseCaseAHasBeenInvoked()
                .andWhen().itIsWaitedUntilUseCaseBHasBeenInvoked()
                .then()
                .useCaseAHasBeenInvoked()
                .useCaseBHasBeenInvoked()
                .useCaseCHasNotBeenInvoked();
    }

    @Disabled
    @Test
    public void testAUseCaseCanRespondViaTheWebSocket() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/query_foo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"foo\"}");
    }

    @Disabled
    @Test
    public void testTheContentOfAFrameCanGetMappedToAUseCaseParameter() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/echo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"echoValue\": \"test\" }")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"test\"}");
    }

    @Test
    public void testAWebSocketCanConnectToAParameterizedPath() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/pre/foo/post").withoutHeaders()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully();
    }

    @Disabled
    @Test
    public void testPathParametersCanBeMappedToUseCaseParameters() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/pre/yxcv/post").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"yxcv\"}");
    }

    @Disabled
    @Test
    public void testQueryParametersCanBeMappedToUseCaseParameters() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/query?var=hooo").withoutHeaders()
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"hooo\"}");
    }

    @Disabled
    @Test
    public void testHeadersCanBeMappedToUseCaseParameters() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/header").withTheHeaders("var=mmm")
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"mmm\"}");
    }

    @Disabled
    @Test
    public void testAWebSocketCanReceiveArbitraryMessagesFromUseCases() {
        given(theExampleChatServerHttpMaidInstance())
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=hallo", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("{\"recipient\":\"elefant\",\"content\":\"hallo\"}");
    }

    @Disabled
    @Test
    public void testAMultiBrowsertabAwareChatServerCanBeImplemented() {
        given(theExampleChatServerHttpMaidInstance())
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=hallo", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyTwoDifferentWebSocketsReceivedTheMessage("{\"content\":\"hallo\",\"recipient\":\"elefant\"}");
    }

    @Disabled
    @Test
    public void testASendingUseCaseCanDistinguishBetweenDifferentGroupsOfWebSockets() {
        given(theExampleChatServerHttpMaidInstance())
                .when().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=elefant")
                .andWhen().aWebSocketIsConnectedToThePath("/subscribe").withTheHeaders("user=ente")
                .andWhen().aNormalGETRequestIsIssuedToThePath("/send").withTheHeaders("user=maus", "content=die ente ist doof", "recipient=elefant")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().allWebSocketConnectionsCouldBeEstablishedSuccessfully()
                .exactlyOneWebSocketReceivedMessage("{\"recipient\":\"elefant\",\"content\":\"die ente ist doof\"}");
    }

    @Test
    public void testTheNumberOfActiveConnectionsCanBeQueried() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1);
    }

    @Disabled
    @Test
    public void testAWebSocketThatGetsClosedByTheClientWillGetCleanedUp() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().allWebSocketsAreClosedOnClientSide()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(0)
                .exactlyOneClientHasBeenClosed();
    }

    @Disabled
    @Test
    public void testAWebSocketThatGetsClosedByTheServerWillGetCleanedUp() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/close").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithEmptyContent()
                .andWhen().itItWaitedForTheClosingOfAWebSocket()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(0)
                .exactlyOneClientHasBeenClosed();
    }

    @Disabled
    @Test
    public void testAnExceptionDuringWebSocketMessageProcessingDoesNotCloseTheWebSocket() {
        given(theExampleHttpMaidInstanceWithWebSocketsSupport())
                .when().aWebSocketIsConnectedToThePath("/exception").withoutHeaders()
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"mode\": \"throw\"}")
                .andWhen().theNumberOfActiveWebSocketsIsQueried()
                .then().theQueriedNumberOfActiveConnectionsWas(1)
                .andWhen().aMessageIsSentWithViaTheMostRecentlyEstablishedWebSocketWithTheContent("{ \"mode\": \"hello\"}")
                .andWhen().itIsWaitedForTheReceptionOfAFrame()
                .then().exactlyOneWebSocketReceivedMessage("{\"stringValue\":\"hello\"}");
    }
}
