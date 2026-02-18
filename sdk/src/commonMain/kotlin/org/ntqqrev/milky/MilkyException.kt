package org.ntqqrev.milky

public class MilkyException(
    retcode: Int,
    message: String
) : Exception("API call failed with $retcode: $message")