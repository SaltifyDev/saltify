package org.ntqqrev.milky

public class MilkyClientConfig(
    public var addressBase: String = "",
    public var eventConnectionType: EventConnectionType = EventConnectionType.WebSocket,
    public var accessToken: String? = null
)