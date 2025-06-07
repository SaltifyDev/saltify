package org.ntqqrev.lagrange.internal.exception

class OidbException(
    oidbCmd: Int,
    oidbSubCmd: Int,
    retCode: Int,
    extra: String
) : Exception("Oidb (0x${oidbCmd.toString(16)}_$oidbSubCmd) call failed with code $retCode: $extra")