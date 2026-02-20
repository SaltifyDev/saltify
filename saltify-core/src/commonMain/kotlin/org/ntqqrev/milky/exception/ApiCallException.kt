package org.ntqqrev.milky.exception

import kotlinx.io.IOException

public class ApiCallException(
    retcode: Int,
    message: String
) : IOException("API call failed with $retcode: $message"), MilkyException
