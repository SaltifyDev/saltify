package org.ntqqrev.milky

class MilkyException(
    retcode: Int,
    message: String
) : Exception("API call failed with $retcode: $message")