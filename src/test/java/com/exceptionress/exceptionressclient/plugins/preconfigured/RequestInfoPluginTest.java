package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.TestFixtures;
import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.configuration.PrivateInformationInclusions;
import com.exceptionress.exceptionressclient.enums.EventPropertyKey;
import com.exceptionress.exceptionressclient.enums.ServerSettingKey;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.PluginContext;
import com.exceptionress.exceptionressclient.models.RequestInfo;
import com.exceptionress.exceptionressclient.settings.ServerSettings;
import com.exceptionress.exceptionressclient.storage.InMemoryStorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RequestInfoPluginTest {
  private InMemoryStorageProvider storageProvider;
  private Configuration configuration;
  private RequestInfoPlugin plugin;
  private EventPluginContext context;

  @BeforeEach
  public void setup() {
    plugin = RequestInfoPlugin.builder().build();
    storageProvider = InMemoryStorageProvider.builder().build();
    configuration =
        TestFixtures.aDefaultConfigurationManager().storageProvider(storageProvider).build();
  }

  @Test
  public void itShouldNotDoAnythingIfRequestInfoIsPresentInEvent() {
    RequestInfo requestInfo = RequestInfo.builder().userAgent("test-agent").build();
    context =
        EventPluginContext.from(
            Event.builder().property(EventPropertyKey.REQUEST_INFO.value(), requestInfo).build());

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    assertThat(context.getEvent().getRequestInfo().get()).isSameAs(requestInfo);
  }

  @Test
  public void itShouldNotDoAnythingIfRequestInfoIsNotPresentInEventButAbsentFromContext() {
    context = EventPluginContext.from(Event.builder().build());

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isNotPresent();
  }

  @Test
  public void itShouldCancelEventIfUserAgentIsABotPattern() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:5000/test-path"))
            .header("User-Agent", "test-user-agent")
            .GET()
            .build();
    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();

    storageProvider
        .getSettings()
        .save(
            ServerSettings.builder()
                .settings(
                    Map.of(ServerSettingKey.USER_AGENT_BOT_PATTERNS.value(), "test-user-agent"))
                .build());

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isNotPresent();
    assertThat(context.getContext().isEventCancelled()).isTrue();
  }

  @Test
  public void itShouldAddRequestInfoToEvent() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder().uri(URI.create("http://localhost:5000/test-path")).GET().build();
    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    RequestInfo requestInfo = context.getEvent().getRequestInfo().get();
    assertThat(requestInfo.getUserAgent()).isNull();
    assertThat(requestInfo.isSecure()).isFalse();
    assertThat(requestInfo.getHttpMethod()).isEqualTo("GET");
    assertThat(requestInfo.getHost()).isEqualTo("localhost");
    assertThat(requestInfo.getPath()).isEqualTo("/test-path");
    assertThat(requestInfo.getPort()).isEqualTo(5000);
  }

  @Test
  public void itCanIdentifyASecuredUri() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder().uri(URI.create("https://localhost:5000/test-path")).GET().build();

    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    RequestInfo requestInfo = context.getEvent().getRequestInfo().get();
    assertThat(requestInfo.isSecure()).isTrue();
  }

  @Test
  public void itShouldNotIncludeIpAddressCookiesQueryStringByDefault() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder().uri(URI.create("https://localhost:5000/test-path")).GET().build();

    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    RequestInfo requestInfo = context.getEvent().getRequestInfo().get();
    assertThat(requestInfo.getClientIpAddress()).isNull();
    assertThat(requestInfo.getCookies()).isEmpty();
    assertThat(requestInfo.getQueryString()).isEmpty();
  }

  @Test
  public void itCanGetIpAddressCookiesAndQueryStringFromAHttpRequest() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create("https://localhost:5000/test-path?query-param-key=query-param-value"))
            .header("Cookie", "cookie1=value1;cookie2=value2")
            .GET()
            .build();

    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();

    PrivateInformationInclusions inclusions =
        configuration.getPrivateInformationInclusions();
    inclusions.setIpAddress(true);
    inclusions.setCookies(true);
    inclusions.setQueryString(true);

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    RequestInfo requestInfo = context.getEvent().getRequestInfo().get();
    assertThat(requestInfo.getClientIpAddress()).isNotNull();
    assertThat(requestInfo.getCookies())
        .isEqualTo(Map.of("cookie1", "value1", "cookie2", "value2"));
    assertThat(requestInfo.getQueryString())
        .isEqualTo(Map.of("query-param-key", List.of("query-param-value")));
  }

  @Test
  public void itCanExcludeData() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://localhost:5000/test-path?query-param-key=query-param-value&exclude-query-param=exclude-value"))
            .header("Cookie", "cookie1=value1;cookie2=value2;exclude-cookie=exclude-value")
            .GET()
            .build();

    context =
        EventPluginContext.builder()
            .event(Event.builder().build())
            .context(PluginContext.builder().request(httpRequest).build())
            .build();
    configuration.addDataExclusions("exclude-query-param", "exclude-cookie");
    PrivateInformationInclusions inclusions =
        configuration.getPrivateInformationInclusions();
    inclusions.setIpAddress(true);
    inclusions.setCookies(true);
    inclusions.setQueryString(true);

    plugin.run(context, configuration);

    assertThat(context.getEvent().getRequestInfo()).isPresent();
    RequestInfo requestInfo = context.getEvent().getRequestInfo().get();
    assertThat(requestInfo.getClientIpAddress()).isNotNull();
    assertThat(requestInfo.getCookies())
        .isEqualTo(Map.of("cookie1", "value1", "cookie2", "value2"));
    assertThat(requestInfo.getQueryString())
        .isEqualTo(Map.of("query-param-key", List.of("query-param-value")));
  }
}
