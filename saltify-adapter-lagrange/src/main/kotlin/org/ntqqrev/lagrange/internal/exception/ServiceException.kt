package org.ntqqrev.lagrange.internal.exception

class ServiceException(
    cmd: String,
    retCode: Int,
    extra: String
) : Exception("Service ($cmd) call failed with code $retCode: $extra")