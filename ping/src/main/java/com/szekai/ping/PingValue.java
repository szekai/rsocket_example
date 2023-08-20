package com.szekai.ping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.style.ToStringCreator;

@AllArgsConstructor
@Getter
class PingValue {
    final String value;

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("value", value)
                .toString();

    }
}
