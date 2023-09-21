package com.flagsmith.internal

/** Internal interface for objects that track the last time the flags were fetched from the server */
interface FlagsmithEventTimeTracker {
    /** The last time the flags were fetched from the server, as a Unix epoch */
    var lastFlagFetchTime: Double
}
