package com.ota.noteexercise.logging


import com.ota.noteexercise.config.BucketService
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

private const val HEADER_X_API_KEY = "X-api-key"

private const val HEADER_X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining"

private const val HEADER_X_RATE_LIMIT_RETRY_AFTER_SECONDS = "X-Rate-Limit-Retry-After-Seconds"

@Component
class ThrottlingFilter (
    private val bucketService: BucketService
) : Filter {

    override fun init(filterConfig: FilterConfig?) {
        // No initialization necessary
    }

    @Throws(Exception::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        if (servletRequest !is HttpServletRequest || servletResponse !is HttpServletResponse) {
            filterChain.doFilter(servletRequest, servletResponse)
            return
        }

        val apiKey = servletRequest.getHeader(HEADER_X_API_KEY) ?: UUID.randomUUID().toString()

        val tokenBucket = bucketService.resolveBucket(apiKey)
        val probe = tokenBucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            servletResponse.addHeader(HEADER_X_RATE_LIMIT_REMAINING, probe.remainingTokens.toString())
            servletResponse.addHeader(HEADER_X_API_KEY, apiKey)
            filterChain.doFilter(servletRequest, servletResponse)
        } else {
            respondWithTooManyRequests(servletResponse, probe.nanosToWaitForRefill)
        }
    }

    override fun destroy() {
        // No resources to clean up
    }

    private fun respondWithBadRequest(response: HttpServletResponse, message: String) {
        response.apply {
            status = HttpStatus.BAD_REQUEST.value()
            contentType = "text/plain"
            writer.append(message)
        }
    }

    private fun respondWithTooManyRequests(response: HttpServletResponse, nanosToWaitForRefill: Long) {
        response.apply {
            status = HttpStatus.TOO_MANY_REQUESTS.value()
            setHeader(HEADER_X_RATE_LIMIT_RETRY_AFTER_SECONDS, TimeUnit.NANOSECONDS.toSeconds(nanosToWaitForRefill).toString())
            contentType = "text/plain"
            writer.append("Too many requests")
        }
    }
}