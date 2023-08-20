package com.szekai.pong;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.style.ToStringCreator;

@AllArgsConstructor
@Getter
@Setter
class PingValue {
    String value;

    public PingValue() {
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("value", value)
                .toString();

    }
}
