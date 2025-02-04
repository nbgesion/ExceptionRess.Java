package com.exceptionress.exceptionressclient.plugins.preconfigured;

import com.exceptionress.exceptionressclient.configuration.Configuration;
import com.exceptionress.exceptionressclient.models.Event;
import com.exceptionress.exceptionressclient.models.EventPluginContext;
import com.exceptionress.exceptionressclient.models.RequestInfo;
import com.exceptionress.exceptionressclient.plugins.EventPluginIF;
import com.exceptionress.exceptionressclient.plugins.preconfigured.args.RequestInfoGetArgs;
import com.exceptionress.exceptionressclient.utils.Utils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RequestInfoPlugin implements EventPluginIF {
  private static final Integer DEFAULT_PRIORITY = 70;

  @Builder
  public RequestInfoPlugin() {}

  @Override
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }

  @Override
  public void run(
      EventPluginContext eventPluginContext, Configuration configuration) {
    Event event = eventPluginContext.getEvent();
    if (event.getRequestInfo().isPresent()) {
      return;
    }
    HttpRequest request = eventPluginContext.getContext().getRequest();
    if (request == null) {
      return;
    }
    RequestInfo requestInfo =
        getRequestInfo(
            request,
            RequestInfoGetArgs.builder()
                .exclusions(configuration.getDataExclusions())
                .includeCookies(configuration.getPrivateInformationInclusions().getCookies())
                .includeIpAddress(
                    configuration.getPrivateInformationInclusions().getIpAddress())
                .includePostData(
                    configuration.getPrivateInformationInclusions().getPostData())
                .includeQueryString(
                    configuration.getPrivateInformationInclusions().getQueryString())
                .build());

    if (configuration.getUserAgentBotPatterns().stream()
        .anyMatch(pattern -> Utils.match(requestInfo.getUserAgent(), pattern))) {
      log.info("Cancelling event as the request user agent matches a known bot pattern");
      eventPluginContext.getContext().setEventCancelled(true);
      return;
    }

    event.addRequestInfo(requestInfo);
  }

  private RequestInfo getRequestInfo(HttpRequest request, RequestInfoGetArgs args) {
    RequestInfo.RequestInfoBuilder<?, ?> builder =
        RequestInfo.builder()
            .userAgent(request.headers().firstValue("User-Agent").orElse(null))
            .secure(isSecure(request.uri()))
            .httpMethod(request.method())
            .host(request.uri().getHost())
            .path(request.uri().getPath())
            .port(request.uri().getPort());

    if (args.isIncludeIpAddress()) {
      try {
        InetAddress address = InetAddress.getByName(request.uri().getHost());
        builder.clientIpAddress(address.getHostAddress());
      } catch (UnknownHostException e) {
        log.error(
            String.format("Error while getting ip-address for host: %s", request.uri().getHost()));
      }
    }

    if (args.isIncludeCookies()) {
      builder.cookies(filterExclusions(Utils.getCookies(request), args.getExclusions()));
    }

    if (args.isIncludeQueryString()) {
      builder.queryString(
          filterExclusions(Utils.getQueryParams(request.uri()), args.getExclusions()));
    }

    // todo get post data from request.

    return builder.build();
  }

  private <X> Map<String, X> filterExclusions(Map<String, X> map, Set<String> exclusions) {
    return map.entrySet().stream()
        .filter(
            entry ->
                exclusions.stream().noneMatch(exclusion -> Utils.match(entry.getKey(), exclusion)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private boolean isSecure(URI uri) {
    return uri.getScheme().contains("https");
  }
}
