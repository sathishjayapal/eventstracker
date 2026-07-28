package me.sathish.event_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void honorsIncomingCorrelationIdHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "upstream-id-123");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final AtomicReference<String> mdcDuringRequest = new AtomicReference<>();

        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(final jakarta.servlet.ServletRequest req, final jakarta.servlet.ServletResponse res) {
                mdcDuringRequest.set(MDC.get(CorrelationIdFilter.MDC_KEY));
            }
        });

        assertThat(mdcDuringRequest.get()).isEqualTo("upstream-id-123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("upstream-id-123");
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final AtomicReference<String> mdcDuringRequest = new AtomicReference<>();

        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(final jakarta.servlet.ServletRequest req, final jakarta.servlet.ServletResponse res) {
                mdcDuringRequest.set(MDC.get(CorrelationIdFilter.MDC_KEY));
            }
        });

        final String echoed = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(echoed).isNotBlank();
        assertThat(UUID.fromString(echoed)).isNotNull();
        assertThat(mdcDuringRequest.get()).isEqualTo(echoed);
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
