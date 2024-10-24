package com.eventostec.api.exceptions.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Based on RFC7807(<a href="https://datatracker.ietf.org/doc/html/rfc7807">Problem Details for HTTP APIs</a>)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDetails {
    private String title;
    private Integer code;
    private String status;
    private String detail;
    private String instance;
}
