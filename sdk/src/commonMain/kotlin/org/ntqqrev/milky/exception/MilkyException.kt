package org.ntqqrev.milky.exception

public class MilkyException(
    retcode: Int,
    message: String
) : Exception("API call failed with $retcode: $message")
